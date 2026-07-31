package library.util.fold;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.seq.SortedArrays;

/**
 * 静的区間転倒数クエリ
 * Static Range Inversion Query
 * <p>計算量: 構築 O(N√N), クエリ O(√N)
 * 空間計算量: O(N√N) (~128MB for N=1e5)
 *
 * 参照: <https://stackoverflow.com/questions/21763392/counting-inversions-in-ranges>
 */
public class StaticRangeInversion {
    /** 配列の長さ */
    private final int N;
    /** バケットサイズ */
    private final int bs;
    /** バケット数 */
    private final int nb_bc;
    /** 座標圧縮後の値の配列 */
    private final int[] vals;
    /** バケットごとにソートされた (値, 元のインデックス) のペア。 (compressed_value << 32 | original_index) でパッキングされている。 */
    private final long[][] vals_sorted;
    /** presuf[ibc][i] はバケット ibc と要素 vals[i] との間の転倒数 */
    private final int[][] presuf;
    /** バケット内の接尾辞の転倒数 */
    private final int[] sufG;
    /** バケット内の接頭辞の転倒数 */
    private final int[] preH;
    /** R[i][j] はフルバケット i から j までの合計転倒数 */
    private final long[][] R;

    /**
     * O(N√N)
     * @param sequence 構築対象の配列
     */
    public StaticRangeInversion(int[] sequence) {
        this.N = sequence.length;
        if (N == 0) {
            this.bs = 1;
            this.nb_bc = 0;
            this.vals = new int[0];
            this.vals_sorted = new long[0][];
            this.presuf = new int[0][0];
            this.sufG = new int[0];
            this.preH = new int[0];
            this.R = new long[0][0];
            return;
        }
        this.bs = (int) Math.max(1, Math.ceil(Math.sqrt(N)));
        this.nb_bc = (N + bs - 1) / bs;

        // 座標圧縮
        int[] dict = ArrayUtils.sortq(sequence);
        final int D = dict.length;
        this.vals = new int[N];
        for (int i = 0; i < N; i++) {
            vals[i] = SortedArrays.floor(dict, sequence[i]);
        }

        this.vals_sorted = new long[nb_bc][];
        this.presuf = new int[nb_bc][N];
        this.sufG = new int[N];
        this.preH = new int[N];

        for (int ibc = 0; ibc < nb_bc; ibc++) {
            int L = ibc * bs;
            int R_bound = Math.min(L + bs, N);

            // バケット内の値をソート
            long[] bucket_sorted = new long[R_bound - L];
            for (int i = L; i < R_bound; i++) {
                bucket_sorted[i - L] = ((long) vals[i] << 32) | i;
            }
            Arrays.sort(bucket_sorted);
            vals_sorted[ibc] = bucket_sorted;

            // バケット内の値の頻度
            int[] cnt = new int[D + 1];
            for (int i = L; i < R_bound; i++) {
                cnt[vals[i] + 1]++;
            }
            for (int i = 0; i < D; i++) {
                cnt[i + 1] += cnt[i];
            }

            // 他のバケットの要素との転倒数への寄与を前計算
            // presuf[ibc][i] はバケット ibc と要素 vals[i] との間の転倒数
            for (int b = 0; b < ibc; b++) {
                int bL = b * bs;
                int bR = (b + 1) * bs;
                for (int i = bR - 1; i >= bL; i--) {
                    presuf[ibc][i] = (i + 1 == bR ? 0 : presuf[ibc][i + 1]) + cnt[vals[i]];
                }
            }
            for (int b = ibc + 1; b < nb_bc; b++) {
                int bL = b * bs;
                int bR = Math.min((b + 1) * bs, N);
                for (int i = bL; i < bR; i++) {
                    presuf[ibc][i] = (i == bL ? 0 : presuf[ibc][i - 1]) + (cnt[D] - cnt[vals[i] + 1]);
                }
            }

            // preH: バケット内の接頭辞の転倒数
            // sufG: バケット内の接尾辞の転倒数
            for (int i = L; i < R_bound; i++) {
                int inv = 0;
                for (int j = L; j < i; j++) {
                    if (vals[j] > vals[i]) inv++;
                }
                preH[i] = (i == L ? 0 : preH[i - 1]) + inv;
            }
            for (int i = R_bound - 1; i >= L; i--) {
                int inv = 0;
                for (int j = i + 1; j < R_bound; j++) {
                    if (vals[j] < vals[i]) inv++;
                }
                sufG[i] = (i == R_bound - 1 ? 0 : sufG[i + 1]) + inv;
            }
        }

        // R[i][j]: フルバケット i から j までの転倒数
        this.R = new long[nb_bc][nb_bc];
        for (int i = nb_bc - 1; i >= 0; i--) {
            R[i][i] = sufG[i * bs];
            for (int j = i + 1; j < nb_bc; j++) {
                R[i][j] = R[i][j - 1] + R[i + 1][j] - (i + 1 == j ? 0 : R[i + 1][j - 1]) + presuf[j][i * bs];
            }
        }
    }

    /**
     * 区間 [l, r) の転倒数を返す
     * O(√N)
     * @param l inclusive
     * @param r exclusive
     * @return 転倒数
     */
    public long get(int l, int r) {
        if (l >= r) return 0;
        int lb = (l + bs - 1) / bs;
        int rb = r / bs - 1;

        // 同じバケット内のクエリ
        if (l / bs == (r - 1) / bs) {
            int b = l / bs;
            long ret = preH[r - 1] - (l % bs == 0 ? 0 : preH[l - 1]);
            int less_cnt = 0;
            long[] bucket_sorted = vals_sorted[b];
            for (int p = 0; p < bucket_sorted.length; p++) {
                int idx = (int) bucket_sorted[p];
                if (idx >= l && idx < r) {
                    less_cnt++;
                } else if (idx < l) {
                    ret -= less_cnt;
                }
            }
            return ret;
        }

        // バケットを跨ぐクエリ
        long ret = (lb <= rb ? R[lb][rb] : 0);

        // 左側の端数
        if (bs * lb > l) {
            ret += sufG[l];
            for (int b = lb; b <= rb; b++) {
                ret += presuf[b][l];
            }
        }
        // 右側の端数
        if (bs * (rb + 1) < r) {
            ret += preH[r - 1];
            for (int b = lb; b <= rb; b++) {
                ret += presuf[b][r - 1];
            }
        }

        // 左側の端数と右側の端数の間の転倒数
        if (bs * lb > l && bs * (rb + 1) < r) {
            int b_pre = l / bs;
            int b_suf = (r - 1) / bs;
            long[] pre_sorted = vals_sorted[b_pre];
            long[] suf_sorted = vals_sorted[b_suf];
            int less_cnt = 0;
            int j = 0;
            for (int i = 0; i < pre_sorted.length; i++) {
                int idx_pre = (int) pre_sorted[i];
                if (idx_pre >= l) {
                    int val_pre = (int) (pre_sorted[i] >> 32);
                    while (j < suf_sorted.length) {
                        int val_suf = (int) (suf_sorted[j] >> 32);
                        int idx_suf = (int) suf_sorted[j];
                        if (idx_suf >= r || val_suf < val_pre) {
                            if (idx_suf < r) less_cnt++;
                            j++;
                        } else {
                            break;
                        }
                    }
                    ret += less_cnt;
                }
            }
        }

        return ret;
    }
}
