package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.fold.StaticRangeDistinctCount;

public class StaticRangeDistinctCountTest {

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        int N = 200;
        int Q = 200;
        int[] a = new int[N];
        for (int i = 0; i < N; i++) {
            a[i] = rnd.nextInt(50);
        }

        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);

        for (int i = 0; i < Q; i++) {
            int l = rnd.nextInt(N);
            int r = rnd.nextInt(N + 1);
            if (l > r) {
                int tmp = l;
                l = r;
                r = tmp;
            }
            int expected = naiveCount(a, l, r);
            int actual = rdc.count(l, r);
            assertEquals(expected, actual, "Failed at l=" + l + ", r=" + r);
        }
    }

    @Test
    public void testRandomLong() {
        Random rnd = new Random(43);
        int N = 200;
        int Q = 200;
        long[] a = new long[N];
        for (int i = 0; i < N; i++) {
            a[i] = rnd.nextLong(1000000000000L);
        }

        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);

        for (int i = 0; i < Q; i++) {
            int l = rnd.nextInt(N);
            int r = rnd.nextInt(N + 1);
            if (l > r) {
                int tmp = l;
                l = r;
                r = tmp;
            }
            int expected = naiveCount(a, l, r);
            int actual = rdc.count(l, r);
            assertEquals(expected, actual, "Failed at l=" + l + ", r=" + r);
        }
    }

    @Test
    public void testEmpty() {
        int[] a = new int[0];
        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);
        assertEquals(0, rdc.count(0, 0));
    }

    @Test
    public void testSingle() {
        int[] a = new int[]{5};
        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);
        assertEquals(1, rdc.count(0, 1));
        assertEquals(0, rdc.count(0, 0));
        assertEquals(0, rdc.count(1, 1));
    }

    @Test
    public void testAllIdentical() {
        int[] a = new int[]{1, 1, 1, 1, 1};
        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);
        assertEquals(1, rdc.count(0, 5));
        assertEquals(1, rdc.count(1, 4));
        assertEquals(0, rdc.count(2, 2));
    }

    @Test
    public void testAllDistinct() {
        int[] a = new int[]{1, 2, 3, 4, 5};
        StaticRangeDistinctCount rdc = new StaticRangeDistinctCount(a);
        assertEquals(5, rdc.count(0, 5));
        assertEquals(3, rdc.count(1, 4));
    }

    private int naiveCount(int[] a, int l, int r) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = l; i < r; i++) {
            set.add(a[i]);
        }
        return set.size();
    }

    private int naiveCount(long[] a, int l, int r) {
        HashSet<Long> set = new HashSet<>();
        for (int i = l; i < r; i++) {
            set.add(a[i]);
        }
        return set.size();
    }
}
