package library.util.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.geometry.IntLine;
import library.util.geometry.LongLine3D;
import library.util.geometry.LongOrientedLine;

public class GeometryOverflowTest {

    @Test
    public void testLongOrientedLineOverflow() {
        // 3x + y = 0
        long a = 3;
        long b = 1;
        long c = 0;
        long x = Long.MIN_VALUE; // -2^63

        LongOrientedLine line = new LongOrientedLine(a, b, c);
        // 3 * (-2^63) + (-2^63) = -2^65 = 0 (mod 2^64)
        // High 64 bits = -2.
        assertFalse(line.onLine(x, x), "Point should NOT be on line 3x + y = 0");
        assertEquals(-1, line.orientedSide(x, x), "Point should be on the right side of 3x + y = 0");

        // Case where hi = 0, lo2 != 0 (e.g. lo2 = 2^63, which is Long.MIN_VALUE)
        // ax + by + c = 2^63
        // Let a = 1, x = 2^63 (but long x cannot be 2^63, only -2^63)
        // Let a = -1, x = -2^63 => ax = 2^63.
        // Let b = 0, c = 0.
        // ax + by + c = (-1)*(-2^63) = 2^63.
        // lo2 = 2^63 (Long.MIN_VALUE). hi = 0.
        LongOrientedLine line2 = new LongOrientedLine(-1, 0, 0);
        assertEquals(1, line2.orientedSide(Long.MIN_VALUE, 0), "Should be positive (left side)");
    }

    @Test
    public void testIntLineOverflow() {
        // IntLine with large x, y
        IntLine line = new IntLine(3, 1, 0);
        long x = Long.MIN_VALUE;
        assertFalse(line.onLine(x, x), "Point should NOT be on line 3x + y = 0");
    }

    @Test
    public void testLongLine3DOverflow() {
        // Line through (0,0,0) and (1,1,1) -> dir = (1,1,1), from = (0,0,0)
        LongLine3D line = new LongLine3D(0, 0, 0, 1, 1, 1);

        // Point on line: (x, x, x)
        assertTrue(line.onLine(123456789L, 123456789L, 123456789L));

        // Point NOT on line that might overflow:
        // cross product components: dx*dir1 - dy*dir0
        // dx = x - from0
        // Let x = 2^62, y = 2^62 + 2^62 = 2^63? No.
        // Let dir = (1, 3, 1), from = (0, 0, 0)
        // dx = 2^63, dy = 2^63.
        // dx*dir1 - dy*dir0 = 2^63 * 3 - 2^63 * 1 = 2 * 2^63 = 2^64 = 0 (mod 2^64)

        LongLine3D line2 = new LongLine3D(0, 0, 0, 1, 3, 1);
        long x = Long.MIN_VALUE; // -2^63 which is 2^63 unsigned
        assertFalse(line2.onLine(x, x, 0), "Point (-2^63, -2^63, 0) should NOT be on line (0,0,0)->(1,3,1)");
    }
}
