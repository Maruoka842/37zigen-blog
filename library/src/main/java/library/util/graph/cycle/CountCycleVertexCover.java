package library.util.graph.cycle;

import library.util.Fp;
import library.util.algebra.strategy.ZnStrategy;
import library.util.linalg.Matrix;

/**
 * サイクルグラフの頂点被覆の数え上げに関する関数群。
 */
public final class CountCycleVertexCover {
    private CountCycleVertexCover() {
    }

    /**
     * 契約:
     * <ul>
     * <li>事前条件: {@code n >= 1} かつ {@code mod >= 1}。</li>
     * <li>事後条件: 返り値はサイクルグラフ $C_n$ の頂点被覆の総数を $mod$ で割った余りである。</li>
     * <li>副作用: なし。</li>
     * <li>計算量: $O(\log n)$。</li>
     * </ul>
     *
     * @param n 頂点数
     * @param mod 法
     * @return 頂点被覆の総数 (mod mod)
     */
    // 未テスト
    public static long count(long n, long mod) {
        if (n == 0) return 1;
        if (n == 1) return 1 % mod;
        if (mod == 1) return 0;

        ZnStrategy strategy = new ZnStrategy(mod);
        Matrix<Long> matrix = new Matrix<>(strategy);

        Long[][] T = {{1L, 1L}, {1L, 0L}};
        Long[][] Tn = matrix.pow(T, n);

        // L_n = Tr(T^n)
        return strategy.add(Tn[0][0], Tn[1][1]);
    }

}
