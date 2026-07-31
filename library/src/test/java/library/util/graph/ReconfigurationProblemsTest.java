package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import library.util.collections.OpenHashMap;

public class ReconfigurationProblemsTest {

    @Test
    public void testZerosOnesRewritingK2() {
        int n = 6;
        int k = 2;
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.zerosOnesRewriting(n, k);
        int[] start = {0, 0, 0, 0, 0, 0};
        int[] target = {1, 0, 1, 1, 0, 1};

        assertTrue(graph.onPath(start, target));
        assertEquals(6, graph.dist(start, target));

        OpenHashMap<int[], Integer> dists = graph.bfsDistances(start);
        for (var entry : dists.entrySet()) {
            assertEquals((int)entry.value, graph.dist(start, entry.key), "Dist from start to " + Arrays.toString(entry.key));
        }
    }

    @Test
    public void testZerosOnesRewritingK2Small() {
        int n = 4;
        int k = 2;
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.zerosOnesRewriting(n, k);
        int[] start = {1, 1, 0, 0};
        int[] target = {1, 0, 0, 1};
        assertEquals(2, graph.dist(start, target));
    }

    @Test
    public void testZerosOnesRewritingK1() {
        int n = 5;
        int k = 1;
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.zerosOnesRewriting(n, k);
        int[] start = {0, 1, 0, 1, 0};
        int[] target = {1, 1, 0, 0, 1};
        assertEquals(3, graph.dist(start, target));
    }

    @Test
    public void testAvoiding0112AdjacentSwapping() {
        ImplicitDigraph<int[]> graph = ReconfigurationProblems.avoiding0112AdjacentSwapping();

        // 1. Check basic properties with short array
        // We have elements: 0, 1, 1, 2
        // Forbidden: 0, 1, 1, 2 itself!
        int[] bad = {0, 1, 1, 2};
        // Let's check starting from a good permutation
        int[] src = {1, 0, 1, 2};
        assertTrue(graph.onPath(src, src));
        assertEquals(0, graph.dist(src, src));

        // Let's find some reachable and unreachable permutations using BFS
        OpenHashMap<int[], Integer> distances = graph.bfsDistances(src);

        // Assert that the forbidden array `bad` is indeed not in the reachable state space
        assertFalse(distances.containsKey(bad));

        // Let's check a state that is reachable vs unreachable
        // All reachable states should satisfy graph.onPath(src, state) -> true
        for (var entry : distances.entrySet()) {
            int[] state = entry.key;
            int expectedDist = entry.value;
            assertTrue(graph.onPath(src, state), "Should be reachable: " + Arrays.toString(state));
            assertEquals(expectedDist, graph.dist(src, state));
        }

        // Test with onPath for a case of type difference (e.g. {0, 1, 1, 2} avoiding state)
        // Let's construct a state that has the same elements but belongs to a different type
        // Let's find all permutations of {0, 1, 1, 2, 3} and check if reachability is correct.
        // Elements: 0, 1, 1, 2
        // Type 0 is: lastIndexOf(0, S) < indexOf(S, 2) [i.e., 2 is after 0] and has a block of 1s between 0 and 2 of size >= 2.
        // S = [0, 1, 1, 2], but that's forbidden. S = [0, 1, 1, 1, 2] is type 0.
        // Let's test onPath logic on {0, 1, 1, 1, 2}
        int[] srcType0 = {0, 1, 1, 1, 2}; // type 0
        int[] dstType1 = {2, 1, 1, 1, 0}; // type 1

        assertFalse(graph.onPath(srcType0, dstType1));

        // BFS distances from srcType0 shouldn't contain dstType1
        OpenHashMap<int[], Integer> distancesType0 = graph.bfsDistances(srcType0);
        assertFalse(distancesType0.containsKey(dstType1));

        // 2. Check validation errors
        int[] invalidState1 = {0, 1, 3, 2};
        int[] invalidState2 = {0, 1, -1, 2};

        assertThrows(IllegalArgumentException.class, () -> graph.nextStates(invalidState1));
        assertThrows(IllegalArgumentException.class, () -> graph.nextStates(invalidState2));

        assertThrows(IllegalArgumentException.class, () -> graph.onPath(invalidState1, src));
        assertThrows(IllegalArgumentException.class, () -> graph.onPath(src, invalidState2));
    }
}
