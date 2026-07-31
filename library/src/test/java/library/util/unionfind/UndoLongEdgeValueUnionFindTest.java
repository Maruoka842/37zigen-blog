package library.util.unionfind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import library.util.algebra.strategy.longs.LongGroupStrategy;

public class UndoLongEdgeValueUnionFindTest {

    private static class AdditiveGroup implements LongGroupStrategy {
        @Override public long identity() { return 0; }
        @Override public long mul(long a, long b) { return a + b; }
        @Override public long inverse(long a) { return -a; }
    }

    @Test
    public void testBasic() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(5, new AdditiveGroup());
        assertEquals(5, uf.numberConnectedComponents());

        // 0 --(10)--> 1  => getValue(0) = getValue(1) + 10
        assertTrue(uf.union(0, 1, 10));
        assertTrue(uf.equiv(0, 1));
        assertEquals(10, uf.getValue(0) - uf.getValue(1));
        assertEquals(4, uf.numberConnectedComponents());

        // 2 --(20)--> 3  => getValue(2) = getValue(3) + 20
        assertTrue(uf.union(2, 3, 20));
        assertTrue(uf.equiv(2, 3));
        assertEquals(20, uf.getValue(2) - uf.getValue(3));
        assertEquals(3, uf.numberConnectedComponents());

        // 1 --(30)--> 2  => getValue(1) = getValue(2) + 30
        assertTrue(uf.union(1, 2, 30));
        assertTrue(uf.equiv(0, 3));
        // getValue(0) = getValue(1) + 10 = (getValue(2) + 30) + 10 = (getValue(3) + 20) + 40 = getValue(3) + 60
        assertEquals(60, uf.getValue(0) - uf.getValue(3));
        assertEquals(2, uf.numberConnectedComponents());

        // Undo last union(1, 2)
        uf.undo();
        assertFalse(uf.equiv(0, 3));
        assertTrue(uf.equiv(0, 1));
        assertTrue(uf.equiv(2, 3));
        assertEquals(10, uf.getValue(0) - uf.getValue(1));
        assertEquals(20, uf.getValue(2) - uf.getValue(3));
        assertEquals(3, uf.numberConnectedComponents());

        // Undo union(2, 3)
        uf.undo();
        assertFalse(uf.equiv(2, 3));
        assertTrue(uf.equiv(0, 1));
        assertEquals(10, uf.getValue(0) - uf.getValue(1));
        assertEquals(4, uf.numberConnectedComponents());

        // Undo union(0, 1)
        uf.undo();
        assertFalse(uf.equiv(0, 1));
        assertEquals(5, uf.numberConnectedComponents());
    }

    @Test
    public void testConsistency() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(3, new AdditiveGroup());
        assertTrue(uf.union(0, 1, 10));
        assertTrue(uf.union(1, 2, 20));

        // Already connected: 0 --(10)--> 1 --(20)--> 2  => 0 --(30)--> 2
        assertTrue(uf.union(0, 2, 30)); // Consistent
        assertFalse(uf.union(0, 2, 40)); // Inconsistent

        assertEquals(1, uf.numberConnectedComponents());

        uf.undo(); // Undo inconsistent
        assertEquals(1, uf.numberConnectedComponents());
        uf.undo(); // Undo consistent
        assertEquals(1, uf.numberConnectedComponents());
        uf.undo(); // Undo (1, 2)
        assertEquals(2, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));
        assertFalse(uf.equiv(1, 2));
    }

    @Test
    public void testSnapshotRollback() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(10, new AdditiveGroup());
        uf.union(0, 1, 1);
        uf.union(2, 3, 2);
        int snap1 = uf.snapshot();

        uf.union(4, 5, 3);
        uf.union(0, 2, 4);
        int snap2 = uf.snapshot();

        uf.union(0, 4, 5);
        uf.union(6, 7, 6);

        assertEquals(4, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 5));

        uf.rollback(snap2);
        assertEquals(6, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 2));
        assertFalse(uf.equiv(0, 4));
        assertEquals(4, uf.getValue(0) - uf.getValue(2));

        uf.rollback(snap1);
        assertEquals(8, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));
        assertFalse(uf.equiv(0, 2));
        assertEquals(1, uf.getValue(0) - uf.getValue(1));

        uf.rollback(0);
        assertEquals(10, uf.numberConnectedComponents());
        for (int i = 0; i < 10; i++) {
            assertEquals(0, uf.getValue(i));
        }
    }

    @Test
    public void testReset() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(5, new AdditiveGroup());
        uf.union(0, 1, 10);
        uf.union(2, 3, 20);
        uf.union(0, 2, 30);

        uf.reset();
        assertEquals(5, uf.numberConnectedComponents());
        for (int i = 0; i < 5; i++) {
            assertTrue(uf.isRoot(i));
            assertEquals(0, uf.getValue(i));
        }
    }

    @Test
    public void testCopy() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(5, new AdditiveGroup());
        uf.union(0, 1, 10);
        uf.union(2, 3, 20);

        UndoLongEdgeValueUnionFind copy = uf.copy();

        // Check identical status
        assertEquals(uf.size(), copy.size());
        assertEquals(uf.numberConnectedComponents(), copy.numberConnectedComponents());
        for (int i = 0; i < 5; i++) {
            assertEquals(uf.root(i), copy.root(i));
            assertEquals(uf.size(i), copy.size(i));
            assertEquals(uf.getValue(i), copy.getValue(i));
            for (int j = 0; j < 5; j++) {
                assertEquals(uf.equiv(i, j), copy.equiv(i, j));
            }
        }

        // Mutate original
        uf.union(1, 2, 30);
        assertTrue(uf.equiv(0, 3));
        assertEquals(60, uf.getValue(0) - uf.getValue(3));
        // Copy must remain unchanged
        assertFalse(copy.equiv(0, 3));

        // Mutate copy
        copy.union(3, 4, 40);
        assertTrue(copy.equiv(2, 4));
        assertEquals(60, copy.getValue(2) - copy.getValue(4));
        // Original must remain unchanged regarding this mutation
        assertFalse(uf.equiv(2, 4));

        // Undo on copy
        copy.undo();
        assertFalse(copy.equiv(2, 4));

        // Undo on original
        uf.undo();
        assertFalse(uf.equiv(0, 3));
    }

    @Test
    public void testToString() {
        UndoLongEdgeValueUnionFind uf = new UndoLongEdgeValueUnionFind(5, new AdditiveGroup());
        assertEquals("{0(0)}{1(0)}{2(0)}{3(0)}{4(0)}", uf.toString());

        uf.union(0, 1, 10);
        assertEquals("{0(0), 1(-10)}{2(0)}{3(0)}{4(0)}", uf.toString());

        uf.union(2, 3, 20);
        assertEquals("{0(0), 1(-10)}{2(0), 3(-20)}{4(0)}", uf.toString());

        uf.undo();
        assertEquals("{0(0), 1(-10)}{2(0)}{3(0)}{4(0)}", uf.toString());
    }
}
