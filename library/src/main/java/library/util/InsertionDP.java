package library.util;

import java.util.Arrays;

/**
 * 挿入DP（Insertion DP）に関するアルゴリズムおよび最適化された数え上げユーティリティを提供するクラスです。
 * 分割統治や多項式乗算（NTT）による高速化を防ぎ、純粋な $O(N^2)$ または $O(N^2 L)$ の挿入DPが必須となるような、
 * ステップ依存・状態依存の「重み付き（Weighted）」ソルバーを全スタイルに提供します。
 *
 * 本クラスは、以下の3つの主要な挿入DPスタイルをサポートします：
 *
 * 1. {@link RelativeRank}: 位置ベース（EDPC T型）の挿入DP。順列の大小関係制約を $O(N^2)$ で解きます。
 * 2. {@link GapInsertion}: 隙間挿入型。重複要素を含む場合の昇順挿入（Increasing K Times型）をサポートします。
 * 3. {@link Component}: 連結成分DP。隣接要素の絶対差の和が $L$ 以下となる順列の総数を $O(N^2 L)$ で求めます。また、連続値隣接禁止の数え上げなどの発展ソルバーも提供します。
 *
 * @author Jules
 */
public final class InsertionDP {

    private InsertionDP() {
        // インスタンス化禁止
    }

    /**
     * 位置ベース（EDPC T型、過去改変型）の挿入DP。
     * $i$ 番目の要素を挿入する際、既存の要素の相対順位（ランク）を状態として保持し、
     * 累積和（Prefix Sum）を用いて各ステップを $O(1)$、全体を $O(N^2)$ で遷移します。
     */
    public static class RelativeRank {

        private RelativeRank() {
        }

        /**
         * 重み付きの不等号制約付き順列の数え上げ。
         * 長さ $N-1$ の signs 配列、およびステップ・ランク依存の重み weights[i][q] が与えられ、
         * 以下の制約を満たす順列の総重みを $mod$ で割った余りを返します。
         * 各ステップ $i$ (1-indexed, $1 \le i < N$) で、新たに挿入する要素の相対順位が $q$ ($1 \le q \le i+1$) のとき、
         * 状態の遷移値に `weights[i][q]` を掛け合わせます。
         *
         * @param signs 隣接関係の不等号制約を示す配列（負: 小なり, 正: 大なり, 0: 制約なし）
         * @param weights 挿入ステップおよび選択ランクに依存する重み配列。
         *                `weights[i][q]` は、部分順列の長さが $i$ の状態から $i+1$ 番目の要素を相対ランク $q$ ($1 \le q \le i+1$) として末尾に挿入するステップ $i$ において適用される乗算重みを表します。
         * @param mod 法とする素数
         * @return 重み付き順列の総和を mod で割った余り
         * @complexity $O(N^2)$ 時間, $O(N)$ 空間
         * // 未テスト
         */
        public static long solvePermutationWithSignConstraintsWeighted(int[] signs, long[][] weights, long mod) {
            if (signs == null) {
                return 0;
            }
            int N = signs.length + 1;
            if (N <= 1) {
                return 1;
            }
            long[] dp = new long[N + 1];
            dp[1] = 1;
            long[] sum = new long[N + 1];
            for (int i = 1; i < N; i++) {
                for (int j = 1; j <= i; j++) {
                    sum[j] = (sum[j - 1] + dp[j]) % mod;
                }
                long[] nextDp = new long[N + 2];
                int sign = signs[i - 1];
                for (int q = 1; q <= i + 1; q++) {
                    long w = (weights != null && weights.length > i && q < weights[i].length) ? weights[i][q] % mod : 1;
                    if (w < 0) w += mod;
                    if (sign < 0) {
                        nextDp[q] = sum[q - 1] * w % mod;
                    } else if (sign > 0) {
                        long val = (sum[i] - sum[q - 1]) % mod;
                        if (val < 0) {
                            val += mod;
                        }
                        nextDp[q] = val * w % mod;
                    } else {
                        nextDp[q] = sum[i] * w % mod;
                    }
                }
                dp = nextDp;
            }
            long ans = 0;
            for (int j = 1; j <= N; j++) {
                ans = (ans + dp[j]) % mod;
            }
            return ans;
        }
    }

    /**
     * 隙間挿入型（割り込み型）の挿入DP。
     * すでに並べられている要素の「隙間」に新たな要素を順に挿入する遷移を扱います。
     */
    public static class GapInsertion {

        private GapInsertion() {
        }

        /**
         * 重複要素を含む可能性がある任意の配列 $A$ について、
         * 各ステップにおける「ペア数を維持する遷移（keep）」および「ペア数を増やす遷移（increase）」に、
         * ステップおよび現在のペア数に依存した重みを適用しながら、昇順ペアがちょうど $K$ 個存在するものの総重みを返します。
         *
         * @param A 任意の整数配列
         * @param K 昇順ペアの個数
         * @param keepWeights 昇順ペア数を増やさない挿入に対する重み配列。
         *                    `keepWeights[i][j]` は、配列 $A$ を昇順ソートしたときに $i+1$ 番目に小さい要素を、既存の $i$ 個の要素からなる部分順列（すでに $j$ 個の昇順ペアが存在する）へ挿入するステップ $i$ (0-indexed) において、昇順ペア数が $j$ のまま維持される遷移に対して適用される乗算重みを表します。
         * @param increaseWeights 昇順ペア数を1つ増やす挿入に対する重み配列。
         *                        `increaseWeights[i][j]` は、配列 $A$ を昇順ソートしたときに $i+1$ 番目に小さい要素を、既存の $i$ 個の要素からなる部分順列（すでに $j$ 個の昇順ペアが存在する）へ挿入するステップ $i$ (0-indexed) において、昇順ペア数が $j+1$ へ増加する遷移に対して適用される乗算重みを表します。
         * @param mod 法とする素数
         * @return 条件を満たす順列の総重みを mod で割った余り
         * @complexity $O(N^2)$ 時間, $O(N)$ 空間
         * // 未テスト
         */
        public static long countPermutationsWithAscentsWeighted(int[] A, int K, long[][] keepWeights, long[][] increaseWeights, long mod) {
            if (A == null || A.length == 0) {
                return K == 0 ? 1 : 0;
            }
            int N = A.length;
            if (K < 0 || K >= N) {
                return 0;
            }
            int[] sorted = A.clone();
            Arrays.sort(sorted);

            long[] dp = new long[K + 1];
            dp[0] = 1;

            // 昇順に1つずつ要素を挿入
            for (int i = 0; i < N; i++) {
                long[] nextDp = new long[K + 1];
                int val = sorted[i];
                int cnt = 0;
                for (int p = 0; p < i; p++) {
                    if (sorted[p] < val) {
                        cnt++;
                    }
                }
                int dup = i - cnt;

                for (int j = 0; j <= K; j++) {
                    if (dp[j] == 0) {
                        continue;
                    }
                    // 昇順ペア数を維持する遷移
                    long kw = (keepWeights != null && keepWeights.length > i && j < keepWeights[i].length) ? keepWeights[i][j] % mod : 1;
                    if (kw < 0) kw += mod;
                    long keepWays = (j + dup + 1) * kw % mod;
                    nextDp[j] = (nextDp[j] + dp[j] * keepWays) % mod;

                    // 昇順ペア数を1つ増やす遷移
                    long iw = (increaseWeights != null && increaseWeights.length > i && j < increaseWeights[i].length) ? increaseWeights[i][j] % mod : 1;
                    if (iw < 0) iw += mod;
                    long increaseWays = (i - j - dup);
                    if (j + 1 <= K && increaseWays > 0) {
                        long totalInc = increaseWays * iw % mod;
                        nextDp[j + 1] = (nextDp[j + 1] + dp[j] * totalInc) % mod;
                    }
                }
                dp = nextDp;
            }
            return dp[K];
        }
    }

    /**
     * 連結成分DP（Connected Component DP / Continuous Component DP）。
     * 要素を値の昇順に挿入しながら、同時に「連続する部分的な連結成分（ブロック）」の個数と境界状態を管理します。
     * 隣接する要素間の絶対差の和（グリッドや1次元直線上の移動コストなど）を状態に含めて遷移を行うことができます。
     */
    public static class Component {

        private Component() {
        }

        /**
         * 重複要素を含む可能性がある任意の配列 $A$ について、
         * 各ステップの5つのケースの遷移に対して任意の重み `caseWeights[i][case_id]` を適用しながら、
         * 隣接要素の絶対差の総和が $L$ 以下となる順列の総和を mod で割った余りを計算します。
         *
         * @param A 任意の配列
         * @param L 絶対差の総和の上限値
         * @param caseWeights 挿入ステップおよび遷移ケースに適用する重み配列。
         *                    `caseWeights[i][case_id]` は、配列 $A$ を昇順ソートしたときに $i+1$ 番目に小さい要素を挿入するステップ $i$ (0-indexed) において、遷移ケース `case_id` ($0 \le case\_id < 5$) に適用される乗算重みを表します。
         *                    遷移ケース `case_id` の定義は以下の通りです：
         *                    - 0: 既存の連結成分と隣接しない位置に、新しく単独の連結成分を1つ作る（全体の端点以外への挿入）
         *                    - 1: 既存の連結成分と隣接しない位置に、新しく単独の連結成分を1つ作る（全体の端点への挿入）
         *                    - 2: 既存の連結成分の端に接続する（その端点は全体の端点としては固定しない）
         *                    - 3: 既存の連結成分の端に接続し、その端点を全体の端点として固定する
         *                    - 4: 隣接する2つの連結成分の間に要素を挿入し、それらをマージして1つに統合する
         * @param mod 法とする素数
         * @return 条件を満たす重み付き順列の総和
         * @complexity $O(N^2 L)$ 時間, $O(N L)$ 空間
         * // 未テスト
         */
        public static long countPermutationsWithAbsoluteDifferenceSumWeighted(int[] A, int L, long[][] caseWeights, long mod) {
            if (A == null || A.length == 0) {
                return L >= 0 ? 1 : 0;
            }
            int N = A.length;
            if (N == 1) {
                return L >= 0 ? 1 : 0;
            }
            if (L < 0) {
                return 0;
            }

            int[] sorted = A.clone();
            Arrays.sort(sorted);

            long[][][] dp = new long[N + 2][3][L + 1];
            dp[0][0][0] = 1;

            for (int i = 0; i < N; i++) {
                // コスト遷移
                if (i > 0) {
                    int diff = sorted[i] - sorted[i - 1];
                    if (diff > 0) {
                        long[][][] transitioned = new long[N + 2][3][L + 1];
                        for (int j = 0; j <= i; j++) {
                            for (int k = 0; k <= 2; k++) {
                                int gaps = 2 * j - k;
                                if (gaps <= 0) {
                                    for (int c = 0; c <= L; c++) {
                                        transitioned[j][k][c] = (transitioned[j][k][c] + dp[j][k][c]) % mod;
                                    }
                                    continue;
                                }
                                long addCost = (long) gaps * diff;
                                for (int c = 0; c <= L; c++) {
                                    if (dp[j][k][c] == 0) {
                                        continue;
                                    }
                                    if (c + addCost <= L) {
                                        int nc = (int) (c + addCost);
                                        transitioned[j][k][nc] = (transitioned[j][k][nc] + dp[j][k][c]) % mod;
                                    }
                                }
                            }
                        }
                        dp = transitioned;
                    }
                }

                // A[i]の挿入遷移
                long[][][] nextDp = new long[N + 2][3][L + 1];
                for (int j = 0; j <= i; j++) {
                    for (int k = 0; k <= 2; k++) {
                        for (int c = 0; c <= L; c++) {
                            long val = dp[j][k][c];
                            if (val == 0) {
                                continue;
                            }

                            // 各ケースの重みの取得
                            long w0 = (caseWeights != null && caseWeights.length > i) ? caseWeights[i][0] % mod : 1;
                            long w1 = (caseWeights != null && caseWeights.length > i) ? caseWeights[i][1] % mod : 1;
                            long w2 = (caseWeights != null && caseWeights.length > i) ? caseWeights[i][2] % mod : 1;
                            long w3 = (caseWeights != null && caseWeights.length > i) ? caseWeights[i][3] % mod : 1;
                            long w4 = (caseWeights != null && caseWeights.length > i) ? caseWeights[i][4] % mod : 1;

                            if (w0 < 0) w0 += mod;
                            if (w1 < 0) w1 += mod;
                            if (w2 < 0) w2 += mod;
                            if (w3 < 0) w3 += mod;
                            if (w4 < 0) w4 += mod;

                            // 1. 新しい連結成分を作る（端以外）
                            {
                                int nj = j + 1;
                                int nk = k;
                                long ways = j + 1 - k;
                                if (nj <= N && ways > 0) {
                                    long totalWays = ways * w0 % mod;
                                    nextDp[nj][nk][c] = (nextDp[nj][nk][c] + val * totalWays) % mod;
                                }
                            }

                            // 2. 新しい連結成分を作る（端のいずれか）
                            {
                                int nj = j + 1;
                                int nk = k + 1;
                                long ways = 2 - k;
                                if (nj <= N && nk <= 2 && ways > 0) {
                                    long totalWays = ways * w1 % mod;
                                    nextDp[nj][nk][c] = (nextDp[nj][nk][c] + val * totalWays) % mod;
                                }
                            }

                            // 3. 既存の連結成分の端に繋げる（端として固定しない）
                            if (j > 0) {
                                int nj = j;
                                int nk = k;
                                long ways = 2 * j - k;
                                if (nj <= N && ways > 0) {
                                    long totalWays = ways * w2 % mod;
                                    nextDp[nj][nk][c] = (nextDp[nj][nk][c] + val * totalWays) % mod;
                                }
                            }

                            // 4. 既存の連結成分 of 端に繋げ、それを全体の端として固定する
                            if (j > 0) {
                                int nj = j;
                                int nk = k + 1;
                                long ways = 2 - k;
                                if (nj <= N && nk <= 2 && ways > 0) {
                                    long totalWays = ways * w3 % mod;
                                    nextDp[nj][nk][c] = (nextDp[nj][nk][c] + val * totalWays) % mod;
                                }
                            }

                            // 5. 2つの既存の連結成分をマージする
                            if (j >= 2) {
                                int nj = j - 1;
                                int nk = k;
                                long ways = j - 1;
                                if (nj >= 0 && ways > 0) {
                                    long totalWays = ways * w4 % mod;
                                    nextDp[nj][nk][c] = (nextDp[nj][nk][c] + val * totalWays) % mod;
                                }
                            }
                        }
                    }
                }
                dp = nextDp;
            }

            long ans = 0;
            for (int c = 0; c <= L; c++) {
                ans = (ans + dp[1][2][c]) % mod;
            }
            return ans;
        }

        /**
         * 1 から $N$ までの整数の順列において、各要素 $i$ ($2 \le i \le N$) を昇順に挿入するステップ $i$ に応じた重みを適用し、
         * 隣接するどの要素の差も $1$ ではない（$|P_a - P_{a+1}| \neq 1$）順列の総重みを計算します。
         *
         * 状態遷移中、DPの第1次元 $j$ は連結成分の個数を、第2次元 $k$ ($0 \le k \le 2$) は
         * 挿入された最新の要素 $i$ と $i-1$ の隣接状況、およびそれによる「不適切な隣接ペア（差が1）」の新規発生数を表します。
         * 具体的には、値 $i$ を挿入するステップ $i$ において、挿入ケース（新規作成、端への接続、マージ）に応じて、
         * 新しく発生する $i$ と $i-1$ の隣接数 $k \in \{0, 1, 2\}$ に対する乗算重みとして `weights[i][k]` を適用します。
         *
         * @param N 順列の長さ
         * @param weights 挿入ステップおよび新たに発生する連続隣接数 $k$ に対する重み配列。
         *                `weights[i][k]` は、値 $i$ を挿入するステップ $i$ ($2 \le i \le N$) において、
         *                要素 $i$ と $i-1$ が隣接することによって生じる「差が1の隣接ペア」の数（新規発生数 $k \in \{0, 1, 2\}$）に対して
         *                適用される乗算重みを表します。
         *                - `k = 2`: 新たに $i$ が単独の新しい連結成分として挿入され、既存の $i-1$ とは隣接しない遷移。
         *                - `k = 1`: $i$ が既存の連結成分の端のいずれかに接続し、既存の $i-1$ と隣接しない、または接続によってちょうど1つ「差が1」の隣接が発生する遷移。
         *                - `k = 0`: $i$ が2つの連結成分をマージして統合する、または既存の成分の端に接続して $i-1$ とは隣接しない遷移。
         * @param mod 法とする素数
         * @return 条件を満たす重み付き順列の総和を mod で割った余り
         * @complexity $O(N^2)$ 時間, $O(N^2)$ 空間
         * // 未テスト
         */
        public static long countPermutationsWithoutAdjacentConsecutiveValuesWeighted(int N, long[][] weights, long mod) {
            if (N <= 0) return 0;
            if (N == 1) return 1;

            long[][] dp = new long[N + 2][3];
            dp[1][0] = 1;

            for (int i = 2; i <= N; i++) {
                long[][] nextDp = new long[N + 2][3];
                long w0 = (weights != null && weights.length > i) ? weights[i][0] % mod : 1;
                long w1 = (weights != null && weights.length > i) ? weights[i][1] % mod : 1;
                long w2 = (weights != null && weights.length > i) ? weights[i][2] % mod : 1;

                if (w0 < 0) w0 += mod;
                if (w1 < 0) w1 += mod;
                if (w2 < 0) w2 += mod;

                for (int j = 1; j <= i; j++) {
                    // k = 2
                    long val2 = (dp[j - 1][0] + dp[j - 1][1] + dp[j - 1][2]) % mod;
                    nextDp[j][2] = val2 * w2 % mod;

                    // k = 1
                    long val1 = (2 * j * dp[j][0] + (2 * j - 1) * dp[j][1] + (2 * j - 2) * dp[j][2]) % mod;
                    nextDp[j][1] = val1 * w1 % mod;

                    // k = 0
                    long val0 = (j * (j + 1) * dp[j + 1][0] + j * j * dp[j + 1][1] + j * (j - 1) * dp[j + 1][2]) % mod;
                    nextDp[j][0] = val0 * w0 % mod;
                }
                dp = nextDp;
            }
            return dp[1][0];
        }

        /**
         * 1..N の順列を「列（sequence）の集合」として構成する挿入 DP を実行します。
         *
         * <p>
         * 値を小さい順に挿入していき、
         * {@code dp[j]} を「現在 j 本の列に分割されている状態の総重み」とします。
         * 各ステップでは次の値を挿入し、以下の 3 種類の操作を行えます。
         * </p>
         *
         * <table border="1">
         * <tr><th>case</th><th>遷移</th><th>遷移数</th></tr>
         * <tr>
         *   <td>0 (新規列)</td>
         *   <td>{@code j <- j+1}</td>
         *   <td>{@code j+1}通り</td>
         * </tr>
         * <tr>
         *   <td>1 (既存列の端に追加)</td>
         *   <td>{@code j <- j}</td>
         *   <td>{@code 2j}通り</td>
         * </tr>
         * <tr>
         *   <td>2 (2列をマージ)</td>
         *   <td>{@code j <- j-1}</td>
         *   <td>
         *     {@code adjacentMergeOnly ? (j-1) : j(j-1)}通り
         *   </td>
         * </tr>
         * </table>
         *
         * <p>
         * ステップ i (0 ≤ i &lt; N) および現在の列数 j (1 ≤ j ≤ i、または i = 0 のとき j = 0) において、
         * それぞれの操作には {@code weights[i][j][0]}, {@code weights[i][j][1]}, {@code weights[i][j][2]}
         * が乗算されます。
         * </p>
         *
         * @param N 順列の長さ。
         * @param weights 操作ごとの重み。
         *        {@code weights[i][j][0]} は新規列追加、
         *        {@code weights[i][j][1]} は既存列への追加、
         *        {@code weights[i][j][2]} は列のマージの重み。
         *        {@code null} の場合は全て 1 とみなします。
         * @param adjacentMergeOnly
         *        {@code true} の場合は隣接する列のみマージ可能、
         *        {@code false} の場合は順序付きの任意の 2 列をマージ可能。
         * @param mod 法。
         * @return {@code ans[j]} が最終的にちょうど {@code j} 本の列となる構成 of 総重み
         *         (mod {@code mod}) を表す。
         *
         * @complexity O(N^2) time, O(N) memory
         * // 未テスト
         */
        public static long[] solveWeightedSequenceInsertionDP(int N, long[][][] weights, boolean adjacentMergeOnly, long mod) {
            if (N <= 0) {
                return new long[0];
            }
            long[] dp = new long[N + 1];
            dp[0] = 1;

            for (int i = 0; i < N; i++) {//i個決定済み
                long[] nextDp = new long[N + 1];
                for (int j = 0; j <= i; j++) {//j個に分割されている
                    long val = dp[j];
                    if (val == 0) {
                        continue;
                    }

                    // 新しい列として挿入 (j -> j+1)
                    if (j + 1 <= N) {
                        long ways = j + 1;
                        long w0 = getWeight(weights, i, j, 0, mod);
                        nextDp[j + 1] = (nextDp[j + 1] + val * ways % mod * w0) % mod;
                    }

                    // 既存の列の端に挿入 (j -> j)
                    {
                        long ways = 2L * j;
                        long w1 = getWeight(weights, i, j, 1, mod);
                        nextDp[j] = (nextDp[j] + val * ways % mod * w1) % mod;
                    }

                    // 既存の列同士をマージして挿入 (j -> j-1)
                    if (j >= 2) {
                        long ways = adjacentMergeOnly ? (j - 1) : (long) j * (j - 1);
                        long w2 = getWeight(weights, i, j, 2, mod);
                        nextDp[j - 1] = (nextDp[j - 1] + val * ways % mod * w2) % mod;
                    }
                }
                dp = nextDp;
            }
            return dp;
        }

        private static long getWeight(long[][][] weights, int i, int j, int caseId, long mod) {
            if (weights == null || i < 0 || i >= weights.length) {
                return 1L;
            }
            if (weights[i] == null || j < 0 || j >= weights[i].length) {
                return 1L;
            }
            if (weights[i][j] == null || caseId < 0 || caseId >= weights[i][j].length) {
                return 1L;
            }
            long w = weights[i][j][caseId] % mod;
            if (w < 0) {
                w += mod;
            }
            return w;
        }
    }
}
