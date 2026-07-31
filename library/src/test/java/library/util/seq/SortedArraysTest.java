package library.util.seq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

public class SortedArraysTest {

    @Test
    public void testLowerInt() {
        int[] a = {1, 3, 5, 7, 9};
        assertEquals(-1, SortedArrays.lower(a, 1));
        assertEquals(0, SortedArrays.lower(a, 2));
        assertEquals(0, SortedArrays.lower(a, 3));
        assertEquals(1, SortedArrays.lower(a, 4));
        assertEquals(4, SortedArrays.lower(a, 10));
    }

    @Test
    public void testLowerLong() {
        long[] a = {1L, 3L, 5L, 7L, 9L};
        assertEquals(-1, SortedArrays.lower(a, 1L));
        assertEquals(0, SortedArrays.lower(a, 2L));
        assertEquals(0, SortedArrays.lower(a, 3L));
        assertEquals(1, SortedArrays.lower(a, 4L));
        assertEquals(4, SortedArrays.lower(a, 10L));
    }

    @Test
    public void testLowerComparable() {
        Integer[] a = {1, 3, 5, 7, 9};
        assertEquals(-1, SortedArrays.lower(a, 1));
        assertEquals(0, SortedArrays.lower(a, 2));
        assertEquals(4, SortedArrays.lower(a, 10));
    }

    @Test
    public void testLowerList() {
        List<Integer> a = Arrays.asList(1, 3, 5, 7, 9);
        assertEquals(-1, SortedArrays.lower(a, 1));
        assertEquals(0, SortedArrays.lower(a, 2));
        assertEquals(4, SortedArrays.lower(a, 10));
    }

    @Test
    public void testChar2D() {
        char[][] a = {{'a', 'a'}, {'a', 'c'}, {'c', 'a'}};
        assertEquals(0, SortedArrays.floor(a, new char[]{'a', 'b'}));
        assertEquals(-1, SortedArrays.lower(a, new char[]{'a', 'a'}));
        assertEquals(1, SortedArrays.lower(a, new char[]{'b', 'a'}));
        assertEquals(1, SortedArrays.ceil(a, new char[]{'a', 'c'}));
        assertEquals(1, SortedArrays.ceil(a, new char[]{'a', 'b'}));
        assertEquals(2, SortedArrays.higher(a, new char[]{'a', 'c'}));
        assertEquals(2, SortedArrays.higher(a, new char[]{'b', 'a'}));
    }

    @Test
    public void testInt2D() {
        int[][] a = {{1, 1}, {1, 3}, {3, 1}};
        assertEquals(0, SortedArrays.floor(a, new int[]{1, 2}));
        assertEquals(-1, SortedArrays.lower(a, new int[]{1, 1}));
        assertEquals(1, SortedArrays.lower(a, new int[]{2, 1}));
        assertEquals(1, SortedArrays.ceil(a, new int[]{1, 3}));
        assertEquals(1, SortedArrays.ceil(a, new int[]{1, 2}));
        assertEquals(2, SortedArrays.higher(a, new int[]{1, 3}));
        assertEquals(2, SortedArrays.higher(a, new int[]{2, 1}));
    }

    @Test
    public void testLong2D() {
        long[][] a = {{1L, 1L}, {1L, 3L}, {3L, 1L}};
        assertEquals(0, SortedArrays.floor(a, new long[]{1L, 2L}));
        assertEquals(-1, SortedArrays.lower(a, new long[]{1L, 1L}));
        assertEquals(1, SortedArrays.lower(a, new long[]{2L, 1L}));
        assertEquals(1, SortedArrays.ceil(a, new long[]{1L, 3L}));
        assertEquals(1, SortedArrays.ceil(a, new long[]{1L, 2L}));
        assertEquals(2, SortedArrays.higher(a, new long[]{1L, 3L}));
        assertEquals(2, SortedArrays.higher(a, new long[]{2L, 1L}));
    }
}
