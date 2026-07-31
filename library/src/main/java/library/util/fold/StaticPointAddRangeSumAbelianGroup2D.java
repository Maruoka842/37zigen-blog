package library.util.fold;

import java.util.Arrays;

import library.util.algebra.strategy.AbelianGroupStrategy;
import library.util.collections.HashMultiSet;

/**
 * 2次元の静的な点更新・区間和取得（アーベル群）
 * @param <T>
 */
public class StaticPointAddRangeSumAbelianGroup2D<T> {
	private final int H, W;
	private final T[][] A;
	private final AbelianGroupStrategy<T> group;
	private boolean built = false;

	/**
	 * HxWの配列を用意する
	 * @param H
	 * @param W
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup2D(int H, int W, AbelianGroupStrategy<T> group) {
		this.H = H;
		this.W = W;
		this.group = group;
		this.A = (T[][]) new Object[H][W];
		for (int i = 0; i < H; i++) {
			Arrays.fill(A[i], group.identity());
		}
	}

	/**
	 * 二次元配列aで初期化する
	 * @param a
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup2D(T[][] a, AbelianGroupStrategy<T> group) {
		this.H = a.length;
		this.W = a[0].length;
		this.group = group;
		this.A = (T[][]) new Object[H][];
		for (int i = 0; i < H; i++) {
			this.A[i] = a[i].clone();
		}
	}

	/**
	 * 座標(i, j)にvalを足す
	 * @param i
	 * @param j
	 * @param val
	 */
	public void add(int i, int j, T val) {
		if (built) throw new AssertionError();
		if (i < 0 || j < 0 || i >= H || j >= W) return;
		A[i][j] = group.mul(A[i][j], val);
	}

	/**
	 * 累積和を構築する
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 二次元累積和を構築する。</p>
	 * <p>副作用: Aの内容が書き換えられ、builtがtrueになる。</p>
	 * <p>計算量: O(HW)</p>
	 * <p>破壊的変更: あり。</p>
	 * 未テスト
	 */
	public void build() {
		if (built) throw new AssertionError();
		for (int i = 0; i < H; i++) {
			for (int j = 1; j < W; j++) {
				A[i][j] = group.mul(A[i][j - 1], A[i][j]);
			}
		}
		for (int j = 0; j < W; j++) {
			for (int i = 1; i < H; i++) {
				A[i][j] = group.mul(A[i - 1][j], A[i][j]);
			}
		}
		built = true;
	}

	private T get(int i, int j) {
		if (i < 0 || j < 0) return group.identity();
		i = Math.min(i, H - 1);
		j = Math.min(j, W - 1);
		return A[i][j];
	}

	/**
	 * [minH, maxH) x [minW, maxW) の和を返す
	 * @param minH
	 * @param minW
	 * @param maxH
	 * @param maxW
	 * @return
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 指定された長方形領域の和を返す。</p>
	 * <p>副作用: !built の場合、内部で build() を呼び出す。</p>
	 * <p>計算量: O(1)</p>
	 * <p>破壊的変更: build() が呼ばれる場合のみあり。</p>
	 * 未テスト
	 */
	public T getRangeSum(int minH, int minW, int maxH, int maxW) {
		if (!built) build();
		if (minH < 0) minH = 0;
		if (minW < 0) minW = 0;
		if (maxH <= minH || maxW <= minW) return group.identity();

		T res = get(maxH - 1, maxW - 1);
		res = group.mul(res, group.inverse(get(minH - 1, maxW - 1)));
		res = group.mul(res, group.inverse(get(maxH - 1, minW - 1)));
		res = group.mul(res, get(minH - 1, minW - 1));
		return res;
	}

	/**
	 * 和が v となる長方形区間の個数を返す。
	 *
	 * @param v ターゲットとなる和
	 * @return #{(h1, w1, h2, w2) | 0 <= h1 < h2 <= H, 0 <= w1 < w2 <= W, \sum_{i=h1}^{h2-1} \sum_{j=w1}^{w2-1} a_{i,j} = v}
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 和が v となる区間の個数を返す。</li>
	 *   <li>副作用: !built の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(min(H, W)^2 * max(H, W))</li>
	 *   <li>破壊的変更: なし（build() による内部状態の変化を除く）。</li>
	 * </ul>
	 * 未テスト
	 */
	public long countRangeSum(T v) {
		if (!built) build();
		long res = 0;
		if (H <= W) {
			for (int h1 = 0; h1 < H; h1++) {
				for (int h2 = h1 + 1; h2 <= H; h2++) {
					HashMultiSet<T> counts = new HashMultiSet<>();
					counts.add(group.identity());
					for (int w = 0; w < W; w++) {
						T current = A[h2 - 1][w];
						if (h1 > 0) {
							current = group.mul(current, group.inverse(A[h1 - 1][w]));
						}
						T target = group.mul(current, group.inverse(v));
						res += counts.getValue(target);
						counts.add(current);
					}
				}
			}
		} else {
			for (int w1 = 0; w1 < W; w1++) {
				for (int w2 = w1 + 1; w2 <= W; w2++) {
					HashMultiSet<T> counts = new HashMultiSet<>();
					counts.add(group.identity());
					for (int h = 0; h < H; h++) {
						T current = A[h][w2 - 1];
						if (w1 > 0) {
							current = group.mul(current, group.inverse(A[h][w1 - 1]));
						}
						T target = group.mul(current, group.inverse(v));
						res += counts.getValue(target);
						counts.add(current);
					}
				}
			}
		}
		return res;
	}
}
