package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

import library.util.ArrayUtils;
import library.util.collections.IntDeque;
/**
 * 流量をFとしてO(F(N+M)log(N+M))。負辺は最初に全て流しきるため、負辺の流量F'が、答えに必要なくてもFに加算される。F'を計算量から消したい場合は、最初から簡約費用にする。
 */
public class MinimumCostFlow {
	int N;
	public ArrayList<Edge>[] g;
	public ArrayList<Edge>[] ig;
	public long[] excess;
	public long[] potential;
	long INF = Long.MAX_VALUE / 3;
	Edge[] incommingShortestPathEdge;
	long[] d;
	long base = 0;
	
	@SuppressWarnings("unchecked")
	public MinimumCostFlow(int N) {
		this.N = N;
		g = new ArrayList[this.N];
		ig = new ArrayList[this.N];
		excess = new long[this.N];
		potential = new long[this.N];
		d = new long[this.N];
		incommingShortestPathEdge = new Edge[this.N];
		for (int i = 0; i < this.N; ++i) {
			g[i] = new ArrayList<>();
			ig[i] = new ArrayList<>();
		}
	}
	
	public class Edge {
		public int from;
		public int to;
		public long cost;
		public long cap;
		public long flow=0;
		Edge reverseEdge;
		
		public Edge(int from, int to, long cost, long cap) {
			this.from = from;
			this.to = to;
			this.cost = cost;
			this.cap = cap;
		}
		
		public long res() {
			return cap - flow;
		}
		
		long reducedCost() {
			//残余ネットワークで、常に簡約費用非負を保つ。
			//初期状態ではポテンシャル0だが、負辺を流しきっているのでok。
			
			// cost - potential[to] + potential[from] >= 0
			// potential[to] + cost >= potential[to]
			// なのである固定した頂点からの距離をpotentialとすると非負になる。
			
			return cost - (potential[to] - potential[from]);
		}
		
		void addFlow(long add) {
			flow += add;
			reverseEdge.flow -= add;
			if (flow > cap) throw new AssertionError();
			if (reverseEdge.flow > reverseEdge.cap) throw new AssertionError();
			excess[from] -= add;
			excess[to] += add;
		}
		
		@Override
		public String toString() {
			return "from "+from+" to " + to + " : flow="+flow + ", cap="+cap+", cost="+cost;
		}
	}
	
	public void addEdge(int from, int to, long cost, long cap) {
		Edge e = new Edge(from, to, cost, cap);
		Edge ie = new Edge(to, from, -cost, 0);
		e.reverseEdge = ie;
		ie.reverseEdge = e;
		g[from].add(e);
		ig[to].add(ie);
	}
	
	
	/**
	 * 流量が固定されている辺を追加する
	 */
	public void addFixedFlowEdge(int from, int to, long cost, long flow) {
		//https://atcoder.jp/contests/abc231/submissions/73435140
		addExcess(from, -flow);
		addExcess(to, flow);
		base += flow * cost;
	}
	
	
	/**
	 * supply>0の場合湧き出し、supply<0の場合吸い込み。
	 * https://atcoder.jp/contests/abc374/submissions/71941544
	 * @param v
	 * @param supply
	 */
	public void addExcess(int v, long supply) {
		excess[v] += supply;
	}
	
	/**
	 * sからtにフローを流したとき、コストは流量の関数として区分線形になる。
	 * そこで、その線分の端点を{cost, flow}として並べたarraylistを返す。
	 * 未テスト
	 * @param s
	 * @param t
	 * @return
	 */
	public ArrayList<long[]> minCostFlowSlope(int s, int t) {
		return minCostFlowSlopeInitialized(s, t, minCostCirculation());
	}

	private ArrayList<long[]> minCostFlowSlopeInitialized(int s, int t, long cost) {
		ArrayList<long[]>ret=new ArrayList<>();
		long flow = 0;
		ret.add(new long[] {flow, cost});
		if(s==t)return ret;
		while (true) {
			updateDistance(s);
			if (d[t] == INF) break;
			long addFlow = Long.MAX_VALUE;
			{
				int cur = t;
				while (cur != s) {
					addFlow = Math.min(addFlow, incommingShortestPathEdge[cur].res());
					cur = incommingShortestPathEdge[cur].from;
				}
			}
			if (addFlow == 0) break;
			flow += addFlow;
			{
				int cur = t;
				while (cur != s) {
					incommingShortestPathEdge[cur].addFlow(addFlow);
					cost += incommingShortestPathEdge[cur].cost * addFlow;
					cur = incommingShortestPathEdge[cur].from;
				}
			}
			ret.add(new long[] {flow, cost});
		}
		return ret;
	}

	/**
	 * 元のネットワークがDAGのとき、初期ポテンシャルをDAG上の最短距離で作ってから
	 * s-t最短路だけに流す。負辺が多くても最初に全負辺を流し切らない。
	 * <p>{@code s} から {@code t} へ流す流量を {@code f} としたとき、最小費用は
	 * {@code f} に関する凸な区分線形関数になる。このメソッドは、その折れ線の端点を
 	 * {@code [flow, cost]} の形で昇順に並べたリストを返す。</p>
	 * @param s
	 * @param t
	 * @return
	 */
	public ArrayList<long[]> minCostFlowSlopeOnDag(int s, int t) {
		//https://atcoder.jp/contests/abc247/submissions/75392055
		initPotentialOnDag();
		return minCostFlowSlopeInitialized(s, t, 0);
	}
	
	public Long minCostFlowAtMost(int s, int t, long cap) {
		addEdge(t, s, 0, cap);
		return minCostCirculation();
	}
	
	public Long minCostFlowOnDagAtMost(int s, int t, long cap) {
		//https://atcoder.jp/contests/abc214/submissions/74140532
		initPotentialOnDag();
		addEdge(t, s, 0, cap);//この辺のみ簡約費用が負になりえる。
		return minCostCirculation();
	}

	private void initPotentialOnDag() {
		Arrays.fill(potential, INF);
		IntDeque dq = new IntDeque();
		int[] indeg = new int[N];
		for (int i = 0; i < N; i++) {
			for (var e : g[i]) {
				indeg[e.to]++;
			}
		}
		for (int i = 0; i < N; i++) {
			if (indeg[i] == 0) {
				potential[i] = 0;
				dq.addLast(i);
			}
		}
		while (!dq.isEmpty()) {
			int v = dq.pollLast();
			for (var e : g[v]) {
				potential[e.to] = Math.min(potential[e.to], potential[e.from] + e.cost);
				indeg[e.to]--;
				if (indeg[e.to] == 0) {
					dq.addLast(e.to);
				}
			}
		}
	}
	
	/**
	 * 最小費用循環流を求める。
	 * 存在しなければnullを返す。
	 * 流量をFとしてO(F(N+M)log(N+M))
	 * @param flow
	 */
	public Long minCostCirculation() {
		//https://atcoder.jp/contests/abc407/submissions/71000667
		//https://atcoder.jp/contests/practice2/submissions/71006911
		for (var edges : g) {
			for (var edge : edges) {
				if (edge.reducedCost() < 0) {
					edge.addFlow(edge.cap);
				}
			}
		}

		while (ArrayUtils.max(excess) > 0) {
			int src = ArrayUtils.argMax(excess);
			updateDistance(src);
			int dst = 0;
			while (dst < N && (d[dst] == INF || excess[dst] >= 0)) ++dst;
			if (dst == N) {
				return null;
			}
			long addFlow = Math.min(excess[src], -excess[dst]);
			{
				int cur = dst;
				while (cur != src) {
					addFlow = Math.min(addFlow, incommingShortestPathEdge[cur].res());
					cur = incommingShortestPathEdge[cur].from;
				}
			}
			{
				int cur = dst;
				while (cur != src) {
					incommingShortestPathEdge[cur].addFlow(addFlow);
					cur = incommingShortestPathEdge[cur].from;
				}
			}
		}
		long ret = 0;
		for (var edges : g) {
			for (var edge : edges) {
				ret += edge.flow * edge.cost;
			}
		}
		return ret + base;
	}
	
	
	/**
	 * 最小費用循環流を求める。
	 * 存在しなければnullを返す。
	 * 流量をFとしてO(FN^2)
	 * @param flow
	 */
	public Long minCostCirculationDense() {
		for (var edges : g) {
			for (var edge : edges) {
				if (edge.reducedCost() < 0) {
					edge.addFlow(edge.cap);
				}
			}
		}

		while (ArrayUtils.max(excess) > 0) {
			int src = ArrayUtils.argMax(excess);
			updateDistanceDense(src);
			int dst = 0;
			while (dst < N && (d[dst] == INF || excess[dst] >= 0)) ++dst;
			if (dst == N) {
				return null;
			}
			long addFlow = Math.min(excess[src], -excess[dst]);
			{
				int cur = dst;
				while (cur != src) {
					addFlow = Math.min(addFlow, incommingShortestPathEdge[cur].res());
					cur = incommingShortestPathEdge[cur].from;
				}
			}
			{
				int cur = dst;
				while (cur != src) {
					incommingShortestPathEdge[cur].addFlow(addFlow);
					cur = incommingShortestPathEdge[cur].from;
				}
			}
		}
		long ret = 0;
		for (var edges : g) {
			for (var edge : edges) {
				ret += edge.flow * edge.cost;
			}
		}
		return ret + base;
	}

	
	
	
	void updateDistance(int src) {
		Arrays.fill(d, INF);
		Arrays.fill(incommingShortestPathEdge, null);
		d[src] = 0;
		record State (int v, long d) implements Comparable<State>{
			@Override
			public int compareTo(State o) {
				return Long.compare(d, o.d);
			}
		}
		PriorityQueue<State> pq = new PriorityQueue<>();
		pq.add(new State(src, 0));
		while (!pq.isEmpty()) {
			State state = pq.poll();
			if (state.d > d[state.v]) continue;
			for (Edge e : g[state.v]) {
				if (e.res() == 0) continue;
				if (d[e.to] > state.d + e.reducedCost()) {
					d[e.to] = state.d + e.reducedCost();
					pq.add(new State(e.to, d[e.to]));
					incommingShortestPathEdge[e.to] = e;
				}
			}
			for (Edge e : ig[state.v]) {
				if (e.res() == 0) continue;
				if (d[e.to] > state.d + e.reducedCost()) {
					d[e.to] = state.d + e.reducedCost();
					pq.add(new State(e.to, d[e.to]));
					incommingShortestPathEdge[e.to] = e;
				}
			}
		}
		for (int i = 0; i < N; ++i) {
			if (d[i] != INF)
				potential[i] += d[i];
		}
	}
	
	
	void updateDistanceDense(int src) {
		Arrays.fill(d, INF);
		Arrays.fill(incommingShortestPathEdge, null);
		d[src] = 0;
		boolean[] vis = new boolean[N];
		for (int i = 0; i < N; ++i) {
			int v = 0;
			while (vis[v]) ++v;
			for (int j = 0; j < N; ++j) {
				if (!vis[j] && d[v] > d[j]) {
					v = j;
				}
			}
			for (Edge e : g[v]) {
				if (e.res() == 0) continue;
				long nd = d[v] + e.reducedCost();
				if (nd < d[e.to]) {
					d[e.to] = nd;
					incommingShortestPathEdge[e.to] = e;
				}
			}
			for (Edge e : ig[v]) {
				if (e.res() == 0) continue;
				long nd = d[v] + e.reducedCost();
				if (nd < d[e.to]) {
					d[e.to] = nd;
					incommingShortestPathEdge[e.to] = e;
				}
			}
			
			vis[v] = true;
		}
		for (int i = 0; i < N; ++i) {
			if (d[i] != INF)
				potential[i] += d[i];
		}
	}

	
	
	public void drawNetwork() {
        System.setProperty("org.graphstream.ui", "swing");
		org.graphstream.graph.Graph graph = new MultiGraph("MyGraph");
		for (int i = 0; i < N; ++i) {
			Node node = graph.addNode(String.valueOf(i));
	        String label = String.format("%d(e=%d)", i, excess[i]);
	        node.setAttribute("ui.label", label);
		}
		int edgeId=0;
		for (int i = 0; i < N; ++i) {
			for (Edge e : g[i]) {
				String a=String.valueOf(e.from);
				String b=String.valueOf(e.to);
				graph.addEdge(""+edgeId++, a, b, true).setAttribute("ui.label", "cap="+(e.cap==INF?"∞":e.cap)+",cost="+(e.cost==INF?"∞":e.cost)+",flow="+e.flow);
			}
		}
        graph.setAttribute("ui.stylesheet",
		    "node { " +
		    "   fill-color: lightblue; " +
		    "   size: 30px; " +
		    "   text-alignment: center; " +
		    "   text-size: 20; " +
		    "   text-color: black; " +
		    "   text-mode: normal;" +
		    "}"+
		    "edge { " +
		    "   text-size: 20px; " +
		    "} "
		);
        graph.display();
	}


	public void drawResidualNetwork() {
        System.setProperty("org.graphstream.ui", "swing");
		org.graphstream.graph.Graph graph = new SingleGraph("MyGraph");
		for (int i = 0; i < N; ++i) {
			Node node = graph.addNode(String.valueOf(i));
	        String label = String.format("%d(e=%d)", i, excess[i]);
	        node.setAttribute("ui.label", label);
		}
		for (int i = 0; i < N; ++i) {
			for (Edge e : g[i]) {
				String a=String.valueOf(e.from);
				String b=String.valueOf(e.to);
				graph.addEdge(a+":"+b, a, b, true).setAttribute("ui.label", (e.res()>INF/2?"∞":e.res()));
			}
		}
		for (int i = 0; i < N; ++i) {
			for (Edge e : ig[i]) {
				String a=String.valueOf(e.from);
				String b=String.valueOf(e.to);
				org.graphstream.graph.Edge  f = graph.addEdge(a+":"+b, a, b, true);
				f.setAttribute("ui.label", (e.res()>INF/2?"∞":e.res()));
				f.setAttribute("ui.style", "text-offset: -25;");
			}
		}

        graph.setAttribute("ui.stylesheet",
		    "node { " +
		    "   fill-color: lightblue; " +
		    "   size: 30px; " +
		    "   text-alignment: center; " +
		    "   text-size: 20; " +
		    "   text-color: black; " +
		    "   text-mode: normal;" +
		    "}"
		    +
		    "edge { " +
		    "   text-size: 20px; " +
		    "   text-alignment: above; " +
		    "} "
		);
        graph.display();
	}
	
	/**
	 * H×Wのグリッドのノード(i,j)が最小費用流の頂点i+j*Hとなっているときの結果を表示する
	 * HW,HW+1はsource,sinkとして扱う
	 * @param H
	 * @param W
	 */
	public void showResult_Grid(int H, int W) {
		for (int i = 0; i < N; i++) {
			int h=i%H;
			int w=i/H;
			for(var e:g[i]) {
				if(e.flow!=0) {
					int nh=e.to%H;
					int nw=e.to/H;
					String name1="("+h+","+w+")";
					String name2="("+nh+","+nw+")";
					if(i==H*W) name1="source";
					if(i==H*W+1)name1="sink";
					if(e.to==H*W) name2="source";
					if(e.to==H*W+1)name2="sink";
					
					System.out.println(name1+"->"+name2+" flow "+e.flow);
				}
			}
		}
	}

	
	
	public void dumpResult() {
	    System.out.println("===== Minimum Cost Flow Result Dump =====");

	    long totalCost = 0;

	    System.out.println("-- Nodes --");
	    for (int i = 0; i < N; i++) {
	        System.out.printf(
	            "v=%d : excess=%d, potential=%d%n",
	            i, excess[i], potential[i]
	        );
	    }

	    System.out.println("-- Edges (flow != 0) --");
	    for (int i = 0; i < N; i++) {
	        for (Edge e : g[i]) {
	            if (e.flow != 0) {
	                long contrib = e.flow * e.cost;
	                totalCost += contrib;
	                System.out.printf(
	                    "%d -> %d | flow=%d / cap=%d | cost=%d | contrib=%d%n",
	                    e.from, e.to, e.flow, e.cap, e.cost, contrib
	                );
	            }
	        }
	    }

	    System.out.println("-- Summary --");
	    System.out.println("Total cost = " + totalCost);
	    System.out.println("========================================");
	}
	
	
	void tr(Object...o) {System.out.println(Arrays.deepToString(o));}
}
