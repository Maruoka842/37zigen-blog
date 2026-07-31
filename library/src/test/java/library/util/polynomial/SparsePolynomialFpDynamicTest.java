package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic2D;

public class SparsePolynomialFpDynamicTest {
	@Test
	public void test1DSparseOperations() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// f = 1 + 2x^2 + 3x^5
		long[] f = {1, 0, 2, 0, 0, 3};
		int n = 10;

		// inv
		long[] inv1 = poly.inv(Arrays.copyOf(f, n));
		long[] inv2 = poly.sparseInv(f, n);
		assertArrayEquals(inv1, inv2, "1D sparseInv failed");

		// log
		long[] log1 = poly.log(Arrays.copyOf(f, n));
		long[] log2 = poly.sparseLog(f, n);
		assertArrayEquals(log1, log2, "1D sparseLog failed");

		// exp (log of f has 0 at constant term, so we can exp it)
		long[] exp1 = poly.exp(log1);
		long[] exp2 = poly.sparseExp(log2, n);
		assertArrayEquals(exp1, exp2, "1D sparseExp failed");

		// pow
		long k = 3;
		long[] pow1 = poly.pow(Arrays.copyOf(f, n), k);
		long[] pow2 = poly.sparsePow(f, n, k);
		assertArrayEquals(pow1, pow2, "1D sparsePow failed");

		// sqrt
		long[] sqrt1 = poly.sqrt(Arrays.copyOf(f, n));
		long[] sqrt2 = poly.sparseSqrt(f, n);
		assertArrayEquals(sqrt1, sqrt2, "1D sparseSqrt failed");

		// sparseMul
		long[] g = {0, 1, 0, 4}; // g = x + 4x^3
		long[] mul1 = poly.mul(f, g);
		long[] mul2 = poly.sparseMul(f, g);
		assertArrayEquals(mul1, mul2, "1D sparseMul failed");

		// Test mul dispatch logic
		long[] largeA = new long[1000];
		Arrays.fill(largeA, 1);
		long[] sparseB = new long[1000];
		sparseB[0] = 1;
		sparseB[500] = 2;
		long[] res = poly.mul(largeA, sparseB);
		assertEquals(1999, res.length);
		assertEquals(1, res[0]);
		assertEquals(1, res[499]);
		assertEquals(3, res[500]); // 1*2 + 1*1
		assertEquals(3, res[999]);
		assertEquals(2, res[1000]);
		assertEquals(2, res[1499]);
		assertEquals(0, res[1500]);
	}

	@Test
	public void test2DSparseOperations() {
		PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
		// f = 1 + 2x + 3y + 4x^2 + 5y^2
		long[][] f = {
			{1, 3, 5},
			{2, 0, 0},
			{4, 0, 0}
		};
		int nx = 5, ny = 5;

		// sparseInv vs dense inv
		// poly2d.inv(a) returns a dense inv
		long[][] denseA = new long[nx][ny];
		for(int i=0; i<f.length; i++) for(int j=0; j<f[i].length; j++) denseA[i][j] = f[i][j];

		long[][] inv1 = poly2d.inv(denseA);
		long[][] inv2 = poly2d.sparseInv(f, nx, ny);
		for(int i=0; i<nx; i++) assertArrayEquals(inv1[i], inv2[i], "2D sparseInv failed at row " + i);

		// sparseLog and sparseExp
		long[][] log = poly2d.sparseLog(f, nx, ny);
		long[][] exp = poly2d.sparseExp(log, nx, ny);
		for(int i=0; i<nx; i++) {
			for(int j=0; j<ny; j++) {
				assertEquals(denseA[i][j], exp[i][j], "2D log/exp failed at (" + i + "," + j + ")");
			}
		}

		// sparsePow
		long k = 2;
		long[][] pow = poly2d.sparsePow(f, nx, ny, k);
		long[][] expectedPow = poly2d.mul(f, f);
		for(int i=0; i<nx; i++) {
			for(int j=0; j<ny; j++) {
				long val = (i < expectedPow.length && j < expectedPow[i].length) ? expectedPow[i][j] : 0;
				assertEquals(val, pow[i][j], "2D sparsePow failed at (" + i + "," + j + ")");
			}
		}

		// sparseSqrt
		long[][] sqrt = poly2d.sparseSqrt(f, nx, ny);
		long[][] sqrtSq = poly2d.mul(sqrt, sqrt);
		for(int i=0; i<nx; i++) {
			for(int j=0; j<ny; j++) {
				long val = (i < sqrtSq.length && j < sqrtSq[i].length) ? sqrtSq[i][j] : 0;
				assertEquals(denseA[i][j], val, "2D sparseSqrt failed at (" + i + "," + j + ")");
			}
		}
	}
}