package library.util.graph.tree;

/**
 * 無標識木の列挙を行うクラス。
 * Rooted trees (A000081) および Unlabeled trees (A000055) を列挙する。
 *
 * 内部的には、根付き木を (最大のサイズの子, その他の部分木) という再帰的構造で保持する。
 * これにより、同型な木を重複なく生成することができる。
 *
 * 計算量: 頂点数 N に対して、生成される木の個数に対して線形。
 * 空間計算量: O(\sum_{i=1}^N A000081(i))。 N=21 で約 5500万要素。
 *
 * 未テスト
 */
public class UnlabeledTreeEnumeration {
    /** OEIS A000081: n頂点の無標識根付き木の個数。 */
    public static final long[] UNLABELED_ROOTED = {
        0, 1, 1, 2, 4, 9, 20, 48, 115, 286, 719, 1842, 4766, 12486, 32973, 87811, 235381, 634847, 1721159, 4688676, 12826228, 35221832, 97055181, 268282855, 743724984, 2067174645
    };

    /** OEIS A000055: n頂点の無標識木の個数。 */
    public static final long[] UNLABELED_UNROOTED = {
        0, 1, 1, 1, 2, 3, 6, 11, 23, 47, 106, 235, 551, 1301, 3159, 7741, 19320, 48629, 123867, 317955, 823065, 2144505, 5623756, 14828074, 39299897, 104636890, 279793450, 751065460, 2023443032
    };

    /** 列挙する最大の頂点数。 */
    public final int maxN;
    /**
     * 根の子として許す部分木サイズの上限。
     * 根付き木を全列挙する場合:limDn = maxN - 1
     * 無根木を重心根で代表させる場合:limDn = maxN / 2
     */
    public final int limDn;

    // T[n][x] の構造を保持する。
    private final int[][] childSize;
    private final int[][] childIndex;
    private final int[][] restIndex;

    /**
     * 指定された条件で木を構築する。
     * @param maxN 列挙する最大の頂点数
     * @param limDn 子の最大のサイズ。無標識根付き木の場合は maxN - 1、無標識木の場合は maxN / 2 を指定する。
     */
    public UnlabeledTreeEnumeration(int maxN, int limDn) {
        this.maxN = maxN;
        this.limDn = limDn;

        int[] counts = new int[maxN + 1];
        counts[1] = 1;
        for (int dn = 1; dn <= limDn; dn++) {
            int dnLen = counts[dn];
            for (int dx = 0; dx < dnLen; dx++) {//サイズがdnの木がdnLen個あり、それを順に根の子に追加
                for (int n = 1; n + dn <= maxN; n++) {
                    counts[n + dn] += counts[n];
                }
            }
            //counts[i]=根の子のサイズがdn以下で全体のサイズがiの木の数、
        }

        childSize = new int[maxN + 1][];//最後に追加された子部分木のサイズ
        childIndex = new int[maxN + 1][];//最後に追加された子部分木のインデックス
        restIndex = new int[maxN + 1][];//最後に追加された子部分木を削除したときのインデックス
        for (int i = 1; i <= maxN; i++) {
            childSize[i] = new int[counts[i]];
            childIndex[i] = new int[counts[i]];
            restIndex[i] = new int[counts[i]];
        }

        int[] ptr = new int[maxN + 1];//ptr[i]=すでに作成したサイズiの木の個数
        // 頂点数1の木
        ptr[1] = 1;
        // 初期値 0 は既に入っている

        for (int dn = 1; dn <= limDn; dn++) {
            int dnLen = ptr[dn];
            for (int dx = 0; dx < dnLen; dx++) {
                for (int n = 1; n + dn <= maxN; n++) {
                    int nLen = ptr[n];
                    for (int x = 0; x < nLen; x++) {
                        int target = n + dn;
                        int p = ptr[target]++;
                        childSize[target][p] = dn;
                        childIndex[target][p] = dx;
                        restIndex[target][p] = x;
                    }
                }
            }
        }
    }

    /**
     * 無標識根付き木を列挙するためのインスタンスを作成する。
     * @param maxN 列挙する最大の頂点数
     * @return
     */
    public static UnlabeledTreeEnumeration rooted(int maxN) {
        return new UnlabeledTreeEnumeration(maxN, Math.max(0, maxN - 1));
    }

    /**
     * 無標識木を列挙するためのインスタンスを作成する。
     * @param maxN 列挙する最大の頂点数
     * @return
     */
    public static UnlabeledTreeEnumeration unrooted(int maxN) {
        return new UnlabeledTreeEnumeration(maxN, maxN / 2);
    }

    /**
     * 頂点数 n の木の個数を返す。
     * @param n 頂点数
     * @return 木の個数
     */
    public int groupSize(int n) {
        return childSize[n].length;
    }

    /**
     * 重心を一つ固定する。指定された木が重心を根としているか判定する。重心が二つある場合、一方だけでTrue
     * @param n 頂点数
     * @param x インデックス
     * @return 重心を根としているか
     */
    public boolean isCentroid(int n, int x) {
        if (n <= 1) return true;
        int dn = childSize[n][x];
        int dx = childIndex[n][x];
        int rx = restIndex[n][x];
        int rn = n - dn;
        // dn < n - dn <=> dn < n/2
        if (dn < rn) return true;
        if (dn > rn) return false;
        // 重心2個の場合、重複を除く
        return dx <= rx;
    }

    /**
     * 指定された木の親子関係を配列形式で返す。
     * 根の親は -1 となる。
     *
     * @param n 頂点数
     * @param x インデックス
     * @return 親配列。長さ n。
     */
    public int[] getParentArray(int n, int x) {
        if (n < 1) return new int[0];
        int[] par = new int[n];
        int[] id = {0};
        getParDfs(n, x, -1, id, par);
        return par;
    }

    private void getParDfs(int n, int x, int p, int[] id, int[] par) {
        int u = id[0]++;
        par[u] = p;
        int currN = n;
        int currX = x;
        while (currN > 1) {
            int dn = childSize[currN][currX];
            int dx = childIndex[currN][currX];
            int rx = restIndex[currN][currX];
            getParDfs(dn, dx, u, id, par);
            currN = currN - dn;
            currX = rx;
        }
    }

    /**
     * 指定された木を Tree オブジェクトとして返す。
     *
     * @param n 頂点数
     * @param x インデックス
     * @return Tree オブジェクト
     */
    public Tree getTree(int n, int x) {
        int[] par = getParentArray(n, x);
        Tree tree = new Tree(n);
        for (int i = 0; i < n; i++) {
            if (par[i] != -1) {
                tree.addEdge(i, par[i]);
            }
        }
        if (n > 0) tree.rooted(0);
        return tree;
    }
}
