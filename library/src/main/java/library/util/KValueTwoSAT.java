package library.util;

/**
 * 各変数が 0 以上 k 未満の整数値をとるような多値 2-SAT 問題を解くためのデータ構造。
 *
 * 各多値変数 x (0 <= x < variableCount) に対し、k 個の Boolean 変数 [x >= 0], [x >= 1], ..., [x >= k-1] を割り当て、
 * 以下の条件および制約のもとで 2-SAT を構成して解く。
 * - [x >= 0] = true
 * - [x >= t+1] => [x >= t] (0 <= t < k-1)
 *
 * 未テスト
 */
public class KValueTwoSAT {
    /** 2-SATソルバの実体 */
    private final TwoSAT sat;
    /** 変数がとりうる状態数 */
    private final int k;
    /** 変数の総数 */
    private final int variableCount;

    /**
     * KValueTwoSAT を構築する。
     * variableCount個の各変数が 0以上k未満。
     * 計算量: O(variableCount * k)
     *
     * @param variableCount 変数の数
     * @param k 変数全体のとりうる状態数
     * 未テスト
     */
    public KValueTwoSAT(int variableCount, int k) {
        if (variableCount < 0) throw new IllegalArgumentException();
        if (k <= 0) throw new IllegalArgumentException();
        this.sat = new TwoSAT(variableCount * k);
        this.k = k;
        this.variableCount = variableCount;
        for (int i = 0; i < variableCount; i++) {
            buildMonotone(i);
            sat.fixTrue(node(i, 0));
        }
    }

    /**
     * [x >= threshold] に対応する TwoSAT の変数インデックスを取得する。
     *
     * 計算量: O(1)
     *
     * @param x 変数インデックス
     * @param threshold 閾値 (0 <= threshold < k)
     * @return TwoSAT の変数インデックス
     * 未テスト
     */
    public int node(int x, int threshold) {
        if (x < 0 || x >= variableCount) throw new IllegalArgumentException();
        if (threshold < 0 || threshold >= k) throw new IllegalArgumentException();
        return x * k + threshold;
    }

    /**
     * [x >= t+1] => [x >= t] の単調性制約を構築する。
     * 未テスト
     */
    private void buildMonotone(int x) {
        for (int t = 0; t + 1 < k; t++) {
            sat.ifThen(node(x, t + 1), node(x, t));
        }
    }


    /**
     * 変数 x の値を v に固定する。
     *
     * 計算量: O(k)
     *
     * @param x 変数インデックス
     * @param v 固定する値 (0 <= v < k)
     * 未テスト
     */
    public void forceValue(int x, int v) {
        if (v < 0 || v >= k) throw new IllegalArgumentException();
        sat.fixTrue(node(x, v));
        if (v + 1 < k) {
            sat.fixFalse(node(x, v + 1));
        }
    }

    /**
     * [x >= a] => [y >= b] という条件制約を課す。
     *
     * 計算量: O(1)
     *
     * @param x 変数 x
     * @param a 閾値 a
     * @param y 変数 y
     * @param b 閾値 b
     * 未テスト
     */
    public void ifGeThenGe(int x, int a, int y, int b) {
        if (a <= 0) {
            if (b >= k) {
                sat.fixFalse(node(x, 0)); // 満たし得ないため、全体の矛盾を発生させる
            } else if (b > 0) {
                sat.fixTrue(node(y, b));
            }
        } else if (a >= k) {
            // 前件が常に偽なので制約は常に満たされる
        } else {
            if (b <= 0) {
                // 後件が常に真なので制約は常に満たされる
            } else if (b >= k) {
                sat.fixFalse(node(x, a));
            } else {
                sat.ifThen(node(x, a), node(y, b));
            }
        }
    }

    /**
     * [x >= a] => [y < b] という条件制約を課す。
     *
     * 計算量: O(1)
     *
     * @param x 変数 x
     * @param a 閾値 a
     * @param y 変数 y
     * @param b 閾値 b
     * 未テスト
     */
    public void ifGeThenLt(int x, int a, int y, int b) {
        if (a <= 0) {
            if (b <= 0) {
                sat.fixFalse(node(x, 0)); // 満たし得ない
            } else if (b < k) {
                sat.fixFalse(node(y, b));
            }
        } else if (a >= k) {
            // 前件が常に偽
        } else {
            if (b <= 0) {
                sat.fixFalse(node(x, a));
            } else if (b < k) {
                sat.or(~node(x, a), ~node(y, b));
            }
        }
    }

    /**
     * [x < a] => [y >= b] という条件制約を課す。
     *
     * 計算量: O(1)
     *
     * @param x 変数 x
     * @param a 閾値 a
     * @param y 変数 y
     * @param b 閾値 b
     * 未テスト
     */
    public void ifLtThenGe(int x, int a, int y, int b) {
        if (a <= 0) {
            // 前件が常に偽
        } else if (a >= k) {
            if (b >= k) {
                sat.fixFalse(node(x, 0)); // 満たし得ない
            } else if (b > 0) {
                sat.fixTrue(node(y, b));
            }
        } else {
            if (b <= 0) {
                // 後件が常に真
            } else if (b >= k) {
                sat.fixTrue(node(x, a));
            } else {
                sat.or(node(x, a), node(y, b));
            }
        }
    }

    /**
     * [x < a] => [y < b] という条件制約を課す。
     *
     * 計算量: O(1)
     *
     * @param x 変数 x
     * @param a 閾値 a
     * @param y 変数 y
     * @param b 閾値 b
     * 未テスト
     */
    public void ifLtThenLt(int x, int a, int y, int b) {
        ifGeThenGe(y, b, x, a);
    }

    /**
     * x ∈ [l, r] を禁止する、すなわち x < l または x >= r + 1 である制約を課す。
     *
     * 計算量: O(1)
     *
     * @param x 変数 x
     * @param l 区間の下限 l
     * @param r 区間の上限 r
     * 未テスト
     */
    public void forbidInterval(int x, int l, int r) {
    	if (l > r) {
            return;
        }
        if (l <= 0) {
            if (r + 1 >= k) {
                sat.fixFalse(node(x, 0)); // 満たし得ない
            } else if (r + 1 > 0) {
                sat.fixTrue(node(x, r + 1));
            }
        } else {
            if (r + 1 >= k) {
                if (l < k) {
                    sat.fixFalse(node(x, l));
                }
            } else if (r + 1 > 0) {
                sat.or(~node(x, l), node(x, r + 1));
            }
        }
    }

    /**
     * x - y <= d という制約を課す。
     *
     * 計算量: O(k)
     *
     * @param x 変数 x
     * @param y 変数 y
     * @param d 許容する差
     * 未テスト
     */
    public void forceDifferenceLeq(int x, int y, int d) {
        for (int a = 0; a < k; a++) {
            int b = a - d;
            // x >= a なら y >= a-d 
            if (b <= 0) {
                continue;
            }
            if (b >= k) {
                sat.fixFalse(node(x, a));
            } else {
                sat.ifThen(node(x, a), node(y, b));
            }
        }
    }

    /**
     * x + y <= c という制約を課す。
     *
     * 計算量: O(k)
     *
     * @param x 変数 x
     * @param y 変数 y
     * @param c 許容する和の上限
     * 未テスト
     */
    public void forceSumLeq(int x, int y, int c) {
    	//https://atcoder.jp/contests/abc277/submissions/77802341
        for (int a = 0; a < k; a++) {
            int b = c - a + 1;
            //　x >= a and y >= b を禁止
            if (b >= k) {
                continue;
            }
            if (b <= 0) {
            	//y >= b は常に真なので、x >= a を禁止
                sat.fixFalse(node(x, a));
            } else {
            	sat.nand(node(x, a), node(y, b));
            }
        }
    }

    /**
     * x + y >= c という制約を課す。
     *
     * 計算量: O(k)
     *
     * @param x 変数 x
     * @param y 変数 y
     * @param c 許容する和の下限
     * 未テスト
     */
    public void forceSumGeq(int x, int y, int c) {
    	//https://atcoder.jp/contests/abc277/submissions/77802341
        for (int a = 0; a < k; a++) {
        	int b = c - a + 1;
        	// x < a ∧ y < b を禁止
        	// <=> x >= a or y >= b
            if (b <= 0) {
                continue;
            }
            if (b >= k) {
                sat.fixTrue(node(x, a));
            } else {
                sat.or(node(x, a), node(y, b));
            }
        }
        // a >= k のループが抜けてるので補充
        if (c >= k) {
            int req = c - (k - 1);
            if (req >= k) {
            	sat.fixFalse(node(x, 0));
            } else {
                sat.fixTrue(node(y, req));
            }
        }
    }

    /**
     * x = y という等号制約を課す。
     *
     * 計算量: O(k)
     *
     * @param x 変数 x
     * @param y 変数 y
     * 未テスト
     */
    public void forceEq(int x, int y) {
        for (int t = 1; t < k; t++) {
            sat.ifThen(node(x, t), node(y, t));
            sat.ifThen(node(y, t), node(x, t));
        }
    }

    /**
     * 内部で使用している TwoSAT インスタンスを取得する。
     *
     * 計算量: O(1)
     *
     * @return TwoSAT インスタンス
     * 未テスト
     */
    public TwoSAT getTwoSAT() {
        return sat;
    }

    /**
     * 制約を満たす各変数の割り当てを返す。解が存在しない場合は null を返す。
     *
     * 計算量: O(variableCount * k + M) ここで M は 2-SAT に追加された制約(辺)の数
     *
     * @return 各変数の値。解が存在しない場合は null
     * 未テスト
     */
    public int[] solve() {
        boolean[] b = sat.calc();
        if (b == null) return null;
        int[] ret = new int[variableCount];
        for (int i = 0; i < variableCount; i++) {
            int v = 0;
            for (int t = 1; t < k; t++) {
                if (b[i * k + t]) v = t;
                else break;
            }
            ret[i] = v;
        }
        return ret;
    }
}
