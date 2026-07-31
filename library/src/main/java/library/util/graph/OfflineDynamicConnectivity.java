package library.util.graph;

import java.util.ArrayList;
import java.util.function.LongConsumer;

import library.util.ArrayUtils;
import library.util.MathUtils;
import library.util.collections.LongArrayList;
import library.util.seq.SortedArrays;
import library.util.unionfind.UndoUnionFind;

/**
 * オフラインダイナミックコネクティビティ。
 * 辺の追加・削除クエリをあらかじめ受け取り、時間軸上のセグメント木を用いて
 * 各時点での連結性をオフラインで計算する。
 * 時間軸は内部で座標圧縮されるため、大きな値や非連続な値も扱える。
 *
 * <h3>計算量</h3>
 * <ul>
 *   <li>時間計算量: O((N + Q + E log Q) log N)</li>
 *   <li>空間計算量: O(Q + E log Q)</li>
 * </ul>
 *
 */
public class OfflineDynamicConnectivity {
	//https://atcoder.jp/contests/past202012-open/submissions/76913910
    private final int N;
    private final ArrayList<long[]> edges = new ArrayList<>();
    private final LongArrayList queries = new LongArrayList();
    private UndoUnionFind uf;

    private long[] qs;
    private int size;
    private LongArrayList[] tree;
    private int currentIdx = -1;
    private int[] snapshots;//snapshots[i]=深さiのノードvの直前の状態。vにおけるunionはまだ。
    private boolean built = false;

    /**
     * N頂点のオフラインダイナミックコネクティビティを初期化する。
     * @param n 頂点数
     */
    public OfflineDynamicConnectivity(int n) {
        this.N = n;
    }

    /**
     * 時点 [l, r) の間、頂点 u と v を結ぶ辺が存在することを追加する。
     * @param u 頂点1
     * @param v 頂点2
     * @param l 開始時点（包含）
     * @param r 終了時点（排他）
     */
    public void addEdge(int u, int v, long l, long r) {
        if (l >= r) return;
        edges.add(new long[]{u, v, l, r});
    }

    /**
     * 時点 t におけるクエリを追加する。
     * @param t 時点
     */
    public void registerQueryTime(long t) {
        queries.add(t);
    }

    /**
     * 内部構造を構築する。
     * advanceTo を呼び出す前に必ず呼び出す必要がある。
     */
    public void build() {
        if (built) return;
        if (queries.isEmpty()) {
            built = true;
            return;
        }
        qs = queries.toArray();
        qs = ArrayUtils.sortq(qs);
        int Q = qs.length;

        size = 1;
        while (size < Q) size <<= 1;
        tree = new LongArrayList[2 * size];
        for (int i = 0; i < tree.length; i++) tree[i] = new LongArrayList();

        for (long[] e : edges) {
            int u = (int) e[0];
            int v = (int) e[1];
            if (u > v) { int tmp = u; u = v; v = tmp; }
            long edge = ((long) u << 32) | (v & 0xFFFFFFFFL);

            int l = SortedArrays.ceil(qs, e[2]);
            int r = SortedArrays.ceil(qs, e[3]);
            if (l >= r) continue;

            int treeL = l + size;
            int treeR = r + size;
            while (treeL < treeR) {
                if ((treeL & 1) == 1) tree[treeL++].add(edge);
                if ((treeR & 1) == 1) tree[--treeR].add(edge);
                treeL >>= 1;
                treeR >>= 1;
            }
        }

        uf = new UndoUnionFind(N);
        int h = Integer.numberOfTrailingZeros(size) + 1;
        snapshots = new int[h];
        currentIdx = -1;
        built = true;
    }

    /**
     * 指定した時点まで状態を進める。
     * 時点は昇順に指定する必要がある。
     * @param t 時点
     */
    public void advanceTo(long t) {
        if (!built) throw new IllegalStateException("build() must be called before advanceTo()");
        if (qs == null || qs.length == 0) return;
        int targetIdx = SortedArrays.floor(qs, t);
        if (targetIdx < currentIdx) {
            throw new IllegalArgumentException("Time must be non-decreasing");
        }
        if (targetIdx >= 0) {
            moveTo(targetIdx);
        }
    }

    private void moveTo(int targetIdx) {
        if (currentIdx == targetIdx) return;

        int h = snapshots.length;
        int targetLeaf = targetIdx + size;

        int commonDepth = 0;
        if (currentIdx >= 0) {
            int currentLeaf = currentIdx + size;
            commonDepth = commonDepth(currentLeaf, targetLeaf);
        }

        uf.rollback(snapshots[commonDepth]);

        for (int depth = commonDepth; depth < h; depth++) {
            int node = targetLeaf >> (h - 1 - depth);

            snapshots[depth] = uf.snapshot();

            LongArrayList edgesAtNode = tree[node];
            for (int j = 0; j < edgesAtNode.size(); j++) {
                long edge = edgesAtNode.get(j);
                uf.union((int) (edge >> 32), (int) edge);
            }
        }

        currentIdx = targetIdx;
    }

    private int commonDepth(int currentLeaf, int targetLeaf) {
        int h = snapshots.length;
        int x = currentLeaf ^ targetLeaf;
        if (x == 0) return h;
        return h - 1 - MathUtils.floorLog2(x);
    }


    /**
     * 現在の時点における UndoUnionFind を取得する。
     * callback 内で呼び出すことで、その時点の連結情報を取得できる。
     * @return UndoUnionFind
     */
    public UndoUnionFind getUnionFind() {
        return uf;
    }
}
