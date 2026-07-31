package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OfflineDigraphReachabilityTest {

    @Test
    public void testDAG() {
        int N = 5;
        Digraph g = new Digraph(N);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 3);
        g.addEdge(3, 4);

        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        int[] a = {0, 0, 1, 1, 2, 3, 4, 0};
        int[] b = {2, 4, 2, 4, 0, 4, 0, 0};
        for (int i = 0; i < a.length; i++) {
            reachability.addQuery(a[i], b[i]);
        }

        // 0->2: T, 0->4: T, 1->2: T, 1->4: F, 2->0: F, 3->4: T, 4->0: F, 0->0: T
        boolean[] expected = {true, true, true, false, false, true, false, true};
        boolean[] actual = reachability.solve();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCycles() {
        int N = 4;
        Digraph g = new Digraph(N);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0); // 0, 1, 2 are in one SCC
        g.addEdge(2, 3);

        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        int[] a = {0, 3, 3, 1, 2};
        int[] b = {3, 0, 3, 0, 1};
        for (int i = 0; i < a.length; i++) {
            reachability.addQuery(a[i], b[i]);
        }

        // 0->3: T, 3->0: F, 3->3: T, 1->0: T, 2->1: T
        boolean[] expected = {true, false, true, true, true};
        boolean[] actual = reachability.solve();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testDisconnected() {
        int N = 4;
        Digraph g = new Digraph(N);
        g.addEdge(0, 1);
        g.addEdge(2, 3);

        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        int[] a = {0, 1, 2, 3, 0};
        int[] b = {1, 0, 3, 2, 3};
        for (int i = 0; i < a.length; i++) {
            reachability.addQuery(a[i], b[i]);
        }

        // 0->1: T, 1->0: F, 2->3: T, 3->2: F, 0->3: F
        boolean[] expected = {true, false, true, false, false};
        boolean[] actual = reachability.solve();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBatchBoundary() {
        int N = 2;
        Digraph g = new Digraph(N);
        g.addEdge(0, 1);

        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        int Q = 130; // 64 * 2 + 2
        boolean[] expected = new boolean[Q];
        for (int i = 0; i < Q; i++) {
            if (i % 2 == 0) {
                reachability.addQuery(0, 1);
                expected[i] = true;
            } else {
                reachability.addQuery(1, 0);
                expected[i] = false;
            }
        }
        boolean[] actual = reachability.solve();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testEmptyQueries() {
        int N = 5;
        Digraph g = new Digraph(N);
        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        boolean[] actual = reachability.solve();
        assertEquals(0, actual.length);
    }

    @Test
    public void testSingleVertex() {
        int N = 1;
        Digraph g = new Digraph(N);
        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        reachability.addQuery(0, 0);
        boolean[] expected = {true};
        boolean[] actual = reachability.solve();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testActualDAG() {
        int N = 5;
        DAG g = new DAG(N);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 3);
        g.addEdge(3, 4);

        OfflineDigraphReachability reachability = new OfflineDigraphReachability(g);
        int[] a = {0, 0, 1, 1, 2, 3, 4, 0};
        int[] b = {2, 4, 2, 4, 0, 4, 0, 0};
        for (int i = 0; i < a.length; i++) {
            reachability.addQuery(a[i], b[i]);
        }

        // 0->2: T, 0->4: T, 1->2: T, 1->4: F, 2->0: F, 3->4: T, 4->0: F, 0->0: T
        boolean[] expected = {true, true, true, false, false, true, false, true};
        boolean[] actual = reachability.solve();

        assertArrayEquals(expected, actual);
    }
}
