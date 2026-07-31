package library.util.fold;

/**
 * N次元の静的な点更新・区間和取得。
 */
public class StaticPointAddRangeSumND extends PrefixSumND {
	//https://atcoder.jp/contests/abc465/submissions/77216955
	private boolean built = false;

	/**
	 * 各次元のサイズを指定して構築する。
	 * @param dims 各次元のサイズ
	 */
	public StaticPointAddRangeSumND(int... dims) {
		super(dims);
	}

	/**
	 * 指定した座標に値を加算する。
	 * @param coords 座標
	 * @param val 値
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: !built であること。</li>
	 *   <li>計算量: O(N)</li>
	 *   <li>破壊的変更: あり</li>
	 * </ul>
	 */
	public void add(int[] coords, long val) {
		if (built) throw new AssertionError("Already built");
		super.add(coords, val);
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
	 * 指定した座標（0から各座標までの閉領域）の累積和を取得する。
	 * 負の座標が含まれる場合は 0 を返す。
	 * @param coords 座標
	 * @return 累積和
	 */
	@Override
	public long getPrefixSum(int[] coords) {
		return super.getPrefixSum(coords);
	}

	/**
	 * 指定された N 次元矩形領域の和を返す。
	 * [minCoords[i], maxCoords[i]) for all i.
	 *
	 * @param minCoords 各次元の最小座標 (inclusive)
	 * @param maxCoords 各次元の最大座標 (exclusive)
	 * @return 領域の和
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>副作用: !built の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(2^N)</li>
	 * </ul>
	 */
	public long getRangeSum(int[] minCoords, int[] maxCoords) {
		if (!built) build();
		for (int i = 0; i < N; i++) {
			if (minCoords[i] >= maxCoords[i]) return 0;
		}

		long res = 0;
		int[] cur = new int[N];
		for (int i = 0; i < (1 << N); i++) {
			int pop = 0;
			for (int j = 0; j < N; j++) {
				if (((i >> j) & 1) == 1) {
					cur[j] = minCoords[j] - 1;
					pop++;
				} else {
					cur[j] = maxCoords[j] - 1;
				}
			}
			long val = getPrefixSum(cur);
			if (pop % 2 == 1) res -= val;
			else res += val;
		}
		return res;
	}
}
