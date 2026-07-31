package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

public class MathUtilsTest {

    @Test
    public void testHighlyCompositeNumberSmall() {
        assertEquals(1, MathUtils.highlyCompositeNumber(1));
        assertEquals(2, MathUtils.highlyCompositeNumber(2));
        assertEquals(2, MathUtils.highlyCompositeNumber(3));
        assertEquals(4, MathUtils.highlyCompositeNumber(4));
        assertEquals(4, MathUtils.highlyCompositeNumber(5));
        assertEquals(6, MathUtils.highlyCompositeNumber(6));
        assertEquals(6, MathUtils.highlyCompositeNumber(11));
        assertEquals(12, MathUtils.highlyCompositeNumber(12));
        assertEquals(12, MathUtils.highlyCompositeNumber(23));
        assertEquals(24, MathUtils.highlyCompositeNumber(24));
        assertEquals(48, MathUtils.highlyCompositeNumber(59));
        assertEquals(60, MathUtils.highlyCompositeNumber(60));
    }

    @Test
    public void testHighlyCompositeNumberAgainstList() {
        long[] list = {
            1, 2, 4, 6, 12, 24, 36, 48, 60, 120, 180, 240, 360, 720, 840, 1260, 1680, 2520, 5040, 7560, 10080,
            15120, 20160, 25200, 27720, 45360, 50400, 55440, 83160, 110880, 166320, 221760, 277200, 332640,
            498960, 554400, 665280, 720720, 1081080, 1441440, 2162160, 2882880, 3603600, 4324320, 6486480,
            7207200, 8648640, 10810800, 14414400, 17297280, 21621600, 32432400, 36756720, 43243200, 61261200,
            73513440, 110270160, 122522400, 147026880, 183783600, 245044800, 294053760, 367567200, 551350800,
            698377680, 735134400, 1102701600, 1396755360, 2095133040L, 2205403200L, 2327925600L, 2793510720L,
            3491888400L, 4655851200L, 5587021440L, 6983776800L, 10475665200L, 13967553600L, 20951330400L, 27935107200L,
            41902660800L, 48886437600L, 64250746560L, 73329656400L, 80313433200L, 97772875200L, 128501493120L,
            146659312800L, 160626866400L, 240940299600L, 293318625600L, 321253732800L, 481880599200L, 642507465600L,
            963761198400L, 1124388064800L, 1606268664000L, 1686582097200L, 1927522396800L, 2248776129600L,
            3212537328000L, 3373164194400L, 4497552259200L, 6746328388800L, 8995104518400L, 9316358251200L,
            13492656777600L, 18632716502400L, 26985313555200L, 27949074753600L, 32607253879200L, 46581791256000L,
            48910880818800L, 55898149507200L, 65214507758400L, 93163582512000L, 97821761637600L, 130429015516800L,
            195643523275200L, 260858031033600L, 288807105787200L, 391287046550400L, 577614211574400L, 782574093100800L,
            866421317361600L, 1010824870255200L, 1444035528936000L, 1516237305382800L, 1732842634723200L,
            2021649740510400L, 2888071057872000L, 3032474610765600L, 4043299481020800L, 6064949221531200L,
            8086598962041600L, 10108248702552000L, 12129898443062400L, 18194847664593600L, 20216497405104000L,
            24259796886124800L, 30324746107656000L, 36389695329187200L, 48519593772249600L, 60649492215312000L,
            72779390658374400L, 74801040398884800L, 106858629141264000L, 112201560598327200L, 149602080797769600L,
            224403121196654400L, 299204161595539200L, 374005201994424000L, 448806242393308800L, 673209363589963200L,
            748010403988848000L, 897612484786617600L
        };

        for (int i = 0; i < list.length; i++) {
            assertEquals(list[i], MathUtils.highlyCompositeNumber(list[i]));
            if (i + 1 < list.length) {
                assertEquals(list[i], MathUtils.highlyCompositeNumber(list[i + 1] - 1));
            }
        }
    }

    @Test
    public void testHighlyCompositeNumberLarge() {
        // N = 10^18
        long n = 1_000_000_000_000_000_000L;
        long res = MathUtils.highlyCompositeNumber(n);
        assertTrue(res <= n);
        assertTrue(res > 0);

        // Check a known large highly composite number (or similar)
        // 735134400 is highly composite (1344 divisors)
        // 897612484786617600L is highly composite (103680 divisors)
        long hcn = 897612484786617600L;
        long hcnDivisors = countDivisors(hcn);
        long resDivisors = countDivisors(res);

        assertTrue(resDivisors >= hcnDivisors, "Result divisors " + resDivisors + " should be at least " + hcnDivisors);
    }

    @Test
    public void testArgminLinearMod() {
        // Simple case: m = 1
        assertEquals(0, MathUtils.argminLinearMod(0, 0, 1, 0));
        assertEquals(0, MathUtils.argminLinearMod(0, 0, 1, 10));

        // Edge case: n = 0
        assertEquals(0, MathUtils.argminLinearMod(3, 4, 10, 0));

        // Small test cases checked against brute force
        for (long m = 2; m <= 30; m++) {
            for (long a = 0; a < m; a++) {
                for (long b = 0; b < m; b++) {
                    for (long n = 0; n <= 2 * m; n++) {
                        long expectedX = bruteForceArgminLinearMod(a, b, m, n);
                        long actualX = MathUtils.argminLinearMod(a, b, m, n);
                        assertEquals(expectedX, actualX, String.format("Failed for a=%d, b=%d, m=%d, n=%d", a, b, m, n));
                    }
                }
            }
        }

        // Random large test cases with safe values (m <= 1,000,000,000 to avoid overflow in unmodified minLinearMod)
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < 1000; i++) {
            long m = rnd.nextLong(2, 1_000_000_000L);
            long a = rnd.nextLong(0, m);
            long b = rnd.nextLong(0, m);
            long n = rnd.nextLong(0, m);

            long x = -1;
            try {
                x = MathUtils.argminLinearMod(a, b, m, n);
                assertTrue(x >= 0 && x <= n);
            } catch (Throwable t) {
                System.out.printf("FAILED AT: a=%d, b=%d, m=%d, n=%d, got_x=%d%n", a, b, m, n, x);
                throw t;
            }

            // Verify that evaluating argminLinearMod gives indeed the minimum
            long minVal = MathUtils.minLinearMod(a, b, m, n);
            long actualVal = BigInteger.valueOf(a).multiply(BigInteger.valueOf(x)).add(BigInteger.valueOf(b)).mod(BigInteger.valueOf(m)).longValue();
            assertEquals(minVal, actualVal, String.format("Minimum value mismatch for a=%d, b=%d, m=%d, n=%d", a, b, m, n));
        }
    }

    @Test
    public void testMinXLinearMod() {
        // Edge cases
        assertEquals(0, MathUtils.minXLinearMod(0, 0, 0, 1));
        assertEquals(-1, MathUtils.minXLinearMod(0, 2, 1, 5));
        assertEquals(-1, MathUtils.minXLinearMod(3, 4, -1, 10));
        assertEquals(0, MathUtils.minXLinearMod(3, 4, 10, 10));

        // Brute force comparison
        for (int m = 2; m <= 30; m++) {
            for (int a = 0; a < m; a++) {
                for (int b = 0; b < m; b++) {
                    for (int c = 0; c < m; c++) {
                        int expected = bruteForceMinXLinearMod(a, b, c, m);
                        int actual = MathUtils.minXLinearMod(a, b, c, m);
                        assertEquals(expected, actual, String.format("Failed for a=%d, b=%d, c=%d, m=%d", a, b, c, m));
                    }
                }
            }
        }

        // Random large test cases
        java.util.Random rnd = new java.util.Random(12345);
        for (int i = 0; i < 2000; i++) {
            int m = rnd.nextInt(1, 1_000_000_000) + 1;
            int a = rnd.nextInt(m);
            int b = rnd.nextInt(m);
            int c = rnd.nextInt(m);

            int x = MathUtils.minXLinearMod(a, b, c, m);
            if (x != -1) {
                // Verify that evaluating minXLinearMod gives indeed a value <= c
                BigInteger val = BigInteger.valueOf(a).multiply(BigInteger.valueOf(x)).add(BigInteger.valueOf(b)).mod(BigInteger.valueOf(m));
                assertTrue(val.compareTo(BigInteger.valueOf(c)) <= 0,
                    String.format("Result does not satisfy inequality for a=%d, b=%d, c=%d, m=%d, x=%d", a, b, c, m, x));

                // Verify minimality of x: check that x - 1 does not satisfy the inequality (if x > 0)
                if (x > 0) {
                    BigInteger valPrev = BigInteger.valueOf(a).multiply(BigInteger.valueOf(x - 1)).add(BigInteger.valueOf(b)).mod(BigInteger.valueOf(m));
                    assertTrue(valPrev.compareTo(BigInteger.valueOf(c)) > 0,
                        String.format("Result is not minimal for a=%d, b=%d, c=%d, m=%d, x=%d", a, b, c, m, x));
                }
            } else {
                // If x == -1, then no 0 <= x < m should satisfy (ax + b) % m <= c.
                for (int j = 0; j < 100; j++) {
                    int rx = rnd.nextInt(m);
                    BigInteger val = BigInteger.valueOf(a).multiply(BigInteger.valueOf(rx)).add(BigInteger.valueOf(b)).mod(BigInteger.valueOf(m));
                    assertTrue(val.compareTo(BigInteger.valueOf(c)) > 0);
                }
            }
        }
    }

    private int bruteForceMinXLinearMod(int a, int b, int c, int m) {
        for (int x = 0; x < m; x++) {
            if (Math.floorMod((long) a * x + b, m) <= c) {
                return x;
            }
        }
        return -1;
    }

    private long bruteForceArgminLinearMod(long a, long b, long m, long n) {
        long minVal = Long.MAX_VALUE;
        long bestX = -1;
        for (long x = 0; x <= n; x++) {
            long val = (a * x + b) % m;
            if (val < minVal) {
                minVal = val;
                bestX = x;
            }
        }
        return bestX;
    }

    private long countDivisors(long n) {
        long count = 1;
        long temp = n;
        for (long i = 2; i * i <= temp; i++) {
            if (temp % i == 0) {
                int exp = 0;
                while (temp % i == 0) {
                    exp++;
                    temp /= i;
                }
                count *= (exp + 1);
            }
        }
        if (temp > 1) count *= 2;
        return count;
    }
}
