package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.util.ArrayUtils;

/**
 * 転置プロダクトツリー (Transposed Product Tree) を用いた一括係数抽出。
 * <p>
 * 与えられた多項式の列 L_i (0 <= i < N-1), R_i (1 <= i < N) および f(x) に対し、
 * 各 i (0 <= i < N) について、多項式 P_i(x) = f(x) * (prod_{j < i} L_j(x)) * (prod_{j > i} R_j(x))
 * の x^K の係数 [x^K] P_i(x) を一括で計算する。
 * </p>
 * <p>
 * 転置プロダクトツリーという名前は、通常のプロダクトツリー（多項式の積をトーナメント形式で計算する木）の
 * 各ノードにおける演算（多項式乗算）を、その転置演算である中間積（Middle Product / Transposed Convolution）
 * に置き換えて、根から葉へと情報を伝播させる構造に由来する。
 * </p>
 * <p>
 * 計算量: O(D log N log D)
 * ここで N は要素数、D は多項式の次数の総和（および K のオーダー）。
 * </p>
 */
public class TransposedProductTree {
	private final int N;
	private final PolynomialFpDynamic poly;
	private final long[][] A;
	private final long[][] B;

	/**
	 * サイズ N の TransposedProductTree を構築する。
	 * @param N 要素数
	 * @param poly 多項式演算器 (PolynomialFpDynamic)
	 */
	public TransposedProductTree(int N, PolynomialFpDynamic poly) {
		this.N = N;
		this.poly = poly;
		this.A = new long[N][];
		this.B = new long[N][];
	}

	/**
	 * i 番目の左側多項式 L_i を設定する。
	 * @param i インデックス (0 <= i < N-1)
	 * @param f 多項式
	 */
	public void setL(int i, long[] f) {
		if (i < 0 || i >= N - 1) throw new IllegalArgumentException();
		A[i] = f;
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
	 * 各 i について [x^K] f(x) * (prod_{j < i} L_j(x)) * (prod_{j > i} R_j(x)) を計算する。
	 * @param K 抽出する係数の次数
	 * @param f 多項式
	 * @return 各 i に対する結果の配列 (長さ N)
	 */
	public long[] calc(int K, long[] f) {
		if (N == 0) return new long[0];
		f = Arrays.copyOf(f, K + 1);

		int[] lch = new int[2 * N];
		int[] rch = new int[2 * N];
		Arrays.fill(lch, -1);
		Arrays.fill(rch, -1);
		List<long[]> treeA = new ArrayList<>();
		List<long[]> treeB = new ArrayList<>();
		int[] deg = new int[2 * N];

		for (int i = 0; i < N; i++) {
			long[] ai = A[i] == null ? new long[] { 1 } : A[i];
			long[] bi = B[i] == null ? new long[] { 1 } : B[i];
			int d = Math.max(ai.length - 1, bi.length - 1);
			deg[i] = d;
			treeA.add(Arrays.copyOf(ai, d + 1));
			treeB.add(Arrays.copyOf(bi, d + 1));
		}

		buildTree(0, N, lch, rch, treeA, treeB, deg);
		int root = treeA.size() - 1;

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
		solve(root, f, lch, rch, treeA, treeB, ans);
		return ans;
	}

	private int buildTree(int L, int R, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB, int[] deg) {
		if (R == L + 1) return L;
		int M = (L + R) / 2;
		int a = buildTree(L, M, lch, rch, treeA, treeB, deg);
		int b = buildTree(M, R, lch, rch, treeA, treeB, deg);
		int v = treeA.size();
		lch[v] = a;
		rch[v] = b;
		long[] resA = poly.mul(treeA.get(a), treeA.get(b));
		long[] resB = poly.mul(treeB.get(a), treeB.get(b));
		int d = deg[a] + deg[b];
		treeA.add(Arrays.copyOf(resA, d + 1));
		treeB.add(Arrays.copyOf(resB, d + 1));
		deg[v] = d;
		return v;
	}

	private void solve(int k, long[] g, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB, long[] ans) {
		if (k < N) {
			ans[k] = g[0];
			return;
		}
		long[] rb = treeB.get(rch[k]);
		long[] la = treeA.get(lch[k]);
		long[] g1 = g.length < rb.length ? new long[0] : poly.validShiftedDotProducts(rb, g);
		long[] g2 = g.length < la.length ? new long[0] : poly.validShiftedDotProducts(la, g);
		solve(lch[k], g1, lch, rch, treeA, treeB, ans);
		solve(rch[k], g2, lch, rch, treeA, treeB, ans);
	}
}
