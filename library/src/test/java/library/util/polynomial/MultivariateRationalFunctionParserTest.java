package library.util.polynomial;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.FractionFieldElement;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class MultivariateRationalFunctionParserTest {
    @Test
    public void testUserExample() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x", "y", "z");

        // 1/(x+y) + z/y
        // = (y + z(x+y)) / (y(x+y))
        // = (y + zx + zy) / (xy + y^2)
        FractionFieldElement<MultivariatePolynomial<Long>> res = parser.parse("1/(x+y) + z/y");

        MultivariatePolynomial<Long> num = res.num();
        MultivariatePolynomial<Long> den = res.den();

        MultivariatePolynomialOverFpStrategy strategy = new MultivariatePolynomialOverFpStrategy(mod);
        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 3);
        MultivariatePolynomial<Long> y = MultivariatePolynomialOverFp.singleTerm(mod, 1, 1, 1, 3);
        MultivariatePolynomial<Long> z = MultivariatePolynomialOverFp.singleTerm(mod, 2, 1, 1, 3);

        MultivariatePolynomial<Long> expectedNum = y.add(z.mul(x.add(y)));
        MultivariatePolynomial<Long> expectedDen = y.mul(x.add(y));

        // Simplified form might be different by a constant factor in Fp, but FractionFieldStrategy should handle it.
        // We can check if num * expectedDen == den * expectedNum
        assertTrue(num.mul(expectedDen).equals(den.mul(expectedNum)));
    }

    @Test
    public void testSimplification() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x");

        // (x^2 - 1) / (x - 1) = x + 1
        FractionFieldElement<MultivariatePolynomial<Long>> res = parser.parse("(x^2 - 1) / (x - 1)");

        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);
        MultivariatePolynomial<Long> expected = x.add(new MultivariatePolynomialOverFp(mod).one());

        assertTrue(res.num().equals(expected));
        assertTrue(res.den().isOne());
    }

    @Test
    public void testComplexExpression() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x", "y");

        // (x/y + y/x)^2 = (x^2/y^2 + 2 + y^2/x^2) = (x^4 + 2x^2y^2 + y^4) / (x^2y^2)
        FractionFieldElement<MultivariatePolynomial<Long>> res = parser.parse("(x/y + y/x)^2");

        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 2);
        MultivariatePolynomial<Long> y = MultivariatePolynomialOverFp.singleTerm(mod, 1, 1, 1, 2);

        MultivariatePolynomial<Long> expectedNum = x.pow(4).add(x.pow(2).mul(y.pow(2)).multiply(2L)).add(y.pow(4));
        MultivariatePolynomial<Long> expectedDen = x.pow(2).mul(y.pow(2));

        assertTrue(res.num().mul(expectedDen).equals(res.den().mul(expectedNum)));
    }
}
