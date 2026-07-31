package library.util.algebra.instance.impl;

import java.math.BigInteger;
import java.util.Objects;

import library.util.MathUtils;
import library.util.algebra.instance.RingElement;
import library.util.algebra.strategy.HurwitzIntStrategy;

/**
 * フルヴィッツ整数 (Hurwitz integer) を表すクラス。
 * a + bi + cj + dk において、a, b, c, d がすべて整数、またはすべて半整数のもの。
 * 内部的には 2a, 2b, 2c, 2d を long で保持する。これらはすべて同じ奇偶を持つ。
 */
public class HurwitzInt implements RingElement<HurwitzInt> {
	// 2a, 2b, 2c, 2d. Always same parity.
	private final long x0, x1, x2, x3;

	public static final HurwitzIntStrategy strategy = new HurwitzIntStrategy();

	@Override
	public HurwitzIntStrategy parent() {
		return strategy;
	}

	@Override
	public HurwitzInt self() {
		return this;
	}

	public static final HurwitzInt ZERO = new HurwitzInt(0, 0, 0, 0);
	public static final HurwitzInt ONE = new HurwitzInt(2, 0, 0, 0);
	public static final HurwitzInt I = new HurwitzInt(0, 2, 0, 0);
	public static final HurwitzInt J = new HurwitzInt(0, 0, 2, 0);
	public static final HurwitzInt K = new HurwitzInt(0, 0, 0, 2);
	public static final HurwitzInt OMEGA = new HurwitzInt(1, 1, 1, 1);

	public HurwitzInt(long x0, long x1, long x2, long x3) {
		this.x0 = x0;
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
	}

	public static HurwitzInt fromLipschitz(long a, long b, long c, long d) {
		return new HurwitzInt(a * 2, b * 2, c * 2, d * 2);
	}

	public double re() { return x0 / 2.0; }
	public double imI() { return x1 / 2.0; }
	public double imJ() { return x2 / 2.0; }
	public double imK() { return x3 / 2.0; }

	public long re2() { return x0; }
	public long imI2() { return x1; }
	public long imJ2() { return x2; }
	public long imK2() { return x3; }

	
	public HurwitzInt add(HurwitzInt x) {
		return new HurwitzInt(x0 + x.x0, x1 + x.x1, x2 + x.x2, x3 + x.x3);
	}

	public HurwitzInt sub(HurwitzInt x) {
		return new HurwitzInt(x0 - x.x0, x1 - x.x1, x2 - x.x2, x3 - x.x3);
	}

	public HurwitzInt neg() {
		return new HurwitzInt(-x0, -x1, -x2, -x3);
	}

	public HurwitzInt mul(HurwitzInt x) {
		long[] w = multiply(x0, x1, x2, x3, x.x0, x.x1, x.x2, x.x3);
		return new HurwitzInt(w[0] / 2, w[1] / 2, w[2] / 2, w[3] / 2);
	}

	private static long[] multiply(long x0, long x1, long x2, long x3, long y0, long y1, long y2, long y3) {
		return new long[] {
			x0 * y0 - x1 * y1 - x2 * y2 - x3 * y3,
			x0 * y1 + x1 * y0 + x2 * y3 - x3 * y2,
			x0 * y2 - x1 * y3 + x2 * y0 + x3 * y1,
			x0 * y3 + x1 * y2 - x2 * y1 + x3 * y0
		};
	}

	private static BigInteger[] multiplyBig(BigInteger x0, BigInteger x1, BigInteger x2, BigInteger x3, BigInteger y0, BigInteger y1, BigInteger y2, BigInteger y3) {
		return new BigInteger[] {
			x0.multiply(y0).subtract(x1.multiply(y1)).subtract(x2.multiply(y2)).subtract(x3.multiply(y3)),
			x0.multiply(y1).add(x1.multiply(y0)).add(x2.multiply(y3)).subtract(x3.multiply(y2)),
			x0.multiply(y2).subtract(x1.multiply(y3)).add(x2.multiply(y0)).add(x3.multiply(y1)),
			x0.multiply(y3).add(x1.multiply(y2)).subtract(x2.multiply(y1)).add(x3.multiply(y0))
		};
	}

	public HurwitzInt conj() {
		return new HurwitzInt(x0, -x1, -x2, -x3);
	}

	public long norm() {
		return (x0 * x0 + x1 * x1 + x2 * x2 + x3 * x3) / 4;
	}

	public boolean isZero() {
		return x0 == 0 && x1 == 0 && x2 == 0 && x3 == 0;
	}

	@Override
	public HurwitzInt one() {
		return ONE;
	}

	@Override
	public HurwitzInt zero() {
		return ZERO;
	}

	/**
	 * 左除法 (divisor on the left): a = b * q + r となる q を返す。 N(r) < N(b)。
	 * q \approx b^{-1} * a = (conj(b) * a) / norm(b)
	 */
	public HurwitzInt divL(HurwitzInt b) {
		if (isInt(x0) && isInt(x1) && isInt(x2) && isInt(x3) &&
			isInt(b.x0) && isInt(b.x1) && isInt(b.x2) && isInt(b.x3)) return divLLong(b);
		return divLBig(b);
	}

	private HurwitzInt divLLong(HurwitzInt b) {
		long n2 = b.x0 * b.x0 + b.x1 * b.x1 + b.x2 * b.x2 + b.x3 * b.x3;
		if (n2 == 0) throw new ArithmeticException("/ by zero");
		long[] w = multiply(b.x0, -b.x1, -b.x2, -b.x3, x0, x1, x2, x3);
		return round(w[0], w[1], w[2], w[3], n2);
	}

	private HurwitzInt divLBig(HurwitzInt b) {
		BigInteger b0 = BigInteger.valueOf(b.x0), b1 = BigInteger.valueOf(b.x1), b2 = BigInteger.valueOf(b.x2), b3 = BigInteger.valueOf(b.x3);
		BigInteger n2 = b0.multiply(b0).add(b1.multiply(b1)).add(b2.multiply(b2)).add(b3.multiply(b3));
		if (n2.signum() == 0) throw new ArithmeticException("/ by zero");
		BigInteger a0 = BigInteger.valueOf(x0), a1 = BigInteger.valueOf(x1), a2 = BigInteger.valueOf(x2), a3 = BigInteger.valueOf(x3);
		BigInteger[] w = multiplyBig(b0, b1.negate(), b2.negate(), b3.negate(), a0, a1, a2, a3);
		return roundBig(w[0], w[1], w[2], w[3], n2);
	}

	/**
	 * 右除法 (divisor on the right): a = q * b + r となる q を返す。 N(r) < N(b)。
	 * q \approx a * b^{-1} = (a * conj(b)) / norm(b)
	 */
	public HurwitzInt divR(HurwitzInt b) {
		if (isInt(x0) && isInt(x1) && isInt(x2) && isInt(x3) &&
			isInt(b.x0) && isInt(b.x1) && isInt(b.x2) && isInt(b.x3)) return divRLong(b);
		return divRBig(b);
	}

	private HurwitzInt divRLong(HurwitzInt b) {
		long n2 = b.x0 * b.x0 + b.x1 * b.x1 + b.x2 * b.x2 + b.x3 * b.x3;
		if (n2 == 0) throw new ArithmeticException("/ by zero");
		long[] w = multiply(x0, x1, x2, x3, b.x0, -b.x1, -b.x2, -b.x3);
		return round(w[0], w[1], w[2], w[3], n2);
	}

	private HurwitzInt divRBig(HurwitzInt b) {
		BigInteger b0 = BigInteger.valueOf(b.x0), b1 = BigInteger.valueOf(b.x1), b2 = BigInteger.valueOf(b.x2), b3 = BigInteger.valueOf(b.x3);
		BigInteger n2 = b0.multiply(b0).add(b1.multiply(b1)).add(b2.multiply(b2)).add(b3.multiply(b3));
		if (n2.signum() == 0) throw new ArithmeticException("/ by zero");
		BigInteger a0 = BigInteger.valueOf(x0), a1 = BigInteger.valueOf(x1), a2 = BigInteger.valueOf(x2), a3 = BigInteger.valueOf(x3);
		BigInteger[] w = multiplyBig(a0, a1, a2, a3, b0, b1.negate(), b2.negate(), b3.negate());
		return roundBig(w[0], w[1], w[2], w[3], n2);
	}

	public HurwitzInt remL(HurwitzInt b) { return sub(b.mul(divL(b))); }
	public HurwitzInt remR(HurwitzInt b) { return sub(divR(b).mul(b)); }

	public HurwitzInt div(HurwitzInt x) { return divL(x); }
	public HurwitzInt rem(HurwitzInt x) { return remL(x); }

	/**
	 * 最大公左因子 (Greatest Common Left Divisor)。 a=gx, b=gy となる g。
	 */
	public static HurwitzInt leftGCD(HurwitzInt a, HurwitzInt b) {
		while (!b.isZero()) {
			HurwitzInt r = a.remL(b);
			a = b;
			b = r;
		}
		return a.normalizeR();
	}

	/**
	 * 最大公右因子 (Greatest Common Right Divisor)。 a=xg, b=yg となる g。
	 */
	public static HurwitzInt rightGCD(HurwitzInt a, HurwitzInt b) {
		while (!b.isZero()) {
			HurwitzInt r = a.remR(b);
			a = b;
			b = r;
		}
		return a.normalizeL();
	}

	public HurwitzInt normalize() { return normalizeL(); }

	/**
	 * 右から単元を掛けて辞書順最大にする (左因子としての正規化)。
	 */
	public HurwitzInt normalizeR() {
		if (isZero()) return ZERO;
		HurwitzInt best = this;
		for (HurwitzInt u : units()) {
			HurwitzInt cand = this.mul(u);
			if (compare(cand, best) > 0) best = cand;
		}
		return best;
	}

	/**
	 * 左から単元を掛けて辞書順最大にする (右因子としての正規化)。
	 */
	public HurwitzInt normalizeL() {
		if (isZero()) return ZERO;
		HurwitzInt best = this;
		for (HurwitzInt u : units()) {
			HurwitzInt cand = u.mul(this);
			if (compare(cand, best) > 0) best = cand;
		}
		return best;
	}

	private static int compare(HurwitzInt a, HurwitzInt b) {
		if (a.x0 != b.x0) return Long.compare(a.x0, b.x0);
		if (a.x1 != b.x1) return Long.compare(a.x1, b.x1);
		if (a.x2 != b.x2) return Long.compare(a.x2, b.x2);
		return Long.compare(a.x3, b.x3);
	}

	public static HurwitzInt[] units() {
		HurwitzInt[] u = new HurwitzInt[24];
		int ptr = 0;
		u[ptr++] = ONE; u[ptr++] = ONE.neg();
		u[ptr++] = I; u[ptr++] = I.neg();
		u[ptr++] = J; u[ptr++] = J.neg();
		u[ptr++] = K; u[ptr++] = K.neg();
		for (int s0 = -1; s0 <= 1; s0 += 2)
			for (int s1 = -1; s1 <= 1; s1 += 2)
				for (int s2 = -1; s2 <= 1; s2 += 2)
					for (int s3 = -1; s3 <= 1; s3 += 2)
						u[ptr++] = new HurwitzInt(s0, s1, s2, s3);
		return u;
	}

	private static boolean isInt(long x) {
		return -Integer.MAX_VALUE <= x && x <= Integer.MAX_VALUE;
	}

	private static HurwitzInt round(long w0, long w1, long w2, long w3, long n) {
		long q0 = MathUtils.roundDiv(w0, n), q1 = MathUtils.roundDiv(w1, n), q2 = MathUtils.roundDiv(w2, n), q3 = MathUtils.roundDiv(w3, n);
		long m0 = roundToOdd(w0, n), m1 = roundToOdd(w1, n), m2 = roundToOdd(w2, n), m3 = roundToOdd(w3, n);
		long d_even = distSq(2*w0, 2*w1, 2*w2, 2*w3, 2*q0, 2*q1, 2*q2, 2*q3, n);
		long d_odd = distSq(2*w0, 2*w1, 2*w2, 2*w3, m0, m1, m2, m3, n);
		return d_even <= d_odd ? new HurwitzInt(2*q0, 2*q1, 2*q2, 2*q3) : new HurwitzInt(m0, m1, m2, m3);
	}

	private static long roundToOdd(long w, long n) {
		long x = MathUtils.roundDiv(2 * w, n);
		if (x % 2 == 0) {
			if (2 * w >= x * n) x++;
			else x--;
		}
		return x;
	}

	private static long distSq(long w0, long w1, long w2, long w3, long z0, long z1, long z2, long z3, long n) {
		long e0 = w0 - z0 * n, e1 = w1 - z1 * n, e2 = w2 - z2 * n, e3 = w3 - z3 * n;
		return e0*e0 + e1*e1 + e2*e2 + e3*e3;
	}

	private static HurwitzInt roundBig(BigInteger w0, BigInteger w1, BigInteger w2, BigInteger w3, BigInteger n) {
		BigInteger q0 = roundDivBig(w0, n), q1 = roundDivBig(w1, n), q2 = roundDivBig(w2, n), q3 = roundDivBig(w3, n);
		BigInteger m0 = roundToOddBig(w0, n), m1 = roundToOddBig(w1, n), m2 = roundToOddBig(w2, n), m3 = roundToOddBig(w3, n);
		BigInteger d_even = distSqBig(w0.shiftLeft(1), w1.shiftLeft(1), w2.shiftLeft(1), w3.shiftLeft(1), q0.shiftLeft(1), q1.shiftLeft(1), q2.shiftLeft(1), q3.shiftLeft(1), n);
		BigInteger d_odd = distSqBig(w0.shiftLeft(1), w1.shiftLeft(1), w2.shiftLeft(1), w3.shiftLeft(1), m0, m1, m2, m3, n);
		return d_even.compareTo(d_odd) <= 0 ? new HurwitzInt(q0.longValueExact() * 2, q1.longValueExact() * 2, q2.longValueExact() * 2, q3.longValueExact() * 2) : new HurwitzInt(m0.longValueExact(), m1.longValueExact(), m2.longValueExact(), m3.longValueExact());
	}

	private static BigInteger roundDivBig(BigInteger a, BigInteger b) {
		BigInteger[] qr = a.divideAndRemainder(b);
		BigInteger q = qr[0], r = qr[1];
		if (r.abs().shiftLeft(1).compareTo(b) >= 0) q = q.add(BigInteger.valueOf(r.signum()));
		return q;
	}

	private static BigInteger roundToOddBig(BigInteger w, BigInteger n) {
		BigInteger x = roundDivBig(w.shiftLeft(1), n);
		if (!x.testBit(0)) {
			if (w.shiftLeft(1).multiply(BigInteger.valueOf(2)).compareTo(x.multiply(n).multiply(BigInteger.valueOf(2))) >= 0) x = x.add(BigInteger.ONE);
			else x = x.subtract(BigInteger.ONE);
		}
		return x;
	}

	private static BigInteger distSqBig(BigInteger w0, BigInteger w1, BigInteger w2, BigInteger w3, BigInteger z0, BigInteger z1, BigInteger z2, BigInteger z3, BigInteger n) {
		BigInteger e0 = w0.subtract(z0.multiply(n)), e1 = w1.subtract(z1.multiply(n)), e2 = w2.subtract(z2.multiply(n)), e3 = w3.subtract(z3.multiply(n));
		return e0.multiply(e0).add(e1.multiply(e1)).add(e2.multiply(e2)).add(e3.multiply(e3));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof HurwitzInt)) return false;
		HurwitzInt that = (HurwitzInt) o;
		return x0 == that.x0 && x1 == that.x1 && x2 == that.x2 && x3 == that.x3;
	}

	@Override
	public int hashCode() { return Objects.hash(x0, x1, x2, x3); }

	@Override
	public String toString() { return "(" + x0/2.0 + ", " + x1/2.0 + ", " + x2/2.0 + ", " + x3/2.0 + ")"; }
}
