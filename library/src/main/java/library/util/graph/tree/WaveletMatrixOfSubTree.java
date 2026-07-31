package library.util.graph.tree;

import library.util.fold.WaveletMatrix;
import library.util.graph.*;

import java.util.Arrays;

import library.util.seq.Permutation;
public class WaveletMatrixOfSubTree {
// https://atcoder.jp/contests/abc337/submissions/72425742
	WaveletMatrix wm;
	int[] in, out;
	int N;
	
	public WaveletMatrixOfSubTree(Tree tree) {
		if(!tree.isRooted())throw new AssertionError();
		N=tree.N;
		in = new int[tree.N];
		out = new int[tree.N];
		dfs(tree.root, -1, in, out, 0, tree);
		var inverseIn = Permutation.inverse(in);
		wm=new WaveletMatrix(inverseIn);
	}

	public WaveletMatrixOfSubTree(Tree tree, long[] weight) {
		if(!tree.isRooted())throw new AssertionError();
		N=tree.N;
		in = new int[tree.N];
		out = new int[tree.N];
		dfs(tree.root, -1, in, out, 0, tree);
		long[] w = new long[N];
		for (int i = 0; i < N; i++) {
			w[in[i]]=weight[i];
		}
		wm=new WaveletMatrix(w);
	}

	
	
	
	int dfs(int cur, int par, int[] in, int[] out, int counter, Tree tree) {
		in[cur]=counter++;
		for (int dst:tree.adj[cur]) {
			if (dst==par) continue;
			counter=dfs(dst,cur,in,out,counter,tree);
		}
		return out[cur]=counter;
	}
	
	public int countLeq(int v, long val) {
		return wm.countLeq(in[v], out[v], val);
	}
	
	public int countLess(int v, long val) {
		return wm.countLess(in[v], out[v], val);
	}
	
	public int countGreater(int v, long val) {
		return wm.countGreater(in[v], out[v], val);
	}
	
	public long quantile(int v, int k) {
		//https://atcoder.jp/contests/abc239/submissions/74355334
		return wm.quantile(in[v], out[v], k);
	}
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
