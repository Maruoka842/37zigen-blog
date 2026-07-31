package library.util.polynomial;

import java.util.Arrays;
import library.util.MathUtils;

/**
 * 1次元有限区間 [0, M) 上のランダムウォークにおける経路数またはパスの重み総和を計算するライブラリです。
 */
public class IntervalWalk {

	/**
	 * 制限区間 0 <= x < M 内に終始とどまる歩行について、0 から maxN までのすべてのステップ数 n に対し、
	 * 位置 k に到達する経路の重みの総和を一括して計算し、配列として返します。
	 *
	 * <p>【数学的定義と関数の仕様】</p>
	 * 以下の歩行モデルおよび初期分布のもとで、すべての中間ステップにおいて 0 <= x < M 内にとどまり、
	 * 各 n in [0, maxN] について位置 k (0 <= k < M) に到達する歩行の重み総和 [x^k] C_reflected(x) A(x)^n を一括して計算します。
	 *
	 * <p>1. <b>歩行モデル:</b></p>
	 * <ul>
	 *   <li>1ステップの移動重みを表す Laurent 多項式を A(x) = sum a_s * x^s とします。</li>
	 *   <li>左右対称な歩行（a_s = a_{-s}、すなわち A(x) = A(x^-1)）を仮定します。</li>
	 *   <li>y = A(x) とおいたとき、複数ある逆関数のうち y -> infinity において phi(y) -> 0 となる逆関数を x = phi(y) = A^-1(y) とします。</li>
	 *   <li>w = 1/y とおき、この逆関数を w の冪級数 psi(w) = phi(1/w) と定義します。これは psi(0) = 0 を満たします。</li>
	 * </ul>
	 *
	 * <p>2. <b>初期分布:</b></p>
	 * <ul>
	 *   <li>初期位置の分布多項式を C(x) = sum_{i=0}^{M-1} c_i * x^i とします。</li>
	 *   <li>反転分布多項式を C_rev(x) = sum_{i=0}^{M-1} c_i * x^(M - 1 - i) とします。</li>
	 *   <li>本メソッドに渡す C_psi は、合成された冪級数 C(psi(w)) mod w^N の係数配列です。</li>
	 *   <li>本メソッドに渡す C_rev_psi は、合成された冪級数 C_rev(psi(w)) mod w^N の係数配列です。</li>
	 * </ul>
	 *
	 * <p>3. <b>事前条件:</b></p>
	 * - M > 0
	 * - 0 <= k < M
	 * - maxN >= 0
	 * - psi.length >= N + 1 （ただし N = maxN + E + 1、E = M + k + 1 - j_min * D）
	 * - C_psi.length >= N
	 * - C_rev_psi.length >= N
	 *
	 * @param fp 多項式演算器
	 * @param C_psi 冪級数 C(psi(w)) mod w^N の係数配列
	 * @param C_rev_psi 冪級数 C_rev(psi(w)) mod w^N の係数配列
	 * @param psi A(x) の逆関数 psi(w) = phi(1/w) の係数配列
	 * @param M 区間の上限（ non-inclusive, 歩行可能範囲は 0 <= x < M ）
	 * @param k 目標位置（ 0 <= k < M ）
	 * @param maxN 歩行ステップ数の上限
	 * @return インデックス n がステップ数 n に対応する重みの総和配列。サイズは maxN + 1
	 *
	 * 計算量: O(maxN log maxN)
	 * // 未テスト
	 */
	public static long[] solveVaryingN(PolynomialFpDynamic fp, long[] C_psi, long[] C_rev_psi, long[] psi, int M, long k, int maxN) {
		if (M <= 0 || k < 0 || k >= M || maxN < 0) {
			return new long[0];
		}

		// --- アルゴリズムの数学的背景 ---
		// 1. 反射原理による境界表現:
		//    吸収壁が -1 と M にあるため、反射周期は D = 2(M + 1)。
		//    有効な歩行の重み和は、鏡像群からの遷移数の交代和となる:
		//    sum_j [x^(k - j*D)] ( C(x) - C(x^-1) * x^-2 ) * A(x)^n
		//    ステップ数が n であるため、寄与する j は j_min <= j <= j_max の有限範囲に限られる。
		//
		// 2. 逆関数法（Lagrange-Bürmann）による変換:
		//    x = phi(y)（A(phi(y)) = y、y -> infinity で phi(y) -> 0）とし、w = 1/y と置くと
		//    psi(w) = phi(1/w) は psi(0) = 0 なwのべき級数となる。
		//    係数抽出を w 上のべき級数に写像すると、各 j の寄与は以下の通り:
		//    [w^n] ( C(psi(w)) * psi(w)^(-k + j*D) - C(psi(w)^-1) * psi(w)^(-k - 2 + j*D) ) * (w * psi'(w) / psi(w))
		//
		// 3. 有限等比数列による一括集約:
		//    すべての寄与する j に対する和について、最も負のべきをクリアするため psi(w)^E を掛け合わせる。
		//    ここで E = M + k + 1 - j_min * D。
		//    これにより負のべきは完全に解消され、分子 U(w) および分母 V(w) = 1 - psi(w)^D による等比級数に集約される。
		//    Q(w) = U(w)/V(w) mod w^N、H(w) = psi(w)/w と置いて
		//    S_k(w) = Q(w) * psi'(w) * H(w)^(-E - 1) の w^(n + E) の係数が求める解となる。
		//
		// 4. maxN を介したステップ数 n の一括取得:
		//    E および E を介した各 w のべきは maxN を最大基準として一括定義されます。
		//    S_k(w) = Q(w) * psi'(w) * H(w)^(-E - 1) は n に依存せず独立に確定するため、
		//    この系列を 1 度だけ N = maxN + E 次まで展開すれば、
		//    任意の n in [0, maxN] に対する解 [x^k] C_reflected(x) A(x)^n は、
		//    S_k(w) の w^(n + E) の係数を読み出すだけで一括取得できます。

		long D = 2L * (M + 1);
		long j_min = (long) Math.ceil((double) (k - maxN - (M - 1)) / D);
		long j_max = (long) Math.floor((double) (maxN + M + k + 1) / D);

		long E = M + k + 1 - j_min * D;
		int totalLimit = (int) (maxN + E);
		int N = totalLimit + 1;

		if (psi.length < N + 1) {
			psi = Arrays.copyOf(psi, N + 1);
		}
		if (C_psi.length < N) {
			C_psi = Arrays.copyOf(C_psi, N);
		}
		if (C_rev_psi.length < N) {
			C_rev_psi = Arrays.copyOf(C_rev_psi, N);
		}

		long pow1 = -k + j_min * D + E;
		long pow2 = - (M - 1) - 2 - k + j_min * D + E;

		// psi_pow1, psi_pow2 (Copy up to N + 1 to include psi[N])
		long[] psi_pow1 = fp.pow(Arrays.copyOf(psi, N + 1), pow1);
		long[] psi_pow2 = fp.pow(Arrays.copyOf(psi, N + 1), pow2);

		long[] term1 = fp.mul(Arrays.copyOf(C_psi, N), Arrays.copyOf(psi_pow1, N));
		if (term1.length > N) {
			term1 = Arrays.copyOf(term1, N);
		}
		long[] term2 = fp.mul(Arrays.copyOf(C_rev_psi, N), Arrays.copyOf(psi_pow2, N));
		if (term2.length > N) {
			term2 = Arrays.copyOf(term2, N);
		}

		long[] diff = new long[N];
		for (int i = 0; i < N; i++) {
			long val1 = i < term1.length ? term1[i] : 0;
			long val2 = i < term2.length ? term2[i] : 0;
			diff[i] = fp.subMod(val1, val2);
		}

		// factor = 1 - psi^{(j_max - j_min + 1) * D} mod w^N
		long num_D = (j_max - j_min + 1) * D;
		long[] psi_pow_num_D = fp.pow(Arrays.copyOf(psi, N + 1), num_D);
		long[] factor = new long[N];
		factor[0] = 1;
		for (int i = 0; i < N; i++) {
			factor[i] = fp.subMod(factor[i], psi_pow_num_D[i]);
		}

		// U(w) = diff * factor mod w^N
		long[] U = fp.mul(diff, factor);
		if (U.length > N) {
			U = Arrays.copyOf(U, N);
		}

		// V(w) = 1 - psi^D mod w^N
		long[] psi_pow_D = fp.pow(Arrays.copyOf(psi, N + 1), D);
		long[] V = new long[N];
		V[0] = 1;
		for (int i = 0; i < N; i++) {
			V[i] = fp.subMod(V[i], psi_pow_D[i]);
		}

		// Q(w) = U(w) / V(w) mod w^N
		long[] inv_V = fp.inv(V);
		long[] Q = fp.mul(U, inv_V);
		if (Q.length > N) {
			Q = Arrays.copyOf(Q, N);
		}

		// H(w) = psi(w) / w
		long[] H = new long[N];
		for (int i = 0; i < N; i++) {
			H[i] = psi[i + 1];
		}

		// H(w)^(-E - 1)
		long[] H_inv = fp.inv(H);
		long[] H_inv_pow = fp.pow(H_inv, E + 1);

		// psi'(w)
		long[] psi_prime = fp.differentiate(psi);

		// S_k(w) = Q(w) * psi'(w) * H(w)^(-E - 1) mod w^N
		long[] S_k = fp.mul(Q, psi_prime);
		S_k = fp.mul(S_k, H_inv_pow);

		long[] ans = new long[maxN + 1];
		for (int n = 0; n <= maxN; n++) {
			int targetIdx = n + (int) E;
			if (targetIdx < S_k.length) {
				ans[n] = fp.getFp().reduce(S_k[targetIdx]);
			}
		}
		return ans;
	}

	/**
	 * 制限区間 0 <= x < M 内に終始とどまる歩行について、0 から maxN までのすべてのステップ数 n に対し、
	 * 位置 k に到達する経路の重みの総和を一括して計算し、配列として返します。
	 * 本オーバーロードは、移動 Laurent 多項式 A(x) の非負部分の係数 a_i (i >= 0) の配列である A_poly を受け取り、
	 * 逆関数 psi(w) と初期分布の合成 C_psi, C_rev_psi を内部的に O(N log^2 N) の多項式合成によって自動計算します。
	 *
	 * <p>【数学的定義と関数の仕様】</p>
	 * 以下の歩行モデルおよび初期分布のもとで、各 n in [0, maxN] について [x^k] C_reflected(x) A(x)^n の重み総和を一括して計算します。
	 *
	 * <p>1. <b>歩行モデル:</b></p>
	 * <ul>
	 *   <li>1ステップの移動重みを表す左右対称な Laurent 多項式 A(x) = sum_{i=-S}^{S} a_i x^i（ただし a_i = a_{-i}）に対し、
	 *       非負の次数 i >= 0 の係数 a_i のみを並べた配列 A_poly = [a_0, a_1, ..., a_S] を受け取ります。</li>
	 *   <li>歩行の対称性より、シフト多項式 A_poly_symmetric(x) = A(x) * x^S の次数は 2S となり、S = A_poly.length - 1 と一意に定まります。</li>
	 *   <li>F(x) = x^S / A_poly_symmetric(x) とおいたとき、F(x) は F(0) = 0, F'(0) != 0 を満たします。</li>
	 *   <li>w = F(x) とおき、その合成逆関数を psi(w) = F^-1(w) と定義します。保存次数 psi(0) = 0 を満たします。</li>
	 * </ul>
	 *
	 * <p>2. <b>初期分布:</b></p>
	 * <ul>
	 *   <li>初期位置の分布多項式を C(x) = sum_{i=0}^{M-1} c_i * x^i とします。</li>
	 *   <li>反転分布多項式を C_rev(x) = sum_{i=0}^{M-1} c_i * x^(M - 1 - i) とします。</li>
	 * </ul>
	 *
	 * <p>3. <b>事前条件:</b></p>
	 * - M > 0
	 * - 0 <= k < M
	 * - maxN >= 0
	 * - A_poly.length >= 2 （対称歩行かつ S >= 1）
	 * - C.length <= M
	 *
	 * @param fp 多項式演算器
	 * @param C 0-based 初期分布多項式の係数配列
	 * @param A_poly A(x) = sum a_i x^i における非負の次数 i >= 0 の係数配列 [a_0, a_1, ..., a_S]
	 * @param M 区間の上限（ non-inclusive, 歩行可能範囲は 0 <= x < M ）
	 * @param k 目標位置（ 0 <= k < M ）
	 * @param maxN 歩行ステップ数の上限
	 * @return インデックス n がステップ数 n に対応する重みの総和配列。サイズは maxN + 1
	 *
	 * 計算量: O(maxN log^2 maxN + M log M)
	 * // 未テスト
	 */
	public static long[] solveVaryingN(PolynomialFpDynamic fp, long[] C, long[] A_poly, int M, long k, int maxN) {
		if (M <= 0 || k < 0 || k >= M || maxN < 0) {
			return new long[0];
		}

		long D = 2L * (M + 1);
		long j_min = (long) Math.ceil((double) (k - maxN - (M - 1)) / D);
		long E = M + k + 1 - j_min * D;
		int totalLimit = (int) (maxN + E);
		int N = totalLimit + 1;

		int S = A_poly.length - 1;
		long[] symmetricA = new long[2 * S + 1];
		for (int i = 0; i <= S; i++) {
			symmetricA[S - i] = A_poly[i];
			symmetricA[S + i] = A_poly[i];
		}

		// F(x) = x^S / A_poly(x) mod x^(N+1) を計算
		int invSize = N + 1 - S;
		if (invSize < 1) invSize = 1;
		long[] A_inv = fp.inv(Arrays.copyOf(symmetricA, invSize));
		long[] F = new long[N + 1];
		for (int i = 0; i < A_inv.length && i + S <= N; i++) {
			F[i + S] = A_inv[i];
		}

		// psi(w) = F^-1(w) mod w^(N+1) を合成逆関数により計算
		long[] psi = fp.compInverse(F);

		// C_new(x) = C(x)
		long[] C_new = Arrays.copyOf(C, M);
		long[] C_rev = new long[M];
		for (int i = 0; i < M; i++) {
			C_rev[i] = C_new[M - 1 - i];
		}

		// C_new(psi(w)) mod w^N および C_rev(psi(w)) mod w^N を O(N log^2 N) の一般合成により計算
		long[] C_psi = fp.comp(C_new, psi, N);
		long[] C_rev_psi = fp.comp(C_rev, psi, N);

		return solveVaryingN(fp, C_psi, C_rev_psi, psi, M, k, maxN);
	}

	/**
	 * 制限区間 0 <= x < M 内に終始とどまる歩行について、固定されたステップ数 n に対し、
	 * すべての目標位置 k in [0, M) に到達する経路の重みの総和を一括して計算し、配列として返します。
	 * 本メソッドは、多項式ダブリング（繰り返し二乗法）を用いて歩行多項式 A(x)^n mod (x^D - 1) を O(M log M log n) で計算します。
	 *
	 * <p>【数学的定義と関数の仕様】</p>
	 * 左右対称なステップ移動多項式 A(x) = sum a_s * x^s （a_s = a_{-s}）および
	 * 初期分布多項式 C(x) = sum_{i=0}^{M-1} c_i * x^i に対し、
	 * 固定された n に対する各 k in [0, M) の制限付き歩行数 [x^k] C_reflected(x) A(x)^n を一括して計算します。
	 *
	 * <p>【事前条件】</p>
	 * - M > 0
	 * - n >= 0
	 * - A_poly.length >= 2 （対称歩行かつ S >= 1）
	 * - C.length <= M
	 *
	 * @param fp 多項式演算器
	 * @param C 0-based 初期分布多項式の係数配列
	 * @param A_poly A(x) = sum a_i x^i における非負の次数 i >= 0 の係数配列 [a_0, a_1, ..., a_S]
	 * @param M 区間の上限（ non-inclusive, 歩行可能範囲は 0 <= x < M ）
	 * @param n 固定された歩行ステップ数
	 * @return インデックス k が目標位置 k に対応する重みの総和配列。サイズは M
	 *
	 * 計算量: O(M log M log n)
	 * // 未テスト
	 */
	public static long[] solveVaryingK(PolynomialFpDynamic fp, long[] C, long[] A_poly, int M, long n) {
		//https://atcoder.jp/contests/abc309/submissions/77707406
		if (M <= 0) {
			return new long[0];
		}
		if (n < 0) {
			return new long[M];
		}

		long D = 2L * (M + 1);
		int S = A_poly.length - 1;
		long[] symmetricA = new long[2 * S + 1];
		for (int i = 0; i <= S; i++) {
			symmetricA[S - i] = A_poly[i];
			symmetricA[S + i] = A_poly[i];
		}

		// 1. C_reflected(x) = C(x) - C(x^-1) * x^-2 mod (x^D - 1) の代わりに C(x) のみをそのままコピー
		long[] P = Arrays.copyOf(C, Math.min(C.length, M));

		// 2. A_poly(x)^n mod (x^D - 1) を繰り返し二乗法により計算
		long[] A_poly_D = reduceModulo(fp, symmetricA, D);
		long[] A_n_poly = powModulo(fp, A_poly_D, n, D);

		// 3. C(x) * A_poly(x)^n mod (x^D - 1) を計算し、未反転の多項式 U(x) を得る
		long[] U = reduceModulo(fp, fp.mul(A_n_poly, P), D);

		// 4. A(x)^n = A_poly(x)^n * x^(-n * S) mod (x^D - 1) によるシフト処理
		// および C_reflected(x) = C(x) - C(x^-1) * x^-2 mod (x^D - 1) による反転処理を
		// 係数の抽出時に同時に適用して、目標位置 k in [0, M) に対応する係数を一括して抽出
		long shift = n * S;
		long[] ans = new long[M];
		for (int i = 0; i < M; i++) {
			long idx1 = (i + shift) % D;
			long idx2 = (2L * M - i + shift) % D;
			long val1 = idx1 < U.length ? U[(int) idx1] : 0;
			long val2 = idx2 < U.length ? U[(int) idx2] : 0;
			ans[i] = fp.subMod(val1, val2);
		}
		return ans;
	}

	/**
	 * 多項式 Z(x) を法 x^D - 1 で剰余縮約します。
	 */
	private static long[] reduceModulo(PolynomialFpDynamic fp, long[] Z, long D) {
		int len = (int) D;
		if (Z.length <= len) {
			return Z;
		}
		long[] res = Arrays.copyOf(Z, len);
		long mod = fp.mod;
		if (Z.length <= 2 * len) {
			for (int i = len; i < Z.length; i++) {
				int target = i - len;
				long val = res[target] + Z[i];
				if (val >= mod) val -= mod;
				res[target] = val;
			}
		} else {
			for (int i = len; i < Z.length; i++) {
				int target = i % len;
				long val = res[target] + Z[i];
				if (val >= mod) val -= mod;
				res[target] = val;
			}
		}
		return res;
	}

	/**
	 * 多項式 base^n mod (x^D - 1) を繰り返し二乗法により計算します。
	 */
	private static long[] powModulo(PolynomialFpDynamic fp, long[] base, long n, long D) {
		long[] res = new long[]{1};
		long[] cur = base.clone();
		while (n > 0) {
			if ((n & 1) == 1) {
				res = reduceModulo(fp, fp.mul(res, cur), D);
			}
			cur = reduceModulo(fp, fp.mul(cur, cur), D);
			n >>= 1;
		}
		return reduceModulo(fp, res, D);
	}
}
