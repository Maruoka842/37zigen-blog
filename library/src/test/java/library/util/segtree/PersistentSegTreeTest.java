package library.util.segtree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import library.util.algebra.strategy.monoid.MonoidStrategy;

public class PersistentSegTreeTest {

    static class IntAdd implements MonoidStrategy<Integer> {
        @Override public Integer identity() { return 0; }
        @Override public Integer mul(Integer a, Integer b) { return a + b; }
    }

    static class LongAdd implements MonoidStrategy<Long> {
        @Override public Long identity() { return 0L; }
        @Override public Long mul(Long a, Long b) { return a + b; }
    }

    @Test
    public void testBasic() {
        Integer[] a = {1, 2, 3, 4, 5};
        PersistentSegTree<Integer> pst = new PersistentSegTree<>(a, new IntAdd());
        PersistentSegTree.Root r0 = pst.getRoot();

        assertEquals(15, pst.allProd(r0));
        assertEquals(3, pst.get(r0, 2));
        assertEquals(6, pst.prod(r0, 0, 3));

        PersistentSegTree.Root r1 = pst.set(r0, 2, 10);
        assertEquals(3, pst.get(r0, 2)); // original unchanged
        assertEquals(10, pst.get(r1, 2));
        assertEquals(15, pst.allProd(r0));
        assertEquals(22, pst.allProd(r1));
        assertEquals(13, pst.prod(r1, 0, 3));
    }

    @Test
    public void testMaxRight() {
        Integer[] a = {1, 2, 3, 4, 5};
        PersistentSegTree<Integer> pst = new PersistentSegTree<>(a, new IntAdd());
        PersistentSegTree.Root r0 = pst.getRoot();

        // prod(0, r) <= 6: r=0(0), 1(1), 2(3), 3(6), 4(10) -> max r is 3
        assertEquals(3, pst.maxRight(r0, 0, x -> x <= 6));
        assertEquals(5, pst.maxRight(r0, 0, x -> x <= 100));
        assertEquals(0, pst.maxRight(r0, 0, x -> x < 1));

        // prod(2, r) <= 7: 2(3), 3(3+4=7), 4(3+4+5=12) -> max r is 4
        assertEquals(4, pst.maxRight(r0, 2, x -> x <= 7));
    }

    @Test
    public void testMinLeft() {
        Integer[] a = {1, 2, 3, 4, 5};
        PersistentSegTree<Integer> pst = new PersistentSegTree<>(a, new IntAdd());
        PersistentSegTree.Root r0 = pst.getRoot();

        // prod(l, 5) <= 9: 4(5), 3(4+5=9), 2(3+4+5=12) -> min l is 3
        assertEquals(3, pst.minLeft(r0, 5, x -> x <= 9));
        assertEquals(0, pst.minLeft(r0, 5, x -> x <= 100));
        assertEquals(5, pst.minLeft(r0, 5, x -> x < 5));
    }

    @Test
    public void testPersistence() {
        PersistentSegTree<Long> pst = new PersistentSegTree<>(10, new LongAdd());
        PersistentSegTree.Root[] roots = new PersistentSegTree.Root[11];
        roots[0] = pst.getRoot();
        for (int i = 0; i < 10; i++) {
            roots[i + 1] = pst.set(roots[i], i, (long) (i + 1));
        }

        for (int i = 0; i <= 10; i++) {
            long expectedSum = (long) i * (i + 1) / 2;
            assertEquals(expectedSum, pst.allProd(roots[i]), "Root " + i + " failed");
            for (int j = 0; j < 10; j++) {
                long expectedVal = (j < i) ? (j + 1) : 0L;
                assertEquals(expectedVal, pst.get(roots[i], j), "Root " + i + " index " + j + " failed");
            }
        }
    }
}
