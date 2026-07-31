package library.util.seq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class StaticRangeModeQueryTest {

    @Test
    public void testSimple() {
        int[] a = {1, 2, 1, 2, 3, 2, 1, 1};
        StaticRangeModeQuery rm = new StaticRangeModeQuery(a);

        // [0, 8): {1, 2, 1, 2, 3, 2, 1, 1} -> mode 1, freq 4
        assertArrayEquals(new int[]{1, 4}, rm.query(0, 8));

        // Check frequency for ranges with multiple modes
        int[] res = rm.query(0, 5); // {1, 2, 1, 2, 3} -> freq 2
        org.junit.jupiter.api.Assertions.assertEquals(2, res[1]);

        res = rm.query(4, 6); // {3, 2} -> freq 1
        org.junit.jupiter.api.Assertions.assertEquals(1, res[1]);
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        int n = 500;
        int q = 2000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(50);
        }

        StaticRangeModeQuery rm = new StaticRangeModeQuery(a);

        for (int i = 0; i < q; i++) {
            int l = rnd.nextInt(n);
            int r = rnd.nextInt(n - l) + l + 1;
            int[] res = rm.query(l, r);

            Map<Integer, Integer> freqMap = new HashMap<>();
            int maxFreq = 0;
            for (int k = l; k < r; k++) {
                int f = freqMap.getOrDefault(a[k], 0) + 1;
                freqMap.put(a[k], f);
                maxFreq = Math.max(maxFreq, f);
            }

            org.junit.jupiter.api.Assertions.assertEquals(maxFreq, res[1], "Frequency mismatch at [" + l + ", " + r + ")");
            org.junit.jupiter.api.Assertions.assertEquals(maxFreq, (int)freqMap.get(res[0]), "Mode mismatch at [" + l + ", " + r + ")");
        }
    }

    @Test
    public void testSingleElement() {
        int[] a = {42};
        StaticRangeModeQuery rm = new StaticRangeModeQuery(a);
        assertArrayEquals(new int[]{42, 1}, rm.query(0, 1));
    }

    @Test
    public void testAllSame() {
        int[] a = {7, 7, 7, 7, 7};
        StaticRangeModeQuery rm = new StaticRangeModeQuery(a);
        assertArrayEquals(new int[]{7, 5}, rm.query(0, 5));
        assertArrayEquals(new int[]{7, 2}, rm.query(1, 3));
    }

    @Test
    public void testLargeValues() {
        int[] a = {1000000000, 500000000, 1000000000};
        StaticRangeModeQuery rm = new StaticRangeModeQuery(a);
        assertArrayEquals(new int[]{1000000000, 2}, rm.query(0, 3));
    }
}
