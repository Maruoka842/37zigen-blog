package library.util.graph;

import java.awt.BasicStroke;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.graphstream.ui.graphicGraph.stylesheet.Color;

import library.tools.FastScanner;
import library.util.Ints;
import library.util.collections.MyPriorityQueue;


public class DoubleValueDigraph {

	public int N;
	public int M;
	public ArrayList<DoubleValueEdge>[] adj;
	
	public DoubleValueDigraph(int N) {
		this.N = N;
		adj = new ArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
	}
	
	public static DoubleValueDigraph read(int N, int M) {
		DoubleValueDigraph graph = new DoubleValueDigraph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			double cost = sc.nextDouble();
			graph.adj[a].add(new DoubleValueEdge(a, b, cost));
		}
		return graph;
	}
	
	public void addEdge(int from, int to, double cost) {
		adj[from].add(new DoubleValueEdge(from, to, cost));
		++M;
	}
	
	public int[] outDegrees() {
		int[] ret = new int[N];
		Arrays.setAll(ret, i -> adj[i].size());
		return ret;
	}
	
	public int[] inDegrees() {
		int[] ret = new int[N];
		for (int i = 0; i < N; ++i) {
			for (DoubleValueEdge e : adj[i]) ret[e.dst]++;
		}
		return ret;
	}
	
	
	public List<DoubleValueEdge> edges() {
		List<DoubleValueEdge> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (DoubleValueEdge e : adj[i]) {
				list.add(e);
			}
		}
		return list;
	}
	
	public double edgeCost() {
		double ret = 0;
		for (int i = 0; i < N; i++) {
			for (DoubleValueEdge e : adj[i]) {
				ret += e.cost;
			}
		}
		return ret;
	}
	
	
	public record DijkstraResult(double[] dist, int[] parent) {
		
	}
	
	/**
	 * 到達できない頂点 v への距離はLong.MAX_VALUE
	 * また最短路木の v の親は -1。
	 * @param src
	 * @return
	 */
	public DijkstraResult dijkstra(int src) {
		double[]dist=new double[N];
		int[] parent=new int[N];
		double INF=Double.MAX_VALUE;
		Arrays.fill(dist, INF);
		dist[src]=0;
		Arrays.fill(parent, -1);
		record State(double d, int v) implements Comparable<State> {

			@Override
			public int compareTo(State o) {
				return Double.compare(d, o.d);
			}
			
		}
		MyPriorityQueue<State> pq=new MyPriorityQueue<>();
		pq.add(new State(0, src));
		while (!pq.isEmpty()) {
			State state=pq.poll();
			int v=state.v;
			if (dist[v]<state.d)continue;
			for(var e : adj[v]) {
				double nd=dist[v]+e.cost;
				if(nd<dist[e.dst]) {
					dist[e.dst]=nd;
					parent[e.dst]=v;
					pq.add(new State(nd, e.dst));
				}
			}
		}
		return new DijkstraResult(dist, parent);
	}
	
	/**
	 * 最短路木の親から最短路を復元する。
	 * @param parent
	 * @return
	 */
	public ArrayList<Integer> restoreShortestPath(int dst, int[] parent) {
		ArrayList<Integer> list=new ArrayList<>();
		do {
			list.add(dst);
			dst=parent[dst];
		} while (dst != -1);
		Collections.reverse(list);
		return list;
	}
	
	public void draw() {
		System.setProperty("org.graphstream.ui", "swing");

		org.graphstream.graph.Graph g = new org.graphstream.graph.implementations.SingleGraph("LongValueDigraph");
		g.setAttribute("ui.stylesheet", """
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

		for (int i = 0; i < N; ++i) {
			org.graphstream.graph.Node node = g.addNode(String.valueOf(i));
			node.setAttribute("ui.label", String.valueOf(i));
		}
		Set<Long>set=new HashSet<>();
		for (int i = 0; i < N; ++i) {
			for (DoubleValueEdge e : adj[i]) {
				String id = e.src + "->" + e.dst;
				if (g.getEdge(id) == null) {
					org.graphstream.graph.Edge edge = g.addEdge(id, String.valueOf(e.src), String.valueOf(e.dst), true);
					// mark reverse edges
					if (set.contains(Ints.pack(e.dst, e.src))) {
						edge.setAttribute("ui.class", "rev");

					}
					edge.setAttribute("ui.label", String.valueOf(e.cost));
					set.add(Ints.pack(e.src, e.dst));
				}
			}
		}

		g.display();
	}
	
	/**
	 * a < b <=> a から b への path が存在
	 * という条件の元で頂点の全順序 v[0] <= v[1] <= .. <= v[n-1] を定め、vを返す。 
	 * DAGでない場合エラー。
	 * @return
	 */
	public int[] topologicalOrder() {
		int[] inDegrees = inDegrees();
		Queue<Integer> que = new ArrayDeque<>();
		for (int i  = 0; i < N; ++i) {
			if (inDegrees[i] == 0) que.add(i);
		}
		boolean[] vis = new boolean[N];
		int[] order = new int[N];
		int p = 0;
		while (!que.isEmpty()) {
			int v = que.poll();
			order[p++] = v;
			vis[v] = true;
			for (var e : adj[v]) {
				if (!vis[e.dst]) {
					--inDegrees[e.dst];
					if (inDegrees[e.dst] == 0) {
						que.add(e.dst);
					}
				}
			}
		}
		if (p != N) throw new AssertionError("DAGではない");
		return order;
	}


	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}
