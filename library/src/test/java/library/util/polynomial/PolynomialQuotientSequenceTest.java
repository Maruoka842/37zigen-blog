package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PolynomialQuotientSequenceTest {
    private final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    @Test
    void testNaiveVsFastSmall() {
        testNaiveVsFast(5, 100);
    }

    @Test
    void testNaiveVsFastMedium() {
        testNaiveVsFast(20, 50);
    }

    @Test
    void testNaiveVsFastLarge() {
        testNaiveVsFast(100, 20);
    }

    @Test
    void testNaiveVsFastExtraLarge() {
        testNaiveVsFast(1000, 5);
    }

    @Test
    void testBenchmark() {
        int n = 10000;
        Random rnd = new Random(42);
        long[] a = randomPoly(n, rnd);
        long[] b = randomPoly(n - 1, rnd);

        long start = System.currentTimeMillis();
        List<long[]> fast = poly.quotientSequenceFast(a, b);
        long fastTime = System.currentTimeMillis() - start;

        System.out.println("Fast time (N=" + n + "): " + fastTime + "ms");

        if (n <= 2000) {
            start = System.currentTimeMillis();
            List<long[]> naive = poly.quotientSequenceNaive(a, b);
            long naiveTime = System.currentTimeMillis() - start;
            System.out.println("Naive time (N=" + n + "): " + naiveTime + "ms");
            assertEquals(naive.size(), fast.size());
        }
    }

    @Test
    void testEdgeCases() {
        testSpecific(new long[]{1, 2, 3}, new long[]{2});
        testSpecific(new long[]{0, 0, 1}, new long[]{0, 1}); // x^2, x
        testSpecific(new long[]{2, 3, 1}, new long[]{3, 4, 1}); // (x+1)(x+2), (x+1)(x+3)
        testSpecific(new long[]{1, 1}, new long[]{1, 2, 1});
    }

    @Test
    void testStaticApi() {
        long[] a = {1, 2, 3, 4};
        long[] b = {1, 2};
        List<long[]> naive = PolynomialFp.quotientSequenceNaive(a, b);
        List<long[]> fast = PolynomialFp.quotientSequenceFast(a, b);
        assertEquals(naive.size(), fast.size());
        for (int i = 0; i < naive.size(); i++) {
            assertArrayEquals(PolynomialFp.resize(naive.get(i)), PolynomialFp.resize(fast.get(i)));
        }
    }

    private void testNaiveVsFast(int maxDeg, int trials) {
        Random rnd = new Random(42);
        for (int i = 0; i < trials; i++) {
            int degA = rnd.nextInt(maxDeg + 1);
            int degB = rnd.nextInt(maxDeg + 1);
            long[] a = randomPoly(degA, rnd);
            long[] b = randomPoly(degB, rnd);
            if (poly.isZero(b)) continue;
            testSpecific(a, b);
        }
    }

    private void testSpecific(long[] a, long[] b) {
        List<long[]> naive = poly.quotientSequenceNaive(a, b);
        List<long[]> fast = poly.quotientSequenceFast(a, b);

        assertEquals(naive.size(), fast.size(), "Size mismatch for a=" + poly.resize(a).length + " b=" + poly.resize(b).length);
        for (int i = 0; i < naive.size(); i++) {
            assertArrayEquals(poly.resize(naive.get(i)), poly.resize(fast.get(i)), "Mismatch at index " + i);
        }

        Pair p = replay(a, b, fast);
        assertTrue(poly.isZero(p.second), "Remainder must be zero after replay");

        long[] gcdNaive = poly.monic(poly.gcdNaive(a, b));
        long[] gcdReplayed = poly.monic(p.first);
        assertArrayEquals(gcdNaive, gcdReplayed, "GCD mismatch after replay");

        if (poly.deg(a) >= poly.deg(b) && poly.deg(a) > 0) {
            PolynomialFpDynamic.HalfGcdResultWithQuotients h = poly.halfGcdWithQuotients(a, b);
            long[][] cd = h.apply(a, b);
            assertArrayEquals(poly.resize(cd[0]), poly.resize(h.c), "c mismatch");
            assertArrayEquals(poly.resize(cd[1]), poly.resize(h.d), "d mismatch");

            Pair p2 = replay(a, b, h.quotients);
            assertArrayEquals(poly.resize(p2.first), poly.resize(h.c), "replayed c mismatch");
            assertArrayEquals(poly.resize(p2.second), poly.resize(h.d), "replayed d mismatch");
        }
    }

    private long[] randomPoly(int deg, Random rnd) {
        if (deg < 0) return new long[0];
        long[] res = new long[deg + 1];
        for (int i = 0; i <= deg; i++) {
            res[i] = rnd.nextInt((int) poly.mod);
        }
        if (res[deg] == 0) res[deg] = 1;
        return res;
    }

    private static class Pair {
        long[] first, second;
        Pair(long[] f, long[] s) { first = f; second = s; }
    }

    private Pair replay(long[] a, long[] b, List<long[]> qs) {
        for (long[] q : qs) {
            long[] r = poly.sub(a, poly.mul(q, b));
            a = b;
            b = r;
        }
        return new Pair(a, b);
    }
}
