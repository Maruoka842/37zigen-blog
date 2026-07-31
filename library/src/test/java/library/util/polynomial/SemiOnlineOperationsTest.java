package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class SemiOnlineOperationsTest {

    @Test
    public void testSemiOnlineInv() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(42);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Edge case: constant term is 0 (ArithmeticException)
            PolynomialFpDynamic.OnlineInv errSolver = new PolynomialFpDynamic.OnlineInv(5, poly);
            assertThrows(ArithmeticException.class, () -> errSolver.append(0));

            // Random cases
            for (int len : new int[]{2, 5, 20, 50}) {
                long[] a = new long[len];
                a[0] = 1 + rng.nextInt((int) mod - 1);
                for (int i = 1; i < len; i++) {
                    a[i] = rng.nextInt((int) mod);
                }

                long[] expected = poly.inv(a);
                PolynomialFpDynamic.OnlineInv solver = new PolynomialFpDynamic.OnlineInv(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(a[i]);
                }
                assertArrayEquals(expected, actual, "Failed on inv with len " + len + " under mod " + mod);
            }
        }
    }

    @Test
    public void testSemiOnlineLog() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(43);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Edge case: constant term is not 1 (IllegalArgumentException)
            PolynomialFpDynamic.OnlineLog errSolver = new PolynomialFpDynamic.OnlineLog(5, poly);
            assertThrows(IllegalArgumentException.class, () -> errSolver.append(2));

            // Random cases
            for (int len : new int[]{2, 5, 20, 50}) {
                long[] a = new long[len];
                a[0] = 1;
                for (int i = 1; i < len; i++) {
                    a[i] = rng.nextInt((int) mod);
                }

                long[] expected = poly.log(a);
                PolynomialFpDynamic.OnlineLog solver = new PolynomialFpDynamic.OnlineLog(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(a[i]);
                }
                assertArrayEquals(expected, actual, "Failed on log with len " + len + " under mod " + mod);
            }
        }
    }

    @Test
    public void testSemiOnlineExp() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(44);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Edge case: constant term is not 0 (IllegalArgumentException)
            PolynomialFpDynamic.OnlineExp errSolver = new PolynomialFpDynamic.OnlineExp(5, poly);
            assertThrows(IllegalArgumentException.class, () -> errSolver.append(1));

            // Random cases
            for (int len : new int[]{2, 5, 20, 50}) {
                long[] a = new long[len];
                a[0] = 0;
                for (int i = 1; i < len; i++) {
                    a[i] = rng.nextInt((int) mod);
                }

                long[] expected = poly.exp(a);
                PolynomialFpDynamic.OnlineExp solver = new PolynomialFpDynamic.OnlineExp(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(a[i]);
                }
                assertArrayEquals(expected, actual, "Failed on exp with len " + len + " under mod " + mod);
            }
        }
    }

    @Test
    public void testSemiOnlineSqrt() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(45);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Edge case: non-quadratic residue constant term (returns null / throws)
            PolynomialFpDynamic.OnlineSqrt errSolver = new PolynomialFpDynamic.OnlineSqrt(5, poly);
            assertThrows(IllegalArgumentException.class, () -> errSolver.append(5));

            // Random cases with QR constant terms (using squares of random numbers)
            for (int len : new int[]{2, 5, 20, 50}) {
                long[] a = new long[len];
                long root = 1 + rng.nextInt((int) mod - 1);
                a[0] = root * root % mod;
                for (int i = 1; i < len; i++) {
                    a[i] = rng.nextInt((int) mod);
                }

                long[] expected = poly.sqrt(a);
                PolynomialFpDynamic.OnlineSqrt solver = new PolynomialFpDynamic.OnlineSqrt(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(a[i]);
                }
                assertArrayEquals(expected, actual, "Failed on sqrt with len " + len + " under mod " + mod);
            }
        }
    }

    @Test
    public void testSemiOnlinePow() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(46);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Edge case: constant term is 0 (ArithmeticException)
            PolynomialFpDynamic.OnlinePow errSolver = new PolynomialFpDynamic.OnlinePow(5, 3, poly);
            assertThrows(ArithmeticException.class, () -> errSolver.append(0));

            // Random cases
            for (int len : new int[]{5, 20, 50}) {
                long[] a = new long[len];
                a[0] = 1 + rng.nextInt((int) mod - 1);
                for (int i = 1; i < len; i++) {
                    a[i] = rng.nextInt((int) mod);
                }

                for (long m : new long[]{3, 5, 10}) {
                    long[] expected = poly.pow(a, m);
                    PolynomialFpDynamic.OnlinePow solver = new PolynomialFpDynamic.OnlinePow(len, m, poly);
                    long[] actual = new long[len];
                    for (int i = 0; i < len; i++) {
                        actual[i] = solver.append(a[i]);
                    }
                    assertArrayEquals(expected, actual, "Failed on pow with len " + len + " power " + m + " under mod " + mod);
                }
            }
        }
    }

    @Test
    public void testSemiOnlineCompositionDirect() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(147);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;
            for (int len : new int[]{1, 2, 3, 4, 5, 10, 16, 17, 31, 32}) {
                long[] f = new long[len];
                long[] g = new long[len];
                for (int i = 0; i < len; i++) {
                    f[i] = rng.nextInt((int) mod);
                    g[i] = rng.nextInt((int) mod);
                }
                g[0] = 0;

                // Compute expected offline
                long[] expected = poly.comp(f, g, len);

                // Test semi-online solver
                PolynomialFpDynamic.SemiOnlineComposition solver = new PolynomialFpDynamic.SemiOnlineComposition(len, g, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(f[i]);
                }

                assertArrayEquals(expected, actual, "Failed on SemiOnlineComposition with len " + len + " under mod " + mod);
            }
        }
    }

    @Test
    public void testOnlinePolynomialComposition() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(47);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;
            for (int len : new int[]{1, 2, 3, 5, 8, 10, 17}) {
                long[] f = new long[len];
                long[] g = new long[len];
                for (int i = 0; i < len; i++) {
                    f[i] = rng.nextInt((int) mod);
                    g[i] = rng.nextInt((int) mod);
                }
                g[0] = 0; // Composition requires constant term of g to be 0

                // Compute expected offline
                long[] expected = new long[len];
                for (int i = 0; i < len; i++) {
                    long[] g_pow = new long[len];
                    g_pow[0] = 1;
                    for (int s = 0; s < i; s++) {
                        long[] next_g = new long[len];
                        for (int a = 0; a < len; a++) {
                            for (int b = 0; b < len - a; b++) {
                                next_g[a + b] = (next_g[a + b] + g_pow[a] * g[b]) % mod;
                            }
                        }
                        g_pow = next_g;
                    }
                    for (int t = 0; t < len; t++) {
                        expected[t] = (expected[t] + f[i] * g_pow[t]) % mod;
                    }
                }

                // Test online solver step-by-step
                PolynomialFpDynamic.OnlineComposition solver = new PolynomialFpDynamic.OnlineComposition(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    actual[i] = solver.append(f[i], g[i]);
                }

                assertArrayEquals(expected, actual, "Failed on online polynomial composition under mod " + mod + " with len " + len);
            }
        }
    }

    @Test
    public void testOnlinePowerProjection() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(48);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Perform 100 random test cases with randomized lengths
            for (int tc = 0; tc < 100; tc++) {
                int len = 1 + rng.nextInt(64);

                long[] W = new long[len];
                long[] g = new long[len];
                for (int i = 0; i < len; i++) {
                    W[i] = rng.nextInt((int) mod);
                    g[i] = rng.nextInt((int) mod);
                }
                g[0] = 0; // Composition/projection requires constant term of g to be 0

                // Solve using our OnlinePowerProjection and check step-by-step return values
                PolynomialFpDynamic.OnlinePowerProjection solver = new PolynomialFpDynamic.OnlinePowerProjection(len, poly);
                long[] actual = new long[len];
                for (int i = 0; i < len; i++) {
                    long g_val = (i < len - 1) ? g[i + 1] : 0;
                    long res = solver.append(W[len - 1 - i], g_val);
                    actual[len - 1 - i] = res;
                }

                long[] R = solver.getResult();

                // Verify that the step-by-step returned values match R
                assertArrayEquals(R, actual, "Online collected result must match getResult() at testcase " + tc);

                // Verify using the Transpose Relation (Tellegen's Theorem)
                // for multiple random f vectors: sum_i H_i W_i == sum_j f_j R_j (mod mod)
                for (int iter = 0; iter < 5; iter++) {
                    long[] f = new long[len];
                    for (int i = 0; i < len; i++) {
                        f[i] = rng.nextInt((int) mod);
                    }

                    // Compute forward composition H = f(g) mod x^len
                    long[] H = new long[len];
                    for (int i = 0; i < len; i++) {
                        long[] g_pow = new long[len];
                        g_pow[0] = 1;
                        for (int s = 0; s < i; s++) {
                            long[] next_g = new long[len];
                            for (int a = 0; a < len; a++) {
                                for (int b = 0; b < len - a; b++) {
                                    next_g[a + b] = (next_g[a + b] + g_pow[a] * g[b]) % mod;
                                }
                            }
                            g_pow = next_g;
                        }
                        for (int t = 0; t < len; t++) {
                            H[t] = (H[t] + f[i] * g_pow[t]) % mod;
                        }
                    }

                    long sum_H_W = 0;
                    long sum_f_R = 0;
                    for (int i = 0; i < len; i++) {
                        sum_H_W = (sum_H_W + H[i] * W[i]) % mod;
                        sum_f_R = (sum_f_R + f[i] * R[i]) % mod;
                    }

                    assertEquals(sum_H_W, sum_f_R, "Failed on transpose relation check under mod " + mod + " iter " + iter + " testcase " + tc);
                }
            }
        }
    }

    @Test
    public void testOnlinePolynomialCompositionStress() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(49);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;
            for (int TEST = 0; TEST < 100; TEST++) {
            	int len=rng.nextInt(1, 100);
        		long[] f = new long[len];
        		long[] g = new long[len];
        		for (int i = 0; i < len; i++) {
        			f[i] = rng.nextInt((int) mod);
        			g[i] = rng.nextInt((int) mod);
        		}
        		g[0] = 0; // Composition requires constant term of g to be 0
        		
        		// Compute expected using fast offline composition
        		long[] expected = poly.comp(f, g, len);
        		
        		// Test online solver step-by-step
        		PolynomialFpDynamic.OnlineComposition solver = new PolynomialFpDynamic.OnlineComposition(len, poly);
        		long[] actual = new long[len];
        		for (int i = 0; i < len; i++) {
        			actual[i] = solver.append(f[i], g[i]);
        		}
        		
        		assertArrayEquals(expected, actual, "Stress test failed on online polynomial composition with len " + len + " under mod " + mod);
        	}
        }
    }

    @Test
    public void testSemiOnlinePowerProjectionFixingPowerBase() {
        PolynomialFpDynamic[] polys = {PolynomialFpDynamic.MOD998244353, PolynomialFpDynamic.of(1000000007L)};
        Random rng = new Random(50);

        for (PolynomialFpDynamic poly : polys) {
            long mod = poly.mod;

            // Test various sizes of N (both powers of 2 and arbitrary sizes)
            for (int N : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 16, 17, 31, 32, 33, 35, 47, 53, 64}) {
                for (int tc = 0; tc < 20; tc++) {
                	long[] g = new long[N];
                    long[] W = new long[N];

                    g[0] = 0; // g_0 must be 0
                    for (int i = 1; i < N; i++) {
                        g[i] = rng.nextInt((int) mod);
                    }
                    for (int i = 0; i < N; i++) {
                        W[i] = rng.nextInt((int) mod);
                    }

                    // Naive calculation of expected result
                    long[] expected = new long[N];
                    long[] g_pow = new long[N];
                    g_pow[0] = 1;
                    for (int j = 0; j < N; j++) {
                        if (j > 0) {
                            g_pow = poly.mul(g_pow, g);
                            if (g_pow.length > N) {
                                g_pow = Arrays.copyOf(g_pow, N);
                            } else if (g_pow.length < N) {
                                long[] tmp = new long[N];
                                System.arraycopy(g_pow, 0, tmp, 0, g_pow.length);
                                g_pow = tmp;
                            }
                        }
                        long sum = 0;
                        for (int i = 0; i < N; i++) {
                            sum = (sum + W[i] * g_pow[i]) % mod;
                        }
                        expected[j] = sum;
                    }

                    // Test step-by-step append
                    PolynomialFpDynamic.SemiOnlinePowerProjectionFixingPowerBase solver =
                            new PolynomialFpDynamic.SemiOnlinePowerProjectionFixingPowerBase(N, g, poly);

                    long[] actual = new long[N];
                    for (int i = 0; i < N; i++) {
                        // We append from highest to lowest degree
                        long W_i = W[N - 1 - i];
                        long returnedVal = solver.append(W_i);
                        // The returned value corresponds to the result at index N - 1 - i
                        actual[N - 1 - i] = returnedVal;
                        assertEquals(expected[N - 1 - i], returnedVal,
                                "Step mismatch at index " + (N - 1 - i) + " with N=" + N + ", mod=" + mod);
                    }

                    // Verify the final full result array
                    assertArrayEquals(expected, solver.getResult(),
                            "Final result mismatch with N=" + N + ", mod=" + mod);
                }
            }
        }
    }
}
