package library.util.graph;

import java.awt.Component;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.Ints;
import library.util.collections.ArrayLists;
import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;
import library.util.unionfind.UnionFind;

/**
 * 無向グラフ
 */
public class Graph {
	public int N;
	public int M;
	public IntArrayList[] adj;
	
	@SuppressWarnings("unchecked")
	public Graph(int N) {
		this.N = N;
		adj = new IntArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new IntArrayList();
	}
	
	public Graph(boolean[][] adjcentMatrix) {
		if (adjcentMatrix.length != adjcentMatrix[0].length)throw new AssertionError();
		this.N = adjcentMatrix.length;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (adjcentMatrix[i][j]!=adjcentMatrix[j][i])throw new AssertionError();
			}
		}
		adj = new IntArrayList[N];
		for (int i = 0; i < N; i++) {
			adj[i] = new IntArrayList();
		}
		for (int i = 0; i < N; i++) {
			for (int j = i; j < N; j++) {
				if (adjcentMatrix[i][j])addEdge(i, j);
			}
		}
	}
	
	public static Graph read(int N, int M) {
		Graph graph = new Graph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			graph.adj[a].add(b);
			graph.adj[b].add(a);
		}
		return graph;
	}
	
	public static Graph read0indexed(int N, int M) {
		Graph graph = new Graph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt();
			int b = sc.nextInt();
			graph.addEdge(a, b);
		}
		return graph;
	}
	
	
	/**
	 * 無向辺を追加
	 * @param u
	 * @param v
	 */
	public void addEdge(int u, int v) {
		adj[u].add(v);
		++M;
		if (v != u) {
			adj[v].add(u);
		}
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
				adj[i] = new IntArrayList();
			}
		}
		return N++;
	}

	/**
	 * 未テスト
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code 0 <= u < N}, {@code 0 <= v < N}, かつ辺 {@code {u, v}} が1本以上存在する。</li>
	 * <li>事後条件: {@code M' = M - 1}。{@code u != v} なら {@code adj[u]} から {@code v} を1個、{@code adj[v]} から {@code u} を1個削除する。{@code u == v} なら {@code adj[u]} から {@code u} を1個削除する。</li>
	 * <li>副作用: このグラフの隣接リストと辺数を破壊的に変更する。</li>
	 * <li>破壊的変更: 削除した要素の位置に各隣接リストの末尾要素を移動できる。隣接リストの順序は保存されない。</li>
	 * <li>参照共有・所有権: 新たな配列・隣接リストを生成せず、既存の {@code adj} と各 {@code IntArrayList} を保持する。</li>
	 * <li>例外・未定義条件: 事前条件違反時の挙動は未定義。ただし辺が存在しない場合、または {@code u != v} で片側の隣接リストにしか存在しない場合は {@link AssertionError} を送出する。</li>
	 * <li>計算量: 時間計算量 {@code O(deg(u) + deg(v))}、追加空間計算量 {@code O(1)}。</li>
	 * </ul>
	 * @param u 端点u
	 * @param v 端点v
	 */
	public void removeEdge(int u, int v) {
		int iu = -1;
		for (int i = 0; i < adj[u].size(); ++i) {
			if (adj[u].get(i) == v) {
				iu = i;
				break;
			}
		}
		if (iu == -1) throw new AssertionError();
		if (u == v) {
			adj[u].set(iu, adj[u].peekLast());
			adj[u].pollLast();
			--M;
			return;
		}
		int iv = -1;
		for (int i = 0; i < adj[v].size(); ++i) {
			if (adj[v].get(i) == u) {
				iv = i;
				break;
			}
		}
		if (iv == -1) throw new AssertionError();
		adj[u].set(iu, adj[u].peekLast());
		adj[u].pollLast();
		adj[v].set(iv, adj[v].peekLast());
		adj[v].pollLast();
		--M;
	}

	/**
	 * 未テスト
	 * @param dist
	 * @param dst
	 * @return
	 */
	public int[] restoreShortestPath(int[]dist, int dst) {
		int[]ret=new int[dist[dst]+1];
		int v=dst;
		int last=ret.length-1;
		ret[last--]=v;
		while(last!=-1) {
			for (int u:adj[v]) {
				if(dist[u]<dist[v]) {
					v=u;
					break;
				}
			}
			ret[last--]=v;
		}
		return ret;
	}
	
	/***
	 * 頂点 src からの距離を配列で返す。
	 * 到達不能な頂点に対する距離はInteger.MAX_VALUE/3。
	 * @param src
	 * @return
	 */
	public int[] bfsDistances(int src) {
		int[] dist=new int[N];
		int INF=Integer.MAX_VALUE/3;
		Arrays.fill(dist, INF);
		dist[src]=0;
		Queue<Integer>que=new ArrayDeque<>();
		que.add(src);
		while(!que.isEmpty()) {
			int v=que.poll();
			for(int u:adj[v]) {
				if(dist[u]==INF) {
					dist[u]=dist[v]+1;
					que.add(u);
				}
			}
		}
		return dist;
	}
	
	
	/***
	 * 頂点 src からの距離を配列で返す。
	 * 到達不能な頂点に対する距離はInteger.MAX_VALUE/3。
	 * @param src
	 * @return
	 */
	public int[] bfsDistancesWithDeletedVertices(int src, boolean[] isDeleted) {
		//https://atcoder.jp/contests/abc417/submissions/73785745
		if (isDeleted[src]) throw new AssertionError();
		int[] dist=new int[N];
		int INF=Integer.MAX_VALUE/3;
		Arrays.fill(dist, INF);
		dist[src]=0;
		Queue<Integer>que=new ArrayDeque<>();
		que.add(src);
		while(!que.isEmpty()) {
			int v=que.poll();
			for(int u:adj[v]) {
				if (isDeleted[u]) continue;
				if(dist[u]==INF) {
					dist[u]=dist[v]+1;
					que.add(u);
				}
			}
		}
		return dist;
	}
	
	
	/***
	 * 頂点 src ∈ source からの距離のminを配列で返す。
	 * 到達不能な頂点に対する距離はN+1とする。
	 * @param src
	 * @return
	 */
	public int[] bfsDistances(Iterable<Integer> source) {
		int[] dist=new int[N];
		Arrays.fill(dist, N+1);
		Queue<Integer>que=new ArrayDeque<>();
		for (int src : source) {
			dist[src]=0;
			que.add(src);
		}
		while(!que.isEmpty()) {
			int v=que.poll();
			for(int u:adj[v]) {
				if(dist[u]==N+1) {
					dist[u]=dist[v]+1;
					que.add(u);
				}
			}
		}
		return dist;
	}

	
	/***
	 * 頂点 src ∈ source からの距離をminのうち上からK個(始点が異なるもの)を返す。
	 * 到達不能な頂点に対する距離はN+1とする。
	 */
	public ArrayList<Result>[] bfsdistancesTopK(Iterable<Integer> source, int K) {
		int[] dist=new int[N];
		Arrays.fill(dist, N+1);
        class State {
            int dist;
            int from;
            int cur;

            public State(int dist, int from, int cur) {
                this.dist = dist;
                this.from = from;
                this.cur = cur;
            }
        }
        Queue<State> que = new ArrayDeque<>();
        Set<Integer>[] vis = new HashSet[N];
        for (int i = 0; i < N; i++) {
            vis[i] = new HashSet<>();
        }
        ArrayList<Result>[] ret = new ArrayList[N];
        for (int i = 0; i < N; i++) {
			ret[i]=new ArrayList<>();
		}
        for (int i : source) {
            que.add(new State(0, i, i));
            vis[i].add(i);
            ret[i].add(new Result(i, 0));
        }
        for (int i = 0; i < N; i++) {
			ret[i] = new ArrayList<>();
		}
        while (!que.isEmpty()) {
            State state = que.poll();
            for (int v : adj[state.cur]) {
                if (vis[v].size() == K) {
                    continue;
                }
                if (vis[v].contains(state.from)) {
                    continue;
                }
                vis[v].add(state.from);
                que.add(new State(state.dist + 1, state.from, v));
                ret[v].add(new Result(state.from, state.dist+1));
            }
        } 
		return ret;
	}
	
	public record Result (int source, long distance){
	}
	
	
	
	
	public int[] degrees() {
		int[] ret = new int[N];
		Arrays.setAll(ret, i -> adj[i].size());
		return ret;
	}
	
	public int deg(int i) {
		return adj[i].size();
	}

	/**
	 * 無向グラフのオイラー路を頂点列で返す。存在しないときはnull。
	 * 未テスト
	 * O(N+M)
	 * @return
	 */
	public int[] eulerTrail() {
		//https://judge.yosupo.jp/submission/310081
		if (N == 0) return new int[0];
		int start = 0;
		for (int i = 0; i < N; ++i) {
			int deg = eulerDegree(i);
			if ((deg & 1) == 1) return eulerTrail(i);
			if (deg > 0) start = i;
		}
		return eulerTrail(start);
	}

	/**
	 * 頂点startから始まる無向グラフのオイラー路を頂点列で返す。存在しないときはnull。
	 * 未テスト
	 * O(N+M)
	 * @param start
	 * @return
	 */
	public int[] eulerTrail(int start) {
		//https://judge.yosupo.jp/submission/370979
		if (start < 0 || start >= N) throw new AssertionError();
		Map<Long, Integer> edgeCount = new HashMap<>();
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				if (i > j) continue;
				long key = Ints.packUnorderedPair(i, j);
				edgeCount.put(key, edgeCount.getOrDefault(key, 0) + 1);
			}
		}
		if (M == 0) return new int[] {start};
		int odd = 0;
		for (int i = 0; i < N; ++i) {
			if ((eulerDegree(i) & 1) == 1) odd++;
		}
		if (odd != 0 && odd != 2) return null;
		if (odd == 2 && (eulerDegree(start) & 1) == 0) return null;
		if (eulerDegree(start) == 0) return null;

		int[] it = new int[N];
		IntArrayList stack = new IntArrayList(M + 1);
		IntArrayList path = new IntArrayList(M + 1);
		stack.add(start);
		while (stack.isNonEmpty()) {
			int v = stack.peekLast();
			while (it[v] < adj[v].size() && edgeCount.getOrDefault(Ints.packUnorderedPair(v, adj[v].get(it[v])), 0) == 0) it[v]++;
			if (it[v] == adj[v].size()) {
				path.add(stack.pollLast());
			} else {
				int u = adj[v].get(it[v]++);
				long key = Ints.packUnorderedPair(v, u);
				edgeCount.put(key, edgeCount.get(key) - 1);
				stack.add(u);
			}
		}
		if (path.size() != M + 1) return null;
		path.reverse();
		return path.toArray();
	}

	private int eulerDegree(int v) {
		int deg = adj[v].size();
		for (int u : adj[v]) if (u == v) deg++;
		return deg;
	}
	
	/**
	 * ランダムな単純グラフを返す
	 * @param N
	 * @param M
	 * @return
	 */
	public static Graph randomGraph(int N, int M) {
		if (M > (long) N * (N - 1) / 2) throw new AssertionError();
		Graph ret = new Graph(N);
		Random rnd = new Random();
		Set<Long> set = new HashSet<>();
		for (int i = 0; i < M; ++i) {
			while (true) {
				int a = rnd.nextInt(N);
				int b = rnd.nextInt(N);
				if(a==b)continue;
				long hash = Ints.packUnorderedPair(a, b);
				if (set.contains(hash)) continue;
				set.add(hash);
				ret.addEdge(a, b);
				break;
			}
		}
		return ret;
	}

	public static Graph randomConnectedGraph(int N) {
		int M=new Random().nextInt(N-1, 1+N*(N-1)/2);
		return randomConnectedGraph(N, M);
	}
	
	/**
	 * ランダムな連結単純グラフを返す。
	 * 最初にランダムな全域木を構築し、その後 $M$ 辺になるまでランダムに辺を追加する。
	 * <ul>
	 *   <li>事前条件: $N-1 \le M \le N(N-1)/2$。ただし $N=0$ のときは $M=0$。</li>
	 *   <li>事後条件: 頂点数 $N$、辺数 $M$ の連結な単純グラフを返す。</li>
	 *   <li>計算量: $O(M \log N)$</li>
	 * </ul>
	 * @param N 頂点数
	 * @param M 辺数
	 * @return ランダムな連結単純グラフ
	 */
	public static Graph randomConnectedGraph(int N, int M) {
		if (N == 0) {
			if (M == 0) return new Graph(0);
			else throw new AssertionError();
		}
		if (M < N - 1 || M > (long) N * (N - 1) / 2) throw new AssertionError();
		Graph ret = new Graph(N);
		Random rnd = new Random();
		Set<Long> set = new HashSet<>();
		if (N > 1) {
			int[] p = ArrayUtils.randomPermutation(N);
			for (int i = 1; i < N; i++) {
				int u = p[i];
				int v = p[rnd.nextInt(i)];
				ret.addEdge(u, v);
				set.add(Ints.packUnorderedPair(u, v));
			}
		}
		while (ret.M < M) {
			int a = rnd.nextInt(N);
			int b = rnd.nextInt(N);
			if (a == b) continue;
			long hash = Ints.packUnorderedPair(a, b);
			if (set.contains(hash)) continue;
			set.add(hash);
			ret.addEdge(a, b);
		}
		return ret;
	}
	
	
	
	/**
	 * ランダムな単純グラフを返す
	 * @param N
	 * @param M
	 * @return
	 */
	public static Graph randomGraph(int N) {
		Random rnd=new Random();
		return randomGraph(N, rnd.nextInt(N*(N-1)/2+1));
	}

	/**
	 * グラフのコピーを生成する。
	 * <p>
	 * 戻り値のグラフは元のグラフと構造を共有しない深いコピーである。
	 * </p>
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 元のグラフと同じ頂点数・辺構造を持つ新しいグラフを返す。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N + M)$</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: 内部の隣接リストを含め、新しくインスタンスが生成される。</li>
	 *   <li>例外: なし。</li>
	 * </ul>
	 * 未テスト
	 * @return グラフのコピー
	 */
	public Graph copy() {
		Graph ret = new Graph(N);
		ret.M = M;
		for (int i = 0; i < N; i++) {
			ret.adj[i].addAll(this.adj[i]);
		}
		return ret;
	}
	
	/***
	 * 辺重み1 とした ValueGraph を返す。
	 * @return
	 */
	public LongValueGraph toLongValueGraph() {
		LongValueGraph ret = new LongValueGraph(N);
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				ret.adj[i].add(new Edge(i, j, 1));
			}
		}
		return ret;
	}

	public DoubleValueGraph toDoubleValueGraph() {
		DoubleValueGraph ret = new DoubleValueGraph(N);
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				ret.adj[i].add(new DoubleValueEdge(i, j, 1));
			}
		}
		return ret;
	}
	
	
	
	/***
	 * 未検証
	 * 次数k以上の頂点からなる最大の部分グラフを返す。
	 * ただし、頂点の削除はせず、辺の削除だけ行う。本来削除される頂点は次数0の頂点として残る。
	 * ループがある場合は知らん。
	 */
	public Graph kCore(int k) {
		@SuppressWarnings("unchecked")
		TreeSet<Integer>[] g = new TreeSet[N];
		for (int i  = 0; i < N; ++i) g[i] = new TreeSet<>();
		for (int v = 0; v < N; ++v) for (int u : adj[v]) g[v].add(u); 
		Queue<Integer>que=new ArrayDeque<>();
		boolean[] enqued=new boolean[N];
		for (int i = 0; i < N; ++i) {
			if (g[i].size() < k) {
				que.add(i);
				enqued[i]=true;
			}
		}
		while (!que.isEmpty()) {
			int v=que.poll();
			for (int u : g[v]) {
				g[v].remove(u);
				g[u].remove(v);
				if(!enqued[u]&&g[u].size()<k) {
					que.add(u);
					enqued[u]=true;
				}
			}
		}
		Graph ret=new Graph(N);
		for (int i = 0; i < N; i++) {
			for (int j : g[i]) {
				if (i < j) {
					ret.addEdge(i, j);
				}
			}
		}
		return ret;
	}
	
	
	/***
	 * 未検証
	 * terminal[v]=Trueの頂点は残して、次数k以上の頂点からなる最大の部分グラフを返す。
	 * ただし、頂点の削除はせず、辺の削除だけ行う。本来削除される頂点は次数0の頂点として残る。
	 * ループがある場合は知らん。
	 */
	public Graph kCore(int k, boolean[] isTerminal) {
		@SuppressWarnings("unchecked")
		TreeSet<Integer>[] g = new TreeSet[N];
		for (int i  = 0; i < N; ++i) g[i] = new TreeSet<>();
		for (int v = 0; v < N; ++v) for (int u : adj[v]) g[v].add(u); 
		Queue<Integer>que=new ArrayDeque<>();
		boolean[] enqued=new boolean[N];
		for (int i = 0; i < N; ++i) {
			if (g[i].size() < k && !isTerminal[i]) {
				que.add(i);
				enqued[i]=true;
			}
		}
		while (!que.isEmpty()) {
			int v=que.poll();
			for (int u : g[v]) {
				g[v].remove(u);
				g[u].remove(v);
				if(!enqued[u]&&g[u].size()<k&&!isTerminal[u]) {
					que.add(u);
					enqued[u]=true;
				}
			}
		}
		Graph ret=new Graph(N);
		for (int i = 0; i < N; i++) {
			for (int j : g[i]) {
				if (i < j) {
					ret.addEdge(i, j);
				}
			}
		}
		return ret;
	}

	
	
	private static int drawCount = 0; // 呼び出しカウンタ

	public void draw() {
        System.setProperty("org.graphstream.ui", "swing");
        String graphName = "MyGraph-" + (++drawCount);
		org.graphstream.graph.Graph graph = new MultiGraph(graphName);

		for (int i = 0; i < N; ++i) {
			graph.addNode(String.valueOf(i));
		}
		int edgeCnt=0;
		Map<String, Integer> cnt = new HashMap<>();
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				if (i <= j) {
					String a=String.valueOf(i);
					String b=String.valueOf(j);
		            String key = a + "," + b;
		            int k = cnt.getOrDefault(key, 0);
		            var e=graph.addEdge(""+edgeCnt++, a, b, false);
				}
			}
		}
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }
        graph.setAttribute("ui.stylesheet",
		    "node { " +
		    "   fill-color: lightblue; " +
		    "   size: 30px; " +
		    "   text-alignment: center; " +
		    "   text-size: 20; " +
		    "   text-color: black; " +
		    "}"
		);
        Viewer viewer = graph.display();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((Component) viewer.getDefaultView());
            if (frame != null) {
                frame.setTitle(graphName);
            }
        });
	}
	
	
	
	/**
	 * a < b <=> a から b への path が存在
	 * という条件の元で頂点の全順序 v[0] <= v[1] <= .. <= v[n-1] を定め、vを返す。 
	 * DAGでない場合エラー。
	 * @return
	 */
	public int[] topologicalOrder() {
		int[] outDegrees = degrees();
		Queue<Integer> que = new ArrayDeque<>();
		for (int i  = 0; i < N; ++i) {
			if (outDegrees[i] == 0) que.add(i);
		}
		boolean[] vis = new boolean[N];
		int[] order = new int[N];
		int p = 0;
		while (!que.isEmpty()) {
			int v = que.poll();
			order[p++] = v;
			vis[v] = true;
			for (int u : adj[v]) {
				if (!vis[u]) {
					--outDegrees[u];
					if (outDegrees[u] == 0) {
						que.add(u);
					}
				}
			}
		}
		if (p != N) throw new AssertionError("DAGではない");
		return order;
	}
	
	/***
	 * srcから到達可能な頂点をBFS順に並べたArrayListを返す。
	 * HashSetを使っているので多分遅い。
	 * @param src
	 * @return
	 */
	public ArrayList<Integer> bfsOrder(int src) {
		Queue<Integer> que = new ArrayDeque<>();
		que.add(src);
		HashSet<Integer> vis = new HashSet<>();
		vis.add(src);
		ArrayList<Integer> ret = new ArrayList<>();
		ret.add(src);
		while (!que.isEmpty()) {
			int v = que.poll();
			for (int u : adj[v]) {
				if (!vis.contains(u)) {
					vis.add(u);
					que.add(u);
					ret.add(u);
				}
			}
		}
		return ret;
	}
	
	/***
	 * 各強連結成分をArrayListとした配列を返す。連結成分は頂点のminの昇順に並ぶ。各連結成分内の頂点はminの頂点からのBFS順で並ぶ。
	 * @return
	 */
	public ArrayList<Integer>[] components() {
		int[]color=new int[N];
		Arrays.fill(color, -1);;
		Queue<Integer>que=new ArrayDeque<>();
		int size = 0;
		for (int i = 0; i < N; i++) {
			if (color[i]!=-1)continue;
			que.add(i);
			color[i]=size;
			while(!que.isEmpty()) {
				int v=que.poll();
				for(int u:adj[v]) {
					if(color[u]!=-1)continue;
					color[u]=size;
					que.add(u);
				}
			}
			size++;
		}
		ArrayList<Integer>[]ret=ArrayLists.newArrayOfIntArrayLists(size);
		for (int i = 0; i < N; i++) {
			ret[color[i]].add(i);
		}
		return ret;
	}
	
	public boolean isConnected() {
		UnionFind uf = new UnionFind(N);
		for (int i = 0; i < N; i++) {
			for (var j : adj[i]) {
				uf.union(i, j);
			}
		}
		return uf.size(0) == N;
	}
	
	/**
	 * 辺ij (i<=j)を列挙する。
	 * @return
	 */
	public List<int[]> edges() {
		List<int[]> list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (int j : adj[i]) {
				if (j  >= i) {
					list.add(new int[] {i, j});
				}
			}
		}
		return list;
	}

	/**
	 * G=K_1のときは、関節点ではないとする。
	 * @return
	 * verified:https://judge.u-aizu.ac.jp/onlinejudge/review.jsp?rid=10985481#1(連結グラフ)
	 */
	public ArrayList<Integer> articulations() {
		int[] min=new int[N];
		int[] depth=new int[N];
		boolean[]isArticulation=new boolean[N];
		ArrayList<Integer> ret=new ArrayList<>();
		boolean[]vis=new boolean[N];
		for (int i = 0; i < N; i++) {
			if(!vis[i]) {
				_dfsArticulations(0, -1, min, vis,depth,isArticulation,i);
			}
		}
		for (int i = 0; i < N; i++) {
			if(isArticulation[i]) ret.add(i);
		}
		return ret;
	}
	
	
	
	
	/**
	 * グラフは連結と仮定している。G-eが非連結となるようなe=uv (u<v) を列挙する
	 * @return
	 */
	public ArrayList<int[]> bridges() {
		int[] min=new int[N];
		int[] depth=new int[N];
		boolean[]vis=new boolean[N];
		ArrayList<int[]> ret=new ArrayList<>();
		for (int i = 0; i < N; i++) {
			if(!vis[i]) {
				_dfsBridges(i, -1, min, vis, depth, ret);
			}
		}
		return ret;
	}

	
	
	/**
	 * min[v]:=dfs木で, 辺(v,parent[v])を切った時、どの深さの頂点まで戻れるか。
	 */
	int _dfsArticulations(int v, int p, int[] min, boolean[] vis, int[]depth, boolean[] isArticulation, int root) {
		if(p!=-1)depth[v]=depth[p]+1;
		min[v]=depth[v];
		vis[v]=true;
		int allSubTreeMin = Integer.MAX_VALUE;
		int deg = 0;
		for (int u : adj[v]) {
			if (u == p) continue;
			if (vis[u]) {//後退辺 or その逆辺
				min[v]=Math.min(min[v], min[u]);
			} else {
				deg++;
				int subTreeMin=_dfsArticulations(u, v, min, vis, depth, isArticulation, root);
				allSubTreeMin=Math.min(allSubTreeMin, subTreeMin);
				if (v != root) {
					isArticulation[v] |= subTreeMin >= depth[v];
				}
			}
		}
		if (v == root) {
			isArticulation[v] = deg >= 2;
		}
		min[v] = Math.min(min[v], allSubTreeMin);
		return min[v];
	}

	
	
	/**
	 * min[v]:=dfs木で, 辺(v,parent[v])を切った時、どの深さの頂点まで戻れるか。
	 */
	int _dfsBridges(int v, int p, int[] min, boolean[] vis, int[] depth, ArrayList<int[]> bridges) {
		if(p!=-1)depth[v]=depth[p]+1;
		min[v]=depth[v];
		vis[v]=true;
		int allSubTreeMin = Integer.MAX_VALUE;
		for (int u : adj[v]) {
			if (u == p) continue;
			if (vis[u]) {//後退辺 or その逆辺
				min[v]=Math.min(min[v], min[u]);
			} else {
				int subTreeMin=_dfsBridges(u, v, min, vis, depth, bridges);
				allSubTreeMin=Math.min(allSubTreeMin, subTreeMin);
			}
		}
		min[v] = Math.min(min[v], allSubTreeMin);
		if (min[v]==depth[v] && p != -1) bridges.add(new int[] {Math.min(v, p), Math.max(v, p)}); 
		return min[v];
	}

	
	
	public int nodeConnectivity(Set<Integer> s, Set<Integer> t) {
		return nodeConnectivity(s, t, Integer.MAX_VALUE);
	}
	
	/**
	 * s, tを非連結にするために削除する必要がある頂点数とcutoffの小さい方を返す。
	 * Mengerの定理よりs-t disjoint path(端点は共有可)の本数と等しい。
	 * 元のグラフに自己ループがないと仮定している。
	 * @param s
	 * @param t
	 * @param cutoff
	 * @return
	 */
	public int nodeConnectivity(Set<Integer> s, Set<Integer> t, int cutoff) {
		MaxFlow mf=new MaxFlow(2*N+2);
		int source=2*N;
		int sink=2*N+1;
		for (int v : s) {
			mf.addEdge(source, 2*v, M);
		}
		for (int v : t) {
			mf.addEdge(2*v+1, sink, M);
		}
		for (int i = 0; i < N; ++i) {
			mf.addEdge(2*i, 2*i+1, s.contains(i)||t.contains(i)?M:1);
		}
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				if (!(s.contains(i)&&s.contains(j))&&!(t.contains(i)&&t.contains(j))) {//この処理はなくても正しい答えを返す
					mf.addEdge(2*i+1, 2*j, 1);
				}
			}
		}
		return (int)mf.maxFlowValue(source, sink, cutoff);
	}
	
	public record WeightedNodeConnectivityResult(long cost, int[] vertexCut) {};
	/**
	 * s,tを切断する最小重み頂点カットを求める。s,tをcut vertexには選べないので、cost[s],cost[t]は無視してinfとして扱う。
	 * @param s
	 * @param t
	 * @param cost
	 * @return
	 */
	public WeightedNodeConnectivityResult weightedNodeConnectivity(int s, int t, long[] cost) {
		//https://atcoder.jp/contests/abc239/submissions/71991711
		MaxFlow mf=new MaxFlow(2*N+2);
		int source=2*N;
		int sink=2*N+1;
		long INF=Long.MAX_VALUE/3;
		mf.addEdge(source, 2*s, INF);
		mf.addEdge(2*t+1, sink, INF);
		for (int i = 0; i < N; ++i) {
			mf.addEdge(2*i, 2*i+1, (i==s||i==t)?INF:cost[i]);
		}
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				mf.addEdge(2*i+1, 2*j, INF);
			}
		}
		long flowValue=mf.maxFlowValue(source, sink);
		boolean[] vis=mf.reachableFromSourceOnResidualNetwork();
		int[]cut=new int[N];
		int size=0;
		for (int i = 0; i < N; i++) {
			if(i==s||i==t)continue;
			if (vis[2*i]&&!vis[2*i+1]) {
				cut[size++]=i;
			}
		}
		cut=Arrays.copyOf(cut, size);
		return new WeightedNodeConnectivityResult(flowValue, cut);
	}

	
	/**
	 * verified:https://atcoder.jp/contests/abc327/submissions/71206148
	 * @return
	 */
	public boolean isBipartite() {
		int[] col=new int[N];
		Arrays.fill(col, -1);
		for (int i = 0; i < N; i++) {
			if(col[i]==-1) {
				col[i]=0;
				Queue<Integer>que=new ArrayDeque<>();
				que.add(i);
				while(!que.isEmpty()) {
					int v=que.poll();
					for (int u:adj[v]) {
						if(col[u]!=-1) {
							if(col[u]!=(col[v]^1))return false;
						} else {
							col[u]=col[v]^1;
							que.add(u);
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 自己ループがあるとエラー
	 * O(N+M)
	 * @return
	 */
	public int[] greedyColoring() {
		//https://atcoder.jp/contests/abc451/submissions/74498525 (2部グラフ)
		int[] col=new int[N];
		Arrays.fill(col, -1);
		Queue<Integer>que=new ArrayDeque<>();
		for (int i = 0; i < N; i++) {
			if(col[i]==-1) {
				que.add(i);
				while(!que.isEmpty()) {
					int v=que.poll();
					if(col[v]!=-1)continue;
					boolean[] isAdjacentColor=new boolean[adj[v].size()];
					for (int u:adj[v]) {
						if(u==v)throw new AssertionError("自己ループはダメ");
						if(col[u]==-1) {
							que.add(u);							
						} else if (col[u] < isAdjacentColor.length) {
							isAdjacentColor[col[u]]=true;
						}
					}
					col[v]=0;
					while(col[v] < isAdjacentColor.length && isAdjacentColor[col[v]])++col[v];
				}
			}
		}
		return col;
	}

	
	public int numberOfConnectedComponents() {
		boolean[] vis=new boolean[N];
		int ans = 0;
		for (int i = 0; i < N; i++) {
			if (!vis[i]) {
				++ans;
				IntArrayList stack=new IntArrayList();
				stack.add(i);
				vis[i]=true;
				while (stack.isNonEmpty()) {
					int v=stack.pollLast();
					for (int u : adj[v]) {
						if (!vis[u]) {
							vis[u]=true;
							stack.add(u);
						}
					}
				}
			}
		}
		return ans;
	}
	
	public int[] degreesOnBlockCutTree() {
		boolean[] vis=new boolean[N];
		int[] deg=new int[N];
		int[] depth=new int[N];
		int[] min=new int[N];
		int[] parent = new int[N];
		int[] it = new int[N]; // 次に見る隣接辺
		for (int root = 0; root < N; root++) {
			if(vis[root])continue;
			IntArrayList stack = new IntArrayList();
	
			stack.add(root);
			parent[root] = -1;
			depth[root] = 0;
			while (stack.isNonEmpty()) {
			    int v = stack.get(stack.size()-1);
			    
			    if (it[v] == 0) {
			        vis[v] = true;
			        min[v] = depth[v];
			    }
	
			    if (it[v] < adj[v].size()) {
			        int u = adj[v].get(it[v]++);
			        if (u == parent[v]) continue;
			        if (vis[u]) {
			            min[v] = Math.min(min[v], depth[u]);
			        } else {
			            parent[u] = v;
			            depth[u] = depth[v] + 1;
			            stack.add(u);
			        }
			    } else {
			        stack.pollLast();
			        if (parent[v] != -1) {
			            min[parent[v]] = Math.min(min[parent[v]], min[v]);
			            if (parent[v] != root && min[v] >= depth[parent[v]]) deg[parent[v]]++;//root≠parent[v]の下でparent[v]がvに対してcutNodeである場合
			            if (parent[v] == root) deg[root]++;
			        }
			        if(v!=root)deg[v]++;
			    }
			}
			if(deg[root]==0)deg[root]++;
		}
		return deg;
	}
	
	
    int dfs(int v, int p, boolean[] vis, int[] min, int[] depth, int[] deg) {
        if (p != -1) {
            depth[v] = depth[p] + 1;
        }
        min[v] = depth[v];
        vis[v] = true;
        int allSubTreeMin = Integer.MAX_VALUE;
        int childs = 0;// 子の数

        for (int u : adj[v]) {
            if (u == p) {
                continue;
            }
            if (vis[u]) {
                // 後退辺。辺(v,parent[v])ではないのでminを更新。
                min[v] = Math.min(min[v], depth[u]);
            } else {
                childs++;
                int subTreeMin = dfs(u, v, vis, min, depth, deg);
                allSubTreeMin = Math.min(allSubTreeMin, subTreeMin);
                if ((p != -1 && min[u] >= depth[v]) || (p == -1 && childs >= 2)) {
                	// 子に対してvがcutNodeである場合　(v=root⇔p=-1で場合分け)
                    deg[v]++;
                }
            }
        }
        deg[v]++;
        min[v] = Math.min(min[v], allSubTreeMin);
        return min[v];
    }

    public int[] findCycleVertices() {
    	//https://judge.yosupo.jp/problem/cycle_detection_undirected
		int[] state=new int[N];//0:未訪問,1:pathに載っている,2:棄却した
		IntArrayList ans=new IntArrayList();
		for (int i = 0; i < N; i++) {
			if(state[i]==0) {
				IntDeque stk=new IntDeque();
				dfsFidCycleVertices(i, -1, state, stk, ans);
			}
			if (!ans.isEmpty()) return ans.toArray();
		}
		return null;
	}
	
	void dfsFidCycleVertices(int v, int par, int[] state, IntDeque path, IntArrayList ans) {
		state[v]=1;
		path.addLast(v);
		boolean f=true;
		for (int u : adj[v]) {
			if (u == par && f) {
				f=false;
				continue;//同じ辺を２回使う長さ２のサイクルは無視
			}
			if (state[u] == 2) continue;
			if (state[u] == 1) {
				while(path.peekLast() != u) {
					ans.add(path.pollLast());
				}
				ans.add(u);
				return;
			} else {
				dfsFidCycleVertices(u, v, state, path, ans);
				if (!ans.isEmpty()) return;
			}
		}
		path.pollLast();
		state[v]=2;
	}

	/**
	 * グラフから奇サイクルを一つ探索する。
	 * 奇サイクルが存在する場合、その頂点列 $(v_1, v_2, \dots, v_k)$ を返す。
	 * ここで $k$ は奇数であり、任意の $1 \le i < k$ について $(v_i, v_{i+1}) \in E$ かつ $(v_k, v_1) \in E$ である。
	 * 存在しない場合は null を返す。
	 * <p>
	 * 内部的には BFS を用い、各連結成分において距離の偶奇が等しい頂点間を結ぶ辺を探索する。
	 * </p>
	 *
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 戻り値が非 null の場合、奇サイクルを構成する頂点列である。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N + M)$</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>例外: なし。</li>
	 * </ul>
	 * 未テスト
	 * @return 奇サイクルの頂点列、または null
	 */
	public int[] findOddCycle() {
		int[] dist = new int[N];
		int[] parent = new int[N];
		Arrays.fill(dist, -1);
		for (int i = 0; i < N; i++) {
			if (dist[i] != -1) continue;
			dist[i] = 0;
			parent[i] = -1;
			Queue<Integer> que = new ArrayDeque<>();
			que.add(i);
			while (!que.isEmpty()) {
				int v = que.poll();
				for (int u : adj[v]) {
					if (dist[u] == -1) {
						dist[u] = dist[v] + 1;
						parent[u] = v;
						que.add(u);
					} else if ((dist[u] & 1) == (dist[v] & 1)) {
						return _reconstructCycle(u, v, parent, dist);
					}
				}
			}
		}
		return null;
	}

	/**
	 * グラフから偶サイクルを一つ探索する。
	 * 偶サイクルが存在する場合、その頂点列 $(v_1, v_2, \dots, v_k)$ を返す。
	 * ここで $k$ は偶数であり、任意の $1 \le i < k$ について $(v_i, v_{i+1}) \in E$ かつ $(v_k, v_1) \in E$ である。
	 * 存在しない場合は null を返す。
	 * <p>
	 * 内部的には BFS 木を構築し、木に含まれない辺 $(u, v)$ であって $dist(u, root) \not\equiv dist(v, root) \pmod 2$ となるものを探索する。
	 * 多重辺が存在する場合、長さ 2 のサイクルも検出対象となる。
	 * </p>
	 *
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 戻り値が非 null の場合、偶サイクルを構成する頂点列である。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N + M)$</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>例外: なし。</li>
	 * </ul>
	 * 未テスト
	 * @return 偶サイクルの頂点列、または null
	 */
	public int[] findEvenCycle() {
		int[] dist = new int[N];
		int[] parent = new int[N];
		Arrays.fill(dist, -1);
		for (int i = 0; i < N; i++) {
			if (dist[i] != -1) continue;
			dist[i] = 0;
			parent[i] = -1;
			Queue<Integer> que = new ArrayDeque<>();
			que.add(i);
			while (!que.isEmpty()) {
				int v = que.poll();
				boolean parentSkipped = false;
				for (int u : adj[v]) {
					if (u == parent[v] && !parentSkipped) {
						parentSkipped = true;
						continue;
					}
					if (dist[u] == -1) {
						dist[u] = dist[v] + 1;
						parent[u] = v;
						que.add(u);
					} else if ((dist[u] & 1) != (dist[v] & 1)) {
						return _reconstructCycle(u, v, parent, dist);
					}
				}
			}
		}
		return null;
	}

	private int[] _reconstructCycle(int u, int v, int[] parent, int[] dist) {
		IntArrayList pathU = new IntArrayList();
		IntArrayList pathV = new IntArrayList();
		int currU = u;
		int currV = v;
		while (currU != currV) {
			if (dist[currU] > dist[currV]) {
				pathU.add(currU);
				currU = parent[currU];
			} else if (dist[currV] > dist[currU]) {
				pathV.add(currV);
				currV = parent[currV];
			} else {
				pathU.add(currU);
				pathV.add(currV);
				currU = parent[currU];
				currV = parent[currV];
			}
		}
		pathU.add(currU);
		pathV.reverse();
		int[] res = new int[pathU.size() + pathV.size()];
		int ptr = 0;
		for (int i = 0; i < pathU.size(); i++) res[ptr++] = pathU.get(i);
		for (int i = 0; i < pathV.size(); i++) res[ptr++] = pathV.get(i);
		return res;
	}
	
	
	/**
	 * u,vを含むサイクルを返す。未テスト
	 * @param u
	 * @param v
	 * @return
	 */
    public int[] findCycleVertices(int u, int v) {
		MaxFlow mf=new MaxFlow(2*N);
		int source=2*u;
		int sink=2*v+1;
		for (int i = 0; i < N; ++i) {
			mf.addEdge(2*i, 2*i+1, u == i || v==i ? 2:1);
		}
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				mf.addEdge(2*i+1, 2*j, 1);
			}
		}
		long conn=mf.maxFlowValue(source, sink, 2);
		if (conn < 2) return null;
		IntArrayList list1=new IntArrayList();
		IntArrayList list2=new IntArrayList();
		int[] iter=new int[2*N];
		{
			int x=source;
			if(x%2==0)list1.add(x/2);
			while(x != sink) {
				while (mf.g[x].get(iter[x]).flow <= 0) ++iter[x];
				var e=mf.g[x].get(iter[x]);
				x=e.v;
				e.flow--;
				if(x%2==0)list1.add(x/2);
			}
			
		}
		{
			int x=source;
			if(x%2==0) list2.add(x/2);
			while(x != sink) {
				while (mf.g[x].get(iter[x]).flow <= 0) ++iter[x];
				var e=mf.g[x].get(iter[x]);
				x=e.v;
				e.flow--;
				if(x%2==0) list2.add(x/2);
			}
		}
		int[] path1=list1.toArray();
		int[] path2=list2.toArray();
		ArrayUtils.reverse(path2);
		path2=Arrays.copyOfRange(path2, 1, path2.length-1);
		return ArrayUtils.concat(path1, path2);

    }

    
    
    
    private static class OrderNode {
        int v;
        OrderNode prev, next;
        OrderNode(int v) { this.v = v; }
    }

    /**
     * sを先頭, tを末尾とする頂点の並び　P であって、この並びに沿って辺を向きづけたときに任意の頂点 v について、s-v-tパスが存在するものを構築する。
     * そして inv(P) を返す。ただし、Pに含まれない頂点はinv(P)[i]=-1となる。
     * https://kops.uni-konstanz.de/server/api/core/bitstreams/009bf012-002e-4f94-961d-27537df26689/content
     * https://judge.yosupo.jp/submission/355932
     * @param s
     * @param t
     * @return
     */
    public int[] stNumbering(int s, int t) {
        if (N < 2) return new int[N];
        if (bfsDistances(s)[t] >= N) {
        	int[]ret=new int[N];
        	Arrays.fill(ret, -1);
        	return ret;
        }
        int[]next=new int[N];
        int[]prev=new int[N];
        Arrays.fill(next, -1);
        Arrays.fill(prev, -1);
        next[s]=t;
        prev[t]=s;

        int[] parent = new int[N];
        int[] currentChild = new int[N];
        boolean[] isUp = new boolean[N];
        boolean[] inList = new boolean[N];
        
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] dependency = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            dependency[i] = new ArrayList<>();
            parent[i] = -1;
        }
        int[] state=new int[N];//0:未訪問,1:訪問済みの祖先(自分含む),2:訪問済みの子孫(自分含まない)
        state[s]=state[t]=1;
        inList[s] = true;
        inList[t] = true;
        parent[t] = s;
        currentChild[s] = t;
        dfsSt(t, s, state, parent, currentChild, dependency, inList, prev, next, isUp);
        int[] orderMap = new int[N];
        Arrays.fill(orderMap, -1);
        int rank = 0;
        while (s != -1) {
            orderMap[s] = rank++;
            s = next[s];
        }
        return orderMap;
    }

    private void dfsSt(int v, int p, int[] state, int[] parent, int[] currentChild, 
                       ArrayList<Integer>[] dependency, boolean[] inList, int[] prev, int[] next, boolean[] isUp) {
    	state[v] = 1;
        parent[v] = p;
        for (int u : adj[v]) {
            if (u == p) continue;
            if (state[u] == 0) {
                currentChild[v] = u;
                dfsSt(u, v, state, parent, currentChild, dependency, inList, prev, next, isUp);
            } else if (state[u] == 1){
                // 後退辺 (v, u)
            	int x = currentChild[u];
            	dependency[x].add(v);
                if (inList[x]) {
                	processEars(u, currentChild[u], dependency, inList, prev, next, parent, isUp);
                }
            }
        }
        state[v] = 2;
    }

    // 耳（パス）の挿入
    private void processEars(int x, int w, ArrayList<Integer>[] dependency, 
                             boolean[] inList, int[] prev, int[] next, int[] parent, boolean[] isUp) {
        ArrayList<Integer> src = new ArrayList<>(dependency[w]);
        dependency[w].clear();
        for (int v : src) {
            if (inList[v]) continue;
            // 耳（inListでない頂点の連鎖）を特定
            IntArrayList ear = new IntArrayList();
            int curr = v;
            while (!inList[curr]) {
                ear.add(curr);
                curr = parent[curr];
            }
            
            //  x--w--              -v
            //  |                    |
            //  -----(後退辺）---------

            
            
            int lastNode = curr;
            if (isUp[w]) {
                //根    x←w--    -curr-[ear   先頭=v]      葉 
                //      |                          |
                //      -----(後退辺）---------------
            	
            	// [ear]は→に向きづけて、後退辺とxwが同じ向きになるようにする
            	
                //根    x←w--    -curr→[ear→→→→→→→→]      葉 
                //      ↑                          ↓
                //      -----(後退辺）-------←←←←←←←←

            	for (int i = ear.size() - 1; i >= 0; i--) {
            		int p = ear.get(i);
            		insertAfter(lastNode, p, prev, next);
            		inList[p] = true;
            		lastNode = p;
            		isUp[p]=false;
            	}
            } else {
            	for (int i = ear.size() - 1; i >= 0; --i) {
            		int p = ear.get(i);
            		insertBefore(lastNode, p, prev, next);
            		inList[p] = true;
            		lastNode = p;
            		isUp[p]=true;
            	}
            }
            for (int p : ear) {
            	processEars(parent[p], p, dependency, inList, prev, next, parent, isUp);
            }
        }
    }
    
    private void insertAfter(int target, int newNode, int[] prev, int[] next) {
    	int b=next[target];
    	next[target] = newNode;
    	prev[newNode] = target;
    	next[newNode] = b;
    	if (b != -1) prev[b] = newNode;
    }

    private void insertBefore(int target, int newNode, int[] prev, int[] next) {
    	int a=prev[target];
    	if (a != -1)next[a]=newNode;
    	prev[newNode] = a;
    	next[newNode] = target;
    	prev[target] = newNode;
    }    
    
    
    
	/**
	 * 多重辺を一つの辺にする。
	 * 未テスト
	 */
	public void removeMultipleEdges() {
		int[]cnt=new int[N];
		for (int i = 0; i < N; i++) {
			boolean multipleEdgeExist=false;
			for (var j : adj[i]) {
				cnt[j]++;
				multipleEdgeExist |= cnt[j] >= 2;
			}
			if (multipleEdgeExist) {
				IntArrayList nadj=new IntArrayList(adj[i].size());
				for (var j : adj[i]) {
					if (cnt[j] == 1) {
						nadj.add(j);
					} else {
						if (j >= i)
							M--;
					}
					cnt[j]--;
				}
				adj[i] = nadj;
			} else {
				for (var j : adj[i]) {
					cnt[j]--;
				}
			}
		}
	}

    
    
    public void dump() {
    	System.out.println("N="+N + " M=" + M);
    	for (var e : edges()) {
    		System.out.println(e[0] + " " + e[1]);
    	}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph graph)) return false;
        if (N != graph.N || M != graph.M) return false;
        for (int i = 0; i < N; i++) {
            if (adj[i].size() != graph.adj[i].size()) return false;
            int[] a1 = adj[i].toArray();
            int[] a2 = graph.adj[i].toArray();
            Arrays.sort(a1);
            Arrays.sort(a2);
            if (!Arrays.equals(a1, a2)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = N;
        result = 31 * result + M;
        for (int i = 0; i < N; i++) {
            int h = 0;
            for (int j = 0; j < adj[i].size(); j++) {
                h += adj[i].get(j);
            }
            result = 31 * result + h;
        }
        return result;
    }

	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
