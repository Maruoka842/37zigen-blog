package library.util.graph;

import library.util.ArrayUtils;
import library.util.collections.IntArrayList;

public class Bipartites {
	/**
	 * https://atcoder.jp/contests/abc445/submissions/73334498
	 * @param g
	 * @return
	 */
	public static int[] maxIndependentSet(Graph g) {
		int[]col=g.greedyColoring();
		if(ArrayUtils.max(col)>=3)throw new AssertionError();
		MinCut cut=new MinCut(g.N);
		for (int i = 0; i < g.N; i++) {
			if (col[i]==0) {
				cut.addCostIfFalse(i, 1);
			} else {
				cut.addCostIfTrue(i, 1);
			}
			if (col[i]==0) {
				for (int v : g.adj[i]) {
					cut.ifThen(i, v);
				}
			}
		}
		cut.minCutValue();
		boolean[] a=cut.restoreMinCut();
		IntArrayList list=new IntArrayList();
		for (int i = 0; i < g.N; i++) {
			if((col[i]==0) == a[i]) {
				list.add(i);
			}
		}
		return list.toArray();
	}
}
