package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic2D;
import java.util.Arrays;

public class PolynomialFpDynamicDiffEvalTest {
    @Test
    public void test1DDiff() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // f(x) = 1 + 2x + 3x^2
        long[] f = {1, 2, 3};

        // f'(x) = 2 + 6x
        assertArrayEquals(new long[]{2, 6}, poly.diff(f, 1));

        // f''(x) = 6
        assertArrayEquals(new long[]{6}, poly.diff(f, 2));

        // f'''(x) = 0
        assertArrayEquals(new long[]{}, poly.diff(f, 3));

        // repeat = 0
        assertArrayEquals(f, poly.diff(f, 0));
    }

    @Test
    public void test2DDiff() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        // f(x, y) = 1 + 2x + 3y + 4xy + 5x^2 + 6y^2
        // f(x, y) = (1 + 3y + 6y^2) + (2 + 4y)x + (5)x^2
        long[][] f = {
            {1, 3, 6},
            {2, 4},
            {5}
        };

        // ∂f/∂x = (2 + 4y) + (10)x
        long[][] dfdx = {
            {2, 4},
            {10}
        };
        assertTrue(poly2d.equals(dfdx, poly2d.diffX(f, 1)));

        // ∂^2f/∂x^2 = 10
        long[][] d2fdx2 = {
            {10}
        };
        assertTrue(poly2d.equals(d2fdx2, poly2d.diffX(f, 2)));

        // ∂f/∂y = (3 + 12y) + (4)x
        long[][] dfdy = {
            {3, 12},
            {4}
        };
        assertTrue(poly2d.equals(dfdy, poly2d.diffY(f, 1)));
        assertTrue(poly2d.equals(dfdy, poly2d.diffY(f)));

        // ∂^2f/∂y^2 = 12
        long[][] d2fdy2 = {
            {12}
        };
        assertTrue(poly2d.equals(d2fdy2, poly2d.diffY(f, 2)));
    }

    @Test
    public void test2DEval() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        // f(x, y) = 1 + 2x + 3y + 4xy
        // f(x, y) = (1 + 3y) + (2 + 4y)x
        long[][] f = {
            {1, 3},
            {2, 4}
        };

        // f(2, y) = (1 + 3y) + (2 + 4y)*2 = 1 + 3y + 4 + 8y = 5 + 11y
        assertArrayEquals(new long[]{5, 11}, poly2d.evalX(f, 2));

        // f(x, 3) = 1 + 2x + 3(3) + 4x(3) = 1 + 2x + 9 + 12x = 10 + 14x
        assertArrayEquals(new long[]{10, 14}, poly2d.evalY(f, 3));
    }
}
