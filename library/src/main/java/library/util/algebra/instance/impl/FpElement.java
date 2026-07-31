package library.util.algebra.instance.impl;

import library.util.algebra.instance.*;

import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.RingStrategy;

public class FpElement implements FieldElement<FpElement>, ExactDivRingElement<FpElement> {
	public long val;
	private final FpStrategy strategy;

	private final RingStrategy<FpElement> parent = new RingStrategy<>() {
		@Override public FpElement zero() { return FpElement.this.zero(); }
		@Override public FpElement one() { return FpElement.this.one(); }
		@Override public FpElement add(FpElement a, FpElement b) { return a.add(b); }
		@Override public FpElement mul(FpElement a, FpElement b) { return a.mul(b); }
		@Override public FpElement neg(FpElement a) { return a.neg(); }
		@Override public boolean equals(FpElement a, FpElement b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<FpElement> parent() {
		return parent;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public FpElement self() {
		return this;
	}

	public FpElement(long val, long mod) {
		this.val = val;
		this.strategy = new FpStrategy(mod);
	}

	private FpElement(long val, FpStrategy strategy) {
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
	public FpElement add(FpElement a) {
		return new FpElement(strategy.add(val, a.val), strategy);
	}

	@Override
	public FpElement mul(FpElement a) {
		return new FpElement(strategy.mul(val, a.val), strategy);
	}

	@Override
	public FpElement neg() {
		return new FpElement(strategy.neg(val), strategy);
	}

	@Override
	public FpElement exactDiv(FpElement a) {
		return new FpElement(strategy.exactDiv(val, a.val), strategy);
	}

	@Override
	public FpElement gcd(FpElement a) {
		return new FpElement(strategy.gcd(val, a.val), strategy);
	}

	@Override
	public FpElement div(FpElement a) {
		return new FpElement(strategy.div(val, a.val), strategy);
	}

	@Override
	public FpElement mod(FpElement a) {
		return new FpElement(strategy.mod(val, a.val), strategy);
	}

	@Override
	public long norm() {
		return strategy.norm(val);
	}

	@Override
	public FpElement inv() {
		return new FpElement(strategy.inv(val), strategy);
	}

	/**
	 * 乗法単位元 1 を返す。
	 * @return 1
	 */
	@Override
	public FpElement one() {
		return new FpElement(strategy.one(), strategy);
	}

	/**
	 * 加法単位元 0 を返す。
	 * @return 0
	 */
	@Override
	public FpElement zero() {
		return new FpElement(strategy.zero(), strategy);
	}
}
