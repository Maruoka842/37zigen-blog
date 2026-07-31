package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class PolynomialPowModXTest {

    @Test
    void testEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Zero polynomial divisor
        assertThrows(ArithmeticException.class, () -> {
            poly.xpowMod(5, new long[0]);
        });
        assertThrows(ArithmeticException.class, () -> {
            poly.xpowMod(5, new long[]{0, 0, 0});
        });

        // Constant divisor
        assertArrayEquals(new long[0], poly.xpowMod(5, new long[]{5}));
        assertArrayEquals(new long[0], poly.xpowMod(0, new long[]{998244352}));

        // Negative k
        assertThrows(IllegalArgumentException.class, () -> {
            poly.xpowMod(-1, new long[]{1, 2});
        });

        // k < d
        // For k = 2, d = 4 (F = [1, 2, 3, 4, 5])
        long[] F = {1, 2, 3, 4, 5};
        assertArrayEquals(new long[]{0, 0, 1}, poly.xpowMod(2, F));
        assertArrayEquals(new long[]{1}, poly.xpowMod(0, F));
        assertArrayEquals(new long[]{0, 1}, poly.xpowMod(1, F));
    }

    @Test
    void testFibonacciExample() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // F(x) = x^2 - x - 1 -> [-1, -1, 1]
        long[] F = {998244352, 998244352, 1};

        // x^4 mod F(x) = 3x + 2 -> [2, 3]
        long[] res4 = poly.xpowMod(4, F);
        assertArrayEquals(new long[]{2, 3}, res4);

        // x^10 mod F(x)
        long[] res10 = poly.xpowMod(10, F);
        long[] ref10 = poly.powMod(new long[]{0, 1}, 10, F);
        assertArrayEquals(ref10, res10);
    }

    @Test
    void testRandomStressMod998244353() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runRandomStress(poly, 100, 42);
    }

    @Test
    void testRandomStressMod1000000007() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runRandomStress(poly, 100, 12345);
    }

    @Test
    void testLargeK() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] F = {5, 12, 1, 998244352, 3}; // d = 4
        long k = 1000000000000000L; // 10^15
        long[] res = poly.xpowMod(k, F);
        long[] ref = poly.powMod(new long[]{0, 1}, k, F);
        assertArrayEquals(ref, res);
    }

    @Test
    void testPowSumEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Zero polynomial divisor
        assertThrows(ArithmeticException.class, () -> {
            poly.geometricSumMod(5, new long[0]);
        });
        assertThrows(ArithmeticException.class, () -> {
            poly.geometricSumMod(5, new long[]{0, 0, 0});
        });

        // Constant divisor
        assertArrayEquals(new long[0], poly.geometricSumMod(5, new long[]{5}));
        assertArrayEquals(new long[0], poly.geometricSumMod(0, new long[]{998244352}));

        // Negative k
        assertThrows(IllegalArgumentException.class, () -> {
            poly.geometricSumMod(-1, new long[]{1, 2});
        });

        // k < d
        // For k = 2, d = 4 (F = [1, 2, 3, 4, 5])
        long[] F = {1, 2, 3, 4, 5};
        assertArrayEquals(new long[]{1, 1}, poly.geometricSumMod(2, F));
        assertArrayEquals(new long[0], poly.geometricSumMod(0, F));
        assertArrayEquals(new long[]{1}, poly.geometricSumMod(1, F));
    }

    @Test
    void testPowSumRandomStressMod998244353() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runRandomPowSumStress(poly, 100, 42);
    }

    @Test
    void testPowSumRandomStressMod1000000007() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runRandomPowSumStress(poly, 100, 12345);
    }

    private void runRandomStress(PolynomialFpDynamic poly, int iterations, long seed) {
        Random rnd = new Random(seed);
        long mod = poly.mod;

        for (int step = 0; step < iterations; step++) {
            int d = rnd.nextInt(15) + 1; // degree from 1 to 15
            long[] F = new long[d + 1];
            for (int i = 0; i < d; i++) {
                F[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            }
            // Ensure leading coefficient is non-zero
            F[d] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);

            long k = rnd.nextInt(500) + d;

            long[] res = poly.xpowMod(k, F);
            long[] ref = poly.powMod(new long[]{0, 1}, k, F);

            assertArrayEquals(ref, res, "Mismatch at step " + step + " with d = " + d + ", k = " + k);
        }
    }

    private void runRandomPowSumStress(PolynomialFpDynamic poly, int iterations, long seed) {
        Random rnd = new Random(seed);
        long mod = poly.mod;

        for (int step = 0; step < iterations; step++) {
            int d = rnd.nextInt(15) + 1; // degree from 1 to 15
            long[] F = new long[d + 1];
            for (int i = 0; i < d; i++) {
                F[i] = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            }
            // Ensure leading coefficient is non-zero
            F[d] = 1 + (rnd.nextLong() & Long.MAX_VALUE) % (mod - 1);

            long k = rnd.nextInt(200) + d; // k up to ~215 for reasonably fast reference sum

            long[] res = poly.geometricSumMod(k, F);
            long[] ref = getPowSumReference(poly, k, F);

            assertArrayEquals(ref, res, "Mismatch at step " + step + " with d = " + d + ", k = " + k);
        }
    }

    private long[] getPowSumReference(PolynomialFpDynamic poly, long k, long[] F) {
        long[] sum = new long[0];
        for (long i = 0; i < k; i++) {
            long[] term = poly.powMod(new long[]{0, 1}, i, F);
            sum = poly.add(sum, term);
        }
        return poly.resize(sum);
    }
}
