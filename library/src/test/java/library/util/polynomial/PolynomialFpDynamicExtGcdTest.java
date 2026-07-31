package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.algebra.strategy.EuclideanDomainStrategy;

public class PolynomialFpDynamicExtGcdTest {
    @Test
    void testExtGcd() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // f = (x+1)(x+2) = x^2 + 3x + 2
        // g = (x+1)(x+3) = x^2 + 4x + 3
        // gcd = x+1 (monic)
        long[] f = {2, 3, 1};
        long[] g = {3, 4, 1};
        EuclideanDomainStrategy.ExtGCDResult<long[]> res = poly.extgcd(f, g);

        // f*s + g*t = gcd
        long[] fs = poly.mul(f, res.x());
        long[] gt = poly.mul(g, res.y());
        long[] sum = poly.add(fs, gt);

        assertArrayEquals(new long[]{1, 1}, poly.resize(sum), "f*s + g*t == gcd");
        assertArrayEquals(new long[]{1, 1}, poly.resize(res.gcd()), "gcd == x+1");
    }

    @Test
    void testExtGcdLarge() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        int n = 4000; // Above 3072 to trigger Half-GCD
        long[] f = new long[n + 1];
        long[] g = new long[n];
        f[n] = 1; f[0] = 1; // x^n + 1
        g[n-1] = 1; g[0] = 1; // x^{n-1} + 1

        EuclideanDomainStrategy.ExtGCDResult<long[]> res = poly.extgcd(f, g);

        long[] fs = poly.mul(f, res.x());
        long[] gt = poly.mul(g, res.y());
        long[] sum = poly.add(fs, gt);

        assertArrayEquals(poly.resize(res.gcd()), poly.resize(sum), "f*s + g*t == gcd (Large)");

        // For x^n+1 and x^{n-1}+1, if n is even, x+1 is a factor?
        // n=4000. x^4000+1: (-1)^4000+1 = 2 != 0.
        // Actually gcd depends on n. But the identity must hold.
    }

    @Test
    void testExtGcdZero() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] f = {0};
        long[] g = {0};
        EuclideanDomainStrategy.ExtGCDResult<long[]> res = poly.extgcd(f, g);
        assertArrayEquals(new long[]{0}, res.gcd());
        assertArrayEquals(new long[]{0}, res.x());
        assertArrayEquals(new long[]{0}, res.y());
    }
}
