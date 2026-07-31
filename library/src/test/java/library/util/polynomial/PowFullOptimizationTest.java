package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class PowFullOptimizationTest {

    @Test
    public void testPowFullMonomial() {
        long[] a = {0, 1}; // x
        int n = 5;
        long[] expected = {0, 0, 0, 0, 0, 1}; // x^5
        assertArrayEquals(expected, PolynomialFp.powFull(a, n));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.powFull(a, n));
    }

    @Test
    public void testPowFullBinomial() {
        long[] a = {1, 1}; // 1 + x
        int n = 3;
        long[] expected = {1, 3, 3, 1}; // (1 + x)^3 = 1 + 3x + 3x^2 + x^3
        assertArrayEquals(expected, PolynomialFp.powFull(a, n));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.powFull(a, n));
    }

    @Test
    public void testPowFullLargeN() {
        long[] a = {1, 2, 1}; // (1 + x)^2
        int n = 10;
        // (1 + x)^20
        long[] expected = new long[21];
        long mod = PolynomialFp.mod;
        library.util.Fp fp = new library.util.Fp(mod);
        for (int i = 0; i <= 20; i++) {
            expected[i] = fp.comb(20, i);
        }
        assertArrayEquals(expected, PolynomialFp.powFull(a, n));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.powFull(a, n));
    }

    @Test
    public void testPowFullWithLeadingZeros() {
        long[] a = {0, 0, 1, 1}; // x^2(1 + x)
        int n = 2;
        long[] expected = {0, 0, 0, 0, 1, 2, 1}; // x^4(1 + 2x + x^2)
        assertArrayEquals(expected, PolynomialFp.powFull(a, n));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.powFull(a, n));
    }

    @Test
    public void testPowFullRandom() {
        Random rnd = new Random(42);
        for (int k = 1; k <= 10; k++) {
            long[] a = new long[k + 1];
            for (int i = 0; i <= k; i++) {
                a[i] = rnd.nextInt(998244353);
            }
            if (a[0] == 0) a[0] = 1;
            int n = 5;

            long[] resBinary = powFullBinary(a, n);
            assertArrayEquals(resBinary, PolynomialFp.powFull(a, n));
            assertArrayEquals(resBinary, PolynomialFpDynamic.MOD998244353.powFull(a, n));
        }
    }

    private long[] powFullBinary(long[] a, int e) {
        long[] res = {1};
        long[] base = a;
        while (e > 0) {
            if (e % 2 == 1) res = PolynomialFp.mul(res, base);
            base = PolynomialFp.mul(base, base);
            e /= 2;
        }
        return res;
    }
}
