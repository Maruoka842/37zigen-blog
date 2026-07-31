package library.util.seq;
import library.util.MathUtils;
import library.util.Fp;
import library.util.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import library.util.polynomial.PolynomialFp;
import library.util.segtree.SegTreeFactory;
/**
 * 0 ≤ a[i] < N となる順列を扱う
 */
public class MultiPermutation {
	
	/**
	 * f[i] = a[i, r) の転倒数が v 以下となる最大の r ≤ n を返す。
	 * @param a
	 * @param v
	 * @return
	 */
	public static int[] leqInvRangeFixingStart(int[] a, long v) {
		//https://atcoder.jp/contests/typical90/submissions/76894181
		int n=a.length;
		var seg=SegTreeFactory.sum(n);
		long inv=0;
		int t=0;
		int[]f=new int[n];
		for (int i = 0; i < n; i++) {
			if (i == t) {
				seg.mul(a[i], 1);
				t++;
			}
			while(t <= n && inv <= v) {
				if (t != n) {
					inv+=seg.fold(a[t]+1, n);
					seg.mul(a[t], 1);
				}
				t++;
			}
			// [i, t) がinv > v となる最小の t ≤ n。存在しなければt=n+1
			if (t == n + 1) {
				for (int j = i; j < n; j++) {
					f[j]=n;
				}
				break;
			}
			f[i] = t - 1;
			inv -= seg.fold(0, a[i]);
			seg.mul(a[i], -1);
		}
		return f;
	}

	
	
	
	
	/**
	 * 多重集合に含まれる各値の個数 counts から、多重順列の
	 * left-to-right maxima（prefix maximum 更新点数）の分布を返す。
	 *
	 * 戻り値 res は res[k] = left-to-right maxima がk 個である
	 * 多重順列の個数 mod MOD を表す。
	 *
	 * 位置 i がleft-to-right maximum であるとは、
	 * w[i] がそれ以前のすべての値より真に大きいことをいう。
	 * したがって同じ値は新たな更新点を作らない。
	 *
	 * 値を大きい順に挿入すると、各値グループは
	 *
	 *   C(c+s-1, s-1) + C(c+s-1, s) x
	 *
	 * という一次式を寄与する。ただしc はその値の個数、
	 * s はそれより大きい値の総個数である。
	 *
	 * @param counts 各値の出現回数。index が大きいほど値が大きい。
	 * @return left-to-right maxima 数の分布
	 */
	public static long[] prefixMaximumUpdateDistribution(int[] counts) {
		if (counts == null || counts.length == 0) {
			return new long[] { 1 };
		}
		int maxIdx = -1;
		for (int i = counts.length - 1; i >= 0; i--) {
			if (counts[i] > 0) {
				maxIdx = i;
				break;
			}
		}
		if (maxIdx == -1) {
			return new long[] { 1 };
		}

		long mod = PolynomialFp.mod;
		Fp fp = Fp.MOD998244353;

		List<long[]> factors = new ArrayList<>();
		factors.add(new long[] { 0, 1 });
		long s = counts[maxIdx];
		for (int i = maxIdx - 1; i >= 0; i--) {
			if (counts[i] <= 0)
				continue;
			int c = counts[i];
			// C(c+s-1, s-1) = combrep(s, c)
			// C(c+s-1, s) = combrep(s+1, c-1)
			factors.add(new long[] { fp.combrep((int) s, c), fp.combrep((int) s + 1, c - 1) });
			s += c;
		}

		return PolynomialFp.mulAll(factors.toArray(new long[0][]));
		}

	/**
	 * 多重集合に含まれる各値の個数 counts から、多重順列の descent 数分布を返す。
	 *
	 * 戻り値 res は res[k] = descent 数がk である多重順列の個数 mod MOD を表す。
	 * ここで descent とは隣接する位置 i で w[i] > w[i+1] となる箇所である。
	 *
	 * この多項式は multiset Eulerian polynomial
	 *
	 *   A_c(t) = Σ_w t^{des(w)}
	 *
	 * であり、MacMahon の公式
	 *
	 *   Σ_{r>=0} Π_i binom(r + counts[i], counts[i]) t^r
	 *     = A_c(t) / (1 - t)^{N+1}
	 *
	 * を用いて計算する。
	 *
	 * @param counts 各値の出現回数
	 * @return 多重順列の descent 数分布
	 */
	public static long[] descentDistribution(int[] counts) {
		if (counts == null || counts.length == 0)
			return new long[] { 1 };
		int N = 0;
		int m = 0;
		int maxCount = 0;
		for (int c : counts) {
			if (c > 0) {
				N += c;
				m++;
				maxCount = Math.max(maxCount, c);
			}
		}
		if (N == 0 || m <= 1)
			return new long[] { 1 };

		long mod = PolynomialFp.mod;
		Fp fp = Fp.MOD998244353;

		int[] h = new int[maxCount + 1];
		for (int c : counts) {
			if (c > 0)
				h[c]++;
		}
		for (int i = maxCount - 1; i >= 1; i--) {
			h[i] += h[i + 1];
		}

		// Σ_{r>=0} Π_i binom(r + counts[i], counts[i]) t^r
		//=Σ_{r>=0} Π_i {(r + counts[i])(r + counts[i] - 1) .. (r + 1) /counts[i]! } t^r
		//=C Σ_{r>=0} Π_i (r + counts[i])(r + counts[i] - 1) .. (r + 1)  t^r
		//=C Σ_{r>=0} Π_i {(r + i) ^ h[i]  t^r
		List<long[]> factors = new ArrayList<>();
		for (int j = 1; j <= maxCount; j++) {
			if (h[j] > 0) {
				// (x + j)^h[j] = Σ_{k=0}^h[j] C(h[j], k) j^{h[j]-k} x^k
				long[] poly = new long[h[j] + 1];
				long j_pow = fp.pow(j, h[j]);
				long inv_j = fp.inv(j);
				for (int k = 0; k <= h[j]; k++) {
					poly[k] = fp.comb(h[j], k) * j_pow % mod;
					j_pow = j_pow * inv_j % mod;
				}
				factors.add(poly);
			}
		}

		long[] P_poly = PolynomialFp.mulAll(factors.toArray(new long[0][]));
		long[] points = new long[N + 1];
		for (int r = 0; r <= N; r++)
			points[r] = r;
		long[] Pr = PolynomialFp.multipointEval(P_poly, points);
		long C = 1;
		for (int c : counts) {
			if (c > 0)
				C = C * fp.ifac(c) % mod;
		}

		long[] B = new long[N + 1];
		for (int r = 0; r <= N; r++) {
			B[r] = Pr[r] * C % mod;
		}

		// P(t) = (1 - t)^{N+1}
		// P[j] = (-1)^j * binom(N+1, j)
		long[] poly1minusT = new long[N + 2];
		for (int j = 0; j <= N + 1; j++) {
			poly1minusT[j] = fp.comb(N + 1, j);
			if (j % 2 == 1 && poly1minusT[j] != 0) {
				poly1minusT[j] = mod - poly1minusT[j];
			}
		}

		long[] A = PolynomialFp.mul(poly1minusT, B);
		long[] res = Arrays.copyOf(A, N);

		// Trim trailing zeros
		int last = res.length - 1;
		while (last > 0 && res[last] == 0)
			last--;
		return Arrays.copyOf(res, last + 1);
	}

	public static int[] bincount(int[] a) {
		int[] ret = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[a[i]]++;
		}
		return ret;
	}

	/**
	 * 各値 i について、i = a[k] となるインデックス k を昇順に並べた配列を返す。
	 * 未テスト。
	 *
	 * 戻り値 res は res[i] = {k | a[k] = i} を表す。
	 * 0 ≤ a[i] < a.length を仮定する。
	 *
	 * @param a 多重順列
	 * @return res[i] = {k | a[k] = i}
	 * @complexity O(a.length)
	 */
	public static int[][] positions(int[] a) {
		int n = a.length;
		int[] counts = bincount(a);
		int[][] res = new int[n][];
		for (int i = 0; i < n; i++) {
			res[i] = new int[counts[i]];
		}
		int[] ptr = new int[n];
		for (int k = 0; k < n; k++) {
			int val = a[k];
			res[val][ptr[val]++] = k;
		}
		return res;
	}

	/**
	 * 転倒数を返す。未テスト。
	 * @param a
	 * @return
	 */
	public static long inversion(int[] a) {
		int n = a.length;
		var tree=SegTreeFactory.sum(n);
		long ans = 0;
		for (int i = 0; i < a.length; ++i) {
			ans += tree.fold(a[i]+1, n);
			tree.mul(a[i], 1L);
		}
		return ans;
	}
	
	/**
	 * 未テスト。
	 * @param a
	 * @param b
	 * @return
	 */
	public static long inversionBetween(int[] a, int[] b) {
		int n=a.length;
		Deque<Integer>[]que=new ArrayDeque[n];
		for (int i = 0; i < n; i++) {
			que[i]=new ArrayDeque<>();
		}
		for (int i = 0; i < n; i++) {
			que[a[i]].add(i);
		}
		int[]c=new int[a.length];
		for (int i = 0; i < n; i++) {
			c[i]=que[b[i]].poll();
		}
		return inversion(c);
	}

	/**
	 * 個数ごとの種類数 counts から、多重順列の転倒数分布を返す。未テスト。
	 *
	 * 戻り値 res は res[k] = 転倒数がk である多重順列の個数 mod 998244353 を表す。
	 * これは q-multinomial coefficient の係数列に等しい。
	 *
	 * counts[i] は「個数 i の要素が何種類あるか」を表す。
	 * 例えば counts[1] = 3, counts[2] = 1 なら、多重集合の各値の個数は [1, 1, 1, 2]。
	 * N = Σ_i i * counts[i]。
	 *
	 * @param counts counts[i] = 個数 i の要素が何種類あるか
	 * @return 多重順列の転倒数分布
	 * @throws IllegalArgumentException counts[i] が負の場合
	 * @throws AssertionError 転倒数の最大値がPolynomialFp.exp の扱える長さを超える場合
	 */
	public static long[] inversionDistribution(int[] counts) {
		int N = 0;
		long maxDegree = 0;
		for (int multiplicity = 0; multiplicity < counts.length; multiplicity++) {
			if (multiplicity == 0 || counts[multiplicity] == 0) continue;
			// 既に見た種類の要素 totalCount と、個数 multiplicity の種類 kinds 個との間で作れる転倒数。
			maxDegree = MathUtils.saturatingAdd(maxDegree, MathUtils.saturatingMul(N, multiplicity, counts[multiplicity]));
			// 同じ個数 multiplicity の種類同士でも、種類の組ごとに multiplicity*multiplicity 個まで転倒が作れる。
			long sameMultiplicityKindPairs = 1L * counts[multiplicity] * (counts[multiplicity] -  1) / 2;
			maxDegree = MathUtils.saturatingAdd(maxDegree,
					MathUtils.saturatingMul(MathUtils.saturatingMul(multiplicity, multiplicity), sameMultiplicityKindPairs));
			N += multiplicity * counts[multiplicity];
		}
		Fp fp = Fp.MOD998244353;
		if (maxDegree == 0) return new long[] {1};
		if (maxDegree == Long.MAX_VALUE || maxDegree + 1 > 1 << 23) throw new AssertionError();
		int maxDegreeInt = (int) maxDegree;
		long mod = PolynomialFp.mod;
		long[] logF = new long[maxDegreeInt + 1];
		long[]freq=ArrayUtils.suffixSumFromEmpty(counts);
		// F = (q;q)_N / Π_i ((q;q)_i)^{counts[i]}
		// log F = Σ log(1-q^i) - Σ_j counts[j] * Σ_{i=1..j} log(1-q^i)
		// freq[x] = Σ_{j>=x} counts[j] とすると
		// log F = ∑ (1 - freq[i]) log(1-q^i)
		// log F = ∑ (freq[i] - 1) q^(ij)/j
		for (int i = 1; i <= N; i++) {
			for (int j = 1; i * j <= maxDegreeInt; j++) {
				long way = (i < freq.length ? freq[i] : 0) - 1;
				if (way < 0) way += mod;
				logF[i * j] += way * fp.inv(j) % mod;
				if (logF[i * j] >= mod) logF[i * j] -= mod;
			}
		}
		return PolynomialFp.exp(logF);
	}

	/**
	 * 個数ごとの種類数 counts から、多重順列の major index 分布を返す。未テスト。
	 *
	 * 戻り値 res は res[k] = major index がk である多重順列の個数 mod 998244353 を表す。
	 * 多重順列では転倒数 inv と major index maj は同分布なので、
	 * {@link #inversionDistribution(int[])} と同じ係数列を返す。
	 *
	 * counts[i] は「個数 i の要素が何種類あるか」を表す。
	 * maj(a) = sum i s.t. a[i] > a[i + 1]
	 * @param counts counts[i] = 個数 i の要素が何種類あるか
	 * @return 多重順列の major index 分布
	 * @throws IllegalArgumentException counts[i] が負の場合
	 * @throws AssertionError major index の最大値がPolynomialFp.exp の扱える長さを超える場合
	 */
	public static long[] majorDistribution(int[] counts) {
		return inversionDistribution(counts);
	}
	
	/**
	 * aの並び替えが何通りあるかを返す
	 * https://atcoder.jp/contests/abc421/submissions/73262106
	 * @param a
	 * @return
	 */
	public static long count(int[] a) {
		if(a.length>20)throw new AssertionError();
		int[]bincount=bincount(a);
		long ans=MathUtils.factorial(a.length);
		for (int i = 0; i < bincount.length; i++) {
			ans/=MathUtils.factorial(bincount[i]);
		}
		return ans;
	}
	
	/**
	 * 部分列の個数を、空列を含めて数える。未テスト。O(n)。
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long modCountSubsequence(int[] a, long mod) {
		int[] last=new int[a.length];
		long[]dp=new long[a.length+1];
		long[]sum=new long[a.length+1];
		dp[0]=1;
		sum[0]=1;
		for (int i = 0; i < a.length; i++) {
			int s=last[a[i]];
			dp[i+1]=sum[i]-(s==0?0:sum[s-1])+mod;
			if(dp[i+1]>=mod)dp[i+1]-=mod;
			last[a[i]]=i+1;
			sum[i+1]=sum[i]+dp[i+1];
			if(sum[i+1]>=mod)sum[i+1]-=mod;
		}
		long ans=0;
		for (int i = 0; i < dp.length; i++) {
			ans+=dp[i];
			if(ans>=mod)ans-=mod;
		}
		return ans;
	}
	
	/**
	 * 各位置 i について、i より前にある同じ値 a[i] の直近の出現位置を返す。
	 *
	 * 同じ値が前に出現していない場合は -1 となる。
	 * a[i] は 0 以上 a.length 未満であることを仮定する。
	 *
	 * @param a 配列
	 * @return ret[i] = max { j | j < i かつ a[j] = a[i] }。存在しない場合は -1。
	 */
	public static int[] prevOccurences(int[] a) {
		int[] ret=new int[a.length];
		int[] last=new int[a.length];
		Arrays.fill(last, -1);
		for (int i = 0; i < a.length; i++) {
			ret[i]=last[a[i]];
			last[a[i]]=i;
		}
		return ret;
	}
	
	/**
	 * 各位置 i について、i より後にある同じ値 a[i] の直近の出現位置を返す。
	 *
	 * 同じ値が後に出現していない場合は a.length となる。
	 * a[i] は 0 以上 a.length 未満であることを仮定する。
	 *
	 * @param a 配列
	 * @return ret[i] = min { j | i < j かつ a[j] = a[i] }。存在しない場合は a.length。
	 */
	public static int[] nextOccurences(int[] a) {
		int[] ret=new int[a.length];
		int[] last=new int[a.length];
		Arrays.fill(last, a.length);
		for (int i = a.length - 1; i >= 0; i--) {
			ret[i]=last[a[i]];
			last[a[i]]=i;
		}
		return ret;
	}
	
	
}


