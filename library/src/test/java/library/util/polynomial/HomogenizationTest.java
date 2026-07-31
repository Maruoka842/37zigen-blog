package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class HomogenizationTest {
    @Test
    public void test() {
    	long mod = 998244353;
    	PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "t");
        MultivariatePolynomial a = parser.parse("y^2");
        MultivariatePolynomial f = parser.parse("x^2");
        // Compute Picard-Fuchs equation.
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 2, 1);
        for (var p : pf) {
        	tr(p);
        }
        //annihilator should be 1.
        assertEquals(1, pf.size());
        assertArrayEquals(pf.get(0), new long[] {1});
        //斉次化により R = y^2/(x^2*extra_var^3) = x y^2 / (x * extra_var) ^ 3
        //∂_y (x * extra_var) = 0 なので、<f_x, f_y>で割ることはできないが、syzyでpoly(y)f_y=0。これで割ると 0 
    }
    
    void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
    @Test
    public void testPicardFuchsWithPoleOrder() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial f = P.parse("x - t");
        MultivariatePolynomial a = P.parse("1");

        // Picard-Fuchs for 1/(x-t) should be D_t
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 1, 1);
        assertNotNull(pf);
        assertTrue(PolynomialFpDynamic.MOD998244353.isZero(pf.get(0)));
        assertFalse(PolynomialFpDynamic.MOD998244353.isZero(pf.get(1)));
    }

    @Test
    public void testMatrix() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "t");
        MultivariatePolynomial a = parser.parse("y^2");
        MultivariatePolynomial f = parser.parse("x^2");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 2, 1);
        assertNotNull(pf);
        assertEquals(1, pf.size());
        assertArrayEquals(pf.get(0), new long[] {1});
    }

    @Test
    public void testPicardFuchsMatrixWithPoleOrder() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial f = P.parse("x - t");
        MultivariatePolynomial a = P.parse("1");

        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf);
        assertTrue(PolynomialFpDynamic.MOD998244353.isZero(pf.get(0)));
        assertFalse(PolynomialFpDynamic.MOD998244353.isZero(pf.get(1)));
    }
}
