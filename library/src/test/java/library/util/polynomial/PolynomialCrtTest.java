package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class PolynomialCrtTest {
    @Test
    public void testMulCrt() {
        long mod = 1_000_000_007L;
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
        Random rnd = new Random(42);

        for (int deg : new int[]{10, 100, 300, 513, 1000}) {
            long[] a = new long[deg];
            long[] b = new long[deg];
            for (int i = 0; i < deg; i++) {
                a[i] = rnd.nextInt((int) mod);
                b[i] = rnd.nextInt((int) mod);
            }

            long[] expected = poly.mulNaive(a, b);
            long[] actual = poly.mul(a, b);

            assertArrayEquals(expected, actual, "Failed for degree " + deg);
        }
    }

    @Test
    public void testMulCrtOtherMod() {
        long mod = 1000000009L; // not NTT friendly
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
        Random rnd = new Random(42);

        int deg = 200; // result length 399 > 128, will use mulCRT
        long[] a = new long[deg];
        long[] b = new long[deg];
        for (int i = 0; i < deg; i++) {
            a[i] = Math.abs(rnd.nextLong()) % mod;
            b[i] = Math.abs(rnd.nextLong()) % mod;
        }

        long[] expected = poly.mulNaive(a, b);
        long[] actual = poly.mul(a, b);

        assertArrayEquals(expected, actual);
    }
}
