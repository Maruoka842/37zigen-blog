package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.fold.StaticRangeInversion;

public class StaticRangeInversionTest {

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        int N = 200;
        int Q = 200;
        int[] a = new int[N];
        for (int i = 0; i < N; i++) {
            a[i] = rnd.nextInt(100);
        }

        StaticRangeInversion sri = new StaticRangeInversion(a);

        for (int i = 0; i < Q; i++) {
            int l = rnd.nextInt(N);
            int r = rnd.nextInt(N + 1);
            if (l > r) {
                int tmp = l;
                l = r;
                r = tmp;
            }
            long expected = naiveInversions(a, l, r);
            long actual = sri.get(l, r);
            assertEquals(expected, actual, "Failed at l=" + l + ", r=" + r);
        }
    }

    @Test
    public void testEmpty() {
        int[] a = new int[0];
        StaticRangeInversion sri = new StaticRangeInversion(a);
        assertEquals(0, sri.get(0, 0));
    }

    @Test
    public void testSingle() {
        int[] a = new int[]{5};
        StaticRangeInversion sri = new StaticRangeInversion(a);
        assertEquals(0, sri.get(0, 1));
        assertEquals(0, sri.get(0, 0));
        assertEquals(0, sri.get(1, 1));
    }

    @Test
    public void testDuplicates() {
        int[] a = new int[]{1, 1, 1, 1, 1};
        StaticRangeInversion sri = new StaticRangeInversion(a);
        assertEquals(0, sri.get(0, 5));
    }

    private long naiveInversions(int[] a, int l, int r) {
        long inv = 0;
        for (int i = l; i < r; i++) {
            for (int j = i + 1; j < r; j++) {
                if (a[i] > a[j]) inv++;
            }
        }
        return inv;
    }
}
