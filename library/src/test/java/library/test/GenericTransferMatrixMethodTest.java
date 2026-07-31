package library.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.ZnStrategy;
import library.util.graph.LongValueDigraph;
import library.util.graph.TransferMatrixMethod;
import library.util.graph.ValueDigraph;
import library.util.polynomial.PolynomialLong;

public class GenericTransferMatrixMethodTest {

	@Test
	public void testGenericVsLong() {
		int N = 3;
		long mod = 998244353;
		LongValueDigraph g = new LongValueDigraph(N);
		g.addEdge(0, 1, 1);
		g.addEdge(1, 2, 2);
		g.addEdge(2, 0, 3);
		g.addEdge(0, 0, 4);

		// Existing method
		FractionFieldElement<long[]> resFixed = TransferMatrixMethod.fixedWalkGeneratingFunction(g, 0, 2, mod);
		FractionFieldElement<long[]> resFree = TransferMatrixMethod.freeWalkGeneratingFunction(g, mod);
		FractionFieldElement<long[]> resClosed = TransferMatrixMethod.closedWalkGeneratingFunction(g, mod);

		// Generic method with Polynomials
		ValueDigraph<long[]> g2 = new ValueDigraph<>(N);
		ZnStrategy base = new ZnStrategy(mod);
		g2.addEdge(0, 1, new long[] {0, 1}); // 1x
		g2.addEdge(1, 2, new long[] {0, 2}); // 2x
		g2.addEdge(2, 0, new long[] {0, 3}); // 3x
		g2.addEdge(0, 0, new long[] {0, 4}); // 4x

		var strategy = PolynomialLong.Strategy(base);
		FractionFieldElement<long[]> gresFixed = TransferMatrixMethod.fixedWalkGeneratingFunction(g2, 0, 2, strategy);
		FractionFieldElement<long[]> gresFree = TransferMatrixMethod.freeWalkGeneratingFunction(g2, strategy);

		assertArrayEquals(resFixed.num(), gresFixed.num());
		assertArrayEquals(resFixed.den(), gresFixed.den());
		assertArrayEquals(resFree.num(), gresFree.num());
		assertArrayEquals(resFree.den(), gresFree.den());

		FractionFieldElement<long[]> resFixedStart = TransferMatrixMethod.fixedStartWalkGeneratingFunction(g, 0, mod);
		FractionFieldElement<long[]> gresFixedStart = TransferMatrixMethod.fixedStartWalkGeneratingFunction(g2, 0, strategy);
		assertArrayEquals(resFixedStart.num(), gresFixedStart.num());
		assertArrayEquals(resFixedStart.den(), gresFixedStart.den());
	}

	@Test
	public void testSimple() {
		int N = 2;
		long mod = 998244353;
		ZnStrategy strategy = new ZnStrategy(mod);
		ValueDigraph<Long> g = new ValueDigraph<>(N);
		// 0 -> 1 weight a
		// 1 -> 0 weight b
		long a = 2, b = 3;
		g.addEdge(0, 1, a);
		g.addEdge(1, 0, b);

		// I - A = [ 1  -a ]
		//         [ -b  1 ]
		// det = 1 - ab
		// adj = [ 1   a ]
		//       [ b   1 ]

		FractionFieldElement<Long> res00 = TransferMatrixMethod.fixedWalkGeneratingFunction(g, 0, 0, strategy);
		assertEquals(1L, res00.num());
		assertEquals((1 - a * b % mod + mod) % mod, res00.den());

		FractionFieldElement<Long> res01 = TransferMatrixMethod.fixedWalkGeneratingFunction(g, 0, 1, strategy);
		assertEquals(a, res01.num());
		assertEquals((1 - a * b % mod + mod) % mod, res01.den());

		FractionFieldElement<Long> resFree = TransferMatrixMethod.freeWalkGeneratingFunction(g, strategy);
		// 1^T adj 1 = 1 + a + b + 1 = 2 + a + b
		assertEquals((2 + a + b) % mod, resFree.num());
	}
}
