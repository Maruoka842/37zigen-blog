package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic;

public class PolynomialFpDynamicSquareFreeTest {

	@Test
	public void testConstant() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		assertTrue(poly.isSquareFree(new long[] { 1 }));
		assertTrue(poly.isSquareFree(new long[] { 123 }));
		assertFalse(poly.isSquareFree(new long[] { 0 }));
	}

	@Test
	public void testLinear() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		assertTrue(poly.isSquareFree(new long[] { 1, 1 })); // x + 1
		assertTrue(poly.isSquareFree(new long[] { 5, 2 })); // 2x + 5
	}

	@Test
	public void testQuadratic() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// (x-1)(x-2) = x^2 - 3x + 2
		assertTrue(poly.isSquareFree(new long[] { 2, 998244350, 1 }));
		// (x-1)^2 = x^2 - 2x + 1
		assertFalse(poly.isSquareFree(new long[] { 1, 998244351, 1 }));
	}

	@Test
	public void testHigherDegree() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// (x-1)(x-2)(x-3)
		long[] f1 = poly.mul(new long[] { 998244352, 1 }, poly.mul(new long[] { 998244351, 1 }, new long[] { 998244350, 1 }));
		assertTrue(poly.isSquareFree(f1));

		// (x-1)^2 (x-2)
		long[] f2 = poly.mul(new long[] { 1, 998244351, 1 }, new long[] { 998244351, 1 });
		assertFalse(poly.isSquareFree(f2));
	}

	@Test
	public void testSmallMod() {
		// p = 3
		PolynomialFpDynamic poly = PolynomialFpDynamic.of(3);
		// x^3 = (x)^3. f' = 3x^2 = 0.
		assertFalse(poly.isSquareFree(new long[] { 0, 0, 0, 1 }));

		// x^3 + x + 1. f' = 3x^2 + 1 = 1.
		// gcd(x^3+x+1, 1) = 1.
		assertTrue(poly.isSquareFree(new long[] { 1, 1, 0, 1 }));

		// (x+1)^3 = x^3 + 3x^2 + 3x + 1 = x^3 + 1. f' = 3x^2 = 0.
		assertFalse(poly.isSquareFree(new long[] { 1, 0, 0, 1 }));

		// x^2 + 1 in mod 3. Irreducible. f' = 2x. gcd(x^2+1, 2x) = 1.
		assertTrue(poly.isSquareFree(new long[] { 1, 0, 1 }));
	}
}
