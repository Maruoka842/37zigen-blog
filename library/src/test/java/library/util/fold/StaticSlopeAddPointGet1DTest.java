package library.util.fold;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class StaticSlopeAddPointGet1DTest {

    @Test
    public void testAddConstant() {
        int N = 10;
        StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
        orig.addConstant(2, 8, 5);
        orig.build();

        long[] expected = {0, 0, 5, 5, 5, 5, 5, 5, 0, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], orig.get(i));
        }
    }

    @Test
    public void testAddMountain() {
        int N = 6;
        StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
        orig.addMountain(0, 5, 4, 2);
        orig.build();

        long[] expected = {0, 2, 4, 4, 2, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], orig.get(i));
        }
    }

    @Test
    public void testAddMountainWithFlatPeak() {
        int N = 6;
        StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
        orig.addMountain(0, 5, 2, 3);
        orig.build();

        // Expected: min(x, 5-x, 2) * 3
        // x=0: 0, x=1: 3, x=2: 6, x=3: 6, x=4: 3, x=5: 0
        long[] expected = {0, 3, 6, 6, 3, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], orig.get(i));
        }
    }

    @Test
    public void testAddMountainRandom() {
        Random rnd = new Random(123);
        int N = 50;

        for (int iter = 0; iter < 200; iter++) {
            StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);

            int l = rnd.nextInt(N);
            int r = l + rnd.nextInt(N - l);
            int a = 1 + rnd.nextInt(N);
            long scale = rnd.nextInt(20) - 10;

            orig.addMountain(l, r, a, scale);
            orig.build();

            long[] expected = new long[N];
            for (int x = l; x <= r; x++) {
                expected[x] += Math.min(Math.min(x - l, r - x), a) * scale;
            }

            for (int i = 0; i < N; i++) {
                assertEquals(expected[i], orig.get(i), "Mismatch at iteration " + iter + " index " + i);
            }
        }
    }
}
