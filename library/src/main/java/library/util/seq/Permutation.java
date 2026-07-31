package library.util.seq;
import library.util.MathUtils;
import library.util.Fp;
import library.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import library.util.polynomial.PolynomialFp;
import library.util.segtree.SegTree;
import library.util.segtree.SegTreeFactory;

public class Permutation {
	
	/**
	 * <p>{@code a} は {@code 0, 1, ..., a.length - 1} 上の置換を表す配列であり、
	 * 各 {@code i} について {@code i -> a[i]} という写像を考える。
	 * このメソッドは、その置換を互いに素な巡回のリストとして返す。</p>
	 *
	 * <p>戻り値の各配列 {@code cycle} は、ある巡回
	 * {@code cycle[0] -> cycle[1] -> ... -> cycle[cycle.length - 1] -> cycle[0]}
	 * を表す。すなわち、各 {@code k} について
	 * {@code a[cycle[k]] == cycle[(k + 1) % cycle.length]} が成り立つ。</p>
	 * @param a
	 * @return
	 */
	public static List<int[]> cycles(int[] a) {
		//https://atcoder.jp/contests/abc371/submissions/72662957
		boolean[]vis=new boolean[a.length];
		ArrayList<int[]> ret=new ArrayList<>();
		for (int i = 0; i < a.length; i++) {
			if (!vis[i]) {
				int j=i;
				int len = 0;
				do {
					len++;
					j = a[j];
				} while (j != i);
				int[] cycle=new int[len];
				j=i;
				for (int k = 0; k < len; k++, j=a[j]) {
					cycle[k]=j;
					vis[j]=true;
				}
				ret.add(cycle);
			}
		}
		return ret;
	}
	
	
	public static long inversion(int[] a) {
		int n = a.length;
		SegTree<Long> tree = new SegTree<Long>(n, Long::sum, 0L);
		long ans = 0;
		for (int i = 0; i < a.length; ++i) {
			ans += tree.fold(a[i] + 1, n);
			tree.set(a[i], 1L);
		}
		return ans;
	}

	/**
	 * 長さn の順列の descent 数分布を返す。未テスト。O(n log n + M(n))。
	 *
	 * 戻り値 res は res[k] = descent 数がk である順列の個数 mod 998244353 を表す。
	 * ここで descent とは隣接する位置 i で p[i] > p[i+1] となる箇所である。
	 *
	 * @param n 順列の長さ
	 * @return descent 数分布
	 */
	public static long[] descentDistribution(int n) {
		if (n < 0) throw new AssertionError();
		if (n == 0) return new long[] { 1 };
		long mod = 998244353L;
		Fp fp = Fp.MOD998244353;
		long[] oneMinusX = new long[n + 2];
		for (int i = 0; i <= n + 1; i++) {
			oneMinusX[i] = fp.comb(n + 1, i);
			if ((i & 1) == 1 && oneMinusX[i] != 0) oneMinusX[i] = mod - oneMinusX[i];
		}
		long[] powerSum = new long[n + 1];
		for (int i = 0; i <= n; i++) {
			powerSum[i] = fp.pow(i, n);
		}
		long[] eulerian = PolynomialFp.mul(oneMinusX, powerSum);
		long[] ret = new long[n];
		for (int i = 0; i < n; i++) {
			ret[i] = eulerian[i + 1];
		}
		return ret;
	}

	/**
	 * b[i] = a.length - 1 - a[i]とした配列bを返す
	 * @param a
	 * @return
	 */
	public static int[] complement(int[] a) {
		int[] b = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			if (!(0 <= a[i] && a[i] < a.length)) throw new AssertionError();
			b[i] = a.length - 1 - a[i];
		}
		return b;
	}
	
	public static int[] inverse(int[] a) {
		int[] ret = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			ret[a[i]] = i;
		}
		return ret;
	}
	
	/**
	 * Sは<,>,?からなる長さN-1の文字列。P[i], P[i+1]の大小関係がS[i]で与えられるような順列の個数を返す。
	 * @param S
	 * @param mod
	 * @return
	 */
	public static long countWithAdjacentInequalities(char[] S, long mod) {
		int N=S.length+1;
		long[] dp = new long[N];
        dp[0] = 1;
        // > について消去
        Fp fp = new Fp(mod);
        for (int i = 0; i < (N - 1); i++) {
            int sign = 1;
            for (int j = i; j >= (-1); j--) {
                // 全ての>を≤に置き換える。
                // P[j]?P[j+1]<..<P[i]<P[i+1]とする.
                if (j == -1 || S[j] == '>' || S[j] == '?') {
                    dp[i + 1] += ((j == (-1) ? 1 : dp[j]) * fp.ifac((i - j) + 1)) * sign;
                    dp[i + 1] %= mod;
                    sign *= -1;
                }
                if (j != -1 && S[j] == '?')break;
            }
        }
        long ans = dp[N - 1] * fp.fac(N);
        ans = fp.reduce(ans);
        return ans;
	}
	
	
	
	/**
	 * 長さnの順列の転倒数の平均 n(n-1)/4 を返す。
	 * @param n
	 * @param mod
	 * @return
	 */
	public static long averageInversion(int n, long mod) {
		//https://atcoder.jp/contests/abc380/tasks/abc380_g
		if(n<0)throw new AssertionError();
		return 1L*n*(n-1)%mod*MathUtils.modInv(4, mod)%mod;
	}
	
	/**
	 * 長さnの順列の転倒数の2乗の平均
	 * @param n
	 * @param mod
	 * @return
	 */
	public static long averageInversionSquared(int n, long mod) {
		if(n<0)throw new AssertionError();
		Fp fp=new Fp(mod);
		return fp.mul(1L*n,1L*(n-1),(9*n%mod*n%mod-5*n+10)%mod, fp.inv(144));
	}
	
	/**
	 * https://atcoder.jp/contests/abc378/submissions/72838437
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long modHookLengthFormula(int[] a, long mod) {
		int[]b=a.clone();
		ArrayUtils.rsort(b);
		int n=(int)ArrayUtils.sum(a);
		Fp fp=new Fp(mod);
		long ans=fp.fac(n);
		for (int i = 1; i <= n; i++) {
			int w=0;
			while(w < b.length && b[w]>=i)++w;
			for (int j = 0; j < w; j++) {
				ans=ans*fp.inv((b[j]-i+1)+w-j-1)%mod;
			}
		}
		return ans;
	}
	
	/**
	 * f[i] = a[i, r) の転倒数がv 以下となる最大の r ≤ n を返す。
	 * @param a
	 * @param v
	 * @return
	 */
	public static int[] leqInvRangeFixingStart(int[] a, long v) {
		//https://atcoder.jp/contests/abc452/submissions/74699805
		int n=a.length;
		var seg=SegTreeFactory.sum(n);
		long inv=0;
		int t=0;
		int[]f=new int[n];
		for (int i = 0; i < n; i++) {
			if (i == t) {
				seg.set(a[i], 1);
				t++;
			}
			while(t <= n && inv <= v) {
				if (t != n) {
					inv+=seg.fold(a[t]+1, n);
					seg.set(a[t], 1);
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
			seg.set(a[i], 0);
		}
		return f;
	}
	
	public static int sign(int[] a) {
		int n = a.length;
		boolean[] vis = new boolean[n];
		int parity = 0;
		for (int i = 0; i < n; i++) {
			if (!vis[i]) {
				int j = i;
				int len = 0;
				do {
					vis[j] = true;
					len++;
					j = a[j];
				} while (j != i);
				if (len % 2 == 0) parity ^= 1;
			}
		}
		return parity == 0 ? 1 : -1;
	}

	/**
	 * 与えられた置換の集合が生成する群の、強い生成系(Strong Generating Set)を求める。
	 * 未テスト。
	 * 計算量 $O(n^2 |orb| + n |perm|)$。
	 *
	 * シュライヤー・シムズアルゴリズムを用いて、安定化群の列（Stabilizer Chain）を構築する。
	 ** <p><b>【数学的定義】</b><br>
	 * 点集合 $X = \{0, 1, \dots, n-1\}$ に対する安定化群の減少列を以下のように定義します。<br>
	 * <ul>
	 * <li>$G^{(n)} = G$</li>
	 * <li>$G^{(f)} = \{ g \in G \mid \forall k \in \{f, f+1, \dots, n-1\}, \, p[k] = k \}$ （$1 \le f < n$）</li>
	 * <li>$G^{(0)} = \{ e \}$</li>
	 * </ul>
	 * このとき、戻り値の要素 {@code res.get(f)} は、剰余類展開 $G^{(f+1)} / G^{(f)}$ の完全代表系（Transversal）$U_f$ を表します。<br>
	 * 具体的には、要素 $f$ の $G^{(f+1)}$ による軌道（Orbit）を $O_f$ としたとき、
	 * 各 $j \in O_f$ に対して「$f$ を $j$ に写し、かつ $f+1 \dots n-1$ をすべて固定する置換」がちょうど1つずつ含まれます。</p>
	 *
	 * @param n 置換のサイズ
	 * @param perm 生成元のリスト
	 * @param forceSizeN 結果の置換のサイズをnにするかどうか。falseの場合は各段階の軌道サイズになる。
	 * @return res[f] は f を動かし、f+1...n-1 を固定する置換の集合（Transversal）。
	 */
	public static List<int[][]> simplifyPermutationSubgroup(int n, List<int[]> perm, boolean forceSizeN) {
		// elim[i][j] は i+1...n-1 を固定し、i を j (<i) に写す置換。
		// これは安定化群の列 G = G_n >= G_{n-1} >= ... >= G_1 において、
		// G_{i+1} における G_i の剰余類の代表元（の候補）を保持する。
		int[][][] elim = new int[n][][];
		for (int i = 0; i < n; i++) elim[i] = new int[i][];

		// 初期生成元を stabilizer chain 構造に挿入する
		for (int[] p : perm) elimInsert(elim, p.clone(), n);

		List<int[][]> res = new ArrayList<>(n);
		for (int i = 0; i < n; i++) res.add(new int[0][]);

		for (int f = n - 1; f >= 1; f--) {
			// f の軌道(orbit)を求める
			int[][] orb = new int[f + 1][];
			ArrayList<Integer> bfs = new ArrayList<>();
			bfs.add(f);
			orb[f] = new int[f + 1];
			for (int i = 0; i <= f; i++) orb[f][i] = i;

			for (int z = 0; z < bfs.size(); z++) {
				int s = bfs.get(z);
				for (int a = s; a <= f; a++) {
					for (int[] pm : elim[a]) {
						if (pm != null && orb[pm[s]] == null) {
							// 新しい軌道の要素が見つかった場合、その要素へ飛ばす置換を記録
							int[] nx = new int[f + 1];
							for (int i = 0; i <= f; i++) nx[i] = pm[orb[s][i]];
							orb[pm[s]] = nx;
							bfs.add(pm[s]);
						}
					}
				}
			}

			// 軌道の各要素に対する代表元の逆置換を用意
			int[][] iorb = new int[f + 1][];
			for (int a = 0; a <= f; a++) {
				if (orb[a] != null) {
					iorb[a] = new int[f + 1];
					for (int i = 0; i <= f; i++) iorb[a][orb[a][i]] = i;
				}
			}

			int[][][] oldElim = elim;
			elim = new int[f + 1][][];
			for (int d = 0; d <= f; d++) elim[d] = new int[d][];

			// シュライヤーの補題（Schreier's Lemma）を用いて、一つ下の安定化群の生成元を求める
			for (int a = 1; a <= f; a++) {
				for (int[] p : oldElim[a]) {
					if (p != null) {
						for (int o : bfs) {
							int[] q = new int[f];
							// q = transversal[p(o)]^-1 * p * transversal[o]
							for (int i = 0; i < f; i++) q[i] = iorb[p[o]][p[orb[o][i]]];
							elimInsert(elim, q, f);
						}
					}
				}
			}

			int[][] resF = new int[bfs.size()][];
			for (int i = 0; i < bfs.size(); i++) {
				int a = bfs.get(i);
				if (forceSizeN) {
					resF[i] = new int[n];
					for (int j = 0; j < n; j++) resF[i][j] = j;
					for (int j = 0; j <= f; j++) resF[i][j] = orb[a][j];
				} else {
					resF[i] = orb[a];
				}
			}
			res.set(f, resF);
		}
		return res;
	}

	/**
	 * 置換を現在の強い生成系（SGS）の構造に挿入する。
	 *
	 * @param elim elim[i][j] は i+1...m-1 を固定し、i を j (<i) に写す置換を保持する
	 * @param p 挿入する置換
	 * @param m 置換のサイズ
	 */
	private static void elimInsert(int[][][] elim, int[] p, int m) {
		int[] nx = new int[m];
		// 大きい要素から順に固定していく
		for (int a = m - 1; a >= 1; a--) {
			if (p[a] != a) {
				// a を p[a] に飛ばす置換がまだ登録されていない場合は登録して終了
				if (elim[a][p[a]] == null) {
					elim[a][p[a]] = p;
					return;
				}
				// 既に登録されている置換 tg を使って、p が a を固定するように更新する
				// p' = p * tg^-1
				int[] tg = elim[a][p[a]];
				for (int t = 0; t < m; t++) nx[tg[t]] = t;
				int[] nextP = new int[m];
				for (int t = 0; t < m; t++) nextP[t] = nx[p[t]];
				p = nextP;
			}
		}
	}

	/**
	 * ロビンソン・シェンステッド・クヌース対応（RSK対応）を求める。
	 * 計算量 $O(n \log n)$ (平均), $O(n^2)$ (最悪)。
	 *
	 * 与えられた順列 a に対して、同じ形状の2つの標準ヤングタブロ P, Q のペアを返す。
	 * P は a の要素を Schensted 挿入して得られるタブロ、
	 * Q は a の逆置換 a^-1 の P タブロと一致する。
	 *
	 * @param a 順列
	 * @return RSK対応の結果 (P, Q)
	 */
	public static RSKResult rsk(int[] a) {
		return new RSKResult(pTableau(a), pTableau(inverse(a)));
	}

	/**
	 * 与えられた順列 a の P タブロ（挿入タブロ）を求める。
	 * 計算量 $O(n \log n)$ (平均), $O(n^2)$ (最悪)。
	 *
	 * @param a 順列
	 * @return P タブロ
	 */
	public static int[][] pTableau(int[] a) {
		int n = a.length;
		List<TreeSet<Integer>> pTableau = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			int x = a[i];
			int currRow = 0;
			while (true) {
				if (currRow == pTableau.size()) {
					pTableau.add(new TreeSet<>());
				}
				TreeSet<Integer> row = pTableau.get(currRow);
				Integer y = row.higher(x);
				if (y == null) {
					row.add(x);
					break;
				} else {
					row.remove(y);
					row.add(x);
					x = y;
					currRow++;
				}
			}
		}

		int[][] p = new int[pTableau.size()][];
		for (int i = 0; i < pTableau.size(); i++) {
			p[i] = pTableau.get(i).stream().mapToInt(Integer::intValue).toArray();
		}
		return p;
	}

	public record RSKResult(int[][] P, int[][] Q) {}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}


