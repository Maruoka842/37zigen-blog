package library.util.algebra.instance.impl;

import java.math.BigInteger;

/**
 * BigInteger 係数のガウス整数。
 */
public record BigGaussInt(BigInteger a, BigInteger b) {
	public static final BigGaussInt ZERO = new BigGaussInt(BigInteger.ZERO, BigInteger.ZERO);
	public static final BigGaussInt ONE = new BigGaussInt(BigInteger.ONE, BigInteger.ZERO);
	public static final BigGaussInt I = new BigGaussInt(BigInteger.ZERO, BigInteger.ONE);

	public BigGaussInt add(BigGaussInt x) {
		return new BigGaussInt(a.add(x.a), b.add(x.b));
	}

	public BigGaussInt sub(BigGaussInt x) {
		return new BigGaussInt(a.subtract(x.a), b.subtract(x.b));
	}

	public BigGaussInt neg() {
		return new BigGaussInt(a.negate(), b.negate());
	}

	public BigGaussInt mul(BigGaussInt x) {
		return new BigGaussInt(
			a.multiply(x.a).subtract(b.multiply(x.b)),
			a.multiply(x.b).add(b.multiply(x.a))
		);
	}

	public BigInteger norm() {
		return a.multiply(a).add(b.multiply(b));
	}

	public BigGaussInt conj() {
		return new BigGaussInt(a, b.negate());
	}

	public BigGaussInt div(BigGaussInt x) {
		BigInteger n = x.norm();
		if (n.signum() == 0) throw new ArithmeticException("/ by zero");
		BigInteger na = a.multiply(x.a).add(b.multiply(x.b));
		BigInteger nb = b.multiply(x.a).subtract(a.multiply(x.b));
		return new BigGaussInt(roundDiv(na, n), roundDiv(nb, n));
	}

	private static BigInteger roundDiv(BigInteger a, BigInteger b) {
		BigInteger[] qr = a.divideAndRemainder(b);
		BigInteger q = qr[0];
		BigInteger r = qr[1];
		if (r.abs().shiftLeft(1).compareTo(b) >= 0) {
			q = q.add(BigInteger.valueOf(r.signum()));
		}
		return q;
	}

	public BigGaussInt rem(BigGaussInt x) {
		return sub(this.div(x).mul(x));
	}

	public static BigGaussInt gcd(BigGaussInt x, BigGaussInt y) {
		while (!y.isZero()) {
			BigGaussInt r = x.rem(y);
			x = y;
			y = r;
		}
		return x.normalize();
	}

	public BigGaussInt normalize() {
		BigGaussInt cur = this;
		for (int i = 0; i < 4; i++) {
			if (cur.a.signum() > 0 && cur.b.signum() >= 0) return cur;
			// multiply by i: (a+bi)i = -b + ai
			cur = new BigGaussInt(cur.b.negate(), cur.a);
		}
		return ZERO;
	}

	public boolean isZero() {
		return a.signum() == 0 && b.signum() == 0;
	}
}
