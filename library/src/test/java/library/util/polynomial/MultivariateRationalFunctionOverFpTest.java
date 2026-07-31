package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultivariateRationalFunctionOverFpTest {
    @Test
    public void testBasicArithmetic() {
        long mod = 998244353;
        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 2);
        MultivariatePolynomial<Long> y = MultivariatePolynomialOverFp.singleTerm(mod, 1, 1, 1, 2);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        MultivariateRationalFunctionOverFp f1 = new MultivariateRationalFunctionOverFp(x, one, mod); // f1 = x
        MultivariateRationalFunctionOverFp f2 = new MultivariateRationalFunctionOverFp(y, one, mod); // f2 = y

        // x + y
        MultivariateRationalFunctionOverFp sum = f1.add(f2);
        assertEquals(x.add(y), sum.num());
        assertTrue(sum.den().isOne());

        // x * y
        MultivariateRationalFunctionOverFp prod = f1.mul(f2);
        assertEquals(x.mul(y), prod.num());
        assertTrue(prod.den().isOne());

        // 1/x + 1/y = (x+y)/xy
        MultivariateRationalFunctionOverFp invSum = f1.inv().add(f2.inv());
        assertEquals(x.add(y), invSum.num());
        assertEquals(x.mul(y), invSum.den());
    }

    @Test
    public void testSimplification() {
        long mod = 998244353;
        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        // (x^2 - 1) / (x - 1) = x + 1
        MultivariatePolynomial<Long> x2minus1 = x.mul(x).sub(one);
        MultivariatePolynomial<Long> xminus1 = x.sub(one);

        MultivariateRationalFunctionOverFp f = new MultivariateRationalFunctionOverFp(x2minus1, xminus1, mod);
        assertEquals(x.add(one), f.num());
        assertTrue(f.den().isOne());
    }

    @Test
    public void testDifferentiation() {
        long mod = 998244353;
        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 2);
        MultivariatePolynomial<Long> y = MultivariatePolynomialOverFp.singleTerm(mod, 1, 1, 1, 2);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        // f(x, y) = x / (x + y)
        MultivariateRationalFunctionOverFp f = new MultivariateRationalFunctionOverFp(x, x.add(y), mod);

        // df/dx = (1 * (x + y) - x * 1) / (x + y)^2 = y / (x + y)^2
        MultivariateRationalFunctionOverFp dfdx = f.differentiate(0);
        assertEquals(y, dfdx.num());
        assertEquals(x.add(y).mul(x.add(y)), dfdx.den());

        // df/dy = (0 * (x + y) - x * 1) / (x + y)^2 = -x / (x + y)^2
        MultivariateRationalFunctionOverFp dfdy = f.differentiate(1);
        MultivariatePolynomialOverFpStrategy polyStrategy = new MultivariatePolynomialOverFpStrategy(mod);
        assertEquals(polyStrategy.neg(x), dfdy.num());
        assertEquals(x.add(y).mul(x.add(y)), dfdy.den());
    }

    @Test
    public void testDifferentiationHigherOrder() {
        long mod = 998244353;
        MultivariatePolynomial<Long> x = MultivariatePolynomialOverFp.singleTerm(mod, 0, 1, 1, 1);
        MultivariatePolynomial<Long> one = new MultivariatePolynomialOverFp(mod).one();

        // f(x) = 1 / x
        MultivariateRationalFunctionOverFp f = new MultivariateRationalFunctionOverFp(one, x, mod);

        // f'(x) = -1 / x^2
        MultivariateRationalFunctionOverFp df = f.differentiate(0);
        MultivariatePolynomialOverFpStrategy polyStrategy = new MultivariatePolynomialOverFpStrategy(mod);
        assertEquals(polyStrategy.neg(one), df.num());
        assertEquals(x.mul(x), df.den());

        // f''(x) = 2 / x^3
        MultivariateRationalFunctionOverFp ddf = df.differentiate(0);
        assertEquals(one.multiply(2L), ddf.num());
        assertEquals(x.pow(3), ddf.den());
    }
}
