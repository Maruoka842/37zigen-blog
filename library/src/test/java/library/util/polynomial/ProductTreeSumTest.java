package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class ProductTreeSumTest {
	@Test
	public void testSmall() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 4;
		ProductTreeSum pts = new ProductTreeSum(N, poly);
		long[][] L = {
			{1, 2},
			{1, 3},
			{1, 4}
		};
		long[][] R = {
			null,
			{1, 5},
			{1, 6},
			{1, 7}
		};
		for (int i = 0; i < N - 1; i++) pts.setL(i, L[i]);
		for (int i = 1; i < N; i++) pts.setR(i, R[i]);

		long[][] f = {
			{1, 2},
			{3, 4},
			{5, 6},
			{7, 8}
		};
		long[] ans = pts.calc(f);

		long[] expected = new long[0];
		for (int i = 0; i < N; i++) {
			long[] cur = f[i];
			for (int j = 0; j < i; j++) cur = poly.mul(cur, L[j]);
			for (int j = i + 1; j < N; j++) cur = poly.mul(cur, R[j]);
			expected = poly.add(expected, cur);
		}

		assertArrayEquals(poly.resize(expected), poly.resize(ans));
	}

	@Test
	public void testRandom() {
		Random rnd = new Random(42);
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int numTests = 10;
		for (int t = 0; t < numTests; t++) {
			int N = rnd.nextInt(20) + 1;
			ProductTreeSum pts = new ProductTreeSum(N, poly);
			long[][] L = new long[N - 1][];
			long[][] R = new long[N][];
			for (int i = 0; i < N - 1; i++) {
				L[i] = new long[rnd.nextInt(5) + 1];
				for (int j = 0; j < L[i].length; j++) L[i][j] = rnd.nextInt((int) poly.mod);
				pts.setL(i, L[i]);
			}
			for (int i = 1; i < N; i++) {
				R[i] = new long[rnd.nextInt(5) + 1];
				for (int j = 0; j < R[i].length; j++) R[i][j] = rnd.nextInt((int) poly.mod);
				pts.setR(i, R[i]);
			}
			long[][] f = new long[N][];
			for (int i = 0; i < N; i++) {
				f[i] = new long[rnd.nextInt(10) + 1];
				for (int j = 0; j < f[i].length; j++) f[i][j] = rnd.nextInt((int) poly.mod);
			}
			long[] ans = pts.calc(f);

			long[] expected = new long[0];
			for (int i = 0; i < N; i++) {
				long[] cur = f[i];
				for (int j = 0; j < i; j++) cur = poly.mul(cur, L[j]);
				for (int j = i + 1; j < N; j++) cur = poly.mul(cur, R[j]);
				expected = poly.add(expected, cur);
			}
			assertArrayEquals(poly.resize(expected), poly.resize(ans));
		}
	}
}
