package library.util.algebra.instance;

import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import library.util.algebra.strategy.RingStrategy;
import java.util.Arrays;
import java.util.Objects;

public class FractionFieldElement<T> implements FieldElement<FractionFieldElement<T>> {
	public final T num, den;
	private final FractionFieldStrategy<T> strategy;

	private final RingStrategy<FractionFieldElement<T>> parent = new RingStrategy<>() {
		@Override public FractionFieldElement<T> zero() { return FractionFieldElement.this.zero(); }
		@Override public FractionFieldElement<T> one() { return FractionFieldElement.this.one(); }
		@Override public FractionFieldElement<T> add(FractionFieldElement<T> a, FractionFieldElement<T> b) { return a.add(b); }
		@Override public FractionFieldElement<T> mul(FractionFieldElement<T> a, FractionFieldElement<T> b) { return a.mul(b); }
		@Override public FractionFieldElement<T> neg(FractionFieldElement<T> a) { return a.neg(); }
		@Override public boolean equals(FractionFieldElement<T> a, FractionFieldElement<T> b) { return a.equals(b); }
	};

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public RingStrategy<FractionFieldElement<T>> parent() {
		return parent;
	}

	/**
	 * 未テスト。
	 * 計算量: O(1)。
	 */
	@Override
	public FractionFieldElement<T> self() {
		return this;
	}

	public FractionFieldElement(T num, T den, IntegralDomainStrategy<T> baseStrategy) {
		this.num = num;
		this.den = den;
		this.strategy = new FractionFieldStrategy<>(baseStrategy);
	}

	public FractionFieldElement(T num, T den, FractionFieldStrategy<T> strategy) {
		this.num = num;
		this.den = den;
		this.strategy = strategy;
	}

	public T num() {
		return num;
	}

	public T den() {
		return den;
	}

	@Override
	public FractionFieldElement<T> add(FractionFieldElement<T> a) {
		return strategy.add(this, a);
	}

	@Override
	public FractionFieldElement<T> mul(FractionFieldElement<T> a) {
		return strategy.mul(this, a);
	}

	public FractionFieldElement<T> neg() {
		return strategy.neg(this);
	}

	public FractionFieldElement<T> inv() {
		return strategy.inv(this);
	}

	@Override
	public FractionFieldElement<T> gcd(FractionFieldElement<T> a) {
		return strategy.gcd(this, a);
	}

	@Override
	public FractionFieldElement<T> div(FractionFieldElement<T> a) {
		return strategy.div(this, a);
	}

	@Override
	public FractionFieldElement<T> mod(FractionFieldElement<T> a) {
		return strategy.mod(this, a);
	}

	@Override
	public long norm() {
		return strategy.norm(this);
	}

	@Override
	public FractionFieldElement<T> one() {
		return strategy.one();
	}

	@Override
	public FractionFieldElement<T> zero() {
		return strategy.zero();
	}

	@Override
	public String toString() {
		String ns = num instanceof long[] a ? Arrays.toString(a) : String.valueOf(num);
		String ds = den instanceof long[] a ? Arrays.toString(a) : String.valueOf(den);
		return ns + "/" + ds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FractionFieldElement<?> that)) return false;
		return Objects.deepEquals(num, that.num) && Objects.deepEquals(den, that.den);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Objects.hashCode(num), Objects.hashCode(den));
	}
}
