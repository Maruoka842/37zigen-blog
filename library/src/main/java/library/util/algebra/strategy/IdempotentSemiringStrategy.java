package library.util.algebra.strategy;

public interface IdempotentSemiringStrategy<T> {
	T add(T a, T b);//冪等
	T mul(T a, T b);
	T zero();
	T one();
}
