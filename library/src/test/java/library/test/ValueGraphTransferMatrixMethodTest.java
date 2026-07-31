package library.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import library.util.algebra.strategy.ZStrategy;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.ZnStrategy;
import library.util.graph.TransferMatrixMethod;
import library.util.graph.ValueDigraph;
import library.util.graph.ValueGraph;

public class ValueGraphTransferMatrixMethodTest {

	@Test
	public void testValueGraphVsValueDigraphFp() {
		int N = 3;
		long mod = 998244353;
		FpStrategy strategy = new FpStrategy(mod);

		// Undirected graph (ValueGraph)
		ValueGraph<Long> gUndirected = new ValueGraph<>(N);
		gUndirected.addEdge(0, 1, 2L);
		gUndirected.addEdge(1, 2, 3L);
		gUndirected.addEdge(2, 0, 4L);
		gUndirected.addEdge(0, 0, 5L); // self loop

		// Corresponding directed graph (ValueDigraph)
		ValueDigraph<Long> gDirected = new ValueDigraph<>(N);
		// Undirected edge 0-1 with weight 2
		gDirected.addEdge(0, 1, 2L);
		gDirected.addEdge(1, 0, 2L);
		// Undirected edge 1-2 with weight 3
		gDirected.addEdge(1, 2, 3L);
		gDirected.addEdge(2, 1, 3L);
		// Undirected edge 2-0 with weight 4
		gDirected.addEdge(2, 0, 4L);
		gDirected.addEdge(0, 2, 4L);
		// Self loop 0-0 with weight 5
		gDirected.addEdge(0, 0, 5L);

		// 1. fixedWalkGeneratingFunction
		for (int s = 0; s < N; s++) {
			for (int t = 0; t < N; t++) {
				FractionFieldElement<Long> resUndirected = TransferMatrixMethod.fixedWalkGeneratingFunction(gUndirected, s, t, strategy);
				FractionFieldElement<Long> resDirected = TransferMatrixMethod.fixedWalkGeneratingFunction(gDirected, s, t, strategy);
				assertEquals(resDirected.num(), resUndirected.num(), "Fixed walk numerator mismatch at s=" + s + ", t=" + t);
				assertEquals(resDirected.den(), resUndirected.den(), "Fixed walk denominator mismatch at s=" + s + ", t=" + t);
			}
		}

		// 2. fixedStartWalkGeneratingFunction
		for (int s = 0; s < N; s++) {
			FractionFieldElement<Long> resUndirected = TransferMatrixMethod.fixedStartWalkGeneratingFunction(gUndirected, s, strategy);
			FractionFieldElement<Long> resDirected = TransferMatrixMethod.fixedStartWalkGeneratingFunction(gDirected, s, strategy);
			assertEquals(resDirected.num(), resUndirected.num(), "Fixed start numerator mismatch at s=" + s);
			assertEquals(resDirected.den(), resUndirected.den(), "Fixed start denominator mismatch at s=" + s);
		}

		// 3. freeWalkGeneratingFunction
		FractionFieldElement<Long> resFreeUndirected = TransferMatrixMethod.freeWalkGeneratingFunction(gUndirected, strategy);
		FractionFieldElement<Long> resFreeDirected = TransferMatrixMethod.freeWalkGeneratingFunction(gDirected, strategy);
		assertEquals(resFreeDirected.num(), resFreeUndirected.num(), "Free walk numerator mismatch");
		assertEquals(resFreeDirected.den(), resFreeUndirected.den(), "Free walk denominator mismatch");
	}

	@Test
	public void testValueGraphVsValueDigraphZn() {
		// Use ZnStrategy which is CommutativeRingStrategy but NOT FieldStrategy
		int N = 3;
		long mod = 12345;
		ZnStrategy strategy = new ZnStrategy(mod);

		ValueGraph<Long> gUndirected = new ValueGraph<>(N);
		gUndirected.addEdge(0, 1, 2L);
		gUndirected.addEdge(1, 2, 3L);
		gUndirected.addEdge(2, 0, 4L);
		gUndirected.addEdge(0, 0, 5L);

		ValueDigraph<Long> gDirected = new ValueDigraph<>(N);
		gDirected.addEdge(0, 1, 2L);
		gDirected.addEdge(1, 0, 2L);
		gDirected.addEdge(1, 2, 3L);
		gDirected.addEdge(2, 1, 3L);
		gDirected.addEdge(2, 0, 4L);
		gDirected.addEdge(0, 2, 4L);
		gDirected.addEdge(0, 0, 5L);

		// 1. fixedWalkGeneratingFunction
		for (int s = 0; s < N; s++) {
			for (int t = 0; t < N; t++) {
				FractionFieldElement<Long> resUndirected = TransferMatrixMethod.fixedWalkGeneratingFunction(gUndirected, s, t, strategy);
				FractionFieldElement<Long> resDirected = TransferMatrixMethod.fixedWalkGeneratingFunction(gDirected, s, t, strategy);
				assertEquals(resDirected.num(), resUndirected.num());
				assertEquals(resDirected.den(), resUndirected.den());
			}
		}

		// 2. fixedStartWalkGeneratingFunction
		for (int s = 0; s < N; s++) {
			FractionFieldElement<Long> resUndirected = TransferMatrixMethod.fixedStartWalkGeneratingFunction(gUndirected, s, strategy);
			FractionFieldElement<Long> resDirected = TransferMatrixMethod.fixedStartWalkGeneratingFunction(gDirected, s, strategy);
			assertEquals(resDirected.num(), resUndirected.num());
			assertEquals(resDirected.den(), resUndirected.den());
		}

		// 3. freeWalkGeneratingFunction
		FractionFieldElement<Long> resFreeUndirected = TransferMatrixMethod.freeWalkGeneratingFunction(gUndirected, strategy);
		FractionFieldElement<Long> resFreeDirected = TransferMatrixMethod.freeWalkGeneratingFunction(gDirected, strategy);
		assertEquals(resFreeDirected.num(), resFreeUndirected.num());
		assertEquals(resFreeDirected.den(), resFreeUndirected.den());
	}

	private <T> FractionFieldElement<T> naiveDPPathSum(ValueDigraph<T> g, library.util.algebra.strategy.IntegralDomainStrategy<T> strategy) {
		int N = g.N;
		FractionFieldStrategy<T> fracStrategy = new FractionFieldStrategy<>(strategy);
		if (N <= 0) return fracStrategy.zero();

		// Calculate self loop sum
		@SuppressWarnings("unchecked")
		T[] selfLoopSum = (T[]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N);
		java.util.Arrays.fill(selfLoopSum, strategy.zero());
		for (int u = 0; u < N; u++) {
			for (library.util.graph.ValueEdge<T> e : g.adj[u]) {
				if (e.dst() == u) {
					selfLoopSum[u] = strategy.add(selfLoopSum[u], e.weight());
				}
			}
		}

		// F_x = (1 - selfLoopSum[x])^-1 in the fraction field
		@SuppressWarnings("unchecked")
		FractionFieldElement<T>[] F = (FractionFieldElement<T>[]) java.lang.reflect.Array.newInstance(FractionFieldElement.class, N);
		for (int x = 0; x < N; x++) {
			FractionFieldElement<T> oneMinusW = fracStrategy.of(strategy.sub(strategy.one(), selfLoopSum[x]), strategy.one());
			F[x] = fracStrategy.inv(oneMinusW);
		}

		if (N == 1) return F[0];

		// DAG DP starting with DP[0] = 1 (unit start)
		@SuppressWarnings("unchecked")
		FractionFieldElement<T>[] dp = (FractionFieldElement<T>[]) java.lang.reflect.Array.newInstance(FractionFieldElement.class, N);
		java.util.Arrays.fill(dp, fracStrategy.zero());
		dp[0] = fracStrategy.one();

		for (int u = 0; u < N; u++) {
			if (fracStrategy.equals(dp[u], fracStrategy.zero())) continue;
			for (library.util.graph.ValueEdge<T> e : g.adj[u]) {
				int v = e.dst();
				if (v == u || v == 0) continue; // ignore self loop and back-edge
				FractionFieldElement<T> trans = fracStrategy.of(e.weight(), strategy.one());
				FractionFieldElement<T> term = fracStrategy.mul(fracStrategy.mul(dp[u], trans), F[v]);
				dp[v] = fracStrategy.add(dp[v], term);
			}
		}

		// Calculate C_unit_start = sum_{u=1}^{N-1} dp[u] * w(u -> 0)
		FractionFieldElement<T> C_unit_start = fracStrategy.zero();
		for (int u = 1; u < N; u++) {
			T backWeight = strategy.zero();
			for (library.util.graph.ValueEdge<T> e : g.adj[u]) {
				if (e.dst() == 0) {
					backWeight = strategy.add(backWeight, e.weight());
				}
			}
			if (!strategy.equals(backWeight, strategy.zero())) {
				FractionFieldElement<T> wFrac = fracStrategy.of(backWeight, strategy.one());
				C_unit_start = fracStrategy.add(C_unit_start, fracStrategy.mul(dp[u], wFrac));
			}
		}

		// C_loop = F[0] * C_unit_start
		FractionFieldElement<T> C_loop = fracStrategy.mul(F[0], C_unit_start);
		FractionFieldElement<T> oneMinusCLoop = fracStrategy.sub(fracStrategy.one(), C_loop);

		// DP_actual[0] = F[0] / (1 - C_loop)
		FractionFieldElement<T> DP_actual_0 = fracStrategy.mul(F[0], fracStrategy.inv(oneMinusCLoop));

		// DP_actual[N-1] = DP_actual[0] * dp[N-1]
		return fracStrategy.mul(DP_actual_0, dp[N - 1]);
	}

	@Test
	public void testPathWeightSumSequentialBasic() {
		ZStrategy strategy = new ZStrategy();
		FractionFieldStrategy<Long> fracStrategy = new FractionFieldStrategy<>(strategy);

		// N = 1, no self loop
		ValueDigraph<Long> g1 = new ValueDigraph<>(1);
		assertEquals(fracStrategy.one(), TransferMatrixMethod.pathWeightSumSequential(g1, strategy));

		// N = 1, with self loop of weight 2
		ValueDigraph<Long> g1_self = new ValueDigraph<>(1);
		g1_self.addEdge(0, 0, 2L);
		// expected: 1 / (1 - 2) = -1
		assertEquals(fracStrategy.of(-1L, 1L), TransferMatrixMethod.pathWeightSumSequential(g1_self, strategy));

		// N = 3, simple path
		ValueDigraph<Long> g2 = new ValueDigraph<>(3);
		g2.addEdge(0, 1, 2L);
		g2.addEdge(1, 2, 3L);
		assertEquals(fracStrategy.of(6L, 1L), TransferMatrixMethod.pathWeightSumSequential(g2, strategy));

		// N = 3, with self loops
		ValueDigraph<Long> g2_self = new ValueDigraph<>(3);
		g2_self.addEdge(0, 0, 2L);
		g2_self.addEdge(0, 1, 3L);
		g2_self.addEdge(1, 1, 4L);
		g2_self.addEdge(1, 2, 5L);
		// F0 = 1 / (1 - 2) = -1
		// F1 = 1 / (1 - 4) = -1/3
		// expected: F0 * 3 * F1 * 5 = (-1) * 3 * (-1/3) * 5 = 5
		assertEquals(fracStrategy.of(5L, 1L), TransferMatrixMethod.pathWeightSumSequential(g2_self, strategy));

		// N = 2, with back-edge to 0
		ValueDigraph<Long> g3 = new ValueDigraph<>(2);
		g3.addEdge(0, 1, 2L);
		g3.addEdge(1, 0, 3L);
		// F0 = 1, F1 = 1
		// C_loop = 1 * 2 * 1 * 3 = 6
		// DP_actual[0] = 1 / (1 - 6) = -1/5
		// DP_actual[1] = (-1/5) * 2 = -2/5
		assertEquals(fracStrategy.of(-2L, 5L), TransferMatrixMethod.pathWeightSumSequential(g3, strategy));
	}

	@Test
	public void testPathWeightSumSequentialRandomized() {
		ZStrategy strategy = new ZStrategy();
		FractionFieldStrategy<Long> fracStrategy = new FractionFieldStrategy<>(strategy);
		java.util.Random rng = new java.util.Random(42);

		for (int t = 0; t < 100; t++) {
			// Keep N, K, and weights small to prevent 64-bit long overflow in FractionField calculations
			int N = rng.nextInt(4) + 2; // N in [2, 5]
			int K = rng.nextInt(2) + 2; // K in [2, 3]
			ValueDigraph<Long> g = new ValueDigraph<>(N);

			// Add random self-loops with weight that won't make 1 - w = 0 (e.g., 2)
			for (int i = 0; i < N; i++) {
				if (rng.nextDouble() < 0.3) {
					g.addEdge(i, i, 2L);
				}
			}

			// Add random back-edges to 0
			for (int i = 1; i < N; i++) {
				if (rng.nextDouble() < 0.3) {
					g.addEdge(i, 0, 2L);
				}
			}

			// Add random edges with step size <= K
			for (int u = 0; u < N; u++) {
				for (int step = 1; step <= K; step++) {
					int v = u + step;
					if (v < N) {
						if (rng.nextDouble() < 0.5) {
							long weight = rng.nextInt(2) + 1; // weight in [1, 2]
							g.addEdge(u, v, weight);
						}
					}
				}
			}

			FractionFieldElement<Long> expected = naiveDPPathSum(g, strategy);
			FractionFieldElement<Long> actual = TransferMatrixMethod.pathWeightSumSequential(g, strategy);
			assertTrue(fracStrategy.equals(expected, actual), "Failed on test " + t + " with N=" + N + ", K=" + K + "\nExpected: " + expected + "\nActual: " + actual);
		}
	}
}
