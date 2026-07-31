package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class CountLinearModLessTest {

    @Test
    public void testCountLinearModLessBasic() {
        // n=5, m=3, a=1, b=0, t=2
        // (1*i + 0) mod 3 for i=0,1,2,3,4:
        // 0 mod 3 = 0 < 2 (yes)
        // 1 mod 3 = 1 < 2 (yes)
        // 2 mod 3 = 2 < 2 (no)
        // 3 mod 3 = 0 < 2 (yes)
        // 4 mod 3 = 1 < 2 (yes)
        // Total: 4
        assertEquals(4, FloorSum.countLinearModLess(5, 3, 1, 0, 2));
    }

    @Test
    public void testCountLinearModLessOrEqualBasic() {
        // n=5, m=3, a=1, b=0, t=1
        // (1*i + 0) mod 3 for i=0,1,2,3,4:
        // 0 mod 3 = 0 <= 1 (yes)
        // 1 mod 3 = 1 <= 1 (yes)
        // 2 mod 3 = 2 <= 1 (no)
        // 3 mod 3 = 0 <= 1 (yes)
        // 4 mod 3 = 1 <= 1 (yes)
        // Total: 4
        assertEquals(4, FloorSum.countLinearModLessOrEqual(5, 3, 1, 0, 1));
    }

    @Test
    public void testCountLinearModGreaterBasic() {
        // n=5, m=3, a=1, b=0, t=1
        // (1*i + 0) mod 3 for i=0,1,2,3,4:
        // 0 mod 3 = 0 > 1 (no)
        // 1 mod 3 = 1 > 1 (no)
        // 2 mod 3 = 2 > 1 (yes)
        // 3 mod 3 = 0 > 1 (no)
        // 4 mod 3 = 1 > 1 (no)
        // Total: 1
        assertEquals(1, FloorSum.countLinearModGreater(5, 3, 1, 0, 1));
    }

    @Test
    public void testCountLinearModGreaterOrEqualBasic() {
        // n=5, m=3, a=1, b=0, t=2
        // (1*i + 0) mod 3 for i=0,1,2,3,4:
        // 0 mod 3 = 0 >= 2 (no)
        // 1 mod 3 = 1 >= 2 (no)
        // 2 mod 3 = 2 >= 2 (yes)
        // 3 mod 3 = 0 >= 2 (no)
        // 4 mod 3 = 1 >= 2 (no)
        // Total: 1
        assertEquals(1, FloorSum.countLinearModGreaterOrEqual(5, 3, 1, 0, 2));
    }

    @Test
    public void testCountLinearModEdgeCases() {
        // Less
        assertEquals(0, FloorSum.countLinearModLess(0, 3, 1, 0, 2));
        assertEquals(0, FloorSum.countLinearModLess(5, 3, 1, 0, 0));
        assertEquals(0, FloorSum.countLinearModLess(5, 3, 1, 0, -1));
        assertEquals(5, FloorSum.countLinearModLess(5, 3, 1, 0, 3));
        assertEquals(5, FloorSum.countLinearModLess(5, 3, 1, 0, 4));

        // LessOrEqual
        assertEquals(0, FloorSum.countLinearModLessOrEqual(0, 3, 1, 0, 2));
        assertEquals(0, FloorSum.countLinearModLessOrEqual(5, 3, 1, 0, -1));
        assertEquals(5, FloorSum.countLinearModLessOrEqual(5, 3, 1, 0, 2));
        assertEquals(5, FloorSum.countLinearModLessOrEqual(5, 3, 1, 0, 3));

        // Greater
        assertEquals(0, FloorSum.countLinearModGreater(0, 3, 1, 0, 2));
        assertEquals(5, FloorSum.countLinearModGreater(5, 3, 1, 0, -1));
        assertEquals(0, FloorSum.countLinearModGreater(5, 3, 1, 0, 2));
        assertEquals(0, FloorSum.countLinearModGreater(5, 3, 1, 0, 3));

        // GreaterOrEqual
        assertEquals(0, FloorSum.countLinearModGreaterOrEqual(0, 3, 1, 0, 2));
        assertEquals(5, FloorSum.countLinearModGreaterOrEqual(5, 3, 1, 0, 0));
        assertEquals(5, FloorSum.countLinearModGreaterOrEqual(5, 3, 1, 0, -1));
        assertEquals(0, FloorSum.countLinearModGreaterOrEqual(5, 3, 1, 0, 3));
    }

    @Test
    public void testCountLinearModRandom() {
        Random rnd = new Random(42);
        for (int i = 0; i < 2000; i++) {
            long n = rnd.nextInt(100);
            long m = rnd.nextInt(1, 100);
            long a = rnd.nextLong(-100, 100);
            long b = rnd.nextLong(-100, 100);
            long t = rnd.nextLong(-10, 110);

            long expectedLess = naiveCount(n, m, a, b, t, "<");
            long actualLess = FloorSum.countLinearModLess(n, m, a, b, t);
            assertEquals(expectedLess, actualLess, String.format("Less failed for n=%d, m=%d, a=%d, b=%d, t=%d", n, m, a, b, t));

            long expectedLessEq = naiveCount(n, m, a, b, t, "<=");
            long actualLessEq = FloorSum.countLinearModLessOrEqual(n, m, a, b, t);
            assertEquals(expectedLessEq, actualLessEq, String.format("LessEq failed for n=%d, m=%d, a=%d, b=%d, t=%d", n, m, a, b, t));

            long expectedGreater = naiveCount(n, m, a, b, t, ">");
            long actualGreater = FloorSum.countLinearModGreater(n, m, a, b, t);
            assertEquals(expectedGreater, actualGreater, String.format("Greater failed for n=%d, m=%d, a=%d, b=%d, t=%d", n, m, a, b, t));

            long expectedGreaterEq = naiveCount(n, m, a, b, t, ">=");
            long actualGreaterEq = FloorSum.countLinearModGreaterOrEqual(n, m, a, b, t);
            assertEquals(expectedGreaterEq, actualGreaterEq, String.format("GreaterEq failed for n=%d, m=%d, a=%d, b=%d, t=%d", n, m, a, b, t));
        }
    }

    private long naiveCount(long n, long m, long a, long b, long t, String op) {
        long count = 0;
        for (long i = 0; i < n; i++) {
            long val = a * i + b;
            long rem = val % m;
            if (rem < 0) rem += m;

            boolean match = false;
            if (op.equals("<")) {
                match = (rem < t);
            } else if (op.equals("<=")) {
                match = (rem <= t);
            } else if (op.equals(">")) {
                match = (rem > t);
            } else if (op.equals(">=")) {
                match = (rem >= t);
            }
            if (match) count++;
        }
        return count;
    }
}
