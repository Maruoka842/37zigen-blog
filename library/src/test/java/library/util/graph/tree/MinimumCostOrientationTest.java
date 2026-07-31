package library.util.graph.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinimumCostOrientationTest {
    @Test
    public void testSingleNode() {
        Tree tree = new Tree(1);
        long[][] costs = {
            {5L}
        };
        assertEquals(5L, TreedpFactory.minimumCostOrientation(tree, costs));
    }

    @Test
    public void testTwoNodes() {
        Tree tree = new Tree(2);
        tree.addEdge(0, 1);
        // Orientation 0 -> 1: in(0)=0, in(1)=1. Cost = f(0,0) + f(1,1) = 10 + 20 = 30
        // Orientation 1 -> 0: in(0)=1, in(1)=0. Cost = f(0,1) + f(1,0) = 2 + 3 = 5
        long[][] costs = {
            {10L, 2L}, // f(0,0), f(0,1)
            {3L, 20L}  // f(1,0), f(1,1)
        };
        assertEquals(5L, TreedpFactory.minimumCostOrientation(tree, costs));
    }

    @Test
    public void testStarGraph() {
        int N = 4;
        Tree tree = new Tree(N);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);

        // f(0, k) = (k-1)^2
        // f(i, 0) = 0, f(i, 1) = 10 for i > 0
        long[][] costs = new long[N][];
        costs[0] = new long[]{1L, 0L, 1L, 4L}; // k=0, 1, 2, 3
        for (int i = 1; i < N; i++) {
            costs[i] = new long[]{0L, 10L};
        }

        // Orientations:
        // 0 -> {1,2,3}: in(0)=0, in(1..3)=1. Cost = 1 + 3*10 = 31
        // {1} -> 0, 0 -> {2,3}: in(0)=1, in(1)=0, in(2,3)=1. Cost = 0 + 0 + 2*10 = 20
        // {1,2} -> 0, 0 -> {3}: in(0)=2, in(1,2)=0, in(3)=1. Cost = 1 + 0 + 10 = 11
        // {1,2,3} -> 0: in(0)=3, in(1,2,3)=0. Cost = 4 + 0 = 4

        assertEquals(4L, TreedpFactory.minimumCostOrientation(tree, costs));
    }

    @Test
    public void testPathGraph() {
        int N = 3;
        Tree tree = new Tree(N);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);

        // 0-1-2
        // possible orientations:
        // 0->1, 1->2: in(0)=0, in(1)=1, in(2)=1
        // 0->1, 2->1: in(0)=0, in(1)=2, in(2)=0
        // 1->0, 1->2: in(0)=1, in(1)=0, in(2)=1
        // 1->0, 2->1: in(0)=1, in(1)=1, in(2)=0

        long[][] costs = {
            {10L, 10L}, // 0
            {10L, 10L, 0L}, // 1: favors in-degree 2
            {10L, 10L}  // 2
        };

        // Costs:
        // 0->1, 1->2: 10 + 10 + 10 = 30
        // 0->1, 2->1: 10 + 0 + 10 = 20
        // 1->0, 1->2: 10 + 10 + 10 = 30
        // 1->0, 2->1: 10 + 10 + 10 = 30

        assertEquals(20L, TreedpFactory.minimumCostOrientation(tree, costs));
    }
}
