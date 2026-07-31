package library.util.seq;

import java.util.Arrays;
import library.util.ArrayUtils;

/**
 * 静的な配列に対する区間最頻値クエリを平方分割を用いて処理する。
 */
public class StaticRangeModeQuery {
    private final int n;
    private final int[] a;
    private final int[] originalValues;
    private final int[][] indices;
    private final int[] pos;

    private final int blockSize;
    private final int blockCount;
    private final int[][] modeTable;
    private final int[][] freqTable;

    /**
     * 長さ n の配列 a に対するクエリの前計算を行う。
     * 計算量: O(n \sqrt{n})
     *
     * @param a クエリ対象の配列
     */
    public StaticRangeModeQuery(int[] a) {
        this.n = a.length;
        this.originalValues = ArrayUtils.sortq(a);
        this.a = ArrayUtils.compress(a);

        int distinctCount = originalValues.length;
        int[] counts = MultiPermutation.bincount(this.a);
        this.indices = new int[distinctCount][];
        for (int i = 0; i < distinctCount; i++) {
            this.indices[i] = new int[counts[i]];
        }
        int[] ptr = new int[distinctCount];
        this.pos = new int[n];
        for (int i = 0; i < n; i++) {
            int x = this.a[i];
            pos[i] = ptr[x];
            indices[x][ptr[x]++] = i;
        }

        this.blockSize = Math.max(1, (int) Math.sqrt(n));
        this.blockCount = (n + blockSize - 1) / blockSize;

        this.modeTable = new int[blockCount + 1][blockCount + 1];
        this.freqTable = new int[blockCount + 1][blockCount + 1];

        int[] freq = new int[distinctCount];
        for (int i = 0; i < blockCount; i++) {
            Arrays.fill(freq, 0);
            int curMode = -1;
            int curMaxFreq = 0;
            for (int j = i; j < blockCount; j++) {
                int start = j * blockSize;
                int end = Math.min(n, (j + 1) * blockSize);
                for (int k = start; k < end; k++) {
                    int x = this.a[k];
                    freq[x]++;
                    if (freq[x] > curMaxFreq) {
                        curMaxFreq = freq[x];
                        curMode = x;
                    }
                }
                modeTable[i][j + 1] = curMode;
                freqTable[i][j + 1] = curMaxFreq;
            }
        }
    }

    /**
     * 区間 [l, r) における最頻値とその出現回数を返す。
     * 計算量: O(\sqrt{n})
     *
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @return {最頻値, 出現回数}
     */
    public int[] query(int l, int r) {
        if (l >= r) return new int[]{0, 0};

        int bl = (l + blockSize - 1) / blockSize;
        int br = r / blockSize;

        int resMode = -1;
        int resFreq = 0;

        if (bl < br) {
            resMode = modeTable[bl][br];
            resFreq = freqTable[bl][br];

            for (int i = l; i < bl * blockSize; i++) {
                int x = a[i];
                int p = pos[i];
                while (p + resFreq < indices[x].length && indices[x][p + resFreq] < r) {
                    resFreq++;
                    resMode = x;
                }
            }
            for (int i = br * blockSize; i < r; i++) {
                int x = a[i];
                int p = pos[i];
                while (p - resFreq >= 0 && indices[x][p - resFreq] >= l) {
                    resFreq++;
                    resMode = x;
                }
            }
        } else {
            for (int i = l; i < r; i++) {
                int x = a[i];
                int p = pos[i];
                while (p + resFreq < indices[x].length && indices[x][p + resFreq] < r) {
                    resFreq++;
                    resMode = x;
                }
            }
        }

        return new int[]{originalValues[resMode], resFreq};
    }
}
