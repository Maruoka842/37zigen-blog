package library.util.polynomial;

import org.junit.jupiter.api.Test;

public class PolynomialPrintTest {
	@Test
	public void test1D() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		long[] f = { 1, 2, 0, 1 }; // 1 + 2x + x^3
		poly.printPolyAsExpr("1D Test (1 + 2x + x^3)", f);

		long[] zero = { 0, 0 };
		poly.printPolyAsExpr("1D Zero Test", zero);
	}

	@Test
	public void test2D() {
		PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
		long[][] f = {
			{ 1, 0, 3 }, // 1 + 3y^2
			{ 0, 4 },    // 4xy
			{ 5 }        // 5x^2
		};
		poly.printPolyAsExpr("2D Test (1 + 3y^2 + 4xy + 5x^2)", f);
	}

	@Test
	public void test3D() {
		PolynomialFpDynamic3D poly = PolynomialFpDynamic3D.MOD998244353;
		long[][][] f = new long[2][2][2];
		f[0][0][0] = 1;
		f[1][1][1] = 1;
		poly.printPolyAsExpr("3D Test (1 + xyz)", f);
	}

	@Test
	public void test4D() {
		PolynomialFpDynamic4D poly = PolynomialFpDynamic4D.MOD998244353;
		long[][][][] f = new long[2][1][1][2];
		f[0][0][0][0] = 10;
		f[1][0][0][1] = 1; // xw
		poly.printPolyAsExpr("4D Test (10 + xw)", f);
	}
}
