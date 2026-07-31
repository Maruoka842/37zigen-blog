package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class MulAllOptimizationTest {

    @Test
    public void testMulAllList() {
        long[] f1 = {1, 1}; // 1 + x
        long[] f2 = {1, 2}; // 1 + 2x
        List<long[]> fsList = List.of(f1, f1, f2, f2, f1);
        long[] expected = {1, 7, 19, 25, 16, 4};

        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.mulAll(fsList));
    }

    @Test
    public void testMulAllIdentical() {
        long[] f = {1, 1}; // 1 + x
        long[][] fs = {f, f, f};
        long[] expected = {1, 3, 3, 1}; // (1 + x)^3

        assertArrayEquals(expected, PolynomialFp.mulAll(fs));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.mulAll(fs));
    }

    @Test
    public void testMulAllMixed() {
        long[] f1 = {1, 1}; // 1 + x
        long[] f2 = {1, 2}; // 1 + 2x
        long[][] fs = {f1, f1, f2, f2, f1};
        // (1 + x)^3 * (1 + 2x)^2
        // = (1 + 3x + 3x^2 + x^3) * (1 + 4x + 4x^2)
        // = 1 + 4x + 4x^2
        //     + 3x + 12x^2 + 12x^3
        //          + 3x^2 + 12x^3 + 12x^4
        //                 + x^3 + 4x^4 + 4x^5
        // = 1 + 7x + 19x^2 + 25x^3 + 16x^4 + 4x^5
        long[] expected = {1, 7, 19, 25, 16, 4};

        assertArrayEquals(expected, PolynomialFp.mulAll(fs));
        assertArrayEquals(expected, PolynomialFpDynamic.MOD998244353.mulAll(fs));
    }

    @Test
    public void testMulAllCutoff() {
        long[] f = {1, 1}; // 1 + x
        long[][] fs = {f, f, f};
        int cutoff = 2;
        long[] expected = {1, 3}; // (1 + x)^3 = 1 + 3x + 3x^2 + x^3, cutoff at 2

        assertArrayEquals(expected, PolynomialFp.mulAll(fs, cutoff));
    }
}
