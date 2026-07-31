package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import library.tools.FastScanner;
import library.util.ArrayUtils;
import library.util.Ints;
import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;
import library.util.unionfind.UnionFind;

public class Digraph {
	public int N;
	public int M;
	public IntArrayList[] adj;
	
	
	public Digraph(int N) {
		//DAGのreverseでN=0で生成してから手動で作ってるので帰る時は注意
		this.N = N;
		adj = new IntArrayList[N];
		for (int i = 0; i < N; ++i) adj[i] = new IntArrayList();
	}
	
	/**
	 * N M
	 * a[1] b[1]
	 * ..
	 * a[M] b[M]
	 * という入力(1-origin)を受け取り、辺(a[i],b[i])を張る
	 * @param N
	 * @param M
	 * @return
	 */
	public static Digraph read(int N, int M) {
		Digraph graph = new Digraph(N);
		FastScanner sc = FastScanner.getInstance();
		for (int i = 0; i < M; ++i) {
			int a = sc.nextInt() - 1;
			int b = sc.nextInt() - 1;
			graph.addEdge(a, b);
		}
		return graph;
	}
	
	public void addEdge(int from, int to) {
		adj[from].add(to);
		++M;
	}
	
	/***
	 * 頂点 src からの距離を配列で返す。
	 * 到達不能な頂点に対する距離はInteger.MAX_VALUE/3とする。
	 * @param src
	 * @return
	 */
	public int[] bfsDistances(int src) {
		int INF=Integer.MAX_VALUE/3;
		int[] dist=new int[N];
		Arrays.fill(dist, INF);
		dist[src]=0;
		IntDeque que=new IntDeque();
		que.addLast(src);
		while(!que.isEmpty()) {
			int v=que.pollFirst();
			for(int u:adj[v]) {
				if(dist[u]==INF) {
					dist[u]=dist[v]+1;
					que.addLast(u);
				}
			}
		}
		return dist;
	}
	
	public int outDegree(int i) {
		return adj[i].size();
	}
	
	public int[] outDegrees() {
		int[] ret = new int[N];
		Arrays.setAll(ret, i -> adj[i].size());
		return ret;
	}
	
	public int[] inDegrees() {
		int[] ret = new int[N];
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) ret[j]++;
		}
		return ret;
	}

	/**
	 * 有向グラフのオイラー路を頂点列で返す。存在しないときはnull。
	 * O(N+M)
	 * @return
	 */
	public int[] eulerTrail() {
		//https://judge.yosupo.jp/submission/371006
		if (N == 0) return new int[0];
		int[] inDegree = inDegrees();
		int start = 0;
		for (int i = 0; i < N; ++i) {
			if (adj[i].size() - inDegree[i] == 1) return eulerTrail(i);
			if (adj[i].size() > 0) start = i;
		}
		return eulerTrail(start);
	}

	/**
	 * 頂点startから始まる有向グラフのオイラー路を頂点列で返す。存在しないときはnull。
	 * O(N+M)
	 * @param start
	 * @return
	 */
	public int[] eulerTrail(int start) {
		//https://judge.yosupo.jp/submission/371006
		if (start < 0 || start >= N) throw new AssertionError();
		if (M == 0) return new int[] {start};
		int[] it = new int[N];
		int[] indegs=inDegrees();
		{// eulerian trailが存在するか判定
			boolean f = false;
			for (int i = 0; i < N; i++) {
				if (indegs[i] == outDegree(i)) continue;
				if (indegs[i] + 1 == outDegree(i)) {
					if (start != i) return null;
				} else if (indegs[i] == outDegree(i) + 1) {
					if (f) return null;
					else f = true;
				} else return null;
				
			}
		}
		IntArrayList stack = new IntArrayList(M + 1);
		IntArrayList path = new IntArrayList(M + 1);
		stack.add(start);
		while (stack.isNonEmpty()) {
			int v = stack.peekLast();
			if (it[v] == adj[v].size()) {
				path.add(stack.pollLast());
			} else {
				stack.add(adj[v].get(it[v]++));
			}
		}
		if (path.size() != M + 1) return null;
		path.reverse();
		return path.toArray();
	}
	
	public static Digraph randomDiGraph(int N, int M) {
		if (M > N * N) throw new AssertionError();
		Digraph ret = new Digraph(N);
		Random rnd = new Random();
		Set<Long> set = new HashSet<>();
		for (int i = 0; i < M; ++i) {
			while (true) {
				int a = rnd.nextInt(N);
				int b = rnd.nextInt(N);
				long hash = Ints.pack(a, b);
				if (set.contains(hash)) continue;
				set.add(hash);
				ret.addEdge(a, b);
				break;
			}
		}
		return ret;
	}

	
	/***
	 * 辺重み1 とした ValueGraph を返す。
	 * @return
	 */
	public LongValueDigraph toLongValueDigraph() {
		LongValueDigraph ret = new LongValueDigraph(N);
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				ret.addEdge(i, j, 1);
			}
		}
		return ret;
	}

	/**
	 * 次数2（入次数が1、かつ出次数が1）の頂点を縮約した有向重み付きグラフを返す。
	 * 縮約後の辺の重みは、元のグラフで対応する有向パスの辺数である。
	 * <p>
	 * 縮約後も頂点番号と頂点数は元のグラフのまま保つ。
	 * 縮約された頂点は、入辺も出辺も持たない孤立点として残る。
	 * 全ての頂点が次数2である有向サイクル成分については、代表頂点（番号最小の頂点）を1つ残し、
	 * サイクル長を重みとする自己ループとして表す。
	 * 多重辺はそのまま残す。
	 * </p>
	 * <p>
	 * 計算量は O(N + M)。
	 * </p>
	 *
	 * @return 次数2の頂点を縮約した {@link LongValueDigraph}
	 */
	public LongValueDigraph homeomorhicReductionToLongDigraph() {
		return homeomorhicReductionToLongDigraph(new boolean[N]);
	}

	/**
	 * 次数2（入次数が1、かつ出次数が1）の頂点を縮約した有向重み付きグラフを返す。
	 * ただし、{@code isTerminal[v]} が true の頂点 {@code v} は、次数2でも縮約せずに残す。
	 * 縮約後の辺の重みは、元のグラフで対応する有向パスの辺数である。
	 * <p>
	 * 縮約後も頂点番号と頂点数は元のグラフのまま保つ。
	 * 縮約された頂点は、入辺も出辺も持たない孤立点として残る。
	 * 全ての非ターミナル頂点が次数2である有向サイクル成分については、代表頂点（番号最小の頂点）を1つ残し、
	 * サイクル長を重みとする自己ループとして表す。
	 * 多重辺はそのまま残す。
	 * </p>
	 * <p>
	 * 計算量は O(N + M)。
	 * </p>
	 *
	 * @param isTerminal 縮約せずに残す頂点を表す長さNの配列
	 * @return 次数2の非ターミナル頂点を縮約した {@link LongValueDigraph}
	 */
	public LongValueDigraph homeomorhicReductionToLongDigraph(boolean[] isTerminal) {
		//https://atcoder.jp/contests/abc372/submissions/75307257
		if (isTerminal.length != N) throw new AssertionError();
		int[] inDegree = inDegrees();
		boolean[] contractable = new boolean[N];
		for (int i = 0; i < N; ++i) {
			contractable[i] = inDegree[i] == 1 && adj[i].size() == 1 && !isTerminal[i];
		}
		LongValueDigraph ret = new LongValueDigraph(N);
		boolean[] vis = new boolean[N];
		for (int i = 0; i < N; ++i) {
			if (contractable[i]) continue;
			for (int j : adj[i]) {
				int v = j;
				long cost = 1;
				while (contractable[v]) {
					vis[v] = true;
					v = adj[v].get(0);
					++cost;
				}
				ret.addEdge(i, v, cost);
			}
		}
		for (int i = 0; i < N; ++i) {//サイクルを処理
			if (!contractable[i] || vis[i]) continue;
			int v = i;
			long cost = 0;
			do {
				vis[v] = true;
				v = adj[v].get(0);
				++cost;
			} while (v != i);
			ret.addEdge(i, i, cost);
		}
		return ret;
	}
	
	
	
	public void draw() {
        System.setProperty("org.graphstream.ui", "swing");
		org.graphstream.graph.Graph graph = new MultiGraph("MyGraph");
		for (int i = 0; i < N; ++i) {
			graph.addNode(String.valueOf(i));
		}
		int edgeId=0;
		for (int i = 0; i < N; ++i) {
			for (int j : adj[i]) {
				String a=String.valueOf(i);
				String b=String.valueOf(j);
				graph.addEdge(a+":"+b+"(id="+edgeId+++")", a, b, true);
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
		    "}" +
		    "edge { " +
		    "   arrow-shape: arrow; " +
		    "}"
		);
        graph.display();
	}

	
	public boolean isDAG() {
		int[] inDegrees = inDegrees();
		Queue<Integer> que = new ArrayDeque<>();
		for (int i  = 0; i < N; ++i) {
			if (inDegrees[i] == 0) que.add(i);
		}
		boolean[] vis = new boolean[N];
		int p = 0;
		while (!que.isEmpty()) {
			int v = que.poll();
			p++;
			vis[v] = true;
			for (int u : adj[v]) {
				if (!vis[u]) {
					--inDegrees[u];
					if (inDegrees[u] == 0) {
						que.add(u);
					}
				}
			}
		}
		return p == N;
	}
	
	
	
	/**
	 * a < b <=> a から b への path が存在
	 * という条件の元で頂点の全順序 v[0] <= v[1] <= .. <= v[n-1] を定め、vを返す。 
	 * つまり小さい方から大きい方へ辺があるとする。
	 * DAGでない場合は null を返す。
	 * @return トポロジカルソート順に並んだ頂点配列。DAGでない場合は null
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
			for (int u : adj[v]) {
				if (!vis[u]) {
					--inDegrees[u];
					if (inDegrees[u] == 0) {
						que.add(u);
					}
				}
			}
		}
		if (p != N) return null;
		return order;
	}
	
	
	
	
	
	/**
	 * a < b <=> a から b への path が存在
	 * という条件の元で頂点の全順序 v[0] <= v[1] <= .. <= v[n-1] を定め、vを返す。 
	 * つまり小さい方から大きい方へ辺があるとする。
	 * DAGでない場合エラー。複数のtopological sortがあるときは辞書順最小を返す。
	 * https://atcoder.jp/contests/abc223/submissions/73241640
	 * @return
	 */
	public int[] lexsmallestTopologicalOrder() {
		int[] inDegrees = inDegrees();
		PriorityQueue<Integer> que = new PriorityQueue<>();
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
			for (int u : adj[v]) {
				if (!vis[u]) {
					--inDegrees[u];
					if (inDegrees[u] == 0) {
						que.add(u);
					}
				}
			}
		}
		if (p != N) throw new AssertionError("DAGではない");
		return order;
	}

	/**
	 * グラフに含まれる全てのトポロジカルソートを列挙し、それぞれに対して work を実行する。
	 * workに与えられる順列はcloneされていて、変更しても副作用がない。
	 * work が false を返した場合は列挙を中断する。
	 * 辞書順で列挙する。
	 * <p>
	 * 計算量: $O(V \cdot V! + E \cdot V!)$
	 * </p>
	 * @param n 頂点数
	 * @param g 対象のグラフ
	 * @param work 各トポロジカルソートに対して実行する処理
	 */
	public void forEachTopologicalSortLexOrder(int n, Predicate<int[]> work) {
		//https://atcoder.jp/contests/typical90/submissions/76905159
		int[] inDegrees = inDegrees();
		TreeSet<Integer> candidates = new TreeSet<>();
		for (int i = 0; i < n; i++) {
			if (inDegrees[i] == 0) candidates.add(i);
		}
		int[] currentOrder = new int[n];
		forEachTopologicalSortRecursiveLexOrder(0, n, inDegrees, candidates, currentOrder, work);
	}

	private boolean forEachTopologicalSortRecursiveLexOrder(int depth, int n, int[] inDegrees, TreeSet<Integer> candidates, int[] currentOrder, Predicate<int[]> work) {
		if (depth == n) {
			return work.test(currentOrder.clone());
		}
		if (candidates.isEmpty()) return false;//DAGでない
		Integer choice = candidates.first();
	    while (choice != null) {
	        Integer nextChoice = candidates.higher(choice);

	        candidates.remove(choice);
	        currentOrder[depth] = choice;

	        for (int idx = 0; idx < adj[choice].size(); idx++) {
	            int to = adj[choice].get(idx);
	            inDegrees[to]--;
	            if (inDegrees[to] == 0) {
	                candidates.add(to);
	            }
	        }

	        if (!forEachTopologicalSortRecursiveLexOrder(
	                depth + 1, n, inDegrees, candidates, currentOrder, work
	        )) {
	            return false;
	        }

	        for (int idx = 0; idx < adj[choice].size(); idx++) {
	            int to = adj[choice].get(idx);
	            if (inDegrees[to] == 0) {
	                candidates.remove(to);
	            }
	            inDegrees[to]++;
	        }

	        candidates.add(choice);

	        choice = nextChoice;
	    }
		return true;
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
	 * 各強連結成分をArrayListとした配列を返す。
	 * 強連結成分は、toplogical sort順（親が先）に並んでいる。
	 * @return
	 */
	public IntArrayList[] scc() {
		int[] sorted = new int[N];
		{
			boolean[]vis=new boolean[N];
			int p = N - 1;
			int[] next = new int[N];
			int[] parent = new int[N];
			Arrays.fill(parent, -1);
			for (int i = 0; i < N; i++) {
				if(vis[i])continue;
				ArrayList<Integer> dfsOrder = new ArrayList<>();
				int v = i;
				while (v != -1) {
					vis[v] = true;
					if (next[v] == adj[v].size()) {
						dfsOrder.add(v);
						v = parent[v];
					} else {
						int u = adj[v].get(next[v]++);
						if (u == parent[v] || vis[u]) {
							continue;
						} else {
							parent[u] = v;
							v = u;
						}
					}
				}
				for (int j = 0; j < dfsOrder.size(); j++) {
					int u = dfsOrder.get(j);
					sorted[p--] = u;
					vis[u] = true;
				}
			}
		}
		//sorted配列は、sccしたときに親 < 子となるような順番で並んでいる。
		//sccしたときの森について、逆辺でどこまで辿れるかを計算する。
		int[] id = new int[N];
		int sz = 0;
		{
			Arrays.fill(id, -1);
			var ig = reverse();
			for (int i : sorted) {
				if (id[i] != -1) continue;
				ArrayList<Integer> bfsOrder = new ArrayList<>();
				Queue<Integer> que = new ArrayDeque<Integer>();
				que.add(i);
				bfsOrder.add(i);
				id[i] = sz;
				while (!que.isEmpty()) {
					int v = que.poll();
					for (int u : ig.adj[v]) {
						if (id[u] == -1) {
							bfsOrder.add(u);
							que.add(u);
							id[u] = sz;
						}
					}
				}
				sz++;
			}
		}
		IntArrayList[] list = new IntArrayList[sz];
		for (int i = 0; i < list.length; i++) {
			list[i]=new IntArrayList();
		}
		for (int i = 0; i < N; i++) {
			list[id[i]].add(i);
		}
		return list;
	}

	public record sccDAGResult(IntArrayList[] comps, int[] col, Digraph g) {};

	/***
	 * 各強連結成分をArrayListとした配列、元の頂点からscc後の頂点への写像col、scc後のグラフを返す。
	 * 強連結成分は、toplogical sort順（親が先）に並んでいる。
	 * scc後のグラフでは自己ループは取り除き多重辺はそのまま。
	 * @return
	 */
	public sccDAGResult sccDAG() {
		var comps=scc();
		int[] size=new int[comps.length];
		DAG h=new DAG(comps.length);
		int[]col=new int[N];
		for (int i = 0; i < comps.length; i++) {
			size[i]=comps[i].size();
			for (int j : comps[i]) {
				col[j]=i;
			}
		}
		for (var e : edges()) {
			if (col[e.src] != col[e.dst]) {
				h.addEdge(col[e.src], col[e.dst]);
			}
		}
		return new sccDAGResult(comps, col, h);
	}
	
	
	boolean isWeaklyConnected() {
		UnionFind uf = new UnionFind(N);
		for (int i = 0; i < N; i++) {
			for (var j : adj[i]) {
				uf.union(i, j);
			}
		}
		return uf.size(0) == N;
	}
	
	public Digraph reverse() {
		Digraph ig = new Digraph(N);
		for (int i = 0; i < N; i++) {
			for (int v : adj[i]) {
				ig.addEdge(v, i);
			}
		}
		return ig;
	}
	
	public ArrayList<Edge> edges() {
		ArrayList<Edge> ret=new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (int j : adj[i]) {
				ret.add(new Edge(i, j, 1));
			}
		}
		return ret;
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
	public Digraph copy() {
		Digraph ret = new Digraph(N);
		ret.M = M;
		for (int i = 0; i < N; i++) {
			ret.adj[i].addAll(this.adj[i]);
		}
		return ret;
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
	
	/**
	 * srcを始点とする最短閉路の頂点列を返す。列の長さは(閉路の長さ)になる。
	 * 見つからなかったときはnull
	 * @param src
	 * @return
	 */
	public int[] findMinCycle(int src) {
		//https://atcoder.jp/contests/abc306/submissions/73270403
		int[] prev=new int[N];
		Arrays.fill(prev, -1);
		Queue<Integer> que=new ArrayDeque<>();
		que.add(src);
		out:while(!que.isEmpty()) {
			int v=que.poll();
			for (int u : adj[v]) {
				if (prev[u] == -1) {
					prev[u] = v;
					if (u == src) break out;
					que.add(u);
				}
			}
		}
		if(prev[src]==-1)return null;
		IntArrayList list=new IntArrayList();
		int v=src;
		do {
			list.add(v);
			v=prev[v];
		} while (v != src);
		int[] ret=list.toArray();
		ArrayUtils.reverse(ret);
		return ret;
	}

	
    public int[] findCycleVertices() {
    	//https://judge.yosupo.jp/submission/355264
		int[] state=new int[N];//0:未訪問,1:pathに載っている,2:棄却した
		IntArrayList ans=new IntArrayList();
		for (int i = 0; i < N; i++) {
			if(state[i]==0) {
				IntDeque stk=new IntDeque();
				dfsFidCycleVertices(i, state, stk, ans);
				if (!ans.isEmpty()) {
					int[]ret=ans.toArray();
					ArrayUtils.reverse(ret);
					return ret;
				}
			}
		}
		return null;
	}
	
    void dfsFidCycleVertices(int v, int[] state, IntDeque path, IntArrayList ans) {
		state[v]=1;
		path.addLast(v);
		for (int u : adj[v]) {
			if (state[u] == 2) continue;
			if (state[u] == 1) {
				while(path.peekLast() != u) {
					ans.add(path.pollLast());
				}
				ans.add(u);
				return;
			} else {
				dfsFidCycleVertices(u, state, path, ans);
				if (!ans.isEmpty()) return;
			}
		}
		path.pollLast();
		state[v]=2;
	}

	
	
	/**
	 * 多重辺を一つの辺にする。
	 * 未テスト
	 */
	public void removeMultipleEdges() {
		//https://atcoder.jp/contests/abc440/submissions/73551756
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
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
