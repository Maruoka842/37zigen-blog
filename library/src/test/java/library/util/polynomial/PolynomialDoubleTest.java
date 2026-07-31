package library.util.polynomial;

import library.util.polynomial.PolynomialDouble;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class PolynomialDoubleTest {

    @Test
    public void testMul() {
        double[] f = {1.0, 2.0, 3.0};
        double[] g = {4.0, 5.0};
        // (1 + 2x + 3x^2)(4 + 5x) = 4 + 5x + 8x + 10x^2 + 12x^2 + 15x^3 = 4 + 13x + 22x^2 + 15x^3
        double[] expected = {4.0, 13.0, 22.0, 15.0};
        double[] actual = PolynomialDouble.mul(f, g);
        assertArrayEquals(expected, actual, 1e-9);
    }

    @Test
    public void testMulLarge() {
        Random rnd = new Random(42);
        int n = 2048;
        int m = 2048;
        double[] f = new double[n];
        double[] g = new double[m];
        for (int i = 0; i < n; i++) f[i] = rnd.nextDouble();
        for (int i = 0; i < m; i++) g[i] = rnd.nextDouble();

        double[] expected = mulNaive(f, g);
        double[] actual = PolynomialDouble.mul(f, g);
        assertArrayEquals(expected, actual, 1e-9);
    }

    @Test
    public void testMulRandom() {
        Random rnd = new Random(42);
        int n = 100;
        int m = 100;
        double[] f = new double[n];
        double[] g = new double[m];
        for (int i = 0; i < n; i++) f[i] = rnd.nextDouble();
        for (int i = 0; i < m; i++) g[i] = rnd.nextDouble();

        double[] expected = mulNaive(f, g);
        double[] actual = PolynomialDouble.mul(f, g);
        assertArrayEquals(expected, actual, 1e-9);
    }

    private double[] mulNaive(double[] f, double[] g) {
        double[] h = new double[f.length + g.length - 1];
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < g.length; j++) {
                h[i + j] += f[i] * g[j];
            }
        }
        return h;
    }
}
