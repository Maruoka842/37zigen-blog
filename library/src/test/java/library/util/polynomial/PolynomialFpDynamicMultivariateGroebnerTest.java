package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;
import java.util.Arrays;
import java.util.List;

public class PolynomialFpDynamicMultivariateGroebnerTest {

    @Test
    public void test3DGroebnerSimple() {
        PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
        // I = <x + y + z, x + y>
        // Reduced GB should be {x + y, z}
        long[][][] f1 = poly3d.add(poly3d.add(poly3d.x(), poly3d.y()), poly3d.z());
        long[][][] f2 = poly3d.add(poly3d.x(), poly3d.y());

        List<long[][][]> gb = poly3d.reducedGroebnerBasis(Arrays.asList(f1, f2));
        assertEquals(2, gb.size());

        boolean foundZ = false;
        boolean foundXY = false;
        for (long[][][] g : gb) {
            if (poly3d.equals(g, poly3d.z())) foundZ = true;
            if (poly3d.equals(g, poly3d.add(poly3d.x(), poly3d.y()))) foundXY = true;
        }
        assertTrue(foundZ, "Should contain z");
        assertTrue(foundXY, "Should contain x + y");
    }

    @Test
    public void test3DGroebnerCircle() {
        PolynomialFpDynamic3D poly3d = PolynomialFpDynamic3D.MOD998244353;
        // I = <x^2 + y^2 - 1, x - y>
        // x = y, 2y^2 - 1 = 0 => y^2 = 1/2
        // GB should contain x - y and y^2 - 499122177 (since 1/2 mod 998244353 is 499122177)
        long[][][] f1 = poly3d.sub(poly3d.add(poly3d.mul(poly3d.x(), poly3d.x()), poly3d.mul(poly3d.y(), poly3d.y())), poly3d.one());
        long[][][] f2 = poly3d.sub(poly3d.x(), poly3d.y());

        List<long[][][]> gb = poly3d.reducedGroebnerBasis(Arrays.asList(f1, f2));
        assertEquals(2, gb.size());

        long inv2 = 499122177L;
        long[][][] targetY2 = poly3d.sub(poly3d.mul(poly3d.y(), poly3d.y()), poly3d.mul(poly3d.one(), inv2));

        boolean foundXY = false;
        boolean foundY2 = false;
        for (long[][][] g : gb) {
            if (poly3d.equals(g, poly3d.sub(poly3d.x(), poly3d.y()))) foundXY = true;
            if (poly3d.equals(g, targetY2)) foundY2 = true;
        }
        assertTrue(foundXY, "Should contain x - y");
        assertTrue(foundY2, "Should contain y^2 - 1/2");
    }

    @Test
    public void test4DGroebnerSimple() {
        PolynomialFpDynamic4D poly4d = PolynomialFpDynamic4D.MOD998244353;
        // I = <x + y + z + w, x + y + z>
        // Reduced GB should be {x + y + z, w}
        long[][][][] f1 = poly4d.add(poly4d.add(poly4d.add(poly4d.x(), poly4d.y()), poly4d.z()), poly4d.w());
        long[][][][] f2 = poly4d.add(poly4d.add(poly4d.x(), poly4d.y()), poly4d.z());

        List<long[][][][]> gb = poly4d.reducedGroebnerBasis(Arrays.asList(f1, f2));
        assertEquals(2, gb.size());

        boolean foundW = false;
        boolean foundXYZ = false;
        for (long[][][][] g : gb) {
            if (poly4d.equals(g, poly4d.w())) foundW = true;
            if (poly4d.equals(g, poly4d.add(poly4d.add(poly4d.x(), poly4d.y()), poly4d.z()))) foundXYZ = true;
        }
        assertTrue(foundW, "Should contain w");
        assertTrue(foundXYZ, "Should contain x + y + z");
    }
}
