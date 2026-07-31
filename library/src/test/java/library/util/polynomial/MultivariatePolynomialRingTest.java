package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import library.util.algebra.strategy.FpStrategy;

public class MultivariatePolynomialRingTest {

    @Test
    public void testRingOperations() {
        long mod = 998244353;
        FpStrategy baseRing = new FpStrategy(mod);
        MultivariatePolynomialRing<Long> ring = new MultivariatePolynomialRing<>(baseRing, "x", "y");

        // Test one() and zero()
        MultivariatePolynomial<Long> zero = ring.zero();
        MultivariatePolynomial<Long> one = ring.one();
        assertTrue(zero.isZero());
        assertFalse(one.isZero());

        // Test parsing
        MultivariatePolynomial<Long> p1 = ring.parse("x^2 + y");
        MultivariatePolynomial<Long> p2 = ring.parse("x^2 + x");

        assertNotNull(p1);
        assertNotNull(p2);

        // Test f.ring()
        MultivariatePolynomialRing<Long> inferredRing = p1.ring();
        assertNotNull(inferredRing);
        assertEquals(baseRing, inferredRing.getCoefficientRing());

        // Test add / sub / mul
        MultivariatePolynomial<Long> sum = ring.add(p1, p2); // 2x^2 + x + y
        MultivariatePolynomial<Long> expectedSum = ring.parse("2*x^2 + x + y");
        assertEquals(expectedSum, sum);

        // Test Grobner Basis
        List<MultivariatePolynomial<Long>> gb = ring.grobnerBasis(Arrays.asList(p1, p2));
        assertFalse(gb.isEmpty());
        for (MultivariatePolynomial<Long> f : Arrays.asList(p1, p2)) {
            assertTrue(ring.divide(f, gb).remainder.isZero());
        }
    }

    @Test
    public void testRingOperationsNoVars() {
        long mod = 998244353;
        FpStrategy baseRing = new FpStrategy(mod);
        MultivariatePolynomialRing<Long> ring = new MultivariatePolynomialRing<>(baseRing);

        // Test one() and zero()
        MultivariatePolynomial<Long> zero = ring.zero();
        MultivariatePolynomial<Long> one = ring.one();
        assertTrue(zero.isZero());
        assertFalse(one.isZero());

        // Use helper parsing to construct elements
        MultivariatePolynomialRing<Long> namedRing = new MultivariatePolynomialRing<>(baseRing, "x", "y");
        MultivariatePolynomial<Long> p1 = namedRing.parse("x^2 + y");
        MultivariatePolynomial<Long> p2 = namedRing.parse("x^2 + x");

        // Test division and Grobner basis using the general ring (without totalVars)
        List<MultivariatePolynomial<Long>> gb = ring.grobnerBasis(Arrays.asList(p1, p2));
        assertFalse(gb.isEmpty());
        for (MultivariatePolynomial<Long> f : Arrays.asList(p1, p2)) {
            assertTrue(ring.divide(f, gb).remainder.isZero());
        }
    }
}
