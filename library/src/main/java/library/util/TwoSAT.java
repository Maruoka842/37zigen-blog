package library.util;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.graph.Digraph;

public class TwoSAT {
	Digraph g;
	int n;
	int addN = 0;
	
	public TwoSAT(int n) {
		this.n = n;
		g=new Digraph(2*n);
	}
	
	/**
	 * a or b を追加。not a は ~aで表す。
	 */
	public void or(int a, int b) {
		//https://judge.yosupo.jp/problem/two_sat
		if(a>=0) {
			a=2*a;
		}else {
			a=2*(~a)+1;
		}
		if(b>=0) {
			b=2*b;
		}else {
			b=2*(~b)+1;
		}
		g.addEdge(a^1, b);// a or b <=> not a => b
		g.addEdge(b^1, a);
	}
	
	public void nand(int a, int b) {
		//https://atcoder.jp/contests/practice2/submissions/77098249
		or(~a, ~b);
	}
	
	public void neq(int a, int b) {
	    or(a, b);
	    or(~a, ~b);
	}
	
	/**
	 * a=Trueを固定。not a は ~aで表す。
	 */
	public void fixTrue(int a) {
		or(a, a);
	}
	
	public void fixFalse(int a) {
		or(~a, ~a);
	}
	
	public void ifThen(int a, int b) {
		or(~a, b);
	}
	
	
	
	public void atMostOne(ArrayList<Integer> x) {
		atMostOne(x.stream().mapToInt(Integer::intValue).toArray());
	}
	
	/**
	 * 高々一つだけTrue
	 * @param a
	 */
	public void atMostOne(int[] x) {
		//https://atcoder.jp/contests/abc210/submissions/70653206
		// b[i] = x[0] or x[1] or .. x[i] = b[i-1] or x[i]とする。
		// これは x[i] ⇒ b[i] かつ b[i-1] ⇒ b[i] とすればよい。
		// このとき、x[0], x[1], .. のうち高々一つがTrueは、
		// b[i] ⇒ !x[i+1]
		// と書ける。
		if (x.length <= 1)return;
		for (int i = 0; i < x.length; i++) {
			g.addNode();
			g.addNode();
		}
		for (int i = 0; i < x.length; i++) {
			ifThen(x[i], n+addN+i);
		}
		for (int i = 0; i+1 < x.length; i++) {
			ifThen(n+addN+i, n+addN+i+1);
		}
		for (int i = 0; i+1 < x.length; i++) {
			ifThen(n+addN+i, ~x[i+1]);
		}
		addN+=x.length;
	}
	
	
	/**
	 * 2SATを満たす割り当てを返す。存在しないときはnullを返す。
	 * @return
	 */
	public boolean[] calc() {
    	int[] logicalValues=new int[n+addN];
    	Arrays.fill(logicalValues, -1);
    	var scc=g.scc();
    	ArrayUtils.reverse(scc);
    	for (var comp:scc) {
    		for (int v : comp) {
    			if (logicalValues[v / 2] != -1) continue;
    			logicalValues[v / 2] = v % 2;
    		}
    	}
    	for (var e : g.edges()) {
    		if (logicalValues[e.src/2]==e.src%2&&logicalValues[e.dst/2]!=e.dst%2) {
    			return null;
    		}
    	}
    	boolean[] ans = new boolean[n];
    	for (int i = 0; i < n; i++) {
    	    ans[i] = logicalValues[i] == 0;
    	}
    	return ans;
	}
	
	/**
	 * 2*i      変数 i が true であるリテラル
	 * 2*i + 1  変数 i が false であるリテラル、つまり ~i
	 */
	public void draw() {
		g.draw();
	}

}
