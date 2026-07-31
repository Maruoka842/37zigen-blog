package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FpCombGeneralizedTest {
    @Test
    public void testCombGeneralized() {
        Fp fp = Fp.MOD998244353;

        // n >= k >= 0
        assertEquals(1, fp.combGeneralized(5, 0));
        assertEquals(5, fp.combGeneralized(5, 1));
        assertEquals(10, fp.combGeneralized(5, 2));
        assertEquals(10, fp.combGeneralized(5, 3));
        assertEquals(5, fp.combGeneralized(5, 4));
        assertEquals(1, fp.combGeneralized(5, 5));
        assertEquals(0, fp.combGeneralized(5, 6));

        // n < 0
        // comb(-1, k) = (-1)^k
        assertEquals(1, fp.combGeneralized(-1, 0));
        assertEquals(fp.mod - 1, fp.combGeneralized(-1, 1));
        assertEquals(1, fp.combGeneralized(-1, 2));
        assertEquals(fp.mod - 1, fp.combGeneralized(-1, 3));

        // comb(-2, k) = (-1)^k * (k+1)
        assertEquals(1, fp.combGeneralized(-2, 0));
        assertEquals(fp.mod - 2, fp.combGeneralized(-2, 1)); // (-1)^1 * 2 = -2
        assertEquals(3, fp.combGeneralized(-2, 2));        // (-1)^2 * 3 = 3
        assertEquals(fp.mod - 4, fp.combGeneralized(-2, 3)); // (-1)^3 * 4 = -4

        // k < 0
        assertEquals(0, fp.combGeneralized(5, -1));
        assertEquals(0, fp.combGeneralized(-1, -1));

        // n = 0
        assertEquals(1, fp.combGeneralized(0, 0));
        assertEquals(0, fp.combGeneralized(0, 1));

        // large n within int range
        // comb(1000000, 2)
        int nLarge = 1000000;
        long expectedLarge = 1L * nLarge * (nLarge - 1) / 2 % fp.mod;
        assertEquals(expectedLarge, fp.combGeneralized(nLarge, 2));

        // comb(-1000000, 2)
        // = (-1)^2 * comb(2 - (-1000000) - 1, 2)
        // = comb(1000000 + 1, 2)
        // = (1000001 * 1000000 / 2) % mod
        int nNegLarge = -1000000;
        int nArg = 2 - nNegLarge - 1;
        long expectedNegLarge = 1L * nArg * (nArg - 1) / 2 % fp.mod;
        assertEquals(expectedNegLarge, fp.combGeneralized(nNegLarge, 2));
    }
}
