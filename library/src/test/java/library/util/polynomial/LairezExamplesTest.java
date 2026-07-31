package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import library.util.MathUtils;

public class LairezExamplesTest {
    private final long mod = 998244353;


    //@Test
    public void testF1AperyPeriod() {
        PolynomialParser parser = PolynomialParser.of(mod, "x1", "x2", "x3", "t");
        // f1 := 1/(1-(1-x*y)*z-t*x*y*z*(1-x)*(1-y)*(1-z));
        MultivariatePolynomial<Long> f = parser.parse("1-(1-x1*x2)*x3-t*x1*x2*x3*(1-x1)*(1-x2)*(1-x3)");

        // Numerator a = 1
        MultivariatePolynomial<Long> a = parser.parse("1");

        // Compute Picard-Fuchs equation.
        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 3, 2);
        assertNotNull(pf);
        // Example 2 states L_R = t^2(t^2-34t+1)d^3 + ... + (t-5)
        long[] a3 = pf.get(3);
        long[] a0 = pf.get(0);

        // Verify ratios at t=1: a3(1)/a0(1) = (1-34+1)/(1-5) = 8
        assertEquals(8, (PolynomialFpDynamic.MOD998244353.evaluate(a3, 1) * MathUtils.modInv(PolynomialFpDynamic.MOD998244353.evaluate(a0, 1), mod)) % mod);
    }
    
    void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}



}
