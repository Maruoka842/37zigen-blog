package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;

public class ItertoolsTest {

    @Test
    public void testMultiplicativePartitions() {
        // n = 12
        // Partitions: [12], [2, 6], [2, 2, 3], [3, 4]
        List<long[]> result = new ArrayList<>();
        for (long[] p : Itertools.multiplicativePartitions(12)) {
            result.add(p.clone());
        }
        assertEquals(4, result.size());

        List<String> sortedResults = result.stream()
                .map(Arrays::toString)
                .sorted()
                .collect(Collectors.toList());

        assertTrue(sortedResults.contains(Arrays.toString(new long[]{12})));
        assertTrue(sortedResults.contains(Arrays.toString(new long[]{2, 6})));
        assertTrue(sortedResults.contains(Arrays.toString(new long[]{2, 2, 3})));
        assertTrue(sortedResults.contains(Arrays.toString(new long[]{3, 4})));
    }

    @Test
    public void testDistinctMultiplicativePartitions() {
        // n = 12
        // Distinct partitions: [12], [2, 6], [3, 4]
        // [2, 2, 3] is NOT distinct because 2 is repeated.
        List<long[]> result = new ArrayList<>();
        for (long[] p : Itertools.distinctMultiplicativePartitions(12)) {
            result.add(p.clone());
        }
        assertEquals(3, result.size());

        List<String> sortedResults = result.stream()
                .map(Arrays::toString)
                .sorted()
                .collect(Collectors.toList());

        assertTrue(sortedResults.contains(Arrays.toString(new long[]{12})));
        assertTrue(sortedResults.contains(Arrays.toString(new long[]{2, 6})));
        assertTrue(sortedResults.contains(Arrays.toString(new long[]{3, 4})));
        assertFalse(sortedResults.contains(Arrays.toString(new long[]{2, 2, 3})));
    }

    @Test
    public void testMultiplicativePartitionsSmall() {
        // n = 1
        int count1 = 0;
        for (long[] p : Itertools.multiplicativePartitions(1)) {
            assertArrayEquals(new long[]{1}, p);
            count1++;
        }
        assertEquals(1, count1);

        // n = 2
        int count2 = 0;
        for (long[] p : Itertools.multiplicativePartitions(2)) {
            assertArrayEquals(new long[]{2}, p);
            count2++;
        }
        assertEquals(1, count2);
    }

    @Test
    public void testMultiplicativePartitionsError() {
        assertThrows(IllegalArgumentException.class, () -> Itertools.multiplicativePartitions(0));
        assertThrows(IllegalArgumentException.class, () -> Itertools.multiplicativePartitions(-1));
    }

    @Test
    public void testMultiplicativePartitions30() {
        // n = 30
        // [30], [2, 15], [2, 3, 5], [3, 10], [5, 6]
        List<long[]> result = new ArrayList<>();
        for (long[] p : Itertools.multiplicativePartitions(30)) {
            result.add(p.clone());
        }
        assertEquals(5, result.size());
    }

    @Test
    public void testMultiplicativePartitions16() {
        // n = 16
        // [16], [2, 8], [2, 2, 4], [2, 2, 2, 2], [4, 4]
        List<long[]> result = new ArrayList<>();
        for (long[] p : Itertools.multiplicativePartitions(16)) {
            result.add(p.clone());
        }
        assertEquals(5, result.size());

        // distinct n = 16
        // [16], [2, 8]
        List<long[]> resultDistinct = new ArrayList<>();
        for (long[] p : Itertools.distinctMultiplicativePartitions(16)) {
            resultDistinct.add(p.clone());
        }
        assertEquals(2, resultDistinct.size());
    }

    @Test
    public void testGroupByLong() {
        // Empty array
        long[] empty = {};
        assertArrayEquals(new int[0][], Itertools.groupBy(empty));

        // Single element
        long[] single = {100L};
        int[][] resSingle = Itertools.groupBy(single);
        assertEquals(1, resSingle.length);
        assertArrayEquals(new int[]{0}, resSingle[0]);

        // Multiple groups
        long[] a = {1L, 1L, 2L, 3L, 3L, 3L, 2L};
        int[][] resA = Itertools.groupBy(a);
        assertEquals(4, resA.length);
        assertArrayEquals(new int[]{0, 1}, resA[0]);
        assertArrayEquals(new int[]{2}, resA[1]);
        assertArrayEquals(new int[]{3, 4, 5}, resA[2]);
        assertArrayEquals(new int[]{6}, resA[3]);

        // Custom predicate: groups elements within difference of 1
        long[] b = {1L, 2L, 4L, 5L, 7L};
        int[][] resB = Itertools.groupBy(b, (u, v) -> Math.abs(u - v) <= 1);
        assertEquals(3, resB.length);
        assertArrayEquals(new int[]{0, 1}, resB[0]);
        assertArrayEquals(new int[]{2, 3}, resB[1]);
        assertArrayEquals(new int[]{4}, resB[2]);
    }

    @Test
    public void testCombinationsWithReplacement() {
        // n = 3, k = 2
        // Combinations with replacement: [0,0], [0,1], [0,2], [1,1], [1,2], [2,2]
        List<int[]> result = new ArrayList<>();
        List<int[]> sharedRefList = new ArrayList<>();
        for (int[] c : Itertools.combinationsWithReplacement(3, 2)) {
            result.add(c.clone());
            sharedRefList.add(c);
        }

        assertEquals(6, result.size());
        assertArrayEquals(new int[]{0, 0}, result.get(0));
        assertArrayEquals(new int[]{0, 1}, result.get(1));
        assertArrayEquals(new int[]{0, 2}, result.get(2));
        assertArrayEquals(new int[]{1, 1}, result.get(3));
        assertArrayEquals(new int[]{1, 2}, result.get(4));
        assertArrayEquals(new int[]{2, 2}, result.get(5));

        // Since the iterator updates the array in-place, all elements in sharedRefList
        // refer to the same array instance, which ultimately has the last state.
        for (int[] c : sharedRefList) {
            assertSame(sharedRefList.get(0), c);
            assertArrayEquals(new int[]{2, 2}, c);
        }

        // n = 4, k = 1
        List<int[]> result2 = new ArrayList<>();
        for (int[] c : Itertools.combinationsWithReplacement(4, 1)) {
            result2.add(c.clone());
        }
        assertEquals(4, result2.size());
        assertArrayEquals(new int[]{0}, result2.get(0));
        assertArrayEquals(new int[]{1}, result2.get(1));
        assertArrayEquals(new int[]{2}, result2.get(2));
        assertArrayEquals(new int[]{3}, result2.get(3));

        // k = 0
        List<int[]> resultEmpty = new ArrayList<>();
        for (int[] c : Itertools.combinationsWithReplacement(3, 0)) {
            resultEmpty.add(c.clone());
        }
        assertEquals(1, resultEmpty.size());
        assertEquals(0, resultEmpty.get(0).length);
    }
}
