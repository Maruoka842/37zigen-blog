package library.util.linalg;

import java.util.Arrays;

/**
 * F_2 上のベクトル空間における、接頭辞（Prefix）ごとの貪欲線形基底（Greedy Linear Basis）を管理するデータ構造。
 * セグメント木を使用せずに、任意の区間 [L, R) に対して線形基底のクエリを O(d) で処理できる。
 * ここで、d = 64 はビット幅を表す。
 */
public class PrefixLinearBasisOverF2 {
    /** 接頭辞ごとの基底ベクトル。第一次元が接頭辞の長さ、第二次元がビット位置。 */
    private final long[][] basis;
    /** 接頭辞ごとの基底ベクトルの由来インデックス。第一次元が接頭辞の長さ、第二次元がビット位置。 */
    private final int[][] pos;
    /** 元の配列の長さ N。 */
    private final int n;

    /**
     * 与えられた配列 a から Prefix Linear Basis を構築する。
     * 未テスト。
     * 事前条件: a != null。
     * 事後条件: O(N d) の時間および空間計算量で接頭辞ごとの基底状態が構築される。
     * 計算量: O(N d)。ただし d = 64。
     * @param a 構築元の配列
     */
    public PrefixLinearBasisOverF2(long[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }
        this.n = a.length;
        this.basis = new long[n + 1][64];
        this.pos = new int[n + 1][64];

        for (int i = 0; i <= n; i++) {
            Arrays.fill(pos[i], -1);
        }

        for (int i = 0; i < n; i++) {
            // 前のステップの状態をコピー
            System.arraycopy(basis[i], 0, basis[i + 1], 0, 64);
            System.arraycopy(pos[i], 0, pos[i + 1], 0, 64);

            long val = a[i];
            int p = i;
            for (int b = 63; b >= 0; b--) {
                if (((val >>> b) & 1) == 1) {
                    if (basis[i + 1][b] == 0) {
                        basis[i + 1][b] = val;
                        pos[i + 1][b] = p;
                        break;
                    }
                    if (p > pos[i + 1][b]) {
                        long tmpVal = val;
                        val = basis[i + 1][b];
                        basis[i + 1][b] = tmpVal;

                        int tmpP = p;
                        p = pos[i + 1][b];
                        pos[i + 1][b] = tmpP;
                    }
                    val ^= basis[i + 1][b];
                }
            }
        }
    }

    /**
     * 元の配列の長さ N を取得する。
     * 未テスト。
     * 計算量: O(1)。
     * @return 元の配列の長さ N
     */
    public int size() {
        return n;
    }

    /**
     * 区間 [l, r) における線形基底を降順（MSBの大きい順）に取得する。
     * 未テスト。
     * 事前条件: 0 <= l <= r <= N。
     * 計算量: O(d)。ただし d = 64。
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @return 区間 [l, r) の線形基底を格納した配列
     */
    public long[] basis(int l, int r) {
        if (l < 0 || r > n || l >= r) {
            return new long[0];
        }
        int count = 0;
        for (int b = 63; b >= 0; b--) {
            if (basis[r][b] != 0 && pos[r][b] >= l) {
                count++;
            }
        }
        long[] res = new long[count];
        int idx = 0;
        for (int b = 63; b >= 0; b--) {
            if (basis[r][b] != 0 && pos[r][b] >= l) {
                res[idx++] = basis[r][b];
            }
        }
        return res;
    }

    /**
     * 区間 [l, r) における線形基底の次元（基底ベクトルの個数、ランク）を取得する。
     * 未テスト。
     * 事前条件: 0 <= l <= r <= N。
     * 計算量: O(d)。ただし d = 64。
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @return 区間 [l, r) の線形基底の次元
     */
    public int rank(int l, int r) {
        if (l < 0 || r > n || l >= r) {
            return 0;
        }
        int count = 0;
        for (int b = 63; b >= 0; b--) {
            if (basis[r][b] != 0 && pos[r][b] >= l) {
                count++;
            }
        }
        return count;
    }

    /**
     * 区間 [l, r) の要素の任意の部分集合の XOR 和の最大値を取得する。
     * 値の比較は 64 ビット符号なし整数 (unsigned 64-bit integer) として行われる。
     * 未テスト。
     * 事前条件: 0 <= l <= r <= N。
     * 計算量: O(d)。ただし d = 64。
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @return 最大の XOR 和（符号なし比較による最大値）
     */
    public long maxXor(int l, int r) {
        if (l < 0 || r > n || l >= r) {
            return 0;
        }
        long res = 0;
        for (int b = 63; b >= 0; b--) {
            if (basis[r][b] != 0 && pos[r][b] >= l) {
                if (Long.compareUnsigned(res ^ basis[r][b], res) > 0) {
                    res ^= basis[r][b];
                }
            }
        }
        return res;
    }

    /**
     * 与えられた値 x が区間 [l, r) の要素の XOR 和として表せるか判定する。
     * 未テスト。
     * 事前条件: 0 <= l <= r <= N。
     * 計算量: O(d)。ただし d = 64。
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @param x 判定対象の値
     * @return x が区間の XOR 和として表せる場合は true、そうでない場合は false
     */
    public boolean contains(int l, int r, long x) {
    	//https://atcoder.jp/contests/abc223/submissions/77427549
        return reduce(l, r, x) == 0;
    }

    /**
     * 区間 [l, r) の線形基底を用いて値 x をできるだけ消去（簡約）した結果を返す。
     * 未テスト。
     * 事前条件: 0 <= l <= r <= N。
     * 計算量: O(d)。ただし d = 64。
     * @param l 開始インデックス（inclusive）
     * @param r 終了インデックス（exclusive）
     * @param x 簡約対象の値
     * @return 簡約された値
     */
    public long reduce(int l, int r, long x) {
        if (l < 0 || r > n || l >= r) {
            return x;
        }
        for (int b = 63; b >= 0; b--) {
            if (((x >>> b) & 1) == 1) {
                if (basis[r][b] != 0 && pos[r][b] >= l) {
                    x ^= basis[r][b];
                }
            }
        }
        return x;
    }

    /**
     * 保持している接頭辞ごとの線形基底の全状態を標準出力にダンプ（表示）する。
     * デバッグ用途に利用される。
     * 未テスト。
     * 計算量: O(N d)。ただし d = 64。
     */
    public void dump() {
        System.out.println("PrefixLinearBasisOverF2 Dump:");
        for (int i = 0; i <= n; i++) {
            System.out.print("Prefix " + i + ": ");
            boolean first = true;
            for (int b = 63; b >= 0; b--) {
                if (basis[i][b] != 0) {
                    if (!first) {
                        System.out.print(", ");
                    }
                    System.out.print("[bit " + b + ": val=" + basis[i][b] + " (from idx " + pos[i][b] + ")]");
                    first = false;
                }
            }
            if (first) {
                System.out.print("empty");
            }
            System.out.println();
        }
    }
}
