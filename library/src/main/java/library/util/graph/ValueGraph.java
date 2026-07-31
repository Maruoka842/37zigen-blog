package library.util.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * 辺の重みを型 T で持つ無向グラフを表すクラスです。
 * @param <T> 辺の重みの型
 */
public class ValueGraph<T> {
	/** 頂点数 */
	public int N;
	/** 辺数 */
	public int M;
	/** 隣接リスト */
	public ArrayList<ValueEdge<T>>[] adj;

	/**
	 * 頂点数 N の無向グラフを構築します。
	 * @param N 頂点数
	 * @complexity O(N)
	 */
	// 未テスト
	@SuppressWarnings("unchecked")
	public ValueGraph(int N) {
		this.N = N;
		this.adj = new ArrayList[N];
		for (int i = 0; i < N; i++) {
			adj[i] = new ArrayList<>();
		}
	}

	/**
	 * 無向辺を追加します。
	 * @param from 一方の端点
	 * @param to もう一方の端点
	 * @param weight 辺の重み
	 * @complexity O(1)
	 */
	// 未テスト
	public void addEdge(int from, int to, T weight) {
		if (from < 0 || from >= N || to < 0 || to >= N) {
			throw new IndexOutOfBoundsException();
		}
		adj[from].add(new ValueEdge<>(from, to, weight));
		if (from != to) {
			adj[to].add(new ValueEdge<>(to, from, weight));
		}
		M++;
	}

	/**
	 * e.src <= e.dst を満たす全ての辺のリストを返します。
	 * @return 辺のリスト
	 * @complexity O(N + M)
	 */
	// 未テスト
	public List<ValueEdge<T>> edges() {
		List<ValueEdge<T>> edges = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			for (ValueEdge<T> e : adj[i]) {
				if (e.dst() >= i) {
					edges.add(e);
				}
			}
		}
		return edges;
	}
}
