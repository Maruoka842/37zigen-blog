package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RationalFunctionFpDynamicTest {

    @Test
    void testNormalization() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[] num = {0, 0, 2}; // 2x^2
        long[] den = {0, 4};    // 4x
        RationalFunctionFpDynamic f = new RationalFunctionFpDynamic(num, den, poly);

        // 2x^2 / 4x = (1/2)x = 499122177x
        // Normalized: num = [0, 499122177], den = [1]
        assertArrayEquals(new long[]{0, 499122177}, f.num);
        assertArrayEquals(new long[]{1}, f.den);
    }

    @Test
    void testArithmetic() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // f = 1/x
        RationalFunctionFpDynamic f = new RationalFunctionFpDynamic(new long[]{1}, new long[]{0, 1}, poly);
        // g = 1/(x+1)
        RationalFunctionFpDynamic g = new RationalFunctionFpDynamic(new long[]{1}, new long[]{1, 1}, poly);

        // f + g = (x+1 + x) / x(x+1) = (2x + 1) / (x^2 + x)
        RationalFunctionFpDynamic sum = f.add(g);
        assertArrayEquals(new long[]{1, 2}, sum.num);
        assertArrayEquals(new long[]{0, 1, 1}, sum.den);

        // f * g = 1 / (x^2 + x)
        RationalFunctionFpDynamic prod = f.mul(g);
        assertArrayEquals(new long[]{1}, prod.num);
        assertArrayEquals(new long[]{0, 1, 1}, prod.den);

        // f / g = (x+1) / x
        RationalFunctionFpDynamic div = f.div(g);
        assertArrayEquals(new long[]{1, 1}, div.num);
        assertArrayEquals(new long[]{0, 1}, div.den);
    }

    @Test
    void testDifferentiate() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // f = 1/x
        RationalFunctionFpDynamic f = new RationalFunctionFpDynamic(new long[]{1}, new long[]{0, 1}, poly);
        // f' = -1/x^2
        RationalFunctionFpDynamic df = f.differentiate();
        assertArrayEquals(new long[]{998244352}, df.num);
        assertArrayEquals(new long[]{0, 0, 1}, df.den);
    }

    @Test
    void testEvaluate() {
        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        // f = (x+1)/(x-1)
        RationalFunctionFpDynamic f = new RationalFunctionFpDynamic(new long[]{1, 1}, new long[]{998244352, 1}, poly);
        // f(2) = (2+1)/(2-1) = 3/1 = 3
        assertEquals(3, f.evaluate(2));
        // f(3) = (3+1)/(3-1) = 4/2 = 2
        assertEquals(2, f.evaluate(3));
    }
}
