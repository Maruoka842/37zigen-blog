package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Reproduction suite for identified bugs in RationalIntegralReduction.
 */
public class RationalIntegralReductionBugTest {

    @Test
    public void testBug2VarMinimal() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");
        
        // J(t) = Res_{x=0} (1 / x(1-x-t)) = 1/(1-t)
        // Satisfies (1-t)J' - J = 0.
        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("x - x^2 - x*t");

        // We use tVarIdx = 1 (parameter t)
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 1, 1);
        assertNotNull(pf, "Should return a non-null operator");
        
        long[] J = new long[15];
        for(int i=0; i<15; i++) J[i] = 1; // 1 + t + t^2 + ... = 1/(1-t)
        
        assertTrue(verify(pf, J, mod), "Picard-Fuchs operator for 1/(1-t) is incorrect");
    }

    @Test
    public void testBug3VarMinimal() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "t");
        
        // J(t) = [x^0 y^0] (1 / (1-x-y-t)) = 1/(1-t)
        MultivariatePolynomial a = parser.parse("1");
        // We use xy(1-x-y-t) to get the constant term via residue at origin.
        MultivariatePolynomial f = parser.parse("x*y - x^2*y - x*y^2 - x*y*t");

        // tVarIdx = 2
        List<long[]> pf = RationalIntegralReduction.computePicardFuchs(a, f, 2, 1);
        assertNotNull(pf, "Should return a non-null operator");
        
        long[] J = new long[15];
        for(int i=0; i<15; i++) J[i] = 1; // 1 + t + t^2 + ... = 1/(1-t)
        
        assertTrue(verify(pf, J, mod), "Picard-Fuchs operator for 1/(1-t) in 3 variables is incorrect");
    }

    @Test
    public void testBug2VarMinimalMatrix() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "t");

        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("x - x^2 - x*t");

        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 1, 1);
        assertNotNull(pf, "Should return a non-null operator");

        long[] J = new long[15];
        for(int i=0; i<15; i++) J[i] = 1;

        assertTrue(verify(pf, J, mod), "Picard-Fuchs matrix operator for 1/(1-t) is incorrect");
    }

    @Test
    public void testBug3VarMinimalMatrix() {
        long mod = 998244353;
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "t");

        MultivariatePolynomial a = parser.parse("1");
        MultivariatePolynomial f = parser.parse("x*y - x^2*y - x*y^2 - x*y*t");

        List<long[]> pf = RationalIntegralReduction.computePicardFuchsMatrix(a, f, 2, 1);
        assertNotNull(pf, "Should return a non-null operator");

        long[] J = new long[15];
        for(int i=0; i<15; i++) J[i] = 1;

        assertTrue(verify(pf, J, mod), "Picard-Fuchs matrix operator for 1/(1-t) in 3 variables is incorrect");
    }

    private boolean verify(List<long[]> pf, long[] J, long mod) {
        int order = pf.size() - 1, n = J.length;
        long[] res = new long[n];
        for (int i = 0; i <= order; i++) {
            long[] dJ = J.clone();
            for (int d = 0; d < i; d++) {
                long[] next = new long[n];
                for (int k = 1; k < n; k++) next[k - 1] = (dJ[k] * k) % mod;
                dJ = next;
            }
            long[] term = new long[n];
            long[] ai = pf.get(i);
            for (int j = 0; j < ai.length; j++) {
                if (ai[j] == 0) continue;
                for (int k = 0; j + k < n; k++) term[j + k] = (term[j + k] + ai[j] * dJ[k]) % mod;
            }
            for (int k = 0; k < n; k++) res[k] = (res[k] + term[k]) % mod;
        }
        // Result should be 0.
        for (int k = 0; k < n - order - 1; k++) if (res[k] != 0) return false;
        return true;
    }
}
