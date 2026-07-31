package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import library.util.algebra.strategy.CommutativeRingStrategy;

public class PolynomialFpDynamicTruncatedTest {

    @Test
    public void testBasicArithmetic() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 3;
        CommutativeRingStrategy<long[]> strategy = poly.truncatedStrategy(n);

        // a = 1 + 2x + 3x^2
        long[] a = {1, 2, 3};
        // b = 4 + 5x + 6x^2
        long[] b = {4, 5, 6};

        // zero, one
        assertArrayEquals(new long[]{}, strategy.zero());
        assertArrayEquals(new long[]{1}, strategy.one());

        // add: a + b = 5 + 7x + 9x^2 -> 5 + 7x + 9x^2
        assertArrayEquals(new long[]{5, 7, 9}, strategy.add(a, b));

        // sub: a - b = -3 - 3x - 3x^2 = (mod-3) + (mod-3)x + (mod-3)x^2
        long mod = poly.mod;
        assertArrayEquals(new long[]{mod - 3, mod - 3, mod - 3}, strategy.sub(a, b));

        // neg: -a = -1 - 2x - 3x^2
        assertArrayEquals(new long[]{mod - 1, mod - 2, mod - 3}, strategy.neg(a));

        // mul: a * b = (1 + 2x + 3x^2)(4 + 5x + 6x^2) = 4 + 13x + 28x^2 + 27x^3 + 18x^4
        // Modulo x^3: 4 + 13x + 28x^2
        assertArrayEquals(new long[]{4, 13, 28}, strategy.mul(a, b));
    }

    @Test
    public void testTruncationAndTrimming() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 3;
        CommutativeRingStrategy<long[]> strategy = poly.truncatedStrategy(n);

        // Input with length > n
        long[] a = {1, 2, 3, 4, 5};
        long[] b = {0, 0, 0, 1, 1};

        // Equals should compare only up to degree n - 1 (modulo x^n)
        // a mod x^3 = 1 + 2x + 3x^2
        // c = 1 + 2x + 3x^2
        long[] c = {1, 2, 3};
        assertTrue(strategy.equals(a, c));

        // b mod x^3 has length 3 of all zeros ({0, 0, 0}), so equals behaves correctly
        assertTrue(strategy.equals(b, strategy.zero()));

        // Check if output is truncated (trailing zeros are kept as-is if within limit)
        // d = 1 + 2x + 0x^2 -> 1 + 2x + 0x^2
        long[] d = {1, 2, 0, 4, 5};
        assertArrayEquals(new long[]{1, 2, 0}, strategy.add(d, strategy.zero()));
    }

    @Test
    public void testEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // n = 0
        CommutativeRingStrategy<long[]> strategy0 = poly.truncatedStrategy(0);
        assertArrayEquals(new long[]{}, strategy0.zero());
        assertArrayEquals(new long[]{}, strategy0.one());
        assertArrayEquals(new long[]{}, strategy0.add(new long[]{1, 2}, new long[]{3, 4}));
        assertArrayEquals(new long[]{}, strategy0.mul(new long[]{1, 2}, new long[]{3, 4}));

        // n = 1
        CommutativeRingStrategy<long[]> strategy1 = poly.truncatedStrategy(1);
        assertArrayEquals(new long[]{}, strategy1.zero());
        assertArrayEquals(new long[]{1}, strategy1.one());
        assertArrayEquals(new long[]{4}, strategy1.add(new long[]{1, 2}, new long[]{3, 4}));
        assertArrayEquals(new long[]{3}, strategy1.mul(new long[]{1, 2}, new long[]{3, 4}));
    }

    @Test
    public void testPow() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 4;
        CommutativeRingStrategy<long[]> strategy = poly.truncatedStrategy(n);

        // (1 + x)^3 = 1 + 3x + 3x^2 + x^3
        long[] a = {1, 1};
        assertArrayEquals(new long[]{1, 3, 3, 1}, strategy.pow(a, 3));

        // (1 + x)^4 = 1 + 4x + 6x^2 + 4x^3 + x^4 mod x^4 = 1 + 4x + 6x^2 + 4x^3
        assertArrayEquals(new long[]{1, 4, 6, 4}, strategy.pow(a, 4));
    }

    @Test
    public void testInvalidN() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        assertThrows(IllegalArgumentException.class, () -> poly.truncatedStrategy(-1));
    }

    @Test
    public void testTruncateAndTrimOptimization() throws Exception {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        CommutativeRingStrategy<long[]> strategy = poly.truncatedStrategy(5);

        // Access the private helper method truncateAndTrim via reflection
        java.lang.reflect.Method method = strategy.getClass().getDeclaredMethod("truncateAndTrim", long[].class, int.class);
        method.setAccessible(true);

        // No truncation needed (length <= limit): should return the exact same array instance directly, even with trailing zeros
        long[] a = {1, 2, 3};
        long[] resA = (long[]) method.invoke(strategy, a, 5);
        assertSame(a, resA);

        long[] b = {1, 2, 0};
        long[] resB = (long[]) method.invoke(strategy, b, 5);
        assertSame(b, resB); // Handled by length-only optimization!

        // With length > limit: should truncate and return a new instance directly of length limit
        long[] c = {1, 2, 3, 4};
        long[] resC = (long[]) method.invoke(strategy, c, 2);
        assertNotSame(c, resC);
        assertArrayEquals(new long[]{1, 2}, resC);
    }
}
