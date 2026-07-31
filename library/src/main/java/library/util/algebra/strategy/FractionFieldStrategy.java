package library.util.algebra.strategy;

import library.util.algebra.instance.FractionFieldElement;

public class FractionFieldStrategy<T> implements FieldStrategy<FractionFieldElement<T>>{
	private final IntegralDomainStrategy<T> strategy;

	public FractionFieldStrategy(IntegralDomainStrategy<T> strategy) {
		this.strategy = strategy;
	}

	public FractionFieldElement<T> of(T num, T den) {
		if (strategy.equals(den, strategy.zero())) {
			throw new ArithmeticException("Division by zero");
		}
		return simplify(new FractionFieldElement<>(num, den, this));
	}

	public FractionFieldElement<T> from(T value) {
		return new FractionFieldElement<>(value, strategy.one(), this);
	}
  
	public FractionFieldElement<T> zero() {
		return new FractionFieldElement<>(strategy.zero(), strategy.one(), this);
	}

	@Override
	public FractionFieldElement<T> one() {
		return new FractionFieldElement<>(strategy.one(), strategy.one(), this);
	}

	@Override
	public FractionFieldElement<T> add(FractionFieldElement<T> a, FractionFieldElement<T> b) {
		T num = strategy.add(strategy.mul(a.num(), b.den()), strategy.mul(b.num(), a.den()));
		T den = strategy.mul(a.den(), b.den());
		return simplify(new FractionFieldElement<>(num, den, this));
	}

	@Override
	public FractionFieldElement<T> mul(FractionFieldElement<T> a, FractionFieldElement<T> b) {
		T num = strategy.mul(a.num(), b.num());
		T den = strategy.mul(a.den(), b.den());
		return simplify(new FractionFieldElement<>(num, den, this));
	}

	@Override
	public FractionFieldElement<T> neg(FractionFieldElement<T> a) {
		return new FractionFieldElement<>(strategy.neg(a.num()), a.den(), this);
	}

	@Override
	public FractionFieldElement<T> inv(FractionFieldElement<T> a) {
		if (strategy.equals(a.num(), strategy.zero())) {
			throw new ArithmeticException("Division by zero");
		}
		return simplify(new FractionFieldElement<>(a.den(), a.num(), this));
	}

	@Override
	public boolean equals(FractionFieldElement<T> a, FractionFieldElement<T> b) {
		return strategy.equals(strategy.mul(a.num(), b.den()), strategy.mul(b.num(), a.den()));
	}

	@Override
	public FractionFieldElement<T> mod(FractionFieldElement<T> a, FractionFieldElement<T> b) {
		if (equals(b, zero())) throw new ArithmeticException("Division by zero");
		return zero();
	}

	@Override
	public long norm(FractionFieldElement<T> a) {
		return equals(a, zero()) ? 0 : 1;
	}

	@Override
	public FractionFieldElement<T> canonicalUnit(FractionFieldElement<T> a) {
		return equals(a, zero()) ? one() : a;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FractionFieldStrategy<?> that = (FractionFieldStrategy<?>) o;
		return java.util.Objects.equals(strategy, that.strategy);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(strategy);
	}

	private FractionFieldElement<T> simplify(FractionFieldElement<T> f) {
		if (strategy instanceof GCDDomainStrategy<T> gs) {
			T g = gs.gcd(f.num(), f.den());
			T num = f.num();
			T den = f.den();
			if (!strategy.equals(g, strategy.zero()) && !strategy.equals(g, strategy.one())) {
				if (strategy instanceof ExactDivRingStrategy<T> eds) {
					num = eds.exactDiv(num, g);
					den = eds.exactDiv(den, g);
				} else if (strategy instanceof EuclideanDomainStrategy<T> ed) {
					num = ed.div(num, g);
					den = ed.div(den, g);
				}
			}
			if (strategy instanceof EuclideanDomainStrategy<T> ed) {
				T u = ed.canonicalUnit(den);
				if (!strategy.equals(u, strategy.one())) {
					num = ed.div(num, u);
					den = ed.div(den, u);
				}
			}
			return new FractionFieldElement<>(num, den, this);
		}
		return f;
	}
}
