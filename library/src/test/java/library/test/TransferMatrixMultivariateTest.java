package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import library.util.algebra.instance.FractionFieldElement;
import library.util.graph.Edge;
import library.util.graph.LongValueDigraph;
import library.util.graph.TransferMatrixMethod;
import org.junit.jupiter.api.Test;

public class TransferMatrixMultivariateTest {

	@Test
	public void testSingleVertexWithLoop() {
		long mod = 998244353;
		int N = 1;
		LongValueDigraph g = new LongValueDigraph(N);
		g.addEdge(0, 0, 2); // Loop with weight 2

		// Walk generating function: x0 + 2*x0^2 + 4*x0^3 + ... = x0 / (1 - 2*x0)
		// Numerator = x0, Denominator = 1 - 2*x0
		FractionFieldElement<long[][][]> res = TransferMatrixMethod.multivariateFreeWalkGeneratingFunction(g, mod);

		// den = 1 - 2*x0
		long[][][] expectedDen = new long[2][1][1];
		expectedDen[0][0][0] = 1;
		expectedDen[1][0][0] = mod - 2;

		// num = x0
		long[][][] expectedNum = new long[2][1][1];
		expectedNum[1][0][0] = 1;

		assertPolyEquals(expectedNum, res.num(), mod);
		assertPolyEquals(expectedDen, res.den(), mod);
	}

	@Test
	public void testTwoVerticesOneEdge() {
		long mod = 998244353;
		int N = 2;
		LongValueDigraph g = new LongValueDigraph(N);
		g.addEdge(0, 1, 1); // 0 -> 1 with weight 1

		// Walks: (0), (1), (0, 1)
		// G = x0 + x1 + x0*x1
		FractionFieldElement<long[][][]> res = TransferMatrixMethod.multivariateFreeWalkGeneratingFunction(g, mod);

		long[][][] expectedDen = {{{1}}};
		long[][][] expectedNum = new long[2][2][1];
		expectedNum[1][0][0] = 1; // x0
		expectedNum[0][1][0] = 1; // x1
		expectedNum[1][1][0] = 1; // x0*x1

		assertPolyEquals(expectedNum, res.num(), mod);
		assertPolyEquals(expectedDen, res.den(), mod);
	}

	@Test
	public void testThreeVerticesCycle() {
		long mod = 998244353;
		int N = 3;
		LongValueDigraph g = new LongValueDigraph(N);
		g.addEdge(0, 1, 1);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 0, 1);

		// (I - WX) = [1, -x1, 0; 0, 1, -x2; -x0, 0, 1]
		// det = 1 - x0*x1*x2
		// adj = [1, x1, x1*x2; x0*x2, 1, x2; x0, x0*x1, 1]
		// u^T adj v = [x0, x1, x2] * adj * [1; 1; 1]
		//           = [x0 + x0*x1*x2 + x0*x2, x0*x1 + x1 + x0*x1*x2, x0*x1*x2 + x1*x2 + x2] * [1; 1; 1]
		//           = x0+x1+x2 + x0*x1+x1*x2+x2*x0 + 3*x0*x1*x2
		FractionFieldElement<long[][][]> res = TransferMatrixMethod.multivariateFreeWalkGeneratingFunction(g, mod);

		long[][][] expectedDen = {
				{{1}, {0}},
				{{0}, {0}},
				{{0}, {0}}
		};
		expectedDen[0][0][0] = 1;
		// -x0*x1*x2
		if (expectedDen.length <= 1) expectedDen = new long[2][1][1];
		if (expectedDen[1].length <= 1) expectedDen[1] = new long[2][1];
		if (expectedDen[1][1].length <= 1) expectedDen[1][1] = new long[2];
		expectedDen[1][1][1] = mod - 1;

		assertPolyEquals(expectedDen, res.den(), mod);

		// num = x0+x1+x2 + x0x1+x1x2+x2x0 + 3x0x1x2
		// num[1][0][0]=1, num[0][1][0]=1, num[0][0][1]=1
		// num[1][1][0]=1, num[0][1][1]=1, num[1][0][1]=1
		// num[1][1][1]=3
		long[][][] expectedNum = new long[2][2][2];
		expectedNum[1][0][0] = 1;
		expectedNum[0][1][0] = 1;
		expectedNum[0][0][1] = 1;
		expectedNum[1][1][0] = 1;
		expectedNum[0][1][1] = 1;
		expectedNum[1][0][1] = 1;
		expectedNum[1][1][1] = 3;

		assertPolyEquals(expectedNum, res.num(), mod);
	}

	@Test
	public void testNoModVersion() {
		int N = 2;
		LongValueDigraph g = new LongValueDigraph(N);
		g.addEdge(0, 0, 1);
		g.addEdge(0, 1, 1);
		g.addEdge(1, 1, 1);

		// I - WX = [1 - x0, -x1; 0, 1 - x1]
		// det = (1 - x0)(1 - x1) = 1 - x0 - x1 + x0x1
		// u^T adj v = [x0, x1] * [1 - x1, x1; 0, 1 - x0] * [1; 1]
		//           = [x0(1 - x1), x0x1 + x1(1 - x0)] * [1; 1]
		//           = x0 - x0x1 + x0x1 + x1 - x0x1 = x0 + x1 - x0x1
		FractionFieldElement<long[][][]> res = TransferMatrixMethod.multivariateFreeWalkGeneratingFunction(g);

		long[][][] expectedDen = new long[2][2][1];
		expectedDen[0][0][0] = 1;
		expectedDen[1][0][0] = -1;
		expectedDen[0][1][0] = -1;
		expectedDen[1][1][0] = 1;

		long[][][] expectedNum = new long[2][2][1];
		expectedNum[1][0][0] = 1;
		expectedNum[0][1][0] = 1;
		expectedNum[1][1][0] = -1;

		assertPolyEquals(expectedNum, res.num(), Long.MAX_VALUE); // Large value to effectively skip mod
		assertPolyEquals(expectedDen, res.den(), Long.MAX_VALUE);
	}

	private void assertPolyEquals(long[][][] expected, long[][][] actual, long mod) {
		int n = Math.max(expected.length, actual.length);
		int m = 0;
		int l = 0;
		for (int i = 0; i < expected.length; i++) {
			m = Math.max(m, expected[i].length);
			for (int j = 0; j < expected[i].length; j++) l = Math.max(l, expected[i][j].length);
		}
		for (int i = 0; i < actual.length; i++) {
			m = Math.max(m, actual[i].length);
			for (int j = 0; j < actual[i].length; j++) l = Math.max(l, actual[i][j].length);
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int k = 0; k < l; k++) {
					long v1 = (i < expected.length && j < expected[i].length && k < expected[i][j].length) ? expected[i][j][k] : 0;
					long v2 = (i < actual.length && j < actual[i].length && k < actual[i][j].length) ? actual[i][j][k] : 0;
					v1 = (v1 % mod + mod) % mod;
					v2 = (v2 % mod + mod) % mod;
					if (v1 != v2) {
						throw new AssertionError("Mismatch at [" + i + "][" + j + "][" + k + "]: expected " + v1 + " but got " + v2);
					}
				}
			}
		}
	}
}
