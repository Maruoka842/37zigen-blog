package library.util.polynomial;

import java.util.*;
import library.util.algebra.strategy.FpStrategy;
import cc.redberry.rings.IntegersZp64;
import cc.redberry.rings.poly.multivar.GroebnerBases;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MonomialZp64;
import cc.redberry.rings.poly.multivar.MultivariatePolynomialZp64;

/**
 * 係数環を F_p = Z/pZ に固定した多変数多項式。
 * {@link MultivariatePolynomial} の汎用係数環実装に、long の法 p による構築だけを追加する。
 */
public class MultivariatePolynomialOverFp extends MultivariatePolynomial<Long> {
    /** F_p の法 p。 */
    private final long mod;

    /**
     * F_p 上の空（ゼロ）多項式を生成する。
     * 未テスト。
     * 数学的表記: 0 in F_p[x_0,x_1,...]。
     * 事前条件: mod > 0。
     * 事後条件: isZero() == true かつ getMod() == mod。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 項写像は新規所有する。
     * 例外・未定義条件: mod <= 0 のとき未定義。
     * @param mod 法 p。
     */
    public MultivariatePolynomialOverFp(long mod) {
        super(new FpStrategy(mod));
        this.mod = mod;
    }

    /**
     * F_p 上の初期項付き多項式を生成する。
     * 未テスト。
     * 数学的表記: Σ_m terms[m] m in F_p[x_0,x_1,...]。
     * 事前条件: mod > 0, terms != null, すべての key/value != null。
     * 事後条件: 係数は F_p の演算で正規化され、零係数の項は削除される。
     * 副作用: なし。
     * 計算量: O(T log T) (T = terms.size())。
     * 破壊的変更: terms を変更しない。
     * 参照共有・所有権: Monomial 参照を共有し、項写像は新規所有する。
     * 例外・未定義条件: null 要素があるとき NullPointerException。
     * @param mod 法 p。
     * @param terms 初期項。
     */
    public MultivariatePolynomialOverFp(long mod, TreeMap<Monomial, Long> terms) {
        super(new FpStrategy(mod), terms);
        this.mod = mod;
    }

    /**
     * 法 p を返す。
     * 未テスト。
     * 数学的表記: p where coefficients are in F_p。
     * 事前条件: なし。
     * 事後条件: 戻り値 == コンストラクタに渡した mod。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: なし。
     * 例外・未定義条件: なし。
     * @return 法 p。
     */
    public long getMod() {
        return mod;
    }

    @Override
    protected MultivariatePolynomialOverFp fromMap(java.util.Map<Monomial, Long> terms) {
        return new MultivariatePolynomialOverFp(mod, new java.util.TreeMap<>(terms));
    }

    @Override
    public MultivariatePolynomialOverFp zero() {
        return new MultivariatePolynomialOverFp(mod);
    }

    @Override
    public MultivariatePolynomialOverFp one() {
        return singleTerm(mod, new Monomial(new int[0]), 1L);
    }

    /**
     * 主係数 (LC) を long として返す。
     * 未テスト。
     * @return 主係数。
     */
    public long getLeadingCoefficient() {
        return leadingCoefficient();
    }

    /**
     * 簡約グレブナー基底を計算するための Buchberger のアルゴリズム。
     * rings の F4/GB 実装を用いてグレブナー基底を計算する最適化を含む。
     * 未テスト。
     * @param F 入力多項式のリスト。
     * @return 簡約グレブナー基底。
     */
    public static List<MultivariatePolynomial<Long>> grobnerBasisFp(List<MultivariatePolynomial<Long>> F) {
        List<MultivariatePolynomial<Long>> fast = grobnerBasisOptimized(F);
        if (fast != null) return fast;
        return MultivariatePolynomial.grobnerBasis(F);
    }

    /**
     * rings の F4/GB 実装を用いてグレブナー基底を計算する。
     * 未テスト。
     */
    private static List<MultivariatePolynomial<Long>> grobnerBasisOptimized(List<MultivariatePolynomial<Long>> F) {
        if (F.isEmpty()) return new ArrayList<>();
        long mod = -1;
        for (MultivariatePolynomial<Long> p : F) {
            if (p instanceof MultivariatePolynomialOverFp pFp) {
                if (mod == -1) mod = pFp.getMod();
                else if (mod != pFp.getMod()) return null;
            } else {
                // FpStrategy か ZnStrategy かチェック
                if (p.getCoefficientRing() instanceof library.util.algebra.strategy.ZnStrategy zn) {
                    if (mod == -1) mod = zn.getMod();
                    else if (mod != zn.getMod()) return null;
                } else {
                    return null;
                }
            }
        }
        if (mod <= 1) return null;

        int nVars = 0;
        for (MultivariatePolynomial<Long> p : F) {
            for (Monomial m : p.getTerms().keySet()) nVars = Math.max(nVars, m.size());
        }
        if (nVars == 0) nVars = 1;

        IntegersZp64 ring = new IntegersZp64(mod);
        List<MultivariatePolynomialZp64> converted = new ArrayList<>();
        for (MultivariatePolynomial<Long> p : F) converted.add(toRingsPolynomial(p, nVars, ring));
        try {
            List<MultivariatePolynomialZp64> gb = GroebnerBases.GroebnerBasis(converted, MonomialOrder.GREVLEX);
            List<MultivariatePolynomial<Long>> res = new ArrayList<>();
            for (MultivariatePolynomialZp64 p : gb) {
                MultivariatePolynomial<Long> q = fromRingsPolynomial(p, mod);
                if (!q.isZero()) res.add(q);
            }
            return MultivariatePolynomial.reduceGrobnerBasis(res);
        } catch (RuntimeException | AssertionError e) {
            return null;
        }
    }

    /**
     * 自前の多項式表現を rings の Zp64 多項式へ変換する。
     * 未テスト。
     */
    public static MultivariatePolynomialZp64 toRingsPolynomial(MultivariatePolynomial<Long> p, int nVars, IntegersZp64 ring) {
        List<MonomialZp64> terms = new ArrayList<>();
        for (Map.Entry<Monomial, Long> entry : p.getTerms().entrySet()) {
            int[] exps = new int[nVars];
            for (int i = 0; i < nVars; i++) exps[i] = entry.getKey().getExponent(i);
            terms.add(new MonomialZp64(exps, entry.getValue()));
        }
        return MultivariatePolynomialZp64.create(nVars, ring, MonomialOrder.GREVLEX, terms);
    }

    /**
     * rings の Zp64 多項式を自前の多項式表現へ変換する。
     * 未テスト。
     */
    public static MultivariatePolynomial<Long> fromRingsPolynomial(MultivariatePolynomialZp64 p, long mod) {
        TreeMap<Monomial, Long> terms = new TreeMap<>();
        for (MonomialZp64 term : p) {
            terms.put(new Monomial(term.exponents), (term.coefficient % mod + mod) % mod);
        }
        return new MultivariatePolynomialOverFp(mod, terms);
    }

    /**
     * F_p 上の単項多項式 c*m を返す。
     * 未テスト。
     * 数学的表記: (c mod p)*m in F_p[x_0,x_1,...]。
     * 事前条件: mod > 0, m != null。
     * 事後条件: c = 0 in F_p なら零多項式、そうでなければ supp(戻り値) = {m}。
     * 副作用: なし。
     * 計算量: O(1)。
     * 破壊的変更: なし。
     * 参照共有・所有権: m を共有し、項写像は新規所有する。
     * 例外・未定義条件: mod <= 0 または m == null のとき未定義。
     * @param mod 法 p。
     * @param m 単項式。
     * @param c 係数。
     * @return 単項多項式。
     */
    public static MultivariatePolynomialOverFp singleTerm(long mod, Monomial m, long c) {
        TreeMap<Monomial, Long> terms = new TreeMap<>();
        terms.put(m, (c % mod + mod) % mod);
        return new MultivariatePolynomialOverFp(mod, terms);
    }

    /**
     * F_p 上で指定変数の単項多項式 c*x_varIdx^exp を返す。
     * 未テスト。
     * 数学的表記: (c mod p) x_{varIdx}^{exp} in F_p[x_0,...,x_{totalVars-1}]。
     * 事前条件: mod > 0, 0 <= varIdx < totalVars, exp >= 0。
     * 事後条件: 戻り値は指定された単項式のみを持つ。ただし c = 0 in F_p なら零多項式。
     * 副作用: なし。
     * 計算量: O(totalVars)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 返値は新規所有する。
     * 例外・未定義条件: 事前条件を満たさない場合は未定義。
     * @param mod 法 p。
     * @param varIdx 変数インデックス。
     * @param exp 指数。
     * @param c 係数。
     * @param totalVars 単項式の変数配列長。
     * @return 単項多項式。
     */
    public static MultivariatePolynomialOverFp singleTerm(long mod, int varIdx, int exp, long c, int totalVars) {
        int[] exps = new int[totalVars];
        exps[varIdx] = exp;
        return singleTerm(mod, new Monomial(exps), c);
    }

}
