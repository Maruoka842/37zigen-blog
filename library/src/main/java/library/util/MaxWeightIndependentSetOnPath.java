package library.util;

import library.util.collections.IntLinkedListArray;
import library.util.collections.MyPriorityQueue;

/**
 * 最大非隣接和を求めるライブラリ。
 */
public class MaxWeightIndependentSetOnPath {

    /**
     * $N$要素の配列 $as$ から非隣接な $k$ 個の要素を選んだときの和の最大値 $ans[k]$ をすべての $0 \le k \le \lceil N/2 \rceil$ について計算する。
     * $ans$ は凹関数（$ans[k+1] - ans[k] \ge ans[k+2] - ans[k+1]$）となる。
     *
     * @param as 入力配列 $as$
     * @return $ans[k]$ (和の最大値) の配列
     * @throws NullPointerException $as$ が null の場合
     * 計算量: $O(N \log N)$
     * 副作用: なし
     */
    public static long[] solve(long[] as) {
        int n = as.length;
        boolean[] del = new boolean[n + 2];
        IntLinkedListArray list = new IntLinkedListArray(n + 2);
        for (int i = 0; i <= n; i++) {
            list.addEdge(i, i + 1);
        }
        long[] ds = new long[n + 2];

        // PriorityQueue to store pairs of (value, index) for greedy selection.
        MyPriorityQueue<long[]> que = new MyPriorityQueue<>((x, y) -> Long.compare(y[0], x[0]));

        del[0] = del[n + 1] = true;
        for (int i = 1; i <= n; i++) {
            ds[i] = as[i - 1];
            que.add(new long[]{ds[i], i});
        }

        long[] ans = new long[(n + 1) / 2 + 1];
        int k = 0;
        while (!que.isEmpty()) {
            long[] top = que.poll();
            int i = (int) top[1];
            if (!del[i]) {
                ans[k + 1] = ans[k] + ds[i];
                k++;
                int l = list.prev(i);
                int r = list.next(i);

                if (1 <= l && r <= n && !del[l] && !del[r]) {
                    ds[i] = ds[l] + ds[r] - ds[i];
                    list.spliceOut(l);
                    list.spliceOut(r);
                    que.add(new long[]{ds[i], i});
                } else {
                    list.spliceOut(i);
                    del[i] = true;
                }
                del[l] = del[r] = true;
            }
        }
        return ans;
    }

    /**
     * $N$要素の配列 $as$ から非隣接な $k$ 個の要素を選んだときの和の最大値 $ans[k]$ をすべての $0 \le k \le \lceil N/2 \rceil$ について計算する。
     * $ans$ は凹関数（$ans[k+1] - ans[k] \ge ans[k+2] - ans[k+1]$）となる。
     *
     * @param as 入力配列 $as$
     * @return $ans[k]$ (和の最大値) の配列
     * @throws NullPointerException $as$ が null の場合
     * 計算量: $O(N \log N)$
     * 副作用: なし
     */
    public static long[] solve(int[] as) {
        long[] las = new long[as.length];
        for (int i = 0; i < as.length; i++) las[i] = as[i];
        return solve(las);
    }
}
