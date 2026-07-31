package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import library.util.collections.IntDeque;

public class MaxFlow {
	
	final long INF=Long.MAX_VALUE/3;
	
	public class Edge {
		public final int v;
		public final long cap;
		public long flow;//flowは非負。 flowとie.flowの高々一方のみが正。
		final int invEdgeId;

		public Edge(int v, long cap, int invEdgeId) {
			this.v = v;
			this.cap = cap;
			this.invEdgeId = invEdgeId;
		}
		long res() {
			return cap-flow+g[v].get(invEdgeId).flow;
		}
	}
	int n;
	public ArrayList<Edge>[] g;
	
	@SuppressWarnings("unchecked")
	public MaxFlow(int n) {
		this.n = n;
		g = new ArrayList[n];
		Arrays.setAll(g, i->new ArrayList<>());
	}
	
	public void addEdge(int u, int v, long cap) {
		if(cap<0)throw new AssertionError();
		Edge e=new Edge(v, cap, g[v].size());
		Edge ie=new Edge(u, 0, g[u].size());
		g[u].add(e);
		g[v].add(ie);
	}
	
	int[] d;
	int[] itr;
	int s, t;
	
	/**
	 * 一度最大流を求めた後に、辺を追加して再度呼び出すと、追加でいくつ流せるかが返される。
	 * O(N^2 M)
	 * @param s
	 * @param t
	 * @return
	 * verified:https://atcoder.jp/contests/abc263/submissions/71027612
	 */
	public long maxFlowValue(int s, int t) {
		return maxFlowValue(s, t, Long.MAX_VALUE/3);
	}
	/**
	 * 最大流量がcutoff以上になったら打ち切る。
	 * @param s
	 * @param t
	 * @param cutoff
	 * @return
	 * verified:https://judge.u-aizu.ac.jp/onlinejudge/review.jsp?rid=11030642#1
	 */
	public long maxFlowValue(int s, int t, long cutoff) {
		if(s==t)return 0;
		this.s=s;
		this.t=t;
		long ans=0;
		d = new int[n];
		itr = new int[n];
		while(ans < cutoff) {
			IntDeque que=new IntDeque();
			que.addLast(s);
			Arrays.fill(itr, 0);
			Arrays.fill(d, n+1);
			d[s]=0;
			//残余ネットワークで最短路を求める
			while(!que.isEmpty()) {
				int v=que.pollFirst();
				if(v==t)break;
				for(Edge e:g[v]) {
					if(d[e.v]==n+1&&e.res()>0) {
						d[e.v]=d[v]+1;
						que.addLast(e.v);
					}
				}
			}
			if(d[t]==n+1)break;
			//最短路上でフローを流す
			while(ans<cutoff) {//一回につき飽和辺が一つ以上増えるので、高々O(E)回周る。
				long add=dfs(s, cutoff-ans);
				ans+=add;
				if(add==0)break;
			}
		}
		return Math.min(ans, cutoff);
	}
	
	private long dfs(int v, long upper) {
		if(v==t)return upper;
		while(itr[v]<g[v].size()) {
			Edge e=g[v].get(itr[v]);
			if(e.res()>0&&d[e.v]==d[v]+1) {
				long add=dfs(e.v, Math.min(upper, e.res()));
				if(add!=0) {
					var ie=g[e.v].get(e.invEdgeId);
					long add0=Math.min(ie.flow, add);
					ie.flow -= add0;
					e.flow += add - add0;
					return add;
				}
			}
			itr[v]++;//e.res()>0&&d[e.v]==d[v]+1はdfsでflowを一回流しただけで満たされなくなるとは限らないなので、ifの前ではなくifの後に呼ぶ。ifの前に置くと遅い。
		}
		return 0;
	}
	
	

	/**
	 * GraphStreamを用いた可視化
	 * - 青い矢印: 通常のエッジ
	 * - 赤い曲線: 逆向きエッジ
	 * - ラベル: 現在のフロー / 容量
	 */
	public void draw() {
		System.setProperty("org.graphstream.ui", "swing");
		org.graphstream.graph.Graph graph = new SingleGraph("MaxFlow");

		graph.setAttribute("ui.stylesheet", """
			node {
				fill-color: lightblue;
				size: 30px;
				text-alignment: center;
				text-size: 18;
				text-color: black;
				stroke-mode: plain;
				stroke-color: black;
			}
			edge {
				fill-color: gray;
				text-size: 14;
				text-background-mode: plain;
				text-background-color: white;
				text-padding: 2px;
				arrow-shape: arrow;
				arrow-size: 10px, 6px;
				shape: line;
				text-offset: -10px, 0;
				text-alignment: along;
			}
			edge.rev {
				shape: cubic-curve;
				fill-color: red;
			    text-offset: 10px, 0;
				text-color: red;
			}
		""");

		for (int i = 0; i < n; ++i) {
			Node node = graph.addNode(String.valueOf(i));
			node.setAttribute("ui.label", String.valueOf(i));
		}


		for (int u = 0; u < n; ++u) {
			for (Edge e : g[u]) {
				// 無向ペアIDを作る

				if (e.cap > 0) {
					String id = u + "->" + e.v;
					Edge rev = g[e.v].get(e.invEdgeId);

					org.graphstream.graph.Edge ge = graph.addEdge(id, String.valueOf(u), String.valueOf(e.v), true);
					ge.setAttribute("ui.label", e.flow + "/" + e.cap);

					// 逆方向も存在していれば赤線
					if (rev.cap > 0) {
						ge.setAttribute("ui.class", "rev");
					}
				}
			}
		}

		graph.display();
	}
	
	/**
	 * 最大流を計算した後に呼ぶこと
	 * @return
	 * https://atcoder.jp/contests/abc239/submissions/71991711
	 */
	public boolean[] reachableFromSourceOnResidualNetwork() {
		boolean[]vis=new boolean[n];
		IntDeque que=new IntDeque();
		que.addLast(s);
		vis[s]=true;
		while(!que.isEmpty()) {
			int v = que.pollFirst();
			for (Edge e : g[v]) {
				if (e.res() > 0 && !vis[e.v]) {
					vis[e.v] = true;
					que.addLast(e.v);
				}
			}
		}
		return vis;
	}
}
