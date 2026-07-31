package library.util.graph;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class UndirectedValuedAdjacencyMatrixTest {

	private long[][] createEmptyMatrix(int N) {
		long[][] A = new long[N][N];
		for (int i = 0; i < N; i++) {
			Arrays.fill(A[i], UndirectedValuedAdjacencyMatrix.INF);
			A[i][i] = 0;
		}
		return A;
	}

	private void addEdge(long[][] A, int u, int v, long cost) {
		A[u][v] = Math.min(A[u][v], cost);
		A[v][u] = Math.min(A[v][u], cost);
	}

	@Test
	public void testDijkstraCorrectness() {
		int N = 5;
		long[][] A = createEmptyMatrix(N);
		addEdge(A, 0, 1, 10);
		addEdge(A, 0, 4, 3);
		addEdge(A, 1, 2, 2);
		addEdge(A, 4, 1, 1);
		addEdge(A, 1, 3, 5);
		addEdge(A, 2, 3, 1);

		// Path to 1: 0 -> 4 -> 1 has cost 3 + 1 = 4 (better than direct 10)
		// Path to 2: 0 -> 4 -> 1 -> 2 has cost 4 + 2 = 6
		// Path to 3: 0 -> 4 -> 1 -> 2 -> 3 has cost 6 + 1 = 7 (better than 0 -> 4 -> 1 -> 3 which is 4 + 5 = 9)
		// Path to 4: 0 -> 4 has cost 3
		long[] dist = UndirectedValuedAdjacencyMatrix.dijkstra(A, 0);
		assertEquals(0, dist[0]);
		assertEquals(4, dist[1]);
		assertEquals(6, dist[2]);
		assertEquals(7, dist[3]);
		assertEquals(3, dist[4]);

		// Check disconnected node behavior
		long[][] A2 = createEmptyMatrix(3);
		addEdge(A2, 0, 1, 5);
		long[] dist2 = UndirectedValuedAdjacencyMatrix.dijkstra(A2, 0);
		assertEquals(0, dist2[0]);
		assertEquals(5, dist2[1]);
		assertEquals(UndirectedValuedAdjacencyMatrix.INF, dist2[2]);
	}

	@Test
	public void testMinWeightCycleCost() {
		// Case 1: Simple triangle graph
		int N1 = 3;
		long[][] A1 = createEmptyMatrix(N1);
		addEdge(A1, 0, 1, 2);
		addEdge(A1, 1, 2, 3);
		addEdge(A1, 2, 0, 4);

		// Any vertex in the triangle should find the cycle with cost 2 + 3 + 4 = 9
		assertEquals(9, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A1, 0));
		assertEquals(9, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A1, 1));
		assertEquals(9, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A1, 2));

		// Case 2: No cycles (tree or forest)
		int N2 = 4;
		long[][] A2 = createEmptyMatrix(N2);
		addEdge(A2, 0, 1, 2);
		addEdge(A2, 1, 2, 3);
		addEdge(A2, 2, 3, 4);
		assertEquals(UndirectedValuedAdjacencyMatrix.INF, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A2, 0));
		assertEquals(UndirectedValuedAdjacencyMatrix.INF, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A2, 1));

		// Case 3: Multiple cycles, finding minimum weight cycle involving r
		int N3 = 5;
		long[][] A3 = createEmptyMatrix(N3);
		// Cycle 1: 0 - 1 - 2 - 0 (cost: 10 + 10 + 10 = 30)
		addEdge(A3, 0, 1, 10);
		addEdge(A3, 1, 2, 10);
		addEdge(A3, 2, 0, 10);
		// Cycle 2: 0 - 3 - 4 - 0 (cost: 2 + 3 + 4 = 9)
		addEdge(A3, 0, 3, 2);
		addEdge(A3, 3, 4, 3);
		addEdge(A3, 4, 0, 4);

		// Min weight cycle at 0 should be 9 (Cycle 2 is cheaper)
		assertEquals(9, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A3, 0));
		// Min weight cycle at 1 should be 30 (only Cycle 1 contains 1)
		assertEquals(30, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A3, 1));
		// Min weight cycle at 3 should be 9
		assertEquals(9, UndirectedValuedAdjacencyMatrix.findMinWeightCycleCostAt(A3, 3));
	}
}
