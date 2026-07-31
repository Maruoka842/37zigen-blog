package library.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.linalg.MatrixUtilsZ;
import library.util.linalg.MatrixUtilsZn;

public class MatrixUtilsLinearEquationOnZTest {

    @Test
    void testUniqueSolution() {
        long[][] a = {
            {1, 2},
            {3, 4}
        };
        long[] b = {5, 11};
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNotNull(res);
        assertEquals(1, res.length); // 1 particular, 0 basis
        long[] x = res[0];
        assertArrayEquals(new long[]{1, 2}, x);
    }

    @Test
    void testNoSolution() {
        long[][] a = {
            {2, 4},
            {4, 8}
        };
        long[] b = {1, 2}; // 2x + 4y = 1 has no integer solution
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNull(res);
    }

    @Test
    void testNoSolutionDivisibility() {
        long[][] a = {
            {2, 0},
            {0, 2}
        };
        long[] b = {1, 1}; // 2x = 1 has no integer solution
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNull(res);
    }

    @Test
    void testInfiniteSolutions() {
        long[][] a = {
            {1, 2, 3},
            {4, 5, 6}
        };
        long[] b = {6, 15};
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNotNull(res);
        assertTrue(res.length > 1);

        long[] x = res[0];
        // Verify Ax = b
        verifyAxB(a, b, x);

        // Verify basis Ax = 0
        for (int k = 1; k < res.length; k++) {
            verifyAxB(a, new long[a.length], res[k]);
        }
    }

    @Test
    void testOverdetermined() {
        long[][] a = {
            {1, 1},
            {1, -1},
            {2, 1}
        };
        long[] b = {2, 0, 3};
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNotNull(res);
        long[] x = res[0];
        assertArrayEquals(new long[]{1, 1}, x);
    }

    @Test
    void testRankDeficient() {
        long[][] a = {
            {1, 1, 1},
            {1, 1, 1}
        };
        long[] b = {3, 3};
        long[][] res = MatrixUtilsZ.linearEquationOnZ(a, b);
        assertNotNull(res);
        assertTrue(res.length == 3); // 1 particular + 2 basis
        verifyAxB(a, b, res[0]);
        verifyAxB(a, new long[]{0, 0, 0}, res[1]);
        verifyAxB(a, new long[]{0, 0, 0}, res[2]);
    }

    @Test
    void testSmithNormalForm() {
        long[][] a = {
            {2, 4, 4},
            {-6, 6, 12},
            {10, 2, -4}
        };
        MatrixUtilsZ.SmithResult sr = MatrixUtilsZ.smithNormalForm(a);
        long[][] U = sr.U();
        long[][] S = sr.S();
        long[][] V = sr.V();
        int rank = sr.rank();

        assertEquals(2, rank);
        // Verify UAV = S
        long[][] UA = MatrixUtilsZ.mul(U, a);
        long[][] UAV = MatrixUtilsZ.mul(UA, V);
        for (int i = 0; i < S.length; i++) {
            assertArrayEquals(S[i], UAV[i], "UAV = S failed at row " + i);
        }

        // Verify Unimodularity of U, V
        long detU = MatrixUtilsZn.determinantByEuclid(U, 1_000_000_007);
        assertTrue(detU == 1 || detU == 1_000_000_007 - 1, "U should be unimodular");
        long detV = MatrixUtilsZn.determinantByEuclid(V, 1_000_000_007);
        assertTrue(detV == 1 || detV == 1_000_000_007 - 1, "V should be unimodular");

        // Verify SNF properties (diagonal, s_i | s_{i+1})
        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < S[i].length; j++) {
                if (i != j) assertEquals(0, S[i][j], "S should be diagonal");
            }
        }
        for (int i = 0; i < rank - 1; i++) {
            assertTrue(S[i + 1][i + 1] % S[i][i] == 0, "s_i should divide s_{i+1}");
        }
    }

    @Test
    void testSmithNormalFormSparsePivot() {
        long[][] a = {
            {0, 0, 0},
            {0, 0, 1},
            {0, 0, 0}
        };
        MatrixUtilsZ.SmithResult sr = MatrixUtilsZ.smithNormalForm(a);
        assertEquals(1, sr.rank());
        assertEquals(1, sr.S()[0][0]);
        // Verify UAV = S
        long[][] UA = MatrixUtilsZ.mul(sr.U(), a);
        long[][] UAV = MatrixUtilsZ.mul(UA, sr.V());
        for (int i = 0; i < sr.S().length; i++) {
            assertArrayEquals(sr.S()[i], UAV[i], "UAV = S failed in SparsePivot");
        }
    }

    private void verifyAxB(long[][] a, long[] b, long[] x) {
        int n = a.length;
        int m = a[0].length;
        for (int i = 0; i < n; i++) {
            long sum = 0;
            for (int j = 0; j < m; j++) {
                sum += a[i][j] * x[j];
            }
            assertEquals(b[i], sum, "Ax = b failed at row " + i);
        }
    }
}
