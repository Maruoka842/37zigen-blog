package library.util.segtree;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;

public class LiChaoTreeTest {

    @Test
    public void testBasic() {
        long[] xs = {0, 1, 2, 3, 4};
        LiChaoTree lct = new LiChaoTree(xs);
        lct.addLine(1, 0); // y = x
        lct.addLine(0, 2); // y = 2

        assertEquals(0, lct.get(0).minVal());
        assertEquals(1, lct.get(1).minVal());
        assertEquals(2, lct.get(2).minVal());
        assertEquals(2, lct.get(3).minVal());
        assertEquals(2, lct.get(4).minVal());
    }

    @Test
    public void testSegment() {
        long[] xs = {0, 1, 2, 3, 4};
        LiChaoTree lct = new LiChaoTree(xs);
        lct.addSegment(1, 4, -1, 4); // y = -x + 4 for x in [1, 4)

        assertFalse(lct.get(0).isValid());
        assertEquals(3, lct.get(1).minVal());
        assertEquals(2, lct.get(2).minVal());
        assertEquals(1, lct.get(3).minVal());
        assertFalse(lct.get(4).isValid());
    }

    @Test
    public void testStress() {
        Random rnd = new Random(42);
        int n = 100;
        long[] xs = new long[n];
        for (int i = 0; i < n; i++) xs[i] = rnd.nextInt(1000);
        Arrays.sort(xs);
        // unique
        int m = 1;
        for (int i = 1; i < n; i++) if (xs[i] != xs[i-1]) xs[m++] = xs[i];
        xs = Arrays.copyOf(xs, m);

        LiChaoTree lct = new LiChaoTree(xs);

        int numLines = 200;
        long[] lineA = new long[numLines];
        long[] lineB = new long[numLines];
        long[] lineL = new long[numLines];
        long[] lineR = new long[numLines];

        for (int i = 0; i < numLines; i++) {
            lineA[i] = rnd.nextInt(2000) - 1000;
            lineB[i] = rnd.nextLong(2000000) - 1000000;
            int lIdx = rnd.nextInt(xs.length);
            int rIdx = rnd.nextInt(xs.length + 1);
            if (lIdx >= rIdx) {
                // Add as line
                lct.addLine(lineA[i], lineB[i], i);
                lineL[i] = Long.MIN_VALUE;
                lineR[i] = Long.MAX_VALUE;
            } else {
                // Add as segment
                lineL[i] = xs[lIdx];
                lineR[i] = rIdx == xs.length ? xs[xs.length-1] + 1 : xs[rIdx];
                lct.addSegment(lineL[i], lineR[i], lineA[i], lineB[i], i);
            }
        }

        for (long x : xs) {
            long expected = Long.MAX_VALUE;
            boolean valid = false;
            for (int i = 0; i < numLines; i++) {
                if (x >= lineL[i] && x < lineR[i]) {
                    long val = lineA[i] * x + lineB[i];
                    if (!valid || val < expected) {
                        expected = val;
                        valid = true;
                    }
                }
            }
            LiChaoTree.Result res = lct.get(x);
            assertEquals(valid, res.isValid());
            if (valid) {
                assertEquals(expected, res.minVal());
            }
        }
    }
}
