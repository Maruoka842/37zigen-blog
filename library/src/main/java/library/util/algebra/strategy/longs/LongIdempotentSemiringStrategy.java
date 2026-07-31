package library.util.algebra.strategy.longs;

/**
 * primitive long に特化した冪等半環の代数的構造。
 */
public interface LongIdempotentSemiringStrategy {
	/**
	 * @param a
	 * @param b
	 * @return a + b (冪等)
	 */
	long add(long a, long b);
	/**
	 * @param a
	 * @param b
	 * @return a * b
	 */
	long mul(long a, long b);
	/**
	 * @return 加法の単位元
	 */
	long zero();
	/**
	 * @return 乗法の単位元
	 */
	long one();
}
