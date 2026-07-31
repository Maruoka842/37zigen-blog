package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stoer-Wagner アルゴリズムを用いて無向グラフの最小カットを求めるクラス。
 * 辺の重みがすべて非負である必要がある。
 */
public class StoerWagner {

	/**
	 * 最小カットの結果を保持するレコード。
	 * @param value 最小カットの値
	 * @param side 最小カットによって分割された一方の集合に属するかどうかを示す配列
	 */
	public record Result(long value, boolean[] side) {}

	/**
	 * 無向グラフの最小カットの値を求める。
	 * <h3>契約</h3>
	 * <ul>
	 *   <li>事前条件:
	 *     <ul>
	 *       <li>{@code g} は無向グラフである。</li>
	 *       <li>任意の辺 $e \in E$ について、その重み $w(e) \ge 0$ である。</li>
	 *       <li>頂点数 $N = |V| \ge 2$ である。</li>
	 *     </ul>
	 *   </li>
	 *   <li>事後条件:
	 *     <ul>
	 *       <li>グラフ $G=(V, E)$ を空でない2つの集合 $S, V \setminus S$ に分割したとき、
	 *           切断される辺の重みの総和 $\sum_{u \in S, v \in V \setminus S} w(u, v)$ の最小値を返す。</li>
	 *     </ul>
	 *   </li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N^3)$。</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: 入力グラフ {@code g} は変更されない。</li>
	 * </ul>
	 *
	 * @param g 無向グラフ
	 * @return 最小カットの値
	 */
	public static long minCutValue(LongValueGraph g) {
		if (g.N < 2) return 0;
		return minCut(g).value();
	}

	/**
	 * 無向グラフの最小カットとその復元を行う。
	 * 未テスト
	 * <h3>契約</h3>
	 * <ul>
	 *   <li>事前条件:
	 *     <ul>
	 *       <li>{@code g} は無向グラフである。</li>
	 *       <li>任意の辺 $e \in E$ について、その重み $w(e) \ge 0$ である。</li>
	 *       <li>頂点数 $N = |V| \ge 2$ である。</li>
	 *     </ul>
	 *   </li>
	 *   <li>事後条件:
	 *     <ul>
	 *       <li>最小カットの値とその分割を返す。</li>
	 *     </ul>
	 *   </li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N^3)$。</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: 入力グラフ {@code g} は変更されない。</li>
	 * </ul>
	 *
	 * @param g 無向グラフ
	 * @return 最小カットの結果
	 */
	public static Result minCut(LongValueGraph g) {
		int n = g.N;
		if (n < 2) return new Result(0, new boolean[n]);

		long[][] adj = new long[n][n];
		for (int i = 0; i < n; i++) {
			for (Edge e : g.adj[i]) {
				if (e.cost < 0) throw new IllegalArgumentException("Edge weight must be non-negative");
				if (i < e.dst) {
					adj[i][e.dst] += e.cost;
					adj[e.dst][i] += e.cost;
				}
			}
		}

		int[] v = new int[n];
		for (int i = 0; i < n; i++) v[i] = i;

		List<Integer>[] groups = new List[n];
		for (int i = 0; i < n; i++) {
			groups[i] = new ArrayList<>();
			groups[i].add(i);
		}

		long minCut = Long.MAX_VALUE;
		boolean[] bestSide = new boolean[n];
		int currentN = n;

		long[] ws = new long[n];
		boolean[] added = new boolean[n];

		while (currentN > 1) {
			Arrays.fill(ws, 0, currentN, 0);
			Arrays.fill(added, 0, currentN, false);
			int last = -1;
			int next = -1;

			for (int i = 0; i < currentN; i++) {
				next = -1;
				for (int j = 0; j < currentN; j++) {
					if (!added[j] && (next == -1 || ws[j] > ws[next])) {
						next = j;
					}
				}

				added[next] = true;
				if (i == currentN - 1) {
					if (ws[next] < minCut) {
						minCut = ws[next];
						Arrays.fill(bestSide, false);
						for (int node : groups[v[next]]) {
							bestSide[node] = true;
						}
					}
					// Merge v[next] into v[last]
					int t = v[next];
					int s = v[last];
					for (int j = 0; j < currentN; j++) {
						if (j != last && j != next) {
							int u = v[j];
							adj[s][u] += adj[t][u];
							adj[u][s] += adj[t][u];
						}
					}
					groups[s].addAll(groups[t]);
					v[next] = v[currentN - 1];
				} else {
					for (int j = 0; j < currentN; j++) {
						if (!added[j]) {
							ws[j] += adj[v[next]][v[j]];
						}
					}
					last = next;
				}
			}
			currentN--;
		}

		return new Result(minCut, bestSide);
	}
}
