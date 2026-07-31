package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class MathUtilsLinearModLessTest {

    private long[][] naive(long n, long m, long a, long b, long t) {
        long[][] ret = new long[2][];
        ret[0] = new long[2];
        ret[1] = new long[1];
        if (t <= 0) return ret;
        for (long i = 0; i < n; i++) {
            long val = a * i + b;
            long rem = Math.floorMod(val, m);
            if (rem < t) {
                long floor = Math.floorDiv(val, m);
                ret[0][0] += 1;
                ret[1][0] += i;
                ret[0][1] += floor;
            }
        }
        return ret;
    }

    private void assertArrayEquals2D(long[][] expected, long[][] actual) {
        assertEquals(expected.length, actual.length, "Outer array length mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], "Inner array length or value mismatch at index " + i);
        }
    }

    @Test
    public void testBasic() {
        long n = 10, m = 7, a = 3, b = 2, t = 4;
        assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
    }

    @Test
    public void testNegativeA() {
        long n = 10, m = 7, a = -3, b = 2, t = 4;
        assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
    }

    @Test
    public void testNegativeB() {
        long n = 10, m = 7, a = 3, b = -2, t = 4;
        assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
    }

    @Test
    public void testNegativeAB() {
        long n = 10, m = 7, a = -3, b = -2, t = 4;
        assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
    }

    @Test
    public void testEdgeT() {
        long n = 10, m = 7, a = 3, b = 2;
        // t = 0
        assertArrayEquals2D(naive(n, m, a, b, 0), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, 0));
        // t = 7 (m)
        assertArrayEquals2D(naive(n, m, a, b, 7), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, 7));
        // t = 8 (>m)
        assertArrayEquals2D(naive(n, m, a, b, 8), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, 8));
        // t = -1
        assertArrayEquals2D(naive(n, m, a, b, -1), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, -1));
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int i = 0; i < 1000; i++) {
            long n = rnd.nextInt(100) + 1;
            long m = rnd.nextInt(100) + 1;
            long a = rnd.nextInt(200) - 100;
            long b = rnd.nextInt(200) - 100;
            long t = rnd.nextInt((int)m + 2);
            assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
        }
    }

    @Test
    public void testLargeN() {
        // We can't use naive for very large N, but we can check consistency for t=m
        long n = 1_000_000_000L;
        long m = 998244353;
        long a = 123456789;
        long b = 987654321;
        long t = m;

        long[][] res = FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t);

        // When t=m, it should be equivalent to summing i^p floor((ai+b)/m)^q for all 0 <= i < n.
        // We can check if these match the direct floorSum logic if we had it,
        // but here we can at least check if it matches the case withra, rb.

        // Actually, let's just test n=10^5 which naive can handle in reasonable time.
        n = 100_000;
        assertArrayEquals2D(naive(n, m, a, b, t), FloorSum.halfplaneMomentsLinearModLess1(n, m, a, b, t));
    }
}
