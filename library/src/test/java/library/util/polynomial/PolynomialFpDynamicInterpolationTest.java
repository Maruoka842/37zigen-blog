package library.util.polynomial;

import library.util.polynomial.PolynomialFpDynamic;
import java.util.Arrays;
import java.util.Random;

public class PolynomialFpDynamicInterpolationTest {
    static final long MOD = 998244353L;
    static final PolynomialFpDynamic poly = new PolynomialFpDynamic(MOD, 3);
    static final Random rnd = new Random(42);

    public static void main(String[] args) {
        testEmpty();
        testConstant();
        testLinear();
        testQuadratic();
        testANotOne();
        testSmallRandom();
        testLargeRandom();
        System.out.println("All interpolation tests passed!");
    }

    static void testEmpty() {
        long[] got = poly.interpolateAtGeometricProgression(new long[0], 1, 2);
        if (got.length != 0) throw new RuntimeException("FAIL empty");
    }

    static void testConstant() {
        long[] v = {5};
        long[] got = poly.interpolateAtGeometricProgression(v, 1, 2);
        assertArrayEquals(new long[]{5}, got, "constant a=1 q=2");
    }

    static void testLinear() {
        // f(x) = 2 + 3x. Points at a=1,q=2: x=1,2 -> values 5,8
        long[] v = {5, 8};
        long[] got = poly.interpolateAtGeometricProgression(v, 1, 2);
        assertArrayEquals(new long[]{2, 3}, got, "linear a=1 q=2");
    }

    static void testQuadratic() {
        // f(x) = 1 + 2x + 3x^2. Points at a=1,q=2: x=1,2,4 -> values 6, 17, 57
        long[] v = {6, 17, 57};
        long[] got = poly.interpolateAtGeometricProgression(v, 1, 2);
        assertArrayEquals(new long[]{1, 2, 3}, got, "quadratic a=1 q=2");
    }

    static void testANotOne() {
        // f(x) = 1 + 2x + 3x^2
        // a=2, q=3. Points: 2, 6, 18
        long[] coeffs = {1, 2, 3};
        long a = 2, q = 3;
        long[] v = evalAtGeometric(coeffs, a, q);
        long[] got = poly.interpolateAtGeometricProgression(v, a, q);
        assertArrayEquals(coeffs, got, "aNotOne");
    }

    static void testSmallRandom() {
        for (int t = 0; t < 500; t++) {
            int n = 1 + rnd.nextInt(30);
            long a = 1 + rnd.nextInt((int) (MOD - 1));
            long q = 1 + rnd.nextInt((int) (MOD - 1));
            if (q == 1) q = 2;

            long[] coeffs = new long[n];
            for (int i = 0; i < n; i++) coeffs[i] = rnd.nextInt((int) MOD);

            long[] v = evalAtGeometric(coeffs, a, q);
            long[] got = poly.interpolateAtGeometricProgression(v, a, q);
            assertArrayEquals(coeffs, got, "smallRandom trial=" + t + " n=" + n);
        }
    }

    static void testLargeRandom() {
        for (int t = 0; t < 30; t++) {
            int n = 100 + rnd.nextInt(900);
            long a = 1 + rnd.nextInt((int) (MOD - 1));
            long q = 2 + rnd.nextInt((int) (MOD - 2));

            long[] coeffs = new long[n];
            for (int i = 0; i < n; i++) coeffs[i] = rnd.nextInt((int) MOD);

            long[] v = evalAtGeometric(coeffs, a, q);
            long[] got = poly.interpolateAtGeometricProgression(v, a, q);
            assertArrayEquals(coeffs, got, "largeRandom trial=" + t + " n=" + n);
        }
    }

    static long[] evalAtGeometric(long[] f, long a, long q) {
        int n = f.length;
        long[] v = new long[n];
        long qi = 1;
        for (int i = 0; i < n; i++) {
            long x = a * qi % MOD;
            v[i] = evalDirect(f, x);
            qi = qi * q % MOD;
        }
        return v;
    }

    static long evalDirect(long[] f, long x) {
        long res = 0;
        long xp = 1;
        for (int i = 0; i < f.length; i++) {
            res = (res + f[i] * xp) % MOD;
            xp = xp * x % MOD;
        }
        return res;
    }

    static void assertArrayEquals(long[] expected, long[] actual, String msg) {
        if (!Arrays.equals(expected, actual)) {
            throw new RuntimeException("FAIL " + msg
                + "\nexpected=" + Arrays.toString(expected)
                + "\nactual  =" + Arrays.toString(actual));
        }
    }
}
