package library.util.polynomial;

import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.GCDDomainStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import cc.redberry.rings.IntegersZp64;
import cc.redberry.rings.poly.multivar.MultivariateGCD;
import cc.redberry.rings.poly.multivar.MultivariatePolynomialZp64;
import cc.redberry.rings.poly.multivar.MultivariateDivision;

/**
 * MultivariatePolynomialOverFp を項とする環の演算ストラテジ。
 * cc.redberry.rings を用いて GCD および完全除算を実装する。
 */
public class MultivariatePolynomialOverFpStrategy implements
    IntegralDomainStrategy<MultivariatePolynomial<Long>>,
    GCDDomainStrategy<MultivariatePolynomial<Long>>,
    ExactDivRingStrategy<MultivariatePolynomial<Long>> {

    private final long mod;
    private final IntegersZp64 ringsRing;

    /**
     * MultivariatePolynomialOverFpStrategy のコンストラクタ。
     * Pre-condition: mod は素数であること。
     * Calculation complexity: O(1).
     * @param mod 有限体の標数。
     */
    public MultivariatePolynomialOverFpStrategy(long mod) {
        this.mod = mod;
        this.ringsRing = new IntegersZp64(mod);
    }

    @Override
    public MultivariatePolynomial<Long> zero() {
        return new MultivariatePolynomialOverFp(mod);
    }

    @Override
    public MultivariatePolynomial<Long> one() {
        return new MultivariatePolynomialOverFp(mod).one();
    }

    @Override
    public MultivariatePolynomial<Long> add(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> b) {
        return a.add(b);
    }

    @Override
    public MultivariatePolynomial<Long> mul(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> b) {
        return a.mul(b);
    }

    @Override
    public MultivariatePolynomial<Long> neg(MultivariatePolynomial<Long> a) {
        return a.multiply(mod - 1);
    }

    @Override
    public boolean equals(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> b) {
        return a.equals(b);
    }

    @Override
    public MultivariatePolynomial<Long> gcd(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> b) {
        if (a.isZero()) return b;
        if (b.isZero()) return a;
        int nVars = Math.max(inferVariableCount(a), inferVariableCount(b));
        MultivariatePolynomialZp64 aZp = MultivariatePolynomialOverFp.toRingsPolynomial(a, nVars, ringsRing);
        MultivariatePolynomialZp64 bZp = MultivariatePolynomialOverFp.toRingsPolynomial(b, nVars, ringsRing);
        MultivariatePolynomialZp64 gcdZp = MultivariateGCD.PolynomialGCD(aZp, bZp);
        return MultivariatePolynomialOverFp.fromRingsPolynomial(gcdZp, mod);
    }

    @Override
    public MultivariatePolynomial<Long> exactDiv(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> b) {
        if (b.isZero()) throw new ArithmeticException("Division by zero");
        if (a.isZero()) return a;
        int nVars = Math.max(inferVariableCount(a), inferVariableCount(b));
        MultivariatePolynomialZp64 aZp = MultivariatePolynomialOverFp.toRingsPolynomial(a, nVars, ringsRing);
        MultivariatePolynomialZp64 bZp = MultivariatePolynomialOverFp.toRingsPolynomial(b, nVars, ringsRing);
        MultivariatePolynomialZp64[] divRem = MultivariateDivision.divideAndRemainder(aZp, bZp);
        if (divRem == null || !divRem[1].isZero()) {
            throw new ArithmeticException("Not an exact division");
        }
        return MultivariatePolynomialOverFp.fromRingsPolynomial(divRem[0], mod);
    }

    private int inferVariableCount(MultivariatePolynomial<Long> f) {
        int nVars = 0;
        for (Monomial m : f.getTerms().keySet()) nVars = Math.max(nVars, m.size());
        return Math.max(nVars, 1);
    }
}
