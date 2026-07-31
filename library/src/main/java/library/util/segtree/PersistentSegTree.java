package library.util.segtree;

import java.util.Arrays;
import java.util.function.Predicate;
import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * 永続セグメント木 (Persistent Segment Tree)
 *
 * 各更新操作に対して新しい根を返し、過去の状態を保持します。
 * 全ての操作は O(log N) です。
 *
 * 未テスト
 *
 * @param <S> 要素の型
 */
public class PersistentSegTree<S> {
    /**
     * 木の特定のバージョンを表すレコード
     */
    public record Root(int id) {}

    private final int n;
    private final int size;
    private final int lg;
    private final MonoidStrategy<S> strategy;

    private S[] values;
    private int[] left;
    private int[] right;
    private int nodeCount;

    /**
     * 長さ n、全ての要素が identity である永続セグメント木を構築します。
     * O(N)
     *
     * @param n 長さ
     * @param strategy モノイドの戦略 (演算と単位元)
     */
    @SuppressWarnings("unchecked")
    public PersistentSegTree(int n, MonoidStrategy<S> strategy) {
        this.n = n;
        this.strategy = strategy;
        S identity = strategy.identity();
        this.size = n <= 1 ? 1 : Integer.highestOneBit(n - 1) << 1;
        this.lg = Integer.numberOfTrailingZeros(size);

        int initialNodes = 2 * size;
        this.values = (S[]) new Object[initialNodes];
        this.left = new int[initialNodes];
        this.right = new int[initialNodes];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);
        Arrays.fill(values, identity);

        for (int i = size - 1; i >= 1; i--) {
            this.left[i] = 2 * i;
            this.right[i] = 2 * i + 1;
            this.values[i] = strategy.mul(this.values[2 * i], this.values[2 * i + 1]);
        }
        this.nodeCount = 2 * size;
    }

    /**
     * 配列 v を元に永続セグメント木を構築します。
     * O(N)
     *
     * @param v 初期配列
     * @param strategy モノイドの戦略 (演算と単位元)
     */
    @SuppressWarnings("unchecked")
    public PersistentSegTree(S[] v, MonoidStrategy<S> strategy) {
        this.n = v.length;
        this.strategy = strategy;
        S identity = strategy.identity();
        this.size = n <= 1 ? 1 : Integer.highestOneBit(n - 1) << 1;
        this.lg = Integer.numberOfTrailingZeros(size);

        int initialNodes = 2 * size;
        this.values = (S[]) new Object[initialNodes];
        this.left = new int[initialNodes];
        this.right = new int[initialNodes];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        for (int i = 0; i < n; i++) this.values[size + i] = v[i];
        for (int i = n; i < size; i++) this.values[size + i] = identity;

        for (int i = size - 1; i >= 1; i--) {
            this.left[i] = 2 * i;
            this.right[i] = 2 * i + 1;
            this.values[i] = strategy.mul(this.values[2 * i], this.values[2 * i + 1]);
        }
        this.nodeCount = 2 * size;
    }

    private int newNode(S val, int l, int r) {
        if (nodeCount == values.length) {
            int newCap = values.length << 1;
            values = Arrays.copyOf(values, newCap);
            left = Arrays.copyOf(left, newCap);
            right = Arrays.copyOf(right, newCap);
        }
        values[nodeCount] = val;
        left[nodeCount] = l;
        right[nodeCount] = r;
        return nodeCount++;
    }

    /**
     * 初期状態の根を取得します。
     * O(1)
     *
     * @return 根
     */
    public Root getRoot() {
        return new Root(1);
    }

    /**
     * インデックス p の値を x に更新した新しいバージョンを作成します。
     * O(log N)
     *
     * @param root 現在の根
     * @param p インデックス
     * @param x 新しい値
     * @return 新しいバージョンの根
     */
    public Root set(Root root, int p, S x) {
        if (!(0 <= p && p < n)) throw new IndexOutOfBoundsException();

        int[] ids = new int[lg + 1];
        ids[lg] = root.id;
        for (int i = lg - 1; i >= 0; i--) {
            ids[i] = ((p >> i) & 1) == 1 ? right[ids[i + 1]] : left[ids[i + 1]];
        }

        int copyCur = newNode(x, -1, -1);
        for (int i = 1; i <= lg; i++) {
            int par = ids[i];
            int cur = ids[i - 1];
            int l = left[par] == cur ? copyCur : left[par];
            int r = right[par] == cur ? copyCur : right[par];
            copyCur = newNode(strategy.mul(values[l], values[r]), l, r);
        }
        return new Root(copyCur);
    }

    /**
     * インデックス p の値を取得します。
     * O(log N)
     *
     * @param root 根
     * @param p インデックス
     * @return 値
     */
    public S get(Root root, int p) {
        if (!(0 <= p && p < n)) throw new IndexOutOfBoundsException();
        int cur = root.id;
        for (int i = lg - 1; i >= 0; i--) {
            cur = ((p >> i) & 1) == 1 ? right[cur] : left[cur];
        }
        return values[cur];
    }

    /**
     * 範囲 [l, r) の積を計算します。
     * O(log N)
     *
     * @param root 根
     * @param l 左端 (inclusive)
     * @param r 右端 (exclusive)
     * @return 範囲の積
     */
    public S prod(Root root, int l, int r) {
        if (!(0 <= l && l <= r && r <= n)) throw new IndexOutOfBoundsException();
        return prod(root.id, 0, size, l, r);
    }

    private S prod(int cur, int lo, int hi, int l, int r) {
        if (r <= lo || hi <= l) return strategy.identity();
        if (l <= lo && hi <= r) return values[cur];
        int mid = (lo + hi) >> 1;
        return strategy.mul(prod(left[cur], lo, mid, l, r), prod(right[cur], mid, hi, l, r));
    }

    /**
     * 全範囲の積を取得します。
     * O(1)
     *
     * @param root 根
     * @return 全範囲の積
     */
    public S allProd(Root root) {
        return values[root.id];
    }

    /**
     * f(prod(l, r)) が真となる最大の r を返します。
     * O(log N)
     *
     * @param root 根
     * @param l 左端
     * @param f 判定関数
     * @return 最大の r
     */
    public int maxRight(Root root, int l, Predicate<S> f) {
        if (!(0 <= l && l <= n)) throw new IndexOutOfBoundsException();
        if (!f.test(strategy.identity())) throw new IllegalArgumentException("f(identity) must be true");
        if (l == n) return n;

        MaxRightState state = new MaxRightState(strategy.identity());
        return Math.min(maxRightRec(root.id, 0, size, l, f, state), n);
    }

    private class MaxRightState {
        S sm;
        MaxRightState(S sm) { this.sm = sm; }
    }

    private int maxRightRec(int cur, int lo, int hi, int l, Predicate<S> f, MaxRightState state) {
        if (hi <= l) return hi;
        if (l <= lo) {
            S nxt = strategy.mul(state.sm, values[cur]);
            if (f.test(nxt)) {
                state.sm = nxt;
                return hi;
            }
            if (hi - lo == 1) return lo;
        }
        int mid = (lo + hi) >> 1;
        if (l < mid) {
            int leftRes = maxRightRec(left[cur], lo, mid, l, f, state);
            if (leftRes < mid) return leftRes;
        }
        return maxRightRec(right[cur], mid, hi, l, f, state);
    }

    /**
     * f(prod(l, r)) が真となる最小の l を返します。
     * O(log N)
     *
     * @param root 根
     * @param r 右端
     * @param f 判定関数
     * @return 最小の l
     */
    public int minLeft(Root root, int r, Predicate<S> f) {
        if (!(0 <= r && r <= n)) throw new IndexOutOfBoundsException();
        if (!f.test(strategy.identity())) throw new IllegalArgumentException("f(identity) must be true");
        if (r == 0) return 0;

        MinLeftState state = new MinLeftState(strategy.identity());
        return minLeftRec(root.id, 0, size, r, f, state);
    }

    private class MinLeftState {
        S sm;
        MinLeftState(S sm) { this.sm = sm; }
    }

    private int minLeftRec(int cur, int lo, int hi, int r, Predicate<S> f, MinLeftState state) {
        if (r <= lo) return lo;
        if (hi <= r) {
            S nxt = strategy.mul(values[cur], state.sm);
            if (f.test(nxt)) {
                state.sm = nxt;
                return lo;
            }
            if (hi - lo == 1) return hi;
        }
        int mid = (lo + hi) >> 1;
        if (mid < r) {
            int rightRes = minLeftRec(right[cur], mid, hi, r, f, state);
            if (mid < rightRes) return rightRes;
        }
        return minLeftRec(left[cur], lo, mid, r, f, state);
    }
}
