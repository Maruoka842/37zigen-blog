package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class HeldKarpTest {

    @Test
    public void testMstEdges() {
        long[][] d = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        HeldKarp.DenseDistanceMatrix dist = new HeldKarp.DenseDistanceMatrix(d);
        List<int[]> edges = HeldKarp.mstEdges(dist);

        assertEquals(3, edges.size());
        long totalWeight = 0;
        for (int[] e : edges) {
            totalWeight += d[e[0]][e[1]];
        }
        assertEquals(45, totalWeight);
    }

    @Test
    public void testMinimumOneTree() {
        long[][] d = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        HeldKarp.DenseDistanceMatrix dist = new HeldKarp.DenseDistanceMatrix(d);
        long[] pi = new long[4];
        HeldKarp.OneTreeResult res = HeldKarp.minimumOneTree(dist, pi);

        // MST is 45 (0-1, 0-2, 0-3)
        // Leaves are 1, 2, 3.
        // 1-tree for leaf 1: 45 + 25 (1-3) = 70
        // 1-tree for leaf 2: 45 + 30 (2-3) = 75
        // 1-tree for leaf 3: 45 + 25 (3-1) = 70
        // The implementation picks max among leaves: 75.
        assertEquals(75, res.weight());
    }

    @Test
    public void testHeldKarpLowerBound() {
        long[][] d = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        // Optimal TSP is 0-1-3-2-0: 10 + 25 + 30 + 15 = 80.
        // Held-Karp should give 80 because it's a small graph.
        HeldKarp.DenseDistanceMatrix dist = new HeldKarp.DenseDistanceMatrix(d);
        HeldKarp.HeldKarpResult res = HeldKarp.heldKarpLowerBound(dist);

        assertTrue(res.lowerBound() <= 80);
        // It should at least be better than the initial 1-tree weight.
        assertTrue(res.lowerBound() >= 75);

        // For a 4-node graph, Held-Karp is typically exact.
        assertEquals(80, res.lowerBound());
    }
}
