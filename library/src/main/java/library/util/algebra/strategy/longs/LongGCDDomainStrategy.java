package library.util.algebra.strategy.longs;

/**
 * primitive long に特化したGCD整域の代数的構造。
 */
public interface LongGCDDomainStrategy extends LongIntegralDomainStrategy {
	/**
	 * @param a
	 * @param b
	 * @return gcd(a, b)
	 */
	long gcd(long a, long b);
}
