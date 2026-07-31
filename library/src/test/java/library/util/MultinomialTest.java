package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MultinomialTest {

    @Test
    public void testMathUtilsMultinomial() {
        assertEquals(1, MathUtils.multinomial());
        assertEquals(1, MathUtils.multinomial(5));
        assertEquals(1, MathUtils.multinomial(0, 0));
        assertEquals(2, MathUtils.multinomial(1, 1));
        assertEquals(6, MathUtils.multinomial(1, 1, 1));
        assertEquals(3, MathUtils.multinomial(2, 1));
        assertEquals(10, MathUtils.multinomial(3, 2));
        assertEquals(1260, MathUtils.multinomial(4, 3, 2));
    }

    @Test
    public void testMathUtilsMultinomialNegative() {
        assertEquals(0, MathUtils.multinomial(-1, 1));
        assertEquals(0, MathUtils.multinomial(1, -1));
    }

    @Test
    public void testFpMultinomial() {
        Fp fp = Fp.MOD998244353;
        assertEquals(1, fp.multinomial());
        assertEquals(1, fp.multinomial(5));
        assertEquals(2, fp.multinomial(1, 1));
        assertEquals(6, fp.multinomial(1, 1, 1));
        assertEquals(3, fp.multinomial(2, 1));
        assertEquals(1260, fp.multinomial(4, 3, 2));
    }

    @Test
    public void testFpMultinomialLarge() {
        Fp fp = Fp.MOD998244353;
        // Test with values that exceed fac table size (default expand might be needed)
        // Let's use something like 100, 100
        long expected = fp.comb(200, 100);
        assertEquals(expected, fp.multinomial(100, 100));
    }

    @Test
    public void testFpMultinomialNegative() {
        Fp fp = Fp.MOD998244353;
        assertEquals(0, fp.multinomial(-1, 1));
    }

    @Test
    public void testConsistency() {
        Fp fp = new Fp(1000000007); // A different prime
        int[] ks = {2, 3, 4};
        long mathRes = MathUtils.multinomial(ks);
        long fpRes = fp.multinomial(ks);
        assertEquals(mathRes % 1000000007, fpRes);
    }
}
