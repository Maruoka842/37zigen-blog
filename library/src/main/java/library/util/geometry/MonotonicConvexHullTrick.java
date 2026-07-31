package library.util.geometry;

import library.util.collections.ObjectDeque;
import java.util.NoSuchElementException;

/**
 * 傾きが単調増加な直線群を追加し、ある点の $x$ における最小値（min）をクエリする Convex Hull Trick です。
 *
 * <p>以下の操作をサポートします：
 * <ul>
 *   <li>{@code add(a, b)}: 直線 $y = ax + b$ の追加。追加する直線の傾き $a$ は単調増加である必要があります。ならし $O(1)$</li>
 *   <li>{@code query(x)}: ある整数 $x$ における $y = ax + b$ の最小値の取得。$O(\log N)$</li>
 * </ul>
 * </p>
 *
 * <p>クエリ $x$ が整数であることを利用し、凸判定および二分探索を {@link Math#floorDiv(long, long)} による整数除算のみで簡潔かつ安全に実装しています。
 * 座標の値は、内部の比較においてオーバーフローが発生しない範囲（一般に $|a|, |x| \le 10^9$, $|b| \le 10^{18}$ 程度）である必要があります。</p>
 *
 * <p>計算量は $O$ 記法で示されています。</p>
 *
 * @author Jules
 * @see <a href="https://noshi91.hatenablog.com/entry/2021/03/23/200810">クエリが整数の Convex Hull Trick の 凸 判定 (noshi91)</a>
 */
// 未テスト
public class MonotonicConvexHullTrick {

    public static class Line {
        public final long a; // 傾き
        public final long b; // y切片

        public Line(long a, long b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "y = " + a + "x + " + b;
        }

        /**
         * この直線と別のオブジェクトの同値性を判定します。
         *
         * <p>計算量: $O(1)$</p>
         *
         * @param obj 比較対象のオブジェクト
         * @return 同値であれば true, そうでなければ false
         */
        // 未テスト
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Line)) return false;
            Line other = (Line) obj;
            return a == other.a && b == other.b;
        }

        /**
         * この直線のハッシュコードを計算します。
         *
         * <p>計算量: $O(1)$</p>
         *
         * @return ハッシュコード
         */
        // 未テスト
        @Override
        public int hashCode() {
            return java.util.Objects.hash(a, b);
        }
    }

    private final ObjectDeque<Line> lines;

    /**
     * 空の Convex Hull Trick オブジェクトを構築します。
     *
     * <p>計算量: $O(1)$</p>
     */
    public MonotonicConvexHullTrick() {
        this.lines = new ObjectDeque<>();
    }

    /**
     * 直線 $y = ax + b$ を追加します。
     * 追加する直線の傾き $a$ は単調増加である必要があります。
     *
     * <p>計算量: ならし $O(1)$</p>
     *
     * @param a 直線の傾き
     * @param b 直線の y 切片
     * @throws IllegalArgumentException 追加する直線の傾き $a$ が、最後に追加された直線の傾き未満の場合
     */
    public void add(long a, long b) {
        if (!lines.isEmpty() && a < lines.peekFirst().a) {
            throw new IllegalArgumentException("Added slope must be monotonically increasing. Last: " + lines.peekFirst().a + ", New: " + a);
        }
        Line newLine = new Line(a, b);
        while (lines.size() >= 1) {
            Line last = lines.peekFirst();
            if (last.a == a) {
                if (last.b <= b) {
                    // 同一の傾きで、既存の直線の方が y 切片が小さいか等しいため、新しい直線は不要
                    return;
                } else {
                    // 同一の傾きで、新しい直線の方が y 切片が小さいため、古い直線を削除
                    lines.pollFirst();
                }
            } else {
                break;
            }
        }

        while (lines.size() >= 2) {
            Line last1 = lines.get(0);
            Line last2 = lines.get(1);
            // lines.get(0) が last1, lines.get(1) が last2 に対応する。
            // 傾きは a (newLine) > last1.a > last2.a である。
            // クエリが整数の Convex Hull Trick の凸判定（noshi91の記事より）
            // 3つの直線 L0(newLine), L1(last1), L2(last2) において、
            // L0 と L1、L1 と L2 が交わるx座標（の整数部分）を f(L0, L1), f(L1, L2) としたとき、
            // f(L0, L1) >= f(L1, L2) であれば L1(last1) は最小値を取り得ないため冗長であり、削除できる。
            // f(L_i, L_j) = floor((L_j.b - L_i.b) / (L_i.a - L_j.a))
            if (Math.floorDiv(last1.b - b, a - last1.a) >= Math.floorDiv(last2.b - last1.b, last1.a - last2.a)) {
                lines.pollFirst();
            } else {
                break;
            }
        }
        lines.addFirst(newLine);
    }

    /**
     * 指定された整数 $x$ において、これまでに追加されたすべての直線 $y = ax + b$ の最小値（min）を取得します。
     *
     * <p>計算量: $O(\log N)$（$N$ は追加された直線の数）</p>
     *
     * @param x 最小値を求めたい $x$ 座標
     * @return 最小の $y$ 値
     * @throws NoSuchElementException 直線が一つも追加されていない場合
     */
    public long min(long x) {
    	//https://atcoder.jp/contests/abc228/submissions/77548681
        if (lines.isEmpty()) {
            throw new NoSuchElementException("No lines added to Convex Hull Trick.");
        }
        int l = 0, r = lines.size() - 1;
        while (l < r) {
            int mid = (l + r) / 2;
            Line line1 = lines.get(mid);
            Line line2 = lines.get(mid + 1);
            // 傾きは line1.a > line2.a。
            // line1 が line2 より小さくなる（または等しくなる）x の範囲は、
            // line1.a * x + line1.b <= line2.a * x + line2.b
            // <=> (line1.a - line2.a) * x <= line2.b - line1.b
            // <=> x <= (line2.b - line1.b) / (line1.a - line2.a)
            // クエリ x が整数のため、x <= floor((line2.b - line1.b) / (line1.a - line2.a)) と同値。
            if (x <= Math.floorDiv(line2.b - line1.b, line1.a - line2.a)) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        Line opt = lines.get(l);
        return opt.a * x + opt.b;
    }

    /**
     * 現在管理されている直線の数を取得します。
     *
     * <p>計算量: $O(1)$</p>
     *
     * @return 直線の数
     */
    public int size() {
        return lines.size();
    }

    /**
     * 内部状態（直線リスト）を標準出力にダンプします。
     * デバッグ用途で使用します。
     *
     * <p>計算量: $O(N)$</p>
     */
    public void dump() {
        System.out.print("ConvexHullTrick { lines: [");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(lines.get(i));
        }
        System.out.println("] }");
    }

    /**
     * 描画回数を追跡するためのアトミックカウンター。
     */
    private static final java.util.concurrent.atomic.AtomicInteger drawCount = new java.util.concurrent.atomic.AtomicInteger(0);

    /**
     * 現在管理されている直線群と、それらによって構成される下側凸包（Lower Envelope）を可視化するための XYChart オブジェクトを構築します。
     *
     * <p>計算量: $O(N)$</p>
     *
     * @return 描画用の XYChart オブジェクト
     * @throws java.util.NoSuchElementException 直線が一つも追加されていない場合
     */
    // 未テスト
    public org.knowm.xchart.XYChart toChart() {
        if (lines.isEmpty()) {
            throw new java.util.NoSuchElementException("No lines to draw.");
        }

        int N = lines.size();
        double[] intersections = new double[N - 1];
        for (int i = 0; i < N - 1; i++) {
            Line line1 = lines.get(i);
            Line line2 = lines.get(i + 1);
            intersections[i] = (double) (line2.b - line1.b) / (line1.a - line2.a);
        }

        double minX, maxX;
        if (N == 1) {
            minX = -10.0;
            maxX = 10.0;
        } else {
            double low = intersections[0];
            double high = intersections[N - 2];
            if (low == high) {
                minX = low - 10.0;
                maxX = low + 10.0;
            } else {
                double diff = high - low;
                minX = low - diff * 0.2 - 2.0;
                maxX = high + diff * 0.2 + 2.0;
            }
        }

        // 下側凸包の点を計算
        java.util.List<Double> envX = new java.util.ArrayList<>();
        java.util.List<Double> envY = new java.util.ArrayList<>();

        // 最初の点
        envX.add(minX);
        envY.add(lines.get(0).a * minX + lines.get(0).b);

        // 交点
        for (int i = 0; i < N - 1; i++) {
            double x = intersections[i];
            if (x >= minX && x <= maxX) {
                envX.add(x);
                envY.add(lines.get(i).a * x + lines.get(i).b);
            }
        }

        // 最後の点
        envX.add(maxX);
        envY.add(lines.get(N - 1).a * maxX + lines.get(N - 1).b);

        // yの表示範囲を決定
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (double y : envY) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (minY == maxY) {
            minY -= 10.0;
            maxY += 10.0;
        } else {
            double yDiff = maxY - minY;
            minY = minY - yDiff * 0.2 - 1.0;
            maxY = maxY + yDiff * 0.4 + 1.0;
        }

        org.knowm.xchart.XYChart chart = new org.knowm.xchart.XYChartBuilder()
                .width(800)
                .height(600)
                .title("ConvexHullTrick-" + drawCount.incrementAndGet())
                .xAxisTitle("x")
                .yAxisTitle("y")
                .build();

        chart.getStyler().setXAxisMin(minX);
        chart.getStyler().setXAxisMax(maxX);
        chart.getStyler().setYAxisMin(minY);
        chart.getStyler().setYAxisMax(maxY);

        // 個々の直線を描画
        for (int i = 0; i < N; i++) {
            Line line = lines.get(i);
            java.util.List<Double> lx = java.util.List.of(minX, maxX);
            java.util.List<Double> ly = java.util.List.of(line.a * minX + line.b, line.a * maxX + line.b);
            org.knowm.xchart.XYSeries series = chart.addSeries("Line " + i + ": " + line, lx, ly);
            series.setMarker(org.knowm.xchart.style.markers.SeriesMarkers.NONE);
            series.setLineStyle(org.knowm.xchart.style.lines.SeriesLines.DASH_DASH);
            series.setLineColor(java.awt.Color.LIGHT_GRAY);
        }

        // 下側凸包（最小値の境界）を太線で描画
        org.knowm.xchart.XYSeries envSeries = chart.addSeries("Lower Envelope", envX, envY);
        envSeries.setMarker(org.knowm.xchart.style.markers.SeriesMarkers.CIRCLE);
        envSeries.setMarkerColor(java.awt.Color.RED);
        envSeries.setLineStyle(org.knowm.xchart.style.lines.SeriesLines.SOLID);
        envSeries.setLineColor(java.awt.Color.RED);

        return chart;
    }

    /**
     * 管理されている直線群と下側凸包（Lower Envelope）をグラフ描画して表示します。
     *
     * <p>計算量: $O(N)$</p>
     *
     * @throws java.util.NoSuchElementException 直線が一つも追加されていない場合
     */
    // 未テスト
    public void draw() {
        new org.knowm.xchart.SwingWrapper<>(toChart()).displayChart();
    }

	/**
	 * この Convex Hull Trick と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は直線の数です。</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MonotonicConvexHullTrick)) return false;
		MonotonicConvexHullTrick other = (MonotonicConvexHullTrick) obj;
		return lines.equals(other.lines);
	}

	/**
	 * この Convex Hull Trick のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(N)$、ここで $N$ は直線の数です。</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(lines);
	}
}
