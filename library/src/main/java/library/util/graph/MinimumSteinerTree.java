package library.util.graph;

import library.util.collections.MyPriorityQueue;
import library.util.graph.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import library.util.unionfind.UnionFind;

/**
 * 無向グラフの最小シュタイナー木を求めるクラス。
 * 与えられたターミナル集合をすべて含む、最小重みの部分グラフ（木）を求める。
 *
 * <p>以下の2つのアルゴリズムを使い分ける：
 * <ul>
 *   <li><b>DP (Dreyfus-Wagner アルゴリズム系):</b> 計算量 O(3^k n + 2^k m log n)。ターミナル数 k が小さい場合に有効。</li>
 *   <li><b>Dense (非ターミナル頂点全探索):</b> 計算量 O(2^{n-k} m alpha(n))。非ターミナル頂点数 n-k が小さい場合に有効。</li>
 * </ul>
 *
 * @see <a href="https://judge.yosupo.jp/problem/minimum_steiner_tree">Library Checker - Minimum Steiner Tree</a>
 */
public class MinimumSteinerTree {

    /**
     * 計算結果を保持するレコード。
     * @param cost 最小シュタイナー木の総コスト。
     * @param edges 最小シュタイナー木を構成する辺のリスト。
     */
    public record Result(long cost, List<Edge> edges) {}

    /**
     * 最小シュタイナー木を求める。
     * ターミナル数 k と非ターミナル数 n-k に基づいて、適切なアルゴリズムを選択する。
     *
     * @param g 無向グラフ
     * @param terminals ターミナルの頂点番号リスト
     * @return 最小シュタイナー木のコストと使用された辺のリスト
     */
    public static Result solve(LongValueGraph g, int[] terminals) {
        int n = g.N;
        int k = terminals.length;
        int m = g.M;

        if (k == 0) return new Result(0, Collections.emptyList());
        if (k == 1) return new Result(0, Collections.emptyList());

        // アルゴリズムの選択基準 (C++版に準拠)
        boolean useDP;
        if (n - k > 30) {
            useDP = true;
        } else if (k > 20) {
            useDP = false;
        } else {
            // 各アルゴリズムの計算量の概算を比較して有利な方を選択
            double dpCost = Math.pow(3, k) * n + Math.pow(2, k) * m * Math.log(m + 1);
            double denseCost = Math.pow(2, n - k) * m;
            useDP = dpCost < denseCost;
        }

        if (useDP) {
            // メモリ制限等のため k が大きすぎる場合は例外 (2^k の配列サイズが限界を超える)
            if (k > 20) {
                 throw new IllegalArgumentException("Terminal size k too large for DP: " + k);
            }
            return solveDP(g, terminals);
        } else {
            // 非ターミナル数が多い場合は全探索が現実的でない
            if (n - k > 30) {
                throw new IllegalArgumentException("Non-terminal size n-k too large for Dense: " + (n - k));
            }
            return solveDense(g, terminals);
        }
    }

    /**
     * Dreyfus-Wagner 法に基づいた DP による最小シュタイナー木。
     * ターミナル数 k が小さい場合に適している。
     * dp[mask][v]: ターミナルの部分集合 mask を含み、頂点 v を根とするシュタイナー木の最小コスト
     */
    public static Result solveDP(LongValueGraph g, int[] terminals) {
        int n = g.N;
        int k = terminals.length;
        if (n == 0 || k == 0) return new Result(0, Collections.emptyList());
        if (k == 1) return new Result(0, Collections.emptyList());

        long INF = Long.MAX_VALUE / 3;
        long[][] dp = new long[1 << k][n];
        for (int i = 0; i < (1 << k); i++) Arrays.fill(dp[i], INF);

        // 復元用情報: 経路を遡るための情報を保持
        byte[][] type = new byte[1 << k][n]; // 1: TERMINAL (初期状態), 2: SPLIT (同一頂点での合体), 3: EDGE (隣接頂点からの移動)
        int[][] prv1 = new int[1 << k][n]; // SPLIT時は部分集合 mask1, EDGE時は移動前の頂点 u
        int[][] prv2 = new int[1 << k][n]; // SPLIT時は部分集合 mask2, EDGE時は g.adj[u] 内の隣接辺インデックス

        // 初期状態: 各ターミナル単体（1点のみ）のコストは0
        for (int j = 0; j < k; j++) {
            dp[1 << j][terminals[j]] = 0;
            type[1 << j][terminals[j]] = 1; // TERMINAL
        }

        // 集合サイズが小さい方から順に埋めていく
        for (int s = 1; s < (1 << k); s++) {
            // 1. 同一頂点での分割・合体 (Dreyfus-Wagner 遷移)
            // dp[s][i] = min(dp[t][i] + dp[s ^ t][i])
            // 同じ頂点 i を根とする2つのシュタイナー木（それぞれ集合 t と s\t をカバー）を合体させる。
            for (int t = (s - 1) & s; t > 0; t = (t - 1) & s) {
                if (t < (s ^ t)) continue; // 重複計算の回避 (t と s^t の入れ替えは同じ)
                long[] dpt = dp[t];
                long[] dpst = dp[s ^ t];
                long[] dps = dp[s];
                byte[] ts = type[s];
                int[] p1 = prv1[s];
                int[] p2 = prv2[s];
                for (int i = 0; i < n; i++) {
                    if (dpt[i] == INF || dpst[i] == INF) continue;
                    long newCost = dpt[i] + dpst[i];
                    if (newCost < dps[i]) {
                        dps[i] = newCost;
                        ts[i] = 2; // SPLIT
                        p1[i] = t;
                        p2[i] = s ^ t;
                    }
                }
            }

            // 2. 頂点間を跨ぐ遷移 (Dijkstra による更新)
            // dp[s][v] = min(dp[s][u] + cost(u, v))
            // 集合 s をカバーする頂点 u を根とする木に、辺 (u, v) を追加して根を v に移動させる。
            MyPriorityQueue<long[]> pq = new MyPriorityQueue<>();
            long[] dps = dp[s];
            for (int i = 0; i < n; i++) {
                if (dps[i] != INF) pq.add(new long[]{dps[i], i});
            }

            while (!pq.isEmpty()) {
                long[] curr = pq.poll();
                long cost = curr[0];
                int u = (int) curr[1];
                if (cost > dps[u]) continue;

                ArrayList<Edge> adjU = g.adj[u];
                for (int i = 0; i < adjU.size(); i++) {
                    Edge e = adjU.get(i);
                    int v = e.dst;
                    long nextCost = cost + e.cost;
                    if (nextCost < dps[v]) {
                        dps[v] = nextCost;
                        type[s][v] = 3; // EDGE
                        prv1[s][v] = u;
                        prv2[s][v] = i;
                        pq.add(new long[]{nextCost, v});
                    }
                }
            }
        }

        // 全てのターミナルを含む最小コストを探す（根はどの頂点でも良い）
        long minCost = INF;
        int bestV = -1;
        for (int i = 0; i < n; i++) {
            if (dp[(1 << k) - 1][i] < minCost) {
                minCost = dp[(1 << k) - 1][i];
                bestV = i;
            }
        }

        if (bestV == -1) return new Result(INF, Collections.emptyList());

        // 使用した辺を再帰的に復元
        List<Edge> usedEdges = new ArrayList<>();
        boolean[][] visited = new boolean[1 << k][n];
        reconstruct(bestV, (1 << k) - 1, g, type, prv1, prv2, usedEdges, visited);

        return new Result(minCost, usedEdges);
    }

    /**
     * DPの復元用情報を辿って使用された辺を収集する。
     */
    private static void reconstruct(int v, int s, LongValueGraph g, byte[][] type, int[][] prv1, int[][] prv2, List<Edge> usedEdges, boolean[][] visited) {
        if (s == 0 || visited[s][v]) return;
        visited[s][v] = true;
        if (type[s][v] == 1) return; // TERMINAL: 葉に到達

        if (type[s][v] == 2) { // SPLIT: 同一頂点での集合分割を再帰的に辿る
            reconstruct(v, prv1[s][v], g, type, prv1, prv2, usedEdges, visited);
            reconstruct(v, prv2[s][v], g, type, prv1, prv2, usedEdges, visited);
        } else if (type[s][v] == 3) { // EDGE: 隣接頂点からの遷移を辿り、辺を追加
            int u = prv1[s][v];
            int adjIdx = prv2[s][v];
            usedEdges.add(g.adj[u].get(adjIdx));
            reconstruct(u, s, g, type, prv1, prv2, usedEdges, visited);
        }
    }

    /**
     * 非ターミナル頂点のサブセットを全探索し、MST を求める手法。
     * 非ターミナル数が少ない場合に有効。
     */
    public static Result solveDense(LongValueGraph g, int[] terminals) {
        int n = g.N;
        int k = terminals.length;
        if (n == 0 || k == 0) return new Result(0, Collections.emptyList());
        if (k == 1) return new Result(0, Collections.emptyList());

        // 非ターミナル頂点のリストを作成
        boolean[] isTerminal = new boolean[n];
        for (int t : terminals) isTerminal[t] = true;

        List<Integer> nonTerminals = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!isTerminal[i]) nonTerminals.add(i);
        }

        int m_non = nonTerminals.size();
        long minCost = Long.MAX_VALUE / 3;
        List<Edge> bestEdges = Collections.emptyList();

        // クラスカル法の準備として全ての辺を重みの昇順にソート
        List<Edge> allEdges = g.edges();
        Collections.sort(allEdges);

        // 非ターミナルのどの頂点を使用するかをビット全探索 (2^(n-k))
        // 使用する頂点集合を固定すれば、その中での最小全域木がシュタイナー木の候補となる
        for (int i = 0; i < (1 << m_non); i++) {
            boolean[] use = new boolean[n];
            for (int t : terminals) use[t] = true; // ターミナルは必ず使用
            for (int j = 0; j < m_non; j++) {
                if (((i >> j) & 1) == 1) {
                    use[nonTerminals.get(j)] = true; // 非ターミナルの一部を選択
                }
            }

            // 選んだ頂点集合における最小全域木を求める (クラスカル法)
            UnionFind uf = new UnionFind(n);
            long cost = 0;
            List<Edge> edges = new ArrayList<>();
            for (Edge e : allEdges) {
                // 両端点が使用対象に含まれる辺のみを考慮
                if (use[e.src] && use[e.dst]) {
                    if (!uf.equiv(e.src, e.dst)) {
                        uf.union(e.src, e.dst);
                        cost += e.cost;
                        edges.add(e);
                    }
                }
            }

            // 選んだ頂点集合で全てのターミナルが連結されているか確認
            boolean connected = true;
            for (int j = 1; j < k; j++) {
                if (!uf.equiv(terminals[0], terminals[j])) {
                    connected = false;
                    break;
                }
            }

            // 連結であり、かつ最小コストを更新していれば記録
            if (connected && cost < minCost) {
                minCost = cost;
                bestEdges = edges;
            }
        }

        return new Result(minCost, bestEdges);
    }
}
