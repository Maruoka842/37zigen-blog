package library.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class IntervalsTest {

    @Test
    public void testInclusionMinimalBasic() {
        // [1, 5), [2, 4), [3, 6] -> [2, 4) and [3, 6) are inclusion minimal.
        // [1, 5) contains [2, 4), so [1, 5) is discarded.
        ArrayList<long[]> list = new ArrayList<>();
        list.add(new long[]{1, 5});
        list.add(new long[]{2, 4});
        list.add(new long[]{3, 6});

        ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
        assertEquals(2, res.size());
        assertArrayEquals(new long[]{2, 4}, res.get(0));
        assertArrayEquals(new long[]{3, 6}, res.get(1));
    }

    @Test
    public void testInclusionMinimalWithDuplicates() {
        ArrayList<long[]> list = new ArrayList<>();
        list.add(new long[]{1, 3});
        list.add(new long[]{1, 3});
        list.add(new long[]{2, 5});

        ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
        assertEquals(2, res.size());
        assertArrayEquals(new long[]{1, 3}, res.get(0));
        assertArrayEquals(new long[]{2, 5}, res.get(1));
    }

    @Test
    public void testInclusionMinimalWithEmptyAndInvalid() {
        ArrayList<long[]> list = new ArrayList<>();
        list.add(new long[]{1, 3});
        list.add(new long[]{2, 2}); // empty
        list.add(new long[]{5, 4}); // invalid
        list.add(new long[]{2, 5});

        ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
        assertEquals(2, res.size());
        assertArrayEquals(new long[]{1, 3}, res.get(0));
        assertArrayEquals(new long[]{2, 5}, res.get(1));
    }

    @Test
    public void testInclusionMinimalOverloads() {
        long[] L = {1, 2, 3};
        long[] R = {5, 4, 6};

        ArrayList<long[]> resLong = Intervals.inclusionMinimalIntervals(L, R);
        assertEquals(2, resLong.size());
        assertArrayEquals(new long[]{2, 4}, resLong.get(0));
        assertArrayEquals(new long[]{3, 6}, resLong.get(1));

        int[] L_int = {1, 2, 3};
        int[] R_int = {5, 4, 6};

        ArrayList<long[]> resInt = Intervals.inclusionMinimalIntervals(L_int, R_int);
        assertEquals(2, resInt.size());
        assertArrayEquals(new long[]{2, 4}, resInt.get(0));
        assertArrayEquals(new long[]{3, 6}, resInt.get(1));
    }

    @Test
    public void testEdgeCases() {
        // Null or empty inputs
        assertTrue(Intervals.inclusionMinimalIntervals((ArrayList<long[]>) null).isEmpty());
        assertTrue(Intervals.inclusionMinimalIntervals(new ArrayList<>()).isEmpty());
        assertTrue(Intervals.inclusionMinimalIntervals((long[]) null, (long[]) null).isEmpty());
        assertTrue(Intervals.inclusionMinimalIntervals((int[]) null, (int[]) null).isEmpty());

        // L and R length mismatch
        assertThrows(IllegalArgumentException.class, () -> {
            Intervals.inclusionMinimalIntervals(new long[]{1}, new long[]{2, 3});
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Intervals.inclusionMinimalIntervals(new int[]{1}, new int[]{2, 3});
        });

        // Infinite boundaries (Long.MAX_VALUE)
        ArrayList<long[]> list = new ArrayList<>();
        list.add(new long[]{1, Long.MAX_VALUE});
        list.add(new long[]{2, Long.MAX_VALUE});
        list.add(new long[]{2, 5});

        ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
        // [2, 5) and [2, Long.MAX_VALUE) are identical on left, [2, 5) is smaller, so [2, Long.MAX_VALUE) is discarded.
        // [1, Long.MAX_VALUE) contains both, so it is discarded.
        // Thus, only [2, 5) remains.
        assertEquals(1, res.size());
        assertArrayEquals(new long[]{2, 5}, res.get(0));
    }

    @Test
    public void testNegativeCoordinates() {
        ArrayList<long[]> list = new ArrayList<>();
        list.add(new long[]{-10, -5});
        list.add(new long[]{-8, -6});
        list.add(new long[]{-7, -2});

        ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
        // [-10, -5) contains [-8, -6), so [-10, -5) is discarded.
        // [-8, -6) does not contain [-7, -2) and vice versa.
        // So remaining: [-8, -6) and [-7, -2)
        assertEquals(2, res.size());
        assertArrayEquals(new long[]{-8, -6}, res.get(0));
        assertArrayEquals(new long[]{-7, -2}, res.get(1));
    }

    private List<long[]> bruteForceInclusionMinimal(List<long[]> list) {
        List<long[]> valid = new ArrayList<>();
        for (long[] interval : list) {
            if (interval != null && interval.length >= 2 && interval[0] < interval[1]) {
                valid.add(interval);
            }
        }
        List<long[]> unique = new ArrayList<>();
        for (long[] iv : valid) {
            boolean dup = false;
            for (long[] u : unique) {
                if (u[0] == iv[0] && u[1] == iv[1]) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                unique.add(iv);
            }
        }

        List<long[]> res = new ArrayList<>();
        for (long[] iv1 : unique) {
            boolean hasSubset = false;
            for (long[] iv2 : unique) {
                if (iv1 == iv2) continue;
                if (iv1[0] <= iv2[0] && iv2[1] <= iv1[1]) {
                    hasSubset = true;
                    break;
                }
            }
            if (!hasSubset) {
                res.add(iv1);
            }
        }
        res.sort((x, y) -> {
            int cmp = Long.compare(x[0], y[0]);
            if (cmp != 0) return cmp;
            return Long.compare(x[1], y[1]);
        });
        return res;
    }

    @Test
    public void testRandomStress() {
        Random rand = new Random(42);
        int numTests = 200;
        for (int t = 0; t < numTests; t++) {
            int n = rand.nextInt(50) + 1;
            ArrayList<long[]> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                long l = rand.nextInt(20) - 10;
                long r = l + rand.nextInt(20) - 5; // can be invalid or empty
                list.add(new long[]{l, r});
            }

            ArrayList<long[]> res = Intervals.inclusionMinimalIntervals(list);
            List<long[]> expected = bruteForceInclusionMinimal(list);

            assertEquals(expected.size(), res.size());
            for (int i = 0; i < expected.size(); i++) {
                assertArrayEquals(expected.get(i), res.get(i));
            }
        }
    }
}
