package library.util.graph.tree;

import library.util.segtree.LazySegTree;
/**
 * 
 * @param <Acting>
 * @param <Acted>
 * verified:https://atcoder.jp/contests/abc301/submissions/70367326
 */
public class LazySegOnTree<Acting, Acted> {
	
	HLDecomposition hl;
	LazySegTree<Acting, Acted> seg;
	
	public LazySegOnTree(Tree f, LazySegTree<Acting, Acted> seg) {
		this.hl=new HLDecomposition(f);
		this.seg=seg;
		f.rooted(0);
	}
	
	public void setVertexValue(int a, Acted val) {
		seg.set(hl.id[a], val);
	}
	
	public Acted foldOnEdge(int a, int b) {
		int lca=hl.lca(a, b);
		Acted ret = seg.identityX();
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
	
	public void actOnEdge(int a, int b, Acting c) {
		int lca=hl.lca(a, b);
		if(a != lca) {
			int pa=hl.f.getLevelAncestor(a, hl.f.depth[a]-hl.f.depth[lca]-1);
			act(a, pa, c);
		}
		if(b != lca) {
			int pb=hl.f.getLevelAncestor(b, hl.f.depth[b]-hl.f.depth[lca]-1);
			act(b, pb, c);
		}
	}
	
	public Acted fold(int a, int b) {
		Acted ea = seg.identityX();
		Acted eb = seg.identityX();

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

	public void act(int a, int b, Acting c) {
		while (hl.head[a] != hl.head[b]) {
			if (hl.depth[hl.head[a]] < hl.depth[hl.head[b]]) {
				int tmp = a;
				a = b;
				b = tmp;
			}
			seg.act(hl.id[hl.head[a]], hl.id[a] + 1, c);
			a = hl.parent[hl.head[a]];
		}
		if (hl.depth[a] < hl.depth[b]) {
			int tmp = a;
			a = b;
			b = tmp;
		}
		if (hl.id[b] > hl.id[a]) {
			throw new AssertionError();
		}
		seg.act(hl.id[b], hl.id[a] + 1, c);
	}
}