package library.util.polynomial;

import library.util.Fp;

/**
 * ラベルなし木の個数を数え上げるユーティリティクラス。
 */
public class UnlabeledTreeCounter {
    private static final long MOD = 998244353L;
    private static final Fp fp = Fp.MOD998244353;

    private UnlabeledTreeCounter() {}

    /**
     * ラベルなし根付き木の個数を求める。
     * <p>
     * 頂点数 1 から {@code N} までのラベルなし根付き木の個数（OEIS A000081）を計算し、
     * 長さ {@code N + 1} の配列として返す。
     * 0番目の要素は0、{@code i} 番目の要素（{@code 1 <= i <= N}）は頂点数 {@code i} の根付き木の個数。
     *
     * @param N 最大の頂点数
     * @return インデックスが頂点数に対応する個数の配列
     * @complexity O(N^2)
     */
    public static long[] countRooted(int N) {
        if (N < 0) return new long[0];
        long[] f = new long[N + 1];
        if (N == 0) return f;
        f[1] = 1;
        long[] s = new long[N + 1];
        // s[k] = \sum_{d|k} d * f[d]
        // Initially, we know f[1] = 1, so s[k] += 1 * f[1] for all k >= 1
        for (int k = 1; k <= N; k++) {
            s[k] = (s[k] + f[1]) % MOD;
        }

        for (int i = 1; i < N; i++) {
            // i 頂点の情報まで使って f[i+1] を求める
            // (i) * f[i+1] = \sum_{j=1}^i s[j] * f[i-j+1]
            long sum = 0;
            for (int j = 1; j <= i; j++) {
                sum = (sum + s[j] * f[i - j + 1]) % MOD;
            }
            f[i + 1] = sum * fp.inv(i) % MOD;

            // f[i+1] が決まったので、s を更新する
            long val = (long) (i + 1) * f[i + 1] % MOD;
            for (int k = i + 1; k <= N; k += i + 1) {
                s[k] = (s[k] + val) % MOD;
            }
        }
        return f;
    }

    /**
     * ラベルなし（根なし）木の個数を求める。
     * <p>
     * 頂点数 1 から {@code N} までのラベルなし木の個数（OEIS A000055）を計算し、
     * 長さ {@code N + 1} の配列として返す。
     * 0番目の要素は0、{@code i} 番目の要素（{@code 1 <= i <= N}）は頂点数 {@code i} の木の個数。
     *
     * @param N 最大の頂点数
     * @return インデックスが頂点数に対応する個数の配列
     * @complexity O(N^2)
     */
    public static long[] countUnrooted(int N) {
        if (N < 0) return new long[0];
        if (N == 0) return new long[] {0};
        long[] f = countRooted(N);
        long[] f2 = PolynomialFp.mul(f, f);
        long[] res = new long[N + 1];
        long inv2 = fp.inv(2);
        for (int i = 1; i <= N; i++) {
            long val = f[i];
            // Otter's formula: T(x) = f(x) - 1/2(f(x)^2 - f(x^2))
            long f_sq = (i < f2.length) ? f2[i] : 0;
            long f_x2 = (i % 2 == 0) ? f[i / 2] : 0;
            long sub = (f_sq - f_x2 + MOD) % MOD * inv2 % MOD;
            res[i] = (val - sub + MOD) % MOD;
        }
        return res;
    }
}
