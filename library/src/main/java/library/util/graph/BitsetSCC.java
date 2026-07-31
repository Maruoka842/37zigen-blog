package library.util.graph;

import java.util.Arrays;

import library.util.collections.IntArrayList;

/**
 * bitsetを用いた強連結成分分解 (SCC) クラスです。
 * 稠密なグラフに対して高速に動作します。
 * 計算量: O(V^2/64)
 */
public class BitsetSCC {
    /** 頂点数 */
    private final int V;
    /** bitset(long配列)の長さ */
    private final int words;
    /** 隣接行列 (bitset形式) */
    private final long[][] e;
    /** 逆辺の隣接行列 (bitset形式) */
    private final long[][] einv;
    /** 各頂点が属する強連結成分のID */
    private final int[] cmp;
    /** 帰りがけ順の頂点リスト */
    private final IntArrayList vs;
    /** 訪問済み頂点を管理するbitset */
    private final long[] visited;
    /** 強連結成分の総数 */
    private int sccNum;
    /** DFS/RDFS用のスタック */
    private final IntArrayList st;

    /**
     * 頂点数と隣接行列（bitset形式）を指定してSCCを計算します。
     * @param V 頂点数
     * @param e 隣接行列 e[i][j>>6] & (1L << (j&63)) が辺 i->j の存在を表す
     * @param einv 逆辺の隣接行列
     */
    public BitsetSCC(int V, long[][] e, long[][] einv) {
        this.V = V;
        this.words = (V + 63) / 64;
        this.e = e;
        this.einv = einv;
        this.cmp = new int[V];
        this.vs = new IntArrayList(V);
        this.visited = new long[words];
        this.st = new IntArrayList(V);
        this.sccNum = 0;

        calc();
    }

    /**
     * DigraphオブジェクトからSCCを計算します。
     * @param g 有向グラフ
     */
    public BitsetSCC(Digraph g) {
        this.V = g.N;
        this.words = (V + 63) / 64;
        this.e = new long[V][words];
        this.einv = new long[V][words];
        for (int i = 0; i < V; i++) {
            for (int jIdx = 0; jIdx < g.adj[i].size(); jIdx++) {
                int j = g.adj[i].get(jIdx);
                e[i][j >> 6] |= 1L << (j & 63);
                einv[j][i >> 6] |= 1L << (i & 63);
            }
        }
        this.cmp = new int[V];
        this.vs = new IntArrayList(V);
        this.visited = new long[words];
        this.st = new IntArrayList(V);
        this.sccNum = 0;

        calc();
    }

    /**
     * SCCの計算を実行します。
     */
    private void calc() {
        Arrays.fill(visited, 0L);

        for (int i = 0; i < V; i++) {
            if ((visited[i >> 6] & (1L << (i & 63))) == 0) {
                dfs(i);
            }
        }

        Arrays.fill(visited, 0L);
        Arrays.fill(cmp, -1);

        for (int i = V - 1; i >= 0; i--) {
            int v = vs.get(i);
            if ((visited[v >> 6] & (1L << (v & 63))) == 0) {
                rdfs(v, sccNum++);
            }
        }
    }

    /**
     * 1回目のDFSを行い、探索が終わった順に頂点をリスト {@code vs} に追加します。
     * これにより、{@code vs} を逆順に辿ることでトポロジカル順に近い順序で探索できます。
     * @param head 開始頂点
     */
    private void dfs(int head) {
        st.clear();
        st.add(head);
        visited[head >> 6] |= 1L << (head & 63);
        while (st.isNonEmpty()) {
            int now = st.peekLast();
            int nxt = -1;
            for (int i = 0; i < words; i++) {
                long intersection = (~visited[i]) & e[now][i];
                if (intersection != 0) {
                    nxt = i * 64 + Long.numberOfTrailingZeros(intersection);
                    break;
                }
            }

            if (nxt != -1 && nxt < V) {
                visited[nxt >> 6] |= 1L << (nxt & 63);
                st.add(nxt);
            } else {
                st.pollLast();
                vs.add(now);
            }
        }
    }

    /**
     * 2回目のDFSを逆向きの辺を用いて行い、到達可能な頂点に強連結成分のID {@code k} を割り振ります。
     * 逆向きの辺を用いることで、同じ強連結成分に属する頂点のみを抽出します。
     * @param head 開始頂点
     * @param k 強連結成分のID
     */
    private void rdfs(int head, int k) {
        st.clear();
        st.add(head);
        visited[head >> 6] |= 1L << (head & 63);
        while (st.isNonEmpty()) {
            int now = st.pollLast();
            cmp[now] = k;
            while (true) {
                int nxt = -1;
                for (int i = 0; i < words; i++) {
                    long intersection = (~visited[i]) & einv[now][i];
                    if (intersection != 0) {
                        nxt = i * 64 + Long.numberOfTrailingZeros(intersection);
                        break;
                    }
                }
                if (nxt == -1 || nxt >= V) break;
                st.add(nxt);
                visited[nxt >> 6] |= 1L << (nxt & 63);
            }
        }
    }

    /**
     * 各頂点が属する強連結成分のIDを返します。
     * IDはトポロジカル順に割り振られています。
     * @return 頂点ごとのコンポーネントID
     */
    public int[] getCmp() {
        return cmp;
    }

    /**
     * 強連結成分の総数を返します。
     * @return SCCの数
     */
    public int getSccNum() {
        return sccNum;
    }

    /**
     * 各強連結成分に属する頂点リストの配列を返します。
     * 配列の順序はトポロジカル順（親が先）です。
     * @return 強連結成分ごとの頂点リスト
     */
    public IntArrayList[] getComponents() {
        IntArrayList[] res = new IntArrayList[sccNum];
        for (int i = 0; i < sccNum; i++) res[i] = new IntArrayList();
        for (int i = 0; i < V; i++) {
            res[cmp[i]].add(i);
        }
        return res;
    }
}
