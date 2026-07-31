package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ArrayUtilsTest {

    @Test
    public void testMaxExceptSelf() {
        long[] a = {10, 5, 10, 8};
        long[] expected = {10, 10, 10, 10};
        assertArrayEquals(expected, ArrayUtils.maxExceptSelf(a));

        long[] b = {10, 5, 3, 8};
        long[] expectedB = {8, 10, 10, 10};
        assertArrayEquals(expectedB, ArrayUtils.maxExceptSelf(b));

        long[] c = {10};
        long[] expectedC = {Long.MIN_VALUE / 3};
        assertArrayEquals(expectedC, ArrayUtils.maxExceptSelf(c));
    }

    @Test
    public void testMinExceptSelf() {
        long[] a = {3, 8, 3, 5};
        long[] expected = {3, 3, 3, 3};
        assertArrayEquals(expected, ArrayUtils.minExceptSelf(a));

        long[] b = {3, 8, 4, 5};
        long[] expectedB = {4, 3, 3, 3};
        assertArrayEquals(expectedB, ArrayUtils.minExceptSelf(b));
    }

    @Test
    public void testSecondMaxLong() {
        assertEquals(10, ArrayUtils.secondMax(new long[]{10, 5, 10, 8}));
        assertEquals(8, ArrayUtils.secondMax(new long[]{10, 5, 3, 8}));
        assertEquals(5, ArrayUtils.secondMax(new long[]{5, 5}));
        assertEquals(Long.MIN_VALUE, ArrayUtils.secondMax(new long[]{10}));
        assertEquals(Long.MIN_VALUE, ArrayUtils.secondMax(new long[]{}));
    }

    @Test
    public void testSecondMaxInt() {
        assertEquals(10, ArrayUtils.secondMax(new int[]{10, 5, 10, 8}));
        assertEquals(8, ArrayUtils.secondMax(new int[]{10, 5, 3, 8}));
        assertEquals(5, ArrayUtils.secondMax(new int[]{5, 5}));
        assertEquals(Integer.MIN_VALUE, ArrayUtils.secondMax(new int[]{10}));
        assertEquals(Integer.MIN_VALUE, ArrayUtils.secondMax(new int[]{}));
    }

    @Test
    public void testSecondMaxDouble() {
        assertEquals(10.0, ArrayUtils.secondMax(new double[]{10.0, 5.0, 10.0, 8.0}), 1e-9);
        assertEquals(8.0, ArrayUtils.secondMax(new double[]{10.0, 5.0, 3.0, 8.0}), 1e-9);
        assertEquals(5.0, ArrayUtils.secondMax(new double[]{5.0, 5.0}), 1e-9);
        assertEquals(Double.NEGATIVE_INFINITY, ArrayUtils.secondMax(new double[]{10.0}), 1e-9);
        assertEquals(Double.NEGATIVE_INFINITY, ArrayUtils.secondMax(new double[]{}), 1e-9);
    }

    @Test
    public void testSecondMinLong() {
        assertEquals(3, ArrayUtils.secondMin(new long[]{3, 8, 3, 5}));
        assertEquals(4, ArrayUtils.secondMin(new long[]{3, 8, 4, 5}));
        assertEquals(5, ArrayUtils.secondMin(new long[]{5, 5}));
        assertEquals(Long.MAX_VALUE, ArrayUtils.secondMin(new long[]{3}));
        assertEquals(Long.MAX_VALUE, ArrayUtils.secondMin(new long[]{}));
    }

    @Test
    public void testSecondMinInt() {
        assertEquals(3, ArrayUtils.secondMin(new int[]{3, 8, 3, 5}));
        assertEquals(4, ArrayUtils.secondMin(new int[]{3, 8, 4, 5}));
        assertEquals(5, ArrayUtils.secondMin(new int[]{5, 5}));
        assertEquals(Integer.MAX_VALUE, ArrayUtils.secondMin(new int[]{3}));
        assertEquals(Integer.MAX_VALUE, ArrayUtils.secondMin(new int[]{}));
    }

    @Test
    public void testSecondMinDouble() {
        assertEquals(3.0, ArrayUtils.secondMin(new double[]{3.0, 8.0, 3.0, 5.0}), 1e-9);
        assertEquals(4.0, ArrayUtils.secondMin(new double[]{3.0, 8.0, 4.0, 5.0}), 1e-9);
        assertEquals(5.0, ArrayUtils.secondMin(new double[]{5.0, 5.0}), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, ArrayUtils.secondMin(new double[]{3.0}), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, ArrayUtils.secondMin(new double[]{}), 1e-9);
    }

    @Test
    public void testCharGridRotation() {
        char[][] grid = {
            {'1', '2', '3'},
            {'4', '5', '6'}
        };
        char[][] rotatedLeft = {
            {'3', '6'},
            {'2', '5'},
            {'1', '4'}
        };
        char[][] rotatedRight = {
            {'4', '1'},
            {'5', '2'},
            {'6', '3'}
        };

        assertDeepEquals(rotatedLeft, ArrayUtils.rotateLeftGrid(grid));
        assertDeepEquals(rotatedRight, ArrayUtils.rotateRightGrid(grid));

        // Rotating left 4 times returns to the original
        char[][] current = grid;
        for (int i = 0; i < 4; i++) {
            current = ArrayUtils.rotateLeftGrid(current);
        }
        assertDeepEquals(grid, current);

        // Rotating right then left returns to the original
        assertDeepEquals(grid, ArrayUtils.rotateLeftGrid(ArrayUtils.rotateRightGrid(grid)));
        assertDeepEquals(grid, ArrayUtils.rotateRightGrid(ArrayUtils.rotateLeftGrid(grid)));
    }

    @Test
    public void testIntGridRotation() {
        int[][] grid = {
            {1, 2, 3},
            {4, 5, 6}
        };
        int[][] rotatedLeft = {
            {3, 6},
            {2, 5},
            {1, 4}
        };
        int[][] rotatedRight = {
            {4, 1},
            {5, 2},
            {6, 3}
        };

        assertDeepEquals(rotatedLeft, ArrayUtils.rotateLeftGrid(grid));
        assertDeepEquals(rotatedRight, ArrayUtils.rotateRightGrid(grid));

        // Rotating left 4 times returns to the original
        int[][] current = grid;
        for (int i = 0; i < 4; i++) {
            current = ArrayUtils.rotateLeftGrid(current);
        }
        assertDeepEquals(grid, current);

        // Rotating right then left returns to the original
        assertDeepEquals(grid, ArrayUtils.rotateLeftGrid(ArrayUtils.rotateRightGrid(grid)));
        assertDeepEquals(grid, ArrayUtils.rotateRightGrid(ArrayUtils.rotateLeftGrid(grid)));
    }

    @Test
    public void testLongGridRotation() {
        long[][] grid = {
            {1L, 2L, 3L},
            {4L, 5L, 6L}
        };
        long[][] rotatedLeft = {
            {3L, 6L},
            {2L, 5L},
            {1L, 4L}
        };
        long[][] rotatedRight = {
            {4L, 1L},
            {5L, 2L},
            {6L, 3L}
        };

        assertDeepEquals(rotatedLeft, ArrayUtils.rotateLeftGrid(grid));
        assertDeepEquals(rotatedRight, ArrayUtils.rightRotateGrid(grid));

        // Rotating left 4 times returns to the original
        long[][] current = grid;
        for (int i = 0; i < 4; i++) {
            current = ArrayUtils.rotateLeftGrid(current);
        }
        assertDeepEquals(grid, current);

        // Rotating right then left returns to the original
        assertDeepEquals(grid, ArrayUtils.rotateLeftGrid(ArrayUtils.rightRotateGrid(grid)));
        assertDeepEquals(grid, ArrayUtils.rightRotateGrid(ArrayUtils.rotateLeftGrid(grid)));
    }

    private void assertDeepEquals(char[][] expected, char[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    private void assertDeepEquals(int[][] expected, int[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    private void assertDeepEquals(long[][] expected, long[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }
}
