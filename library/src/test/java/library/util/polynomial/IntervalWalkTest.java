package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.Random;
import java.util.Arrays;
import library.util.MathUtils;

public class IntervalWalkTest {

	private static final long MOD_NTT = 998244353L;
	private static final long MOD_NON_NTT = 1_000_000_007L;

	/**
	 * Naive DP simulation of the limited interval walk on [0, M).
	 */
	private static long solveDP(long mod, long[] C, int M, long k, long n, boolean hasStay) {
		if (M <= 0 || k < 0 || k >= M || n < 0) return 0;
		long[] dp = new long[M];
		for (int i = 0; i < M; i++) {
			if (i < C.length) {
				dp[i] = (C[i] % mod + mod) % mod;
			}
		}
		for (int step = 0; step < n; step++) {
			long[] nextDp = new long[M];
			for (int i = 0; i < M; i++) {
				if (dp[i] == 0) continue;
				// Left
				if (i - 1 >= 0) {
					nextDp[i - 1] = (nextDp[i - 1] + dp[i]) % mod;
				}
				// Stay
				if (hasStay) {
					nextDp[i] = (nextDp[i] + dp[i]) % mod;
				}
				// Right
				if (i + 1 < M) {
					nextDp[i + 1] = (nextDp[i + 1] + dp[i]) % mod;
				}
			}
			dp = nextDp;
		}
		return dp[(int) k];
	}

	/**
	 * psi(w) generator for A(x) = x^-1 + 1 + x (with stay)
	 */
	private static long[] getStandardPsi(long mod, int N) {
		long[] g = new long[N + 2];
		g[0] = 1;
		if (N >= 0) {
			g[1] = (mod - 1) % mod;
		}
		for (int j = 1; j <= N; j++) {
			long term1 = (2 * j - 1) * g[j] % mod;
			long term2 = 3 * (j - 2) * g[j - 1] % mod;
			long num = (term1 + term2) % mod;
			if (num < 0) num += mod;
			g[j + 1] = num * MathUtils.modInv(j + 1, mod) % mod;
		}
		long[] psi = new long[N + 1];
		for (int j = 1; j <= N; j++) {
			long val = (mod - g[j + 1]) % mod;
			if (val % 2 == 1) val += mod;
			psi[j] = (val / 2) % mod;
		}
		return psi;
	}

	/**
	 * psi(w) generator for A(x) = x^-1 + x (no stay)
	 */
	private static long[] getStandardPsiNoStay(long mod, int N) {
		long[] psi = new long[N + 1];
		if (N >= 1) {
			psi[1] = 1;
		}
		long cat = 1;
		for (int k = 1; 2 * k + 1 <= N; k++) {
			long num = (4 * k - 2) * cat % mod;
			cat = num * MathUtils.modInv(k + 1, mod) % mod;
			psi[2 * k + 1] = cat;
		}
		return psi;
	}

	@Test
	public void testSmallWalkNoStay() {
		PolynomialFpDynamic fp = PolynomialFpDynamic.of(MOD_NTT);
		long[] C = {0, 1}; // Starts at 1
		int M_val = 4; // walks on [0, 4), equivalent to [0, 3]
		long k = 1;
		int maxN = 4;

		long D = 2L * (M_val + 1);
		long j_min = (long) Math.ceil((double) (k - maxN - (M_val - 1)) / D);
		long E = M_val + k + 1 - j_min * D;
		int M_idx = (int) (maxN + E);
		int N = M_idx + 1;
		long[] psi = getStandardPsiNoStay(MOD_NTT, N + 1);

		long[] C_new = Arrays.copyOf(C, M_val);
		long[] C_rev = new long[M_val];
		for (int i = 0; i < M_val; i++) {
			C_rev[i] = C_new[M_val - 1 - i];
		}

		long[] C_new_psi = compose(fp, C_new, psi, N);
		long[] C_rev_psi = compose(fp, C_rev, psi, N);

		long[] ansAnalytical = IntervalWalk.solveVaryingN(fp, C_new_psi, C_rev_psi, psi, M_val, k, maxN);
		long ansDP = solveDP(MOD_NTT, C, M_val, k, maxN, false);

		assertEquals(5L, ansDP);
		assertEquals(ansDP, ansAnalytical[maxN]);

		// test target k = 3
		k = 3;
		j_min = (long) Math.ceil((double) (k - maxN - (M_val - 1)) / D);
		E = M_val + k + 1 - j_min * D;
		M_idx = (int) (maxN + E);
		N = M_idx + 1;
		psi = getStandardPsiNoStay(MOD_NTT, N + 1);
		C_new_psi = compose(fp, C_new, psi, N);
		C_rev_psi = compose(fp, C_rev, psi, N);

		ansAnalytical = IntervalWalk.solveVaryingN(fp, C_new_psi, C_rev_psi, psi, M_val, k, maxN);
		ansDP = solveDP(MOD_NTT, C, M_val, k, maxN, false);

		assertEquals(3L, ansDP);
		assertEquals(ansDP, ansAnalytical[maxN]);
	}

	@Test
	public void testSmallWalkWithStay() {
		PolynomialFpDynamic fp = PolynomialFpDynamic.of(MOD_NTT);
		long[] C = {0, 1, 3}; // C(x) = x + 3 x^2
		int M_val = 5; // walks on [0, 5)
		long k = 2;
		int maxN = 5;

		long D = 2L * (M_val + 1);
		long j_min = (long) Math.ceil((double) (k - maxN - (M_val - 1)) / D);
		long E = M_val + k + 1 - j_min * D;
		int M_idx = (int) (maxN + E);
		int N = M_idx + 1;
		long[] psi = getStandardPsi(MOD_NTT, N + 1);

		long[] C_new = Arrays.copyOf(C, M_val);
		long[] C_rev = new long[M_val];
		for (int i = 0; i < M_val; i++) {
			C_rev[i] = C_new[M_val - 1 - i];
		}

		long[] C_new_psi = compose(fp, C_new, psi, N);
		long[] C_rev_psi = compose(fp, C_rev, psi, N);

		long[] ansAnalytical = IntervalWalk.solveVaryingN(fp, C_new_psi, C_rev_psi, psi, M_val, k, maxN);
		long ansDP = solveDP(MOD_NTT, C, M_val, k, maxN, true);

		assertEquals(ansDP, ansAnalytical[maxN]);
	}

	@Test
	public void testNonNTTMod() {
		PolynomialFpDynamic fp = PolynomialFpDynamic.of(MOD_NON_NTT);
		long[] C = {0, 0, 1}; // Starts at 2
		int M_val = 6; // walks on [0, 6)
		long k = 3;
		int maxN = 6;

		long D = 2L * (M_val + 1);
		long j_min = (long) Math.ceil((double) (k - maxN - (M_val - 1)) / D);
		long E = M_val + k + 1 - j_min * D;
		int M_idx = (int) (maxN + E);
		int N = M_idx + 1;
		long[] psi = getStandardPsiNoStay(MOD_NON_NTT, N + 1);

		long[] C_new = Arrays.copyOf(C, M_val);
		long[] C_rev = new long[M_val];
		for (int i = 0; i < M_val; i++) {
			C_rev[i] = C_new[M_val - 1 - i];
		}

		long[] C_new_psi = compose(fp, C_new, psi, N);
		long[] C_rev_psi = compose(fp, C_rev, psi, N);

		long[] ansAnalytical = IntervalWalk.solveVaryingN(fp, C_new_psi, C_rev_psi, psi, M_val, k, maxN);
		long ansDP = solveDP(MOD_NON_NTT, C, M_val, k, maxN, false);

		assertEquals(ansDP, ansAnalytical[maxN]);
	}

	@Test
	public void testRandomStress() {
		Random rng = new Random(42);
		PolynomialFpDynamic fpNTT = PolynomialFpDynamic.of(MOD_NTT);
		PolynomialFpDynamic fpNonNTT = PolynomialFpDynamic.of(MOD_NON_NTT);

		for (int iter = 0; iter < 100; iter++) {
			int M_val = rng.nextInt(15) + 2; // [0, M) with M >= 2
			long k = rng.nextInt(M_val);
			int maxN = rng.nextInt(40) + 1;
			boolean hasStay = rng.nextBoolean();
			boolean isNTT = rng.nextBoolean();

			long[] C = new long[M_val];
			for (int i = 0; i < M_val; i++) {
				C[i] = rng.nextInt(100);
			}

			PolynomialFpDynamic fp = isNTT ? fpNTT : fpNonNTT;
			long mod = fp.mod;

			long D = 2L * (M_val + 1);
			long j_min = (long) Math.ceil((double) (k - maxN - (M_val - 1)) / D);
			long E = M_val + k + 1 - j_min * D;
			int M_idx = (int) (maxN + E);
			int N = M_idx + 1;
			long[] psi = hasStay ? getStandardPsi(mod, N + 1) : getStandardPsiNoStay(mod, N + 1);

			long[] C_new = Arrays.copyOf(C, M_val);
			long[] C_rev = new long[M_val];
			for (int i = 0; i < M_val; i++) {
				C_rev[i] = C_new[M_val - 1 - i];
			}

			long[] C_new_psi = compose(fp, C_new, psi, N);
			long[] C_rev_psi = compose(fp, C_rev, psi, N);

			// Compute answers for all n in [0, maxN] using solveVaryingN (Explicit API)
			long[] ansVarying = IntervalWalk.solveVaryingN(fp, C_new_psi, C_rev_psi, psi, M_val, k, maxN);
			assertEquals(maxN + 1, ansVarying.length);

			// Compute answers for all n in [0, maxN] using solveVaryingN (Overloaded Raw Polynomial API)
			long[] A_poly = hasStay ? new long[]{1, 1} : new long[]{0, 1}; // non-negative coefficients: [a_0, a_1]
			long[] ansVaryingOverloaded = IntervalWalk.solveVaryingN(fp, C, A_poly, M_val, k, maxN);
			assertArrayEquals(ansVarying, ansVaryingOverloaded);

			// Compute answers for all target positions k_target with fixed n = maxN
			long[] ansVaryingK = IntervalWalk.solveVaryingK(fp, C, A_poly, M_val, maxN);
			assertEquals(M_val, ansVaryingK.length);
			assertEquals(ansVarying[maxN], ansVaryingK[(int) k]);

			for (int n = 0; n <= maxN; n++) {
				long expectedDP = solveDP(mod, C, M_val, k, n, hasStay);
				assertEquals(expectedDP, ansVarying[n], "VaryingN mismatch at iter " + iter + " with n=" + n);
			}

			// Verify all k_target in [0, M_val) for fixed maxN
			for (int kt = 0; kt < M_val; kt++) {
				long expectedDP_kt = solveDP(mod, C, M_val, kt, maxN, hasStay);
				assertEquals(expectedDP_kt, ansVaryingK[kt], "VaryingK mismatch at iter " + iter + " with k_target=" + kt);
			}
		}
	}

	private static long[] compose(PolynomialFpDynamic fp, long[] P, long[] psi, int N) {
		if (P.length == 0) {
			return new long[0];
		}
		return composeHelper(fp, P, 0, P.length - 1, psi, N)[0];
	}

	private static long[][] composeHelper(PolynomialFpDynamic fp, long[] P, int l, int r, long[] psi, int N) {
		if (l == r) {
			long[] val = new long[]{fp.getFp().reduce(P[l])};
			long[] psi_power = Arrays.copyOf(psi, N);
			return new long[][]{val, psi_power};
		}
		int m = (l + r) >> 1;
		long[][] left = composeHelper(fp, P, l, m, psi, N);
		long[][] right = composeHelper(fp, P, m + 1, r, psi, N);

		long[] B_times_psi_pow = fp.mul(right[0], left[1]);
		if (B_times_psi_pow.length > N) {
			B_times_psi_pow = Arrays.copyOf(B_times_psi_pow, N);
		}
		long[] val = fp.add(left[0], B_times_psi_pow);
		if (val.length > N) {
			val = Arrays.copyOf(val, N);
		}

		long[] psi_power = fp.mul(left[1], right[1]);
		if (psi_power.length > N) {
			psi_power = Arrays.copyOf(psi_power, N);
		}

		return new long[][]{val, psi_power};
	}
}
