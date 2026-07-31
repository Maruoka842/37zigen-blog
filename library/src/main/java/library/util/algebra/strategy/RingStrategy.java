package library.util.algebra.strategy;

public interface RingStrategy<T> extends SemiRingStrategy<T> {
	T neg(T a);
	
	default T sub(T a, T b) {
		return add(a, neg(b));
	}
}
