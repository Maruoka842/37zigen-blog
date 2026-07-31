package library.util.monoid;

import library.util.algebra.instance.MonoidElement;
import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * バイナリ列の反転数を管理するためのモノイド。
 * c0: 0の個数
 * c1: 1の個数
 * c01: (0, 1) というペアの個数
 * c10: (1, 0) というペアの個数（反転数）
 */
public class BinaryInversionState implements MonoidElement<BinaryInversionState> {
	/** 0の個数。 */
	public final long c0;
	/** 1の個数。 */
	public final long c1;
	/** (0, 1) というペアの個数。 */
	public final long c01;
	/** (1, 0) というペアの個数。 */
	public final long c10;

	/**
	 * BinaryInversionState モノイドのストラテジ。
	 */
	public static final MonoidStrategy<BinaryInversionState> STRATEGY = new MonoidStrategy<>() {
		/**
		 * 単位元 (0, 0, 0, 0) を返す。
		 * 未テスト。
		 * 計算量: O(1)。
		 * @return 単位元
		 */
		@Override
		public BinaryInversionState identity() {
			return new BinaryInversionState(0, 0, 0, 0);
		}

		/**
		 * 2つの BinaryInversionState をマージする。
		 * 未テスト。
		 * 計算量: O(1)。
		 * @param a 左項
		 * @param b 右項
		 * @return マージ結果
		 */
		@Override
		public BinaryInversionState mul(BinaryInversionState a, BinaryInversionState b) {
			return new BinaryInversionState(
					a.c0 + b.c0,
					a.c1 + b.c1,
					a.c01 + b.c01 + a.c0 * b.c1,
					a.c10 + b.c10 + a.c1 * b.c0
			);
		}
	};

	/**
	 * コンストラクタ。
	 * @param c0 0の個数
	 * @param c1 1の個数
	 * @param c01 (0, 1) というペアの個数
	 * @param c10 (1, 0) というペアの個数
	 */
	public BinaryInversionState(long c0, long c1, long c01, long c10) {
		this.c0 = c0;
		this.c1 = c1;
		this.c01 = c01;
		this.c10 = c10;
	}

	/**
	 * 値 v から状態を生成する。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @param v 0 または 1
	 * @return 状態
	 */
	public static BinaryInversionState of(int v) {
		if (v == 0) return new BinaryInversionState(1, 0, 0, 0);
		else return new BinaryInversionState(0, 1, 0, 0);
	}

	/**
	 * 0 と 1 を反転させた状態を返す。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @return 反転後の状態
	 */
	public BinaryInversionState flip() {
		return new BinaryInversionState(c1, c0, c10, c01);
	}

	/**
	 * 親モノイドのストラテジを返す。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @return ストラテジ
	 */
	@Override
	public MonoidStrategy<BinaryInversionState> parent() {
		return STRATEGY;
	}

	/**
	 * 自身を返す。
	 * 未テスト。
	 * 計算量: O(1)。
	 * @return 自身
	 */
	@Override
	public BinaryInversionState self() {
		return this;
	}

	@Override
	public String toString() {
		return "BinaryInversionState{c0=" + c0 + ", c1=" + c1 + ", c01=" + c01 + ", c10=" + c10 + "}";
	}
}
