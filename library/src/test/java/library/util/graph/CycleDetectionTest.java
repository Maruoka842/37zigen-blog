package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class CycleDetectionTest {

    @Test
    public void testOddCycleK3() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        int[] cycle = g.findOddCycle();
        assertNotNull(cycle);
        assertTrue(cycle.length % 2 != 0);
        verifyCycle(g, cycle);
    }

    @Test
    public void testOddCycleBipartite() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 0); // C4 is bipartite
        int[] cycle = g.findOddCycle();
        assertNull(cycle);
    }

    @Test
    public void testEvenCycleC4() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 0);
        int[] cycle = g.findEvenCycle();
        assertNotNull(cycle);
        assertEquals(0, cycle.length % 2);
        verifyCycle(g, cycle);
    }

    @Test
    public void testEvenCycleK3() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        int[] cycle = g.findEvenCycle();
        assertNull(cycle, "K3 has no even cycle");
    }

    @Test
    public void testOddCycleSelfLoop() {
        Graph g = new Graph(1);
        g.addEdge(0, 0);
        int[] cycle = g.findOddCycle();
        assertNotNull(cycle);
        assertEquals(1, cycle.length);
        assertEquals(0, cycle[0]);
    }

    @Test
    public void testEvenCycleMultiEdge() {
        Graph g = new Graph(2);
        g.addEdge(0, 1);
        g.addEdge(0, 1);
        int[] cycle = g.findEvenCycle();
        assertNotNull(cycle);
        assertEquals(2, cycle.length);
        verifyCycle(g, cycle);
    }

    @Test
    public void testDisconnectedComponents() {
        Graph g = new Graph(6);
        // Component 1: path 0-1-2
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        // Component 2: K3 (3-4-5)
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 3);

        int[] oddCycle = g.findOddCycle();
        assertNotNull(oddCycle);
        verifyCycle(g, oddCycle);

        int[] evenCycle = g.findEvenCycle();
        assertNull(evenCycle);
    }

    @Test
    public void testComplexGraph() {
        // Graph with both odd and even cycles
        Graph g = new Graph(5);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0); // odd (0,1,2)
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2); // odd (2,3,4)
        // even cycle exists: (0,1,2,3,4) is NOT necessarily a cycle but (0,1,2,4,3,2) isn't either.
        // wait, 0-1-2-0 and 2-3-4-2. The union has an even cycle?
        // 0-1-2-3-4-2-0 is not a simple cycle.
        // If we have two odd cycles sharing a vertex, we don't necessarily have an even cycle.
        // Let's add an even cycle explicitly.
        g.addEdge(0, 3);
        g.addEdge(1, 4);

        int[] odd = g.findOddCycle();
        assertNotNull(odd);
        verifyCycle(g, odd);

        int[] even = g.findEvenCycle();
        assertNotNull(even);
        verifyCycle(g, even);
    }

    private void verifyCycle(Graph g, int[] cycle) {
        int k = cycle.length;
        assertTrue(k >= 1);
        for (int i = 0; i < k; i++) {
            int u = cycle[i];
            int v = cycle[(i + 1) % k];
            boolean found = false;
            for (int neighbor : g.adj[u]) {
                if (neighbor == v) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Edge (" + u + ", " + v + ") must exist in graph");
        }
        // Verify vertices are unique (for simple cycles)
        // Note: my BFS based reconstruction should produce simple cycles.
        Set<Integer> set = new HashSet<>();
        for (int v : cycle) {
            assertTrue(set.add(v), "Duplicate vertex " + v + " in cycle");
        }
    }
}
