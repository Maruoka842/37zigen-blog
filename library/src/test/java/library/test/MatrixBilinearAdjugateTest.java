package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.MathUtils;
import library.util.linalg.MatrixUtilsFp;

public class MatrixBilinearAdjugateTest {

	@Test
	public void testRandomBilinearFormAdjugate() {
		Random rnd = new Random(42);
		long mod = 998244353;
		int n = 5;
		int trials = 10;

		for (int t = 0; t < trials; t++) {
			long[][] A = new long[n][n];
			long[][] B = new long[n][n];
			long[] u = new long[n];
			long[] v = new long[n];

			for (int i = 0; i < n; i++) {
				u[i] = rnd.nextInt((int) mod);
				v[i] = rnd.nextInt((int) mod);
				for (int j = 0; j < n; j++) {
					A[i][j] = rnd.nextInt((int) mod);
					B[i][j] = rnd.nextInt((int) mod);
				}
			}

			long[] poly = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(A, B, u, v, mod);

			// Evaluation points
			for (int xVal = 0; xVal < 5; xVal++) {
				long expected = naiveBilinearAdjugate(A, B, u, v, xVal, mod);
				long actual = evaluatePolynomial(poly, xVal, mod);
				assertEquals(expected, actual, "Trial " + t + ", x=" + xVal);
			}
		}
	}

	private long naiveBilinearAdjugate(long[][] A, long[][] B, long[] u, long[] v, long x, long mod) {
		int n = A.length;
		long[][] M = new long[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				M[i][j] = (A[i][j] * x + B[i][j]) % mod;
			}
		}

		long[][] adj = MatrixUtilsFp.modAdjugate(M, mod);
		// u^T adj v = sum_i sum_j u_i adj_{i,j} v_j
		long res = 0;
		for (int i = 0; i < n; i++) {
			long rowSum = 0;
			for (int j = 0; j < n; j++) {
				rowSum = (rowSum + adj[i][j] * v[j]) % mod;
			}
			res = (res + u[i] * rowSum) % mod;
		}
		return res;
	}

	private long evaluatePolynomial(long[] poly, long x, long mod) {
		long res = 0;
		long xp = 1;
		for (int i = 0; i < poly.length; i++) {
			res = (res + poly[i] * xp) % mod;
			xp = xp * x % mod;
		}
		return res;
	}
}
