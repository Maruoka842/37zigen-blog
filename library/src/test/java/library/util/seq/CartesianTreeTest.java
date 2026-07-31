package library.util.seq;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class CartesianTreeTest {

    @Test
    public void testEmptyAndSingle() {
        // empty array
        CartesianTree ct0 = new CartesianTree(new int[0]);
        assertEquals(0, ct0.n);
        assertEquals(-1, ct0.root);
        assertArrayEquals(new int[0], ct0.preOrder());
        assertArrayEquals(new int[0], ct0.inOrder());
        assertArrayEquals(new int[0], ct0.postOrder());
        assertArrayEquals(new int[0], ct0.depths());
        assertArrayEquals(new int[0], ct0.subtreeSizes());

        // single element
        CartesianTree ct1 = new CartesianTree(new int[]{42});
        assertEquals(1, ct1.n);
        assertEquals(0, ct1.root);
        assertEquals(-1, ct1.parent[0]);
        assertEquals(-1, ct1.lch[0]);
        assertEquals(-1, ct1.rch[0]);
        assertArrayEquals(new int[]{0}, ct1.preOrder());
        assertArrayEquals(new int[]{0}, ct1.inOrder());
        assertArrayEquals(new int[]{0}, ct1.postOrder());
        assertArrayEquals(new int[]{0}, ct1.depths());
        assertArrayEquals(new int[]{1}, ct1.subtreeSizes());
    }

    @Test
    public void testSimpleMinHeap() {
        int[] a = {9, 3, 7, 1, 8, 12, 10, 20, 15, 18, 5};
        CartesianTree ct = new CartesianTree(a, true);

        // Check root: 1 is the minimum value (at index 3)
        assertEquals(3, ct.root);
        assertEquals(-1, ct.parent[3]);

        // Check inOrder returns 0..n-1
        int[] expectedIn = new int[a.length];
        for (int i = 0; i < a.length; i++) expectedIn[i] = i;
        assertArrayEquals(expectedIn, ct.inOrder());

        // Check Heap property
        for (int i = 0; i < a.length; i++) {
            if (ct.lch[i] != -1) {
                assertEquals(i, ct.parent[ct.lch[i]]);
                assertTrue(a[i] <= a[ct.lch[i]]);
            }
            if (ct.rch[i] != -1) {
                assertEquals(i, ct.parent[ct.rch[i]]);
                assertTrue(a[i] <= a[ct.rch[i]]);
            }
        }

        // Check depths and subtree sizes
        int[] d = ct.depths();
        int[] sz = ct.subtreeSizes();
        assertEquals(0, d[ct.root]);
        assertEquals(a.length, sz[ct.root]);

        for (int i = 0; i < a.length; i++) {
            if (ct.parent[i] != -1) {
                assertEquals(d[ct.parent[i]] + 1, d[i]);
            }
        }

        // Output dump to see it compiles/works
        ct.dump();
    }

    @Test
    public void testSimpleMaxHeap() {
        int[] a = {9, 3, 7, 1, 8, 12, 10, 20, 15, 18, 5};
        CartesianTree ct = new CartesianTree(a, false);

        // Check root: 20 is the maximum value (at index 7)
        assertEquals(7, ct.root);
        assertEquals(-1, ct.parent[7]);

        // Check inOrder returns 0..n-1
        int[] expectedIn = new int[a.length];
        for (int i = 0; i < a.length; i++) expectedIn[i] = i;
        assertArrayEquals(expectedIn, ct.inOrder());

        // Check Heap property
        for (int i = 0; i < a.length; i++) {
            if (ct.lch[i] != -1) {
                assertEquals(i, ct.parent[ct.lch[i]]);
                assertTrue(a[i] >= a[ct.lch[i]]);
            }
            if (ct.rch[i] != -1) {
                assertEquals(i, ct.parent[ct.rch[i]]);
                assertTrue(a[i] >= a[ct.rch[i]]);
            }
        }
    }

    @Test
    public void testLongArrayAndDuplicates() {
        long[] a = {5L, 3L, 5L, 2L, 5L, 3L, 5L};
        CartesianTree ct = new CartesianTree(a, true);

        // Root should be 3 (value 2L)
        assertEquals(3, ct.root);

        // Validate structure
        for (int i = 0; i < a.length; i++) {
            if (ct.lch[i] != -1) {
                assertEquals(i, ct.parent[ct.lch[i]]);
                assertTrue(a[i] <= a[ct.lch[i]]);
            }
            if (ct.rch[i] != -1) {
                assertEquals(i, ct.parent[ct.rch[i]]);
                assertTrue(a[i] <= a[ct.rch[i]]);
            }
        }

        // Test inOrder
        int[] expectedIn = {0, 1, 2, 3, 4, 5, 6};
        assertArrayEquals(expectedIn, ct.inOrder());
    }

    @Test
    public void testCustomComparator() {
        // We can sort strings or other values
        String[] a = {"apple", "orange", "banana", "pear", "grape"};
        // Min Cartesian Tree based on string lexicographical order
        CartesianTree ct = new CartesianTree(a.length, (u, v) -> {
            int cmp = a[u].compareTo(a[v]);
            if (cmp != 0) return cmp;
            return Integer.compare(u, v);
        });

        // "apple" is min, at index 0
        assertEquals(0, ct.root);

        for (int i = 0; i < a.length; i++) {
            if (ct.lch[i] != -1) {
                assertEquals(i, ct.parent[ct.lch[i]]);
                assertTrue(a[i].compareTo(a[ct.lch[i]]) <= 0);
            }
            if (ct.rch[i] != -1) {
                assertEquals(i, ct.parent[ct.rch[i]]);
                assertTrue(a[i].compareTo(a[ct.rch[i]]) <= 0);
            }
        }
    }

    @Test
    public void testIntervalAndIntervals() {
        // empty array
        CartesianTree ct0 = new CartesianTree(new int[0]);
        assertNotNull(ct0.intervals());
        assertEquals(0, ct0.intervals().length);
        assertThrows(IndexOutOfBoundsException.class, () -> ct0.interval(0));
        assertThrows(IndexOutOfBoundsException.class, () -> ct0.interval(-1));

        // single element
        CartesianTree ct1 = new CartesianTree(new int[]{42});
        int[][] intervals1 = ct1.intervals();
        assertEquals(1, intervals1.length);
        assertArrayEquals(new int[]{0, 1}, intervals1[0]);
        assertArrayEquals(new int[]{0, 1}, ct1.interval(0));

        // Multiple elements
        int[] a = {9, 3, 7, 1, 8, 12, 10, 20, 15, 18, 5};
        CartesianTree ct = new CartesianTree(a, true);

        // root is 3 (value 1)
        // Its interval should be the entire array [0, 11)
        assertArrayEquals(new int[]{0, 11}, ct.interval(3));

        // Left child of root is 1 (value 3)
        // Subtree elements in inorder: [9, 3, 7] -> index range [0, 3)
        assertArrayEquals(new int[]{0, 3}, ct.interval(1));

        // Right child of root is 10 (value 5)
        // Subtree elements in inorder: [8, 12, 10, 20, 15, 18, 5] -> index range [4, 11)
        assertArrayEquals(new int[]{4, 11}, ct.interval(10));

        // Let's verify the entire intervals() array
        int[][] intervals = ct.intervals();
        assertEquals(a.length, intervals.length);
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(ct.interval(i), intervals[i]);
        }

        // Test with interval cached
        // interval(i) should return from cache
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(intervals[i], ct.interval(i));
        }

        // Test custom comparator / max heap
        CartesianTree ctMax = new CartesianTree(a, false);
        int[][] intervalsMax = ctMax.intervals();
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(ctMax.interval(i), intervalsMax[i]);
            // check that root is indeed [0, 11)
            if (i == ctMax.root) {
                assertArrayEquals(new int[]{0, 11}, intervalsMax[i]);
            }
        }

        // Test exception on out-of-bounds index
        assertThrows(IndexOutOfBoundsException.class, () -> ct.interval(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> ct.interval(a.length));
    }

    @Test
    public void testRandomStress() {
        Random rng = new Random(12345);
        for (int iter = 0; iter < 100; iter++) {
            int n = rng.nextInt(100) + 1;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = rng.nextInt(1000);
            }

            boolean isMin = rng.nextBoolean();
            CartesianTree ct = new CartesianTree(a, isMin);

            // 1. inOrder check
            int[] in = ct.inOrder();
            int[] expectedIn = new int[n];
            for (int i = 0; i < n; i++) expectedIn[i] = i;
            assertArrayEquals(expectedIn, in);

            // 2. parent-child consistency
            int rootCount = 0;
            for (int i = 0; i < n; i++) {
                if (ct.parent[i] == -1) {
                    rootCount++;
                    assertEquals(ct.root, i);
                } else {
                    int p = ct.parent[i];
                    assertTrue(ct.lch[p] == i || ct.rch[p] == i);
                }

                if (ct.lch[i] != -1) {
                    assertEquals(i, ct.parent[ct.lch[i]]);
                }
                if (ct.rch[i] != -1) {
                    assertEquals(i, ct.parent[ct.rch[i]]);
                }
            }
            assertEquals(1, rootCount);

            // 3. Heap Property
            for (int i = 0; i < n; i++) {
                if (ct.parent[i] != -1) {
                    int p = ct.parent[i];
                    if (isMin) {
                        assertTrue(a[p] <= a[i]);
                    } else {
                        assertTrue(a[p] >= a[i]);
                    }
                }
            }

            // 4. Traversal consistency
            int[] pre = ct.preOrder();
            int[] post = ct.postOrder();
            assertEquals(n, pre.length);
            assertEquals(n, post.length);

            // verify pre/post/in have same elements (indices 0..n-1)
            boolean[] visitedPre = new boolean[n];
            boolean[] visitedPost = new boolean[n];
            for (int x : pre) visitedPre[x] = true;
            for (int x : post) visitedPost[x] = true;
            for (int i = 0; i < n; i++) {
                assertTrue(visitedPre[i]);
                assertTrue(visitedPost[i]);
            }

            // 5. depths and subtree sizes
            int[] d = ct.depths();
            int[] sz = ct.subtreeSizes();
            assertEquals(0, d[ct.root]);
            assertEquals(n, sz[ct.root]);
            for (int i = 0; i < n; i++) {
                if (ct.lch[i] != -1) {
                    assertEquals(d[i] + 1, d[ct.lch[i]]);
                }
                if (ct.rch[i] != -1) {
                    assertEquals(d[i] + 1, d[ct.rch[i]]);
                }
                int expectedSz = 1;
                if (ct.lch[i] != -1) expectedSz += sz[ct.lch[i]];
                if (ct.rch[i] != -1) expectedSz += sz[ct.rch[i]];
                assertEquals(expectedSz, sz[i]);
            }

            // 6. Intervals check
            int[][] intervals = ct.intervals();
            assertEquals(n, intervals.length);
            for (int i = 0; i < n; i++) {
                int[] iv = ct.interval(i);
                assertArrayEquals(intervals[i], iv);
                int l = iv[0];
                int r = iv[1];
                assertTrue(l >= 0 && r <= n && l <= i && i < r);
                assertEquals(sz[i], r - l);
            }
        }
    }
}
