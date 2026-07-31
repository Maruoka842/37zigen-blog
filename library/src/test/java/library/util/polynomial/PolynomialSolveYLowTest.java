package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class PolynomialSolveYLowTest {

    @Test
    void testEdgeAndValidationCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] A = {1, 2, 3};
        long N = 5;
        long[] BA = {1, 1};
        long[] Blow = {1, 2, 3};

        // Exception on zero/negative N
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, 0, BA, Blow, 3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, -5, BA, Blow, 3);
        });

        // Exception on zero/negative L
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow, -1);
        });

        // Exception on A(0) == 0
        long[] A_zero = {0, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A_zero, N, BA, Blow, 3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(new long[0], N, BA, Blow, 3);
        });

        // Null pointer check
        assertThrows(NullPointerException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, N, null, Blow, 3);
        });
        assertThrows(NullPointerException.class, () -> {
            poly.solveCyclicCongruenceLowTerms(A, N, BA, null, 3);
        });
    }

    @Test
    void testSmallExactHandCalculated() {
        // Let mod = 998244353
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Let N = 3, L = 2
        // A(x) = 1 + x (A = {1, 1})
        // Y(x) = 2 + 3x + 4x^2 (Y = {2, 3, 4})
        // B(x) = A(x) Y(x) mod (x^3 - 1)
        // A(x)Y(x) = (1+x)(2+3x+4x^2) = 2 + 5x + 7x^2 + 4x^3
        // mod (x^3 - 1) => 4x^3 is replaced by 4, so B(x) = 6 + 5x + 7x^2
        // BA = B mod A = B(-1) mod A = (6 - 5 + 7) mod A = 8
        // BA = {8}
        // Blow = B_{<2} = {6, 5}
        long[] A = {1, 1};
        long N = 3;
        long[] BA = {8};
        long[] Blow = {6, 5};
        int L = 2;

        long[] Ylow = poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow, L);
        assertNotNull(Ylow);
        // Expected Ylow is {2, 3}
        assertArrayEquals(new long[]{2, 3}, Ylow);
    }

    @Test
    void testRandomStressMod998244353() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runRandomStress(poly, 100, 42);
    }

    @Test
    void testRandomStressMod1000000007() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runRandomStress(poly, 100, 12345);
    }

    private void runRandomStress(PolynomialFpDynamic poly, int iterations, long seed) {
        Random rnd = new Random(seed);
        long mod = poly.mod;

        for (int step = 0; step < iterations; step++) {
            // Choose degrees
            int degA = rnd.nextInt(5) + 2; // deg A in [2, 6]
            int N = rnd.nextInt(15) + degA + 2;

            long[] A;
            if (rnd.nextBoolean()) {
                // Coprime case
                A = new long[degA + 1];
                for (int i = 0; i <= degA; i++) {
                    A[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
                }
                while (A[0] == 0) {
                    A[0] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);
                }
                while (A[degA] == 0) {
                    A[degA] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);
                }
            } else {
                // Non-coprime case: make A share a factor with x^N - 1
                long[] F;
                if (rnd.nextBoolean() && N > 2) {
                    // Choose a random divisor d of N (d < N)
                    java.util.List<Integer> divisors = new java.util.ArrayList<>();
                    for (int d = 1; d < N; d++) {
                        if (N % d == 0) divisors.add(d);
                    }
                    int d = divisors.isEmpty() ? 1 : divisors.get(rnd.nextInt(divisors.size()));
                    F = new long[d + 1];
                    F[0] = mod - 1; // -1
                    F[d] = 1;       // x^d
                } else {
                    F = new long[]{mod - 1, 1}; // x - 1
                }

                int degBase = Math.max(1, degA - (F.length - 1));
                long[] A_base = new long[degBase + 1];
                for (int i = 0; i <= degBase; i++) {
                    A_base[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
                }
                while (A_base[0] == 0) {
                    A_base[0] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);
                }
                while (A_base[degBase] == 0) {
                    A_base[degBase] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);
                }
                A = poly.mul(A_base, F);
            }
            A = poly.resize(A);
            degA = poly.deg(A);

            // Generate Y of degree < N
            long[] Y = new long[N];
            for (int i = 0; i < N; i++) {
                Y[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            }
            Y = poly.resize(Y);

            // B(x) = A(x)Y(x) mod (x^N - 1)
            long[] AY = poly.mul(A, Y);
            long[] B = new long[N];
            for (int i = 0; i < AY.length; i++) {
                B[i % N] = (B[i % N] + AY[i]) % mod;
            }
            B = poly.resize(B);

            // BA = B mod A
            long[] BA = poly.mod(B, A);

            // Solve full Y_solved using L = N
            long[] Blow_full = Arrays.copyOf(B, N);
            long[] Yfull = poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow_full, N);
            assertNotNull(Yfull, "Yfull must not be null at step " + step);
            Yfull = poly.resize(Yfull);

            // Verify that the solved Yfull satisfies the congruence A(x) Yfull(x) \equiv B(x) \pmod{x^N - 1}
            long[] AY_solved = poly.mul(A, Yfull);
            long[] B_solved = new long[N];
            for (int i = 0; i < AY_solved.length; i++) {
                B_solved[i % N] = (B_solved[i % N] + AY_solved[i]) % mod;
            }
            B_solved = poly.resize(B_solved);
            assertArrayEquals(B, B_solved, "Solved Y does not satisfy A*Y = B mod (x^N-1) at step " + step);

            // Blow = B_{<L}
            int L = rnd.nextInt(N - 1) + 1; // L in [1, N-1]
            long[] Blow = Arrays.copyOf(B, L);

            // Compute solveLowTermsOfY
            long[] Ylow = poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow, L);
            assertNotNull(Ylow, "Ylow must not be null at step " + step);

            long[] expectedYlow = Arrays.copyOf(Yfull, L);
            for (int i = 0; i < L; i++) {
                expectedYlow[i] = poly.getFp().reduce(expectedYlow[i]);
            }

            assertArrayEquals(expectedYlow, Ylow, "Mismatch at step " + step + " with N = " + N + ", L = " + L + ", degA = " + degA);
        }
    }
}
