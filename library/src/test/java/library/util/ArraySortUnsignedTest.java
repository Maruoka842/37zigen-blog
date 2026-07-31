package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;

public class ArraySortUnsignedTest {

    @Test
    public void testSortUnsignedInt() {
        int[] a = {0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE, 100, -100};
        // Unsigned order: 0, 1, 100, Integer.MAX_VALUE (2^31-1), Integer.MIN_VALUE (2^31), -100 (unsigned), -1 (all bits 1)
        ArrayUtils.sortUnsigned(a);
        for (int i = 0; i < a.length - 1; i++) {
            assertTrue(Integer.compareUnsigned(a[i], a[i+1]) <= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testSortUnsignedLong() {
        long[] a = {0L, -1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE, 100L, -100L};
        ArrayUtils.sortUnsigned(a);
        for (int i = 0; i < a.length - 1; i++) {
            assertTrue(Long.compareUnsigned(a[i], a[i+1]) <= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testRSortUnsignedInt() {
        int[] a = {0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE, 100, -100};
        ArrayUtils.rsortUnsigned(a);
        for (int i = 0; i < a.length - 1; i++) {
            assertTrue(Integer.compareUnsigned(a[i], a[i+1]) >= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testRSortUnsignedLong() {
        long[] a = {0L, -1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE, 100L, -100L};
        ArrayUtils.rsortUnsigned(a);
        for (int i = 0; i < a.length - 1; i++) {
            assertTrue(Long.compareUnsigned(a[i], a[i+1]) >= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testArgSortUnsignedInt() {
        int[] a = {0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE, 100, -100};
        int[] idx = ArrayUtils.argSortUnsigned(a);
        assertEquals(a.length, idx.length);
        for (int i = 0; i < idx.length - 1; i++) {
            assertTrue(Integer.compareUnsigned(a[idx[i]], a[idx[i+1]]) <= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testArgSortUnsignedLong() {
        long[] a = {0L, -1L, 1L, Long.MAX_VALUE, Long.MIN_VALUE, 100L, -100L};
        int[] idx = ArrayUtils.argSortUnsigned(a);
        assertEquals(a.length, idx.length);
        for (int i = 0; i < idx.length - 1; i++) {
            assertTrue(Long.compareUnsigned(a[idx[i]], a[idx[i+1]]) <= 0, "Failed at index " + i);
        }
    }

    @Test
    public void testRandomInt() {
        Random rnd = new Random();
        int n = 1000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
        int[] b = a.clone();

        ArrayUtils.sortUnsigned(a);

        Integer[] expected = new Integer[n];
        for (int i = 0; i < n; i++) expected[i] = b[i];
        Arrays.sort(expected, Integer::compareUnsigned);

        for (int i = 0; i < n; i++) {
            assertEquals(expected[i].intValue(), a[i]);
        }
    }

    @Test
    public void testRandomLong() {
        Random rnd = new Random();
        int n = 1000;
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextLong();
        long[] b = a.clone();

        ArrayUtils.sortUnsigned(a);

        Long[] expected = new Long[n];
        for (int i = 0; i < n; i++) expected[i] = b[i];
        Arrays.sort(expected, Long::compareUnsigned);

        for (int i = 0; i < n; i++) {
            assertEquals(expected[i].longValue(), a[i]);
        }
    }
}
