package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class LinearlyRecurrentSequenceTest {

    @Test
    public void testKthTermFibonacci() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] initial = {0, 1};
        long[] recurrence = {1, 1};

        // F_0 = 0, F_1 = 1, F_2 = 1, F_3 = 2, F_4 = 3, F_5 = 5, F_6 = 8, F_7 = 13, F_8 = 21, F_9 = 34, F_10 = 55
        assertEquals(0L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 0));
        assertEquals(1L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 1));
        assertEquals(1L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 2));
        assertEquals(2L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 3));
        assertEquals(3L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 4));
        assertEquals(5L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 5));
        assertEquals(55L, poly.kthTermOfLinearlyRecurrentSequence(initial, recurrence, 10));
    }

    @Test
    public void testConsecutiveTermsFibonacci() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] initial = {0, 1};
        long[] recurrence = {1, 1};

        // F_0 to F_4
        assertArrayEquals(new long[]{0, 1, 1, 2, 3}, poly.consecutiveTermsOfLinearlyRecurrentSequence(initial, recurrence, 0, 5));

        // F_3 to F_6
        assertArrayEquals(new long[]{2, 3, 5, 8}, poly.consecutiveTermsOfLinearlyRecurrentSequence(initial, recurrence, 3, 4));

        // m = 0 case
        assertArrayEquals(new long[0], poly.consecutiveTermsOfLinearlyRecurrentSequence(initial, recurrence, 5, 0));

        // k < 0 should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.consecutiveTermsOfLinearlyRecurrentSequence(initial, recurrence, -1, 5);
        });

        // m < 0 should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.consecutiveTermsOfLinearlyRecurrentSequence(initial, recurrence, 0, -1);
        });

        // initial.length != recurrence.length should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.consecutiveTermsOfLinearlyRecurrentSequence(new long[]{0}, recurrence, 0, 5);
        });

        // d = 0 should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.consecutiveTermsOfLinearlyRecurrentSequence(new long[0], new long[0], 0, 5);
        });
    }

    @Test
    public void testExtendedCoefficientsFibonacci() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] recurrence = {1, 1};

        // k = 3
        // x^3 mod (x^2 - x - 1) = 2x + 1, so coeff is [1, 2]
        assertArrayEquals(new long[]{1, 2}, poly.extendedLinearlyRecurrentSequenceCoefficients(recurrence, 3));

        // Let's verify for k = 5
        // x^5 mod (x^2 - x - 1) = 5x + 3, so coeff is [3, 5]
        assertArrayEquals(new long[]{3, 5}, poly.extendedLinearlyRecurrentSequenceCoefficients(recurrence, 5));

        // Verifying linear combination relation for random/arbitrary initial terms
        long[] initial = {2, 5}; // a_0 = 2, a_1 = 5
        // a_2 = 5 + 2 = 7
        // a_3 = 7 + 5 = 12
        // a_4 = 12 + 7 = 19
        // a_5 = 19 + 12 = 31
        // a_6 = 31 + 19 = 50
        // a_7 = 50 + 31 = 81

        long[] coeffsK5 = poly.extendedLinearlyRecurrentSequenceCoefficients(recurrence, 5);
        // a_5 = coeffsK5[0]*a_0 + coeffsK5[1]*a_1 = 3*2 + 5*5 = 31 (matches!)
        // a_6 = coeffsK5[0]*a_1 + coeffsK5[1]*a_2 = 3*5 + 5*7 = 50 (matches!)
        // a_7 = coeffsK5[0]*a_2 + coeffsK5[1]*a_3 = 3*7 + 5*12 = 81 (matches!)

        assertEquals(31L, (coeffsK5[0] * initial[0] + coeffsK5[1] * initial[1]) % poly.mod);
        assertEquals(50L, (coeffsK5[0] * initial[1] + coeffsK5[1] * 7) % poly.mod);
        assertEquals(81L, (coeffsK5[0] * 7 + coeffsK5[1] * 12) % poly.mod);
    }

    private long naiveGetTermWithSparseAssign(long[] initial, long[] recurrence, long[] indices, long[] values, long k, long mod) {
        int d = recurrence.length;
        java.util.Map<Long, Long> assignMap = new java.util.HashMap<>();
        for (int i = 0; i < indices.length; i++) {
            assignMap.put(indices[i], values[i]);
        }

        long[] seq = new long[(int) k + d + 5];
        for (int i = 0; i < d; i++) {
            seq[i] = initial[i] % mod;
            if (seq[i] < 0) seq[i] += mod;
        }

        for (int i = 0; i < d; i++) {
            if (assignMap.containsKey((long) i)) {
                long v = assignMap.get((long) i) % mod;
                if (v < 0) v += mod;
                seq[i] = v;
            }
        }

        for (int i = d; i <= k; i++) {
            if (assignMap.containsKey((long) i)) {
                long v = assignMap.get((long) i) % mod;
                if (v < 0) v += mod;
                seq[i] = v;
            } else {
                long sum = 0;
                for (int j = 1; j <= d; j++) {
                    long c = recurrence[j - 1] % mod;
                    if (c < 0) c += mod;
                    sum = (sum + c * seq[i - j]) % mod;
                }
                if (sum < 0) sum += mod;
                seq[i] = sum;
            }
        }
        return seq[(int) k];
    }

    @Test
    public void testKthTermWithSparseAssignBasic() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] initial = {0, 1}; // F_0 = 0, F_1 = 1
        long[] recurrence = {1, 1}; // c_1 = 1, c_2 = 1

        // No assignments
        long[] indicesEmpty = {};
        long[] valuesEmpty = {};
        assertEquals(55L, poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, indicesEmpty, valuesEmpty, 10));

        // Assignment at index < d
        long[] indicesLessD = {1};
        long[] valuesLessD = {5};
        // Sequence with F_1 assigned to 5:
        // F_0 = 0, F_1 = 5, F_2 = 5, F_3 = 10, F_4 = 15, F_5 = 25
        assertEquals(25L, poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, indicesLessD, valuesLessD, 5));

        // Assignment at index >= d
        long[] indicesGteD = {3};
        long[] valuesGteD = {10};
        // Sequence with F_3 assigned to 10:
        // F_0 = 0, F_1 = 1, F_2 = 1, F_3 = 10 (override), F_4 = 11, F_5 = 21
        assertEquals(21L, poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, indicesGteD, valuesGteD, 5));

        // Duplicate assignments to same index
        long[] indicesDup = {3, 3};
        long[] valuesDup = {5, 10}; // the last one (10) should take precedence
        assertEquals(21L, poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, indicesDup, valuesDup, 5));

        // Negative values handling
        long[] initialNeg = {-1, 2};
        long[] recurrenceNeg = {-1, 3};
        long[] indicesNeg = {2};
        long[] valuesNeg = {-5};
        // Sequence:
        // a_0 = -1 (mod), a_1 = 2
        // a_2 = -5 (override)
        // a_3 = -1 * a_2 + 3 * a_1 = 5 + 6 = 11
        long ans = poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initialNeg, recurrenceNeg, indicesNeg, valuesNeg, 3);
        long expected = 11L;
        assertEquals(expected, ans);
    }

    @Test
    public void testKthTermWithSparseAssignRandomStress() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        java.util.Random rng = new java.util.Random(42);

        for (int tc = 0; tc < 50; tc++) {
            int d = rng.nextInt(5) + 1; // 1 to 5
            long[] initial = new long[d];
            for (int i = 0; i < d; i++) {
                initial[i] = rng.nextInt(20) - 10;
            }
            long[] recurrence = new long[d];
            for (int i = 0; i < d; i++) {
                recurrence[i] = rng.nextInt(10) - 5;
            }

            int m = rng.nextInt(6); // 0 to 5 assignments
            long[] indices = new long[m];
            long[] values = new long[m];
            Set<Long>set=new HashSet<>();
            for (int i = 0; i < m; i++) {
                indices[i] = rng.nextInt(15);
                values[i] = rng.nextInt(100) - 50;
                while(set.contains(indices[i])) {
                	indices[i]++;
                }
                set.add(indices[i]);
            }
            
            int k = rng.nextInt(20);

            long expected = naiveGetTermWithSparseAssign(initial, recurrence, indices, values, k, poly.mod);
            long actual = poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, indices, values, k);

            assertEquals(expected, actual, "Failed on testcase " + tc + " with k=" + k + ", d=" + d);
        }
    }

    @Test
    public void testKthTermWithSparseAssignValidation() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] initial = {1, 2};
        long[] recurrence = {1, 1};

        // Negative k should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, new long[]{}, new long[]{}, -1);
        });

        // Mismatched indices and values lengths should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, new long[]{1}, new long[]{}, 2);
        });

        // Mismatched initial and recurrence lengths should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(new long[]{1}, recurrence, new long[]{}, new long[]{}, 2);
        });

        // Negative index in assignments should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.kthTermOfLinearlyRecurrentSequenceWithSparseAssign(initial, recurrence, new long[]{-1}, new long[]{5}, 2);
        });
    }

    private long naiveGetCWithSparseZero(long[] P, long[] Q, long[] initial, long[] indices, long k, long mod) {
        int degP = P.length - 1;
        while (degP >= 0 && P[degP] % mod == 0) degP--;
        int degQ = Q.length - 1;
        while (degQ >= 0 && Q[degQ] % mod == 0) degQ--;

        int limit = (int) k + 10;
        long[] a = new long[limit];

        long q0 = Q[0] % mod;
        if (q0 < 0) q0 += mod;
        long invQ0 = library.util.MathUtils.modInv(q0, mod);
        for (int n = 0; n < limit; n++) {
            long pVal = (n < P.length) ? (P[n] % mod + (P[n] < 0 ? mod : 0)) % mod : 0;
            long sum = 0;
            for (int i = 1; i <= n && i < Q.length; i++) {
                long qVal = (Q[i] % mod + (Q[i] < 0 ? mod : 0)) % mod;
                sum = (sum + qVal * a[n - i]) % mod;
            }
            a[n] = (pVal - sum + mod) % mod * invQ0 % mod;
        }

        long[] c = new long[limit];
        for (int i = 0; i < initial.length && i < limit; i++) {
            c[i] = initial[i] % mod;
            if (c[i] < 0) c[i] += mod;
        }

        java.util.Set<Long> overrideSet = new java.util.HashSet<>();
        for (long idx : indices) {
            overrideSet.add(idx);
        }

        // Apply overrides on initial terms
        for (int i = 0; i < initial.length && i < limit; i++) {
            if (overrideSet.contains((long) i)) {
                c[i] = 0;
            }
        }

        for (int n = initial.length; n < limit; n++) {
            if (overrideSet.contains((long) n)) {
                c[n] = 0;
            } else {
                long sum = 0;
                for (int i = 1; i <= n; i++) {
                    sum = (sum + c[n - i] * a[i]) % mod;
                }
                c[n] = sum;
            }
        }

        return c[(int) k];
    }

    @Test
    public void testKthTermOfCWithSparseZeroBasic() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] P = {0, 1}; // P(x) = x
        long[] Q = {1, -1}; // Q(x) = 1 - x
        // gives a_i = 1 for i >= 1

        long[] initial = {1, 2, 4}; // Given finite initial sequence

        // No overrides
        long[] indicesEmpty = {};
        assertEquals(1L, poly.kthTermOfCWithSparseZero(P, Q, initial, indicesEmpty, 0));
        assertEquals(2L, poly.kthTermOfCWithSparseZero(P, Q, initial, indicesEmpty, 1));
        assertEquals(4L, poly.kthTermOfCWithSparseZero(P, Q, initial, indicesEmpty, 2));
        assertEquals(7L, poly.kthTermOfCWithSparseZero(P, Q, initial, indicesEmpty, 3)); // c_3 = c_2 + c_1 + c_0 = 4 + 2 + 1 = 7
        assertEquals(14L, poly.kthTermOfCWithSparseZero(P, Q, initial, indicesEmpty, 4)); // c_4 = c_3 + c_2 + c_1 + c_0 = 7 + 4 + 2 + 1 = 14

        // Override at 1
        long[] indices1 = {1};
        assertEquals(1L, poly.kthTermOfCWithSparseZero(P, Q, initial, indices1, 0));
        assertEquals(0L, poly.kthTermOfCWithSparseZero(P, Q, initial, indices1, 1));
        assertEquals(4L, poly.kthTermOfCWithSparseZero(P, Q, initial, indices1, 2));
        // c_3 = c_2 + c_1 + c_0 = 4 + 0 + 1 = 5
        assertEquals(5L, poly.kthTermOfCWithSparseZero(P, Q, initial, indices1, 3));
        // c_4 = c_3 + c_2 + c_1 + c_0 = 5 + 4 + 0 + 1 = 10
        assertEquals(10L, poly.kthTermOfCWithSparseZero(P, Q, initial, indices1, 4));
    }

    @Test
    public void testKthTermOfCWithSparseZeroStress() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        java.util.Random rng = new java.util.Random(42);

        for (int tc = 0; tc < 100; tc++) {
            int lenP = rng.nextInt(5) + 1;
            long[] P = new long[lenP];
            for (int i = 0; i < lenP; i++) {
                P[i] = rng.nextInt(20);
            }
            P[0] = 0;

            int lenQ = rng.nextInt(5) + 1;
            long[] Q = new long[lenQ];
            for (int i = 0; i < lenQ; i++) {
                Q[i] = rng.nextInt(20);
            }
            Q[0] = 1;

            int lenInit = rng.nextInt(5) + 1;
            long[] initial = new long[lenInit];
            for (int i = 0; i < lenInit; i++) {
                initial[i] = rng.nextInt(100);
            }

            int m = rng.nextInt(6);
            long[] indices = new long[m];
            Set<Long> set = new HashSet<>();
            for (int i = 0; i < m; i++) {
                long idx = rng.nextInt(15) + 1; // n >= 1
                while (set.contains(idx)) {
                    idx++;
                }
                indices[i] = idx;
                set.add(idx);
            }
            java.util.Arrays.sort(indices);

            int k = rng.nextInt(20);

            long expected = naiveGetCWithSparseZero(P, Q, initial, indices, k, poly.mod);
            long actual = poly.kthTermOfCWithSparseZero(P, Q, initial, indices, k);
            assertEquals(expected, actual, "Failed on stress testcase " + tc + " with k=" + k);
        }
    }

    @Test
    public void testKthTermOfCWithSparseZeroValidation() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] initial = {1};

        // Q[0] != 1 should throw IllegalArgumentException
        long[] P = {0, 1};
        long[] Q_not_1 = {2, -1};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.kthTermOfCWithSparseZero(P, Q_not_1, initial, new long[]{}, 2);
        });

        // deg(Q) < 0 should throw ArithmeticException
        long[] Q_zero = {};
        assertThrows(ArithmeticException.class, () -> {
            poly.kthTermOfCWithSparseZero(P, Q_zero, initial, new long[]{}, 2);
        });

        // Negative k should throw AssertionError
        assertThrows(AssertionError.class, () -> {
            poly.kthTermOfCWithSparseZero(P, new long[]{1}, initial, new long[]{}, -1);
        });

        // P[0] != 0 should throw IllegalArgumentException
        long[] P_not_zero = {1, 1};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.kthTermOfCWithSparseZero(P_not_zero, new long[]{1, -1}, initial, new long[]{}, 2);
        });
    }

}
