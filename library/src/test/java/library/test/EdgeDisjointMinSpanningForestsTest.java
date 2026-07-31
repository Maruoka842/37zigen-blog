package library.test;

import library.util.Itertools;
import library.util.graph.Edge;
import library.util.graph.Graphs;
import library.util.graph.tree.LongValueForest;
import library.util.graph.LongValueGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeDisjointMinSpanningForestsTest {

    @Test
    void testTriangle() {
        int N = 3;
        LongValueGraph g = new LongValueGraph(N);
        g.addEdge(0, 1, 10);
        g.addEdge(1, 2, 20);
        g.addEdge(2, 0, 30);
        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        assertEquals(2, forests.length);

        verifyForests(N, forests, 3);

        // Min property: total weight should be 10 + 20 + 30 = 60
        long totalWeight = forests[0].edgeCost() + forests[1].edgeCost();
        assertEquals(60, totalWeight);
    }

    @Test
    void testK4() {
        int N = 4;
        LongValueGraph g = new LongValueGraph(N);
        // K4 has 6 edges. 2 spanning trees have 3+3=6 edges.
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 2);
        g.addEdge(0, 3, 3);
        g.addEdge(1, 2, 4);
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 6);

        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        verifyForests(N, forests, 6);

        long totalWeight = forests[0].edgeCost() + forests[1].edgeCost();
        assertEquals(21, totalWeight);
    }

    @Test
    void testWeights() {
        int N = 3;
        LongValueGraph g = new LongValueGraph(N);
        // Triangle with a very heavy edge
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(0, 2, 100);

        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        verifyForests(N, forests, 3);

        long totalWeight = forests[0].edgeCost() + forests[1].edgeCost();
        assertEquals(102, totalWeight);
    }

    @Test
    void testDisconnected() {
        int N = 6;
        LongValueGraph g = new LongValueGraph(N);
        // Two triangles
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1); g.addEdge(2, 0, 1);
        g.addEdge(3, 4, 1); g.addEdge(4, 5, 1); g.addEdge(5, 3, 1);

        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        verifyForests(N, forests, 6);
    }

    @Test
    void testMultipleEdges() {
        int N = 2;
        LongValueGraph g = new LongValueGraph(N);
        g.addEdge(0, 1, 10);
        g.addEdge(0, 1, 20);
        g.addEdge(0, 1, 5);

        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        // Max edges in 2 forests for 2 nodes is 1 + 1 = 2.
        // It should pick 5 and 10.
        verifyForests(N, forests, 2);
        long totalWeight = forests[0].edgeCost() + forests[1].edgeCost();
        assertEquals(15, totalWeight);
    }

    @Test
    void testSelfLoop() {
        int N = 2;
        LongValueGraph g = new LongValueGraph(N);
        g.addEdge(0, 0, 1);
        g.addEdge(0, 1, 10);
        g.addEdge(1, 1, 5);

        LongValueForest[] forests = Graphs.edgeDisjointMinSpanningForests(g);
        // Self loops should be ignored.
        verifyForests(N, forests, 1);
        assertEquals(10, forests[0].edgeCost() + forests[1].edgeCost());
    }

    @Test
    void testStress() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = rnd.nextInt(5) + 13; 
            int m = rnd.nextInt(9) + 8; 
            LongValueGraph g = new LongValueGraph(n);
            for (int i = 0; i < m; i++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                if (u == v) { i--; continue; }
                g.addEdge(u, v, rnd.nextInt(100) + 1);
            }

            LongValueForest[] res = Graphs.edgeDisjointMinSpanningForests(g);
            long actualWeight = res[0].edgeCost() + res[1].edgeCost();
            int actualEdges = res[0].edges().size() + res[1].edges().size();

            // Brute force
            List<Edge> edges = g.edges();
            int M = edges.size();
            long minWeight = Long.MAX_VALUE;
            int maxEdges = 0;

            for (int k = 0; k <= Math.min(M, n - 1); k++) {
                for (int[] combo : Itertools.combinations(M, k)) {
                    long w1 = 0;
                    if (hasCycleInCombo(n, edges, combo)) continue;
                    for (int idx : combo) w1 += edges.get(idx).cost;

                    // Remaining edges MSF
                    LongValueGraph remaining = new LongValueGraph(n);
                    boolean[] used = new boolean[M];
                    for (int idx : combo) used[idx] = true;
                    for (int i = 0; i < M; i++) {
                        if (!used[i]) {
                            Edge e = edges.get(i);
                            remaining.addEdge(e.src, e.dst, e.cost);
                        }
                    }

                    LongValueForest f2 = Graphs.minimumSpanningForest(remaining);
                    long w2 = f2.edgeCost();
                    int totalEdges = k + f2.edges().size();
                    long totalWeight = w1 + w2;

                    if (totalEdges > maxEdges) {
                        maxEdges = totalEdges;
                        minWeight = totalWeight;
                    } else if (totalEdges == maxEdges) {
                        minWeight = Math.min(minWeight, totalWeight);
                    }
                }
            }
            assertEquals(maxEdges, actualEdges, "Mismatch in edge count at test " + t);
            assertEquals(minWeight, actualWeight, "Mismatch in total weight at test " + t);
        }
    }

    private boolean hasCycleInCombo(int n, List<Edge> edges, int[] combo) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        for (int idx : combo) {
            Edge e = edges.get(idx);
            if (!union(e.src, e.dst, parent)) return true;
        }
        return false;
    }

    private int find(int i, int[] parent) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent[i], parent);
    }

    private boolean union(int i, int j, int[] parent) {
        int rootI = find(i, parent);
        int rootJ = find(j, parent);
        if (rootI != rootJ) {
            parent[rootI] = rootJ;
            return true;
        }
        return false;
    }

    private void verifyForests(int N, LongValueForest[] forests, int expectedTotalEdges) {
        assertEquals(2, forests.length);
        int totalEdges = 0;

        for (int i = 0; i < 2; i++) {
            LongValueForest f = forests[i];
            assertEquals(N, f.N);
            List<Edge> edges = f.edges();
            totalEdges += edges.size();

            assertTrue(isForest(f), "Forest " + i + " has a cycle");
        }
        assertEquals(expectedTotalEdges, totalEdges);
    }

    private boolean isForest(LongValueForest f) {
        boolean[] visited = new boolean[f.N];
        for (int i = 0; i < f.N; i++) {
            if (!visited[i]) {
                if (hasCycle(f, i, -1, visited)) return false;
            }
        }
        return true;
    }

    private boolean hasCycle(LongValueForest f, int v, int p, boolean[] visited) {
        visited[v] = true;
        for (Edge e : f.adj[v]) {
            if (e.dst == p) continue;
            if (visited[e.dst]) return true;
            if (hasCycle(f, e.dst, v, visited)) return true;
        }
        return false;
    }
}
