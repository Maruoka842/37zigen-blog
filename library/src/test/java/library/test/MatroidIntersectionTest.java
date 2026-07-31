package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.Matroid;
import library.util.graph.MatroidIntersection;

public class MatroidIntersectionTest {

	// 集合の分割マトロイド。要素 [0, n) をいくつかのグループに分け、各グループから選べる上限を指定する。
	static class BetterPartitionMatroid implements Matroid {
		int n;
		int[] elementToGroup;
		int[] groupCapacity;
		boolean[] I;
		int[] currentGroupCount;

		BetterPartitionMatroid(int n, int numGroups, int[] elementToGroup, int[] groupCapacity) {
			this.n = n;
			this.elementToGroup = elementToGroup;
			this.groupCapacity = groupCapacity;
			this.currentGroupCount = new int[numGroups];
		}

		@Override
		public int size() {
			return n;
		}

		@Override
		public void set(boolean[] I) {
			this.I = I.clone();
			for (int i = 0; i < currentGroupCount.length; i++) currentGroupCount[i] = 0;
			for (int i = 0; i < n; i++) {
				if (I[i]) currentGroupCount[elementToGroup[i]]++;
			}
		}

		@Override
		public IntArrayList circuit(int e) {
			int group = elementToGroup[e];
			if (currentGroupCount[group] < groupCapacity[group]) {
				return new IntArrayList();
			}
			IntArrayList res = new IntArrayList();
			res.add(e);
			for (int i = 0; i < n; i++) {
				if (I[i] && elementToGroup[i] == group) {
					res.add(i);
				}
			}
			return res;
		}
	}

	@Test
	public void testBipartiteMatching() {
		// 二部グラフの最大マッチングをマトロイド交差で解く。
		// 左側頂点集合 L, 右側頂点集合 R, 辺集合 E.
		// マトロイド1: 各 L 側頂点に接続する辺は 1 つまで。
		// マトロイド2: 各 R 側頂点に接続する辺は 1 つまで。

		int nl = 3, nr = 3;
		// Edges: (0,0), (0,1), (1,1), (1,2), (2,0)
		int[] u = {0, 0, 1, 1, 2};
		int[] v = {0, 1, 1, 2, 0};
		int m = u.length;

		BetterPartitionMatroid m1 = new BetterPartitionMatroid(m, nl, u, new int[]{1, 1, 1});
		BetterPartitionMatroid m2 = new BetterPartitionMatroid(m, nr, v, new int[]{1, 1, 1});

		boolean[] res = MatroidIntersection.solve(m1, m2);
		int count = 0;
		for (boolean b : res) if (b) count++;
		assertEquals(3, count); // (0,1), (1,2), (2,0) is a matching of size 3.
	}

	@Test
	public void testWeightedBipartiteMatching() {
		int nl = 2, nr = 2;
		// Edges: (0,0) weight 10, (0,1) weight 5, (1,0) weight 5, (1,1) weight 10
		int[] u = {0, 0, 1, 1};
		int[] v = {0, 1, 0, 1};
		long[] weights = {10, 5, 5, 10};
		int m = u.length;

		BetterPartitionMatroid m1 = new BetterPartitionMatroid(m, nl, u, new int[]{1, 1});
		BetterPartitionMatroid m2 = new BetterPartitionMatroid(m, nr, v, new int[]{1, 1});

		boolean[] res = MatroidIntersection.solve(m1, m2, weights);
		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < m; i++) {
			if (res[i]) {
				count++;
				totalWeight += weights[i];
			}
		}
		assertEquals(2, count);
		assertEquals(10, totalWeight); // Should pick (0,1) and (1,0)
	}

	@Test
	public void testMaxWeightBipartiteMatching() {
		int nl = 2, nr = 2;
		// Edges: (0,0) weight 10, (0,1) weight 5, (1,0) weight 5, (1,1) weight 10
		int[] u = {0, 0, 1, 1};
		int[] v = {0, 1, 0, 1};
		long[] weights = {10, 5, 5, 10};
		int m = u.length;

		BetterPartitionMatroid m1 = new BetterPartitionMatroid(m, nl, u, new int[]{1, 1});
		BetterPartitionMatroid m2 = new BetterPartitionMatroid(m, nr, v, new int[]{1, 1});

		boolean[] res = MatroidIntersection.solveMaxWeight(m1, m2, weights);
		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < m; i++) {
			if (res[i]) {
				count++;
				totalWeight += weights[i];
			}
		}
		assertEquals(2, count);
		assertEquals(20, totalWeight); // Should pick (0,0) and (1,1)
	}
}
