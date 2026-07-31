package library.util.algebra.strategy.monoid;

import library.util.DiscreteLogarithm;

public interface MonoidStrategy<Monoid> {
	Monoid identity();
	Monoid mul(Monoid a, Monoid b);
	default Monoid pow(Monoid a, long n) {
		if (n < 0) throw new AssertionError();
		Monoid ret=identity();
		while(n!=0) {
			if(n%2==1)ret=mul(ret, a);
			a=mul(a, a);
			n/=2;
		}
		return ret;
	}

	/**
	 * Solve min_n a^n = t (0 <= n <= maxSearch).
	 */
	default long discreteLog(Monoid a, Monoid t, long maxSearch) {
		return DiscreteLogarithm.discreteLog(a, identity(), t, this::mul, this::mul, maxSearch);
	}
}
