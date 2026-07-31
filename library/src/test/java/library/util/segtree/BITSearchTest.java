package library.util.segtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import library.util.algebra.strategy.longs.LongAbelianGroupStrategy;
import library.util.algebra.strategy.longs.LongZStrategy;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;

public class BITSearchTest {

    static class LongZAbelianGroupStrategy implements LongAbelianGroupStrategy {
        LongZStrategy z = new LongZStrategy();
        @Override public long identity() { return z.zero(); }
        @Override public long mul(long a, long b) { return z.add(a, b); }
        @Override public long inverse(long a) { return z.neg(a); }
    }

    @Test
    public void testIntSumBIT() {
        int n = 10;
        IntSumBinaryIndexedTree bit = new IntSumBinaryIndexedTree(n);
        int[] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < n; i++) bit.add(i, a[i]);

        // maximalRight
        for (int l = 0; l <= n; l++) {
            for (int limit = -5; limit <= 60; limit++) {
                final int finalLimit = limit;
                IntPredicate f = v -> v <= finalLimit;
                int expected = l;
                int sum = 0;
                for (int r = l + 1; r <= n; r++) {
                    sum += a[r - 1];
                    if (f.test(sum)) expected = r;
                    else break;
                }
                assertEquals(expected, bit.maximalRight(l, f), "maximalRight l=" + l + " limit=" + limit);
            }
        }

        // minimalLeft
        for (int r = 0; r < n; r++) {
            for (int limit = -5; limit <= 60; limit++) {
                final int finalLimit = limit;
                IntPredicate f = v -> v <= finalLimit;
                int expected = -1;
                int sum = 0;
                for (int l = r; l >= 0; l--) {
                    sum += a[l];
                    if (!f.test(sum)) {
                        expected = l;
                        break;
                    }
                }
                assertEquals(expected, bit.minimalLeft(r, f), "minimalLeft r=" + r + " limit=" + limit);
            }
        }
    }

    @Test
    public void testLongAbelianGroupBIT() {
        int n = 10;
        LongZAbelianGroupStrategy strategy = new LongZAbelianGroupStrategy();
        LongAbelianGroupBinaryIndexedTree bit = new LongAbelianGroupBinaryIndexedTree(n, strategy);
        long[] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < n; i++) bit.add(i, a[i]);

        // maximalRight
        for (int l = 0; l <= n; l++) {
            for (int limit = -5; limit <= 60; limit++) {
                final int finalLimit = limit;
                LongPredicate f = v -> v <= finalLimit;
                int expected = l;
                long sum = 0;
                for (int r = l + 1; r <= n; r++) {
                    sum += a[r - 1];
                    if (f.test(sum)) expected = r;
                    else break;
                }
                assertEquals(expected, bit.maximalRight(l, f), "maximalRight l=" + l + " limit=" + limit);
            }
        }

        // minimalLeft
        for (int r = 0; r < n; r++) {
            for (int limit = -5; limit <= 60; limit++) {
                final int finalLimit = limit;
                LongPredicate f = v -> v <= finalLimit;
                int expected = -1;
                long sum = 0;
                for (int l = r; l >= 0; l--) {
                    sum += a[l];
                    if (!f.test(sum)) {
                        expected = l;
                        break;
                    }
                }
                assertEquals(expected, bit.minimalLeft(r, f), "minimalLeft r=" + r + " limit=" + limit);
            }
        }
    }
}
