package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

public class TopKTest {

    @Test
    public void testTopKInt() {
        int n = 100;
        int[] a = new int[n];
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1000);

        int[] sorted = a.clone();
        Arrays.sort(sorted);
        ArrayUtils.reverse(sorted);

        int k = 9; // Index for top 10 elements
        int[] b = a.clone();
        ArrayUtils.topK(b, k);

        int[] topKResult = Arrays.copyOf(b, k + 1);
        Arrays.sort(topKResult);
        ArrayUtils.reverse(topKResult);

        for (int i = 0; i <= k; i++) {
            assertEquals(sorted[i], topKResult[i]);
        }
    }

    @Test
    public void testTopKLong() {
        int n = 100;
        long[] a = new long[n];
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) a[i] = rnd.nextLong(1000);

        long[] sorted = a.clone();
        Arrays.sort(sorted);
        ArrayUtils.reverse(sorted);

        int k = 19; // Index for top 20 elements
        long[] b = a.clone();
        ArrayUtils.topK(b, k);

        long[] topKResult = Arrays.copyOf(b, k + 1);
        Arrays.sort(topKResult);
        ArrayUtils.reverse(topKResult);

        for (int i = 0; i <= k; i++) {
            assertEquals(sorted[i], topKResult[i]);
        }
    }

    @Test
    public void testNthElementInt() {
        int n = 100;
        int[] a = new int[n];
        Random rnd = new Random(42);
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1000);

        int[] sorted = a.clone();
        Arrays.sort(sorted);

        int k = 50;
        int[] b = a.clone();
        ArrayUtils.nthElement(b, k);

        assertEquals(sorted[k], b[k]);
        for (int i = 0; i < k; i++) {
            assertTrue(b[i] <= b[k]);
        }
        for (int i = k + 1; i < n; i++) {
            assertTrue(b[i] >= b[k]);
        }
    }

    @Test
    public void testTopKEdgeCases() {
        int[] a = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};

        // k = 0 (Ensures a[0] is the maximum)
        int[] b0 = a.clone();
        ArrayUtils.topK(b0, 0);
        assertEquals(9, b0[0]);

        // k = n-1
        int[] bn = a.clone();
        ArrayUtils.topK(bn, a.length - 1);
        // Should contain all elements, but 9 must be among them (trivial)
        // More importantly, we check if it doesn't crash and behaves consistently.

        // k = 1 (top 2 elements)
        int[] b1 = a.clone();
        ArrayUtils.topK(b1, 1);
        int[] top2 = {b1[0], b1[1]};
        Arrays.sort(top2);
        assertArrayEquals(new int[]{6, 9}, top2);
    }

    @Test
    public void testTopKDuplicates() {
        int[] a = {5, 5, 5, 5, 5, 1, 1, 1};
        int k = 2; // top 3 elements
        int[] b = a.clone();
        ArrayUtils.topK(b, k);
        for (int i = 0; i <= k; i++) {
            assertEquals(5, b[i]);
        }

        k = 5; // top 6 elements
        b = a.clone();
        ArrayUtils.topK(b, k);
        int fives = 0;
        int ones = 0;
        for (int i = 0; i <= k; i++) {
            if (b[i] == 5) fives++;
            else if (b[i] == 1) ones++;
        }
        assertEquals(5, fives);
        assertEquals(1, ones);
    }
}
