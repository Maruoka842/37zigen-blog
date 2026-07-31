package library.util.graph.tree;

import library.util.graph.*;

import java.util.function.Predicate;

import library.util.segtree.LazySegTree;
import library.util.segtree.LazySegTreeStrategy;
import library.util.seq.Permutation;

public class SubtreeAddVertexGet<Acting, Acted> {
	Tree tree;
	LazySegTree<Acting, Acted> seg;
	LazySegTreeStrategy<Acting, Acted> strategy;
	int[] in, out;
	int[] inverseIn;
	
	public SubtreeAddVertexGet(Tree tree, LazySegTreeStrategy<Acting, Acted> strategy) {
		if(!tree.isRooted())throw new AssertionError();
		this.tree=tree;
		this.strategy=strategy;
		in = new int[tree.N];
		out = new int[tree.N];
		seg=new LazySegTree<Acting, Acted>(tree.N, strategy);
		dfs(tree.root, -1, in, out, 0);
		inverseIn = Permutation.inverse(in);
	}
	
	int dfs(int cur, int par, int[] in, int[] out, int counter) {
		in[cur]=counter++;
		for (int dst:tree.adj[cur]) {
			if (dst==par) continue;
			counter=dfs(dst,cur,in,out,counter);
		}
		return out[cur]=counter;
	}
	
	public void act(int a, Acting val) {
		seg.act(in[a], out[a], val);
	}
	
	public void set(int a, Acted val) {
		seg.set(in[a], val);
	}
	
	public Acted get(int a) {
		return seg.get(in[a]);
	}
	
	public Acted prodAll() {
		return seg.fold(0, tree.N);
	}
	
	public int maximalRight(int a, Predicate<Acted> f) {
		int ret=seg.maximalRight(in[a], f);
		return ret==tree.N?ret:inverseIn[ret];
	}

}
