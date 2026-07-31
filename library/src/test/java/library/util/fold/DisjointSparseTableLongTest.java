package library.util.fold;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import java.util.function.LongBinaryOperator;

public class DisjointSparseTableLongTest {

    @Test
    public void testEmptyArray() {
        long[] v = new long[0];
        DisjointSparseTableLong dst = new DisjointSparseTableLong(v, Long::sum);
        dst.dump();
    }

    @Test
    public void testSingleElement() {
        long[] v = { 42 };
        DisjointSparseTableLong dst = new DisjointSparseTableLong(v, Long::sum);
        assertEquals(42, dst.fold(0, 1));
    }

    @Test
    public void testBasicSum() {
        long[] v = { 10, 20, 30, 40, 50 };
        DisjointSparseTableLong dst = new DisjointSparseTableLong(v, Long::sum);
        assertEquals(10, dst.fold(0, 1));
        assertEquals(30, dst.fold(0, 2));
        assertEquals(60, dst.fold(0, 3));
        assertEquals(100, dst.fold(0, 4));
        assertEquals(150, dst.fold(0, 5));
        assertEquals(50, dst.fold(1, 3));
        assertEquals(120, dst.fold(2, 5));
    }

    @Test
    public void testBasicMin() {
        long[] v = { 5, 2, 8, 1, 9 };
        DisjointSparseTableLong dst = new DisjointSparseTableLong(v, Math::min);
        assertEquals(2, dst.fold(0, 3));
        assertEquals(1, dst.fold(0, 4));
        assertEquals(1, dst.fold(1, 5));
        assertEquals(8, dst.fold(2, 3));
    }

    @Test
    public void testStress() {
        Random rnd = new Random(12345);
        int n = 300;
        long[] v = new long[n];
        for (int i = 0; i < n; i++) {
            v[i] = rnd.nextLong() % 1000000;
        }

        LongBinaryOperator[] ops = {
            Long::sum,
            Math::min,
            Math::max,
            (a, b) -> a ^ b
        };

        for (LongBinaryOperator op : ops) {
            DisjointSparseTableLong dst = new DisjointSparseTableLong(v, op);
            for (int l = 0; l < n; l++) {
                for (int r = l + 1; r <= n; r++) {
                    long expected = naiveFold(v, op, l, r);
                    long actual = dst.fold(l, r);
                    assertEquals(expected, actual, "Failed for range [" + l + ", " + r + ") and op");
                }
            }
        }
    }

    private long naiveFold(long[] v, LongBinaryOperator op, int l, int r) {
        long res = v[l];
        for (int i = l + 1; i < r; i++) {
            res = op.applyAsLong(res, v[i]);
        }
        return res;
    }
}
