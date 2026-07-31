package library.test;
import library.util.polynomial.*;
import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.polynomial.PolynomialFpDynamic;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AperyProfilingTest {
    @Test
    public void profileApery() {
        // F = 1/(1-(1-x*y)*z-t*x*y*z*(1-x)*(1-y)*(1-z))
        long mod = 998244353;
        FractionFieldStrategy<long[]> ffs = new FractionFieldStrategy<>(PolynomialFpDynamic.of(mod));
        PolynomialParser parser = PolynomialParser.of(mod, "x", "y", "z", "t");
        MultivariatePolynomial<Long> f = parser.parse("1-(1-x*y)*z-t*x*y*z*(1-x)*(1-y)*(1-z)");

        long u = 42;
        MultivariatePolynomial<Long> fu = f.evaluate(3, u);

        List<MultivariatePolynomial<Long>> df = new ArrayList<>();
        for (int i = 0; i < 3; i++) df.add(fu.differentiate(i));

        long start = System.currentTimeMillis();
        List<MultivariatePolynomial<Long>> gb = MultivariatePolynomial.grobnerBasis(df);
        long end = System.currentTimeMillis();

        System.err.println("MultivariatePolynomial GB size: " + gb.size() + " Time: " + (end - start) + "ms");

        List<MultivariatePolynomialOverFpFunctionField> dfF = new ArrayList<>();
        for (MultivariatePolynomial<Long> p : df) {
            dfF.add(toSpatial(p, 3));
        }

        start = System.currentTimeMillis();
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> tgb = MultivariatePolynomialOverFpFunctionField.taggedGrobnerBasis(dfF).idealBasis();
        end = System.currentTimeMillis();
        System.err.println("MultivariateFractionPolynomial Tagged GB size: " + tgb.size() + " Time: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        List<MultivariatePolynomialOverFpFunctionField[]> syz = MultivariatePolynomialOverFpFunctionField.computeSyzygiesOfGB(tgb);
        end = System.currentTimeMillis();
        System.err.println("MultivariateFractionPolynomial Syzygies of GB size: " + syz.size() + " Time: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> submoduleGB = new ArrayList<>();
        for (MultivariatePolynomialOverFpFunctionField.TaggedPolynomial g : tgb) {
            MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[3];
            for (int i = 0; i < 3; i++) coeffs[i] = g.coeffs[i].multiply(new MultivariatePolynomialOverFpFunctionField(mod, new TreeMap<>(Map.of(new Monomial(new int[0]), ffs.of(new long[]{mod-1}, new long[]{1})))));
            submoduleGB.add(new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(g.poly, coeffs));
        }
        for (MultivariatePolynomialOverFpFunctionField[] sGB : syz) {
            MultivariatePolynomialOverFpFunctionField[] sOrig = MultivariatePolynomialOverFpFunctionField.convertSyzygy(sGB, tgb);
            submoduleGB.add(new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(new MultivariatePolynomialOverFpFunctionField(mod), sOrig));
        }
        submoduleGB = MultivariatePolynomialOverFpFunctionField.moduleReduceGrobnerBasis(submoduleGB);
        end = System.currentTimeMillis();
        System.err.println("Submodule GB (incl syz and reduce) size: " + submoduleGB.size() + " Time: " + (end - start) + "ms");
    }

    private MultivariatePolynomialOverFpFunctionField toSpatial(MultivariatePolynomial<Long> p, int tVarIdx) {
        TreeMap<Monomial, FractionFieldElement<long[]>> terms = new TreeMap<>();
        long mod = (p instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) p.getCoefficientRing()).getMod();
        FractionFieldStrategy<long[]> ffs = new FractionFieldStrategy<>(PolynomialFpDynamic.of(mod));
        for (Map.Entry<Monomial, Long> entry : p.getTerms().entrySet()) {
            Monomial m = entry.getKey();
            int et = tVarIdx >= 0 ? m.getExponent(tVarIdx) : 0;
            int[] spatialExps = new int[m.size()];
            for (int i = 0; i < m.size(); i++) if (i != tVarIdx) spatialExps[i] = m.getExponent(i);
            Monomial sm = new Monomial(spatialExps);
            long[] num = new long[et + 1];
            num[et] = entry.getValue();
            terms.merge(sm, ffs.of(num, new long[]{1}), (a, b) -> a); // simplified
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, terms);
    }
}
