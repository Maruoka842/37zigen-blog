package library.util.graph.tree;

import library.util.graph.*;

import java.util.function.LongPredicate;

import library.util.segtree.LazySegTreeStrategy_longlong;
import library.util.segtree.LazySegTreelonglong;
import library.util.seq.Permutation;
/**
 * https://atcoder.jp/contests/abc369/submissions/72236529
 */
public class SubtreeAddVertexGet_longlong {
	LazySegTreelonglong seg;
	LazySegTreeStrategy_longlong strategy;
	int[] in, out;
	int[] inverseIn;
	int N;
	
	public SubtreeAddVertexGet_longlong(Tree tree, LazySegTreeStrategy_longlong strategy) {
		if(!tree.isRooted())throw new AssertionError();
		N=tree.N;
		this.strategy=strategy;
		in = new int[tree.N];
		out = new int[tree.N];
		seg=new LazySegTreelonglong(tree.N, strategy::mergeA, strategy::mergeX, strategy::mergeAX, strategy.identityA(), strategy.identityX());
		dfs(tree.root, -1, in, out, 0, tree);
		inverseIn = Permutation.inverse(in);
	}

	
	public SubtreeAddVertexGet_longlong(LongValueTree tree, LazySegTreeStrategy_longlong strategy) {
		if(!tree.isRooted())throw new AssertionError();
		N=tree.N;
		this.strategy=strategy;
		in = new int[tree.N];
		out = new int[tree.N];
		seg=new LazySegTreelonglong(tree.N, strategy::mergeA, strategy::mergeX, strategy::mergeAX, strategy.identityA(), strategy.identityX());
		dfs(tree.root, -1, in, out, 0, tree);
		inverseIn = Permutation.inverse(in);
	}
	
	
	
	
	int dfs(int cur, int par, int[] in, int[] out, int counter, Tree tree) {
		in[cur]=counter++;
		for (int dst:tree.adj[cur]) {
			if (dst==par) continue;
			counter=dfs(dst,cur,in,out,counter,tree);
		}
		return out[cur]=counter;
	}
	
	
	int dfs(int cur, int par, int[] in, int[] out, int counter, LongValueTree tree) {
		in[cur]=counter++;
		for (var e:tree.adj[cur]) {
			if (e.dst==par) continue;
			counter=dfs(e.dst,cur,in,out,counter,tree);
		}
		return out[cur]=counter;
	}
	
	public void act(int a, long val) {
		seg.act(in[a], out[a], val);
	}
	
	public void set(int a, long val) {
		seg.set(in[a], val);
	}
	
	public long get(int a) {
		return seg.get(in[a]);
	}
	
	public long prodAll() {
		return seg.fold(0, N);
	}
	
	public int maximalRight(int a, LongPredicate f) {
		int ret=seg.maximalRight(in[a], f);
		return ret==N?ret:inverseIn[ret];
	}
}