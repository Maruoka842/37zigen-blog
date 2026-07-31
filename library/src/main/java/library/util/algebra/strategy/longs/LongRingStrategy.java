package library.util.algebra.strategy.longs;

/**
 * primitive long に特化した環の代数的構造。
 */
public interface LongRingStrategy extends LongSemiRingStrategy {
	/**
	 * @param a
	 * @return -a
	 */
	long neg(long a);

	/**
	 * @param a
	 * @param b
	 * @return a - b
	 */
	default long sub(long a, long b) {
		return add(a, neg(b));
	}
}
