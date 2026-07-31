package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;
import library.util.graph.tree.Tree;
import library.util.linalg.MatrixUtilsFp;
import library.util.linalg.MatrixUtilsZ;
import library.util.graph.tree.LongValueTree;
import library.util.graph.tree.MatchingOnTree;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.graph.Edge;
import java.util.Arrays;

public class MatchingOnTreeTest {
    private static final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    @Test
    public void testSingleNode() {
        Tree tree = new Tree(1);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Matching size 0: 1 (empty matching)
        assertArrayEquals(new long[]{1}, result);
    }

    @Test
    public void testDeterminantIXA() {
        // 0 - 1 - 2
        Tree tree = new Tree(3);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.rooted(0);
        long[] result = MatchingOnTree.determinantIXA(tree);
        // Matching: {1, 2}
        // det(I - xA) = 1 - 2x^2
        assertArrayEquals(new long[]{1, 0, (poly.mod - 2) % poly.mod}, result);
    }

    @Test
    public void testWeightedDeterminantIXA() {
        // 0 -(2)- 1 -(3)- 2
        LongValueTree tree = new LongValueTree(3);
        tree.addEdge(0, 1, 2);
        tree.addEdge(1, 2, 3);
        tree.rooted(0);
        long[] result = MatchingOnTree.determinantIXA(tree);
        // det(I - xA) = 1 - (2^2 + 3^2)x^2 = 1 - 13x^2
        assertArrayEquals(new long[]{1, 0, (poly.mod - 13) % poly.mod}, result);
    }

    @Test
    public void testDeterminantPath4() {
        // 0 - 1 - 2 - 3
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.rooted(0);
        long[] result = MatchingOnTree.determinantIXA(tree);
        // Matching: {1, 3, 1}
        // det(I - xA) = 1 - 3x^2 + 1x^4
        assertArrayEquals(new long[]{1, 0, (poly.mod - 3) % poly.mod, 0, 1}, result);
    }

    @Test
    public void testPath3() {
        // 0 - 1 - 2
        Tree tree = new Tree(3);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Size 0: 1
        // Size 1: 2 (edges (0,1) and (1,2))
        // Expected: {1, 2}
        assertArrayEquals(new long[]{1, 2}, result);
    }

    @Test
    public void testStar4() {
        // 0 - 1, 0 - 2, 0 - 3
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Size 0: 1
        // Size 1: 3
        // Size 2+: 0 (all edges share node 0)
        assertArrayEquals(new long[]{1, 3}, result);
    }

    @Test
    public void testPath4() {
        // 0 - 1 - 2 - 3
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Size 0: 1
        // Size 1: 3 (edges (0,1), (1,2), (2,3))
        // Size 2: 1 (matching {(0,1), (2,3)})
        assertArrayEquals(new long[]{1, 3, 1}, result);
    }

    @Test
    public void testWeightedMatching() {
        // 0 -(2)- 1 -(3)- 2
        LongValueTree tree = new LongValueTree(3);
        tree.addEdge(0, 1, 2);
        tree.addEdge(1, 2, 3);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Size 0: 1
        // Size 1: 2 + 3 = 5
        // Expected: {1, 5}
        assertArrayEquals(new long[]{1, 5}, result);
    }

    @Test
    public void testWeightedMatchingStar() {
        // 0 -(10)- 1, 0 -(20)- 2, 0 -(30)- 3
        LongValueTree tree = new LongValueTree(4);
        tree.addEdge(0, 1, 10);
        tree.addEdge(0, 2, 20);
        tree.addEdge(0, 3, 30);
        tree.rooted(0);
        long[] result = MatchingOnTree.countMatching(tree);
        // Size 0: 1
        // Size 1: 10 + 20 + 30 = 60
        assertArrayEquals(new long[]{1, 60}, result);
    }

    @Test
    public void testRandomTree() {
        for (int t = 0; t < 10; t++) {
            int N = 50;
            Tree tree = Tree.randomTree(N);
            tree.rooted(0);
            long[] expected = naiveMatchingDP(tree);
            long[] actual = MatchingOnTree.countMatching(tree);
            assertArrayEquals(poly.resize(expected), poly.resize(actual));
        }
    }

    @Test
    public void testRandomDeterminantIXA() {
        for (int t = 0; t < 10; t++) {
            int N = 20;
            Tree tree = Tree.randomTree(N);
            tree.rooted(0);
            long[][] A = new long[N][N];
            for (int i = 0; i < N; i++) {
                for (int next : tree.adj[i].toArray()) {
                    A[i][next] = 1;
                }
            }

            long[][] A_utils = new long[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (A[i][j] != 0) A_utils[i][j] = (poly.mod - A[i][j]) % poly.mod;
                }
            }
            long[][] B_utils = MatrixUtilsZ.longMatrixIdentity(N);
            long[] expected = MatrixUtilsFp.determinantAxPlusBOnFp(A_utils, B_utils, poly.mod);

            long[] actual = MatchingOnTree.determinantIXA(tree);
            assertArrayEquals(poly.resize(expected), poly.resize(actual));
        }
    }

    @Test
    public void testRandomWeightedDeterminantIXA() {
        for (int t = 0; t < 10; t++) {
            int N = 20;
            LongValueTree tree = new LongValueTree(N);
            // Construct a random tree
            int[] p = new int[N];
            for (int i = 1; i < N; i++) {
                p[i] = (int) (Math.random() * i);
                long w = (long) (Math.random() * poly.mod);
                tree.addEdge(i, p[i], w);
            }
            tree.rooted(0);

            long[][] A = new long[N][N];
            for (int i = 0; i < N; i++) {
                for (Edge e : tree.adj[i]) {
                    A[i][e.dst] = e.cost % poly.mod;
                }
            }

            long[][] A_utils = new long[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (A[i][j] != 0) A_utils[i][j] = (poly.mod - A[i][j]) % poly.mod;
                }
            }
            long[][] B_utils = MatrixUtilsZ.longMatrixIdentity(N);
            long[] expected = MatrixUtilsFp.determinantAxPlusBOnFp(A_utils, B_utils, poly.mod);

            long[] actual = MatchingOnTree.determinantIXA(tree);
            assertArrayEquals(poly.resize(expected), poly.resize(actual));
        }
    }

    private long[] naiveMatchingDP(Tree tree) {
        int N = tree.N;
        int[] order = tree.postOrder();
        // dp[v][0]: v is not matched
        // dp[v][1]: v is matched
        long[][] dp0 = new long[N][];
        long[][] dp1 = new long[N][];

        for (int v : order) {
            long[] f0 = new long[]{1};
            long[] f1 = new long[0];

            for (int child : tree.childs[v].toArray()) {
                long[] g0 = dp0[child];
                long[] g1 = dp1[child];
                long[] g = poly.add(g0, g1);

                // v is matched with child
                long[] next_f1 = poly.add(poly.mul(f1, g), poly.multiplyByX(poly.mul(f0, g0), 1));
                // v is not matched with child
                long[] next_f0 = poly.mul(f0, g);

                f0 = next_f0;
                f1 = next_f1;
            }
            dp0[v] = f0;
            dp1[v] = f1;
        }
        return poly.add(dp0[tree.root()], dp1[tree.root()]);
    }
}
