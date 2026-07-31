package library.util.algebra.instance.impl;

import library.util.algebra.instance.*;

import library.util.algebra.strategy.RingStrategy;
import library.util.algebra.strategy.ZnStrategy;

public class ZnElement implements CommutativeRingElement<ZnElement> {
	public long val;
	private final ZnStrategy strategy;

	private final RingStrategy<ZnElement> parent = new RingStrategy<>() {
		@Override public ZnElement zero() { return ZnElement.this.zero(); }
		@Override public ZnElement one() { return ZnElement.this.one(); }
		@Override public ZnElement add(ZnElement a, ZnElement b) { return a.add(b); }
		@Override public ZnElement mul(ZnElement a, ZnElement b) { return a.mul(b); }
		@Override public ZnElement neg(ZnElement a) { return a.neg(); }
		@Override public boolean equals(ZnElement a, ZnElement b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<ZnElement> parent() {
		return parent;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public ZnElement self() {
		return this;
	}

	public ZnElement(long val, long mod) {
		this.val = val;
		this.strategy = new ZnStrategy(mod);
	}

	private ZnElement(long val, ZnStrategy strategy) {
		this.val = val;
		this.strategy = strategy;
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
	public ZnElement add(ZnElement a) {
		return new ZnElement(strategy.add(val, a.val), strategy);
	}

	@Override
	public ZnElement mul(ZnElement a) {
		return new ZnElement(strategy.mul(val, a.val), strategy);
	}

	public ZnElement neg() {
		return new ZnElement(strategy.neg(val), strategy);
	}

	@Override
	public ZnElement one() {
		return new ZnElement(strategy.one(), strategy);
	}

	@Override
	public ZnElement zero() {
		return new ZnElement(strategy.zero(), strategy);
	}
}
