package library.util.polynomial;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.FractionFieldElement;

import static org.junit.jupiter.api.Assertions.*;

public class MultivariateRationalFunctionParserExtendedTest {
    @Test
    public void testNegativeExponents() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x");

        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        // x^-1
        var f1 = parser.parse("x^-1");
        assertTrue(f1.num().equals(one));
        assertTrue(f1.den().equals(x));

        // x^(-1)
        var f2 = parser.parse("x^(-1)");
        assertTrue(f2.num().equals(one));
        assertTrue(f2.den().equals(x));

        // x^-2
        var f3 = parser.parse("x^-2");
        assertTrue(f3.num().equals(one));
        assertTrue(f3.den().equals(x.pow(2)));

        // x^(-2)
        var f4 = parser.parse("x^(-2)");
        assertTrue(f4.num().equals(one));
        assertTrue(f4.den().equals(x.pow(2)));
    }

    @Test
    public void testParenthesizedPositiveExponents() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x");

        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);

        // x^(2)
        var f1 = parser.parse("x^(2)");
        assertTrue(f1.num().equals(x.pow(2)));
        assertTrue(f1.den().isOne());
    }

    @Test
    public void testComplexExpression() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x");

        // (x+x^-1)^2 = x^2 + 2 + x^-2 = (x^4 + 2x^2 + 1) / x^2
        var f1 = parser.parse("(x+x^-1)^2");

        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);
        MultivariatePolynomial<Long> expectedNum = x.pow(4).add(x.pow(2).multiply(2L)).add(new MultivariatePolynomialOverFp(mod).one());
        MultivariatePolynomial<Long> expectedDen = x.pow(2);

        assertTrue(f1.num().mul(expectedDen).equals(f1.den().mul(expectedNum)));
    }

    @Test
    public void testUserReportedExpression() {
        long mod = 998244353;
        MultivariateRationalFunctionParser parser = MultivariateRationalFunctionParser.of(mod, "x", "y", "z", "w");
        // This should not throw an exception
        var f = parser.parse("1/(1-w*(x^2+x^(-2)+y+y^(-2)+(x+x^(-1)+y+y^(-1))*(z+z^(-1))))");
        assertNotNull(f);
    }
}
