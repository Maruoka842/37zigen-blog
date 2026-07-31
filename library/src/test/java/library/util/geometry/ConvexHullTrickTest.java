package library.util.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class ConvexHullTrickTest {

    @Test
    public void testSingleLine() {
        MonotonicConvexHullTrick cht = new MonotonicConvexHullTrick();
        cht.add(2, 3); // y = 2x + 3
        assertEquals(3, cht.min(0));
        assertEquals(5, cht.min(1));
        assertEquals(1, cht.min(-1));
        assertEquals(2003, cht.min(1000));
        assertEquals(1, cht.size());
    }

    @Test
    public void testMultipleLines() {
        MonotonicConvexHullTrick cht = new MonotonicConvexHullTrick();
        // y = x + 1
        // y = 2x
        cht.add(1, 1);
        cht.add(2, 0);

        // At x = 0: y = 1 vs y = 0 => min is 0 (y = 2x)
        // At x = 1: y = 2 vs y = 2 => min is 2
        // At x = 2: y = 3 vs y = 4 => min is 3 (y = x + 1)
        assertEquals(0, cht.min(0));
        assertEquals(2, cht.min(1));
        assertEquals(3, cht.min(2));
        assertEquals(-2, cht.min(-1)); // y = 2x => -2
    }

    @Test
    public void testParallelLines() {
        MonotonicConvexHullTrick cht = new MonotonicConvexHullTrick();
        // Same slope, different intercepts
        cht.add(2, 5);
        cht.add(2, 3); // should replace y = 2x + 5
        assertEquals(1, cht.size());
        assertEquals(3, cht.min(0));
        assertEquals(5, cht.min(1));

        // Same slope, larger intercept added later
        cht.add(2, 10); // should be ignored
        assertEquals(1, cht.size());
        assertEquals(3, cht.min(0));
    }

    @Test
    public void testRedundancyPruning() {
        MonotonicConvexHullTrick cht = new MonotonicConvexHullTrick();
        // Adding y = x, y = 2x - 2, y = 3x - 5
        cht.add(1, 0);
        cht.add(2, -2);
        assertEquals(2, cht.size());

        cht.add(3, -5); // This should prune y = 2x - 2
        assertEquals(2, cht.size()); // y = x and y = 3x - 5

        // Query values:
        // x = 1: y=x => 1, y=3x-5 => -2. Min: -2
        // x = 2: y=x => 2, y=3x-5 => 1. Min: 1
        // x = 3: y=x => 3, y=3x-5 => 4. Min: 3
        assertEquals(-2, cht.min(1));
        assertEquals(1, cht.min(2));
        assertEquals(3, cht.min(3));
    }

    @Test
    public void testRandomizedStressTest() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            MonotonicConvexHullTrick cht = new MonotonicConvexHullTrick();
            ArrayList<MonotonicConvexHullTrick.Line> allLines = new ArrayList<>();

            int n = rnd.nextInt(200) + 5;
            long currSlope = -1000;
            for (int i = 0; i < n; i++) {
                currSlope += rnd.nextInt(5); // monotonically increasing
                long intercept = rnd.nextLong() % 100000;
                cht.add(currSlope, intercept);
                allLines.add(new MonotonicConvexHullTrick.Line(currSlope, intercept));
            }

            // Query many x points and compare with brute force
            for (int q = 0; q < 200; q++) {
                long x = rnd.nextInt(20000) - 10000;
                long expected = Long.MAX_VALUE;
                for (MonotonicConvexHullTrick.Line line : allLines) {
                    expected = Math.min(expected, line.a * x + line.b);
                }
                long actual = cht.min(x);
                assertEquals(expected, actual, "Mismatch at x = " + x);
            }
        }
    }

    @Test
    public void testSlopeMonotonicityGuard() {
        var cht = new MonotonicConvexHullTrick();
        cht.add(10, 5);
        cht.add(10, 2); // Same slope is allowed (either replaced or ignored depending on y-intercept)
        cht.add(15, 3); // Strictly increasing slope is allowed

        // Decreasing slope should trigger IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            cht.add(14, 100);
        });
    }

    @Test
    public void testDrawWithSingleLine() {
        var cht = new MonotonicConvexHullTrick();
        cht.add(2, 3);
        assertNotNull(cht.toChart());
        // Verify draw() runs without throwing exceptions
        // (This tests drawing setup, but Swing display won't block in non-GUI environment)
        System.setProperty("java.awt.headless", "true");
        try {
            cht.draw();
        } catch (java.awt.HeadlessException e) {
            // HeadlessException is expected and fine when we run draw() in headless mode
        }
    }

    @Test
    public void testDrawWithMultipleLines() {
        var cht = new MonotonicConvexHullTrick();
        cht.add(1, 10);
        cht.add(3, 5);
        cht.add(5, 0);
        assertNotNull(cht.toChart());
        System.setProperty("java.awt.headless", "true");
        try {
            cht.draw();
        } catch (java.awt.HeadlessException e) {
            // HeadlessException is expected and fine in headless mode
        }
    }
}
