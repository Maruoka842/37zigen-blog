package library.util.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.util.collections.IntArrayList;
import library.util.collections.MeldableSkewHeap;
import library.util.unionfind.UnionFind;

/**
 * 最小有向全域木 (Minimum Directed Spanning Tree / Spanning Arborescence) を求めるクラス。
 * Chu-Liu/Edmonds のアルゴリズムを使用する。
 * 計算量: O(M log N)
 *
 * 参考:
 * [1] R. E. Tarjan, "Finding optimum branchings," Networks 7.1 (1977): 25-35.
 */
public class MinimumDirectedSpanningTree {

    /**
     * 最小有向全域木の計算結果を保持するクラス。
     */
    public static class Result {
        /**
         * 最小有向全域木の総重み。
         */
        public final long weight;
        /**
         * 最小有向全域木に含まれる辺のインデックス配列。
         */
        public final int[] edgeIndices;

        /**
         * 結果オブジェクトを構築する。
         * @param weight 総重み
         * @param edgeIndices 採用された辺のインデックス
         */
        public Result(long weight, int[] edgeIndices) {
            this.weight = weight;
            this.edgeIndices = edgeIndices;
        }
    }

    /**
     * 根から外向きの最小有向全域木を求める。
     * @param N 頂点数
     * @param edges 辺のリスト
     * @param root 根となる頂点
     * @return 最小重みと採用された辺のインデックス。到達不能な頂点がある場合は null を返す。
     */
    public static Result solveFromRoot(int N, List<Edge> edges, int root) {
        if (N <= 0) return null;
        if (N == 1) return new Result(0, new int[0]);

        MeldableSkewHeap.Node[] heaps = new MeldableSkewHeap.Node[2 * N];
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (e.src == e.dst || e.dst == root) continue;
            heaps[e.dst] = MeldableSkewHeap.meld(heaps[e.dst], new MeldableSkewHeap.Node(e.cost, i));
        }

        UnionFind uf = new UnionFind(2 * N);
        int[] used = new int[2 * N];
        int[] group = new int[2 * N];
        for (int i = 0; i < 2 * N; i++) group[i] = i;

        int[] contractionParent = new int[2 * N];
        int[] nodeIncomingEdge = new int[2 * N];
        long[] edgeWeightAtContraction = new long[2 * N];
        Arrays.fill(contractionParent, -1);
        Arrays.fill(nodeIncomingEdge, -1);
        Arrays.fill(used, 0);
        used[root] = 2;

        int nextNode = N;
        for (int i = 0; i < N; i++) {
            int startGroup = group[uf.root(i)];
            if (used[startGroup] != 0) continue;

            int curr = startGroup;
            IntArrayList path = new IntArrayList();
            while (used[curr] != 2) {
                used[curr] = 1;
                path.add(curr);
                if (heaps[curr] == null) return null;

                MeldableSkewHeap.Node top = heaps[curr];
                heaps[curr] = MeldableSkewHeap.pop(heaps[curr]);
                int id = top.id;
                long w = top.weight();
                int srcGroup = group[uf.root(edges.get(id).src)];

                if (srcGroup == curr) continue;

                nodeIncomingEdge[curr] = id;
                edgeWeightAtContraction[curr] = w;

                if (used[srcGroup] == 1) {
                    int newNode = nextNode++;
                    IntArrayList cycle = new IntArrayList();
                    int p = curr;
                    while (p != -1 && contractionParent[p] == -1) {
                        contractionParent[p] = newNode;
                        cycle.add(p);
                        p = group[uf.root(edges.get(nodeIncomingEdge[p]).src)];
                    }
                    for (int j = 0; j < cycle.size(); j++) {
                        int node = cycle.get(j);
                        if (heaps[node] != null) heaps[node].add -= edgeWeightAtContraction[node];
                        heaps[newNode] = MeldableSkewHeap.meld(heaps[newNode], heaps[node]);
                        uf.union(node, newNode);
                    }
                    group[uf.root(newNode)] = newNode;
                    curr = newNode;
                } else {
                    curr = srcGroup;
                }
            }
            for (int j = 0; j < path.size(); j++) {
                int v = path.get(j);
                while (v != -1 && used[v] != 2) {
                    used[v] = 2;
                    v = contractionParent[v];
                }
            }
        }

        boolean[] picked = new boolean[nextNode];
        int[] finalEdges = new int[N];
        Arrays.fill(finalEdges, -1);
        picked[root] = true;

        for (int i = nextNode - 1; i >= 0; i--) {
            if (i != root && !picked[i] && nodeIncomingEdge[i] != -1) {
                int id = nodeIncomingEdge[i];
                finalEdges[edges.get(id).dst] = id;
                int v = edges.get(id).dst;
                while (v != -1 && !picked[v]) {
                    picked[v] = true;
                    v = contractionParent[v];
                }
            }
        }

        long totalWeight = 0;
        IntArrayList res = new IntArrayList();
        for (int i = 0; i < N; i++) {
            if (i != root) {
                if (finalEdges[i] == -1) return null;
                res.add(finalEdges[i]);
                totalWeight += edges.get(finalEdges[i]).cost;
            }
        }
        return new Result(totalWeight, res.toArray());
    }

    /**
     * 根に向かう最小有向全域木を求める。
     * @param N 頂点数
     * @param edges 辺のリスト
     * @param root 根となる頂点
     * @return 最小重みと採用された辺のインデックス。到達不能な頂点がある場合は null を返す。
     */
    public static Result solveToRoot(int N, List<Edge> edges, int root) {
        List<Edge> reversedEdges = new ArrayList<>(edges.size());
        for (Edge e : edges) {
            reversedEdges.add(new Edge(e.dst, e.src, e.cost));
        }
        return solveFromRoot(N, reversedEdges, root);
    }
}