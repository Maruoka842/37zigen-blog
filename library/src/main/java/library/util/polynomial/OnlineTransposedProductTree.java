package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import library.util.ArrayUtils;

/**
 * 転置プロダクトツリー (Transposed Product Tree) を用いたオンライン一括係数抽出。
 * <p>
 * 与えられた多項式の列 R_i (1 <= i < N), f(x), およびオンラインに定義される L_i (0 <= i < N-1) に対し、
 * 各 i (0 <= i < N) について、多項式 P_i(x) = f(x) * (prod_{j < i} L_j(x)) * (prod_{j > i} R_j(x))
 * の x^K の係数 [x^K] P_i(x) を一括で計算する。
 * </p>
 * <p>
 * L_i(x) は P_0,...,P_i が確定した時点で定義される。
 * </p>
 * <p>
 * 転置プロダクトツリーという名前は、通常のプロダクトツリー（多項式の積をトーナメント形式で計算する木）の
 * 各ノードにおける演算（多項式乗算）を、その転置演算である中間積（Middle Product / Transposed Convolution）
 * に置き換えて、根から葉へと情報を伝播させる構造に由来する。
 * </p>
 * <p>
 * 計算量: O(D log N log D)
 * ここで N は要素数、D = sum_i max(deg(L_i), deg(R_i))（および K のオーダー）。
 * </p>
 */
public class OnlineTransposedProductTree {
	// 乗法単位元 1 を表す共有定数。
	private static final long[] ONE = { 1 };

	// 要素数
	private final int N;
	// 多項式演算器 (PolynomialFpDynamic)
	private final PolynomialFpDynamic poly;
	// 各 i に対する右側の多項式 R_i (1 <= i < N)
	private final long[][] B;

	/**
	 * サイズ N の OnlineTransposedProductTree を構築する。
	 * @param N 要素数
	 * @param poly 多項式演算器 (PolynomialFpDynamic)
	 */
	public OnlineTransposedProductTree(int N, PolynomialFpDynamic poly) {
		this.N = N;
		this.poly = poly;
		this.B = new long[N][];
	}

	/**
	 * i 番目の右側多項式 R_i を設定する。
	 * @param i インデックス (1 <= i < N)
	 * @param f 多項式
	 */
	public void setR(int i, long[] f) {
		if (i < 1 || i >= N) throw new IllegalArgumentException();
		B[i] = f;
	}

	/**
	 * P_i = [x^K] f(x) * (\prod_{0 <= j < i} L_j(x)) * (\prod_{i < j < N} R_j(x)) を i=0,1,...,N-1 の順に確定し、
	 * 各 L_i(x) を P_0,...,P_i の確定後に {@code onlineL.apply(i, P)} で生成してから以後の計算に用いる。
	 * 事前条件: {@code degL[i] >= deg(L_i)} (0 <= i < N-1)。{@code onlineL.apply(N-1, P)} は呼ばれない。
	 * 計算量: O(D \log N \log D), D = \sum_i \max(\text{degL}[i], \text{deg}(R_i))。
	 * @param K 抽出する係数の次数
	 * @param f 多項式
	 * @param degL L_i の次数上界 (長さ N-1 以上)
	 * @param onlineL L_i を返す関数
	 * @return 各 i に対する結果の配列 (長さ N)
	 */
	// 未テスト
	public long[] calc(int K, long[] f, int[] degL, BiFunction<Integer, long[], long[]> onlineL) {
		if (N == 0) return new long[0];
		if (degL.length < N - 1) throw new IllegalArgumentException();
		f = Arrays.copyOf(f, K + 1);

		int[] lch = new int[2 * N];
		int[] rch = new int[2 * N];
		Arrays.fill(lch, -1);
		Arrays.fill(rch, -1);
		List<long[]> treeA = new ArrayList<>();
		List<long[]> treeB = new ArrayList<>();
		int[] deg = new int[2 * N];

		for (int i = 0; i < N; i++) {
			long[] bi = B[i] == null ? ONE : B[i];
			int da = i == N - 1 ? 0 : degL[i];
			int d = Math.max(da, bi.length - 1);
			deg[i] = d;
			treeA.add(null);
			treeB.add(Arrays.copyOf(bi, d + 1));
		}

		buildTree(0, N, lch, rch, treeA, treeB, deg);
		int root = treeB.size() - 1;

		int d = deg[root];
		if (K < d) {
			long[] nextF = new long[f.length + (d - K)];
			System.arraycopy(f, 0, nextF, d - K, f.length);
			f = nextF;
			K = d;
		}
		if (K > d) {
			f = Arrays.copyOfRange(f, K - d, f.length);
			K = d;
		}
		ArrayUtils.reverse(f);

		long[] ans = new long[N];
		solve(root, f, lch, rch, treeA, treeB, ans, onlineL);
		return ans;
	}

	/**
	 * プロダクトツリーをボトムアップに構築する。
	 * 計算量: O(D \log N \log D)
	 * @param L 左端
	 * @param R 右端
	 * @param lch 左の子ノード
	 * @param rch 右の子ノード
	 * @param treeA 各ノードの A 側多項式のキャッシュ
	 * @param treeB 各ノードの B 側多項式
	 * @param deg 各ノードの次数
	 * @return 構築した部分木の根ノードのインデックス
	 */
	// 未テスト
	private int buildTree(int L, int R, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB, int[] deg) {
		if (R == L + 1) return L;
		int M = (L + R) / 2;
		int a = buildTree(L, M, lch, rch, treeA, treeB, deg);
		int b = buildTree(M, R, lch, rch, treeA, treeB, deg);
		int v = treeA.size();
		lch[v] = a;
		rch[v] = b;
		long[] resB = poly.mul(treeB.get(a), treeB.get(b));
		int d = deg[a] + deg[b];
		treeA.add(null);
		treeB.add(Arrays.copyOf(resB, d + 1));
		deg[v] = d;
		return v;
	}

	/**
	 * プロダクトツリーをトップダウンに走査して、オンラインに L_i(x) を確定させながら P_i を求める。
	 * 計算量: O(D \log N \log D)
	 * @param k 現在のノード
	 * @param g 根から伝播してきた中間積
	 * @param lch 左の子ノード
	 * @param rch 右の子ノード
	 * @param treeA 各ノードの A 側多項式のキャッシュ
	 * @param treeB 各ノードの B 側多項式
	 * @param ans 結果を格納する配列
	 * @param onlineL L_i を返す関数
	 */
	// 未テスト
	private void solve(int k, long[] g, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB, long[] ans, BiFunction<Integer, long[], long[]> onlineL) {
		if (k < N) {
			ans[k] = g[0];
			if (k < N - 1) treeA.set(k, onlineL.apply(k, ans));
			else treeA.set(k, ONE);
			return;
		}
		long[] rb = treeB.get(rch[k]);
		long[] g1 = g.length < rb.length ? new long[0] : poly.validShiftedDotProducts(rb, g);
		solve(lch[k], g1, lch, rch, treeA, treeB, ans, onlineL);
		long[] la = productA(lch[k], lch, rch, treeA);
		long[] g2 = g.length < la.length ? new long[0] : poly.validShiftedDotProducts(la, g);
		solve(rch[k], g2, lch, rch, treeA, treeB, ans, onlineL);
	}

	/**
	 * ノード k 以下の部分木に対応する L_i(x) の積を計算しキャッシュする。
	 * 計算量: O(D \log N \log D)
	 * @param k ノード
	 * @param lch 左の子ノード
	 * @param rch 右の子ノード
	 * @param treeA 各ノードの A 側多項式のキャッシュ
	 * @return ノード k に対応する A 側多項式
	 */
	// 未テスト
	private long[] productA(int k, int[] lch, int[] rch, List<long[]> treeA) {
		long[] res = treeA.get(k);
		if (res != null) return res;
		res = poly.mul(productA(lch[k], lch, rch, treeA), productA(rch[k], lch, rch, treeA));
		treeA.set(k, res);
		return res;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: O(N * D log D)。ここで D = deg(f) + \sum deg(L_j) + \sum deg(R_j) は P_i の最大次数。</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @param K 抽出する係数の次数
	 * @param f 多項式
	 * @param degL L_i の次数上界
	 * @param onlineL L_i を返す関数
	 */
	// 未テスト
	public void dump(int K, long[] f, int[] degL, BiFunction<Integer, long[], long[]> onlineL) {
		long[][] L = null;
		long[][] P = null;
		if (f != null) {
			L = new long[N - 1][];
			P = new long[N][];
			long[] ans = new long[N];
			for (int i = 0; i < N; i++) {
				long[] cur = f;
				for (int j = 0; j < i; j++) {
					long[] lj = L[j] == null ? ONE : L[j];
					cur = poly.mul(cur, lj);
				}
				for (int j = i + 1; j < N; j++) {
					long[] rj = B[j] == null ? ONE : B[j];
					cur = poly.mul(cur, rj);
				}
				P[i] = cur;
				long pVal = K < cur.length ? cur[K] : 0;
				ans[i] = pVal;
				if (i < N - 1 && onlineL != null) {
					L[i] = onlineL.apply(i, ans);
				}
			}
		}
		System.out.println("OnlineTransposedProductTree {");
		System.out.println("  N: " + N);
		System.out.println("  f: " + java.util.Arrays.toString(f));
		System.out.println("  L: " + java.util.Arrays.deepToString(L));
		System.out.println("  R: " + java.util.Arrays.deepToString(B));
		System.out.println("  P: " + java.util.Arrays.deepToString(P));
		System.out.println("}");
	}
}
