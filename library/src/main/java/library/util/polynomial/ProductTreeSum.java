package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * プロダクトツリーを用いた多項式の総和計算。
 * <p>
 * 与えられた多項式の列 L_i (0 <= i < N-1), R_i (1 <= i < N) および f_i(x) (0 <= i < N) に対し、
 * P(x) = \sum_{i=0}^{N-1} f_i(x) * (prod_{j < i} L_j(x)) * (prod_{j > i} R_j(x))
 * を計算する。
 * </p>
 * <p>
 * これは {@link TransposedProductTree} の「元の問題（転置前の問題）」にあたる。
 * プロダクトツリーを構築し、ボトムアップに多項式をマージしていくことで計算する。
 * </p>
 * <p>
 * 計算量: O(D log N log D)
 * ここで N は要素数、D は多項式の次数の総和。
 * </p>
 */
public class ProductTreeSum {
	private final int N;
	private final PolynomialFpDynamic poly;
	private final long[][] A;
	private final long[][] B;

	/**
	 * サイズ N の ProductTreeSum を構築する。
	 * @param N 要素数
	 * @param poly 多項式演算器 (PolynomialFpDynamic)
	 */
	public ProductTreeSum(int N, PolynomialFpDynamic poly) {
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
	 * P(x) = \sum f_i(x) * (prod_{j < i} L_j(x)) * (prod_{j > i} R_j(x)) を計算する。
	 * @param f 多項式の列 f_0, ..., f_{N-1}
	 * @return 総和多項式 P(x)
	 */
	public long[] calc(long[][] f) {
		if (N == 0) return new long[0];
		if (f.length != N) throw new IllegalArgumentException();

		int[] lch = new int[2 * N];
		int[] rch = new int[2 * N];
		Arrays.fill(lch, -1);
		Arrays.fill(rch, -1);
		List<long[]> treeA = new ArrayList<>();
		List<long[]> treeB = new ArrayList<>();

		for (int i = 0; i < N; i++) {
			long[] ai = A[i] == null ? new long[] { 1 } : A[i];
			long[] bi = B[i] == null ? new long[] { 1 } : B[i];
			treeA.add(ai);
			treeB.add(bi);
		}

		int root = buildTree(0, N, lch, rch, treeA, treeB);
		return solve(root, f, lch, rch, treeA, treeB);
	}

	private int buildTree(int L, int R, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB) {
		if (R == L + 1) return L;
		int M = (L + R) / 2;
		int a = buildTree(L, M, lch, rch, treeA, treeB);
		int b = buildTree(M, R, lch, rch, treeA, treeB);
		int v = treeA.size();
		lch[v] = a;
		rch[v] = b;
		treeA.add(poly.mul(treeA.get(a), treeA.get(b)));
		treeB.add(poly.mul(treeB.get(a), treeB.get(b)));
		return v;
	}

	private long[] solve(int k, long[][] f, int[] lch, int[] rch, List<long[]> treeA, List<long[]> treeB) {
		if (k < N) {
			return f[k];
		}
		long[] left = solve(lch[k], f, lch, rch, treeA, treeB);
		long[] right = solve(rch[k], f, lch, rch, treeA, treeB);

		// left * treeB[rch] + right * treeA[lch]
		long[] resL = poly.mul(left, treeB.get(rch[k]));
		long[] resR = poly.mul(right, treeA.get(lch[k]));
		return poly.add(resL, resR);
	}
}
