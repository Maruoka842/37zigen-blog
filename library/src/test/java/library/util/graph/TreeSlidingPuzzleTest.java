package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import library.util.graph.tree.Tree;

public class TreeSlidingPuzzleTest {

    @Test
    public void testPathGraph() {
        int n = 3;
        Tree tree = new Tree(n);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.treeSlidingPuzzle(tree);

        int[] src = {0, 1, 2};
        // next states should be {1, 0, 2}
        List<int[]> nexts = new ArrayList<>();
        for (int[] next : graph.nextStates(src)) {
            nexts.add(next);
        }
        assertEquals(1, nexts.size());
        assertArrayEquals(new int[]{1, 0, 2}, nexts.get(0));

        // Test reachability via BFS
        assertEquals(3, graph.countReachable(src));
    }

    @Test
    public void testStarGraph() {
        int n = 4;
        Tree tree = new Tree(n);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.treeSlidingPuzzle(tree);

        int[] src = {0, 1, 2, 3};
        // Hole at center 0. next states: {1, 0, 2, 3}, {2, 1, 0, 3}, {3, 1, 2, 0}
        List<int[]> nexts = new ArrayList<>();
        for (int[] next : graph.nextStates(src)) {
            nexts.add(next);
        }
        assertEquals(3, nexts.size());

        assertEquals(4, graph.countReachable(src));
    }

    @Test
    public void testMultipleHoles() {
        int n = 4;
        Tree tree = new Tree(n);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.treeSlidingPuzzle(tree);

        int[] src = {0, 0, 1, 2};
        // Reachable states for non-path graph with 2 holes: all multiset permutations
        // but this is a path graph.
        // For path with 2 holes {0,0,1,2}, labels 1,2 relative order is fixed.
        // Combinations: (0,0,1,2), (0,1,0,2), (0,1,2,0), (1,0,0,2), (1,0,2,0), (1,2,0,0) -> 6
        assertEquals(6, graph.countReachable(src));
    }
}
