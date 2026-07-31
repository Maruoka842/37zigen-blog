package library.util.fold;

import java.util.Arrays;

import library.util.algebra.strategy.AbelianGroupStrategy;
import library.util.collections.HashMultiSet;

/**
 * 1次元の静的な点更新・区間和取得（アーベル群）
 * @param <T>
 */
public class StaticPointAddRangeSumAbelianGroup1D<T> {
	private final int N;
	private final T[] A;
	private final AbelianGroupStrategy<T> group;
	private boolean isBuilt = false;

	/**
	 * 長さNの配列を用意する
	 * @param N
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup1D(int N, AbelianGroupStrategy<T> group) {
		this.N = N;
		this.group = group;
		this.A = (T[]) new Object[N];
		Arrays.fill(A, group.identity());
	}

	/**
	 * 座標iにa[i]をセットして初期化する
	 * @param a
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup1D(T[] a, AbelianGroupStrategy<T> group) {
		this.N = a.length;
		this.group = group;
		this.A = a.clone();
	}

	/**
	 * 座標iにvalを足す
	 * @param i
	 * @param val
	 */
	public void add(int i, T val) {
		if (isBuilt) throw new AssertionError();
		A[i] = group.mul(A[i], val);
	}

	/**
	 * 累積和を構築する
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 累積和を構築する。</p>
	 * <p>副作用: Aの内容が書き換えられ、isBuiltがtrueになる。</p>
	 * <p>計算量: O(N)</p>
	 * <p>破壊的変更: あり。</p>
	 * 未テスト
	 */
	public void build() {
		if (isBuilt) throw new AssertionError();
		for (int i = 1; i < N; i++) {
			A[i] = group.mul(A[i - 1], A[i]);
		}
		isBuilt = true;
	}

	/**
	 * [leftInclusive, rightExclusive) の和を返す
	 * @param leftInclusive
	 * @param rightExclusive
	 * @return
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: [leftInclusive, rightExclusive) の和を返す。</p>
	 * <p>副作用: !isBuilt の場合、内部で build() を呼び出す。</p>
	 * <p>計算量: O(1)</p>
	 * <p>破壊的変更: build() が呼ばれる場合のみあり。</p>
	 * 未テスト
	 */
	public T getRangeSum(int leftInclusive, int rightExclusive) {
		if (!isBuilt) {
			build();
		}
		leftInclusive = Math.max(leftInclusive, 0);
		if (rightExclusive <= leftInclusive) return group.identity();
		rightExclusive = Math.min(rightExclusive, N);
		T res = A[rightExclusive - 1];
		if (leftInclusive > 0) {
			res = group.mul(res, group.inverse(A[leftInclusive - 1]));
		}
		return res;
	}

	/**
	 * 配列をクリアする
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 全ての要素を単位元にし、isBuiltをfalseにする。</p>
	 * <p>副作用: Aの内容が書き換えられる。</p>
	 * <p>計算量: O(N)</p>
	 * <p>破壊的変更: あり。</p>
	 * 未テスト
	 */
	public void clear() {
		isBuilt = false;
		Arrays.fill(A, group.identity());
	}

	/**
	 * 和が v となる区間の個数を返す。
	 *
	 * @param v ターゲットとなる和
	 * @return #{(l, r) | 0 <= l < r <= N, \sum_{i=l}^{r-1} a_i = v}
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 和が v となる区間の個数を返す。</li>
	 *   <li>副作用: !isBuilt の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(N)</li>
	 *   <li>破壊的変更: なし（build() による内部状態の変化を除く）。</li>
	 * </ul>
	 * 未テスト
	 */
	public long countRangeSum(T v) {
		if (!isBuilt) {
			build();
		}
		HashMultiSet<T> counts = new HashMultiSet<>();
		counts.add(group.identity());
		long res = 0;
		for (T s : A) {
			// s - prefix = v => prefix = s - v
			T target = group.mul(s, group.inverse(v));
			res += counts.getValue(target);
			counts.add(s);
		}
		return res;
	}
}
