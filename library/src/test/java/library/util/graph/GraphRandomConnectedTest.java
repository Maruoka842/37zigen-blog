package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import library.util.Ints;

public class GraphRandomConnectedTest {

    @Test
    public void testSmallGraph() {
        int N = 5;
        int M = 6;
        Graph g = Graph.randomConnectedGraph(N, M);
        assertEquals(N, g.N);
        assertEquals(M, g.M);
        assertTrue(g.isConnected());
        verifySimple(g);
    }

    @Test
    public void testMinimumEdges() {
        int N = 10;
        int M = N - 1;
        Graph g = Graph.randomConnectedGraph(N, M);
        assertEquals(N, g.N);
        assertEquals(M, g.M);
        assertTrue(g.isConnected());
        verifySimple(g);
    }

    @Test
    public void testMaximumEdges() {
        int N = 5;
        int M = N * (N - 1) / 2;
        Graph g = Graph.randomConnectedGraph(N, M);
        assertEquals(N, g.N);
        assertEquals(M, g.M);
        assertTrue(g.isConnected());
        verifySimple(g);
    }

    @Test
    public void testSingleNode() {
        int N = 1;
        int M = 0;
        Graph g = Graph.randomConnectedGraph(N, M);
        assertEquals(N, g.N);
        assertEquals(M, g.M);
        assertTrue(g.isConnected());
        verifySimple(g);
    }

    @Test
    public void testEmptyGraph() {
        int N = 0;
        int M = 0;
        Graph g = Graph.randomConnectedGraph(N, M);
        assertEquals(0, g.N);
        assertEquals(0, g.M);
    }

    @Test
    public void testAssertionError() {
        // M too small
        assertThrows(AssertionError.class, () -> Graph.randomConnectedGraph(5, 3));
        // M too large
        assertThrows(AssertionError.class, () -> Graph.randomConnectedGraph(5, 11));
    }

    private void verifySimple(Graph g) {
        Set<Long> edges = new HashSet<>();
        for (int u = 0; u < g.N; u++) {
            for (int i = 0; i < g.adj[u].size(); i++) {
                int v = g.adj[u].get(i);
                assertNotEquals(u, v, "Self-loop detected");
                long packed = Ints.packUnorderedPair(u, v);
                edges.add(packed);
            }
        }
        assertEquals(g.M, edges.size(), "Duplicate edges detected or edge count mismatch");
    }
}
