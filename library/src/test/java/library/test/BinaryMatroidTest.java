package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.BinaryMatroid;
import library.util.graph.MatroidIntersection;

public class BinaryMatroidTest {

    @Test
    public void testBasic() {
        long[] vecs = {
            0b001,
            0b010,
            0b011,
            0b100
        };
        BinaryMatroid bm = new BinaryMatroid(vecs);
        assertEquals(4, bm.size());

        // Test independent set {0, 1}
        boolean[] I = new boolean[4];
        I[0] = true;
        I[1] = true;
        bm.set(I);

        // v2 = v0 ^ v1, so {0, 1, 2} is a circuit
        IntArrayList c2 = bm.circuit(2);
        assertEquals(3, c2.size());
        int[] circuit = c2.toArray();
        Arrays.sort(circuit);
        assertArrayEquals(new int[]{0, 1, 2}, circuit);

        // v3 is independent of {0, 1}
        IntArrayList c3 = bm.circuit(3);
        assertTrue(c3.isEmpty());
    }

    @Test
    public void testCircuit() {
        long[] vecs = {
            0b1,
            0b10,
            0b100,
            0b111 // 1 ^ 10 ^ 100
        };
        BinaryMatroid bm = new BinaryMatroid(vecs);
        boolean[] I = new boolean[4];
        I[0] = true;
        I[1] = true;
        I[2] = true;
        bm.set(I);

        IntArrayList c = bm.circuit(3);
        int[] res = c.toArray();
        Arrays.sort(res);
        assertArrayEquals(new int[]{0, 1, 2, 3}, res);
    }

    @Test
    public void testIntersection() {
        // Find max subset of vecs that are linearly independent.
        // This is just a single matroid, but we can intersect it with a free matroid.
        long[] vecs = {
            0b001,
            0b010,
            0b011,
            0b100,
            0b101,
            0b110,
            0b111
        };
        BinaryMatroid bm = new BinaryMatroid(vecs);

        // Free matroid: all subsets are independent
        MatroidIntersectionTest.BetterPartitionMatroid free = new MatroidIntersectionTest.BetterPartitionMatroid(
            vecs.length, 1, new int[vecs.length], new int[]{vecs.length}
        );

        boolean[] res = MatroidIntersection.solve(bm, free);
        int count = 0;
        for (boolean b : res) if (b) count++;
        // Max rank in 3D is 3.
        assertEquals(3, count);
    }

    @Test
    public void testMultivariate() {
        long[][] mat = {
            {1L, 0L},
            {0L, 1L},
            {1L, 1L},
            {0L, 0L} // zero vector
        };
        BinaryMatroid bm = new BinaryMatroid(mat);

        boolean[] I = new boolean[4];
        I[0] = true;
        I[1] = true;
        bm.set(I);

        IntArrayList c2 = bm.circuit(2);
        assertEquals(3, c2.size());

        IntArrayList c3 = bm.circuit(3);
        assertEquals(1, c3.size()); // zero vector is a circuit by itself
        assertEquals(3, c3.get(0));
    }

    private void assertArrayEquals(int[] expected, int[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}
