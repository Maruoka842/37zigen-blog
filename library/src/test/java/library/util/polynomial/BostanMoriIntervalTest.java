package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class BostanMoriIntervalTest {

    @Test
    void testSimpleGeometric() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] P = {1};
        long[] Q = {1, 998244353 - 2}; // 1 - 2x
        // 1 / (1 - 2x) = 1 + 2x + 4x^2 + 8x^3 + ...

        long[] res = poly.consecutiveTermsOfRationalFunction(P, Q, 0, 5);
        assertArrayEquals(new long[]{1, 2, 4, 8, 16}, res);

        long[] res2 = poly.consecutiveTermsOfRationalFunction(P, Q, 3, 4);
        assertArrayEquals(new long[]{8, 16, 32, 64}, res2);
    }

    @Test
    void testEnforceOne() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] P = {1};
        long[] Q_zero = {0, 1};
        long[] Q_two = {2, 1};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.consecutiveTermsOfRationalFunction(P, Q_zero, 0, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.consecutiveTermsOfRationalFunction(P, Q_two, 0, 5);
        });
    }

    @Test
    void testWithReferenceSmall() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        Random rnd = new Random(42);

        for (int step = 0; step < 50; step++) {
            int degP = rnd.nextInt(10);
            int degQ = rnd.nextInt(10) + 1;
            long[] P = new long[degP + 1];
            for (int i = 0; i <= degP; i++) {
                P[i] = rnd.nextInt(100);
            }
            long[] Q = new long[degQ + 1];
            // Ensure Q[0] is exactly 1
            Q[0] = 1;
            for (int i = 1; i <= degQ; i++) {
                Q[i] = rnd.nextInt(100);
            }

            long k = rnd.nextInt(20);
            int m = rnd.nextInt(15) + 1;

            long[] res = poly.consecutiveTermsOfRationalFunction(P, Q, k, m);
            long[] ref = getReference(P, Q, k, m, poly);
            assertArrayEquals(ref, res, "Failed at step " + step);
        }
    }

    @Test
    void testWithNthLargeK() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        Random rnd = new Random(100);

        for (int step = 0; step < 10; step++) {
            int degP = rnd.nextInt(5);
            int degQ = rnd.nextInt(5) + 1;
            long[] P = new long[degP + 1];
            for (int i = 0; i <= degP; i++) {
                P[i] = rnd.nextInt(100000);
            }
            long[] Q = new long[degQ + 1];
            Q[0] = 1;
            for (int i = 1; i <= degQ; i++) {
                Q[i] = rnd.nextInt(100000);
            }

            long k = 1000000000000000L + rnd.nextInt(100000);
            int m = 10;

            long[] res = poly.consecutiveTermsOfRationalFunction(P, Q, k, m);

            for (int i = 0; i < m; i++) {
                long expected = poly.nth(k + i, P, Q);
                assertEquals(expected, res[i], "Mismatch at index " + i);
            }
        }
    }

    @Test
    void testComprehensiveRandomStress() {
        // Run random stress test on both NTT-friendly (998244353) and non-NTT friendly (1000000007) moduli
        PolynomialFpDynamic[] polys = {
            PolynomialFpDynamic.MOD998244353,
            PolynomialFpDynamic.of(1000000007L)
        };
        Random rnd = new Random(20230329);

        for (PolynomialFpDynamic poly : polys) {
            for (int step = 0; step < 100; step++) {
                int degP = rnd.nextInt(30);
                int degQ = rnd.nextInt(30) + 1;

                long[] P = new long[degP + 1];
                for (int i = 0; i <= degP; i++) {
                    P[i] = (rnd.nextLong() & Long.MAX_VALUE) % poly.mod;
                }

                long[] Q = new long[degQ + 1];
                Q[0] = 1;
                for (int i = 1; i <= degQ; i++) {
                    Q[i] = (rnd.nextLong() & Long.MAX_VALUE) % poly.mod;
                }

                long k = rnd.nextInt(100);
                int m = rnd.nextInt(50) + 1;

                long[] res = poly.consecutiveTermsOfRationalFunction(P, Q, k, m);
                long[] ref = getReference(P, Q, k, m, poly);
                assertArrayEquals(ref, res, "Mismatch at step " + step + " for mod " + poly.mod);
            }
        }
    }

    private long[] getReference(long[] P, long[] Q, long k, int m, PolynomialFpDynamic poly) {
        if (Q.length == 0 || poly.getFp().reduce(Q[0]) != 1) {
            throw new IllegalArgumentException("Q[0] must be congruent to 1");
        }
        int limit = (int) (k + m);
        long[] invQ = poly.inv(Arrays.copyOf(Q, limit));
        long[] prod = poly.mul(P, invQ);
        long[] ans = new long[m];
        for (int i = 0; i < m; i++) {
            long idx = k + i;
            if (idx < prod.length) {
                ans[i] = prod[(int) idx];
            }
        }
        return ans;
    }
}
