package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.util.List;

public class MulAll2DTest {

    @Test
    public void testEmptyList() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        long[][] expected = poly.one();
        assertArrayEquals(expected, poly.mulAll(List.of()));
    }

    @Test
    public void testSingleElement() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        long[][] f = {{1, 2}, {3, 4}};
        assertTrue(poly.equals(f, poly.mulAll(List.of(new long[][][]{f}))));
    }

    @Test
    public void testIdenticalElements() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        long[][] f = {{1, 1}, {1, 0}}; // 1 + y + x
        List<long[][]> list = List.of(f, f, f);
        long[][] expected = poly.powFull(f, 3);
        assertTrue(poly.equals(expected, poly.mulAll(list)));
    }

    @Test
    public void testMixedElements() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        long[][] f1 = {{1, 1}}; // 1 + y
        long[][] f2 = {{1}, {1}}; // 1 + x
        List<long[][]> list = List.of(f1, f1, f2, f1, f2); // (1+y)^3 * (1+x)^2
        // (1+y)^3 = 1 + 3y + 3y^2 + y^3
        // (1+x)^2 = 1 + 2x + x^2
        // product = (1 + 3y + 3y^2 + y^3) * (1 + 2x + x^2)
        //         = (1 + 3y + 3y^2 + y^3) + 2x(1 + 3y + 3y^2 + y^3) + x^2(1 + 3y + 3y^2 + y^3)
        long[][] expected = {
            {1, 3, 3, 1}, // x^0
            {2, 6, 6, 2}, // x^1
            {1, 3, 3, 1}  // x^2
        };
        assertTrue(poly.equals(expected, poly.mulAll(list)));
    }
}
