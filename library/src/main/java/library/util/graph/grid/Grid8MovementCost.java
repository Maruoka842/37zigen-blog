package library.util.graph.grid;

/**
 * 8近傍の無限グリッド上の移動コストを扱うユーティリティ。
 * 移動コストが十分に大きい（{@code INF} 以上）方向は禁止方向（移動不可）として扱われます。
 */
public final class Grid8MovementCost {
	/** 方向 k の x 座標増分。順序は E, NE, N, NW, W, SW, S, SE。 */
	public static final int[] DX = {1, 1, 0, -1, -1, -1, 0, 1};
	/** 方向 k の y 座標増分。順序は E, NE, N, NW, W, SW, S, SE。 */
	public static final int[] DY = {0, 1, 1, 1, 0, -1, -1, -1};
	/** long の加算・乗算でオーバーフローしない入力に対する正の番兵値。 */
	private static final long INF = Long.MAX_VALUE / 4;
	/** 負閉路により最小コストが下に非有界であることを表す返値。 */
	public static final long NEGATIVE_INFINITY = Long.MIN_VALUE;

	private Grid8MovementCost() {
	}

	/**
	 * 契約:
	 * <ul>
	 * <li>事前条件: {@code cost.length = 8}。</li>
	 * <li>事前条件: 返値および内部の {@code a * cost[i] + b * cost[j] + cost[k]} は {@code long} に収まる。</li>
	 * <li>事後条件: {@code (0,0)} に戻る負コスト閉路が存在する場合、返値 = {@link #NEGATIVE_INFINITY}。</li>
	 * <li>事後条件: それ以外の場合、返値 = {@code min Σ_t cost[d_t]}。ただし各 {@code d_t in {0,...,7}} で、
	 *     {@code Σ_t DX[d_t] = x} かつ {@code Σ_t DY[d_t] = y}。</li>
	 * <li>副作用: なし。</li>
	 * <li>計算量: {@code O(8^3)} 時間、{@code O(1)} 追加空間。</li>
	 * <li>破壊的変更: なし。{@code cost} は読み取り専用であり、変更しない。</li>
	 * <li>参照共有・所有権: {@code cost} の参照を保持しない。</li>
	 * <li>例外: {@code cost.length != 8} の場合、{@link IllegalArgumentException}。</li>
	 * <li>未定義条件: 事前条件の算術範囲を満たさない場合。</li>
	 * </ul>
	 */
	// 未テスト: src/test/java/library/util/Grid8MovementCostTest.java で基本性質と小さい座標の全探索照合を追加済み。
	public static long minCost(long x, long y, long[] cost) {
		//https://atcoder.jp/contests/abc271/submissions/77797521（コスト1 or INF）
		validate(cost);
		if (hasNegativeCycle(cost)) return NEGATIVE_INFINITY;
		long ans = INF;
		ans = Math.min(ans, minCostByAtMostTwoDirections(x, y, cost));
		for (int k = 0; k < 8; k++) {
			long restX = x - DX[k];
			long restY = y - DY[k];
			long tail = minCostByAtMostTwoDirections(restX, restY, cost);
			if (tail < INF) ans = Math.min(ans, safeAdd(cost[k], tail));
		}
		return ans;
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: なし。</li><li>事後条件: {@code cost.length = 8}。</li><li>副作用: なし。</li><li>計算量: {@code O(1)}。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: {@code cost} の参照を保持しない。</li><li>例外: {@code cost.length != 8} の場合、{@link IllegalArgumentException}。</li><li>未定義条件: {@code cost == null}。</li></ul>
	 */
	// 未テスト: public API 経由で長さ不正を検査する。
	private static void validate(long[] cost) {
		if (cost.length != 8) throw new IllegalArgumentException("cost.length must be 8");
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: {@code cost.length = 8}。</li><li>事後条件: 返値 = 「非空の移動列 {@code d_t} が存在し、{@code Σ DX[d_t] = 0}, {@code Σ DY[d_t] = 0}, {@code Σ cost[d_t] < 0}」。</li><li>副作用: なし。</li><li>計算量: {@code O(8^3)}。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: {@code cost} の参照を保持しない。</li><li>例外: なし。</li><li>未定義条件: 事前条件違反。</li></ul>
	 */
	// 未テスト: public API 経由で負閉路を検査する。
	private static boolean hasNegativeCycle(long[] cost) {
		for (int i = 0; i < 8; i++) {
			for (int j = i + 1; j < 8; j++) {
				if (DX[i] + DX[j] == 0 && DY[i] + DY[j] == 0 && safeAdd(cost[i], cost[j]) < 0) return true;
				for (int k = j + 1; k < 8; k++) {
					long[] cycle = primitiveCycle(i, j, k);
					if (cycle == null) continue;
					long total = safeAdd(safeAdd(safeMul(cycle[0], cost[i]), safeMul(cycle[1], cost[j])), safeMul(cycle[2], cost[k]));
					if (total < 0) return true;
				}
			}
		}
		return false;
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: {@code 0 <= i < j < k < 8}。</li><li>事後条件: 返値が非nullなら {@code a*D_i + b*D_j + c*D_k = (0,0)} かつ {@code gcd(a,b,c)=1} かつ {@code a,b,c > 0}。</li><li>事後条件: 返値がnullなら正係数の一次従属は存在しない。</li><li>副作用: なし。</li><li>計算量: {@code O(log 8)}。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: 新しい配列を返す。</li><li>例外: なし。</li><li>未定義条件: 事前条件違反。</li></ul>
	 */
	// 未テスト: public API 経由で三角形型負閉路を検査する。
	private static long[] primitiveCycle(int i, int j, int k) {
		long a = (long) DX[j] * DY[k] - (long) DY[j] * DX[k];
		long b = (long) DX[k] * DY[i] - (long) DY[k] * DX[i];
		long c = (long) DX[i] * DY[j] - (long) DY[i] * DX[j];
		if (a == 0 || b == 0 || c == 0) return null;
		if (a < 0 && b < 0 && c < 0) {
			a = -a;
			b = -b;
			c = -c;
		}
		if (a <= 0 || b <= 0 || c <= 0) return null;
		long g = gcd(gcd(a, b), c);
		return new long[] {a / g, b / g, c / g};
	}

	/**
	 * 契約:
	 * <ul><li>事前条件: {@code a >= 0 && b >= 0}。</li><li>事後条件: 返値 = {@code gcd(a,b)}。</li><li>副作用: なし。</li><li>計算量: {@code O(log max(a,b))}。</li><li>破壊的変更: なし。</li><li>参照共有・所有権: なし。</li><li>例外: なし。</li><li>未定義条件: 事前条件違反。</li></ul>
	 */
	// 未テスト: primitiveCycle 経由でのみ使用する。
	private static long gcd(long a, long b) {
		while (b != 0) {
			long t = a % b;
			a = b;
			b = t;
		}
		return a;
	}

	private static long minCostByAtMostTwoDirections(long x, long y, long[] cost) {
		long ans = (x == 0 && y == 0) ? 0 : INF;
		for (int i = 0; i < 8; i++) {
			long single = minCostByOneDirection(x, y, i, cost);
			ans = Math.min(ans, single);
			for (int j = i + 1; j < 8; j++) {
				long pair = minCostByTwoDirections(x, y, i, j, cost);
				ans = Math.min(ans, pair);
			}
		}
		return ans;
	}

	private static long minCostByOneDirection(long x, long y, int i, long[] cost) {
		long dx = DX[i], dy = DY[i];
		if (dx == 0) {
			if (x != 0 || dy == 0 || y % dy != 0) return INF;
			long a = y / dy;
			return a >= 0 ? safeMul(a, cost[i]) : INF;
		}
		if (dy == 0) {
			if (y != 0 || x % dx != 0) return INF;
			long a = x / dx;
			return a >= 0 ? safeMul(a, cost[i]) : INF;
		}
		if (x % dx != 0 || y % dy != 0) return INF;
		long a = x / dx;
		return a >= 0 && a == y / dy ? safeMul(a, cost[i]) : INF;
	}

	private static long minCostByTwoDirections(long x, long y, int i, int j, long[] cost) {
		long det = (long) DX[i] * DY[j] - (long) DY[i] * DX[j];
		if (det == 0) return INF;
		long na = x * DY[j] - y * DX[j];
		long nb = (long) DX[i] * y - (long) DY[i] * x;
		if (na % det != 0 || nb % det != 0) return INF;
		long a = na / det;
		long b = nb / det;
		if (a < 0 || b < 0) return INF;
		return safeAdd(safeMul(a, cost[i]), safeMul(b, cost[j]));
	}

	private static long safeAdd(long a, long b) {
		if (a >= INF || b >= INF) return INF;
		return a + b;
	}

	private static long safeMul(long a, long b) {
		if (a == 0 || b == 0) return 0;
		if (a >= INF || b >= INF) return INF;
		return a * b;
	}
}
