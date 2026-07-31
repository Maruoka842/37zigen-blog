package library.test;

import library.util.graph.Graph;
import library.util.graph.Graphs;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CountTreesForSubsetsTest {

    @Test
    public void testEmptyAndSingleNode() {
        Graph g0 = new Graph(0);
        long[] res0 = Graphs.countSpanningTreeForSubsets(g0, 998244353L);
        assertArrayEquals(new long[]{1}, res0);

        Graph g1 = new Graph(1);
        long[] res1 = Graphs.countSpanningTreeForSubsets(g1, 998244353L);
        assertArrayEquals(new long[]{1, 1}, res1);
    }

    @Test
    public void testPathGraph() {
        // Path graph: 0 - 1 - 2
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);

        long mod = 998244353L;
        long[] res = Graphs.countSpanningTreeForSubsets(g, mod);

        // Expected trees:
        // Size 0:
        // S = {} (000) -> 1
        // Size 1:
        // S = {0} (001) -> 1
        // S = {1} (010) -> 1
        // S = {2} (100) -> 1
        // Size 2:
        // S = {0,1} (011) -> 1 (edge 0-1)
        // S = {1,2} (110) -> 1 (edge 1-2)
        // S = {0,2} (101) -> 0 (no edge)
        // Size 3:
        // S = {0,1,2} (111) -> 1 (spanning tree of path)

        assertEquals(1, res[0]);
        assertEquals(1, res[1]); // {0}
        assertEquals(1, res[2]); // {1}
        assertEquals(1, res[3]); // {0,1}
        assertEquals(1, res[4]); // {2}
        assertEquals(0, res[5]); // {0,2}
        assertEquals(1, res[6]); // {1,2}
        assertEquals(1, res[7]); // {0,1,2}
    }

    @Test
    public void testCycleGraph() {
        // Cycle graph: 0 - 1 - 2 - 0
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);

        long mod = 998244353L;
        long[] res = Graphs.countSpanningTreeForSubsets(g, mod);

        // Expected trees for {0,1,2}: 3 (we can remove any of the 3 edges to get a spanning tree)
        assertEquals(3, res[7]);
        // S = {0,1} (3) -> 1
        // S = {1,2} (6) -> 1
        // S = {0,2} (5) -> 1
        assertEquals(1, res[3]);
        assertEquals(1, res[5]);
        assertEquals(1, res[6]);
    }

    @Test
    public void testCompleteGraph() {
        // K_4 (Cayley's formula: N^(N-2) = 4^2 = 16 spanning trees for complete graph of 4 vertices)
        int N = 4;
        Graph g = new Graph(N);
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                g.addEdge(i, j);
            }
        }

        long mod = 998244353L;
        long[] res = Graphs.countSpanningTreeForSubsets(g, mod);

        // For N=4, res[15] (all 4 vertices) should be 16
        assertEquals(16, res[15]);

        // For subsets of size 3, Cayley's says 3^(3-2) = 3 trees
        // e.g., S = {0,1,2} (7), {0,1,3} (11), {0,2,3} (13), {1,2,3} (14)
        assertEquals(3, res[7]);
        assertEquals(3, res[11]);
        assertEquals(3, res[13]);
        assertEquals(3, res[14]);
    }

    @Test
    public void testDisconnectedGraph() {
        // Disconnected: 0-1, 2-3
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(2, 3);

        long mod = 998244353L;
        long[] res = Graphs.countSpanningTreeForSubsets(g, mod);

        // S = {0,1,2,3} (15) -> 0 (disconnected, no spanning tree)
        assertEquals(0, res[15]);
        // S = {0,1,2} (7) -> 0
        assertEquals(0, res[7]);
        // S = {0,1} (3) -> 1
        assertEquals(1, res[3]);
        // S = {2,3} (12) -> 1
        assertEquals(1, res[12]);
    }
}
