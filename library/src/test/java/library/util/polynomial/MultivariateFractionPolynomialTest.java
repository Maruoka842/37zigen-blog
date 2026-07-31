package library.util.polynomial;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultivariateFractionPolynomialTest {
    @Test
    public void testZero() {
        long mod = 998244353;
        MultivariatePolynomialOverFpFunctionField zero = new MultivariatePolynomialOverFpFunctionField(mod);
        assertTrue(zero.isZero());
        assertFalse(zero.isOne());
        assertEquals(0, zero.getFractionTerms().size());
    }

    @Test
    public void testOne() {
        long mod = 998244353;
        MultivariatePolynomialOverFpFunctionField one = new MultivariatePolynomialOverFpFunctionField(mod).one();
        assertFalse(one.isZero());
        assertTrue(one.isOne());
        assertEquals(1, one.getFractionTerms().size());

        Monomial lm = one.leadingMonomial();
        assertEquals(0, lm.getDegree());

        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);
        assertTrue(field.equals(one.leadingCoefficient(), field.one()));
    }

    @Test
    public void testIsOneComplex() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        // 1 * x^0
        MultivariatePolynomialOverFpFunctionField p1 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[0]), field.one());
        assertTrue(p1.isOne());

        // 1 * x^1
        MultivariatePolynomialOverFpFunctionField p2 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1}), field.one());
        assertFalse(p2.isOne());

        // 2 * x^0
        MultivariatePolynomialOverFpFunctionField p3 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[0]), field.from(new long[]{2}));
        assertFalse(p3.isOne());

        // 1 * x^0 + 1 * x^1
        MultivariatePolynomialOverFpFunctionField p4 = p1.add(p2);
        assertFalse(p4.isOne());
    }

    @Test
    public void testRowEchelonBasisAndReduction() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        MultivariatePolynomialOverFpFunctionField x = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 0}), field.one());
        MultivariatePolynomialOverFpFunctionField y = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one());
        MultivariatePolynomialOverFpFunctionField p1 = x.add(y);
        MultivariatePolynomialOverFpFunctionField p2 = x.add(y.multiply(field.from(new long[]{2})));

        java.util.List<MultivariatePolynomialOverFpFunctionField> basis = MultivariatePolynomialOverFpFunctionField.rowEchelonBasis(java.util.List.of(p1, p2));

        assertEquals(2, basis.size());
        assertEquals(new Monomial(new int[]{1, 0}), basis.get(0).leadingMonomial());
        assertEquals(new Monomial(new int[]{0, 1}), basis.get(1).leadingMonomial());
        assertTrue(field.equals(basis.get(0).leadingCoefficient(), field.one()));
        assertTrue(field.equals(basis.get(1).leadingCoefficient(), field.one()));

        MultivariatePolynomialOverFpFunctionField target = x.multiply(field.from(new long[]{3})).add(y.multiply(field.from(new long[]{4})));
        assertTrue(MultivariatePolynomialOverFpFunctionField.reduceByEchelon(target, basis).isZero());
    }

    @Test
    public void testReduceByEchelonDoesNotUseMonomialDivision() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        MultivariatePolynomialOverFpFunctionField x = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 0}), field.one());
        MultivariatePolynomialOverFpFunctionField x2 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{2, 0}), field.one());
        java.util.List<MultivariatePolynomialOverFpFunctionField> basis = MultivariatePolynomialOverFpFunctionField.rowEchelonBasis(java.util.List.of(x));

        MultivariatePolynomialOverFpFunctionField reduced = MultivariatePolynomialOverFpFunctionField.reduceByEchelon(x2, basis);

        assertEquals(x2.getFractionTerms(), reduced.getFractionTerms());
    }
}
