package library.util.algebra.strategy.longs;

/**
 * primitive long に特化した体の代数的構造。
 */
public interface LongFieldStrategy extends LongEuclideanDomainStrategy, LongExactDivRingStrategy {
	@Override
	default long divExact(long a, long b) {
		return div(a, b);
	}

	/**
	 * @param a
	 * @return a^-1
	 */
	long inv(long a);

	/**
	 * @param a
	 * @param b
	 * @return a / b
	 */
	default long div(long a, long b) {
		return mul(a, inv(b));
	}

	/**
	 * 1 + a + a^2 + ... = 1 / (1 - a) を計算する。
	 * @param a 公比
	 * @return 等比級数の和
	 */
	default long geometricSum(long a) {
		return inv(sub(one(), a));
	}

	@Override
	default ExtGCDResult extgcd(long a, long b) {
		if (!equals(a, zero())) {
			return new ExtGCDResult(inv(a), zero(), one());
		} else if (!equals(b, zero())) {
			return new ExtGCDResult(zero(), inv(b), one());
		} else {
			return new ExtGCDResult(zero(), zero(), zero());
		}
	}
}
