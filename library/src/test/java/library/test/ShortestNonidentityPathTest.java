package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.GroupStrategy;
import library.util.graph.ShortestNonidentityPath;

public class ShortestNonidentityPathTest {

    static class XorGroupStrategy implements GroupStrategy<Long> {
        @Override
        public Long identity() {
            return 0L;
        }

        @Override
        public Long mul(Long a, Long b) {
            return a ^ b;
        }

        @Override
        public Long inverse(Long a) {
            return a;
        }

        @Override
        public boolean equals(Long a, Long b) {
            return a.equals(b);
        }
    }

    @Test
    public void testBasic() {
        int n = 3;
        XorGroupStrategy group = new XorGroupStrategy();
        ShortestNonidentityPath<Long> snp = new ShortestNonidentityPath<>(n, group);

        // 0 -(1, 1)-> 1 -(1, 1)-> 2
        // Shortest path 0->2 is length 2, but group label is 1^1 = 0
        snp.addBiEdge(0, 1, 1, 1L);
        snp.addBiEdge(1, 2, 1, 1L);

        // 0 -(10, 1)-> 2
        // Path 0->2 is length 10, group label is 1 (non-zero)
        snp.addBiEdge(0, 2, 10, 1L);

        snp.solve(0);

        // Should find non-zero path 0-2 (10)
        assertEquals(10, snp.dist[2]);
        assertEquals(1, snp.dist[1]);
        // For node 0, it depends on non-zero cycle. Cycle 0-1-2-0 has label 1^1^1 = 1, length 1+1+10=12.
        assertEquals(12, snp.dist[0]);
    }

    @Test
    public void testComplex() {
        // Based on Example 1 from the paper
        int n = 4;
        XorGroupStrategy group = new XorGroupStrategy();
        ShortestNonidentityPath<Long> snp = new ShortestNonidentityPath<>(n, group);

        snp.addBiEdge(0, 1, 1, 1L);
        snp.addBiEdge(1, 2, 1, 0L);
        snp.addBiEdge(2, 3, 1, 1L);
        snp.addBiEdge(3, 0, 1, 0L);

        snp.solve(0);

        assertEquals(1, snp.dist[1]); // 0-1 (label 1)
        assertEquals(2, snp.dist[2]); // 0-1-2 (label 1)

        // Cycle 0-1-2-3-0 is 1^0^1^0 = 0. So no non-zero cycle.
        assertEquals(ShortestNonidentityPath.INF, snp.dist[3]);
        assertEquals(ShortestNonidentityPath.INF, snp.dist[0]);
    }

    @Test
    public void testWithNonzeroCycle() {
        int n = 2;
        XorGroupStrategy group = new XorGroupStrategy();
        ShortestNonidentityPath<Long> snp = new ShortestNonidentityPath<>(n, group);

        snp.addBiEdge(0, 1, 1, 0L); // Tree edge
        snp.addBiEdge(0, 1, 10, 1L); // Non-zero edge

        snp.solve(0);

        assertEquals(10, snp.dist[1]);
        assertEquals(11, snp.dist[0]); // Cycle 0-1-0 is length 11, label 1
    }

    record Edge(int u, int v, long len, long g) {}

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        int numTests = 100;
        XorGroupStrategy group = new XorGroupStrategy();

        for (int t = 0; t < numTests; t++) {
            int n = rnd.nextInt(5) + 2;
            int m = rnd.nextInt(10) + 1;
            ShortestNonidentityPath<Long> snp = new ShortestNonidentityPath<>(n, group);

            ArrayList<Edge> edges = new ArrayList<>();

            for (int i = 0; i < m; i++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                if (u == v) continue;
                long len = rnd.nextInt(100) + 1;
                long g = rnd.nextInt(4);
                snp.addBiEdge(u, v, len, g);
                edges.add(new Edge(u, v, len, g));
            }

            int s = rnd.nextInt(n);
            snp.solve(s);

            long[] expected = solveNaive(n, edges, s);
            assertArrayEquals(expected, snp.dist, "Failed on test " + t);
        }
    }

    private long[] solveNaive(int n, ArrayList<Edge> edges, int s) {
        int numGroups = 4;
        long[][] dist = new long[n][numGroups];
        for (int i = 0; i < n; i++) Arrays.fill(dist[i], ShortestNonidentityPath.INF);

        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        dist[s][0] = 0;
        pq.add(new long[]{0, s, 0});

        while (!pq.isEmpty()) {
            long[] state = pq.poll();
            long d = state[0];
            int u = (int) state[1];
            int g = (int) state[2];

            if (d > dist[u][g]) continue;

            for (Edge e : edges) {
                int v = -1;
                if (e.u == u) v = e.v;
                else if (e.v == u) v = e.u;

                if (v != -1) {
                    int ng = (int) (g ^ e.g);
                    if (dist[v][ng] > d + e.len) {
                        dist[v][ng] = d + e.len;
                        pq.add(new long[]{dist[v][ng], v, ng});
                    }
                }
            }
        }

        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            long minD = ShortestNonidentityPath.INF;
            for (int g = 1; g < numGroups; g++) {
                minD = Math.min(minD, dist[i][g]);
            }
            result[i] = minD;
        }
        return result;
    }
}