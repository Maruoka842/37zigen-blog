package library.test;

import library.util.graph.DAG;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortRangeConstraintsTest {

    @Test
    public void testSingleNode() {
        DAG g = new DAG(1);
        int[] L = {0};
        int[] R = {0};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNotNull(order);
        assertArrayEquals(new int[]{0}, order);
    }

    @Test
    public void testTwoNodesIndependent() {
        DAG g = new DAG(2);
        // Independent nodes, but L and R force a specific ordering
        // v0 must be at index 1, v1 must be at index 0
        int[] L = {1, 0};
        int[] R = {1, 0};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNotNull(order);
        // Topological order is: vertex 1 then vertex 0
        assertArrayEquals(new int[]{1, 0}, order);
    }

    @Test
    public void testTwoNodesPathSuccess() {
        DAG g = new DAG(2);
        g.addEdge(0, 1); // 0 -> 1 (topo order requires vertex 0 before vertex 1)
        int[] L = {0, 0};
        int[] R = {1, 1};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNotNull(order);
        // order must be exactly [0, 1]
        assertArrayEquals(new int[]{0, 1}, order);
    }

    @Test
    public void testTwoNodesPathFailureDueToConstraints() {
        DAG g = new DAG(2);
        g.addEdge(0, 1); // 0 -> 1 (topo order requires vertex 0 before vertex 1)
        // Constraint requires vertex 0 to be at index 1, and vertex 1 to be at index 0
        int[] L = {1, 0};
        int[] R = {1, 0};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNull(order);
    }

    @Test
    public void testCyclicGraph() {
        DAG g = new DAG(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0); // Cycle: 0 -> 1 -> 2 -> 0
        int[] L = {0, 0, 0};
        int[] R = {2, 2, 2};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNull(order);
    }

    @Test
    public void testInvalidBoundsAndNegativeValues() {
        DAG g = new DAG(2);
        // L[i] > R[i]
        int[] L1 = {1, 0};
        int[] R1 = {0, 1};
        assertNull(g.topologicalSortWithShedule(L1, R1));

        // Out of bound upper limit
        int[] L2 = {0, 0};
        int[] R2 = {2, 1}; // size is 2, so upper limit 2 is out of bounds
        assertNull(g.topologicalSortWithShedule(L2, R2));

        // Negative values
        int[] L3 = {-1, 0};
        int[] R3 = {1, 1};
        assertNull(g.topologicalSortWithShedule(L3, R3));
    }

    @Test
    public void testInstanceMethod() {
        DAG g = new DAG(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        int[] L = {0, 1, 2};
        int[] R = {0, 1, 2};
        int[] order = g.topologicalSortWithShedule(L, R);
        assertNotNull(order);
        assertArrayEquals(new int[]{0, 1, 2}, order);
    }

    @Test
    public void testPreconditionCheck() {
        DAG g = new DAG(2);
        int[] L = {0}; // invalid length
        int[] R = {1, 1};
        assertThrows(IllegalArgumentException.class, () -> {
            g.topologicalSortWithShedule(L, R);
        });
    }
}
