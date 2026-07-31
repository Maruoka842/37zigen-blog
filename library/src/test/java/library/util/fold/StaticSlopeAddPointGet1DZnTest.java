package library.util.fold;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class StaticSlopeAddPointGet1DZnTest {

    @Test
    public void testAgainstOriginalNoWrap() {
        // When mod is large enough, results should match the original 1D class exactly.
        int N = 50;
        long mod = 1_000_000_007L;
        Random rnd = new Random(42);

        for (int iter = 0; iter < 50; iter++) {
            StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
            StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);

            int opsCount = 15;
            for (int op = 0; op < opsCount; op++) {
                int type = rnd.nextInt(5);
                long val = rnd.nextInt(20) - 10;
                long a = rnd.nextInt(10) - 5;
                long b = rnd.nextInt(20) - 10;

                switch (type) {
                    case 0: { // addSlope
                        int l = rnd.nextInt(N);
                        int r = l + rnd.nextInt(N - l + 1);
                        orig.addSlope(l, r, a, b);
                        zn.addSlope(l, r, a, b);
                        break;
                    }
                    case 1: { // addDistanceFrom
                        int center = rnd.nextInt(N - 1);
                        orig.addDistanceFrom(val, center);
                        zn.addDistanceFrom(val, center);
                        break;
                    }
                    case 2: { // addCircularDistanceFrom
                        int center = rnd.nextInt(N);
                        orig.addCircularDistanceFrom(a, center);
                        zn.addCircularDistanceFrom(a, center);
                        break;
                    }
                    case 3: { // addRightRamp
                        int pos = rnd.nextInt(N - 1);
                        orig.addRightRamp(a, pos);
                        zn.addRightRamp(a, pos);
                        break;
                    }
                    case 4: { // addLeftRamp
                        int pos = rnd.nextInt(N - 1);
                        orig.addLeftRamp(a, pos);
                        zn.addLeftRamp(a, pos);
                        break;
                    }
                }
            }

            orig.build();
            zn.build();

            for (int i = 0; i < N; i++) {
                long expected = (orig.get(i) % mod + mod) % mod;
                assertEquals(expected, zn.get(i), "Mismatch at iteration " + iter + ", index " + i);
            }
        }
    }

    @Test
    public void testWithSmallMod() {
        // Under small mod, we verify manually computed expected values using a slow simulator.
        int N = 10;
        long mod = 7;
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);

        // [2, 7) + 3(x-2) + 4
        // x=2: 4, x=3: 7=0, x=4: 10=3, x=5: 13=6, x=6: 16=2
        zn.addSlope(2, 7, 3, 4);

        // Right ramp at 5 with slope 2
        // x=6: 2*(1)=2, x=7: 2*(2)=4, x=8: 2*(3)=6, x=9: 2*(4)=8=1
        zn.addRightRamp(2, 5);

        // Left ramp at 4 with slope 1
        // x=4: 0, x=3: 1, x=2: 2, x=1: 3, x=0: 4
        zn.addLeftRamp(1, 4);

        zn.build();

        long[] expected = new long[N];
        // Slope term:
        for (int i = 2; i < 7; i++) {
            expected[i] += 3 * (i - 2) + 4;
        }
        // Right ramp:
        for (int i = 0; i < N; i++) {
            expected[i] += 2 * Math.max(0, i - 5);
        }
        // Left ramp:
        for (int i = 0; i < N; i++) {
            expected[i] += 1 * Math.max(0, -i + 4);
        }

        for (int i = 0; i < N; i++) {
            long expectedMod = (expected[i] % mod + mod) % mod;
            assertEquals(expectedMod, zn.get(i), "Mismatch at index " + i);
        }
    }

    @Test
    public void testCircularPositiveAndNegativeNoWrap() {
        int N = 50;
        long mod = 1_000_000_007L;
        Random rnd = new Random(12345);

        for (int iter = 0; iter < 50; iter++) {
            StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
            StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);

            int opsCount = 10;
            for (int op = 0; op < opsCount; op++) {
                int type = rnd.nextInt(2);
                long a = rnd.nextInt(10) - 5;
                int center = rnd.nextInt(N);

                if (type == 0) {
                    orig.addCircularDistanceFromPositive(a, center);
                    zn.addCircularDistanceFromPositive(a, center);
                } else {
                    orig.addCircularDistanceFromNegative(a, center);
                    zn.addCircularDistanceFromNegative(a, center);
                }
            }

            orig.build();
            zn.build();

            for (int i = 0; i < N; i++) {
                long expected = (orig.get(i) % mod + mod) % mod;
                assertEquals(expected, zn.get(i), "Mismatch at iteration " + iter + ", index " + i);
            }
        }
    }

    @Test
    public void testBuildLock() {
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(10, 7);
        zn.addSlope(0, 5, 1, 1);
        zn.build();

        // Attempts to modify after build must throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> zn.addSlope(1, 2, 1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addDistanceFrom(1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addCircularDistanceFrom(1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addCircularDistanceFromPositive(1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addCircularDistanceFromNegative(1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addRightRamp(1, 1));
        assertThrows(IllegalStateException.class, () -> zn.addLeftRamp(1, 1));

        // Subsequent build call should throw AssertionError
        assertThrows(AssertionError.class, () -> zn.build());
    }

    @Test
    public void testEmptyStructure() {
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(10, 7);
        zn.build();
        for (int i = 0; i < 10; i++) {
            assertEquals(0, zn.get(i));
        }
    }

    @Test
    public void testInvalidMod() {
        assertThrows(IllegalArgumentException.class, () -> new StaticSlopeAddPointGet1DZn(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new StaticSlopeAddPointGet1DZn(10, -5));
    }

    @Test
    public void testAddConstant() {
        int N = 10;
        long mod = 13;
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);
        zn.addConstant(2, 8, 5);
        zn.build();

        long[] expected = {0, 0, 5, 5, 5, 5, 5, 5, 0, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], zn.get(i));
        }
    }

    @Test
    public void testAddMountain() {
        int N = 6;
        long mod = 17;
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);
        zn.addMountain(0, 5, 4, 2);
        zn.build();

        // Expected: min(x, 5-x, 4)*2
        // x=0: min(0, 5, 4)*2 = 0
        // x=1: min(1, 4, 4)*2 = 2
        // x=2: min(2, 3, 4)*2 = 4
        // x=3: min(3, 2, 4)*2 = 4
        // x=4: min(4, 1, 4)*2 = 2
        // x=5: min(5, 0, 4)*2 = 0
        long[] expected = {0, 2, 4, 4, 2, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], zn.get(i));
        }
    }

    @Test
    public void testAddMountainWithFlatPeak() {
        int N = 5;
        long mod = 11;
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);
        zn.addMountain(0, 4, 2, 3);
        zn.build();

        // Expected: min(x, 4-x, 2)*3
        // x=0: 0, x=1: 3, x=2: 6, x=3: 3, x=4: 0
        long[] expected = {0, 3, 6, 3, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], zn.get(i));
        }
    }

    @Test
    public void testAddMountainWrapAround() {
        int N = 5;
        long mod = 5;
        StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);
        zn.addMountain(0, 4, 3, 2); // scale is 2
        zn.build();

        // Expected: min(x, 4-x, 3) * 2
        // x=0: 0
        // x=1: 2
        // x=2: 4
        // x=3: 2
        // x=4: 0
        long[] expected = {0, 2, 4, 2, 0};
        for (int i = 0; i < N; i++) {
            assertEquals(expected[i], zn.get(i));
        }
    }

    @Test
    public void testAddMountainRandom() {
        Random rnd = new Random(12345);
        int N = 50;

        for (int iter = 0; iter < 200; iter++) {
            long mod = 3 + rnd.nextInt(100);
            StaticSlopeAddPointGet1DZn zn = new StaticSlopeAddPointGet1DZn(N, mod);

            int l = rnd.nextInt(N);
            int r = l + rnd.nextInt(N - l);
            int a = 1 + rnd.nextInt(N);
            long scale = rnd.nextInt(200) - 100;

            zn.addMountain(l, r, a, scale);
            zn.build();

            long[] expected = new long[N];
            for (int x = l; x <= r; x++) {
                long term = Math.min(Math.min(x - l, r - x), a) * scale;
                expected[x] = (expected[x] + term) % mod;
                if (expected[x] < 0) expected[x] += mod;
            }

            for (int i = 0; i < N; i++) {
                assertEquals(expected[i], zn.get(i), "Mismatch at iteration " + iter + " index " + i + " under mod " + mod);
            }
        }
    }
}
