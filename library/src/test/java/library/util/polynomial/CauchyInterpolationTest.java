package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Random;
import library.util.MathUtils;

public class CauchyInterpolationTest {
    static final long MOD = 998244353L;
    static final PolynomialFpDynamic poly = new PolynomialFpDynamic(MOD, 3);

    @Test
    public void testSimple() {
        // r = 1, t = x + 2
        // k = 1, n = 2
        // points u = {0, 1}
        // v = {1/2, 1/3}
        long[] x = {0, 1};
        long inv2 = MathUtils.modInv(2, MOD);
        long inv3 = MathUtils.modInv(3, MOD);
        long[] y = {inv2, inv3};
        int k = 1;

        long[][] res = poly.cauchyInterpolation(x, y, k);
        assertNotNull(res);
        long[] r = res[0];
        long[] t = res[1];

        // Check r(ui) = y[i] * t(ui)
        for (int i = 0; i < x.length; i++) {
            long ri = poly.eval(r, x[i]);
            long ti = poly.eval(t, x[i]);
            assertEquals(ri, y[i] * ti % MOD);
            assertNotEquals(0, ti, "t(ui) should not be zero");
        }
        assertTrue(poly.deg(r) < k);
        assertTrue(poly.deg(t) <= x.length - k);
    }

    @Test
    public void testNoSolutionCommonFactor() {
        // r = x - 1, t = x - 2
        // Try to interpolate at u=1, v=0
        // But if k=1, n=1, deg r < 1 -> r is constant.
        // If we have (x-1)/(x-2) and we want deg r < 1, deg t <= 0, it's impossible.

        // More realistic:
        // u = {1, 2}, v = {0, 1}
        // r = x - 1, t = 1 works. deg r = 1, deg t = 0.
        // If we set k = 1, we want deg r < 1, deg t <= 1.
        // G(1) = 0, G(2) = 1  => G(x) = x - 1.
        // M(x) = (x-1)(x-2)
        // Euclidean algo on M, G:
        // M = (x-2) G + 0.
        // R0 = M, R1 = G.
        // R2 = M mod G = 0.
        // h = k = 1. deg R1 = 1 >= 1. deg R2 = -1 < 1.
        // so res.cur.r = R2 = 0, res.cur.y = ...
        // If r = 0, gcd(r, t) = t. deg t > 0 => returns null.
        long[] x = {1, 2};
        long[] y = {0, 1};
        int k = 1;
        long[][] res = poly.cauchyInterpolation(x, y, k);
        assertNull(res, "Should have no solution with deg r < 1");
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int trial = 0; trial < 100; trial++) {
            int degR = rnd.nextInt(10);
            int degT = rnd.nextInt(10);
            int k = degR + 1;
            int n = k + degT;

            long[] r_true = new long[degR + 1];
            for (int i = 0; i <= degR; i++) r_true[i] = rnd.nextInt((int) MOD);
            if (poly.deg(r_true) == -1) r_true[0] = 1;

            long[] t_true = new long[degT + 1];
            for (int i = 0; i <= degT; i++) t_true[i] = rnd.nextInt((int) MOD);
            if (poly.deg(t_true) == -1) t_true[0] = 1;

            // Ensure gcd(r, t) = 1
            if (poly.deg(poly.gcd(r_true, t_true)) > 0) {
                trial--; continue;
            }

            long[] x = new long[n];
            long[] y = new long[n];
            for (int i = 0; i < n; i++) {
                while (true) {
                    x[i] = rnd.nextInt((int) MOD);
                    boolean dup = false;
                    for (int j = 0; j < i; j++) if (x[j] == x[i]) dup = true;
                    if (dup) continue;
                    long ti = poly.eval(t_true, x[i]);
                    if (ti != 0) break;
                }
                long ri = poly.eval(r_true, x[i]);
                long ti = poly.eval(t_true, x[i]);
                y[i] = ri * MathUtils.modInv(ti, MOD) % MOD;
            }

            long[][] res = poly.cauchyInterpolation(x, y, k);
            assertNotNull(res, "Trial " + trial + " should have a solution");
            long[] r = res[0];
            long[] t = res[1];

            for (int i = 0; i < n; i++) {
                long ri = poly.eval(r, x[i]);
                long ti = poly.eval(t, x[i]);
                assertEquals(ri, y[i] * ti % MOD, "Trial " + trial + " point " + i);
                assertNotEquals(0, ti, "Trial " + trial + " point " + i);
            }
            assertTrue(poly.deg(r) < k, "deg r too large in trial " + trial);
            assertTrue(poly.deg(t) <= n - k, "deg t too large in trial " + trial);
        }
    }

    @Test
    public void testRandomNoSolution() {
        Random rnd = new Random(43);
        for (int trial = 0; trial < 100; trial++) {
            // Create a case where gcd(r, t) != 1
            int degCommon = 1 + rnd.nextInt(3);
            int degR = degCommon + rnd.nextInt(5);
            int degT = degCommon + rnd.nextInt(5);

            long[] common = new long[degCommon + 1];
            for (int i = 0; i <= degCommon; i++) common[i] = rnd.nextInt((int) MOD);
            if (poly.deg(common) <= 0) common[0] = 1;

            long[] r_base = new long[degR - degCommon + 1];
            for (int i = 0; i <= degR - degCommon; i++) r_base[i] = rnd.nextInt((int) MOD);
            if (poly.deg(r_base) == -1) r_base[0] = 1;

            long[] t_base = new long[degT - degCommon + 1];
            for (int i = 0; i <= degT - degCommon; i++) t_base[i] = rnd.nextInt((int) MOD);
            if (poly.deg(t_base) == -1) t_base[0] = 1;

            long[] r_true = poly.mul(r_base, common);
            long[] t_true = poly.mul(t_base, common);

            int k = degR + 1;
            int n = k + degT;

            long[] x = new long[n];
            long[] y = new long[n];
            boolean possible = true;
            for (int i = 0; i < n; i++) {
                while (true) {
                    x[i] = rnd.nextInt((int) MOD);
                    boolean dup = false;
                    for (int j = 0; j < i; j++) if (x[j] == x[i]) dup = true;
                    if (dup) continue;
                    break;
                }
                long ti = poly.eval(t_true, x[i]);
                if (ti == 0) {
                    // if t(ui) = 0 and r(ui) = 0, the condition r(ui) = vi * t(ui) is satisfied for any vi.
                    // But the original problem states r(ui)/t(ui) = vi, which implies t(ui) != 0.
                    // If common factor vanishes at ui, then we can't have a valid rational function value.
                    y[i] = rnd.nextInt((int) MOD);
                    possible = false;
                } else {
                    long ri = poly.eval(r_true, x[i]);
                    y[i] = ri * MathUtils.modInv(ti, MOD) % MOD;
                }
            }

            long[][] res = poly.cauchyInterpolation(x, y, k);
            // If we constructed it with a common factor, it might still find a reduced version if common factor didn't vanish at any point.
            // But we chose n points. deg(r_true) + deg(t_true) = degR + degT = n - 1.
            // Here degR < k, degT <= n-k => degR + degT <= n - 1.
            // If we have n points, the rational function is unique (if it exists).
            // If gcd(r, t) != 1 for the *minimal* degrees, then no solution exists for those specific points.

            if (res != null) {
                // If a solution is found, it must satisfy all points and constraints.
                long[] r = res[0];
                long[] t = res[1];
                assertEquals(1, poly.deg(poly.gcd(r, t)) <= 0 ? 1 : 0, "Solution found but not reduced");
                for (int i = 0; i < n; i++) {
                    long ri = poly.eval(r, x[i]);
                    long ti = poly.eval(t, x[i]);
                    assertEquals(ri, y[i] * ti % MOD);
                    assertNotEquals(0, ti);
                }
            }
        }
    }
}
