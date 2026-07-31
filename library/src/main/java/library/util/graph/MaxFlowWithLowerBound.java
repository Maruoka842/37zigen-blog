package library.util.graph;

import java.util.ArrayList;

public class MaxFlowWithLowerBound {
	int n;
	int s2;
	int t2;
	ArrayList<Edge>[] g;
	MaxFlow mf;
	
	@SuppressWarnings("unchecked")
	public MaxFlowWithLowerBound(int n) {
		this.n = n;
		this.s2 = n;
		this.t2 = n+1;
		mf = new MaxFlow(n+2);
	}
	
	public void addEdge(int u, int v, int cap) {
		mf.addEdge(u, v, cap);
	}
	
	/**
	 * この辺に、最低でもlowerFlowBoundだけFlowを流す
	 * @param u
	 * @param v
	 * @param cap
	 * @param lowerFlowBound
	 */
	public void addEdge(int u, int v, int cap, int lowerFlowBound) {
		mf.addEdge(u, v, cap-lowerFlowBound);
		mf.addEdge(u, t2, lowerFlowBound);
		mf.addEdge(s2, v, lowerFlowBound);
	}
	
	/**
	 * 存在しないときは-1を返す。
	 * @param s
	 * @param t
	 * @return
	 * 実行可能解を求めるところまでhttps://atcoder.jp/contests/abc285/submissions/70743963
	 * 実行可能解からmaxFlowを求めるところは未検証
	 */
	public long getMaxFlowValue(int s, int t) {
		mf.addEdge(t, s, Integer.MAX_VALUE);
		long flow=mf.maxFlowValue(s2, t2);
		for (var e : mf.g[s2]) {
			flow -= e.cap;
		}
		if (flow != -0) {
			return -1;
		}
		mf.g[s].removeLast();
		mf.g[t].removeLast();
		return mf.maxFlowValue(s, t);
	}
	
	
	
}
