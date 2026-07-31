package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class PolynomialFpDynamicSquaredTest {

    @Test
    public void testSquaredEdgeCases() {
        PolynomialFpDynamic polyNTT = PolynomialFpDynamic.MOD998244353;
        PolynomialFpDynamic polyNonNTT = PolynomialFpDynamic.of(1000000007L);

        // Empty
        assertArrayEquals(new long[]{}, polyNTT.squared(new long[]{}));
        assertArrayEquals(new long[]{}, polyNonNTT.squared(new long[]{}));

        // Single element
        assertArrayEquals(new long[]{4}, polyNTT.squared(new long[]{2}));
        assertArrayEquals(new long[]{4}, polyNonNTT.squared(new long[]{2}));

        // Single element with modulo
        assertArrayEquals(new long[]{(1000000000L * 1000000000L) % 1000000007L}, polyNonNTT.squared(new long[]{1000000000L}));
    }

    @Test
    public void testSquaredRandomNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        Random rnd = new Random(42);

        // Test with different sizes to cover naive and FFT paths
        int[] sizes = {2, 5, 32, 64, 100, 128, 150, 256};
        for (int size : sizes) {
            long[] a = new long[size];
            for (int i = 0; i < size; i++) {
                a[i] = rnd.nextInt(998244353);
            }
            long[] expected = poly.mul(a, a);
            long[] actual = poly.squared(a);
            assertArrayEquals(expected, actual, "Failed for NTT-friendly modulo with size " + size);
        }
    }

    @Test
    public void testSquaredRandomNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        Random rnd = new Random(43);

        // Test with different sizes to cover naive and CRT paths
        int[] sizes = {2, 5, 32, 64, 100, 128, 150, 256};
        for (int size : sizes) {
            long[] a = new long[size];
            for (int i = 0; i < size; i++) {
                a[i] = rnd.nextInt(1000000007);
            }
            long[] expected = poly.mul(a, a);
            long[] actual = poly.squared(a);
            assertArrayEquals(expected, actual, "Failed for non-NTT-friendly modulo with size " + size);
        }
    }

    @Test
    public void testSquaredAgainstPolynomialFp() {
        Random rnd = new Random(44);
        int[] sizes = {2, 10, 64, 128, 200};
        for (int size : sizes) {
            long[] a = new long[size];
            for (int i = 0; i < size; i++) {
                a[i] = rnd.nextInt(998244353);
            }
            long[] expected = PolynomialFp.squared(a);
            long[] actual = PolynomialFpDynamic.MOD998244353.squared(a);
            assertArrayEquals(expected, actual, "Mismatch between PolynomialFp.squared and PolynomialFpDynamic.squared for size " + size);
        }
    }
}
