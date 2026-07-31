package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic3D;

public class PolynomialFpDynamic3DSparseTest {
	@Test
	public void test3DSparseOperations() {
		PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
		// f = 1 + 2x + 3y + 4z
		long[][][] f = {
			{ { 1, 4 }, { 3 } },
			{ { 2 } }
		};
		int ni = 4, nj = 4, nk = 4;

		// sparseInv vs dense inv
		long[][][] denseA = new long[ni][nj][nk];
		for (int i = 0; i < f.length; i++)
			for (int j = 0; j < f[i].length; j++)
				for (int k = 0; k < f[i][j].length; k++)
					denseA[i][j][k] = f[i][j][k];

		long[][][] inv1 = poly3d.invNaive(denseA);
		long[][][] inv2 = poly3d.sparseInv(f, ni, nj, nk);
		for (int i = 0; i < ni; i++)
			for (int j = 0; j < nj; j++)
				assertArrayEquals(inv1[i][j], inv2[i][j], "3D sparseInv failed at (" + i + "," + j + ")");

		// sparseMul
		long[][][] mul1 = poly3d.resize(poly3d.mul(denseA, denseA));
		long[][][] mul2 = poly3d.resize(poly3d.sparseMul(denseA, denseA));
		assertEquals(mul1.length, mul2.length);
		for (int i = 0; i < mul1.length; i++) {
			assertEquals(mul1[i].length, mul2[i].length);
			for (int j = 0; j < mul1[i].length; j++)
				assertArrayEquals(mul1[i][j], mul2[i][j], "3D sparseMul failed at (" + i + "," + j + ")");
		}

		// sparseLog and sparseExp
		long[][][] log = poly3d.sparseLog(f, ni, nj, nk);
		long[][][] exp = poly3d.sparseExp(log, ni, nj, nk);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					assertEquals(denseA[i][j][k], exp[i][j][k], "3D log/exp failed at (" + i + "," + j + "," + k + ")");
				}
			}
		}

		// sparsePow
		long p = 2;
		long[][][] pow = poly3d.sparsePow(f, ni, nj, nk, p);
		long[][][] expectedPow = poly3d.mul(f, f);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					long val = (i < expectedPow.length && j < expectedPow[i].length && k < expectedPow[i][j].length)
							? expectedPow[i][j][k]
							: 0;
					assertEquals(val, pow[i][j][k], "3D sparsePow failed at (" + i + "," + j + "," + k + ")");
				}
			}
		}

		// sparseSqrt
		long[][][] sqrt = poly3d.sparseSqrt(f, ni, nj, nk);
		long[][][] sqrtSq = poly3d.mul(sqrt, sqrt);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					long val = (i < sqrtSq.length && j < sqrtSq[i].length && k < sqrtSq[i][j].length)
							? sqrtSq[i][j][k]
							: 0;
					assertEquals(denseA[i][j][k], val, "3D sparseSqrt failed at (" + i + "," + j + "," + k + ")");
				}
			}
		}
	}
}
