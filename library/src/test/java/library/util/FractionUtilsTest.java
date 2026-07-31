package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FractionUtilsTest {

    @Test
    public void testContinuedFractionSqrt() {
        assertArrayEquals(new long[]{0}, FractionUtils.continuedFractionSqrt(0));
        assertArrayEquals(new long[]{1}, FractionUtils.continuedFractionSqrt(1));
        assertArrayEquals(new long[]{1, 2}, FractionUtils.continuedFractionSqrt(2));
        assertArrayEquals(new long[]{1, 1, 2}, FractionUtils.continuedFractionSqrt(3));
        assertArrayEquals(new long[]{2}, FractionUtils.continuedFractionSqrt(4));
        assertArrayEquals(new long[]{2, 4}, FractionUtils.continuedFractionSqrt(5));
        assertArrayEquals(new long[]{2, 2, 4}, FractionUtils.continuedFractionSqrt(6));
        assertArrayEquals(new long[]{2, 1, 1, 1, 4}, FractionUtils.continuedFractionSqrt(7));
        assertArrayEquals(new long[]{2, 1, 4}, FractionUtils.continuedFractionSqrt(8));
        assertArrayEquals(new long[]{3}, FractionUtils.continuedFractionSqrt(9));
        assertArrayEquals(new long[]{3, 6}, FractionUtils.continuedFractionSqrt(10));
        assertArrayEquals(new long[]{3, 3, 6}, FractionUtils.continuedFractionSqrt(11));
        assertArrayEquals(new long[]{3, 2, 6}, FractionUtils.continuedFractionSqrt(12));
        assertArrayEquals(new long[]{3, 1, 1, 1, 1, 6}, FractionUtils.continuedFractionSqrt(13));
        assertArrayEquals(new long[]{3, 1, 2, 1, 6}, FractionUtils.continuedFractionSqrt(14));
        assertArrayEquals(new long[]{3, 1, 6}, FractionUtils.continuedFractionSqrt(15));
    }

    @Test
    public void testSqrtLarge() {
        assertEquals(1000000000L, MathUtils.sqrt(1000000000000000000L));
        assertEquals(3037000499L, MathUtils.sqrt(9223372030926249001L));
    }
}
