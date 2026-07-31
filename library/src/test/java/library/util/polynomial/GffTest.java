package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GffTest {
    @Test
    public void testGff() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Case 1: f = x(x-1) = x^2 - x
        // g1 = 1, g2 = x
        // [g1]_1 * [g2]_2 = 1 * (x * (x-1)) = x^2 - x
        long[] f1 = new long[] {0, 998244352, 1}; // x^2 - x
        long[][] g1 = poly.gff(f1);

        assertEquals(2, g1.length);
        assertArrayEquals(new long[] {1}, g1[0]); // g1 = 1
        assertArrayEquals(new long[] {0, 1}, g1[1]); // g2 = x

        // Case 2: f = x(x-1)(x-2) = x^3 - 3x^2 + 2x
        // g1 = 1, g2 = 1, g3 = x
        long[] f2 = new long[] {0, 2, 998244350, 1}; // x^3 - 3x^2 + 2x
        long[][] g2 = poly.gff(f2);

        assertEquals(3, g2.length);
        assertArrayEquals(new long[] {1}, g2[0]);
        assertArrayEquals(new long[] {1}, g2[1]);
        assertArrayEquals(new long[] {0, 1}, g2[2]);

        // Case 3: f = (x+1)x(x-1) = x^3 - x
        // g1 = 1, g2 = 1, g3 = x + 1
        // [1]_1 * [1]_2 * [x+1]_3 = (x+1)x(x-1) = x^3 - x
        long[] f3 = new long[] {0, 998244352, 0, 1}; // x^3 - x
        long[][] g3 = poly.gff(f3);

        assertEquals(3, g3.length);
        assertArrayEquals(new long[] {1}, g3[0]);
        assertArrayEquals(new long[] {1}, g3[1]);
        assertArrayEquals(new long[] {1, 1}, g3[2]); // g3 = x + 1

        // Case 4: f = (x^2 + 1)(x^2 + 1 - 2x + 1) ... no
        // Let's try something with multiple different g_i
        // f = [x]_2 * [x+10]_1 = x(x-1) * (x+10)
        // g2 = x, g1 = x+10
        // Provided there's no overlap.
        // x, x-1, x+10 are factors.
        // gcd(x, (x+10)+k) = 1 for k >= 1? x = -10-k.
        // gcd(x-1, (x+10)+k) = 1? x-1 = -10-k => x = -9-k.
        // If mod is large, these are different.
        long[] f4 = poly.mul(new long[] {0, 998244352, 1}, new long[] {10, 1}); // (x^2-x)(x+10) = x^3 + 9x^2 - 10x
        long[][] g4 = poly.gff(f4);
        assertEquals(2, g4.length);
        assertArrayEquals(new long[] {10, 1}, g4[0]); // g1 = x + 10
        assertArrayEquals(new long[] {0, 1}, g4[1]); // g2 = x
    }
}
