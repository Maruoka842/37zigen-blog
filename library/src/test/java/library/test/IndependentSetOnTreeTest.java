package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;
import library.util.graph.tree.Tree;
import library.util.graph.tree.CountIndependentSetOnTree;
import library.util.polynomial.PolynomialFpDynamic;
import java.util.Arrays;

public class IndependentSetOnTreeTest {
    private static final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    @Test
    public void testSingleNode() {
        Tree tree = new Tree(1);
        tree.rooted(0);
        long[] result = CountIndependentSetOnTree.countIndependentSet(tree);
        // Size 0: 1 (empty set)
        // Size 1: 1 ({0})
        assertArrayEquals(new long[]{1, 1}, result);
    }

    @Test
    public void testPath2() {
        // 0 - 1
        Tree tree = new Tree(2);
        tree.addEdge(0, 1);
        tree.rooted(0);
        long[] result = CountIndependentSetOnTree.countIndependentSet(tree);
        // Size 0: 1 (empty)
        // Size 1: 2 ({0}, {1})
        // Size 2: 0 (0 and 1 are adjacent)
        assertArrayEquals(new long[]{1, 2}, result);
    }

    @Test
    public void testPath3() {
        // 0 - 1 - 2
        Tree tree = new Tree(3);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.rooted(0);
        long[] result = CountIndependentSetOnTree.countIndependentSet(tree);
        // Size 0: 1 (empty)
        // Size 1: 3 ({0}, {1}, {2})
        // Size 2: 1 ({0, 2})
        assertArrayEquals(new long[]{1, 3, 1}, result);
    }

    @Test
    public void testStar4() {
        // 0 - 1, 0 - 2, 0 - 3
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);
        tree.rooted(0);
        long[] result = CountIndependentSetOnTree.countIndependentSet(tree);
        // Size 0: 1 (empty)
        // Size 1: 4 ({0}, {1}, {2}, {3})
        // Size 2: 3 ({1, 2}, {1, 3}, {2, 3})
        // Size 3: 1 ({1, 2, 3})
        assertArrayEquals(new long[]{1, 4, 3, 1}, result);
    }

    @Test
    public void testRandomTree() {
        for (int t = 0; t < 10; t++) {
            int N = 50;
            Tree tree = Tree.randomTree(N);
            tree.rooted(0);
            long[] expected = naiveIndependentSetDP(tree);
            long[] actual = CountIndependentSetOnTree.countIndependentSet(tree);
            assertArrayEquals(poly.resize(expected), poly.resize(actual));
        }
    }

    private long[] naiveIndependentSetDP(Tree tree) {
        int N = tree.N;
        int[] order = tree.postOrder();
        // dp[v][0]: v is not in independent set
        // dp[v][1]: v is in independent set
        long[][] dp0 = new long[N][];
        long[][] dp1 = new long[N][];

        for (int v : order) {
            long[] f0 = new long[]{1};
            long[] f1 = new long[]{0, 1};

            for (int child : tree.childs[v].toArray()) {
                long[] g0 = dp0[child];
                long[] g1 = dp1[child];

                // If v is NOT in IS, child can be either in IS or not in IS
                f0 = poly.mul(f0, poly.add(g0, g1));
                // If v IS in IS, child must NOT be in IS
                f1 = poly.mul(f1, g0);
            }
            dp0[v] = f0;
            dp1[v] = f1;
        }
        return poly.add(dp0[tree.root()], dp1[tree.root()]);
    }
}
