package library.util.unionfind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnionFindTest {

    @Test
    public void testCopy() {
        UnionFind uf = new UnionFind(10);
        uf.union(0, 1);
        uf.union(2, 3);
        uf.union(4, 5);
        uf.union(0, 2);

        UnionFind copy = uf.copy();

        // Check consistency
        assertEquals(uf.size(), copy.size());
        assertEquals(uf.numberConnectedComponents(), copy.numberConnectedComponents());
        for (int i = 0; i < 10; i++) {
            assertEquals(uf.root(i), copy.root(i));
            assertEquals(uf.size(i), copy.size(i));
        }

        // Verify independence - modify copy
        copy.union(0, 4);
        assertTrue(copy.equiv(0, 4));
        assertFalse(uf.equiv(0, 4));
        assertEquals(uf.numberConnectedComponents() - 1, copy.numberConnectedComponents());

        // Verify independence - modify original
        uf.union(6, 7);
        assertTrue(uf.equiv(6, 7));
        assertFalse(copy.equiv(6, 7));
    }

    @Test
    public void testCopyEmpty() {
        UnionFind uf = new UnionFind(0);
        UnionFind copy = uf.copy();
        assertEquals(0, copy.size());
        assertEquals(0, copy.numberConnectedComponents());
    }

    @Test
    public void testCopySingle() {
        UnionFind uf = new UnionFind(1);
        UnionFind copy = uf.copy();
        assertEquals(1, copy.size());
        assertEquals(1, copy.numberConnectedComponents());
        assertTrue(copy.isRoot(0));
    }

    @Test
    public void testDump() {
        UnionFind uf = new UnionFind(5);
        uf.union(0, 2);
        uf.union(1, 4);
        System.out.println("Testing dump():");
        uf.dump();
    }

    @Test
    public void testToString() {
        UnionFind uf = new UnionFind(5);
        // Initially, each element is in its own component
        assertEquals("{0}{1}{2}{3}{4}", uf.toString());

        uf.union(0, 2);
        assertEquals("{1}{0, 2}{3}{4}", uf.toString());

        uf.union(1, 4);
        assertEquals("{0, 2}{3}{1, 4}", uf.toString());

        uf.union(2, 4);
        assertEquals("{3}{0, 1, 2, 4}", uf.toString());
    }
}
