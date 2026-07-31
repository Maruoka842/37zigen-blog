package library.util.collections;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SquareRootDecompositionBBSTTest {

    @Test
    public void stressTest() {
        Random rnd = new Random(42);
        int n = 100;
        long[] initial = new long[n];
        for (int i = 0; i < n; i++) initial[i] = rnd.nextInt(100) - 50;

        SquareRootDecompositionBBST sd = new SquareRootDecompositionBBST(initial);
        long[] naive = initial.clone();

        int numOps = 1000;
        for (int op = 0; op < numOps; op++) {
            int type = rnd.nextInt(18);
            int l = rnd.nextInt(n);
            int r = rnd.nextInt(n - l) + l + 1; // [l, r)

            switch (type) {
                case 0: // get
                    int idx = rnd.nextInt(n);
                    assertEquals(naive[idx], sd.get(idx));
                    break;
                case 1: // set
                    idx = rnd.nextInt(n);
                    long val = rnd.nextInt(100) - 50;
                    naive[idx] = val;
                    sd.set(idx, val);
                    break;
                case 2: // add
                    idx = rnd.nextInt(n);
                    val = rnd.nextInt(100) - 50;
                    naive[idx] += val;
                    sd.add(idx, val);
                    break;
                case 3: // rangeAdd
                    val = rnd.nextInt(100) - 50;
                    for (int i = l; i < r; i++) naive[i] += val;
                    sd.rangeAdd(l, r, val);
                    break;
                case 4: // rangeSum
                    long sum = 0;
                    for (int i = l; i < r; i++) sum += naive[i];
                    assertEquals(sum, sd.rangeSum(l, r));
                    break;
                case 5: // rangeMin
                    long min = Long.MAX_VALUE;
                    for (int i = l; i < r; i++) min = Math.min(min, naive[i]);
                    assertEquals(min, sd.rangeMin(l, r));
                    break;
                case 6: // rangeMax
                    long max = Long.MIN_VALUE;
                    for (int i = l; i < r; i++) max = Math.max(max, naive[i]);
                    assertEquals(max, sd.rangeMax(l, r));
                    break;
                case 7: // rangeCount
                    long lower = rnd.nextInt(100) - 50;
                    long upper = lower + rnd.nextInt(50);
                    long count = 0;
                    for (int i = l; i < r; i++) if (naive[i] >= lower && naive[i] < upper) count++;
                    assertEquals(count, sd.rangeCount(l, r, lower, upper));
                    break;
                case 8: // rangeRank
                    val = rnd.nextInt(100) - 50;
                    count = 0;
                    for (int i = l; i < r; i++) if (naive[i] < val) count++;
                    assertEquals(count, sd.rangeRank(l, r, val));
                    break;
                case 9: // rangeFrequency
                    val = naive[l + rnd.nextInt(r - l)];
                    count = 0;
                    for (int i = l; i < r; i++) if (naive[i] == val) count++;
                    assertEquals(count, sd.rangeFrequency(l, r, val));
                    break;
                case 10: // rangeLower
                    val = rnd.nextInt(100) - 50;
                    Long res = null;
                    for (int i = l; i < r; i++) if (naive[i] < val) res = (res == null) ? naive[i] : Math.max(res, naive[i]);
                    assertEquals(res, sd.rangeLower(l, r, val));
                    break;
                case 11: // rangeFloor
                    val = rnd.nextInt(100) - 50;
                    res = null;
                    for (int i = l; i < r; i++) if (naive[i] <= val) res = (res == null) ? naive[i] : Math.max(res, naive[i]);
                    assertEquals(res, sd.rangeFloor(l, r, val));
                    break;
                case 12: // rangeCeil
                    val = rnd.nextInt(100) - 50;
                    res = null;
                    for (int i = l; i < r; i++) if (naive[i] >= val) res = (res == null) ? naive[i] : Math.min(res, naive[i]);
                    assertEquals(res, sd.rangeCeil(l, r, val));
                    break;
                case 13: // rangeHigher
                    val = rnd.nextInt(100) - 50;
                    res = null;
                    for (int i = l; i < r; i++) if (naive[i] > val) res = (res == null) ? naive[i] : Math.min(res, naive[i]);
                    assertEquals(res, sd.rangeHigher(l, r, val));
                    break;
                case 14: // rangeKthSmallest
                    int k = rnd.nextInt(r - l);
                    long[] sub = new long[r - l];
                    for (int i = 0; i < r - l; i++) sub[i] = naive[l + i];
                    Arrays.sort(sub);
                    assertEquals(sub[k], sd.rangeKthSmallest(l, r, k));
                    break;
                case 15: // findFirst
                    val = naive[l + rnd.nextInt(r - l)];
                    int first = -1;
                    for (int i = l; i < r; i++) if (naive[i] == val) { first = i; break; }
                    assertEquals(first, sd.findFirst(l, r, val));
                    break;
                case 16: // findLast
                    val = naive[l + rnd.nextInt(r - l)];
                    int last = -1;
                    for (int i = r - 1; i >= l; i--) if (naive[i] == val) { last = i; break; }
                    assertEquals(last, sd.findLast(l, r, val));
                    break;
                case 17: // min/max index
                    min = Long.MAX_VALUE;
                    for (int i = l; i < r; i++) min = Math.min(min, naive[i]);
                    first = -1;
                    for (int i = l; i < r; i++) if (naive[i] == min) { first = i; break; }
                    assertEquals(first, sd.rangeMinIndex(l, r));

                    max = Long.MIN_VALUE;
                    for (int i = l; i < r; i++) max = Math.max(max, naive[i]);
                    first = -1;
                    for (int i = l; i < r; i++) if (naive[i] == max) { first = i; break; }
                    assertEquals(first, sd.rangeMaxIndex(l, r));

                    last = -1;
                    for (int i = r - 1; i >= l; i--) if (naive[i] == min) { last = i; break; }
                    assertEquals(last, sd.rangeMinLastIndex(l, r));

                    last = -1;
                    for (int i = r - 1; i >= l; i--) if (naive[i] == max) { last = i; break; }
                    assertEquals(last, sd.rangeMaxLastIndex(l, r));
                    break;
            }
        }
    }
}
