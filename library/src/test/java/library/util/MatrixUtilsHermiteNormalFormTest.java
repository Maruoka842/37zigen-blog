package library.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

import library.util.linalg.MatrixUtilsZ;
import library.util.linalg.MatrixUtilsZn;

public class MatrixUtilsHermiteNormalFormTest {

    @Test
    void testRowWikipediaExample() {
        long[][] a = {
            {2, 3, 6, 2},
            {5, 6, 1, 6},
            {8, 3, 1, 1}
        };

        MatrixUtilsZ.HermiteResult result = MatrixUtilsZ.rowHermiteNormalForm(a);
        assertNotNull(result);

        long[][] U = result.U();
        long[][] H = result.H();
        int rank = result.rank();

        assertEquals(3, rank);

        // Verify UA = H
        long[][] UA = MatrixUtilsZ.mul(U, a);
        for (int i = 0; i < H.length; i++) {
            assertArrayEquals(H[i], UA[i], "UA = H failed at row " + i);
        }

        // Verify U is unimodular
        long detU = MatrixUtilsZn.determinantByEuclid(U, 1_000_000_007);
        assertTrue(detU == 1 || detU == 1_000_000_007 - 1, "U should be unimodular, but got det: " + detU);

        // Verify row HNF properties
        verifyRowHermiteProperties(H, rank);

        // Check against exact expected output from Wikipedia:
        // [ 1  0  50 -11 ]
        // [ 0  3  28  -2 ]
        // [ 0  0  61 -13 ]
        long[][] expectedH = {
            {1, 0, 50, -11},
            {0, 3, 28, -2},
            {0, 0, 61, -13}
        };
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(expectedH[i], H[i], "H does not match Wikipedia expectation at row " + i);
        }
    }

    @Test
    void testColumnWikipediaExample() {
        // Transpose of Wikipedia example
        long[][] a = {
            {2, 5, 8},
            {3, 6, 3},
            {6, 1, 1},
            {2, 6, 1}
        };

        MatrixUtilsZ.HermiteResult result = MatrixUtilsZ.columnHermiteNormalForm(a);
        assertNotNull(result);

        long[][] V = result.U(); // U holds V for column HNF
        long[][] H = result.H();
        int rank = result.rank();

        assertEquals(3, rank);

        // Verify AV = H
        long[][] AV = MatrixUtilsZ.mul(a, V);
        for (int i = 0; i < H.length; i++) {
            assertArrayEquals(H[i], AV[i], "AV = H failed at row " + i);
        }

        // Verify V is unimodular
        long detV = MatrixUtilsZn.determinantByEuclid(V, 1_000_000_007);
        assertTrue(detV == 1 || detV == 1_000_000_007 - 1, "V should be unimodular");

        // Verify column HNF properties
        verifyColHermiteProperties(H, rank);

        // Check against expected output (transpose of row expectedH):
        // [  1   0   0 ]
        // [  0   3   0 ]
        // [ 50  28  61 ]
        // [-11  -2 -13 ]
        long[][] expectedH = {
            {1, 0, 0},
            {0, 3, 0},
            {50, 28, 61},
            {-11, -2, -13}
        };
        for (int i = 0; i < expectedH.length; i++) {
            assertArrayEquals(expectedH[i], H[i], "H does not match Wikipedia expectation at row " + i);
        }
    }

    @Test
    void testRankDeficientMatrix() {
        long[][] a = {
            {1, 2, 3},
            {2, 4, 6},
            {3, 6, 9}
        };

        // Row HNF
        MatrixUtilsZ.HermiteResult rowRes = MatrixUtilsZ.rowHermiteNormalForm(a);
        assertEquals(1, rowRes.rank());
        verifyRowHermiteProperties(rowRes.H(), rowRes.rank());
        long[][] UA = MatrixUtilsZ.mul(rowRes.U(), a);
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(rowRes.H()[i], UA[i]);
        }

        // Column HNF
        MatrixUtilsZ.HermiteResult colRes = MatrixUtilsZ.columnHermiteNormalForm(a);
        assertEquals(1, colRes.rank());
        verifyColHermiteProperties(colRes.H(), colRes.rank());
        long[][] AV = MatrixUtilsZ.mul(a, colRes.U());
        for (int i = 0; i < a.length; i++) {
            assertArrayEquals(colRes.H()[i], AV[i]);
        }
    }

    @Test
    void testEmptyAndSingleRowCol() {
        // Empty row test
        MatrixUtilsZ.HermiteResult emptyRow = MatrixUtilsZ.rowHermiteNormalForm(new long[0][0]);
        assertEquals(0, emptyRow.rank());
        assertEquals(0, emptyRow.U().length);
        assertEquals(0, emptyRow.H().length);

        // Empty col test (n > 0, m == 0)
        MatrixUtilsZ.HermiteResult emptyCol = MatrixUtilsZ.rowHermiteNormalForm(new long[3][0]);
        assertEquals(0, emptyCol.rank());
        assertEquals(3, emptyCol.U().length);
        assertEquals(3, emptyCol.H().length);

        // Single element positive row
        long[][] singlePos = {{5}};
        MatrixUtilsZ.HermiteResult resSinglePos = MatrixUtilsZ.rowHermiteNormalForm(singlePos);
        assertEquals(1, resSinglePos.rank());
        assertEquals(5, resSinglePos.H()[0][0]);
        assertEquals(1, resSinglePos.U()[0][0]);

        // Single element negative row
        long[][] singleNeg = {{-3}};
        MatrixUtilsZ.HermiteResult resSingleNeg = MatrixUtilsZ.rowHermiteNormalForm(singleNeg);
        assertEquals(1, resSingleNeg.rank());
        assertEquals(3, resSingleNeg.H()[0][0]);
        assertEquals(-1, resSingleNeg.U()[0][0]);
    }

    @Test
    void testRectangularMatrices() {
        Random rng = new Random(42);
        // Test many random matrices with n < m
        for (int t = 0; t < 10; t++) {
            int n = 4;
            int m = 6;
            long[][] a = new long[n][m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    a[i][j] = rng.nextInt(21) - 10;
                }
            }
            // Row HNF
            MatrixUtilsZ.HermiteResult rowRes = MatrixUtilsZ.rowHermiteNormalForm(a);
            long[][] UA = MatrixUtilsZ.mul(rowRes.U(), a);
            for (int i = 0; i < n; i++) assertArrayEquals(rowRes.H()[i], UA[i]);
            long detU = MatrixUtilsZn.determinantByEuclid(rowRes.U(), 1_000_000_007);
            assertTrue(detU == 1 || detU == 1_000_000_007 - 1);
            verifyRowHermiteProperties(rowRes.H(), rowRes.rank());

            // Column HNF
            MatrixUtilsZ.HermiteResult colRes = MatrixUtilsZ.columnHermiteNormalForm(a);
            long[][] AV = MatrixUtilsZ.mul(a, colRes.U());
            for (int i = 0; i < n; i++) assertArrayEquals(colRes.H()[i], AV[i]);
            long detV = MatrixUtilsZn.determinantByEuclid(colRes.U(), 1_000_000_007);
            assertTrue(detV == 1 || detV == 1_000_000_007 - 1);
            verifyColHermiteProperties(colRes.H(), colRes.rank());
        }

        // Test many random matrices with n > m
        for (int t = 0; t < 10; t++) {
            int n = 6;
            int m = 4;
            long[][] a = new long[n][m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    a[i][j] = rng.nextInt(21) - 10;
                }
            }
            // Row HNF
            MatrixUtilsZ.HermiteResult rowRes = MatrixUtilsZ.rowHermiteNormalForm(a);
            long[][] UA = MatrixUtilsZ.mul(rowRes.U(), a);
            for (int i = 0; i < n; i++) assertArrayEquals(rowRes.H()[i], UA[i]);
            long detU = MatrixUtilsZn.determinantByEuclid(rowRes.U(), 1_000_000_007);
            assertTrue(detU == 1 || detU == 1_000_000_007 - 1);
            verifyRowHermiteProperties(rowRes.H(), rowRes.rank());

            // Column HNF
            MatrixUtilsZ.HermiteResult colRes = MatrixUtilsZ.columnHermiteNormalForm(a);
            long[][] AV = MatrixUtilsZ.mul(a, colRes.U());
            for (int i = 0; i < n; i++) assertArrayEquals(colRes.H()[i], AV[i]);
            long detV = MatrixUtilsZn.determinantByEuclid(colRes.U(), 1_000_000_007);
            assertTrue(detV == 1 || detV == 1_000_000_007 - 1);
            verifyColHermiteProperties(colRes.H(), colRes.rank());
        }
    }

    @Test
    void testZeroMatrix() {
        long[][] a = {
            {0, 0, 0},
            {0, 0, 0}
        };
        MatrixUtilsZ.HermiteResult result = MatrixUtilsZ.rowHermiteNormalForm(a);
        assertEquals(0, result.rank());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(0, result.H()[i][j]);
            }
        }
        long detU = MatrixUtilsZn.determinantByEuclid(result.U(), 1_000_000_007);
        assertTrue(detU == 1 || detU == 1_000_000_007 - 1);
    }

    private void verifyRowHermiteProperties(long[][] H, int rank) {
        int n = H.length;
        if (n == 0) return;
        int m = H[0].length;
        if (m == 0) return;

        // 1. Zero rows are at the bottom
        for (int i = rank; i < n; i++) {
            for (int j = 0; j < m; j++) {
                assertEquals(0, H[i][j]);
            }
        }

        // 2. Find pivots and verify properties
        int lastPivotCol = -1;
        for (int i = 0; i < rank; i++) {
            int pivotCol = -1;
            for (int j = 0; j < m; j++) {
                if (H[i][j] != 0) {
                    pivotCol = j;
                    break;
                }
            }
            assertTrue(pivotCol != -1);
            assertTrue(pivotCol > lastPivotCol);
            lastPivotCol = pivotCol;

            long pivotVal = H[i][pivotCol];
            assertTrue(pivotVal > 0);

            for (int r = i + 1; r < n; r++) {
                assertEquals(0, H[r][pivotCol]);
            }

            for (int r = 0; r < i; r++) {
                long valAbove = H[r][pivotCol];
                assertTrue(valAbove >= 0 && valAbove < pivotVal);
            }
        }
    }

    private void verifyColHermiteProperties(long[][] H, int rank) {
        int n = H.length;
        if (n == 0) return;
        int m = H[0].length;
        if (m == 0) return;

        // 1. Zero columns are on the right
        for (int j = rank; j < m; j++) {
            for (int i = 0; i < n; i++) {
                assertEquals(0, H[i][j]);
            }
        }

        // 2. Find pivots and verify properties
        int lastPivotRow = -1;
        for (int j = 0; j < rank; j++) {
            int pivotRow = -1;
            for (int i = 0; i < n; i++) {
                if (H[i][j] != 0) {
                    pivotRow = i;
                    break;
                }
            }
            assertTrue(pivotRow != -1);
            assertTrue(pivotRow > lastPivotRow);
            lastPivotRow = pivotRow;

            long pivotVal = H[pivotRow][j];
            assertTrue(pivotVal > 0);

            for (int c = j + 1; c < m; c++) {
                assertEquals(0, H[pivotRow][c]);
            }

            for (int c = 0; c < j; c++) {
                long valLeft = H[pivotRow][c];
                assertTrue(valLeft >= 0 && valLeft < pivotVal);
            }
        }
    }
}
