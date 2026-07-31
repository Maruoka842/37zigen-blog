package library.util.algebra.strategy.longs;

/**
 * 整数環 Z 上の代数的構造。
 */
public class LongZStrategy implements LongEuclideanDomainStrategy, LongUFDStrategy, LongExactDivRingStrategy {
	@Override
	public long zero() {
		return 0;
	}

	@Override
	public long one() {
		return 1;
	}

	@Override
	public long add(long a, long b) {
		return a + b;
	}

	@Override
	public long mul(long a, long b) {
		return a * b;
	}

	@Override
	public long neg(long a) {
		return -a;
	}

	@Override
	public boolean equals(long a, long b) {
		return a == b;
	}

	@Override
	public long div(long a, long b) {
		return a / b;
	}

	@Override
	public long mod(long a, long b) {
		return a % b;
	}

	@Override
	public long norm(long a) {
		return Math.abs(a);
	}

	@Override
	public long divExact(long a, long b) {
		return a / b;
	}

	@Override
	public long canonicalUnit(long a) {
		return a < 0 ? -1 : 1;
	}
}
