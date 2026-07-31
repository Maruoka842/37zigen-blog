package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FpMirrorTest {
    private final Fp fp = Fp.MOD998244353;

    @Test
    public void testCatalan() {
        // n=2k, level=0 corresponds to paths from (0,0) to (2k, 0) with y >= 0
        // which are Catalan numbers C_k = 1/(k+1) * comb(2k, k)
        assertEquals(1, fp.mirror(2, 0)); // C_1 = 1
        assertEquals(2, fp.mirror(4, 0)); // C_2 = 2
        assertEquals(5, fp.mirror(6, 0)); // C_3 = 5
        assertEquals(14, fp.mirror(8, 0)); // C_4 = 14
    }

    @Test
    public void testBasic() {
        // n=3, level=-1: paths to (3, -1) staying >= -1.
        // Total paths: comb(3, 1) = 3.
        // Forbidden paths (go below -1): (0,0) -> (1,-1) -> (2,-2) -> (3,-1)
        // Only 1 forbidden path. 3 - 1 = 2.
        assertEquals(2, fp.mirror(3, -1));

        // n=4, level=-2: paths to (4, -2) staying >= -2.
        // Total paths: comb(4, 1) = 4.
        // Forbidden paths (go below -2): (0,0) -> (1,-1) -> (2,-2) -> (3,-3) -> (4,-2)
        // Only 1 forbidden path. 4 - 1 = 3.
        assertEquals(3, fp.mirror(4, -2));

        // n=1, level=1: starts at (0,0). 0 < 1, so it should be 0.
        assertEquals(0, fp.mirror(1, 1));

        // n=2, level=2: starts at (0,0). 0 < 2, so it should be 0.
        assertEquals(0, fp.mirror(2, 2));
    }

    @Test
    public void testParityAndBounds() {
        assertEquals(0, fp.mirror(3, 0)); // Parity
        assertEquals(0, fp.mirror(2, 1)); // Parity
        assertEquals(0, fp.mirror(2, 3)); // Bounds
        assertEquals(0, fp.mirror(2, -3)); // Bounds
    }

    @Test
    public void testNegativeLevel() {
        // n=2, level=-2. y >= -2. All paths to (2, -2) stay >= -2.
        // Total paths = comb(2, 0) = 1.
        assertEquals(1, fp.mirror(2, -2));
    }
}
