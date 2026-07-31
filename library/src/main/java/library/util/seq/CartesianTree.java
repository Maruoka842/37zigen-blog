package library.util.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import library.util.IntComparator;

/**
 * <p>
 * デカルト木 (Cartesian Tree) を構築するクラス。
 * </p>
 * <p>
 * 与えられた配列 $A$ に対し、以下の性質を満たす二分木を構築する：
 * </p>
 * <ul>
 *   <li>ヒーププロパティ：
 *     <ul>
 *       <li>Min-Heap: 各ノード $u$ の親 $p$ について $A[p] \le A[u]$</li>
 *       <li>Max-Heap: 各ノード $u$ の親 $p$ について $A[p] \ge A[u]$</li>
 *     </ul>
 *   </li>
 *   <li>中順巡回 (In-order Traversal) を行うと、元の配列 of indices $0, 1, \dots, N-1$ になる。</li>
 * </ul>
 *
 * <p>
 * 構築は線形時間 $O(N)$ で行われる。
 * </p>
 */
public class CartesianTree {
    /** ノードの総数 */
    public final int n;
    /** 各ノードの親のインデックス（親がない場合は -1） */
    public final int[] parent;
    /** 各ノードの左の子のインデックス（子がない場合は -1） */
    public final int[] lch;
    /** 各ノードの右の子のインデックス（子がない場合は -1） */
    public final int[] rch;
    /** 木の根のインデックス（ノード総数が 0 の場合は -1） */
    public final int root;

    /** キャッシュされた順序配列 */
    private int[] preOrder;
    private int[] inOrder;
    private int[] postOrder;
    /** 各ノードが表す区間のキャッシュ */
    private int[][] intervals;

    /**
     * <p>
     * 与えられた比較器に基づいてデカルト木を構築する。
     * </p>
     * <p>
     * 比較器 <code>cmp</code> は、要素のインデックス $u, v$ ($u < v$) に対し、
     * $u$ を $v$ の下に配置すべき（すなわち $u$ が $v$ の下位要素、あるいは $v$ がより優先度が高い）
     * 場合に正の値を返すように定義される。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @param n ノード数 ($N \ge 0$)
     * @param cmp 優先度を判定するためのインデックス比較器
     */
    // 未テスト
    public CartesianTree(int n, IntComparator cmp) {
        this.n = n;
        this.parent = new int[n];
        this.lch = new int[n];
        this.rch = new int[n];
        Arrays.fill(parent, -1);
        Arrays.fill(lch, -1);
        Arrays.fill(rch, -1);

        if (n == 0) {
            this.root = -1;
            return;
        }

        int[] stk = new int[n];
        int top = 0;

        for (int i = 0; i < n; i++) {
            int last = -1;
            while (top > 0 && cmp.compare(stk[top - 1], i) > 0) {
                last = stk[--top];
            }
            if (top > 0) {
                int p = stk[top - 1];
                rch[p] = i;
                parent[i] = p;
            }
            if (last != -1) {
                parent[last] = i;
                lch[i] = last;
            }
            stk[top++] = i;
        }
        this.root = stk[0];
    }

    /**
     * <p>
     * <code>int</code> 型の配列から最小ヒープ（Min-Heap）のデカルト木を構築する。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @param a 構築元の配列
     */
    // 未テスト
    public CartesianTree(int[] a) {
        this(a, true);
    }

    /**
     * <p>
     * <code>int</code> 型の配列からデカルト木を構築する。<code>isMinHeap</code> によって最小ヒープか最大ヒープかを選択する。
     * 同値の場合はインデックスが小さい方を優先する安定な構築を行う。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @param a 構築元の配列
     * @param isMinHeap 最小ヒープを構築する場合は <code>true</code>、最大ヒープを構築する場合は <code>false</code>
     */
    // 未テスト
    public CartesianTree(int[] a, boolean isMinHeap) {
        this(a.length, (u, v) -> {
            int valComp = Integer.compare(a[u], a[v]);
            if (valComp != 0) {
                return isMinHeap ? valComp : -valComp;
            }
            return Integer.compare(u, v);
        });
    }

    /**
     * <p>
     * <code>long</code> 型の配列から最小ヒープ（Min-Heap）のデカルト木を構築する。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @param a 構築元の配列
     */
    // 未テスト
    public CartesianTree(long[] a) {
        this(a, true);
    }

    /**
     * <p>
     * <code>long</code> 型の配列からデカルト木を構築する。<code>isMinHeap</code> によって最小ヒープか最大ヒープかを選択する。
     * 同値の場合はインデックスが小さい方を優先する安定な構築を行う。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @param a 構築元の配列
     * @param isMinHeap 最小ヒープを構築する場合は <code>true</code>、最大ヒープを構築する場合は <code>false</code>
     */
    // 未テスト
    public CartesianTree(long[] a, boolean isMinHeap) {
        this(a.length, (u, v) -> {
            int valComp = Long.compare(a[u], a[v]);
            if (valComp != 0) {
                return isMinHeap ? valComp : -valComp;
            }
            return Integer.compare(u, v);
        });
    }

    /**
     * <p>
     * 行き順巡回（Pre-order Traversal）を行ったときのノード of indices を返す。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return 行き順巡回順のインデックス配列
     */
    // 未テスト
    public int[] preOrder() {
        if (preOrder != null) return preOrder;
        preOrder = new int[n];
        if (n == 0) return preOrder;

        int[] stack = new int[n];
        int top = 0;
        stack[top++] = root;
        int idx = 0;
        while (top > 0) {
            int v = stack[--top];
            preOrder[idx++] = v;
            if (rch[v] != -1) stack[top++] = rch[v];
            if (lch[v] != -1) stack[top++] = lch[v];
        }
        return preOrder;
    }

    /**
     * <p>
     * 通り順巡回（In-order Traversal）を行ったときのノード of indices を返す。
     * デカルト木の定義により、常に <code>[0, 1, ..., N-1]</code> となる。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return 通り順巡回順のインデックス配列
     */
    // 未テスト
    public int[] inOrder() {
        if (inOrder != null) return inOrder;
        inOrder = new int[n];
        if (n == 0) return inOrder;

        int[] stack = new int[n];
        int top = 0;
        int cur = root;
        int idx = 0;
        while (cur != -1 || top > 0) {
            while (cur != -1) {
                stack[top++] = cur;
                cur = lch[cur];
            }
            cur = stack[--top];
            inOrder[idx++] = cur;
            cur = rch[cur];
        }
        return inOrder;
    }

    /**
     * <p>
     * 帰り順巡回（Post-order Traversal）を行ったときのノード of indices を返す。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return 帰り順巡回順のインデックス配列
     */
    // 未テスト
    public int[] postOrder() {
        if (postOrder != null) return postOrder;
        postOrder = new int[n];
        if (n == 0) return postOrder;

        int[] stack = new int[n];
        int top = 0;
        stack[top++] = root;
        int pointer = n - 1;
        while (top > 0) {
            int v = stack[--top];
            postOrder[pointer--] = v;
            if (lch[v] != -1) stack[top++] = lch[v];
            if (rch[v] != -1) stack[top++] = rch[v];
        }
        return postOrder;
    }

    /**
     * <p>
     * 各ノードの深さを計算して返す。根の深さは 0 とする。
     * 帰り順巡回（Post-order）の逆順（トポロジカル順）を利用して計算する。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return 各ノードの深さを格納した配列
     */
    // 未テスト
    public int[] depths() {
        int[] d = new int[n];
        if (n == 0) return d;

        int[] po = postOrder();
        for (int i = n - 1; i >= 0; i--) {
            int v = po[i];
            if (v == root) {
                d[v] = 0;
            } else {
                d[v] = d[parent[v]] + 1;
            }
        }
        return d;
    }

    /**
     * <p>
     * 各ノードを根とする部分木のサイズ（頂点数）を計算して返す。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return 各ノードを根とする部分木のサイズを格納した配列
     */
    // 未テスト
    public int[] subtreeSizes() {
        int[] sizes = new int[n];
        if (n == 0) return sizes;

        int[] po = postOrder();
        for (int v : po) {
            sizes[v] = 1;
            if (lch[v] != -1) {
                sizes[v] += sizes[lch[v]];
            }
            if (rch[v] != -1) {
                sizes[v] += sizes[rch[v]];
            }
        }
        return sizes;
    }

    /**
     * <p>
     * 全てのノードが表す区間 [L, R) を計算して返す。
     * <code>intervals[v]</code> はノード <code>v</code> (入力配列のv番目の要素）が表す区間 <code>[L, R]</code>（半開区間 [L, R)）を含む。
     * </p>
     * <ul>
     *   <li>計算量: O(N)</li>
     * </ul>
     * @return 各ノードが表す半開区間 [L, R) を格納した2次元配列
     */
    // 未テスト
    public int[][] intervals() {
        if (intervals != null) return intervals;
        intervals = new int[n][2];
        if (n == 0) return intervals;

        int[] sizes = subtreeSizes();
        for (int i = 0; i < n; i++) {
            int leftSize = (lch[i] == -1) ? 0 : sizes[lch[i]];
            int rightSize = (rch[i] == -1) ? 0 : sizes[rch[i]];
            intervals[i][0] = i - leftSize;
            intervals[i][1] = i + 1 + rightSize;
        }
        return intervals;
    }

    /**
     * <p>
     * 指定されたノード <code>v</code> が表す区間 $[L, R)$ を返す。
     * 左右の境界が確定（<code>l > 0 && r < n</code>）した時点で祖先の探索を早期に終了する。
     * </p>
     * <ul>
     *   <li>計算量: キャッシュが存在する場合は $O(1)$、存在しない場合は最悪 $O(\text{depth})$（平均 $O(\log N)$、早期終了による高速化あり）</li>
     * </ul>
     * @param v ノードのインデックス ($0 \le v < N$)
     * @return ノード <code>v</code> が表す半開区間 $[L, R)$ を格納した長さ 2 の配列
     * @throws IndexOutOfBoundsException <code>v</code> が範囲外の場合
     */
    // 未テスト
    public int[] interval(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Node index out of bounds: " + v);
        }
        if (intervals != null) {
            return intervals[v];
        }
        int l = 0;
        int r = n;
        int curr = v;
        while (parent[curr] != -1) {
            int p = parent[curr];
            if (lch[p] == curr) {
                r = Math.min(r, p);
            } else {
                l = Math.max(l, p + 1);
            }
            if (l > 0 && r < n) {
                break;
            }
            curr = p;
        }
        return new int[]{l, r};
    }

    /**
     * デカルト木の構造を文字列として表す。
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     * @return デカルト木の文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> lines = getDisplayLines();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i));
            if (i < lines.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * <p>
     * デバッグ用に、木の構造を標準出力に出力する。
     * </p>
     * <ul>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     */
    // 未テスト
    public void dump() {
        System.out.println(toString());
    }

    private List<String> getDisplayLines() {
        return buildLines(this.root);
    }

    private List<String> buildLines(int v) {
        if (v == -1) return new ArrayList<>();

        List<String> leftLines = buildLines(lch[v]);
        List<String> rightLines = buildLines(rch[v]);

        String label = String.valueOf(v);
        int labelW = label.length();

        int leftW = leftLines.isEmpty() ? 0 : leftLines.get(0).length();
        int rightW = rightLines.isEmpty() ? 0 : rightLines.get(0).length();
        int maxH = Math.max(leftLines.size(), rightLines.size());

        int leftBranch = leftW > 0 ? (leftW / 2) : 0;
        int rightBranch = rightW > 0 ? (rightW / 2) : 0;

        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();

        if (leftW > 0) {
            s1.append(" ".repeat(leftBranch + 1));
            s1.append("_".repeat(leftW - leftBranch - 1));
            s2.append(" ".repeat(leftBranch)).append("/").append(" ".repeat(leftW - leftBranch - 1));
        }

        s1.append(label);
        s2.append(" ".repeat(labelW));

        if (rightW > 0) {
            s1.append("_".repeat(rightBranch));
            s1.append(" ".repeat(rightW - rightBranch));
            s2.append(" ".repeat(rightBranch)).append("\\").append(" ".repeat(rightW - rightBranch - 1));
        }

        List<String> res = new ArrayList<>();
        res.add(s1.toString());
        res.add(s2.toString());

        for (int i = 0; i < maxH; i++) {
            String leftPart = i < leftLines.size() ? leftLines.get(i) : " ".repeat(leftW);
            String rightPart = i < rightLines.size() ? rightLines.get(i) : " ".repeat(rightW);
            res.add(leftPart + " ".repeat(labelW) + rightPart);
        }
        return res;
    }
}
