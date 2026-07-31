package library.util.segtree;

import java.util.function.BiFunction;

import library.util.monoid.operator.MonoidOperator;
import library.util.monoid.operator.MonoidOperator.Pair_int_long;

public class Add_Argmax extends LazySegTree<Long, MonoidOperator.Pair_int_long>{

    Add_Argmax(int N) {
    	super(N, Long::sum, MonoidOperator.argMax, new BiFunction<Long, MonoidOperator.Pair_int_long, MonoidOperator.Pair_int_long>(){

			@Override
			public MonoidOperator.Pair_int_long apply(Long t, MonoidOperator.Pair_int_long u) {
				return new MonoidOperator.Pair_int_long(u.key, u.val+t);
			}
    	}, new MonoidOperator.Pair_int_long(-1, Long.MIN_VALUE));
    }

    public void set(int i, long val) {
        set(i, new MonoidOperator.Pair_int_long(i, val));
    }
    
    /**
     * set(i, Long.MIN_VALUE);
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
		System.out.print("Add_Argmax: ");
		super.dump();
	}
}
