package library.util.polynomial;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.MonoidRingElement;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class MultivariatePolynomialTest {
    @Test
    public void testEqualsAndHashCode() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y");

        MultivariatePolynomial<Long> p1 = P.parse("x + y");
        MultivariatePolynomial<Long> p2 = P.parse("y + x");
        MultivariatePolynomial<Long> p3 = P.parse("x + 2*y");

        // Same polynomial
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Different coefficients
        assertNotEquals(p1, p3);

        // Different mod
        MultivariatePolynomial<Long> p1_otherMod = new MultivariatePolynomialOverFp(1000000007);
        p1_otherMod = p1_otherMod.add(MultivariatePolynomialOverFp.singleTerm(1000000007, new Monomial(new int[]{1, 0}), 1L));
        p1_otherMod = p1_otherMod.add(MultivariatePolynomialOverFp.singleTerm(1000000007, new Monomial(new int[]{0, 1}), 1L));
        assertNotEquals(p1, p1_otherMod);

        // Different variables (Monomial representation)
        // Monomial(new int[]{1}) vs Monomial(new int[]{1, 0})
        // These should be equal because grevlex comparison and hashCode ignore trailing zeros.
        Monomial m1 = new Monomial(new int[]{1});
        Monomial m2 = new Monomial(new int[]{1, 0});
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        MultivariatePolynomial<Long> pa = new MultivariatePolynomialOverFp(mod);
        pa = pa.add(MultivariatePolynomialOverFp.singleTerm(mod, m1, 1L));
        MultivariatePolynomial<Long> pb = new MultivariatePolynomialOverFp(mod);
        pb = pb.add(MultivariatePolynomialOverFp.singleTerm(mod, m2, 1L));

        // MultivariatePolynomial.equals uses Map.equals, which handles Monomial.equals correctly.
        assertEquals(pa, pb);
        assertEquals(pa.hashCode(), pb.hashCode());

        // More complex Monomial length differences
        Monomial m3 = new Monomial(new int[]{0, 1});
        Monomial m4 = new Monomial(new int[]{0, 1, 0, 0});
        assertEquals(m3, m4);
        assertEquals(m3.hashCode(), m4.hashCode());
    }

    @Test
    public void testZeroCoefficientRemoval() {
        long mod = 998244353;
        TreeMap<Monomial, Long> terms = new TreeMap<>();
        Monomial m1 = new Monomial(new int[]{1});
        terms.put(m1, mod); // mod is equivalent to 0 in FpElement
        terms.put(new Monomial(new int[]{2}), 1L);
        terms.put(new Monomial(new int[]{3}), 0L); // Explicit 0

        // Constructor should call cleanup() and remove zero coefficients
        MultivariatePolynomial<Long> p = new MultivariatePolynomialOverFp(mod, terms);
        assertEquals(1, p.getTerms().size());
        assertFalse(p.getTerms().containsKey(m1));
        assertFalse(p.getTerms().containsValue(0L));

        MultivariatePolynomial<Long> p2 = new MultivariatePolynomialOverFp(mod);
        p2 = p2.add(MultivariatePolynomialOverFp.singleTerm(mod, new Monomial(new int[]{2}), 1L));
        assertEquals(p, p2);

        // Test subtraction resulting in zero
        MultivariatePolynomial<Long> p3 = p.sub(p2);
        assertTrue(p3.isZero());
        assertEquals(0, p3.getTerms().size());

        MultivariatePolynomial<Long> pZero = new MultivariatePolynomialOverFp(mod);
        assertEquals(pZero, p3);
    }

    @Test
    public void testEqualsEdgeCases() {
        long mod = 998244353;
        MultivariatePolynomial<Long> p1 = new MultivariatePolynomialOverFp(mod);

        // Null
        assertNotEquals(p1, null);

        // Different class
        assertNotEquals(p1, "not a polynomial");

        // Same instance
        assertEquals(p1, p1);

        // Zero polynomials
        MultivariatePolynomial<Long> p2 = new MultivariatePolynomialOverFp(mod);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testZeroAndOne() {
        long mod = 998244353;
        MultivariatePolynomial<Long> zero = new MultivariatePolynomialOverFp(mod);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        assertTrue(zero.isZero());
        assertFalse(zero.isOne());
        assertFalse(one.isZero());
        assertTrue(one.isOne());

        assertEquals(0, zero.getTerms().size());
        assertEquals(1, one.getTerms().size());

        Monomial constM = new Monomial(new int[0]);
        assertEquals(1L, one.getTerms().get(constM));

        // Arithmetic with zero and one
        MultivariatePolynomial<Long> p = PolynomialParser.of(mod, "x").parse("x + 1");
        assertEquals(p, p.add(zero));
        assertEquals(p, p.mul(one));
        assertTrue(p.mul(zero).isZero());

        MultivariatePolynomial<Long> x = PolynomialParser.of(mod, "x").parse("x");
        assertEquals(x, p.sub(one));
    }

    @Test
    public void testAddInplaceMultivariatePolynomialDoesNotRecurse() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y");
        MultivariatePolynomial<Long> p = P.parse("x + 1");
        MultivariatePolynomial<Long> q = P.parse("2*x + y + 3");

        p.addInplace(q);

        assertEquals(P.parse("3*x + y + 4"), p);
        assertEquals(P.parse("2*x + y + 3"), q);
    }

}
