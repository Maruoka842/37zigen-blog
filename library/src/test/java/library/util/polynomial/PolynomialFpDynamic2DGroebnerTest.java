package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFpDynamic2D;

public class PolynomialFpDynamic2DGroebnerTest {

    @Test
    public void testGroebnerExample1() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        // x^2 + y
        long[][] f1 = { {0, 1}, {0}, {1} };
        // xy + 1
        long[][] f2 = { {1}, {0, 1} };

        List<long[][]> basis = poly.reducedGroebnerBasis(Arrays.asList(f1, f2));

        // Expect {x - y^2, y^3 + 1}
        // x - y^2 -> {{-y^2}, {1}} -> {{0, 0, -1}, {1}}
        // y^3 + 1 -> {{1, 0, 0, 1}}

        assertEquals(2, basis.size());

        // monic(x - y^2) = x - y^2
        long[][] e1 = { {0, 0, 998244352}, {1} };
        // monic(y^3 + 1) = y^3 + 1
        long[][] e2 = { {1, 0, 0, 1} };

        assertTrue(poly.equals(e1, basis.get(0)));
        assertTrue(poly.equals(e2, basis.get(1)));
    }

    @Test
    public void testGroebnerExample2() {
        PolynomialFpDynamic2D poly = PolynomialFpDynamic2D.MOD998244353;
        // x^3 - 2xy
        long[][] f1 = { {0}, {0, 998244351}, {0}, {1} };
        // x^2y - 2y^2 + x
        long[][] f2 = { {0, 0, 998244351}, {1}, {0, 1} };

        List<long[][]> basis = poly.reducedGroebnerBasis(Arrays.asList(f1, f2));

        // Just verify it doesn't crash and returns a non-empty list
        assertTrue(basis.size() > 0);
    }
}
