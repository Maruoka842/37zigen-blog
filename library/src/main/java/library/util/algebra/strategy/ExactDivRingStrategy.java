package library.util.algebra.strategy;

public interface ExactDivRingStrategy<T> extends IntegralDomainStrategy<T> {
	T exactDiv(T a, T b);
}
