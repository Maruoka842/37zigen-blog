package library.util.unionfind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UndoUnionFindTest {

    @Test
    public void testBasic() {
        UndoUnionFind uf = new UndoUnionFind(5);
        assertEquals(5, uf.numberConnectedComponents());

        assertTrue(uf.union(0, 1));
        assertTrue(uf.equiv(0, 1));
        assertEquals(4, uf.numberConnectedComponents());
        assertEquals(2, uf.size(0));

        assertTrue(uf.union(2, 3));
        assertTrue(uf.equiv(2, 3));
        assertFalse(uf.equiv(0, 2));
        assertEquals(3, uf.numberConnectedComponents());

        uf.union(1, 2);
        assertTrue(uf.equiv(0, 3));
        assertEquals(2, uf.numberConnectedComponents());
        assertEquals(4, uf.size(0));

        // Undo last union(1, 2)
        uf.undo();
        assertFalse(uf.equiv(0, 3));
        assertTrue(uf.equiv(0, 1));
        assertTrue(uf.equiv(2, 3));
        assertEquals(3, uf.numberConnectedComponents());

        // Undo union(2, 3)
        uf.undo();
        assertFalse(uf.equiv(2, 3));
        assertTrue(uf.equiv(0, 1));
        assertEquals(4, uf.numberConnectedComponents());

        // Undo union(0, 1)
        uf.undo();
        assertFalse(uf.equiv(0, 1));
        assertEquals(5, uf.numberConnectedComponents());
    }

    @Test
    public void testUnionSame() {
        UndoUnionFind uf = new UndoUnionFind(3);
        assertTrue(uf.union(0, 1));
        assertFalse(uf.union(0, 1)); // Already same
        assertEquals(2, uf.numberConnectedComponents());

        uf.undo(); // Undo the second union (which did nothing)
        assertEquals(2, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));

        uf.undo(); // Undo the first union
        assertEquals(3, uf.numberConnectedComponents());
        assertFalse(uf.equiv(0, 1));
    }

    @Test
    public void testReset() {
        UndoUnionFind uf = new UndoUnionFind(10);
        uf.union(0, 1);
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(0, 2);
        uf.union(0, 4);
        assertEquals(5, uf.numberConnectedComponents());

        uf.reset();
        assertEquals(10, uf.numberConnectedComponents());
        for (int i = 0; i < 10; i++) {
            assertEquals(1, uf.size(i));
            for (int j = i + 1; j < 10; j++) {
                assertFalse(uf.equiv(i, j));
            }
        }
    }

    @Test
    public void testSnapshotRollback() {
        UndoUnionFind uf = new UndoUnionFind(10);
        uf.union(0, 1);
        uf.union(2, 3);
        int snap1 = uf.snapshot();

        uf.union(4, 5);
        uf.union(0, 2);
        int snap2 = uf.snapshot();

        uf.union(0, 4);
        uf.union(6, 7);

        assertEquals(4, uf.numberConnectedComponents());

        uf.rollback(snap2);
        assertEquals(6, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 2));
        assertFalse(uf.equiv(0, 4));

        uf.rollback(snap1);
        assertEquals(8, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));
        assertFalse(uf.equiv(0, 2));

        uf.rollback(0);
        assertEquals(10, uf.numberConnectedComponents());
    }

    @Test
    public void testCopy() {
        UndoUnionFind uf = new UndoUnionFind(5);
        uf.union(0, 1);
        uf.union(2, 3);

        UndoUnionFind copy = uf.copy();

        // Check identical status
        assertEquals(uf.size(), copy.size());
        assertEquals(uf.numberConnectedComponents(), copy.numberConnectedComponents());
        for (int i = 0; i < 5; i++) {
            assertEquals(uf.root(i), copy.root(i));
            assertEquals(uf.size(i), copy.size(i));
            for (int j = 0; j < 5; j++) {
                assertEquals(uf.equiv(i, j), copy.equiv(i, j));
            }
        }

        // Mutate the original
        uf.union(1, 2);
        assertTrue(uf.equiv(0, 3));
        // Copy should remain unchanged
        assertFalse(copy.equiv(0, 3));

        // Mutate the copy
        copy.union(3, 4);
        assertTrue(copy.equiv(2, 4));
        // Original should remain unchanged regarding this mutation
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
        UndoUnionFind uf = new UndoUnionFind(5);
        assertEquals("{0}{1}{2}{3}{4}", uf.toString());

        uf.union(0, 1);
        assertEquals("{0, 1}{2}{3}{4}", uf.toString());

        uf.union(2, 3);
        assertEquals("{0, 1}{2, 3}{4}", uf.toString());

        uf.undo();
        assertEquals("{0, 1}{2}{3}{4}", uf.toString());
    }
}
