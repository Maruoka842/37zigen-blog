package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import library.util.algebra.strategy.ZStrategy;
import library.util.algebra.strategy.ZnStrategy;
import org.junit.jupiter.api.Test;

public class MultivariatePolynomialGenericTest {
    @Test
    public void worksOverZnIncludingNonFieldModulus() {
        CommutativeRingStrategy<Long> zn = new ZnStrategy(6);
        Monomial x = new Monomial(new int[]{1});

        MultivariatePolynomial<Long> threeX = MultivariatePolynomial.singleTerm(zn, x, 3L);
        MultivariatePolynomial<Long> fourX = MultivariatePolynomial.singleTerm(zn, x, 4L);

        assertEquals(MultivariatePolynomial.singleTerm(zn, x, 1L), threeX.add(fourX));
        assertTrue(threeX.multiply(2L).isZero());
        assertEquals(MultivariatePolynomial.singleTerm(zn, new Monomial(new int[]{2}), 0L), threeX.mul(fourX));
    }

    @Test
    public void worksOverRationalsViaFractionField() {
        FractionFieldStrategy<Long> q = new FractionFieldStrategy<>(new ZStrategy());
        Monomial x = new Monomial(new int[]{1});
        Monomial one = new Monomial(new int[0]);

        MultivariatePolynomial<FractionFieldElement<Long>> halfX = MultivariatePolynomial.singleTerm(q, x, q.of(1L, 2L));
        MultivariatePolynomial<FractionFieldElement<Long>> third = MultivariatePolynomial.singleTerm(q, one, q.of(1L, 3L));
        MultivariatePolynomial<FractionFieldElement<Long>> f = halfX.add(third);

        assertTrue(q.equals(q.of(1L, 2L), (FractionFieldElement<Long>) f.differentiate(0).coefficientOf(one)));
        assertTrue(q.equals(q.of(5L, 6L), (FractionFieldElement<Long>) f.evaluate(0, q.one()).coefficientOf(one)));
    }

    @Test
    public void worksOverPrimeFieldAndSupportsDivision() {
        FieldStrategy<Long> fp = new FpStrategy(7);
        Monomial x = new Monomial(new int[]{1});
        Monomial one = new Monomial(new int[0]);
        MultivariatePolynomial<Long> dividend = MultivariatePolynomial.singleTerm(fp, new Monomial(new int[]{2}), 1L)
                .sub(MultivariatePolynomial.singleTerm(fp, one, 1L));
        MultivariatePolynomial<Long> divisor = MultivariatePolynomial.singleTerm(fp, x, 1L)
                .sub(MultivariatePolynomial.singleTerm(fp, one, 1L));

        MultivariatePolynomial.DivisionResult<Long> result = MultivariatePolynomial.divide(dividend, List.of(divisor));

        assertTrue(result.remainder.isZero());
        assertEquals(MultivariatePolynomial.singleTerm(fp, x, 1L).add(MultivariatePolynomial.singleTerm(fp, one, 1L)), result.quotients.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void worksOverRationalFunctionFieldKt() {
        CommutativeRingStrategy<long[]> polynomialRing = PolynomialLong.Strategy(new FpStrategy(5));
        FractionFieldStrategy<long[]> kt = new FractionFieldStrategy<>((IntegralDomainStrategy<long[]>) polynomialRing);
        Monomial x = new Monomial(new int[]{1});
        Monomial one = new Monomial(new int[0]);
        FractionFieldElement<long[]> t = kt.of(new long[]{0, 1}, new long[]{1});
        FractionFieldElement<long[]> invOnePlusT = kt.of(new long[]{1}, new long[]{1, 1});

        MultivariatePolynomial<FractionFieldElement<long[]>> f = MultivariatePolynomial.singleTerm(kt, x, t)
                .add(MultivariatePolynomial.singleTerm(kt, one, invOnePlusT));
        MultivariatePolynomial<FractionFieldElement<long[]>> evaluated = f.evaluate(0, kt.one());

        assertTrue(kt.equals(kt.add(t, invOnePlusT), (FractionFieldElement<long[]>) evaluated.coefficientOf(one)));
        assertEquals(f, new MultivariatePolynomial<>(kt).add(f));
    }
}
