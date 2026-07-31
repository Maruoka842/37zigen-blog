package library.util.geometry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import library.util.geometry.LongLine;

public class LongLineTest {

    @Test
    public void testOnLineOverflow() {
        // Line ax + by + c = 0
        // We want a case where ax + by + c is a non-zero multiple of 2^64.

        long a = 1000000007L;
        long b = 1000000009L;
        long c = 0;

        // Solve ax + by = 2^64
        // Using BigInteger to find x, y
        BigInteger bigA = BigInteger.valueOf(a);
        BigInteger bigB = BigInteger.valueOf(b);
        BigInteger bigMod = BigInteger.ONE.shiftLeft(64);

        // ax = 2^64 - by  => ax = -by (mod 2^64)
        // Let's just find x, y such that ax + by = 2^64
        // Using extended GCD: a*x0 + b*y0 = gcd(a, b) = 1
        // Then a*(x0 * 2^64) + b*(y0 * 2^64) = 2^64
        // But we need x, y to be long.

        // Let's try something simpler.
        // If a = 1, b = 0, c = 0. Line is x = 0.
        // onLine(2^64, 0) would be onLine(0, 0) which is true.
        // But 2^64 cannot be represented as long.

        // How about a = 2, b = 1, c = 0. Line is 2x + y = 0.
        // x = 2^62, y = 2^64 - 2*2^62 = 0?
        // No, y = -2*2^62 = -2^63.
        // 2 * 2^62 + (-2^63) = 2^63 - 2^63 = 0. This is a true positive.

        // We need ax + by + c = k * 2^64.
        // Let a = 3, b = 1, c = 0. 3x + y = 0.
        // Try x = 2^63, y = 2^63.
        // 3 * 2^63 + 2^63 = 4 * 2^63 = 2^65 = 0 (mod 2^64).
        // Is (2^63, 2^63) on 3x + y = 0? No, 3*2^63 + 2^63 = 2^65 != 0.

        long a1 = 3;
        long b1 = 1;
        long c1 = 0;
        long x1 = Long.MIN_VALUE; // -2^63, which is 2^63 in 2's complement if we are careful?
        // Wait, Long.MIN_VALUE is -2^63.
        // 3 * (-2^63) + (-2^63) = -4 * 2^63 = -2^65.
        // In 64-bit: -2^65 = 0.

        LongLine line1 = new LongLine(a1, b1, c1);
        // The following will fail if onLine uses 64-bit long arithmetic
        assertFalse(line1.isOnLine(x1, x1), "Point (-2^63, -2^63) should NOT be on line 3x + y = 0");
    }

    @Test
    public void testOnLineTruePositive() {
        LongLine line = new LongLine(1, 1, 0);
        assertTrue(line.isOnLine(10, -10));
        assertTrue(line.isOnLine(0, 0));
    }
}
