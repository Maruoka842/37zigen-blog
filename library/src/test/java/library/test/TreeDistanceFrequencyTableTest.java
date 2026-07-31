package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.tree.Tree;
import library.util.graph.tree.TreeDistanceFrequencyTable;
import library.util.polynomial.PolynomialFpDynamic;

public class TreeDistanceFrequencyTableTest {

    @Test
    public void testPath() {
        int n = 4;
        Tree tree = new Tree(n);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        // 0-1-2-3
        // Paths:
        // dist 0: 0
        // dist 1: (0,1), (1,2), (2,3) -> 3
        // dist 2: (0,2), (1,3) -> 2
        // dist 3: (0,3) -> 1

        long[] weight = new long[n];
        Arrays.fill(weight, 1);
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        TreeDistanceFrequencyTable solver = new TreeDistanceFrequencyTable(tree);
        long[] result = solver.solve(weight, mod, (a, b) -> poly.mul(a, b));

        long[] expected = {0, 3, 2, 1};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testStar() {
        int n = 4;
        Tree tree = new Tree(n);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);
        // Star: 0 is center
        // Paths:
        // dist 0: 0
        // dist 1: (0,1), (0,2), (0,3) -> 3
        // dist 2: (1,2), (1,3), (2,3) -> 3

        long[] weight = new long[n];
        Arrays.fill(weight, 1);
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        TreeDistanceFrequencyTable solver = new TreeDistanceFrequencyTable(tree);
        long[] result = solver.solve(weight, mod, (a, b) -> poly.mul(a, b));

        long[] expected = {0, 3, 3, 0};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testRandomized() {
        int n = 50;
        int trials = 5;
        long mod = 998244353;
        Random rnd = new Random(42);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        for (int t = 0; t < trials; t++) {
            Tree tree = Tree.randomTree(n);
            long[] weight = new long[n];
            for (int i = 0; i < n; i++) weight[i] = rnd.nextInt(100);

            TreeDistanceFrequencyTable solver = new TreeDistanceFrequencyTable(tree);
            long[] result = solver.solve(weight, mod, (a, b) -> poly.mul(a, b));

            long[] expected = solveNaive(tree, weight, mod);
            assertArrayEquals(expected, result, "Trial " + t + " failed");
        }
    }

    private long[] solveNaive(Tree tree, long[] weight, long mod) {
        int n = tree.N;
        long[] ret = new long[n];
        int[][] dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], -1);
            dist[i][i] = 0;
            dfs(i, -1, 0, i, dist, tree);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int d = dist[i][j];
                if (d < n) {
                    long w = weight[i] * weight[j] % mod;
                    ret[d] = (ret[d] + w) % mod;
                }
            }
        }
        return ret;
    }

    private void dfs(int v, int p, int d, int start, int[][] dist, Tree tree) {
        dist[start][v] = d;
        for (int u : tree.adj[v]) {
            if (u != p) {
                dfs(u, v, d + 1, start, dist, tree);
            }
        }
    }
}
