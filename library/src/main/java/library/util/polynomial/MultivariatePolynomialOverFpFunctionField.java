package library.util.polynomial;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.MathUtils;
import java.util.*;
import cc.redberry.rings.IntegersZp64;
import cc.redberry.rings.poly.multivar.MultivariatePolynomialZp64;
import cc.redberry.rings.poly.multivar.MonomialZp64;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.GroebnerBases;
import cc.redberry.rings.poly.multivar.MultivariateDivision;

/**
 * 多変数多項式環 {@code Frac(F_mod[t])[x_0,x_1,...]} の元。
 * 係数は {@code FractionFieldElement<long[]>} で、{@code long[]} は {@code F_mod[t]} を表す。
 */
public class MultivariatePolynomialOverFpFunctionField extends MultivariatePolynomial<FractionFieldElement<long[]>> {
    private final long mod;
    private final FractionFieldStrategy<long[]> field;
    private final PolynomialFpDynamic poly1d;
    private Boolean isScalarCached = null;

    /**
     * ゼロ多項式を生成する。
     * 未テスト。
     * 数学的表記: {@code 0 in Frac(F_mod[t])[x_0,x_1,...]}。
     * 事前条件: mod > 0。
     * 事後条件: {@code isZero() == true}。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 係数体と項写像を新規所有する。
     * 例外・未定義条件: mod が係数演算の契約を満たさない場合は未定義。
     * @param mod 有限体の標数。
     */
    public MultivariatePolynomialOverFpFunctionField(long mod) {
        this(mod, new TreeMap<>());
    }

    /**
     * 項写像から多項式を生成する。
     * 未テスト。
     * 数学的表記: {@code Σ_{m in supp(terms)} terms[m] m in Frac(F_mod[t])[x_0,x_1,...]}。
     * 事前条件: mod > 0, terms != null, すべての key/value != null。
     * 事後条件: 零係数項を除いた多項式を表し、{@code this instanceof MultivariatePolynomial}。
     * 副作用: なし。
     * 計算量: O(T log T)。
     * 破壊的変更: terms を変更しない。
     * 参照共有・所有権: Monomial と係数の参照を共有し、内部項写像は新規所有する。
     * 例外・未定義条件: null 引数または null 要素があるとき NullPointerException。
     * @param mod 有限体の標数。
     * @param terms 初期項写像。
     */
    public MultivariatePolynomialOverFpFunctionField(long mod, TreeMap<Monomial, FractionFieldElement<long[]>> terms) {
        super(new FractionFieldStrategy<>(PolynomialFpDynamic.of(mod)), terms);
        this.mod = mod;
        this.poly1d = PolynomialFpDynamic.of(mod);
        this.field = new FractionFieldStrategy<>(poly1d);
        cleanup();
    }

    /**
     * 零係数項を削除する。
     * 未テスト。
     * 数学的表記: {@code f := Σ_{c_m != 0} c_m m}。
     * 事前条件: terms != null。
     * 事後条件: terms は field.zero() と等しい値を含まない。
     * 副作用: this.terms と親クラスの項写像を破壊的に更新する。
     * 計算量: O(T)。
     * 破壊的変更: this を変更する。
     * 参照共有・所有権: key/value 参照は維持する。
     * 例外・未定義条件: terms に null 値が含まれる場合は未定義。
     */
    private void cleanup() {
        getTerms().entrySet().removeIf(e -> field.equals(e.getValue(), field.zero()));
    }

    public long getMod() { return mod; }

    /**
     * 分数係数の項写像を返す。
     * 未テスト。
     * 数学的表記: {@code supp(f) -> Frac(F_mod[t])}, {@code m -> [m]f}。
     * 事前条件: なし。
     * 事後条件: 戻り値 == 内部項写像。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 内部 {@code TreeMap} と key/value 参照を共有し、呼び出し側が変更できる。
     * 例外・未定義条件: 呼び出し側が戻り値を変更した場合、零係数除去は自動では保証されない。
     * @return 分数係数の項写像。
     */
    public TreeMap<Monomial, FractionFieldElement<long[]>> getFractionTerms() { return getTerms(); }

    public static MultivariatePolynomialOverFpFunctionField singleTerm(long mod, Monomial m, FractionFieldElement<long[]> coeff) {
        TreeMap<Monomial, FractionFieldElement<long[]>> terms = new TreeMap<>();
        terms.put(m, coeff);
        return new MultivariatePolynomialOverFpFunctionField(mod, terms);
    }

    public boolean isZero() { return getTerms().isEmpty(); }

    /**
     * 多項式が単位元 1 であるかどうかを判定する。
     * Pre-condition: なし。
     * Post-condition: 単位元 1 であれば true を返す。
     * Side-effect: なし。
     * Calculation complexity: O(1) (cleanup 済みであるため)。
     * Destructive change: なし。
     * Reference sharing: なし。
     * Exception: なし。
     */
    public boolean isOne() {
        if (getTerms().size() != 1) return false;
        Map.Entry<Monomial, FractionFieldElement<long[]>> entry = getTerms().firstEntry();
        return entry.getKey().getDegree() == 0 && field.equals(entry.getValue(), field.one());
    }


    @Override
    protected MultivariatePolynomialOverFpFunctionField fromMap(Map<Monomial, FractionFieldElement<long[]>> terms) {
        return new MultivariatePolynomialOverFpFunctionField(mod, new TreeMap<>(terms));
    }

    @Override
    public MultivariatePolynomialOverFpFunctionField add(MultivariatePolynomial<FractionFieldElement<long[]>> other) {
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>(getTerms());
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : other.getTerms().entrySet()) {
            res.merge(entry.getKey(), entry.getValue(), field::add);
        }
        return fromMap(res);
    }

    @Override
    public MultivariatePolynomialOverFpFunctionField sub(MultivariatePolynomial<FractionFieldElement<long[]>> other) {
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>(getTerms());
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : other.getTerms().entrySet()) {
            res.merge(entry.getKey(), field.neg(entry.getValue()), field::add);
        }
        return fromMap(res);
    }

    public MultivariatePolynomialOverFpFunctionField subtract(MultivariatePolynomialOverFpFunctionField other) {
        return sub(other);
    }

    @Override
    public MultivariatePolynomialOverFpFunctionField zero() {
        return new MultivariatePolynomialOverFpFunctionField(mod);
    }

    @Override
    public MultivariatePolynomialOverFpFunctionField one() {
        return singleTerm(mod, new Monomial(new int[0]), field.one());
    }

    @Override
    public MultivariatePolynomialOverFpFunctionField mul(MultivariatePolynomial<FractionFieldElement<long[]>> other) {
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> e1 : getTerms().entrySet()) {
            for (Map.Entry<Monomial, FractionFieldElement<long[]>> e2 : other.getTerms().entrySet()) {
                Monomial m = e1.getKey().mul(e2.getKey());
                FractionFieldElement<long[]> c = field.mul(e1.getValue(), e2.getValue());
                res.merge(m, c, field::add);
            }
        }
        return fromMap(res);
    }

    public MultivariatePolynomialOverFpFunctionField multiply(MultivariatePolynomialOverFpFunctionField other) {
        return mul(other);
    }

    public MultivariatePolynomialOverFpFunctionField multiply(FractionFieldElement<long[]> scalar) {
        if (field.equals(scalar, field.zero())) return zero();
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().entrySet()) {
            res.put(entry.getKey(), field.mul(entry.getValue(), scalar));
        }
        return fromMap(res);
    }

    public MultivariatePolynomialOverFpFunctionField multiply(FractionFieldElement<long[]> scalar, Monomial m) {
        if (field.equals(scalar, field.zero())) return zero();
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().entrySet()) {
            res.put(entry.getKey().mul(m), field.mul(entry.getValue(), scalar));
        }
        return fromMap(res);
    }

    public MultivariatePolynomialOverFpFunctionField differentiate(int varIdx) {
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().entrySet()) {
            Monomial m = entry.getKey();
            int exp = m.getExponent(varIdx);
            if (exp > 0) {
                int[] nextExps = m.exponents().clone();
                nextExps[varIdx]--;
                Monomial nextM = new Monomial(nextExps);
                FractionFieldElement<long[]> nextC = field.mul(entry.getValue(), field.from(new long[]{exp % mod}));
                res.merge(nextM, nextC, field::add);
            }
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, res);
    }

    public MultivariatePolynomialOverFpFunctionField differentiateT() {
        TreeMap<Monomial, FractionFieldElement<long[]>> res = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().entrySet()) {
            FractionFieldElement<long[]> c = entry.getValue();
            long[] num = c.num();
            long[] den = c.den();
            long[] dNum = poly1d.diff(num, 1);
            long[] dDen = poly1d.diff(den, 1);

            // (num' * den - num * den') / den^2
            long[] term1 = poly1d.mul(dNum, den);
            long[] term2 = poly1d.mul(num, dDen);
            long[] newNum = poly1d.sub(term1, term2);
            long[] newDen = poly1d.mul(den, den);
            res.put(entry.getKey(), field.of(newNum, newDen));
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, res);
    }

    public Monomial leadingMonomial() {
        return getTerms().isEmpty() ? null : getTerms().lastKey();
    }

    public FractionFieldElement<long[]> leadingCoefficient() {
        return getTerms().isEmpty() ? null : getTerms().lastEntry().getValue();
    }

    public static class DivRem {
        public final List<MultivariatePolynomialOverFpFunctionField> quotients;
        public final MultivariatePolynomialOverFpFunctionField remainder;
        public DivRem(List<MultivariatePolynomialOverFpFunctionField> quotients, MultivariatePolynomialOverFpFunctionField remainder) {
            this.quotients = quotients;
            this.remainder = remainder;
        }
    }

    /**
     * 多項式とその生成元に関する係数ベクトルを保持するクラス。
     * 未テスト。
     */
    public static class TaggedPolynomial {
        /** 多項式本体。 */
        public final MultivariatePolynomialOverFpFunctionField poly;
        /** 生成元に対する係数ベクトル。 f = sum coeffs[i] * r_i。 */
        public final MultivariatePolynomialOverFpFunctionField[] coeffs;

        /**
         * TaggedPolynomial のコンストラクタ。
         * 事前条件: poly != null, coeffs != null。
         * 事後条件: 新しい TaggedPolynomial インスタンスを作成する。
         * 計算量: O(1)。
         */
        public TaggedPolynomial(MultivariatePolynomialOverFpFunctionField poly, MultivariatePolynomialOverFpFunctionField[] coeffs) {
            this.poly = poly;
            this.coeffs = coeffs;
        }

        /**
         * 2つの TaggedPolynomial の和を計算する。
         * 事前条件: coeffs の長さが一致していること。
         * 事後条件: 和を返す。
         * 計算量: O(T1 + T2 + L * T_coeffs) (T は項数、L は係数ベクトルの長さ)。
         */
        public TaggedPolynomial add(TaggedPolynomial other) {
            int n = Math.max(coeffs.length, other.coeffs.length);
            MultivariatePolynomialOverFpFunctionField[] nextCoeffs = new MultivariatePolynomialOverFpFunctionField[n];
            for (int i = 0; i < n; i++) {
                MultivariatePolynomialOverFpFunctionField c1 = (i < coeffs.length && coeffs[i] != null) ? coeffs[i] : poly.zero();
                MultivariatePolynomialOverFpFunctionField c2 = (i < other.coeffs.length && other.coeffs[i] != null) ? other.coeffs[i] : poly.zero();
                nextCoeffs[i] = c1.add(c2);
            }
            return new TaggedPolynomial(poly.add(other.poly), nextCoeffs);
        }

        /**
         * 2つの TaggedPolynomial の差を計算する。
         * 事前条件: coeffs の長さが一致していること。
         * 事後条件: 差を返す。
         * 計算量: O(T1 + T2 + L * T_coeffs)。
         */
        public TaggedPolynomial subtract(TaggedPolynomial other) {
            int n = Math.max(coeffs.length, other.coeffs.length);
            MultivariatePolynomialOverFpFunctionField[] nextCoeffs = new MultivariatePolynomialOverFpFunctionField[n];
            for (int i = 0; i < n; i++) {
                MultivariatePolynomialOverFpFunctionField c1 = (i < coeffs.length && coeffs[i] != null) ? coeffs[i] : poly.zero();
                MultivariatePolynomialOverFpFunctionField c2 = (i < other.coeffs.length && other.coeffs[i] != null) ? other.coeffs[i] : poly.zero();
                nextCoeffs[i] = c1.subtract(c2);
            }
            return new TaggedPolynomial(poly.subtract(other.poly), nextCoeffs);
        }

        /**
         * TaggedPolynomial をスカラー倍する。
         * 事後条件: スカラー倍された TaggedPolynomial を返す。
         * 計算量: O(T + L * T_coeffs)。
         */
        public TaggedPolynomial multiply(FractionFieldElement<long[]> scalar) {
            MultivariatePolynomialOverFpFunctionField[] nextCoeffs = new MultivariatePolynomialOverFpFunctionField[coeffs.length];
            for (int i = 0; i < coeffs.length; i++) {
                nextCoeffs[i] = coeffs[i].multiply(scalar);
            }
            return new TaggedPolynomial(poly.multiply(scalar), nextCoeffs);
        }

        /**
         * TaggedPolynomial に単項式とスカラーを乗算する。
         * 事後条件: 乗算された TaggedPolynomial を返す。
         * 計算量: O(T + L * T_coeffs)。
         */
        public TaggedPolynomial multiply(FractionFieldElement<long[]> scalar, Monomial m) {
            MultivariatePolynomialOverFpFunctionField[] nextCoeffs = new MultivariatePolynomialOverFpFunctionField[coeffs.length];
            for (int i = 0; i < coeffs.length; i++) {
                nextCoeffs[i] = (coeffs[i] == null) ? new MultivariatePolynomialOverFpFunctionField(poly.mod) : coeffs[i].multiply(scalar, m);
            }
            return new TaggedPolynomial(poly.multiply(scalar, m), nextCoeffs);
        }

        /**
         * TaggedPolynomial がゼロベクトルであるか判定する。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: ゼロベクトルであれば true を返す。
         * 副作用: なし。
         * 計算量: O(L * T_coeffs) (L は成分数、T_coeffs は各成分の項数)。
         * 破壊的変更: なし。
         * 参照共有: なし。
         * 例外: なし。
         * @return ゼロベクトルの場合は true。
         */
        public boolean isZero() {
            if (!poly.isZero()) return false;
            for (MultivariatePolynomialOverFpFunctionField c : coeffs) {
                if (c != null && !c.isZero()) return false;
            }
            return true;
        }

        /**
         * TaggedPolynomial の先行モジュール単項式（成分インデックスと単項式のペア）を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: [成分インデックス (Integer), 単項式 (Monomial)] の配列を返す。成分 0 は poly、1..n は coeffs[0..n-1]。
         * 副作用: なし。
         * 計算量: O(L * T_coeffs)。
         * 破壊的変更: なし。
         * 参照共有: なし。
         * 例外: なし。
         * @return 先行モジュール単項式。
         */
        public Object[] leadingModuleMonomial() {
            if (!poly.isZero()) return new Object[]{0, poly.leadingMonomial()};
            for (int i = 0; i < coeffs.length; i++) {
                if (coeffs[i] != null && !coeffs[i].isZero()) {
                    return new Object[]{i + 1, coeffs[i].leadingMonomial()};
                }
            }
            return null;
        }

        /**
         * TaggedPolynomial の先行係数を返す。
         * 未テスト。
         * 事前条件: なし。
         * 事後条件: 先行係数を返す。
         * 副作用: なし。
         * 計算量: O(L * T_coeffs)。
         * 破壊的変更: なし。
         * 参照共有: なし。
         * 例外: なし。
         * @return 先行係数。
         */
        public FractionFieldElement<long[]> leadingModuleCoefficient() {
            if (!poly.isZero()) return poly.leadingCoefficient();
            for (MultivariatePolynomialOverFpFunctionField c : coeffs) {
                if (c != null && !c.isZero()) return c.leadingCoefficient();
            }
            return null;
        }
    }

    /**
     * TaggedPolynomial の除算の結果を保持するクラス。
     * 未テスト。
     */
    public static class TaggedDivRem {
        /** 商のリスト。 */
        public final List<MultivariatePolynomialOverFpFunctionField> quotients;
        /** 剰余（TaggedPolynomial）。 */
        public final TaggedPolynomial remainder;
        public TaggedDivRem(List<MultivariatePolynomialOverFpFunctionField> quotients, TaggedPolynomial remainder) {
            this.quotients = quotients;
            this.remainder = remainder;
        }
    }


    /**
     * 有限個の多項式を係数体 K = F_mod(t) 上の行ベクトルとしてガウス消去し、行階段形基底を返す。
     * 未テスト。
     * 事前条件: fs != null, すべての f in fs について f != null, すべての非ゼロ f は同じ mod を持つ。
     * 事後条件: 戻り値 E は以下を満たす。
     *  (1) span_K(E) = span_K(fs)。
     *  (2) すべての e in E は leadingCoefficient(e) = 1。
     *  (3) i != j なら leadingMonomial(E[i]) != leadingMonomial(E[j])。
     *  (4) E は leadingMonomial の降順に整列される。
     * 副作用: なし。
     * 計算量: O(m * b * T_sub)。m = fs.size(), b = 戻り値の行数, T_sub = 1 回の多項式減算のコスト。
     * 破壊的変更: なし。
     * 参照共有・所有権: 戻り値のリストは新規作成される。要素は入力多項式を破壊せずに作った多項式であり、入力リストを所有しない。
     * 例外・未定義条件: 事前条件違反時の動作は未定義。
     * @param fs 行ベクトル化する多項式列。
     * @return K-線形包の行階段形基底。
     */
    public static List<MultivariatePolynomialOverFpFunctionField> rowEchelonBasis(List<MultivariatePolynomialOverFpFunctionField> fs) {
        List<MultivariatePolynomialOverFpFunctionField> basis = new ArrayList<>();
        for (MultivariatePolynomialOverFpFunctionField f : fs) {
            MultivariatePolynomialOverFpFunctionField r = reduceByEchelon(f, basis);
            if (r.isZero()) continue;
            FractionFieldElement<long[]> lcInv = r.field.inv(r.leadingCoefficient());
            MultivariatePolynomialOverFpFunctionField normalized = r.multiply(lcInv);
            int insertAt = 0;
            while (insertAt < basis.size() && basis.get(insertAt).leadingMonomial().compareTo(normalized.leadingMonomial()) > 0) {
                insertAt++;
            }
            basis.add(insertAt, normalized);
        }
        return basis;
    }

    /**
     * 行階段形基底によって多項式をガウス消去し、ピボット列成分を消した剰余を返す。
     * 未テスト。
     * 事前条件: f != null, echelonBasis != null, 各 e in echelonBasis は e != null かつ leadingCoefficient(e) = 1、
     *  i != j なら leadingMonomial(e_i) != leadingMonomial(e_j)。
     * 事後条件: 戻り値 r は、ある c_i in K について f = r + sum_i c_i e_i を満たし、
     *  すべての e_i について r の leadingMonomial(e_i) 係数は 0。
     * 副作用: なし。
     * 計算量: O(b * T_sub)。b = echelonBasis.size(), T_sub = 1 回の多項式減算のコスト。
     * 破壊的変更: なし。
     * 参照共有・所有権: 戻り値は新規多項式または入力 f と同値な不変扱いの参照。入力基底の所有権は取得しない。
     * 例外・未定義条件: 事前条件違反時の動作は未定義。
     * @param f 被簡約多項式。
     * @param echelonBasis 行階段形基底。
     * @return ピボット列を消去した剰余。
     */
    public static MultivariatePolynomialOverFpFunctionField reduceByEchelon(MultivariatePolynomialOverFpFunctionField f, List<MultivariatePolynomialOverFpFunctionField> echelonBasis) {
        MultivariatePolynomialOverFpFunctionField r = f;
        for (MultivariatePolynomialOverFpFunctionField row : echelonBasis) {
            Monomial pivot = row.leadingMonomial();
            if (pivot == null) continue;
            FractionFieldElement<long[]> coeff = r.getTerms().get(pivot);
            if (coeff == null || r.field.equals(coeff, r.field.zero())) continue;
            r = r.subtract(row.multiply(coeff));
        }
        return r;
    }

    public static DivRem divide(MultivariatePolynomialOverFpFunctionField f, List<MultivariatePolynomialOverFpFunctionField> gs) {
        if (isScalar(f) && isAllScalar(gs)) {
            return divideScalar(f, gs);
        }
        int m = gs.size();
        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(new MultivariatePolynomialOverFpFunctionField(f.mod));
        MultivariatePolynomialOverFpFunctionField r = new MultivariatePolynomialOverFpFunctionField(f.mod);
        MultivariatePolynomialOverFpFunctionField p = f;
        FractionFieldStrategy<long[]> field = f.field;

        while (!p.isZero()) {
            Monomial ltP = p.leadingMonomial();
            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Monomial ltG = gs.get(i).leadingMonomial();
                if (ltG != null && ltP.isDivisibleBy(ltG)) {
                    Monomial factorM = ltP.divide(ltG);
                    FractionFieldElement<long[]> factorC = field.div(p.leadingCoefficient(), gs.get(i).leadingCoefficient());
                    MultivariatePolynomialOverFpFunctionField step = singleTerm(f.mod, factorM, factorC);
                    qs.set(i, qs.get(i).add(step));
                    p = p.subtract(gs.get(i).multiply(factorC, factorM));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                r = r.add(singleTerm(f.mod, ltP, p.leadingCoefficient()));
                p = p.subtract(singleTerm(f.mod, ltP, p.leadingCoefficient()));
            }
        }
        return new DivRem(qs, r);
    }

    /**
     * 多項式のリストから Grobner 基底を計算する。
     * 未テスト。
     * @param fs 多項式のリスト。
     * @return Grobner 基底。
     * <p>計算量: O(m^2 * Iter * T)。</p>
     */
    public static List<MultivariatePolynomialOverFpFunctionField> grobnerBasisFractional(List<MultivariatePolynomialOverFpFunctionField> fs) {
        if (fs.isEmpty()) return new ArrayList<>();
        long mod = fs.get(0).mod;
        if (isAllScalar(fs)) {
            List<MultivariatePolynomial<Long>> mfs = new ArrayList<>();
            for (MultivariatePolynomialOverFpFunctionField f : fs) mfs.add(f.toMultivariate());
            List<MultivariatePolynomial<Long>> mgb = MultivariatePolynomialOverFp.grobnerBasisFp(mfs);
            List<MultivariatePolynomialOverFpFunctionField> res = new ArrayList<>();
            for (MultivariatePolynomial<Long> p : mgb) res.add(fromMultivariate(p));
            return res;
        }
        FractionFieldStrategy<long[]> field = fs.get(0).field;
        List<MultivariatePolynomialOverFpFunctionField> g = new ArrayList<>();
        List<int[]> pairs = new ArrayList<>();

        for (MultivariatePolynomialOverFpFunctionField f : fs) {
            MultivariatePolynomialOverFpFunctionField r = divide(f, g).remainder;
            if (!r.isZero()) {
                int newIdx = g.size();
                for (int i = 0; i < g.size(); i++) pairs.add(new int[]{i, newIdx});
                g.add(r);
            }
        }

        int nextPairIdx = 0;
        while (nextPairIdx < pairs.size()) {
            int[] pair = pairs.get(nextPairIdx++);
            MultivariatePolynomialOverFpFunctionField s = sPolynomial(g.get(pair[0]), g.get(pair[1]), mod, field);
            MultivariatePolynomialOverFpFunctionField r = divide(s, g).remainder;
            if (!r.isZero()) {
                int newIdx = g.size();
                for (int i = 0; i < g.size(); i++) pairs.add(new int[]{i, newIdx});
                g.add(r);
            }
        }
        return reduceGrobnerBasisFractional(g);
    }

    /**
     * 多項式を TaggedPolynomial のリストで除算する。
     * 未テスト。
     * @param f 被除多項式。
     * @param gs 除数 TaggedPolynomial のリスト。
     * @return 除算結果。
     * <p>計算量: O(Iter * m * (T + L * T_coeffs))。</p>
     */
    public static TaggedDivRem taggedDivide(MultivariatePolynomialOverFpFunctionField f, List<TaggedPolynomial> gs) {
        int m = gs.size();
        int n = m > 0 ? gs.get(0).coeffs.length : 0;
        MultivariatePolynomialOverFpFunctionField[] zeroCoeffs = new MultivariatePolynomialOverFpFunctionField[n];
        for (int i = 0; i < n; i++) zeroCoeffs[i] = f.zero();
        return taggedDivide(new TaggedPolynomial(f, zeroCoeffs), gs);
    }

    /**
     * TaggedPolynomial を TaggedPolynomial のリストで除算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: f = sum qs[i] * gs[i] + remainder となる結果を返す。
     * 副作用: なし。
     * 計算量: O(Iter * m * (T + L * T_coeffs))。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param f 被除 TaggedPolynomial。
     * @param gs 除数 TaggedPolynomial のリスト。
     * @return 除算結果。
     */
    public static TaggedDivRem taggedDivide(TaggedPolynomial f, List<TaggedPolynomial> gs) {
        int m = gs.size();
        int n = f.coeffs.length;
        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(f.poly.zero());
        MultivariatePolynomialOverFpFunctionField rPoly = f.poly.zero();
        MultivariatePolynomialOverFpFunctionField pPoly = f.poly;
        FractionFieldStrategy<long[]> field = f.poly.field;

        while (!pPoly.isZero()) {
            Monomial ltP = pPoly.leadingMonomial();
            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Monomial ltG = gs.get(i).poly.leadingMonomial();
                if (ltG != null && ltP.isDivisibleBy(ltG)) {
                    Monomial factorM = ltP.divide(ltG);
                    FractionFieldElement<long[]> factorC = field.div(pPoly.leadingCoefficient(), gs.get(i).poly.leadingCoefficient());
                    MultivariatePolynomialOverFpFunctionField step = singleTerm(f.poly.mod, factorM, factorC);
                    qs.set(i, qs.get(i).add(step));
                    pPoly = pPoly.subtract(gs.get(i).poly.multiply(factorC, factorM));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                rPoly = rPoly.add(singleTerm(f.poly.mod, ltP, pPoly.leadingCoefficient()));
                pPoly = pPoly.subtract(singleTerm(f.poly.mod, ltP, pPoly.leadingCoefficient()));
            }
        }
        MultivariatePolynomialOverFpFunctionField[] rCoeffs = f.coeffs.clone();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                MultivariatePolynomialOverFpFunctionField c1 = (j < rCoeffs.length && rCoeffs[j] != null) ? rCoeffs[j] : f.poly.zero();
                MultivariatePolynomialOverFpFunctionField c2 = (j < gs.get(i).coeffs.length && gs.get(i).coeffs[j] != null) ? gs.get(i).coeffs[j] : f.poly.zero();
                rCoeffs[j] = c1.subtract(qs.get(i).multiply(c2));
            }
        }
        return new TaggedDivRem(qs, new TaggedPolynomial(rPoly, rCoeffs));
    }

    public record TaggedGrobnerBasisResult(List<TaggedPolynomial> idealBasis, List<TaggedPolynomial> syzygyBasis) {
        public List<TaggedPolynomial> idealBasis() { return idealBasis; }
        public List<TaggedPolynomial> syzygyBasis() { return syzygyBasis; }
    }

    /**
     * 多項式のリストから TaggedPolynomial の Grobner 基底を計算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: fs が生成するイデアルの規格化された Grobner 基底 (Tagged) を返す。
     * 副作用: なし。
     * 計算量: O(m^2 * Iter * (T + L * T_coeffs))。
     * 破壊的変更: なし.
     * 参照共有: なし.
     * 例外: なし.
     * @param fs 多項式のリスト。
     * @return TaggedPolynomial の Grobner 基底。
     */
    public static TaggedGrobnerBasisResult taggedGrobnerBasis(List<MultivariatePolynomialOverFpFunctionField> fs) {
        if (fs.isEmpty()) return new TaggedGrobnerBasisResult(new ArrayList<>(), new ArrayList<>());
        if (isAllScalar(fs)) {
            return taggedGrobnerBasisScalar(fs);
        }
        int m = fs.size();
        long mod = fs.get(0).mod;
        FractionFieldStrategy<long[]> field = fs.get(0).field;

        List<TaggedPolynomial> g = new ArrayList<>();
        List<int[]> pairs = new ArrayList<>();

        for (int i = 0; i < m; i++) {
            MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[m];
            for (int j = 0; j < m; j++) coeffs[j] = fs.get(i).zero();
            coeffs[i] = fs.get(i).one();
            TaggedPolynomial f = new TaggedPolynomial(fs.get(i), coeffs);

            TaggedPolynomial r = taggedDivide(f, g).remainder;
            if (!r.poly.isZero()) {
                int newIdx = g.size();
                for (int j = 0; j < g.size(); j++) pairs.add(new int[]{j, newIdx});
                g.add(r);
            }
        }

        int nextPairIdx = 0;
        while (nextPairIdx < pairs.size()) {
            int[] pair = pairs.get(nextPairIdx++);
            TaggedPolynomial s = taggedSPolynomial(g.get(pair[0]), g.get(pair[1]), mod, field);
            TaggedPolynomial r = taggedDivide(s, g).remainder;
            if (!r.poly.isZero()) {
                int newIdx = g.size();
                for (int i = 0; i < g.size(); i++) pairs.add(new int[]{i, newIdx});
                g.add(r);
            }
        }
        List<TaggedPolynomial> idealBasis = taggedReduceGrobnerBasis(g);
        // Note: Generic taggedGrobnerBasis currently does not return syzygies.
        // It's mostly used for scalar cases where taggedGrobnerBasisScalar is called.
        return new TaggedGrobnerBasisResult(idealBasis, new ArrayList<>());
    }
    
    private static boolean isScalar(MultivariatePolynomialOverFpFunctionField f) {
        if (f.isScalarCached != null) return f.isScalarCached;
        for (FractionFieldElement<long[]> c : f.getTerms().values()) {
            if (c.num().length > 1 || c.den().length > 1) {
                f.isScalarCached = false;
                return false;
            }
        }
        f.isScalarCached = true;
        return true;
    }

    private static boolean isAllScalar(List<MultivariatePolynomialOverFpFunctionField> fs) {
        for (MultivariatePolynomialOverFpFunctionField f : fs) {
            if (!isScalar(f)) return false;
        }
        return true;
    }

    private static boolean isAllScalarTagged(List<TaggedPolynomial> fs) {
        for (TaggedPolynomial f : fs) {
            if (!isScalar(f.poly)) return false;
            for (MultivariatePolynomialOverFpFunctionField c : f.coeffs) if (c != null && !isScalar(c)) return false;
        }
        return true;
    }

    private static DivRem divideScalar(MultivariatePolynomialOverFpFunctionField f, List<MultivariatePolynomialOverFpFunctionField> gs) {
        MultivariatePolynomial<Long> mf = f.toMultivariate();
        List<MultivariatePolynomial<Long>> mgs = new ArrayList<>();
        for (MultivariatePolynomialOverFpFunctionField g : gs) mgs.add(g.toMultivariate());
        var res = MultivariatePolynomial.divide(mf, mgs);
        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (MultivariatePolynomial<Long> q : res.quotients) qs.add(fromMultivariate(q));
        return new DivRem(qs, fromMultivariate(res.remainder));
    }

    private static TaggedDivRem moduleDivideScalar(TaggedPolynomial f, List<TaggedPolynomial> gs) {
        int m = gs.size();
        if (m == 0) return moduleDivideStandard(f, gs);
        int nComps = f.coeffs.length;
        for (TaggedPolynomial g : gs) nComps = Math.max(nComps, g.coeffs.length);

        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(f.poly.zero());

        long mod = f.poly.mod;
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(PolynomialFpDynamic.of(mod));

        // Fast path for module division with scalar coefficients
        // We use standard MultivariatePolynomial for components
        MultivariatePolynomial<Long> currentPoly = f.poly.toMultivariate();
        MultivariatePolynomial<Long>[] currentCoeffs = new MultivariatePolynomial[nComps];
        for (int i = 0; i < nComps; i++) currentCoeffs[i] = (i < f.coeffs.length && f.coeffs[i] != null) ? f.coeffs[i].toMultivariate() : new MultivariatePolynomialOverFp(f.poly.mod);

        List<MultivariatePolynomial<Long>> mgsPoly = new ArrayList<>();
        List<MultivariatePolynomial<Long>[]> mgsCoeffs = new ArrayList<>();
        for (TaggedPolynomial g : gs) {
            mgsPoly.add(g.poly.toMultivariate());
            MultivariatePolynomial<Long>[] gc = new MultivariatePolynomial[nComps];
            for (int i = 0; i < nComps; i++) gc[i] = (i < g.coeffs.length && g.coeffs[i] != null) ? g.coeffs[i].toMultivariate() : new MultivariatePolynomialOverFp(f.poly.mod);
            mgsCoeffs.add(gc);
        }

        MultivariatePolynomial<Long> remPoly = new MultivariatePolynomialOverFp(mod);
        MultivariatePolynomial<Long>[] remCoeffs = new MultivariatePolynomial[nComps];
        for (int i = 0; i < nComps; i++) remCoeffs[i] = new MultivariatePolynomialOverFp(mod);

        while (!currentPoly.isZero() || !isAllZero(currentCoeffs)) {
            // Find leading component
            int bestIdx = -1;
            Monomial bestM = null;
            long bestC = 0;

            if (!currentPoly.isZero()) {
                bestIdx = 0;
                bestM = currentPoly.getLeadingMonomial();
                bestC = ((MultivariatePolynomialOverFp) currentPoly).getLeadingCoefficient();
            } else {
                for (int i = 0; i < nComps; i++) {
                    if (!currentCoeffs[i].isZero()) {
                        bestIdx = i + 1;
                        bestM = currentCoeffs[i].getLeadingMonomial();
                        bestC = ((MultivariatePolynomialOverFp) currentCoeffs[i]).getLeadingCoefficient();
                        break;
                    }
                }
            }

            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Monomial ltG;
                long lcG;
                int idxG;
                if (!mgsPoly.get(i).isZero()) {
                    idxG = 0;
                    ltG = mgsPoly.get(i).getLeadingMonomial();
                    lcG = ((MultivariatePolynomialOverFp) mgsPoly.get(i)).getLeadingCoefficient();
                } else {
                    idxG = -1; ltG = null; lcG = 0;
                    for (int j = 0; j < nComps; j++) {
                        if (!mgsCoeffs.get(i)[j].isZero()) {
                            idxG = j + 1;
                            ltG = mgsCoeffs.get(i)[j].getLeadingMonomial();
                            lcG = ((MultivariatePolynomialOverFp) mgsCoeffs.get(i)[j]).getLeadingCoefficient();
                            break;
                        }
                    }
                }

                if (idxG == bestIdx && (ltG != null && bestM.isDivisibleBy(ltG))) {
                    Monomial factorM = bestM.divide(ltG);
                    long factorC = bestC * MathUtils.modInv(lcG, mod) % mod;

                    qs.set(i, qs.get(i).add(singleTerm(mod, factorM, field.of(new long[]{factorC}, new long[]{1}))));

                    currentPoly.subtractInPlace(mgsPoly.get(i), factorC, factorM);
                    for (int j = 0; j < nComps; j++) {
                        currentCoeffs[j].subtractInPlace(mgsCoeffs.get(i)[j], factorC, factorM);
                    }
                    divided = true;
                    break;
                }
            }

            if (!divided) {
                if (bestIdx == 0) {
                    remPoly.addInplace(MultivariatePolynomialOverFp.singleTerm(mod, bestM, bestC));
                    currentPoly.getTerms().remove(bestM);
                } else {
                    remCoeffs[bestIdx - 1].addInplace(MultivariatePolynomialOverFp.singleTerm(mod, bestM, bestC));
                    currentCoeffs[bestIdx - 1].getTerms().remove(bestM);
                }
            }
        }

        MultivariatePolynomialOverFpFunctionField[] resCoeffs = new MultivariatePolynomialOverFpFunctionField[nComps];
        for (int i = 0; i < nComps; i++) resCoeffs[i] = fromMultivariate(remCoeffs[i]);
        return new TaggedDivRem(qs, new TaggedPolynomial(fromMultivariate(remPoly), resCoeffs));
    }

    private static boolean isAllZero(MultivariatePolynomial<Long>[] ps) {
        for (MultivariatePolynomial<Long> p : ps) if (!p.isZero()) return false;
        return true;
    }

    private static TaggedDivRem moduleDivideStandard(TaggedPolynomial f, List<TaggedPolynomial> gs) {
        int m = gs.size();
        int nComps = f.coeffs.length;
        for (TaggedPolynomial g : gs) nComps = Math.max(nComps, g.coeffs.length);
        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(f.poly.zero());

        TaggedPolynomial p = f;
        FractionFieldStrategy<long[]> field = f.poly.field;

        TaggedPolynomial remainder = new TaggedPolynomial(f.poly.zero(), new MultivariatePolynomialOverFpFunctionField[nComps]);
        for (int i = 0; i < nComps; i++) remainder.coeffs[i] = f.poly.zero();

        while (!p.isZero()) {
            Object[] lmP = p.leadingModuleMonomial();
            int idxP = (Integer) lmP[0];
            Monomial mP = (Monomial) lmP[1];
            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Object[] lmG = gs.get(i).leadingModuleMonomial();
                if (lmG == null) continue;
                int idxG = (Integer) lmG[0];
                Monomial mG = (Monomial) lmG[1];

                if (idxP == idxG && mP.isDivisibleBy(mG)) {
                    Monomial factorM = mP.divide(mG);
                    FractionFieldElement<long[]> factorC = field.div(p.leadingModuleCoefficient(), gs.get(i).leadingModuleCoefficient());
                    qs.set(i, qs.get(i).add(singleTerm(f.poly.mod, factorM, factorC)));
                    p = p.subtract(gs.get(i).multiply(factorC, factorM));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                FractionFieldElement<long[]> lc = p.leadingModuleCoefficient();
                MultivariatePolynomialOverFpFunctionField step = singleTerm(f.poly.mod, mP, lc);
                if (idxP == 0) {
                    remainder = new TaggedPolynomial(remainder.poly.add(step), remainder.coeffs);
                    p = new TaggedPolynomial(p.poly.subtract(step), p.coeffs);
                } else {
                    MultivariatePolynomialOverFpFunctionField[] nextCoeffs = remainder.coeffs.clone();
                    nextCoeffs[idxP - 1] = nextCoeffs[idxP - 1].add(step);
                    remainder = new TaggedPolynomial(remainder.poly, nextCoeffs);

                    MultivariatePolynomialOverFpFunctionField[] nextPCoeffs = p.coeffs.clone();
                    nextPCoeffs[idxP - 1] = nextPCoeffs[idxP - 1].subtract(step);
                    p = new TaggedPolynomial(p.poly, nextPCoeffs);
                }
            }
        }
        return new TaggedDivRem(qs, remainder);
    }

    private MultivariatePolynomial<Long> toMultivariate() {
        TreeMap<Monomial, Long> mTerms = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().entrySet()) {
            long num = entry.getValue().num().length > 0 ? entry.getValue().num()[0] : 0;
            long den = entry.getValue().den().length > 0 ? entry.getValue().den()[0] : 1;
            mTerms.put(entry.getKey(), num * MathUtils.modInv(den, mod) % mod);
        }
        return new MultivariatePolynomialOverFp(mod, mTerms);
    }

    private static MultivariatePolynomialOverFpFunctionField fromMultivariate(MultivariatePolynomial<Long> p) {
        TreeMap<Monomial, FractionFieldElement<long[]>> fTerms = new TreeMap<>();
        long mod = (p instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) p.getCoefficientRing()).getMod();
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);
        for (Map.Entry<Monomial, Long> entry : p.getTerms().entrySet()) {
            fTerms.put(entry.getKey(), field.from(new long[]{entry.getValue()}));
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, fTerms);
    }

    private static TaggedGrobnerBasisResult taggedGrobnerBasisScalar(List<MultivariatePolynomialOverFpFunctionField> fs) {
        if (fs.isEmpty()) return new TaggedGrobnerBasisResult(new ArrayList<>(), new ArrayList<>());
        int m = fs.size();
        List<TaggedPolynomial> taggedInputs = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            MultivariatePolynomialOverFpFunctionField f = fs.get(i);
            MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[m];
            for (int j = 0; j < m; j++) coeffs[j] = f.zero();
            coeffs[i] = f.one();
            taggedInputs.add(new TaggedPolynomial(f, coeffs));
        }
        List<TaggedPolynomial> moduleGb = moduleGrobnerBasis(taggedInputs);
        List<TaggedPolynomial> idealBasis = new ArrayList<>();
        List<TaggedPolynomial> syzygyBasis = new ArrayList<>();
        for (TaggedPolynomial g : moduleGb) {
            if (!g.poly.isZero()) idealBasis.add(g);
            else syzygyBasis.add(g);
        }
        return new TaggedGrobnerBasisResult(idealBasis, syzygyBasis);
    }

    public static MultivariatePolynomialOverFpFunctionField fromRingsPolynomialFractional(MultivariatePolynomialZp64 p, long mod) {
        TreeMap<Monomial, FractionFieldElement<long[]>> fTerms = new TreeMap<>();
        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(poly1d);
        for (MonomialZp64 term : p) {
            fTerms.put(new Monomial(term.exponents), field.from(new long[]{(term.coefficient % mod + mod) % mod}));
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, fTerms);
    }

    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

    private static MultivariatePolynomialOverFpFunctionField sPolynomial(MultivariatePolynomialOverFpFunctionField f1, MultivariatePolynomialOverFpFunctionField f2, long mod, FractionFieldStrategy<long[]> field) {
        Monomial m1 = f1.leadingMonomial();
        Monomial m2 = f2.leadingMonomial();
        if (m1 == null || m2 == null) return f1.zero();
        Monomial lcm = Monomial.lcm(m1, m2);

        Monomial factor1 = lcm.divide(m1);
        Monomial factor2 = lcm.divide(m2);

        FractionFieldElement<long[]> c1 = field.inv(f1.leadingCoefficient());
        FractionFieldElement<long[]> c2 = field.inv(f2.leadingCoefficient());

        return f1.multiply(c1, factor1).subtract(f2.multiply(c2, factor2));
    }

    /**
     * 2つの TaggedPolynomial の S-多項式を計算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: S-多項式を返す。
     * 副作用: なし。
     * 計算量: O(T + L * T_coeffs)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param f1 第1項。
     * @param f2 第2項。
     * @param mod 標数。
     * @param field 係数体。
     * @return S-多項式。
     */
    private static TaggedPolynomial taggedSPolynomial(TaggedPolynomial f1, TaggedPolynomial f2, long mod, FractionFieldStrategy<long[]> field) {
        Monomial m1 = f1.poly.leadingMonomial();
        Monomial m2 = f2.poly.leadingMonomial();
        if (m1 == null || m2 == null) return new TaggedPolynomial(f1.poly.zero(), new MultivariatePolynomialOverFpFunctionField[f1.coeffs.length]);
        Monomial lcm = Monomial.lcm(m1, m2);

        Monomial factor1 = lcm.divide(m1);
        Monomial factor2 = lcm.divide(m2);

        FractionFieldElement<long[]> c1 = field.inv(f1.poly.leadingCoefficient());
        FractionFieldElement<long[]> c2 = field.inv(f2.poly.leadingCoefficient());

        return f1.multiply(c1, factor1).subtract(f2.multiply(c2, factor2));
    }

    public static List<MultivariatePolynomialOverFpFunctionField> reduceGrobnerBasisFractional(List<MultivariatePolynomialOverFpFunctionField> g) {
        if (g.isEmpty()) return g;
        List<MultivariatePolynomialOverFpFunctionField> res = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            boolean redundant = false;
            Monomial ltI = g.get(i).leadingMonomial();
            if (ltI == null) { redundant = true; }
            else {
                for (int j = 0; j < g.size(); j++) {
                    if (i == j) continue;
                    Monomial ltJ = g.get(j).leadingMonomial();
                    if (ltJ == null) continue;
                    if (ltI.isDivisibleBy(ltJ) && ltI.compareTo(ltJ) != 0) {
                        redundant = true;
                        break;
                    }
                    if (ltI.compareTo(ltJ) == 0 && j < i) {
                        redundant = true;
                        break;
                    }
                }
            }
            if (!redundant) res.add(g.get(i));
        }
        for (int i = 0; i < res.size(); i++) {
            MultivariatePolynomialOverFpFunctionField f = res.get(i);
            FractionFieldElement<long[]> lcInv = f.field.inv(f.leadingCoefficient());
            res.set(i, f.multiply(lcInv));
        }
        return res;
    }

    /**
     * TaggedPolynomial をベクトルとして扱い、それらが生成するサブモジュールの Grobner 基底を計算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: fs が生成するサブモジュールの規格化された Grobner 基底を返す。
     * 副作用: なし。
     * 計算量: O(m^2 * Iter * (T + L * T_coeffs))。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param fs 生成ベクトルのリスト。
     * @return サブモジュールの Grobner 基底。
     */
    public static List<TaggedPolynomial> moduleGrobnerBasis(List<TaggedPolynomial> fs) {
        if (fs.isEmpty()) return new ArrayList<>();
        long mod = fs.get(0).poly.mod;
        FractionFieldStrategy<long[]> field = fs.get(0).poly.field;

        List<TaggedPolynomial> g = new ArrayList<>();
        List<int[]> pairs = new ArrayList<>();

        for (TaggedPolynomial f : fs) {
            TaggedPolynomial r = moduleDivide(f, g).remainder;
            if (!r.isZero()) {
                int newIdx = g.size();
                for (int i = 0; i < g.size(); i++) pairs.add(new int[]{i, newIdx});
                g.add(r);
            }
        }

        int nextPairIdx = 0;
        while (nextPairIdx < pairs.size()) {
            int[] pair = pairs.get(nextPairIdx++);
            TaggedPolynomial s = moduleSPolynomial(g.get(pair[0]), g.get(pair[1]), mod, field);
            if (s == null) continue;
            TaggedPolynomial r = moduleDivide(s, g).remainder;
            if (!r.isZero()) {
                int newIdx = g.size();
                for (int i = 0; i < g.size(); i++) pairs.add(new int[]{i, newIdx});
                g.add(r);
            }
        }
        return moduleReduceGrobnerBasis(g);
    }

    /**
     * TaggedPolynomial (ベクトル) を TaggedPolynomial のリスト (GB) で除算する。
     * 未テスト。
     * 事前条件: gs は規格化されたモジュール Grobner 基底であること。
     * 事後条件: f = sum qs[i] * gs[i] + remainder となる結果を返す。
     * 副作用: なし。
     * 計算量: O(Iter * m * (T + L * T_coeffs))。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param f 除算されるベクトル。
     * @param gs 除数ベクトルのリスト。
     * @return 除算結果。
     */
    public static TaggedDivRem moduleDivide(TaggedPolynomial f, List<TaggedPolynomial> gs) {
        if (isScalar(f.poly) && isAllScalarTagged(gs)) {
            return moduleDivideScalar(f, gs);
        }
        int m = gs.size();
        int nComps = f.coeffs.length;
        for (TaggedPolynomial g : gs) nComps = Math.max(nComps, g.coeffs.length);
        List<MultivariatePolynomialOverFpFunctionField> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(f.poly.zero());

        TaggedPolynomial p = f;
        FractionFieldStrategy<long[]> field = f.poly.field;

        TaggedPolynomial remainder = new TaggedPolynomial(f.poly.zero(), new MultivariatePolynomialOverFpFunctionField[nComps]);
        for (int i = 0; i < nComps; i++) remainder.coeffs[i] = f.poly.zero();

        while (!p.isZero()) {
            Object[] lmP = p.leadingModuleMonomial();
            int idxP = (Integer) lmP[0];
            Monomial mP = (Monomial) lmP[1];
            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Object[] lmG = gs.get(i).leadingModuleMonomial();
                if (lmG == null) continue;
                int idxG = (Integer) lmG[0];
                Monomial mG = (Monomial) lmG[1];

                if (idxP == idxG && mP.isDivisibleBy(mG)) {
                    Monomial factorM = mP.divide(mG);
                    FractionFieldElement<long[]> factorC = field.div(p.leadingModuleCoefficient(), gs.get(i).leadingModuleCoefficient());
                    qs.set(i, qs.get(i).add(singleTerm(f.poly.mod, factorM, factorC)));
                    p = p.subtract(gs.get(i).multiply(factorC, factorM));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                FractionFieldElement<long[]> lc = p.leadingModuleCoefficient();
                MultivariatePolynomialOverFpFunctionField step = singleTerm(f.poly.mod, mP, lc);
                if (idxP == 0) {
                    remainder = new TaggedPolynomial(remainder.poly.add(step), remainder.coeffs);
                    p = new TaggedPolynomial(p.poly.subtract(step), p.coeffs);
                } else {
                    MultivariatePolynomialOverFpFunctionField[] nextCoeffs = remainder.coeffs.clone();
                    nextCoeffs[idxP - 1] = nextCoeffs[idxP - 1].add(step);
                    remainder = new TaggedPolynomial(remainder.poly, nextCoeffs);

                    MultivariatePolynomialOverFpFunctionField[] nextPCoeffs = p.coeffs.clone();
                    nextPCoeffs[idxP - 1] = nextPCoeffs[idxP - 1].subtract(step);
                    p = new TaggedPolynomial(p.poly, nextPCoeffs);
                }
            }
        }
        return new TaggedDivRem(qs, remainder);
    }

    private static TaggedPolynomial moduleSPolynomial(TaggedPolynomial f1, TaggedPolynomial f2, long mod, FractionFieldStrategy<long[]> field) {
        Object[] lm1 = f1.leadingModuleMonomial();
        Object[] lm2 = f2.leadingModuleMonomial();
        if (lm1 == null || lm2 == null) return null;
        int idx1 = (Integer) lm1[0];
        int idx2 = (Integer) lm2[0];
        if (idx1 != idx2) return null;

        Monomial m1 = (Monomial) lm1[1];
        Monomial m2 = (Monomial) lm2[1];
        Monomial lcm = Monomial.lcm(m1, m2);

        Monomial factor1 = lcm.divide(m1);
        Monomial factor2 = lcm.divide(m2);

        FractionFieldElement<long[]> c1 = field.inv(f1.leadingModuleCoefficient());
        FractionFieldElement<long[]> c2 = field.inv(f2.leadingModuleCoefficient());

        return f1.multiply(c1, factor1).subtract(f2.multiply(c2, factor2));
    }

    /**
     * 加群の Grobner 基底を簡約化する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: 規格化された Grobner 基底を返す。
     * 副作用: なし。
     * 計算量: O(m^2 * T)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param g Grobner 基底のリスト。
     * @return 簡約化された Grobner 基底。
     */
    public static List<TaggedPolynomial> moduleReduceGrobnerBasis(List<TaggedPolynomial> g) {
        if (g.isEmpty()) return g;
        List<TaggedPolynomial> res = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            boolean redundant = false;
            Object[] lmI = g.get(i).leadingModuleMonomial();
            if (lmI == null) { redundant = true; }
            else {
                int idxI = (Integer) lmI[0];
                Monomial mI = (Monomial) lmI[1];
                for (int j = 0; j < g.size(); j++) {
                    if (i == j) continue;
                    Object[] lmJ = g.get(j).leadingModuleMonomial();
                    if (lmJ == null) continue;
                    int idxJ = (Integer) lmJ[0];
                    Monomial mJ = (Monomial) lmJ[1];
                    if (idxI == idxJ && mI.isDivisibleBy(mJ)) {
                        if (mI.compareTo(mJ) != 0 || j < i) {
                            redundant = true;
                            break;
                        }
                    }
                }
            }
            if (!redundant) res.add(g.get(i));
        }
        for (int i = 0; i < res.size(); i++) {
            TaggedPolynomial f = res.get(i);
            FractionFieldElement<long[]> lcInv = f.poly.field.inv(f.leadingModuleCoefficient());
            res.set(i, f.multiply(lcInv, new Monomial(new int[0])));
        }
        return res;
    }

    /**
     * Gröbner 基底 G に対するシジジー加群 Syz(G) の Gröbner 基底を Schreyer の定理を用いて計算する。
     * 未テスト。
     * @param gb 規格化された Gröbner 基底。
     * @return Syz(G) の Gröbner 基底（gb の各要素に対する係数ベクトルのリスト）。
     * <p>計算量: O(t^2 * Iter * T)。</p>
     */
    public static List<MultivariatePolynomialOverFpFunctionField[]> computeSyzygiesOfGB(List<TaggedPolynomial> gb) {
        if (gb.isEmpty()) return new ArrayList<>();
        if (isAllScalarTagged(gb)) {
            return computeSyzygiesOfGBScalar(gb);
        }
        int t = gb.size();
        long mod = gb.get(0).poly.mod;
        List<MultivariatePolynomialOverFpFunctionField[]> syzygies = new ArrayList<>();

        for (int i = 0; i < t; i++) {
            for (int j = i + 1; j < t; j++) {
                Monomial mi = gb.get(i).poly.leadingMonomial();
                Monomial mj = gb.get(j).poly.leadingMonomial();
                if (mi == null || mj == null) continue;
                Monomial lcm = Monomial.lcm(mi, mj);

                Monomial factorI = lcm.divide(mi);
                Monomial factorJ = lcm.divide(mj);

                FractionFieldStrategy<long[]> field = gb.get(i).poly.field;
                FractionFieldElement<long[]> ci = field.inv(gb.get(i).poly.leadingCoefficient());
                FractionFieldElement<long[]> cj = field.inv(gb.get(j).poly.leadingCoefficient());

                MultivariatePolynomialOverFpFunctionField sPoly = gb.get(i).poly.multiply(ci, factorI)
                        .subtract(gb.get(j).poly.multiply(cj, factorJ));

                List<MultivariatePolynomialOverFpFunctionField> gs = new ArrayList<>();
                for (TaggedPolynomial tp : gb) gs.add(tp.poly);
                DivRem dr = divide(sPoly, gs);

                MultivariatePolynomialOverFpFunctionField[] syz = new MultivariatePolynomialOverFpFunctionField[t];
                for (int k = 0; k < t; k++) syz[k] = gb.get(0).poly.zero();
                syz[i] = singleTerm(mod, factorI, ci);
                syz[j] = singleTerm(mod, factorJ, field.neg(cj));
                for (int k = 0; k < t; k++) {
                    syz[k] = syz[k].subtract(dr.quotients.get(k));
                }
                syzygies.add(syz);
            }
        }
        return syzygies;
    }

    private static List<MultivariatePolynomialOverFpFunctionField[]> computeSyzygiesOfGBScalar(List<TaggedPolynomial> gb) {
        int t = gb.size();
        long mod = gb.get(0).poly.mod;
        FractionFieldStrategy<long[]> field = new FractionFieldStrategy<>(PolynomialFpDynamic.of(mod));
        List<MultivariatePolynomial<Long>> mgb = new ArrayList<>();
        for (TaggedPolynomial tp : gb) mgb.add(tp.poly.toMultivariate());

        List<MultivariatePolynomialOverFpFunctionField[]> syzygies = new ArrayList<>();
        for (int i = 0; i < t; i++) {
            for (int j = i + 1; j < t; j++) {
                MultivariatePolynomial<Long> sPoly = MultivariatePolynomial.sPolynomial(mgb.get(i), mgb.get(j));
                var res = MultivariatePolynomial.divide(sPoly, mgb);

                Monomial mi = mgb.get(i).getLeadingMonomial();
                Monomial mj = mgb.get(j).getLeadingMonomial();
                Monomial lcm = Monomial.lcm(mi, mj);
                long ci = MathUtils.modInv(((MultivariatePolynomialOverFp) mgb.get(i)).getLeadingCoefficient(), mod);
                long cj = MathUtils.modInv(((MultivariatePolynomialOverFp) mgb.get(j)).getLeadingCoefficient(), mod);

                MultivariatePolynomialOverFpFunctionField[] syz = new MultivariatePolynomialOverFpFunctionField[t];
                for (int k = 0; k < t; k++) syz[k] = gb.get(0).poly.zero();
                syz[i] = singleTerm(mod, lcm.divide(mi), field.of(new long[]{ci}, new long[]{1}));
                syz[j] = singleTerm(mod, lcm.divide(mj), field.of(new long[]{(mod - cj) % mod}, new long[]{1}));
                for (int k = 0; k < t; k++) {
                    syz[k] = syz[k].subtract(fromMultivariate(res.quotients.get(k)));
                }
                syzygies.add(syz);
            }
        }
        return syzygies;
    }

    /**
     * GB に対するシジジーを元の生成元に対するシジジーに変換する。
     * 未テスト。
     * @param gbSyzygy GB に対する係数ベクトル。
     * @param gb Tagged な GB。
     * @return 元の生成元に対する係数ベクトル。
     * <p>計算量: O(t * L * T_coeffs)。</p>
     */
    public static MultivariatePolynomialOverFpFunctionField[] convertSyzygy(MultivariatePolynomialOverFpFunctionField[] gbSyzygy, List<TaggedPolynomial> gb) {
        if (gb.isEmpty()) return new MultivariatePolynomialOverFpFunctionField[0];
        int nOriginal = gb.get(0).coeffs.length;
        int t = gb.size();
        MultivariatePolynomialOverFpFunctionField[] res = new MultivariatePolynomialOverFpFunctionField[nOriginal];
        for (int i = 0; i < nOriginal; i++) res[i] = gb.get(0).poly.zero();

        for (int j = 0; j < t; j++) {
            if (gbSyzygy[j] == null || gbSyzygy[j].isZero()) continue;
            for (int i = 0; i < nOriginal; i++) {
                if (gb.get(j).coeffs[i] != null) {
                    res[i] = res[i].add(gbSyzygy[j].multiply(gb.get(j).coeffs[i]));
                }
            }
        }
        return res;
    }

    /**
     * TaggedPolynomial のリストから冗長な要素を取り除き、規格化された Grobner 基底を計算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: 規格化されたイデアル Grobner 基底 (Tagged) を返す。
     * 副作用: なし。
     * 計算量: O(m^2 * T)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param g Grobner 基底のリスト。
     * @return 規格化された Grobner 基底。
     */
    public static List<TaggedPolynomial> taggedReduceGrobnerBasis(List<TaggedPolynomial> g) {
        if (g.isEmpty()) return g;
        List<TaggedPolynomial> res = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            boolean redundant = false;
            Monomial ltI = g.get(i).poly.leadingMonomial();
            if (ltI == null) { redundant = true; }
            else {
                for (int j = 0; j < g.size(); j++) {
                    if (i == j) continue;
                    Monomial ltJ = g.get(j).poly.leadingMonomial();
                    if (ltJ == null) continue;
                    if (ltI.isDivisibleBy(ltJ) && ltI.compareTo(ltJ) != 0) {
                        redundant = true;
                        break;
                    }
                    if (ltI.compareTo(ltJ) == 0 && j < i) {
                        redundant = true;
                        break;
                    }
                }
            }
            if (!redundant) res.add(g.get(i));
        }
        for (int i = 0; i < res.size(); i++) {
            TaggedPolynomial f = res.get(i);
            FractionFieldElement<long[]> lcInv = f.poly.field.inv(f.poly.leadingCoefficient());
            res.set(i, f.multiply(lcInv));
        }
        return res;
    }

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : getTerms().descendingMap().entrySet()) {
            Monomial m = entry.getKey();
            FractionFieldElement<long[]> f = entry.getValue();

            long[] num = f.num();
            long[] den = f.den();

            String nStr = formatPoly(num, "t");
            String dStr = formatPoly(den, "t");

            boolean numHasMultiple = hasMultipleTerms(num);
            boolean denNeedsParens = needsParensDen(den);

            if (numHasMultiple) nStr = "(" + nStr + ")";
            if (denNeedsParens) dStr = "(" + dStr + ")";

            String mStr = formatMonomial(m);

            if (sb.length() > 0) {
                if (!numHasMultiple && nStr.startsWith("-")) {
                    sb.append(" - ");
                    nStr = nStr.substring(1);
                } else {
                    sb.append(" + ");
                }
            }

            boolean isOne = nStr.equals("1");
            boolean isMinusOne = nStr.equals("-1");
            boolean isDenOne = den.length == 1 && den[0] == 1;

            if (isDenOne) {
                if (mStr.isEmpty()) {
                    sb.append(nStr);
                } else {
                    if (isOne) sb.append(mStr);
                    else if (isMinusOne) sb.append("-").append(mStr);
                    else sb.append(nStr).append("*").append(mStr);
                }
            } else {
                String frac = nStr + "/" + dStr;
                if (mStr.isEmpty()) {
                    sb.append(frac);
                } else {
                    sb.append(frac).append("*").append(mStr);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 多項式の係数（一変数多項式）を文字列形式にフォーマットする。
     * 未テスト。
     *
     * @param p 係数配列
     * @param var 変数名
     * @return フォーマットされた文字列
     * <p>計算量: O(deg(p))。</p>
     */
    private String formatPoly(long[] p, String var) {
        int deg = -1;
        for (int i = p.length - 1; i >= 0; i--) {
            if (p[i] != 0) {
                deg = i;
                break;
            }
        }
        if (deg == -1) return "0";

        StringBuilder sb = new StringBuilder();
        for (int i = deg; i >= 0; i--) {
            long c = p[i];
            if (c == 0) continue;

            long sc = c;
            if (sc > mod / 2) sc -= mod;

            if (sb.length() > 0) {
                if (sc > 0) {
                    sb.append(" + ");
                } else {
                    sb.append(" - ");
                    sc = -sc;
                }
            } else {
                if (sc < 0) {
                    sb.append("-");
                    sc = -sc;
                }
            }

            if (i == 0) {
                sb.append(sc);
            } else {
                if (sc == 1) {
                    sb.append(var);
                } else if (sc == -1) {
                    sb.append("-").append(var);
                } else {
                    sb.append(sc).append("*").append(var);
                }
                if (i > 1) {
                    sb.append("^").append(i);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 多項式が複数の項を持つか判定する。
     * 未テスト。
     *
     * @param p 係数配列
     * @return 複数の非ゼロ項を持つ場合は true
     * <p>計算量: O(len(p))。</p>
     */
    private boolean hasMultipleTerms(long[] p) {
        int count = 0;
        for (long c : p) {
            if (c != 0) count++;
        }
        return count > 1;
    }

    /**
     * 分母に括弧が必要か判定する。
     * 未テスト。
     *
     * @param p 分母多項式
     * @return 括弧が必要な場合は true
     * <p>計算量: O(len(p))。</p>
     */
    private boolean needsParensDen(long[] p) {
        if (hasMultipleTerms(p)) return true;
        int idx = -1;
        for (int i = 0; i < p.length; i++) {
            if (p[i] != 0) idx = i;
        }
        if (idx <= 0) return false;
        if (idx == 1) {
            long c = p[1];
            if (c > mod / 2) c -= mod;
            return c != 1 && c != -1;
        }
        return true;
    }

    /**
     * 単項式を文字列形式にフォーマットする。
     * 未テスト。
     *
     * @param m 単項式
     * @return フォーマットされた文字列
     * <p>計算量: O(変数の数)。</p>
     */
    private String formatMonomial(Monomial m) {
        StringBuilder sb = new StringBuilder();
        int[] exps = m.exponents();
        String[] vars = {"x", "y", "z", "w"};
        for (int i = 0; i < exps.length; i++) {
            if (exps[i] == 0) continue;
            if (sb.length() > 0) sb.append("*");
            String var = (i < vars.length) ? vars[i] : "x" + i;
            sb.append(var);
            if (exps[i] > 1) {
                sb.append("^").append(exps[i]);
            }
        }
        return sb.toString();
    }
}
