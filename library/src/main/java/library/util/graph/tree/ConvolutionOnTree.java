package library.util.graph.tree;

import library.util.graph.*;

import java.util.function.BiFunction;

/**
 * Convolution on Tree: 根付き木上で根方向へ畳み込みを行うユーティリティクラス。
 * 各頂点 v について、その部分木内の頂点 u との距離に応じた重み付き和を計算します：
 * ret[v] = sum_{u in subtree(v)} f[u] * trans[dist(u, v)]
 *
 * 長鏈分割 (Long Path Decomposition) を用いることで、効率的に計算を行います。
 * 時間計算量: O(N log N) または O(N log^2 N) (使用する畳み込み関数の計算量に依存)
 * 空間計算量: O(N)
 *
 * 参照: https://yukicoder.me/problems/no/2004
 */
public class ConvolutionOnTree {
    private final int N;
    private int root = -1;
    private final int[] par;
    private final int[][] children;
    private final int[] depth;
    private final int[] farthestLeaf;

    /**
     * ConvolutionOnTree のコンストラクタ。
     * 木の構造を初期化し、長鏈分割のための最遠葉を計算します。
     *
     * @param par 親頂点の配列。par[i] は頂点 i の親。根の場合は -1。
     * @throws IllegalArgumentException 根が見つからない場合、または複数の根がある場合にスローされます。
     */
    public ConvolutionOnTree(int[] par) {
        this.N = par.length;
        this.par = par.clone();
        int[] degree = new int[N];
        for (int i = 0; i < N; i++) {
            if (par[i] >= 0 && par[i] < N) {
                degree[par[i]]++;
            } else {
                if (this.root != -1) {
                    throw new IllegalArgumentException("Multiple roots found");
                }
                this.root = i;
                this.par[i] = -1;
            }
        }
        if (N > 0 && root == -1) {
            throw new IllegalArgumentException("Root not found");
        }

        this.children = new int[N][];
        for (int i = 0; i < N; i++) {
            children[i] = new int[degree[i]];
        }
        int[] ptr = new int[N];
        for (int i = 0; i < N; i++) {
            if (par[i] >= 0 && par[i] < N) {
                children[par[i]][ptr[par[i]]++] = i;
            }
        }

        this.depth = new int[N];
        this.farthestLeaf = new int[N];
        if (N > 0) {
            build(root);
        }
    }

    /**
     * 各頂点の深さと、部分木内の最遠葉を計算します（長鏈分割用）。
     */
    private void build(int now) {
        farthestLeaf[now] = now;
        for (int nxt : children[now]) {
            depth[nxt] = depth[now] + 1;
            build(nxt);
            if (depth[farthestLeaf[now]] < depth[farthestLeaf[nxt]]) {
                farthestLeaf[now] = farthestLeaf[nxt];
            }
        }
    }

    /**
     * 畳み込みを実行します。
     *
     * @param f         各頂点の初期値。
     * @param trans     遷移係数。trans[d] は距離 d の頂点に対する重み。
     * @param mod       法。
     * @param convolver 2つの long 配列の畳み込みを行う関数（例：NTT）。
     * @return 畳み込み結果の配列。ret[v] = sum_{u in subtree(v)} f[u] * trans[dist(u, v)] (mod mod)。
     */
    public long[] run(long[] f, long[] trans, long mod, BiFunction<long[], long[], long[]> convolver) {
        if (N == 0) return new long[0];
        long[] ret = new long[N];
        runRec(root, -1, ret, f, trans, mod, convolver);
        return ret;
    }

    /**
     * 長鏈分割に基づいた再帰的な畳み込み処理。
     * r: 現在の部分木の根
     * @return 以下の数式で定義される配列 g:
     *         g[i] = \sum_{u in subtree(r), depth[farthestLeaf[r]] - depth[u] = i} f[u]
     */
    private long[] runRec(int r, int h, long[] ret, long[] f, long[] trans, long mod, BiFunction<long[], long[], long[]> convolver) {
        int leaf = farthestLeaf[r];
        int d = depth[leaf] - depth[r] + 1;
        long[] g = new long[d];
        int[] ids = new int[d];

        int idsPtr = 0;
        int prv = -1;
        int cur = leaf;

        // 葉から r に向かってlong Pathを辿り、値を収集する。
        // g[i] は leaf から距離 i の位置にある頂点（およびその横の枝）の合計値を保持する。
        while (true) {
            ids[idsPtr] = cur;//ids = [leaf, ..., r]
            g[idsPtr] = (f[cur] % mod + mod) % mod;

            for (int nxt : children[cur]) {
                if (nxt == prv) continue;

                int nxtLen = depth[farthestLeaf[nxt]] - depth[cur];
                //childHはfarthestLeaf[nxt]と並列にある[leaf,..,r]の頂点
                //convolver.apply(g, transSub)で余分な値を再帰内部で事前に引いておくために使う
                int childH = (idsPtr - nxtLen >= 0) ? ids[idsPtr - nxtLen] : -1;
                long[] gChild = runRec(nxt, childH, ret, f, trans, mod, convolver);
                for (int i = 0; i < gChild.length; i++) {
                    int targetIdx = idsPtr - gChild.length + i;
                    if (targetIdx >= 0) {
                        g[targetIdx] = (g[targetIdx] + gChild[i]) % mod;
                    }
                }
            }

            idsPtr++;
            if (cur == r) break;
            prv = cur;
            cur = par[cur];
        }

        // 長鏈上のマージされた値 g と遷移係数を畳み込む。
        int transLen = Math.min(trans.length, g.length);
        if (transLen > 0) {
            long[] transSub = new long[transLen];
            System.arraycopy(trans, 0, transSub, 0, transLen);

            long[] conv = convolver.apply(g, transSub);

            cur = leaf;
            int i = 0;
            while (true) {
                long val = (i < conv.length) ? conv[i] % mod : 0;
                ret[cur] = (ret[cur] + val) % mod;
                if (h >= 0) {
                    ret[h] = (ret[h] - val + mod) % mod;
                    h = (par[h] >= 0) ? par[h] : -1;
                }

                if (cur == r) break;
                cur = (par[cur] >= 0) ? par[cur] : -1;
                if (cur == -1) break;
                i++;
            }
        }

        return g;
    }

    /**
     * 転置畳み込み（根から葉方向への畳み込み）を実行します。
     * 各頂点 u について、その先祖 v との距離に応じた重み付き和を計算します：
     * ret[u] = sum_{v in ancestors(u)} f[v] * trans[dist(u, v)]
     *
     * @param f         各頂点の初期値。
     * @param trans     遷移係数。trans[d] は距離 d の頂点に対する重み。
     * @param mod       法。
     * @param convolver 2つの long 配列の畳み込みを行う関数（例：NTT）。
     * @return 畳み込み結果の配列。ret[u] = sum_{v in ancestors(u)} f[v] * trans[dist(u, v)] (mod mod)。
     */
    public long[] runTransposed(long[] f, long[] trans, long mod, BiFunction<long[], long[], long[]> convolver) {
        if (N == 0) return new long[0];
        long[] ret = new long[N];
        runRecTransposed(root, -1, null, ret, f, trans, mod, convolver);
        return ret;
    }

    /**
     * 転置畳み込みの再帰的処理（トップダウン）。
     *
     * @param r          現在の部分木の根。
     * @param h          親パスから引き継がれる境界頂点。
     * @param inheritedG 親パスから引き継がれる畳み込みの途中結果。
     * @param ret        結果を格納する配列。
     * @param f          各頂点の値。
     * @param trans      遷移係数。
     * @param mod        法。
     * @param convolver  畳み込み関数。
     */
    private void runRecTransposed(int r, int h, long[] inheritedG, long[] ret, long[] f, long[] trans, long mod, BiFunction<long[], long[], long[]> convolver) {
        int leaf = farthestLeaf[r];
        int d = depth[leaf] - depth[r] + 1;
        long[] cPrime = new long[d];
        int[] ids = new int[d];

        int idsPtr = 0;
        int cur = leaf;
        int tempH = h;
        // 長鏈上の頂点を収集し、転置された「境界更新」を適用した cPrime を作成。
        while (true) {
            ids[idsPtr] = cur;
            long fVal = (f[cur] % mod + mod) % mod;
            long hVal = (tempH >= 0) ? (f[tempH] % mod + mod) % mod : 0;
            cPrime[idsPtr] = (fVal - hVal + mod) % mod;

            if (tempH >= 0) tempH = (par[tempH] >= 0) ? par[tempH] : -1;
            if (cur == r) break;
            cur = par[cur];
            idsPtr++;
        }

        // cPrime と trans の転置畳み込み（相関）を計算。
        int transLen = Math.min(trans.length, d);
        long[] g = new long[d];
        if (transLen > 0) {
            long[] transSub = new long[transLen];
            System.arraycopy(trans, 0, transSub, 0, transLen);

            long[] cRev = new long[d];
            for (int i = 0; i < d; i++) cRev[i] = cPrime[d - 1 - i];

            long[] conv = convolver.apply(cRev, transSub);
            for (int i = 0; i < d; i++) {
                g[i] = (i < conv.length) ? conv[i] % mod : 0;
            }
            // 相関を得るために結果を反転。
            for (int i = 0; i < d / 2; i++) {
                long tmp = g[i];
                g[i] = g[d - 1 - i];
                g[d - 1 - i] = tmp;
            }
        }

        // 親から引き継いだ値をマージ。
        if (inheritedG != null) {
            for (int i = 0; i < d; i++) {
                g[i] = (g[i] + inheritedG[i]) % mod;
            }
        }

        // 自身のパス上の頂点の結果を確定。
        for (int i = 0; i < d; i++) {
            ret[ids[i]] = g[i];
        }

        // 子のパスへ再帰的に処理を伝播。
        for (int i = 0; i < d; i++) {
            for (int nxt : children[ids[i]]) {
                if (i > 0 && nxt == ids[i - 1]) continue; // 長鏈の続きはスキップ

                int nxtLen = depth[farthestLeaf[nxt]] - depth[ids[i]];
                int childH = (i - nxtLen >= 0) ? ids[i - nxtLen] : -1;

                long[] subG = new long[nxtLen];
                System.arraycopy(g, i - nxtLen, subG, 0, nxtLen);
                runRecTransposed(nxt, childH, subG, ret, f, trans, mod, convolver);
            }
        }
    }
}
