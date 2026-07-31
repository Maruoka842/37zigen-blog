package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.BitsetSCC;
import library.util.graph.Digraph;

public class BitsetSCCTest {

    @Test
    public void testSimple() {
        Digraph g = new Digraph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);

        BitsetSCC scc = new BitsetSCC(g);
        assertEquals(1, scc.getSccNum());
        assertEquals(0, scc.getCmp()[0]);
        assertEquals(0, scc.getCmp()[1]);
        assertEquals(0, scc.getCmp()[2]);
    }

    @Test
    public void testTwoComponents() {
        Digraph g = new Digraph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 0);
        g.addEdge(2, 3);
        g.addEdge(3, 2);
        g.addEdge(1, 2);

        BitsetSCC scc = new BitsetSCC(g);
        assertEquals(2, scc.getSccNum());
        assertEquals(scc.getCmp()[0], scc.getCmp()[1]);
        assertEquals(scc.getCmp()[2], scc.getCmp()[3]);
        assertTrue(scc.getCmp()[0] != scc.getCmp()[2]);

        // topological order check: 0->1->2->3 means cmp[0,1] < cmp[2,3]
        assertTrue(scc.getCmp()[0] < scc.getCmp()[2]);
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = rnd.nextInt(50) + 1;
            int m = rnd.nextInt(n * n);
            Digraph g = new Digraph(n);
            for (int i = 0; i < m; i++) {
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n));
            }

            BitsetSCC bitsetSCC = new BitsetSCC(g);
            IntArrayList[] expected = g.scc();
            IntArrayList[] actual = bitsetSCC.getComponents();

            assertEquals(expected.length, actual.length);

            // Both results should have the same set of components.
            // Since there can be multiple topological orders, we sort each component and then sort the array of components.
            int[][] eSorted = new int[expected.length][];
            for (int i = 0; i < expected.length; i++) {
                eSorted[i] = expected[i].toArray();
                Arrays.sort(eSorted[i]);
            }
            Arrays.sort(eSorted, (a, b) -> a[0] - b[0]);

            int[][] aSorted = new int[actual.length][];
            for (int i = 0; i < actual.length; i++) {
                aSorted[i] = actual[i].toArray();
                Arrays.sort(aSorted[i]);
            }
            Arrays.sort(aSorted, (a, b) -> a[0] - b[0]);

            for (int i = 0; i < eSorted.length; i++) {
                assertArrayEquals(eSorted[i], aSorted[i]);
            }
        }
    }

    @Test
    public void testDense() {
        int n = 500;
        Digraph g = new Digraph(n);
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                if (rnd.nextDouble() < 0.5) {
                    g.addEdge(i, j);
                }
            }
        }

        long start = System.currentTimeMillis();
        BitsetSCC bitsetSCC = new BitsetSCC(g);
        long end = System.currentTimeMillis();
        System.out.println("BitsetSCC dense time: " + (end - start) + "ms");

        IntArrayList[] expected = g.scc();
        IntArrayList[] actual = bitsetSCC.getComponents();

        assertEquals(expected.length, actual.length);

        int[][] eSorted = new int[expected.length][];
        for (int i = 0; i < expected.length; i++) {
            eSorted[i] = expected[i].toArray();
            Arrays.sort(eSorted[i]);
        }
        Arrays.sort(eSorted, (a, b) -> a[0] - b[0]);

        int[][] aSorted = new int[actual.length][];
        for (int i = 0; i < actual.length; i++) {
            aSorted[i] = actual[i].toArray();
            Arrays.sort(aSorted[i]);
        }
        Arrays.sort(aSorted, (a, b) -> a[0] - b[0]);

        for (int i = 0; i < eSorted.length; i++) {
            assertArrayEquals(eSorted[i], aSorted[i]);
        }
    }
}
