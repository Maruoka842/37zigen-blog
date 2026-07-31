package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import library.util.collections.Hash;
import library.util.collections.OpenHashMap;
import library.util.collections.OpenHashSet;

/**
 * 暗黙的に定義された有向グラフを扱うための抽象クラス。
 *
 * <p>頂点集合が膨大または未知であり、隣接リストが動的に生成される場合に使用する。
 * 到達可能な範囲の探索や、各種グラフアルゴリズムをサポートする。</p>
 *
 * @param <V> 頂点（状態）の型。{@code equals} と {@code hashCode} が適切に実装されている必要がある。
 */
public abstract class ImplicitDigraph<V> {

	/** ハッシュ戦略。 */
	protected final Hash.Strategy<V> strategy;

	/**
	 * デフォルトのハッシュ戦略を使用するコンストラクタ。
	 */
	protected ImplicitDigraph() {
		this(Hash.defaultStrategy());
	}

	/**
	 * 指定されたハッシュ戦略を使用するコンストラクタ。
	 * @param strategy ハッシュ戦略
	 */
	protected ImplicitDigraph(Hash.Strategy<V> strategy) {
		this.strategy = strategy;
	}

	/**
	 * 指定された頂点から最大何回移動ができるかを返す。
	 *
	 * @param start 開始頂点
	 * @return 最大移動回数。無限に移動できる（閉路に到達可能）場合は -1。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public int maxMoves(V start) {
		var res = toExplicit(start);
		if (!res.g.isDAG()) return -1;
		int[] order = res.g.topologicalOrder();
		int[] dp = new int[res.g.N];
		for (int k = res.g.N - 1; k >= 0; k--) {
			int i = order[k];
			for (int j : res.g.adj[i]) {
				dp[i] = Math.max(dp[i], dp[j] + 1);
			}
		}
		return dp[0];
	}

	/**
	 * 指定された頂点から到達可能な頂点数を返す。
	 *
	 * @param start 開始頂点
	 * @return 到達可能な頂点数
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public int countReachable(V start) {
		return bfsDistances(start).size();
	}

	/**
	 * 指定された複数の開始頂点から到達可能な頂点数を返す。
	 *
	 * @param starts 開始頂点のイテラブル
	 * @return 到達可能な頂点数
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public int countReachable(Iterable<V> starts) {
		return bfsDistances(starts).size();
	}
	
	public OpenHashSet<V> reachables(V start) {
		var map=bfsDistances(start);
		OpenHashSet<V> set=new OpenHashSet<V>(strategy);
		for (var es : map.entrySet()) set.add(es.key);
		return set;
	}

	/**
	 * 指定された頂点から遷移可能な次の頂点の集合を返す。
	 *
	 * @param v 現在の頂点
	 * @return 次の頂点のイテラブル
	 */
	public abstract Iterable<V> nextStates(V v);

	/**
	 * 明示的なグラフとその頂点情報。
	 * @param <V> 頂点の型
	 */
	public record ExplicitResult<V>(Digraph g, List<V> nodes, OpenHashMap<V, Integer> idMap) {}

	/**
	 * 指定された開始頂点から到達可能な範囲を走査し、明示的な有向グラフに変換する。
	 *
	 * @param start 開始頂点
	 * @return 明示的なグラフとその頂点情報を含む {@link ExplicitResult}
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public ExplicitResult<V> toExplicit(V start) {
		List<V> nodes = new ArrayList<>();
		OpenHashMap<V, Integer> idMap = new OpenHashMap<>(strategy);
		List<int[]> edges = new ArrayList<>();
		Queue<V> que = new ArrayDeque<>();

		idMap.put(start, nodes.size());
		nodes.add(start);
		que.add(start);

		while (!que.isEmpty()) {
			V u = que.poll();
			int uId = idMap.get(u);
			for (V v : nextStates(u)) {
				if (!idMap.containsKey(v)) {
					idMap.put(v, nodes.size());
					nodes.add(v);
					que.add(v);
				}
				edges.add(new int[]{uId, idMap.get(v)});
			}
		}

		Digraph g = new Digraph(nodes.size());
		for (int[] e : edges) {
			g.addEdge(e[0], e[1]);
		}
		return new ExplicitResult<>(g, nodes, idMap);
	}

	/**
	 * 指定された複数の開始頂点から到達可能な範囲を走査し、明示的な有向グラフに変換する。
	 *
	 * @param starts 開始頂点のイテラブル
	 * @return 明示的なグラフとその頂点情報を含む {@link ExplicitResult}
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public ExplicitResult<V> toExplicit(Iterable<V> starts) {
		List<V> nodes = new ArrayList<>();
		OpenHashMap<V, Integer> idMap = new OpenHashMap<>(strategy);
		List<int[]> edges = new ArrayList<>();
		Queue<V> que = new ArrayDeque<>();

		for (V start : starts) {
			if (!idMap.containsKey(start)) {
				idMap.put(start, nodes.size());
				nodes.add(start);
				que.add(start);
			}
		}

		while (!que.isEmpty()) {
			V u = que.poll();
			int uId = idMap.get(u);
			for (V v : nextStates(u)) {
				if (!idMap.containsKey(v)) {
					idMap.put(v, nodes.size());
					nodes.add(v);
					que.add(v);
				}
				edges.add(new int[]{uId, idMap.get(v)});
			}
		}

		Digraph g = new Digraph(nodes.size());
		for (int[] e : edges) {
			g.addEdge(e[0], e[1]);
		}
		return new ExplicitResult<>(g, nodes, idMap);
	}

	/**
	 * 頂点 src から dst への最短距離を返す。
	 *
	 * @param src 始点
	 * @param dst 終点
	 * @return 最短距離。到達不能な場合は -1。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public long dist(V src, V dst) {
		return bfsDistances(src).getOrDefaultValue(dst, -1);
	}

	/**
	 * 頂点 src から dst への最短パスを一つ返す。
	 *
	 * @param src 始点
	 * @param dst 終点
	 * @return 頂点列のリスト。到達不能な場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> path(V src, V dst) {
		OpenHashMap<V, V> prev = new OpenHashMap<>(strategy);
		Queue<V> que = new ArrayDeque<>();
		que.add(src);
		prev.put(src, null);

		boolean found = false;
		while (!que.isEmpty()) {
			V v = que.poll();
			if (strategy.equals(v, dst)) {
				found = true;
				break;
			}
			for (V u : nextStates(v)) {
				if (!prev.containsKey(u)) {
					prev.put(u, v);
					que.add(u);
				}
			}
		}

		if (!found) return null;

		List<V> path = new ArrayList<>();
		V curr = dst;
		while (curr != null) {
			path.add(curr);
			curr = prev.get(curr);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * 指定された頂点から受理条件を満たす頂点への最短パスを一つ返す。
	 *
	 * @param src 始点
	 * @param acceptingCondition 受理条件
	 * @return 頂点列のリスト。到達不能な場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> path(V src, Predicate<? super V> acceptingCondition) {
		OpenHashMap<V, V> prev = new OpenHashMap<>(strategy);
		Queue<V> que = new ArrayDeque<>();
		que.add(src);
		prev.put(src, null);

		V found = null;
		while (!que.isEmpty()) {
			V v = que.poll();
			if (acceptingCondition.test(v)) {
				found = v;
				break;
			}
			for (V u : nextStates(v)) {
				if (!prev.containsKey(u)) {
					prev.put(u, v);
					que.add(u);
				}
			}
		}

		if (found == null) return null;

		List<V> path = new ArrayList<>();
		V curr = found;
		while (curr != null) {
			path.add(curr);
			curr = prev.get(curr);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * 頂点 src から dst へのパスが存在するか判定する。
	 *
	 * @param src 始点
	 * @param dst 終点
	 * @return パスが存在すれば true
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public boolean onPath(V src, V dst) {
		return bfsDistances(src).containsKey(dst);
	}

	/**
	 * 頂点 src からの最短距離を返す。
	 *
	 * @param src 始点
	 * @return 頂点から距離へのマップ。到達不能な頂点は含まれない。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public OpenHashMap<V, Integer> bfsDistances(V src) {
		OpenHashMap<V, Integer> dist = new OpenHashMap<>(strategy);
		dist.put(src, 0);
		Queue<V> que = new ArrayDeque<>();
		que.add(src);
		while (!que.isEmpty()) {
			V v = que.poll();
			int d = dist.get(v);
			for (V u : nextStates(v)) {
				if (!dist.containsKey(u)) {
					dist.put(u, d + 1);
					que.add(u);
				}
			}
		}
		return dist;
	}

	/**
	 * 複数の始点からの最短距離（の最小値）を返す。
	 *
	 * @param sources 始点の集合
	 * @return 頂点から距離へのマップ。到達不能な頂点は含まれない。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public OpenHashMap<V, Integer> bfsDistances(Iterable<V> sources) {
		OpenHashMap<V, Integer> dist = new OpenHashMap<>(strategy);
		Queue<V> que = new ArrayDeque<>();
		for (V src : sources) {
			if (!dist.containsKey(src)) {
				dist.put(src, 0);
				que.add(src);
			}
		}
		while (!que.isEmpty()) {
			V v = que.poll();
			int d = dist.get(v);
			for (V u : nextStates(v)) {
				if (!dist.containsKey(u)) {
					dist.put(u, d + 1);
					que.add(u);
				}
			}
		}
		return dist;
	}

	/**
	 * 指定された頂点の出次数を返す。
	 *
	 * @param v 頂点
	 * @return 出次数
	 *
	 * <p>計算量: 出次数を $k$ として $O(k)$。</p>
	 */
	public int outDegree(V v) {
		int count = 0;
		for (V ignored : nextStates(v)) count++;
		return count;
	}

	/**
	 * 到達可能な範囲にオイラー路が存在するか判定し、存在すれば頂点列を返す。
	 *
	 * @param start 開始頂点
	 * @return 頂点列のリスト。存在しない場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> eulerTrail(V start) {
		var res = toExplicit(start);
		int[] trail = res.g.eulerTrail();
		if (trail == null) return null;
		List<V> ret = new ArrayList<>();
		for (int i : trail) ret.add(res.nodes.get(i));
		return ret;
	}

	/**
	 * 指定された頂点から始まるオイラー路が存在するか判定し、存在すれば頂点列を返す。
	 *
	 * @param start 到達可能範囲の探索開始頂点
	 * @param trailStart オイラー路の開始頂点
	 * @return 頂点列のリスト。存在しない場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> eulerTrail(V start, V trailStart) {
		var res = toExplicit(start);
		if (!res.idMap.containsKey(trailStart)) return null;
		int[] trail = res.g.eulerTrail(res.idMap.get(trailStart));
		if (trail == null) return null;
		List<V> ret = new ArrayList<>();
		for (int i : trail) ret.add(res.nodes.get(i));
		return ret;
	}

	/**
	 * 到達可能な範囲が非巡回（DAG）であるか判定する。
	 *
	 * @param start 開始頂点
	 * @return DAG であれば true
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public boolean isAcyclic(V start) {
		return toExplicit(start).g.isDAG();
	}

	/**
	 * トポロジカルソート順を返す。
	 *
	 * @param start 開始頂点
	 * @return 頂点列のリスト。DAG でない場合は AssertionError を投げる。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> topologicalOrder(V start) {
		var res = toExplicit(start);
		int[] order = res.g.topologicalOrder();
		List<V> ret = new ArrayList<>();
		for (int i : order) ret.add(res.nodes.get(i));
		return ret;
	}

	/**
	 * 辞書順最小のトポロジカルソート順を返す。
	 *
	 * @param start 開始頂点
	 * @return 頂点列のリスト。DAG でない場合は AssertionError を投げる。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M \log V)$。</p>
	 */
	public List<V> lexsmallestTopologicalOrder(V start) {
		var res = toExplicit(start);
		int[] order = res.g.lexsmallestTopologicalOrder();
		List<V> ret = new ArrayList<>();
		for (int i : order) ret.add(res.nodes.get(i));
		return ret;
	}

	/**
	 * 全てのトポロジカルソートを列挙し、それぞれに対して処理を実行する。
	 *
	 * @param start 開始頂点
	 * @param work 各トポロジカルソート（頂点リスト）に対して実行する述語。false を返すと列挙を中断する。
	 *
	 * <p>計算量: 頂点数を $V$ として $O(V \cdot V!)$。</p>
	 */
	public void forEachTopologicalSortLexOrder(V start, Predicate<List<V>> work) {
		var res = toExplicit(start);
		res.g.forEachTopologicalSortLexOrder(res.g.N, order -> {
			List<V> vOrder = new ArrayList<>();
			for (int i : order) vOrder.add(res.nodes.get(i));
			return work.test(vOrder);
		});
	}

	/**
	 * src から到達可能な頂点を BFS 順に並べたリストを返す。
	 *
	 * @param src 始点
	 * @return 頂点列のリスト
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> bfsOrder(V src) {
		List<V> ret = new ArrayList<>();
		OpenHashMap<V, Boolean> vis = new OpenHashMap<>(strategy);
		Queue<V> que = new ArrayDeque<>();
		vis.put(src, true);
		que.add(src);
		while (!que.isEmpty()) {
			V v = que.poll();
			ret.add(v);
			for (V u : nextStates(v)) {
				if (!vis.containsKey(u)) {
					vis.put(u, true);
					que.add(u);
				}
			}
		}
		return ret;
	}

	/**
	 * 強連結成分分解を行う。
	 *
	 * @param start 開始頂点
	 * @return 各強連結成分に含まれる頂点リストのリスト。トポロジカル順に並んでいる。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<List<V>> scc(V start) {
		var res = toExplicit(start);
		var components = res.g.scc();
		List<List<V>> ret = new ArrayList<>();
		for (var comp : components) {
			List<V> vComp = new ArrayList<>();
			for (int i : comp) vComp.add(res.nodes.get(i));
			ret.add(vComp);
		}
		return ret;
	}

	/**
	 * 明示的な辺。
	 * @param <V> 頂点の型
	 */
	public record Edge<V>(V src, V dst, long cost) {}

	/**
	 * 到達可能範囲の全ての辺をリストで返す。
	 *
	 * @param start 開始頂点
	 * @return 辺のリスト
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<Edge<V>> edges(V start) {
		var res = toExplicit(start);
		List<Edge<V>> ret = new ArrayList<>();
		for (var e : res.g.edges()) {
			ret.add(new Edge<>(res.nodes.get(e.src), res.nodes.get(e.dst), e.cost));
		}
		return ret;
	}

	/**
	 * 頂点 src を含む最短閉路を構成する頂点列を返す。
	 *
	 * @param src 始点
	 * @return 頂点列のリスト。閉路が存在しない場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> findMinCycle(V src) {
		var res = toExplicit(src);
		int[] cycle = res.g.findMinCycle(0); // src ID is 0
		if (cycle == null) return null;
		List<V> ret = new ArrayList<>();
		for (int i : cycle) ret.add(res.nodes.get(i));
		return ret;
	}

	/**
	 * 到達可能な範囲に含まれる、いずれかの閉路を構成する頂点列を返す。
	 *
	 * @param start 開始頂点
	 * @return 頂点列のリスト。閉路が存在しない場合は null。
	 *
	 * <p>計算量: 到達可能な頂点数を $V$, 辺数を $M$ として $O(V + M)$。</p>
	 */
	public List<V> findCycleVertices(V start) {
		var res = toExplicit(start);
		int[] cycle = res.g.findCycleVertices();
		if (cycle == null) return null;
		List<V> ret = new ArrayList<>();
		for (int i : cycle) ret.add(res.nodes.get(i));
		return ret;
	}
}
