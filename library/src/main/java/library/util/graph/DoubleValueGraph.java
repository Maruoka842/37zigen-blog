package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.collections.IntDeque;

public class DoubleValueGraph {

	public int N;
	public int M;
	public ArrayList<DoubleValueEdge>[] adj;

	@SuppressWarnings("unchecked")
	public DoubleValueGraph(int N) {
		this.N = N;
		adj = new ArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
	}

	public static DoubleValueGraph readDoubleValueGraph(int N, int M) {
		DoubleValueGraph graph = new DoubleValueGraph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			double cost = sc.nextDouble();
			graph.adj[a].add(new DoubleValueEdge(a, b, cost));
			graph.adj[b].add(new DoubleValueEdge(b, a, cost));
		}
		return graph;
	}

	public void addEdge(int a, int b, double cost) {
		++M;
		adj[a].add(new DoubleValueEdge(a, b, cost));
		adj[b].add(new DoubleValueEdge(b, a, cost));
	}

	public DoubleValueGraph homeomorphicReductionOf2core(boolean[] isTerminal) {
		@SuppressWarnings("unchecked")
		TreeSet<DoubleValueEdge>[] g = new TreeSet[N];
		for (int i = 0; i < N; ++i) g[i] = new TreeSet<>();
		for (int v = 0; v < N; ++v) for (DoubleValueEdge e : adj[v]) g[v].add(e);
		for (int i = 0; i < N; ++i) {
			int v = i;
			while (g[v].size() == 1 && !isTerminal[v]) {
				DoubleValueEdge e = g[v].first();
				g[e.dst].remove(new DoubleValueEdge(e.dst, v, e.cost));
				g[v].clear();
				v = e.dst;
			}
		}
		for (int i = 0; i < N; ++i) {
			if (g[i].size() == 2 && !isTerminal[i]) {
				DoubleValueEdge e1 = g[i].first();
				DoubleValueEdge e2 = g[i].last();
				int u = e1.dst;
				int v = e2.dst;
				double cost = e1.cost + e2.cost;
				g[u].remove(new DoubleValueEdge(u, i, e1.cost));
				g[v].remove(new DoubleValueEdge(v, i, e2.cost));
				g[i].clear();
				if (u == v && (!isTerminal[u] || !isTerminal[v])) continue;
				g[u].add(new DoubleValueEdge(u, v, cost));
				g[v].add(new DoubleValueEdge(v, u, cost));
			}
		}
		DoubleValueGraph ret = new DoubleValueGraph(N);
		for (int i = 0; i < N; ++i) {
			for (DoubleValueEdge e : g[i]) {
				ret.adj[i].add(e);
			}
		}
		return ret;
	}

	public int[] degrees() {
		int[] ret = new int[N];
		Arrays.setAll(ret, i -> adj[i].size());
		return ret;
	}

	public List<DoubleValueEdge> edges() {
		List<DoubleValueEdge> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (DoubleValueEdge e : adj[i]) {
				if (e.dst >= i) {
					list.add(e);
				}
			}
		}
		return list;
	}

	public double edgeCost() {
		double ret = 0;
		for (int i = 0; i < N; i++) {
			for (DoubleValueEdge e : adj[i]) {
				if (e.dst >= i) {
					ret += e.cost;
				}
			}
		}
		return ret;
	}

	public double[] dijkstra(int src) {
		record State(double dist, int v) implements Comparable<State> {
			@Override
			public int compareTo(State o) {
				return Double.compare(dist, o.dist);
			}
		}
		PriorityQueue<State> pq = new PriorityQueue<>();
		pq.add(new State(0, src));
		double[] dist = new double[N];
		Arrays.fill(dist, Double.MAX_VALUE);
		dist[src] = 0;
		while (!pq.isEmpty()) {
			State state = pq.poll();
			int v = state.v;
			double d = state.dist;
			if (d > dist[v]) continue;
			for (var e : adj[v]) {
				double nd = d + e.cost;
				if (dist[e.dst] > nd) {
					dist[e.dst] = nd;
					pq.add(new State(nd, e.dst));
				}
			}
		}
		return dist;
	}

	public int[] shortestPath(int src, int dst) {
		record State(double dist, int v) implements Comparable<State> {
			@Override
			public int compareTo(State o) {
				return Double.compare(dist, o.dist);
			}
		}
		PriorityQueue<State> pq = new PriorityQueue<>();
		pq.add(new State(0, src));
		double[] dist = new double[N];
		Arrays.fill(dist, Double.MAX_VALUE);
		dist[src] = 0;
		int[] prev = new int[N];
		Arrays.fill(prev, -1);
		while (!pq.isEmpty()) {
			State state = pq.poll();
			int v = state.v;
			double d = state.dist;
			if (d > dist[v]) continue;
			for (var e : adj[v]) {
				double nd = d + e.cost;
				if (dist[e.dst] > nd) {
					dist[e.dst] = nd;
					pq.add(new State(nd, e.dst));
					prev[e.dst] = v;
				}
			}
		}
		if (dist[dst] == Double.MAX_VALUE) return null;
		IntDeque dq = new IntDeque();
		for (int v = dst; v != -1; v = prev[v]) {
			dq.addFirst(v);
		}
		return dq.toArray();
	}

	public int[] restoreShortestPath(double[] d, int dst) {
		IntDeque dq = new IntDeque();
		int v = dst;
		dq.addLast(v);
		while (d[v] != 0) {
			boolean found = false;
			for (DoubleValueEdge e : adj[v]) {
				if (e.cost <= 0) throw new AssertionError();
				double nd = d[e.dst] + e.cost;
				double eps = 1e-9 * Math.max(1d, Math.max(Math.abs(nd), Math.abs(d[v])));
				if (Math.abs(nd - d[v]) <= eps) {
					v = e.dst;
					found = true;
					break;
				}
			}
			if (!found) throw new AssertionError();
			dq.addFirst(v);
		}
		return dq.toArray();
	}

	public void draw() {
		System.setProperty("org.graphstream.ui", "swing");

		org.graphstream.graph.Graph g = new org.graphstream.graph.implementations.SingleGraph("DoubleValueGraph");
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
				arrow-size: 10px, 6px;
				shape: line;
				text-offset: -10px, 0;
				text-alignment: along;
			}
		""");

		for (int i = 0; i < N; ++i) {
			org.graphstream.graph.Node node = g.addNode(String.valueOf(i));
			node.setAttribute("ui.label", String.valueOf(i));
		}
		for (int i = 0; i < N; ++i) {
			for (DoubleValueEdge e : adj[i]) {
				if (e.src >= e.dst) {
					String id = e.src + ":" + e.dst;
					if (g.getEdge(id) == null) {
						org.graphstream.graph.Edge edge = g.addEdge(id, String.valueOf(e.src), String.valueOf(e.dst), false);
						edge.setAttribute("ui.label", String.valueOf(e.cost));
					}
				}
			}
		}
		g.display();
	}

	public double[][] warshalFloyd() {
		double[][] d = new double[N][N];
		double INF = Double.MAX_VALUE / 3;
		ArrayUtils.fill(d, INF);
		for (int i = 0; i < N; i++) {
			d[i][i] = 0;
		}
		for (var e : edges()) {
			d[e.src][e.dst] = Math.min(d[e.src][e.dst], e.cost);
			d[e.dst][e.src] = Math.min(d[e.dst][e.src], e.cost);
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					d[j][k] = Math.min(d[j][k], d[j][i] + d[i][k]);
				}
			}
		}
		return d;
	}

	@SuppressWarnings("unchecked")
	public void addNode() {
		if (adj.length <= N) {
			int newLength = N;
			while (newLength <= N) newLength *= 2;
			ArrayList<DoubleValueEdge>[] newAdj = new ArrayList[newLength];
			for (int i = 0; i < newAdj.length; i++) {
				newAdj[i] = new ArrayList<>();
			}
			for (int i = 0; i < N; i++) {
				for (DoubleValueEdge e : adj[i]) {
					newAdj[i].add(new DoubleValueEdge(e.src, e.dst, e.cost));
				}
			}
			adj = newAdj;
		}
		N++;
	}

	public DoubleValueGraph clone() {
		DoubleValueGraph g = new DoubleValueGraph(N);
		g.M = M;
		for (int i = 0; i < N; i++) {
			for (DoubleValueEdge e : adj[i]) {
				g.adj[i].add(new DoubleValueEdge(e.src, e.dst, e.cost));
			}
		}
		return g;
	}

	public boolean isConnected() {
		boolean[] vis = new boolean[N];
		Queue<Integer> que = new ArrayDeque<>();
		que.add(0);
		vis[0] = true;
		while (!que.isEmpty()) {
			int v = que.poll();
			for (var e : adj[v]) {
				if (!vis[e.dst]) {
					vis[e.dst] = true;
					que.add(e.dst);
				}
			}
		}
		for (int i = 0; i < N; i++) {
			if (!vis[i]) return false;
		}
		return true;
	}

	public ArrayList<Result>[] distancesTopK(Iterable<Integer> source, int K) {
		class State implements Comparable<State> {
			double dist;
			int from;
			int cur;

			public State(double dist, int from, int cur) {
				this.dist = dist;
				this.from = from;
				this.cur = cur;
			}

			public int compareTo(State o) {
				return Double.compare(dist, o.dist);
			}
		}
		PriorityQueue<State> que = new PriorityQueue<>();
		@SuppressWarnings("unchecked")
		Map<Integer, Double>[] dp = new HashMap[N];
		for (int i = 0; i < N; i++) {
			dp[i] = new HashMap<Integer, Double>();
		}
		for (int i : source) {
			que.add(new State(0, i, i));
			dp[i].put(i, 0d);
		}
		while (!que.isEmpty()) {
			State state = que.poll();
			Double curDist = dp[state.cur].get(state.from);
			if (curDist == null || curDist != state.dist) continue;
			for (DoubleValueEdge e : adj[state.cur]) {
				double ndist = e.cost + state.dist;
				if (dp[e.dst].containsKey(state.from)) {
					if (dp[e.dst].get(state.from) > ndist) {
						dp[e.dst].put(state.from, ndist);
						que.add(new State(ndist, state.from, e.dst));
					} else {
						continue;
					}
				} else if (dp[e.dst].size() < K) {
					dp[e.dst].put(state.from, ndist);
					que.add(new State(ndist, state.from, e.dst));
				} else {
					int from = -1;
					double dist = -1;
					for (var es : dp[e.dst].entrySet()) {
						if (es.getValue() > dist) {
							from = es.getKey();
							dist = es.getValue();
						}
					}
					if (dist > ndist) {
						dp[e.dst].remove(from);
						dp[e.dst].put(state.from, ndist);
						que.add(new State(ndist, state.from, e.dst));
					}
				}
			}
		}
		@SuppressWarnings("unchecked")
		ArrayList<Result>[] ret = new ArrayList[N];
		for (int i = 0; i < N; i++) {
			ret[i] = new ArrayList<>();
		}
		for (int i = 0; i < N; i++) {
			for (var es : dp[i].entrySet()) {
				ret[i].add(new Result(es.getKey(), es.getValue()));
			}
			Collections.sort(ret[i], (x, y) -> Double.compare(x.distance(), y.distance()));
		}
		return ret;
	}

	public record Result(int source, double distance) {
	}
}
