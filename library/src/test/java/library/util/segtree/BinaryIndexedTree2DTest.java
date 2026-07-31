package library.util.segtree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryIndexedTree2DTest {

    @Test
    public void testBasic() {
        int H = 5, W = 5;
        BinaryIndexedTree2D bit = new BinaryIndexedTree2D(H, W);

        bit.add(1, 1, 10);
        bit.add(3, 3, 5);
        bit.add(1, 3, 7);

        assertEquals(10, bit.get(1, 1));
        assertEquals(7, bit.get(1, 3));
        assertEquals(5, bit.get(3, 3));
        assertEquals(0, bit.get(0, 0));

        assertEquals(10, bit.prefixSum(2, 2));
        assertEquals(17, bit.prefixSum(2, 4));
        assertEquals(22, bit.prefixSum(5, 5));

        assertEquals(12, bit.sum(1, 2, 4, 4)); // (1, 3) and (3, 3) -> 7 + 5 = 12
        assertEquals(10, bit.sum(1, 1, 2, 2)); // (1, 1) -> 10
        assertEquals(22, bit.sum(0, 0, 5, 5));
    }

    @Test
    public void testNegative() {
        int H = 3, W = 3;
        BinaryIndexedTree2D bit = new BinaryIndexedTree2D(H, W);

        bit.add(0, 0, 10);
        bit.add(1, 1, -5);
        bit.add(2, 2, 3);

        assertEquals(10, bit.get(0, 0));
        assertEquals(-5, bit.get(1, 1));
        assertEquals(3, bit.get(2, 2));

        assertEquals(5, bit.prefixSum(2, 2));
        assertEquals(8, bit.prefixSum(3, 3));

        assertEquals(-5, bit.sum(1, 1, 2, 2));
        assertEquals(-2, bit.sum(1, 1, 3, 3));
    }

    @Test
    public void testClear() {
        int H = 3, W = 3;
        BinaryIndexedTree2D bit = new BinaryIndexedTree2D(H, W);
        bit.add(1, 1, 100);
        assertEquals(100, bit.get(1, 1));
        bit.clear();
        assertEquals(0, bit.get(1, 1));
        assertEquals(0, bit.prefixSum(3, 3));
    }

    @Test
    public void testGeneric() {
        // XOR group
        int H = 3, W = 3;
        BinaryIndexedTree2D bit = new BinaryIndexedTree2D(H, W, (x, y) -> x ^ y, x -> x, 0L);

        bit.add(0, 0, 1);
        bit.add(0, 1, 2);
        bit.add(1, 0, 4);
        bit.add(1, 1, 8);

        assertEquals(1, bit.get(0, 0));
        assertEquals(2, bit.get(0, 1));
        assertEquals(4, bit.get(1, 0));
        assertEquals(8, bit.get(1, 1));

        assertEquals(1 ^ 2 ^ 4 ^ 8, bit.prefixSum(2, 2));
        assertEquals(2 ^ 8, bit.sum(0, 1, 2, 2));
    }

    @Test
    public void testEdgeCases() {
        int H = 2, W = 2;
        BinaryIndexedTree2D bit = new BinaryIndexedTree2D(H, W);

        // Out of bounds add
        bit.add(-1, 0, 10);
        bit.add(2, 0, 10);
        bit.add(0, -1, 10);
        bit.add(0, 2, 10);
        assertEquals(0, bit.prefixSum(2, 2));

        // Prefix sum with 0 or out of bounds
        assertEquals(0, bit.prefixSum(0, 2));
        assertEquals(0, bit.prefixSum(2, 0));

        bit.add(0, 0, 5);
        assertEquals(5, bit.prefixSum(10, 10));

        // Sum with empty/inverted range
        assertEquals(0, bit.sum(1, 1, 1, 1));
        assertEquals(0, bit.sum(2, 2, 1, 1));
    }
}
