package library.util.polynomial;

import org.junit.jupiter.api.Test;
import library.util.linalg.MatrixUtilsFp;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class PolynomialResultantTest {
    private final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    @Test
    public void testBaseCases() {
        // res(f, 0) = 0
        assertEquals(0, poly.resultant(new long[]{1, 2, 3}, new long[]{}));
        assertEquals(0, poly.resultant(new long[]{}, new long[]{1, 2, 3}));

        // res(f, c) = c^deg(f)
        long[] f = {1, 2, 1}; // (x+1)^2, deg=2
        long c = 3;
        assertEquals(3 * 3 % poly.mod, poly.resultant(f, new long[]{c}));

        // res(c, g) = c^deg(g)
        long[] g = {1, 2, 3, 4}; // deg=3
        assertEquals(3 * 3 * 3 % poly.mod, poly.resultant(new long[]{c}, g));
    }

    @Test
    public void testLinear() {
        // res(x-a, x-b) = a-b
        long a = 10, b = 7;
        long[] f = {poly.mod - a, 1};
        long[] g = {poly.mod - b, 1};
        assertEquals((a - b + poly.mod) % poly.mod, poly.resultant(f, g));

        // res(ax+b, cx+d) = ad-bc
        long a1 = 2, b1 = 3, c1 = 4, d1 = 5;
        f = new long[]{b1, a1};
        g = new long[]{d1, c1};
        assertEquals((a1 * d1 % poly.mod - b1 * c1 % poly.mod + poly.mod) % poly.mod, poly.resultant(f, g));
    }

    @Test
    public void testQuadratic() {
        // res(x^2+1, x-1) = 1^2+1 = 2
        long[] f = {1, 0, 1};
        long[] g = {poly.mod - 1, 1};
        assertEquals(2, poly.resultant(f, g));

        // res(x^2+1, x^2-1) = res(x^2+1, -2) = (-2)^2 = 4
        // Or product of roots: roots of x^2+1 are i, -i. (i^2-1)(-i^2-1) = (-1-1)(--1-1) = (-2)(0) wait.
        // roots of x^2+1 are i, -i. g(i) = i^2-1 = -2. g(-i) = (-i)^2-1 = -2. (-2)*(-2)=4. Correct.
        g = new long[]{poly.mod - 1, 0, 1};
        assertEquals(4, poly.resultant(f, g));
    }

    @Test
    public void testCommonFactor() {
        // Common factor (x-2)
        long[] f = {6, poly.mod - 5, 1}; // (x-2)(x-3)
        long[] g = {poly.mod - 2, 1};    // (x-2)
        assertEquals(0, poly.resultant(f, g));

        long[] h = {poly.mod - 4, 1};    // (x-4)
        long[] f2 = poly.mul(f, h);
        long[] g2 = poly.mul(g, h);
        assertEquals(0, poly.resultant(f2, g2));
    }

    @Test
    public void testSymmetry() {
        Random rnd = new Random(42);
        for (int i = 0; i < 10; i++) {
            long[] f = randomPoly(rnd, 5);
            long[] g = randomPoly(rnd, 7);
            long resFG = poly.resultant(f, g);
            long resGF = poly.resultant(g, f);

            int n = poly.deg(f);
            int m = poly.deg(g);
            if (n % 2 == 1 && m % 2 == 1) {
                assertEquals((poly.mod - resFG) % poly.mod, resGF);
            } else {
                assertEquals(resFG, resGF);
            }
        }
    }

    @Test
    public void testAgainstNaive() {
        Random rnd = new Random(42);
        for (int i = 0; i < 20; i++) {
            int degF = rnd.nextInt(50) + 1;
            int degG = rnd.nextInt(50) + 1;
            long[] f = randomPoly(rnd, degF);
            long[] g = randomPoly(rnd, degG);

            long expected = naiveResultant(poly, f, g);
            long actual = poly.resultant(f, g);
            assertEquals(expected, actual, "Failed for degF=" + degF + ", degG=" + degG);
        }
    }

    @Test
    public void testAgainstDeterminant() {
        Random rnd = new Random(123);
        for (int i = 0; i < 50; i++) {
            int degF = rnd.nextInt(30) + 1;
            int degG = rnd.nextInt(30) + 1;
            long[] f = randomPoly(rnd, degF);
            long[] g = randomPoly(rnd, degG);

            long expected = MatrixUtilsFp.modDeterminant(sylvesterMatrix(f, g), poly.mod);
            long actual = poly.resultant(f, g);
            assertEquals(expected, actual, "Failed for degF=" + degF + ", degG=" + degG);
        }
    }

    @Test
    public void testAgainstDeterminantLarge() {
        Random rnd = new Random(456);
        for (int i = 0; i < 10; i++) {
            int degF = rnd.nextInt(100) + 50;
            int degG = rnd.nextInt(100) + 50;
            long[] f = randomPoly(rnd, degF);
            long[] g = randomPoly(rnd, degG);

            long expected = MatrixUtilsFp.modDeterminant(sylvesterMatrix(f, g), poly.mod);
            long actual = poly.resultant(f, g);
            assertEquals(expected, actual, "Failed for degF=" + degF + ", degG=" + degG);
        }
    }

    @Test
    public void testDifferentModuli() {
        long[] moduli = {998244353L, 1000000007L, 1000000009L};
        Random rnd = new Random(789);
        for (long mod : moduli) {
            PolynomialFpDynamic p = PolynomialFpDynamic.of(mod);
            for (int i = 0; i < 10; i++) {
                int degF = rnd.nextInt(20) + 1;
                int degG = rnd.nextInt(20) + 1;
                long[] f = new long[degF + 1];
                long[] g = new long[degG + 1];
                for (int j = 0; j <= degF; j++) f[j] = rnd.nextLong(mod);
                for (int j = 0; j <= degG; j++) g[j] = rnd.nextLong(mod);
                if (f[degF] == 0) f[degF] = 1;
                if (g[degG] == 0) g[degG] = 1;

                long expected = MatrixUtilsFp.modDeterminant(sylvesterMatrixForMod(f, g, p), mod);
                long actual = p.resultant(f, g);
                assertEquals(expected, actual, "Failed for mod=" + mod + ", degF=" + degF + ", degG=" + degG);
            }
        }
    }

    private long[][] sylvesterMatrixForMod(long[] f, long[] g, PolynomialFpDynamic p) {
        int n = p.deg(f);
        int m = p.deg(g);
        int size = n + m;
        long[][] res = new long[size][size];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= n; j++) {
                res[i][i + j] = f[n - j];
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= m; j++) {
                res[m + i][i + j] = g[m - j];
            }
        }
        return res;
    }

    private long[][] sylvesterMatrix(long[] f, long[] g) {
        int n = poly.deg(f);
        int m = poly.deg(g);
        int size = n + m;
        long[][] res = new long[size][size];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= n; j++) {
                res[i][i + j] = f[n - j];
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= m; j++) {
                res[m + i][i + j] = g[m - j];
            }
        }
        return res;
    }

    @Test
    public void testLarge() {
        Random rnd = new Random(42);
        int deg = 1000;
        long[] f = randomPoly(rnd, deg);
        long[] g = randomPoly(rnd, deg);
        // Just check it runs in reasonable time
        long res = poly.resultant(f, g);
        // Verify symmetry at least
        long res2 = poly.resultant(g, f);
        if (deg % 2 == 1) {
            assertEquals((poly.mod - res) % poly.mod, res2);
        } else {
            assertEquals(res, res2);
        }
    }

    private long[] randomPoly(Random rnd, int deg) {
        long[] res = new long[deg + 1];
        for (int i = 0; i <= deg; i++) {
            res[i] = rnd.nextInt((int) poly.mod);
        }
        if (res[deg] == 0) res[deg] = 1;
        return res;
    }

    private long naiveResultant(PolynomialFpDynamic poly, long[] f, long[] g) {
        f = poly.resize(f); g = poly.resize(g);
        int n = poly.deg(f), m = poly.deg(g);
        if (n == -1 || m == -1) return 0;
        if (n == 0) return poly.getFp().pow(f[0], m);
        if (m == 0) return poly.getFp().pow(g[0], n);

        if (n < m) {
            long res = naiveResultant(poly, g, f);
            if (n % 2 == 1 && m % 2 == 1) return (res == 0) ? 0 : poly.mod - res;
            return res;
        }

        long[] r = poly.modNaive(f, g);
        int k = poly.deg(r);
        long factor = poly.getFp().pow(g[m], n - k);
        if (n % 2 == 1 && m % 2 == 1) factor = (factor == 0) ? 0 : poly.mod - factor;

        return factor * naiveResultant(poly, g, r) % poly.mod;
    }
}
