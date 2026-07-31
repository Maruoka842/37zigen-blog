package library.util.graph;

import java.util.Arrays;

/**
 * 重み付き無向グラフを隣接行列で表現し、各種アルゴリズムを提供するクラス。
 *
 * <p>数学的仕様:
 * 頂点集合を $V = \{0, 1, \dots, N-1\}$ とする。
 * 隣接行列 $A$ の各要素 $A[i][j]$ は頂点 $i$ と頂点 $j$ の間の辺の重みを表す。
 * 辺が存在しない場合は $A[i][j] \ge \text{INF}$ とする。
 */
public class UndirectedValuedAdjacencyMatrix {
	/**
	 * 辺が存在しない（無限大）ことを表す定数。
	 */
	public static final long INF = Long.MAX_VALUE / 3;

	/**
	 * 隣接行列 $A$ において、頂点 $r$ を含む最小重み単純サイクルのコストを求める。
	 * 存在しない場合は INF を返す。
	 * <ul>
	 *   <li>事前条件: $0 \le r < A.length$</li>
	 *   <li>計算量: $O(N^2)$</li>
	 * </ul>
	 * @param A 隣接行列
	 * @param r 対象の頂点
	 * @return 最小重み単純サイクルのコスト
	 */
	public static long findMinWeightCycleCostAt(long[][] A, int r) {
		//https://atcoder.jp/contests/abc308/submissions/77311178
		// 未テスト
		int N = A.length;
		long[] dist = new long[N];
		int[] root = new int[N];
		boolean[] used = new boolean[N];

		Arrays.fill(dist, INF);
		Arrays.fill(root, -1);

		used[r] = true;

		for (int v = 0; v < N; v++) {
			if (v == r) continue;
			if (A[r][v] >= INF) continue;

			dist[v] = A[r][v];
			root[v] = v;
		}

		long best = INF;

		for (int it = 0; it < N; it++) {
			int u = -1;
			long du = INF;

			for (int v = 0; v < N; v++) {
				if (!used[v] && dist[v] < du) {
					du = dist[v];
					u = v;
				}
			}

			if (u == -1) break;

			used[u] = true;

			for (int v = 0; v < N; v++) {
				if (v == r) continue;
				if (A[u][v] >= INF) continue;

				if (dist[v] < INF && root[u] != root[v]) {
					long cand = dist[u] + A[u][v] + dist[v];
					if (cand < best) best = cand;
				}

				if (!used[v]) {
					long nd = dist[u] + A[u][v];
					if (nd < dist[v]) {
						dist[v] = nd;
						root[v] = root[u];
					}
				}
			}
		}

		return best;
	}

	/**
	 * 隣接行列 $A$ において、始点 $src$ から全頂点への最短経路長を $O(N^2)$ Dijkstra法で求める。
	 * <ul>
	 *   <li>事前条件: すべての辺の重みは非負（$A[i][j] \ge 0$、ただし自己ループまたは存在しない辺を除く）であり、$0 \le src < A.length$。</li>
	 *   <li>計算量: $O(N^2)$</li>
	 * </ul>
	 * @param A 隣接行列
	 * @param src 始点
	 * @return 各頂点への最短経路長を格納した配列
	 */
	public static long[] dijkstra(long[][] A, int src) {
		// 未テスト
		int N = A.length;
		long[] dist = new long[N];
		Arrays.fill(dist, INF);
		dist[src] = 0;
		boolean[] used = new boolean[N];
		for (int it = 0; it < N; it++) {
			int u = -1;
			long du = INF;
			for (int v = 0; v < N; v++) {
				if (!used[v] && dist[v] < du) {
					du = dist[v];
					u = v;
				}
			}
			if (u == -1) break;
			used[u] = true;
			for (int v = 0; v < N; v++) {
				if (A[u][v] < INF) {
					long nd = dist[u] + A[u][v];
					if (nd < dist[v]) {
						dist[v] = nd;
					}
				}
			}
		}
		return dist;
	}
}
