package library.util.segtree;

import java.util.function.BiFunction;

import library.util.monoid.MaxAndCount;
import library.util.monoid.operator.MonoidOperator;

public class Add_CountMax extends LazySegTree<Long, MaxAndCount> {

	Add_CountMax(int N) {
		super(N, MonoidOperator.add, MonoidOperator::merge, new BiFunction<Long, MaxAndCount, MaxAndCount>() {

			@Override
			public MaxAndCount apply(Long t, MaxAndCount u) {
				return new MaxAndCount(MonoidOperator.add.apply(u.max, t), u.count);
			}
		}, new MaxAndCount(Long.MIN_VALUE, 0));
	}

	public void set(int i, long val) {
		set(i, new MaxAndCount(val, 1));
	}

	/**
	 * set(i, Long.MIN_VALUE);
	 * 
	 * @param i
	 */
	public void delete(int i) {
		set(i, Long.MIN_VALUE);
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	@Override
	public void dump() {
		System.out.print("Add_CountMax: ");
		super.dump();
	}
}
