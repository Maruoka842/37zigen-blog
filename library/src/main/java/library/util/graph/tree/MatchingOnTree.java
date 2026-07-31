package library.util.graph.tree;

import library.util.graph.tree.StaticTopTree;
import library.util.graph.tree.STTSolver;
import library.util.graph.tree.STTStrategy;
import library.util.graph.tree.Tree;
import library.util.polynomial.PolynomialFpDynamic;
import java.util.Arrays;

/**
 * 木上のマッチングの個数を求めるクラス。
 * Static Top Tree を用いて、各サイズ k のマッチングの個数を O(N log^2 N) で計算します。
 * 結果は多項式として返され、k 次の係数がサイズ k のマッチングの個数に対応します。
 * 隣接行列の特性多項式を求めることと等価です。
 *
 * https://atcoder.jp/contests/abc269/tasks/abc269_h
 */
public class MatchingOnTree {
    private static final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    /**
     * Point クラスター（根付き部分木）の状態を保持するレコード。
     * p0: 根がマッチングに含まれていない状態の多項式。
     * p1: 根がマッチングに含まれている状態の多項式。
     */
    public record PointData(long[] p0, long[] p1) {}

    /**
     * Path クラスター（重分解されたパスのセグメント）の状態を保持するレコード。
     * fij: 上端の状態が i, 下端の状態が j である多項式 (0: 未マッチ, 1: マッチ済)。
     * top: このパスセグメントの上端の頂点インデックス。
     * bottom: このパスセグメントの下端の頂点インデックス。
     */
    public record PathData(long[] f00, long[] f01, long[] f10, long[] f11, int top, int bottom) {
        /**
         * 指定された端点の状態に対応する多項式を取得します。
         * @param i 上端の状態 (0: 未マッチ, 1: マッチ済)
         * @param j 下端の状態 (0: 未マッチ, 1: マッチ済)
         * @return 対応するマッチング多項式。存在しない場合は空配列。
         */
        public long[] getPoly(int i, int j) {
            if (i == 0 && j == 0) return f00 == null ? new long[0] : f00;
            if (i == 0 && j == 1) return f01 == null ? new long[0] : f01;
            if (i == 1 && j == 1) return f11 == null ? new long[0] : f11;
            if (i == 1 && j == 0) return f10 == null ? new long[0] : f10;
            return new long[0];
        }

        /**
         * このクラスターが単一の頂点のみを含む（top == bottom）かどうかを判定します。
         */
        public boolean isSingle() {
            return top == bottom;
        }
    }

    /**
     * 与えられた木 G 上のマッチングの個数をサイズごとに計算します。
     * @param tree 対象の木
     * @return マッチング多項式。k 次の係数がサイズ k のマッチングの個数。
     */
    public static long[] countMatching(Tree tree) {
        if (tree.N == 0) return new long[0];
        if (!tree.isRooted()) tree.rooted(0);

        MatchingOnTreeStrategy strategy = new MatchingOnTreeStrategy(null);
        STTSolver<PointData, PathData> solver = new STTSolver<>(tree, strategy);
        PathData res = solver.getResult();

        long[] ans = new long[0];
        ans = poly.add(ans, res.f00);
        ans = poly.add(ans, res.f01);
        ans = poly.add(ans, res.f10);
        ans = poly.add(ans, res.f11);
        return ans;
    }

    /**
     * 与えられた重み付き木 G 上の重み付きマッチングの多項式をサイズごとに計算します。
     * 各マッチングの重みは、選ばれた辺の重みの積です。
     * @param tree 対象の重み付き木
     * @return マッチング多項式。k 次の係数がサイズ k の重み付きマッチングの総和。
     */
    public static long[] countMatching(LongValueTree tree) {
        if (tree.N == 0) return new long[0];
        if (!tree.isRooted()) tree.rooted(0);

        StaticTopTree stt = new StaticTopTree(tree.N, tree.root(), tree.childs);
        MatchingOnTreeStrategy strategy = new MatchingOnTreeStrategy(tree.parentEdgeCost);
        STTSolver<PointData, PathData> solver = new STTSolver<>(stt, strategy);
        PathData res = solver.getResult();

        long[] ans = new long[0];
        ans = poly.add(ans, res.f00);
        ans = poly.add(ans, res.f01);
        ans = poly.add(ans, res.f10);
        ans = poly.add(ans, res.f11);
        return ans;
    }

    /**
     * 木の隣接行列 A に対して det(I - xA) を計算します。
     * O(N log^2 N)
     * 未テスト
     * @param tree 対象の木
     * @return 特性多項式 det(I - xA)
     */
    public static long[] determinantIXA(Tree tree) {
        if (tree.N == 0) return new long[]{1};
        long[] m = countMatching(tree);
        long[] res = new long[2 * m.length - 1];
        for (int k = 0; k < m.length; k++) {
            long val = m[k];
            if (k % 2 == 1) val = (poly.mod - val) % poly.mod;
            res[2 * k] = val;
        }
        return res;
    }

    /**
     * 重み付き木の隣接行列 A に対して det(I - xA) を計算します。
     * 各辺の重みは A_uv = A_vu = w_uv です。
     * O(N log^2 N)
     * 未テスト
     * @param tree 対象の重み付き木
     * @return 特性多項式 det(I - xA)
     */
    public static long[] determinantIXA(LongValueTree tree) {
        if (tree.N == 0) return new long[]{1};
        if (!tree.isRooted()) tree.rooted(0);

        long[] negatedSquaredWeights = new long[tree.parentEdgeCost.length];
        for (int i = 0; i < negatedSquaredWeights.length; i++) {
            long w = tree.parentEdgeCost[i] % poly.mod;
            negatedSquaredWeights[i] = (poly.mod - (w * w % poly.mod)) % poly.mod;
        }

        StaticTopTree stt = new StaticTopTree(tree.N, tree.root(), tree.childs);
        MatchingOnTreeStrategy strategy = new MatchingOnTreeStrategy(negatedSquaredWeights);
        STTSolver<PointData, PathData> solver = new STTSolver<>(stt, strategy);
        PathData pathRes = solver.getResult();

        long[] m = new long[0];
        m = poly.add(m, pathRes.f00);
        m = poly.add(m, pathRes.f01);
        m = poly.add(m, pathRes.f10);
        m = poly.add(m, pathRes.f11);

        long[] res = new long[2 * m.length - 1];
        for (int k = 0; k < m.length; k++) {
            res[2 * k] = m[k];
        }
        return res;
    }

    private static class MatchingOnTreeStrategy implements STTStrategy<PointData, PathData> {
        private final long[] parentEdgeCost;

        public MatchingOnTreeStrategy(long[] parentEdgeCost) {
            this.parentEdgeCost = parentEdgeCost;
        }

        @Override
        public PathData createVertex(int v) {
            // 単一頂点: 状態は (未マッチ, 未マッチ) のみ
            return new PathData(new long[]{1}, new long[0], new long[0], new long[0], v, v);
        }

        @Override
        public PointData appendVirtualRoot(PathData path) {
            // 軽部分木の辺 (u, v) を追加。u は仮想的な親。
            // p0: u が v とマッチしない場合。v は任意の状態でよい。
            // p1: u が v とマッチする場合。v は未マッチでなければならない。
            int v = path.top;
            long weight = (parentEdgeCost != null && v < parentEdgeCost.length) ? parentEdgeCost[v] : 1;
            long[] p0 = poly.add(poly.add(path.f00, path.f10), poly.add(path.f01, path.f11));
            long[] p1 = poly.mul(poly.add(path.f00, path.f01), new long[]{0, weight % poly.mod});
            return new PointData(p0, p1);
        }

        @Override
        public PointData mergeVirtualRoot(PointData l, PointData r) {
            // 同じ親にぶら下がる 2 つの軽部分木をマージ。
            // z0: 親がいずれともマッチしない。
            // z1: 親が l または r のいずれかとマッチする。
            long[] p0 = poly.mul(l.p0, r.p0);
            long[] p1 = poly.add(poly.mul(l.p0, r.p1), poly.mul(l.p1, r.p0));
            return new PointData(p0, p1);
        }

        @Override
        public PathData replaceVirtualRoot(PointData point, int v) {
            // 頂点 v とその全ての軽部分木を結合。
            // v は単一の頂点として扱われる (top=bottom=v)。
            return new PathData(point.p0, new long[0], new long[0], point.p1, v, v);
        }

        @Override
        public PathData joinHeavyEdge(PathData parent, PathData child) {
            // 重辺でつながれた 2 つのパスセグメントを結合。
            // u: parent クラスターの下端, v: child クラスターの上端。
            int u = parent.bottom;
            int v = child.top;
            long[][][] z = new long[2][2][];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) z[i][j] = new long[0];
            }

            long weight = (parentEdgeCost != null && v < parentEdgeCost.length) ? parentEdgeCost[v] : 1;

            for (int a = 0; a < 2; a++) {
                for (int d = 0; d < 2; d++) {
                    // 1. 重辺 (parent.bottom, child.top) をマッチングに使う場合
                    // 両端が未マッチ状態でなければならない。
                    long[] f0 = parent.getPoly(a, 0);
                    long[] g0 = child.getPoly(0, d);
                    if (f0.length > 0 && g0.length > 0) {
                        long[] f = poly.mul(poly.mul(f0, g0), new long[]{0, weight % poly.mod});
                        int x = parent.isSingle() ? 1 : a;
                        int y = child.isSingle() ? 1 : d;
                        z[x][y] = poly.add(z[x][y], f);
                    }

                    // 2. 重辺をマッチングに使わない場合
                    // 両端は任意の状態（マッチ済または未マッチ）でよい。
                    long[] pa = poly.add(parent.getPoly(a, 0), parent.getPoly(a, 1));
                    long[] gd = poly.add(child.getPoly(0, d), child.getPoly(1, d));
                    if (pa.length > 0 && gd.length > 0) {
                        long[] f_not = poly.mul(pa, gd);
                        z[a][d] = poly.add(z[a][d], f_not);
                    }
                }
            }
            return new PathData(z[0][0], z[0][1], z[1][0], z[1][1], parent.top, child.bottom);
        }
    }
}
