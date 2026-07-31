package library.util.algebra.strategy;

/**
 * 整数環 Z 上のユークリッド整域としての代数的構造。
 */
public class ZStrategy implements EuclideanDomainStrategy<Long>, ExactDivRingStrategy<Long> {
	@Override
	public Long zero() {
		return 0L;
	}

	@Override
	public Long one() {
		return 1L;
	}

	@Override
	public Long add(Long a, Long b) {
		return a + b;
	}

	@Override
	public Long mul(Long a, Long b) {
		return a * b;
	}

	@Override
	public Long neg(Long a) {
		return -a;
	}

	@Override
	public boolean equals(Long a, Long b) {
		return a.equals(b);
	}

	@Override
	public Long div(Long a, Long b) {
		return a / b;
	}

	@Override
	public Long mod(Long a, Long b) {
		return a % b;
	}

	@Override
	public long norm(Long a) {
		return Math.abs(a);
	}

	@Override
	public Long exactDiv(Long a, Long b) {
		if (b == 0) throw new ArithmeticException("Division by zero");
		if (a % b != 0) throw new ArithmeticException("Not exactly divisible");
		return a / b;
	}

	@Override
	public Long canonicalUnit(Long a) {
		return a < 0 ? -1L : 1L;
	}
}
