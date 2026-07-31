package library.util.polynomial;

import library.util.Fp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class PolynomialFpDynamicHermiteTest {
    @Test
    public void testXInverseSquared() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(998244353L);
        long[] f = {1}; // 1
        long[] g = {0, 0, 1}; // x^2

        var result = poly.hermiteReduction(f, g);

        // f/g = q + (c/d)' + logPart
        // 1/x^2 = 0 + (-1/x)' + 0

        assertTrue(poly.isZero(result.q()));

        // c/d should be -1/x
        // c = -1 = mod - 1, d = x
        long[] expectedC = {poly.mod - 1};
        long[] expectedD = {0, 1};

        // Check if c/d == -1/x
        // c * x == -1 * d
        assertTrue(poly.equals(poly.mul(result.c(), expectedD), poly.mul(result.d(), expectedC)));

        assertEquals(0, result.logPart().length);
    }

    @Test
    public void testInverseOfSquaredQuadratic() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(998244353L);
        // 1 / (x^2 + 1)^2
        long[] g_base = {1, 0, 1}; // x^2 + 1
        long[] g = poly.mul(g_base, g_base); // (x^2 + 1)^2 = x^4 + 2x^2 + 1
        long[] f = {1};

        var result = poly.hermiteReduction(f, g);

        // 1 / (x^2 + 1)^2 = (x / (2(x^2 + 1)))' + 1/2 / (x^2 + 1)

        assertTrue(poly.isZero(result.q()));

        // c/d = x / (2(x^2 + 1))
        // c = x, d = 2x^2 + 2
        long[] expectedC = {0, 1};
        long[] expectedD = {2, 0, 2};

        assertTrue(poly.equals(poly.mul(result.c(), expectedD), poly.mul(result.d(), expectedC)));

        assertEquals(1, result.logPart().length);
        var logTerm = result.logPart()[0];
        // logTerm.a / logTerm.g = (1/2) / (x^2 + 1)
        long[] expectedLogA = {poly.getFp().inv(2)};
        long[] expectedLogG = {1, 0, 1};

        assertTrue(poly.equals(poly.mul(logTerm.a(), expectedLogG), poly.mul(logTerm.g(), expectedLogA)));
    }

    @Test
    public void testVerification() {
        // Verify that q + (c/d)' + sum(a_i/g_i) == f/g
        PolynomialFpDynamic poly = PolynomialFpDynamic.of(998244353L);

        // f = x^5 + 2x^2 + 1
        // g = (x-1)^2 * (x+1) = (x^2-2x+1)(x+1) = x^3 - x^2 - x + 1
        long[] x_minus_1 = {poly.mod - 1, 1};
        long[] x_plus_1 = {1, 1};
        long[] g = poly.mul(poly.mul(x_minus_1, x_minus_1), x_plus_1);
        long[] f = {1, 0, 2, 0, 0, 1};

        var result = poly.hermiteReduction(f, g);

        // Recalculate f/g from result
        // (c/d)' = (c'd - cd') / d^2
        long[] cp = poly.differentiate(result.c());
        long[] dp = poly.differentiate(result.d());
        long[] numCDp = poly.sub(poly.mul(cp, result.d()), poly.mul(result.c(), dp));
        long[] denCDp = poly.mul(result.d(), result.d());

        // sum logPart
        long[] logNum = {0};
        long[] logDen = {1};
        for (var term : result.logPart()) {
            logNum = poly.add(poly.mul(logNum, term.g()), poly.mul(term.a(), logDen));
            logDen = poly.mul(logDen, term.g());
        }

        // Total = q + numCDp/denCDp + logNum/logDen
        // Total = (q * denCDp * logDen + numCDp * logDen + logNum * denCDp) / (denCDp * logDen)
        long[] totalNum = poly.add(poly.mul(poly.mul(result.q(), denCDp), logDen),
                                   poly.add(poly.mul(numCDp, logDen), poly.mul(logNum, denCDp)));
        long[] totalDen = poly.mul(denCDp, logDen);

        // Check totalNum / totalDen == f / g
        // totalNum * g == f * totalDen
        assertTrue(poly.equals(poly.mul(totalNum, g), poly.mul(f, totalDen)));
    }
}
