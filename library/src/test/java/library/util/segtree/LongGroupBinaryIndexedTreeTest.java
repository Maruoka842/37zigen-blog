package library.util.segtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import library.util.algebra.strategy.longs.LongAbelianGroupStrategy;
import library.util.algebra.strategy.longs.LongZStrategy;

public class LongGroupBinaryIndexedTreeTest {
    static class LongZAbelianGroupStrategy implements LongAbelianGroupStrategy {
        LongZStrategy z = new LongZStrategy();
        @Override public long identity() { return z.zero(); }
        @Override public long mul(long a, long b) { return z.add(a, b); }
        @Override public long inverse(long a) { return z.neg(a); }
    }

    @Test
    public void testBasic() {
        int n = 10;
        LongZAbelianGroupStrategy strategy = new LongZAbelianGroupStrategy();
        LongAbelianGroupBinaryIndexedTree bit = new LongAbelianGroupBinaryIndexedTree(n, strategy);

        bit.add(2, 5);
        bit.add(5, 10);
        bit.add(8, -3);

        assertEquals(0, bit.prefixSum(0));
        assertEquals(0, bit.prefixSum(1));
        assertEquals(0, bit.prefixSum(2));
        assertEquals(5, bit.prefixSum(3));
        assertEquals(5, bit.prefixSum(4));
        assertEquals(5, bit.prefixSum(5));
        assertEquals(15, bit.prefixSum(6));
        assertEquals(15, bit.prefixSum(7));
        assertEquals(15, bit.prefixSum(8));
        assertEquals(12, bit.prefixSum(9));
        assertEquals(12, bit.prefixSum(10));

        assertEquals(5, bit.fold(2, 3));
        assertEquals(10, bit.fold(5, 6));
        assertEquals(-3, bit.fold(8, 9));
        assertEquals(15, bit.fold(0, 6));
        assertEquals(12, bit.fold(0, 10));
        assertEquals(7, bit.fold(5, 10));
        assertEquals(0, bit.fold(3, 3));
        assertEquals(0, bit.fold(5, 3));
    }

    @Test
    public void testClear() {
        int n = 5;
        LongZAbelianGroupStrategy strategy = new LongZAbelianGroupStrategy();
        LongAbelianGroupBinaryIndexedTree bit = new LongAbelianGroupBinaryIndexedTree(n, strategy);

        bit.add(1, 10);
        bit.add(3, 20);
        assertEquals(30, bit.prefixSum(5));

        bit.clear();
        assertEquals(0, bit.prefixSum(5));
        assertEquals(0, bit.fold(0, 5));
    }
}
