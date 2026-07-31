package library.util.polynomial;

import java.util.Objects;
import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.RingStrategy;

/**
 * F_p 上の 1 変数有理関数 F_p(x)。
 * 多項式 P, Q ∈ F_p[x] の比 P/Q を表す。
 * 演算時には自動的に約分が行われる。
 */
public class RationalFunctionFpDynamic extends FractionFieldElement<long[]> {
    /** 多項式の演算戦略。 */
    private final PolynomialFpDynamic polyStrategy;

    /**
     * 与えられた分子と分母から有理関数を生成する。
     *
     * <p>数学的表記: P/Q in F_p(x)。
     * <p>事前条件: den != 0。
     * <p>事後条件: 自動的に GCD(P, Q) で約分され、分母の主係数が正規化される。
     * <p>計算量: O(GCD(P, Q) + Division)。
     * <p>未テスト。
     * @param num 分子 P。
     * @param den 分母 Q。
     * @param polyStrategy 多項式演算戦略。
     */
    public RationalFunctionFpDynamic(long[] num, long[] den, PolynomialFpDynamic polyStrategy) {
        this(normalize(num, den, polyStrategy), polyStrategy);
    }

    private RationalFunctionFpDynamic(long[][] normalized, PolynomialFpDynamic polyStrategy) {
        super(normalized[0], normalized[1], new FractionFieldStrategy<>(polyStrategy));
        this.polyStrategy = polyStrategy;
    }

    private static long[][] normalize(long[] num, long[] den, PolynomialFpDynamic polyStrategy) {
        if (polyStrategy.isZero(den)) {
            throw new ArithmeticException("Division by zero");
        }

        long[] g = polyStrategy.gcd(num, den);
        long[] simplifiedNum = polyStrategy.resize(polyStrategy.exactDiv(num, g));
        long[] simplifiedDen = polyStrategy.resize(polyStrategy.exactDiv(den, g));

        int d = polyStrategy.deg(simplifiedDen);
        if (d == -1) throw new ArithmeticException("Division by zero");
        long lc = simplifiedDen[d];
        if (lc != 1) {
            long invLc = polyStrategy.getFp().inv(lc);
            simplifiedNum = polyStrategy.mul(simplifiedNum, invLc);
            simplifiedDen = polyStrategy.mul(simplifiedDen, invLc);
        }
        return new long[][] { simplifiedNum, simplifiedDen };
    }

    /**
     * 加法。
     *
     * <p>計算量: O(T_P * T_Q)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic add(FractionFieldElement<long[]> a) {
        FractionFieldElement<long[]> res = super.add(a);
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 減法。
     *
     * <p>計算量: O(T_P * T_Q)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic sub(FractionFieldElement<long[]> a) {
        FractionFieldElement<long[]> res = super.sub(a);
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 乗法。
     *
     * <p>計算量: O(T_P * T_Q)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic mul(FractionFieldElement<long[]> a) {
        FractionFieldElement<long[]> res = super.mul(a);
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 除法。
     *
     * <p>計算量: O(T_P * T_Q)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic div(FractionFieldElement<long[]> a) {
        FractionFieldElement<long[]> res = super.div(a);
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 符号反転。
     *
     * <p>計算量: O(T_P)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic neg() {
        FractionFieldElement<long[]> res = super.neg();
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 逆元。
     *
     * <p>計算量: O(GCD + Division)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic inv() {
        FractionFieldElement<long[]> res = super.inv();
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 零元。
     *
     * <p>計算量: O(1)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic zero() {
        FractionFieldElement<long[]> res = super.zero();
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 単位元。
     *
     * <p>計算量: O(1)。
     * <p>未テスト。
     */
    @Override
    public RationalFunctionFpDynamic one() {
        FractionFieldElement<long[]> res = super.one();
        return new RationalFunctionFpDynamic(new long[][] { res.num, res.den }, polyStrategy);
    }

    /**
     * 微分を計算する。
     *
     * <p>数学的表記: (P/Q)' = (P'Q - PQ') / Q^2。
     * <p>計算量: O(M(deg P + deg Q))。
     * <p>未テスト。
     * @return 微分結果。
     */
    public RationalFunctionFpDynamic differentiate() {
        long[] dP = polyStrategy.differentiate(num);
        long[] dQ = polyStrategy.differentiate(den);

        // (dP * Q - P * dQ) / Q^2
        long[] nextNum = polyStrategy.sub(polyStrategy.mul(dP, den), polyStrategy.mul(num, dQ));
        long[] nextDen = polyStrategy.mul(den, den);
        return new RationalFunctionFpDynamic(nextNum, nextDen, polyStrategy);
    }

    /**
     * 有理関数を指定された点で評価する。
     *
     * <p>数学的表記: f(x) = P(x) / Q(x)。
     * <p>事前条件: Q(x) != 0。
     * <p>計算量: O(deg P + deg Q)。
     * <p>未テスト。
     * @param x 評価点。
     * @return 評価値。
     */
    public long evaluate(long x) {
        long vNum = polyStrategy.evaluate(num, x);
        long vDen = polyStrategy.evaluate(den, x);
        if (vDen == 0) {
            throw new ArithmeticException("Division by zero at evaluation point");
        }
        return polyStrategy.getFp().mul(vNum, polyStrategy.getFp().inv(vDen));
    }

    /**
     * 多項式演算戦略を返す。
     * @return 多項式演算戦略。
     */
    public PolynomialFpDynamic getPolyStrategy() {
        return polyStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RationalFunctionFpDynamic that)) return false;
        // normalize() により規約かつ分母の主係数が1になっているため、分子と分母の比較で十分
        return Objects.equals(polyStrategy, that.polyStrategy) && polyStrategy.equals(num, that.num) && polyStrategy.equals(den, that.den);
    }

    @Override
    public int hashCode() {
        return Objects.hash(polyStrategy, java.util.Arrays.hashCode(num), java.util.Arrays.hashCode(den));
    }

    @Override
    public String toString() {
        if (polyStrategy.equals(den, polyStrategy.one())) return java.util.Arrays.toString(num);
        return "(" + java.util.Arrays.toString(num) + ") / (" + java.util.Arrays.toString(den) + ")";
    }

    @Override
    public RationalFunctionFpDynamic self() {
        return this;
    }

    @Override
    public RingStrategy<FractionFieldElement<long[]>> parent() {
        return new FractionFieldStrategy<>(polyStrategy) {
            @Override public FractionFieldElement<long[]> zero() { return RationalFunctionFpDynamic.this.zero(); }
            @Override public FractionFieldElement<long[]> one() { return RationalFunctionFpDynamic.this.one(); }
            @Override public FractionFieldElement<long[]> add(FractionFieldElement<long[]> a, FractionFieldElement<long[]> b) { return ((RationalFunctionFpDynamic)a).add(b); }
            @Override public FractionFieldElement<long[]> mul(FractionFieldElement<long[]> a, FractionFieldElement<long[]> b) { return ((RationalFunctionFpDynamic)a).mul(b); }
            @Override public FractionFieldElement<long[]> neg(FractionFieldElement<long[]> a) { return ((RationalFunctionFpDynamic)a).neg(); }
            @Override public boolean equals(FractionFieldElement<long[]> a, FractionFieldElement<long[]> b) { return a.equals(b); }
        };
    }
}
