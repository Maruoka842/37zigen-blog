package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.IntStream;

import cc.redberry.rings.IntegersZp64;
import cc.redberry.rings.poly.PolynomialFactorDecomposition;
import cc.redberry.rings.poly.multivar.MonomialZp64;
import cc.redberry.rings.poly.multivar.MultivariatePolynomialZp64;
import cc.redberry.rings.poly.multivar.MultivariateSquareFreeFactorization;
import library.util.Itertools;
import library.util.MathUtils;
import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.instance.VectorSpaceElement;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.VectorSpaceStrategy;
import library.util.linalg.IncrementalVectorBasis;
import library.util.linalg.Matrix;

/**
 * 有理関数の積分のための Griffiths-Dwork 簡約およびその拡張 [·]_r。
 * Pierre Lairez の "Computing periods of rational integrals" に基づく。
 */
public class RationalIntegralReduction {
    private final long mod;
    private final MultivariatePolynomial<Long> f;
    private final MultivariatePolynomialOverFpFunctionField fSpatial;
    private final int[] spaceIndices;
    private final int dim;
    private final int degf;
    private final int tVarIdx;
    private final int maxVarIdx;

    private List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> submoduleGB;
    private List<MultivariatePolynomialOverFpFunctionField[]> SyzGB;
    private List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> SyzPrimeGB;

    private final Map<String, IncrementalVectorBasis<FractionFieldElement<long[]>, Monomial>> basisXCache = new HashMap<>();
    private final FractionFieldStrategy<long[]> field;
    private final PolynomialFpDynamic poly1d;

    private RationalIntegralReduction(MultivariatePolynomial<Long> f, int[] spaceIndices, int tVarIdx) {
        this.mod = (f instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) f.getCoefficientRing()).getMod();
        this.f = f;
        this.tVarIdx = tVarIdx;
        this.spaceIndices = spaceIndices.clone();
        this.dim = spaceIndices.length;

        int mv = tVarIdx;
        for (int si : spaceIndices) mv = Math.max(mv, si);
        this.maxVarIdx = mv + 1;

        this.poly1d = PolynomialFpDynamic.of(mod);
        this.field = new FractionFieldStrategy<>(poly1d);
        this.fSpatial = toSpatial(f);
        this.degf = f.getDegree(spaceIndices);
    }

    public static class HomogenizedTriple {
        public final MultivariatePolynomial<Long> a, f;
        public final int p, tVarIdx;
        public final int[] spaceIndices;
        public HomogenizedTriple(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int tVarIdx, int[] spaceIndices) {
            this.a = a; this.f = f; this.p = p; this.tVarIdx = tVarIdx; this.spaceIndices = spaceIndices;
        }
    }
    
    public static HomogenizedTriple prepareFraction(HomogenizedTriple triple) {
        long mod = (triple.f instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) triple.f.getCoefficientRing()).getMod();
        int nVars = 0;
        for (Monomial m : triple.f.getTerms().keySet()) nVars = Math.max(nVars, m.size());
        for (Monomial m : triple.a.getTerms().keySet()) nVars = Math.max(nVars, m.size());
        if (triple.tVarIdx >= 0) nVars = Math.max(nVars, triple.tVarIdx + 1);
        for (int si : triple.spaceIndices) nVars = Math.max(nVars, si + 1);
        if (nVars == 0) nVars = 1;

        IntegersZp64 ring = new IntegersZp64(mod);
        MultivariatePolynomialZp64 fZp = MultivariatePolynomialOverFp.toRingsPolynomial(triple.f, nVars, ring);
        PolynomialFactorDecomposition<MultivariatePolynomialZp64> factors = MultivariateSquareFreeFactorization.SquareFreeFactorization(fZp);
        int maxE = 0;
        for (int i = 0; i < factors.size(); i++) maxE = Math.max(maxE, factors.getExponent(i));
        if (maxE == 0) return triple;
        MultivariatePolynomialZp64 fNewZp = fZp.createOne();
        
        int qNew = triple.p * maxE;
        MultivariatePolynomialZp64 aNewZp = MultivariatePolynomialOverFp.toRingsPolynomial(triple.a, nVars, ring);
        for (int i = 0; i < factors.size(); i++) {
            int power = triple.p * (maxE - factors.getExponent(i));
            if (power > 0) {
                MultivariatePolynomialZp64 p_i = factors.get(i).clone();
                MultivariatePolynomialZp64 p_pow = fZp.createOne();
                MultivariatePolynomialZp64 base = p_i;
                int e = power;
                while (e > 0) {
                    if ((e & 1) == 1) p_pow = p_pow.multiply(base);
                    base = base.multiply(base);
                    e >>= 1;
                }
                aNewZp = aNewZp.multiply(p_pow);
            }
            fNewZp = fNewZp.multiply(factors.get(i));
        }

        return new HomogenizedTriple(
            MultivariatePolynomialOverFp.fromRingsPolynomial(aNewZp, mod),
            MultivariatePolynomialOverFp.fromRingsPolynomial(fNewZp, mod),
            qNew,
            triple.tVarIdx,
            triple.spaceIndices
        );
    }

    /**
     * 多項式 a と f を斉次化する。
     * 斉次化変数は、既存の最大変数インデックスの次（末尾）に配置される。
     * 未テスト。
     * @param a 分子多項式。
     * @param f 分母多項式。
     * @param p 極の数。
     * @param tVarIdx パラメータ t のインデックス。
     * @return 斉次化された多項式とパラメータを含む HomogenizedTriple。
     * <p>計算量: O(T * L)。</p>
     */
    public static HomogenizedTriple homogenize(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int tVarIdx) {
        int maxIdx = -1;
        for (Monomial m : f.getTerms().keySet()) for (int i = 0; i < m.size(); i++) if (m.getExponent(i) > 0) maxIdx = Math.max(maxIdx, i);
        for (Monomial m : a.getTerms().keySet()) for (int i = 0; i < m.size(); i++) if (m.getExponent(i) > 0) maxIdx = Math.max(maxIdx, i);
        maxIdx = Math.max(maxIdx, tVarIdx);
        if (maxIdx == -1) maxIdx = 0;

        int hVarIdx = maxIdx + 1;
        int newMaxIdx = hVarIdx + 1;

        int[] oldSpaceIndices = IntStream.range(0, hVarIdx).filter(i -> i != tVarIdx).toArray();
        MultivariatePolynomial<Long> F = f.homogenize(hVarIdx, oldSpaceIndices);
        MultivariatePolynomial<Long> A = a.homogenize(hVarIdx, oldSpaceIndices);

        int[] spaceIndices = IntStream.range(0, newMaxIdx).filter(i -> i != tVarIdx).toArray();
        int dim = spaceIndices.length;

        int degA_hom = A.getDegree(spaceIndices);
        int degF_hom = F.getDegree(spaceIndices);

        int N = Math.max(degF_hom, (degA_hom + dim + p - 1) / p);
        int expF = N - degF_hom;
        int expA = p * N - dim - degA_hom;

        long fMod = (f instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) f.getCoefficientRing()).getMod();
        if (expF > 0) F = F.mul(MultivariatePolynomialOverFp.singleTerm(fMod, hVarIdx, expF, 1, newMaxIdx));
        if (expA > 0) A = A.mul(MultivariatePolynomialOverFp.singleTerm(fMod, hVarIdx, expA, 1, newMaxIdx));

        return new HomogenizedTriple(A, F, p, tVarIdx, spaceIndices);
    }

    private MultivariatePolynomialOverFpFunctionField toSpatial(MultivariatePolynomial<Long> p) {
        TreeMap<Monomial, FractionFieldElement<long[]>> terms = new TreeMap<>();
        for (Map.Entry<Monomial, Long> entry : p.getTerms().entrySet()) {
            Monomial m = entry.getKey();
            int et = tVarIdx >= 0 ? m.getExponent(tVarIdx) : 0;
            int[] spatialExps = new int[maxVarIdx];
            for (int i = 0; i < m.size(); i++) if (i != tVarIdx && i < maxVarIdx) spatialExps[i] = m.getExponent(i);
            Monomial sm = new Monomial(spatialExps);
            long[] num = new long[et + 1];
            num[et] = entry.getValue();
            terms.merge(sm, field.of(num, new long[]{1}), field::add);
        }
        return new MultivariatePolynomialOverFpFunctionField(mod, terms);
    }

    /**
     * サブモジュール関係式の規格化された Grobner 基底を計算する。
     * 未テスト。
     * 事前条件: なし。
     * 事後条件: 関係式 d f \omega - \xi_i = 0 からなるサブモジュールの Grobner 基底を返す。
     * 副作用: submoduleGB に結果をキャッシュする。
     * 計算量: O(GB)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @return サブモジュールの Grobner 基底。
     */
    public List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> computeSubmoduleGB() {
        if (submoduleGB != null) return submoduleGB;
        // 1. Jacobian ideal J = (df_i) の Tagged GB を計算する
        List<MultivariatePolynomialOverFpFunctionField> df = new ArrayList<>();
        for (int idx : spaceIndices) df.add(fSpatial.differentiate(idx));
        var res = MultivariatePolynomialOverFpFunctionField.taggedGrobnerBasis(df);

        submoduleGB = new ArrayList<>();
        // 2. jacobianGB から関係式 g_j * omega = sum A_ji * xi_i を構成する
        for (MultivariatePolynomialOverFpFunctionField.TaggedPolynomial g : res.idealBasis()) {
            MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[dim];
            for (int i = 0; i < dim; i++) {
                coeffs[i] = g.coeffs[i].multiply(field.from(new long[]{mod - 1}));
            }
            submoduleGB.add(new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(g.poly, coeffs));
        }

        // 3. Syzygy basis から関係式 0 * omega + sum sigma_ki * xi_i = 0 を構成する
        submoduleGB.addAll(res.syzygyBasis());

        submoduleGB = MultivariatePolynomialOverFpFunctionField.moduleReduceGrobnerBasis(submoduleGB);
        return submoduleGB;
    }

    private List<MultivariatePolynomialOverFpFunctionField[]> computeSyzGB() {
        if (SyzGB != null) return SyzGB;
        SyzGB = new ArrayList<>();
        for (MultivariatePolynomialOverFpFunctionField.TaggedPolynomial g : computeSubmoduleGB()) {
            if (g.poly.isZero()) SyzGB.add(g.coeffs);
        }
        return SyzGB;
    }

    private List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> computeSyzPrimeGB() {
        if (SyzPrimeGB != null) return SyzPrimeGB;
        List<MultivariatePolynomialOverFpFunctionField.TaggedPolynomial> relations = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                MultivariatePolynomialOverFpFunctionField df_di = fSpatial.differentiate(spaceIndices[i]);
                MultivariatePolynomialOverFpFunctionField df_dj = fSpatial.differentiate(spaceIndices[j]);
                // df_di * xi_j - df_dj * xi_i = 0
                MultivariatePolynomialOverFpFunctionField[] coeffs = new MultivariatePolynomialOverFpFunctionField[dim];
                for (int k = 0; k < dim; k++) coeffs[k] = new MultivariatePolynomialOverFpFunctionField(mod);
                coeffs[j] = df_di;
                coeffs[i] = df_dj.multiply(field.from(new long[]{mod - 1}));
                relations.add(new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(new MultivariatePolynomialOverFpFunctionField(mod), coeffs));
            }
        }
        SyzPrimeGB = MultivariatePolynomialOverFpFunctionField.moduleGrobnerBasis(relations);
        return SyzPrimeGB;
    }

    /**
     * Griffiths-Dwork 簡約の一ステップを行う。
     * 未テスト。
     * 事前条件: alpha != null。
     * 事後条件: alpha ω と同じコホモロジー類を持つ p ω の係数 p を返す。
     * 副作用: なし。
     * 計算量: O(GB * T)。
     * 破壊的変更: なし。
     * 参照共有: 簡約不要時は戻り値 == alpha、それ以外は新規計算された多項式を返す。
     * 例外・未定義条件: alpha == null のとき NullPointerException。
     * @param alpha 入力多項式（omega の係数）。
     * @return 簡約後の多項式（omega の係数）。
     */
    public MultivariatePolynomialOverFpFunctionField redStep(MultivariatePolynomialOverFpFunctionField alpha) {
        int maxQ = (alpha.totalDegree() + dim) / degf;
        if (maxQ <= 1) return alpha;
        
        TreeMap<Monomial, FractionFieldElement<long[]>> alphaQTerms = new TreeMap<>();
        TreeMap<Monomial, FractionFieldElement<long[]>> otherTerms = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : alpha.getFractionTerms().entrySet()) {
            Monomial m = entry.getKey();
            int shiftedDegree = m.getDegree() + dim;
            if (shiftedDegree % degf == 0 && shiftedDegree / degf == maxQ) {
            	alphaQTerms.put(m, entry.getValue());
            } else {
                otherTerms.put(m, entry.getValue());
            }
        }
        
        MultivariatePolynomialOverFpFunctionField alphaQ = new MultivariatePolynomialOverFpFunctionField(mod, alphaQTerms);
        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial alphaQTagged = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(alphaQ, new MultivariatePolynomialOverFpFunctionField[dim]);
        
        var submodulegb = computeSubmoduleGB();
        var dr = MultivariatePolynomialOverFpFunctionField.moduleDivide(alphaQTagged, submodulegb);
        MultivariatePolynomialOverFpFunctionField.TaggedPolynomial rem = dr.remainder;

        MultivariatePolynomialOverFpFunctionField resPoly = rem.poly;
        FractionFieldElement<long[]> invQ = field.from(new long[]{MathUtils.modInv(maxQ - 1, mod)});

        for (int i = 0; i < dim; i++) {
            MultivariatePolynomialOverFpFunctionField beta_i = rem.coeffs[i];
            if (beta_i != null && !beta_i.isZero()) {
                MultivariatePolynomialOverFpFunctionField d_beta_i = beta_i.differentiate(spaceIndices[i]);
                resPoly = resPoly.add(d_beta_i.multiply(invQ));
            }
        }
        resPoly.addInplace(new MultivariatePolynomialOverFpFunctionField(mod, otherTerms));
        return resPoly;
    }

    private IncrementalVectorBasis<FractionFieldElement<long[]>, Monomial> computeBasisX(int r, int q) {
        String key = r + "," + q;
        if (basisXCache.containsKey(key)) return basisXCache.get(key);

        IncrementalVectorBasis<FractionFieldElement<long[]>, Monomial> basis = new IncrementalVectorBasis<>(new VectorSpaceStrategy<>(field), Collections.reverseOrder());

        if (r == 1) {
            if (SyzGB == null) SyzGB = computeSyzGB();
            if (SyzPrimeGB == null) SyzPrimeGB = computeSyzPrimeGB();
            int targetDegree = (q - 1) * degf - dim + 1;
            for (MultivariatePolynomialOverFpFunctionField[] sCoeffs : SyzGB) {
                Monomial lm = null;
                for (MultivariatePolynomialOverFpFunctionField coeff : sCoeffs) {
                    if (coeff == null || coeff.isZero()) continue;
                    Monomial coeffLm = coeff.leadingMonomial();
                    if (lm == null || lm.compareTo(coeffLm) < 0) {
                        lm = coeffLm;
                    }
                }
                if (lm == null || lm.getDegree() > targetDegree) continue;
                int degreeGap = targetDegree - lm.getDegree();
                for (int[] variables : Itertools.combinationsWithReplacement(dim, degreeGap)) {
                    int[] exponents = new int[maxVarIdx];
                    for (int variable : variables) exponents[spaceIndices[variable]]++;
                    Monomial multiplier = new Monomial(exponents);
                    MultivariatePolynomialOverFpFunctionField[] multipliedCoeffs = new MultivariatePolynomialOverFpFunctionField[sCoeffs.length];
                    for (int i = 0; i < sCoeffs.length; i++) {
                        multipliedCoeffs[i] = sCoeffs[i].multiply(field.one(), multiplier);
                    }
                    MultivariatePolynomialOverFpFunctionField.TaggedPolynomial multipliedS = new MultivariatePolynomialOverFpFunctionField.TaggedPolynomial(new MultivariatePolynomialOverFpFunctionField(mod), multipliedCoeffs);
                    if (!MultivariatePolynomialOverFpFunctionField.moduleDivide(multipliedS, SyzPrimeGB).remainder.isZero()) {
                        var sum = new MultivariatePolynomialOverFpFunctionField(mod);
                        for (int i = 0; i < multipliedCoeffs.length; i++) {
                            sum = sum.add(multipliedCoeffs[i].differentiate(spaceIndices[i]));
                        }
                        basis.add(new VectorSpaceElement<>(sum.getFractionTerms(), field));
                        if(q==4) {
                        	System.out.println(sum);
                        }
                    }
                }
            }
            
        } else {
        	basis.addAll(computeBasisX(1, q).basis());
            IncrementalVectorBasis<FractionFieldElement<long[]>, Monomial> prevXBasis = computeBasisX(r - 1, q + 1);
            for (VectorSpaceElement<FractionFieldElement<long[]>, Monomial> vsAlpha : prevXBasis.basis()) {
		TreeMap<Monomial, FractionFieldElement<long[]>> reduced = new TreeMap<>();
		for (var es : vsAlpha.val().entrySet()) {
            		if (es.getKey().getDegree() + dim == q * degf) {
            			
            		} else {
            			reduced.put(es.getKey(), es.getValue());
            		}
            	}
		MultivariatePolynomialOverFpFunctionField alpha = new MultivariatePolynomialOverFpFunctionField(mod, new TreeMap<>(vsAlpha.val()));
                if (alpha.leadingMonomial().getDegree() + dim == q * degf) {
                    Map<Monomial, FractionFieldElement<long[]>> terms=new TreeMap<>();
                    MultivariatePolynomialOverFpFunctionField red = redStep(alpha);
                    for (Map.Entry<Monomial, FractionFieldElement<long[]>> e : red.getFractionTerms().entrySet()) {
                    	terms.put(e.getKey(), e.getValue());
                    }
                    if (!terms.isEmpty()) {
                        basis.add(new VectorSpaceElement<>(terms, field));
                    }
                }
            }
        }
        basisXCache.put(key, basis);
        return basis;
    }

    private record GaussManinModularResult(long[] rho0, long[][] m, List<Monomial> basis) {}

    private static class ExponentsKey {
        final int[] exps;
        ExponentsKey(int[] exps) { this.exps = exps; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExponentsKey)) return false;
            return Arrays.equals(exps, ((ExponentsKey) o).exps);
        }
        @Override
        public int hashCode() { return Arrays.hashCode(exps); }
    }

    /**
     * 固定されたパラメータ $t=u$ における Gauss-Manin 接続行列 $m(u)$ と初期ベクトル $\rho_0(u)$ を計算する。
     * 未テスト。
     * <p>数学的仕様:
     * <ul>
     *   <li>$\rho_0(u) = [a(u)/f(u)] \in H^n(U_u)$</li>
     *   <li>$m(u)_{ij}$ は $\delta \omega_j \equiv -\sum_i m_{ij} \omega_i \pmod{d\Omega}$ を満たす行列</li>
     * </ul>
     * </p>
     * <p>事前条件: $f(u)$ が滑らかであること。$u$ において標数が十分大きいこと。</p>
     * <p>事後条件: 発見された基底に関する座標表現を返す。</p>
     * <p>副作用: なし。</p>
     * <p>計算量: $O(k \cdot GB)$ ($k$ は基底サイズ)。</p>
     * <p>破壊的変更: なし。</p>
     * <p>参照共有: なし。</p>
     * <p>例外: $f(u)$ が特異な場合、簡約が収束しない可能性がある。</p>
     * @param u パラメータ $t$ の値。
     * @param a 分子多項式 $a(x, t)$。
     * @param r 拡張簡約次数。
     * @return $\rho_0(u)$、行列 $m(u)$、および基底 $basis$ を含む GaussManinModularResult。
     */
    private GaussManinModularResult computeGaussManinConnectionModular(long u, MultivariatePolynomial<Long> a, int r) {
        long u_red = (u % mod + mod) % mod;
        MultivariatePolynomial<Long> fu = f.evaluate(tVarIdx, u_red);
        IntegersZp64 ring = new IntegersZp64(mod);
        MultivariatePolynomialZp64 fuZp = MultivariatePolynomialOverFp.toRingsPolynomial(fu, maxVarIdx, ring);

        List<TaggedMultivariatePolynomialZp64> submoduleGB = computeSubmoduleGBModularZp64(fuZp);
        List<MultivariatePolynomialZp64[]> syzGB = null;
        List<MultivariatePolynomialZp64[]> syzPrimeGB = null;
        if (r >= 1) {
            syzGB = new ArrayList<>();
            for(var g : submoduleGB) if(g.poly.isZero()) syzGB.add(g.coeffs);
            syzPrimeGB = computeSyzPrimeGBModularZp64(fuZp);
        }

        Map<String, IncrementalVectorBasis<Long, Monomial>> basisXCache = new HashMap<>();

        MultivariatePolynomial<Long> au = a.evaluate(tVarIdx, u_red);
        MultivariatePolynomialZp64 auZp = MultivariatePolynomialOverFp.toRingsPolynomial(au, maxVarIdx, ring);
        MultivariatePolynomialZp64 rho0PolyZp = reductionRZp64(auZp, r, fuZp, basisXCache, submoduleGB, syzGB, syzPrimeGB);

        MultivariatePolynomial<Long> dfdt = f.differentiate(tVarIdx).evaluate(tVarIdx, u_red);
        MultivariatePolynomialZp64 dfdtZp = MultivariatePolynomialOverFp.toRingsPolynomial(dfdt, maxVarIdx, ring);

        Map<ExponentsKey, MonomialZp64> basisSet = new LinkedHashMap<>();
        List<MultivariatePolynomialZp64> images = new ArrayList<>();
        Queue<MonomialZp64> frontier = new LinkedList<>();
        for(MonomialZp64 m : rho0PolyZp) frontier.add(m);

        while (!frontier.isEmpty()) {
            MonomialZp64 m = frontier.poll();
            ExponentsKey key = new ExponentsKey(m.exponents);
            if (basisSet.containsKey(key)) continue;
            basisSet.put(key, new MonomialZp64(m.exponents, 1L));

            MultivariatePolynomialZp64 form = fuZp.createZero().add(new MonomialZp64(m.exponents, 1L));
            MultivariatePolynomialZp64 img = reductionRZp64(computeDeltaAtUZp64(form, dfdtZp), r, fuZp, basisXCache, submoduleGB, syzGB, syzPrimeGB);
            images.add(img);

            for (MonomialZp64 newM : img) {
                if (!basisSet.containsKey(new ExponentsKey(newM.exponents))) {
                    frontier.add(newM);
                }
            }
        }

        List<MonomialZp64> basisList = new ArrayList<>(basisSet.values());
        Map<ExponentsKey, Integer> basisIndexMap = new HashMap<>();
        for(int i=0; i<basisList.size(); i++) basisIndexMap.put(new ExponentsKey(basisList.get(i).exponents), i);

        int k = basisList.size();
        long[] rho0Vec = extractVectorZp64(rho0PolyZp, basisIndexMap);
        long[][] m = new long[k][k];
        for (int j = 0; j < k; j++) {
            MultivariatePolynomialZp64 img = images.get(j);
            long[] col = extractVectorZp64(img, basisIndexMap);
            for (int i = 0; i < k; i++) m[i][j] = (mod - col[i]) % mod;
        }

        return new GaussManinModularResult(rho0Vec, m, convertToMonomialList(basisList));
    }

    private List<Monomial> convertToMonomialList(List<cc.redberry.rings.poly.multivar.MonomialZp64> basisList) {
        List<Monomial> res = new ArrayList<>();
        for(var m : basisList) res.add(new Monomial(m.exponents));
        return res;
    }

    private VectorSpaceElement<Long, Monomial> toVectorSpaceZp64(MultivariatePolynomialZp64 p, FpStrategy strategy) {
        Map<Monomial, Long> terms = new HashMap<>();
        for (MonomialZp64 m : p) {
            if (m.coefficient != 0) {
                terms.put(new Monomial(m.exponents), m.coefficient);
            }
        }
        return new VectorSpaceElement<>(terms, strategy);
    }

    private MultivariatePolynomialZp64 fromVectorSpaceZp64(VectorSpaceElement<Long, Monomial> vs, MultivariatePolynomialZp64 factory) {
        MultivariatePolynomialZp64 res = factory.createZero();
        for (Map.Entry<Monomial, Long> entry : vs.val().entrySet()) {
            res.add(new MonomialZp64(entry.getKey().exponents(), entry.getValue()));
        }
        return res;
    }

    private long[] extractVectorZp64(MultivariatePolynomialZp64 rho, Map<ExponentsKey, Integer> basisIndexMap) {
        long[] vec = new long[basisIndexMap.size()];
        for (MonomialZp64 m : rho) {
            Integer idx = basisIndexMap.get(new ExponentsKey(m.exponents));
            if (idx != null) {
                vec[idx] = m.coefficient;
            }
        }
        return vec;
    }

    private MultivariatePolynomialZp64 computeDeltaAtUZp64(MultivariatePolynomialZp64 alpha, MultivariatePolynomialZp64 dfdtZp) {
        MultivariatePolynomialZp64 res = alpha.createZero();
        for (MonomialZp64 m : alpha) {
            int q = (m.totalDegree + dim) / degf;
            long multiplier = (mod - (long)(q % mod)) % mod;
            if (multiplier == 0) continue;
            res.add(dfdtZp.clone().multiply(m).multiply(multiplier));
        }
        return res;
    }

    private MultivariatePolynomialOverFpFunctionField reductionR(MultivariatePolynomialOverFpFunctionField alpha, int r) {
        MultivariatePolynomialOverFpFunctionField current = alpha;
        int cnt = 0;
        int lastMaxQ = Integer.MAX_VALUE;
        while (true) {
        	if (++cnt >= 100) throw new AssertionError("Loop in reductionR");
            if (degf == 0) break;
            int maxQ = -1;
            for (Monomial m : current.getFractionTerms().keySet()) {
                if ((m.getDegree() + dim) % degf == 0) maxQ = Math.max(maxQ, (m.getDegree() + dim) / degf);
            }
            if (maxQ <= 1 || maxQ >= lastMaxQ) break;
            lastMaxQ = maxQ;
            TreeMap<Monomial, FractionFieldElement<long[]>> aQTerms = new TreeMap<>();
            TreeMap<Monomial, FractionFieldElement<long[]>> otherTerms = new TreeMap<>();
            for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : current.getFractionTerms().entrySet()) {
                Monomial m = entry.getKey();
                if ((m.getDegree() + dim) % degf == 0 && (m.getDegree() + dim) / degf == maxQ) aQTerms.put(m, entry.getValue());
                else otherTerms.put(m, entry.getValue());
            }
            MultivariatePolynomialOverFpFunctionField redA = redStep(new MultivariatePolynomialOverFpFunctionField(mod, aQTerms));
            IncrementalVectorBasis<FractionFieldElement<long[]>, Monomial> Xq = computeBasisX(r, maxQ);
            VectorSpaceElement<FractionFieldElement<long[]>, Monomial> redAVS = new VectorSpaceElement<>(redA.getFractionTerms(), field);
            VectorSpaceElement<FractionFieldElement<long[]>, Monomial> finalRedVS = Xq.reduce(redAVS);
            MultivariatePolynomialOverFpFunctionField finalRed = new MultivariatePolynomialOverFpFunctionField(mod, new TreeMap<>(finalRedVS.val()));
            current = new MultivariatePolynomialOverFpFunctionField(mod, otherTerms).add(finalRed);
        }
        return current;
    }

    /**
     * Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * 事前条件: a, f != null. tVarIdx はパラメータ t のインデックス。 r は簡約の拡張次数。
     * 事後条件: Picard-Fuchs 演算子の係数多項式リストを返す。
     * 副作用: なし。
     * 計算量: O(ord * r * GB)。 (ord は微分方程式の階数)
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param a 分子。
     * @param f 分母。
     * @param tVarIdx t のインデックス。
     * @param r 拡張簡約の次数。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computePicardFuchs(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int tVarIdx, int r) {
        return computePicardFuchs(a, f, 1, tVarIdx, r);
    }

    /**
     * Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * 事前条件: a, f != null. tVarIdx はパラメータ t のインデックス。 r は簡約の拡張次数。
     * 事後条件: Picard-Fuchs 演算子の係数多項式リストを返す。
     * 副作用: なし。
     * 計算量: O(ord * r * GB)。 (ord は微分方程式の階数)
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param a 分子。
     * @param f 分母。
     * @param p 分母の指数（極の位数）。
     * @param tVarIdx t のインデックス。
     * @param r 拡張簡約の次数。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computePicardFuchs(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int tVarIdx, int r) {
        HomogenizedTriple hom = homogenize(a, f, p, tVarIdx);
        HomogenizedTriple triple = prepareFraction(hom);
        RationalIntegralReduction reduction = new RationalIntegralReduction(triple.f, triple.spaceIndices, triple.tVarIdx);
        return reduction.computePicardFuchsInternal(triple.a, r);
    }

    /**
     * 行列を用いたアルゴリズムにより Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * <p>数学的仕様:
     * 周期積分 $I(t) = \oint \frac{a(x, t)}{f(x, t)} dx$ が満たす微分方程式 $L I(t) = 0$ の作用素 $L = \sum_i a_i(t) \partial_t^i$ を求める。
     * </p>
     * <p>事前条件: $a, f \neq null$。$f$ は $t$ に関して微分可能。</p>
     * <p>事後条件: Picard-Fuchs 演算子の係数多項式リスト $[a_0(t), a_1(t), \dots]$ を返す。</p>
     * <p>副作用: なし。</p>
     * <p>計算量: $O(N \cdot (k^2 \cdot GB + k^3))$ ($N$ は評価点数, $k$ はコホモロジー次元)。</p>
     * <p>破壊的変更: なし。</p>
     * <p>参照共有: なし。</p>
     * <p>例外: 線形従属関係が見つからない場合は null を返す。</p>
     * @param a 分子 $a(x, t)$。
     * @param f 分母 $f(x, t)$。
     * @param tVarIdx パラメータ $t$ のインデックス。
     * @param r 拡張簡約の次数。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computePicardFuchsMatrix(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int tVarIdx, int r) {
        return computePicardFuchsMatrix(a, f, 1, tVarIdx, r);
    }

    /**
     * 行列を用いたアルゴリズムにより Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * @param a 分子 $a(x, t)$。
     * @param f 分母 $f(x, t)$。
     * @param p 分母の指数（極の位数）。
     * @param tVarIdx パラメータ $t$ のインデックス。
     * @param r 拡張簡約の次数。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computePicardFuchsMatrix(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int tVarIdx, int r) {
        HomogenizedTriple hom = homogenize(a, f, p, tVarIdx);
        HomogenizedTriple triple = prepareFraction(hom);
        RationalIntegralReduction reduction = new RationalIntegralReduction(triple.f, triple.spaceIndices, triple.tVarIdx);
        return reduction.computePicardFuchsMatrixInternal(triple.a, r);
    }

    private List<long[]> computePicardFuchsMatrixInternal(MultivariatePolynomial<Long> aFinal, int r) {
    	Random rnd = new Random(42);
        List<Long> points = new ArrayList<>();
        List<long[]> rho0Values = new ArrayList<>();
        List<long[][]> mValues = new ArrayList<>();
        List<Monomial> basis = null;

        FractionFieldElement<long[]>[] rho0Reconstructed = null;
        FractionFieldElement<long[]>[][] mReconstructed = null;

        for (int step = 0; step < 100; step++) {
        	System.out.println("step"+step);
        	long u = (rnd.nextLong() & Long.MAX_VALUE) % mod;
            GaussManinModularResult res = computeGaussManinConnectionModular(u, aFinal, r);
            if (basis == null) {
                basis = res.basis;
            } else if (!basis.equals(res.basis)) {
                // If basis changes, we might need a more robust strategy.
                // For now, we follow the user's assumption that mod is large enough.
                // We only use points that have the same basis as the first point.
                continue;
            }
            points.add(u);
            rho0Values.add(res.rho0);
            mValues.add(res.m);

            if (points.size() > 5 && points.size() % 5 == 0) {
                rho0Reconstructed = reconstructVector(points, rho0Values);
                mReconstructed = reconstructMatrix(points, mValues);
                if (rho0Reconstructed != null && mReconstructed != null) {
                    // Validate with one more point
                    long uVal = (rnd.nextLong() & Long.MAX_VALUE) % mod;
                    GaussManinModularResult valRes = computeGaussManinConnectionModular(uVal, aFinal, r);
                    if (basis.equals(valRes.basis) && validateVector(rho0Reconstructed, uVal, valRes.rho0) && validateMatrix(mReconstructed, uVal, valRes.m)) {
                        break;
                    }
                }
            }
        }

        if (rho0Reconstructed == null || mReconstructed == null) return null;
        int k = basis.size();
        if (k == 0) return List.of(new long[]{1});

        List<List<FractionFieldElement<long[]>>> rhos = new ArrayList<>();
        rhos.add(Arrays.asList(rho0Reconstructed));

        for (int i = 0; i < 100; i++) {
            List<long[]> dependency = findLinearDependencyVectors(rhos, basis);
            if (dependency != null) return dependency;

            List<FractionFieldElement<long[]>> currentRho = rhos.get(i);
            List<FractionFieldElement<long[]>> nextRho = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                // rho_{i+1} = rho_i' - m(t) * rho_i
                FractionFieldElement<long[]> der = differentiateRational(currentRho.get(j));
                FractionFieldElement<long[]> sum = field.zero();
                for (int l = 0; l < k; l++) {
                    sum = field.add(sum, field.mul(mReconstructed[j][l], currentRho.get(l)));
                }
                nextRho.add(field.sub(der, sum));
            }
            rhos.add(nextRho);
        }
        return null;
    }

    private FractionFieldElement<long[]>[] reconstructVector(List<Long> points, List<long[]> values) {
        int k = values.get(0).length;
        long[] x = new long[points.size()];
        for (int i = 0; i < x.length; i++) x[i] = points.get(i);
        @SuppressWarnings("unchecked")
        FractionFieldElement<long[]>[] res = new FractionFieldElement[k];
        for (int i = 0; i < k; i++) {
            long[] y = new long[points.size()];
            for (int j = 0; j < y.length; j++) y[j] = values.get(j)[i];
            long[][] ci = poly1d.cauchyInterpolation(x, y, x.length / 2);
            if (ci == null) return null;
            res[i] = field.of(ci[0], ci[1]);
        }
        return res;
    }

    private FractionFieldElement<long[]>[][] reconstructMatrix(List<Long> points, List<long[][]> values) {
        int k = values.get(0).length;
        long[] x = new long[points.size()];
        for (int i = 0; i < x.length; i++) x[i] = points.get(i);
        @SuppressWarnings("unchecked")
        FractionFieldElement<long[]>[][] res = new FractionFieldElement[k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                long[] y = new long[points.size()];
                for (int l = 0; l < y.length; l++) y[l] = values.get(l)[i][j];
                long[][] ci = poly1d.cauchyInterpolation(x, y, x.length / 2);
                if (ci == null) return null;
                res[i][j] = field.of(ci[0], ci[1]);
            }
        }
        return res;
    }

    private MultivariatePolynomialZp64 reductionRZp64(MultivariatePolynomialZp64 alpha, int r, MultivariatePolynomialZp64 fZp, Map<String, IncrementalVectorBasis<Long, Monomial>> cache, List<TaggedMultivariatePolynomialZp64> submoduleGB, List<MultivariatePolynomialZp64[]> syzGB, List<MultivariatePolynomialZp64[]> syzPrimeGB) {
        MultivariatePolynomialZp64 current = alpha.clone();
        int cnt = 0;
        int lastMaxQ = Integer.MAX_VALUE;
        FpStrategy strategy = new FpStrategy(fZp.ring.modulus);
        while (true) {
            if (++cnt >= 100) throw new AssertionError("Loop in reductionR");
            if (degf == 0) break;
            int maxQ = -1;
            for (cc.redberry.rings.poly.multivar.MonomialZp64 m : current) {
                if ((m.totalDegree + dim) % degf == 0) maxQ = Math.max(maxQ, (int) ((m.totalDegree + dim) / degf));
            }
            if (maxQ <= 1 || maxQ >= lastMaxQ) break;
            lastMaxQ = maxQ;

            MultivariatePolynomialZp64 aQPoly = current.createZero();
            MultivariatePolynomialZp64 otherPoly = current.createZero();
            for (MonomialZp64 m : current) {
                if ((m.totalDegree + dim) % degf == 0 && (m.totalDegree + dim) / degf == maxQ) {
                    aQPoly.add(m);
                } else {
                    otherPoly.add(m);
                }
            }

            MultivariatePolynomialZp64 redA = redStepModularZp64(aQPoly, submoduleGB);
            if (r >= 1) {
                IncrementalVectorBasis<Long, Monomial> Xq = computeBasisXModularZp64(r, maxQ, fZp, cache, submoduleGB, syzGB, syzPrimeGB);
                VectorSpaceElement<Long, Monomial> redAVS = toVectorSpaceZp64(redA, strategy);
                VectorSpaceElement<Long, Monomial> finalRedVS = Xq.reduce(redAVS);
                MultivariatePolynomialZp64 finalRed = fromVectorSpaceZp64(finalRedVS, fZp);
                current = otherPoly.add(finalRed);
            } else {
                current = otherPoly.add(redA);
            }
        }
        return current;
    }

    private boolean validateVector(FractionFieldElement<long[]>[] reconstructed, long u, long[] values) {
        for (int i = 0; i < reconstructed.length; i++) {
            long val = poly1d.eval(reconstructed[i].num(), u) * MathUtils.modInv(poly1d.eval(reconstructed[i].den(), u), mod) % mod;
            if (val != values[i]) return false;
        }
        return true;
    }

    private boolean validateMatrix(FractionFieldElement<long[]>[][] reconstructed, long u, long[][] values) {
        for (int i = 0; i < reconstructed.length; i++) {
            for (int j = 0; j < reconstructed[i].length; j++) {
                long val = poly1d.eval(reconstructed[i][j].num(), u) * MathUtils.modInv(poly1d.eval(reconstructed[i][j].den(), u), mod) % mod;
                if (val != values[i][j]) return false;
            }
        }
        return true;
    }

    private FractionFieldElement<long[]> differentiateRational(FractionFieldElement<long[]> f) {
        long[] num = f.num();
        long[] den = f.den();
        long[] dNum = poly1d.diff(num, 1);
        long[] dDen = poly1d.diff(den, 1);
        long[] term1 = poly1d.mul(dNum, den);
        long[] term2 = poly1d.mul(num, dDen);
        long[] newNum = poly1d.sub(term1, term2);
        long[] newDen = poly1d.mul(den, den);
        return field.of(newNum, newDen);
    }

    private List<long[]> findLinearDependencyVectors(List<List<FractionFieldElement<long[]>>> rhos, List<Monomial> basis) {
        List<MultivariatePolynomialOverFpFunctionField> rhosPoly = new ArrayList<>();
        for (List<FractionFieldElement<long[]>> rho : rhos) {
            TreeMap<Monomial, FractionFieldElement<long[]>> terms = new TreeMap<>();
            for (int i = 0; i < basis.size(); i++) {
                terms.put(basis.get(i), rho.get(i));
            }
            rhosPoly.add(new MultivariatePolynomialOverFpFunctionField(mod, terms));
        }
        return findLinearDependencySymbolicByExact(rhosPoly);
    }

    /**
     * 有理関数 a/f^p の対角成分 diag(a/f^p)=[Πx_i^n] a/f^p の Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * <p>数学的仕様:
     * $I(t) = \sum_{k=0}^\infty [x^k] (a/f^p) t^k$ が満たす微分方程式を計算する。
     * これは $n+1$ 変数の有理関数の周期積分の Picard-Fuchs 方程式に帰着される。
     * </p>
     * 事前条件: f の定数項は非ゼロであること。
     * 事後条件: 対角成分が満たす微分方程式の係数多項式リスト $[a_0(t), a_1(t), \dots]$ を返す。
     * 副作用: なし。
     * 計算量: O(DiagTrans + PicardFuchs)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param a 分子多項式 a(x)。
     * @param f 分母多項式 f(x)。
     * @param p 分母の指数（極の位数）。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computeDiagonalPicardFuchs(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p) {
        HomogenizedTriple triple = transformForDiagonal(a, f, p);
        return computePicardFuchs(triple.a, triple.f, triple.p, triple.tVarIdx, 1);
    }

    /**
     * 行列を用いたアルゴリズムにより、対角成分の Picard-Fuchs 方程式を計算する。
     * 未テスト。
     * <p>数学的仕様:
     * 有理関数 $a/f^p$ の対角項 $\text{diag}(a/f^p)$ が満たす Picard-Fuchs 方程式を求める。
     * </p>
     * 事前条件: $a, f \neq null$。$f$ の定数項は非ゼロであること。
     * 事後条件: Picard-Fuchs 方程式の係数多項式リストを返す。
     * 副作用: なし。
     * 計算量: $O(\text{DiagTrans} + \text{PicardFuchsMatrix})$。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param a 分子多項式 $a(x)$。
     * @param f 分母多項式 $f(x)$。
     * @param p 分母の指数（極の位数）。
     * @return 係数多項式のリスト。
     */
    public static List<long[]> computeDiagonalPicardFuchsMatrix(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p) {
        HomogenizedTriple triple = transformForDiagonal(a, f, p);
        return computePicardFuchsMatrix(triple.a, triple.f, triple.p, triple.tVarIdx, 1);
    }
    
    private static class TaggedMultivariatePolynomialZp64 {
        final MultivariatePolynomialZp64 poly;
        final MultivariatePolynomialZp64[] coeffs;

        TaggedMultivariatePolynomialZp64(MultivariatePolynomialZp64 poly, MultivariatePolynomialZp64[] coeffs) {
            this.poly = poly;
            this.coeffs = coeffs;
        }

        TaggedMultivariatePolynomialZp64 subtract(TaggedMultivariatePolynomialZp64 other) {
            int n = Math.max(coeffs.length, other.coeffs.length);
            MultivariatePolynomialZp64[] nextCoeffs = new MultivariatePolynomialZp64[n];
            for (int i = 0; i < n; i++) {
                MultivariatePolynomialZp64 c1 = (i < coeffs.length && coeffs[i] != null) ? coeffs[i] : poly.createZero();
                MultivariatePolynomialZp64 c2 = (i < other.coeffs.length && other.coeffs[i] != null) ? other.coeffs[i] : poly.createZero();
                nextCoeffs[i] = c1.clone().subtract(c2);
            }
            return new TaggedMultivariatePolynomialZp64(poly.clone().subtract(other.poly), nextCoeffs);
        }

        TaggedMultivariatePolynomialZp64 multiply(long scalar) {
            MultivariatePolynomialZp64[] nextCoeffs = new MultivariatePolynomialZp64[coeffs.length];
            for (int i = 0; i < coeffs.length; i++) {
                nextCoeffs[i] = coeffs[i].clone().multiply(scalar);
            }
            return new TaggedMultivariatePolynomialZp64(poly.clone().multiply(scalar), nextCoeffs);
        }

        TaggedMultivariatePolynomialZp64 multiply(cc.redberry.rings.poly.multivar.MonomialZp64 m, long scalar) {
            MultivariatePolynomialZp64[] nextCoeffs = new MultivariatePolynomialZp64[coeffs.length];
            for (int i = 0; i < coeffs.length; i++) {
                nextCoeffs[i] = (coeffs[i] == null) ? poly.createZero() : coeffs[i].clone().multiply(m).multiply(scalar);
            }
            return new TaggedMultivariatePolynomialZp64(poly.clone().multiply(m).multiply(scalar), nextCoeffs);
        }

        boolean isZero() {
            if (!poly.isZero()) return false;
            for (MultivariatePolynomialZp64 c : coeffs) {
                if (c != null && !c.isZero()) return false;
            }
            return true;
        }

        Object[] leadingModuleMonomial() {
            if (!poly.isZero()) return new Object[]{0, poly.lt()};
            for (int i = 0; i < coeffs.length; i++) {
                if (coeffs[i] != null && !coeffs[i].isZero()) {
                    return new Object[]{i + 1, coeffs[i].lt()};
                }
            }
            return null;
        }

        long leadingModuleCoefficient() {
            if (!poly.isZero()) return poly.lc();
            for (MultivariatePolynomialZp64 c : coeffs) {
                if (c != null && !c.isZero()) return c.lc();
            }
            return 0;
        }
    }

    private static class TaggedMultivariateDivRemZp64 {
        final TaggedMultivariatePolynomialZp64 remainder;
        TaggedMultivariateDivRemZp64(List<MultivariatePolynomialZp64> quotients, TaggedMultivariatePolynomialZp64 remainder) {
            this.remainder = remainder;
        }
    }

    private static boolean isDivisibleZp64(cc.redberry.rings.poly.multivar.MonomialZp64 m1, cc.redberry.rings.poly.multivar.MonomialZp64 m2) {
        for (int i = 0; i < Math.max(m1.exponents.length, m2.exponents.length); i++) {
            int e1 = i < m1.exponents.length ? m1.exponents[i] : 0;
            int e2 = i < m2.exponents.length ? m2.exponents[i] : 0;
            if (e1 < e2) return false;
        }
        return true;
    }

    private static cc.redberry.rings.poly.multivar.MonomialZp64 divideZp64(cc.redberry.rings.poly.multivar.MonomialZp64 m1, cc.redberry.rings.poly.multivar.MonomialZp64 m2) {
        int n = Math.max(m1.exponents.length, m2.exponents.length);
        int[] diff = new int[n];
        for (int i = 0; i < n; i++) {
            int e1 = i < m1.exponents.length ? m1.exponents[i] : 0;
            int e2 = i < m2.exponents.length ? m2.exponents[i] : 0;
            diff[i] = e1 - e2;
        }
        return new cc.redberry.rings.poly.multivar.MonomialZp64(diff, 1L);
    }

    private static cc.redberry.rings.poly.multivar.MonomialZp64 lcmZp64(cc.redberry.rings.poly.multivar.MonomialZp64 m1, cc.redberry.rings.poly.multivar.MonomialZp64 m2) {
        int n = Math.max(m1.exponents.length, m2.exponents.length);
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            int e1 = i < m1.exponents.length ? m1.exponents[i] : 0;
            int e2 = i < m2.exponents.length ? m2.exponents[i] : 0;
            res[i] = Math.max(e1, e2);
        }
        return new cc.redberry.rings.poly.multivar.MonomialZp64(res, 1L);
    }

    private static TaggedMultivariateDivRemZp64 moduleDivideModularZp64(TaggedMultivariatePolynomialZp64 f, List<TaggedMultivariatePolynomialZp64> gs) {
        int m = gs.size();
        int nComps = f.coeffs.length;
        for (TaggedMultivariatePolynomialZp64 g : gs) nComps = Math.max(nComps, g.coeffs.length);
        List<MultivariatePolynomialZp64> qs = new ArrayList<>();
        for (int i = 0; i < m; i++) qs.add(f.poly.createZero());

        MultivariatePolynomialZp64[] pCoeffs = new MultivariatePolynomialZp64[nComps];
        for (int i = 0; i < nComps; i++) pCoeffs[i] = (i < f.coeffs.length && f.coeffs[i] != null) ? f.coeffs[i].clone() : f.poly.createZero();
        TaggedMultivariatePolynomialZp64 p = new TaggedMultivariatePolynomialZp64(f.poly.clone(), pCoeffs);
        long mod = f.poly.ring.modulus;

        TaggedMultivariatePolynomialZp64 remainder = new TaggedMultivariatePolynomialZp64(f.poly.createZero(), new MultivariatePolynomialZp64[nComps]);
        for (int i = 0; i < nComps; i++) remainder.coeffs[i] = f.poly.createZero();

        while (!p.isZero()) {
            Object[] lmP = p.leadingModuleMonomial();
            int idxP = (Integer) lmP[0];
            cc.redberry.rings.poly.multivar.MonomialZp64 mP = (cc.redberry.rings.poly.multivar.MonomialZp64) lmP[1];
            boolean divided = false;
            for (int i = 0; i < m; i++) {
                Object[] lmG = gs.get(i).leadingModuleMonomial();
                if (lmG == null) continue;
                int idxG = (Integer) lmG[0];
                cc.redberry.rings.poly.multivar.MonomialZp64 mG = (cc.redberry.rings.poly.multivar.MonomialZp64) lmG[1];

                if (idxP == idxG && isDivisibleZp64(mP, mG)) {
                    cc.redberry.rings.poly.multivar.MonomialZp64 factorM = divideZp64(mP, mG);
                    long factorC = (p.leadingModuleCoefficient() * MathUtils.modInv(gs.get(i).leadingModuleCoefficient(), mod)) % mod;
                    MultivariatePolynomialZp64 step = f.poly.createZero().add(new cc.redberry.rings.poly.multivar.MonomialZp64(factorM.exponents, factorC));
                    qs.set(i, qs.get(i).add(step));
                    p = p.subtract(gs.get(i).multiply(factorM, factorC));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                long lc = p.leadingModuleCoefficient();
                cc.redberry.rings.poly.multivar.MonomialZp64 term = new cc.redberry.rings.poly.multivar.MonomialZp64(mP.exponents, lc);
                if (idxP == 0) {
                    remainder.poly.add(term);
                    p.poly.subtract(term);
                } else {
                    remainder.coeffs[idxP - 1].add(term);
                    p.coeffs[idxP - 1].subtract(term);
                }
            }
        }
        return new TaggedMultivariateDivRemZp64(qs, remainder);
    }

    private static List<TaggedMultivariatePolynomialZp64> moduleGrobnerBasisModularZp64(List<TaggedMultivariatePolynomialZp64> fs) {
        if (fs.isEmpty()) return new ArrayList<>();
        long mod = fs.get(0).poly.ring.modulus;

        List<TaggedMultivariatePolynomialZp64> g = new ArrayList<>();
        PriorityQueue<ModulePairZp64> pairs = new PriorityQueue<>();

        for (TaggedMultivariatePolynomialZp64 f : fs) {
            TaggedMultivariatePolynomialZp64 r = moduleDivideModularZp64(f, g).remainder;
            if (!r.isZero()) {
                addNewElementZp64(g, pairs, r, mod);
            }
        }

        while (!pairs.isEmpty()) {
            ModulePairZp64 pair = pairs.poll();
            TaggedMultivariatePolynomialZp64 s = moduleSPolynomialModularZp64(g.get(pair.i), g.get(pair.j), mod);
            if (s == null) continue;
            TaggedMultivariatePolynomialZp64 r = moduleDivideModularZp64(s, g).remainder;
            if (!r.isZero()) {
                addNewElementZp64(g, pairs, r, mod);
            }
        }
        return moduleReduceGrobnerBasisModularZp64(g);
    }

    private static void addNewElementZp64(List<TaggedMultivariatePolynomialZp64> G, PriorityQueue<ModulePairZp64> pairs, TaggedMultivariatePolynomialZp64 r, long mod) {
        long lcInv = MathUtils.modInv(r.leadingModuleCoefficient(), mod);
        TaggedMultivariatePolynomialZp64 monicR = r.multiply(lcInv);
        Object[] lmNew = monicR.leadingModuleMonomial();
        int idxNew = (Integer) lmNew[0];
        cc.redberry.rings.poly.multivar.MonomialZp64 mNew = (cc.redberry.rings.poly.multivar.MonomialZp64) lmNew[1];

        int newIdx = G.size();
        for (int i = 0; i < G.size(); i++) {
            Object[] lmI = G.get(i).leadingModuleMonomial();
            if (lmI == null) continue;
            int idxI = (Integer) lmI[0];
            cc.redberry.rings.poly.multivar.MonomialZp64 mI = (cc.redberry.rings.poly.multivar.MonomialZp64) lmI[1];
            if (idxI == idxNew) {
                pairs.add(new ModulePairZp64(i, newIdx, mI, mNew));
            }
        }
        G.add(monicR);
    }

    private static class ModulePairZp64 implements Comparable<ModulePairZp64> {
        final int i, j;
        final cc.redberry.rings.poly.multivar.MonomialZp64 lcm;
        ModulePairZp64(int i, int j, cc.redberry.rings.poly.multivar.MonomialZp64 m1, cc.redberry.rings.poly.multivar.MonomialZp64 m2) {
            this.i = i; this.j = j;
            this.lcm = lcmZp64(m1, m2);
        }
        @Override
        public int compareTo(ModulePairZp64 o) {
            // Need a valid comparison for MonomialZp64. Rings uses orders.
            // For Buchberger, grevlex is fine. MonomialZp64 doesn't implement Comparable directly in a way we want.
            // Let's use total degree and then some tie-break.
            if (this.lcm.totalDegree != o.lcm.totalDegree) return Integer.compare(this.lcm.totalDegree, o.lcm.totalDegree);
            return Integer.compare(this.lcm.hashCode(), o.lcm.hashCode());
        }
    }

    private static TaggedMultivariatePolynomialZp64 moduleSPolynomialModularZp64(TaggedMultivariatePolynomialZp64 f1, TaggedMultivariatePolynomialZp64 f2, long mod) {
        Object[] lm1 = f1.leadingModuleMonomial();
        Object[] lm2 = f2.leadingModuleMonomial();
        if (lm1 == null || lm2 == null) return null;
        int idx1 = (Integer) lm1[0];
        int idx2 = (Integer) lm2[0];
        if (idx1 != idx2) return null;

        cc.redberry.rings.poly.multivar.MonomialZp64 m1 = (cc.redberry.rings.poly.multivar.MonomialZp64) lm1[1];
        cc.redberry.rings.poly.multivar.MonomialZp64 m2 = (cc.redberry.rings.poly.multivar.MonomialZp64) lm2[1];
        cc.redberry.rings.poly.multivar.MonomialZp64 lcm = lcmZp64(m1, m2);

        cc.redberry.rings.poly.multivar.MonomialZp64 factor1 = divideZp64(lcm, m1);
        cc.redberry.rings.poly.multivar.MonomialZp64 factor2 = divideZp64(lcm, m2);

        long c1 = MathUtils.modInv(f1.leadingModuleCoefficient(), mod);
        long c2 = MathUtils.modInv(f2.leadingModuleCoefficient(), mod);

        return f1.multiply(factor1, c1).subtract(f2.multiply(factor2, c2));
    }

    private static List<TaggedMultivariatePolynomialZp64> moduleReduceGrobnerBasisModularZp64(List<TaggedMultivariatePolynomialZp64> g) {
        if (g.isEmpty()) return g;
        List<TaggedMultivariatePolynomialZp64> res = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            boolean redundant = false;
            Object[] lmI = g.get(i).leadingModuleMonomial();
            if (lmI == null) { redundant = true; }
            else {
                int idxI = (Integer) lmI[0];
                cc.redberry.rings.poly.multivar.MonomialZp64 mI = (cc.redberry.rings.poly.multivar.MonomialZp64) lmI[1];
                for (int j = 0; j < g.size(); j++) {
                    if (i == j) continue;
                    Object[] lmJ = g.get(j).leadingModuleMonomial();
                    if (lmJ == null) continue;
                    int idxJ = (Integer) lmJ[0];
                    cc.redberry.rings.poly.multivar.MonomialZp64 mJ = (cc.redberry.rings.poly.multivar.MonomialZp64) lmJ[1];
                    if (idxI == idxJ && isDivisibleZp64(mI, mJ)) {
                        if (!mI.equals(mJ) || j < i) {
                            redundant = true;
                            break;
                        }
                    }
                }
            }
            if (!redundant) res.add(g.get(i));
        }
        for (int i = 0; i < res.size(); i++) {
            TaggedMultivariatePolynomialZp64 f = res.get(i);
            long lcInv = MathUtils.modInv(f.leadingModuleCoefficient(), f.poly.ring.modulus);
            res.set(i, f.multiply(lcInv));
        }
        return res;
    }

    private List<TaggedMultivariatePolynomialZp64> computeSubmoduleGBModularZp64(MultivariatePolynomialZp64 fZp) {
        long mod = fZp.ring.modulus;
        List<MultivariatePolynomialZp64> df = new ArrayList<>();
        for (int idx : spaceIndices) df.add(fZp.derivative(idx));

        var res = taggedGrobnerBasisModularZp64(df);
        List<TaggedMultivariatePolynomialZp64> submoduleGB = new ArrayList<>();

        for (TaggedMultivariatePolynomialZp64 g : res.idealBasis()) {
            MultivariatePolynomialZp64[] coeffs = new MultivariatePolynomialZp64[dim];
            for (int i = 0; i < dim; i++) {
                coeffs[i] = g.coeffs[i].clone().multiply(mod - 1);
            }
            submoduleGB.add(new TaggedMultivariatePolynomialZp64(g.poly, coeffs));
        }
        submoduleGB.addAll(res.syzygyBasis());

        return moduleReduceGrobnerBasisModularZp64(submoduleGB);
    }

    private static TaggedGrobnerBasisResultZp64 taggedGrobnerBasisModularZp64(List<MultivariatePolynomialZp64> fs) {
        if (fs.isEmpty()) return new TaggedGrobnerBasisResultZp64(new ArrayList<>(), new ArrayList<>());
        int n = fs.size();
        List<TaggedMultivariatePolynomialZp64> taggedInputs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            MultivariatePolynomialZp64 f = fs.get(i);
            MultivariatePolynomialZp64[] coeffs = new MultivariatePolynomialZp64[n];
            for (int j = 0; j < n; j++) coeffs[j] = f.createZero();
            coeffs[i] = f.createOne();
            taggedInputs.add(new TaggedMultivariatePolynomialZp64(f, coeffs));
        }
        List<TaggedMultivariatePolynomialZp64> moduleGb = moduleGrobnerBasisModularZp64(taggedInputs);
        List<TaggedMultivariatePolynomialZp64> idealBasis = new ArrayList<>();
        List<TaggedMultivariatePolynomialZp64> syzygyBasis = new ArrayList<>();
        for (TaggedMultivariatePolynomialZp64 g : moduleGb) {
            if (!g.poly.isZero()) idealBasis.add(g);
            else syzygyBasis.add(g);
        }
        return new TaggedGrobnerBasisResultZp64(idealBasis, syzygyBasis);
    }

    private List<MultivariatePolynomialZp64[]> computeSyzPrimeGBModularZp64(MultivariatePolynomialZp64 fZp) {
        long mod = fZp.ring.modulus;
        List<TaggedMultivariatePolynomialZp64> relations = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                MultivariatePolynomialZp64 df_di = fZp.derivative(spaceIndices[i]);
                MultivariatePolynomialZp64 df_dj = fZp.derivative(spaceIndices[j]);
                MultivariatePolynomialZp64[] coeffs = new MultivariatePolynomialZp64[dim];
                for (int k = 0; k < dim; k++) coeffs[k] = fZp.createZero();
                coeffs[j] = df_di;
                coeffs[i] = df_dj.clone().multiply(mod - 1);
                relations.add(new TaggedMultivariatePolynomialZp64(fZp.createZero(), coeffs));
            }
        }
        List<TaggedMultivariatePolynomialZp64> gb = moduleGrobnerBasisModularZp64(relations);
        List<MultivariatePolynomialZp64[]> res = new ArrayList<>();
        for (var g : gb) res.add(g.coeffs);
        return res;
    }

    private IncrementalVectorBasis<Long, Monomial> computeBasisXModularZp64(int r, int q, MultivariatePolynomialZp64 fZp, Map<String, IncrementalVectorBasis<Long, Monomial>> cache, List<TaggedMultivariatePolynomialZp64> submoduleGB, List<MultivariatePolynomialZp64[]> syzGB, List<MultivariatePolynomialZp64[]> syzPrimeGB) {
        String key = r + "," + q;
        if (cache.containsKey(key)) return cache.get(key);

        FpStrategy strategy = new FpStrategy(fZp.ring.modulus);
        IncrementalVectorBasis<Long, Monomial> basis = new IncrementalVectorBasis<>(new VectorSpaceStrategy<>(strategy), Collections.reverseOrder());

        if (r == 1) {
            List<TaggedMultivariatePolynomialZp64> syzPrimeGBWrapped = new ArrayList<>();
            for (var coeffs : syzPrimeGB) syzPrimeGBWrapped.add(new TaggedMultivariatePolynomialZp64(fZp.createZero(), coeffs));
            
            int targetDegree = (q - 1) * degf - (dim - 1);
            for (MultivariatePolynomialZp64[] sCoeffs : syzGB) {
                TaggedMultivariatePolynomialZp64 s = new TaggedMultivariatePolynomialZp64(fZp.createZero(), sCoeffs);
                Object[] lmRes = s.leadingModuleMonomial();
                cc.redberry.rings.poly.multivar.MonomialZp64 lm = lmRes == null ? null : (cc.redberry.rings.poly.multivar.MonomialZp64) lmRes[1];

                if (lm == null || lm.totalDegree > targetDegree) continue;

                int degreeGap = targetDegree - lm.totalDegree;
                for (int[] variables : Itertools.combinationsWithReplacement(dim, degreeGap)) {
                    int[] exponents = new int[maxVarIdx];
                    for (int variable : variables) {
                        exponents[spaceIndices[variable]]++;
                    }
                    cc.redberry.rings.poly.multivar.MonomialZp64 multiplier = new cc.redberry.rings.poly.multivar.MonomialZp64(exponents, 1L);

                    TaggedMultivariatePolynomialZp64 multipliedS = s.multiply(multiplier, 1L);
                    if (!moduleDivideModularZp64(multipliedS, syzPrimeGBWrapped).remainder.isZero()) {
                        var div = diffFormModularZp64(multipliedS.coeffs);
                        if (!div.isZero()) {
                            basis.add(toVectorSpaceZp64(div, strategy));
                        }
                    }
                }
            }
        } else {
            basis.addAll(computeBasisXModularZp64(1, q, fZp, cache, submoduleGB, syzGB, syzPrimeGB).basis());
            IncrementalVectorBasis<Long, Monomial> prevXBasis = computeBasisXModularZp64(r - 1, q + 1, fZp, cache, submoduleGB, syzGB, syzPrimeGB);
            for (VectorSpaceElement<Long, Monomial> vsAlpha : prevXBasis.basis()) {
                MultivariatePolynomialZp64 alpha = fromVectorSpaceZp64(vsAlpha, fZp);
                if (alpha.degree() + dim == q * degf) {
                    MultivariatePolynomialZp64 red = redStepModularZp64(alpha, submoduleGB);
                    if (!red.isZero()) {
                        basis.add(toVectorSpaceZp64(red, strategy));
                    }
                }
            }
        }
        cache.put(key, basis);
        return basis;
    }

    private MultivariatePolynomialZp64 diffFormModularZp64(MultivariatePolynomialZp64[] beta) {
        MultivariatePolynomialZp64 d_beta = beta[0].createZero();
        for (int i = 0; i < beta.length; i++) {
                d_beta.add(beta[i].derivative(spaceIndices[i]));
        }
        return d_beta;
    }


    private MultivariatePolynomialZp64 redStepModularZp64(MultivariatePolynomialZp64 alpha, List<TaggedMultivariatePolynomialZp64> submoduleGB) {
        int maxQ = -1;
        for (cc.redberry.rings.poly.multivar.MonomialZp64 m : alpha) {
            int shiftedDegree = m.totalDegree + dim;
            if (shiftedDegree % degf == 0) maxQ = Math.max(maxQ, shiftedDegree / degf);
        }
        if (maxQ <= 1) return alpha;
        
        MultivariatePolynomialZp64 alphaQ = alpha.createZero();
        MultivariatePolynomialZp64 other = alpha.createZero();
        for (cc.redberry.rings.poly.multivar.MonomialZp64 m : alpha) {
            int shiftedDegree = m.totalDegree + dim;
            if (shiftedDegree % degf == 0 && shiftedDegree / degf == maxQ) {
                alphaQ.add(m);
            } else {
                other.add(m);
            }
        }
        TaggedMultivariatePolynomialZp64 alphaTagged = new TaggedMultivariatePolynomialZp64(alphaQ, new MultivariatePolynomialZp64[dim]);
        
        TaggedMultivariateDivRemZp64 dr = moduleDivideModularZp64(alphaTagged, submoduleGB);
        TaggedMultivariatePolynomialZp64 rem = dr.remainder;

        MultivariatePolynomialZp64 resPoly = rem.poly.clone();
        long invQ = MathUtils.modInv(maxQ - 1, alpha.ring.modulus);
        
        for (int i = 0; i < dim; i++) {
            MultivariatePolynomialZp64 beta_i = rem.coeffs[i];
            if (beta_i != null && !beta_i.isZero()) {
                MultivariatePolynomialZp64 d_beta_i = beta_i.derivative(spaceIndices[i]);
                resPoly.add(d_beta_i.multiply(invQ));
            }
        }
        resPoly = resPoly.add(other);
        return resPoly;
    }

    private record TaggedGrobnerBasisResultZp64(List<TaggedMultivariatePolynomialZp64> idealBasis, List<TaggedMultivariatePolynomialZp64> syzygyBasis) {}

    static void tr(Object...o) {System.out.println(Arrays.deepToString(o));}

    private List<long[]> computePicardFuchsInternal(MultivariatePolynomial<Long> aFinal, int rStart) {
        for (int r = rStart; r <= 10; r++) {
            List<MultivariatePolynomialOverFpFunctionField> rhos = new ArrayList<>();
            MultivariatePolynomialOverFpFunctionField aFinalSpatial = toSpatial(aFinal);
            rhos.add(reductionR(aFinalSpatial, r));

            boolean rTooSmall = false;
            for (int k = 0; k < 100; k++) {
                MultivariatePolynomialOverFpFunctionField currentRho = rhos.get(k);
                if (currentRho.isZero()) {
                    if (k == 0) return List.of(new long[]{1});
                    break;
                }

                int maxQ = -1;
                for (Monomial mon : currentRho.getFractionTerms().keySet()) {
                    maxQ = Math.max(maxQ, (mon.getDegree() + dim) / degf);
                }
                if (maxQ > dim - 1) {
                    rTooSmall = true;
                    break;
                }

                List<long[]> res = findLinearDependencySymbolicByExact(rhos);
                if (res != null) return res;

                var delta = computeDelta(currentRho);
                rhos.add(reductionR(delta, r));
            }
            if (!rTooSmall) {
                return findLinearDependencySymbolicByExact(rhos);
            }
        }
        return null;
    }

    private List<long[]> findLinearDependencySymbolicByExact(List<MultivariatePolynomialOverFpFunctionField> rhos) {
    	int m = rhos.size();
        List<Monomial> smList = new ArrayList<>();
        Set<Monomial> smSet = new HashSet<>();
        for (MultivariatePolynomialOverFpFunctionField rho : rhos) {
            for (Monomial sm : rho.getFractionTerms().keySet()) {
                if (smSet.add(sm)) smList.add(sm);
            }
        }
        int n = smList.size();
        if (n == 0) return List.of(new long[]{1});

        Matrix<FractionFieldElement<long[]>> matrixUtil = new Matrix<>(field);
        @SuppressWarnings("unchecked")
        FractionFieldElement<long[]>[][] mat = (FractionFieldElement<long[]>[][]) new FractionFieldElement[n][m];
        for (int i = 0; i < n; i++) {
            Monomial sm = smList.get(i);
            for (int j = 0; j < m; j++) {
                FractionFieldElement<long[]> c = rhos.get(j).getFractionTerms().get(sm);
                mat[i][j] = (c == null) ? field.zero() : c;
            }
        }

        FractionFieldElement<long[]>[][] ns = matrixUtil.nullSpace(mat);
        if (ns == null || ns.length == 0 || ns[0].length == 0) return null;

        int bestCol = -1;
        int minOrder = -1;
        for (int j = 0; j < ns[0].length; j++) {
            int order = -1;
            for (int i = m - 1; i >= 0; i--) {
                if (!field.equals(ns[i][j], field.zero())) {
                    order = i;
                    break;
                }
            }
            if (order != -1 && (minOrder == -1 || order < minOrder)) {
                minOrder = order;
                bestCol = j;
            }
        }

        if (bestCol == -1) return null;

        PolynomialFpDynamic poly1d = PolynomialFpDynamic.of(mod);
        long[] lcm = new long[]{1};
        for (int i = 0; i <= minOrder; i++) {
            long[] den = ns[i][bestCol].den();
            long[] g = poly1d.gcd(lcm, den);
            lcm = poly1d.mul(lcm, poly1d.exactDiv(den, g));
        }

        List<long[]> res = new ArrayList<>();
        for (int i = 0; i <= minOrder; i++) {
            long[] num = ns[i][bestCol].num();
            long[] den = ns[i][bestCol].den();
            res.add(poly1d.mul(num, poly1d.exactDiv(lcm, den)));
        }

        long[] commonGcd = null;
        for (long[] p : res) {
            if (p.length > 0) {
                if (commonGcd == null) commonGcd = p;
                else commonGcd = poly1d.gcd(commonGcd, p);
            }
        }
        if (commonGcd != null && poly1d.deg(commonGcd) > 0) {
            for (int i = 0; i < res.size(); i++) {
                if (res.get(i).length > 0) {
                    res.set(i, poly1d.exactDiv(res.get(i), commonGcd));
                }
            }
        }

        long lc = 0;
        for (int i = res.size() - 1; i >= 0; i--) {
            long[] p = res.get(i);
            if (p.length > 0) {
                int last = p.length - 1;
                while (last >= 0 && p[last] == 0) last--;
                if (last >= 0) {
                    lc = p[last];
                    break;
                }
            }
        }
        if (lc != 0 && lc != 1) {
            long inv = poly1d.fp.inv(lc);
            for (int i = 0; i < res.size(); i++) {
                for (int j = 0; j < res.get(i).length; j++) {
                    res.get(i)[j] = poly1d.fp.mul(res.get(i)[j], inv);
                }
            }
        }
        return res;
    }


    private MultivariatePolynomialOverFpFunctionField computeDelta(MultivariatePolynomialOverFpFunctionField alpha) {
        MultivariatePolynomialOverFpFunctionField res = new MultivariatePolynomialOverFpFunctionField(mod);
        MultivariatePolynomialOverFpFunctionField fDelta = fSpatial.differentiateT();
        Map<Integer, MultivariatePolynomialOverFpFunctionField> grouped = new HashMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : alpha.getFractionTerms().entrySet()) {
            int q = (entry.getKey().getDegree() + dim) / degf;
            grouped.put(q, grouped.getOrDefault(q, new MultivariatePolynomialOverFpFunctionField(mod)).add(MultivariatePolynomialOverFpFunctionField.singleTerm(mod, entry.getKey(), entry.getValue())));
        }
        for (Map.Entry<Integer, MultivariatePolynomialOverFpFunctionField> entry : grouped.entrySet()) {
            MultivariatePolynomialOverFpFunctionField a = entry.getValue();
            res = res.add(a.differentiateT().multiply(fSpatial));
            res = res.subtract(a.multiply(fDelta).multiply(field.from(new long[]{entry.getKey() % mod})));
        }
        return res;
    }

    /**
     * 対角成分 $I(t) = \text{diag}(a/f^p)$ を周期積分 $J(t) = \oint \frac{A}{F^p} \Omega$ の形式に変換する。
     * 未テスト。
     * <p>数学的仕様:
     * $n$ 変数の対角計算を $n+1$ 変数の周期積分計算に帰着させる。
     * $x_0$ を斉次化変数に似た役割として導入し、新しい変数 $t$ を導入する。
     * </p>
     * 事前条件: $a, f \neq null$。$f$ の定数項は非ゼロ。
     * 事後条件: 変換後の分子、分母、パラメータを含む HomogenizedTriple を返す。
     * 副作用: なし。
     * 計算量: $O(T \log T)$ (項数 $T$ に関して)。
     * 破壊的変更: なし。
     * 参照共有: なし。
     * 例外: なし。
     * @param a 分子多項式。
     * @param f 分母多項式。
     * @param p 分母の位数。
     * @return 変換後の多項式を含む HomogenizedTriple。
     */
    public static HomogenizedTriple transformForDiagonal(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p) {
        long mod = (f instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) f.getCoefficientRing()).getMod();
        int n = -1;
        for (Monomial m : f.getTerms().keySet()) {
            for (int i = 0; i < m.size(); i++) {
                if (m.getExponent(i) > 0) n = Math.max(n, i);
            }
        }
        for (Monomial m : a.getTerms().keySet()) {
            for (int i = 0; i < m.size(); i++) {
                if (m.getExponent(i) > 0) n = Math.max(n, i);
            }
        }
        if (n == -1) n = 0;

        int df = 0;
        for (Monomial m : f.getTerms().keySet()) {
            df = Math.max(df, m.getExponent(0));
        }
        int da = 0;
        for (Monomial m : a.getTerms().keySet()) {
            da = Math.max(da, m.getExponent(0));
        }
        int D = Math.max(Math.max(df, (da + p) / p), 1);

        TreeMap<Monomial, Long> nFTerms = new TreeMap<>();
        for (Map.Entry<Monomial, Long> entry : f.getTerms().entrySet()) {
            Monomial m = entry.getKey();
            int e0 = m.getExponent(0);
            int[] exps = new int[n + 1];
            exps[0] = e0;
            for (int i = 1; i <= n; i++) {
                exps[i] = m.getExponent(i) + (D - e0);
            }
            nFTerms.put(new Monomial(exps), entry.getValue());
        }

        TreeMap<Monomial, Long> nATerms = new TreeMap<>();
        for (Map.Entry<Monomial, Long> entry : a.getTerms().entrySet()) {
            Monomial m = entry.getKey();
            int a0 = m.getExponent(0);
            int[] exps = new int[n + 1];
            exps[0] = a0;
            for (int i = 1; i <= n; i++) {
                exps[i] = m.getExponent(i) + (p * D - 1 - a0);
            }
            nATerms.put(new Monomial(exps), entry.getValue());
        }

        int[] spaceIndices = IntStream.range(1, n + 1).toArray();
        return new HomogenizedTriple(
            new MultivariatePolynomialOverFp(mod, nATerms),
            new MultivariatePolynomialOverFp(mod, nFTerms),
            p,
            0,
            spaceIndices
        );
    }
    
    
  
    
    /**
     * 斉次入力 a/f^p を斉次化せずに [a]_r まで簡約した係数多項式を返す。
     * 未テスト。
     * 事前条件: a, f != null。a と f は同じ有限体上の斉次多項式。a は pole p の係数次数を満たす。
     * 事後条件: reductionR(toSpatial(a), r) を MultivariatePolynomial として返す。
     * 副作用: なし。
     * 計算量: O(r * GB + R) (R は簡約で生じる項数に依存)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 返値は新しい MultivariatePolynomial。
     * 例外・未定義条件: a/f^p が斉次でない場合、または係数が定数体に落ちない場合の意味は未定義。
     * @param a 分子多項式 a。
     * @param f 分母多項式 f。
     * @param p pole order。斉次性の契約を表す値で、この実装では次数推定は a の次数から行う。
     * @param r 拡張簡約の次数。
     * @return [a]_r を表す係数多項式。
     */
    public static MultivariatePolynomial<Long> reduceHomogeneous(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int r) {
        int nVars = Math.max(inferVariableCount(a), inferVariableCount(f));
        int[] spaceIndices = IntStream.range(0, nVars).toArray();
        RationalIntegralReduction reduction = new RationalIntegralReduction(f, spaceIndices, -1);
        MultivariatePolynomialOverFpFunctionField reduced = reduction.reductionR(reduction.toSpatial(a), r);
        return reduction.toConstantMultivariatePolynomial(reduced);
    }
    
    /**
     * 斉次入力 $a/f^p$ を斉次化せずに Zp64 実装で $[a]_r$ まで簡約した係数多項式を返す。
     * 未テスト。
     * 事前条件: $a,f\ne null$。$a$ と $f$ は同じ有限体 $\mathbb F_m$ 上の斉次多項式。$m$ は Zp64 で表せる素数。$a/f^p$ は斉次。
     * 事後条件: 返値は {@code reductionRZp64(toRingsPolynomial(a), r, toRingsPolynomial(f), ...)} を {@link MultivariatePolynomial} に戻した多項式である。
     * 副作用: なし。
     * 計算量: $O(GB + X_r + R)$。$GB$ は $\langle\partial_i f\rangle$ と syzygy の Groebner 基底計算、$X_r$ は $X_q^r$ の構成、$R$ は簡約の項数に依存する。
     * 破壊的変更: なし。$a,f$ を変更しない。
     * 参照共有・所有権: 返値は新規 {@link MultivariatePolynomial}。内部 Zp64 多項式、Groebner 基底、キャッシュはメソッド内で所有する。
     * 例外・未定義条件: $a/f^p$ が斉次でない場合、$f$ が対象次数でない場合、または簡約が収束しない場合の数学的意味は未定義。
     * @param a 分子多項式 $a$。
     * @param f 分母多項式 $f$。
     * @param p pole order。斉次性の契約を表す値で、この実装では次数推定は $a$ と $f$ の変数数から行う。
     * @param r 拡張簡約の次数。
     * @return Zp64 実装による $[a]_r$ を表す係数多項式。
     */
    public static MultivariatePolynomial<Long> reduceHomogeneousZp64(MultivariatePolynomial<Long> a, MultivariatePolynomial<Long> f, int p, int r) {
        int nVars = Math.max(inferVariableCount(a), inferVariableCount(f));
        int[] spaceIndices = IntStream.range(0, nVars).toArray();
        RationalIntegralReduction reduction = new RationalIntegralReduction(f, spaceIndices, -1);
        long fMod = (f instanceof MultivariatePolynomialOverFp pFp) ? pFp.getMod() : ((library.util.algebra.strategy.ZnStrategy) f.getCoefficientRing()).getMod();
        IntegersZp64 ring = new IntegersZp64(fMod);
        MultivariatePolynomialZp64 aZp = MultivariatePolynomialOverFp.toRingsPolynomial(a, nVars, ring);
        MultivariatePolynomialZp64 fZp = MultivariatePolynomialOverFp.toRingsPolynomial(f, nVars, ring);
        List<TaggedMultivariatePolynomialZp64> submoduleGB = reduction.computeSubmoduleGBModularZp64(fZp);
        List<MultivariatePolynomialZp64[]> syzGB = null;
        List<MultivariatePolynomialZp64[]> syzPrimeGB = null;
        if (r >= 1) {
            syzGB = new ArrayList<>();
            for (TaggedMultivariatePolynomialZp64 g : submoduleGB) if (g.poly.isZero()) syzGB.add(g.coeffs);
            syzPrimeGB = reduction.computeSyzPrimeGBModularZp64(fZp);
        }
        MultivariatePolynomialZp64 reduced = reduction.reductionRZp64(
                aZp, r, fZp, new HashMap<String, IncrementalVectorBasis<Long, Monomial>>(), submoduleGB, syzGB, syzPrimeGB);
        return MultivariatePolynomialOverFp.fromRingsPolynomial(reduced, fMod);
    }
    private static int inferVariableCount(MultivariatePolynomial<Long> f) {
        int nVars = 0;
        for (Monomial m : f.getTerms().keySet()) nVars = Math.max(nVars, m.size());
        return Math.max(nVars, 1);
    }
    
    
    /**
     * 定数係数 MultivariateFractionPolynomial を MultivariatePolynomial に変換する。
     * 未テスト。
     * 事前条件: p の係数は F_mod の元である。
     * 事後条件: 各項 c*x^m を (num(c)/den(c))*x^m に写した多項式を返す。
     * 副作用: なし。
     * 計算量: O(T log T)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 返値は新しい MultivariatePolynomial。
     * 例外・未定義条件: den(c) が 0 または非定数の場合の動作は未定義。
     * @param p 入力多項式。
     * @return 多変数多項式。
     */
    private MultivariatePolynomial<Long> toConstantMultivariatePolynomial(MultivariatePolynomialOverFpFunctionField p) {
        TreeMap<Monomial, Long> terms = new TreeMap<>();
        for (Map.Entry<Monomial, FractionFieldElement<long[]>> entry : p.getFractionTerms().entrySet()) {
            long num = entry.getValue().num().length == 0 ? 0 : entry.getValue().num()[0];
            long den = entry.getValue().den().length == 0 ? 1 : entry.getValue().den()[0];
            terms.put(entry.getKey(), num * MathUtils.modInv(den, mod) % mod);
        }
        return new MultivariatePolynomialOverFp(mod, terms);
    }

}
