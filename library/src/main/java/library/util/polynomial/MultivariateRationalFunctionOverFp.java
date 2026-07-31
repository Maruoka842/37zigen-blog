package library.util.polynomial;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.RingStrategy;
import java.util.Objects;

/**
 * F_p 上の多変数有理関数 F_p(x_0, x_1, ...)。
 * 多変数多項式 P, Q ∈ F_p[x_0, x_1, ...] の比 P/Q を表す。
 * 演算時には自動的に約分が行われる。
 */
public class MultivariateRationalFunctionOverFp extends FractionFieldElement<MultivariatePolynomial<Long>> {
    /** 有限体の標数 p。 */
    private final long mod;
    /** 多項式の演算戦略。 */
    private final MultivariatePolynomialOverFpStrategy polyStrategy;

    /**
     * 与えられた分子と分母から有理関数を生成する。
     *
     * 数学的表記: P/Q in F_p(x_0, x_1, ...)。
     * 事前条件: den != 0, mod > 0。
     * 事後条件: 自動的に GCD(P, Q) で約分され、分母の主係数が正規化される。
     * 計算量: O(GCD(P, Q) + Division)。
     * @param num 分子 P。
     * @param den 分母 Q。
     * @param mod 標数 p。
     */
    public MultivariateRationalFunctionOverFp(MultivariatePolynomial<Long> num, MultivariatePolynomial<Long> den, long mod) {
        super(normalize(num, den, mod)[0], normalize(num, den, mod)[1], new FractionFieldStrategy<>(new MultivariatePolynomialOverFpStrategy(mod)));
        this.mod = mod;
        this.polyStrategy = new MultivariatePolynomialOverFpStrategy(mod);
    }

    private static MultivariatePolynomial<Long>[] normalize(MultivariatePolynomial<Long> num, MultivariatePolynomial<Long> den, long mod) {
        MultivariatePolynomialOverFpStrategy polyStrategy = new MultivariatePolynomialOverFpStrategy(mod);
        if (polyStrategy.equals(den, polyStrategy.zero())) {
            throw new ArithmeticException("Division by zero");
        }

        MultivariatePolynomial<Long> g = polyStrategy.gcd(num, den);
        MultivariatePolynomial<Long> simplifiedNum = polyStrategy.exactDiv(num, g);
        MultivariatePolynomial<Long> simplifiedDen = polyStrategy.exactDiv(den, g);

        // 分母の主係数を 1 に正規化する
        long lc = simplifiedDen.leadingCoefficient();
        if (lc != 1) {
            long invLc = library.util.MathUtils.modInv(lc, mod);
            return new MultivariatePolynomial[] { simplifiedNum.multiply(invLc), simplifiedDen.multiply(invLc) };
        } else {
            return new MultivariatePolynomial[] { simplifiedNum, simplifiedDen };
        }
    }

    /**
     * 標数を返す。
     *
     * 計算量: O(1)。
     * @return 標数。
     */
    public long getMod() { return mod; }

    /**
     * 加法。
     *
     * 計算量: O(T_P * T_Q)。
     */
    @Override
    public MultivariateRationalFunctionOverFp add(FractionFieldElement<MultivariatePolynomial<Long>> a) {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.add(a);
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 減法。
     *
     * 計算量: O(T_P * T_Q)。
     */
    @Override
    public MultivariateRationalFunctionOverFp sub(FractionFieldElement<MultivariatePolynomial<Long>> a) {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.sub(a);
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 乗法。
     *
     * 計算量: O(T_P * T_Q)。
     */
    @Override
    public MultivariateRationalFunctionOverFp mul(FractionFieldElement<MultivariatePolynomial<Long>> a) {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.mul(a);
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 除法。
     *
     * 計算量: O(T_P * T_Q)。
     */
    @Override
    public MultivariateRationalFunctionOverFp div(FractionFieldElement<MultivariatePolynomial<Long>> a) {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.div(a);
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 符号反転。
     *
     * 計算量: O(T_P)。
     */
    @Override
    public MultivariateRationalFunctionOverFp neg() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.neg();
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 逆元。
     *
     * 計算量: O(GCD + Division)。
     */
    @Override
    public MultivariateRationalFunctionOverFp inv() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.inv();
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 零元。
     *
     * 計算量: O(1)。
     */
    @Override
    public MultivariateRationalFunctionOverFp zero() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.zero();
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 単位元。
     *
     * 計算量: O(1)。
     */
    @Override
    public MultivariateRationalFunctionOverFp one() {
        FractionFieldElement<MultivariatePolynomial<Long>> res = super.one();
        return new MultivariateRationalFunctionOverFp(res.num, res.den, mod);
    }

    /**
     * 指定された変数に関する偏微分を計算する。
     *
     * 数学的表記: ∂f / ∂x_i = (P_{x_i} Q - P Q_{x_i}) / Q^2。
     * 計算量: O(T_P log T_P + T_Q log T_Q + T_P * T_Q)。
     * @param varIdx 変数インデックス。
     * @return 偏微分。
     */
    public MultivariateRationalFunctionOverFp differentiate(int varIdx) {
        MultivariatePolynomial<Long> dP = num.differentiate(varIdx);
        MultivariatePolynomial<Long> dQ = den.differentiate(varIdx);

        // (dP * Q - P * dQ) / Q^2
        MultivariatePolynomial<Long> nextNum = polyStrategy.sub(polyStrategy.mul(dP, den), polyStrategy.mul(num, dQ));
        MultivariatePolynomial<Long> nextDen = polyStrategy.mul(den, den);
        return new MultivariateRationalFunctionOverFp(nextNum, nextDen, mod);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultivariateRationalFunctionOverFp that)) return false;
        // 約分済みなので、分子と分母を比較するだけでよい
        return mod == that.mod && num.equals(that.num) && den.equals(that.den);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den, mod);
    }

    @Override
    public String toString() {
        if (den.isOne()) return num.toString();
        return "(" + num.toString() + ") / (" + den.toString() + ")";
    }

    @Override
    public MultivariateRationalFunctionOverFp self() {
        return this;
    }

    @Override
    public RingStrategy<FractionFieldElement<MultivariatePolynomial<Long>>> parent() {
        return new FractionFieldStrategy<>(new MultivariatePolynomialOverFpStrategy(mod)) {
            @Override public FractionFieldElement<MultivariatePolynomial<Long>> zero() { return MultivariateRationalFunctionOverFp.this.zero(); }
            @Override public FractionFieldElement<MultivariatePolynomial<Long>> one() { return MultivariateRationalFunctionOverFp.this.one(); }
            @Override public FractionFieldElement<MultivariatePolynomial<Long>> add(FractionFieldElement<MultivariatePolynomial<Long>> a, FractionFieldElement<MultivariatePolynomial<Long>> b) { return ((MultivariateRationalFunctionOverFp)a).add(b); }
            @Override public FractionFieldElement<MultivariatePolynomial<Long>> mul(FractionFieldElement<MultivariatePolynomial<Long>> a, FractionFieldElement<MultivariatePolynomial<Long>> b) { return ((MultivariateRationalFunctionOverFp)a).mul(b); }
            @Override public FractionFieldElement<MultivariatePolynomial<Long>> neg(FractionFieldElement<MultivariatePolynomial<Long>> a) { return ((MultivariateRationalFunctionOverFp)a).neg(); }
            @Override public boolean equals(FractionFieldElement<MultivariatePolynomial<Long>> a, FractionFieldElement<MultivariatePolynomial<Long>> b) { return a.equals(b); }
        };
    }
}
