package library.util;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import library.util.graph.Edge;

/**
 * a+b=min(a,b)
 * a*b=max(a,b)
 */
public class MinMax {

	/**
	 * Min-Max半環上の全点対最短路を計算します。
	 * 頂点数 N が閾値（50）未満の場合は O(N^3) の Floyd-Warshall、
	 * それ以上の場合は O(N^3 / 64 + M log M) の bitset-based アルゴリズムへ自動的に分岐します。
	 *
	 * // 未テスト
	 * @param N 頂点数
	 * @param edges 辺のリスト
	 * @param unreachableValue 到達不能な場合の初期値・返り値
	 * @return 全点対の最小最大辺重みを格納した二次元配列
	 */
	public static long[][] asps(long[][] cost) {
		//https://atcoder.jp/contests/abc287/submissions/77715053
		int N=cost.length;
		if (N <= 0) return new long[0][0];
		if (N < 50) {
			return warshalFloyd(cost);
		}
		return aspsBitset(cost);
	}

	private static long[][] aspsBitset(long[][] cost) {
		int N=cost.length;
		long[][] ans = new long[N][N];
		long unreachableValue = Long.MAX_VALUE;
		for (int i = 0; i < N; i++) {
			Arrays.fill(ans[i], unreachableValue);
			ans[i][i] = 0;
		}
		List<Edge> sortedEdges = new ArrayList<>();
		for (int u = 0; u < N; u++) {
		    for (int v = 0; v < N; v++) {
		        if (u != v && cost[u][v] != Long.MAX_VALUE) {
		            sortedEdges.add(new Edge(u, v, cost[u][v]));
		        }
		    }
		}
		sortedEdges.sort((e1, e2) -> Long.compare(e1.cost, e2.cost));

		int words = (N + 63) / 64;
		long[][] reach = new long[N][words];
		long[][] invReach = new long[N][words];
		for (int i = 0; i < N; i++) {
			reach[i][i >>> 6] |= 1L << (i & 63);
			invReach[i][i >>> 6] |= 1L << (i & 63);
		}

		for (Edge e : sortedEdges) {
			//reach[u]=処理済みの辺で、uから到達可能な頂点集合
			//inv_reach[u]=処理済みの辺で、uに到達可能な頂点集合
			int u = e.src;
			int v = e.dst;
			long w = e.cost;

			if (u < 0 || u >= N || v < 0 || v >= N) throw new AssertionError();

			if ((reach[u][v >>> 6] & (1L << (v & 63))) != 0) {//すでに辺uvがあるなら何もしない
				continue;
			}

			long[] target_x = new long[words];
			boolean hasTarget = false;
			for (int i = 0; i < words; i++) {
				target_x[i] = invReach[u][i] & ~invReach[v][i];//uに到達可能かつ、vに到達不可能な頂点集合
				if (target_x[i] != 0) hasTarget = true;
			}
			if (!hasTarget) continue;

			long[] reachV = reach[v].clone();

			for (int i = 0; i < words; i++) {
				long txWord = target_x[i];
				if (txWord == 0) continue;
				int baseX = i << 6;
				while (txWord != 0) {
					int tzX = Long.numberOfTrailingZeros(txWord);
					int x = baseX + tzX;//uに到達可能かつ、vに到達不可能な頂点x。
					txWord &= txWord - 1;//xのビットを消す

					long[] reachX = reach[x];
					for (int j = 0; j < words; j++) {
						long newReachWord = reachV[j] & ~reachX[j];//vから到達可能かつxから到達不可能な頂点集合
						reachX[j] |= newReachWord;

						if (newReachWord != 0) {
							int baseY = j << 6;
							while (newReachWord != 0) {
								int tzY = Long.numberOfTrailingZeros(newReachWord);
								int y = baseY + tzY;
								newReachWord &= newReachWord - 1;

								ans[x][y] = w;
								invReach[y][x >>> 6] |= 1L << (x & 63);
							}
						}
					}
				}
			}
		}
		return ans;
	}

	
	/**
	 * https://atcoder.jp/contests/abc257/submissions/73364536
	 * @param a
	 * @return
	 */
	public static long[][] warshalFloyd(long[][] a) {
		long[][]b=ArrayUtils.copy(a);
		int n=a.length;
		for (int mid = 0; mid < n; mid++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					b[i][j]=Math.min(b[i][j], Math.max(b[i][mid], b[mid][j]));
				}
			}
		}
		return b;
	}
}