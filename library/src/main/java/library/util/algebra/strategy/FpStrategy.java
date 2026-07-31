package library.util.algebra.strategy;

import library.util.MathUtils;

/**
 * 有限体 Z/pZ 上の体としての代数的構造。
 */
public class FpStrategy extends ZnStrategy implements FieldStrategy<Long> {
	public FpStrategy(long mod) {
		super(mod);
	}

	@Override
	public Long inv(Long a) {
		return MathUtils.modInv(a, mod);
	}

	@Override
	public Long div(Long a, Long b) {
		return mul(a, inv(b));
	}

	@Override
	public Long mod(Long a, Long b) {
		if (equals(b, zero())) throw new ArithmeticException("Division by zero");
		return zero();
	}

	@Override
	public long norm(Long a) {
		return equals(a, zero()) ? 0 : 1;
	}

	@Override
	public Long canonicalUnit(Long a) {
		if (equals(a, zero())) return one();
		return a;
	}
}
