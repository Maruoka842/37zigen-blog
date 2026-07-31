package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * 2つのマトロイドの強不和和（マトロイド和）に関する問題を解くクラス。
 */
public class MatroidUnion {

	private MatroidUnion() {}

	/**
	 * マトロイド和の結果を保持するレコード。
	 */
	public record MatroidUnionResult(boolean[] i1, boolean[] i2) {
		/**
		 * 和集合（独立集合）のサイズを返す。
		 * @return サイズ
		 */
		public int size() {
			int count = 0;
			for (boolean b : i1) if (b) count++;
			for (boolean b : i2) if (b) count++;
			return count;
		}
	}

	/**
	 * 2つのマトロイドの最大サイズ独立集合の和を求める。(未テスト)
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @return マトロイド和の結果
	 */
	public static MatroidUnionResult solve(Matroid m1, Matroid m2) {
		return solve(m1, m2, null);
	}

	/**
	 * 重み付きマトロイド和問題を解き、最大サイズの独立集合のうち、重みの総和が最小のものを求める。(未テスト)
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @param weights 各要素の重み
	 * @return マトロイド和の結果
	 */
	public static MatroidUnionResult solve(Matroid m1, Matroid m2, long[] weights) {
		int n = m1.size();
		boolean[] i1 = new boolean[n];
		boolean[] i2 = new boolean[n];
		while (augment(m1, m2, i1, i2, weights)) ;
		return new MatroidUnionResult(i1, i2);
	}

	private static boolean augment(Matroid m1, Matroid m2, boolean[] i1, boolean[] i2, long[] weights) {
		int n = m1.size();
		int gs = n;
		int gt = n + 1;
		LongValueDigraph g = new LongValueDigraph(n + 2);
		int[] color = new int[n];
		Arrays.fill(color, -1);

		m1.set(i1);
		m2.set(i2);

		for (int e = 0; e < n; e++) {
			if (!i1[e] && !i2[e]) {
				long cost = (weights == null) ? 1 : weights[e] * (n + 1) + 1;
				g.addEdge(gs, e, cost);
			}
			if (!i1[e]) {
				IntArrayList c = m1.circuit(e);
				if (c.isEmpty()) {
					g.addEdge(e, gt, 0);
					color[e] = 0;
				} else {
					for (int f : c) {
						if (f != e) g.addEdge(e, f, 1);
					}
				}
			}
			if (!i2[e]) {
				IntArrayList c = m2.circuit(e);
				if (c.isEmpty()) {
					g.addEdge(e, gt, 0);
					color[e] = 1;
				} else {
					for (int f : c) {
						if (f != e) g.addEdge(e, f, 1);
					}
				}
			}
		}

		LongValueDigraph.DijkstraResult res = g.spfa(gs);
		if (res == null || res.dist()[gt] >= Long.MAX_VALUE / 3) return false;

		ArrayList<Integer> path = g.restoreShortestPath(gt, res.parent());
		// path: [gs, e1, e2, ..., ek, gt]
		int ek = path.get(path.size() - 2);
		int c0 = -1;
		if (i1[ek]) c0 = 1;
		else if (i2[ek]) c0 = 0;
		else c0 = color[ek];

		for (int k = path.size() - 2; k >= 1; k--) {
			int e = path.get(k);
			if (c0 == 0) {
				i1[e] = true;
				i2[e] = false;
				c0 = 1;
			} else {
				i2[e] = true;
				i1[e] = false;
				c0 = 0;
			}
		}
		return true;
	}
}
