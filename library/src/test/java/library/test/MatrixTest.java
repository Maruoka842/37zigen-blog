package library.test;

import library.util.algebra.strategy.*;
import library.util.linalg.Matrix;
import library.util.linalg.MatrixUtilsFp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class MatrixTest {

    static class DoubleField implements FieldStrategy<Double>, EuclideanDomainStrategy<Double>, CommutativeRingStrategy<Double> {
        @Override public Double zero() { return 0.0; }
        @Override public Double one() { return 1.0; }
        @Override public Double add(Double a, Double b) { return a + b; }
        @Override public Double mul(Double a, Double b) { return a * b; }
        @Override public Double neg(Double a) { return -a; }
        @Override public Double inv(Double a) { return 1.0 / a; }
        @Override public boolean equals(Double a, Double b) { return Math.abs(a - b) < 1e-9; }
        @Override public Double div(Double a, Double b) { return FieldStrategy.super.div(a, b); }
        @Override public ExtGCDResult<Double> extgcd(Double a, Double b) { return FieldStrategy.super.extgcd(a, b); }
        @Override public Double mod(Double a, Double b) { return a % b; }
        @Override public long norm(Double a) { return (long) Math.abs(a); }
    }

    static class IntegerRing implements ExactDivRingStrategy<Integer>, EuclideanDomainStrategy<Integer>, CommutativeRingStrategy<Integer> {
        @Override public Integer zero() { return 0; }
        @Override public Integer one() { return 1; }
        @Override public Integer add(Integer a, Integer b) { return a + b; }
        @Override public Integer mul(Integer a, Integer b) { return a * b; }
        @Override public Integer neg(Integer a) { return -a; }
        @Override public boolean equals(Integer a, Integer b) { return a.equals(b); }
        @Override public Integer exactDiv(Integer a, Integer b) { return a / b; }
        @Override public Integer div(Integer a, Integer b) { return a / b; }
        @Override public Integer mod(Integer a, Integer b) { return a % b; }
        @Override public long norm(Integer a) { return Math.abs(a); }
    }

    @Test
    void testBasic() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        Double[][] m1 = {{1.0, 2.0}, {3.0, 4.0}};
        Double[][] m2 = {{5.0, 6.0}, {7.0, 8.0}};

        Double[][] m3 = m.add(m1, m2);
        assertEquals(6.0, m3[0][0]);
        assertEquals(8.0, m3[0][1]);
        assertEquals(10.0, m3[1][0]);
        assertEquals(12.0, m3[1][1]);

        Double[][] m4 = m.mul(m1, m2);
        assertEquals(19.0, m4[0][0]);
        assertEquals(22.0, m4[0][1]);
        assertEquals(43.0, m4[1][0]);
        assertEquals(50.0, m4[1][1]);
    }

    @Test
    void testDet() {
        IntegerRing ir = new IntegerRing();
        Matrix<Integer> m = new Matrix<>(ir);
        Integer[][] m1 = {{1, 2}, {3, 4}};
        assertEquals(-2, m.det(m1));

        Integer[][] m3 = {
            {1, 2, 3},
            {0, 4, 5},
            {1, 0, 6}
        };
        assertEquals(22, m.det(m3));
    }

    @Test
    void testInv() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        Double[][] m1 = {{1.0, 2.0}, {3.0, 4.0}};
        Double[][] inv = m.inv(m1);
        assertNotNull(inv);
        assertTrue(Math.abs(-2.0 - inv[0][0]) < 1e-9);
        assertTrue(Math.abs(1.0 - inv[0][1]) < 1e-9);
        assertTrue(Math.abs(1.5 - inv[1][0]) < 1e-9);
        assertTrue(Math.abs(-0.5 - inv[1][1]) < 1e-9);

        Double[][] id = m.mul(m1, inv);
        assertTrue(df.equals(1.0, id[0][0]));
        assertTrue(df.equals(0.0, id[0][1]));
        assertTrue(df.equals(0.0, id[1][0]));
        assertTrue(df.equals(1.0, id[1][1]));
    }

    @Test
    void testSolve() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        Double[][] a = {{1.0, 2.0}, {3.0, 4.0}};
        Double[][] b = {{5.0}, {11.0}};
        Double[][] x = m.solve(a, b);
        assertNotNull(x);
        assertTrue(Math.abs(1.0 - x[0][0]) < 1e-9);
        assertTrue(Math.abs(2.0 - x[1][0]) < 1e-9);
    }

    @Test
    void testPow() {
        IntegerRing ir = new IntegerRing();
        Matrix<Integer> m = new Matrix<>(ir);
        Integer[][] m1 = {{1, 1}, {1, 0}};
        Integer[][] m3 = m.pow(m1, 3);
        assertEquals(3, m3[0][0]);
        assertEquals(2, m3[0][1]);
        assertEquals(2, m3[1][0]);
        assertEquals(1, m3[1][1]);

        DoubleField df = new DoubleField();
        Matrix<Double> dm = new Matrix<>(df);
        Double[][] m1d = {{1.0, 2.0}, {3.0, 4.0}};
        Double[][] inv = dm.inv(m1d);
        Double[][] powInv = dm.pow(m1d, -1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertTrue(df.equals(inv[i][j], powInv[i][j]));
            }
        }
    }

    @Test
    void testEuclidean() {
        IntegerRing ir = new IntegerRing();
        EuclideanDomainStrategy.ExtGCDResult<Integer> res = ir.extgcd(12, 18);
        assertEquals(6, res.gcd());
        assertEquals(6, 12 * res.x() + 18 * res.y());

        assertEquals(6, ir.gcd(12, 18));
    }

    @Test
    void testRREF() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        Double[][] a = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        };
        Double[][] rref = m.reducedRowEchelonForm(a);
        Double[][] expected = {
            {1.0, 0.0, -1.0},
            {0.0, 1.0, 2.0},
            {0.0, 0.0, 0.0}
        };
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertTrue(df.equals(expected[i][j], rref[i][j]), "RREF mismatch at (" + i + "," + j + ")");
            }
        }
    }

    @Test
    void testLinearEquation() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        // Case 1: Unique solution
        Double[][] a1 = {{1.0, 2.0}, {3.0, 4.0}};
        Double[] b1 = {5.0, 11.0};
        Double[][] res1 = m.linearEquation(a1, b1);
        assertNotNull(res1);
        assertEquals(1, res1.length);
        assertTrue(df.equals(1.0, res1[0][0]));
        assertTrue(df.equals(2.0, res1[0][1]));

        // Case 2: Infinitely many solutions
        Double[][] a2 = {{1.0, 2.0}, {2.0, 4.0}};
        Double[] b2 = {3.0, 6.0};
        Double[][] res2 = m.linearEquation(a2, b2);
        assertNotNull(res2);
        assertEquals(2, res2.length); // 1 particular + 1 basis
        // Particular solution x + 2y = 3, e.g., (3, 0)
        // Basis vector x + 2y = 0, e.g., (-2, 1)
        Double[] x = res2[0];
        Double[] basis = res2[1];
        assertTrue(df.equals(3.0, x[0] + 2.0 * x[1]));
        assertTrue(df.equals(0.0, basis[0] + 2.0 * basis[1]));

        // Case 3: No solution
        Double[][] a3 = {{1.0, 2.0}, {2.0, 4.0}};
        Double[] b3 = {3.0, 7.0};
        Double[][] res3 = m.linearEquation(a3, b3);
        assertNull(res3);
    }

    @Test
    void testLinearEquationEuclidean() {
        IntegerRing ir = new IntegerRing();
        Matrix<Integer> m = new Matrix<>(ir);

        // Unique solution
        Integer[][] a1 = {{1, 2}, {3, 4}};
        Integer[] b1 = {5, 11};
        Integer[][] res1 = m.linearEquation(a1, b1);
        assertNotNull(res1);
        assertEquals(1, res1.length);
        assertEquals(1, res1[0][0]);
        assertEquals(2, res1[0][1]);

        // Infinitely many solutions (Z)
        // 1x + 2y = 4
        Integer[][] a2 = {{1, 2}};
        Integer[] b2 = {4};
        Integer[][] res2 = m.linearEquation(a2, b2);
        assertNotNull(res2);
        assertEquals(2, res2.length); // 1 particular + 1 basis
        // x + 2y = 4
        // res2[0] = {4, 0} (or similar)
        // res2[1] = {-2, 1} (or similar)
        Integer[] x = res2[0];
        Integer[] basis = res2[1];
        assertEquals(4, x[0] + 2 * x[1]);
        assertEquals(0, basis[0] + 2 * basis[1]);

        // No solution (Z)
        // 2x + 4y = 5
        Integer[][] a3 = {{2, 4}};
        Integer[] b3 = {5};
        Integer[][] res3 = m.linearEquation(a3, b3);
        assertNull(res3);

        // Multi-dimensional case
        Integer[][] a4 = {
            {2, 4, 6},
            {4, 8, 12}
        };
        Integer[] b4 = {10, 20};
        Integer[][] res4 = m.linearEquation(a4, b4);
        assertNotNull(res4);
        // rank of a4 is 1. n=2, m=3.
        // nullity = 3 - 1 = 2.
        // res4.length = 1 (particular) + 2 (basis) = 3.
        assertEquals(3, res4.length);
        for (int k = 0; k < res4.length; k++) {
            int val = 2 * res4[k][0] + 4 * res4[k][1] + 6 * res4[k][2];
            if (k == 0) assertEquals(10, val);
            else assertEquals(0, val);
        }
    }

    @Test
    void testNullSpace() {
        DoubleField df = new DoubleField();
        Matrix<Double> m = new Matrix<>(df);
        Double[][] a = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        };
        Double[][] ns = m.nullSpace(a);
        assertNotNull(ns);
        assertEquals(3, ns.length);
        assertEquals(1, ns[0].length);
        // basis should be proportional to (1, -2, 1)
        // ns returns columns. ns[0][0]=1, ns[1][0]=-2, ns[2][0]=1
        assertTrue(df.equals(1.0, ns[0][0]));
        assertTrue(df.equals(-2.0, ns[1][0]));
        assertTrue(df.equals(1.0, ns[2][0]));

        // Verify AB = 0
        Double[][] b = ns;
        Double[][] zero = m.mul(a, b);
        for (int i = 0; i < zero.length; i++) {
            for (int j = 0; j < zero[0].length; j++) {
                assertTrue(df.equals(0.0, zero[i][j]));
            }
        }
    }

    @Test
    void testCompareWithMatrixUtils() {
        Random rnd = new Random(123);
        long mod = 998244353;
        FpStrategy fp = new FpStrategy(mod);
        Matrix<Long> m = new Matrix<>(fp);

        for (int t = 0; t < 50; t++) {
            int rows = rnd.nextInt(10) + 1;
            int cols = rnd.nextInt(10) + 1;
            long[][] aLong = new long[rows][cols];
            Long[][] aBoxed = new Long[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    aLong[i][j] = rnd.nextInt((int) mod);
                    aBoxed[i][j] = aLong[i][j];
                }
            }

            // RREF
            long[][] rrefUtils = MatrixUtilsFp.reducedRowEchelonFormOnFp(aLong, mod);
            Long[][] rrefMatrix = m.reducedRowEchelonForm(aBoxed);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    assertEquals(rrefUtils[i][j], rrefMatrix[i][j], "RREF mismatch at t=" + t);
                }
            }

            // NullSpace
            long[][] nsUtils = MatrixUtilsFp.nullSpace(aLong, mod);
            Long[][] nsMatrix = m.nullSpace(aBoxed);
            assertEquals(nsUtils.length, nsMatrix.length, "NullSpace rows mismatch at t=" + t);
            if (nsUtils.length > 0) {
                assertEquals(nsUtils[0].length, nsMatrix[0].length, "NullSpace cols mismatch at t=" + t);
                for (int i = 0; i < nsUtils.length; i++) {
                    for (int j = 0; j < nsUtils[0].length; j++) {
                        assertEquals(nsUtils[i][j], nsMatrix[i][j], "NullSpace value mismatch at t=" + t);
                    }
                }
            }

            // LinearEquation
            long[] bLong = new long[rows];
            Long[] bBoxed = new Long[rows];
            for (int i = 0; i < rows; i++) {
                bLong[i] = rnd.nextInt((int) mod);
                bBoxed[i] = bLong[i];
            }
            long[][] solUtils = MatrixUtilsFp.solveLinearEquation(aLong, bLong, mod);
            Long[][] solMatrix = m.linearEquation(aBoxed, bBoxed);
            if (solUtils == null) {
                assertNull(solMatrix, "LinearEquation should be null at t=" + t);
            } else {
                assertNotNull(solMatrix, "LinearEquation should not be null at t=" + t);
                assertEquals(solUtils.length, solMatrix.length, "LinearEquation solutions count mismatch at t=" + t);
                for (int i = 0; i < solUtils.length; i++) {
                    for (int j = 0; j < cols; j++) {
                        assertEquals(solUtils[i][j], solMatrix[i][j], "LinearEquation solution mismatch at t=" + t);
                    }
                }
            }
        }
    }

    @Test
    void testRandomDet() {
        Random rnd = new Random(42);
        DoubleField df = new DoubleField();
        IntegerRing ir = new IntegerRing();
        Matrix<Double> dm = new Matrix<>(df);
        Matrix<Integer> im = new Matrix<>(ir);

        for (int t = 0; t < 100; t++) {
            int n = rnd.nextInt(5) + 1; // 1 to 5
            Double[][] da = new Double[n][n];
            Integer[][] ia = new Integer[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int val = rnd.nextInt(10) - 5;
                    da[i][j] = (double) val;
                    ia[i][j] = val;
                }
            }

            double d1 = dm.detGaussian(da);
            double d2 = dm.detLeibniz(da);
            assertTrue(Math.abs(d1 - d2) < 1e-9, "Gaussian vs Leibniz (Double) failed at t=" + t + " n=" + n);

            int i1 = im.detBareiss(ia);
            int i2 = im.detEuclidean(ia);
            int i3 = im.detLeibniz(ia);
            assertEquals(i3, i1, "Bareiss vs Leibniz (Integer) failed at t=" + t);
            assertEquals(i3, i2, "Euclidean vs Leibniz (Integer) failed at t=" + t);

            int i4 = im.detMahajanVinay(ia);
            assertEquals(i3, i4, "Mahajan-Vinay vs Leibniz (Integer) failed at t=" + t);
        }
    }

    @Test
    void testIsSymmetric() {
        DoubleField df = new DoubleField();
        Matrix<Double> dm = new Matrix<>(df);

        Double[][] sym = {
            {1.0, 2.0, 3.0},
            {2.0, 4.0, 5.0},
            {3.0, 5.0, 6.0}
        };
        assertTrue(dm.isSymmetric(sym));

        Double[][] nonSym = {
            {1.0, 2.0, 3.0},
            {2.0, 4.0, 5.0},
            {3.0, 6.0, 6.0} // (2, 1) is 5, but (1, 2) is 6
        };
        assertFalse(dm.isSymmetric(nonSym));

        Double[][] nonSquare = {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 5.0}
        };
        assertFalse(dm.isSymmetric(nonSquare));
    }

    @Test
    void testDetGaussianSymmetricBasic() {
        DoubleField df = new DoubleField();
        Matrix<Double> dm = new Matrix<>(df);

        // 1x1 matrix
        Double[][] m1 = {{5.0}};
        assertEquals(5.0, dm.det(m1), 1e-9);
        assertEquals(5.0, dm.detGaussianSymmetric(m1), 1e-9);

        // 2x2 with zero diagonal: [[0, 2], [2, 0]]
        Double[][] m2 = {
            {0.0, 2.0},
            {2.0, 0.0}
        };
        assertEquals(-4.0, dm.det(m2), 1e-9);
        assertEquals(-4.0, dm.detGaussianSymmetric(m2), 1e-9);

        // 3x3 known symmetric matrix with pivot swap and non-zero det
        Double[][] m3 = {
            {0.0, 1.0, 2.0},
            {1.0, 3.0, 0.0},
            {2.0, 0.0, 4.0}
        };
        assertEquals(-16.0, dm.det(m3), 1e-9);
        assertEquals(-16.0, dm.detGaussianSymmetric(m3), 1e-9);

        // 3x3 all zero
        Double[][] m4 = {
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0}
        };
        assertEquals(0.0, dm.det(m4), 1e-9);
        assertEquals(0.0, dm.detGaussianSymmetric(m4), 1e-9);
    }

    @Test
    void testDetGaussianSymmetricRandomAndStress() {
        Random rnd = new Random(54321);
        DoubleField df = new DoubleField();
        Matrix<Double> dm = new Matrix<>(df);

        long mod = 998244353;
        FpStrategy fp = new FpStrategy(mod);
        Matrix<Long> lm = new Matrix<>(fp);

        for (int t = 0; t < 500; t++) {
            int n = rnd.nextInt(8) + 1; // 1 to 8

            // 1. Double field random symmetric matrix
            Double[][] da = new Double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n; j++) {
                    // With 20% probability, let's make diagonal elements 0 to trigger pivoting/addition
                    double val;
                    if (i == j && rnd.nextDouble() < 0.20) {
                        val = 0.0;
                    } else {
                        val = rnd.nextInt(10) - 5;
                    }
                    da[i][j] = val;
                    da[j][i] = val;
                }
            }

            double detStdDouble = dm.detGaussian(da);
            double detSymDouble = dm.detGaussianSymmetric(da);
            double detGeneralDouble = dm.det(da);
            assertEquals(detStdDouble, detSymDouble, 1e-7, "Symmetric det mismatch (Double) at trial " + t);
            assertEquals(detStdDouble, detGeneralDouble, 1e-7, "General det mismatch (Double) at trial " + t);

            // 2. Mod 998244353 (Fp field) random symmetric matrix
            Long[][] la = new Long[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n; j++) {
                    long val;
                    if (i == j && rnd.nextDouble() < 0.20) {
                        val = 0L;
                    } else {
                        val = rnd.nextInt((int) mod);
                    }
                    la[i][j] = val;
                    la[j][i] = val;
                }
            }

            long detStdLong = lm.detGaussian(la);
            long detSymLong = lm.detGaussianSymmetric(la);
            long detGeneralLong = lm.det(la);
            assertEquals(detStdLong, detSymLong, "Symmetric det mismatch (Fp) at trial " + t);
            assertEquals(detStdLong, detGeneralLong, "General det mismatch (Fp) at trial " + t);
        }
    }

    @Test
    void testCharacteristic2Symmetric() {
        FpStrategy fp2 = new FpStrategy(2L);
        Matrix<Long> m2 = new Matrix<>(fp2);

        // Symmetric matrix over F2: [[0, 1], [1, 0]]
        // Determinant is -1 = 1 mod 2.
        Long[][] mat = {
            {0L, 1L},
            {1L, 0L}
        };

        assertTrue(m2.isSymmetric(mat));

        // detGaussianSymmetric should gracefully fallback to detGaussian internally and return 1
        long detSym = m2.detGaussianSymmetric(mat);
        assertEquals(1L, detSym);

        long det = m2.det(mat);
        assertEquals(1L, det);
    }

}
