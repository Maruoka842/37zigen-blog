package library.util.graph.tree;

import library.util.polynomial.PolynomialFpDynamic;

/**
 * 木上の独立集合の個数を求めるクラス。
 * Static Top Tree を用いて、各サイズ k の独立集合の個数を O(N log^2 N) で計算します。
 * 結果は多項式として返され、k 次の係数がサイズ k の独立集合の個数に対応します。
 */
public class CountIndependentSetOnTree {
    private static final PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;

    /**
     * Point クラスター（根付き部分木）の状態を保持するレコード。
     * p0: 根が独立集合に含まれない状態の多項式。
     * p1: 根が独立集合に含まれる状態の多項式。
     */
    public record PointData(long[] p0, long[] p1) {}

    /**
     * Path クラスター（重分解されたパスのセグメント）の状態を保持するレコード。
     * fij: 上端の状態が i, 下端の状態が j である多項式 (0: 含まない, 1: 含む)。
     * top: このパスセグメントの上端の頂点インデックス。
     * bottom: このパスセグメントの下端の頂点インデックス。
     */
    public record PathData(long[] f00, long[] f01, long[] f10, long[] f11, int top, int bottom) {
        /**
         * 指定された端点の状態に対応する多項式を取得します。
         * @param i 上端の状態 (0: 含まない, 1: 含む)
         * @param j 下端の状態 (0: 含まない, 1: 含む)
         * @return 対応する多項式。存在しない場合は空配列。
         */
        public long[] getPoly(int i, int j) {
            if (i == 0 && j == 0) return f00 == null ? new long[0] : f00;
            if (i == 0 && j == 1) return f01 == null ? new long[0] : f01;
            if (i == 1 && j == 0) return f10 == null ? new long[0] : f10;
            if (i == 1 && j == 1) return f11 == null ? new long[0] : f11;
            return new long[0];
        }
    }

    /**
     * 与えられた木 tree 上の独立集合の個数をサイズごとに計算します。
     * @param tree 対象の木
     * @return 独立集合多項式。k 次の係数がサイズ k の独立集合の個数。
     */
    public static long[] countIndependentSet(Tree tree) {
        if (tree.N == 0) return new long[]{1};
        if (!tree.isRooted()) tree.rooted(0);

        IndependentSetOnTreeStrategy strategy = new IndependentSetOnTreeStrategy();
        STTSolver<PointData, PathData> solver = new STTSolver<>(tree, strategy);
        PathData res = solver.getResult();

        long[] ans = new long[0];
        ans = poly.add(ans, res.f00);
        ans = poly.add(ans, res.f01);
        ans = poly.add(ans, res.f10);
        ans = poly.add(ans, res.f11);
        return ans;
    }

    private static class IndependentSetOnTreeStrategy implements STTStrategy<PointData, PathData> {
        @Override
        public PathData createVertex(int v) {
            // 単一頂点: 状態は (0, 0) または (1, 1) のみ。
            // (0, 0): 頂点 v を含まない -> 多項式 1
            // (1, 1): 頂点 v を含む -> 多項式 x
            return new PathData(new long[]{1}, new long[0], new long[0], new long[]{0, 1}, v, v);
        }

        @Override
        public PointData appendVirtualRoot(PathData path) {
            // 軽部分木の辺 (v, u) を追加。v は親、u は path.top。
            // p0: v が独立集合に含まれない場合、u は 0 でも 1 でもよい。
            // p1: v が独立集合に含まれる場合、u は 0 でなければならない。
            long[] sumAll = poly.add(poly.add(path.f00, path.f01), poly.add(path.f10, path.f11));
            long[] sum0 = poly.add(path.f00, path.f01);
            return new PointData(sumAll, sum0);
        }

        @Override
        public PointData mergeVirtualRoot(PointData l, PointData r) {
            // 同じ親にぶら下がる 2 つの軽部分木をマージ。
            long[] p0 = poly.mul(l.p0, r.p0);
            long[] p1 = poly.mul(l.p1, r.p1);
            return new PointData(p0, p1);
        }

        @Override
        public PathData replaceVirtualRoot(PointData point, int v) {
            // 頂点 v とその全ての軽部分木を結合。
            // v が 0 のとき: すべての軽部分木 u に対して (u=0 or u=1)
            // v が 1 のとき: すべての軽部分木 u に対して (u=0)
            long[] f00 = (point == null) ? new long[]{1} : point.p0;
            long[] f11 = (point == null) ? new long[]{0, 1} : poly.mul(point.p1, new long[]{0, 1});
            return new PathData(f00, new long[0], new long[0], f11, v, v);
        }

        @Override
        public PathData joinHeavyEdge(PathData parent, PathData child) {
            // 重辺 (u, v) でつながれた 2 つのパスセグメントを結合。
            // u = parent.bottom, v = child.top。
            // u と v が同時に独立集合に含まれることはできない。

            long[][][] z = new long[2][2][];

            for (int d = 0; d < 2; d++) {
                long[] cSum = poly.add(child.getPoly(0, d), child.getPoly(1, d));
                long[] c0 = child.getPoly(0, d);
                for (int a = 0; a < 2; a++) {
                    long[] res = new long[0];
                    // u=0 の場合: v は 0 または 1 のどちらでもよい。
                    long[] p0 = parent.getPoly(a, 0);
                    if (p0.length > 0) {
                        res = poly.add(res, poly.mul(p0, cSum));
                    }
                    // u=1 の場合: v は 0 でなければならない。
                    long[] p1 = parent.getPoly(a, 1);
                    if (p1.length > 0) {
                        res = poly.add(res, poly.mul(p1, c0));
                    }
                    z[a][d] = res;
                }
            }
            return new PathData(z[0][0], z[0][1], z[1][0], z[1][1], parent.top, child.bottom);
        }
    }
}
