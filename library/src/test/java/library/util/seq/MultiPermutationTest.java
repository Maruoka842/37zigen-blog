package library.util.seq;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class MultiPermutationTest {

    @Test
    public void testPositions() {
        int[] a = {1, 0, 1, 2};
        int[][] res = MultiPermutation.positions(a);

        assertEquals(4, res.length);
        assertArrayEquals(new int[]{1}, res[0]);
        assertArrayEquals(new int[]{0, 2}, res[1]);
        assertArrayEquals(new int[]{3}, res[2]);
        assertArrayEquals(new int[]{}, res[3]);
    }

    @Test
    public void testPositionsEmpty() {
        int[] a = {};
        int[][] res = MultiPermutation.positions(a);
        assertEquals(0, res.length);
    }

    @Test
    public void testPositionsAllSame() {
        int[] a = {2, 2, 2};
        int[][] res = MultiPermutation.positions(a);
        assertEquals(3, res.length);
        assertArrayEquals(new int[]{}, res[0]);
        assertArrayEquals(new int[]{}, res[1]);
        assertArrayEquals(new int[]{0, 1, 2}, res[2]);
    }

    @Test
    public void testPositionsRangeLimit() {
        // According to the class Javadoc, 0 <= a[i] < a.length is assumed.
        int[] a = {0, 1};
        int[][] res = MultiPermutation.positions(a);
        assertEquals(2, res.length);
        assertArrayEquals(new int[]{0}, res[0]);
        assertArrayEquals(new int[]{1}, res[1]);
    }
}
