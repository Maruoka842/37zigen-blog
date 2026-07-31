package library.util.graph.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.longs.LongAbelianGroupStrategy;

public class StaticOfflineSubtreeSumTest {

    private static class SumStrategy implements LongAbelianGroupStrategy {
        @Override public long identity() { return 0; }
        @Override public long mul(long a, long b) { return a + b; }
        @Override public long inverse(long a) { return -a; }
    }

    private record P(int v, int k, long v1) {}

    @Test
    public void testMinimalManual() {
        Tree tree = new Tree(5);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(1, 3);
        tree.addEdge(1, 4);
        tree.rooted(0);

        StaticOfflineSubtreeSum soss = new StaticOfflineSubtreeSum(tree, new SumStrategy(), 100);
        soss.addPoint(0, 10, 1);
        soss.addPoint(1, 20, 2);
        soss.addPoint(2, 30, 4);
        soss.addPoint(3, 15, 8);
        soss.addPoint(4, 25, 16);
        int q0 = soss.addQuery(1, 15, 26);
        int q1 = soss.addQuery(1, 26, 31);
        long[] ans = soss.solve();
        assertEquals(26, ans[q0]);
        assertEquals(0, ans[q1]);
    }

    @Test
    public void testPath() {
        int n = 1000;
        Tree tree = new Tree(n);
        for (int i = 0; i < n - 1; i++) {
            tree.addEdge(i, i + 1);
        }
        tree.rooted(0);

        StaticOfflineSubtreeSum soss = new StaticOfflineSubtreeSum(tree, new SumStrategy(), n);
        for (int i = 0; i < n; i++) {
            soss.addPoint(i, i, 1);
        }
        int[] qs = new int[n];
        for (int i = 0; i < n; i++) {
            qs[i] = soss.addQuery(i, 0, n);
        }
        long[] ans = soss.solve();
        for (int i = 0; i < n; i++) {
            assertEquals(n - i, ans[qs[i]]);
        }
    }

    @Test
    public void testSiblingSubtreeDoesNotLeak() {
        Tree tree = new Tree(3);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.rooted(0);

        StaticOfflineSubtreeSum soss = new StaticOfflineSubtreeSum(tree, new SumStrategy(), 100);
        soss.addPoint(1, 5, 10);
        soss.addPoint(2, 5, 100);
        int q0 = soss.addQuery(1, 0, 10);
        int q1 = soss.addQuery(2, 0, 10);
        long[] ans = soss.solve();
        assertEquals(10, ans[q0]);
        assertEquals(100, ans[q1]);
    }

    @Test
    public void testRandom() {
        int n = 100;
        Random rnd = new Random(42);
        Tree tree = Tree.randomTree(n);
        tree.rooted(0);

        StaticOfflineSubtreeSum soss = new StaticOfflineSubtreeSum(tree, new SumStrategy(), 100);

        List<P> points = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            int v = rnd.nextInt(n);
            int k = rnd.nextInt(100);
            long v1 = rnd.nextLong(1000);
            soss.addPoint(v, k, v1);
            points.add(new P(v, k, v1));
        }
        record Q(int v, int l, int r, int id) {}
        List<Q> queries = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            int v = rnd.nextInt(n);
            int l = rnd.nextInt(50);
            int r = l + rnd.nextInt(50);
            queries.add(new Q(v, l, r, soss.addQuery(v, l, r)));
        }
        long[] ans = soss.solve();
        for (Q q : queries) {
            long expected = 0;
            List<Integer> subtree = getSubtree(tree, q.v());
            for (P p : points) {
                if (subtree.contains(p.v()) && p.k() >= q.l() && p.k() < q.r()) {
                    expected += p.v1();
                }
            }
            assertEquals(expected, ans[q.id()], "Failed for query at v=" + q.v() + " [" + q.l() + ", " + q.r() + ")");
        }
    }

    private List<Integer> getSubtree(Tree tree, int root) {
        List<Integer> res = new ArrayList<>();
        dfs(tree, root, res);
        return res;
    }

    private void dfs(Tree tree, int v, List<Integer> res) {
        res.add(v);
        for (int child : tree.childs[v]) {
            dfs(tree, child, res);
        }
    }
}
