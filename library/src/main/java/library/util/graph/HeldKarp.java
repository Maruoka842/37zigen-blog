package library.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import library.util.collections.IntArrayList;

/**
 * Held-Karp下界を計算するクラス。
 * TSP（巡回セールスマン問題）の良質な下界を与える。
 *
 * <p>
 * {@link #heldKarpLowerBound(DistanceMatrix)} を用いることで、与えられた距離行列に対する
 * TSPのHeld-Karp下界を計算できる。
 * </p>
 *
 * <p>
 * 参考文献:
 * <a href="http://webhotel4.ruc.dk/~keld/research/LKH/LKH-2.0/DOC/LKH_REPORT.pdf">
 * K. Helsgaun, "An Effective Implementation of the Lin-Kernighan Traveling Salesman Heuristic", 2000.
 * </a>
 * </p>
 *
 * 未テスト
 */
public class HeldKarp {

	public record Adjacent(int to, long weight) {}

	/**
	 * 距離行列のインターフェース。
	 */
	public interface DistanceMatrix {
		/**
		 * 頂点数を返す。
		 */
		int n();

		/**
		 * 頂点 i と j の間の距離を返す。
		 */
		long dist(int i, int j);

		/**
		 * 頂点 i に隣接する頂点とその距離のリストを返す。
		 */
		Iterable<Adjacent> adjacents(int i);
	}

	/**
	 * 隣接行列による距離行列の実装。
	 */
	public static class DenseDistanceMatrix implements DistanceMatrix {
		private final int n;
		private final long[][] d;

		public DenseDistanceMatrix(long[][] d) {
			this.n = d.length;
			this.d = d;
		}

		@Override public int n() { return n; }
		@Override public long dist(int i, int j) { return d[i][j]; }
		@Override
		public Iterable<Adjacent> adjacents(int i) {
			return () -> new Iterator<>() {
				int j = 0;
				@Override public boolean hasNext() { return j < n; }
				@Override
				public Adjacent next() {
					Adjacent res = new Adjacent(j, d[i][j]);
					j++;
					return res;
				}
			};
		}
	}

	/**
	 * CSR形式による距離行列の実装。
	 */
	public static class CSRDistanceMatrix implements DistanceMatrix {
		private final int n;
		private final int[] begins;
		private final int[] tos;
		private final long[] weights;

		public CSRDistanceMatrix(int n, List<Edge> edges) {
			this.n = n;
			this.begins = new int[n + 1];
			int[] degs = new int[n];
			for (Edge e : edges) degs[e.src]++;
			for (int i = 0; i < n; i++) begins[i + 1] = begins[i] + degs[i];
			this.tos = new int[edges.size()];
			this.weights = new long[edges.size()];
			int[] cur = Arrays.copyOf(begins, n + 1);
			for (Edge e : edges) {
				int p = cur[e.src]++;
				tos[p] = e.dst;
				weights[p] = e.cost;
			}
		}

		@Override public int n() { return n; }
		@Override
		public long dist(int i, int j) {
			for (int k = begins[i]; k < begins[i + 1]; k++) {
				if (tos[k] == j) return weights[k];
			}
			return Long.MAX_VALUE / 3;
		}
		@Override
		public Iterable<Adjacent> adjacents(int i) {
			return () -> new Iterator<>() {
				int k = begins[i];
				@Override public boolean hasNext() { return k < begins[i + 1]; }
				@Override
				public Adjacent next() {
					Adjacent res = new Adjacent(tos[k], weights[k]);
					k++;
					return res;
				}
			};
		}
	}

	/**
	 * 最小全域木の辺を列挙する（Prim法）。
	 */
	public static List<int[]> mstEdges(DistanceMatrix dist) {
		int n = dist.n();
		if (n <= 1) return Collections.emptyList();
		long[] dp = new long[n];
		Arrays.fill(dp, Long.MAX_VALUE);
		int[] prv = new int[n];
		Arrays.fill(prv, -1);
		boolean[] used = new boolean[n];
		List<int[]> ret = new ArrayList<>(n - 1);

		dp[0] = 0;
		for (int t = 0; t < n; ++t) {
			int x = -1;
			for (int i = 0; i < n; i++) {
				if (!used[i] && (x == -1 || dp[i] < dp[x])) x = i;
			}
			if (x == -1 || dp[x] == Long.MAX_VALUE) break;
			used[x] = true;
			if (prv[x] != -1) ret.add(new int[]{prv[x], x});

			for (Adjacent adj : dist.adjacents(x)) {
				if (!used[adj.to] && adj.weight < dp[adj.to]) {
					dp[adj.to] = adj.weight;
					prv[adj.to] = x;
				}
			}
		}
		return ret;
	}

	/**
	 * LKHアルゴリズムにおけるα値を計算する。
	 */
	public static long[][] calcLkhAlpha(DistanceMatrix dist) {
		int n = dist.n();
		List<int[]> mst = mstEdges(dist);
		IntArrayList[] to = new IntArrayList[n];
		for (int i = 0; i < n; i++) to[i] = new IntArrayList();
		for (int[] e : mst) {
			to[e[0]].add(e[1]);
			to[e[1]].add(e[0]);
		}

		long[][] ret = new long[n][n];
		for (int s = 0; s < n; s++) {
			Deque<AlphaState> stack = new ArrayDeque<>();
			stack.push(new AlphaState(s, -1, 0));
			while (!stack.isEmpty()) {
				AlphaState curr = stack.pop();
				ret[s][curr.now] = dist.dist(s, curr.now) - curr.hi;
				for (int i = 0; i < to[curr.now].size(); i++) {
					int nxt = to[curr.now].get(i);
					if (nxt == curr.prv) continue;
					stack.push(new AlphaState(nxt, curr.now, Math.max(curr.hi, dist.dist(curr.now, nxt))));
				}
			}
		}

		int bestOne = -1;
		long longest2ndNearest = 0;

		for (int one = 0; one < n; one++) {
			if (to[one].size() != 1) continue;
			int ng = to[one].get(0);
			boolean found = false;
			long secondNearest = 0;

			for (Adjacent adj : dist.adjacents(one)) {
				if (adj.to == ng) continue;
				if (!found) {
					found = true;
					secondNearest = adj.weight;
				} else if (adj.weight < secondNearest) {
					secondNearest = adj.weight;
				}
			}

			if (found && (bestOne < 0 || secondNearest > longest2ndNearest)) {
				bestOne = one;
				longest2ndNearest = secondNearest;
			}
		}

		if (bestOne != -1) {
			int ng = to[bestOne].get(0);
			for (Adjacent adj : dist.adjacents(bestOne)) {
				if (adj.to == ng) continue;
				ret[bestOne][adj.to] = ret[adj.to][bestOne] = adj.weight - longest2ndNearest;
			}
		}

		return ret;
	}

	private record AlphaState(int now, int prv, long hi) {}

	/**
	 * α値に基づいて上位 sz 個の候補辺からなる疎なグラフを構築する。
	 */
	public static CSRDistanceMatrix buildAdjacentInfo(DistanceMatrix dist, int sz) {
		int n = dist.n();
		long[][] alpha = calcLkhAlpha(dist);
		List<Edge> adjacentEdges = new ArrayList<>();

		record Candidate(long alphaVal, long dVal, int j) implements Comparable<Candidate> {
			@Override
			public int compareTo(Candidate o) {
				if (this.alphaVal != o.alphaVal) return Long.compare(this.alphaVal, o.alphaVal);
				if (this.dVal != o.dVal) return Long.compare(this.dVal, o.dVal);
				return Integer.compare(this.j, o.j);
			}
		}

		List<Candidate> candidates = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			candidates.clear();
			for (Adjacent adj : dist.adjacents(i)) {
				if (i != adj.to) candidates.add(new Candidate(alpha[i][adj.to], adj.weight, adj.to));
			}

			int finalSz = Math.min(sz, candidates.size());
			Collections.sort(candidates);
			if (candidates.size() > finalSz) {
				candidates = new ArrayList<>(candidates.subList(0, finalSz));
			}
			candidates.sort(Comparator.comparingLong(c -> c.dVal));
			for (Candidate c : candidates) {
				adjacentEdges.add(new Edge(i, c.j, c.dVal));
			}
		}
		return new CSRDistanceMatrix(n, adjacentEdges);
	}

	public record OneTreeResult(long weight, int[] excessDegrees) {}

	/**
	 * 与えられたペナルティ pi の下での最小 1-tree を求める。
	 * ここでは LKH の実装に倣い、MST の葉から 1 つ選んで辺を追加するヒューリスティックを用いる。
	 */
	public static OneTreeResult minimumOneTree(DistanceMatrix dist, long[] pi) {
		int n = dist.n();
		if (n <= 2) {
			// n=2の場合はTSPが定義されないが、エラー回避のために実装
			return new OneTreeResult(0, new int[n]);
		}
		long[] dp = new long[n];
		Arrays.fill(dp, Long.MAX_VALUE);
		int[] prv = new int[n];
		Arrays.fill(prv, -1);
		boolean[] used = new boolean[n];

		int[] excessDegrees = new int[n];
		Arrays.fill(excessDegrees, -2);

		autoFixV(0, dist, pi, used, dp, prv);
		long weight = 0;
		for (int t = 0; t < n - 1; t++) {
			int i = -1;
			for (int k = 0; k < n; k++) {
				if (!used[k] && (i == -1 || dp[k] < dp[i])) i = k;
			}
			weight += dp[i];
			excessDegrees[i]++;
			excessDegrees[prv[i]]++;
			autoFixV(i, dist, pi, used, dp, prv);
		}

		long wlo = 0;
		int ilo = -1, jlo = -1;
		for (int i = 0; i < n; i++) {
			if (excessDegrees[i] != -1) continue;
			long tmp = 0;
			int jtmp = -1;
			for (Adjacent adj : dist.adjacents(i)) {
				if (prv[i] == adj.to || (adj.to >= 0 && adj.to < n && prv[adj.to] == i) || i == adj.to) continue;
				long len = pi[i] + pi[adj.to] + adj.weight;
				if (jtmp == -1 || tmp > len) {
					tmp = len;
					jtmp = adj.to;
				}
			}
			if (jtmp != -1 && (ilo == -1 || wlo < tmp)) {
				wlo = tmp;
				ilo = i;
				jlo = jtmp;
			}
		}
		if (ilo != -1) {
			excessDegrees[ilo]++;
			excessDegrees[jlo]++;
			weight += wlo;
		}

		long sumPi = 0;
		for (long p : pi) sumPi += p;
		weight -= sumPi * 2;

		return new OneTreeResult(weight, excessDegrees);
	}

	private static void autoFixV(int x, DistanceMatrix dist, long[] pi, boolean[] used, long[] dp, int[] prv) {
		dp[x] = Long.MAX_VALUE;
		used[x] = true;
		for (Adjacent adj : dist.adjacents(x)) {
			if (used[adj.to]) continue;
			long len = pi[x] + pi[adj.to] + adj.weight;
			if (len < dp[adj.to]) {
				dp[adj.to] = len;
				prv[adj.to] = x;
			}
		}
	}

	/**
	 * Held-Karp下界の計算結果。
	 */
	public record HeldKarpResult(long lowerBound, long[] pi) {}

	/**
	 * Held-Karp下界を計算する。
	 *
	 * @param dist 距離行列
	 * @return 計算された下界と、その時のペナルティ pi
	 */
	public static HeldKarpResult heldKarpLowerBound(DistanceMatrix dist) {
		int n = dist.n();
		long[] bestPi = new long[n];
		long[] pi = new long[n];
		OneTreeResult res = minimumOneTree(dist, pi);
		boolean allZero = true;
		for (int v : res.excessDegrees) if (v != 0) { allZero = false; break; }
		if (allZero) return new HeldKarpResult(res.weight, pi);

		int[] lastV = res.excessDegrees;
		long bestW = res.weight;
		int initialPeriod = (n + 1) / 2;
		boolean isInitialPhase = true;
		int period = initialPeriod;

		CSRDistanceMatrix sparseSubgraph = buildAdjacentInfo(dist, 50);

		for (long t0 = 1; t0 > 0; period /= 2, t0 /= 2) {
			for (int p = 1; t0 > 0 && p <= period; p++) {
				for (int i = 0; i < n; i++) {
					if (res.excessDegrees[i] != 0) {
						pi[i] += t0 * (7L * res.excessDegrees[i] + 3L * lastV[i]) / 10;
					}
				}
				lastV = res.excessDegrees;
				res = minimumOneTree(sparseSubgraph, pi);

				boolean currentAllZero = true;
				for (int i = 0; i < n; i++) if (res.excessDegrees[i] != 0) { currentAllZero = false; break; }
				if (currentAllZero) return new HeldKarpResult(res.weight, pi);

				if (res.weight > bestW) {
					bestW = res.weight;
					bestPi = pi.clone();
					if (isInitialPhase) t0 *= 2;
					if (p == period) period = Math.min(period * 2, initialPeriod);
				} else if (isInitialPhase && p > period / 2) {
					isInitialPhase = false;
					p = 0;
					t0 = 3 * t0 / 4;
				}
			}
		}
		bestW = minimumOneTree(dist, bestPi).weight;
		return new HeldKarpResult(bestW, bestPi);
	}
}
