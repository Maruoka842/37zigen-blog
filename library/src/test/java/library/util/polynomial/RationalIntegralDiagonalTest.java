package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class RationalIntegralDiagonalTest {
    @Test
    public void testDiagonalBinomial() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x0", "x1");
        // 1 / (1 - x - y)  diagonal is 1/sqrt(1-4t)
        // annihilator: (1-4t) Dt - 2 = 0, which is equivalent to (t - 1/4) Dt + 1/2 = 0
        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("1 - x0 - x1");

        List<long[]> res = RationalIntegralReduction.computeDiagonalPicardFuchs(a, f, 1);
        assertNotNull(res);
        assertEquals(2, res.size(), "Order of the differential operator should be 1 (size of coefficients list = 2)");

        long[] c0 = res.get(0);
        long[] c1 = res.get(1);

        // c0 should be [1/2]
        assertEquals(1, c0.length);
        assertEquals(499122177L, c0[0], "c0 should be exactly 1/2 mod 998244353");

        // c1 should be [-1/4, 1]
        assertEquals(2, c1.length);
        assertEquals(249561088L, c1[0], "c1 constant term should be exactly -1/4 mod 998244353");
        assertEquals(1L, c1[1], "c1 leading coefficient should be exactly 1");
    }

    @Test
    public void testDiagonalBinomialMatrix() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x0", "x1");
        // 1 / (1 - x - y)  diagonal is 1/sqrt(1-4t)
        // annihilator: (1-4t) Dt - 2 = 0, which is equivalent to (t - 1/4) Dt + 1/2 = 0
        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("1 - x0 - x1");

        List<long[]> res = RationalIntegralReduction.computeDiagonalPicardFuchsMatrix(a, f, 1);
        assertNotNull(res);
        assertEquals(2, res.size(), "Order of the differential operator should be 1 (size of coefficients list = 2)");

        long[] c0 = res.get(0);
        long[] c1 = res.get(1);

        // c0 should be [1/2]
        assertEquals(1, c0.length);
        assertEquals(499122177L, c0[0], "c0 should be exactly 1/2 mod 998244353");

        // c1 should be [-1/4, 1]
        assertEquals(2, c1.length);
        assertEquals(249561088L, c1[0], "c1 constant term should be exactly -1/4 mod 998244353");
        assertEquals(1L, c1[1], "c1 leading coefficient should be exactly 1");
    }

    @Test
    public void testDiagonalApery() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x0", "x1");
        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("1 - x0 - x1 - x0*x1");
        List<long[]> res = RationalIntegralReduction.computeDiagonalPicardFuchs(a, f, 1);
        assertNotNull(res);
    }
}
