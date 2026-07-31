package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import library.util.polynomial.Monomial;
import library.util.polynomial.MultivariatePolynomial;
import library.util.polynomial.PolynomialFpDynamic;
import java.util.Map;
import java.util.Arrays;
import library.util.MathUtils;
import library.util.linalg.MatrixUtilsFp;
import library.util.linalg.MatrixUtilsZn;

/**
 * P-recursive（ホロノミック）数列の第 n 項を O(√n log n) で求めるクラス。
 *
 * P-recursive 数列とは、各項が前の数項の線形結合で表され、その係数が n の多項式である数列です。
 * 代表的な例として、階乗 (n!)、フィボナッチ数列、攪乱順列、カタラン数などがあります。
 *
 * 数列がベクトル漸化式 v_{i+1} = M(i) v_i を満たすとき、v_n = M(n-1) M(n-2) ... M(0) v_0 となります。
 * このクラスは、遷移行列 M(i) の累積積（Prefix Product）を、通常の O(n) よりも高速な O(√n log n) で計算します。
 *
 * アルゴリズムの背景:
 * 1. ブロックサイズ B ≒ √n を設定します（通常は 2 のべき乗）。
 * 2. 長さ L の積を表す行列多項式 G_L(x) = M(x+L-1) ... M(x) を定義します。
 * 3. ダブリング（Doubling）を用いて、L = 1, 2, 4, ..., B まで G_L を求めていきます。
 *    関係式 G_{2L}(x) = G_L(x+L) * G_L(x) を利用します。
 * 4. G_L(x) をそのまま多項式として持つと、次数が L 倍ずつ増えて計算量が悪化します。
 *    そこで、多項式を直接持つ代わりに、等差数列（グリッド）上の値のみを保持します。
 * 5. ラグランジュ補間の手法（サンプリング点シフト）を用いることで、
 *    既存のグリッド上の値から「延長されたグリッド」や「シフトされたグリッド」での値を
 *    高速（O(L log L)）に計算できることを利用しています。
 */
public class HolonomicSequence {

	/**
	 * 多項式 P(x) に対して、累積積 \prod_{i=0}^{n-1} P(i) を O(√n log n) で計算する。
	 * @param coeffs 多項式の係数 [p_0, p_1, ..., p_d] (P(x) = sum p_i x^i)
	 * @param n 積をとる項数
	 * @param poly 多項式演算器（NTT-friendly な mod であることを推奨）
	 * @return 累積積の結果
	 */
	public static long prefixProduct(long[] coeffs, long n, PolynomialFpDynamic poly) {
		if (n == 0) return 1;
		long mod = poly.mod;

		// 多項式の次数 D を求める（積の多項式の次数 LD を管理するために必要）
		int D = coeffs.length - 1;
		while (D >= 0 && coeffs[D] == 0) D--;
		if (D < 0) return 0; // P(x) = 0 の場合、積は 0
		if (D == 0) return MathUtils.modPow(coeffs[0], n, mod); // P(x) が定数の場合は単なる累乗

		// n が小さい場合はオーバーヘッドを避けるため愚直に計算
		if (n <= 512) {
			long res = 1;
			for (long i = 0; i < n; i++) {
				res = res * poly.evaluate(coeffs, i) % mod;
			}
			return res;
		}

		// B ≒ √n となる 2 のべき乗をブロックサイズとする。
		// これによりダブリングの回数は log √n = (1/2) log n 回になる。
		int k = 0;
		while ((1L << (k + k)) < n) k++;
		int B = 1 << k;
		long invB = MathUtils.modInv(B, mod);

		// vals[i] = G_L(i * B) の値を保持する。
		// G_L(x) は次数 LD なので、一意に定めるには LD+1 個のサンプリング点が必要。
		// 初期状態: L=1, G_1(x) = P(x). サンプリング点は D+1 個用意。
		long[] vals = new long[D + 1];
		for (int i = 0; i <= D; i++) vals[i] = poly.evaluate(coeffs, (long) i * B % mod);

		int L = 1;
		for (int step = 0; step < k; step++) {
			// 現在 vals には [G_L(0), G_L(B), ..., G_L(LD*B)] が入っている。
			// ダブリングにより G_{2L}(x) = G_L(x+L) * G_L(x) を求めたい。
			// 次数は 2LD になる。

			// 1. サンプリング点シフトを用いて、グリッドを 0..LD から 0..2LD に延長する。
			//    vals から G_L(0*B), G_L(1*B), ..., G_L(2LD*B) を得る。
			long[] extended = poly.samplePointShift(vals, 0, 2 * L * D + 1);

			// 2. サンプリング点シフトを用いて、値を L = (L/B)*B だけずらした値を求める。
			//    G_L(L), G_L(L+B), ..., G_L(L+2LD*B) を得る。
			//    samplePointShift は O(N log N) で等差数列上の値を別の等差数列上へ移せる。
			long[] shifted = poly.samplePointShift(vals, L * invB % mod, 2 * L * D + 1);

			// 3. 各点ごとに G_L(x+L) * G_L(x) を計算し、G_{2L} のサンプリング点 2LD+1 個を得る。
			long[] nextVals = new long[2 * L * D + 1];
			for (int i = 0; i <= 2 * L * D; i++) {
				nextVals[i] = extended[i] * shifted[i] % mod;
			}
			vals = nextVals;
			L *= 2;
		}

		// 最終的に L=B となり、vals にはブロックごとの積 G_B(0), G_B(B), ... が入っている。
		// G_B(i*B) = P((i*B)+B-1) * ... * P(i*B)
		long res = 1;
		long i = 0;
		for (; i + B <= n; i += B) {
			res = res * vals[(int) (i / B)] % mod;
		}
		// B の倍数でない端数部分を愚直に計算（最大 B-1 ≒ √n 回）
		for (; i < n; i++) {
			res = res * poly.evaluate(coeffs, i) % mod;
		}
		return res;
	}

	/**
	 * 行列の多項式 M(x) に対して、累積積 M(n-1) M(n-2) ... M(0) を O(d^3 √n log n) で計算する。
	 * @param matrixPoly matrixPoly[r][c] が行列の (r, c) 成分の多項式の係数配列 [c_0, c_1, ...]
	 * @param n 積をとる項数
	 * @param poly 多項式演算器
	 * @return 累積積の結果行列 M(n-1)...M(0)
	 */
	public static long[][] prefixProduct(long[][][] matrixPoly, long n, PolynomialFpDynamic poly) {
		int d = matrixPoly.length;
		long mod = poly.mod;
		if (n == 0) {
			long[][] res = new long[d][d];
			for (int i = 0; i < d; i++) res[i][i] = 1; // 単位行列を返す
			return res;
		}

		// 各成分の多項式の最大次数 D を求める。積の多項式の次数を管理するために必要。
		int D = 0;
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				int deg = matrixPoly[i][j].length - 1;
				while (deg >= 0 && matrixPoly[i][j][deg] == 0) deg--;
				D = Math.max(D, deg);
			}
		}
		if (D < 0) return new long[d][d]; // 全成分 0 の行列
		if (D == 0) {
			// 全成分が定数の場合は、通常の行列累乗 (O(d^3 log n)) で十分
			long[][] base = new long[d][d];
			for (int i = 0; i < d; i++)
				for (int j = 0; j < d; j++)
					base[i][j] = matrixPoly[i][j].length > 0 ? matrixPoly[i][j][0] : 0;
			return MatrixUtilsZn.pow(base, n, mod);
		}

		// 小さい n はオーバーヘッドを嫌って愚直計算
		if (n <= 128) {
			return computeNaive(matrixPoly, 0, n, poly);
		}

		// ブロックサイズ B = 2^k ≒ √n
		int k = 0;
		while ((1L << (k + k)) < n) k++;
		int B = 1 << k;
		long invB = MathUtils.modInv(B, mod);

		// vals[i] = G_L(i * B) = M((iB)+L-1) ... M(iB)
		// 初期状態 L=1
		long[][][] vals = new long[D + 1][d][d];
		for (int i = 0; i <= D; i++) vals[i] = evaluateMatrix(matrixPoly, (long) i * B % mod, poly);

		int L = 1;
		for (int step = 0; step < k; step++) {
			// ダブリングステップ L -> 2L
			long[][][] extended = new long[2 * L * D + 1][d][d];
			long[][][] shifted = new long[2 * L * D + 1][d][d];

			// 行列の全成分（d*d 個）に対して、サンプリング点シフトを適用する。
			// O(d^2 * L log L)
			for (int r = 0; r < d; r++) {
				for (int c = 0; c < d; c++) {
					long[] entryVals = new long[L * D + 1];
					for (int i = 0; i <= L * D; i++) entryVals[i] = vals[i][r][c];

					// サンプリング点を延長 (0..LD -> 0..2LD)
					long[] ext = poly.samplePointShift(entryVals, 0, 2 * L * D + 1);
					// サンプリング点を L シフト
					long[] shi = poly.samplePointShift(entryVals, L * invB % mod, 2 * L * D + 1);

					for (int i = 0; i <= 2 * L * D; i++) {
						extended[i][r][c] = ext[i];
						shifted[i][r][c] = shi[i];
					}
				}
			}

			long[][][] nextVals = new long[2 * L * D + 1][d][d];
			for (int i = 0; i <= 2 * L * D; i++) {
				// G_{2L}(x) = G_L(x+L) * G_L(x)
				// 行列の積は非可換なので順序が重要（左側が後のステップ）
				// O(d^3 * LD)
				nextVals[i] = MatrixUtilsZn.mul(shifted[i], extended[i], mod);
			}
			vals = nextVals;
			L *= 2;
		}

		// 求まった B 個ずつの行列積ブロックを順番に掛けていく
		long[][] res = new long[d][d];
		for (int i_ = 0; i_ < d; i_++) res[i_][i_] = 1;

		long i = 0;
		for (; i + B <= n; i += B) {
			res = MatrixUtilsZn.mul(vals[(int) (i / B)], res, mod);
		}
		// 端数の処理
		if (i < n) {
			res = MatrixUtilsZn.mul(computeNaive(matrixPoly, i, n, poly), res, mod);
		}
		return res;
	}

	/**
	 * P-recursive な漸化式 sum_{i=0}^d P_i(k) a_{k+i} = 0 の第 n 項を O(d^3 √n log n) で求める。
	 *
	 * @param initialValues 初期項 [a_0, a_1, ..., a_{d-1}]
	 * @param Ps Ps[i] が P_i(k) の係数配列
	 * @param n 求めたい項のインデックス
	 * @param poly 多項式演算器
	 * @return a_n
	 */
	public static long nthTerm(long[] initialValues, long[][] Ps, long n, PolynomialFpDynamic poly) {
		int d = Ps.length - 1;
		if (d <= 0) return 0;
		if (n < initialValues.length) return initialValues[(int) n];

		long mod = poly.mod;
		// v_k = [a_{k+d-1}, ..., a_k]^T
		// v_{k+1} = M(k) v_k
		// M(k) = [[-P_{d-1}(k), ..., -P_0(k)],
		//         [P_d(k), 0, ..., 0],
		//         [0, P_d(k), ..., 0]]
		long[][][] matrixPoly = new long[d][d][];
		for (int j = 0; j < d; j++) {
			matrixPoly[0][j] = poly.neg(Ps[d - 1 - j]);
		}
		for (int i = 1; i < d; i++) {
			matrixPoly[i][i - 1] = Ps[d];
		}
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				if (matrixPoly[i][j] == null) matrixPoly[i][j] = new long[0];
			}
		}

		long steps = n - d + 1;
		long[][] prefixM = prefixProduct(matrixPoly, steps, poly);
		long prefixDenom = prefixProduct(Ps[d], steps, poly);

		long[] v0 = new long[d];
		for (int i = 0; i < d; i++) {
			v0[i] = initialValues[d - 1 - i];
		}

		long resVal = 0;
		for (int j = 0; j < d; j++) {
			long term = prefixM[0][j] * v0[j] % mod;
			resVal = (resVal + term) % mod;
		}
		return resVal * MathUtils.modInv(prefixDenom, mod) % mod;
	}

	/**
	 * D-finite な母関数 A(x) = sum a_n x^n の第 n 項を O(d^3 √n log n) で求める。
	 * 母関数は微分方程式 sum_{i=0}^d Q_i(x) A^{(i)}(x) = 0 を満たすとする。
	 * ここで Q_i(x) は x の多項式。
	 *
	 * @param initialValues 初期項 [a_0, a_1, ...]
	 * @param Q 多項式係数 Q[i] が Q_i(x) の係数配列 [q_0, q_1, ...]
	 * @param n 求めたい項のインデックス
	 * @param poly 多項式演算器
	 * @return a_n
	 */
	public static long nthTermOfDfinite(long[] initialValues, long[][] Q, long n, PolynomialFpDynamic poly) {
		int d = Q.length - 1;
		int sMax = -1000000;
		int sMin = 1000000;
		for (int i = 0; i <= d; i++) {
			int deg = Q[i].length - 1;
			while (deg >= 0 && Q[i][deg] == 0) deg--;
			if (deg >= 0) {
				sMin = Math.min(sMin, i - deg);
				int val = 0;
				while (val < Q[i].length && Q[i][val] == 0) val++;
				sMax = Math.max(sMax, i - val);
			}
		}

		if (n < initialValues.length) return initialValues[(int) n];

		int L = sMax - sMin;
		if (L == 0) return 0;

		long mod = poly.mod;
		long[][] Ps = new long[L + 1][];
		for (int s = sMin; s <= sMax; s++) {
			long[] res = new long[0];
			for (int i = 0; i <= d; i++) {
				int j = i - s;
				if (j < 0 || j >= Q[i].length) continue;
				long qij = Q[i][j];
				if (qij == 0) continue;
				// sum_i sum_j q_{i,j} (k+s)(k+s-1)...(k+s-i+1) a_{k+s} = 0
				long[] ff = {1};
				for (int m = 0; m < i; m++) {
					long val = (s - m) % mod;
					if (val < 0) val += mod;
					ff = poly.mul(ff, new long[]{val, 1});
				}
				res = poly.add(res, poly.mul(ff, qij));
			}
			Ps[s - sMin] = res;
		}

		int k0 = Math.max(0, -sMin);
		// シフト分を多項式に反映
		if (k0 > 0) {
			for (int s = 0; s <= L; s++) {
				Ps[s] = poly.taylorShift(Ps[s], k0);
			}
		}

		long[] initialValuesShifted = Arrays.copyOfRange(initialValues, k0 + sMin, initialValues.length);
		return nthTerm(initialValuesShifted, Ps, n - (k0 + sMin), poly);
	}

	/**
	 * P-recursive 数列の第 n 項を計算する。
	 * ベクトル漸化式 v_{i+1} = M(i) v_i に基づき、v_n = M(n-1) M(n-2) ... M(0) v_0 を求めます。
	 *
	 * @param initialValue 初期値ベクトル v_0 (長さ d)
	 * @param matrixPoly 遷移行列 M(x) の各成分を x の多項式 [c_0, c_1, ...] として並べたもの
	 * @param n 求めたい項のインデックス
	 * @param poly 多項式演算器
	 * @return 第 n 項のベクトル v_n
	 */
	public static long[] nthTerm(long[] initialValue, long[][][] matrixPoly, long n, PolynomialFpDynamic poly) {
		// 行列多項式の累積積を O(√n log n) で計算
		long[][] prefix = prefixProduct(matrixPoly, n, poly);
		int d = initialValue.length;
		long[] res = new long[d];
		long mod = poly.mod;
		// 求まった累積積行列と初期値ベクトルの積をとる
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				res[i] = (res[i] + prefix[i][j] * initialValue[j]) % mod;
			}
		}
		return res;
	}

	/**
	 * 行列多項式 M(x) の各成分について値を評価する。
	 */
	private static long[][] evaluateMatrix(long[][][] matrixPoly, long x, PolynomialFpDynamic poly) {
		int d = matrixPoly.length;
		long[][] res = new long[d][d];
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				res[i][j] = poly.evaluate(matrixPoly[i][j], x);
			}
		}
		return res;
	}

	/**
	 * 区間 [start, end) において遷移行列 M(i) の積を愚直に計算する。
	 */
	private static long[][] computeNaive(long[][][] matrixPoly, long start, long end, PolynomialFpDynamic poly) {
		int d = matrixPoly.length;
		long[][] res = new long[d][d];
		for (int i = 0; i < d; i++) res[i][i] = 1; // 単位行列で初期化
		long mod = poly.mod;
		for (long i = start; i < end; i++) {
			// res = M(i) * res
			res = MatrixUtilsZn.mul(evaluateMatrix(matrixPoly, i, poly), res, mod);
		}
		return res;
	}

	/**
	 * 与えられた初期項と漸化式を用いて、数列を指定された長さまで延長します。
	 * 漸化式は sum_{i=0}^d P_i(k) a_{k+i} = 0 です。
	 *
	 * @param initialValues 初期項 [a_0, a_1, ..., a_{d-1}]。d は漸化式の次数。
	 * @param Ps Ps[i] が P_i(k) の係数配列
	 * @param targetLen 延長後の数列 of 数列の長さ。targetLen <= initialValues.length の場合は initialValues のコピー（または targetLen に切り詰めたもの）を返します。
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * maxDeg * log(mod))、ここで d = Ps.length - 1、maxDeg は Ps[i] の最大次数。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][] Ps, int targetLen, PolynomialFpDynamic poly) {
		return extend(initialValues, Ps, 0, targetLen, poly);
	}

	/**
	 * 与えられた初期項（任意の開始インデックス startK を含む）と漸化式を用いて、数列を指定された長さまで延長します。
	 * 漸化式は sum_{i=0}^d P_i(k) a_{k+i} = 0 です。
	 *
	 * @param initialValues 開始インデックスが startK である初期項 [a_{startK}, a_{startK+1}, ..., a_{startK+d-1}]
	 * @param Ps Ps[i] が P_i(k) の係数配列
	 * @param startK 初期項の開始インデックス
	 * @param targetLen 延長後の数列の長さ。targetLen <= initialValues.length の場合は initialValues のコピー（または targetLen に切り詰めたもの）を返します。
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列（a_{startK}, a_{startK+1}, ..., a_{startK+targetLen-1}）。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * maxDeg * log(mod))、ここで d = Ps.length - 1、maxDeg は Ps[i] の最大次数。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][] Ps, int startK, int targetLen, PolynomialFpDynamic poly) {
		if (targetLen <= 0) {
			return new long[0];
		}
		int d = Ps.length - 1;
		if (d <= 0) {
			return Arrays.copyOf(initialValues, targetLen);
		}
		int initLen = initialValues.length;
		if (initLen < d) {
			throw new IllegalArgumentException("The number of initial values must be at least the order of the recurrence relation (Ps.length - 1).");
		}
		long[] res = new long[targetLen];
		for (int i = 0; i < Math.min(initLen, targetLen); i++) {
			res[i] = initialValues[i];
		}
		if (targetLen <= initLen) {
			return res;
		}

		long mod = poly.mod;
		for (int idx = initLen; idx < targetLen; idx++) {
			int k = startK + idx - d;
			long pdVal = poly.evaluate(Ps[d], k);
			if (pdVal % mod == 0) {
				throw new ArithmeticException("P_d(" + k + ") is 0 modulo mod, division by zero is not possible.");
			}
			long invPd = MathUtils.modInv(pdVal, mod);

			long sum = 0;
			for (int i = 0; i < d; i++) {
				long piVal = poly.evaluate(Ps[i], k);
				sum = (sum + piVal * res[idx - d + i]) % mod;
			}
			long nextVal = (mod - sum) % mod * invPd % mod;
			res[idx] = nextVal;
		}
		return res;
	}

	/**
	 * 与えられた数列 s から、P-recursive な漸化式 sum_{i=0}^d P_i(k) a_{k+i} = 0 を推測する。
	 *
	 * @param s 数列の最初の数項
	 * @param maxD 漸化式の最大次数（order）
	 * @param maxG 係数多項式の最大次数（degree）
	 * @param poly 多項式演算器
	 * @return 推測された漸化式の係数多項式 Ps[i] (P_i(k) の係数配列)。見つからない場合は null。
	 *
	 * <p>数学的仕様:
	 * <ul>
	 *   <li>sum_{i=0}^d P_i(k) s_{k+i} ≡ 0 (mod poly.mod) を全項で満たす P_i を探す。</li>
	 *   <li>P_d(k) が恒等的に 0 でない最小の d, g の組を優先して探索する。</li>
	 * </ul>
	 *
	 * <p>事前条件:
	 * <ul>
	 *   <li>s.length は十分大きいこと (s.length > (maxD+1)*(maxG+1) + maxD 程度)。</li>
	 * </ul>
	 *
	 * <p>計算量: O((maxD*maxG) * (s.length * (maxD*maxG)^2)) 程度（連立方程式の求解が支配的）
	 */
	public static long[][] guess(long[] s, int maxD, int maxG, PolynomialFpDynamic poly) {
		int N = s.length;
		long mod = poly.mod;

		for (int d = 1; d <= maxD; d++) {
			for (int g = 0; g <= maxG; g++) {
				int numVars = (d + 1) * (g + 1);
				if (numVars > N - d - 2) continue; // 要求される項数が足りない場合はスキップ

				int numEqs = N - d;
				long[][] mat = new long[numEqs][numVars];

				for (int k = 0; k < numEqs; k++) {
					for (int i = 0; i <= d; i++) {
						long term = s[k + i];
						long kPow = 1;
						for (int j = 0; j <= g; j++) {
							mat[k][i * (g + 1) + j] = term * kPow % mod;
							kPow = kPow * k % mod;
						}
					}
				}

				long[][] ns = MatrixUtilsFp.nullSpace(mat, mod);
				if (ns != null && ns.length > 0 && ns[0].length > 0) {
					// 核空間の基底から、P_d が恒等的に 0 でないものを探す
					for (int col = 0; col < ns[0].length; col++) {
						boolean pdIsZero = true;
						for (int j = 0; j <= g; j++) {
							if (ns[d * (g + 1) + j][col] != 0) {
								pdIsZero = false;
								break;
							}
						}
						if (pdIsZero) continue;

						long[][] res = new long[d + 1][];
						for (int i = 0; i <= d; i++) {
							int last = -1;
							for (int j = 0; j <= g; j++) {
								if (ns[i * (g + 1) + j][col] != 0) last = j;
							}
							if (last == -1) {
								res[i] = new long[0];
							} else {
								res[i] = new long[last + 1];
								for (int j = 0; j <= last; j++) {
									res[i][j] = ns[i * (g + 1) + j][col];
								}
							}
						}
						return res;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 与えられた1変数パラメータ付き初期項と漸化式を用いて、特定のパラメータ値 x における数列を指定された長さまで延長します。
	 * 漸化式は sum_{j=0}^d P_j(k, x) a_{k+j}(x) = 0 です。
	 *
	 * @param initialValues 1変数パラメータ付き初期項 [a_0(x), a_1(x), ...]
	 * @param Ps Ps[j][r][s_val] が k^r x^s_val の係数
	 * @param x パラメータ x
	 * @param targetLen 延長後の数列の長さ
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列の配列。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k, x) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * gk * (gx + log(mod))), ここで d = Ps.length - 1、gk = Ps[0].length - 1、gx = Ps[0][0].length - 1。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][][] Ps, long x, int targetLen, PolynomialFpDynamic poly) {
		return extend(initialValues, Ps, x, 0, targetLen, poly);
	}

	/**
	 * 与えられた1変数パラメータ付き初期項と漸化式を用いて、特定のパラメータ値 x における数列（任意の開始インデックス startK を含む）を指定された長さまで延長します。
	 * 漸化式は sum_{j=0}^d P_j(k, x) a_{k+j}(x) = 0 です。
	 *
	 * @param initialValues 1変数パラメータ付き初期項 [a_{startK}(x), a_{startK+1}(x), ...]
	 * @param Ps Ps[j][r][s_val] が k^r x^s_val の係数
	 * @param x パラメータ x
	 * @param startK 初期項の開始インデックス
	 * @param targetLen 延長後の数列の長さ
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列の配列。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k, x) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * gk * (gx + log(mod))), ここで d = Ps.length - 1、gk = Ps[0].length - 1、gx = Ps[0][0].length - 1。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][][] Ps, long x, int startK, int targetLen, PolynomialFpDynamic poly) {
		if (targetLen <= 0) {
			return new long[0];
		}
		int d = Ps.length - 1;
		long mod = poly.mod;
		int gk = 0;
		for (int j = 0; j <= d; j++) {
			if (Ps[j] != null) {
				gk = Math.max(gk, Ps[j].length - 1);
			}
		}

		long xVal = x % mod;
		if (xVal < 0) xVal += mod;
		long[][] Ps_1D = new long[d + 1][gk + 1];
		for (int j = 0; j <= d; j++) {
			for (int r = 0; r <= gk; r++) {
				if (Ps[j] != null && r < Ps[j].length && Ps[j][r] != null) {
					Ps_1D[j][r] = poly.evaluate(Ps[j][r], xVal);
				} else {
					Ps_1D[j][r] = 0;
				}
			}
		}
		return extend(initialValues, Ps_1D, startK, targetLen, poly);
	}

	/**
	 * 与えられたパラメータ付き数列 s[x][k] から、P-recursive な漸化式 sum_{j=0}^d P_j(k, x) a_{k+j}(x) = 0 を推測します。
	 *
	 * <p>
	 * P_j(k, x) = sum_{r=0}^{gk} sum_{s_val=0}^{gx} c[j][r][s_val] k^r x^s_val (modulo poly.mod) とし、
	 * 各 (x, k) において sum_{j=0}^d sum_{r=0}^{gk} sum_{s_val=0}^{gx} c[j][r][s_val] k^r x^s_val s[x][k+j] = 0 (modulo poly.mod)
	 * を満たす非自明な係数 c を線形方程式の核（null space）を求めることで探索します。
	 * P_d(k, x) が恒等的に 0 でない最小の d, gk, gx の組を優先して探索します。
	 * </p>
	 *
	 * @param s パラメータ付き数列。s[x][k] = a_k(x) を満たす。
	 * @param maxD 漸化式の最大次数（order）
	 * @param maxGK k の最大次数（degree of k）
	 * @param maxGX x の最大次数（degree of x）
	 * @param poly 多項式演算器
	 * @return 推測された漸化式の係数配列 c[j][r][s_val]。見つからない場合は null。
	 * @complexity O(totalEqs * (maxD * maxGK * maxGX)^2)、ここで totalEqs は構成可能な方程式の総数。
	 * // 未テスト
	 */
	public static long[][][] guess(
		long[][] s,
		int maxD,
		int maxGK,
		int maxGX,
		PolynomialFpDynamic poly
	) {
		int X = s.length;
		long mod = poly.mod;

		for (int d = 1; d <= maxD; d++) {
			for (int gk = 0; gk <= maxGK; gk++) {
				for (int gx = 0; gx <= maxGX; gx++) {
					int numVars = (d + 1) * (gk + 1) * (gx + 1);

					int totalEqs = 0;
					for (int x = 0; x < X; x++) {
						if (s[x] != null) {
							totalEqs += Math.max(0, s[x].length - d);
						}
					}

					if (numVars > totalEqs - 2) continue;

					long[][] mat = new long[totalEqs][numVars];
					int row = 0;
					for (int x = 0; x < X; x++) {
						if (s[x] == null) continue;
						long xVal = x % mod;
						int len = s[x].length;
						for (int k = 0; k <= len - 1 - d; k++) {
							for (int i = 0; i <= d; i++) {
								long term = s[x][k + i];
								long kPow = 1;
								for (int r = 0; r <= gk; r++) {
									long xPow = 1;
									for (int s_val = 0; s_val <= gx; s_val++) {
										int col = i * (gk + 1) * (gx + 1) + r * (gx + 1) + s_val;
										mat[row][col] = term * kPow % mod * xPow % mod;
										xPow = xPow * xVal % mod;
									}
									kPow = kPow * k % mod;
								}
							}
							row++;
						}
					}

					long[][] ns = MatrixUtilsFp.nullSpace(mat, mod);
					if (ns != null && ns.length > 0 && ns[0].length > 0) {
						for (int col = 0; col < ns[0].length; col++) {
							boolean pdIsZero = true;
							for (int r = 0; r <= gk; r++) {
								for (int s_val = 0; s_val <= gx; s_val++) {
									int colIdx = d * (gk + 1) * (gx + 1) + r * (gx + 1) + s_val;
									if (ns[colIdx][col] != 0) {
										pdIsZero = false;
										break;
									}
								}
								if (!pdIsZero) break;
							}
							if (pdIsZero) continue;

							long[][][] res = new long[d + 1][gk + 1][gx + 1];
							for (int i = 0; i <= d; i++) {
								for (int r = 0; r <= gk; r++) {
									for (int s_val = 0; s_val <= gx; s_val++) {
										int colIdx = i * (gk + 1) * (gx + 1) + r * (gx + 1) + s_val;
										res[i][r][s_val] = ns[colIdx][col];
									}
								}
							}
							return res;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 与えられた2変数パラメータ付き初期項と漸化式を用いて、特定のパラメータ値 x, y における数列を指定された長さまで延長します。
	 * 漸化式は sum_{j=0}^d P_j(k, x, y) a_{k+j}(x, y) = 0 です。
	 *
	 * @param initialValues 2変数パラメータ付き初期項 [a_0(x, y), a_1(x, y), ...]
	 * @param Ps Ps[j][r][s_val][t] が k^r x^s_val y^t の係数
	 * @param x パラメータ x
	 * @param y パラメータ y
	 * @param targetLen 延長後の数列の長さ
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列の配列。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k, x, y) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * gk * (gx * gy + log(mod))), ここで d = Ps.length - 1、gk = Ps[0].length - 1、gx = Ps[0][0].length - 1、gy = Ps[0][0][0].length - 1。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][][][] Ps, long x, long y, int targetLen, PolynomialFpDynamic poly) {
		return extend(initialValues, Ps, x, y, 0, targetLen, poly);
	}

	/**
	 * 与えられた2変数パラメータ付き初期項と漸化式を用いて、特定のパラメータ値 x, y における数列（任意の開始インデックス startK を含む）を指定された長さまで延長します。
	 * 漸化式は sum_{j=0}^d P_j(k, x, y) a_{k+j}(x, y) = 0 です。
	 *
	 * @param initialValues 2変数パラメータ付き初期項 [a_{startK}(x, y), a_{startK+1}(x, y), ...]
	 * @param Ps Ps[j][r][s_val][t] が k^r x^s_val y^t の係数
	 * @param x パラメータ x
	 * @param y パラメータ y
	 * @param startK 初期項の開始インデックス
	 * @param targetLen 延長後の数列の長さ
	 * @param poly 多項式演算器
	 * @return 長さ targetLen の延長された数列の配列。
	 * @throws IllegalArgumentException 初期項の数が漸化式の次数より少ない場合
	 * @throws ArithmeticException P_d(k, x, y) ≡ 0 (mod poly.mod) となり逆元が存在しない場合
	 * @complexity O(d * targetLen + d * gk * (gx * gy + log(mod))), ここで d = Ps.length - 1、gk = Ps[0].length - 1、gx = Ps[0][0].length - 1、gy = Ps[0][0][0].length - 1。
	 * // 未テスト
	 */
	public static long[] extend(long[] initialValues, long[][][][] Ps, long x, long y, int startK, int targetLen, PolynomialFpDynamic poly) {
		if (targetLen <= 0) {
			return new long[0];
		}
		int d = Ps.length - 1;
		long mod = poly.mod;
		int gk = 0;
		int gx = 0;
		for (int j = 0; j <= d; j++) {
			if (Ps[j] != null) {
				gk = Math.max(gk, Ps[j].length - 1);
				for (int r = 0; r < Ps[j].length; r++) {
					if (Ps[j][r] != null) {
						gx = Math.max(gx, Ps[j][r].length - 1);
					}
				}
			}
		}

		long xVal = x % mod;
		if (xVal < 0) xVal += mod;
		long yVal = y % mod;
		if (yVal < 0) yVal += mod;

		long[][] Ps_1D = new long[d + 1][gk + 1];
		for (int j = 0; j <= d; j++) {
			for (int r = 0; r <= gk; r++) {
				if (Ps[j] != null && r < Ps[j].length && Ps[j][r] != null) {
					long[] coeffX = new long[gx + 1];
					for (int s_val = 0; s_val <= gx; s_val++) {
						if (s_val < Ps[j][r].length && Ps[j][r][s_val] != null) {
							coeffX[s_val] = poly.evaluate(Ps[j][r][s_val], yVal);
						} else {
							coeffX[s_val] = 0;
						}
					}
					Ps_1D[j][r] = poly.evaluate(coeffX, xVal);
				} else {
					Ps_1D[j][r] = 0;
				}
			}
		}
		return extend(initialValues, Ps_1D, startK, targetLen, poly);
	}

	/**
	 * 与えられた 2 変数パラメータ付き数列
	 * {@code a_k(x, y) = s[x][y][k]} から、
	 * P-recursive な線形漸化式を推測します。
	 *
	 * <p>探索する漸化式は
	 *
	 * <pre>{@code
	 * Σ_{j=0}^d P_j(k, x, y) a_{k+j}(x, y) = 0
	 * }</pre>
	 *
	 * の形であり、各係数多項式 {@code P_j} は
	 *
	 * <pre>{@code
	 * P_j(k,x,y)
	 *   = Σ c[j][r][sx][ty] k^r x^sx y^ty
	 * }</pre>
	 *
	 * と仮定します。
	 *
	 * <p>次数 {@code d, gk, gx, gy} を小さい順に列挙し、
	 * 全ての利用可能な {@code (x, y, k)} に対して上式を満たすような
	 * 係数 {@code c} を未知数とする線形方程式系を構成します。
	 * その零空間（null space）を求めることで漸化式を推測します。
	 *
	 * <p>係数多項式 {@code P_d} が恒等的に 0 である解は
	 * 実際の次数がより小さい漸化式に対応するため除外します。
	 * 最初に見つかった解を返し、見つからなければ {@code null} を返します。
	 *
	 * @param s
	 *     数列 {@code s[x][y][k] = a_k(x,y)}。
	 *     {@code x} および {@code y} はパラメータ、
	 *     第 3 添字が漸化式の添字 {@code k} を表す。
	 * @param maxD 探索する漸化式次数の上限
	 * @param maxGK {@code k} に関する係数多項式の最大次数
	 * @param maxGX {@code x} に関する係数多項式の最大次数
	 * @param maxGY {@code y} に関する係数多項式の最大次数
	 * @param poly 有限体上の多項式演算器
	 * @return
	 *     推測された係数配列
	 *     {@code c[j][r][sx][ty]}。
	 *     これは
	 *     {@code k^r x^sx y^ty} の係数を表す。
	 *     漸化式が見つからなければ {@code null}。
	 *
	 * @complexity
	 * O(EV²)。
	 * ここで
	 * {@code E} は生成される線形方程式数、
	 * {@code V=(d+1)(gk+1)(gx+1)(gy+1)}
	 * は未知数の個数である。
	 */
	public static long[][][][] guess(
		long[][][] s,
		int maxD,
		int maxGK,
		int maxGX,
		int maxGY,
		PolynomialFpDynamic poly
	) {
		int X = s.length;
		long mod = poly.mod;

		for (int d = 1; d <= maxD; d++) {
			for (int gk = 0; gk <= maxGK; gk++) {
				for (int gx = 0; gx <= maxGX; gx++) {
					for (int gy = 0; gy <= maxGY; gy++) {
						int numVars = (d + 1) * (gk + 1) * (gx + 1) * (gy + 1);

						int totalEqs = 0;
						for (int x = 0; x < X; x++) {
							if (s[x] != null) {
								int Y = s[x].length;
								for (int y = 0; y < Y; y++) {
									if (s[x][y] != null) {
										totalEqs += Math.max(0, s[x][y].length - d);
									}
								}
							}
						}

						if (numVars > totalEqs - 2) continue;

						long[][] mat = new long[totalEqs][numVars];
						int row = 0;
						for (int x = 0; x < X; x++) {
							if (s[x] == null) continue;
							long xVal = x % mod;
							int Y = s[x].length;
							for (int y = 0; y < Y; y++) {
								if (s[x][y] == null) continue;
								long yVal = y % mod;
								int len = s[x][y].length;
								for (int k = 0; k <= len - 1 - d; k++) {
									for (int i = 0; i <= d; i++) {
										long term = s[x][y][k + i];
										long kPow = 1;
										for (int r = 0; r <= gk; r++) {
											long xPow = 1;
											for (int s_val = 0; s_val <= gx; s_val++) {
												long yPow = 1;
												for (int t = 0; t <= gy; t++) {
													int col = i * (gk + 1) * (gx + 1) * (gy + 1) + r * (gx + 1) * (gy + 1) + s_val * (gy + 1) + t;
													mat[row][col] = term * kPow % mod * xPow % mod * yPow % mod;
													yPow = yPow * yVal % mod;
												}
												xPow = xPow * xVal % mod;
											}
											kPow = kPow * k % mod;
										}
									}
									row++;
								}
							}
						}

						long[][] ns = MatrixUtilsFp.nullSpace(mat, mod);
						if (ns != null && ns.length > 0 && ns[0].length > 0) {
							for (int col = 0; col < ns[0].length; col++) {
								boolean pdIsZero = true;
								for (int r = 0; r <= gk; r++) {
									for (int s_val = 0; s_val <= gx; s_val++) {
										for (int t = 0; t <= gy; t++) {
											int colIdx = d * (gk + 1) * (gx + 1) * (gy + 1) + r * (gx + 1) * (gy + 1) + s_val * (gy + 1) + t;
											if (ns[colIdx][col] != 0) {
												pdIsZero = false;
												break;
											}
										}
										if (!pdIsZero) break;
									}
									if (!pdIsZero) break;
								}
								if (pdIsZero) continue;

								long[][][][] res = new long[d + 1][gk + 1][gx + 1][gy + 1];
								for (int i = 0; i <= d; i++) {
									for (int r = 0; r <= gk; r++) {
										for (int s_val = 0; s_val <= gx; s_val++) {
											for (int t = 0; t <= gy; t++) {
												int colIdx = i * (gk + 1) * (gx + 1) * (gy + 1) + r * (gx + 1) * (gy + 1) + s_val * (gy + 1) + t;
												res[i][r][s_val][t] = ns[colIdx][col];
											}
										}
									}
								}
								return res;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Picard-Fuchs 演算子 L = sum a_k(t) delta^k から数列の 第 n 項を計算する。
	 * 注意: delta = d/dt。
	 *
	 * @param L Picard-Fuchs 演算子の係数 a_k(t)。
	 * @param initialValues 初期項 [a_0, a_1, ...]。
	 * @param n 求めたい項のインデックス。
	 * @param poly 多項式演算器。
	 * @return 第 n 項の値。
	 * 計算量: O(d^3 sqrt(n) log n)。
	 */
	public static long nthTermFromPicardFuchs(List<MultivariatePolynomial<Long>> L, long[] initialValues, long n, PolynomialFpDynamic poly) {
		long mod = poly.mod;

		// Picard-Fuchs L(A(t)) = 0 は D-finite 微分方程式 sum a_i(t) A^(i)(t) = 0。
		// nthTermOfDfinite はまさにこの形式の微分方程式の級数解の係数を計算する。

		long[][] Q = new long[L.size()][];
		for (int i = 0; i < L.size(); i++) {
			MultivariatePolynomial<Long> ai = L.get(i);
			int maxDegT = 0;
			for(Monomial m : ai.getTerms().keySet()) maxDegT = Math.max(maxDegT, m.getExponent(m.size()-1));
			Q[i] = new long[maxDegT + 1];
			for(Map.Entry<Monomial, Long> entry : ai.getTerms().entrySet()) {
				Q[i][entry.getKey().getExponent(entry.getKey().size()-1)] = entry.getValue();
			}
		}

		return nthTermOfDfinite(initialValues, Q, n, poly);
	}
}
