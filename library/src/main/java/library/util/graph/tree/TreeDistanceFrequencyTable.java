package library.util.graph.tree;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;

/**
 * 木の全頂点対間距離の頻度（または重み付き和）を求めるクラス。
 *
  * 各距離 d について、dist(u, v) = d となる全頂点対 {u, v} (u != v, 順序なし) に対する
 * weight[u] * weight[v] の総和を計算します。
 *
 * <h3>アルゴリズム</h3>
 * 重心分解 (Centroid Decomposition) を用いることで効率的に計算を行います。
 * 重心 c を選んだとき、c を跨ぐ（または c を端点とする）パスの重み付き和は、
 * c の各隣接部分木から得られる距離ごとの重み分布を畳み込むことで求められます。
 *
 *
 * <h3>空間計算量</h3>
 * 重心分解の各ステップで必要な配列のサイズの総和は O(N) です。
 */
public class TreeDistanceFrequencyTable {
    private final Tree tree;

    /**
     * TreeDistanceFrequencyTable のコンストラクタ。
     * @param tree 対象となる木。
     */
    public TreeDistanceFrequencyTable(Tree tree) {
        this.tree = tree;
    }

    /**
     * 各距離における重み付き和を計算します。
     *
     * <p>結果の配列 ret について、ret[d] は距離 d の順序なし頂点対 {u, v} (u != v) に対する
     * weight[u] * weight[v] の和を格納します。</p>
     *
     * @param weight 各頂点の重み。
     * @param mod 法。
     * @param convolver 2つの long 配列の畳み込みを行う関数（例：NTT）。
     *                  入力 a, b に対して、c[k] = sum_{i+j=k} a[i]*b[j] を返すことを期待します。
     * @return 結果の配列 ret。ret[d] は距離 d の頂点対の重み付き和。
     */
    public long[] solve(long[] weight, long mod, BiFunction<long[], long[], long[]> convolver) {
        int n = tree.N;
        if (n == 0) return new long[0];
        // alive[i] は重心分解においてまだ取り除かれていない（現在の連結成分に含まれる）頂点か
        boolean[] alive = new boolean[n];
        Arrays.fill(alive, true);
        long[] ret = new long[n];

        // 各距離 d における順序なし頂点対 {u, v} (u != v) の weight[u]*weight[v] の和を計算します。
        // 自己ループを含めたり、順序付きペアを数えたりする場合は、呼び出し側で適宜補正してください。
        // ここでは重心分解の各ステップにおいて重心を跨ぐパスを 1 回ずつカウントします。

        // 重心分解を行い、重心を順に取得
        Tree centroidTree = tree.centroidDecomposition();
        int[] centroids = centroidTree.bfsOrder();

        for (int root : centroids) {
            // 現在の重心を削除し、この重心を跨ぐパスを数え上げる
            alive[root] = false;
            // 部分木ごとの重み分布（距離ごとの合計重み）を管理する。短いものから順にマージするための PriorityQueue。
            PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a.length, b.length));
            long wRoot = (weight[root] % mod + mod) % mod;

            for (int nxt : tree.adj[root]) {
                if (!alive[nxt]) continue;

                // 部分木内の距離ごとの合計重みを収集
                // v[i] は重心 root からの距離が i+1 の頂点の重み合計
                long[] v = collectDistances(nxt, root, alive, weight, mod);

                // 重心 root を片方の端点とするパス (root, u) を処理
                for (int i = 0; i < v.length; i++) {
                    long val = v[i] * wRoot % mod;
                    // v[i] は重心からの距離が i+1 の頂点の重み合計。全体では距離 i+1 となる。
                    if (i + 1 < n) {
                        ret[i + 1] = (ret[i + 1] + val) % mod;
                    }
                }
                pq.add(v);
            }

            // ハフマンマージ: 短い配列から順に畳み込むことで、計算量を最適化する。
            // 2つの異なる部分木から1点ずつ選ぶパス（重心 root を跨ぐパス）を数え上げる。
            while (pq.size() >= 2) {
                long[] a = pq.poll();
                long[] b = pq.poll();

                // 畳み込み c[i] = sum_{x+y=i} a[x] * b[y]
                long[] c = convolver.apply(a, b);
                for (int i = 0; i < c.length; i++) {
                    // a[x] は重心からの距離 x+1, b[y] は重心からの距離 y+1
                    // 全体の距離は (x+1) + (y+1) = x+y+2 = i+2
                    // 畳み込み結果 c[i] において i = x+y なので、全体の距離は i+2
                    if (i + 2 < n) {
                        ret[i + 2] = (ret[i + 2] + c[i]) % mod;
                    }
                }

                // a と b の分布を合算して PriorityQueue に戻す
                long[] nextSum = new long[Math.max(a.length, b.length)];
                for (int i = 0; i < nextSum.length; i++) {
                    long s = 0;
                    if (i < a.length) s = (s + a[i]) % mod;
                    if (i < b.length) s = (s + b[i]) % mod;
                    nextSum[i] = s;
                }
                pq.add(nextSum);
            }
        }
        return ret;
    }

    /**
     * 重心分解の各ステップで、特定の隣接部分木内の頂点の距離分布を収集します。
     * BFS により、深い木での StackOverflowError を回避します。
     *
     * @param startNode 探索を開始する頂点（部分木の根）。
     * @param root 重心分解における現在の重心。
     * @param alive 重心分解において、まだ重心として選ばれていない（探索対象となる）頂点かどうかを示す配列。
     * @param weight 各頂点の重み。
     * @param mod 法。
     * @return 距離ごとの合計重み配列。
     */
    private long[] collectDistances(int startNode, int root, boolean[] alive, long[] weight, long mod) {
        IntArrayList nodes = new IntArrayList();
        IntArrayList depths = new IntArrayList();

        IntDeque nodeQueue = new IntDeque();
        IntDeque parentQueue = new IntDeque();
        IntDeque depthQueue = new IntDeque();

        nodeQueue.addLast(startNode);
        parentQueue.addLast(root);
        depthQueue.addLast(0);

        int maxDepth = 0;
        while(!nodeQueue.isEmpty()) {
            int u = nodeQueue.pollFirst();
            int p = parentQueue.pollFirst();
            int d = depthQueue.pollFirst();

            nodes.add(u);
            depths.add(d);
            if (d > maxDepth) maxDepth = d;

            for (int v : tree.adj[u]) {
                if (v != p && alive[v]) {
                    nodeQueue.addLast(v);
                    parentQueue.addLast(u);
                    depthQueue.addLast(d + 1);
                }
            }
        }

        // 距離ごとの重み合計を配列に格納
        long[] res = new long[maxDepth + 1];
        for (int i = 0; i < nodes.size(); i++) {
            int u = nodes.get(i);
            int d = depths.get(i);
            long w = (weight[u] % mod + mod) % mod;
            res[d] = (res[d] + w) % mod;
        }
        return res;
    }
}
