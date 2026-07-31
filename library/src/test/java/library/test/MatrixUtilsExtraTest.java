package library.test;

import org.junit.jupiter.api.Test;

import library.util.linalg.MatrixUtilsZ;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixUtilsExtraTest {
    @Test
    void testEmpty() {
        long[][] a = new long[0][0];
        long[][] b = new long[0][0];
        long[][] c = MatrixUtilsZ.mul(a, b);
        assertEquals(0, c.length);

        a = new long[2][0];
        b = new long[0][3];
        c = MatrixUtilsZ.mul(a, b);
        assertEquals(2, c.length);
        assertEquals(0, c[0].length);
    }

    @Test
    void testJagged() {
        long[][] a = {{1, 2}, {3}};
        long[][] b = {{4, 5}, {6, 7}};
        // c[0][0] = 1*4 + 2*6 = 16
        // c[0][1] = 1*5 + 2*7 = 19
        // c[1][0] = 3*4 = 12
        // c[1][1] = 3*5 = 15
        long[][] c = MatrixUtilsZ.mul(a, b);
        assertEquals(16, c[0][0]);
        assertEquals(19, c[0][1]);
        assertEquals(12, c[1][0]);
        assertEquals(15, c[1][1]);
    }
}
