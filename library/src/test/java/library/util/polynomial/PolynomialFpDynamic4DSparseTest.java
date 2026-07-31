package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic4D;

public class PolynomialFpDynamic4DSparseTest {
	@Test
	public void test4DSparseOperations() {
		PolynomialFpDynamic4D poly4d = PolynomialFpDynamic4D.MOD998244353;
		// f = 1 + 2x + 3y + 4z + 5w
		long[][][][] f = {
			{ { { 1, 5 }, { 4 } }, { { 3 } } },
			{ { { 2 } } }
		};
		int ni = 3, nj = 3, nk = 3, nl = 3;

		// sparseInv vs dense inv
		long[][][][] denseA = new long[ni][nj][nk][nl];
		for (int i = 0; i < f.length; i++)
			for (int j = 0; j < f[i].length; j++)
				for (int k = 0; k < f[i][j].length; k++)
					for (int l = 0; l < f[i][j][k].length; l++)
						denseA[i][j][k][l] = f[i][j][k][l];

		long[][][][] inv1 = poly4d.invNaive(denseA);
		long[][][][] inv2 = poly4d.sparseInv(f, ni, nj, nk, nl);
		for (int i = 0; i < ni; i++)
			for (int j = 0; j < nj; j++)
				for (int k = 0; k < nk; k++)
					assertArrayEquals(inv1[i][j][k], inv2[i][j][k], "4D sparseInv failed at (" + i + "," + j + "," + k + ")");

		// sparseMul
		long[][][][] mul1 = poly4d.resize(poly4d.mul(denseA, denseA));
		long[][][][] mul2 = poly4d.resize(poly4d.sparseMul(denseA, denseA));
		assertEquals(mul1.length, mul2.length);
		for (int i = 0; i < mul1.length; i++) {
			assertEquals(mul1[i].length, mul2[i].length);
			for (int j = 0; j < mul1[i].length; j++) {
				assertEquals(mul1[i][j].length, mul2[i][j].length);
				for (int k = 0; k < mul1[i][j].length; k++)
					assertArrayEquals(mul1[i][j][k], mul2[i][j][k], "4D sparseMul failed at (" + i + "," + j + "," + k + ")");
			}
		}

		// sparseLog and sparseExp
		long[][][][] log = poly4d.sparseLog(f, ni, nj, nk, nl);
		long[][][][] exp = poly4d.sparseExp(log, ni, nj, nk, nl);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						assertEquals(denseA[i][j][k][l], exp[i][j][k][l], "4D log/exp failed at (" + i + "," + j + "," + k + "," + l + ")");
					}
				}
			}
		}

		// sparsePow
		long p = 2;
		long[][][][] pow = poly4d.sparsePow(f, ni, nj, nk, nl, p);
		long[][][][] expectedPow = poly4d.mul(f, f);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						long val = (i < expectedPow.length && j < expectedPow[i].length && k < expectedPow[i][j].length && l < expectedPow[i][j][k].length)
								? expectedPow[i][j][k][l]
								: 0;
						assertEquals(val, pow[i][j][k][l], "4D sparsePow failed at (" + i + "," + j + "," + k + "," + l + ")");
					}
				}
			}
		}

		// sparseSqrt
		long[][][][] sqrt = poly4d.sparseSqrt(f, ni, nj, nk, nl);
		long[][][][] sqrtSq = poly4d.mul(sqrt, sqrt);
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < nj; j++) {
				for (int k = 0; k < nk; k++) {
					for (int l = 0; l < nl; l++) {
						long val = (i < sqrtSq.length && j < sqrtSq[i].length && k < sqrtSq[i][j].length && l < sqrtSq[i][j][k].length)
								? sqrtSq[i][j][k][l]
								: 0;
						assertEquals(denseA[i][j][k][l], val, "4D sparseSqrt failed at (" + i + "," + j + "," + k + "," + l + ")");
					}
				}
			}
		}
	}
}
