package library.util.algebra.instance;

import library.util.algebra.strategy.SemiRingStrategy;

public interface SemiRingElement<X> {
	
	SemiRingStrategy<X> parent();
	X self();
	
    default X add(X a) {
        return parent().add(self(), a);
    }

    default X mul(X a) {
        return parent().mul(self(), a);
    }
    
	default X pow(long n) {
		return parent().pow(self(), n);
	}

	default X one() {
		return parent().one();
	}

	default X zero() {
		return parent().zero();
	}
}
