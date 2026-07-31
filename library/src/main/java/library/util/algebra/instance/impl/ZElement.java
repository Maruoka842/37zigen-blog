package library.util.algebra.instance.impl;

import library.util.algebra.instance.*;

import library.util.algebra.strategy.RingStrategy;
import library.util.algebra.strategy.ZStrategy;

public class ZElement implements EuclideanDomainElement<ZElement>, ExactDivRingElement<ZElement> {
	/** 整数環 ZElement の元を表す値。 */
	public long val;
	/** 整数環 ZElement の演算を実装する共有ストラテジ。 */
	private static final ZStrategy strategy = new ZStrategy();

	private final RingStrategy<ZElement> parent = new RingStrategy<>() {
		@Override public ZElement zero() { return ZElement.this.zero(); }
		@Override public ZElement one() { return ZElement.this.one(); }
		@Override public ZElement add(ZElement a, ZElement b) { return a.add(b); }
		@Override public ZElement mul(ZElement a, ZElement b) { return a.mul(b); }
		@Override public ZElement neg(ZElement a) { return a.neg(); }
		@Override public boolean equals(ZElement a, ZElement b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<ZElement> parent() {
		return parent;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public ZElement self() {
		return this;
	}

	public ZElement(long val) {
		this.val = val;
	}

	/**
	 * x + a を返す。
	 * 未テスト。
	 * 事前条件: this と a は同一半環 S の元, a != null。
	 * 事後条件: 戻り値 = this + a。
	 * 副作用: なし。
	 * 計算量: strategy.add に同じ。
	 * 破壊的変更: this と a を変更しない。
	 * 参照共有・所有権: 戻り値は this.strategy と同じストラテジを共有し、所有権は移動しない。
	 * 例外・未定義条件: a == null、または this と a が同一半環 S の元でないとき未定義。
	 * @param a 加数
	 * @return this + a
	 */
	@Override
	public ZElement add(ZElement a) {
		return new ZElement(strategy.add(val, a.val));
	}

	@Override
	public ZElement mul(ZElement a) {
		return new ZElement(strategy.mul(val, a.val));
	}

	public ZElement neg() {
		return new ZElement(strategy.neg(val));
	}

	@Override
	public ZElement exactDiv(ZElement a) {
		return new ZElement(strategy.exactDiv(val, a.val));
	}

	@Override
	public ZElement gcd(ZElement a) {
		return new ZElement(strategy.gcd(val, a.val));
	}

	@Override
	public ZElement div(ZElement a) {
		return new ZElement(strategy.div(val, a.val));
	}

	@Override
	public ZElement mod(ZElement a) {
		return new ZElement(strategy.mod(val, a.val));
	}

	@Override
	public long norm() {
		return strategy.norm(val);
	}

	/**
	 * 乗法単位元 1 を返す。
	 * @return 1
	 */
	@Override
	public ZElement one() {
		return new ZElement(strategy.one());
	}

	/**
	 * 加法単位元 0 を返す。
	 * @return 0
	 */
	@Override
	public ZElement zero() {
		return new ZElement(strategy.zero());
	}
}
