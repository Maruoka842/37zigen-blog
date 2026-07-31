package library.util.collections;

/**
 * 平方分割を用いた、値に対する様々なクエリをサポートするライブラリです。
 * 各ブロックは平衡二分木（LongTreapMultiSet）で管理されます。
 *
 * @complexity 構築: O(N log B), 各クエリ: O(B + (N/B) log B) or O(B + (N/B) log^2 B)
 *             ここで B はブロックサイズ（デフォルトで sqrt(N)）
 */
public class SquareRootDecompositionBBST {
    private final int n;
    private final int blockSize;
    private final int numBlocks;
    private final long[] a;
    private final LongTreapMultiSet[] blocks;
    private final long[] lazyAdd;
    private final long[] blockSum;

    /**
     * 長さ n の配列を 0 で初期化して構築します。
     * @param n 配列の長さ
     */
    public SquareRootDecompositionBBST(int n) {
        this(new long[n]);
    }

    /**
     * 初期配列を指定して構築します。
     * @param initialArray 初期配列
     */
    public SquareRootDecompositionBBST(long[] initialArray) {
        this.n = initialArray.length;
        this.blockSize = (int) Math.sqrt(n + 1) + 1;
        this.numBlocks = (n + blockSize - 1) / blockSize;
        this.a = initialArray.clone();
        this.blocks = new LongTreapMultiSet[numBlocks];
        this.lazyAdd = new long[numBlocks];
        this.blockSum = new long[numBlocks];

        for (int b = 0; b < numBlocks; b++) {
            blocks[b] = new LongTreapMultiSet();
            int l = b * blockSize;
            int r = Math.min(n, (b + 1) * blockSize);
            for (int i = l; i < r; i++) {
                blocks[b].add(a[i]);
                blockSum[b] += a[i];
            }
        }
    }

    /**
     * a[i] を取得します。
     * @param i インデックス
     * @return a[i]
     */
    public long get(int i) {
        return a[i] + lazyAdd[i / blockSize];
    }

    /**
     * a[i] を x に変更します。
     * @param i インデックス
     * @param x 新しい値
     */
    public void set(int i, long x) {
        int b = i / blockSize;
        blocks[b].remove(a[i]);
        blockSum[b] -= a[i];
        a[i] = x - lazyAdd[b];
        blocks[b].add(a[i]);
        blockSum[b] += a[i];
    }

    /**
     * a[i] に x を加算します。
     * @param i インデックス
     * @param x 加算する値
     */
    public void add(int i, long x) {
        int b = i / blockSize;
        blocks[b].remove(a[i]);
        a[i] += x;
        blocks[b].add(a[i]);
        blockSum[b] += x;
    }

    /**
     * a[l...r) に x を加算します。
     * @param l 開始インデックス（含む）
     * @param r 終了インデックス（含まない）
     * @param x 加算する値
     */
    public void rangeAdd(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        if (bl == br) {
            for (int i = l; i < r; i++) {
                addPointInternal(i, x);
            }
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) {
                addPointInternal(i, x);
            }
            for (int b = bl + 1; b < br; b++) {
                lazyAdd[b] += x;
                blockSum[b] += x * blockSize;
            }
            for (int i = br * blockSize; i < r; i++) {
                addPointInternal(i, x);
            }
        }
    }

    private void addPointInternal(int i, long x) {
        int b = i / blockSize;
        blocks[b].remove(a[i]);
        a[i] += x;
        blocks[b].add(a[i]);
        blockSum[b] += x;
    }

    /**
     * a[l...r) の合計を取得します。
     * @param l
     * @param r
     * @return 和
     */
    public long rangeSum(int l, int r) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        long res = 0;
        if (bl == br) {
            for (int i = l; i < r; i++) res += get(i);
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) res += get(i);
            for (int b = bl + 1; b < br; b++) res += blockSum[b];
            for (int i = br * blockSize; i < r; i++) res += get(i);
        }
        return res;
    }

    /**
     * a[l...r) 内で最小値を持つ最初のインデックスを返します。
     * @param l
     * @param r
     * @return インデックス
     */
    public int rangeMinIndex(int l, int r) {
        long minVal = rangeMin(l, r);
        return findFirst(l, r, minVal);
    }

    /**
     * a[l...r) 内で最大値を持つ最初のインデックスを返します。
     * @param l
     * @param r
     * @return インデックス
     */
    public int rangeMaxIndex(int l, int r) {
        long maxVal = rangeMax(l, r);
        return findFirst(l, r, maxVal);
    }

    /**
     * a[l...r) の最小値を取得します。
     * @param l
     * @param r
     * @return 最小値
     */
    public long rangeMin(int l, int r) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        long res = Long.MAX_VALUE;
        if (bl == br) {
            for (int i = l; i < r; i++) res = Math.min(res, get(i));
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) res = Math.min(res, get(i));
            for (int b = bl + 1; b < br; b++) res = Math.min(res, blocks[b].peekFirst() + lazyAdd[b]);
            for (int i = br * blockSize; i < r; i++) res = Math.min(res, get(i));
        }
        return res;
    }

    /**
     * a[l...r) の最大値を取得します。
     * @param l
     * @param r
     * @return 最大値
     */
    public long rangeMax(int l, int r) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        long res = Long.MIN_VALUE;
        if (bl == br) {
            for (int i = l; i < r; i++) res = Math.max(res, get(i));
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) res = Math.max(res, get(i));
            for (int b = bl + 1; b < br; b++) res = Math.max(res, blocks[b].peekLast() + lazyAdd[b]);
            for (int i = br * blockSize; i < r; i++) res = Math.max(res, get(i));
        }
        return res;
    }

    /**
     * a[l...r) 内で lower 以上 upper 未満の値を持つ要素の個数を返します。
     * @param l
     * @param r
     * @param lower
     * @param upper
     * @return 個数
     */
    public long rangeCount(int l, int r, long lower, long upper) {
        if (lower >= upper) return 0;
        return rangeRank(l, r, upper) - rangeRank(l, r, lower);
    }

    /**
     * a[l...r) 内で x 未満の要素の個数を返します。
     * @param l
     * @param r
     * @param x
     * @return 個数
     */
    public long rangeRank(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        long res = 0;
        if (bl == br) {
            for (int i = l; i < r; i++) if (get(i) < x) res++;
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) if (get(i) < x) res++;
            for (int b = bl + 1; b < br; b++) res += blocks[b].countLeq(x - lazyAdd[b] - 1);
            for (int i = br * blockSize; i < r; i++) if (get(i) < x) res++;
        }
        return res;
    }

    /**
     * a[l...r) 内で x と等しい要素の個数を返します。
     * @param l
     * @param r
     * @param x
     * @return 個数
     */
    public long rangeFrequency(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        long res = 0;
        if (bl == br) {
            for (int i = l; i < r; i++) if (get(i) == x) res++;
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) if (get(i) == x) res++;
            for (int b = bl + 1; b < br; b++) res += blocks[b].count(x - lazyAdd[b]);
            for (int i = br * blockSize; i < r; i++) if (get(i) == x) res++;
        }
        return res;
    }

    /**
     * a[l...r) 内で x より小さい最大の値を返します。存在しない場合は null。
     * @param l
     * @param r
     * @param x
     * @return 値
     */
    public Long rangeLower(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        Long res = null;
        if (bl == br) {
            for (int i = l; i < r; i++) {
                long v = get(i);
                if (v < x) res = (res == null) ? v : Math.max(res, v);
            }
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) {
                long v = get(i);
                if (v < x) res = (res == null) ? v : Math.max(res, v);
            }
            for (int b = bl + 1; b < br; b++) {
                Long v = blocks[b].lower(x - lazyAdd[b]);
                if (v != null) {
                    v += lazyAdd[b];
                    res = (res == null) ? v : Math.max(res, v);
                }
            }
            for (int i = br * blockSize; i < r; i++) {
                long v = get(i);
                if (v < x) res = (res == null) ? v : Math.max(res, v);
            }
        }
        return res;
    }

    /**
     * a[l...r) 内で x 以下の最大値を返します。存在しない場合は null。
     * @param l
     * @param r
     * @param x
     * @return 値
     */
    public Long rangeFloor(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        Long res = null;
        if (bl == br) {
            for (int i = l; i < r; i++) {
                long v = get(i);
                if (v <= x) res = (res == null) ? v : Math.max(res, v);
            }
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) {
                long v = get(i);
                if (v <= x) res = (res == null) ? v : Math.max(res, v);
            }
            for (int b = bl + 1; b < br; b++) {
                Long v = blocks[b].floor(x - lazyAdd[b]);
                if (v != null) {
                    v += lazyAdd[b];
                    res = (res == null) ? v : Math.max(res, v);
                }
            }
            for (int i = br * blockSize; i < r; i++) {
                long v = get(i);
                if (v <= x) res = (res == null) ? v : Math.max(res, v);
            }
        }
        return res;
    }

    /**
     * a[l...r) 内で x 以上の最小値を返します。存在しない場合は null。
     * @param l
     * @param r
     * @param x
     * @return 値
     */
    public Long rangeCeil(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        Long res = null;
        if (bl == br) {
            for (int i = l; i < r; i++) {
                long v = get(i);
                if (v >= x) res = (res == null) ? v : Math.min(res, v);
            }
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) {
                long v = get(i);
                if (v >= x) res = (res == null) ? v : Math.min(res, v);
            }
            for (int b = bl + 1; b < br; b++) {
                Long v = blocks[b].ceil(x - lazyAdd[b]);
                if (v != null) {
                    v += lazyAdd[b];
                    res = (res == null) ? v : Math.min(res, v);
                }
            }
            for (int i = br * blockSize; i < r; i++) {
                long v = get(i);
                if (v >= x) res = (res == null) ? v : Math.min(res, v);
            }
        }
        return res;
    }

    /**
     * a[l...r) 内で x より大きい最小の値を返します。存在しない場合は null。
     * @param l
     * @param r
     * @param x
     * @return 値
     */
    public Long rangeHigher(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        Long res = null;
        if (bl == br) {
            for (int i = l; i < r; i++) {
                long v = get(i);
                if (v > x) res = (res == null) ? v : Math.min(res, v);
            }
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) {
                long v = get(i);
                if (v > x) res = (res == null) ? v : Math.min(res, v);
            }
            for (int b = bl + 1; b < br; b++) {
                Long v = blocks[b].higher(x - lazyAdd[b]);
                if (v != null) {
                    v += lazyAdd[b];
                    res = (res == null) ? v : Math.min(res, v);
                }
            }
            for (int i = br * blockSize; i < r; i++) {
                long v = get(i);
                if (v > x) res = (res == null) ? v : Math.min(res, v);
            }
        }
        return res;
    }

    /**
     * a[l...r) を昇順に並べたときの k 番目（0-indexed）の要素を返します。
     * @param l
     * @param r
     * @param k
     * @return k 番目の要素
     */
    public long rangeKthSmallest(int l, int r, long k) {
        long low = -2_000_000_000_000_000_000L;
        long high = 2_000_000_000_000_000_000L;
        while (high - low > 1) {
            long mid = low + (high - low) / 2;
            if (rangeRank(l, r, mid) <= k) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
     * a[l...r) を降順に並べたときの k 番目（0-indexed）の要素を返します。
     * @param l
     * @param r
     * @param k
     * @return k 番目の要素
     */
    public long rangeKthLargest(int l, int r, long k) {
        return rangeKthSmallest(l, r, (long)(r - l) - 1 - k);
    }

    /**
     * a[l...r) 内で x と等しい最初のインデックスを返します。存在しない場合は -1。
     * @param l
     * @param r
     * @param x
     * @return インデックス
     */
    public int findFirst(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        if (bl == br) {
            for (int i = l; i < r; i++) if (get(i) == x) return i;
        } else {
            for (int i = l; i < (bl + 1) * blockSize; i++) if (get(i) == x) return i;
            for (int b = bl + 1; b < br; b++) {
                if (blocks[b].contains(x - lazyAdd[b])) {
                    for (int i = b * blockSize; i < (b + 1) * blockSize; i++) {
                        if (get(i) == x) return i;
                    }
                }
            }
            for (int i = br * blockSize; i < r; i++) if (get(i) == x) return i;
        }
        return -1;
    }

    /**
     * a[l...r) 内で x と等しい最後のインデックスを返します。存在しない場合は -1。
     * @param l
     * @param r
     * @param x
     * @return インデックス
     */
    public int findLast(int l, int r, long x) {
        int bl = l / blockSize;
        int br = (r - 1) / blockSize;
        if (bl == br) {
            for (int i = r - 1; i >= l; i--) if (get(i) == x) return i;
        } else {
            for (int i = r - 1; i >= br * blockSize; i--) if (get(i) == x) return i;
            for (int b = br - 1; b > bl; b--) {
                if (blocks[b].contains(x - lazyAdd[b])) {
                    for (int i = (b + 1) * blockSize - 1; i >= b * blockSize; i--) {
                        if (get(i) == x) return i;
                    }
                }
            }
            for (int i = (bl + 1) * blockSize - 1; i >= l; i--) if (get(i) == x) return i;
        }
        return -1;
    }

    /**
     * a[l...r) 内で最小値を持つ最後のインデックスを返します。
     * @param l
     * @param r
     * @return インデックス
     */
    public int rangeMinLastIndex(int l, int r) {
        long minVal = rangeMin(l, r);
        return findLast(l, r, minVal);
    }

    /**
     * a[l...r) 内で最大値を持つ最後のインデックスを返します。
     * @param l
     * @param r
     * @return インデックス
     */
    public int rangeMaxLastIndex(int l, int r) {
        long maxVal = rangeMax(l, r);
        return findLast(l, r, maxVal);
    }

    /**
     * 内部状態を文字列として表現します。
     *
     * <p>計算量: $O(N)$</p>
     *
     * @return 内部状態の文字列表現
     */
    // 未テスト
    @Override
    public String toString() {
        long[] cur = new long[n];
        for (int i = 0; i < n; i++) cur[i] = get(i);
        return "SquareRootDecompositionBBST{a=" + java.util.Arrays.toString(cur) +
                ", lazyAdd=" + java.util.Arrays.toString(lazyAdd) + "}";
    }

    /**
     * 内部状態を標準出力に出力する。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし.</li>
     *   <li>副作用: 標準出力への出力。</li>
     *   <li>計算量: $O(N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     */
    // 未テスト
    public void dump() {
        System.out.println(toString());
    }
}
