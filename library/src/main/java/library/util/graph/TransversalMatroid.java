package library.util.graph;

import java.util.Arrays;

import library.util.collections.IntArrayList;
import library.util.collections.IntQueue;

/**
 * 横断マトロイド（Transversal Matroid）を表すクラス。
 * 二部グラフの片側の頂点集合の、マッチング可能な部分集合を独立集合とする。
 * 未テスト
 */
public class TransversalMatroid implements Matroid {
    private final int U, V;
    private final IntArrayList[] adj;
    private final int[] matchU;
    private final int[] matchV;

    /**
     * U 側の頂点数、V 側の頂点数、および二部グラフの辺集合を指定して構築する。
     * @param U U 側の頂点数
     * @param V V 側の頂点数
     * @param edges 辺集合 (u, v) のリスト。0 <= u < U, 0 <= v < V
     */
    public TransversalMatroid(int U, int V, int[][] edges) {
        this.U = U;
        this.V = V;
        this.adj = new IntArrayList[U];
        for (int i = 0; i < U; i++) adj[i] = new IntArrayList();
        for (int[] e : edges) {
            adj[e[0]].add(e[1]);
        }
        this.matchU = new int[U];
        this.matchV = new int[V];
        Arrays.fill(matchU, -1);
        Arrays.fill(matchV, -1);
    }

    @Override
    public int size() {
        return U;
    }

    @Override
    public void set(boolean[] I) {
        BipartiteMatching bm = new BipartiteMatching(U, V);
        for (int u = 0; u < U; u++) {
            if (I[u]) {
                for (int i = 0; i < adj[u].size(); i++) {
                    bm.addEdge(u, adj[u].get(i));
                }
            }
        }
        bm.calc();
        for (int u = 0; u < U; u++) {
            matchU[u] = bm.fromLtoR(u);
        }
        for (int v = 0; v < V; v++) {
            matchV[v] = bm.fromRtoL(v);
        }
    }

    @Override
    public IntArrayList circuit(int e) {
        if (matchU[e] != -1) return new IntArrayList();

        IntQueue q = new IntQueue();
        q.add(e);

        boolean[] reachedU = new boolean[U];
        boolean[] reachedV = new boolean[V];
        reachedU[e] = true;

        boolean foundAugmenting = false;
        while (!q.isEmpty()) {
            int curr = q.poll();
            for (int i = 0; i < adj[curr].size(); i++) {
                int v = adj[curr].get(i);
                if (!reachedV[v]) {
                    reachedV[v] = true;
                    if (matchV[v] == -1) {
                        foundAugmenting = true;
                        break;
                    }
                    int nextU = matchV[v];
                    if (!reachedU[nextU]) {
                        reachedU[nextU] = true;
                        q.add(nextU);
                    }
                }
            }
            if (foundAugmenting) break;
        }

        if (foundAugmenting) return new IntArrayList();

        IntArrayList res = new IntArrayList();
        for (int u = 0; u < U; u++) {
            if (reachedU[u]) res.add(u);
        }
        return res;
    }
}
