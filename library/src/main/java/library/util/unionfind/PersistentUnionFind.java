package library.util.unionfind;

import library.util.collections.PersistentIntArray;

/**
 * 永続Union-Find (Persistent Union-Find)
 *
 * 各更新操作に対して新しい状態を表す `PersistentUnionFind` インスタンスを返し、
 * 過去の状態を完全に保持します。内部では、プリミティブ `int` に特化した
 * 永続配列 `PersistentIntArray` を用いることで、ボクシングオーバーヘッドを完全に排除しています。
 *
 * 未テスト
 */
public class PersistentUnionFind {

    /** 内部で状態を保持するための永続配列 */
    private final PersistentIntArray array;

    /** 現在のバージョンの根ノードID */
    private final PersistentIntArray.Root root;

    /** 現在の連結成分数 */
    private final int numberConnectedComponents;

    /** 要素数 */
    private final int size;

    /**
     * 要素数 n の永続Union-Findを構築します。
     * 各要素はサイズ 1 の独立した集合として初期化されます。
     * <ul>
     *   <li>事前条件: $n \ge 0$</li>
     *   <li>計算量: $O(N)$</li>
     * </ul>
     *
     * @param n 要素数
     */
    // 未テスト
    public PersistentUnionFind(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative: " + n);
        }
        this.array = new PersistentIntArray(n, -1);
        this.root = array.getRoot();
        this.numberConnectedComponents = n;
        this.size = n;
    }

    /**
     * 内部構築用のプライベートコンストラクタです。
     */
    private PersistentUnionFind(PersistentIntArray array, PersistentIntArray.Root root, int numberConnectedComponents, int size) {
        this.array = array;
        this.root = root;
        this.numberConnectedComponents = numberConnectedComponents;
        this.size = size;
    }

    /**
     * 要素数（配列の長さ）を返します。
     * <ul>
     *   <li>計算量: $O(1)$</li>
     * </ul>
     *
     * @return 要素数
     */
    // 未テスト
    public int size() {
        return size;
    }

    /**
     * 要素 x が属する集合 of の代表元（根）を返します。
     * <ul>
     *   <li>事前条件: $0 \le x < \text{size}$</li>
     *   <li>計算量: $O(\log^2 N)$</li>
     * </ul>
     *
     * @param x 要素 of のインデックス
     * @return 代表元
     */
    // 未テスト
    public int find(int x) {
        if (x < 0 || x >= size) {
            throw new IndexOutOfBoundsException("Index " + x + " out of bounds for length " + size);
        }
        int cur = x;
        while (true) {
            int p = array.get(root, cur);
            if (p < 0) {
                return cur;
            }
            cur = p;
        }
    }

    /**
     * find(x) のエイリアスです。要素 x が属する集合の代表元（根）を返します。
     * <ul>
     *   <li>事前条件: $0 \le x < \text{size}$</li>
     *   <li>計算量: $O(\log^2 N)$</li>
     * </ul>
     *
     * @param x 要素のインデックス
     * @return 代表元
     */
    // 未テスト
    public int root(int x) {
        return find(x);
    }

    /**
     * 要素 x がその集合の根であるかを判定します。
     * <ul>
     *   <li>事前条件: $0 \le x < \text{size}$</li>
     *   <li>計算量: $O(\log N)$</li>
     * </ul>
     *
     * @param x 要素のインデックス
     * @return 根であれば true、そうでなければ false
     */
    // 未テスト
    public boolean isRoot(int x) {
        if (x < 0 || x >= size) {
            throw new IndexOutOfBoundsException("Index " + x + " out of bounds for length " + size);
        }
        return array.get(root, x) < 0;
    }

    /**
     * 要素 x と要素 y の属する集合を併合した新しい永続Union-Findを返します。
     * <ul>
     *   <li>事前条件: $0 \le x, y < \text{size}$</li>
     *   <li>事後条件: x と y の属する集合が併合された新しい `PersistentUnionFind` インスタンスを返す。既に同じ集合に属している場合は、`this` インスタンスをそのまま返す。</li>
     *   <li>計算量: $O(\log^2 N)$</li>
     * </ul>
     *
     * @param x 要素のインデックス
     * @param y 要素のインデックス
     * @return 併合後の永続Union-Find
     */
    // 未テスト
    public PersistentUnionFind union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) {
            return this;
        }
        int szX = -array.get(root, rx);
        int szY = -array.get(root, ry);

        PersistentIntArray.Root newRoot;
        if (szX < szY) {
            PersistentIntArray.Root tempRoot = array.set(root, ry, -(szX + szY));
            newRoot = array.set(tempRoot, rx, ry);
        } else {
            PersistentIntArray.Root tempRoot = array.set(root, rx, -(szX + szY));
            newRoot = array.set(tempRoot, ry, rx);
        }
        return new PersistentUnionFind(array, newRoot, numberConnectedComponents - 1, size);
    }

    /**
     * 要素 x と要素 y が同じ集合に属しているかを判定します。
     * <ul>
     *   <li>事前条件: $0 \le x, y < \text{size}$</li>
     *   <li>計算量: $O(\log^2 N)$</li>
     * </ul>
     *
     * @param x 要素のインデックス
     * @param y 要素のインデックス
     * @return 同じ集合に属していれば true、そうでなければ false
     */
    // 未テスト
    public boolean equiv(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * 要素 x が属する集合のサイズを返します。
     * <ul>
     *   <li>事前条件: $0 \le x < \text{size}$</li>
     *   <li>計算量: $O(\log^2 N)$</li>
     * </ul>
     *
     * @param x 要素のインデックス
     * @return 集合のサイズ
     */
    // 未テスト
    public int size(int x) {
        int rx = find(x);
        return -array.get(root, rx);
    }

    /**
     * 現在の連結成分の総数を返します。
     * <ul>
     *   <li>計算量: $O(1)$</li>
     * </ul>
     *
     * @return 連結成分数
     */
    // 未テスト
    public int numberConnectedComponents() {
        return numberConnectedComponents;
    }

    /**
     * 根となっているすべての代表元を配列として返します。
     * <ul>
     *   <li>計算量: $O(N \log N)$</li>
     * </ul>
     *
     * @return 代表元の配列
     */
    // 未テスト
    public int[] roots() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (isRoot(i)) {
                count++;
            }
        }
        int[] ret = new int[count];
        int ptr = 0;
        for (int i = 0; i < size; i++) {
            if (isRoot(i)) {
                ret[ptr++] = i;
            }
        }
        return ret;
    }

    /**
     * 永続Union-Findの現在の状態をコピーした新しいインスタンスを返します。
     * 永続配列の実体を共有しているため、コピーによるメモリ増加はありません。
     * <ul>
     *   <li>計算量: $O(1)$</li>
     * </ul>
     *
     * @return 自身の新しい参照
     */
    // 未テスト
    public PersistentUnionFind copy() {
        return new PersistentUnionFind(this.array, this.root, this.numberConnectedComponents, this.size);
    }

    /**
     * 永続Union-Findの現在の状態を、連結成分ごとに文字列として表します。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列を返す。</li>
     *   <li>計算量: $O(N \log^2 N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * @return 連結成分ごとの文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        int[] rootVal = new int[size];
        for (int i = 0; i < size; i++) rootVal[i] = find(i);
        int[] count = new int[size];
        for (int i = 0; i < size; i++) count[rootVal[i]]++;
        int[][] groups = new int[size][];
        for (int i = 0; i < size; i++) if (count[i] > 0) groups[i] = new int[count[i]];
        int[] ptr = new int[size];
        for (int i = 0; i < size; i++) groups[rootVal[i]][ptr[rootVal[i]]++] = i;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (groups[i] != null) {
                sb.append("{");
                for (int j = 0; j < groups[i].length; j++) {
                    sb.append(groups[i][j]);
                    if (j < groups[i].length - 1) sb.append(", ");
                }
                sb.append("}");
            }
        }
        return sb.toString();
    }

    /**
     * 現在の状態を、連結成分ごとに標準出力へ出力します。
     * <ul>
     *   <li>計算量: $O(N \log^2 N)$</li>
     *   <li>副作用: 標準出力への出力。</li>
     * </ul>
     */
    public void dump() {
        System.out.println(toString());
    }
}
