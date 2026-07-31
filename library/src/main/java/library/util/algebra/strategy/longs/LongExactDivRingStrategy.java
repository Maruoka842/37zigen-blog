package library.util.algebra.strategy.longs;

/**
 * primitive long に特化した除法が可能な環の代数的構造。
 */
public interface LongExactDivRingStrategy extends LongIntegralDomainStrategy {
	/**
	 * 割り切れることが保証されている場合に a / b を計算する。
	 * @param a
	 * @param b
	 * @return a / b
	 */
	long divExact(long a, long b);
}
