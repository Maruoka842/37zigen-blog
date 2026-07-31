package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class PolynomialCoeffRCoreTest {

    @Test
    void testEdgeAndValidationCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] A = {1, 2, 3};
        long N = 3;
        long[] BA = {1, 1};
        long[] BA_le_R = {1, 1};

        // Exception on zero/negative A0
        long[] A_zero = {0, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruencePoint(A_zero, N, BA, BA_le_R, 3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruencePoint(new long[0], N, BA, BA_le_R, 3);
        });

        // Exception when k = deg(A) <= 0
        long[] A_constant = {5};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruencePoint(A_constant, N, BA, BA_le_R, 3);
        });

        // Exception when N <= 0
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruencePoint(A, 0, BA, BA_le_R, 3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.solveCyclicCongruencePoint(A, -1, BA, BA_le_R, 3);
        });

        // Negative R should return 0L
        assertEquals(0L, poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, -1));
        assertEquals(0L, poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, -100));

        // R >= N should return 0L
        assertEquals(0L, poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, N));
        assertEquals(0L, poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, N + 10));
    }

    @Test
    void testUserSpecificBugCase() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long mod = poly.mod;
        // A(x) = 1 - x
        long[] A = {1, mod - 1};
        long N = 2;
        // B(x) = x - 1
        long[] BA = {0}; // (x - 1) mod (1 - x) = 0
        long[] BA_le_R = {0}; // (x - 1) mod (1 - x) = 0
        long R = 1;

        long actualY1 = poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, R);
        System.out.println("actualY1: " + actualY1);
    }

    @Test
    void testSmallExactHandCalculated() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Let N = 3
        // A(x) = 1 + x (A = {1, 1})
        // Y(x) = 2 + 3x + 4x^2 (Y = {2, 3, 4})
        // B(x) = A(x) Y(x) mod (x^3 - 1) = 6 + 5x + 7x^2
        // BA = B mod A = B(-1) = (6 - 5 + 7) = 8
        // Let's target R = 1.
        // B_{<=1} = 6 + 5x
        // BA_{<=1} = B_{<=1} mod A = B_{<=1}(-1) = (6 - 5) = 1
        long[] A = {1, 1};
        long N = 3;
        long[] BA = {8};
        long[] BA_le_R = {1};
        long R = 1;

        long result = poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, R);
        assertEquals(3L, result);
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

            // Solve full Y using solveCyclicCongruenceLowTerms with L = N
            long[] Blow = Arrays.copyOf(B, N);
            long[] Yfull = poly.solveCyclicCongruenceLowTerms(A, N, BA, Blow, N);
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

            // Choose R in [0, N + 5] to sometimes test R >= N
            long R = rnd.nextInt(N + 6);

            // BA_le_R = B_{<=R} mod A
            long[] B_le_R = new long[(int) R + 1];
            for (int i = 0; i <= R; i++) {
                if (i < B.length) {
                    B_le_R[i] = B[i];
                }
            }
            long[] BA_le_R = poly.mod(B_le_R, A);

            long actual = poly.solveCyclicCongruencePoint(A, N, BA, BA_le_R, R);
            long expected = (R < N && R < Yfull.length) ? poly.getFp().reduce(Yfull[(int) R]) : 0L;

            assertEquals(expected, actual, "Mismatch at step " + step + " with N = " + N + ", R = " + R + ", degA = " + degA);
        }
    }
}
