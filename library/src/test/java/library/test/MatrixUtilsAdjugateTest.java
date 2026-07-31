package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Random;
import org.junit.jupiter.api.Test;
import library.util.linalg.MatrixUtilsFp;

public class MatrixUtilsAdjugateTest {

	private static final long MOD = 998244353L;

	@Test
	public void testNZero() {
		long[][] A = new long[0][0];
		long[][] B = new long[0][0];
		long[] u = new long[0];
		long[][] L = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);
		assertEquals(0, L.length);
	}

	@Test
	public void testNOne() {
		long[][] A = {{5}};
		long[][] B = {{3}};
		long[] u = {7};
		long[][] L = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);
		assertEquals(1, L.length);
		assertEquals(1, L[0].length);
		assertEquals(7, L[0][0]);
	}

	@Test
	public void testRegularAndSingularMatrices() {
		Random rnd = new Random(12345);
		for (int N = 2; N <= 4; N++) {
			for (int trial = 0; trial < 10; trial++) {
				long[][] A = new long[N][N];
				long[][] B = new long[N][N];
				long[] u = new long[N];
				long[] v = new long[N];

				for (int i = 0; i < N; i++) {
					u[i] = rnd.nextLong(0, MOD);
					v[i] = rnd.nextLong(0, MOD);
					for (int j = 0; j < N; j++) {
						A[i][j] = rnd.nextLong(0, MOD);
						B[i][j] = rnd.nextLong(0, MOD);
					}
				}

				// Sometimes make B singular to test the shift logic
				if (trial % 2 == 1) {
					// Make B have redundant row/col
					for (int j = 0; j < N; j++) {
						B[N - 1][j] = B[0][j];
					}
				}

				// Compute via leftVectorAdjugateAxPlusBOnFp
				long[][] L = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);
				assertNotNull(L);

				// Compute u^T adj(Ax + B) v using L(x)
				// L_m is the coefficient of x^m of the vector L(x) = u^T adj(Ax + B).
				// u^T adj(Ax + B) v = sum_{m=0}^{N-1} (L_m * v) x^m
				long[] resFromL = new long[N];
				for (int m = 0; m < N; m++) {
					long sum = 0;
					for (int j = 0; j < N; j++) {
						sum = (sum + L[m][j] * v[j]) % MOD;
					}
					resFromL[m] = sum;
				}

				// Truncate trailing zeros for comparison
				int degL = resFromL.length - 1;
				while (degL >= 0 && resFromL[degL] == 0) degL--;
				long[] resFromL_truncated = new long[degL < 0 ? 1 : degL + 1];
				for (int i = 0; i < resFromL_truncated.length; i++) {
					resFromL_truncated[i] = resFromL[i];
				}

				// Compute via bilinearFormAdjugateAxPlusBOnFp (which uses determinant-based formulas)
				long[] resBilinear = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(A, B, u, v, MOD);

				assertArrayEquals(resBilinear, resFromL_truncated, "Failed on N=" + N + ", trial=" + trial);
			}
		}
	}

	@Test
	public void testSingularPencilRankLessThanOrEqualNMinusTwo() {
		// N = 3, both A and B are all zeros (rank = 0 <= N-2)
		int N = 3;
		long[][] A = new long[N][N];
		long[][] B = new long[N][N];
		long[] u = {1, 2, 3};
		long[][] L = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);
		assertNotNull(L);
		assertEquals(N, L.length);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				assertEquals(0, L[i][j]);
			}
		}
	}

	@Test
	public void testSingularPencilRankNMinusOneThrowsArithmeticException() {
		// N = 3, singular pencil with rank N-1 = 2
		int N = 3;
		// A has row 0 = row 1, B has row 0 = row 1
		long[][] A = {
			{1, 2, 3},
			{1, 2, 3},
			{4, 5, 6}
		};
		long[][] B = {
			{7, 8, 9},
			{7, 8, 9},
			{1, 0, 2}
		};
		long[] u = {1, 1, 1};
		try {
			MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);
			org.junit.jupiter.api.Assertions.fail("Should have thrown ArithmeticException");
		} catch (ArithmeticException e) {
			assertEquals("singular pencil with rank N-1, need kernel interpolation", e.getMessage());
		}
	}

	@Test
	public void testRandomStress() {
		Random rnd = new Random(54321);
		long[] moduli = {13, 10007, 998244353L};
		for (long mod : moduli) {
			for (int N = 1; N <= 5; N++) {
				for (int trial = 0; trial < 20; trial++) {
					long[][] A = new long[N][N];
					long[][] B = new long[N][N];
					long[] u = new long[N];
					long[] v = new long[N];

					for (int i = 0; i < N; i++) {
						u[i] = rnd.nextLong(0, mod);
						v[i] = rnd.nextLong(0, mod);
						for (int j = 0; j < N; j++) {
							A[i][j] = rnd.nextLong(0, mod);
							B[i][j] = rnd.nextLong(0, mod);
						}
					}

					// Randomly make B singular
					if (rnd.nextBoolean() && N > 1) {
						int sourceRow = rnd.nextInt(N);
						int destRow = rnd.nextInt(N);
						if (sourceRow != destRow) {
							long multiplier = rnd.nextLong(1, mod);
							for (int j = 0; j < N; j++) {
								B[destRow][j] = (B[sourceRow][j] * multiplier) % mod;
							}
						}
					}

					long[][] L;
					try {
						L = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, mod);
					} catch (ArithmeticException e) {
						// Singular pencil of rank N-1, expected exception
						assertEquals("singular pencil with rank N-1, need kernel interpolation", e.getMessage());
						continue;
					}

					assertNotNull(L);

					// Compute u^T adj(Ax + B) v using L(x)
					long[] resFromL = new long[N];
					for (int m = 0; m < N; m++) {
						long sum = 0;
						for (int j = 0; j < N; j++) {
							sum = (sum + L[m][j] * v[j]) % mod;
						}
						resFromL[m] = sum;
					}

					// Truncate trailing zeros
					int degL = resFromL.length - 1;
					while (degL >= 0 && resFromL[degL] == 0) degL--;
					long[] resFromL_truncated = new long[degL < 0 ? 1 : degL + 1];
					for (int i = 0; i < resFromL_truncated.length; i++) {
						resFromL_truncated[i] = resFromL[i];
					}

					// Compute via slower bilinearFormAdjugateAxPlusBOnFp
					long[] resBilinear = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(A, B, u, v, mod);

					assertArrayEquals(resBilinear, resFromL_truncated, "Failed stress on N=" + N + ", mod=" + mod + ", trial=" + trial);
				}
			}
		}
	}

	@Test
	public void testInverseIminusAxOnFp_NZero() {
		long[][] A = new long[0][0];
		long[] u = new long[0];
		MatrixUtilsFp.RationalVectorResult res = MatrixUtilsFp.inverseIminusAxOnFp(A, u, MOD);
		assertEquals(0, res.numerators.length);
		assertEquals(1, res.denominator.length);
		assertEquals(1 % MOD, res.denominator[0]);
	}

	@Test
	public void testInverseIminusAxOnFp_NOne() {
		long[][] A = {{5}};
		long[] u = {7};
		MatrixUtilsFp.RationalVectorResult res = MatrixUtilsFp.inverseIminusAxOnFp(A, u, MOD);
		assertEquals(1, res.numerators.length);
		assertEquals(1, res.numerators[0].length);
		assertEquals(7 % MOD, res.numerators[0][0]);

		assertEquals(2, res.denominator.length);
		assertEquals(1 % MOD, res.denominator[0]);
		assertEquals((MOD - 5) % MOD, res.denominator[1]);
	}

	@Test
	public void testInverseIminusAxOnFp_RandomAndStress() {
		Random rnd = new Random(12345);
		long[] moduli = {13, 10007, 998244353L};
		for (long mod : moduli) {
			for (int N = 1; N <= 5; N++) {
				for (int trial = 0; trial < 30; trial++) {
					long[][] A = new long[N][N];
					long[] u = new long[N];
					for (int i = 0; i < N; i++) {
						u[i] = rnd.nextLong(0, mod);
						for (int j = 0; j < N; j++) {
							A[i][j] = rnd.nextLong(0, mod);
						}
					}

					MatrixUtilsFp.RationalVectorResult res = MatrixUtilsFp.inverseIminusAxOnFp(A, u, mod);
					assertNotNull(res);
					assertEquals(N, res.numerators.length);
					assertEquals(N + 1, res.denominator.length);

					// Pick a random evaluation point x_0
					long x0 = rnd.nextLong(0, mod);

					// Evaluate denominator: Q(x_0) = \sum D_i x_0^i
					long Q_eval = 0;
					long current_power = 1;
					for (int i = 0; i <= N; i++) {
						long term = res.denominator[i] * current_power % mod;
						Q_eval = (Q_eval + term) % mod;
						current_power = current_power * x0 % mod;
					}

					// If Q(x0) == 0, skip this x0 because I - A * x0 is singular
					if (Q_eval == 0) continue;

					// Construct I - A * x_0
					long[][] M = new long[N][N];
					for (int i = 0; i < N; i++) {
						M[i][i] = 1;
						for (int j = 0; j < N; j++) {
							long term = A[i][j] * x0 % mod;
							M[i][j] = (M[i][j] - term + mod) % mod;
						}
					}

					// Invert I - A * x_0
					long[][] invM = MatrixUtilsFp.inv(M, mod);
					assertNotNull(invM, "Matrix I - A * x0 should be invertible when Q_eval != 0");

					// Compute vector v = invM * u
					long[] v = new long[N];
					for (int i = 0; i < N; i++) {
						long sum = 0;
						for (int j = 0; j < N; j++) {
							sum = (sum + invM[i][j] * u[j]) % mod;
						}
						v[i] = sum;
					}

					// Verify relation Q_eval * v_j == P_j(x0) for each component j
					for (int j = 0; j < N; j++) {
						// Evaluate numerator: P_j(x0) = \sum numerators[j][i] x0^i
						long P_eval_j = 0;
						long current_power_num = 1;
						for (int i = 0; i < N; i++) {
							long term = res.numerators[j][i] * current_power_num % mod;
							P_eval_j = (P_eval_j + term) % mod;
							current_power_num = current_power_num * x0 % mod;
						}

						long expected = Q_eval * v[j] % mod;
						assertEquals(expected, P_eval_j, "Failed relation at component " + j + " on N=" + N + ", mod=" + mod + ", trial=" + trial);
					}
				}
			}
		}
	}

	@Test
  public void testInverseAxPlusBOnFp_NZero() {
		long[][] A = new long[0][0];
		long[][] B = new long[0][0];
		long[] u = new long[0];
		MatrixUtilsFp.RationalVectorResult res = MatrixUtilsFp.inverseAxPlusBOnFp(A, B, u, MOD);
		assertEquals(0, res.numerators.length);
		assertEquals(1, res.denominator.length);
		assertEquals(1 % MOD, res.denominator[0]);
	}

	@Test
	public void testInverseAxPlusBOnFp_NOne() {
		long[][] A = {{5}};
		long[][] B = {{3}};
		long[] u = {7};
		MatrixUtilsFp.RationalVectorResult res = MatrixUtilsFp.inverseAxPlusBOnFp(A, B, u, MOD);
		assertEquals(1, res.numerators.length);
		assertEquals(1, res.numerators[0].length);
		assertEquals(2, res.denominator.length);

		// The answer P(x)/Q(x) should be equal to 7 / (5x + 3) as a fraction.
		// That means P(x) * (5x + 3) == Q(x) * 7.
		// Let's verify this polynomial identity coefficient-by-coefficient:
		// Left-hand side: (numerators[0][0]) * (5x + 3) = (3 * P0) + (5 * P0) x
		// Right-hand side: (D0 + D1 x) * 7 = (7 * D0) + (7 * D1) x
		long P0 = res.numerators[0][0];
		long D0 = res.denominator[0];
		long D1 = res.denominator[1];

		assertEquals(3 * P0 % MOD, 7 * D0 % MOD);
		assertEquals(5 * P0 % MOD, 7 * D1 % MOD);
	}


	@Test
	public void testLeftVectorAdjugateAxPlusIOnFp_RandomAndStress() {
		Random rnd = new Random(6789);
		long[] moduli = {13, 10007, 998244353L};
		for (long mod : moduli) {
			for (int N = 1; N <= 4; N++) {
				for (int trial = 0; trial < 20; trial++) {
					long[][] A = new long[N][N];
					long[][] B = new long[N][N];
					long[] u = new long[N];
					for (int i = 0; i < N; i++) {
						u[i] = rnd.nextLong(0, mod);
						for (int j = 0; j < N; j++) {
							A[i][j] = rnd.nextLong(0, mod);
							B[i][j] = rnd.nextLong(0, mod);
						}
					}

					// Randomly make B singular
					if (rnd.nextBoolean() && N > 1) {
						int sourceRow = rnd.nextInt(N);
						int destRow = rnd.nextInt(N);
						if (sourceRow != destRow) {
							long multiplier = rnd.nextLong(1, mod);
							for (int j = 0; j < N; j++) {
								B[destRow][j] = (B[sourceRow][j] * multiplier) % mod;
							}
						}
					}

					MatrixUtilsFp.RationalVectorResult res;
					try {
						res = MatrixUtilsFp.inverseAxPlusBOnFp(A, B, u, mod);
					} catch (ArithmeticException e) {
						assertEquals("singular pencil with rank N-1, need kernel interpolation", e.getMessage());
						continue;
					}

					assertNotNull(res);
					assertEquals(N, res.numerators.length);
					assertEquals(N + 1, res.denominator.length);

					// Pick a random evaluation point x_0
					long x0 = rnd.nextLong(0, mod);

					// Evaluate denominator: Q(x_0) = \sum D_i x_0^i
					long Q_eval = 0;
					long current_power = 1;
					for (int i = 0; i <= N; i++) {
						long term = res.denominator[i] * current_power % mod;
						Q_eval = (Q_eval + term) % mod;
						current_power = current_power * x0 % mod;
					}

					// If Q(x0) == 0, skip this x0 because A * x0 + B is singular
					if (Q_eval == 0) continue;

					// Construct A * x_0 + B
					long[][] M = new long[N][N];
					for (int i = 0; i < N; i++) {
						for (int j = 0; j < N; j++) {
							long term = (A[i][j] * x0 + B[i][j]) % mod;
							M[i][j] = (term + mod) % mod;
						}
					}

					// Invert A * x_0 + B
					long[][] invM = MatrixUtilsFp.inv(M, mod);
					assertNotNull(invM, "Matrix A * x0 + B should be invertible when Q_eval != 0");

					// Compute vector v = invM * u
					long[] v = new long[N];
					for (int i = 0; i < N; i++) {
						long sum = 0;
						for (int j = 0; j < N; j++) {
							sum = (sum + invM[i][j] * u[j]) % mod;
						}
						v[i] = sum;
					}

					// Verify relation Q_eval * v_j == P_j(x0) for each component j
					for (int j = 0; j < N; j++) {
						// Evaluate numerator: P_j(x0) = \sum numerators[j][i] x0^i
						long P_eval_j = 0;
						long current_power_num = 1;
						for (int i = 0; i < N; i++) {
							long term = res.numerators[j][i] * current_power_num % mod;
							P_eval_j = (P_eval_j + term) % mod;
							current_power_num = current_power_num * x0 % mod;
						}

						long expected = Q_eval * v[j] % mod;
						assertEquals(expected, P_eval_j, "Failed relation at component " + j + " on N=" + N + ", mod=" + mod + ", trial=" + trial);
					}
				}
			}
		}
	}

	@Test
	public void testLeftVectorAdjugateAxPlusIOnFp_Direct() {
		Random rnd = new Random(98765);
		for (int N = 1; N <= 4; N++) {
			for (int trial = 0; trial < 10; trial++) {
				long[][] A = new long[N][N];
				long[][] B = new long[N][N];
				long[] u = new long[N];
				for (int i = 0; i < N; i++) {
					u[i] = rnd.nextLong(0, MOD);
					B[i][i] = 1; // Identity matrix
					for (int j = 0; j < N; j++) {
						A[i][j] = rnd.nextLong(0, MOD);
					}
				}

				long[][] L1 = MatrixUtilsFp.leftVectorAdjugateAxPlusIOnFp(A, u, MOD);
				long[][] L2 = MatrixUtilsFp.leftVectorAdjugateAxPlusBOnFp(A, B, u, MOD);

				for (int i = 0; i < N; i++) {
					assertArrayEquals(L1[i], L2[i], "Mismatch between leftVectorAdjugateAxPlusIOnFp and leftVectorAdjugateAxPlusBOnFp with B=I");
				}
			}
		}
	}

	@Test
	public void testInverseAxPlusBOnFp_SingularThrows() {
		int N = 3;
		// A and B both zero, so rank <= N-2, singular pencil
		long[][] A = new long[N][N];
		long[][] B = new long[N][N];
		long[] u = {1, 2, 3};
		try {
			MatrixUtilsFp.inverseAxPlusBOnFp(A, B, u, MOD);
			org.junit.jupiter.api.Assertions.fail("Should have thrown ArithmeticException for singular pencil");
		} catch (ArithmeticException e) {
			assertEquals("singular pencil with rank <= N-2", e.getMessage());
		}
	}
}
