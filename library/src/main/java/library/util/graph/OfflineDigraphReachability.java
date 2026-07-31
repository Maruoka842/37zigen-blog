package library.util.graph;

import library.util.collections.IntArrayList;

/**
 * 有向グラフにおける到達可能性クエリをオフラインで高速に処理するクラスです。
 * クエリを64個ずつまとめてbitsetで伝播させることで計算量を削減しています。
 * SCC分解またはDAGのトポロジカル順序を利用し、自明なケースを除外して処理します。
 * 計算量: O((N + M) + Q' * N / 64) (Q' は非自明なクエリ数)
 */
public class OfflineDigraphReachability {
    private final Digraph g;
    private final IntArrayList queryA;
    private final IntArrayList queryB;

    /**
     * 指定された有向グラフに対して到達可能性判定を行うオブジェクトを構築します。
     * @param g 有向グラフ
     */
    public OfflineDigraphReachability(Digraph g) {
        this.g = g;
        this.queryA = new IntArrayList();
        this.queryB = new IntArrayList();
    }

    /**
     * 到達可能性を判定したい頂点ペア (u, v) をクエリとして追加します。
     * @param u 始点頂点
     * @param v 終点頂点
     */
    public void addQuery(int u, int v) {
        queryA.add(u);
        queryB.add(v);
    }

    /**
     * 追加された全てのクエリに対して到達可能性を計算します。
     *
     * @return 各クエリに対する到達可能性（true: 到達可能, false: 到達不能）
     */
    public boolean[] solve() {
    	//https://atcoder.jp/contests/typical90/submissions/77235985
        int Q = queryA.size();
        boolean[] res = new boolean[Q];
        if (Q == 0) return res;

        int[] col; // col[u] は頂点 u の所属するコンポーネント（または頂点そのもの）のトポロジカル順序（ランク）
        int[] topoOrder; // topoOrder[i] はランク i のコンポーネント ID（または頂点 ID）
        Digraph propG; // 伝播に使用する隣接リストを持つグラフ
        boolean isDag = g instanceof DAG;

        if (isDag) {
            DAG dag = (DAG) g;
            topoOrder = dag.topologicalOrder();
            col = new int[g.N];
            for (int i = 0; i < g.N; i++) col[topoOrder[i]] = i;
            propG = dag;
        } else {
            Digraph.sccDAGResult scc = g.sccDAG();
            col = scc.col();
            propG = scc.g();
            propG.removeMultipleEdges();
            int nCond = propG.N;
            topoOrder = new int[nCond];
            for (int i = 0; i < nCond; i++) topoOrder[i] = i;
        }
        int N = propG.N;

        // 自明なクエリを判定し、非自明なクエリ（ランクが小さいものから大きいものへのクエリ）のみを抽出
        IntArrayList activeIndices = new IntArrayList();
        for (int j = 0; j < Q; j++) {
            int u = queryA.get(j);
            int v = queryB.get(j);
            if (col[u] == col[v]) {
                res[j] = true; // 同じSCC内（DAGなら u == v）なので到達可能
            } else if (col[u] < col[v]) {
                activeIndices.add(j);
            } else {
                res[j] = false; // トポロジカル順序が逆なので到達不能
            }
        }

        int activeQ = activeIndices.size();
        // 非自明なクエリを64個ずつまとめて処理
        for (int k = 0; k < activeQ; k += 64) {
            int end = Math.min(k + 64, activeQ);
            long[] reach = new long[N]; // reach[i] はランク i の到達状態

            // バッチ内のクエリの始点をセット
            for (int j = k; j < end; j++) {
                int qIdx = activeIndices.get(j);
                reach[col[queryA.get(qIdx)]] |= (1L << (j - k));
            }

            // トポロジカル順（ランク 0, 1, ..., N-1）に伝播
            for (int i = 0; i < N; i++) {
                if (reach[i] == 0) continue;
                int u = topoOrder[i];
                for (int nextIdx = 0; nextIdx < propG.adj[u].size(); nextIdx++) {
                    int v = propG.adj[u].get(nextIdx);
                    int targetRank = isDag ? col[v] : v;
                    reach[targetRank] |= reach[i];
                }
            }

            // 結果を抽出
            for (int j = k; j < end; j++) {
                int qIdx = activeIndices.get(j);
                if (((reach[col[queryB.get(qIdx)]] >> (j - k)) & 1L) != 0) {
                    res[qIdx] = true;
                }
            }
        }

        return res;
    }
}
