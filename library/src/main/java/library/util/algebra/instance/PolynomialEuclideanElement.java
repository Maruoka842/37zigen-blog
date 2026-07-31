package library.util.algebra.instance;

import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.PolynomialEuclideanStrategy;
import library.util.algebra.strategy.RingStrategy;

public class PolynomialEuclideanElement<T> implements PolynomialElement<T, PolynomialEuclideanElement<T>>, EuclideanDomainElement<PolynomialEuclideanElement<T>> {
	public T[] coeffs;
	private final PolynomialEuclideanStrategy<T> strategy;

	private final RingStrategy<PolynomialEuclideanElement<T>> parent = new RingStrategy<>() {
		@Override public PolynomialEuclideanElement<T> zero() { return PolynomialEuclideanElement.this.zero(); }
		@Override public PolynomialEuclideanElement<T> one() { return PolynomialEuclideanElement.this.one(); }
		@Override public PolynomialEuclideanElement<T> add(PolynomialEuclideanElement<T> a, PolynomialEuclideanElement<T> b) { return a.add(b); }
		@Override public PolynomialEuclideanElement<T> mul(PolynomialEuclideanElement<T> a, PolynomialEuclideanElement<T> b) { return a.mul(b); }
		@Override public PolynomialEuclideanElement<T> neg(PolynomialEuclideanElement<T> a) { return a.neg(); }
		@Override public boolean equals(PolynomialEuclideanElement<T> a, PolynomialEuclideanElement<T> b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<PolynomialEuclideanElement<T>> parent() {
		return parent;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public PolynomialEuclideanElement<T> self() {
		return this;
	}

	public PolynomialEuclideanElement(T[] coeffs, FieldStrategy<T> fieldStrategy) {
		this.coeffs= coeffs;
		this.strategy = new PolynomialEuclideanStrategy<>(fieldStrategy);
	}

	private PolynomialEuclideanElement(T[] coeffs, PolynomialEuclideanStrategy<T> strategy) {
		this.coeffs = coeffs;
		this.strategy = strategy;
	}

	public T[] coeffs() {
		return coeffs;
	}

	@Override
	public PolynomialEuclideanStrategy<T> strategy() {
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
	public PolynomialEuclideanElement<T> add(PolynomialEuclideanElement<T> a) {
		return new PolynomialEuclideanElement<>(strategy.add(coeffs, a.coeffs()), strategy);
	}

	@Override
	public PolynomialEuclideanElement<T> mul(PolynomialEuclideanElement<T> a) {
		return new PolynomialEuclideanElement<>(strategy.mul(coeffs, a.coeffs()), strategy);
	}

	public PolynomialEuclideanElement<T> neg() {
		return new PolynomialEuclideanElement<>(strategy.neg(coeffs), strategy);
	}

	@Override
	public PolynomialEuclideanElement<T> gcd(PolynomialEuclideanElement<T> a) {
		return new PolynomialEuclideanElement<>(strategy.gcd(coeffs, a.coeffs), strategy);
	}

	@Override
	public PolynomialEuclideanElement<T> div(PolynomialEuclideanElement<T> a) {
		return new PolynomialEuclideanElement<>(strategy.div(coeffs, a.coeffs), strategy);
	}

	@Override
	public PolynomialEuclideanElement<T> mod(PolynomialEuclideanElement<T> a) {
		return new PolynomialEuclideanElement<>(strategy.mod(coeffs, a.coeffs), strategy);
	}

	@Override
	public long norm() {
		return strategy.norm(coeffs);
	}

	@Override
	public PolynomialEuclideanElement<T> one() {
		return new PolynomialEuclideanElement<>(strategy.one(), strategy);
	}

	@Override
	public PolynomialEuclideanElement<T> zero() {
		return new PolynomialEuclideanElement<>(strategy.zero(), strategy);
	}
}
