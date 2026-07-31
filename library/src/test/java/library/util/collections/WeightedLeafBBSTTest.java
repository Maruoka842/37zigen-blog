package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import library.util.algebra.strategy.longs.LongMonoidStrategy;

public class WeightedLeafBBSTTest {

    @Test
    public void testRepeatedBasic() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2); // Sequence: [10, 20]
        assertEquals(2, bbst.size(root));

        // k = 0
        int r0 = bbst.repeat(root, 0);
        assertEquals(0, bbst.size(r0));

        // k = 1
        int r1 = bbst.repeat(root, 1);
        assertEquals(2, bbst.size(r1));
        assertEquals(10, bbst.getValue(r1, 0));
        assertEquals(20, bbst.getValue(r1, 1));

        // k = 3 -> [10, 20, 10, 20, 10, 20]
        int r3 = bbst.repeat(root, 3);
        assertEquals(6, bbst.size(r3));
        assertEquals(10, bbst.getValue(r3, 0));
        assertEquals(20, bbst.getValue(r3, 1));
        assertEquals(10, bbst.getValue(r3, 2));
        assertEquals(20, bbst.getValue(r3, 3));
        assertEquals(10, bbst.getValue(r3, 4));
        assertEquals(20, bbst.getValue(r3, 5));
    }

    @Test
    public void testRepeatedExceptions() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2); // size = 2

        // Negative k
        assertThrows(IllegalArgumentException.class, () -> bbst.repeat(root, -1));

        // Overflow k
        assertThrows(ArithmeticException.class, () -> bbst.repeat(root, Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> bbst.repeat(root, Long.MAX_VALUE / 2 + 1));
        // No overflow since k * size <= Long.MAX_VALUE
        assertDoesNotThrow(() -> bbst.repeat(root, Long.MAX_VALUE / 2));

        // When u is empty, size is 0, so no overflow should happen even with massive k
        assertDoesNotThrow(() -> bbst.repeat(0, Long.MAX_VALUE));
    }

    @Test
    public void testSplitBasic() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int leaf3 = bbst.newLeaf(30);
        int leaf4 = bbst.newLeaf(40);
        int leaf5 = bbst.newLeaf(50);

        int root = bbst.merge(bbst.merge(leaf1, leaf2), bbst.merge(bbst.merge(leaf3, leaf4), leaf5)); // [10, 20, 30, 40, 50]
        assertEquals(5, bbst.size(root));

        // split at k = 2
        int[] res2 = bbst.split(root, 2);
        assertEquals(2, bbst.size(res2[0]));
        assertEquals(3, bbst.size(res2[1]));
        assertEquals(10, bbst.getValue(res2[0], 0));
        assertEquals(20, bbst.getValue(res2[0], 1));
        assertEquals(30, bbst.getValue(res2[1], 0));
        assertEquals(40, bbst.getValue(res2[1], 1));
        assertEquals(50, bbst.getValue(res2[1], 2));

        // split at k = 0
        int[] res0 = bbst.split(root, 0);
        assertEquals(0, bbst.size(res0[0]));
        assertEquals(5, bbst.size(res0[1]));
        assertEquals(10, bbst.getValue(res0[1], 0));

        // split at k = 5
        int[] res5 = bbst.split(root, 5);
        assertEquals(5, bbst.size(res5[0]));
        assertEquals(0, bbst.size(res5[1]));
        assertEquals(10, bbst.getValue(res5[0], 0));
    }

    @Test
    public void testSplitOverloadsAndExceptions() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2); // [10, 20], size = 2

        // Check passing int as parameter (auto-widened to long)
        int[] resInt = bbst.split(root, 1);
        assertEquals(1, bbst.size(resInt[0]));
        assertEquals(1, bbst.size(resInt[1]));
        assertEquals(10, bbst.getValue(resInt[0], 0));
        assertEquals(20, bbst.getValue(resInt[1], 0));

        // Out of bounds exception tests
        assertThrows(IllegalArgumentException.class, () -> bbst.split(root, -1));
        assertThrows(IllegalArgumentException.class, () -> bbst.split(root, 3));
    }

    @Test
    public void testSliceBasic() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int leaf3 = bbst.newLeaf(30);
        int leaf4 = bbst.newLeaf(40);
        int leaf5 = bbst.newLeaf(50);

        int root = bbst.merge(bbst.merge(leaf1, leaf2), bbst.merge(bbst.merge(leaf3, leaf4), leaf5)); // [10, 20, 30, 40, 50]
        assertEquals(5, bbst.size(root));

        // Slice [1, 4) -> [20, 30, 40]
        int s14 = bbst.slice(root, 1, 4);
        assertEquals(3, bbst.size(s14));
        assertEquals(20, bbst.getValue(s14, 0));
        assertEquals(30, bbst.getValue(s14, 1));
        assertEquals(40, bbst.getValue(s14, 2));

        // Slice [0, 5) -> entire tree [10, 20, 30, 40, 50]
        int s05 = bbst.slice(root, 0, 5);
        assertEquals(5, bbst.size(s05));
        assertEquals(10, bbst.getValue(s05, 0));

        // Slice [2, 2) -> empty slice []
        int s22 = bbst.slice(root, 2, 2);
        assertEquals(0, bbst.size(s22));
    }

    @Test
    public void testSliceExceptions() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2); // [10, 20], size = 2

        // Out of bounds exception tests
        assertThrows(IllegalArgumentException.class, () -> bbst.slice(root, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> bbst.slice(root, 1, 3));
        assertThrows(IllegalArgumentException.class, () -> bbst.slice(root, 2, 1)); // l > r
    }

    @Test
    public void testEraseBasic() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int leaf3 = bbst.newLeaf(30);
        int leaf4 = bbst.newLeaf(40);
        int leaf5 = bbst.newLeaf(50);

        int root = bbst.merge(bbst.merge(leaf1, leaf2), bbst.merge(bbst.merge(leaf3, leaf4), leaf5)); // [10, 20, 30, 40, 50]
        assertEquals(5, bbst.size(root));

        // Erase [1, 4) -> [10, 50]
        int e14 = bbst.erase(root, 1, 4);
        assertEquals(2, bbst.size(e14));
        assertEquals(10, bbst.getValue(e14, 0));
        assertEquals(50, bbst.getValue(e14, 1));

        // Erase [0, 5) -> empty tree []
        int e05 = bbst.erase(root, 0, 5);
        assertEquals(0, bbst.size(e05));

        // Erase [2, 2) -> entire tree [10, 20, 30, 40, 50]
        int e22 = bbst.erase(root, 2, 2);
        assertEquals(5, bbst.size(e22));
        assertEquals(10, bbst.getValue(e22, 0));
        assertEquals(20, bbst.getValue(e22, 1));
        assertEquals(30, bbst.getValue(e22, 2));
        assertEquals(40, bbst.getValue(e22, 3));
        assertEquals(50, bbst.getValue(e22, 4));
    }

    @Test
    public void testEraseExceptions() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2); // [10, 20], size = 2

        // Out of bounds exception tests
        assertThrows(IllegalArgumentException.class, () -> bbst.erase(root, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> bbst.erase(root, 1, 3));
        assertThrows(IllegalArgumentException.class, () -> bbst.erase(root, 2, 1)); // l > r
    }

    @Test
    public void testWithLongMonoidStrategy() {
        LongMonoidStrategy st = new LongMonoidStrategy() {
            @Override
            public long identity() {
                return 0;
            }
            @Override
            public long mul(long a, long b) {
                return a + b;
            }
        };

        WeightedLeafBBST bbst = new WeightedLeafBBST(100, st);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int leaf3 = bbst.newLeaf(30);

        int root = bbst.merge(bbst.merge(leaf1, leaf2), leaf3); // [10, 20, 30]

        assertEquals(3, bbst.size(root));
        assertEquals(10, bbst.getValue(root, 0));
        assertEquals(20, bbst.getValue(root, 1));
        assertEquals(30, bbst.getValue(root, 2));

        // Test folding
        assertEquals(60, bbst.fold(0, 3, root));
        assertEquals(30, bbst.fold(0, 2, root));
        assertEquals(50, bbst.fold(1, 3, root));
        assertEquals(20, bbst.fold(1, 2, root));
        assertEquals(0, bbst.fold(2, 2, root));
        assertEquals(0, bbst.fold(0, 0, root));
        assertEquals(0, bbst.fold(1, 1, root));
    }

    @Test
    public void testFoldUnsupportedWhenStrategyNull() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(100);
        int leaf1 = bbst.newLeaf(10);
        int leaf2 = bbst.newLeaf(20);
        int root = bbst.merge(leaf1, leaf2);

        assertThrows(UnsupportedOperationException.class, () -> bbst.fold(0, 2, root));
    }
}
