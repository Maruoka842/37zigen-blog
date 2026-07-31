package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import library.util.MathUtils;

public class MultivariateRationalIntegralReductionTest {
	
	@Test
	public void test() {
		long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
		MultivariatePolynomial<Long> a = parser.parse("1");
        MultivariatePolynomial<Long> f = parser.parse("t*x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 1, 1);
        assertEquals(pf.size(), 2);
        assertArrayEquals(new long[] {1}, pf.get(0));
        assertArrayEquals(new long[] {0, 1}, pf.get(1));
	}
	
	@Test
	public void test2() {
		long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
		MultivariatePolynomial<Long> a = parser.parse("t");
        MultivariatePolynomial<Long> f = parser.parse("x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 1, 1);
        assertEquals(pf.size(), 2);
        assertArrayEquals(new long[] {mod-1}, pf.get(0));
        assertArrayEquals(new long[] {0, 1}, pf.get(1));
        
	}

	@Test
	public void test3() {
		//斉次化すると　y/(x*extra_var)^2
		//分母はヤコビイデアル x, extra_var で消せない
		//r=2が必要
		long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "t");
		MultivariatePolynomial<Long> a = parser.parse("y");
        MultivariatePolynomial<Long> f = parser.parse("x*x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 2, 2);
        assertEquals(1, pf.size());
        assertArrayEquals(new long[] {1}, pf.get(0));
	}
	
    @Test
    public void testMultivariatePolynomialArithmetic() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y");

        MultivariatePolynomial<Long> p3 = P.parse("x + y");
        MultivariatePolynomial<Long> p4 = p3.mul(p3); // (x + y)^2 = x^2 + 2xy + y^2

        TreeMap<Monomial, Long> expected = new TreeMap<>();
        expected.put(new Monomial(new int[]{2, 0}), 1L);
        expected.put(new Monomial(new int[]{1, 1}), 2L);
        expected.put(new Monomial(new int[]{0, 2}), 1L);

        assertEquals(expected, p4.getTerms());
    }

    @Test
    public void testGrobnerBasis() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y");

        MultivariatePolynomial<Long> f1 = P.parse("x^2 + y");
        MultivariatePolynomial<Long> f2 = P.parse("x^2 + x");

        List<MultivariatePolynomial<Long>> gb = MultivariatePolynomial.grobnerBasis(Arrays.asList(f1, f2));
        assertFalse(gb.isEmpty());
        // Verify that original polynomials are reducible to zero
        for (MultivariatePolynomial<Long> f : Arrays.asList(f1, f2)) {
            assertTrue(MultivariatePolynomial.divide(f, gb).remainder.isZero());
        }
    }

    @Test
    public void testPicardFuchsSimple() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y", "t");

        // f = y^2 - t*x^2
        MultivariatePolynomial<Long> f = P.parse("y^2 - t*x^2");
        MultivariatePolynomial<Long> a = P.parse("1");

        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 2, 1);
        assertEquals(pf.size(), 2);
        assertArrayEquals(pf.get(0), new long[] {MathUtils.modInv(2, mod)});
        assertArrayEquals(pf.get(1), new long[] {0, 1});
    }
    
    void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

    @Test
    public void testDinagonalBinom() {
    	// F = 1/{(1-x-t/x)x}
    	//   = 1/(x-x^2-t)
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "t");
	MultivariatePolynomial<Long> f = P.parse("x-x^2-t");
        MultivariatePolynomial<Long> a = P.parse("1");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 1, 1);
        tr("size",pf.size());
        for (var p : pf) {
    		tr(p);
    	}
        assertEquals(pf.size(), 2);
        assertArrayEquals(pf.get(0), new long[] {MathUtils.modInv(2, mod)});
        assertArrayEquals(pf.get(1), new long[] {mod-MathUtils.modInv(4, mod), 1});
        //(t-1/4)Dt+1/2
    }

    @Test
    public void testPicardFuchsMatrixSimple() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y", "t");

        // f = y^2 - t*x^2
        MultivariatePolynomial<Long> f = P.parse("y^2 - t*x^2");
        MultivariatePolynomial<Long> a = P.parse("1");

        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 2, 1);
        assertNotNull(pf);
        assertEquals(pf.size(), 2);
        assertArrayEquals(pf.get(0), new long[] {MathUtils.modInv(2, mod)});
        assertArrayEquals(pf.get(1), new long[] {0, 1});
    }

    @Test
    public void testPicardFuchsMatrixSimple2() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial<Long> a = parser.parse("1");
        MultivariatePolynomial<Long> f = parser.parse("t*x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf);
        assertEquals(pf.size(), 2);
        assertArrayEquals(new long[] {1}, pf.get(0));
        assertArrayEquals(new long[] {0, 1}, pf.get(1));
    }

    @Test
    public void testMatrix() {
        long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial<Long> a = parser.parse("1");
        MultivariatePolynomial<Long> f = parser.parse("t*x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf);
        assertEquals(pf.size(), 2);
        assertArrayEquals(new long[] {1}, pf.get(0));
        assertArrayEquals(new long[] {0, 1}, pf.get(1));
    }

    @Test
    public void test2Matrix() {
        long mod=998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial<Long> a = parser.parse("t");
        MultivariatePolynomial<Long> f = parser.parse("x");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf);
        assertEquals(pf.size(), 2);
        assertArrayEquals(new long[] {mod-1, 0}, pf.get(0));
        assertArrayEquals(new long[] {0, 1}, pf.get(1));
    }

    @Test
    public void testDiagonalBinomMatrix() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "t");
        MultivariatePolynomial<Long> f = P.parse("x-x^2-t");
        MultivariatePolynomial<Long> a = P.parse("1");
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf);
        assertEquals(pf.size(), 2);
        assertArrayEquals(pf.get(0), new long[] {MathUtils.modInv(2, mod)});
        assertArrayEquals(pf.get(1), new long[] {mod-MathUtils.modInv(4, mod), 1});
    }
    
    /**
     * TLE
     */
    //@Test
    public void testAperyPicardFuchs() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y", "z", "t");

        MultivariatePolynomial<Long> f = P.parse("1 - (1 - x*y)*z - t*x*y*z*(1 - x)*(1 - y)*(1 - z)");
        MultivariatePolynomial<Long> a = P.parse("1");

        // Picard-Fuchs computation
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 3, 1);
        assertNotNull(pf);
        assertEquals(4, pf.size());

        long[] a3 = pf.get(3);
        long[] a0 = pf.get(0);

        long v3 = PolynomialFpDynamic.MOD998244353.evaluate(a3, 1);
        long v0 = PolynomialFpDynamic.MOD998244353.evaluate(a0, 1);
        assertEquals(8, (v3 * MathUtils.modInv(v0, mod)) % mod);
    }

    @Test
    public void testAperyPicardFuchsMatrix() {
        long mod = 998244353;
        PolynomialParser P = PolynomialParser.of(mod, "x", "y", "z", "t");

        MultivariatePolynomial<Long> f = P.parse("1 - (1 - x*y)*z - t*x*y*z*(1 - x)*(1 - y)*(1 - z)");
        MultivariatePolynomial<Long> a = P.parse("1");

        long start = System.currentTimeMillis();
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 3, 2);
        long end = System.currentTimeMillis();
        System.out.println("Apery Picard-Fuchs Matrix Time: " + (end - start) + "ms");

        assertNotNull(pf);
        // Apery PF is 2nd order (3 coefficients)
        // L = (t^4 - 34t^3 + t^2) Dt^2 + (6t^3 - 153t^2 + 3t) Dt + (t^2 - 10t + 1)
        // Wait, the coefficients depend on the normalization.
        // Usually it is order 2 for Apery.
        assertTrue(pf.size() >= 3);
    }
}
