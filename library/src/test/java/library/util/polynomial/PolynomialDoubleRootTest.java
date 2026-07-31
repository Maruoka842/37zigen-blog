package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class PolynomialDoubleRootTest {

    @Test
    public void testEvaluate() {
        double[] f = {1.0, 2.0, 3.0}; // 3x^2 + 2x + 1
        assertEquals(1.0, PolynomialDouble.evaluate(f, 0.0), 1e-9);
        assertEquals(6.0, PolynomialDouble.evaluate(f, 1.0), 1e-9);
        assertEquals(2.0, PolynomialDouble.evaluate(f, -1.0), 1e-9);
    }

    @Test
    public void testDifferentiate() {
        double[] f = {1.0, 2.0, 3.0}; // 3x^2 + 2x + 1
        double[] expected = {2.0, 6.0}; // 6x + 2
        assertArrayEquals(expected, PolynomialDouble.differentiate(f), 1e-9);

        double[] f2 = {5.0};
        assertArrayEquals(new double[0], PolynomialDouble.differentiate(f2), 1e-9);
    }

    @Test
    public void testRealRootsLinear() {
        double[] f = {-2.0, 1.0}; // x - 2 = 0
        double[] roots = PolynomialDouble.realRoots(f);
        assertArrayEquals(new double[]{2.0}, roots, 1e-9);
    }

    @Test
    public void testRealRootsQuadratic() {
        double[] f = {-1.0, 0.0, 1.0}; // x^2 - 1 = 0
        double[] roots = PolynomialDouble.realRoots(f);
        assertArrayEquals(new double[]{-1.0, 1.0}, roots, 1e-9);

        double[] f2 = {1.0, 0.0, 1.0}; // x^2 + 1 = 0
        double[] roots2 = PolynomialDouble.realRoots(f2);
        assertArrayEquals(new double[0], roots2, 1e-9);

        double[] f3 = {1.0, -2.0, 1.0}; // (x-1)^2 = 0
        double[] roots3 = PolynomialDouble.realRoots(f3);
        assertArrayEquals(new double[]{1.0}, roots3, 1e-7);
    }

    @Test
    public void testRealRootsCubic() {
        double[] f = {0.0, -1.0, 0.0, 1.0}; // x^3 - x = x(x-1)(x+1)
        double[] roots = PolynomialDouble.realRoots(f);
        assertArrayEquals(new double[]{-1.0, 0.0, 1.0}, roots, 1e-9);

        double[] f2 = {-1.0, 0.0, 0.0, 1.0}; // x^3 - 1 = 0
        double[] roots2 = PolynomialDouble.realRoots(f2);
        assertArrayEquals(new double[]{1.0}, roots2, 1e-9);
    }

    @Test
    public void testRealRootsQuartic() {
        // (x-1)(x-2)(x-3)(x-4) = x^4 - 10x^3 + 35x^2 - 50x + 24
        double[] f = {24.0, -50.0, 35.0, -10.0, 1.0};
        double[] roots = PolynomialDouble.realRoots(f);
        assertEquals(4, roots.length);
        assertEquals(1.0, roots[0], 1e-7);
        assertEquals(2.0, roots[1], 1e-7);
        assertEquals(3.0, roots[2], 1e-7);
        assertEquals(4.0, roots[3], 1e-7);
    }
}
