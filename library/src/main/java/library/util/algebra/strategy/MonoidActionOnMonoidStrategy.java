package library.util.algebra.strategy;

import library.util.algebra.strategy.monoid.MonoidStrategy;

public interface MonoidActionOnMonoidStrategy<F, X> extends MonoidActionStrategy<F, X> {
	public MonoidStrategy<X> actedMonoidStrategy();
}
