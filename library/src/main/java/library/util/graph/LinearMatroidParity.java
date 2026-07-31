package library.util.graph;

import java.util.Random;

import library.util.linalg.MatrixUtilsFp;

import java.util.Arrays;

/**
 * 線形マトロイドパリティ問題を解くクラス。
 * 与えられたベクトルのペア集合から、線形独立なものの和集合のサイズが最大となるようなペアの集合を求める。
 *
 * アルゴリズムの概要:
 * 1. 各ペア (b_i, c_i) に対してランダムな値 x_i を割り当て、歪対称行列 Y = Σ x_i (b_i c_i^T - c_i b_i^T) を作成する。
 * 2. Y のランクの半分が、最大マッチングサイズ（選べるペアの最大数）に対応する。
 * 3. 辞書順最小の解を求めるため、各ペアを順番に取り除いてみて、ランクが減らなければそのペアを除去する。
 * 4. 行列の更新には Sherman-Morrison の公式を用いた逆行列の動的更新を行い、計算量を抑える。
 *
 * 参考:
 * [1] H. Y. Cheung, L. C. Lau, K. M. Leung,
 *     "Algebraic Algorithms for Linear Matroid Parity Problems,"
 *     ACM Transactions on Algorithms, 10(3), 1-26, 2014.
 */
public class LinearMatroidParity {
    /**
     * ベクトルのペアを表すレコード。
     */
    public record VectorPair(long[] b, long[] c) {}

    private LinearMatroidParity() {}

    /**
     * 線形マトロイドパリティ問題の最大マッチングサイズを返す。
     * 成功確率は少なくとも 1 - r/mod。
     * 計算量: O(r^2(m + r)) (r: 次数, m: ペア数)
     * @param bcs ベクトルのペアの配列
     * @param mod 法（素数）
     * @return 最大マッチングサイズ（選べるペアの最大数）
     */
    public static int size(VectorPair[] bcs, long mod) {
    	//https://yukicoder.me/submissions/1165728
        return size(bcs, mod, System.currentTimeMillis());
    }

    /**
     * 線形マトロイドパリティ問題の最大マッチングサイズを返す。
     * 成功確率は少なくとも 1 - r/mod。
     * 計算量: O(r^2(m + r)) (r: 次数, m: ペア数)
     * @param bcs ベクトルのペアの配列
     * @param mod 法（素数）
     * @param seed 乱数の種
     * @return 最大マッチングサイズ（選べるペアの最大数）
     */
    public static int size(VectorPair[] bcs, long mod, long seed) {
        if (bcs.length == 0) return 0;
        int r = bcs[0].b().length;
        // 歪対称行列 Y = Σ x_i (b_i c_i^T - c_i b_i^T) を構築
        long[][] mat = new long[r][r];
        Random rnd = new Random(seed);

        for (VectorPair bc : bcs) {
            long x = rnd.nextLong(mod);
            long[] b = bc.b();
            long[] c = bc.c();
            for (int i = 0; i < r; i++) {
                if (b[i] == 0 && c[i] == 0) continue;
                for (int j = 0; j < r; j++) {
                    // b_i * c_j^T - c_i * b_j^T
                    long val = x * (b[i] * c[j] % mod - b[j] * c[i] % mod + mod) % mod;
                    mat[i][j] = (mat[i][j] + val) % mod;
                }
            }
        }
        // 歪対称行列のランクは常に偶数であり、その半分が最大パリティマッチングのサイズになる
        return MatrixUtilsFp.rank(mat, mod) / 2;
    }

    /**
     * 線形マトロイドパリティ問題を解き、辞書順最小の解を返す。
     * ここで「辞書順最小」とは、各ペアを採用するかどうかを表す真偽値配列を
     * (false: 0, true: 1) と見なしたときの、バイナリベクトルとしての辞書順比較を指す。
     * つまり、可能な限りインデックスの小さいペアを除去（false）しようとする解を返す。
     *
     * 成功確率は少なくとも 1 - (n + m)/mod。
     * 計算量: O(d^2(d + m)) (d = 2 * ceil(r/2), m: ペア数)
     * @param bcs ベクトルのペアの配列
     * @param mod 法（素数）
     * @return 各ペアを採用するかどうかの真偽値配列（採用する場合 true）
     */
    public static boolean[] solve(VectorPair[] bcs, long mod) {
        return solve(bcs, mod, System.currentTimeMillis());
    }

    /**
     * 線形マトロイドパリティ問題を解き、辞書順最小の解を返す。
     * ここで「辞書順最小」とは、各ペアを採用するかどうかを表す真偽値配列を
     * (false: 0, true: 1) と見なしたときの、バイナリベクトルとしての辞書順比較を指す。
     * つまり、可能な限りインデックスの小さいペアを除去（false）しようとする解を返す。
     *
     * 成功確率は少なくとも 1 - (n + m)/mod。
     * 計算量: O(d^2(d + m)) (d = 2 * ceil(r/2), m: ペア数)
     * @param bcs ベクトルのペアの配列
     * @param mod 法（素数）
     * @param seed 乱数の種
     * @return 各ペアを採用するかどうかの真偽値配列（採用する場合 true）
     */
    public static boolean[] solve(VectorPair[] bcs, long mod, long seed) {
        if (bcs.length == 0) return new boolean[0];
        int m = bcs.length;
        int r = bcs[0].b().length;
        // 歪対称行列が正則になるよう、次元を偶数に切り上げ、必要に応じてダミーのペアを追加する
        int r2 = (r + 1) / 2;
        int n = r2 * 2;
        Random rnd = new Random(seed);

        VectorPair[] currentBcs = new VectorPair[m];
        long[] currentX = new long[m];
        for (int i = 0; i < m; i++) {
            long[] nb = new long[n];
            long[] nc = new long[n];
            System.arraycopy(bcs[i].b(), 0, nb, 0, r);
            System.arraycopy(bcs[i].c(), 0, nc, 0, r);
            currentBcs[i] = new VectorPair(nb, nc);
            currentX[i] = rnd.nextLong(mod);
        }

        long[][] yInv = null;
        while (true) {
            // 初期行列 Y を構築
            long[][] y = new long[n][n];
            for (int i = 0; i < currentBcs.length; i++) {
                long[] b = currentBcs[i].b();
                long[] c = currentBcs[i].c();
                long xi = currentX[i];
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        long val = xi * (b[j] * c[k] % mod - b[k] * c[j] % mod + mod) % mod;
                        y[j][k] = (y[j][k] + val) % mod;
                    }
                }
            }
            int rank = MatrixUtilsFp.rank(y, mod);
            if (rank < n) {
                // ランクが足りない場合は、ランダムなダミーペアを追加して正則にする
                int numAdd = (n - rank) / 2;
                int oldLen = currentBcs.length;
                currentBcs = Arrays.copyOf(currentBcs, oldLen + numAdd);
                currentX = Arrays.copyOf(currentX, oldLen + numAdd);
                for (int i = oldLen; i < currentBcs.length; i++) {
                    long[] nb = new long[n];
                    long[] nc = new long[n];
                    for (int j = 0; j < n; j++) {
                        nb[j] = rnd.nextLong(mod);
                        nc[j] = rnd.nextLong(mod);
                    }
                    currentBcs[i] = new VectorPair(nb, nc);
                    currentX[i] = rnd.nextLong(mod);
                }
            } else {
                // 逆行列を計算。計算できれば準備完了
                yInv = MatrixUtilsFp.inv(y, mod);
                if (yInv == null) throw new AssertionError("MatrixUtilsFp.modInv failed for a full-rank matrix.");
                break;
            }
        }

        boolean[] ret = new boolean[currentBcs.length];
        Arrays.fill(ret, true);

        // まず追加したダミーペアを取り除けるか試す
        for (int i = m; i < currentBcs.length; i++) {
            yInv = tryErase(i, currentBcs, currentX, yInv, ret, mod);
        }
        // 次に元のペアを前から順番に取り除けるか試す（これにより辞書順最小性が保証される）
        for (int i = 0; i < m; i++) {
            yInv = tryErase(i, currentBcs, currentX, yInv, ret, mod);
        }

        return Arrays.copyOf(ret, m);
    }

    /**
     * インデックス i のペアを除去してもランクが維持されるか試行する。
     * ランクが維持されるなら除去し、逆行列を更新して返す。
     * 除去により特異行列になる（ランクが下がる）場合は、元の逆行列をそのまま返す。
     */
    private static long[][] tryErase(int i, VectorPair[] bcs, long[] x, long[][] yInv, boolean[] ret, long mod) {
        long[] b = bcs[i].b();
        long[] c = bcs[i].c();
        long xi = x[i];
        int n = yInv.length;

        // ペアの除去 ΔY = -x_i(bc^T - cb^T) を Sherman-Morrison の公式を2回適用して更新する。
        // ΔY = (-x_i b) c^T + (x_i c) b^T
        long mxi = (mod - xi) % mod;
        long[] u1 = new long[n];
        for (int j = 0; j < n; j++) u1[j] = b[j] * mxi % mod;

        // 1回目のランク1更新
        long[][] nextYinv = MatrixUtilsFp.invUpdateRank1(yInv, u1, c, mod);
        if (nextYinv == null) return yInv; // 1回目で特異になるならランクが下がるので除去不可

        long[] u2 = new long[n];
        for (int j = 0; j < n; j++) u2[j] = c[j] * xi % mod;

        // 2回目のランク1更新
        long[][] finalYinv = MatrixUtilsFp.invUpdateRank1(nextYinv, u2, b, mod);
        if (finalYinv == null) {
            // 歪対称行列の性質上、ランクは2ずつ変化するため、1回目が成功して2回目が失敗することはないはず
            throw new AssertionError("Inconsistent rank update for skew-symmetric matrix.");
        }

        ret[i] = false; // 除去成功
        return finalYinv;
    }
}
