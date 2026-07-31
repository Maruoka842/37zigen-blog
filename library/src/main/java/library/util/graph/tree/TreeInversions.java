package library.util.graph.tree;

import java.util.ArrayDeque;
import java.util.Deque;

import library.util.algebra.strategy.longs.LongAddAbelianGroupStrategy;
import library.util.segtree.LongAbelianGroupBinaryIndexedTree;

/**
 * 木の転倒数を計算するクラス。
 * 頂点 r を根としたときの転倒数は、頂点 r から頂点 u の単純パス上に頂点 v が含まれるような組 (u, v) (u < v) の個数。
 * 全ての r について O(N log N) で計算する。
 */
public class TreeInversions {

	/**
	 * 全ての頂点 r について、r を根としたときの転倒数を求める。
	 * O(N log N)
	 * @param tree 木
	 * @return 転倒数の配列 (0-indexed)
	 */
	public static long[] countInversionsForAllRoots(Tree tree) {
		int n = tree.N;
		if (n == 0) return new long[0];
		if (!tree.isRooted()) {
			tree.rooted(0);
		}
		int root = tree.root();

		long[] inv = new long[n];
		LongAbelianGroupBinaryIndexedTree bit = new LongAbelianGroupBinaryIndexedTree(n, LongAddAbelianGroupStrategy.STRATEGY);

		// 1. Compute inv[root] (O(N log N))
		// inv[root] = sum_{u} count {v in anc(u) : v > u}
		long invRoot = 0;
		int[] childIdx = new int[n];
		Deque<Integer> stack = new ArrayDeque<>();
		stack.push(root);
		while (!stack.isEmpty()) {
			int v = stack.peek();
			if (childIdx[v] == 0) {
				// Entry
				invRoot += bit.fold(v + 1, n);
				bit.add(v, 1);
			}
			if (childIdx[v] < tree.childs[v].size()) {
				stack.push(tree.childs[v].get(childIdx[v]));
				childIdx[v]++;
			} else {
				// Exit
				bit.add(v, -1);
				stack.pop();
			}
		}
		inv[root] = invRoot;

		// 2. Compute CountGreater(T_v, v) and CountGreater(T_v, parent(v)) (O(N log N)).
		// T_v is subtree at v when rooted at 'root'.
		long[] countGreaterSubtreeV = new long[n];
		long[] countGreaterSubtreeP = new long[n];
		StaticOfflineSubtreeSum subtreeSum = new StaticOfflineSubtreeSum(tree, LongAddAbelianGroupStrategy.STRATEGY, n);
		for (int v = 0; v < n; v++) {
			subtreeSum.addPoint(v, v, 1);
		}
		int[] queryV = new int[n];
		int[] queryP = new int[n];
		for (int v = 0; v < n; v++) {
			queryV[v] = subtreeSum.addQuery(v, v + 1, n);
			if (v != root) {
				int p = tree.parent(v);
				queryP[v] = subtreeSum.addQuery(v, p + 1, n);
			}
		}
		long[] queryAnswer = subtreeSum.solve();
		for (int v = 0; v < n; v++) {
			countGreaterSubtreeV[v] = queryAnswer[queryV[v]];
			if (v != root) countGreaterSubtreeP[v] = queryAnswer[queryP[v]];
		}

		// 3. Rerooting (O(N))
		// Inv(v) = Inv(p) + v + 1 - 2 * size[v] + CountGreater(T_v, v) + CountGreater(T_v, p)
		int[] bfsOrder = tree.bfsOrder();
		for (int v : bfsOrder) {
			if (v == root) continue;
			int p = tree.parent(v);
			inv[v] = inv[p] + v + 1 - 2L * tree.size(v) + countGreaterSubtreeV[v] + countGreaterSubtreeP[v];
		}

		return inv;
	}
}
