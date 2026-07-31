package library.util.algebra.strategy;

public interface FieldStrategy<T> extends EuclideanDomainStrategy<T>, ExactDivRingStrategy<T> {
	@Override
	default T exactDiv(T a, T b) {
		return div(a, b);
	}

	T inv(T a);

	default T div(T a, T b) {
		return mul(a, inv(b));
	}
	
	default T mod(T a, T b) {
		return zero();
	}

	@Override
	default T pow(T a, long n) {
		if (n < 0) return pow(inv(a), -n);
		return EuclideanDomainStrategy.super.pow(a, n);
	}

	/**
	 * 1 + a + a^2 + ... = 1 / (1 - a) を計算する。
	 * @param a 公比
	 * @return 等比級数の和
	 */
	default T geometricSum(T a) {
		return inv(sub(one(), a));
	}

	default EuclideanDomainStrategy.ExtGCDResult<T> extgcd(T a, T b) {
		if (!equals(a, zero())) {
			return new EuclideanDomainStrategy.ExtGCDResult<>(inv(a), zero(), one());
		} else if (!equals(b, zero())) {
			return new EuclideanDomainStrategy.ExtGCDResult<>(zero(), inv(b), one());
		} else {
			return new EuclideanDomainStrategy.ExtGCDResult<>(zero(), zero(), zero());
		}
	}
}
