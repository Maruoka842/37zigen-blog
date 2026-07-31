package library.util;

/**
 * Dual Slope Trick: 凸共役を利用した凸関数の操作。
 *
 * 凸関数 f(x) に対し、その凸共役 f*(p) = sup_x (px - f(x)) を SlopeTrick で管理する。
 * f(x) は、傾きが整数の範囲で変化し、変化点の座標も整数であるような区分線形凸関数である。
 *
 * 以下の操作をサポートする:
 * - f(0) の取得: O(1)
 * - 定数の加算 f(x) += b: O(1)
 * - 平行移動 f(x) <- f(x - d): O(|d| log N)
 * - 線形関数の加算 f(x) += ax + b: O(log N)
 * - max(c(x - a), 0) の加算: O(log N + |a| log N)
 * - スライド最小値 f(x) <- min_{a <= d <= b} f(x - d): O((|a| + |b-a|) log N)
 * - 最小値 min f(x) の取得: O(N)
 *
 * 実装の参考:
 * - https://maspypy.com/slope-trick-3-slope-trick-%E3%81%AE%E5%87%B8%E5%85%B1%E5%BD%B9
 *
 * 未テスト
 */
public class DualSlopeTrick {
    private final SlopeTrick st;

    /**
     * f(x) = 0 (x = 0), infinity (x != 0) で初期化する。
     */
    public DualSlopeTrick() {
        this.st = new SlopeTrick();
    }

    /**
     * f(0) を取得する。
     * @return f(0)
     */
    public long getAtZero() {
        return -st.getMin().min();
    }

    /**
     * f(x) += b を行う。
     * @param b 加算する定数
     * @return this
     */
    public DualSlopeTrick addConst(long b) {
        st.addConst(-b);
        return this;
    }

    /**
     * f(x) <- f(x - d) を行う。
     * @param d 移動量
     * @return this
     */
    public DualSlopeTrick shift(long d) {
        long C = 1_000_000_000_000_000L;
        while (d > 0) {
            st.addRelu(-C).addConst(-C);
            d--;
        }
        while (d < 0) {
            st.addIrelu(C).addConst(-C);
            d++;
        }
        return this;
    }

    /**
     * f(x) += ax + b を行う。
     * @param a 傾き
     * @param b 切片
     * @return this
     */
    public DualSlopeTrick addLinear(long a, long b) {
        st.translate(a).addConst(-b);
        return this;
    }

    /**
     * f(x) += max(c(x - a), 0) を行う。
     * @param c 傾き
     * @param a 変化点の座標
     * @return this
     */
    public DualSlopeTrick addLinearOrZero(long c, long a) {
        shift(-a);
        if (c > 0) {
            st.moveRightCurve(c);
        } else if (c < 0) {
            st.moveLeftCurve(-c);
        }
        return shift(a);
    }

    /**
     * f(x) <- min_{a <= d <= b} f(x - d) を行う。
     * @param a 範囲の下限
     * @param b 範囲の上限
     * @return this
     */
    public DualSlopeTrick slideMin(long a, long b) {
        if (a > b) throw new IllegalArgumentException("a must be <= b");
        shift(a);
        long diff = b - a;
        for (long t = 0; t < diff; t++) {
            st.addRelu(0);
        }
        return this;
    }

    /**
     * f(x) の最小値を取得する。
     * @return min f(x)
     */
    public long getMin() {
        return -st.getAt(0);
    }
}
