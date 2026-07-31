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



public class LongValueGraph {

	public int N;
	public int M;
	public ArrayList<Edge>[] adj;
	
	public LongValueGraph(int N) {
		this.N = N;
		adj = new ArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
	}
	
	public static LongValueGraph readLongValueGraph(int N, int M) {
		LongValueGraph graph = new LongValueGraph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			long cost = sc.nextLong();
			graph.adj[a].add(new Edge(a, b, cost));
			graph.adj[b].add(new Edge(b, a, cost));
		}
		return graph;
	}
	
	public void addEdge(int a, int b, long cost) {
		++M;
		adj[a].add(new Edge(a, b, cost));
		adj[b].add(new Edge(b, a, cost));
	}
	
	
	/***
	 * ターミナルに指定された頂点間の任意の（最短とは限らない）pathの距離を保ったまま、辺数を小さくする。
	 * まず、次数１の頂点を削る。次に次数２の頂点を縮約。自己ループは取り除く。
	 * @param isTerminal
	 * @return
	 */
	public LongValueGraph homeomorphicReductionOf2core(boolean[] isTerminal) {
		TreeSet<Edge>[] g = new TreeSet[N];
		for (int i  = 0; i < N; ++i) g[i] = new TreeSet<>();
		for (int v = 0; v < N; ++v) for (Edge e : adj[v]) g[v].add(e); 
		for (int i = 0; i < N; ++i) {
			int v = i;
			while (g[v].size() == 1 && !isTerminal[v]) {
				Edge e = g[v].first();
				g[e.dst].remove(new Edge(e.dst, v, e.cost));
				g[v].clear();
				v = e.dst;
			}
		}
		for (int i = 0; i < N; ++i) {
			if (g[i].size() == 2  && !isTerminal[i]) {
				Edge e1 = g[i].first();
				Edge e2 = g[i].last();
				int u = e1.dst;
				int v = e2.dst;
				long cost = e1.cost + e2.cost;
				g[u].remove(new Edge(u, i, e1.cost));
				g[v].remove(new Edge(u, i, e2.cost));
				g[i].clear();
				if (u == v && (!isTerminal[u] || !isTerminal[v])) continue;
				g[u].add(new Edge(u, v, cost));
				g[v].add(new Edge(v, u, cost));
			}
		}
		LongValueGraph ret = new LongValueGraph(N);
		for (int i = 0; i < N; ++i) {
			for (Edge e : g[i]) {
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
	
	/**
	 * e.src <= e.dst の辺のみ列挙
	 * @return
	 */
	public List<Edge> edges() {
		List<Edge> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				if (e.dst  >= i) {
					list.add(e);
				}
			}
		}
		return list;
	}
	
	public long edgeCost() {
		long ret = 0;
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				if (e.dst >= i) {
					ret += e.cost;
				}
			}
		}
		return ret;
	}
	
	/**
	 * 到達不能な頂点に対する距離はLong.MAX_VALUEとする
	 * @param src
	 * @return
	 */
	public long[] dijkstra(int src) {
		PriorityQueue<long[]>pq=new PriorityQueue<>((x,y)->Arrays.compare(x, y));
		pq.add(new long[] {0, src});
		long[]dist=new long[N];
		ArrayUtils.fill(dist, Long.MAX_VALUE);
		dist[src] = 0;
		while(!pq.isEmpty()) {
			long[] state=pq.poll();
			int v=(int)(state[1]);
			long d=state[0];
			if(d>dist[v])continue;
			for (var e : adj[v]) {
				long nd=d+e.cost;
				if (dist[e.dst] > nd) {
					dist[e.dst] = nd;
					pq.add(new long[] {nd, e.dst});
				}
			}
		}
		return dist;
	}
	
	
	/**
	 * 未テスト
	 * @param src
	 * @param dst
	 * @return
	 */
	public int[] shortestPath(int src, int dst) {
		PriorityQueue<long[]>pq=new PriorityQueue<>((x,y)->Arrays.compare(x, y));
		pq.add(new long[] {0, src});
		long[]dist=new long[N];
		ArrayUtils.fill(dist, Long.MAX_VALUE);
		dist[src] = 0;
		int[] prev=new int[N];
		Arrays.fill(prev, -1);
		while(!pq.isEmpty()) {
			long[] state=pq.poll();
			int v=(int)(state[1]);
			long d=state[0];
			if(d>dist[v])continue;
			for (var e : adj[v]) {
				long nd=d+e.cost;
				if (dist[e.dst] > nd) {
					dist[e.dst] = nd;
					pq.add(new long[] {nd, e.dst});
					prev[e.dst] = v;
				}
			}
		}
		if(dist[dst]==Long.MAX_VALUE)return null;
		IntDeque dq=new IntDeque();
		for (int v=dst;v!=-1;v=prev[v]) {
			dq.addFirst(v);
		}
		return dq.toArray();
	}
	
	/**
	 * 未テスト
	 * 0以下の辺があるとエラー
	 * @param d
	 * @param dst
	 * @return
	 */
	public int[] restoreShortestPath(long[] d, int dst) {
		IntDeque dq=new IntDeque();
		int v=dst;
		dq.addLast(v);
		while(d[v]!=0) {
			System.out.println(v);
			for (Edge e:adj[v]) {
				if(e.cost<=0)throw new AssertionError();
				if(d[e.dst]+e.cost==d[v]) {
					v=e.dst;
					break;
				}
			}
			dq.addFirst(v);
		}
		return dq.toArray();
	}
	
	
	public void draw() {
		System.setProperty("org.graphstream.ui", "swing");

		org.graphstream.graph.Graph g = new org.graphstream.graph.implementations.SingleGraph("LongValueGraph");
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
			for (Edge e : adj[i]) {
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

	/**
	 * Long.MAX_VALUE/3をINFに使用
	 * @return
	 */
	public long[][] warshalFloyd() {
		long[][]d=new long[N][N];
		long INF=Long.MAX_VALUE/3;
		ArrayUtils.fill(d, INF);
		for (int i = 0; i < N; i++) {
			d[i][i]=0;
		}
		for (var e : edges()) {
			d[e.src][e.dst]=Math.min(d[e.src][e.dst], e.cost);
			d[e.dst][e.src]=Math.min(d[e.dst][e.src], e.cost);
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					d[j][k]=Math.min(d[j][k], d[j][i]+d[i][k]);
				}
			}
		}
		return d;
	}
	
	/**
	 * 頂点 $r$ を含む最小重みの単純サイクルのコストを求める。
	 * 存在しない場合は INF (Long.MAX_VALUE / 3) を返す。
	 * <ul>
	 *   <li>事前条件: $0 \le r < N$、辺重みは非負</li>
	 *   <li>時間計算量: $O(M \log N)$</li>
	 *   <li>空間計算量: $O(N)$</li>
	 * </ul>
	 * @param r 対象の頂点
	 * @return 最小重み単純サイクルのコスト
	 */
	public long findMinWeightCycleCostAt(int r) {
		// 未テスト
		long INF = Long.MAX_VALUE / 3;
		long[] dist = new long[N];
		int[] root = new int[N];
		boolean[] used = new boolean[N];
		Arrays.fill(dist, INF);
		Arrays.fill(root, -1);
		used[r] = true;

		PriorityQueue<long[]> pq = new PriorityQueue<>((x, y) -> Long.compare(x[0], y[0]));

		for (Edge e : adj[r]) {
			int v = e.dst;
			if (v == r) continue;
			if (e.cost < dist[v]) {
				dist[v] = e.cost;
				root[v] = v;
				pq.add(new long[]{dist[v], v});
			}
		}

		long best = INF;

		while (!pq.isEmpty()) {
			long[] state = pq.poll();
			long d = state[0];
			int u = (int) state[1];
			if (d > dist[u]) continue;
			used[u] = true;

			for (Edge e : adj[u]) {
				int v = e.dst;
				if (v == r) continue;

				if (dist[v] < INF && root[u] != root[v]) {
					long cand = dist[u] + e.cost + dist[v];
					if (cand < best) best = cand;
				}

				if (!used[v]) {
					long nd = dist[u] + e.cost;
					if (nd < dist[v]) {
						dist[v] = nd;
						root[v] = root[u];
						pq.add(new long[]{nd, v});
					}
				}
			}
		}

		return best;
	}

	
	
	/**
	 * 頂点を1つ追加する。
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 頂点数 $N$ が 1 増加し、新しく追加された頂点のインデックス $N-1$ を返す。</li>
	 *   <li>副作用: 隣接リストの配列 {@code adj} が必要に応じて拡張される。</li>
	 *   <li>計算量: ならし $O(1)$</li>
	 *   <li>破壊的変更: なし（既存の隣接リストは保持される）。</li>
	 * </ul>
	 * 未テスト
	 * @return 追加された頂点のインデックス
	 */
	public int addNode() {
		if (adj.length <= N) {
			int newLength = Math.max(adj.length * 2, 1);
			adj = Arrays.copyOf(adj, newLength);
			for (int i = N; i < newLength; i++) {
				adj[i] = new ArrayList<>();
			}
		}
		return N++;
	}

	/**
	 * グラフのコピーを生成する。
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 元のグラフと同じ頂点数・辺構造を持つ新しいグラフを返す。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N + M)$</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: 内部の隣接リストを含め、新しくインスタンスが生成される（深いコピー）。</li>
	 * </ul>
	 * 未テスト
	 * @return グラフのコピー
	 */
	public LongValueGraph copy() {
		LongValueGraph g = new LongValueGraph(N);
		g.M = M;
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				g.adj[i].add(new Edge(e.src, e.dst, e.cost));
			}
		}
		return g;
	}
	
	public boolean isConnected() {
		boolean[]vis=new boolean[N];
		Queue<Integer>que=new ArrayDeque<>();
		que.add(0);
		vis[0]=true;
		while(!que.isEmpty()) {
			int v=que.poll();
			for (var e : adj[v]) {
				if(!vis[e.dst]) {
					vis[e.dst] = true;
					que.add(e.dst);
				}
			}
		}
		for (int i = 0; i < N; i++) {
			if(!vis[i])return false;
		}
		return true;
	}
	
	
	
	
	/***
	 * 頂点 src ∈ source からの距離をminのうち上からK個(始点が異なるもの)を返す。
	 * 未テスト
	 */
	public ArrayList<Result>[] distancesTopK(Iterable<Integer> source, int K) {
        class State implements Comparable<State> {
            long dist;
            int from;
            int cur;

            public State(long dist, int from, int cur) {
                this.dist = dist;
                this.from = from;
                this.cur = cur;
            }
            
            public int compareTo(State o) {
            	return Long.compare(dist, o.dist);
            };
        }
        PriorityQueue<State> que = new PriorityQueue<>();
        Map<Integer, Long>[] dp = new HashMap[N];
        for (int i = 0; i < N; i++) {
			dp[i]=new HashMap<Integer, Long>();
		}
        for (int i : source) {
            que.add(new State(0, i, i));
            dp[i].put(i, 0L);
        }
        while (!que.isEmpty()) {
            State state = que.poll();
            Long curDist=dp[state.cur].get(state.from);
            if(curDist == null || curDist != state.dist) continue;
            for (Edge e : adj[state.cur]) {
            	long ndist=e.cost+state.dist;
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
                	int from=-1;
                	long dist=-1;
                	for (var es : dp[e.dst].entrySet()) {
                		if (es.getValue() > dist) {
                			from=es.getKey();
                			dist=es.getValue();
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
        ArrayList<Result>[] ret = new ArrayList[N];
        for (int i = 0; i < N; i++) {
			ret[i]=new ArrayList<>();
		}
        for (int i = 0; i < N; i++) {
			for (var es : dp[i].entrySet()) {
				ret[i].add(new Result(es.getKey(), es.getValue()));
			}
			Collections.sort(ret[i], (x, y)->Long.compare(x.distance(), y.distance()));
		}
		return ret;
	}
	
	public record Result (int source, long distance){
	}



}
