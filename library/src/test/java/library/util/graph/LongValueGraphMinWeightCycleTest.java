package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LongValueGraphMinWeightCycleTest {

	private static final long INF = Long.MAX_VALUE / 3;

	@Test
	public void testSimpleTriangle() {
		// 3 vertices triangle: 0 - 1 - 2 - 0
		LongValueGraph g = new LongValueGraph(3);
		g.addEdge(0, 1, 2);
		g.addEdge(1, 2, 3);
		g.addEdge(2, 0, 4);

		assertEquals(9, g.findMinWeightCycleCostAt(0));
		assertEquals(9, g.findMinWeightCycleCostAt(1));
		assertEquals(9, g.findMinWeightCycleCostAt(2));
	}

	@Test
	public void testNoCycles() {
		// A simple path of 4 vertices: 0 - 1 - 2 - 3
		LongValueGraph g = new LongValueGraph(4);
		g.addEdge(0, 1, 2);
		g.addEdge(1, 2, 3);
		g.addEdge(2, 3, 4);

		assertEquals(INF, g.findMinWeightCycleCostAt(0));
		assertEquals(INF, g.findMinWeightCycleCostAt(1));
		assertEquals(INF, g.findMinWeightCycleCostAt(2));
		assertEquals(INF, g.findMinWeightCycleCostAt(3));
	}

	@Test
	public void testMultipleCycles() {
		// 5 vertices graph
		// Cycle 1: 0 - 1 - 2 - 0 (cost: 10 + 10 + 10 = 30)
		// Cycle 2: 0 - 3 - 4 - 0 (cost: 2 + 3 + 4 = 9)
		LongValueGraph g = new LongValueGraph(5);
		g.addEdge(0, 1, 10);
		g.addEdge(1, 2, 10);
		g.addEdge(2, 0, 10);
		g.addEdge(0, 3, 2);
		g.addEdge(3, 4, 3);
		g.addEdge(4, 0, 4);

		// Min weight cycle at 0 should be 9 (Cycle 2 is cheaper)
		assertEquals(9, g.findMinWeightCycleCostAt(0));
		// Min weight cycle at 1 should be 30 (only Cycle 1 contains 1)
		assertEquals(30, g.findMinWeightCycleCostAt(1));
		// Min weight cycle at 3 should be 9
		assertEquals(9, g.findMinWeightCycleCostAt(3));
	}

	@Test
	public void testDisconnectedGraph() {
		// Disconnected graph with 4 vertices
		// Connected component A: 0 - 1 (path, no cycle)
		// Connected component B: 2 - 3 (path, no cycle)
		LongValueGraph g = new LongValueGraph(4);
		g.addEdge(0, 1, 5);
		g.addEdge(2, 3, 10);

		assertEquals(INF, g.findMinWeightCycleCostAt(0));
		assertEquals(INF, g.findMinWeightCycleCostAt(1));
		assertEquals(INF, g.findMinWeightCycleCostAt(2));
		assertEquals(INF, g.findMinWeightCycleCostAt(3));
	}
}
