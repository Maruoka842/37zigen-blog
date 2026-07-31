package library.util.segtree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class WeightedSumTest {

    @Test
    public void testRangeAddWeightedSum() {
        int n = 100;
        long[] weights = new long[n];
        Random random = new Random(42);
        for (int i = 0; i < n; i++) weights[i] = random.nextInt(10);

        RangeAddWeightedSum seg = SegTreeFactory.add_weightedSum(weights);
        long[] a = new long[n];

        for (int q = 0; q < 200; q++) {
            int type = random.nextInt(2);
            int l = random.nextInt(n);
            int r = random.nextInt(n);
            if (l > r) { int t = l; l = r; r = t; }
            r++; // [l, r)

            if (type == 0) { // add
                long val = random.nextInt(10) - 5;
                seg.add(l, r, val);
                for (int i = l; i < r; i++) a[i] += val;
            } else { // sum
                long expected = 0;
                for (int i = l; i < r; i++) expected += a[i] * weights[i];
                assertEquals(expected, seg.sum(l, r), "Query " + q + " failed");
            }
        }
    }

    @Test
    public void testRangeAssignWeightedSum() {
        int n = 100;
        long[] weights = new long[n];
        Random random = new Random(43);
        for (int i = 0; i < n; i++) weights[i] = random.nextInt(10);

        RangeAssignWeightedSum seg = SegTreeFactory.assign_weightedSum(weights);
        long[] a = new long[n];

        for (int q = 0; q < 200; q++) {
            int type = random.nextInt(2);
            int l = random.nextInt(n);
            int r = random.nextInt(n);
            if (l > r) { int t = l; l = r; r = t; }
            r++; // [l, r)

            if (type == 0) { // assign
                long val = random.nextInt(10);
                seg.assign(l, r, val);
                for (int i = l; i < r; i++) a[i] = val;
            } else { // sum
                long expected = 0;
                for (int i = l; i < r; i++) expected += a[i] * weights[i];
                assertEquals(expected, seg.sum(l, r), "Query " + q + " failed");
            }
        }
    }

    @Test
    public void testPointOps() {
        long[] weights = {1, 2, 3, 4, 5};
        RangeAddWeightedSum segAdd = SegTreeFactory.add_weightedSum(weights);
        RangeAssignWeightedSum segAssign = SegTreeFactory.assign_weightedSum(weights);

        segAdd.set(2, 10); // A[2] = 10, A = [0, 0, 10, 0, 0]
        assertEquals(10 * 3, segAdd.get(2));
        assertEquals(10 * 3, segAdd.sum(0, 5));

        segAssign.set(2, 10);
        assertEquals(10 * 3, segAssign.get(2));
        assertEquals(10 * 3, segAssign.sum(0, 5));
    }
}
