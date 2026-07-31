package library.util.segtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import library.util.ArrayUtils;
import library.util.seq.SortedArrays;
import library.util.algebra.strategy.longs.LongAddAbelianGroupStrategy;

/**
 * 二次元平面における静的な矩形加算を x 方向にスイープしながら処理し、
 * 任意の y における値を求めるデータ構造。
 *
 * <p>特徴：
 * <ul>
 *   <li>矩形 $[x_0, x_1) \times [y_0, y_1)$ への加算を登録可能</li>
 *   <li>x 昇順にクエリ get(x, y) を呼び出すことで、
 *       x 以下の加算をすべて反映した状態の y における値を返す</li>
 *   <li>内部では差分イベント（x0 で +add、x1 で -add）を用いる</li>
 *   <li>y は座標圧縮され、Binary Indexed Tree により管理される</li>
 * </ul>
 *
 * <p>使用手順：
 * <ol>
 *   <li>rectAdd(...) で全ての更新を追加する</li>
 *   <li>build()</li>
 *   <li>x を単調非減少で get(x, y) を呼ぶ</li>
 * </ol>
 *
 * <p>制約・注意：
 * <ul>
 *   <li>x クエリは必ず昇順で行うこと</li>
 *   <li>オーバーフロー対策はしていない</li>
 *   <li>区間の閉半開は [l, r) を採用</li>
 * </ul>
 */
public class OfflineStatic_RectAdd_PointGet {
    /** 矩形更新イベントのリスト。 [x, y0, y1, add] */
    private final ArrayList<long[]> updates;
    /** 座標圧縮用の y 座標リスト。 */
    private final ArrayList<Long> ys;
    /** 座標圧縮後のソート済みユニーク y 座標配列。 */
    private long[] sortqY;
    /** 平面全体への加算値。 */
    private long base = 0;

    /** 階差を管理する BIT。 */
    private LongAbelianGroupBinaryIndexedTree bit;
    /** 内部構造が構築済みかどうか。 */
    private boolean built = false;

    /**
     * デフォルトコンストラクタ。
     * 未テスト
     */
    public OfflineStatic_RectAdd_PointGet() {
        this.updates = new ArrayList<>();
        this.ys = new ArrayList<>();
    }

    /**
     * 矩形更新クエリの予定数を指定するコンストラクタ。
     * 未テスト
     * @param rectAddQuerySize 矩形更新クエリの数
     */
    public OfflineStatic_RectAdd_PointGet(int rectAddQuerySize) {
        this.updates = new ArrayList<>(2 * rectAddQuerySize);
        this.ys = new ArrayList<>(2 + 2 * rectAddQuerySize);
    }

    /**
     * 矩形 $[x_0, x_1) \times [y_0, y_1)$ に $add$ を加算する。
     *
     * <p>計算量: $O(1)$
     * <p>事前条件: $x_0 \le x_1, y_0 \le y_1$
     * <p>副作用: `updates`, `ys` に要素を追加する
     *
     * 未テスト
     * @param x0 xの下限
     * @param y0 yの下限
     * @param x1 xの上限
     * @param y1 yの上限
     * @param add 加算する値
     */
    public void rectAdd(long x0, long y0, long x1, long y1, long add) {
        if (x0 < x1 && y0 < y1) {
            updates.add(new long[]{x0, y0, y1, add});
            updates.add(new long[]{x1, y0, y1, -add});
            registerY(y0);
            registerY(y1);
        }
    }

    /**
     * 平面全体に $add$ を加算する。
     *
     * <p>計算量: $O(1)$
     * <p>事後条件: `base` に `add` が加算される
     *
     * 未テスト
     * @param add 加算する値
     */
    public void addAll(long add) {
        base += add;
    }

    /**
     * y 座標を座標圧縮の対象として登録する。
     *
     * 未テスト
     * @param y 登録する y 座標
     */
    public void registerY(long y) {
        ys.add(y);
    }

    /**
     * 登録された矩形情報を元に内部構造を構築する。
     *
     * <p>計算量: $O((N+Q) \log (N+Q))$、ここで $N$ は矩形数、$Q$ は登録された y 座標数。
     * <p>事後条件: `built` が `true` になる。
     * <p>副作用: `updates` がソートされ、`ys` から `sortqY` が作成される。BIT が初期化される。
     * <p>破壊的変更: `updates`, `ys` の状態が変更される。
     *
     * 未テスト
     */
    public void build() {
        registerY(Long.MIN_VALUE / 4);
        registerY(Long.MAX_VALUE / 4);

        Collections.sort(updates, (x, y) -> Long.compare(x[0], y[0]));
        sortqY = new long[ys.size()];
        for (int i = 0; i < ys.size(); i++) {
            sortqY[i] = ys.get(i);
        }
        sortqY = ArrayUtils.sortq(sortqY);

        int n = sortqY.length;
        bit = new LongAbelianGroupBinaryIndexedTree(n, LongAddAbelianGroupStrategy.STRATEGY);
        built = true;
    }

    private long lastX = Long.MIN_VALUE;
    private int pointer = 0;

    /**
     * 指定された $x$ における $y$ の値を求める。
     *
     * <p>計算量: $O(\log (N+Q))$ (更新の償却を除く)
     * <p>事前条件: `build()` が呼び出されていること。$x$ は前回の呼び出し時の $x$ より小さくないこと。
     * <p>副作用: `pointer`, `lastX` を更新し、BIT の状態を変更する。
     * <p>例外・未定義条件: $x$ が減少した場合 `AssertionError`。
     *
     * 未テスト
     * @param x クエリを実行する x 座標
     * @param y 取得する y 座標
     * @return $y$ における値
     */
    public long get(long x, long y) {
        if (!built) throw new AssertionError("build() must be called before get()");
        if (lastX > x) throw new AssertionError("x must be non-decreasing");
        lastX = x;

        while (pointer < updates.size() && updates.get(pointer)[0] <= x) {
            long[] update = updates.get(pointer);
            int l = SortedArrays.ceil(sortqY, update[1]);
            int r = SortedArrays.ceil(sortqY, update[2]);
            long v = update[3];

            bit.add(l, v);
            bit.add(r, -v);

            pointer++;
        }

        int k = SortedArrays.floor(sortqY, y);
        if (k < 0) return base;
        return bit.prefixSum(k + 1) + base;
    }
}
