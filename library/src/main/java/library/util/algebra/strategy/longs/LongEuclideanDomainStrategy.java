package library.util.algebra.strategy.longs;

/**
 * primitive long に特化したユークリッド整域の代数的構造。
 */
public interface LongEuclideanDomainStrategy extends LongGCDDomainStrategy {
	/**
	 * @param a
	 * @param b
	 * @return a / b
	 */
	long div(long a, long b);
	/**
	 * @param a
	 * @param b
	 * @return a % b
	 */
	long mod(long a, long b);
	/**
	 * @param a
	 * @return a のノルム
	 */
	long norm(long a);

	/**
	 * a = u * canonical(a) となる単元 u を返す。
	 * @param a
	 * @return 単元 u
	 */
	default long canonicalUnit(long a) {
		return one();
	}

	@Override
	default long gcd(long a, long b) {
		while (!equals(b, zero())) {
			a = mod(a, b);
			long t = a; a = b; b = t;
		}
		if (equals(a, zero())) return a;
		return div(a, canonicalUnit(a));
	}

	record ExtGCDResult(long x, long y, long gcd) {}

	/**
	 * ax + by = gcd(a, b) を解く。
	 * @param a
	 * @param b
	 * @return 解 (x, y, gcd)
	 */
	default ExtGCDResult extgcd(long a, long b) {
		long x0 = one(), y0 = zero(), g0 = a;
		long x1 = zero(), y1 = one(), g1 = b;
		while (!equals(g1, zero())) {
			long q = div(g0, g1);
			long nextG = sub(g0, mul(q, g1));
			long nextX = sub(x0, mul(q, x1));
			long nextY = sub(y0, mul(q, y1));
			x0 = x1; y0 = y1; g0 = g1;
			x1 = nextX; y1 = nextY; g1 = nextG;
		}
		if (equals(g0, zero())) return new ExtGCDResult(x0, y0, g0);
		long u = canonicalUnit(g0);
		return new ExtGCDResult(div(x0, u), div(y0, u), div(g0, u));
	}
}
