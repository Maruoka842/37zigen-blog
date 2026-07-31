package library.util.graph.tree;

import library.util.algebra.strategy.GroupStrategy;
import library.util.segtree.SegTree;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Euler Tour を用いた木のパス上の積（可換群）を計算するライブラリ。
 * 構築: O(N)
 * 更新: O(log N)
 * クエリ: O(log N)
 * @param <T> 群の要素の型
 */
public class EulerTourPath<T> {
	private final Tree tree;
	private final GroupStrategy<T> strategy;
	private final SegTree<T> seg;
	private final int[] in, out;
	private final T[] vertexValues;

	@SuppressWarnings("unchecked")
	public EulerTourPath(Tree tree, GroupStrategy<T> strategy) {
		if (!tree.isRooted()) throw new AssertionError("Tree must be rooted.");
		this.tree = tree;
		this.strategy = strategy;
		int n = tree.N;
		this.in = new int[n];
		this.out = new int[n];
		this.vertexValues = (T[]) new Object[n];
		for (int i = 0; i < n; i++) {
			vertexValues[i] = strategy.identity();
		}

		int ptr = 0;
		// Iterative DFS to avoid StackOverflow
		Deque<Integer> stack = new ArrayDeque<>();
		stack.push(tree.root());
		int[] childIdx = new int[n];

		while (!stack.isEmpty()) {
			int v = stack.peek();
			if (childIdx[v] == 0) {
				in[v] = ptr++;
			}
			if (childIdx[v] < tree.childs[v].size()) {
				stack.push(tree.childs[v].get(childIdx[v]++));
			} else {
				out[v] = ptr++;
				stack.pop();
			}
		}

		this.seg = new SegTree<>(2 * n, strategy::mul, strategy.identity());
	}

	/**
	 * 頂点 v の値を val に設定する。
	 * @param v 頂点
	 * @param val 値
	 */
	public void setVertexValue(int v, T val) {
		seg.set(in[v], val);
		seg.set(out[v], strategy.inverse(val));
		vertexValues[v] = val;
	}

	/**
	 * 頂点 v の値に val を掛ける（mergeする）。
	 * @param v 頂点
	 * @param val 掛けたい値
	 */
	public void updateVertexValue(int v, T val) {
		T newVal = strategy.mul(vertexValues[v], val);
		setVertexValue(v, newVal);
	}

	/**
	 * 辺 {u, v} の値を val に設定する。
	 * 根に近い方の頂点を親、遠い方を子とし、子側の頂点に値を紐付ける。
	 * @param u 頂点1
	 * @param v 頂点2
	 * @param val 値
	 */
	public void setEdgeValue(int u, int v, T val) {
		int child = (tree.depth[u] > tree.depth[v]) ? u : v;
		setVertexValue(child, val);
	}

	/**
	 * 辺 {u, v} の値に val を掛ける（mergeする）。
	 * @param u 頂点1
	 * @param v 頂点2
	 * @param val 掛けたい値
	 */
	public void updateEdgeValue(int u, int v, T val) {
		int child = (tree.depth[u] > tree.depth[v]) ? u : v;
		updateVertexValue(child, val);
	}

	/**
	 * 頂点 u から v までのパス上の頂点の積を計算する。
	 * @param u 頂点1
	 * @param v 頂点2
	 * @return パス上の頂点の積
	 */
	public T foldPathVertex(int u, int v) {
		int lca = tree.lca(u, v);
		T resU = seg.fold(0, in[u] + 1);
		T resV = seg.fold(0, in[v] + 1);
		T resL = seg.fold(0, in[lca] + 1);
		int p = tree.parent[lca];
		T resP = (p < 0 || p >= tree.N) ? strategy.identity() : seg.fold(0, in[p] + 1);

		T res = strategy.mul(resU, resV);
		res = strategy.mul(res, strategy.inverse(resL));
		res = strategy.mul(res, strategy.inverse(resP));
		return res;
	}

	/**
	 * 頂点 u から v までのパス上の辺の積を計算する。
	 * @param u 頂点1
	 * @param v 頂点2
	 * @return パス上の辺の積
	 */
	public T foldPathEdge(int u, int v) {
		int lca = tree.lca(u, v);
		T resU = seg.fold(0, in[u] + 1);
		T resV = seg.fold(0, in[v] + 1);
		T resL = seg.fold(0, in[lca] + 1);

		T res = strategy.mul(resU, resV);
		T invResL = strategy.inverse(resL);
		res = strategy.mul(res, invResL);
		res = strategy.mul(res, invResL);
		return res;
	}
}
