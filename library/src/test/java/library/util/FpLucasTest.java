package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;

public class FpLucasTest {

    @Test
    public void testSmallPrimes() {
        Fp fp = new Fp(5);
        // comb(n, k)
        assertEquals(1, fp.combLucas(0, 0));
        assertEquals(1, fp.combLucas(5, 0));
        assertEquals(5 % 5, fp.combLucas(5, 1));
        assertEquals(10 % 5, fp.combLucas(5, 2));
        assertEquals(10 % 5, fp.combLucas(5, 3));
        assertEquals(5 % 5, fp.combLucas(5, 4));
        assertEquals(1, fp.combLucas(5, 5));

        // Let's test with Lucas' theorem
        // n = 6, k = 2. In base 5: 6 = (11)_5, 2 = (02)_5.
        // comb(6, 2) mod 5 = (comb(1, 0) * comb(1, 2)) mod 5 = 1 * 0 = 0.
        // Mathematically: comb(6, 2) = 15, 15 % 5 == 0.
        assertEquals(0, fp.combLucas(6, 2));

        // n = 7, k = 2. In base 5: 7 = (12)_5, 2 = (02)_5.
        // comb(7, 2) mod 5 = (comb(1, 0) * comb(2, 2)) mod 5 = 1 * 1 = 1.
        // Mathematically: comb(7, 2) = 21, 21 % 5 == 1.
        assertEquals(1, fp.combLucas(7, 2));

        // n = 23, k = 11. In base 5: 23 = (43)_5, 11 = (21)_5.
        // comb(23, 11) mod 5 = comb(4, 2) * comb(3, 1) mod 5 = 6 * 3 = 18 = 3 mod 5.
        // Mathematically: comb(23, 11) = 1352078, 1352078 % 5 = 3.
        assertEquals(3, fp.combLucas(23, 11));
    }

    @Test
    public void testEdgeAndBoundaryCases() {
        Fp fp = Fp.MOD998244353;
        assertEquals(0, fp.combLucas(5, -1));
        assertEquals(0, fp.combLucas(5, 6));
        assertEquals(0, fp.combLucas(-1, 5));
        assertEquals(0, fp.combLucas(-5, -3));
        assertEquals(1, fp.combLucas(0, 0));
        assertEquals(1, fp.combLucas(100, 100));
        assertEquals(0, fp.combLucas(0, 1));
    }

    @Test
    public void testRandomizedMod998244353() {
        Fp fp = Fp.MOD998244353;
        Random rng = new Random(42);
        // Small n, k where standard comb or combNaive matches
        for (int i = 0; i < 500; i++) {
            long n = rng.nextInt(1000000);
            long k = rng.nextInt((int) n + 1);
            long expected = fp.combNaive(n, k);
            long actual = fp.combLucas(n, k);
            assertEquals(expected, actual, String.format("Failed for combLucas(%d, %d)", n, k));
        }
    }

    @Test
    public void testLargeNAndK() {
        Fp fp = new Fp(1000003); // Prime
        // n = 1000003 * 5 + 2 = 5000017
        // k = 1000003 * 2 + 1 = 2000007
        // combLucas(n, k) = comb(5, 2) * comb(2, 1) % mod = 10 * 2 = 20
        assertEquals(20, fp.combLucas(5000017, 2000007));
    }

    @Test
    public void testEvenLargerNAndK() {
        Fp fp = Fp.MOD1000000007; // 10^9 + 7
        // Let's verify with smaller exponents/powers that can still exceed standard limits
        // 1000000007 is around 10^9
        long n = 1000000007L * 3 + 5;
        long k = 1000000007L * 2 + 2;
        // combLucas(n, k) = comb(3, 2) * comb(5, 2) = 3 * 10 = 30.
        assertEquals(30, fp.combLucas(n, k));
    }
}
