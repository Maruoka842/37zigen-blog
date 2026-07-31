package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import library.util.MonotoneMinima.CostFunction;

public class MongeMatrixProductTest {

	@Test
	public void testMongeMatrixProduct() {
		int N = 100;
		// A[i][j] = (i - j)^2 if i <= j else INF
		CostFunction A = (i, j) -> i <= j ? (long) (i - j) * (i - j) : Long.MAX_VALUE / 3;
		// B[i][j] = (i - j)^2 if i <= j else INF
		CostFunction B = (i, j) -> i <= j ? (long) (i - j) * (i - j) : Long.MAX_VALUE / 3;

		long[][] C = MinPlus.mongeMatrixProduct(N, A, B);
		long[][] expected = naive(N, A, B);

		for (int i = 0; i <= N; i++) {
			assertArrayEquals(expected[i], C[i], "Row " + i + " mismatch");
		}
	}

	@Test
	public void testMongeMatrixProduct2() {
		int N = 50;
		// Random Monge matrices
		// A[i][j] = a[i] + b[j] + c[i]*d[j] where c[i] is increasing and d[j] is decreasing?
		// No, simpler: a Monge matrix can be formed by f(j-i) where f is convex.
		// f(x) = x^2 is convex.

		CostFunction A = (i, j) -> i <= j ? (long) (j - i) * (j - i) + 10 * i : Long.MAX_VALUE / 3;
		CostFunction B = (i, j) -> i <= j ? (long) (j - i) * (j - i) - 5 * j : Long.MAX_VALUE / 3;

		long[][] C = MinPlus.mongeMatrixProduct(N, A, B);
		long[][] expected = naive(N, A, B);

		for (int i = 0; i <= N; i++) {
			assertArrayEquals(expected[i], C[i], "Row " + i + " mismatch");
		}
	}

	private long[][] naive(int N, CostFunction A, CostFunction B) {
		long[][] C = new long[N + 1][N + 1];
		for (int i = 0; i <= N; i++) {
			Arrays.fill(C[i], Long.MAX_VALUE / 3);
			for (int j = i; j <= N; j++) {
				for (int k = i; k <= j; k++) {
					C[i][j] = Math.min(C[i][j], A.calc(i, k) + B.calc(k, j));
				}
			}
		}
		return C;
	}
}
