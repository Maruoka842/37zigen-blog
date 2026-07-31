package library.test;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.*;
import library.util.linalg.Matrix;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FractionFieldTest {

    static class IntegerRing implements EuclideanDomainStrategy<Integer>, CommutativeRingStrategy<Integer> {
        @Override public Integer zero() { return 0; }
        @Override public Integer one() { return 1; }
        @Override public Integer add(Integer a, Integer b) { return a + b; }
        @Override public Integer mul(Integer a, Integer b) { return a * b; }
        @Override public Integer neg(Integer a) { return -a; }
        @Override public boolean equals(Integer a, Integer b) { return a.equals(b); }
        @Override public Integer div(Integer a, Integer b) { return a / b; }
        @Override public Integer mod(Integer a, Integer b) { return a % b; }
        @Override public long norm(Integer a) { return Math.abs(a); }

        @Override
        public Integer gcd(Integer a, Integer b) {
            a = Math.abs(a);
            b = Math.abs(b);
            while (b != 0) {
                a %= b;
                int t = a; a = b; b = t;
            }
            return a;
        }

        @Override
        public Integer canonicalUnit(Integer a) {
            return a < 0 ? -1 : 1;
        }
    }

    @Test
    void testBasicArithmetic() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);

        FractionFieldElement<Integer> a = ffs.of(1, 2);
        FractionFieldElement<Integer> b = ffs.of(1, 3);

        // 1/2 + 1/3 = 5/6
        FractionFieldElement<Integer> sum = ffs.add(a, b);
        assertTrue(ffs.equals(ffs.of(5, 6), sum));

        // 1/2 * 1/3 = 1/6
        FractionFieldElement<Integer> prod = ffs.mul(a, b);
        assertTrue(ffs.equals(ffs.of(1, 6), prod));

        // 1/2 - 1/3 = 1/6
        FractionFieldElement<Integer> diff = ffs.sub(a, b);
        assertTrue(ffs.equals(ffs.of(1, 6), diff));

        // (1/2) / (1/3) = 3/2
        FractionFieldElement<Integer> quot = ffs.mul(a, ffs.inv(b));
        assertTrue(ffs.equals(ffs.of(3, 2), quot));
    }

    @Test
    void testSimplification() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);

        FractionFieldElement<Integer> a = ffs.of(2, 4);
        // Should be simplified to 1/2 if EuclideanDomainStrategy is used
        assertEquals(1, a.num());
        assertEquals(2, a.den());

        FractionFieldElement<Integer> b = ffs.of(10, 5);
        assertEquals(2, b.num());
        assertEquals(1, b.den());

        // Test normalization with canonicalUnit
        FractionFieldElement<Integer> c = ffs.of(1, -2);
        assertEquals(-1, c.num());
        assertEquals(2, c.den());

        FractionFieldElement<Integer> d = ffs.of(-2, -4);
        assertEquals(1, d.num());
        assertEquals(2, d.den());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMatrixIntegration() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);
        Matrix<FractionFieldElement<Integer>> matrix = new Matrix<>(ffs);

        // [ 1/2  1/3 ]
        // [ 1/4  1/5 ]
        FractionFieldElement<Integer>[][] m1 = (FractionFieldElement<Integer>[][]) new FractionFieldElement[][]{
            {ffs.of(1, 2), ffs.of(1, 3)},
            {ffs.of(1, 4), ffs.of(1, 5)}
        };

        // Det = (1/2 * 1/5) - (1/3 * 1/4) = 1/10 - 1/12 = 6/60 - 5/60 = 1/60
        FractionFieldElement<Integer> det = matrix.det(m1);
        assertTrue(ffs.equals(ffs.of(1, 60), det));

        // Inverse
        // [  12   -20 ]
        // [ -15    30 ]  * 1/60 ??? No.
        // Inverse of [a b; c d] is 1/(ad-bc) * [d -b; -c a]
        // 60 * [1/5 -1/3; -1/4 1/2] = [12 -20; -15 30]
        FractionFieldElement<Integer>[][] inv = matrix.inv(m1);
        assertNotNull(inv);
        assertTrue(ffs.equals(ffs.of(12, 1), inv[0][0]));
        assertTrue(ffs.equals(ffs.of(-20, 1), inv[0][1]));
        assertTrue(ffs.equals(ffs.of(-15, 1), inv[1][0]));
        assertTrue(ffs.equals(ffs.of(30, 1), inv[1][1]));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSolve() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);
        Matrix<FractionFieldElement<Integer>> matrix = new Matrix<>(ffs);

        // 1/2x + 1/3y = 1
        // 1/4x + 1/5y = 0
        FractionFieldElement<Integer>[][] a = (FractionFieldElement<Integer>[][]) new FractionFieldElement[][]{
            {ffs.of(1, 2), ffs.of(1, 3)},
            {ffs.of(1, 4), ffs.of(1, 5)}
        };
        FractionFieldElement<Integer>[][] b = (FractionFieldElement<Integer>[][]) new FractionFieldElement[][]{
            {ffs.of(1, 1)},
            {ffs.of(0, 1)}
        };

        FractionFieldElement<Integer>[][] x = matrix.solve(a, b);
        assertNotNull(x);

        // x = 12, y = -15
        // 1/2(12) + 1/3(-15) = 6 - 5 = 1 (OK)
        // 1/4(12) + 1/5(-15) = 3 - 3 = 0 (OK)
        assertTrue(ffs.equals(ffs.of(12, 1), x[0][0]));
        assertTrue(ffs.equals(ffs.of(-15, 1), x[1][0]));
    }

    @Test
    void testExtGCD() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);

        FractionFieldElement<Integer> a = ffs.of(1, 2);
        FractionFieldElement<Integer> b = ffs.of(1, 3);

        EuclideanDomainStrategy.ExtGCDResult<FractionFieldElement<Integer>> res = ffs.extgcd(a, b);
        assertTrue(ffs.equals(ffs.one(), res.gcd()));
        // a * x + b * y = gcd
        FractionFieldElement<Integer> combined = ffs.add(ffs.mul(a, res.x()), ffs.mul(b, res.y()));
        assertTrue(ffs.equals(res.gcd(), combined));

        // Test with zero
        EuclideanDomainStrategy.ExtGCDResult<FractionFieldElement<Integer>> resZero = ffs.extgcd(ffs.zero(), b);
        assertTrue(ffs.equals(ffs.one(), resZero.gcd()));
        assertTrue(ffs.equals(ffs.zero(), resZero.x()));
        assertTrue(ffs.equals(ffs.inv(b), resZero.y()));

        EuclideanDomainStrategy.ExtGCDResult<FractionFieldElement<Integer>> resBothZero = ffs.extgcd(ffs.zero(), ffs.zero());
        assertTrue(ffs.equals(ffs.zero(), resBothZero.gcd()));
    }

    @Test
    void testGeometricSum() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);

        // a = 1/2
        // 1 + a + a^2 + ... = 1 / (1 - a) = 1 / (1 - 1/2) = 1 / (1/2) = 2
        FractionFieldElement<Integer> a = ffs.of(1, 2);
        FractionFieldElement<Integer> res = ffs.geometricSum(a);
        assertTrue(ffs.equals(ffs.of(2, 1), res));

        // a = 2/3
        // 1 / (1 - 2/3) = 1 / (1/3) = 3
        FractionFieldElement<Integer> b = ffs.of(2, 3);
        FractionFieldElement<Integer> resB = ffs.geometricSum(b);
        assertTrue(ffs.equals(ffs.of(3, 1), resB));
    }

    @Test
    void testFractionCanonicalization() {
        IntegerRing ir = new IntegerRing();
        FractionFieldStrategy<Integer> ffs = new FractionFieldStrategy<>(ir);

        FractionFieldElement<Integer> a = ffs.of(2, 1);
        FractionFieldElement<Integer> u = ffs.canonicalUnit(a);
        assertTrue(ffs.equals(a, u));
        assertTrue(ffs.equals(ffs.one(), ffs.div(a, u)));

        FractionFieldElement<Integer> zero = ffs.zero();
        assertTrue(ffs.equals(ffs.one(), ffs.canonicalUnit(zero)));

        // GCD should be one for non-zero fractions
        FractionFieldElement<Integer> b = ffs.of(1, 3);
        assertTrue(ffs.equals(ffs.one(), ffs.gcd(a, b)));
    }
}
