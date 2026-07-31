package library.util.algebra.instance;

import library.util.algebra.strategy.RingStrategy;

public interface RingElement<X> extends SemiRingElement<X> {
    @Override
    RingStrategy<X> parent();
	
	default X sub(X a) {
		return parent().sub(self(), a);
	}
	
	default X neg() {
		return parent().neg(self());
	}
}
