package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class PolynomialParserTest {
    @Test
    public void testBasicParsing() {
        long mod = 998244353;
        Map<String, Integer> varMap = new HashMap<>();
        varMap.put("x", 0);
        varMap.put("y", 1);
        PolynomialParser parser = new PolynomialParser(mod, varMap, 2);

        // x + y
        MultivariatePolynomial p1 = parser.parse("x + y");
        TreeMap<Monomial, Long> expected1 = new TreeMap<>();
        expected1.put(new Monomial(new int[]{1, 0}), 1L);
        expected1.put(new Monomial(new int[]{0, 1}), 1L);
        assertEquals(expected1, p1.getTerms());

        // x * y - 2
        MultivariatePolynomial p2 = parser.parse("x * y - 2");
        TreeMap<Monomial, Long> expected2 = new TreeMap<>();
        expected2.put(new Monomial(new int[]{1, 1}), 1L);
        expected2.put(new Monomial(new int[]{0, 0}), mod - 2);
        assertEquals(expected2, p2.getTerms());
    }

    @Test
    public void testExponentiationAndParentheses() {
        long mod = 998244353;
        Map<String, Integer> varMap = new HashMap<>();
        varMap.put("x", 0);
        PolynomialParser parser = new PolynomialParser(mod, varMap, 1);

        // (x + 1)^2 = x^2 + 2x + 1
        MultivariatePolynomial p1 = parser.parse("(x + 1)^2");
        TreeMap<Monomial, Long> expected1 = new TreeMap<>();
        expected1.put(new Monomial(new int[]{2}), 1L);
        expected1.put(new Monomial(new int[]{1}), 2L);
        expected1.put(new Monomial(new int[]{0}), 1L);
        assertEquals(expected1, p1.getTerms());
    }

    @Test
    public void testImplicitMultiplication() {
        long mod = 998244353;
        Map<String, Integer> varMap = new HashMap<>();
        varMap.put("x", 0);
        varMap.put("y", 1);
        PolynomialParser parser = new PolynomialParser(mod, varMap, 2);

        // 2x(y+1) = 2xy + 2x
        MultivariatePolynomial p1 = parser.parse("2x(y+1)");
        TreeMap<Monomial, Long> expected1 = new TreeMap<>();
        expected1.put(new Monomial(new int[]{1, 1}), 2L);
        expected1.put(new Monomial(new int[]{1, 0}), 2L);
        assertEquals(expected1, p1.getTerms());
    }
}
