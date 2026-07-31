package library.util;

import java.util.ArrayList;

import library.util.graph.LongValueDigraph;

public class MaxPotentialDifference {
	int N;
	LongValueDigraph g;
	boolean containsNegativeEdge=false;
	
	public MaxPotentialDifference(int N) {
		this.N = N;
		g = new LongValueDigraph(N);
	}
	
	/**
	 * <pre>v[a] = v[b] + w</pre>
	 * 内部ではbからaへの重みwの辺とaからbへの重み-wの辺が貼られる。Bellman-Ford確定。
	 * @param a
	 * @param b
	 */
	public void forceEqual(int a, int b, long w) {
		if (a == b) return;
		g.addEdge(a, b, -w);
		g.addEdge(b, a, w);
		containsNegativeEdge=true;
	}
	
	/**
	 * <pre>v[a] ≤ v[b] + w</pre>
	 * 内部ではbからaへの重みwの辺が貼られる。
	 * @param a
	 * @param b
	 */
	public void forceDiffLeq(int a, int b, long w) {
		g.addEdge(b, a, w);
		if(w<0)containsNegativeEdge=true;
	}
	
	class Leq {
		int v;
		long w;
		public Leq(int v, long w) {
			this.v=v;
			this.w=w;
		}
	}
	
	ArrayList<Leq> leqs=new ArrayList<>();
	
	
	/**
	 * <pre>v[a] ≤ w</pre>
	 * 内部ではfixからaへの重みwの辺が貼られる。
	 */
	public void forceLeq(int a, long w) {
		leqs.add(new Leq(a, w));
	}
	
	/**
	 * <pre>|v[a]-v[b]| ≤ w</pre>
	 * 内部ではbからaへの重みwの辺とaからbへの重みwの辺が貼られる。
	 * @param a
	 * @param b
	 */
	public void forceAbsDiffLeq(int a, int b, long w) {
		forceDiffLeq(a, b, w);
		forceDiffLeq(b, a, w);
	}
	
	private boolean flushLeqs(int fix) {
		for (var leq : leqs) {
			if (leq.v == fix) {
				if (leq.w < 0) return false;
			} else {
				forceDiffLeq(leq.v, fix, leq.w);
			}
		}
		leqs.clear();
		return true;
	}
	
	
	/**
	 * v[fix]=0の下で各iについてv[i]を最大化する。
	 * <ul>
	 * <li>負辺がある場合　O(NM) (Bellman-Ford)</li>
	 * <li>負辺がない場合 Dijkstra</li>
	 * </ul>
	 * @param s
	 * @param t
	 * @return
	 */
	public long[] maximize(int fix) {
		//https://atcoder.jp/contests/past21-open/submissions/75015150
		flushLeqs(fix);
		long[] ret;
		if(containsNegativeEdge) {
			ret = g.bellmanFord(fix);
		} else {
			ret = g.dijkstra(fix).dist();
		}
		for (int i = 0; i < ret.length; i++) {
			if(ret[i]==Long.MIN_VALUE) return null;
		}
		return ret;
	}
	
	/**
	 * v[fix]=0の下で各iについてv[i]を最小化する。
	 * 非負だとDijkstra
	 * -∞のときはLong.MIN_VALUE。
	 * 解が存在しないときはnull
	 * @param fix
	 * @return
	 */
	public long[] minimize(int fix) {
		//https://atcoder.jp/contests/abc404/submissions/75016005
		/* v[a] <= v[b] + w の下で各 i について v[i] を最小化
		 * 
		 * u[i]=-v[i] と置くと
		 *   v[a] <= v[b] + w
		 * ⇔u[a] >= u[b] - w
		 * ⇔u[b] <= u[a] + w
		 */
		flushLeqs(fix);
		long[] ret;
	    if (containsNegativeEdge) {
	    	ret = g.reverse().bellmanFord(fix);
	    } else {
	        ret = g.reverse().dijkstra(fix).dist();
	    }
		for (int i = 0; i < g.N; i++) {
			if(ret[i]==Long.MAX_VALUE) {
				ret[i]=Long.MIN_VALUE;
			} else if(ret[i]==Long.MIN_VALUE) {
				return null;
			} else {
				ret[i] = -ret[i];
			}
		}
		return ret;
	}
	
	
    public record MinMaxResult(long[] min, long[] max) {};

	public  MinMaxResult getRange(int fix) {
		//https://atcoder.jp/contests/past21-open/submissions/75016213
		flushLeqs(fix);
		long[] lower;
		long[] upper;
	    if (containsNegativeEdge) {
	    	lower = g.reverse().bellmanFord(fix);
	    	upper = g.bellmanFord(fix);
	    } else {
	        lower = g.reverse().dijkstra(fix).dist();
	        upper = g.dijkstra(fix).dist();
	    }
		for (int i = 0; i < g.N; i++) {
			if(lower[i]==Long.MAX_VALUE) {
				lower[i]=Long.MIN_VALUE;
			} else if(lower[i]==Long.MIN_VALUE) {
				return null;
			} else {
				lower[i] = -lower[i];
			}
		}
		for (int i = 0; i < upper.length; i++) {
			if(upper[i]==Long.MIN_VALUE) return null;
		}
		return new MinMaxResult(lower, upper);
	}
}