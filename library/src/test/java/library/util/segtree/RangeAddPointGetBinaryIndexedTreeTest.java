package library.util.segtree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RangeAddPointGetBinaryIndexedTreeTest {

    @Test
    public void testBasic() {
        int n = 10;
        RangeAddPointGetBinaryIndexedTree bit = new RangeAddPointGetBinaryIndexedTree(n);

        // Initial values should be 0
        for (int i = 0; i < n; i++) {
            assertEquals(0, bit.get(i));
        }

        // Add to range [2, 5)
        bit.rangeAdd(2, 5, 10);
        assertEquals(0, bit.get(1));
        assertEquals(10, bit.get(2));
        assertEquals(10, bit.get(3));
        assertEquals(10, bit.get(4));
        assertEquals(0, bit.get(5));

        // Add to overlapping range [4, 7)
        bit.rangeAdd(4, 7, 5);
        assertEquals(10, bit.get(3));
        assertEquals(15, bit.get(4));
        assertEquals(5, bit.get(5));
        assertEquals(5, bit.get(6));
        assertEquals(0, bit.get(7));

        // Add negative value
        bit.rangeAdd(0, 10, -3);
        assertEquals(-3, bit.get(0));
        assertEquals(7, bit.get(2));
        assertEquals(12, bit.get(4));
        assertEquals(2, bit.get(6));
        assertEquals(-3, bit.get(9));
    }

    @Test
    public void testEdgeCases() {
        int n = 5;
        RangeAddPointGetBinaryIndexedTree bit = new RangeAddPointGetBinaryIndexedTree(n);

        // Empty range
        bit.rangeAdd(1, 1, 100);
        assertEquals(0, bit.get(1));

        // Reversed range
        bit.rangeAdd(3, 1, 100);
        assertEquals(0, bit.get(1));
        assertEquals(0, bit.get(2));

        // Full range
        bit.rangeAdd(0, 5, 42);
        for (int i = 0; i < n; i++) {
            assertEquals(42, bit.get(i));
        }

        // Out of bounds (should not crash)
        bit.rangeAdd(-1, 10, 1);
        assertEquals(43, bit.get(0));
        assertEquals(43, bit.get(4));

        assertEquals(0, bit.get(-1));
        assertEquals(0, bit.get(5));
    }

    @Test
    public void testSet() {
        int n = 10;
        RangeAddPointGetBinaryIndexedTree bit = new RangeAddPointGetBinaryIndexedTree(n);

        bit.rangeAdd(0, 10, 5);
        for (int i = 0; i < n; i++) {
            assertEquals(5, bit.get(i));
        }

        bit.set(3, 100);
        assertEquals(5, bit.get(2));
        assertEquals(100, bit.get(3));
        assertEquals(5, bit.get(4));

        bit.set(0, -10);
        assertEquals(-10, bit.get(0));
        assertEquals(5, bit.get(1));

        bit.set(9, 42);
        assertEquals(5, bit.get(8));
        assertEquals(42, bit.get(9));

        // Out of bounds (should not crash)
        bit.set(-1, 999);
        bit.set(10, 999);
        assertEquals(-10, bit.get(0));
        assertEquals(42, bit.get(9));
    }
}
