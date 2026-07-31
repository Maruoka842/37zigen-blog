package library.util.polynomial;

import java.util.*;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.ZnStrategy;

/**
 * 可換係数環 C 上の多変数多項式環 R[x_0, x_1, ...] を表すメタオブジェクトクラス。
 * 算術演算、多変数除算、グレブナー基底の計算、および構文解析をサポートする。
 *
 * @param <C> 係数の型。
 */
public class MultivariatePolynomialRing<C> implements CommutativeRingStrategy<MultivariatePolynomial<C>> {
    private final CommutativeRingStrategy<C> coefficientRing;
    private final int totalVars;
    private final Map<String, Integer> varMap;

    /**
     * 指定された係数環で多変数多項式環を生成する。
     * 変数の個数は0（定数項のみまたは動的に構築される前提）として初期化される。
     * 未テスト。
     *
     * @param coefficientRing 係数環。
     */
    public MultivariatePolynomialRing(CommutativeRingStrategy<C> coefficientRing) {
        this.coefficientRing = Objects.requireNonNull(coefficientRing);
        this.totalVars = 0;
        this.varMap = new HashMap<>();
    }

    /**
     * 指定された係数環と変数個数で多変数多項式環を生成する。
     * 未テスト。
     *
     * @param coefficientRing 係数環。
     * @param totalVars 変数個数。
     */
    public MultivariatePolynomialRing(CommutativeRingStrategy<C> coefficientRing, int totalVars) {
        this.coefficientRing = Objects.requireNonNull(coefficientRing);
        this.totalVars = totalVars;
        this.varMap = new HashMap<>();
        for (int i = 0; i < totalVars; i++) {
            this.varMap.put("x" + i, i);
        }
    }

    /**
     * 指定された係数環と変数名リストで多変数多項式環を生成する。
     * 未テスト。
     *
     * @param coefficientRing 係数環。
     * @param vars 変数名リスト。
     */
    public MultivariatePolynomialRing(CommutativeRingStrategy<C> coefficientRing, String... vars) {
        this.coefficientRing = Objects.requireNonNull(coefficientRing);
        this.totalVars = vars.length;
        this.varMap = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            this.varMap.put(vars[i], i);
        }
    }

    /**
     * 係数環を返す。
     * 未テスト。
     *
     * @return 係数環。
     */
    public CommutativeRingStrategy<C> getCoefficientRing() {
        return coefficientRing;
    }

    /**
     * 扱っている全変数の個数を返す。
     * 未テスト。
     *
     * @return 変数個数。
     */
    public int getTotalVars() {
        return totalVars;
    }

    @Override
    public MultivariatePolynomial<C> zero() {
        return new MultivariatePolynomial<>(coefficientRing);
    }

    @Override
    public MultivariatePolynomial<C> one() {
        TreeMap<Monomial, C> terms = new TreeMap<>();
        terms.put(new Monomial(new int[totalVars]), coefficientRing.one());
        return new MultivariatePolynomial<>(coefficientRing, terms);
    }

    @Override
    public MultivariatePolynomial<C> add(MultivariatePolynomial<C> a, MultivariatePolynomial<C> b) {
        return a.add(b);
    }

    @Override
    public MultivariatePolynomial<C> sub(MultivariatePolynomial<C> a, MultivariatePolynomial<C> b) {
        return a.sub(b);
    }

    @Override
    public MultivariatePolynomial<C> mul(MultivariatePolynomial<C> a, MultivariatePolynomial<C> b) {
        return a.mul(b);
    }

    @Override
    public MultivariatePolynomial<C> neg(MultivariatePolynomial<C> a) {
        return a.neg();
    }

    @Override
    public boolean equals(MultivariatePolynomial<C> a, MultivariatePolynomial<C> b) {
        return a.equals(b);
    }

    /**
     * 多変数除算アルゴリズム。
     * f を多項式のリスト F で除算する。
     * 未テスト。
     *
     * @param f 被除数多項式。
     * @param F 除数多項式のリスト。
     * @return 除算結果（商のリストと剰余）。
     */
    public MultivariatePolynomial.DivisionResult<C> divide(MultivariatePolynomial<C> f, List<MultivariatePolynomial<C>> F) {
        if (F.isEmpty()) return new MultivariatePolynomial.DivisionResult<>(new ArrayList<>(), f);
        if (!(coefficientRing instanceof FieldStrategy<C> field)) {
            throw new UnsupportedOperationException("multivariate division requires FieldStrategy coefficients");
        }
        List<MultivariatePolynomial<C>> quotients = new ArrayList<>();
        for (MultivariatePolynomial<C> divisor : F) {
            if (!Objects.equals(coefficientRing, divisor.getCoefficientRing())) {
                throw new IllegalArgumentException("coefficient rings differ");
            }
            quotients.add(zero());
        }
        MultivariatePolynomial<C> remainder = zero();
        MultivariatePolynomial<C> p = f;
        while (!p.isZero()) {
            boolean divided = false;
            Monomial leading = p.getLeadingMonomial();
            C leadingCoefficient = p.leadingCoefficient();
            for (int i = 0; i < F.size(); i++) {
                MultivariatePolynomial<C> divisor = F.get(i);
                Monomial divisorLeading = divisor.getLeadingMonomial();
                if (divisorLeading != null && leading.isDivisibleBy(divisorLeading)) {
                    Monomial quotientMonomial = leading.divide(divisorLeading);
                    C quotientCoefficient = field.div(leadingCoefficient, divisor.leadingCoefficient());
                    MultivariatePolynomial<C> term = MultivariatePolynomial.singleTerm(coefficientRing, quotientMonomial, quotientCoefficient);
                    quotients.set(i, quotients.get(i).add(term));
                    p = p.sub(divisor.multiply(quotientMonomial, quotientCoefficient));
                    divided = true;
                    break;
                }
            }
            if (!divided) {
                MultivariatePolynomial<C> leadingTerm = MultivariatePolynomial.singleTerm(coefficientRing, leading, leadingCoefficient);
                remainder = remainder.add(leadingTerm);
                p = p.sub(leadingTerm);
            }
        }
        return new MultivariatePolynomial.DivisionResult<>(quotients, remainder);
    }

    /**
     * f と g の S-多項式を計算する。
     * 未テスト。
     *
     * @param f 多項式 f。
     * @param g 多項式 g。
     * @return S-多項式。
     */
    public MultivariatePolynomial<C> sPolynomial(MultivariatePolynomial<C> f, MultivariatePolynomial<C> g) {
        if (!(coefficientRing instanceof FieldStrategy<C> field)) {
            throw new UnsupportedOperationException("operation requires FieldStrategy coefficients");
        }
        if (!Objects.equals(coefficientRing, g.getCoefficientRing())) {
            throw new IllegalArgumentException("coefficient rings differ");
        }
        Monomial LTf = f.getLeadingMonomial();
        Monomial LTg = g.getLeadingMonomial();
        Monomial L = Monomial.lcm(LTf, LTg);
        C LCfInv = field.inv(f.leadingCoefficient());
        C LCgInv = field.inv(g.leadingCoefficient());
        MultivariatePolynomial<C> term1 = f.multiply(L.divide(LTf), LCfInv);
        MultivariatePolynomial<C> term2 = g.multiply(L.divide(LTg), LCgInv);
        return term1.sub(term2);
    }

    /**
     * グレブナー基底を計算する（Buchberger のアルゴリズム）。
     * 未テスト。
     *
     * @param F 入力多項式のリスト。
     * @return 簡約グレブナー基底。
     */
    public List<MultivariatePolynomial<C>> grobnerBasis(List<MultivariatePolynomial<C>> F) {
        if (F.isEmpty()) return new ArrayList<>();
        if (!(coefficientRing instanceof FieldStrategy<C> field)) {
            throw new UnsupportedOperationException("operation requires FieldStrategy coefficients");
        }
        List<MultivariatePolynomial<C>> G = new ArrayList<>();
        PriorityQueue<Pair> pairs = new PriorityQueue<>();
        for (MultivariatePolynomial<C> f : F) {
            MultivariatePolynomial<C> r = divide(f, G).remainder;
            if (!r.isZero()) {
                if (r.getLeadingMonomial().getDegree() == 0) {
                    List<MultivariatePolynomial<C>> res = new ArrayList<>();
                    res.add(MultivariatePolynomial.singleTerm(coefficientRing, r.getLeadingMonomial(), coefficientRing.one()));
                    return res;
                }
                addNewElement(G, pairs, r, field);
            }
        }
        while (!pairs.isEmpty()) {
            Pair pair = pairs.poll();
            if (isRedundant(pair, G)) continue;
            MultivariatePolynomial<C> S = sPolynomial(G.get(pair.i), G.get(pair.j));
            MultivariatePolynomial<C> r = divide(S, G).remainder;
            if (!r.isZero()) {
                if (r.getLeadingMonomial().getDegree() == 0) {
                    List<MultivariatePolynomial<C>> res = new ArrayList<>();
                    res.add(MultivariatePolynomial.singleTerm(coefficientRing, r.getLeadingMonomial(), coefficientRing.one()));
                    return res;
                }
                addNewElement(G, pairs, r, field);
            }
        }
        return reduceGrobnerBasis(G, field);
    }

    private void addNewElement(List<MultivariatePolynomial<C>> G, PriorityQueue<Pair> pairs, MultivariatePolynomial<C> r, FieldStrategy<C> field) {
        C lcInv = field.inv(r.leadingCoefficient());
        MultivariatePolynomial<C> monicR = r.multiply(lcInv);
        Monomial ltNew = monicR.getLeadingMonomial();
        int newIdx = G.size();
        for (int i = 0; i < G.size(); i++) {
            Monomial ltI = G.get(i).getLeadingMonomial();
            if (!Monomial.areRelativelyPrime(ltI, ltNew)) {
                pairs.add(new Pair(i, newIdx, G, monicR));
            }
        }
        G.add(monicR);
    }

    private boolean isRedundant(Pair pair, List<MultivariatePolynomial<C>> G) {
        Monomial ltI = G.get(pair.i).getLeadingMonomial();
        Monomial ltJ = G.get(pair.j).getLeadingMonomial();
        return Monomial.areRelativelyPrime(ltI, ltJ);
    }

    private List<MultivariatePolynomial<C>> reduceGrobnerBasis(List<MultivariatePolynomial<C>> G, FieldStrategy<C> field) {
        if (G.isEmpty()) return new ArrayList<>();
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

    private static class Pair implements Comparable<Pair> {
        final int i, j;
        final Monomial lcm;

        Pair(int i, int j, List<? extends MultivariatePolynomial<?>> G, MultivariatePolynomial<?> newP) {
            this.i = i;
            this.j = j;
            this.lcm = Monomial.lcm(G.get(i).getLeadingMonomial(), newP.getLeadingMonomial());
        }

        @Override
        public int compareTo(Pair o) {
            return this.lcm.compareTo(o.lcm);
        }
    }

    /**
     * public MultivariatePolynomialRing(CommutativeRingStrategy<C> coefficientRing, String... vars) で渡した変数順にインデックスが振られる。
     * @param input 解析対象の文字列。
     * @return 解析して得られた多項式。
     */
    public MultivariatePolynomial<Long> parse(String input) {
        if (coefficientRing instanceof FpStrategy fp) {
            PolynomialParser parser = new PolynomialParser(fp.getMod(), varMap, totalVars);
            return parser.parse(input);
        } else if (coefficientRing instanceof ZnStrategy zn) {
            PolynomialParser parser = new PolynomialParser(zn.getMod(), varMap, totalVars);
            return parser.parse(input);
        }
        throw new UnsupportedOperationException("Parsing is only supported for FpStrategy or ZnStrategy coefficient rings.");
    }
}
