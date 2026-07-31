package library.util.algebra.strategy.longs;

/**
 * primitive long に特化した半環の代数的構造。
 */
public interface LongSemiRingStrategy {
	/**
	 * @return 加法の単位元
	 */
	long zero();
	/**
	 * @return 乗法の単位元
	 */
	long one();
	/**
	 * @param a
	 * @param b
	 * @return a + b
	 */
	long add(long a, long b);
	/**
	 * @param a
	 * @param b
	 * @return a * b
	 */
	long mul(long a, long b);
	/**
	 * @param a
	 * @param b
	 * @return a == b
	 */
	boolean equals(long a, long b);
}
