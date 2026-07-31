package library.util.fold;

import library.util.ArrayUtils;
import library.util.seq.MultiPermutation;

/**
 * 静的整数列に対する区間種類数クエリ
 * Static Range Distinct Count Query
 * <p>計算量: 構築 O(N log N), クエリ O(log N)
 *
 */
public class StaticRangeDistinctCount {
    private final WaveletMatrix wm;
    private final int n;

    /**
     * O(N log N)
     * @param a 構築対象の配列
     */
    public StaticRangeDistinctCount(int[] a) {
        this.n = a.length;
        if (n == 0) {
            this.wm = new WaveletMatrix(new int[0]);
        } else {
            int[] compressed = ArrayUtils.compress(a);
            this.wm = buildWM(compressed);
        }
    }

    /**
     * O(N log N)
     * @param a 構築対象の配列
     */
    public StaticRangeDistinctCount(long[] a) {
        this.n = a.length;
        if (n == 0) {
            this.wm = new WaveletMatrix(new int[0]);
        } else {
            int[] compressed = ArrayUtils.compress(a);
            this.wm = buildWM(compressed);
        }
    }

    private WaveletMatrix buildWM(int[] compressed) {
        int[] prev = MultiPermutation.prevOccurences(compressed);
        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            c[i] = prev[i] + 1;
        }
        return new WaveletMatrix(c);
    }

    /**
     * 区間 [l, r) に含まれる相異なる整数の種類数を返す。
     * O(log N)
     * @param l inclusive
     * @param r exclusive
     * @return 種類数
     */
    public int count(int l, int r) {
        if (l >= r) return 0;
        return wm.countLess(l, r, (long) l + 1);
    }
}
