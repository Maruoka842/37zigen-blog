package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class TransposedProductTreeTest {
	@Test
	public void testSmall() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 4;
		TransposedProductTree tdc = new TransposedProductTree(N, poly);
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
		for (int i = 0; i < N - 1; i++) tdc.setL(i, L[i]);
		for (int i = 1; i < N; i++) tdc.setR(i, R[i]);

		long[] f = {1, 1, 1};
		int K = 2;
		long[] ans = tdc.calc(K, f);

		long[] expected = new long[N];
		for (int i = 0; i < N; i++) {
			long[] cur = f;
			for (int j = 0; j < i; j++) cur = poly.mul(cur, L[j]);
			for (int j = i + 1; j < N; j++) cur = poly.mul(cur, R[j]);
			if (K < cur.length) expected[i] = cur[K];
			else expected[i] = 0;
		}

		assertArrayEquals(expected, ans);
	}

	@Test
	public void testIdentity() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 3;
		TransposedProductTree tdc = new TransposedProductTree(N, poly);
		long[] f = {1, 2, 3};
		int K = 1;
		long[] ans = tdc.calc(K, f);
		long[] expected = {2, 2, 2};
		assertArrayEquals(expected, ans);
	}

	@Test
	public void testRandom() {
		Random rnd = new Random(42);
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int numTests = 10;
		for (int t = 0; t < numTests; t++) {
			int N = rnd.nextInt(20) + 1;
			TransposedProductTree tdc = new TransposedProductTree(N, poly);
			long[][] L = new long[N - 1][];
			long[][] R = new long[N][];
			for (int i = 0; i < N - 1; i++) {
				L[i] = new long[rnd.nextInt(5) + 1];
				for (int j = 0; j < L[i].length; j++) L[i][j] = rnd.nextInt((int) poly.mod);
				tdc.setL(i, L[i]);
			}
			for (int i = 1; i < N; i++) {
				R[i] = new long[rnd.nextInt(5) + 1];
				for (int j = 0; j < R[i].length; j++) R[i][j] = rnd.nextInt((int) poly.mod);
				tdc.setR(i, R[i]);
			}
			int fLen = rnd.nextInt(20) + 1;
			long[] f = new long[fLen];
			for (int i = 0; i < fLen; i++) f[i] = rnd.nextInt((int) poly.mod);
			int K = rnd.nextInt(50);
			long[] ans = tdc.calc(K, f);

			long[] expected = new long[N];
			for (int i = 0; i < N; i++) {
				long[] cur = f;
				for (int j = 0; j < i; j++) cur = poly.mul(cur, L[j]);
				for (int j = i + 1; j < N; j++) cur = poly.mul(cur, R[j]);
				if (K < cur.length) expected[i] = cur[K];
				else expected[i] = 0;
			}
			assertArrayEquals(expected, ans);
		}
	}
}
