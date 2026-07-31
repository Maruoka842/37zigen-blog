package library.util.fold;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class StaticSlopeAddPointGetLarge1DTest {

    @Test
    public void testAgainstOriginalSmallRange() {
        int N = 50;
        Random rnd = new Random(42);

        for (int iter = 0; iter < 100; iter++) {
            StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
            StaticSlopeAddPointGetLarge1D large = new StaticSlopeAddPointGetLarge1D();

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
                        large.addSlope(l, r, a, b);
                        break;
                    }
                    case 1: { // addDistanceFrom
                        // original addDistanceFrom has b1[center+1], so center must be < N-1
                        int center = rnd.nextInt(N - 1);
                        orig.addDistanceFrom(val, center);
                        large.addDistanceFrom(val, center);
                        break;
                    }
                    case 2: { // addCircularDistanceFrom
                        int center = rnd.nextInt(N);
                        orig.addCircularDistanceFrom(a, center);
                        large.addCircularDistanceFrom(a, center, N);
                        break;
                    }
                    case 3: { // addRightRamp
                        // original addRightRamp has b1[b+1], so b must be < N-1
                        int pos = rnd.nextInt(N - 1);
                        orig.addRightRamp(a, pos);
                        large.addRightRamp(a, pos);
                        break;
                    }
                    case 4: { // addLeftRamp
                        // original addLeftRamp has b1[b+1], so b must be < N-1
                        int pos = rnd.nextInt(N - 1);
                        orig.addLeftRamp(a, pos);
                        large.addLeftRamp(a, pos);
                        break;
                    }
                }
            }

            orig.build();
            large.build();

            for (int i = 0; i < N; i++) {
                assertEquals(orig.get(i), large.get(i), "Mismatch at iteration " + iter + ", index " + i);
            }
        }
    }

    @Test
    public void testCircularPositiveAndNegative() {
        int N = 50;
        Random rnd = new Random(12345);

        for (int iter = 0; iter < 100; iter++) {
            StaticSlopeAddPointGet1D orig = new StaticSlopeAddPointGet1D(N);
            StaticSlopeAddPointGetLarge1D large = new StaticSlopeAddPointGetLarge1D();

            int opsCount = 10;
            for (int op = 0; op < opsCount; op++) {
                int type = rnd.nextInt(2);
                long a = rnd.nextInt(10) - 5;
                int center = rnd.nextInt(N);

                if (type == 0) {
                    orig.addCircularDistanceFromPositive((int) a, center);
                    large.addCircularDistanceFromPositive(a, center, N);
                } else {
                    orig.addCircularDistanceFromNegative((int) a, center);
                    large.addCircularDistanceFromNegative(a, center, N);
                }
            }

            orig.build();
            large.build();

            for (int i = 0; i < N; i++) {
                assertEquals(orig.get(i), large.get(i), "Mismatch at iteration " + iter + ", index " + i);
            }
        }
    }

    @Test
    public void testCircularPositiveAndNegativeManual() {
        int N = 5;
        long a = 3;
        int center = 2;

        // Positive: distance = (i - 2) mod 5
        // i=0: 3, i=1: 4, i=2: 0, i=3: 1, i=4: 2
        // Values: i=0: 9, i=1: 12, i=2: 0, i=3: 3, i=4: 6
        StaticSlopeAddPointGet1D ds1 = new StaticSlopeAddPointGet1D(N);
        StaticSlopeAddPointGetLarge1D dsLarge1 = new StaticSlopeAddPointGetLarge1D();
        ds1.addCircularDistanceFromPositive(a, center);
        dsLarge1.addCircularDistanceFromPositive(a, center, N);

        ds1.build();
        dsLarge1.build();

        long[] expectedPos = {9, 12, 0, 3, 6};
        for (int i = 0; i < N; i++) {
            assertEquals(expectedPos[i], ds1.get(i));
            assertEquals(expectedPos[i], dsLarge1.get(i));
        }

        // Negative: distance = (2 - i) mod 5
        // i=0: 2, i=1: 1, i=2: 0, i=3: 4, i=4: 3
        // Values: i=0: 6, i=1: 3, i=2: 0, i=3: 12, i=4: 9
        StaticSlopeAddPointGet1D ds2 = new StaticSlopeAddPointGet1D(N);
        StaticSlopeAddPointGetLarge1D dsLarge2 = new StaticSlopeAddPointGetLarge1D();
        ds2.addCircularDistanceFromNegative(a, center);
        dsLarge2.addCircularDistanceFromNegative(a, center, N);

        ds2.build();
        dsLarge2.build();

        long[] expectedNeg = {6, 3, 0, 12, 9};
        for (int i = 0; i < N; i++) {
            assertEquals(expectedNeg[i], ds2.get(i));
            assertEquals(expectedNeg[i], dsLarge2.get(i));
        }
    }

    @Test
    public void testExtremelyLargeCoordinates() {
        StaticSlopeAddPointGetLarge1D ds = new StaticSlopeAddPointGetLarge1D();

        // Add slope over a huge interval: [1000000000000L, 2000000000000L)
        // a = 3, b = 5
        long l = 1_000_000_000_000L;
        long r = 2_000_000_000_000L;
        ds.addSlope(l, r, 3, 5);

        // Add a right ramp at 1500000000000L with a = 2
        ds.addRightRamp(2, 1_500_000_000_000L);

        ds.build();

        // Before interval [0, l)
        assertEquals(0, ds.get(0));
        assertEquals(0, ds.get(l - 1));

        // Inside [l, r) before the right ramp (e.g. x = l + 100)
        // Value should be: a*(x - l) + b = 3*(100) + 5 = 305
        assertEquals(5, ds.get(l));
        assertEquals(305, ds.get(l + 100));

        // After the right ramp (x = 1_500_000_000_000L + 100)
        // Value should be: 3*(500_000_000_100) + 5 + 2*(100) = 1_500_000_000_305 + 200 = 1_500_000_000_505
        long x = 1_500_000_000_100L;
        long expected = 3 * (x - l) + 5 + 2 * (x - 1_500_000_000_000L);
        assertEquals(expected, ds.get(x));

        // After r (x >= r)
        // Outside the slope interval, but right ramp is still active!
        // Right ramp adds: 2 * (x - 1_500_000_000_000)
        long x_after = 2_500_000_000_000L;
        long expected_after = 2 * (x_after - 1_500_000_000_000L);
        assertEquals(expected_after, ds.get(x_after));
    }

    @Test
    public void testNegativeCoordinates() {
        StaticSlopeAddPointGetLarge1D ds = new StaticSlopeAddPointGetLarge1D();

        // Add slope starting from negative coordinates
        ds.addSlope(-500_000_000_000L, 500_000_000_000L, 10, 100);
        ds.build();

        // Before interval
        assertEquals(0, ds.get(-600_000_000_000L));

        // At interval start
        assertEquals(100, ds.get(-500_000_000_000L));

        // At 0
        assertEquals(100 + 10 * 500_000_000_000L, ds.get(0));
    }

    @Test
    public void testEmptyStructure() {
        StaticSlopeAddPointGetLarge1D ds = new StaticSlopeAddPointGetLarge1D();
        // Should not throw and return 0
        assertEquals(0, ds.get(100));
        assertEquals(0, ds.get(-100));
    }

    @Test
    public void testBuildLock() {
        StaticSlopeAddPointGetLarge1D ds = new StaticSlopeAddPointGetLarge1D();
        ds.addSlope(0, 10, 1, 1);
        ds.build();

        // Any attempt to modify should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> ds.addSlope(1, 2, 1, 1));
        assertThrows(IllegalStateException.class, () -> ds.addRightRamp(1, 1));

        // Subsequent build call should throw AssertionError
        assertThrows(AssertionError.class, () -> ds.build());
    }

    @Test
    public void testInfiniteLeftSupport() {
        StaticSlopeAddPointGetLarge1D ds1 = new StaticSlopeAddPointGetLarge1D();
        ds1.addLeftRamp(5, 10);
        ds1.build();

        // x < min(keys) (here min(keys) is 11)
        assertEquals(550, ds1.get(-100));
        assertEquals(50, ds1.get(0));
        assertEquals(25, ds1.get(5));
        assertEquals(5, ds1.get(9));
        assertEquals(0, ds1.get(10));
        // x >= min(keys)
        assertEquals(0, ds1.get(11));
        assertEquals(0, ds1.get(100));

        StaticSlopeAddPointGetLarge1D ds2 = new StaticSlopeAddPointGetLarge1D();
        ds2.addDistanceFrom(3, 5);
        ds2.build();

        // x < min(keys) (here min(keys) is 6)
        assertEquals(315, ds2.get(-100));
        assertEquals(15, ds2.get(0));
        assertEquals(6, ds2.get(3));
        assertEquals(0, ds2.get(5));
        // x >= min(keys)
        assertEquals(3, ds2.get(6));
        assertEquals(15, ds2.get(10));
        assertEquals(285, ds2.get(100));
    }

    @Test
    public void testCircularMethodsDoNotAffectOutOfBounds() {
        int N = 10;
        long a = 2;
        int center = 3;

        // Test StaticSlopeAddPointGet1D
        StaticSlopeAddPointGet1D d1 = new StaticSlopeAddPointGet1D(N);
        d1.addCircularDistanceFrom(a, center);
        d1.build();

        // Ensure we can retrieve values in [0, N)
        for (int i = 0; i < N; i++) {
            long expected = a * Math.min(Math.abs(i - center), N - Math.abs(i - center));
            assertEquals(expected, d1.get(i));
        }

        // Test StaticSlopeAddPointGetLarge1D
        StaticSlopeAddPointGetLarge1D dLarge = new StaticSlopeAddPointGetLarge1D();
        dLarge.addCircularDistanceFrom(a, center, N);
        dLarge.build();

        // Values in [0, N) must match
        for (int i = 0; i < N; i++) {
            long expected = a * Math.min(Math.abs(i - center), N - Math.abs(i - center));
            assertEquals(expected, dLarge.get(i));
        }

        // Outside [0, N) should be 0 because we restricted slope additions using addSlopeTruncated
        assertEquals(0, dLarge.get(-1));
        assertEquals(0, dLarge.get(-100));
        assertEquals(0, dLarge.get(N));
        assertEquals(0, dLarge.get(N + 100));

        // Test Positive & Negative Circular additions for Large1D outside [0, N)
        StaticSlopeAddPointGetLarge1D dLargePos = new StaticSlopeAddPointGetLarge1D();
        dLargePos.addCircularDistanceFromPositive(a, center, N);
        dLargePos.build();

        assertEquals(0, dLargePos.get(-1));
        assertEquals(0, dLargePos.get(N));

        StaticSlopeAddPointGetLarge1D dLargeNeg = new StaticSlopeAddPointGetLarge1D();
        dLargeNeg.addCircularDistanceFromNegative(a, center, N);
        dLargeNeg.build();

        assertEquals(0, dLargeNeg.get(-1));
        assertEquals(0, dLargeNeg.get(N));
    }
}
