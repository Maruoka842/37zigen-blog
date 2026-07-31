package library.util.graph;

import java.util.ArrayList;

/**
 * 完全グラフの b-matching を構成する関数群。
 */
public final class CompleteGraphBMatching {
	private CompleteGraphBMatching() {
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code b != null} かつ {@code b.length >= 1} かつ任意の {@code i} について {@code b[i] >= 0}。</li>
	 * <li>事後条件: 返り値 {@code e} の各要素 {@code e[k]} は {@code 0 <= e[k].u() < e[k].v() < b.length} かつ {@code e[k].count() > 0}。</li>
	 * <li>事後条件: 任意の {@code k != l} について {@code e[k].u() != e[l].u() || e[k].v() != e[l].v()}。</li>
	 * <li>事後条件: 各 {@code e[k]} は辺 {@code (e[k].u(), e[k].v())} を {@code e[k].count()} 回使うことを表す。</li>
	 * <li>事後条件: {@code deg_i(e) <= b[i]}。</li>
	 * <li>事後条件: {@code sum_k e[k].count() = max |F|} subject to {@code F} は {@code E(K_N)} 上の多重集合、かつ任意の {@code i} について {@code deg_i(F) <= b[i]}。</li>
	 * <li>計算量: {@code N = b.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param b 各頂点の次数上限
	 * @return 最大濃度の b-matching で使った辺と使用回数の列
	 */
	// 未テスト。計算量: N = b.length として時間 O(N)、追加空間 O(N)。
	public static Edge[] maximumCardinalityCompressed(int[] b) {
		int n = b.length;
		long sum = 0;
		int maxIndex = 0;
		for (int i = 0; i < n; i++) {
			sum += b[i];
			if (b[i] > b[maxIndex]) maxIndex = i;
		}
		ArrayList<Edge> edges = new ArrayList<>();
		if (sum == 0) return new Edge[0];
		long others = sum - b[maxIndex];
		if (b[maxIndex] >= others) {
			for (int i = 0; i < n; i++) {
				if (i != maxIndex && b[i] > 0) addEdge(edges, maxIndex, i, b[i]);
			}
			return edges.toArray(new Edge[edges.size()]);
		}
		long leftEnd = sum / 2;
		long offset = (sum + 1) / 2;
		int i = 0;
		int j = 0;
		long left = 0;
		long right = 0;
		while (i < n && b[i] == 0) i++;
		while (j < n && right + b[j] <= offset) right += b[j++];
		long p = 0;
		long q = offset;
		while (p < leftEnd) {
			while (i < n && left + b[i] <= p) left += b[i++];
			while (j < n && right + b[j] <= q) right += b[j++];
			long nextP = Math.min(left + b[i], leftEnd);
			long nextQ = right + b[j];
			long count = Math.min(nextP - p, nextQ - q);
			addEdge(edges, i, j, count);
			p += count;
			q += count;
		}
		return edges.toArray(new Edge[edges.size()]);
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code edges != null} かつ {@code count > 0}。</li>
	 * <li>事後条件: {@code edges} の末尾に {@code (min(u,v), max(u,v), count)} を追加する。ただし直前要素が同じ辺なら使用回数に加算する。</li>
	 * <li>副作用: {@code edges} を変更する。</li>
	 * <li>破壊的変更: {@code edges} を変更する。</li>
	 * <li>参照共有・所有権: 追加した {@link Edge} の所有権は {@code edges} へ移る。</li>
	 * <li>例外・未定義条件: 事前条件違反時の動作は未定義。</li>
	 * <li>計算量: 時間 {@code O(1)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param edges 追加先
	 * @param u 端点
	 * @param v 端点
	 * @param count 使用回数
	 */
	// 未テスト。計算量: 時間 O(1)、追加空間 O(1)。
	private static void addEdge(ArrayList<Edge> edges, int u, int v, long count) {
		if (u > v) {
			int tmp = u;
			u = v;
			v = tmp;
		}
		edges.add(new Edge(u, v, count));
	}

	/**
	 * {@code K_N} の辺 {@code (u,v)} を {@code count} 回使うことを表す値。
	 *
	 * @param u 小さい方の端点
	 * @param v 大きい方の端点
	 * @param count 使用回数
	 */
	public static record Edge(int u, int v, long count) {}
}
