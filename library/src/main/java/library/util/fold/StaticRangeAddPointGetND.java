package library.util.fold;

/**
 * N次元の静的な区間加算・一点取得。
 */
public class StaticRangeAddPointGetND extends PrefixSumND {
	private boolean built = false;

	/**
	 * 各次元のサイズを指定して構築する。
	 * @param dims 各次元のサイズ
	 */
	public StaticRangeAddPointGetND(int... dims) {
		super(dims);
	}

	/**
	 * 指定された N 次元矩形領域に値を加算する。
	 * [minCoords[i], maxCoords[i]) for all i.
	 * 範囲が配列外にはみ出ている場合は、配列内の領域に対してのみ加算される。
	 *
	 * @param minCoords 各次元の最小座標 (inclusive)
	 * @param maxCoords 各次元の最大座標 (exclusive)
	 * @param val 加算する値
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: !built であること。</li>
	 *   <li>計算量: O(2^N)</li>
	 *   <li>破壊的変更: あり</li>
	 * </ul>
	 */
	public void rangeAdd(int[] minCoords, int[] maxCoords, long val) {
		if (built) throw new AssertionError("Already built");
		int[] mi = new int[N];
		int[] ma = new int[N];
		for (int i = 0; i < N; i++) {
			mi[i] = Math.max(0, minCoords[i]);
			ma[i] = Math.min(dims[i], maxCoords[i]);
			if (mi[i] >= ma[i]) return;
		}

		int[] cur = new int[N];
		for (int i = 0; i < (1 << N); i++) {
			int pop = 0;
			for (int j = 0; j < N; j++) {
				if (((i >> j) & 1) == 1) {
					cur[j] = ma[j];
					pop++;
				} else {
					cur[j] = mi[j];
				}
			}
			if (pop % 2 == 1) super.add(cur, -val);
			else super.add(cur, val);
		}
	}

	/**
	 * 累積和を構築する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: !built であること。</li>
	 *   <li>計算量: O(N * Π dims)</li>
	 *   <li>破壊的変更: あり</li>
	 * </ul>
	 */
	public void build() {
		if (built) throw new AssertionError("Already built");
		super.build();
		built = true;
	}

	/**
	 * 指定した座標の現在の値を取得する。
	 * @param coords 座標
	 * @return 値
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>副作用: !built の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	public long get(int[] coords) {
		if (!built) build();
		for (int i = 0; i < N; i++) {
			if (coords[i] < 0 || coords[i] >= dims[i]) return 0;
		}
		return super.getPrefixSum(coords);
	}
}
