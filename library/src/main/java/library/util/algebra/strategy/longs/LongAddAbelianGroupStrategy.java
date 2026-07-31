package library.util.algebra.strategy.longs;

/**
 * 加法アーベル群。
 */
public enum LongAddAbelianGroupStrategy implements LongAbelianGroupStrategy {
	STRATEGY;

	@Override
	public long identity() {
		return 0;
	}

	@Override
	public long mul(long a, long b) {
		return a + b;
	}

	@Override
	public long inverse(long a) {
		return -a;
	}
}
