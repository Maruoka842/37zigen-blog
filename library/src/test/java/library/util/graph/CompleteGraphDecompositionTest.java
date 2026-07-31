package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class CompleteGraphDecompositionTest {

    @Test
    public void testHamiltonianPathDecomposition() {
        int N = 6;
        int K = N / 2;
        int[][] paths = CompleteGraphDecomposition.hamiltonianPathDecomposition(N);
        assertEquals(K, paths.length);

        Set<String> allEdges = new HashSet<>();
        for (int[] path : paths) {
            assertEquals(N, path.length);
            Set<Integer> visited = new HashSet<>();
            for (int i = 0; i < N; i++) {
                assertTrue(path[i] >= 0 && path[i] < N);
                visited.add(path[i]);
                if (i > 0) {
                    int u = path[i - 1];
                    int v = path[i];
                    String edge = Math.min(u, v) + "-" + Math.max(u, v);
                    assertFalse(allEdges.contains(edge), "Edge " + edge + " reused");
                    allEdges.add(edge);
                }
            }
            assertEquals(N, visited.size(), "Path did not visit all vertices exactly once");
        }
        assertEquals(K * (N - 1), allEdges.size());
    }

    @Test
    public void testHamiltonianCycleDecomposition() {
        int N = 5;
        int K = (N - 1) / 2;
        int[][] cycles = CompleteGraphDecomposition.hamiltonianCycleDecomposition(N);
        assertEquals(K, cycles.length);

        Set<String> allEdges = new HashSet<>();
        for (int[] cycle : cycles) {
            assertEquals(N, cycle.length);
            Set<Integer> visited = new HashSet<>();
            for (int i = 0; i < N; i++) {
                assertTrue(cycle[i] >= 0 && cycle[i] < N);
                visited.add(cycle[i]);
                int u = cycle[i];
                int v = cycle[(i + 1) % N];
                String edge = Math.min(u, v) + "-" + Math.max(u, v);
                assertFalse(allEdges.contains(edge), "Edge " + edge + " reused");
                allEdges.add(edge);
            }
            assertEquals(N, visited.size(), "Cycle did not visit all vertices exactly once");
        }
        assertEquals(K * N, allEdges.size());
    }

    @Test
    public void testPerfectMatchingDecomposition() {
        int N = 4;
        int[][][] matchings = CompleteGraphDecomposition.perfectMatchingDecomposition(N);
        assertEquals(N - 1, matchings.length);

        Set<String> allEdges = new HashSet<>();
        for (int[][] matching : matchings) {
            assertEquals(N / 2, matching.length);
            Set<Integer> matchedVertices = new HashSet<>();
            for (int[] edgeArr : matching) {
                int u = edgeArr[0];
                int v = edgeArr[1];
                assertTrue(u >= 0 && u < N);
                assertTrue(v >= 0 && v < N);
                assertNotEquals(u, v);
                matchedVertices.add(u);
                matchedVertices.add(v);

                String edge = Math.min(u, v) + "-" + Math.max(u, v);
                assertFalse(allEdges.contains(edge), "Edge " + edge + " reused");
                allEdges.add(edge);
            }
            assertEquals(N, matchedVertices.size(), "Matching is not perfect");
        }
        assertEquals(N * (N - 1) / 2, allEdges.size());
    }

    @Test
    public void testCyclesAndMatchingDecomposition() {
        int N = 6;
        CompleteGraphDecomposition.CyclesAndMatching res = CompleteGraphDecomposition.cyclesAndMatchingDecomposition(N);
        assertEquals(N / 2 - 1, res.cycles().length);
        assertEquals(N / 2, res.matching().length);

        Set<String> allEdges = new HashSet<>();
        for (int[] cycle : res.cycles()) {
            assertEquals(N, cycle.length);
            Set<Integer> visited = new HashSet<>();
            for (int i = 0; i < N; i++) {
                visited.add(cycle[i]);
                int u = cycle[i];
                int v = cycle[(i + 1) % N];
                String edge = Math.min(u, v) + "-" + Math.max(u, v);
                assertFalse(allEdges.contains(edge), "Edge " + edge + " reused");
                allEdges.add(edge);
            }
            assertEquals(N, visited.size());
        }

        Set<Integer> matchedVertices = new HashSet<>();
        for (int[] edgeArr : res.matching()) {
            int u = edgeArr[0];
            int v = edgeArr[1];
            matchedVertices.add(u);
            matchedVertices.add(v);
            String edge = Math.min(u, v) + "-" + Math.max(u, v);
            assertFalse(allEdges.contains(edge), "Edge " + edge + " reused in matching");
            allEdges.add(edge);
        }
        assertEquals(N, matchedVertices.size());
        assertEquals(N * (N - 1) / 2, allEdges.size());
    }
}
