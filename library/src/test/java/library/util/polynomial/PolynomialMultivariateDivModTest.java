package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic2D;
import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;

public class PolynomialMultivariateDivModTest {

    @Test
    public void testDivMod2D() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        long mod = poly2d.mod;

        // x^2 / (x + y) = (x - y) rem y^2
        long[][] a = {{0}, {0}, {1}}; // x^2
        long[][] b = {{0, 1}, {1}};    // x + y

        var res = poly2d.lexdivmod(a, b);

        long[][] expectedQ = {{0, mod - 1}, {1}}; // x - y
        long[][] expectedR = {{0, 0, 1}};          // y^2

        assertTrue(poly2d.equals(expectedQ, res.q), "2D Quotient mismatch");
        assertTrue(poly2d.equals(expectedR, res.r), "2D Remainder mismatch");

        // Verify a = q*b + r
        long[][] check = poly2d.add(poly2d.mul(res.q, b), res.r);
        assertTrue(poly2d.equals(a, check), "2D Relation a = q*b + r failed");
    }

    @Test
    public void testDivMod3D() {
        PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
        long mod = poly3d.mod;

        // x^2 / (x + y + z) = (x - (y + z)) rem (y + z)^2
        long[][][] a = {{{0}}, {{0}}, {{1}}}; // x^2
        long[][][] b = {{{0, 1}, {1}}, {{1}}}; // x + y + z

        var res = poly3d.lexdivmod(a, b);

        long[][][] expectedQ = {{{0, mod - 1}, {mod - 1}}, {{1}}}; // x - y - z
        // (y+z)^2 = y^2 + 2yz + z^2
        long[][][] expectedR = {{{0, 0, 1}, {0, 2}, {1}}};

        assertTrue(poly3d.equals(expectedQ, res.q), "3D Quotient mismatch");
        assertTrue(poly3d.equals(expectedR, res.r), "3D Remainder mismatch");

        long[][][] check = poly3d.add(poly3d.mul(res.q, b), res.r);
        assertTrue(poly3d.equals(a, check), "3D Relation a = q*b + r failed");
    }

    @Test
    public void testDivMod4D() {
        PolynomialFpDynamic4D poly4d = PolynomialFpDynamic4D.MOD998244353;
        long mod = poly4d.mod;

        // x^2 / (x + y + z + w) = (x - (y + z + w)) rem (y + z + w)^2
        long[][][][] a = {{{{0}}}, {{{0}}}, {{{1}}}}; // x^2
        long[][][][] b = {{{{0, 1}, {1}}, {{1}}}, {{{1}}}}; // x + y + z + w

        var res = poly4d.lexdivmod(a, b);

        long[][][][] expectedQ = {{{{0, mod - 1}, {mod - 1}}, {{mod - 1}}}, {{{1}}}}; // x - y - z - w

        long[][][][] check = poly4d.add(poly4d.mul(res.q, b), res.r);
        assertTrue(poly4d.equals(a, check), "4D Relation a = q*b + r failed");
    }

    @Test
    public void testDivByConstant() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        long[][] a = {{1, 2}, {3, 4}}; // 1 + 2y + 3x + 4xy
        long[][] b = {{2}};           // constant 2

        var res = poly2d.lexdivmod(a, b);
        long inv2 = 499122177L; // modInv(2, 998244353)

        long[][] expectedQ = {{inv2, 2 * inv2 % poly2d.mod}, {3 * inv2 % poly2d.mod, 4 * inv2 % poly2d.mod}};
        assertTrue(poly2d.equals(expectedQ, res.q));
        assertTrue(poly2d.equals(poly2d.zero(), res.r));
    }

    @Test
    public void testDivByZero() {
        PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.MOD998244353;
        assertThrows(ArithmeticException.class, () -> poly2d.lexdivmod(poly2d.one(), poly2d.zero()));

        PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
        assertThrows(ArithmeticException.class, () -> poly3d.lexdivmod(poly3d.one(), poly3d.zero()));

        PolynomialFpDynamic4D poly4d = PolynomialFpDynamic4D.MOD998244353;
        assertThrows(ArithmeticException.class, () -> poly4d.lexdivmod(poly4d.one(), poly4d.zero()));
    }
}
