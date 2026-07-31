package library.util.algebra.strategy;

import library.util.polynomial.PolynomialFpDynamic;

/**
 * 有限体上の多項式剰余環 F_p[x] / (P(x)) の代数的構造。
 * {@link PolynomialFpDynamic} を用いて演算を行う。
 */
public class PolynomialQuotientRingFpStrategy implements CommutativeRingStrategy<long[]> {
    private final PolynomialFpDynamic poly;
    private final long[] modPoly;

    /**
     * 指定された多項式演算器と法多項式を用いて剰余環を構築する。
     *
     * @param poly    多項式演算器
     * @param modPoly 法多項式 P(x)
     */
    public PolynomialQuotientRingFpStrategy(PolynomialFpDynamic poly, long[] modPoly) {
        this.poly = poly;
        this.modPoly = poly.resize(modPoly);
        if (poly.isZero(this.modPoly)) throw new IllegalArgumentException("Modulus cannot be zero");
    }

    /**
     * a mod P(x) を計算する。
     * 計算量: O(M(deg a) log (deg a))
     *
     * @param a 多項式
     * @return a mod P(x)
     */
    public long[] mod(long[] a) {
        return poly.mod(a, modPoly);
    }

    @Override
    public long[] zero() {
        return poly.zero();
    }

    @Override
    public long[] one() {
        return poly.mod(poly.one(), modPoly);
    }

    @Override
    public long[] add(long[] a, long[] b) {
        return poly.add(a, b);
    }

    @Override
    public long[] sub(long[] a, long[] b) {
        return poly.sub(a, b);
    }

    @Override
    public long[] mul(long[] a, long[] b) {
        return poly.mulMod(a, b, modPoly);
    }

    @Override
    public long[] neg(long[] a) {
        return poly.neg(a);
    }

    @Override
    public boolean equals(long[] a, long[] b) {
        return poly.equals(a, b);
    }
}
