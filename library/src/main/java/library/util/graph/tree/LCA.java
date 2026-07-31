package library.util.graph.tree;

import library.util.fold.SparseTableInt;
import library.util.graph.tree.LongValueTree;
import library.util.graph.tree.Tree;

/**
 * LCA (Lowest Common Ancestor) class.
 * 構築 O(N log N)、クエリ O(1) で木の 2 頂点の最近共通祖先を求める。
 * pre-order traversal と Sparse Table を使用した O(1) LCA 実装。
 */
public class LCA {
	private final int[] preOrder;
	private final int[] pos;
	private final SparseTableInt dst;
	private final int[] depth;
	private final long[] weightedDepth;

	/**
	 * LCA を構築する。
	 * @param t 木
	 * <p>計算量: O(N log N)</p>
	 * <p>事前条件: t が根付き木であること (t.isRooted() == true)</p>
	 */
	public LCA(Tree t) {
		if (!t.isRooted()) throw new AssertionError();
		this.depth = t.depth;
		this.weightedDepth = null;
		int n = t.N;
		if (n == 0) {
			this.preOrder = new int[0];
			this.pos = new int[0];
			this.dst = null;
			return;
		}
		this.preOrder = t.preOrder();
		this.pos = new int[n];
		for (int i = 0; i < n; i++) {
			pos[preOrder[i]] = i;
		}
		int[] stArr = new int[n];
		for (int i = 0; i < n; i++) {
			int v = preOrder[i];
			stArr[i] = (v == t.root()) ? v : t.parent(v);
		}
		this.dst = new SparseTableInt(stArr, (u, v) -> {
			return depth[u] < depth[v] ? u : v;
		});
	}

	/**
	 * LCA を構築する。
	 * @param t 木 (LongValueTree)
	 * <p>計算量: O(N log N)</p>
	 * <p>事前条件: t が根付き木であること (t.isRooted() == true)</p>
	 */
	public LCA(LongValueTree t) {
		this.depth = t.depth;
		this.weightedDepth = t.weightedDepth;
		int n = t.N;
		if (n == 0) {
			this.preOrder = new int[0];
			this.pos = new int[0];
			this.dst = null;
			return;
		}
		this.preOrder = t.preOrder();
		this.pos = new int[n];
		for (int i = 0; i < n; i++) {
			pos[preOrder[i]] = i;
		}
		int[] stArr = new int[n];
		for (int i = 0; i < n; i++) {
			int v = preOrder[i];
			stArr[i] = (v == t.root()) ? v : t.parent(v);
		}
		this.dst = new SparseTableInt(stArr, (u, v) -> {
			return depth[u] < depth[v] ? u : v;
		});
	}

	
	/**
	 * 2 頂点 u, v の最近共通祖先を返す。
	 * @param u 頂点 1
	 * @param v 頂点 2
	 * @return 最近共通祖先
	 * <p>計算量: O(1)</p>
	 */
	public int lca(int u, int v) {
		if (u == v) return u;
		int l = pos[u], r = pos[v];
		if (l > r) {
			int tmp = l; l = r; r = tmp;
		}
		int res = dst.fold(l + 1, r + 1);
		return depth[u] < depth[res] ? u : res;
	}

	/**
	 * 2 頂点 u, v の間の辺の数を返す。
	 * @param u 頂点 1
	 * @param v 頂点 2
	 * @return 距離（辺の数）
	 * <p>計算量: O(1)</p>
	 */
	public int dist(int u, int v) {
		return depth[u] + depth[v] - 2 * depth[lca(u, v)];
	}

	/**
	 * 2 頂点 u, v の間の重み付き距離を返す。
	 * @param u 頂点 1
	 * @param v 頂点 2
	 * @return 重み付き距離。構築時に重み付き木が与えられなかった場合は辺の数を返す。
	 * <p>計算量: O(1)</p>
	 */
	public long weightedDist(int u, int v) {
		if (weightedDepth == null) return dist(u, v);
		return weightedDepth[u] + weightedDepth[v] - 2 * weightedDepth[lca(u, v)];
	}
}
