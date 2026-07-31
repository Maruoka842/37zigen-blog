package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic.OnlineCyclePlethysticLogarithm;
import library.util.polynomial.PolynomialFpDynamic.OnlinePlethysticLogarithm;

public class OnlinePlethysticOperationsTest {

    @Test
    public void testOnlinePlethysticExponentialBasicEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Invalid constant term (f[0] must be 0)
        assertThrows(IllegalArgumentException.class, () -> {
            PolynomialFpDynamic.OnlinePlethysticExponential solver =
                new PolynomialFpDynamic.OnlinePlethysticExponential(5, poly);
            solver.append(1); // non-zero constant term
        });

        // Smallest size N = 1
        PolynomialFpDynamic.OnlinePlethysticExponential solver1 =
            new PolynomialFpDynamic.OnlinePlethysticExponential(1, poly);
        assertEquals(1, solver1.append(0)); // PE[0] = 1 mod x^1
    }

    @Test
    public void testOnlinePlethysticLogarithmBasicEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Invalid constant term (f[0] must be 1)
        assertThrows(IllegalArgumentException.class, () -> {
            OnlineCyclePlethysticLogarithm solver =
                new OnlineCyclePlethysticLogarithm(5, poly);
            solver.append(0); // constant term not 1
        });
        assertThrows(IllegalArgumentException.class, () -> {
            OnlineCyclePlethysticLogarithm solver =
                new OnlineCyclePlethysticLogarithm(5, poly);
            solver.append(2); // constant term not 1
        });

        // Smallest size N = 1
        OnlineCyclePlethysticLogarithm solver1 =
            new OnlineCyclePlethysticLogarithm(1, poly);
        assertEquals(0, solver1.append(1)); // PL[1] = 0 mod x^1
    }

    @Test
    public void testOnlineCyclePlethysticExponentialBasicEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Invalid constant term (a[0] must be 0)
        assertThrows(IllegalArgumentException.class, () -> {
            PolynomialFpDynamic.OnlineCyclePlethysticExponential solver =
                new PolynomialFpDynamic.OnlineCyclePlethysticExponential(5, poly);
            solver.append(1); // non-zero constant term
        });

        // Smallest size N = 1
        PolynomialFpDynamic.OnlineCyclePlethysticExponential solver1 =
            new PolynomialFpDynamic.OnlineCyclePlethysticExponential(1, poly);
        assertEquals(0, solver1.append(0)); // CYC_PE[0] = 0 mod x^1
    }

    @Test
    public void testOnlineCyclePlethysticLogarithmBasicEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Invalid constant term (b[0] must be 0)
        assertThrows(IllegalArgumentException.class, () -> {
            PolynomialFpDynamic.OnlineCyclePlethysticLogarithm solver =
                new PolynomialFpDynamic.OnlineCyclePlethysticLogarithm(5, poly);
            solver.append(1); // non-zero constant term
        });

        // Smallest size N = 1
        PolynomialFpDynamic.OnlineCyclePlethysticLogarithm solver1 =
            new PolynomialFpDynamic.OnlineCyclePlethysticLogarithm(1, poly);
        assertEquals(0, solver1.append(0)); // CYC_PL[0] = 0 mod x^1
    }

    @Test
    public void testOnlinePlethysticExponentialNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runPlethysticExponentialRandomTests(poly, 42);
    }

    @Test
    public void testOnlinePlethysticExponentialNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runPlethysticExponentialRandomTests(poly, 43);
    }

    @Test
    public void testOnlinePlethysticLogarithmNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runPlethysticLogarithmRandomTests(poly, 44);
    }

    @Test
    public void testOnlinePlethysticLogarithmNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runPlethysticLogarithmRandomTests(poly, 45);
    }

    @Test
    public void testOnlineCyclePlethysticExponentialNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runCyclePlethysticExponentialRandomTests(poly, 46);
    }

    @Test
    public void testOnlineCyclePlethysticExponentialNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runCyclePlethysticExponentialRandomTests(poly, 47);
    }

    @Test
    public void testOnlineCyclePlethysticLogarithmNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        runCyclePlethysticLogarithmRandomTests(poly, 48);
    }

    @Test
    public void testOnlineCyclePlethysticLogarithmNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        runCyclePlethysticLogarithmRandomTests(poly, 49);
    }

    private void runPlethysticExponentialRandomTests(PolynomialFpDynamic poly, long seed) {
        Random rng = new Random(seed);
        long mod = poly.mod;

        for (int len : new int[]{2, 5, 20, 50}) {
            long[] f = new long[len];
            f[0] = 0;
            for (int i = 1; i < len; i++) {
                f[i] = rng.nextInt((int) mod);
            }

            // Expected result offline
            long[] expected = poly.plethysticExponential(f, len);

            // Actual online step-by-step
            PolynomialFpDynamic.OnlinePlethysticExponential solver =
                new PolynomialFpDynamic.OnlinePlethysticExponential(len, poly);
            long[] actual = new long[len];
            for (int i = 0; i < len; i++) {
                actual[i] = solver.append(f[i]);
                assertEquals(expected[i], actual[i],
                    "Mismatch in Plethystic Exponential at index " + i + " for len=" + len + " mod=" + mod);
            }

        }
    }

    private void runPlethysticLogarithmRandomTests(PolynomialFpDynamic poly, long seed) {
        Random rng = new Random(seed);
        long mod = poly.mod;

        for (int len : new int[]{2, 5, 20, 50}) {
            long[] f = new long[len];
            f[0] = 1;
            for (int i = 1; i < len; i++) {
                f[i] = rng.nextInt((int) mod);
            }

            // Expected result offline
            long[] expected = poly.plethysticLogarithm(f, len);

            // Actual online step-by-step
            OnlinePlethysticLogarithm solver =
                new OnlinePlethysticLogarithm(len, poly);
            long[] actual = new long[len];
            for (int i = 0; i < len; i++) {
                actual[i] = solver.append(f[i]);
                assertEquals(expected[i], actual[i],
                    "Mismatch in Plethystic Logarithm at index " + i + " for len=" + len + " mod=" + mod);
            }

        }
    }

    private void runCyclePlethysticExponentialRandomTests(PolynomialFpDynamic poly, long seed) {
        Random rng = new Random(seed);
        long mod = poly.mod;

        for (int len : new int[]{2, 5, 20, 50}) {
            long[] a = new long[len];
            a[0] = 0;
            for (int i = 1; i < len; i++) {
                a[i] = rng.nextInt((int) mod);
            }

            // Expected result offline
            long[] expected = poly.cyclePlethysticExponential(a, len);

            // Actual online step-by-step
            PolynomialFpDynamic.OnlineCyclePlethysticExponential solver =
                new PolynomialFpDynamic.OnlineCyclePlethysticExponential(len, poly);
            long[] actual = new long[len];
            for (int i = 0; i < len; i++) {
                actual[i] = solver.append(a[i]);
                assertEquals(expected[i], actual[i],
                    "Mismatch in Cycle Plethystic Exponential at index " + i + " for len=" + len + " mod=" + mod);
            }

        }
    }

    private void runCyclePlethysticLogarithmRandomTests(PolynomialFpDynamic poly, long seed) {
        Random rng = new Random(seed);
        long mod = poly.mod;

        for (int len : new int[]{2, 5, 20, 50}) {
            long[] b = new long[len];
            b[0] = 0;
            for (int i = 1; i < len; i++) {
                b[i] = rng.nextInt((int) mod);
            }

            // Expected result offline
            long[] expected = poly.cyclePlethysticLogarithm(b, len);

            // Actual online step-by-step
            PolynomialFpDynamic.OnlineCyclePlethysticLogarithm solver =
                new PolynomialFpDynamic.OnlineCyclePlethysticLogarithm(len, poly);
            long[] actual = new long[len];
            for (int i = 0; i < len; i++) {
                actual[i] = solver.append(b[i]);
                assertEquals(expected[i], actual[i],
                    "Mismatch in Cycle Plethystic Logarithm at index " + i + " for len=" + len + " mod=" + mod);
            }

        }
    }
}
