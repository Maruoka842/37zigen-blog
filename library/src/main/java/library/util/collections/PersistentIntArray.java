package library.util.collections;

import java.util.Arrays;

/**
 * 永続int配列 (Persistent int Array)
 *
 * 各更新操作に対して新しい根を返し、過去の状態を保持します。
 * プリミティブ `int` に特化しているため、オートボクシングのオーバーヘッドがありません。
 * 内部的には完全二分木を持ち、パスコピーによって永続性を実現します。
 * 全ての操作は O(log N) です。
 *
 * 未テスト
 */
public class PersistentIntArray {
    /**
     * 配列の特定のバージョンを表すレコード
     */
    public record Root(int id) {}

    private final int n;
    private final int size;
    private final int lg;

    private int[] values;
    private int[] left;
    private int[] right;
    private int nodeCount;

    /**
     * 長さ n、全ての要素が defaultValue である永続int配列を構築します。
     * O(N)
     *
     * @param n 配列の長さ
     * @param defaultValue 初期値
     */
    // 未テスト
    public PersistentIntArray(int n, int defaultValue) {
        this.n = n;
        this.size = n <= 1 ? 1 : Integer.highestOneBit(n - 1) << 1;
        this.lg = Integer.numberOfTrailingZeros(size);

        int initialNodes = 2 * size;
        this.values = new int[initialNodes];
        this.left = new int[initialNodes];
        this.right = new int[initialNodes];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        for (int i = 0; i < n; i++) this.values[size + i] = defaultValue;
        for (int i = size - 1; i >= 1; i--) {
            this.left[i] = 2 * i;
            this.right[i] = 2 * i + 1;
        }
        this.nodeCount = 2 * size;
    }

    /**
     * 初期配列 a を元に永続int配列を構築します。
     * O(N)
     *
     * @param a 初期配列
     */
    // 未テスト
    public PersistentIntArray(int[] a) {
        this.n = a.length;
        this.size = n <= 1 ? 1 : Integer.highestOneBit(n - 1) << 1;
        this.lg = Integer.numberOfTrailingZeros(size);

        int initialNodes = 2 * size;
        this.values = new int[initialNodes];
        this.left = new int[initialNodes];
        this.right = new int[initialNodes];
        Arrays.fill(left, -1);
        Arrays.fill(right, -1);

        for (int i = 0; i < n; i++) this.values[size + i] = a[i];
        for (int i = size - 1; i >= 1; i--) {
            this.left[i] = 2 * i;
            this.right[i] = 2 * i + 1;
        }
        this.nodeCount = 2 * size;
    }

    /**
     * 新しいノードを作成し、引数で与えられた値と子ノードの ID を格納します。
     * 必要に応じて内部配列を拡張します。
     * 拡張が発生しない場合 O(1)、発生する場合 O(nodeCount) です。全体として償却 O(1) です。
     *
     * @param val 格納する値 (葉ノードのみで使用)
     * @param l 左の子の ID として格納する値
     * @param r 右の子の ID として格納する値
     * @return 作成されたノードの ID
     */
    private int newNode(int val, int l, int r) {
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
    // 未テスト
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
    // 未テスト
    public Root set(Root root, int p, int x) {
        if (!(0 <= p && p < n)) throw new IndexOutOfBoundsException("Index " + p + " out of bounds for length " + n);

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
            copyCur = newNode(0, l, r);
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
    // 未テスト
    public int get(Root root, int p) {
        if (!(0 <= p && p < n)) throw new IndexOutOfBoundsException("Index " + p + " out of bounds for length " + n);
        int cur = root.id;
        for (int i = lg - 1; i >= 0; i--) {
            cur = ((p >> i) & 1) == 1 ? right[cur] : left[cur];
        }
        return values[cur];
    }

    /**
     * 配列の長さを返します。
     * O(1)
     *
     * @return 配列の長さ
     */
    // 未テスト
    public int size() {
        return n;
    }

    /**
     * この永続int配列を表す文字列を返します。
     *
     * <p>計算量: $O(1)$</p>
     *
     * @return この永続int配列のメタデータ文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        return "PersistentIntArray{n=" + n + ", size=" + size + ", nodeCount=" + nodeCount + "}";
    }

    /**
     * 指定されたバージョンの配列要素を文字列として表現します。
     *
     * <p>計算量: $O(N \log N)$</p>
     *
     * @param root バージョンを表す根
     * @return バージョン固有の配列の文字列表現
     */
    // 未テスト
    public String toString(Root root) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(get(root, i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 内部状態を標準出力に出力する。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし。</li>
     *   <li>副作用: 標準出力への出力。</li>
     *   <li>計算量: $O(\text{nodeCount})$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     */
    // 未テスト
    public void dump() {
        System.out.println(toString());
    }
}
