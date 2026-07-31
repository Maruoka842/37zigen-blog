package library.util.graph.grid;

import library.util.MathUtils;
import library.util.algebra.strategy.PolynomialQuotientRingFpStrategy;
import library.util.linalg.Matrix;
import library.util.polynomial.PolynomialFpDynamic;

/**
 * 長方形 (N x M) 盤面を (2x1), (1x2) ドミノで敷き詰める方法の数を mod で求める。
 * Kasteleyn / Temperley–Fisher の積公式を Chebyshev 多項式と resultant に落とす方法で実装。
 */
public final class DominoTilingRectangle {

    /**
     * N x M の長方形をドミノで敷き詰める方法の数を求める。
     * 計算量: O(r^2 log M + r^3) where r = floor(min(N, M) / 2)
     *
     * @param N   盤面の行数
     * @param M   盤面の列数
     * @param mod 法（素数）
     * @return 敷き詰め方法の数 (mod mod)
     */
    public static long count(long N, long M, long mod) {
        if ((N & 1) == 1 && (M & 1) == 1) {
            return 0;
        }
        if (N > M) {
            long tmp = N;
            N = M;
            M = tmp;
        }
        // N <= M, and NM is even
        if (N == 0) return 1;
        if (N == 1) {
            return (M & 1) == 0 ? 1 : 0;
        }

        PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
        int r = (int) (N / 2);

        // P_N(x) monic of degree r
        long[] pN = getPN(N, mod);

        // C_s(x) mod P_N(x)
        long[] csMod = getCsMod(M, pN, poly);

        // Resultant(P_N, C_s)
        return poly.resultant(pN, csMod);
    }

    /**
     * P_N(x) = \prod_{j=1}^{r} (x - 4 \cos^2 \frac{j \pi}{N+1}) を求める。
     * O(r) の二項係数公式と漸化式を用いる。
     * a_k = (-1)^{r-k} * comb(r + k + delta, 2*k + delta)
     * 計算量: O(r)
     *
     * @param N   盤面のサイズ（小さい方）
     * @param mod 法
     * @return P_N(x) の係数配列 (底から順に a_0, a_1, ..., a_r)
     */
    private static long[] getPN(long N, long mod) {
        int r = (int) (N / 2);
        int delta = (int) (N % 2);
        long[] res = new long[r + 1];
        if (r == 0) {
            res[0] = 1 % mod;
            return res;
        }
        // a_0 = (-1)^r * comb(r + delta, delta)
        long val = 1;
        if (delta == 1) {
            val = (r + 1) % mod;
        }
        if (r % 2 == 1) {
            res[0] = (mod - val) % mod;
        } else {
            res[0] = val;
        }

        // a_{k+1} = - a_k * (r+k+1+delta)(r-k) / ((2k+2+delta)(2k+1+delta))
        for (int k = 0; k < r; k++) {
            long num = (long) (r + k + 1 + delta) * (r - k) % mod;
            long den = (long) (2 * k + 2 + delta) * (2 * k + 1 + delta) % mod;
            res[k + 1] = (mod - res[k]) % mod * num % mod * MathUtils.modInv(den, mod) % mod;
        }
        return res;
    }

    /**
     * C_s(x) = \prod_{k=1}^{s} (x + 4 \cos^2 \frac{k \pi}{M+1}) mod P_N(x) を求める。
     * 計算量: O(r^2 log M)
     *
     * @param M   盤面のサイズ（大きい方）
     * @param pN  法多項式 P_N(x)
     * @param poly 多項式演算器
     * @return C_s(x) mod P_N(x)
     */
    private static long[] getCsMod(long M, long[] pN, PolynomialFpDynamic poly) {
        long s = M / 2;
        if (s == 0) return poly.one();

        // Matrix power over quotient ring
        return getCsModViaMatrix(s, M % 2 != 0, pN, poly);
    }

    /**
     * 行列累乗を用いて C_s(x) mod P_N(x) を計算する。
     * 計算量: O(r^2 log s)
     */
    private static long[] getCsModViaMatrix(long s, boolean isMOdd, long[] pN, PolynomialFpDynamic poly) {
        PolynomialQuotientRingFpStrategy strategy = new PolynomialQuotientRingFpStrategy(poly, pN);
        Matrix<long[]> matrixUtils = new Matrix<>(strategy);

        // T = [[x+2, -1], [1, 0]]
        long[] polyXplus2 = strategy.mod(new long[]{2 % poly.mod, 1});
        long[] polyMinus1 = strategy.mod(new long[]{(poly.mod - 1) % poly.mod});
        long[] poly1 = strategy.one();
        long[] poly0 = strategy.zero();

        long[][][] base = {{polyXplus2, polyMinus1}, {poly1, poly0}};
        long[][][] res = matrixUtils.pow(base, s - 1);

        long[] c0 = poly1;
        long[] c1;
        if (!isMOdd) {
            // M even: C1 = x + 1
            c1 = strategy.mod(new long[]{1 % poly.mod, 1});
        } else {
            // M odd: C1 = x + 2
            c1 = strategy.mod(new long[]{2 % poly.mod, 1});
        }

        // [Cs, Cs-1]^T = res * [C1, C0]^T
        // Cs = res[0][0] * C1 + res[0][1] * C0
        return strategy.add(strategy.mul(res[0][0], c1), strategy.mul(res[0][1], c0));
    }
}
