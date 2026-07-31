package library.util.graph;

public class KValueMinCut {
	//https://atcoder.jp/contests/past23-open/submissions/74927637
    private final MinCut cut;
    private final int k;
    private final int variableCount;
    
    public KValueMinCut(int variableCount, int k) {
        if (variableCount < 0) throw new IllegalArgumentException();
        if (k <= 0) throw new IllegalArgumentException();
        this.cut = new MinCut(variableCount * k);
        this.k = k;
        this.variableCount = variableCount;
        for (int i = 0; i < variableCount; i++) {
            buildMonotone(i);
            fixBoundary(i);
        }
    }

    private int node(int node, int threshold) {
        return node * k + threshold;
    }

    private void buildMonotone(int node) {
        for (int t = 0; t + 1 < k; t++) {
            // [x >= t+1] => [x >= t]
            cut.ifThen(node(node, t + 1), node(node, t));
        }
    }

    private void fixBoundary(int node) {
        // x >= 0 は常に真
        cut.forceTrue(node(node, 0));
    }

    /**
     * 変数 node の値を v に強制する。
     * 計算量: O(1)
     *
     * @param node 変数のインデックス (0-indexed)
     * @param v 強制する値
     */
    // 未テスト
    public void forceValue(int node, int v) {
        if (node < 0 || node >= variableCount) throw new IllegalArgumentException();
        if (v < 0 || v >= k) throw new IllegalArgumentException();
        // [x >= v] = true
        cut.forceTrue(node(node, v));
        // [x >= v+1] = false
        if (v + 1 < k) {
            cut.forceFalse(node(node, v + 1));
        }
    }

    /**
     * 変数 node の値が 1 増えるごとにコスト w を足す。
     * 計算量: O(k)
     *
     * @param node 変数のインデックス (0-indexed)
     * @param w コスト
     */
    // 未テスト
    public void addCostPerUnit(int node, long w) {
        if (node < 0 || node >= variableCount) throw new IllegalArgumentException();
        // x = sum_{t=1}^{k-1} [x >= t]
        // t=0 は常に真なので入れない
        for (int t = 1; t < k; t++) {
            cut.addCostIfTrue(node(node, t), w);
        }
    }

    /**
     * 指定された変数 node が値 j を取るときにコスト cost[j] を追加する。
     * 計算量: O(k)
     *
     * @param node 変数のインデックス (0-indexed)
     * @param cost 各値をとる場合のコスト of 配列
     */
    // 未テスト
    public void addCost(int node, int[] cost) {
        if (node < 0 || node >= variableCount) throw new IllegalArgumentException();
        if (cost == null) throw new NullPointerException();
        if (cost.length < k) throw new IllegalArgumentException();

        long min = cost[0];
        for (int i = 1; i < k; i++) {
            if (cost[i] < min) {
                min = cost[i];
            }
        }

        cut.base += min;
        if (k == 1) return;

        int base = node * k;
        // j = 0: x = 0 <=> s_1 is FALSE (since s_0 is always TRUE)
        cut.addCostIfFalse(base + 1, cost[0] - min);

        // 0 < j < k - 1: x = j <=> s_{j+1} is FALSE and s_j is TRUE
        for (int j = 1; j < k - 1; j++) {
            cut.addPositiveCostIfFalseTrue(base + j + 1, base + j, cost[j] - min);
        }

        // j = k - 1: x = k - 1 <=> s_{k-1} is TRUE (since s_k is always FALSE)
        cut.addCostIfTrue(base + k - 1, cost[k - 1] - min);
    }

    /**
     * x - y <= d を課す。
     * 計算量: O(k)
     *
     * @param x 変数 x のインデックス (0-indexed)
     * @param y 変数 y のインデックス (0-indexed)
     * @param d 許容する最大差
     */
    // 未テスト
    public void forceDifferenceLeq(int x, int y, int d) {
        if (x < 0 || x >= variableCount || y < 0 || y >= variableCount) {
            throw new IllegalArgumentException();
        }
        for (int a = 0; a < k; a++) {
            int b = a - d;
            if (b <= 0) {
                // y >= 0 は常に真なので制約不要
                continue;
            }
            if (b >= k) {
                // x >= a なら不可能
                cut.forceFalse(node(x, a));
            } else {
            	// x>=a => y>=b
                cut.ifThen(node(x, a), node(y, b));
            }
        }
    }
    
    public int[] restoreValues() {
        boolean[] b = cut.restoreMinCut();
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

    public long minCutValue() {
        return cut.minCutValue();
    }

}
