package library.util;

import org.junit.jupiter.api.Test;

import library.util.graph.grid.DominoTilingRectangle;

import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DominoTilingRectangleTest {

    @Test
    public void testKnownValues() {
        long mod = 1_000_000_007;

        // 1xM: 1 if M even, 0 if M odd
        assertEquals(1, DominoTilingRectangle.count(1, 2, mod));
        assertEquals(0, DominoTilingRectangle.count(1, 3, mod));
        assertEquals(1, DominoTilingRectangle.count(1, 4, mod));

        // 2xM: Fibonacci F_{M+1}
        assertEquals(1, DominoTilingRectangle.count(2, 1, mod));
        assertEquals(2, DominoTilingRectangle.count(2, 2, mod));
        assertEquals(3, DominoTilingRectangle.count(2, 3, mod));
        assertEquals(5, DominoTilingRectangle.count(2, 4, mod));
        assertEquals(8, DominoTilingRectangle.count(2, 5, mod));

        // 3xM
        assertEquals(0, DominoTilingRectangle.count(3, 1, mod));
        assertEquals(3, DominoTilingRectangle.count(3, 2, mod));
        assertEquals(0, DominoTilingRectangle.count(3, 3, mod));
        assertEquals(11, DominoTilingRectangle.count(3, 4, mod));
        assertEquals(41, DominoTilingRectangle.count(3, 6, mod));

        // 4xM
        assertEquals(1, DominoTilingRectangle.count(4, 1, mod));
        assertEquals(5, DominoTilingRectangle.count(4, 2, mod));
        assertEquals(11, DominoTilingRectangle.count(4, 3, mod));
        assertEquals(36, DominoTilingRectangle.count(4, 4, mod));
        assertEquals(95, DominoTilingRectangle.count(4, 5, mod));
        assertEquals(281, DominoTilingRectangle.count(4, 6, mod));
        assertEquals(781, DominoTilingRectangle.count(4, 7, mod));
    }

    @Test
    public void testAgainstBitDP() {
        long mod = 998244353;
        for (int n = 1; n <= 6; n++) {
            for (int m = 1; m <= 8; m++) {
                long expected = bitmaskDP(n, m, mod);
                long actual = DominoTilingRectangle.count(n, m, mod);
                assertEquals(expected, actual, "Failed for " + n + "x" + m);
            }
        }
        // Test a few 8x8 etc.
        assertEquals(bitmaskDP(8, 2, mod), DominoTilingRectangle.count(8, 2, mod));
        assertEquals(bitmaskDP(7, 4, mod), DominoTilingRectangle.count(7, 4, mod));
    }

    private long bitmaskDP(int n, int m, long mod) {
        if ((n & 1) == 1 && (m & 1) == 1) return 0;
        if (n > m) { int t = n; n = m; m = t; }
        // n is smaller
        long[] dp = new long[1 << n];
        dp[0] = 1;
        for (int j = 0; j < m; j++) {
            long[] next = new long[1 << n];
            for (int mask = 0; mask < (1 << n); mask++) {
                if (dp[mask] == 0) continue;
                for (int nextMask = 0; nextMask < (1 << n); nextMask++) {
                    if ((mask & nextMask) != 0) continue;
                    int combined = mask | nextMask;
                    boolean ok = true;
                    for (int i = 0; i < n; ) {
                        if ((combined & (1 << i)) == 0) {
                            if (i + 1 < n && (combined & (1 << (i + 1))) == 0) {
                                i += 2;
                            } else {
                                ok = false;
                                break;
                            }
                        } else {
                            i++;
                        }
                    }
                    if (ok) {
                        next[nextMask] = (next[nextMask] + dp[mask]) % mod;
                    }
                }
            }
            dp = next;
        }
        return dp[0];
    }
}
