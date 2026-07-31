package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

public class PolynomialResultant2DTest {
    private final PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
    private final PolynomialFpDynamic poly1d = poly2d.poly1d;

    @Test
    public void testBaseCases() {
        // res_x(f, 0) = 0
        long[][] f = {{1, 2}, {3, 4}};
        long[][] zero = {};
        assertArrayEquals(new long[]{}, poly2d.res_x(f, zero));
        assertArrayEquals(new long[]{}, poly2d.res_x(zero, f));

        // res_x(f(y), g(x,y)) = f(y)^deg_x(g)
        long[][] fy = {{2, 1}}; // 2 + y, deg_x = 0
        long[][] g = {{0}, {1}}; // x, deg_x = 1
        // res_x(y+2, x) = y+2
        assertArrayEquals(new long[]{2, 1}, poly2d.res_x(fy, g));

        // res_x(f, g(y)) = g(y)^deg_x(f)
        long[][] gy = {{3, 1}}; // 3 + y, deg_x = 0
        f = new long[][]{{0}, {0}, {1}}; // x^2, deg_x = 2
        // res_x(x^2, y+3) = (y+3)^2 = y^2 + 6y + 9
        assertArrayEquals(new long[]{9, 6, 1}, poly2d.res_x(f, gy));
    }

    @Test
    public void testLinearX() {
        // res_x(x - a(y), x - b(y)) = b(y) - a(y) ? No, it's a - b.
        // res(x-a, x-b) = a-b.
        // f = x - (y+1) = {-1, -1}, {1}
        // g = x - (2y+3) = {-3, -2}, {1}
        // res_x = (2y+3) - (y+1) = y + 2? No, res(x-A, x-B) = (A-B).
        // f(x) = x - A => root is A. g(A) = A - B.
        long[][] f = {{poly1d.mod - 1, poly1d.mod - 1}, {1}};
        long[][] g = {{poly1d.mod - 3, poly1d.mod - 2}, {1}};
        long[] res = poly2d.res_x(f, g);
        // A = y+1, B = 2y+3. A-B = -y - 2.
        assertArrayEquals(new long[]{poly1d.mod - 2, poly1d.mod - 1}, res);
    }

    @Test
    public void testQuadraticX() {
        // f = x^2 + y
        // g = x - y
        // res_x = y^2 + y
        long[][] f = {{0, 1}, {0}, {1}};
        long[][] g = {{0, poly1d.mod - 1}, {1}};
        long[] res = poly2d.res_x(f, g);
        assertArrayEquals(new long[]{0, 1, 1}, res);
    }

    @Test
    public void testSymmetry() {
        Random rnd = new Random(42);
        for (int i = 0; i < 5; i++) {
            long[][] f = randomPoly2D(rnd, 2, 2);
            long[][] g = randomPoly2D(rnd, 2, 2);
            long[] resFG = poly2d.res_x(f, g);
            long[] resGF = poly2d.res_x(g, f);

            int n = poly2d.degX(f);
            int m = poly2d.degX(g);
            if (n % 2 == 1 && m % 2 == 1) {
                assertArrayEquals(poly1d.neg(resFG), resGF);
            } else {
                assertArrayEquals(resFG, resGF);
            }
        }
    }

    @Test
    public void testHigherDegree() {
        // f = x^3 + y*x + 1
        // g = x^2 + y
        // res_x(f, g) = res_x(x^3 + y*x + 1 - x*g, g) = res_x(yx + 1 - xy, g) = res_x(1, g) = 1
        // Wait: f - x*g = x^3 + yx + 1 - x(x^2 + y) = x^3 + yx + 1 - x^3 - xy = 1.
        // res_x(f, g) = lc(g)^(deg_x f - deg_x(f mod g)) * res_x(g, f mod g)
        // res_x(f, g) = 1^(3-0) * res_x(g, 1) = 1.
        long[][] f = {{1}, {0, 1}, {0}, {1}};
        long[][] g = {{0, 1}, {0}, {1}};
        long[] res = poly2d.res_x(f, g);
        assertArrayEquals(new long[]{1}, res);
    }

    private long[][] randomPoly2D(Random rnd, int dx, int dy) {
        long[][] res = new long[dx + 1][dy + 1];
        for (int i = 0; i <= dx; i++) {
            for (int j = 0; j <= dy; j++) {
                res[i][j] = rnd.nextInt((int) poly1d.mod);
            }
        }
        return res;
    }
}
