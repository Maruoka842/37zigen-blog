package library.util.graph.tree;

import library.util.segtree.SegTreelong;

/**
 * treeが根付きでない場合エラー。
 * 今のところ、可換モノイドしか乗らない。
 * @param <Acted>
 * verified:https://judge.yosupo.jp/submission/322860
 */
public class LongSegOnTree {
	
	HLDecomposition hl;
	SegTreelong seg;
	
	/**
	 * 可換群の場合は、オイラーツアーでパス積を取る方が高速
	 * @param tree
	 * @param seg
	 */
	public LongSegOnTree(Tree tree, SegTreelong seg) {
		if(tree.M!=tree.N-1)throw new AssertionError();
		if(!tree.isRooted())throw new AssertionError();
		this.hl=new HLDecomposition(tree);
		this.seg=seg;
	}
	
	public void setVertexValue(int a, long val) {
		seg.set(hl.id[a], val);
	}
	
	public void addVertexValue(int a, long val) {
		seg.mul(hl.id[a], val);
	}
	
	/**
	 * 辺 {a, b} に val を割り当てる。
	 * 内部では、{a, b}={v,parent[v]}として、vに割り当てている。
	 * @param a
	 * @param b
	 * @param val
	 */
	public void setEdgeValue(int a, int b, long val) {
		if(hl.depth[a]<hl.depth[b]) {
			setEdgeValue(b, a, val);
			return;
		}
		if(hl.parent[a]!=b) {
			throw new AssertionError();
		}
		seg.set(hl.id[a], val);
	}
	
	public long fold(int a, int b) {
		long ea = seg.identity();
		long eb = seg.identity();

		while (hl.head[a] != hl.head[b]) {
			if (hl.depth[hl.head[a]] < hl.depth[hl.head[b]]) {
				int tmp = a;
				a = b;
				b = tmp;
				long tmp_e = ea;
				ea = eb;
				eb = tmp_e;
			}
			ea = seg.getOp().applyAsLong(seg.fold(hl.id[hl.head[a]], hl.id[a] + 1), ea);
			a = hl.parent[hl.head[a]];
		}
		if (hl.depth[a] < hl.depth[b]) {
			int tmp = a;
			a = b;
			b = tmp;
			long tmp_e = ea;
			ea = eb;
			eb = tmp_e;
		}
		return seg.getOp().applyAsLong(eb, seg.getOp().applyAsLong(seg.fold(hl.id[b], hl.id[a] + 1), ea));
	}
	
	public long foldOnEdge(int a, int b) {
		int lca=hl.lca(a, b);
		long ret = seg.identity();
		if(a != lca) {
			int pa=hl.f.getLevelAncestor(a, hl.depth[a]-hl.depth[lca]-1);
			ret=seg.getOp().applyAsLong(ret, fold(a, pa));
		}
		if(b != lca) {
			int pb=hl.f.getLevelAncestor(b, hl.depth[b]-hl.depth[lca]-1);
			ret=seg.getOp().applyAsLong(ret, fold(b, pb));
		}
		return ret;
	}


}