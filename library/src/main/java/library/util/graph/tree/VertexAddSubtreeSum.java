package library.util.graph.tree;

import library.util.algebra.strategy.monoid.MonoidStrategy;
import library.util.segtree.SegTree;
/**
 * https://judge.yosupo.jp/problem/vertex_add_subtree_sum
 * @param <Monoid>
 */
public class VertexAddSubtreeSum<Monoid> {
	Tree tree;
	SegTree<Monoid> seg;
	MonoidStrategy<Monoid> strategy;
	int[] in, out;
	
	public VertexAddSubtreeSum(Tree tree, MonoidStrategy<Monoid> strategy) {
		if(!tree.isRooted())throw new AssertionError();
		this.tree=tree;
		this.strategy=strategy;
		in = new int[tree.N];
		out = new int[tree.N];
		dfs(tree.root, -1, in, out, 0);
		seg=new SegTree<Monoid>(tree.N, strategy::mul, strategy.identity());
	}
	
	int dfs(int cur, int par, int[] in, int[] out, int counter) {
		in[cur]=counter++;
		for (int dst:tree.adj[cur]) {
			if (dst==par) continue;
			counter=dfs(dst,cur,in,out,counter);
		}
		return out[cur]=counter;
	}
	
	
	public void mul(int a, Monoid val) {
		seg.mul(in[a], val);
	}
	
	public void set(int a, Monoid val) {
		seg.set(in[a], val);
	}

	
	public Monoid fold(int a) {
		return seg.fold(in[a], out[a]);
	}

	/**
	 * 辺{a, parent[a]}を切ったときのparent[a]側の連結成分のfold。未テスト
	 * @param a
	 * @return
	 */
	public Monoid cofold(int a) {
		return strategy.mul(seg.fold(0, in[a]), seg.fold(out[a], tree.N));
	}
}