package library.util.graph;

import java.util.Arrays;

import library.util.Fp;
import library.util.Itertools;
import library.util.MathUtils;
import library.util.polynomial.PolynomialFp;
import library.util.polynomial.PolynomialFpDynamic;

/**
 * 無向完全グラフをハミルトンパス、ハミルトンサイクル、マッチングなどに分解するユーティリティクラス。
 */
public class CountGraph {
	/**
	 * ret[i] = n頂点でi個の有向サイクルからなるものの数。n=0のときはret[0]=1。 x(x+1)..(x+n-1)
	 * 
	 * @param n
	 * @return
	 */
	public static long[] countCyclePartition(int n, long mod) {
		if (n == 0)
			return new long[] { 1 };
		var P = PolynomialFpDynamic.of(mod);
		long[] g = countCyclePartition(n / 2, mod);
		long[] h = P.taylorShift(g, n / 2);
		if (n % 2 == 1)
			return PolynomialFp.mul(PolynomialFp.mul(g, h), new long[] { n - 1, 1 });
		return PolynomialFp.mul(g, h);
	}

	/**
	 * 1,2,..,nをk（≤maxBlocks)個の非空な集合に分割する方法の数S(n, k)を列挙。 a[k] = [x^n/n!](exp(x)-1)^k
	 * / k!
	 * 
	 * @param n         分割する要素の総数
	 * @param maxBlocks 考慮する最大ブロック数（k ≤ maxBlocks）
	 * @return 長さ maxBlocks+1 の配列 a で、a[k] は n 個の要素を k 個の非空集合に分割する方法の数 @see
	 *         https://atcoder.jp/contests/abc327/submissions/72331809
	 */
	public static long[] countKnPartition(long n, int maxBlocks, long mod) {
		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);
		long[] f = new long[maxBlocks + 1];
		long[] g = new long[maxBlocks + 1];
		for (int i = 0; i <= maxBlocks; i++) {
			f[i] = fp.pow(i, n) * fp.ifac(i) % mod;
			g[i] = (i % 2 == 0 ? 1 : (mod - 1)) * fp.ifac(i) % mod;
		}
		long[] fg = P.mul(f, g);
		return Arrays.copyOf(fg, maxBlocks + 1);
	}

	/**
	 * 第2種スターリング数 S(n, k) を k を固定して n = 0, 1, ..., maxN について列挙する。
	 *
	 * <p>数学的仕様:
	 * $S(n, k)$ は $n$ 個の区別できる要素を $k$ 個の区別できない非空な集合に分割する方法の数である。
	 * 指数型母関数（EGF）の関係式 \sum_{n=0}^{\infty} S(n, k) \frac{x^n}{n!} = \frac{(e^x - 1)^k}{k!} を用いて計算する。
	 *
	 * <p>計算量:
	 * <ul>
	 *   <li>時間計算量: O(maxN log maxN) （FFT/NTTが利用可能な場合）</li>
	 *   <li>空間計算量: O(maxN)</li>
	 * </ul>
	 *
	 * @param k        固定するブロック数
	 * @param maxN     考慮する最大要素数（n <= maxN）
	 * @param mod      法
	 * @return         長さ maxN + 1 の配列 ret で、ret[n] = S(n, k)
	 *
	 * 未テスト
	 */
	public static long[] countKnPartitionFixedK(int k, int maxN, long mod) {
		if (maxN < 0) {
			return new long[0];
		}
		long[] ret = new long[maxN + 1];
		if (k < 0 || k >= mod || k > maxN) {
			return ret;
		}
		if (k == 0) {
			ret[0] = 1 % mod;
			return ret;
		}

		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);

		long[] f = new long[maxN + 1];
		for (int i = 1; i <= maxN; i++) {
			f[i] = fp.ifac(i);
		}

		long[] pk = P.pow(f, k);

		long invKFac = fp.ifac(k);
		for (int n = 0; n <= maxN; n++) {
			ret[n] = pk[n] * fp.fac(n) % mod * invKFac % mod;
		}
		return ret;
	}

	/**
	 * a[i,j]=i頂点j辺の連結グラフの個数としたときの sum[i=1..N-1] a[i,j](edgeWeight)^j x^i/i! を返す
	 * 
	 * @param N
	 * @param edgeWeight
	 * @return verified:https://atcoder.jp/contests/fps-24/submissions/70671922
	 */
	public static long[] connectedGraphEGF(int N, long edgeWeight, long mod) {
		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);
		long[] f = new long[N];
		for (int j = 1; j < N; ++j) {
			f[j] = (fp.pow(1 + edgeWeight, 1L * j * (j - 1) / 2) * fp.ifac(j)) % mod;
		}
		f[0] = 1;
		f = P.log(f);
		return f;
	}

	/**
	 * https://oeis.org/A001832
	 * 
	 * @param N
	 * @return
	 */
	public static long[] connectedBipartiteGraphEGF(int N, long mod) {
		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);
		long[] f = new long[N];
		long i2 = fp.inv(2);
		for (int i = 0; i < N; i++) {
			f[i] = fp.pow(i2, 1L * i * (i - 1) / 2) * fp.ifac(i) % mod;
		}
		f = P.mul(f, f);
		for (int i = 0; i < f.length; i++) {
			f[i] = fp.pow(2, 1L * i * (i - 1) / 2) * f[i] % mod;
		}
		f = P.log(f);
		for (int i = 0; i < f.length; i++) {
			f[i] = f[i] * i2 % mod;
		}
		return f;
	}

	/**
	 * n 頂点までのラベル付き二重連結グラフ（1点削除しても連結）の個数を列挙する。
	 * 計算量: O(n log^2 n)
	 * @param n   最大頂点数
	 * @param mod 法
	 * @return a[i] が i 頂点のラベル付き二重連結グラフの個数である長さ n+1 の配列
	 * @see https://oeis.org/A013922
	 */
	public static long[] countLabeledBiconnected(int n, long mod) {
		if (n < 0)
			return new long[0];
		if (n == 0)
			return new long[] { 0 };
		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);
		// C(x) はラベル付き連結グラフの EGF
		long[] C = connectedGraphEGF(n + 1, 1, mod);

		// D(x) = C'(x)
		long[] D = new long[n];
		for (int i = 0; i < n; i++) {
			D[i] = C[i + 1] * (i + 1) % mod;
		}

		//　根付き連結グラフ E(x) = x C'(x)
		long[] E = new long[n + 1];
		for (int i = 0; i < n; i++) {
			E[i + 1] = D[i];
		}
		
		// E(x) = x exp(B'(E(x))) より
		// C'(x) = exp(B'(E(x))
		// B'(x) = log(C'(IE(x)))
		
		// G(x) = log(D(x))
		long[] G = P.log(D);

		// IE(x) = E^{-1}(x)
		long[] IE = P.compInverse(E);

		// B'(x) = G(IE(x))
		long[] Bprime = P.comp(G, IE, n);

		// B(x) = ∫ B'(x) dx
		long[] B = new long[n + 1];
		for (int i = 0; i < Bprime.length; i++) {
			B[i + 1] = Bprime[i] * fp.inv(i + 1) % mod;
		}

		for (int i = 0; i <= n; i++) {
			B[i] = B[i] * fp.fac(i) % mod;
		}
		return B;
	}

	/**
	 * n 頂点のラベル付き強トーナメント（strong tournaments）の個数を列挙する。
	 *
	 * <p>数学的仕様:
	 * $T(x) = \sum_{n=0}^{\infty} 2^{\binom{n}{2}} \frac{x^n}{n!}$ をトーナメントの指数型母関数とすると、
	 * 強トーナメントの指数型母関数 $S(x)$ は $S(x) = 1 - \frac{1}{T(x)}$ で与えられる。
	 *
	 * <p>計算量: $O(N \log N)$
	 * @param N   頂点数の上限
	 * @param mod 法
	 * @return    長さ N+1 の配列。ret[i] は i 頂点のラベル付き強トーナメントの個数。
	 * @see <a href="https://oeis.org/A054946">OEIS A054946</a>
	 */
	public static long[] countLabeledStrongTournament(int N, long mod) {
		Fp fp = new Fp(mod);
		PolynomialFpDynamic P = PolynomialFpDynamic.of(mod);
		long[] f = new long[N + 1];
		f[0] = 1;
		long p2 = 1;
		for (int i = 1; i <= N; i++) {
			f[i] = f[i - 1] * p2 % mod;
			p2 = p2 * 2 % mod;
		}
		for (int i = 0; i <= N; i++) {
			f[i] = f[i] * fp.ifac(i) % mod;
		}
		f = P.inv(f);
		long[] ret = new long[N + 1];
		for (int i = 1; i <= N; i++) {
			ret[i] = (mod - f[i]) * fp.fac(i) % mod;
		}
		return ret;
	}

	/**
	 * n 頂点のラベル付き二重連結グラフの個数を求める。
	 * 
	 * @param n   頂点数
	 * @param mod 法
	 * @return n 頂点のラベル付き二重連結グラフの個数
	 *
	 *         <p>
	 *         計算量: O(n log n)
	 * @see https://oeis.org/A013922
	 */
	public static long countLabeledBiconnectedSingle(int n, long mod) {
		if (n < 2)
			return 0;
		Fp fp = new Fp(mod);
		var P = PolynomialFpDynamic.of(mod);
		// C(x) はラベル付き連結グラフの EGF
		long[] C = connectedGraphEGF(n + 1, 1, mod);

		// D(x) = C'(x)
		long[] D = new long[n];
		for (int i = 0; i < n; i++) {
			D[i] = C[i + 1] * (i + 1) % mod;
		}

		// G(x) = log(C'(x))
		long[] G = P.log(D);

		// E(x) = x C'(x)
		long[] E = new long[n + 1];
		for (int i = 0; i < n; i++) {
			E[i + 1] = D[i];
		}

		// B'(x) = G(IE(x)) なので [x^{n-1}] B'(x) を計算
		long val = P.lagrangeBurmann(G, E, n - 1);

		// B_n = (n-1)! [x^{n-1}] B'(x)
		return val * fp.fac(n - 1) % mod;
	}

/**
	 * n 頂点のラベル付き DAG (Directed Acyclic Graph) の個数を i = 0, ..., n について列挙する。
	 *
	 * <p>
	 * 計算量: O(n log n) (NTT-friendly な mod の場合)
	 *
	 * @param n   頂点数
	 * @param mod 法
	 * @return    長さ n + 1 の配列。ret[i] は i 頂点のラベル付き DAG の個数。
	 */
	public static long[] countLabeledDAG(int n, long mod) {
		Fp fp = new Fp(mod);
		PolynomialFpDynamic P = PolynomialFpDynamic.of(mod);
		long[] f = new long[n + 1];
		f[0] = 1;
		long inv2 = fp.inv(2);
		long p2 = 1;
		for (int i = 1; i <= n; i++) {
			f[i] = f[i - 1] * p2 % mod;
			p2 = p2 * inv2 % mod;
		}
		for (int i = 0; i <= n; i++) {
			f[i] = f[i] * fp.ifac(i) % mod;
			if (i % 2 == 1 && f[i] != 0) f[i] = mod - f[i];
		}
		f = P.inv(f);
		long curP2 = 1;
		long c = 1;
		for (int i = 0; i <= n; i++) {
			f[i] = f[i] * fp.fac(i) % mod * c % mod;
			c = c * curP2 % mod;
			curP2 = (curP2 + curP2) % mod;
		}
		return f;
	}

	/**
	 * n 頂点までのラベル付き強連結有向グラフ（strong digraphs）の個数を列挙する。
	 *
	 * <p>数学的仕様:
	 * <ul>
	 *   <li>戻り値 {@code F} の {@code F[i]} は、頂点数 {@code i} のラベル付き強連結有向グラフの個数である。</li>
	 *   <li>$G(x) = \sum_{n \ge 0} 2^{n(n-1)/2} \frac{x^n}{n!}$ とし、$H(x) = 1/G(x)$ とする。</li>
	 *   <li>$H_n = [x^n/n!] H(x)$ としたとき、$K(x) = \sum_{n \ge 0} H_n 2^{n(n-1)/2} \frac{x^n}{n!}$ とおく。</li>
	 *   <li>求める個数の指数型母関数 $S(x)$ は $S(x) = -\ln(K(x))$ である。</li>
	 * </ul>
	 *
	 * <p>計算量:
	 * <ul>
	 *   <li>$O(n \log n)$</li>
	 * </ul>
	 *
	 * @param n   頂点数
	 * @param mod 法
	 * @return    長さ n + 1 の配列。ret[i] は i 頂点のラベル付き強連結有向グラフの個数。
	 * @see <a href="https://oeis.org/A003030">OEIS A003030</a>
	 */
	public static long[] countLabeledStrongDigraph(int n, long mod) {
		Fp fp = new Fp(mod);
		PolynomialFpDynamic P = PolynomialFpDynamic.of(mod);
		long[] f = new long[n + 1];
		f[0] = 1;
		long p2 = 1;
		for (int i = 1; i <= n; i++) {
			f[i] = f[i - 1] * p2 % mod;
			p2 = p2 * 2 % mod;
		}
		for (int i = 0; i <= n; i++) {
			f[i] = f[i] * fp.ifac(i) % mod;
		}
		f = P.inv(f);
		long curP2 = 1;
		long c = 1;
		for (int i = 0; i <= n; i++) {
			f[i] = f[i] * c % mod;
			c = c * curP2 % mod;
			curP2 = (curP2 + curP2) % mod;
		}
		f = P.log(f);
		for (int i = 0; i <= n; i++) {
			f[i] = (mod - f[i]) * fp.fac(i) % mod;
		}
		return f;
	}

	/**
	 * 橋のないラベル付き連結グラフの個数を頂点数ごとに列挙する。
	 *
	 * <p>
	 * 数学的仕様:
	 * <ul>
	 * <li>戻り値 {@code A} の {@code A[i]} は、頂点数 {@code i} のラベル付き橋のない連結グラフの個数である。</li>
	 * <li>計算の基底となる関係式は以下の通り：</li>
	 * <li>$C(x)$ をラベル付き連結グラフの指数型母関数とする。</li>
	 * <li>$D(x) = x C'(x)$ とし、$E(x) = x \exp(D(x))$ とおく。</li>
	 * <li>$D(x) = B(E(x))$ を満たす $B(x)$ を求めると、求める個数の指数型母関数 $A(x)$ は $A(x) = \int
	 * \frac{B(x)}{x} dx$ となる。</li>
	 * <li>すなわち $A_i = [x^i] B(x) \cdot (i-1)!$ である。</li>
	 * </ul>
	 *
	 * <p>
	 * 事前条件:
	 * <ul>
	 * <li>{@code N >= 0}</li>
	 * <li>{@code mod} は NTT-friendly な素数であることを推奨</li>
	 * </ul>
	 *
	 * <p>
	 * 計算量:
	 * <ul>
	 * <li>$O(N \log^2 N)$</li>
	 * </ul>
	 *
	 * @param N   頂点数の上限
	 * @param mod 法
	 * @return 長さ {@code N+1} の配列。{@code ret[i]} は {@code i} 頂点の橋のない連結グラフの個数。
	 * @see <a href="https://oeis.org/A095983">OEIS A095983</a>
	 *
	 *      未テスト
	 */
	public static long[] countLabeledBridgeless(int N, long mod) {
		if (N < 0)
			return new long[0];
		long[] ret = new long[N + 1];
		if (N == 0)
			return ret;

		var P = PolynomialFpDynamic.of(mod);
		Fp fp = P.getFp();

		// 1. C(x) : 連結グラフの EGF (N+1 項)
		long[] C = connectedGraphEGF(N + 1, 1, mod);

		// 2. D(x) = x C'(x)
		long[] D = new long[N + 1];
		for (int i = 1; i <= N; i++) {
			D[i] = fp.reduce(1L * i * C[i]);
		}

		// 3. E(x) = x exp(D(x))
		long[] expD = P.exp(Arrays.copyOf(D, N + 1));
		long[] E = new long[N + 1];
		for (int i = 1; i <= N; i++) {
			E[i] = expD[i - 1];
		}

		// 4. B(x) = D(E^{-1}(x))
		long[] IE = P.compInverse(E);
		long[] B = P.comp(D, IE, N + 1);

		// 5. A_i = B_i * (i-1)!
		for (int i = 1; i <= N; i++) {
			ret[i] = fp.mul(B[i], fp.fac(i - 1));
		}
		return ret;
	}

	/**
	 * 指定された頂点数 {@code N} のラベル付き橋のない連結グラフの個数を求める。
	 *
	 * <p>
	 * 数学的仕様:
	 * <ul>
	 * <li>Lagrange の反転公式を用いて、特定の項のみを $O(N \log N)$ で計算する。</li>
	 * <li>$[x^N] B(x) = \frac{1}{N} [x^{N-1}] D'(x) \exp(-N D(x))$ を利用。</li>
	 * </ul>
	 *
	 * <p>
	 * 計算量:
	 * <ul>
	 * <li>$O(N \log N)$</li>
	 * </ul>
	 *
	 * @param N   頂点数
	 * @param mod 法
	 * @return ラベル付き橋のない連結グラフの個数
	 *
	 *         未テスト
	 */
	public static long countLabeledBridgelessSingle(int N, long mod) {
		if (N <= 0)
			return 0;
		if (N == 1)
			return 1;

		var P = PolynomialFpDynamic.of(mod);
		Fp fp = P.getFp();

		// 連結グラフの EGF
		long[] C = connectedGraphEGF(N + 1, 1, mod);
		long[] D = new long[N + 1];
		for (int i = 1; i <= N; i++) {
			D[i] = fp.reduce(1L * i * C[i]);
		}

		// D'(x)
		long[] Dp = P.differentiate(D);

		// exp(-N D(x))
		long[] minusND = P.mul(D, (mod - (N % mod)) % mod);
		long[] expMinusND = P.exp(Arrays.copyOf(minusND, N));

		// [x^{N-1}] D'(x) exp(-N D(x))
		long[] prod = P.mul(Dp, expMinusND);
		long BN = (N - 1 < prod.length ? prod[N - 1] : 0);

		// A_N = BN / N * N! / N = BN * (N-1)! / N
		return fp.mul(BN, fp.mul(fp.fac(N - 1), fp.inv(N)));
	}

	/**
	 * n 頂点の同型を除いた（ラベルなし）単純無向グラフの総数 mod mod を返す。
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(p(n) n^2)</li>
	 *   <li>空間計算量: O(n)</li>
	 * </ul>
	 *
	 * @param n 頂点数
	 * @param mod 法
	 * @return 同型を除いた単純無向グラフの総数 mod mod
	 */
	public static long countUnlabeled(int n, long mod) {
		// 未テスト
		// グラフ全体の集合に対する対称群 S_n の作用による軌道数 G_n を、Burnside の補定理を用いて計算する。
		// 各サイクル型 a ＝ (a_k)_k（長さ k のサイクルが a_k 個、sum_k k a_k = n）に対し、
		// 動かない辺の数の合計 c(a) は以下のように定義される：
		// c(a) = sum_k a_k floor(k / 2) + sum_k C(a_k, 2) * k + sum_{k < l} a_k a_l gcd(k, l)
		// 長さkのサイクルが a_k 個ある置換の個数が
		// n! / (prod_k k^{a_k} a_k!)
		// なので
		// G_n = sum_{a} 2^{c(a)} / (prod_k k^{a_k} a_k!)
		if (n < 0) {
			throw new IllegalArgumentException("n must be non-negative");
		}
		if (n == 0 || n == 1) {
			return 1 % mod;
		}

		Fp fp = new Fp(mod);
		long ans = 0;

		for (int[] p : Itertools.partitions(n)) {
			int[][] groups = Itertools.groupBy(p);
			int distinctCount = groups.length;
			int[] keys = new int[distinctCount];
			int[] counts = new int[distinctCount];
			for (int i = 0; i < distinctCount; i++) {
				keys[i] = p[groups[i][0]];
				counts[i] = groups[i].length;
			}

			long c = 0;
			for (int i = 0; i < distinctCount; i++) {
				int k = keys[i];
				long ak = counts[i];
				c += ak * (k / 2);
				c += ak * (ak - 1) / 2 * k;
				for (int j = i + 1; j < distinctCount; j++) {
					int l = keys[j];
					long al = counts[j];
					c += ak * al * MathUtils.gcd(k, l);
				}
			}

			long denom = 1;
			for (int i = 0; i < distinctCount; i++) {
				denom = fp.mul(denom, fp.pow(keys[i], counts[i]), fp.fac(counts[i]));
			}

			long term = fp.pow(2, c) * fp.inv(denom) % mod;
			ans = (ans + term) % mod;
		}

		return ans;
	}

	/**
	 * 1 頂点から n 頂点までの、同型を除いた（ラベルなし）連結単純無向グラフの総数 mod mod を頂点数ごとに列挙する。
	 *
	 * <h3>数学的仕様</h3>
	 * <ul>
	 *   <li>戻り値 {@code ret} の {@code ret[i]} は、頂点数 {@code i} のラベルなし連結単純無向グラフの個数 mod mod である。</li>
	 *   <li>全体のラベルなし単純無向グラフの EGF と連結なものの EGF の間には Riddell の公式（Euler 変換）の関係が成り立つ。</li>
	 *   <li>
	 *     1 + \sum_{n=1}^{\infty} b_n x^n = \prod_{i=1}^{\infty} (1 - x^i)^{-a_i}
	 *     より、中間数列 c_n を c_n = n b_n - \sum_{k=1}^{n-1} c_k b_{n-k} として求める。
	 *   </li>
	 *   <li>
	 *     最後にメビウスの反転公式
	 *     a_n = 1/n * \sum_{d | n} \mu(n/d) * c_d
	 *     を用いて a_n を計算する。
	 *   </li>
	 * </ul>
	 *
	 * <h3>計算量</h3>
	 * <ul>
	 *   <li>時間計算量: O(\sum_{i=1}^n p(i) i^2 + n^2) (ここで p(i) は整数 i の分割数)</li>
	 *   <li>空間計算量: O(n)</li>
	 * </ul>
	 *
	 * @param n   頂点数の上限
	 * @param mod 法
	 * @return    長さ n + 1 の配列。ret[i] は i 頂点のラベルなし連結単純無向グラフの個数 mod mod
	 *
	 * 未テスト
	 */
	public static long[] countUnlabeledConnected(int n, long mod) {
		if (n < 0) {
			throw new IllegalArgumentException("n must be non-negative");
		}
		long[] ret = new long[n + 1];
		if (n == 0) {
			return ret;
		}

		// b_i = countUnlabeled(i, mod)
		long[] b = new long[n + 1];
		b[0] = 1 % mod;
		for (int i = 1; i <= n; i++) {
			b[i] = countUnlabeled(i, mod);
		}

		PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
		return poly.plethysticLogarithm(b);
	}
}
