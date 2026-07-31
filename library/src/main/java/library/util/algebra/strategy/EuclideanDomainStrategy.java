package library.util.algebra.strategy;

public interface EuclideanDomainStrategy<T> extends GCDDomainStrategy<T> {
	T div(T a, T b);
	T mod(T a, T b);
	long norm(T a);

	/**
	 * a = u * canonical(a) となる単元 u を返す。
	 */
	default T canonicalUnit(T a) {
		return one();
	}

	@Override
	default T gcd(T a, T b) {
		while (!equals(b, zero())) {
			a = mod(a, b);
			T t = a; a = b; b = t;
		}
		if (equals(a, zero())) return a;
		return div(a, canonicalUnit(a));
	}

	record ExtGCDResult<T>(T x, T y, T gcd) {}

	default ExtGCDResult<T> extgcd(T a, T b) {
		T x0 = one(), y0 = zero(), g0 = a;
		T x1 = zero(), y1 = one(), g1 = b;
		while (!equals(g1, zero())) {
			T q = div(g0, g1);
			T nextG = sub(g0, mul(q, g1));
			T nextX = sub(x0, mul(q, x1));
			T nextY = sub(y0, mul(q, y1));
			x0 = x1; y0 = y1; g0 = g1;
			x1 = nextX; y1 = nextY; g1 = nextG;
		}
		if (equals(g0, zero())) return new ExtGCDResult<>(x0, y0, g0);
		T u = canonicalUnit(g0);
		return new ExtGCDResult<>(div(x0, u), div(y0, u), div(g0, u));
	}
}
