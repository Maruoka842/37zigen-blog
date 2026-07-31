package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import library.util.graph.Edge;
import library.util.graph.Graphs;
import library.util.graph.Digraph;
import library.util.graph.LongValueDigraph;
import org.junit.jupiter.api.Test;

public class MinimumDirectedSpanningTreeTest {

	@Test
	public void testSimpleMDST_FromRoot() {
		int V = 3;
		List<Edge> edges = new ArrayList<>();
		edges.add(new Edge(0, 1, 10));
		edges.add(new Edge(0, 2, 10));
		edges.add(new Edge(1, 2, 1));

		boolean[] result = Graphs.minimumDirectedSpanningTreeFromRoot(V, edges, 0);

		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < result.length; i++) {
			if (result[i]) {
				count++;
				totalWeight += edges.get(i).cost;
			}
		}

		assertEquals(2, count);
		assertEquals(11, totalWeight);
		assertTrue(result[0]); // 0 -> 1
		assertTrue(result[2]); // 1 -> 2
	}

	@Test
	public void testSimpleMDST_ToRoot() {
		int V = 3;
		List<Edge> edges = new ArrayList<>();
		edges.add(new Edge(1, 0, 10));
		edges.add(new Edge(2, 0, 10));
		edges.add(new Edge(2, 1, 1));

		// 2 -> 1 -> 0 should be weight 11
		boolean[] result = Graphs.minimumDirectedSpanningTreeToRoot(V, edges, 0);

		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < result.length; i++) {
			if (result[i]) {
				count++;
				totalWeight += edges.get(i).cost;
			}
		}

		assertEquals(2, count);
		assertEquals(11, totalWeight);
		assertTrue(result[0]); // 1 -> 0
		assertTrue(result[2]); // 2 -> 1
	}

	@Test
	public void testAnotherMDST() {
		int V = 4;
		List<Edge> edges = new ArrayList<>();
		edges.add(new Edge(0, 1, 1));
		edges.add(new Edge(0, 2, 10));
		edges.add(new Edge(1, 2, 1));
		edges.add(new Edge(2, 3, 1));
		edges.add(new Edge(0, 3, 10));

		boolean[] result = Graphs.minimumDirectedSpanningTreeFromRoot(V, edges, 0);

		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < result.length; i++) {
			if (result[i]) {
				count++;
				totalWeight += edges.get(i).cost;
			}
		}

		assertEquals(3, count);
		assertEquals(3, totalWeight);
		assertTrue(result[0]); // 0 -> 1
		assertTrue(result[2]); // 1 -> 2
		assertTrue(result[3]); // 2 -> 3
	}
}
