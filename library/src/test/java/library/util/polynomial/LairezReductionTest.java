package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import library.util.Fp;
import library.util.MathUtils;
import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic2D;
import java.util.Arrays;
import java.util.TreeMap;
import library.util.polynomial.Monomial;
import library.util.polynomial.MultivariatePolynomial;
import library.util.polynomial.PolynomialFpDynamic2D.LairezForm;
import library.util.polynomial.PolynomialFpDynamic2D.NormalFormResult;
import library.util.polynomial.PolynomialParser;
import library.util.polynomial.RationalIntegralReduction;

public class LairezReductionTest {

    // @Test
    // public void testAperyExample() {
    //     // F = 1/(1-(1-x*y)*z-t*x*y*z*(1-x)*(1-y)*(1-z))
    //     long mod = 998244353;
    //     PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z", "t");
    //     MultivariatePolynomial f = parser.parse("1-(1-x*y)*z-t*x*y*z*(1-x)*(1-y)*(1-z)");
    //     MultivariatePolynomial a = new MultivariatePolynomialOverFp(mod).one();
    //
    //     // This is a complex example. It takes significant time due to the number of reductions.
    //     List<long[]> res = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 3, 1);
    //     assertNotNull(res);
    //     System.out.println("Apery PF order: " + (res.size() - 1));
    // }

    
    @Test
    public void test() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("x*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        var p=RationalIntegralReduction.reduceHomogeneous(a, f, 2, 1);
        assertTrue(p.isZero());
    }
    
    @Test
    public void testReductionRZp64() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("x*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        var p=RationalIntegralReduction.reduceHomogeneousZp64(a, f, 2, 1);
        assertTrue(p.isZero());
    }
    
    
    @Test
    public void test2() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("7*x^4*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        var p=RationalIntegralReduction.reduceHomogeneous(a, f, 3, 1);
        assertTrue(p.isZero());
    }
    
    @Test
    public void test3() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("7*x^4*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        var p=RationalIntegralReduction.reduceHomogeneousZp64(a, f, 3, 1);
        assertTrue(p.isZero());
    }
    
    
    
    @Test
    public void test4() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("4x^6+7*x^4*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        // f_x = y^2
        // f_y = 2xy
        // f_z = -3z^2
        // 7x^4*y^2 = (7x^3y/2) * f_y
        // GD reductionでは
        // 4x^6 + der_y (7x^3y/2)
        // =4x^6 + 7x^3 /2
        // が残る。
        
        // 基本syzygy 2x f_x - y f_y = 0 より
        // 2x^4 f_x - x^3 y f_y = 0 も syzygy
        // divergence
        //  der_x (2x^4) + der_y (-x^3 y)
        // =8x^3-x^3
        // =7x^3
        // より 7x^3 /2 は消える。
        var p=RationalIntegralReduction.reduceHomogeneous(a, f, 3, 1);
        assertEquals(parser.parse("4*x^6"), p);
    }

    @Test
    public void test5() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
        MultivariatePolynomial a = parser.parse("4x^6+7*x^4*y^2");
        MultivariatePolynomial f = parser.parse("x*y^2-z^3");
        // f_x = y^2
        // f_y = 2xy
        // f_z = -3z^2
        // 7x^4*y^2 = (7x^3y/2) * f_y
        // GD reductionでは
        // 4x^6 + der_y (7x^3y/2)
        // =4x^6 + 7x^3 /2
        // が残る。
        
        // 基本syzygy 2x f_x - y f_y = 0 より
        // 2x^4 f_x - x^3 y f_y = 0 も syzygy
        // divergence
        //  der_x (2x^4) + der_y (-x^3 y)
        // =8x^3-x^3
        // =7x^3
        // より 7x^3 /2 は消える。
        var p=RationalIntegralReduction.reduceHomogeneousZp64(a, f, 3, 1);
        assertEquals(parser.parse("4*x^6"), p);
    }
    
    @Test
    public void test6() {
    	long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z");
		MultivariatePolynomial<Long> a = parser.parse("y^7");
		MultivariatePolynomial<Long> f = parser.parse("x^4*y-x^2*y*z^2+x*z^4");
		var p=RationalIntegralReduction.reduceHomogeneousZp64(a, f, 2, 3);
		assertTrue(p.equals(parser.parse("68723153*z*z")));
		System.out.println(p);
    }

}
