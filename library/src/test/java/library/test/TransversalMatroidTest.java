package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.TransversalMatroid;

public class TransversalMatroidTest {

    @Test
    public void testUserExample() {
        int U = 4, V = 2;
        int[][] edges = {
            {0, 0},
            {1, 0},
            {2, 1}
        };

        TransversalMatroid M1 = new TransversalMatroid(U, V, edges);
        boolean[] I = new boolean[U];
        I[0] = true;
        M1.set(I);

        IntArrayList a = M1.circuit(1);
        assertArrayEquals(new int[]{0, 1}, a.toArray());

        IntArrayList b = M1.circuit(2);
        assertTrue(b.isEmpty());
    }

    @Test
    public void testIndependentSetAddition() {
        int U = 3, V = 3;
        int[][] edges = {
            {0, 0},
            {1, 1},
            {2, 2}
        };
        TransversalMatroid M = new TransversalMatroid(U, V, edges);
        boolean[] I = new boolean[U];
        M.set(I);

        assertTrue(M.circuit(0).isEmpty());
        assertTrue(M.circuit(1).isEmpty());
        assertTrue(M.circuit(2).isEmpty());

        I[0] = true;
        M.set(I);
        assertTrue(M.circuit(1).isEmpty());
        assertTrue(M.circuit(2).isEmpty());

        I[1] = true;
        M.set(I);
        assertTrue(M.circuit(2).isEmpty());
    }

    @Test
    public void testDependentSet() {
        int U = 3, V = 1;
        int[][] edges = {
            {0, 0},
            {1, 0},
            {2, 0}
        };
        TransversalMatroid M = new TransversalMatroid(U, V, edges);
        boolean[] I = new boolean[U];
        I[0] = true;
        M.set(I);

        IntArrayList c1 = M.circuit(1);
        assertArrayEquals(new int[]{0, 1}, c1.toArray());

        IntArrayList c2 = M.circuit(2);
        assertArrayEquals(new int[]{0, 2}, c2.toArray());
    }
}
