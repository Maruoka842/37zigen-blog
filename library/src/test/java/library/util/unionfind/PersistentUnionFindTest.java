package library.util.unionfind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class PersistentUnionFindTest {

    @Test
    public void testBasic() {
        PersistentUnionFind uf0 = new PersistentUnionFind(5);
        assertEquals(5, uf0.size());
        assertEquals(5, uf0.numberConnectedComponents());
        for (int i = 0; i < 5; i++) {
            assertEquals(i, uf0.find(i));
            assertEquals(1, uf0.size(i));
            assertTrue(uf0.isRoot(i));
        }

        PersistentUnionFind uf1 = uf0.union(0, 1);
        // uf0 should remain unchanged
        assertEquals(5, uf0.numberConnectedComponents());
        assertEquals(0, uf0.find(0));
        assertEquals(1, uf0.find(1));

        // uf1 should have 0 and 1 merged
        assertEquals(4, uf1.numberConnectedComponents());
        assertTrue(uf1.equiv(0, 1));
        assertEquals(uf1.find(0), uf1.find(1));
        assertEquals(2, uf1.size(0));
        assertEquals(2, uf1.size(1));

        PersistentUnionFind uf2 = uf1.union(2, 3);
        assertEquals(3, uf2.numberConnectedComponents());
        assertTrue(uf2.equiv(2, 3));
        assertFalse(uf2.equiv(0, 2));

        PersistentUnionFind uf3 = uf2.union(0, 2);
        assertEquals(2, uf3.numberConnectedComponents());
        assertTrue(uf3.equiv(0, 3));
        assertEquals(4, uf3.size(0));
        assertEquals(1, uf3.size(4));
    }

    @Test
    public void testBranching() {
        PersistentUnionFind uf0 = new PersistentUnionFind(10);
        // Branch A: Merging 0-1, 2-3, then 0-2
        PersistentUnionFind ufA1 = uf0.union(0, 1);
        PersistentUnionFind ufA2 = ufA1.union(2, 3);
        PersistentUnionFind ufA3 = ufA2.union(0, 2);

        // Branch B: Merging 4-5, 6-7, then 4-6
        PersistentUnionFind ufB1 = uf0.union(4, 5);
        PersistentUnionFind ufB2 = ufB1.union(6, 7);
        PersistentUnionFind ufB3 = ufB2.union(4, 6);

        // Verify Branch A
        assertTrue(ufA3.equiv(0, 3));
        assertFalse(ufA3.equiv(4, 5));
        assertEquals(4, ufA3.size(0));
        assertEquals(1, ufA3.size(4));

        // Verify Branch B
        assertTrue(ufB3.equiv(4, 7));
        assertFalse(ufB3.equiv(0, 1));
        assertEquals(4, ufB3.size(4));
        assertEquals(1, ufB3.size(0));

        // Since they are persistent, ufA3 should still be in state A, and ufB3 in state B
        assertFalse(ufA3.equiv(4, 7));
        assertFalse(ufB3.equiv(0, 3));
    }

    @Test
    public void testBoundary() {
        PersistentUnionFind uf0 = new PersistentUnionFind(0);
        assertEquals(0, uf0.size());
        assertEquals(0, uf0.numberConnectedComponents());
        assertArrayEquals(new int[0], uf0.roots());

        PersistentUnionFind uf1 = new PersistentUnionFind(1);
        assertEquals(1, uf1.size());
        assertEquals(1, uf1.numberConnectedComponents());
        assertEquals(0, uf1.find(0));
        assertTrue(uf1.isRoot(0));
        assertArrayEquals(new int[]{0}, uf1.roots());

        // Exception check
        assertThrows(IndexOutOfBoundsException.class, () -> uf1.find(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> uf1.find(1));
        assertThrows(IndexOutOfBoundsException.class, () -> uf1.union(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new PersistentUnionFind(-5));
    }

    @Test
    public void testRootsAndCopyAndDump() {
        PersistentUnionFind uf = new PersistentUnionFind(5);
        uf = uf.union(0, 1).union(2, 3);
        int[] roots = uf.roots();
        assertEquals(3, roots.length);

        PersistentUnionFind copied = uf.copy();
        assertEquals(uf.numberConnectedComponents(), copied.numberConnectedComponents());
        assertTrue(copied.equiv(0, 1));
        assertTrue(copied.equiv(2, 3));
        assertFalse(copied.equiv(1, 2));

        // Just check if dump runs without throwing exceptions
        uf.dump();
    }

    @Test
    public void testToString() {
        PersistentUnionFind uf = new PersistentUnionFind(5);
        assertEquals("{0}{1}{2}{3}{4}", uf.toString());

        uf = uf.union(0, 1);
        assertEquals("{0, 1}{2}{3}{4}", uf.toString());

        uf = uf.union(2, 3);
        assertEquals("{0, 1}{2, 3}{4}", uf.toString());
    }

    @Test
    public void testStressWithExactHistory() {
        int n = 500;
        int queries = 500;
        PersistentUnionFind[] versions = new PersistentUnionFind[queries + 1];
        versions[0] = new PersistentUnionFind(n);

        int[] prevVers = new int[queries + 1];
        int[] us = new int[queries + 1];
        int[] vs = new int[queries + 1];

        Random rng = new Random(1337);

        for (int i = 1; i <= queries; i++) {
            int prev = rng.nextInt(i);
            int u = rng.nextInt(n);
            int v = rng.nextInt(n);

            prevVers[i] = prev;
            us[i] = u;
            vs[i] = v;

            versions[i] = versions[prev].union(u, v);
        }

        // Validate 50 random versions
        for (int i = 0; i < 50; i++) {
            int target = rng.nextInt(queries + 1);

            // Reconstruct the state using standard UnionFind with same merges
            java.util.List<Integer> ops = new java.util.ArrayList<>();
            int curr = target;
            while (curr > 0) {
                ops.add(curr);
                curr = prevVers[curr];
            }
            java.util.Collections.reverse(ops);

            UnionFind ref = new UnionFind(n);
            for (int op : ops) {
                ref.union(us[op], vs[op]);
            }

            for (int j = 0; j < n; j += 5) {
                assertEquals(ref.size(j), versions[target].size(j));
                for (int k = 0; k < n; k += 5) {
                    assertEquals(ref.equiv(j, k), versions[target].equiv(j, k));
                }
            }
            assertEquals(ref.numberConnectedComponents(), versions[target].numberConnectedComponents());
        }
    }
}
