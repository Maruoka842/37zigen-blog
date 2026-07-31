package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 高速部分分数分解 (Fast Partial Fraction Decomposition)
 * 計算量: O(N log^2 N)
 */
public class PartialFractionDecomposition {
    public static final long mod = PolynomialFp.mod;

    public static class Term {
        public long[][] numerators; // numerators[j] は f_i^j の分子
        public long[] factor;       // f_i

        public Term(long[][] numerators, long[] factor) {
            this.numerators = numerators;
            this.factor = factor;
        }

        @Override
        public String toString() {
            return "Term{nums=" + Arrays.deepToString(numerators) + ", factor=" + Arrays.toString(factor) + "}";
        }
    }

    public static class Result {
        public long[] polynomialPart;
        public ArrayList<Term> terms;

        public Result(long[] polynomialPart) {
            this.polynomialPart = polynomialPart;
            this.terms = new ArrayList<>();
        }
    }

    /**
     * P(x) / Q(x) を部分分数分解します。
     * Q(x) = Π factors[i].factor^{factors[i].multiplicity}
     */
    public static Result decompose(long[] p, PolynomialFp.Factor[] factors) {
        if (factors == null || factors.length == 0) return new Result(p);

        int r = factors.length;
        // 1. m_i = f_i^{e_i} を作る
        long[][] m = new long[r][];
        for (int i = 0; i < r; i++) {
            m[i] = PolynomialFp.powFull(factors[i].factor, factors[i].multiplicity);
        }

        // 2. Product Tree の構築
        int treeSize = 1;
        while (treeSize < r) treeSize *= 2;
        long[][] productTree = new long[2 * treeSize][];
        for (int i = 0; i < r; i++) productTree[treeSize + i] = m[i];
        for (int i = r; i < treeSize; i++) productTree[treeSize + i] = new long[]{1};
        for (int i = treeSize - 1; i >= 1; i--) {
            productTree[i] = PolynomialFp.mul(productTree[2 * i], productTree[2 * i + 1]);
        }

        long[] Q = productTree[1];

        // 次数の削減
        PolynomialFp.DivModResult dm = PolynomialFp.divmod(p, Q);
        Result result = new Result(dm.q);
        // dm.q + g / Q
        long[] g = dm.r;
        if (PolynomialFp.deg(g) == -1) return result;
        
        // 3. Remainder Tree: v_i = g mod m_i
        long[][] v = new long[2 * treeSize][];
        v[1] = g;
        for (int i = 1; i < treeSize; i++) {
            v[2 * i] = PolynomialFp.mod(v[i], productTree[2 * i]);
            v[2 * i + 1] = PolynomialFp.mod(v[i], productTree[2 * i + 1]);
        }

        // 4. Cofactor Tree: E_i = (Q/m_i) mod m_i
        long[][] eTree = new long[2 * treeSize][];
        eTree[1] = new long[]{1};
        for (int i = 1; i < treeSize; i++) {
            // E_L = (E_v * P_R) mod P_L
            eTree[2 * i] = PolynomialFp.mod(PolynomialFp.mul(eTree[i], productTree[2 * i + 1]), productTree[2 * i]);
            // E_R = (E_v * P_L) mod P_R
            eTree[2 * i + 1] = PolynomialFp.mod(PolynomialFp.mul(eTree[i], productTree[2 * i]), productTree[2 * i + 1]);
        }

        // 5. 各葉で c_i = v_i * E_i^{-1} mod m_i を求め、f_i-adic 展開
        for (int i = 0; i < r; i++) {
            long[] vi = v[treeSize + i];
            long[] Ei = eTree[treeSize + i];
            long[] mi = m[i];
            
            // u_i = Ei^{-1} mod mi
            var egcd = PolynomialFp.extgcd(Ei, mi);
            long[] ui = egcd.a; // s*Ei + t*mi = 1 => s = Ei^{-1} mod mi
            
            // P / Q = ∑ c_i / f_i^{e_i}
            long[] ci = PolynomialFp.mod(PolynomialFp.mul(vi, ui), mi);
            long[][] nums = new long[factors[i].multiplicity + 1][];
            expandRecursive(ci, factors[i].factor, factors[i].multiplicity, nums);
            result.terms.add(new Term(nums, factors[i].factor));
        }

        return result;
    }

    private static void expandRecursive(long[] c, long[] f, int e, long[][] nums) {
        expandRecursiveInner(c, f, e, 0, nums);
    }

    private static void expandRecursiveInner(long[] c, long[] f, int e, int shift, long[][] nums) {
        if (e == 0 || PolynomialFp.deg(c) == -1) return;
        if (e == 1) {
            nums[1 + shift] = (nums[1 + shift] == null) ? c : PolynomialFp.add(nums[1 + shift], c);
            return;
        }

        int h = e / 2;
        long[] ph = PolynomialFp.powFull(f, h);
        PolynomialFp.DivModResult dm = PolynomialFp.divmod(c, ph);
        // c = qf^h + r => c/f^{e+shift} = r/f^{e+shift} + q/f^{e-h+shift}
        expandRecursiveInner(dm.r, f, h, shift + (e - h), nums);
        expandRecursiveInner(dm.q, f, e - h, shift, nums);
    }

    /**
     * P(x) / Q(x) を動的modの多項式演算で部分分数分解します。未テスト。
     * Q(x) = Π factors[i].factor^{factors[i].multiplicity}
     * 計算量: O(N log^2 N)
     */
    public static Result decompose(long[] p, PolynomialFpDynamic.Factor[] factors, PolynomialFpDynamic poly) {
        if (factors == null || factors.length == 0) return new Result(p);

        int r = factors.length;
        long[][] m = new long[r][];
        for (int i = 0; i < r; i++) {
            m[i] = poly.powFull(factors[i].factor, factors[i].multiplicity);
        }

        int treeSize = 1;
        while (treeSize < r) treeSize *= 2;
        long[][] productTree = new long[2 * treeSize][];
        for (int i = 0; i < r; i++) productTree[treeSize + i] = m[i];
        for (int i = r; i < treeSize; i++) productTree[treeSize + i] = new long[] {1};
        for (int i = treeSize - 1; i >= 1; i--) {
            productTree[i] = poly.mul(productTree[2 * i], productTree[2 * i + 1]);
        }

        long[] Q = productTree[1];
        PolynomialFpDynamic.DivModResult dm = poly.divmod(p, Q);
        Result result = new Result(dm.q);
        long[] g = dm.r;
        if (poly.deg(g) == -1) return result;

        long[][] v = new long[2 * treeSize][];
        v[1] = g;
        for (int i = 1; i < treeSize; i++) {
            v[2 * i] = poly.mod(v[i], productTree[2 * i]);
            v[2 * i + 1] = poly.mod(v[i], productTree[2 * i + 1]);
        }

        long[][] eTree = new long[2 * treeSize][];
        eTree[1] = new long[] {1};
        for (int i = 1; i < treeSize; i++) {
            eTree[2 * i] = poly.mod(poly.mul(eTree[i], productTree[2 * i + 1]), productTree[2 * i]);
            eTree[2 * i + 1] = poly.mod(poly.mul(eTree[i], productTree[2 * i]), productTree[2 * i + 1]);
        }

        for (int i = 0; i < r; i++) {
            long[] vi = v[treeSize + i];
            long[] Ei = eTree[treeSize + i];
            long[] mi = m[i];
            long[] ui = poly.extgcd(Ei, mi).x();
            long[] ci = poly.mod(poly.mul(vi, ui), mi);
            long[][] nums = new long[factors[i].multiplicity + 1][];
            expandRecursive(ci, factors[i].factor, factors[i].multiplicity, nums, poly);
            result.terms.add(new Term(nums, factors[i].factor));
        }

        return result;
    }

    private static void expandRecursive(long[] c, long[] f, int e, long[][] nums, PolynomialFpDynamic poly) {
        expandRecursiveInner(c, f, e, 0, nums, poly);
    }

    private static void expandRecursiveInner(long[] c, long[] f, int e, int shift, long[][] nums, PolynomialFpDynamic poly) {
        if (e == 0 || poly.deg(c) == -1) return;
        if (e == 1) {
            nums[1 + shift] = (nums[1 + shift] == null) ? c : poly.add(nums[1 + shift], c);
            return;
        }

        int h = e / 2;
        long[] ph = poly.powFull(f, h);
        PolynomialFpDynamic.DivModResult dm = poly.divmod(c, ph);
        expandRecursiveInner(dm.r, f, h, shift + (e - h), nums, poly);
        expandRecursiveInner(dm.q, f, e - h, shift, nums, poly);
    }

}
