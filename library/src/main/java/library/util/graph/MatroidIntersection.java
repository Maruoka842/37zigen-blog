package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.collections.IntArrayList;
import library.util.collections.IntQueue;
import library.util.collections.MyPriorityQueue;

/**
 * 重み付き・重みなしマトロイド交差問題を解くクラス。
 */
public class MatroidIntersection {

	private MatroidIntersection() {}

	/**
	 * 重みなしマトロイド交差問題を解き、最大共通独立集合を返す。
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @return 最大共通独立集合を表すビット配列
	 */
	public static boolean[] solve(Matroid m1, Matroid m2) {
		int n = m1.size();
		boolean[] I = new boolean[n];

		// Greedy initialization
		m1.set(I);
		m2.set(I);
		for (int e = 0; e < n; e++) {
			if (m1.circuit(e).isEmpty() && m2.circuit(e).isEmpty()) {
				I[e] = true;
				m1.set(I);
				m2.set(I);
			}
		}

		while (augment(m1, m2, I, null)) ;
		return I;
	}

	/**
	 * 重み付きマトロイド交差問題を解き、最大サイズの共通独立集合のうち、重みの総和が最小のものを返す。
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @param weights 各要素の重み
	 * @return 共通独立集合を表すビット配列
	 */
	public static boolean[] solve(Matroid m1, Matroid m2, long[] weights) {
		int n = m1.size();
		boolean[] I = new boolean[n];
		// 通常、重み付きマトロイド交差は空集合から始める
		while (augment(m1, m2, I, weights)) ;
		return I;
	}

	/**
	 * 重み付きマトロイド交差問題を解き、最大サイズの共通独立集合のうち、重みの総和が最大となるものを返す。
	 * Dijkstra法を用いてポテンシャルを維持することで、負辺のないグラフでの最短路探索を行う。
	 * 未テスト
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @param weights 各要素の重み
	 * @return 共通独立集合を表すビット配列
	 */
	public static boolean[] solveMaxWeight(Matroid m1, Matroid m2, long[] weights) {
		int n = m1.size();
		boolean[] I = new boolean[n];
		long[] potential = new long[n + 2];
		while (augmentDijkstra(m1, m2, I, weights, potential)) ;
		return I;
	}

	private static boolean augment(Matroid m1, Matroid m2, boolean[] I, long[] weights) {
		int n = m1.size();
		int gs = n;
		int gt = n + 1;
		LongValueDigraph g = new LongValueDigraph(n + 2);

		m1.set(I);
		m2.set(I);

		for (int e = 0; e < n; e++) {
			if (I[e]) continue;

			// M1: e -> f edges
			IntArrayList c1 = m1.circuit(e);
			if (c1.isEmpty()) {
				g.addEdge(e, gt, 0);
			} else {
				for (int i = 0; i < c1.size(); i++) {
					int f = c1.get(i);
					if (f != e) {
						// f is in I, e is not in I.
						// We remove f and add e to keep independence in M1.
						long cost = weights == null ? 1 : -weights[f] * (n + 1) + 1;
						g.addEdge(e, f, cost);
					}
				}
			}

			// M2: f -> e edges (including gs -> e)
			IntArrayList c2 = m2.circuit(e);
			if (c2.isEmpty()) {
				long cost = weights == null ? 1 : weights[e] * (n + 1) + 1;
				g.addEdge(gs, e, cost);
			} else {
				for (int i = 0; i < c2.size(); i++) {
					int f = c2.get(i);
					if (f != e) {
						// f is in I, e is not in I.
						// We remove f and add e to keep independence in M2.
						long cost = weights == null ? 1 : weights[e] * (n + 1) + 1;
						g.addEdge(f, e, cost);
					}
				}
			}
		}

		LongValueDigraph.DijkstraResult res = g.spfa(gs);
		if (res == null || res.dist()[gt] >= Long.MAX_VALUE / 3) {
			return false;
		}

		ArrayList<Integer> path = g.restoreShortestPath(gt, res.parent());
		for (int v : path) {
			if (v < n) {
				I[v] = !I[v];
			}
		}
		return true;
	}

	/**
	 * Dijkstra法を用いて重み付きマトロイド交差の増加道を探索する。
	 * 未テスト
	 * @param m1 マトロイド1
	 * @param m2 マトロイド2
	 * @param I 現在の独立集合
	 * @param weight 各要素の重み
	 * @param potential ポテンシャル
	 * @return 増加道が見つかった場合 true
	 */
	public static boolean augmentDijkstra(Matroid m1, Matroid m2, boolean[] I, long[] weight, long[] potential) {
		int n = I.length;
		m1.set(I);
		m2.set(I);

		// Trivial addition
		int maxElem = -1;
		for (int e = 0; e < n; e++) {
			if (!I[e]) {
				if (maxElem < 0 || weight[e] > weight[maxElem]) maxElem = e;
			}
		}
		if (maxElem >= 0) {
			boolean canAddDirectly = false;
			for (int e = 0; e < n; e++) {
				if (!I[e] && weight[e] == weight[maxElem] && m1.circuit(e).isEmpty() && m2.circuit(e).isEmpty()) {
					potential[e] -= (I[e] ? weight[e] : -weight[e]);
					I[e] = true;
					canAddDirectly = true;
					break;
				}
			}
			if (canAddDirectly) return true;
		}

		int gs = n, gt = n + 1;
		IntArrayList[] to = new IntArrayList[gt + 1];
		for (int i = 0; i <= gt; i++) to[i] = new IntArrayList();

		boolean hasGsEdge = false, hasGtEdge = false;

		for (int e = 0; e < n; e++) {
			if (I[e]) continue;

			IntArrayList c1 = m1.circuit(e);
			if (c1.isEmpty()) {
				to[e].add(gt);
				if (!hasGtEdge) {
					hasGtEdge = true;
					potential[gt] = potential[e];
				}
				long el = -potential[gt] + potential[e]; // l(gt) = 0
				if (el < 0) potential[gt] += el;
			}
			for (int i = 0; i < c1.size(); i++) {
				int f = c1.get(i);
				if (f != e) to[e].add(f);
			}

			IntArrayList c2 = m2.circuit(e);
			if (c2.isEmpty()) {
				to[gs].add(e);
				if (!hasGsEdge) {
					hasGsEdge = true;
					potential[gs] = potential[e] - (-weight[e]); // l(e) = -weight[e]
				}
				long el = -weight[e] - potential[e] + potential[gs];
				if (el < 0) potential[gs] -= el;
			}
			for (int i = 0; i < c2.size(); i++) {
				int f = c2.get(i);
				if (f != e) to[f].add(e);
			}
		}

		long e0 = potential[gs];
		if (e0 != 0) {
			for (int i = 0; i <= gt; i++) potential[i] -= e0;
		}

		if (!hasGsEdge || !hasGtEdge) return false;

		boolean[] potentialFixed = new boolean[gt + 1];
		long[] dijkstra = new long[gt + 1];
		int[] prv = new int[gt + 1];
		Arrays.fill(prv, -1);

		MyPriorityQueue<long[]> pq = new MyPriorityQueue<>();
		pq.add(new long[]{0, gs});

		DijkstraContext ctx = new DijkstraContext(gt, I, weight, potential, potentialFixed, to, prv, dijkstra);

		while (!pq.isEmpty()) {
			long[] state = pq.poll();
			int e = (int) state[1];
			if (potentialFixed[e]) continue;
			if (e != gs) ctx.potential_add_unfixed_es = ctx.edgeLen(prv[e], e);

			if (ctx.rec(e)) break;

			for (int i = 0; i < ctx.pushCandsNxt.size(); i++) {
				int nxt = ctx.pushCandsNxt.get(i);
				int now = ctx.pushCandsNow.get(i);
				if (prv[nxt] == now) pq.add(new long[]{dijkstra[nxt], nxt});
			}
			ctx.pushCandsNxt.clear();
			ctx.pushCandsNow.clear();
		}

		for (int e = 0; e <= gt; e++) {
			if (!potentialFixed[e]) ctx.fixPotential(e);
		}

		if (prv[gt] < 0) return false;

		Arrays.fill(prv, -1);
		IntQueue q = new IntQueue();
		q.add(gs);
		while (!q.isEmpty()) {
			int now = q.poll();
			if (now == gt) break;
			for (int i = 0; i < to[now].size(); i++) {
				int nxt = to[now].get(i);
				if (prv[nxt] == -1 && ctx.edgeLen(now, nxt) == 0) {
					prv[nxt] = now;
					q.add(nxt);
				}
			}
		}

		if (prv[gt] == -1) return false;

		for (int e = prv[gt]; e != gs; e = prv[e]) {
			potential[e] -= (I[e] ? weight[e] : -weight[e]);
			I[e] = !I[e];
		}

		return true;
	}

	private static class DijkstraContext {
		int gt;
		boolean[] I;
		long[] weight;
		long[] potential;
		boolean[] potentialFixed;
		IntArrayList[] to;
		int[] prv;
		long[] dijkstra;
		long potential_add_unfixed_es = 0;
		IntArrayList pushCandsNxt = new IntArrayList();
		IntArrayList pushCandsNow = new IntArrayList();

		DijkstraContext(int gt, boolean[] I, long[] weight, long[] potential, boolean[] potentialFixed, IntArrayList[] to, int[] prv, long[] dijkstra) {
			this.gt = gt;
			this.I = I;
			this.weight = weight;
			this.potential = potential;
			this.potentialFixed = potentialFixed;
			this.to = to;
			this.prv = prv;
			this.dijkstra = dijkstra;
		}

		long l(int e) {
			if (e >= I.length) return 0;
			return I[e] ? weight[e] : -weight[e];
		}

		long edgeLen(int s, int t) {
			return l(t) - potential[t] + potential[s];
		}

		void fixPotential(int e) {
			potentialFixed[e] = true;
			potential[e] += potential_add_unfixed_es;
		}

		boolean rec(int cur) {
			if (cur == gt) return true;
			fixPotential(cur);
			for (int i = 0; i < to[cur].size(); i++) {
				int nxt = to[cur].get(i);
				if (potentialFixed[nxt]) continue;
				long len = edgeLen(cur, nxt) - potential_add_unfixed_es;
				if (len == 0) {
					prv[nxt] = cur;
					if (rec(nxt)) return true;
				} else {
					if (prv[nxt] == -1 || potential_add_unfixed_es + len < dijkstra[nxt]) {
						dijkstra[nxt] = potential_add_unfixed_es + len;
						prv[nxt] = cur;
						pushCandsNxt.add(nxt);
						pushCandsNow.add(cur);
					}
				}
			}
			return false;
		}
	}
}
