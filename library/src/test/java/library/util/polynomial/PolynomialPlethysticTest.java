package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class PolynomialPlethysticTest {

    @Test
    void testBasicEdgeCases() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // Invalid n
        assertThrows(IllegalArgumentException.class, () -> {
            poly.plethysticExponential(new long[]{0, 1}, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.plethysticLogarithm(new long[]{1, 1}, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.cyclePlethysticExponential(new long[]{0, 1}, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.cyclePlethysticLogarithm(new long[]{0, 1}, 0);
        });

        // Invalid constant terms
        assertThrows(IllegalArgumentException.class, () -> {
            poly.plethysticExponential(new long[]{1, 1}, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.plethysticLogarithm(new long[]{0, 1}, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.cyclePlethysticExponential(new long[]{1, 1}, 5);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            poly.cyclePlethysticLogarithm(new long[]{1, 1}, 5);
        });

        // n = 1
        assertArrayEquals(new long[]{1}, poly.plethysticExponential(new long[]{0, 1}, 1));
        assertArrayEquals(new long[]{0}, poly.plethysticLogarithm(new long[]{1, 1}, 1));
        assertArrayEquals(new long[]{0}, poly.cyclePlethysticExponential(new long[]{0, 1}, 1));
        assertArrayEquals(new long[]{0}, poly.cyclePlethysticLogarithm(new long[]{0, 1}, 1));
    }

    @Test
    void testKnownPEIdentities() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // PE[0] = 1 (constant term is 1, rest 0)
        long[] peZero = poly.plethysticExponential(new long[]{0, 0, 0, 0}, 4);
        assertArrayEquals(new long[]{1, 0, 0, 0}, peZero);

        // PE[x] = 1 / (1 - x) = 1 + x + x^2 + x^3 + ...
        // input: f = [0, 1, 0, 0, 0]
        long[] peX = poly.plethysticExponential(new long[]{0, 1, 0, 0, 0}, 5);
        assertArrayEquals(new long[]{1, 1, 1, 1, 1}, peX);

        // PE[x^2] = 1 / (1 - x^2) = 1 + x^2 + x^4 + ...
        // input: f = [0, 0, 1, 0, 0, 0]
        long[] peX2 = poly.plethysticExponential(new long[]{0, 0, 1, 0, 0, 0}, 6);
        assertArrayEquals(new long[]{1, 0, 1, 0, 1, 0}, peX2);

        // PE[2x] = 1 / (1 - x)^2 = 1 + 2x + 3x^2 + 4x^3 + ...
        // input: f = [0, 2, 0, 0, 0]
        long[] pe2X = poly.plethysticExponential(new long[]{0, 2, 0, 0, 0}, 5);
        assertArrayEquals(new long[]{1, 2, 3, 4, 5}, pe2X);
    }

    @Test
    void testAdditiveProperty() {
        // PE[f + g] = PE[f] * PE[g] mod x^n
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 8;
        long[] f = {0, 3, 1, 4, 0, 2, 0, 5};
        long[] g = {0, 1, 5, 9, 2, 6, 5, 3};

        long[] fPlusG = poly.add(f, g);
        long[] peSum = poly.plethysticExponential(fPlusG, n);

        long[] peF = poly.plethysticExponential(f, n);
        long[] peG = poly.plethysticExponential(g, n);
        long[] peProduct = Arrays.copyOf(poly.mul(peF, peG), n);

        assertArrayEquals(peSum, peProduct);
    }

    @Test
    void testInverseRelationshipNTT() {
        // PE[PL[f]] = f and PL[PE[f]] = f for MOD998244353
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 10;
        Random rnd = new Random(42);

        // Test PE[PL[f]] = f
        // f must have constant term 1
        long[] f = new long[n];
        f[0] = 1;
        for (int i = 1; i < n; i++) {
            f[i] = rnd.nextInt(100) + 1;
        }
        long[] pl = poly.plethysticLogarithm(f, n);
        long[] pePl = poly.plethysticExponential(pl, n);
        assertArrayEquals(f, pePl);

        // Test PL[PE[g]] = g
        // g must have constant term 0
        long[] g = new long[n];
        g[0] = 0;
        for (int i = 1; i < n; i++) {
            g[i] = rnd.nextInt(100) + 1;
        }
        long[] pe = poly.plethysticExponential(g, n);
        long[] plPe = poly.plethysticLogarithm(pe, n);
        assertArrayEquals(g, plPe);
    }

    @Test
    void testInverseRelationshipNonNTT() {
        // PE[PL[f]] = f and PL[PE[f]] = f for non-NTT MOD1000000007
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        int n = 10;
        Random rnd = new Random(12345);

        long[] f = new long[n];
        f[0] = 1;
        for (int i = 1; i < n; i++) {
            f[i] = rnd.nextInt(100) + 1;
        }
        long[] pl = poly.plethysticLogarithm(f, n);
        long[] pePl = poly.plethysticExponential(pl, n);
        assertArrayEquals(f, pePl);

        long[] g = new long[n];
        g[0] = 0;
        for (int i = 1; i < n; i++) {
            g[i] = rnd.nextInt(100) + 1;
        }
        long[] pe = poly.plethysticExponential(g, n);
        long[] plPe = poly.plethysticLogarithm(pe, n);
        assertArrayEquals(g, plPe);
    }

    @Test
    void testOverloadAndLengths() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] f = {0, 1, 2, 3}; // length 4

        // Overload uses f.length as n
        long[] resDefault = poly.plethysticExponential(f);
        long[] resExplicit = poly.plethysticExponential(f, f.length);
        assertArrayEquals(resExplicit, resDefault);

        // Smaller n
        long[] resSmall = poly.plethysticExponential(f, 2);
        assertEquals(2, resSmall.length);
        assertArrayEquals(new long[]{1, 1}, resSmall);

        // Larger n
        long[] resLarge = poly.plethysticExponential(f, 6);
        assertEquals(6, resLarge.length);
    }

    @Test
    void testCyclePlethysticIdentities() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

        // CYC_PE[0] = 0
        long[] cycZero = poly.cyclePlethysticExponential(new long[]{0, 0, 0, 0}, 4);
        assertArrayEquals(new long[]{0, 0, 0, 0}, cycZero);

        // CYC_PE[x] = x / (1 - x) = x + x^2 + x^3 + ...
        // input: a = [0, 1, 0, 0, 0]
        long[] cycX = poly.cyclePlethysticExponential(new long[]{0, 1, 0, 0, 0}, 5);
        assertArrayEquals(new long[]{0, 1, 1, 1, 1}, cycX);
    }

    @Test
    void testCyclePlethysticInverseNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 12;
        Random rnd = new Random(42);

        // Generate random input with a_0 = 0
        long[] a = new long[n];
        a[0] = 0;
        for (int i = 1; i < n; i++) {
            a[i] = rnd.nextInt(100) + 1;
        }

        long[] b = poly.cyclePlethysticExponential(a, n);
        long[] recoveredA = poly.cyclePlethysticLogarithm(b, n);
        assertArrayEquals(a, recoveredA);

        long[] recoveredB = poly.cyclePlethysticExponential(recoveredA, n);
        assertArrayEquals(b, recoveredB);
    }

    @Test
    void testCyclePlethysticInverseNonNTT() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(1000000007L);
        int n = 12;
        Random rnd = new Random(12345);

        // Generate random input with a_0 = 0
        long[] a = new long[n];
        a[0] = 0;
        for (int i = 1; i < n; i++) {
            a[i] = rnd.nextInt(100) + 1;
        }

        long[] b = poly.cyclePlethysticExponential(a, n);
        long[] recoveredA = poly.cyclePlethysticLogarithm(b, n);
        assertArrayEquals(a, recoveredA);

        long[] recoveredB = poly.cyclePlethysticExponential(recoveredA, n);
        assertArrayEquals(b, recoveredB);
    }
}
