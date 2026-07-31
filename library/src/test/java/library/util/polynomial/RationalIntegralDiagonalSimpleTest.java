package library.util.polynomial;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RationalIntegralDiagonalSimpleTest {
    @Test
    public void testDiagonalSimple() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x0", "x1");
        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("1 - x0 - x1");

        // This should be very fast
        List<long[]> res = RationalIntegralReduction.computeDiagonalPicardFuchs(a, f, 1);
        assertNotNull(res);
    }
}
