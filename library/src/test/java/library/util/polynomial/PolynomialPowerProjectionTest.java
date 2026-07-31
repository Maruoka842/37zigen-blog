package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class PolynomialPowerProjectionTest {

    @Test
    void testEdgeAndValidationCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        long[] g = {1, 2, 3};
        long[] f = {0, 1, 2};

        // m <= 0 should return empty array
        assertArrayEquals(new long[0], poly.powerProjection(g, f, 0));
        assertArrayEquals(new long[0], poly.powerProjection(g, f, -5));
        assertArrayEquals(new long[0], poly.powerProjectionNaive(g, f, 0));
        assertArrayEquals(new long[0], poly.powerProjectionNaive(g, f, -5));

        // f[0] != 0 should throw IllegalArgumentException
        long[] f_invalid = {1, 1, 2};
        assertThrows(IllegalArgumentException.class, () -> {
            poly.powerProjection(g, f_invalid, 3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.powerProjectionNaive(g, f_invalid, 3);
        });

        // g.length == 0 should return zero-filled array of length m
        assertArrayEquals(new long[]{0, 0, 0}, poly.powerProjection(new long[0], f, 3));
        assertArrayEquals(new long[]{0, 0, 0}, poly.powerProjectionNaive(new long[0], f, 3));
    }

    @Test
    void testSmallExactHandCalculated() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // g = [1, 2], f = [0, 1] (i.e., f(x) = x)
        // n = ceiling_pow2(2) = 2. We extract [x^1].
        // i = 0: [x^1] (1 + 2x) = 2
        // i = 1: [x^1] ((1 + 2x) * x) = [x^1] (x + 2x^2) = 1
        // i = 2: [x^1] ((1 + 2x) * x^2) = [x^1] (x^2 + 2x^3) = 0
        // Expected result for m = 3 is [2, 1, 0].
        long[] g = {1, 2};
        long[] f = {0, 1};
        int m = 3;

        long[] ansNaive = poly.powerProjectionNaive(g, f, m);
        assertArrayEquals(new long[]{2, 1, 0}, ansNaive);

        long[] ansFast = poly.powerProjection(g, f, m);
        assertArrayEquals(new long[]{2, 1, 0}, ansFast);
    }

    @Test
    void testRandomStressMod998244353() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runRandomStress(poly, 100, 42);
    }

    @Test
    void testRandomStressMod1000000007() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        // Note: mod 1000000007 is NOT NTT-friendly, so powerProjection will fall back to powerProjectionNaive.
        // We still run the stress test to verify the fallback logic works seamlessly.
        runRandomStress(poly, 10, 12345);
    }

    private void runRandomStress(PolynomialFpDynamic poly, int iterations, long seed) {
        Random rnd = new Random(seed);
        long mod = poly.mod;

        for (int step = 0; step < iterations; step++) {
            int degG = rnd.nextInt(15) + 1; // deg G in [1, 15] -> length in [2, 16]
            int degF = rnd.nextInt(10) + 1; // deg F in [1, 10] -> length in [2, 11]
            int m = rnd.nextInt(20) + 1;    // m in [1, 20]

            long[] g = new long[degG + 1];
            for (int i = 0; i <= degG; i++) {
                g[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            }
            // Make sure the last term of g is non-zero so we have a well-defined length
            g[degG] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);

            long[] f = new long[degF + 1];
            f[0] = 0;
            for (int i = 1; i <= degF; i++) {
                f[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            }

            long[] ansNaive = poly.powerProjectionNaive(g, f, m);
            long[] ansFast = poly.powerProjection(g, f, m);

            assertArrayEquals(ansNaive, ansFast, "Mismatch at step " + step + " with degG = " + degG + ", degF = " + degF + ", m = " + m);
        }
    }
}
