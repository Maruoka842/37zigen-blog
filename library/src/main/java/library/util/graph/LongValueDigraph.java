package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.Ints;
import library.util.collections.MyPriorityQueue;


public class LongValueDigraph {

	public int N;
	public int M;
	public ArrayList<Edge>[] adj;
	
	@SuppressWarnings("unchecked")
	public LongValueDigraph(int N) {
		this.N = N;
		adj = new ArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new ArrayList<>();
	}
	
	public static LongValueDigraph read(int N, int M) {
		LongValueDigraph graph = new LongValueDigraph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			long cost = sc.nextLong();
			graph.adj[a].add(new Edge(a, b, cost));
		}
		return graph;
	}
	
	public void addEdge(int from, int to, long cost) {
		if(from>=N || to>=N)throw new AssertionError();
		adj[from].add(new Edge(from, to, cost));
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
			for (Edge e : adj[i]) ret[e.dst]++;
		}
		return ret;
	}
	
	
	public List<Edge> edges() {
		List<Edge> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				list.add(e);
			}
		}
		return list;
	}
	
	public long edgeCost() {
		long ret = 0;
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				ret += e.cost;
			}
		}
		return ret;
	}
	
	
	public record DijkstraResult(long[] dist, int[] parent) {
		
	}
	
	/**
	 * 到達できない頂点 v への距離はLong.MAX_VALUE
	 * また最短路木の v の親は -1。負辺あるとエラー。
	 * @param src
	 * @return
	 */
	public DijkstraResult dijkstra(int src) {
		MyPriorityQueue<long[]> pq=new MyPriorityQueue<>();
		pq.add(new long[] {0,src});
		long[]dist=new long[N];
		int[] parent=new int[N];
		long INF=Long.MAX_VALUE;
		Arrays.fill(dist, INF);
		dist[src]=0;
		Arrays.fill(parent, -1);
		while (!pq.isEmpty()) {
			long[] state=pq.poll();
			int v=(int)state[1];
			if (dist[v]<state[0])continue;
			for(var e : adj[v]) {
				if (e.cost < 0) {
					throw new AssertionError("負の辺が存在");
				}
				long nd=dist[v]+e.cost;
				if(nd<dist[e.dst]) {
					dist[e.dst]=nd;
					parent[e.dst]=v;
					pq.add(new long[] {nd, e.dst});
				}
			}
		}
		return new DijkstraResult(dist, parent);
	}
	
	
	/***
	 * 頂点 src ∈ source からの距離を返す。負辺あるとエラー。
	 * 未テスト
	 * @param src
	 * @return
	 */
	public DijkstraResult dijkstra(Iterable<Integer> source) {
		MyPriorityQueue<long[]> pq=new MyPriorityQueue<>();
		long[]dist=new long[N];
		int[] parent=new int[N];
		long INF=Long.MAX_VALUE;
		Arrays.fill(dist, INF);
		Arrays.fill(parent, -1);
		for (int src : source) {
			pq.add(new long[] {0,src});
			dist[src]=0;
		}
		while (!pq.isEmpty()) {
			long[] state=pq.poll();
			int v=(int)state[1];
			if (dist[v]<state[0])continue;
			for(var e : adj[v]) {
				if(e.cost < 0) throw new AssertionError();
				long nd=dist[v]+e.cost;
				if(nd<dist[e.dst]) {
					dist[e.dst]=nd;
					parent[e.dst]=v;
					pq.add(new long[] {nd, e.dst});
				}
			}
		}
		return new DijkstraResult(dist, parent);

	}

	/*
	 * Shortest Path Faster Algorithm (SPFA):
	 * Bellman-Ford アルゴリズムの改善版であり、キューを用いて緩和（最短距離の更新）が発生した頂点のみを
	 * 次の探索対象とすることで、平均的な計算量を削減する。
	 *
	 * 1. 始点の距離を 0、それ以外を無限大に初期化する。始点をキューに追加する。
	 * 2. キューから頂点 u を取り出し、u に隣接する各頂点 v に対して緩和を試みる。
	 * 3. dist[v] > dist[u] + cost(u, v) ならば dist[v] を更新し、v がキューになければ追加する。
	 * 4. 緩和回数が頂点数 N に達した頂点があれば、負閉路が存在すると判断する。
	 */

	/**
	 * Shortest Path Faster Algorithm (SPFA)
	 * 負辺がある場合の単一始点最短路を求める。
	 * 負閉路を検出した場合は null を返す。
	 * @param src
	 * @return
	 */
	public DijkstraResult spfa(int src) {
		long[] dist = new long[N];
		int[] parent = new int[N];
		int[] count = new int[N];
		boolean[] inQueue = new boolean[N];
		long INF = Long.MAX_VALUE / 3;
		Arrays.fill(dist, INF);
		Arrays.fill(parent, -1);
		dist[src] = 0;

		Queue<Integer> queue = new ArrayDeque<>();
		queue.add(src);
		inQueue[src] = true;

		while (!queue.isEmpty()) {
			int u = queue.poll();
			inQueue[u] = false;

			for (Edge e : adj[u]) {
				if (dist[e.dst] > dist[u] + e.cost) {
					dist[e.dst] = dist[u] + e.cost;
					parent[e.dst] = u;
					if (!inQueue[e.dst]) {
						count[e.dst]++;
						if (count[e.dst] >= N) return null; // Negative cycle detected
						queue.add(e.dst);
						inQueue[e.dst] = true;
					}
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
	
	/**
	 * たどり着けない頂点ペアはLong.MAX_VALUE/3
	 * https://judge.u-aizu.ac.jp/onlinejudge/review.jsp?rid=11204257#2
	 * @return
	 */
	public long[][] warshalFloyd() {
		long[][]d=new long[N][N];
		long INF=Long.MAX_VALUE/3;
		ArrayUtils.fill(d, INF);
		for (int i = 0; i < N; i++) {
			d[i][i]=0;
		}
		for (int i = 0; i < N; i++) {
			for (var e : adj[i]) {
				d[i][e.dst]=Math.min(d[i][e.dst], e.cost);
			}
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					if(d[j][i]==INF||d[i][k]==INF)continue;
					d[j][k]=Math.min(d[j][k], d[j][i]+d[i][k]);
				}
			}
		}
		return d;
	}
	
	
	/**
	 * https://judge.u-aizu.ac.jp/onlinejudge/review.jsp?rid=11204257#2
	 * @return
	 */
	public boolean containsNegativeCycle() {
		long[]d=new long[N];
		long INF=Long.MAX_VALUE;
		for (int T = 0; T < 2*N; T++) {
			for (int i = 0; i < N; i++) {
				if(d[i]==INF)continue;
				for (var e:adj[i]) {
					if (d[i] == Long.MIN_VALUE) d[e.dst] = Long.MIN_VALUE;
					else if(d[e.dst] > d[i]+e.cost) {
						if (T >= N) {
							d[e.dst] = Long.MIN_VALUE;
						} else {
							d[e.dst] = d[i] + e.cost;
						}
					}
				}
			}
		}
		for (int i = 0; i < N; i++) {
			if(d[i] == Long.MIN_VALUE) return true;
		}
		return false;
	}
	
	/**
	 * shortest walkの長さを返す
	 * 到達不能ならLong.MAX_VALUE
	 * 負閉路で長さを-∞にできる場合はLong.MIN_VALUE
	 * @param src
	 * @return
	 * verified:https://judge.u-aizu.ac.jp/onlinejudge/description.jsp?id=GRL_1_B
	 */
	public long[] bellmanFord(int src) {
		long[]d=new long[N];
		long INF=Long.MAX_VALUE;
		Arrays.fill(d, Long.MAX_VALUE);
		d[src] = 0;
		//負閉路が存在する場合、T　=　N で d が更新される。その場合、-∞で更新する。その更新を全ての頂点に伝搬するために T ≤ 2N-1 まで取っている。
		for (int T = 0; T < 2*N; T++) {
			for (int i = 0; i < N; i++) {
				if(d[i]==INF)continue;
				for (var e:adj[i]) {
					if (d[i] == Long.MIN_VALUE) d[e.dst] = Long.MIN_VALUE;
					else if(d[e.dst] > d[i]+e.cost) {
						if (T >= N) {
							d[e.dst] = Long.MIN_VALUE;
						} else {
							d[e.dst] = d[i] + e.cost;
						}
					}
				}
			}
		}
		return d;
	}
	
	/**
	 * 未テスト
	 * @param source
	 * @return
	 */
	public long[] bellmanFord(Iterable<Integer> source) {
		long[]d=new long[N];
		long INF=Long.MAX_VALUE;
		Arrays.fill(d, Long.MAX_VALUE);
		for (int src : source) {
			d[src] = 0;
		}
		//負閉路が存在する場合、T　=　N で d が更新される。その場合、-∞で更新する。その更新を全ての頂点に伝搬するために T ≤ 2N-1 まで取っている。
		for (int T = 0; T < 2*N; T++) {
			for (int i = 0; i < N; i++) {
				if(d[i]==INF)continue;
				for (var e:adj[i]) {
					if (d[i] == Long.MIN_VALUE) d[e.dst] = Long.MIN_VALUE;
					else if(d[e.dst] > d[i]+e.cost) {
						if (T >= N) {
							d[e.dst] = Long.MIN_VALUE;
						} else {
							d[e.dst] = d[i] + e.cost;
						}
					}
				}
			}
		}
		return d;
	
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
			for (Edge e : adj[i]) {
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
	
	public LongValueDigraph reverse() {
		LongValueDigraph g=new LongValueDigraph(N);
		for (var e:edges()) {
			g.addEdge(e.dst, e.src, e.cost);
		}
		return g;
	}
	
	
	
	/***
	 * 頂点 src ∈ source からの距離をminのうち上からK個(始点が異なるもの)を返す。
	 */
	public ArrayList<Result>[] distancesTopK(Iterable<Integer> source, int K) {
		//https://atcoder.jp/contests/abc245/submissions/72056511
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
	public LongValueDigraph copy() {
		LongValueDigraph g = new LongValueDigraph(N);
		g.M = M;
		for (int i = 0; i < N; i++) {
			for (Edge e : adj[i]) {
				g.adj[i].add(new Edge(e.src, e.dst, e.cost));
			}
		}
		return g;
	}

	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}
