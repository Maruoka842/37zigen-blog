package library.util.graph.tree;

import java.util.ArrayList;
import java.util.List;

import library.util.algebra.strategy.longs.LongAbelianGroupStrategy;
import library.util.segtree.LongAbelianGroupBinaryIndexedTree;

/**
 * 根付き木において、各頂点に付随する点集合に対して、部分木内のクエリをオフラインで処理するためのスイープライン構造。
 * 頂点 v に (key, value) の組をいくつか追加し、オイラーツアー順に状態を進めることで、
 * 部分木内のキーが [l, r) の範囲にある値の和を求める。
 */
public class StaticOfflineSubtreeSum {
	/** 対象の根付き木。 */
	private final Tree tree;
	/** 値の加法・減法に使うアーベル群。 */
	private final LongAbelianGroupStrategy strategy;
	/** points[v] := 頂点 v に付随する点列。 */
	private final List<Point>[] points;
	/** queries[i] := i 番目に登録されたクエリ。 */
	private final List<Query> queries = new ArrayList<>();
	/** keySize >= 0 かつ許されるキー範囲は [0, keySize)。 */
	private final int keySize;
	/** bit[k] := 現在の preorder prefix に含まれるキー k の値の総和。 */
	private final LongAbelianGroupBinaryIndexedTree bit;
	/** preOrder[i] := 根からの DFS preorder で i 番目の頂点。 */
	private final int[] preOrder;

	/**
	 * 各頂点に付随する点を表す。
	 * @param key キー (0 以上 keySize 未満)
	 * @param value 値
	 */
	public record Point(int key, long value) {}

	/**
	 * 部分木クエリを表す。
	 * @param v 部分木根
	 * @param l キー下限 (inclusive)
	 * @param r キー上限 (exclusive)
	 */
	private record Query(int v, int l, int r) {}

	/**
	 * preorder prefix に置くクエリイベントを表す。
	 * @param queryId クエリ番号
	 * @param inverse true なら現在 prefix 和の逆元を掛ける
	 */
	private record Event(int queryId, boolean inverse) {}

	/**
	 * 契約: points[v] = 空列 (0 <= v < N), queries = 空列。
	 * 事前条件: tree は根付き木, keySize >= 0。
	 * 計算量: O(N)。未テスト
	 * @param tree 木
	 * @param strategy アーベル群の戦略
	 * @param keySize キーの最大範囲 [0, keySize)
	 */
	@SuppressWarnings("unchecked")
	public StaticOfflineSubtreeSum(Tree tree, LongAbelianGroupStrategy strategy, int keySize) {
		if (!tree.isRooted()) throw new AssertionError();
		if (keySize < 0) throw new AssertionError();
		this.tree = tree;
		this.strategy = strategy;
		this.keySize = keySize;
		this.points = new List[tree.N];
		for (int i = 0; i < tree.N; i++) {
			points[i] = new ArrayList<>();
		}
		bit = new LongAbelianGroupBinaryIndexedTree(keySize, strategy);
		preOrder = tree.preOrder();
	}

	/**
	 * 契約: points[v] := points[v] ⧺ [(key, value)]。
	 * 事前条件: 0 <= v < N, 0 <= key < keySize, solve 未呼び出し。
	 * 計算量: O(1)。未テスト
	 * @param v 頂点番号
	 * @param key 点のキー
	 * @param value 点の値
	 */
	public void addPoint(int v, int key, long value) {
		if (key < 0 || key >= keySize) throw new AssertionError();
		points[v].add(new Point(key, value));
	}

	/**
	 * 契約: queries := queries ⧺ [(v, l, r)], 戻り値 = 追加前の |queries|。
	 * 事前条件: 0 <= v < N, 0 <= l <= r <= keySize, solve 未呼び出し。
	 * 計算量: O(1)。未テスト
	 * @param v 部分木根
	 * @param l キー下限 (inclusive)
	 * @param r キー上限 (exclusive)
	 * @return クエリ番号
	 */
	public int addQuery(int v, int l, int r) {
		if (l < 0 || l > r || r > keySize) throw new AssertionError();
		int id = queries.size();
		queries.add(new Query(v, l, r));
		return id;
	}

	/**
	 * 契約: 戻り値[i] = Σ value(p), p ∈ points[u], u ∈ subtree(queries[i].v), queries[i].l <= key(p) < queries[i].r。
	 * 計算量: O((N + P + Q) log keySize)。P = Σ|points[v]|, Q = |queries|。未テスト
	 * @return クエリ回答列
	 */
	@SuppressWarnings("unchecked")
	public long[] solve() {
		List<Event>[] events = new List[tree.N + 1];
		for (int i = 0; i <= tree.N; i++) events[i] = new ArrayList<>();
		for (int i = 0; i < queries.size(); i++) {
			Query q = queries.get(i);
			int in = tree.preOrderOf(q.v());
			int out = in + tree.size(q.v());
			events[in].add(new Event(i, true));
			events[out].add(new Event(i, false));
		}
		long[] ans = new long[queries.size()];
		for (int i = 0; i < ans.length; i++) ans[i] = strategy.identity();
		bit.clear();
		for (int i = 0; i <= tree.N; i++) {
			for (Event e : events[i]) applyEvent(ans, e);
			if (i < tree.N) addVertex(preOrder[i]);
		}
		return ans;
	}

	/**
	 * 契約: ans[e.queryId] := ans[e.queryId] + (e.inverse ? -sum(l,r) : sum(l,r))。
	 * 計算量: O(log keySize)。未テスト
	 * @param ans 回答列
	 * @param e イベント
	 */
	private void applyEvent(long[] ans, Event e) {
		Query q = queries.get(e.queryId());
		long value = sum(q.l(), q.r());
		ans[e.queryId()] = strategy.mul(ans[e.queryId()], e.inverse() ? strategy.inverse(value) : value);
	}

	/**
	 * 契約: bit[k] := bit[k] + Σ value(p), p ∈ points[v], key(p) = k。
	 * 計算量: O(|points[v]| log keySize)。未テスト
	 * @param v 追加する頂点
	 */
	private void addVertex(int v) {
		for (Point p : points[v]) bit.add(p.key(), p.value());
	}

	/**
	 * 内部で利用している Binary Indexed Tree を取得する。
	 * 契約: 戻り値 = 現在の preorder prefix 状態を表す BIT。
	 * 計算量: O(1)。未テスト
	 * @return BIT
	 */
	public LongAbelianGroupBinaryIndexedTree getBIT() {
		return bit;
	}

	/**
	 * 指定された範囲の和を、現在の prefix に対して計算して返す。
	 * 契約: 戻り値 = Σ value(p), p ∈ points[u], u は現在の prefix 内, l <= key(p) < r。
	 * 事前条件: 0 <= l <= r <= keySize。
	 * 計算量: O(log keySize)。未テスト
	 * @param l キーの下限（inclusive）
	 * @param r キーの上限（exclusive）
	 * @return prefix 内の和
	 */
	private long sum(int l, int r) {
		return bit.fold(l, r);
	}
}
