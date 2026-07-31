package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class PolynomialSolveLinearCongruenceTest {

    @Test
    void testCoprimeCase() {
        // Modulo 5
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);

        // A(x) = x + 1 => {1, 1}
        // B(x) = 2     => {2}
        // C(x) = x + 2 => {2, 1}
        // Solve (x+1) D(x) = 2 mod (x+2) in F_5[x]
        // Solution should be 3 => {3}
        long[] a = {1, 1};
        long[] b = {2};
        long[] c = {2, 1};

        long[] dDynamic = poly5.solveLinearCongruence(a, b, c);
        assertNotNull(dDynamic);
        assertArrayEquals(new long[]{3}, dDynamic);
    }

    @Test
    void testCommonDivisorCase() {
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);

        // A(x) = x(x+1) = x^2 + x => {0, 1, 1}
        // B(x) = x(x+2) = x^2 + 2x => {0, 2, 1}
        // C(x) = x^2 => {0, 0, 1}
        // Solve A D = B mod C
        // Solution should be 2 => {2}
        long[] a = {0, 1, 1};
        long[] b = {0, 2, 1};
        long[] c = {0, 0, 1};

        long[] d = poly5.solveLinearCongruence(a, b, c);
        assertNotNull(d);
        assertArrayEquals(new long[]{2}, d);
    }

    @Test
    void testNoSolutionCase() {
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);

        // A(x) = x(x+1) = x^2 + x => {0, 1, 1}
        // B(x) = x+2 => {2, 1}
        // C(x) = x^2 => {0, 0, 1}
        // G = gcd(A, C) = x. B is not a multiple of x, so no solution should exist.
        long[] a = {0, 1, 1};
        long[] b = {2, 1};
        long[] c = {0, 0, 1};

        long[] d = poly5.solveLinearCongruence(a, b, c);
        assertNull(d);
    }

    @Test
    void testZeroC() {
        // Modulo 5 test
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);
        long[] a = {1, 1}; // x + 1
        long[] b5 = {4, 0, 1}; // x^2 - 1 = x^2 + 4 mod 5
        long[] c = {}; // 0

        assertThrows(ArithmeticException.class, () -> poly5.solveLinearCongruence(a, b5, c));
    }

    @Test
    void testZeroAAndZeroC() {
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);

        long[] a = {};
        long[] b = {};
        long[] c = {};

        assertThrows(ArithmeticException.class, () -> poly5.solveLinearCongruence(a, b, c));
    }

    @Test
    void testConstantModulus() {
        PolynomialFpDynamic poly5 = PolynomialFpDynamic.of(5);

        // A(x) = x + 1
        // B(x) = 3x + 3
        // C(x) = 2x + 2
        // G = x + 1. C/G = 2 (constant, degree 0).
        // Since C/G has degree 0, the solution should be {}.
        long[] a = {1, 1};
        long[] b = {3, 3};
        long[] c = {2, 2};

        long[] d = poly5.solveLinearCongruence(a, b, c);
        assertNotNull(d);
        assertEquals(0, d.length);
    }

    @Test
    void testRandomStressDynamic() {
        Random rng = new Random(42);
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        for (int step = 0; step < 1000; step++) {
            int degA = rng.nextInt(10);
            int degC = rng.nextInt(10) + 1; // C != 0
            int degD = rng.nextInt(degC); // deg(D) < deg(C)

            long[] a = new long[degA + 1];
            for (int i = 0; i <= degA; i++) a[i] = rng.nextInt(100000);
            a = poly.resize(a);

            long[] c = new long[degC + 1];
            for (int i = 0; i <= degC; i++) c[i] = rng.nextInt(100000);
            if (c[degC] == 0) c[degC] = 1;
            c = poly.resize(c);

            long[] dTrue = new long[degD + 1];
            for (int i = 0; i <= degD; i++) dTrue[i] = rng.nextInt(100000);
            dTrue = poly.resize(dTrue);

            // B = A * DTrue mod C
            long[] b = poly.mod(poly.mul(a, dTrue), c);

            // Solve AD = B mod C
            long[] dSol = poly.solveLinearCongruence(a, b, c);
            assertNotNull(dSol, "Solution must exist");

            // Verify ADSol = B mod C
            long[] bSol = poly.mod(poly.mul(a, dSol), c);
            assertArrayEquals(poly.resize(b), poly.resize(bSol), "ADSol mod C must equal B mod C");

            // Verify deg(DSol) < deg(C / gcd(A, C))
            long[] g = poly.extgcd(a, c).gcd();
            long[] modulus = poly.div(c, g);
            int degMod = poly.deg(modulus);
            if (degMod == 0) {
                assertEquals(0, dSol.length);
            } else {
                assertTrue(poly.deg(dSol) < degMod, "Degree of solution must be less than modulus degree");
            }
        }
    }
}
