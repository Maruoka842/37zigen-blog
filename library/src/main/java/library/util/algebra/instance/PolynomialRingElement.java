package library.util.algebra.instance;

import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.PolynomialRingStrategy;
import library.util.algebra.strategy.RingStrategy;

public class PolynomialRingElement<T> implements PolynomialElement<T, PolynomialRingElement<T>>, ExactDivRingElement<PolynomialRingElement<T>> {
	public T[] coeffs;
	protected final PolynomialRingStrategy<T> strategy;

	private final RingStrategy<PolynomialRingElement<T>> parentRing = new RingStrategy<>() {
		@Override public PolynomialRingElement<T> zero() { return PolynomialRingElement.this.zero(); }
		@Override public PolynomialRingElement<T> one() { return PolynomialRingElement.this.one(); }
		@Override public PolynomialRingElement<T> add(PolynomialRingElement<T> a, PolynomialRingElement<T> b) { return a.add(b); }
		@Override public PolynomialRingElement<T> mul(PolynomialRingElement<T> a, PolynomialRingElement<T> b) { return a.mul(b); }
		@Override public PolynomialRingElement<T> neg(PolynomialRingElement<T> a) { return a.neg(); }
		@Override public boolean equals(PolynomialRingElement<T> a, PolynomialRingElement<T> b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<PolynomialRingElement<T>> parent() {
		return parentRing;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public PolynomialRingElement<T> self() {
		return this;
	}

	public PolynomialRingElement(T[] val, CommutativeRingStrategy<T> baseStrategy) {
		this.coeffs = val;
		this.strategy = new PolynomialRingStrategy<>(baseStrategy);
	}

	protected PolynomialRingElement(T[] val, PolynomialRingStrategy<T> strategy) {
		this.coeffs = val;
		this.strategy = strategy;
	}

	@Override
	public PolynomialRingStrategy<T> strategy() {
		return strategy;
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
	public PolynomialRingElement<T> add(PolynomialRingElement<T> a) {
		return new PolynomialRingElement<>(strategy.add(coeffs, a.coeffs()), strategy);
	}

	public void mergeInto(PolynomialRingElement<T> a) {
		a.coeffs = strategy.add(coeffs, a.coeffs());
	}



	@Override
	public PolynomialRingElement<T> mul(PolynomialRingElement<T> a) {
		return new PolynomialRingElement<>(strategy.mul(coeffs, a.coeffs()), strategy);
	}

	public PolynomialRingElement<T> neg() {
		return new PolynomialRingElement<>(strategy.neg(coeffs), strategy);
	}

	@Override
	public PolynomialRingElement<T> exactDiv(PolynomialRingElement<T> a) {
		return new PolynomialRingElement<>(strategy.exactDiv(coeffs, a.coeffs), strategy);
	}

	public T[] coeffs() {
		return coeffs;
	}

	@Override
	public PolynomialRingElement<T> one() {
		return new PolynomialRingElement<>(strategy.one(), strategy);
	}

	@Override
	public PolynomialRingElement<T> zero() {
		return new PolynomialRingElement<>(strategy.zero(), strategy);
	}
}
