package library.test;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.*;
import library.util.linalg.Matrix;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RationalFunctionMatrixTest {

    static class FpField implements FieldStrategy<Long>, EuclideanDomainStrategy<Long>, ExactDivRingStrategy<Long> {
        final long mod;
        FpField(long mod) { this.mod = mod; }
        @Override public Long zero() { return 0L; }
        @Override public Long one() { return 1L; }
        @Override public Long add(Long a, Long b) { return (a + b) % mod; }
        @Override public Long sub(Long a, Long b) { return (a - b + mod) % mod; }
        @Override public Long mul(Long a, Long b) { return (a * b) % mod; }
        @Override public Long neg(Long a) { return (mod - a) % mod; }
        @Override public Long inv(Long a) {
            long b = mod, u = 1, v = 0;
            while (b > 0) {
                long t = a / b;
                a -= t * b; { long tmp = a; a = b; b = tmp; }
                u -= t * v; { long tmp = u; u = v; v = tmp; }
            }
            return (u % mod + mod) % mod;
        }
        @Override public boolean equals(Long a, Long b) { return a.equals(b); }
        @Override public Long div(Long a, Long b) { return FieldStrategy.super.div(a, b); }
        @Override public ExtGCDResult<Long> extgcd(Long a, Long b) { return FieldStrategy.super.extgcd(a, b); }
        @Override public Long mod(Long a, Long b) { return 0L; }
        @Override public long norm(Long a) { return a == 0 ? 0 : 1; }
        @Override public Long exactDiv(Long a, Long b) { return div(a, b); }
    }

    @Test
    void test3VarRationalFunctionMatrixInverse() {
        // K = F_998244353
        FpField k = new FpField(998244353L);

        // K[x]
        PolynomialEuclideanStrategy<Long> kx = new PolynomialEuclideanStrategy<>(k);
        // K(x)
        FractionFieldStrategy<Long[]> k_x = new FractionFieldStrategy<>(kx);

        // K(x)[y]
        PolynomialEuclideanStrategy<FractionFieldElement<Long[]>> kxy = new PolynomialEuclideanStrategy<>(k_x);
        // K(x, y)
        FractionFieldStrategy<FractionFieldElement<Long[]>[]> k_xy = new FractionFieldStrategy<>(kxy);

        // K(x, y)[z]
        PolynomialEuclideanStrategy<FractionFieldElement<FractionFieldElement<Long[]>[]>> kxyz = new PolynomialEuclideanStrategy<>(k_xy);
        // K(x, y, z)
        FractionFieldStrategy<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> k_xyz = new FractionFieldStrategy<>(kxyz);

        Matrix<FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>> matrixUtil = new Matrix<>(k_xyz);

        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> x = lift(k, k_x, k_xy, k_xyz);
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> y = liftY(k, k_x, k_xy, k_xyz);
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> z = liftZ(k, k_x, k_xy, k_xyz);

        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p1 = k_xyz.add(k_xyz.add(x, y), z); // x + y + z
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p2 = k_xyz.sub(x, y); // x - y
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p3 = k_xyz.mul(y, z); // y * z
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p4 = k_xyz.one(); // 1

        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] mat = createMatrix2x2(p1, p2, p3, p4);

        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] inv = matrixUtil.inv(mat);
        assertNotNull(inv);

        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] prod = matrixUtil.mul(mat, inv);
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] identity = matrixUtil.identity(2);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertTrue(k_xyz.equals(identity[i][j], prod[i][j]), "At " + i + ", " + j);
            }
        }
    }

    private FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> lift(FpField k, FractionFieldStrategy<Long[]> k_x, FractionFieldStrategy<FractionFieldElement<Long[]>[]> k_xy, FractionFieldStrategy<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> k_xyz) {
        Long[] polyX = {0L, 1L};
        FractionFieldElement<Long[]> fracX = k_x.from(polyX);

        FractionFieldElement<Long[]>[] polyY = (FractionFieldElement<Long[]>[]) new FractionFieldElement[1];
        polyY[0] = fracX;
        FractionFieldElement<FractionFieldElement<Long[]>[]> fracXY = k_xy.from(polyY);

        FractionFieldElement<FractionFieldElement<Long[]>[]>[] polyZ = (FractionFieldElement<FractionFieldElement<Long[]>[]>[]) new FractionFieldElement[1];
        polyZ[0] = fracXY;
        return k_xyz.from(polyZ);
    }

    private FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> liftY(FpField k, FractionFieldStrategy<Long[]> k_x, FractionFieldStrategy<FractionFieldElement<Long[]>[]> k_xy, FractionFieldStrategy<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> k_xyz) {
        FractionFieldElement<Long[]>[] polyY = (FractionFieldElement<Long[]>[]) new FractionFieldElement[2];
        polyY[0] = k_x.zero();
        polyY[1] = k_x.one();
        FractionFieldElement<FractionFieldElement<Long[]>[]> fracXY = k_xy.from(polyY);

        FractionFieldElement<FractionFieldElement<Long[]>[]>[] polyZ = (FractionFieldElement<FractionFieldElement<Long[]>[]>[]) new FractionFieldElement[1];
        polyZ[0] = fracXY;
        return k_xyz.from(polyZ);
    }

    private FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> liftZ(FpField k, FractionFieldStrategy<Long[]> k_x, FractionFieldStrategy<FractionFieldElement<Long[]>[]> k_xy, FractionFieldStrategy<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> k_xyz) {
        FractionFieldElement<FractionFieldElement<Long[]>[]>[] polyZ = (FractionFieldElement<FractionFieldElement<Long[]>[]>[]) new FractionFieldElement[2];
        polyZ[0] = k_xy.zero();
        polyZ[1] = k_xy.one();
        return k_xyz.from(polyZ);
    }

    private FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] createMatrix2x2(
            FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p1,
            FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p2,
            FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p3,
            FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]> p4) {
        @SuppressWarnings("unchecked")
        FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][] mat = (FractionFieldElement<FractionFieldElement<FractionFieldElement<Long[]>[]>[]>[][]) new FractionFieldElement[2][2];
        mat[0][0] = p1;
        mat[0][1] = p2;
        mat[1][0] = p3;
        mat[1][1] = p4;
        return mat;
    }
}
