package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.grid.Grid8MovementCost;

public class Grid8MovementCostTest {
	@Test
	public void testBasicDirections() {
		long[] cost = {3, 4, 5, 6, 7, 8, 9, 10};
		assertEquals(0, Grid8MovementCost.minCost(0, 0, cost));
		assertEquals(3, Grid8MovementCost.minCost(1, 0, cost));
		assertEquals(4, Grid8MovementCost.minCost(1, 1, cost));
		assertEquals(7, Grid8MovementCost.minCost(-1, 0, cost));
	}

	@Test
	public void testParityWithTwoDiagonalMoves() {
		long[] cost = {100, 1, 100, 100, 100, 100, 100, 1};
		assertEquals(2, Grid8MovementCost.minCost(2, 0, cost));
		assertEquals(102, Grid8MovementCost.minCost(3, 0, cost));
	}

	@Test
	public void testAgainstDijkstraOnSmallRandomCases() {
		Random rnd = new Random(1);
		for (int tc = 0; tc < 200; tc++) {
			long[] cost = new long[8];
			for (int i = 0; i < 8; i++) cost[i] = rnd.nextInt(10);
			for (int x = -4; x <= 4; x++) {
				for (int y = -4; y <= 4; y++) {
					assertEquals(dijkstra(x, y, cost), Grid8MovementCost.minCost(x, y, cost), Arrays.toString(cost) + ": (" + x + "," + y + ")");
				}
			}
		}
	}

	@Test
	public void testNegativeCostWithoutNegativeCycle() {
		long[] cost = {-1, 100, 100, 100, 2, 100, 100, 100};
		assertEquals(-3, Grid8MovementCost.minCost(3, 0, cost));
		assertEquals(2, Grid8MovementCost.minCost(-1, 0, cost));
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: NE の辺コストだけが負で、NE+SW の閉路コストは正。</li><li>事後条件: 負の辺を含む最短路がそのまま採用される。</li><li>副作用: なし。</li><li>計算量: {@code O(1)} 個の API 呼び出し。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: なし。</li><li>例外: なし。</li><li>未定義条件: なし。</li></ul>
	 */
	// 未テスト: 負の辺を含むケース自体を検査するテストメソッド。
	@Test
	public void testNegativeDiagonalEdgeWithoutNegativeCycle() {
		long[] cost = {10, -2, 10, 10, 10, 3, 10, 10};
		assertEquals(-4, Grid8MovementCost.minCost(2, 2, cost));
		assertEquals(3, Grid8MovementCost.minCost(-1, -1, cost));
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: E と N の辺コストが負で、E+N+SW の閉路コストは非負。</li><li>事後条件: 複数の負辺を使う最短路が返る。</li><li>副作用: なし。</li><li>計算量: {@code O(1)} 個の API 呼び出し。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: なし。</li><li>例外: なし。</li><li>未定義条件: なし。</li></ul>
	 */
	// 未テスト: 複数の負の辺を含むケース自体を検査するテストメソッド。
	@Test
	public void testMultipleNegativeEdgesWithoutNegativeCycle() {
		long[] cost = {-3, 100, -4, 100, 5, 7, 6, 100};
		assertEquals(-10, Grid8MovementCost.minCost(2, 1, cost));
		assertEquals(3, Grid8MovementCost.minCost(-1, 0, cost));
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: 負の辺を少なくとも1つ含み、負閉路がないランダムケースを生成する。</li><li>事後条件: 10000件について Bellman-Ford 型の有限グリッド参照実装と一致する。</li><li>副作用: なし。</li><li>計算量: {@code O(10000 * V * E)}。ここで {@code V = 17^2}, {@code E <= 8V}。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: なし。</li><li>例外: なし。</li><li>未定義条件: なし。</li></ul>
	 */
	// 未テスト: 負の辺を含むランダムケース自体を検査するテストメソッド。
	@Test
	public void testRandomNegativeEdgesWithoutNegativeCycle() {
		Random rnd = new Random(3);
		for (int tc = 0; tc < 10000; tc++) {
			long[] cost = randomNoNegativeCycleCostWithNegativeEdge(rnd);
			int x = rnd.nextInt(9) - 4;
			int y = rnd.nextInt(9) - 4;
			assertEquals(bellmanFord(x, y, cost), Grid8MovementCost.minCost(x, y, cost), Arrays.toString(cost) + ": (" + x + "," + y + ")");
		}
	}

	@Test
	public void testNegativeCycle() {
		long[] oppositeCycle = {-5, 100, 100, 100, 1, 100, 100, 100};
		assertEquals(Grid8MovementCost.NEGATIVE_INFINITY, Grid8MovementCost.minCost(3, 0, oppositeCycle));

		long[] triangleCycle = {-5, 100, -5, 100, 100, 1, 100, 100};
		assertEquals(Grid8MovementCost.NEGATIVE_INFINITY, Grid8MovementCost.minCost(0, 0, triangleCycle));
	}

	@Test
	public void testInvalidCost() {
		assertThrows(IllegalArgumentException.class, () -> Grid8MovementCost.minCost(0, 0, new long[7]));
	}

	@Test
	public void testForbiddenDirection() {
		// Direction 0 (E) is forbidden (cost >= INF)
		// Direction 1 (NE) has cost 1, Direction 6 (S) has cost 1
		long[] cost = {Long.MAX_VALUE, 1, 100, 100, 100, 100, 1, 100};
		// To go (1, 0), direct is E (forbidden). Alternative is NE + S (cost 1 + 1 = 2)
		assertEquals(2, Grid8MovementCost.minCost(1, 0, cost));

		// All directions forbidden except NE (1) and S (6)
		long[] cost2 = {Long.MAX_VALUE, 1, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 1, Long.MAX_VALUE};
		assertEquals(2, Grid8MovementCost.minCost(1, 0, cost2));

		// Unreachable targets should return cost >= INF
		assertEquals(Long.MAX_VALUE / 4, Grid8MovementCost.minCost(-1, -1, cost2));
	}

	@Test
	public void testRandomWithForbiddenDirections() {
		Random rnd = new Random(42);
		for (int tc = 0; tc < 500; tc++) {
			long[] cost = new long[8];
			for (int i = 0; i < 8; i++) {
				if (rnd.nextDouble() < 0.3) {
					cost[i] = Long.MAX_VALUE; // Forbidden
				} else {
					cost[i] = rnd.nextInt(10);
				}
			}
			for (int x = -4; x <= 4; x++) {
				for (int y = -4; y <= 4; y++) {
					long ref = dijkstra(x, y, cost);
					long act = Grid8MovementCost.minCost(x, y, cost);
					assertEquals(ref, act, Arrays.toString(cost) + ": (" + x + "," + y + ")");
				}
			}
		}
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: {@code rnd != null}。</li><li>事後条件: 返値 {@code cost} は {@code cost.length = 8} かつ {@code exists k: cost[k] < 0}。</li><li>事後条件: {@code cost[k] = p_x DX[k] + p_y DY[k] + s_k} かつ {@code s_k >= 0} であるため、任意の閉路 C について {@code Σ_{k in C} cost[k] >= 0}。</li><li>副作用: {@code rnd} の状態を進める。</li><li>計算量: 期待 {@code O(8)} 時間、{@code O(1)} 追加空間。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: 新しい配列を返す。</li><li>例外: なし。</li><li>未定義条件: 事前条件違反。</li></ul>
	 */
	// 未テスト: testRandomNegativeEdgesWithoutNegativeCycle からのみ使用する。
	private static long[] randomNoNegativeCycleCostWithNegativeEdge(Random rnd) {
		while (true) {
			long px = rnd.nextInt(11) - 5;
			long py = rnd.nextInt(11) - 5;
			long[] cost = new long[8];
			boolean hasNegative = false;
			for (int k = 0; k < 8; k++) {
				long slack = rnd.nextInt(6);
				cost[k] = px * Grid8MovementCost.DX[k] + py * Grid8MovementCost.DY[k] + slack;
				hasNegative |= cost[k] < 0;
			}
			if (hasNegative) return cost;
		}
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: {@code -8 <= tx <= 8 && -8 <= ty <= 8 && cost.length = 8}。</li><li>事後条件: 返値 = 有限グリッド {@code [-8,8]^2} 上で {@code (0,0)} から {@code (tx,ty)} へ移動する最小コスト。</li><li>副作用: なし。</li><li>計算量: {@code O(VE)} 時間、{@code O(V)} 空間。ここで {@code V = 17^2}, {@code E <= 8V}。</li><li>破壊的変更: なし。{@code cost} は読み取り専用であり、変更しない。</li><li>参照共有・所有権: {@code cost} の参照を保持しない。</li><li>例外: なし。</li><li>未定義条件: 事前条件違反、または有限グリッド内に到達可能な負閉路がある場合。</li></ul>
	 */
	// 未テスト: testRandomNegativeEdgesWithoutNegativeCycle からのみ使用する参照実装。
	private static long bellmanFord(int tx, int ty, long[] cost) {
		int margin = 8;
		int n = 2 * margin + 1;
		long[][] dist = new long[n][n];
		for (long[] row : dist) Arrays.fill(row, Long.MAX_VALUE / 4);
		dist[margin][margin] = 0;
		for (int iter = 0; iter < n * n - 1; iter++) {
			boolean updated = false;
			for (int x = 0; x < n; x++) {
				for (int y = 0; y < n; y++) {
					if (dist[x][y] == Long.MAX_VALUE / 4) continue;
					for (int k = 0; k < 8; k++) {
						if (cost[k] >= Long.MAX_VALUE / 4) continue; // Skip forbidden directions
						int nx = x + Grid8MovementCost.DX[k];
						int ny = y + Grid8MovementCost.DY[k];
						if (nx < 0 || nx >= n || ny < 0 || ny >= n) continue;
						long nd = dist[x][y] + cost[k];
						if (nd < dist[nx][ny]) {
							dist[nx][ny] = nd;
							updated = true;
						}
					}
				}
			}
			if (!updated) break;
		}
		return dist[tx + margin][ty + margin];
	}

	private static long dijkstra(int tx, int ty, long[] cost) {
		int margin = 8;
		int n = 2 * margin + 1;
		long[][] dist = new long[n][n];
		for (long[] row : dist) Arrays.fill(row, Long.MAX_VALUE / 4);
		PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
		dist[margin][margin] = 0;
		pq.add(new long[] {0, margin, margin});
		while (!pq.isEmpty()) {
			long[] cur = pq.poll();
			long d = cur[0];
			int x = (int) cur[1], y = (int) cur[2];
			if (d != dist[x][y]) continue;
			for (int k = 0; k < 8; k++) {
				if (cost[k] >= Long.MAX_VALUE / 4) continue; // Skip forbidden directions
				int nx = x + Grid8MovementCost.DX[k];
				int ny = y + Grid8MovementCost.DY[k];
				if (nx < 0 || nx >= n || ny < 0 || ny >= n) continue;
				long nd = d + cost[k];
				if (nd < dist[nx][ny]) {
					dist[nx][ny] = nd;
					pq.add(new long[] {nd, nx, ny});
				}
			}
		}
		return dist[tx + margin][ty + margin];
	}
}
