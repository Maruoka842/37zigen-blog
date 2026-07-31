package library.util.fold;

import java.util.function.IntBinaryOperator;

import library.util.MathUtils;

/**
 * Disjoint Sparse Table for int.
 * 結合的な二項演算に対して、構築 O(N log N)、クエリ O(1) で範囲和を求める。
 */
public class DisjointSparseTableInt {
	private final int[][] table;
	private final IntBinaryOperator op;

	/**
	 * Disjoint Sparse Table を構築する。
	 * @param v 配列
	 * @param op 結合的な二項演算 (IntBinaryOperator)
	 * <p>計算量: O(N log N)</p>
	 * <p>事前条件: op は結合的であること</p>
	 * <p>副作用: 引数の配列 v は変更されない。</p>
	 */
	public DisjointSparseTableInt(int[] v, IntBinaryOperator op) {
		this.op = op;
		int n = v.length;
		if (n == 0) {
			table = new int[0][0];
			return;
		}
		int log = 0;
		while ((1 << log) < n)
			log++;
		if (log == 0)
			log = 1;
		table = new int[log][n];
		for (int i = 0; i < log; i++) {
			int half = 1 << i;
			int block = 1 << (i + 1);
			for (int j = 0; j < n; j += block) {
				int mid = j + half;
				if (mid >= n) {
					table[i][n - 1] = v[n - 1];
					for (int k = n - 2; k >= j; k--) {
						table[i][k] = op.applyAsInt(v[k], table[i][k + 1]);
					}
				} else {
					table[i][mid - 1] = v[mid - 1];
					for (int k = mid - 2; k >= j; k--) {
						table[i][k] = op.applyAsInt(v[k], table[i][k + 1]);
					}
					table[i][mid] = v[mid];
					int end = Math.min(n, j + block);
					for (int k = mid + 1; k < end; k++) {
						table[i][k] = op.applyAsInt(table[i][k - 1], v[k]);
					}
				}
			}
		}
	}

	/**
	 * 半開区間 [l, r) の演算結果を返す。
	 * @param l 開始インデックス（包含）
	 * @param r 終了インデックス（除外）
	 * @return 演算結果
	 * <p>事前条件: 0 <= l < r <= N</p>
	 * <p>計算量: O(1)</p>
	 */
	public int fold(int l, int r) {
		r--;
		if (l == r)
			return table[0][l];
		int i = MathUtils.floorLog2(l ^ r);
		return op.applyAsInt(table[i][l], table[i][r]);
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \log N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("DisjointSparseTableInt { table: " + java.util.Arrays.deepToString(table) + " }");
	}
}
