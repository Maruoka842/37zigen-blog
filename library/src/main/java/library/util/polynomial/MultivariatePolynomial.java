package library.util.polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.StringJoiner;
import java.util.TreeMap;

import library.util.algebra.instance.MonoidRingElement;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.MonoidRingStrategy;

/**
 * 可換係数環 R 上の多変数多項式 R[x_0,x_1,...]。
 * 単項式とその係数の和を表す。
 * 基本的な算術演算、微分、および体係数の場合の多変数除算・Buchberger のアルゴリズムを実装する。
 *
 * @param <C> 係数の型。
 */
public class MultivariatePolynomial<C> extends MonoidRingElement<C, Monomial, MultivariatePolynomial<C>> {
    /** 係数環 R の演算。 */
    private final CommutativeRingStrategy<C> coefficientRing;
    /** 単項式から係数へのマッピング。単項式の比較順序でソートされ、零係数を含まない。 */
    private TreeMap<Monomial, C> terms;

    /**
     * 係数環 R 上の零多項式を生成する。
     * 未テスト。
     * 数学的表記: 0 in R[x_0,x_1,...]。
     * 事前条件: coefficientRing != null。
     * 事後条件: supp(this) = emptyset。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: coefficientRing 参照を共有し、項写像は新規所有する。
     * 例外・未定義条件: coefficientRing == null のとき NullPointerException。
     * @param coefficientRing 係数環 R。
     */
    public MultivariatePolynomial(CommutativeRingStrategy<C> coefficientRing) {
        this(coefficientRing, Collections.emptyMap());
    }

    /**
     * this := this - factor * m * other を行う。
     * 未テスト。
     * 数学的表記: f <- f - c * m * g。
     * 事前条件: other != null, factor != null, m != null, R_this = R_other。
     * 事後条件: this は旧 this - factor * m * other を表し、零係数項を含まない。
     * 副作用: this.terms と this.val を破壊的に変更する。
     * 計算量: O(T_other log T_this)。
     * 破壊的変更: this を変更し、other を変更しない。
     * 参照共有・所有権: other の Monomial/係数参照を this が共有することがある。
     * 例外・未定義条件: other == null のとき NullPointerException、係数環が異なるとき IllegalArgumentException。
     * @param other 減算する多項式のベース。
     * @param factor 係数倍 c。
     * @param m 単項式倍 m。
     */
    public void subtractInPlace(MultivariatePolynomial<C> other, C factor, Monomial m) {
        ensureSameRing(other);
        if (coefficientRing.equals(factor, coefficientRing.zero())) return;
        for (Map.Entry<Monomial, C> entry : other.terms.entrySet()) {
            Monomial monomial = entry.getKey().mul(m);
            C scaledCoeff = coefficientRing.mul(factor, entry.getValue());
            C old = terms.get(monomial);
            C res = (old == null) ? coefficientRing.neg(scaledCoeff) : coefficientRing.sub(old, scaledCoeff);
            if (coefficientRing.equals(res, coefficientRing.zero())) terms.remove(monomial);
            else terms.put(monomial, res);
        }
    }

    /**
     * 係数環 R 上の多項式 Σ_m terms[m]m を生成する。
     * 未テスト。
     * 数学的表記: f = Σ_{m in supp(terms)} terms(m)m in R[x_0,x_1,...]。
     * 事前条件: coefficientRing != null, terms != null, すべての key/value != null。
     * 事後条件: this.terms は TreeMap、かつ ∀m, terms[m] = 0_R の項を含まない。
     * 副作用: なし。
     * 計算量: O(T log T)。
     * 破壊的変更: terms を変更しない。
     * 参照共有・所有権: coefficientRing と非零係数・単項式の参照を共有し、項写像は新規所有する。
     * 例外・未定義条件: null 引数または null 要素のとき NullPointerException。
     * @param coefficientRing 係数環 R。
     * @param terms 初期項写像。
     */
    public MultivariatePolynomial(CommutativeRingStrategy<C> coefficientRing, Map<Monomial, C> terms) {
        this(coefficientRing, normalizedTerms(coefficientRing, terms), true);
    }

    /**
     * 正規化済み項写像を所有する多項式を生成する。
     * 未テスト。
     * 数学的表記: f = Σ_{m in supp(normalizedTerms)} normalizedTerms(m)m。
     * 事前条件: coefficientRing != null, normalizedTerms は非零係数だけを含む TreeMap。
     * 事後条件: this.val == this.terms == normalizedTerms。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: normalizedTerms の所有権を this へ移す。
     * 参照共有・所有権: normalizedTerms を this が所有し、coefficientRing を共有する。
     * 例外・未定義条件: 引数が事前条件を満たさないと未定義。
     * @param coefficientRing 係数環 R。
     * @param normalizedTerms 正規化済み項写像。
     * @param ignored オーバーロード識別子。
     */
    private MultivariatePolynomial(CommutativeRingStrategy<C> coefficientRing, TreeMap<Monomial, C> normalizedTerms, boolean ignored) {
        super(normalizedTerms, polynomialParent(Objects.requireNonNull(coefficientRing)));
        this.coefficientRing = coefficientRing;
        this.terms = normalizedTerms;
    }

    /**
     * この係数環用の親ストラテジを生成する。
     * 未テスト。
     * 数学的表記: R[M] where M is the monomial monoid。
     * 事前条件: coefficientRing != null。
     * 事後条件: 戻り値は R[M] の zero, one, +, * を実装する。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: coefficientRing 参照を共有する。
     * 例外・未定義条件: coefficientRing == null のとき NullPointerException。
     * @param coefficientRing 係数環 R。
     * @return 親ストラテジ。
     */
    private static <C> MonoidRingStrategy<C, Monomial, MultivariatePolynomial<C>> polynomialParent(CommutativeRingStrategy<C> coefficientRing) {
        Objects.requireNonNull(coefficientRing);
        return new MonoidRingStrategy<>(coefficientRing, Monomial.STRATEGY) {
            @Override
            protected MultivariatePolynomial<C> create(Map<Monomial, C> normalizedVal) {
                return new MultivariatePolynomial<>(coefficientRing, normalizedVal);
            }
        };
    }

    /**
     * 項写像を係数環の等号で正規化する。
     * 未テスト。
     * 数学的表記: {m -> c | (m,c) in terms, c != 0_R}。
     * 事前条件: coefficientRing != null, terms != null, すべての key/value != null。
     * 事後条件: 戻り値は TreeMap で、零係数を含まない。
     * 副作用: なし。
     * 計算量: O(T log T)。
     * 破壊的変更: terms を変更しない。
     * 参照共有・所有権: key/value 参照を共有し、Map は新規所有する。
     * 例外・未定義条件: null 引数または null 要素のとき NullPointerException。
     * @param coefficientRing 係数環 R。
     * @param terms 項写像。
     * @return 正規化済み TreeMap。
     */
    private static <C> TreeMap<Monomial, C> normalizedTerms(CommutativeRingStrategy<C> coefficientRing, Map<Monomial, C> terms) {
        Objects.requireNonNull(coefficientRing);
        Objects.requireNonNull(terms);
        TreeMap<Monomial, C> normalized = new TreeMap<>();
        C zero = coefficientRing.zero();
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            Monomial monomial = Objects.requireNonNull(entry.getKey());
            C coefficient = coefficientRing.add(Objects.requireNonNull(entry.getValue()), zero);
            if (!coefficientRing.equals(coefficient, zero)) {
                normalized.merge(monomial, coefficient, coefficientRing::add);
                if (coefficientRing.equals(normalized.get(monomial), zero)) normalized.remove(monomial);
            }
        }
        return normalized;
    }

    /**
     * 係数環 R を返す。
     * 未テスト。
     * 数学的表記: R。
     * 事前条件: なし。
     * 事後条件: 戻り値 == コンストラクタに渡した係数環。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 係数環参照を共有する。
     * 例外・未定義条件: なし。
     * @return 係数環。
     */
    public CommutativeRingStrategy<C> getCoefficientRing() {
        return coefficientRing;
    }

    /**
     * この多項式が属する多変数多項式環を返す。
     * 未テスト。
     *
     * @return 多変数多項式環。
     */
    public MultivariatePolynomialRing<C> ring() {
        return new MultivariatePolynomialRing<>(coefficientRing);
    }

    /**
     * 項写像を返す。
     * 事前条件: なし。
     * 事後条件: 戻り値 == 内部 TreeMap。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 内部 Map と key/value 参照を共有し、呼び出し側が変更できる。
     * 例外・未定義条件: 呼び出し側が変更した場合は cleanup 前まで零係数を含む可能性がある。
     * @return 項写像。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TreeMap<Monomial, C> getTerms() {
        return terms;
    }

    /**
     * 係数型を保った項写像を返す。
     * 未テスト。
     * 数学的表記: m -> c_m。
     * 事前条件: なし。
     * 事後条件: 戻り値 == 内部 TreeMap。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 内部 Map と key/value 参照を共有し、呼び出し側が変更できる。
     * 例外・未定義条件: 呼び出し側が変更した場合は cleanup 前まで零係数を含む可能性がある。
     * @return 項写像。
     */
    public TreeMap<Monomial, C> getCoefficientTerms() {
        return terms;
    }

    /**
     * this と other が同じ係数環上にあることを検査する。
     * 未テスト。
     * 数学的表記: R_this = R_other。
     * 事前条件: other != null。
     * 事後条件: 同じ係数環なら正常終了する。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: なし。
     * 例外・未定義条件: other == null のとき NullPointerException、係数環が異なるとき IllegalArgumentException。
     * @param other 比較対象。
     */
    private void ensureSameRing(MultivariatePolynomial<C> other) {
        Objects.requireNonNull(other);
        if (Objects.equals(coefficientRing, other.coefficientRing)) return;
        throw new IllegalArgumentException("coefficient rings differ");
    }

    /**
     * this := this + other を行う。
     * 未テスト。
     * 数学的表記: f <- f + g。
     * 事前条件: other != null, R_this = R_other。
     * 事後条件: this は旧 this + other を表し、零係数項を含まない。
     * 副作用: this.terms と this.val を破壊的に変更する。
     * 計算量: O(T_other log T_this)。
     * 破壊的変更: this を変更し、other を変更しない。
     * 参照共有・所有権: other の Monomial/係数参照を this が共有することがある。
     * 例外・未定義条件: other == null のとき NullPointerException、係数環が異なるとき IllegalArgumentException。
     * @param other 加算する多項式。
     */
    public void addInplace(MultivariatePolynomial<C> other) {
        ensureSameRing(other);
        for (Map.Entry<Monomial, C> entry : other.terms.entrySet()) {
            Monomial monomial = entry.getKey();
            C coefficient = entry.getValue();
            C old = terms.get(monomial);
            C sum = (old == null) ? coefficient : coefficientRing.add(old, coefficient);
            if (coefficientRing.equals(sum, coefficientRing.zero())) terms.remove(monomial);
            else terms.put(monomial, sum);
        }
    }

    /**
     * 主単項式 (LM) を返す。
     * 計算量: O(1) (TreeMap がソートされているため)。
     */
    public Monomial getLeadingMonomial() {
        return terms.isEmpty() ? null : terms.lastKey();
    }

    /**
     * 主係数を係数型のまま返す。
     * 未テスト。
     * 数学的表記: LC(f)。
     * 事前条件: なし。
     * 事後条件: f = 0 なら 0_R、そうでなければ [LM(f)]f。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 内部係数参照を共有することがある。
     * 例外・未定義条件: なし。
     * @return 主係数。
     */
    public C leadingCoefficient() {
        return terms.isEmpty() ? coefficientRing.zero() : terms.get(terms.lastKey());
    }

    /**
     * MonoidRingElement の値から同じ係数環の多変数多項式を構築する。
     * 未テスト。
     * 数学的表記: f = Σ_{m in supp(terms)} terms(m)m in R[M]。
     * 事前条件: terms != null, terms の key と value は null でない。
     * 事後条件: 戻り値.val は TreeMap で、0 係数は含まれない。
     * 副作用: なし。
     * 計算量: O(T log T)。
     * 破壊的変更: terms を変更しない。
     * 参照共有・所有権: Monomial 参照を再利用し、Map は新規所有。
     * 例外・未定義条件: null 要素を含むとき未定義。
     * @param terms 項の写像。
     * @return 多変数多項式。
     */
    @Override
    protected MultivariatePolynomial<C> fromMap(Map<Monomial, C> terms) {
        return new MultivariatePolynomial<>(coefficientRing, terms);
    }

    /**
     * 変数のインデックスを置換した新しい多項式を返す。
     * 未テスト。
     * @param p 置換マップ (i -> p[i])。
     * @return 置換された多項式。
     */
    public MultivariatePolynomial<C> permuteVariables(int[] p) {
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            res.put(entry.getKey().permuteVariables(p), entry.getValue());
        }
        return fromMap(res);
    }

    /**
     * 指定された変数を値 v で評価する。
     *
     * @param varIdx 変数のインデックス。
     * @param v 評価値。
     * @return 評価後の多項式。
     * 計算量: O(T * log T)。
     */
    public MultivariatePolynomial<C> evaluate(int varIdx, C v) {
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            Monomial m = entry.getKey();
            int exp = m.getExponent(varIdx);
            C factor = powCoefficient(v, exp);
            int[] newExps = new int[m.size()];
            for (int i = 0; i < m.size(); i++) {
                newExps[i] = (i == varIdx) ? 0 : m.getExponent(i);
            }
            res.merge(new Monomial(newExps), coefficientRing.mul(entry.getValue(), factor), coefficientRing::add);
        }
        return fromMap(res);
    }

    /**
     * スカラー乗算。
     * 計算量: O(T)。
     */
    public MultivariatePolynomial<C> multiply(C scalar) {
        Objects.requireNonNull(scalar);
        if (coefficientRing.equals(scalar, coefficientRing.zero())) return zero();
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : this.terms.entrySet()) {
            res.put(entry.getKey(), coefficientRing.mul(entry.getValue(), scalar));
        }
        return fromMap(res);
    }

    /**
     * 項による乗算。
     * 計算量: O(T log T)。
     */
    public MultivariatePolynomial<C> multiply(Monomial m, C scalar) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(scalar);
        if (coefficientRing.equals(scalar, coefficientRing.zero())) return zero();
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : this.terms.entrySet()) {
            res.put(entry.getKey().mul(m), coefficientRing.mul(entry.getValue(), scalar));
        }
        return fromMap(res);
    }

    /**
     * varIdx に関する偏微分。
     * 数学的表記: ∂f / ∂x_i。
     * 計算量: O(T log T)。
     */
    public MultivariatePolynomial<C> differentiate(int varIdx) {
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : this.terms.entrySet()) {
            Monomial m = entry.getKey();
            int exp = m.getExponent(varIdx);
            if (exp > 0) {
                int[] newExponents = new int[Math.max(m.size(), varIdx + 1)];
                for (int i = 0; i < newExponents.length; i++) {
                    newExponents[i] = m.getExponent(i);
                }
                newExponents[varIdx]--;
                Monomial newM = new Monomial(newExponents);
                C newC = multiplyByNatural(entry.getValue(), exp);
                res.merge(newM, newC, coefficientRing::add);
            }
        }
        return fromMap(res);
    }

    /**
     * 多変数除算アルゴリズム。
     * f を多項式のリスト F で除算する。
     * 事前条件: F が空ではないこと、係数環が体であること。
     * 事後条件: f = sum q_i * F_i + r を満たす商 q_i と剰余 r を返す。
     * 計算量: O(num_steps * |F| * complexity_multiplication)。
     */
    public static <C> DivisionResult<C> divide(MultivariatePolynomial<C> f, List<MultivariatePolynomial<C>> F) {
        Objects.requireNonNull(f);
        MultivariatePolynomialRing<C> ring = new MultivariatePolynomialRing<>(f.getCoefficientRing());
        return ring.divide(f, F);
    }

    /**
     * 単項多項式 c * m を作成する。
     * 未テスト。
     * 数学的表記: c*m in R[x_0,x_1,...]。
     * 事前条件: coefficientRing != null, m != null, c != null。
     * 事後条件: c = 0_R なら零多項式、そうでなければ supp(戻り値) = {m}。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: coefficientRing, m, c を共有し、項写像は新規所有する。
     * 例外・未定義条件: null 引数のとき NullPointerException。
     * @param coefficientRing 係数環 R。
     * @param m 単項式 m。
     * @param c 係数 c。
     * @return 単項多項式。
     */
    public static <C> MultivariatePolynomial<C> singleTerm(CommutativeRingStrategy<C> coefficientRing, Monomial m, C c) {
        TreeMap<Monomial, C> terms = new TreeMap<>();
        terms.put(Objects.requireNonNull(m), Objects.requireNonNull(c));
        return new MultivariatePolynomial<>(coefficientRing, terms);
    }

    /**
     * f と g の S-多項式を計算する。
     * 数学的表記: S(f, g) = (L/LT(f))f - (L/LT(g))g (L = lcm(LM(f), LM(g)))。
     */
    public static <C> MultivariatePolynomial<C> sPolynomial(MultivariatePolynomial<C> f, MultivariatePolynomial<C> g) {
        Objects.requireNonNull(f);
        MultivariatePolynomialRing<C> ring = new MultivariatePolynomialRing<>(f.getCoefficientRing());
        return ring.sPolynomial(f, g);
    }

    /**
     * 簡約グレブナー基底を計算するための Buchberger のアルゴリズム。
     * 素な単項式の判定基準を用いた最適化を含む。
     * 計算量: 最悪の場合は指数時間だが、微分形式に対して最適化されている。
     */
    public static <C> List<MultivariatePolynomial<C>> grobnerBasis(List<MultivariatePolynomial<C>> F) {
        if (F.isEmpty()) return new ArrayList<>();
        MultivariatePolynomial<C> first = F.get(0);
        Objects.requireNonNull(first);
        MultivariatePolynomialRing<C> ring = new MultivariatePolynomialRing<>(first.getCoefficientRing());
        return ring.grobnerBasis(F);
    }

    /**
     * グレブナー基底 G を簡約グレブナー基底に変換する。
     * 事後条件: すべての多項式は主係数が 1 (monic) であり、どの主単項式も他を割り切らない。
     */
    public static <C> List<MultivariatePolynomial<C>> reduceGrobnerBasis(List<MultivariatePolynomial<C>> G) {
        if (G.isEmpty()) return new ArrayList<>();
        MultivariatePolynomial<C> first = G.get(0);
        Objects.requireNonNull(first);
        if (!(first.getCoefficientRing() instanceof FieldStrategy)) {
            throw new UnsupportedOperationException("operation requires FieldStrategy coefficients");
        }
        @SuppressWarnings("unchecked")
        FieldStrategy<C> field = (FieldStrategy<C>) first.getCoefficientRing();
        List<MultivariatePolynomial<C>> G1 = new ArrayList<>();
        for (MultivariatePolynomial<C> g : G) {
            if (g.isZero()) continue;
            C lcInv = field.inv(g.leadingCoefficient());
            G1.add(g.multiply(lcInv));
        }
        List<MultivariatePolynomial<C>> G2 = new ArrayList<>();
        for (int i = 0; i < G1.size(); i++) {
            boolean redundant = false;
            Monomial LTi = G1.get(i).getLeadingMonomial();
            if (LTi == null) continue;
            for (int j = 0; j < G1.size(); j++) {
                if (i == j) continue;
                Monomial LTj = G1.get(j).getLeadingMonomial();
                if (LTj == null) continue;
                if (LTi.isDivisibleBy(LTj)) {
                    redundant = true;
                    break;
                }
            }
            if (!redundant) G2.add(G1.get(i));
        }
        List<MultivariatePolynomial<C>> res = new ArrayList<>();
        for (int i = 0; i < G2.size(); i++) {
            MultivariatePolynomial<C> g = G2.get(i);
            List<MultivariatePolynomial<C>> others = new ArrayList<>(G2);
            others.remove(i);
            res.add(divide(g, others).remainder);
        }
        return res;
    }

    /** 多変数除算結果。 */
    public static class DivisionResult<C> {
        /** 商の列。 */
        public final List<MultivariatePolynomial<C>> quotients;
        /** 剰余。 */
        public final MultivariatePolynomial<C> remainder;

        /**
         * 除算結果を生成する。
         * 未テスト。
         * 数学的表記: (q_0,...,q_{k-1},r)。
         * 事前条件: quotients != null, remainder != null。
         * 事後条件: this.quotients == quotients かつ this.remainder == remainder。
         * 副作用: なし。
         * 計算量: O(1)。
         * 破壊的変更: なし。
         * 参照共有・所有権: 引数参照を共有する。
         * 例外・未定義条件: null 引数のとき NullPointerException。
         * @param quotients 商列。
         * @param remainder 剰余。
         */
        public DivisionResult(List<MultivariatePolynomial<C>> quotients, MultivariatePolynomial<C> remainder) {
            this.quotients = Objects.requireNonNull(quotients);
            this.remainder = Objects.requireNonNull(remainder);
        }
    }

    /**
     * この多項式と別のオブジェクトが等しいかどうかを判定する。
     * 未テスト。
     * 数学的表記: f = g iff coefficientRing_f == coefficientRing_g and terms_f = terms_g over coefficientRing。
     * 事前条件: なし。
     * 事後条件: 等しければ true を返す。
     * 計算量: O(T log T) (T は項の数)。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultivariatePolynomial<?> thatRaw)) return false;
        if (!Objects.equals(coefficientRing, thatRaw.coefficientRing)) return false;
        if (!terms.keySet().equals(thatRaw.terms.keySet())) return false;
        @SuppressWarnings("unchecked")
        MultivariatePolynomial<C> that = (MultivariatePolynomial<C>) thatRaw;
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            if (!coefficientRing.equals(entry.getValue(), that.terms.get(entry.getKey()))) return false;
        }
        return true;
    }

    /**
     * この多項式のハッシュ値を返す。
     * 未テスト。
     * 数学的表記: hash({(m,c_m)}_m, mod)。
     * 事前条件: なし。
     * 事後条件: equals(a,b) なら a.hashCode() == b.hashCode()。
     * 副作用: なし。
     * 計算量: O(T)。
     * 破壊的変更: なし。
     * 参照共有・所有権: なし。
     * 例外・未定義条件: なし。
     * @return ハッシュ値。
     */
    @Override
    public int hashCode() {
        int result = Objects.hashCode(coefficientRing);
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            result = 31 * result + entry.getKey().hashCode();
            result = 31 * result + coefficientHash(entry.getValue());
        }
        return result;
    }

    /**
     * 係数の等号と整合するハッシュ値を返す。
     * 未テスト。
     * 数学的表記: hash(c mod p) if R = Z/pZ, otherwise hash(c)。
     * 事前条件: coefficient != null。
     * 事後条件: coefficientRing.equals(a,b) なら戻り値が一致することを意図する。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: なし。
     * 例外・未定義条件: coefficient == null のとき NullPointerException。
     * @param coefficient 係数。
     * @return ハッシュ値。
     */
    private int coefficientHash(C coefficient) {
        return coefficientRing.hashCode(coefficient);
    }

    /**
     * すべての項の変数のインデックスを指定された量だけシフトした新しい多項式を返す。
     * @param delta シフト量。
     * @return シフトされた多項式。
     */
    public MultivariatePolynomial<C> shiftVariables(int delta) {
        if (delta == 0) return this;
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            res.put(entry.getKey().shiftVariables(delta), entry.getValue());
        }
        return fromMap(res);
    }

    /**
     * 指定された変数の集合に関して多項式が斉次であるかどうかを判定する。
     * @param varIndices 変数のインデックス。
     * @return 斉次であれば true。
     */
    public boolean isHomogeneous(int... varIndices) {
        if (terms.isEmpty()) return true;
        int targetDeg = -1;
        for (Monomial m : terms.keySet()) {
            int d = 0;
            for (int idx : varIndices) d += m.getExponent(idx);
            if (targetDeg == -1) targetDeg = d;
            else if (targetDeg != d) return false;
        }
        return true;
    }

    /**
     * 指定された変数の指数が m で割って r 余る項のみを抽出した新しい多項式を返す。
     * 抽出後、指定された変数の指数 e を e / m に置き換える（r=0 の場合を想定）。
     * 未テスト。
     * @param varIdx 変数のインデックス。
     * @param m 法。
     * @param r 余り。
     * @return 抽出・変換後の多項式。
     * <p>計算量: O(T log T)。</p>
     */
    public MultivariatePolynomial<C> section(int varIdx, int m, int r) {
        TreeMap<Monomial, C> res = new TreeMap<>();
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            Monomial mon = entry.getKey();
            int exp = mon.getExponent(varIdx);
            if (exp % m == r) {
                int[] newExps = new int[mon.size()];
                for (int i = 0; i < mon.size(); i++) newExps[i] = (i == varIdx) ? (exp / m) : mon.getExponent(i);
                res.put(new Monomial(newExps), entry.getValue());
            }
        }
        return fromMap(res);
    }

    /**
     * 指定された変数の全次数を計算する。
     *
     * @param varIndices 変数のインデックス。
     * @return 指定された変数の全次数。
     * 計算量: O(T * L) (T は項の数、L は varIndices の長さ)。
     */
    public int getDegree(int... varIndices) {
        int maxDeg = -1;
        for (Monomial m : terms.keySet()) {
            int d = 0;
            for (int idx : varIndices) d += m.getExponent(idx);
            maxDeg = Math.max(maxDeg, d);
        }
        return maxDeg;
    }

    /**
     * 多項式を斉次化する。
     * x ∈ varIndices
     * @param hVarIdx 斉次化変数のインデックス。
     * @param varIndices 斉次化の対象とする変数のインデックス。
     * @return 斉次化された多項式。
     * 計算量: O(T * L) (T は項の数、L は変数の数)。
     */
    public MultivariatePolynomial<C> homogenize(int hVarIdx, int... varIndices) {
        TreeMap<Monomial, C> res = new TreeMap<>();
        int maxdeg = 0;
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            int sum = 0;
            for (int i : varIndices) sum += entry.getKey().getExponent(i);
            maxdeg = Math.max(maxdeg, sum);
        }
        for (Map.Entry<Monomial, C> entry : terms.entrySet()) {
            var x = entry.getKey();
            int[] exp = new int[Math.max(x.exponents().length, hVarIdx + 1)];
            System.arraycopy(x.exponents(), 0, exp, 0, x.exponents().length);
            int deg = 0;
            for (int i : varIndices) deg += entry.getKey().getExponent(i);
            exp[hVarIdx] = maxdeg - deg;
            res.put(new Monomial(exp), entry.getValue());
        }
        return fromMap(res);
    }

    /**
     * 係数環が体であることを検査し FieldStrategy を返す。
     * 未テスト。
     * 数学的表記: R is a field。
     * 事前条件: polynomial != null。
     * 事後条件: coefficientRing instanceof FieldStrategy ならそれを返す。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: FieldStrategy 参照を共有する。
     * 例外・未定義条件: 係数環が体でないと UnsupportedOperationException。
     * @param polynomial 多項式。
     * @return 係数体。
     */
    @SuppressWarnings("unchecked")
    private static <C> FieldStrategy<C> requireField(MultivariatePolynomial<C> polynomial) {
        if (!(polynomial.coefficientRing instanceof FieldStrategy<?>)) {
            throw new UnsupportedOperationException("operation requires FieldStrategy coefficients");
        }
        return (FieldStrategy<C>) polynomial.coefficientRing;
    }

    /**
     * n*a を環の加法で計算する。
     * 未テスト。
     * 数学的表記: n a = Σ_{k=1}^{n} a。
     * 事前条件: a != null, n >= 0。
     * 事後条件: 戻り値 = n*a。
     * 副作用: なし。
     * 計算量: O(log n) 回の係数加算。
     * 破壊的変更: なし。
     * 参照共有・所有権: 係数参照を共有することがある。
     * 例外・未定義条件: a == null のとき NullPointerException。
     * @param a 加算される係数。
     * @param n 非負整数。
     * @return n*a。
     */
    private C multiplyByNatural(C a, int n) {
        Objects.requireNonNull(a);
        C res = coefficientRing.zero();
        C base = a;
        while (n > 0) {
            if ((n & 1) == 1) res = coefficientRing.add(res, base);
            base = coefficientRing.add(base, base);
            n >>= 1;
        }
        return res;
    }

    /**
     * 係数の非負整数冪を返す。
     * 未テスト。
     * 数学的表記: a^n。
     * 事前条件: a != null, n >= 0。
     * 事後条件: n = 0 なら 1_R、n > 0 なら n 個の積。
     * 副作用: なし。
     * 計算量: O(log n) 回の係数乗算。
     * 破壊的変更: なし。
     * 参照共有・所有権: 係数参照を共有することがある。
     * 例外・未定義条件: a == null のとき NullPointerException。
     * @param a 底。
     * @param n 指数。
     * @return a^n。
     */
    private C powCoefficient(C a, int n) {
        Objects.requireNonNull(a);
        C res = coefficientRing.one();
        C base = a;
        while (n > 0) {
            if ((n & 1) == 1) res = coefficientRing.mul(res, base);
            base = coefficientRing.mul(base, base);
            n >>= 1;
        }
        return res;
    }

    public int totalDegree() {
    	int ret = -1;
    	for (var key : terms.keySet()) {
    		ret = Math.max(ret, key.getDegree());
    	}
    	return ret;
    }

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringJoiner sj = new StringJoiner(" + ");
        for (Map.Entry<Monomial, C> entry : terms.descendingMap().entrySet()) {
            sj.add(entry.getValue() + "*" + entry.getKey().toString());
        }
        return sj.toString();
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MultivariatePolynomial<C> self() {
		return this;
	}

}
