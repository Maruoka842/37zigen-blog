package library.util;

import java.util.Arrays;

/**
 * 整数列Aに対して、f(S)=sum_{i in S} A_i s.t. for all i, j in S i neq j => |i - j| >= K を考える。
 * このようなSのうち、max_{S} f(S)を |S|毎に列挙するメソッド、および、与えられたk=|S|についてのみ求めるメソッド。
 */
public class MaxWeightIndependentSetWithDistanceK {

    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * すべての 0 <= k <= (N + K - 1) / K について、サイズ k の独立集合の最大重みを求める。
     *
     * @param a 整数列
     * @param K 最小距離 (K >= 1)
     * @return ans[k] = max { \sum_{i \in S} a[i] : |S| = k, \forall i, j \in S, i \neq j \implies |i - j| \ge K }. 存在しない場合は -INF。
     * 計算量: O(N K^2 log N)
     * 未テスト
     */
    public static long[] solveAll(long[] a, int K) {
        int n = a.length;
        if (n == 0) return new long[]{0};
        Node res = solveAllRecursive(a, 0, n, K);
        long[] ans = trim(res.dp[0][0]);
        for (int i = 0; i < ans.length; i++) {
            if (ans[i] < -INF / 2) ans[i] = -INF;
        }
        return ans;
    }

    private static class Node {
        long[][][] dp;
        int size;

        Node(int K) {
            dp = new long[K + 1][K + 1][];
        }
    }

    private static Node solveAllRecursive(long[] a, int L, int R, int K) {
        int n = R - L;
        Node node = new Node(K);
        if (n == 1) {
            node.size = 1;
            for (int i = 0; i <= K; i++) {
                for (int j = 0; j <= K; j++) {
                    if (i == 0 && j == 0) {
                        node.dp[i][j] = new long[]{0, a[L]};
                    } else {
                        node.dp[i][j] = new long[]{0};
                    }
                }
            }
            return node;
        }

        int mid = (L + R) / 2;
        Node left = solveAllRecursive(a, L, mid, K);
        Node right = solveAllRecursive(a, mid, R, K);
        node.size = n;

        for (int i = 0; i <= K; i++) {
            for (int j = 0; j <= K; j++) {
                int kMax = (n + K - 1) / K;
                long[] res = new long[kMax + 1];
                Arrays.fill(res, -INF);
                res[0] = 0;

                int nextJForL = Math.min(K, Math.max(0, j - right.size));
                long[] lSeqOnly = left.dp[i][nextJForL];
                for (int k = 1; k < Math.min(res.length, lSeqOnly.length); k++) {
                    if (lSeqOnly[k] > -INF / 2) res[k] = Math.max(res[k], lSeqOnly[k]);
                }

                int nextIForR = Math.min(K, Math.max(0, i - left.size));
                long[] rSeqOnly = right.dp[nextIForR][j];
                for (int k = 1; k < Math.min(res.length, rSeqOnly.length); k++) {
                    if (rSeqOnly[k] > -INF / 2) res[k] = Math.max(res[k], rSeqOnly[k]);
                }

                for (int x = 0; x < K; x++) {
                    int y = K - 1 - x;
                    long[] lS = trim(left.dp[i][x]);
                    long[] rS = trim(right.dp[y][j]);
                    if (lS.length <= 1 || rS.length <= 1) continue;

                    long[] lSub = new long[lS.length - 1];
                    System.arraycopy(lS, 1, lSub, 0, lSub.length);
                    long[] rSub = new long[rS.length - 1];
                    System.arraycopy(rS, 1, rSub, 0, rSub.length);

                    long[] conv = MaxPlus.convolveConcaveConcave(lSub, rSub);
                    for (int k = 0; k < conv.length; k++) {
                        if (k + 2 < res.length) {
                            if (conv[k] > -INF / 2) res[k + 2] = Math.max(res[k + 2], conv[k]);
                        }
                    }
                }
                node.dp[i][j] = trim(res);
            }
        }
        return node;
    }

    private static long[] trim(long[] a) {
        int n = a.length;
        while (n > 1 && a[n - 1] <= -INF / 2) n--;
        return Arrays.copyOf(a, n);
    }

    /**
     * 指定されたサイズ k の独立集合の最大重みを求める。
     *
     * @param a 整数列
     * @param K 最小距離 (K >= 1)
     * @param k 選択する要素数
     * @return max { \sum_{i \in S} a[i] : |S| = k, \forall i, j \in S, i \neq j \implies |i - j| \ge K }. 存在しない場合は -INF。
     * 計算量: O(N log(max |A_i|))
     * 未テスト
     */
    public static long solveK(long[] a, int K, int k) {
    	//https://atcoder.jp/contests/abc462/submissions/77104408
        int n = a.length;
        if (k == 0) return 0;
        if (k < 0) return -INF;
        if (n == 0) return -INF;
        if (k > (n + K - 1) / K) return -INF;
        int m = (n + K - 1) / K;//最大の集合サイズ
        long minA=ArrayUtils.min(a);
        long maxA=ArrayUtils.max(a);
        long low = minA-1L*(m+1)*(maxA-minA+1);
        long high = maxA+1;

        for (int iter = 0; iter < 80; iter++) {
            long mid = (low + high) / 2;
            Result res = solveUnconstrained(a, K, mid);
            if (res.maxCount >= k) {
                low = mid;
            } else {
                high = mid;
            }
        }

        Result res = solveUnconstrained(a, K, low);
        if (res.minCount <= k && k <= res.maxCount) {
            return res.maxValue + (long) k * low;
        } else {
            return -INF;
        }
    }

    private static class Result {
        long maxValue;
        int minCount;
        int maxCount;

        Result(long v, int minC, int maxC) {
            maxValue = v;
            minCount = minC;
            maxCount = maxC;
        }
    }

    private static Result solveUnconstrained(long[] a, int K, long lambda) {
        int n = a.length;
        long[] dp = new long[n + 1];
        int[] minC = new int[n + 1];
        int[] maxC = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            dp[i] = dp[i - 1];
            minC[i] = minC[i - 1];
            maxC[i] = maxC[i - 1];

            long val = (i >= K ? dp[i - K] : 0) + a[i - 1] - lambda;
            int curMinC = (i >= K ? minC[i - K] : 0) + 1;
            int curMaxC = (i >= K ? maxC[i - K] : 0) + 1;

            if (val > dp[i]) {
                dp[i] = val;
                minC[i] = curMinC;
                maxC[i] = curMaxC;
            } else if (val == dp[i]) {
                minC[i] = Math.min(minC[i], curMinC);
                maxC[i] = Math.max(maxC[i], curMaxC);
            }
        }
        return new Result(dp[n], minC[n], maxC[n]);
    }
}
