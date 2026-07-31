package library.test;

import library.util.graph.Graph;
import library.util.graph.Graphs;

import java.util.List;
import java.util.Random;

public class GraphsDecomposeToPathsOfLength2Test {
    public static void main(String[] args) {
        test();
        System.out.println("Stress test passed for decomposeToPathsOfLength2.");
    }

    public static void test() {
        Random rnd = new Random(123);
        for (int t = 0; t < 1000; t++) {
            int n = rnd.nextInt(20) + 1;
            int m = rnd.nextInt(50);
            Graph g = new Graph(n);
            for (int i = 0; i < m; i++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                g.addEdge(u, v);
            }

            List<int[]> paths = Graphs.decomposeToPathsOfLength2(g);
            
            // Expected paths = sum over connected components of floor(edges_in_comp / 2)
            var components = g.components();
            int expectedPaths = 0;
            for (var c : components) {
                int actualEdges = 0;
                for (int u : c) {
                    for(int i = 0; i < g.adj[u].size(); i++) {
                        int v = g.adj[u].get(i);
                        if (u <= v) actualEdges++;
                    }
                }
                expectedPaths += actualEdges / 2;
            }
            if (paths.size() != expectedPaths) {
                throw new AssertionError("Expected " + expectedPaths + " paths, but got " + paths.size());
            }

            java.util.Map<Long, Integer> edgePool = new java.util.HashMap<>();
            for (int u = 0; u < n; u++) {
                for (int i = 0; i < g.adj[u].size(); i++) {
                    int v = g.adj[u].get(i);
                    if (u <= v) {
                        long key = library.util.Ints.packUnorderedPair(u, v);
                        edgePool.put(key, edgePool.getOrDefault(key, 0) + 1);
                    }
                }
            }
            
            for (int[] path : paths) {
                int u = path[0], v = path[1], w = path[2];
                if (v != path[1]) throw new AssertionError(); // just sanity
                long e1 = library.util.Ints.packUnorderedPair(u, v);
                long e2 = library.util.Ints.packUnorderedPair(v, w);
                
                int c1 = edgePool.getOrDefault(e1, 0);
                if (c1 <= 0) throw new AssertionError("Edge missing or reused: " + u + "-" + v);
                edgePool.put(e1, c1 - 1);
                
                int c2 = edgePool.getOrDefault(e2, 0);
                if (c2 <= 0) throw new AssertionError("Edge missing or reused: " + v + "-" + w);
                edgePool.put(e2, c2 - 1);
            }
        }
    }
}
