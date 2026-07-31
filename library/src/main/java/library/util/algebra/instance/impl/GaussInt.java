package library.util.algebra.instance.impl;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import library.util.MathUtils;
import library.util.algebra.instance.EuclideanDomainElement;
import library.util.algebra.instance.ExactDivRingElement;
import library.util.algebra.strategy.GaussIntStrategy;
import library.util.algebra.strategy.RingStrategy;

public class GaussInt implements EuclideanDomainElement<GaussInt>, ExactDivRingElement<GaussInt> {
	private final long a;
	private final long b;
	
	public static final GaussIntStrategy strategy = new GaussIntStrategy();

	@Override
	public GaussIntStrategy parent() {
		return strategy;
	}

	@Override
	public GaussInt self() {
		return this;
	}

	public static final GaussInt ZERO = new GaussInt(0, 0);
	public static final GaussInt ONE = new GaussInt(1, 0);
	public static final GaussInt TWO = new GaussInt(2, 0);
	
	public GaussInt(long a, long b) {
		this.a  = a;
		this.b = b;
	}
	
	public GaussInt add(GaussInt x) {
		return new GaussInt(a + x.a, b + x.b);
	}
	
	public GaussInt sub(GaussInt x) {
		return new GaussInt(a - x.a, b - x.b);
	}

	public GaussInt neg() {
		return new GaussInt(-a, -b);
	}

	public GaussInt mul(GaussInt x) {
		//(a+bi)(c+di)=ac-bd+i(ad+bc)
		return new GaussInt(a * x.a - b * x.b, a * x.b + b * x.a);
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	public GaussInt div(GaussInt x) {
		if (isInt(a) && isInt(b) && isInt(x.a) && isInt(x.b)) return divLong(x);
		return divBig(x);
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private GaussInt divLong(GaussInt x) {
		long n = x.norm();
		if (n == 0) throw new ArithmeticException("/ by zero");
		// (a + bi) / (c + di)
		//=(a + bi)(c - di) / norm
		//=ac + bd + i(-ad + bc)
		long na = a * x.a + b * x.b;
		long nb = b * x.a - a * x.b;
		return new GaussInt(MathUtils.roundDiv(na, n), MathUtils.roundDiv(nb, n));
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private static boolean isInt(long x) {
		return -Integer.MAX_VALUE <= x && x <= Integer.MAX_VALUE;
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private GaussInt divBig(GaussInt x) {
		BigInteger xa = BigInteger.valueOf(x.a);
		BigInteger xb = BigInteger.valueOf(x.b);
		BigInteger n = xa.multiply(xa).add(xb.multiply(xb));
		if (n.signum() == 0) throw new ArithmeticException("/ by zero");
		BigInteger na = BigInteger.valueOf(a).multiply(xa).add(BigInteger.valueOf(b).multiply(xb));
		BigInteger nb = BigInteger.valueOf(b).multiply(xa).subtract(BigInteger.valueOf(a).multiply(xb));
		return new GaussInt(roundDivBig(na, n), roundDivBig(nb, n));
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private static long roundDivBig(BigInteger a, BigInteger b) {
		BigInteger[] qr = a.divideAndRemainder(b);
		BigInteger q = qr[0];
		BigInteger r = qr[1];
		if (r.abs().shiftLeft(1).compareTo(b) >= 0) q = q.add(BigInteger.valueOf(r.signum()));
		return q.longValueExact();
	}
	
	/**
	 * 未テスト
	 * this が x で割り切れるかを返す。
	 * 係数の絶対値が 32 bit 以上の場合、中間計算がオーバーフローする場合がある。
	 * @param x 割る数
	 * @return this が x で割り切れるなら true
	 */
	public boolean isDivisibleBy(GaussInt x) {
		if (isInt(a) && isInt(b) && isInt(x.a) && isInt(x.b)) return isDivisibleByLong(x);
		return isDivisibleByBig(x);
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private boolean isDivisibleByLong(GaussInt x) {
		long n = x.norm();
		if (n == 0) return false;
		long na = a * x.a + b * x.b;
		long nb = b * x.a - a * x.b;
		return na % n == 0 && nb % n == 0;
	}
	
	/**
	 * 未テスト
	 * 計算量: O(1)
	 */
	private boolean isDivisibleByBig(GaussInt x) {
		BigInteger xa = BigInteger.valueOf(x.a);
		BigInteger xb = BigInteger.valueOf(x.b);
		BigInteger n = xa.multiply(xa).add(xb.multiply(xb));
		if (n.signum() == 0) return false;
		BigInteger na = BigInteger.valueOf(a).multiply(xa).add(BigInteger.valueOf(b).multiply(xb));
		BigInteger nb = BigInteger.valueOf(b).multiply(xa).subtract(BigInteger.valueOf(a).multiply(xb));
		return na.mod(n).signum() == 0 && nb.mod(n).signum() == 0;
	}
	
	/**
	 * 未テスト
	 * div と mul の中間計算がオーバーフローする場合がある。
	 */
	public GaussInt rem(GaussInt x) {
		return sub(div(x).mul(x));
	}

	@Override
	public GaussInt mod(GaussInt x) {
		return rem(x);
	}

	@Override
	public GaussInt exactDiv(GaussInt x) {
		return div(x);
	}
	
	/**
	 * 未テスト
	 * div と rem の中間計算がオーバーフローする場合がある。
	 */
	public GaussInt gcd(GaussInt x) {
		//https://judge.yosupo.jp/submission/370172
		return gcd(this, x);
	}
	
	/**
	 * 未テスト
	 * div と rem の中間計算がオーバーフローする場合がある。
	 */
	public static GaussInt gcd(GaussInt x, GaussInt y) {
		while (!y.isZero()) {
			GaussInt r = x.rem(y);
			x = y;
			y = r;
		}
		return x.normalize();
	}
	
	/**
	 * 未テスト
	 */
	public boolean isZero() {
		return a == 0 && b == 0;
	}
	
	/**
	 * 未テスト
	 * 1, -1, i, -i 倍の中で re > 0 かつ im >= 0 となる代表元を返す。
	 * 0 の場合は 0 を返す。
	 */
	public GaussInt normalize() {
		GaussInt ret = this;
		for (int i = 0; i < 4; i++) {
			if (ret.a > 0 && ret.b >= 0) return ret;
			ret = ret.muli();
		}
		return ZERO;
	}
	
	/**
	 * 未テスト
	 * 単元は無視し、各素因子は normalize した代表元で返す。
	 * 有理素数 p について、p = 2 なら 1 + i、
	 * p ≡ 3 (mod 4) なら p、
	 * p ≡ 1 (mod 4) なら p = q * conj(q) を満たす q と conj(q) に分解する。
	 * 係数の絶対値が 32 bit 以上の場合、中間計算がオーバーフローする場合がある。
	 * @return ガウス素因子から指数への写像
	 * @throws ArithmeticException this が 0 の場合
	 */
	public Map<GaussInt, Integer> factor() {
		if (isZero()) throw new ArithmeticException("factorization of zero");
		LinkedHashMap<GaussInt, Integer> ret = new LinkedHashMap<>();
		GaussInt cur = normalize();
		if (cur.b == 0) return factorRational(cur.a);
		for (long p : MathUtils.factor(cur.norm()).keySet()) {
			if (p == 2) {
				cur = factorBy(cur, new GaussInt(1, 1), ret);
			} else if (p % 4 == 3) {
				cur = factorBy(cur, new GaussInt(p, 0), ret);
			} else {
				GaussInt q = gaussianPrimeDivisorOf(p);
				cur = factorBy(cur, q, ret);
				cur = factorBy(cur, q.conj().normalize(), ret);
			}
		}
		return ret;
	}
	
	/**
	 * 未テスト
	 * 計算量: Pollard's rho による素因数分解 O(n^1/4) + ガウス素因数への変換
	 */
	public static Map<GaussInt, Integer> factorRational(long n) {
		LinkedHashMap<GaussInt, Integer> ret = new LinkedHashMap<>();
		for (Map.Entry<Long, Integer> entry : MathUtils.factor(n).entrySet()) {
			long p = entry.getKey();
			int e = entry.getValue();
			if (p == 2) {
				ret.merge(new GaussInt(1, 1), e * 2, Integer::sum);
			} else if (p % 4 == 3) {
				ret.merge(new GaussInt(p, 0), e, Integer::sum);
			} else {
				GaussInt q = gaussianPrimeDivisorOf(p);
				ret.merge(q, e, Integer::sum);
				ret.merge(q.conj().normalize(), e, Integer::sum);
			}
		}
		return ret;
	}
	
	/**
	 * x を p で割れるだけ割り、ret に p の指数を加算する。
	 */
	private static GaussInt factorBy(GaussInt x, GaussInt p, Map<GaussInt, Integer> ret) {
//		p = p.normalize();
		int e = 0;
		while (x.isDivisibleBy(p)) {
			x = x.div(p);
			e++;
		}
		if (e > 0) ret.merge(p, e, Integer::sum);
		return x;
	}
	
	/**
	 * p ≡ 1 (mod 4) である有理素数 p のガウス素因子を 1 つ返す。
	 */
	private static GaussInt gaussianPrimeDivisorOf(long p) {
		// t^2 = -1 mod p
		// (t + i)(t - i) = 0 mod p
		long t = MathUtils.modKthRoot(p - 1, 2, p);
		var ret=gcd(new GaussInt(p, 0), new GaussInt(t, 1));
		return ret;
	}
	
	public GaussInt mod(long x) {
		long na=a%x;
		long nb=b%x;
		if(na<0)na+=x;
		if(nb<0)nb+=x;
		return new GaussInt(na, nb);
	}
	
	public long norm() {
		return a * a + b * b;
	}
	
	public GaussInt conj() {
		return new GaussInt(a, -b);
	}
	
	public long re() {
		return a;
	}
	
	public long im() {
		return b;
	}
	
	/**
	 * 虚数単位を掛ける
	 * @return
	 */
	public GaussInt muli() {
		return new GaussInt(-b, a);
	}

	@Override
	public GaussInt one() {
		return ONE;
	}

	@Override
	public GaussInt zero() {
		return ZERO;
	}
	
	/**
	 * 虚数単位で割る
	 * @return
	 */
	public GaussInt divi() {
		return new GaussInt(b, -a);
	}
	
	public GaussInt modPow(long e, long mod) {
		long c = 1;
		long d = 0;
		long f = a;
		long g = b;
		while (e != 0) {
			if (e % 2 == 1) {
				// (c+id)(f+ig)=cf-dg+i(df+cg)
				long nc=c*f-d*g;
				long nd=d*f+c*g;
				c=nc%mod;
				d=nd%mod;
			}
			// (f+ig)(f+ig)=ff-gg+2igf
			long nf=f*f-g*g;
			long ng=2*g*f;
			f=nf%mod;
			g=ng%mod;
			e /= 2;
		}
		if(c<0)c+=mod;
		if(d<0)d+=mod;
		return new GaussInt(c, d);
	}
	
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof GaussInt)) return false;
	    GaussInt x = (GaussInt) o;
	    return a == x.a && b == x.b;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(a, b);
	}
	
	@Override
	public String toString() {
		return "("+a+", "+b+")";
	}
	
}
