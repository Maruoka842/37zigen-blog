package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;

public class TaggedPolynomialTest {
    @Test
    public void testTaggedGrobnerBasis() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        // f1 = x^2 + y
        // f2 = x^2 + 1
        // r1 = f1 - f2 = y - 1

        MultivariatePolynomialOverFpFunctionField f1 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{2, 0}), field.one())
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one()));
        MultivariatePolynomialOverFpFunctionField f2 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{2, 0}), field.one())
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 0}), field.one()));

        List<MultivariatePolynomialOverFpFunctionField> fs = Arrays.asList(f1, f2);
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> gb = MultivariatePolynomialOverFpFunctionField.taggedGrobnerBasis(fs).idealBasis();

        assertFalse(gb.isEmpty());

        for (MultivariatePolynomialOverFpFunctionField.TaggedPolynomial g : gb) {
            MultivariatePolynomialOverFpFunctionField sum = new MultivariatePolynomialOverFpFunctionField(mod);
            for (int i = 0; i < fs.size(); i++) {
                sum = sum.add(g.coeffs[i].multiply(fs.get(i)));
            }
            // Check g.poly == sum coeffs[i] * fs[i]
            assertTrue(g.poly.subtract(sum).isZero(), "g.poly should be sum of coeffs * fs");
        }
    }

    @Test
    public void testTaggedDivide() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        MultivariatePolynomialOverFpFunctionField f1 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 0}), field.one()); // x
        MultivariatePolynomialOverFpFunctionField f2 = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one()); // y

        List<MultivariatePolynomialOverFpFunctionField> fs = Arrays.asList(f1, f2);
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> tfs = new ArrayList<>();
        for(int i=0; i<fs.size(); i++) {
            MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[fs.size()];
            for(int j=0; j<fs.size(); j++) coeffs[j] = new MultivariatePolynomialOverFpFunctionField(mod);
            coeffs[i] = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[0]), field.one());
            tfs.add(new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(fs.get(i), coeffs));
        }

        // p = (y+1)*x + 1*y + 1
        MultivariatePolynomialOverFpFunctionField p = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 1}), field.one())
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 0}), field.one()))
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one()))
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 0}), field.one()));

        MultivariatePolynomialOverFpFunctionField[] pCoeffs = new MultivariatePolynomialOverFpFunctionField[2];
        pCoeffs[0] = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one())
                .add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 0}), field.one())); // y+1
        pCoeffs[1] = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 0}), field.one()); // 1

        // p - (pCoeffs[0]*x + pCoeffs[1]*y) = 1

        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial tp = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(p, pCoeffs);

        MultivariatePolynomialOverFpFunctionField.TaggedDivRem dr = MultivariatePolynomialOverFpFunctionField.taggedDivide(tp, tfs);

        // p = sum q_i * f_i + r
        // tag(r) = tag(p) - sum q_i * tag(f_i)
        // r.poly = sum r.coeffs[i] * f_i + (p - sum p.coeffs[i] * f_i)
        // Wait, if f = sum b_i r_i + rem_f, and g_j = sum a_{ji} r_i,
        // then r = f - sum q_j g_j = (sum b_i r_i + rem_f) - sum q_j (sum a_{ji} r_i)
        // r = sum (b_i - sum q_j a_{ji}) r_i + rem_f.
        // So r.poly - rem_f = sum r.coeffs[i] * f_i.

        MultivariatePolynomialOverFpFunctionField rem_p = p.subtract(pCoeffs[0].multiply(fs.get(0))).subtract(pCoeffs[1].multiply(fs.get(1)));

        MultivariatePolynomialOverFpFunctionField sum = new MultivariatePolynomialOverFpFunctionField(mod);
        for(int i=0; i<fs.size(); i++) {
            sum = sum.add(dr.remainder.coeffs[i].multiply(fs.get(i)));
        }
        assertTrue(dr.remainder.poly.subtract(rem_p).subtract(sum).isZero(), "r.poly - rem_p should be sum of its coeffs * generators");

        // Also check p = sum q_i * f_i + r.poly
        MultivariatePolynomialOverFpFunctionField pReconstructed = dr.remainder.poly;
        for(int i=0; i<fs.size(); i++) {
            pReconstructed = pReconstructed.add(dr.quotients.get(i).multiply(fs.get(i)));
        }
        assertTrue(p.subtract(pReconstructed).isZero(), "p should be sum of q_i * f_i + r");
    }

    @Test
    public void testModuleGrobnerBasis() {
        long mod = 998244353;
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);

        // Vector 1: [x, 1]
        // Vector 2: [y, 0]
        MultivariatePolynomialOverFpFunctionField x = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{1, 0}), field.one());
        MultivariatePolynomialOverFpFunctionField y = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[]{0, 1}), field.one());
        MultivariatePolynomialOverFpFunctionField one = MultivariatePolynomialOverFpFunctionField.singleTerm(mod, new Monomial(new int[0]), field.one());
        MultivariatePolynomialOverFpFunctionField zero = new MultivariatePolynomialOverFpFunctionField(mod);

        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial v1 = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(x, new MultivariatePolynomialOverFpFunctionField[]{one});
        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial v2 = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(y, new MultivariatePolynomialOverFpFunctionField[]{zero});

        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> gens = Arrays.asList(v1, v2);
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> gb = MultivariatePolynomialOverFpFunctionField.moduleGrobnerBasis(gens);

        assertFalse(gb.isEmpty());

        // Target [0, y] should be in the submodule: y*[x, 1] - x*[y, 0] = [xy, y] - [xy, 0] = [0, y]
        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial target = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(zero, new MultivariatePolynomialOverFpFunctionField[]{y});
        MultivariatePolynomialOverFpFunctionField.TaggedDivRem dr = MultivariatePolynomialOverFpFunctionField.moduleDivide(target, gb);
        assertTrue(dr.remainder.isZero(), "Target [0, y] should be reducible to zero by GB");
    }
}
