package library.util.fold;

import java.util.Arrays;

import library.util.algebra.strategy.AbelianGroupStrategy;

/**
 * N次元の静的な点更新・区間和取得（アーベル群）。
 * @param <T>
 */
public class StaticPointAddRangeSumAbelianGroupND<T> {
	private final T[] data;
	private final int[] dims;
	private final int[] strides;
	private final int N;
	private final AbelianGroupStrategy<T> group;
	private boolean built = false;

	/**
	 * 各次元のサイズと群の戦略を指定して構築する。
	 * @param group 群の戦略
	 * @param dims 各次元のサイズ
	 */
	public StaticPointAddRangeSumAbelianGroupND(AbelianGroupStrategy<T> group, int... dims) {
		this.N = dims.length;
		this.dims = dims.clone();
		this.group = group;
		this.strides = new int[N];
		long total = 1;
		for (int i = N - 1; i >= 0; i--) {
			strides[i] = (int) total;
			total *= dims[i];
		}
		if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Total size too large");
		@SuppressWarnings("unchecked")
		T[] d = (T[]) new Object[(int) total];
		this.data = d;
		Arrays.fill(data, group.identity());
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
	public void add(int[] coords, T val) {
		if (built) throw new AssertionError("Already built");
		int idx = 0;
		for (int i = 0; i < N; i++) {
			if (coords[i] < 0 || coords[i] >= dims[i]) return;
			idx += coords[i] * strides[i];
		}
		data[idx] = group.mul(data[idx], val);
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
		for (int k = 0; k < N; k++) {
			int s = strides[k];
			int d = dims[k];
			for (int i = 0; i < data.length; i++) {
				if ((i / s) % d > 0) {
					data[i] = group.mul(data[i - s], data[i]);
				}
			}
		}
		built = true;
	}

	/**
	 * 指定した座標（0から各座標までの閉領域）の累積和を取得する。
	 * 負の座標が含まれる場合は identity を返す。
	 * @param coords 座標
	 * @return 累積和
	 */
	public T getPrefixSum(int[] coords) {
		int idx = 0;
		for (int i = 0; i < N; i++) {
			if (coords[i] < 0) return group.identity();
			int c = Math.min(coords[i], dims[i] - 1);
			idx += c * strides[i];
		}
		return data[idx];
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
	public T getRangeSum(int[] minCoords, int[] maxCoords) {
		if (!built) build();
		for (int i = 0; i < N; i++) {
			if (minCoords[i] >= maxCoords[i]) return group.identity();
		}

		T res = group.identity();
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
			T val = getPrefixSum(cur);
			if (pop % 2 == 1) res = group.mul(res, group.inverse(val));
			else res = group.mul(res, val);
		}
		return res;
	}
}
