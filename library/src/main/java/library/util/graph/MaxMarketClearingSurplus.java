package library.util.graph;

public class MaxMarketClearingSurplus {
	int N;
	int s;
	int t;
	public MinimumCostFlow mcf;
	long INF=Long.MAX_VALUE/3;
	boolean negCostAndINFCap=false;
	
	public MaxMarketClearingSurplus(int N) {
		this.s = N;
		this.t = N + 1; 
		this.N = N + 2;
		mcf = new MinimumCostFlow(this.N);
	}
	
	/**
	 * p[v] ≤ p[u] + w
	 * @param u
	 * @param v
	 * @return
	 * https://atcoder.jp/contests/abc397/submissions/72662342
	 */
	public void addHardConstraint(int u, int v, long w) {
		/*
		 * max -∞ * z[vu]
		 * s.t. p[u] ≤ p[v] + w[vu] + z[vu] 
		 *      z[vu] ≥ 0
		 * はz[vu]=0となるのでp[u] ≤ p[v] + w[vu]が残る。
		 */
		negCostAndINFCap |= w < 0;
		mcf.addEdge(u, v, w, INF);
	}
	
	
	/**
	 * p[v] ≤  w
	 * @param u
	 * @param v
	 * @return
	 * https://atcoder.jp/contests/abc397/submissions/72662342
	 */
	public void addHardConstraint2(int v, long w) {
		negCostAndINFCap |= w < 0;
		mcf.addEdge(s, v, w, INF);
	}

	
	/**
	 * 0 ≤ p[u] + w
	 * @param u
	 * @param v
	 * @return
	 * https://atcoder.jp/contests/abc397/submissions/72662342
	 */
	public void addHardConstraint3(int u, long w) {
		negCostAndINFCap |= w < 0;
		mcf.addEdge(u, t, w, INF);
	}

	
	/**
	 * c >= 0 を仮定（c < 0 なら目的関数を好きなだけ大きくできる）。
	 * 目的関数に -c max(0, p[v] - p[u] - w) を足す。
	 * 
	 * すなわち、
	 * p[v] ≤ p[u] + z[e] + w  </br>
	 * z[e] ≥ 0
	 * を制約に追加し、目的関数から c z[e] を引くことと同じ。
	 * 
	 * @param u
	 * @param v
	 * @return
	 */
	public void addSoftConstraintWithPenalty(int u, int v, long c, long w) {
		if (c < 0) throw new AssertionError();
		mcf.addEdge(u, v, w, c);
	}
	
	/**
	 * c >= 0 を仮定（c < 0 なら目的関数を好きなだけ大きくできる）。
	 * 目的関数に -c max(0, p[v] - w) を足す。
	 * 
	 * @param u
	 * @param v
	 * @return
	 */
	public void addSoftConstraintWithPenalty2(int v, long c, long w) {
		if (c < 0) throw new AssertionError();
		mcf.addEdge(s, v, w, c);
	}
	
	
	/**
	 * c >= 0 を仮定（c < 0 なら目的関数を好きなだけ大きくできる）。
	 * 目的関数に -c max(0, - p[u] - w) を足す。
	 * 
	 * @param u
	 * @param v
	 * @return
	 */
	public void addSoftConstraintWithPenalty3(int u, long c, long w) {
		if (c < 0) throw new AssertionError();
		mcf.addEdge(u, t, w, c);
	}
	
	/**
	 * max  -Σ b[v]p[v] - Σc[e]z[e]
	 * s.t. p[v] ≤ p[u] + w[uv] + z[uv] 
	 *      z[e] ≥ 0
	 * @return
	 */
	public long maximize() {
		//https://atcoder.jp/contests/abc397/submissions/72662342
		if (negCostAndINFCap) throw new AssertionError("負辺の解消でexcessが∞になるので、DAGでポテンシャル前計算か、流量上限を課す");
		return mcf.minCostFlowAtMost(s, t, INF);
	}

	public boolean isFeasible() {
		//https://atcoder.jp/contests/abc397/submissions/72662342
		LongValueDigraph g=new LongValueDigraph(N);
		for (var edges : mcf.g) {
			for (var e : edges) {
				if (e.cap == INF) {
					g.addEdge(e.from, e.to, e.cost);
				}
			}
		}
		return !g.containsNegativeCycle();
	}
}