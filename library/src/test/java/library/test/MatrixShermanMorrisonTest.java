package library.test;

import library.util.algebra.strategy.FpStrategy;
import library.util.linalg.Matrix;
import library.util.linalg.MatrixUtilsFp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class MatrixShermanMorrisonTest {

    private static final long MOD = 998244353;
    private final FpStrategy strategy = new FpStrategy(MOD);
    private final Matrix<Long> matrix = new Matrix<>(strategy);
    private final Random rnd = new Random(42);

    @Test
    public void testRandomInvUpdateRank1() {
        for (int n : new int[]{1, 2, 3, 5, 10}) {
            for (int t = 0; t < 10; t++) {
                Long[][] A = randomMatrix(n);
                Long[][] invA = matrix.inv(A);
                if (invA == null) { t--; continue; }

                Long[] u = randomVector(n);
                Long[] v = randomVector(n);

                // Matrix<T> test
                Long[][] actualInvB = matrix.invUpdateRank1(invA, u, v);

                // MatrixUtils test
                long[][] linvA = toLongArray2D(invA);
                long[][] lactualInvB = MatrixUtilsFp.invUpdateRank1(linvA, toLongVector(u), toLongVector(v), MOD);

                // Expected
                Long[][] B = new Long[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        B[i][j] = (A[i][j] + u[i] * v[j]) % MOD;
                    }
                }
                Long[][] expectedInvB = matrix.inv(B);

                if (expectedInvB == null) {
                    assertNull(actualInvB);
                    assertNull(lactualInvB);
                } else {
                    assertArrayEquals2D(expectedInvB, actualInvB);
                    assertArrayEquals2DLong(toLongArray2D(expectedInvB), lactualInvB);
                }
            }
        }
    }

    @Test
    public void testRandomInvUpdatePoint() {
        for (int n : new int[]{1, 2, 3, 5, 10}) {
            for (int t = 0; t < 10; t++) {
                Long[][] A = randomMatrix(n);
                Long[][] invA = matrix.inv(A);
                if (invA == null) { t--; continue; }

                int r = rnd.nextInt(n);
                int c = rnd.nextInt(n);
                long nextVal = rnd.nextLong(MOD);
                long prevVal = A[r][c];

                // Matrix<T> test
                Long[][] actualInvB = matrix.invUpdatePoint(invA, r, c, nextVal, prevVal);

                // MatrixUtils test
                long[][] lactualInvB = MatrixUtilsFp.invUpdatePoint(toLongArray2D(invA), r, c, nextVal, prevVal, MOD);

                // Expected
                Long[][] B = new Long[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        B[i][j] = A[i][j];
                    }
                }
                B[r][c] = nextVal;
                Long[][] expectedInvB = matrix.inv(B);

                if (expectedInvB == null) {
                    assertNull(actualInvB);
                    assertNull(lactualInvB);
                } else {
                    assertArrayEquals2D(expectedInvB, actualInvB);
                    assertArrayEquals2DLong(toLongArray2D(expectedInvB), lactualInvB);
                }
            }
        }
    }

    @Test
    public void testRandomInvUpdateRow() {
        for (int n : new int[]{1, 2, 3, 5, 10}) {
            for (int t = 0; t < 10; t++) {
                Long[][] A = randomMatrix(n);
                Long[][] invA = matrix.inv(A);
                if (invA == null) { t--; continue; }

                int r = rnd.nextInt(n);
                Long[] nextRow = randomVector(n);
                Long[] prevRow = A[r];

                // Matrix<T> test
                Long[][] actualInvB = matrix.invUpdateRow(invA, r, nextRow, prevRow);

                // MatrixUtils test
                long[][] lactualInvB = MatrixUtilsFp.invUpdateRow(toLongArray2D(invA), r, toLongVector(nextRow), toLongVector(prevRow), MOD);

                // Expected
                Long[][] B = new Long[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        B[i][j] = A[i][j];
                    }
                }
                B[r] = nextRow;
                Long[][] expectedInvB = matrix.inv(B);

                if (expectedInvB == null) {
                    assertNull(actualInvB);
                    assertNull(lactualInvB);
                } else {
                    assertArrayEquals2D(expectedInvB, actualInvB);
                    assertArrayEquals2DLong(toLongArray2D(expectedInvB), lactualInvB);
                }
            }
        }
    }

    @Test
    public void testRandomInvUpdateCol() {
        for (int n : new int[]{1, 2, 3, 5, 10}) {
            for (int t = 0; t < 10; t++) {
                Long[][] A = randomMatrix(n);
                Long[][] invA = matrix.inv(A);
                if (invA == null) { t--; continue; }

                int c = rnd.nextInt(n);
                Long[] nextCol = randomVector(n);
                Long[] prevCol = new Long[n];
                for (int i = 0; i < n; i++) prevCol[i] = A[i][c];

                // Matrix<T> test
                Long[][] actualInvB = matrix.invUpdateCol(invA, c, nextCol, prevCol);

                // MatrixUtils test
                long[][] lactualInvB = MatrixUtilsFp.invUpdateCol(toLongArray2D(invA), c, toLongVector(nextCol), toLongVector(prevCol), MOD);

                // Expected
                Long[][] B = new Long[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        B[i][j] = A[i][j];
                    }
                }
                for (int i = 0; i < n; i++) B[i][c] = nextCol[i];
                Long[][] expectedInvB = matrix.inv(B);

                if (expectedInvB == null) {
                    assertNull(actualInvB);
                    assertNull(lactualInvB);
                } else {
                    assertArrayEquals2D(expectedInvB, actualInvB);
                    assertArrayEquals2DLong(toLongArray2D(expectedInvB), lactualInvB);
                }
            }
        }
    }

    private Long[][] randomMatrix(int n) {
        Long[][] res = new Long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = rnd.nextLong(MOD);
            }
        }
        return res;
    }

    private Long[] randomVector(int n) {
        Long[] res = new Long[n];
        for (int i = 0; i < n; i++) {
            res[i] = rnd.nextLong(MOD);
        }
        return res;
    }

    private long[][] toLongArray2D(Long[][] a) {
        int n = a.length;
        int m = a[0].length;
        long[][] res = new long[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                res[i][j] = a[i][j];
            }
        }
        return res;
    }

    private long[] toLongVector(Long[] a) {
        long[] res = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i];
        }
        return res;
    }

    private void assertArrayEquals2D(Long[][] expected, Long[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], "Mismatch at row " + i);
        }
    }

    private void assertArrayEquals2DLong(long[][] expected, long[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], "Mismatch at row " + i);
        }
    }
}
