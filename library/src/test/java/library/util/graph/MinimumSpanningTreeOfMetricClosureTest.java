package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.tree.LongValueForest;
import library.util.unionfind.UnionFind;

public class MinimumSpanningTreeOfMetricClosureTest {

	@Test
	public void testBasicCase() {
		// 0 --(2)-- 1 --(3)-- 2 --(1)-- 3
		// S = {0, 2, 3}
		// H on S should have edges:
		// (0, 2) with weight 5
		// (2, 3) with weight 1
		// (0, 3) with weight 6
		// MST of H should have edges (2, 3) weight 1 and (0, 2) weight 5. Total cost = 6.
		LongValueGraph g = new LongValueGraph(4);
		g.addEdge(0, 1, 2);
		g.addEdge(1, 2, 3);
		g.addEdge(2, 3, 1);

		int[] S = {0, 2, 3};
		LongValueForest mst = Graphs.minimumSpanningTreeOfMetricClosure(g, S);

		assertNotNull(mst);
		assertEquals(4, mst.N);
		long edgeCost = mst.edgeCost();
		assertEquals(6, edgeCost);

		// Check the edges in MST
		List<Edge> edges = mst.edges();
		assertEquals(2, edges.size());
	}

	@Test
	public void testEmptyAndSingleTerminals() {
		LongValueGraph g = new LongValueGraph(5);
		g.addEdge(0, 1, 10);
		g.addEdge(1, 2, 20);

		// S empty
		LongValueForest mstEmpty = Graphs.minimumSpanningTreeOfMetricClosure(g, new int[]{});
		assertNotNull(mstEmpty);
		assertEquals(0, mstEmpty.edgeCost());
		assertEquals(0, mstEmpty.edges().size());

		// S size 1
		LongValueForest mstOne = Graphs.minimumSpanningTreeOfMetricClosure(g, new int[]{1});
		assertNotNull(mstOne);
		assertEquals(0, mstOne.edgeCost());
		assertEquals(0, mstOne.edges().size());
	}

	@Test
	public void testDuplicatesInTerminals() {
		LongValueGraph g = new LongValueGraph(3);
		g.addEdge(0, 1, 5);
		g.addEdge(1, 2, 5);

		int[] S = {0, 2, 0, 2}; // duplicates
		LongValueForest mst = Graphs.minimumSpanningTreeOfMetricClosure(g, S);
		assertNotNull(mst);
		assertEquals(10, mst.edgeCost());
		assertEquals(1, mst.edges().size()); // Only 1 edge should connect unique terminals 0 and 2
	}

	@Test
	public void testDisconnectedGraph() {
		// Component 1: 0 --(5)-- 1, S contains 0, 1
		// Component 2: 2, S contains 2
		// H is disconnected, MST of H is a forest of MSTs of components.
		// Edge (0, 1) cost 5 should be present. Terminal 2 has no edges.
		LongValueGraph g = new LongValueGraph(3);
		g.addEdge(0, 1, 5);

		int[] S = {0, 1, 2};
		LongValueForest mst = Graphs.minimumSpanningTreeOfMetricClosure(g, S);
		assertNotNull(mst);
		assertEquals(5, mst.edgeCost());
		assertEquals(1, mst.edges().size());
	}

	@Test
	public void testRandomStress() {
		Random rng = new Random(42);
		for (int tc = 0; tc < 500; tc++) {
			int n = rng.nextInt(30) + 2; // 2 to 31 vertices
			int m = rng.nextInt(50) + 1; // 1 to 50 edges
			LongValueGraph g = new LongValueGraph(n);
			for (int i = 0; i < m; i++) {
				int u = rng.nextInt(n);
				int v = rng.nextInt(n);
				if (u != v) {
					long cost = rng.nextInt(100) + 1; // weights in [1, 100]
					g.addEdge(u, v, cost);
				}
			}

			// Generate random terminals
			int k = rng.nextInt(n) + 1;
			int[] S = new int[k];
			for (int i = 0; i < k; i++) {
				S[i] = rng.nextInt(n);
			}

			LongValueForest mst = Graphs.minimumSpanningTreeOfMetricClosure(g, S);
			long expectedCost = naiveMetricClosureMSTCost(g, S);
			assertEquals(expectedCost, mst.edgeCost(), "Failed on stress testcase #" + tc);
		}
	}

	@Test
	public void testLargeScaleRandomStress() {
		Random rng = new Random(998244353);
		// Run a slightly larger scale random stress test
		for (int tc = 0; tc < 50; tc++) {
			int n = rng.nextInt(100) + 50; // 50 to 149 vertices
			int m = rng.nextInt(300) + 100; // 100 to 399 edges
			LongValueGraph g = new LongValueGraph(n);
			for (int i = 0; i < m; i++) {
				int u = rng.nextInt(n);
				int v = rng.nextInt(n);
				if (u != v) {
					long cost = rng.nextInt(1000) + 1;
					g.addEdge(u, v, cost);
				}
			}

			int k = rng.nextInt(n / 2) + 5;
			int[] S = new int[k];
			for (int i = 0; i < k; i++) {
				S[i] = rng.nextInt(n);
			}

			LongValueForest mst = Graphs.minimumSpanningTreeOfMetricClosure(g, S);
			long expectedCost = naiveMetricClosureMSTCost(g, S);
			assertEquals(expectedCost, mst.edgeCost(), "Failed on large-scale stress testcase #" + tc);
		}
	}

	private static long naiveMetricClosureMSTCost(LongValueGraph g, int[] S) {
		int n = g.N;
		long[][] d = g.warshalFloyd();
		long INF = Long.MAX_VALUE / 3;

		// Deduplicate S
		boolean[] isTerminal = new boolean[n];
		List<Integer> termList = new ArrayList<>();
		for (int s : S) {
			if (!isTerminal[s]) {
				isTerminal[s] = true;
				termList.add(s);
			}
		}
		int k = termList.size();
		if (k <= 1) return 0;

		// Build complete graph H on S
		List<Edge> hEdges = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			for (int j = i + 1; j < k; j++) {
				int u = termList.get(i);
				int v = termList.get(j);
				if (d[u][v] < INF) {
					hEdges.add(new Edge(u, v, d[u][v]));
				}
			}
		}

		Collections.sort(hEdges);
		UnionFind uf = new UnionFind(n);
		long totalCost = 0;
		for (Edge e : hEdges) {
			if (!uf.equiv(e.src, e.dst)) {
				uf.union(e.src, e.dst);
				totalCost += e.cost;
			}
		}
		return totalCost;
	}
}
