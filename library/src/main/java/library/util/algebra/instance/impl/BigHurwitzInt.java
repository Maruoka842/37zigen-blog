package library.util.algebra.instance.impl;

import java.math.BigInteger;

/**
 * BigInteger 係数のフルヴィッツ整数 (Hurwitz integer)。
 * a + bi + cj + dk において、a, b, c, d がすべて整数、またはすべて半整数のもの。
 * 内部的には 2a, 2b, 2c, 2d を BigInteger で保持する。これらはすべて同じ奇偶を持つ。
 */
public record BigHurwitzInt(BigInteger x0, BigInteger x1, BigInteger x2, BigInteger x3) {

	public static final BigHurwitzInt ZERO = new BigHurwitzInt(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
	public static final BigHurwitzInt ONE = new BigHurwitzInt(BigInteger.valueOf(2), BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
	public static final BigHurwitzInt I = new BigHurwitzInt(BigInteger.ZERO, BigInteger.valueOf(2), BigInteger.ZERO, BigInteger.ZERO);
	public static final BigHurwitzInt J = new BigHurwitzInt(BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(2), BigInteger.ZERO);
	public static final BigHurwitzInt K = new BigHurwitzInt(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(2));

	public static BigHurwitzInt fromLipschitz(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
		return new BigHurwitzInt(a.shiftLeft(1), b.shiftLeft(1), c.shiftLeft(1), d.shiftLeft(1));
	}

	public BigHurwitzInt add(BigHurwitzInt x) {
		return new BigHurwitzInt(x0.add(x.x0), x1.add(x.x1), x2.add(x.x2), x3.add(x.x3));
	}

	public BigHurwitzInt sub(BigHurwitzInt x) {
		return new BigHurwitzInt(x0.subtract(x.x0), x1.subtract(x.x1), x2.subtract(x.x2), x3.subtract(x.x3));
	}

	public BigHurwitzInt neg() {
		return new BigHurwitzInt(x0.negate(), x1.negate(), x2.negate(), x3.negate());
	}

	public BigHurwitzInt mul(BigHurwitzInt x) {
		BigInteger[] w = multiply(x0, x1, x2, x3, x.x0, x.x1, x.x2, x.x3);
		return new BigHurwitzInt(w[0].shiftRight(1), w[1].shiftRight(1), w[2].shiftRight(1), w[3].shiftRight(1));
	}

	private static BigInteger[] multiply(BigInteger x0, BigInteger x1, BigInteger x2, BigInteger x3, BigInteger y0, BigInteger y1, BigInteger y2, BigInteger y3) {
		return new BigInteger[] {
			x0.multiply(y0).subtract(x1.multiply(y1)).subtract(x2.multiply(y2)).subtract(x3.multiply(y3)),
			x0.multiply(y1).add(x1.multiply(y0)).add(x2.multiply(y3)).subtract(x3.multiply(y2)),
			x0.multiply(y2).subtract(x1.multiply(y3)).add(x2.multiply(y0)).add(x3.multiply(y1)),
			x0.multiply(y3).add(x1.multiply(y2)).subtract(x2.multiply(y1)).add(x3.multiply(y0))
		};
	}

	public BigHurwitzInt conj() {
		return new BigHurwitzInt(x0, x1.negate(), x2.negate(), x3.negate());
	}

	public BigInteger norm() {
		return x0.multiply(x0).add(x1.multiply(x1)).add(x2.multiply(x2)).add(x3.multiply(x3)).shiftRight(2);
	}

	public boolean isZero() {
		return x0.signum() == 0 && x1.signum() == 0 && x2.signum() == 0 && x3.signum() == 0;
	}

	public BigHurwitzInt divR(BigHurwitzInt b) {
		BigInteger n2 = b.x0.multiply(b.x0).add(b.x1.multiply(b.x1)).add(b.x2.multiply(b.x2)).add(b.x3.multiply(b.x3));
		if (n2.signum() == 0) throw new ArithmeticException("/ by zero");
		BigInteger[] w = multiply(x0, x1, x2, x3, b.x0, b.x1.negate(), b.x2.negate(), b.x3.negate());
		return round(w[0], w[1], w[2], w[3], n2);
	}

	public BigHurwitzInt remR(BigHurwitzInt b) {
		return sub(divR(b).mul(b));
	}

	public static BigHurwitzInt rightGCD(BigHurwitzInt a, BigHurwitzInt b) {
		while (!b.isZero()) {
			BigHurwitzInt r = a.remR(b);
			a = b;
			b = r;
		}
		return a.normalizeL();
	}

	public BigHurwitzInt normalizeL() {
		if (isZero()) return ZERO;
		BigHurwitzInt best = this;
		for (BigHurwitzInt u : units()) {
			BigHurwitzInt cand = u.mul(this);
			if (compare(cand, best) > 0) best = cand;
		}
		return best;
	}

	private static int compare(BigHurwitzInt a, BigHurwitzInt b) {
		int c = a.x0.compareTo(b.x0);
		if (c != 0) return c;
		c = a.x1.compareTo(b.x1);
		if (c != 0) return c;
		c = a.x2.compareTo(b.x2);
		if (c != 0) return c;
		return a.x3.compareTo(b.x3);
	}

	public static BigHurwitzInt[] units() {
		BigHurwitzInt[] u = new BigHurwitzInt[24];
		int ptr = 0;
		u[ptr++] = ONE; u[ptr++] = ONE.neg();
		u[ptr++] = I; u[ptr++] = I.neg();
		u[ptr++] = J; u[ptr++] = J.neg();
		u[ptr++] = K; u[ptr++] = K.neg();
		BigInteger p1 = BigInteger.ONE;
		BigInteger m1 = BigInteger.valueOf(-1);
		for (BigInteger s0 : new BigInteger[]{p1, m1})
			for (BigInteger s1 : new BigInteger[]{p1, m1})
				for (BigInteger s2 : new BigInteger[]{p1, m1})
					for (BigInteger s3 : new BigInteger[]{p1, m1})
						u[ptr++] = new BigHurwitzInt(s0, s1, s2, s3);
		return u;
	}

	private static BigHurwitzInt round(BigInteger w0, BigInteger w1, BigInteger w2, BigInteger w3, BigInteger n) {
		BigInteger q0 = roundDiv(w0, n), q1 = roundDiv(w1, n), q2 = roundDiv(w2, n), q3 = roundDiv(w3, n);
		BigInteger m0 = roundToOdd(w0, n), m1 = roundToOdd(w1, n), m2 = roundToOdd(w2, n), m3 = roundToOdd(w3, n);
		BigInteger d_even = distSq(w0.shiftLeft(1), w1.shiftLeft(1), w2.shiftLeft(1), w3.shiftLeft(1), q0.shiftLeft(1), q1.shiftLeft(1), q2.shiftLeft(1), q3.shiftLeft(1), n);
		BigInteger d_odd = distSq(w0.shiftLeft(1), w1.shiftLeft(1), w2.shiftLeft(1), w3.shiftLeft(1), m0, m1, m2, m3, n);
		return d_even.compareTo(d_odd) <= 0 ? new BigHurwitzInt(q0.shiftLeft(1), q1.shiftLeft(1), q2.shiftLeft(1), q3.shiftLeft(1)) : new BigHurwitzInt(m0, m1, m2, m3);
	}

	private static BigInteger roundDiv(BigInteger a, BigInteger b) {
		BigInteger[] qr = a.divideAndRemainder(b);
		BigInteger q = qr[0], r = qr[1];
		if (r.abs().shiftLeft(1).compareTo(b.abs()) >= 0) q = q.add(BigInteger.valueOf(r.signum() * b.signum()));
		return q;
	}

	private static BigInteger roundToOdd(BigInteger w, BigInteger n) {
		BigInteger x = roundDiv(w.shiftLeft(1), n);
		if (!x.testBit(0)) {
			if (w.shiftLeft(1).multiply(BigInteger.valueOf(2)).compareTo(x.multiply(n).multiply(BigInteger.valueOf(2))) >= 0) x = x.add(BigInteger.ONE);
			else x = x.subtract(BigInteger.ONE);
		}
		return x;
	}

	private static BigInteger distSq(BigInteger w0, BigInteger w1, BigInteger w2, BigInteger w3, BigInteger z0, BigInteger z1, BigInteger z2, BigInteger z3, BigInteger n) {
		BigInteger e0 = w0.subtract(z0.multiply(n)), e1 = w1.subtract(z1.multiply(n)), e2 = w2.subtract(z2.multiply(n)), e3 = w3.subtract(z3.multiply(n));
		return e0.multiply(e0).add(e1.multiply(e1)).add(e2.multiply(e2)).add(e3.multiply(e3));
	}

	public boolean isLipschitz() {
		return !x0.testBit(0) && !x1.testBit(0) && !x2.testBit(0) && !x3.testBit(0);
	}

	public BigInteger[] toLipschitz() {
		if (!isLipschitz()) return null;
		return new BigInteger[] { x0.shiftRight(1), x1.shiftRight(1), x2.shiftRight(1), x3.shiftRight(1) };
	}
}
