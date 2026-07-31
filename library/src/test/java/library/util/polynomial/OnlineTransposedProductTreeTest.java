package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class OnlineTransposedProductTreeTest {

	@Test
	public void testSmallOffline() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 4;
		OnlineTransposedProductTree tdc = new OnlineTransposedProductTree(N, poly);
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
		for (int i = 1; i < N; i++) tdc.setR(i, R[i]);

		long[] f = {1, 1, 1};
		int K = 2;
		int[] degL = {1, 1, 1};

		long[] ans = tdc.calc(K, f, degL, (i, P) -> L[i]);

		// Calculate expected offline results using normal polynomial multiplication
		long[] expected = new long[N];
		for (int i = 0; i < N; i++) {
			long[] cur = f;
			for (int j = 0; j < i; j++) cur = poly.mul(cur, L[j]);
			for (int j = i + 1; j < N; j++) cur = poly.mul(cur, R[j] == null ? new long[]{1} : R[j]);
			if (K < cur.length) expected[i] = cur[K];
			else expected[i] = 0;
		}

		assertArrayEquals(expected, ans);
	}

	@Test
	public void testOnlineScenario() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 5;
		OnlineTransposedProductTree tdc = new OnlineTransposedProductTree(N, poly);

		long[][] R = {
			null,
			{1, 2},
			{1, 3},
			{1, 4},
			{1, 5}
		};
		for (int i = 1; i < N; i++) tdc.setR(i, R[i]);

		long[] f = {1, 0, 1};
		int K = 3;

		// In this online scenario, L[i] depends on the computed P_0, ..., P_i.
		// Specifically, let L[i] = {1, P_i}.
		// So degL[i] = 1.
		int[] degL = {1, 1, 1, 1};

		long[] ans = tdc.calc(K, f, degL, (i, P) -> {
			long pVal = P[i];
			return new long[] { 1, pVal };
		});

		// Let's verify manually using a naive step-by-step evaluation simulation
		long[] expected = new long[N];
		long[][] computedL = new long[N - 1][];
		for (int i = 0; i < N; i++) {
			long[] cur = f;
			for (int j = 0; j < i; j++) {
				cur = poly.mul(cur, computedL[j]);
			}
			for (int j = i + 1; j < N; j++) {
				cur = poly.mul(cur, R[j] == null ? new long[]{1} : R[j]);
			}
			long pVal = (K < cur.length) ? cur[K] : 0;
			expected[i] = pVal;
			if (i < N - 1) {
				computedL[i] = new long[] { 1, pVal };
			}
		}

		assertArrayEquals(expected, ans);
	}

	@Test
	public void testIdentity() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 3;
		OnlineTransposedProductTree tdc = new OnlineTransposedProductTree(N, poly);
		long[] f = {1, 2, 3};
		int K = 1;
		int[] degL = {0, 0};
		long[] ans = tdc.calc(K, f, degL, (i, P) -> new long[] {1});
		long[] expected = {2, 2, 2};
		assertArrayEquals(expected, ans);
	}

	@Test
	public void testRandomOnline() {
		Random rnd = new Random(12345);
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int numTests = 20;
		for (int t = 0; t < numTests; t++) {
			final int currentT = t;
			int N = rnd.nextInt(15) + 1;
			OnlineTransposedProductTree tdc = new OnlineTransposedProductTree(N, poly);
			long[][] R = new long[N][];
			for (int i = 1; i < N; i++) {
				R[i] = new long[rnd.nextInt(3) + 1];
				for (int j = 0; j < R[i].length; j++) R[i][j] = rnd.nextInt((int) poly.mod);
				tdc.setR(i, R[i]);
			}
			int fLen = rnd.nextInt(10) + 1;
			long[] f = new long[fLen];
			for (int i = 0; i < fLen; i++) f[i] = rnd.nextInt((int) poly.mod);
			int K = rnd.nextInt(20);

			int[] degL = new int[N];
			for (int i = 0; i < N - 1; i++) degL[i] = rnd.nextInt(2) + 1; // degree 1 or 2

			// Define an online function that uses P to generate a random-ish polynomial of degL[i]
			long[][] actualL = new long[N - 1][];
			long[] ans = tdc.calc(K, f, degL, (i, P) -> {
				long pVal = P[i];
				long[] lPoly = new long[degL[i] + 1];
				lPoly[0] = 1;
				for (int j = 1; j <= degL[i]; j++) {
					lPoly[j] = (pVal * j + currentT) % poly.mod;
				}
				actualL[i] = lPoly;
				return lPoly;
			});

			// Perform naive step-by-step generation with the same rule
			long[] expected = new long[N];
			long[][] naiveL = new long[N - 1][];
			for (int i = 0; i < N; i++) {
				long[] cur = f;
				for (int j = 0; j < i; j++) {
					cur = poly.mul(cur, naiveL[j]);
				}
				for (int j = i + 1; j < N; j++) {
					cur = poly.mul(cur, R[j] == null ? new long[]{1} : R[j]);
				}
				long pVal = (K < cur.length) ? cur[K] : 0;
				expected[i] = pVal;
				if (i < N - 1) {
					long[] lPoly = new long[degL[i] + 1];
					lPoly[0] = 1;
					for (int j = 1; j <= degL[i]; j++) {
						lPoly[j] = (pVal * j + currentT) % poly.mod;
					}
					naiveL[i] = lPoly;
				}
			}

			assertArrayEquals(expected, ans);
		}
	}

	@Test
	public void testDump() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		int N = 3;
		OnlineTransposedProductTree tdc = new OnlineTransposedProductTree(N, poly);
		tdc.setR(1, new long[]{1, 2});
		tdc.setR(2, new long[]{3, 4, 5});

		long[] f = {1, 3};
		int[] degL = {1, 1};

		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		java.io.PrintStream originalOut = System.out;
		try {
			System.setOut(new java.io.PrintStream(out));
			tdc.dump(1, f, degL, (i, P) -> new long[]{1, i + 1});
			String output = out.toString().trim();
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("OnlineTransposedProductTree"));
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("N: 3"));
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("f: [1, 3]"));
			// L[0] = [1, 1], L[1] = [1, 2]
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("L: [[1, 1], [1, 2]]"));
			// R[0] = null, R[1] = [1, 2], R[2] = [3, 4, 5]
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("[1, 2]"));
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("[3, 4, 5]"));
			// Check P:
			// P_2 = f * L_0 * L_1 = [1, 3] * [1, 1] * [1, 2] = [1, 4, 3] * [1, 2] = [1, 6, 11, 6]
			org.junit.jupiter.api.Assertions.assertTrue(output.contains("[1, 6, 11, 6]"));
		} finally {
			System.setOut(originalOut);
		}
	}
}
