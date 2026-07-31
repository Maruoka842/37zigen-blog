package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.graph.tree.ConvolutionOnTree;
import library.util.graph.tree.Tree;
import library.util.polynomial.PolynomialFpDynamic;

public class ConvolutionOnTreeTest {
    @Test
    public void testYukicoder2004Sample1() {
        // Yukicoder 2004 Sample 1
        // N=4, root=0
        // par: -1 0 1 2
        // A: 1 1 1 1
        // trans: 1 2 3 4

        int N = 4;
        int[] par = {-1, 0, 1, 2};
        long[] A = {1, 1, 1, 1};
        long[] trans = {1, 2, 3, 4};
        long mod = 998244353;

        ConvolutionOnTree tree = new ConvolutionOnTree(par);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] result = tree.run(A, trans, mod, (l, r) -> poly.mul(l, r));

        // Expected result:
        // v=3: subtree={3}, dists={0}. ret[3] = A[3]*trans[0] = 1*1 = 1
        // v=2: subtree={2,3}, dists={0,1}. ret[2] = A[2]*trans[0] + A[3]*trans[1] = 1*1 + 1*2 = 3
        // v=1: subtree={1,2,3}, dists={0,1,2}. ret[1] = A[1]*trans[0] + A[2]*trans[1] + A[3]*trans[2] = 1*1 + 1*2 + 1*3 = 6
        // v=0: subtree={0,1,2,3}, dists={0,1,2,3}. ret[0] = A[0]*trans[0] + A[1]*trans[1] + A[2]*trans[2] + A[3]*trans[3] = 1*1 + 1*2 + 1*3 + 1*4 = 10

        long[] expected = {10, 6, 3, 1};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBranchingTree() {
        // Rooted tree: 0 -> 1, 0 -> 2
        // par: -1, 0, 0
        // A: 10, 20, 30
        // trans: 1, 5

        int[] par = {-1, 0, 0};
        long[] A = {10, 20, 30};
        long[] trans = {1, 5};
        long mod = 998244353;

        ConvolutionOnTree tree = new ConvolutionOnTree(par);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] result = tree.run(A, trans, mod, (l, r) -> poly.mul(l, r));

        // Expected:
        // v=1: subtree={1}, dist=0. ret[1] = 20*1 = 20
        // v=2: subtree={2}, dist=0. ret[2] = 30*1 = 30
        // v=0: subtree={0,1,2}, dists: dist(0,0)=0, dist(1,0)=1, dist(2,0)=1
        // ret[0] = 10*trans[0] + 20*trans[1] + 30*trans[1] = 10*1 + 20*5 + 30*5 = 10 + 100 + 150 = 260

        long[] expected = {260, 20, 30};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testRandomized() {
        int N = 100;
        int trials = 10;
        long mod = 998244353;
        Random rnd = new Random(42);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        for (int t = 0; t < trials; t++) {
            Tree tree = Tree.randomTree(N);
            int root = rnd.nextInt(N);
            tree.rooted(root);

            int[] par = new int[N];
            for (int i = 0; i < N; i++) {
                par[i] = (i == root) ? -1 : tree.parent(i);
            }

            long[] f = new long[N];
            for (int i = 0; i < N; i++) f[i] = rnd.nextInt((int) mod);

            long[] trans = new long[N];
            for (int i = 0; i < N; i++) trans[i] = rnd.nextInt((int) mod);

            ConvolutionOnTree cot = new ConvolutionOnTree(par);
            long[] result = cot.run(f, trans, mod, (l, r) -> poly.mul(l, r));

            long[] expected = solveNaive(N, par, f, trans, mod);
            assertArrayEquals(expected, result, "Trial " + t + " failed");
        }
    }

    private long[] solveNaive(int N, int[] par, long[] f, long[] trans, long mod) {
        long[] ret = new long[N];
        int[][] dists = new int[N][N];
        for (int i = 0; i < N; i++) Arrays.fill(dists[i], -1);

        for (int i = 0; i < N; i++) {
            dists[i][i] = 0;
            int cur = i;
            int d = 0;
            while (par[cur] != -1) {
                int p = par[cur];
                d++;
                dists[i][p] = d;
                cur = p;
            }
        }

        for (int v = 0; v < N; v++) {
            for (int u = 0; u < N; u++) {
                if (dists[u][v] != -1) {
                    int d = dists[u][v];
                    if (d < trans.length) {
                        ret[v] = (ret[v] + f[u] * trans[d]) % mod;
                    }
                }
            }
        }
        return ret;
    }

    @Test
    public void testTransposedYukicoder2004Sample1() {
        int N = 4;
        int[] par = {-1, 0, 1, 2};
        long[] f = {1, 1, 1, 1};
        long[] trans = {1, 2, 3, 4};
        long mod = 998244353;

        ConvolutionOnTree tree = new ConvolutionOnTree(par);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] result = tree.runTransposed(f, trans, mod, (l, r) -> poly.mul(l, r));

        // v=0: ancestors={0}, dists={0}. ret[0] = f[0]*trans[0] = 1*1 = 1
        // v=1: ancestors={0,1}, dists={1,0}. ret[1] = f[0]*trans[1] + f[1]*trans[0] = 1*2 + 1*1 = 3
        // v=2: ancestors={0,1,2}, dists={2,1,0}. ret[2] = f[0]*trans[2] + f[1]*trans[1] + f[2]*trans[0] = 1*3 + 1*2 + 1*1 = 6
        // v=3: ancestors={0,1,2,3}, dists={3,2,1,0}. ret[3] = f[0]*trans[3] + f[1]*trans[2] + f[2]*trans[1] + f[3]*trans[0] = 1*4 + 1*3 + 1*2 + 1*1 = 10

        long[] expected = {1, 3, 6, 10};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testTransposedBranchingTree() {
        int[] par = {-1, 0, 0};
        long[] f = {10, 20, 30};
        long[] trans = {1, 5};
        long mod = 998244353;

        ConvolutionOnTree tree = new ConvolutionOnTree(par);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] result = tree.runTransposed(f, trans, mod, (l, r) -> poly.mul(l, r));

        // v=0: ancestors={0}, dist=0. ret[0] = 10*1 = 10
        // v=1: ancestors={0,1}, dists: dist(0,1)=1, dist(1,1)=0. ret[1] = 10*5 + 20*1 = 70
        // v=2: ancestors={0,2}, dists: dist(0,2)=1, dist(2,2)=0. ret[2] = 10*5 + 30*1 = 80

        long[] expected = {10, 70, 80};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testTransposedRandomized() {
        int N = 100;
        int trials = 10;
        long mod = 998244353;
        Random rnd = new Random(43);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        for (int t = 0; t < trials; t++) {
            Tree tree = Tree.randomTree(N);
            int root = rnd.nextInt(N);
            tree.rooted(root);

            int[] par = new int[N];
            for (int i = 0; i < N; i++) {
                par[i] = (i == root) ? -1 : tree.parent(i);
            }

            long[] f = new long[N];
            for (int i = 0; i < N; i++) f[i] = rnd.nextInt((int) mod);

            long[] trans = new long[N];
            for (int i = 0; i < N; i++) trans[i] = rnd.nextInt((int) mod);

            ConvolutionOnTree cot = new ConvolutionOnTree(par);
            long[] result = cot.runTransposed(f, trans, mod, (l, r) -> poly.mul(l, r));

            long[] expected = solveNaiveTransposed(N, par, f, trans, mod);
            assertArrayEquals(expected, result, "Trial " + t + " failed");
        }
    }

    private long[] solveNaiveTransposed(int N, int[] par, long[] f, long[] trans, long mod) {
        long[] ret = new long[N];
        int[][] dists = new int[N][N];
        for (int i = 0; i < N; i++) Arrays.fill(dists[i], -1);

        for (int i = 0; i < N; i++) {
            dists[i][i] = 0;
            int cur = i;
            int d = 0;
            while (par[cur] != -1) {
                int p = par[cur];
                d++;
                dists[i][p] = d;
                cur = p;
            }
        }

        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                if (dists[u][v] != -1) {
                    int d = dists[u][v];
                    if (d < trans.length) {
                        ret[u] = (ret[u] + f[v] * trans[d]) % mod;
                    }
                }
            }
        }
        return ret;
    }

    @Test
    public void testTransposePropertyRandomized() {
        int N = 50;
        int trials = 10;
        long mod = 998244353;
        Random rnd = new Random(44);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        for (int t = 0; t < trials; t++) {
            Tree tree = Tree.randomTree(N);
            int root = rnd.nextInt(N);
            tree.rooted(root);

            int[] par = new int[N];
            for (int i = 0; i < N; i++) {
                par[i] = (i == root) ? -1 : tree.parent(i);
            }

            long[] f = new long[N];
            long[] g = new long[N];
            for (int i = 0; i < N; i++) {
                f[i] = rnd.nextInt((int) mod);
                g[i] = rnd.nextInt((int) mod);
            }

            long[] trans = new long[N];
            for (int i = 0; i < N; i++) trans[i] = rnd.nextInt((int) mod);

            ConvolutionOnTree cot = new ConvolutionOnTree(par);
            long[] convSubtree = cot.run(f, trans, mod, (l, r) -> poly.mul(l, r));
            long[] convAncestors = cot.runTransposed(g, trans, mod, (l, r) -> poly.mul(l, r));

            long sum1 = 0;
            for (int i = 0; i < N; i++) {
                sum1 = (sum1 + convSubtree[i] * g[i]) % mod;
            }

            long sum2 = 0;
            for (int i = 0; i < N; i++) {
                sum2 = (sum2 + f[i] * convAncestors[i]) % mod;
            }

            org.junit.jupiter.api.Assertions.assertEquals(sum1, sum2, "Trial " + t + " failed transpose property");
        }
    }
}
