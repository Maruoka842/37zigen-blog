package library.util.monoid;

import library.util.algebra.instance.MonoidElement;
import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * 最大値とその出現回数を保持するモノイド。
 * (max, count) のペアを元とし、(max1, count1) * (max2, count2) =
 *   max1 > max2 のとき (max1, count1)
 *   max1 < max2 のとき (max2, count2)
 *   max1 = max2 のとき (max1, count1 + count2)
 * と定義される。
 */
public class MaxAndCount implements MonoidElement<MaxAndCount> {
	/** 最大値。 */
	public final long max;
	/** 出現回数。 */
	public final int count;

	/**
	 * MaxAndCount モノイドのストラテジ。
	 */
	public static final MonoidStrategy<MaxAndCount> STRATEGY = new MonoidStrategy<>() {
		/**
		 * 単位元 (Long.MIN_VALUE, 0) を返す。
		 * 未テスト。
		 * 計算量: O(1)。
		 * @return 単位元
		 */
		@Override
		public MaxAndCount identity() {
			return new MaxAndCount(Long.MIN_VALUE, 0);
		}

		/**
		 * 2つの MaxAndCount をマージする。
		 * 最大値が大きい方を返し、最大値が等しい場合は回数を合算した新しい MaxAndCount を返す。
		 * 未テスト。
		 * 計算量: O(1)。
		 * @param t 左項
		 * @param u 右項
		 * @return マージ結果
		 */
		@Override
		public MaxAndCount mul(MaxAndCount t, MaxAndCount u) {
			if (t.max > u.max) return t;
			else if (u.max > t.max) return u;
			else return new MaxAndCount(t.max, t.count + u.count);
		}
	};

	/**
	 * コンストラクタ。
	 * @param max 最大値
	 * @param count 出現回数
	 */
	public MaxAndCount(long max, int count) {
		this.max = max;
		this.count = count;
	}

	/**
	 * 親モノイドのストラテジを返す。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @return ストラテジ
	 */
	@Override
	public MonoidStrategy<MaxAndCount> parent() {
		return STRATEGY;
	}

	/**
	 * 自身を返す。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @return 自身
	 */
	@Override
	public MaxAndCount self() {
		return this;
	}

	@Override
	public String toString() {
		return "(" + max + ", " + count + ")";
	}
}
