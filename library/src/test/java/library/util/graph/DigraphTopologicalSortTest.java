package library.util.graph;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DigraphTopologicalSortTest {

    @Test
    public void testSimpleDAG() {
        int n = 3;
        Digraph g = new Digraph(n);
        // 0 -> 2, 1 -> 2
        g.addEdge(0, 2);
        g.addEdge(1, 2);

        List<int[]> sorts = new ArrayList<>();
        g.forEachTopologicalSortLexOrder(n, ord -> {
            sorts.add(ord);
            return true;
        });

        assertEquals(2, sorts.size());

        boolean found012 = false;
        boolean found102 = false;
        for (int[] s : sorts) {
            if (Arrays.equals(s, new int[]{0, 1, 2})) found012 = true;
            if (Arrays.equals(s, new int[]{1, 0, 2})) found102 = true;
        }
        assertTrue(found012);
        assertTrue(found102);
    }

    @Test
    public void testEarlyExit() {
        int n = 3;
        Digraph g = new Digraph(n);
        g.addEdge(0, 2);
        g.addEdge(1, 2);

        List<int[]> sorts = new ArrayList<>();
        g.forEachTopologicalSortLexOrder(n, ord -> {
            sorts.add(ord);
            return false; // Stop after the first one
        });

        assertEquals(1, sorts.size());
    }

    @Test
    public void testCyclicGraph() {
        int n = 2;
        Digraph g = new Digraph(n);
        g.addEdge(0, 1);
        g.addEdge(1, 0);

        List<int[]> sorts = new ArrayList<>();
        g.forEachTopologicalSortLexOrder(n, ord -> {
            sorts.add(ord);
            return true;
        });

        assertEquals(0, sorts.size());
    }

    @Test
    public void testEmptyGraph() {
        int n = 0;
        Digraph g = new Digraph(n);

        List<int[]> sorts = new ArrayList<>();
        g.forEachTopologicalSortLexOrder(n, ord -> {
            sorts.add(ord);
            return true;
        });

        assertEquals(1, sorts.size()); // One empty ordering
    }

    @Test
    public void testDisconnectedDAG() {
        int n = 2;
        Digraph g = new Digraph(n);
        // No edges

        List<int[]> sorts = new ArrayList<>();
        g.forEachTopologicalSortLexOrder(n, ord -> {
            sorts.add(ord);
            return true;
        });

        assertEquals(2, sorts.size());

        boolean found01 = false;
        boolean found10 = false;
        for (int[] s : sorts) {
            if (Arrays.equals(s, new int[]{0, 1})) found01 = true;
            if (Arrays.equals(s, new int[]{1, 0})) found10 = true;
        }
        assertTrue(found01);
        assertTrue(found10);
    }
}
