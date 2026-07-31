package library.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * Slope trick: 凸折れ線関数に対する高速な操作を行うライブラリ。
 *
 * 以下の形の凸関数 f(x) を管理する:
 * f(x) = min_f + Σ max(0, a_i - x) + Σ max(0, x - b_i)
 * ここで a_i は左側の傾きの変化点 (L)、b_i は右側の傾きの変化点 (R) である。
 *
 * 実装の参考:
 * - https://maspypy.com/slope-trick-1-%E8%A7%A3%E8%AA%AC%E7%B7%A8
 * - https://ei1333.github.io/library/structure/others/slope-trick.cpp
 */
public class SlopeTrick {
    public static final long INF = Long.MAX_VALUE / 3;

    private long minF;
    private long displacementL, displacementR;
    private PriorityQueue<Long> L; // max-heap
    private PriorityQueue<Long> R; // min-heap

    public SlopeTrick() {
        this.minF = 0;
        this.displacementL = 0;
        this.displacementR = 0;
        this.L = new PriorityQueue<>(Collections.reverseOrder());
        this.R = new PriorityQueue<>();
    }

    private void pushR(long a) {
        R.add(a - displacementR);
    }

    private long topR() {
        return R.isEmpty() ? INF : R.peek() + displacementR;
    }

    private long popR() {
        long ret = topR();
        if (!R.isEmpty()) R.poll();
        return ret;
    }

    private void pushL(long a) {
        L.add(a + displacementL);
    }

    private long topL() {
        return L.isEmpty() ? -INF : L.peek() - displacementL;
    }

    private long popL() {
        long ret = topL();
        if (!L.isEmpty()) L.poll();
        return ret;
    }

    public int sizeL() {
        return L.size();
    }

    public int sizeR() {
        return R.size();
    }

    /**
     * argmin f(x), min f(x) を取得する。
     * [lo, hi] が f(x) が最小値をとる区間である。
     */
    public record QueryResult(long min, long lo, long hi) {}

    public QueryResult getMin() {
        return new QueryResult(minF, topL(), topR());
    }

    /**
     * f(x) += b
     */
    public SlopeTrick addConst(long b) {
        minF += b;
        return this;
    }

    /**
     * f(x) += max(x - a, 0)  _/
     */
    public SlopeTrick addRelu(long a) {
        minF += Math.max(0L, topL() - a);
        pushL(a);
        pushR(popL());
        return this;
    }

    /**
     * f(x) += max(a - x, 0)  \_
     */
    public SlopeTrick addIrelu(long a) {
        minF += Math.max(0L, a - topR());
        pushR(a);
        pushL(popR());
        return this;
    }

    /**
     * f(x) += |x - a|  \/
     */
    public SlopeTrick addAbs(long a) {
        return addRelu(a).addIrelu(a);
    }

    /**
     * f(x) <- min_{0 <= y <= w} f(x + y)  .\ -> \_
     * 累積最小値（スライド最小値）のような操作。関数の左側が w だけ左に伸びる。
     */
    public SlopeTrick moveLeftCurve(long w) {
        if (w < 0) throw new IllegalArgumentException("w must be non-negative");
        displacementL += w;
        return this;
    }

    /**
     * f(x) <- min_{0 <= y} f(x + y)
     * 関数の左側をすべて捨てる。
     */
    public SlopeTrick moveLeftCurve() {
        L.clear();
        displacementL = 0;
        return this;
    }

    /**
     * f(x) <- min_{0 <= y <= w} f(x - y)  /. -> _/
     * 関数の右側が w だけ右に伸びる。
     */
    public SlopeTrick moveRightCurve(long w) {
        if (w < 0) throw new IllegalArgumentException("w must be non-negative");
        displacementR += w;
        return this;
    }

    /**
     * f(x) <- min_{0 <= y} f(x - y)
     * 関数の右側をすべて捨てる。
     */
    public SlopeTrick moveRightCurve() {
        R.clear();
        displacementR = 0;
        return this;
    }

    /**
     * f(x) <- f(x - dx) \/. -> .\/
     * 関数全体を dx だけ右に平行移動する。
     */
    public SlopeTrick translate(long dx) {
        displacementL -= dx;
        displacementR += dx;
        return this;
    }

    /**
     * f(x) の値を求める。
     */
    public long getAt(long x) {
        long ret = minF;
        for (long val : L) ret += Math.max(0L, (val - displacementL) - x);
        for (long val : R) ret += Math.max(0L, x - (val + displacementR));
        return ret;
    }

    /**
     * f(x) の値を求める。呼び出し後、インスタンスの状態は破壊される。
     */
    public long getDestructive(long x) {
        long ret = getMin().min;
        while (!L.isEmpty()) ret += Math.max(0L, popL() - x);
        while (!R.isEmpty()) ret += Math.max(0L, x - popR());
        return ret;
    }

    /**
     * f(x) += g(x) を行う。g は破壊される。
     */
    public SlopeTrick mergeDestructive(SlopeTrick g) {
        if (sizeL() + sizeR() < g.sizeL() + g.sizeR()) {
            // Swap fields
            long tmpMinF = minF; minF = g.minF; g.minF = tmpMinF;
            long tmpDL = displacementL; displacementL = g.displacementL; g.displacementL = tmpDL;
            long tmpDR = displacementR; displacementR = g.displacementR; g.displacementR = tmpDR;
            PriorityQueue<Long> tmpL = L; L = g.L; g.L = tmpL;
            PriorityQueue<Long> tmpR = R; R = g.R; g.R = tmpR;
        }
        minF += g.getMin().min;
        while (!g.L.isEmpty()) addIrelu(g.popL());
        while (!g.R.isEmpty()) addRelu(g.popR());
        return this;
    }

    private static final AtomicInteger drawCount = new AtomicInteger(0);

    /**
     * 指定された範囲 [l, r) の外にある変化点を捨てる。
     * @param l 範囲の下限
     * @param r 範囲の上限
     * @return this
     */
    public SlopeTrick discardOutside(long l, long r) {
        L.removeIf(val -> {
            long cp = val - displacementL;
            return cp < l || cp >= r;
        });
        R.removeIf(val -> {
            long cp = val + displacementR;
            return cp < l || cp >= r;
        });
        return this;
    }

    private XYChart toChart() {
        List<Long> changePoints = new ArrayList<>();
        for (long val : L) changePoints.add(val - displacementL);
        for (long val : R) changePoints.add(val + displacementR);
        Collections.sort(changePoints);

        long minX, maxX;
        if (changePoints.isEmpty()) {
            minX = -10;
            maxX = 10;
        } else {
            long low = changePoints.get(0);
            long high = changePoints.get(changePoints.size() - 1);
            if (low == high) {
                minX = low - 10;
                maxX = low + 10;
            } else {
                long diff = high - low;
                minX = low - diff / 10 - 1;
                maxX = high + diff / 10 + 1;
            }
        }

        List<Long> xData = new ArrayList<>();
        xData.add(minX);
        for (long cp : changePoints) {
            if (cp > minX && cp < maxX) {
                xData.add(cp);
            }
        }
        xData.add(maxX);

        List<Double> distinctX = xData.stream().distinct().sorted().map(Long::doubleValue).collect(Collectors.toList());
        List<Double> distinctY = distinctX.stream().map(x -> (double) getAt(Math.round(x))).collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("SlopeTrick-" + drawCount.incrementAndGet())
                .xAxisTitle("x")
                .yAxisTitle("f(x)")
                .build();

        chart.addSeries("f(x)", distinctX, distinctY).setMarker(SeriesMarkers.CIRCLE);

        return chart;
    }

    public void draw() {
        new SwingWrapper<>(toChart()).displayChart();
    }
}