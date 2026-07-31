package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ImplicitDigraphTest {

    @Test
    public void testBfsDistances() {
        // Grid graph 3x3
        ImplicitDigraph<String> graph = new ImplicitDigraph<String>() {
            @Override
            public Iterable<String> nextStates(String v) {
                int r = v.charAt(0) - '0';
                int c = v.charAt(1) - '0';
                List<String> nexts = new java.util.ArrayList<>();
                int[][] moves = {{0, 1}, {1, 0}};
                for (int[] m : moves) {
                    int nr = r + m[0], nc = c + m[1];
                    if (nr < 3 && nc < 3) nexts.add("" + nr + nc);
                }
                return nexts;
            }
        };

        var dists = graph.bfsDistances("00");
        assertEquals(0, dists.get("00"));
        assertEquals(1, dists.get("01"));
        assertEquals(1, dists.get("10"));
        assertEquals(2, dists.get("11"));
        assertEquals(4, dists.get("22"));
        assertFalse(dists.containsKey("33"));
    }

    @Test
    public void testSccAndAcyclic() {
        // 0 -> 1 -> 2 -> 0, 2 -> 3
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1);
                if (v == 1) return Arrays.asList(2);
                if (v == 2) return Arrays.asList(0, 3);
                return Arrays.asList();
            }
        };

        assertFalse(graph.isAcyclic(0));

        List<List<Integer>> scc = graph.scc(0);
        assertEquals(2, scc.size());
        // Topo order: components containing (0,1,2) then (3)
        assertTrue(scc.get(0).containsAll(Arrays.asList(0, 1, 2)));
        assertTrue(scc.get(1).contains(3));
    }

    @Test
    public void testTopologicalOrder() {
        // 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1, 2);
                if (v == 1) return Arrays.asList(3);
                if (v == 2) return Arrays.asList(3);
                return Arrays.asList();
            }
        };

        assertTrue(graph.isAcyclic(0));
        List<Integer> order = graph.topologicalOrder(0);
        assertEquals(4, order.size());
        assertEquals(0, (int) order.get(0));
        assertEquals(3, (int) order.get(3));
    }

    @Test
    public void testFindMinCycle() {
        // 0 -> 1 -> 2 -> 0
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1);
                if (v == 1) return Arrays.asList(2);
                if (v == 2) return Arrays.asList(0);
                return Arrays.asList();
            }
        };

        List<Integer> cycle = graph.findMinCycle(0);
        assertNotNull(cycle);
        assertEquals(3, cycle.size());
        assertTrue(cycle.containsAll(Arrays.asList(0, 1, 2)));
    }

    @Test
    public void testDistAndOnPath() {
        // 0 -> 1 -> 2
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1);
                if (v == 1) return Arrays.asList(2);
                return Arrays.asList();
            }
        };

        // Reachable
        assertEquals(0, graph.dist(0, 0));
        assertTrue(graph.onPath(0, 0));
        assertEquals(1, graph.dist(0, 1));
        assertTrue(graph.onPath(0, 1));
        assertEquals(2, graph.dist(0, 2));
        assertTrue(graph.onPath(0, 2));

        // Unreachable
        assertEquals(-1, graph.dist(2, 0));
        assertFalse(graph.onPath(2, 0));
        assertEquals(-1, graph.dist(1, 0));
        assertFalse(graph.onPath(1, 0));
        assertEquals(-1, graph.dist(0, 3));
        assertFalse(graph.onPath(0, 3));
    }

    @Test
    public void testCountReachable() {
        // 0 -> 1 -> 2, 0 -> 3
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1, 3);
                if (v == 1) return Arrays.asList(2);
                return Arrays.asList();
            }
        };

        assertEquals(4, graph.countReachable(0));
        assertEquals(2, graph.countReachable(1));
        assertEquals(1, graph.countReachable(2));
        assertEquals(1, graph.countReachable(3));

        assertEquals(4, graph.countReachable(Arrays.asList(1, 3, 0)));
        assertEquals(3, graph.countReachable(Arrays.asList(1, 3)));
    }

    @Test
    public void testMaxMoves() {
        // 0 -> 1 -> 2, 0 -> 3
        ImplicitDigraph<Integer> graph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1, 3);
                if (v == 1) return Arrays.asList(2);
                return Arrays.asList();
            }
        };

        assertEquals(2, graph.maxMoves(0)); // 0 -> 1 -> 2
        assertEquals(1, graph.maxMoves(1)); // 1 -> 2
        assertEquals(0, graph.maxMoves(2));
        assertEquals(0, graph.maxMoves(3));

        // Cycle: 0 -> 1 -> 0
        ImplicitDigraph<Integer> cycleGraph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1);
                if (v == 1) return Arrays.asList(0);
                return Arrays.asList();
            }
        };
        assertEquals(-1, cycleGraph.maxMoves(0));

        // Self loop
        ImplicitDigraph<Integer> selfLoopGraph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                return Arrays.asList(v);
            }
        };
        assertEquals(-1, selfLoopGraph.maxMoves(0));
    }

    @Test
    public void testPath() {
        // Grid graph 3x3
        ImplicitDigraph<String> graph = new ImplicitDigraph<String>() {
            @Override
            public Iterable<String> nextStates(String v) {
                int r = v.charAt(0) - '0';
                int c = v.charAt(1) - '0';
                List<String> nexts = new java.util.ArrayList<>();
                int[][] moves = {{0, 1}, {1, 0}};
                for (int[] m : moves) {
                    int nr = r + m[0], nc = c + m[1];
                    if (nr < 3 && nc < 3) nexts.add("" + nr + nc);
                }
                return nexts;
            }
        };

        // Simple path
        List<String> path = graph.path("00", "22");
        assertNotNull(path);
        assertEquals(5, path.size());
        assertEquals("00", path.get(0));
        assertEquals("22", path.get(4));
        for (int i = 0; i < path.size() - 1; i++) {
            String curr = path.get(i);
            String next = path.get(i + 1);
            int r1 = curr.charAt(0) - '0', c1 = curr.charAt(1) - '0';
            int r2 = next.charAt(0) - '0', c2 = next.charAt(1) - '0';
            assertTrue((Math.abs(r1 - r2) == 1 && c1 == c2) || (r1 == r2 && Math.abs(c1 - c2) == 1));
        }

        // src == dst
        List<String> pathSame = graph.path("11", "11");
        assertNotNull(pathSame);
        assertEquals(1, pathSame.size());
        assertEquals("11", pathSame.get(0));

        // Unreachable
        List<String> pathUnreachable = graph.path("22", "00");
        assertNull(pathUnreachable);

        // Path in a graph with a cycle
        ImplicitDigraph<Integer> cycleGraph = new ImplicitDigraph<Integer>() {
            @Override
            public Iterable<Integer> nextStates(Integer v) {
                if (v == 0) return Arrays.asList(1);
                if (v == 1) return Arrays.asList(2, 0);
                if (v == 2) return Arrays.asList(3);
                return Arrays.asList();
            }
        };
        List<Integer> pathCycle = cycleGraph.path(0, 3);
        assertEquals(Arrays.asList(0, 1, 2, 3), pathCycle);
    }

    @Test
    public void testPathWithPredicate() {
        // Grid graph 3x3
        ImplicitDigraph<String> graph = new ImplicitDigraph<String>() {
            @Override
            public Iterable<String> nextStates(String v) {
                int r = v.charAt(0) - '0';
                int c = v.charAt(1) - '0';
                List<String> nexts = new java.util.ArrayList<>();
                int[][] moves = {{0, 1}, {1, 0}};
                for (int[] m : moves) {
                    int nr = r + m[0], nc = c + m[1];
                    if (nr < 3 && nc < 3) nexts.add("" + nr + nc);
                }
                return nexts;
            }
        };

        // Path to (2, 2) using predicate
        List<String> path = graph.path("00", v -> v.equals("22"));
        assertNotNull(path);
        assertEquals(5, path.size());
        assertEquals("00", path.get(0));
        assertEquals("22", path.get(4));

        // Path to any vertex with r+c == 2
        List<String> pathSum2 = graph.path("00", v -> {
            int r = v.charAt(0) - '0';
            int c = v.charAt(1) - '0';
            return r + c == 2;
        });
        assertNotNull(pathSum2);
        assertEquals(3, pathSum2.size());
        String last = pathSum2.get(pathSum2.size() - 1);
        int r = last.charAt(0) - '0';
        int c = last.charAt(1) - '0';
        assertEquals(2, r + c);

        // Unreachable
        List<String> pathUnreachable = graph.path("22", v -> v.equals("00"));
        assertNull(pathUnreachable);

        // Immediate match
        List<String> pathImmediate = graph.path("11", v -> v.equals("11"));
        assertNotNull(pathImmediate);
        assertEquals(1, pathImmediate.size());
        assertEquals("11", pathImmediate.get(0));
    }
}
