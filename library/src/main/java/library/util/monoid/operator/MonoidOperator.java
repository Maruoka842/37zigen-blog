package library.util.monoid.operator;

import java.util.function.BinaryOperator;

import library.util.algebra.instance.MonoidElement;

public class MonoidOperator {
	public static class Pair_int_long {
		public int key;
		public long val;
		public Pair_int_long(int key, long val) {
			this.key = key;
			this.val = val;
		}
		
		public Pair_int_long of(int key, long val) {
			return new Pair_int_long(key, val);
		}
		
		@Override
		public String toString() {
			return "(" +key+","+val+")";
		}
	}
	

	public static BinaryOperator<Long> add = new BinaryOperator<Long>() {
		
		@Override
		public Long apply(Long left, Long right) {
		    if (left.equals(Long.MAX_VALUE) && right.equals(Long.MIN_VALUE)) throw new AssertionError();
		    if (left.equals(Long.MIN_VALUE) && right.equals(Long.MAX_VALUE)) throw new AssertionError();
		    if (left.equals(Long.MAX_VALUE) || right.equals(Long.MAX_VALUE)) return Long.MAX_VALUE;
		    if (left.equals(Long.MIN_VALUE) || right.equals(Long.MIN_VALUE)) return Long.MIN_VALUE;
		    return left + right;
		}
	};
	
	public static BinaryOperator<Pair_int_long> argMin = new BinaryOperator<MonoidOperator.Pair_int_long>() {
		
		@Override
		public Pair_int_long apply(Pair_int_long t, Pair_int_long u) {
			if (t.val <= u.val) return t;
			else return u;
		}
	};
	
	public static BinaryOperator<Pair_int_long> argMax = new BinaryOperator<MonoidOperator.Pair_int_long>() {
		
		@Override
		public Pair_int_long apply(Pair_int_long t, Pair_int_long u) {
			if (t.val >= u.val) return t;
			else return u;
		}
	};
	
	/**
	 * 2つのモノイドの元をマージ（積）した結果を返す。
	 * 未テスト。
	 * @param a 左項
	 * @param b 右項
	 * @param <T> モノイドの元の型
	 * @return a * b
	 */
	public static <T extends MonoidElement<T>> T merge(T a, T b) {
		return a.mul(b.self());
	}

}
