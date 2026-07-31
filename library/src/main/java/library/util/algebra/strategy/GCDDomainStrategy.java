package library.util.algebra.strategy;

public interface GCDDomainStrategy<T> extends IntegralDomainStrategy<T> {
	T gcd(T a, T b);
}
