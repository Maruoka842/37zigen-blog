package library.util.graph.cycle;

import library.util.collections.LongArrayList;

/**
 * サイクルグラフ上の最大 b-マッチングに関する関数群。
 */
public final class CycleBMatching {
	private CycleBMatching() {
	}

	public static long maxBMatchingSize(long[] a) {
		final int n = a.length;
		long sum = 0;
		for (long v : a) sum += v;
		if (n == 1) return a[0] / 2;
		long vertexCover = minimumWeightVertexCoverOnCycle(a);
		return Math.min(sum / 2, vertexCover);
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code a.length >= 1} かつ任意の {@code i} について {@code a[i] >= 0}。</li>
	 * <li>事後条件: 返り値 {@code x} は {@code x.length == a.length} を満たす非負整数列であり、サイクル上の辺 {@code (i, (i+1) mod N)} のマッチング重みを表す。</li>
	 * <li>事後条件: 各頂点 {@code i} について、接続する辺のマッチング重みの和が容量 {@code a[i]} 以下である。
	 * すなわち {@code x[(i-1+N) mod N] + x[i] <= a[i]}（{@code N=1} の場合は {@code 2*x[0] <= a[0]}）。</li>
	 * <li>事後条件: {@code sum(x) == maxBMatchingSize(a)}。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @return 最大 b-マッチングにおける各辺の重み
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(N)。
	public static long[] maxBMatching(long[] a) {
		final int n = a.length;
		if (n == 1) return new long[] {a[0] / 2};
		long bestFirst = bestFirstEdgeForMaxBMatching(a);
		return restoreBMatchingWithFirstEdge(a, bestFirst);
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code w != null} かつ {@code w.length >= 2} かつ任意の {@code i} について {@code w[i] >= 0}。</li>
	 * <li>事前条件: 内部加算が {@code long} でオーバーフローしない。</li>
	 * <li>事後条件: 返り値は、サイクルグラフ {@code C_N} の頂点 {@code i} の重みを {@code w[i]} とした最小重み頂点被覆、
	 * すなわち {@code min sum_{i in S} w[i]} subject to {@code forall i, i in S or (i+1) mod N in S} に等しい。</li>
	 * <li>計算量: {@code N = w.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param w 頂点重み
	 * @return サイクルの最小重み頂点被覆の重み
	 */
	// 未テスト。計算量: N = w.length として時間 O(N)、追加空間 O(1)。
	public static long minimumWeightVertexCoverOnCycle(long[] w) {
		return Math.min(vertexCoverWithFirstState(w, true), vertexCoverWithFirstState(w, false));
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code w != null} かつ {@code w.length >= 2} かつ任意の {@code i} について {@code w[i] >= 0}。</li>
	 * <li>事後条件: 任意の {@code i} について {@code cover[i] || cover[(i+1) mod N]}、すなわち {@code cover} はサイクルグラフ {@code C_N} の頂点被覆である。</li>
	 * <li>事後条件: {@code sum_{cover[i]} w[i] == minimumWeightVertexCoverOnCycle(w)}。</li>
	 * <li>計算量: {@code N = w.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param w 頂点重み
	 * @return サイクルの最小重み頂点被覆を表す真偽値配列
	 */
	// 未テスト。計算量: N = w.length として時間 O(N)、追加空間 O(N)。
	public static boolean[] minimumWeightVertexCoverSetOnCycle(long[] w) {
		long takeFirst = vertexCoverWithFirstState(w, true);
		long notTakeFirst = vertexCoverWithFirstState(w, false);
		return restoreVertexCoverWithFirstState(w, takeFirst <= notTakeFirst);
	}

	/** INF: 不可能状態を表す十分大きい値。 */
	private static final long INF = Long.MAX_VALUE / 4;

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code w != null} かつ {@code w.length >= 2} かつ任意の {@code i} について {@code w[i] >= 0}。</li>
	 * <li>事前条件: 内部加算が {@code long} でオーバーフローしない。</li>
	 * <li>事後条件: 返り値は、頂点 {@code 0} を {@code takeFirst} どおりに固定したサイクル {@code C_N} の
	 * 最小重み頂点被覆の重みに等しい。</li>
	 * <li>計算量: {@code N = w.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param w 頂点重み
	 * @param takeFirst {@code true} なら頂点0を選ぶ、{@code false} なら頂点0を選ばない
	 * @return 固定条件下での最小重み頂点被覆の重み
	 */
	// 未テスト。計算量: N = w.length として時間 O(N)、追加空間 O(1)。
	private static long vertexCoverWithFirstState(long[] w, boolean takeFirst) {
		long dp0 = takeFirst ? INF : 0;
		long dp1 = takeFirst ? w[0] : INF;
		for (int i = 1; i < w.length; i++) {
			long ndp0 = dp1;
			long ndp1 = Math.min(dp0, dp1) + w[i];
			dp0 = ndp0;
			dp1 = ndp1;
		}
		return takeFirst ? Math.min(dp0, dp1) : dp1;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code w != null} かつ {@code w.length >= 2} かつ任意の {@code i} について {@code w[i] >= 0}。</li>
	 * <li>事前条件: 内部加算が {@code long} でオーバーフローしない。</li>
	 * <li>事後条件: 返り値 {@code cover} は、頂点 {@code 0} を {@code takeFirst} どおりに固定したサイクル {@code C_N} の最小重み頂点被覆である。</li>
	 * <li>計算量: {@code N = w.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param w 頂点重み
	 * @param takeFirst {@code true} なら頂点0を選ぶ、{@code false} なら頂点0を選ばない
	 * @return 固定条件下での最小重み頂点被覆を表す真偽値配列
	 */
	// 未テスト。計算量: N = w.length として時間 O(N)、追加空間 O(N)。
	private static boolean[] restoreVertexCoverWithFirstState(long[] w, boolean takeFirst) {
		int n = w.length;
		long[] dp0 = new long[n];
		long[] dp1 = new long[n];
		dp0[0] = takeFirst ? INF : 0;
		dp1[0] = takeFirst ? w[0] : INF;
		for (int i = 1; i < n; i++) {
			dp0[i] = dp1[i - 1];
			dp1[i] = Math.min(dp0[i - 1], dp1[i - 1]) + w[i];
		}
		boolean[] cover = new boolean[n];
		boolean takeLast = takeFirst ? dp1[n - 1] <= dp0[n - 1] : true;
		for (int i = n - 1; i >= 1; i--) {
			cover[i] = takeLast;
			//dp0[i-1], dp1[i-1]のどちらを使うかを決定する。
			if (takeLast) {
				takeLast = dp1[i] == dp0[i - 1] + w[i] ? false : true;
			} else {
				takeLast = true;
			}
		}
		cover[0] = takeFirst;
		return cover;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code a.length >= 2} かつ任意の {@code i} について {@code a[i] >= 0}。</li>
	 * <li>事前条件: 内部加算が {@code long} でオーバーフローしない。</li>
	 * <li>事後条件: 返り値 {@code x} は {@code 0 <= x <= min(a[0], a[1])} を満たす。</li>
	 * <li>事後条件: {@code bMatchingSizeWithFirstEdge(a, x) == max_{0 <= y <= min(a[0],a[1])} bMatchingSizeWithFirstEdge(a, y)}。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @return 最大 b-マッチングを達成する辺0の重み
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(1)。
	private static long bestFirstEdgeForMaxBMatching(long[] a) {
		long[] bases = vertexCoverWithFreeFirstEdge(a);
		long upper = Math.min(a[0], a[1]);
		long bestValue = Long.MIN_VALUE;
		long bestX = 0;
		// max_x min_i (bases[i]-ix+x) を求めたい
		LongArrayList list=new LongArrayList(8);
		list.add(0);
		list.add(upper);
		for (int i = 0; i < bases.length; i++) {
			for (int j = i + 1; j < bases.length; j++) {
				if (bases[i] >= INF || bases[j] >= INF || i == j) continue;
				//交点をすべて試す
				//bases[i]-i*x+x==bases[j]-j*x+x
				//x=(bases[i]-bases[j])/(i-j)
				long numerator = bases[j] - bases[i];
				long denominator = j - i;
				long q = Math.floorDiv(numerator, denominator);
				if (0 < q && q < upper) list.add(q);
				if (0 < q + 1 && q + 1 < upper) list.add(q+1);
			}
		}
		for (long x : list) {
			long min = Long.MAX_VALUE;
			for (int i = 0; i < bases.length; i++) {
				min = Math.min(min, bases[i]-(i-1)*x);
			}
			if (bestValue < min) {
				bestValue = min;
				bestX = x;
			}
		}
		return bestX;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code a.length >= 2} かつ任意の {@code i} について {@code a[i] >= 0}。</li>
	 * <li>事前条件: 内部加算が {@code long} でオーバーフローしない。</li>
	 * <li>事後条件: 返り値 {@code b} は {@code b.length == 3} を満たす。</li>
	 * <li>事後条件: 辺0のマッチング重数を {@code x} とすると、辺0を削除したパスの最小重み頂点被覆は {@code min_{k=0}^{2}(b[k] - k*x)} に等しい。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(1)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @return {@code x} に依存する端点を {@code k} 個選んだ場合の定数項の最小値
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(1)。
	private static long[] vertexCoverWithFreeFirstEdge(long[] a) {
		//辺01を削除すると、二部グラフになるので、b-matching=最小頂点被覆
		long[] dp0 = {0, INF, INF};
		long[] dp1 = {INF, a[1], INF};
		for (int order = 1; order < a.length; order++) {
			int vertex = (order + 1) % a.length;
			int dependent = vertex == 0 ? 1 : 0;
			long weight = a[vertex];
			long[] ndp0 = dp1.clone();
			long[] ndp1 = {INF, INF, INF};
			for (int k = 0; k + dependent <= 2; k++) {
				long prev = Math.min(dp0[k], dp1[k]);
				if (prev < INF) ndp1[k + dependent] = Math.min(ndp1[k + dependent], prev + weight);
			}
			dp0 = ndp0;
			dp1 = ndp1;
		}
		long[] res = new long[3];
		for (int k = 0; k < res.length; k++) {
			res[k] = Math.min(dp0[k], dp1[k]);
		}
		return res;
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code a != null} かつ {@code a.length >= 2} かつ任意の {@code i} について {@code a[i] >= 0}。</li>
	 * <li>事前条件: {@code 0 <= firstEdge <= min(a[0], a[1])}。</li>
	 * <li>事後条件: 返り値 {@code x} は {@code x.length == a.length} を満たす。</li>
	 * <li>事後条件: {@code x[0] == firstEdge}。</li>
	 * <li>事後条件: {@code x} は、辺0の重みを {@code firstEdge} に固定し、残りのパス {@code 1,2,...,N-1} を左から貪欲に最大化したマッチング重数列に等しい。</li>
	 * <li>計算量: {@code N = a.length} として時間 {@code O(N)}、追加空間 {@code O(N)}。</li>
	 * </ul>
	 *
	 * @param a 頂点容量
	 * @param firstEdge 辺0の重み
	 * @return 固定条件下でのマッチング重数列
	 */
	// 未テスト。計算量: N = a.length として時間 O(N)、追加空間 O(N)。
	private static long[] restoreBMatchingWithFirstEdge(long[] a, long firstEdge) {
		long[] rem = a.clone();
		long[] operations = new long[a.length];
		operations[0] = firstEdge;
		rem[0] -= firstEdge;
		rem[1] -= firstEdge;
		for (int i = 1; i < a.length; i++) {
			int j = (i + 1) % a.length;
			long use = Math.min(rem[i], rem[j]);
			operations[i] = use;
			rem[i] -= use;
			rem[j] -= use;
		}
		return operations;
	}
}
