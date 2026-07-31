package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic2D;
import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;

public class PolynomialFpDynamicIsZeroTest {
    @Test
    public void test1DIsZero() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        assertTrue(poly.isZero(new long[]{}));
        assertTrue(poly.isZero(new long[]{0}));
        assertTrue(poly.isZero(new long[]{0, 0}));
        assertFalse(poly.isZero(new long[]{1}));
        assertFalse(poly.isZero(new long[]{0, 1}));
    }

    @Test
    public void test2DIsZero() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        assertTrue(poly2d.isZero(new long[][]{}));
        assertTrue(poly2d.isZero(new long[][]{{0}}));
        assertTrue(poly2d.isZero(new long[][]{{0}, {0, 0}}));
        assertFalse(poly2d.isZero(new long[][]{{1}}));
        assertFalse(poly2d.isZero(new long[][]{{0, 1}}));
        assertFalse(poly2d.isZero(new long[][]{{0}, {1}}));
    }

    @Test
    public void test3DIsZero() {
        PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
        assertTrue(poly3d.isZero(new long[][][]{}));
        assertTrue(poly3d.isZero(new long[][][]{{{0}}}));
        assertTrue(poly3d.isZero(new long[][][]{{{0}, {0}}, {{0}}}));
        assertFalse(poly3d.isZero(new long[][][]{{{1}}}));
        assertFalse(poly3d.isZero(new long[][][]{{{0, 1}}}));
        assertFalse(poly3d.isZero(new long[][][]{{{0}, {1}}}));
        assertFalse(poly3d.isZero(new long[][][]{{{0}}, {{1}}}));
    }

    @Test
    public void test4DIsZero() {
        PolynomialFpDynamic4D poly4d = PolynomialFpDynamic4D.MOD998244353;
        assertTrue(poly4d.isZero(new long[][][][]{}));
        assertTrue(poly4d.isZero(new long[][][][]{{{{0}}}}));
        assertTrue(poly4d.isZero(new long[][][][]{{{{0}}}, {{{0}}}}));
        assertFalse(poly4d.isZero(new long[][][][]{{{{1}}}}));
        assertFalse(poly4d.isZero(new long[][][][]{{{{0, 1}}}}));
        assertFalse(poly4d.isZero(new long[][][][]{{{{0}, {1}}}}));
        assertFalse(poly4d.isZero(new long[][][][]{{{{0}}}, {{{1}}}}));
        assertFalse(poly4d.isZero(new long[][][][]{{{{0}}}, {{{0}}}, {{{1}}}}));
    }
}
