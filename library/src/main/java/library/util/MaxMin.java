package library.util;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import library.util.graph.Edge;

/**
 * a+b=max(a,b)
 * a*b=min(a,b)
 */
public class MaxMin {

	private static final int MUL_THRESHOLD = 350_000;

	/**
	 * Max-Min半環（ボトルネック最短路、最大容量経路）上の全点対最短路を計算します。
	 * 頂点数 N が閾値（50）未満の場合は O(N^3) の Floyd-Warshall、
	 * それ以上の場合は O(N^3 / 64 + M log M) の bitset-based アルゴリズムへ自動的に分岐します。
	 *
	 * // 未テスト
	 * @param cost 隣接行列（到達不能な場合は Long.MIN_VALUE）
	 * @return 全点対の最大ボトルネック容量を格納した二次元配列
	 */
	public static long[][] asps(long[][] cost) {
		int N = cost.length;
		if (N <= 0) return new long[0][0];
		if (N < 50) {
			return warshalFloyd(cost);
		}
		return aspsBitset(cost);
	}

	private static long[][] aspsBitset(long[][] cost) {
		int N = cost.length;
		long[][] ans = new long[N][N];
		long unreachableValue = Long.MIN_VALUE;
		for (int i = 0; i < N; i++) {
			Arrays.fill(ans[i], unreachableValue);
			ans[i][i] = Long.MAX_VALUE;
		}
		List<Edge> sortedEdges = new ArrayList<>();
		for (int u = 0; u < N; u++) {
			for (int v = 0; v < N; v++) {
				if (u != v && cost[u][v] != Long.MIN_VALUE) {
					sortedEdges.add(new Edge(u, v, cost[u][v]));
				}
			}
		}
		sortedEdges.sort((e1, e2) -> Long.compare(e2.cost, e1.cost));

		int words = (N + 63) / 64;
		long[][] reach = new long[N][words];
		long[][] invReach = new long[N][words];
		for (int i = 0; i < N; i++) {
			reach[i][i >>> 6] |= 1L << (i & 63);
			invReach[i][i >>> 6] |= 1L << (i & 63);
		}

		for (Edge e : sortedEdges) {
			int u = e.src;
			int v = e.dst;
			long w = e.cost;

			if (u < 0 || u >= N || v < 0 || v >= N) throw new AssertionError();

			if ((reach[u][v >>> 6] & (1L << (v & 63))) != 0) {
				continue;
			}

			long[] target_x = new long[words];
			boolean hasTarget = false;
			for (int i = 0; i < words; i++) {
				target_x[i] = invReach[u][i] & ~invReach[v][i];
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
					int x = baseX + tzX;
					txWord &= txWord - 1;

					long[] reachX = reach[x];
					for (int j = 0; j < words; j++) {
						long newReachWord = reachV[j] & ~reachX[j];
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
	 * Max-Min半環上の全点対最短路をFloyd-Warshall法で計算します。
	 *
	 * // 未テスト
	 * @param a 隣接行列
	 * @return 全点対の最大ボトルネック容量を格納した二次元配列
	 */
	public static long[][] warshalFloyd(long[][] a) {
		long[][] b = ArrayUtils.copy(a);
		int n = a.length;
		for (int mid = 0; mid < n; mid++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					b[i][j] = Math.max(b[i][j], Math.min(b[i][mid], b[mid][j]));
				}
			}
		}
		return b;
	}

	/**
	 * Boolean行列積 C = A * B を計算する。
	 * 各要素は C[i][j] = or_k (A[i][k] and B[k][j]) である。
	 * longのビットワイズ演算（1ワードあたり64要素）を利用して高速化されている。
	 *
	 * @param a N x M のBoolean行列
	 * @param b M x K のBoolean行列
	 * @return N x K のBoolean行列積 C
	 * @complexity O(N * M * K / 64)
	 * @precondition a.length != 0 かつ a[0].length == b.length
	 */
	// 未テスト
	public static boolean[][] booleanProduct(boolean[][] a, boolean[][] b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Matrices cannot be null");
		}
		int n = a.length;
		int m = n == 0 ? 0 : a[0].length;
		int bk = b.length;
		int kCols = bk == 0 ? 0 : b[0].length;
		if (m != bk) {
			throw new IllegalArgumentException("Matrix dimensions must match for multiplication");
		}
		if (n == 0 || m == 0 || kCols == 0) {
			return new boolean[n][kCols];
		}

		int L = (kCols + 63) / 64;
		long[][] bRow = new long[m][L];
		for (int k = 0; k < m; k++) {
			boolean[] rowB = b[k];
			long[] bRowK = bRow[k];
			for (int j = 0; j < kCols; j++) {
				if (rowB[j]) {
					bRowK[j >>> 6] |= (1L << (j & 63));
				}
			}
		}

		boolean[][] c = new boolean[n][kCols];
		long[] res = new long[L];
		for (int i = 0; i < n; i++) {
			Arrays.fill(res, 0L);
			boolean[] rowA = a[i];
			for (int k = 0; k < m; k++) {
				if (rowA[k]) {
					long[] rowB = bRow[k];
					for (int w = 0; w < L; w++) {
						res[w] |= rowB[w];
					}
				}
			}
			boolean[] rowC = c[i];
			for (int j = 0; j < kCols; j++) {
				rowC[j] = ((res[j >>> 6] >>> (j & 63)) & 1L) != 0;
			}
		}
		return c;
	}

	/**
	 * Max-Min行列積のナイーブな実装です。
	 * O(N * M * K) 時間、O(N * K) 空間。キャッシュフレンドリーなループ順序を使用しています。
	 *
	 * // 未テスト
	 * @param a N x M の値行列 A
	 * @param b M x K の値行列 B
	 * @return N x K のmax-min行列積 C
	 */
	public static long[][] mulNaive(long[][] a, long[][] b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Matrices cannot be null");
		}
		int n = a.length;
		int m = n == 0 ? 0 : a[0].length;
		int bk = b.length;
		int kCols = bk == 0 ? 0 : b[0].length;
		if (m != bk) {
			throw new IllegalArgumentException("Matrix dimensions must match for multiplication");
		}
		long[][] c = new long[n][kCols];
		if (n == 0 || kCols == 0) {
			return c;
		}
		for (int i = 0; i < n; i++) {
			Arrays.fill(c[i], Long.MIN_VALUE);
		}
		for (int i = 0; i < n; i++) {
			for (int k = 0; k < m; k++) {
				long aik = a[i][k];
				if (aik == Long.MIN_VALUE) {
					continue;
				}
				long[] rowB = b[k];
				long[] rowC = c[i];
				for (int j = 0; j < kCols; j++) {
					long val = Math.min(aik, rowB[j]);
					if (val > rowC[j]) {
						rowC[j] = val;
					}
				}
			}
		}
		return c;
	}

	/**
	 * max-min行列積 C_ij = max_k min(A_ik, B_kj) を計算する。
	 * サイズに応じてナイーブな実装と、
	 * 閾値を降順に走査するインクリメンタルなビットセット（long[]）ベース of イベント駆動型アルゴリズムを動的に使い分けます。
	 *
	 * // 未テスト
	 * @param a N x M の値行列 A
	 * @param b M x K の値行列 B
	 * @return N x K のmax-min行列積 C。M = 0 のときは全要素が Long.MIN_VALUE となる
	 * @complexity O(N * M * K / 64 + (N * M + M * K) * log(N * M + M * K))
	 * @precondition a.length != 0 かつ a[0].length == b.length
	 */
	public static long[][] mul(long[][] a, long[][] b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Matrices cannot be null");
		}
		int n = a.length;
		int m = n == 0 ? 0 : a[0].length;
		int bk = b.length;
		int kCols = bk == 0 ? 0 : b[0].length;
		if (m != bk) {
			throw new IllegalArgumentException("Matrix dimensions must match for multiplication");
		}

		// 小さい行列、または演算負荷が低い場合はナイーブな実装へ分岐
		if ((long) n * m * kCols < MUL_THRESHOLD) {
			return mulNaive(a, b);
		}

		long[][] c = new long[n][kCols];
		if (n == 0 || kCols == 0) {
			return c;
		}
		if (m == 0) {
			for (int i = 0; i < n; i++) {
				Arrays.fill(c[i], Long.MIN_VALUE);
			}
			return c;
		}

		int numA = n * m;
		int numB = m * kCols;
		int totalEvents = numA + numB;
		long[] allVals = new long[totalEvents];
		int ptr = 0;
		for (int i = 0; i < n; i++) {
			long[] rowA = a[i];
			for (int k = 0; k < m; k++) {
				allVals[ptr++] = rowA[k];
			}
		}
		for (int k = 0; k < m; k++) {
			long[] rowB = b[k];
			for (int j = 0; j < kCols; j++) {
				allVals[ptr++] = rowB[j];
			}
		}
		Arrays.sort(allVals);

		int uniqueCount = 0;
		for (int i = 0; i < totalEvents; i++) {
			if (i == 0 || allVals[i] != allVals[i - 1]) {
				uniqueCount++;
			}
		}
		long[] ascUniqueVals = new long[uniqueCount];
		int uPtr = 0;
		for (int i = 0; i < totalEvents; i++) {
			if (i == 0 || allVals[i] != allVals[i - 1]) {
				ascUniqueVals[uPtr++] = allVals[i];
			}
		}

		long[] uniqueVals = new long[uniqueCount];
		for (int i = 0; i < uniqueCount; i++) {
			uniqueVals[i] = ascUniqueVals[uniqueCount - 1 - i];
		}

		int[] countA = new int[uniqueCount];
		int[] countB = new int[uniqueCount];
		int[] ranksA = new int[numA];
		int[] ranksB = new int[numB];

		int ptrA = 0;
		for (int i = 0; i < n; i++) {
			long[] rowA = a[i];
			for (int k = 0; k < m; k++) {
				int ascRank = Arrays.binarySearch(ascUniqueVals, rowA[k]);
				int descRank = uniqueCount - 1 - ascRank;
				ranksA[ptrA++] = descRank;
				countA[descRank]++;
			}
		}

		int ptrB = 0;
		for (int k = 0; k < m; k++) {
			long[] rowB = b[k];
			for (int j = 0; j < kCols; j++) {
				int ascRank = Arrays.binarySearch(ascUniqueVals, rowB[j]);
				int descRank = uniqueCount - 1 - ascRank;
				ranksB[ptrB++] = descRank;
				countB[descRank]++;
			}
		}

		int[] offsetA = new int[uniqueCount + 1];
		int[] offsetB = new int[uniqueCount + 1];
		for (int r = 0; r < uniqueCount; r++) {
			offsetA[r + 1] = offsetA[r] + countA[r];
			offsetB[r + 1] = offsetB[r] + countB[r];
		}

		long[] packedA = new long[numA];
		long[] packedB = new long[numB];

		int[] curA = offsetA.clone();
		int[] curB = offsetB.clone();

		ptrA = 0;
		for (int i = 0; i < n; i++) {
			for (int k = 0; k < m; k++) {
				int descRank = ranksA[ptrA++];
				packedA[curA[descRank]++] = ((long) i << 32) | k;
			}
		}

		ptrB = 0;
		for (int k = 0; k < m; k++) {
			for (int j = 0; j < kCols; j++) {
				int descRank = ranksB[ptrB++];
				packedB[curB[descRank]++] = ((long) k << 32) | j;
			}
		}

		int LN = (n + 63) / 64;
		int LK = (kCols + 63) / 64;

		long[][] colA = new long[m][LN];
		long[][] rowB = new long[m][LK];
		long[][] rowRes = new long[n][LK];
		long[][] colRes = new long[kCols][LN];

		for (int i = 0; i < n; i++) {
			Arrays.fill(c[i], Long.MIN_VALUE);
		}

		for (int r = 0; r < uniqueCount; r++) {
			long v = uniqueVals[r];

			int startA = offsetA[r];
			int endA = offsetA[r + 1];
			for (int idx = startA; idx < endA; idx++) {
				long packed = packedA[idx];
				int i = (int) (packed >>> 32);
				int k = (int) (packed & 0xFFFFFFFFL);

				colA[k][i >>> 6] |= (1L << (i & 63));

				long[] rowResI = rowRes[i];
				long[] rowBK = rowB[k];
				for (int w = 0; w < LK; w++) {
					long old = rowResI[w];
					long rowVal = rowBK[w];
					long diff = rowVal & ~old;
					if (diff != 0) {
						rowResI[w] = old | rowVal;
						long tempDiff = diff;
						while (tempDiff != 0) {
							long lsb = tempDiff & -tempDiff;
							int localJ = Long.numberOfTrailingZeros(lsb);
							int j = (w << 6) | localJ;
							c[i][j] = v;
							colRes[j][i >>> 6] |= (1L << (i & 63));
							tempDiff ^= lsb;
						}
					}
				}
			}

			int startB = offsetB[r];
			int endB = offsetB[r + 1];
			for (int idx = startB; idx < endB; idx++) {
				long packed = packedB[idx];
				int k = (int) (packed >>> 32);
				int j = (int) (packed & 0xFFFFFFFFL);

				rowB[k][j >>> 6] |= (1L << (j & 63));

				long[] colResJ = colRes[j];
				long[] colAK = colA[k];
				for (int w = 0; w < LN; w++) {
					long old = colResJ[w];
					long colVal = colAK[w];
					long diff = colVal & ~old;
					if (diff != 0) {
						colResJ[w] = old | colVal;
						long tempDiff = diff;
						while (tempDiff != 0) {
							long lsb = tempDiff & -tempDiff;
							int localI = Long.numberOfTrailingZeros(lsb);
							int i = (w << 6) | localI;
							c[i][j] = v;
							rowRes[i][j >>> 6] |= (1L << (j & 63));
							tempDiff ^= lsb;
						}
					}
				}
			}
		}

		return c;
	}
}
