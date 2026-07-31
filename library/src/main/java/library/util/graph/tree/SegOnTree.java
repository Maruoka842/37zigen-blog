package library.util.graph.tree;

import library.util.segtree.SegTree;

/**
 * treeが根付きでない場合エラー。
 * 今のところ、可換モノイドしか乗らない。
 * @param <Acted>
 * verified:https://judge.yosupo.jp/submission/322860
 */
public class SegOnTree<Acted> {
	
	HLDecomposition hl;
	SegTree<Acted> seg;
	
	/**
	 * 可換群の場合は、オイラーツアーでパス積を取る方が高速
	 * @param tree
	 * @param seg
	 */
	public SegOnTree(Tree tree, SegTree<Acted> seg) {
		if(tree.M!=tree.N-1)throw new AssertionError();
		if(!tree.isRooted())throw new AssertionError();
		this.hl=new HLDecomposition(tree);
		this.seg=seg;
	}
	
	public void setVertexValue(int a, Acted val) {
		seg.set(hl.id[a], val);
	}
	
	public void addVertexValue(int a, Acted val) {
		seg.mul(hl.id[a], val);
	}
	
	/**
	 * 辺 {a, b} に val を割り当てる。
	 * 内部では、{a, b}={v,parent[v]}として、vに割り当てている。
	 * @param a
	 * @param b
	 * @param val
	 */
	public void setEdgeValue(int a, int b, Acted val) {
		if(hl.depth[a]<hl.depth[b]) {
			setEdgeValue(b, a, val);
			return;
		}
		if(hl.parent[a]!=b) {
			throw new AssertionError();
		}
		seg.set(hl.id[a], val);
	}
	
	public Acted fold(int a, int b) {
		Acted ea = seg.identity();
		Acted eb = seg.identity();

		while (hl.head[a] != hl.head[b]) {
			if (hl.depth[hl.head[a]] < hl.depth[hl.head[b]]) {
				int tmp = a;
				a = b;
				b = tmp;
				Acted tmp_e = ea;
				ea = eb;
				eb = tmp_e;
			}
			ea = seg.mergeX().apply(seg.fold(hl.id[hl.head[a]], hl.id[a] + 1), ea);
			a = hl.parent[hl.head[a]];
		}
		if (hl.depth[a] < hl.depth[b]) {
			int tmp = a;
			a = b;
			b = tmp;
			Acted tmp_e = ea;
			ea = eb;
			eb = tmp_e;
		}
		return seg.mergeX().apply(eb, seg.mergeX().apply(seg.fold(hl.id[b], hl.id[a] + 1), ea));
	}
	
	public Acted foldOnEdge(int a, int b) {
		int lca=hl.lca(a, b);
		Acted ret = seg.identity();
		if(a != lca) {
			int pa=hl.f.getLevelAncestor(a, hl.depth[a]-hl.depth[lca]-1);
			ret=seg.mergeX().apply(ret, fold(a, pa));
		}
		if(b != lca) {
			int pb=hl.f.getLevelAncestor(b, hl.depth[b]-hl.depth[lca]-1);
			ret=seg.mergeX().apply(ret, fold(b, pb));
		}
		return ret;
	}


}