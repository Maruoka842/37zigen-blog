package library.util.geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.internal.series.MarkerSeries;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * 2D幾何オブジェクトを描画するためのクラス。
 */
public class GeometryPlotter {

    private final List<PointData> points = new ArrayList<>();
    private final List<LineData> lines = new ArrayList<>();
    private final List<SegmentData> segments = new ArrayList<>();
    private final List<CircleData> circles = new ArrayList<>();
    private final List<VectorData> vectors = new ArrayList<>();

    private double xMin = Double.NaN;
    private double xMax = Double.NaN;
    private double yMin = Double.NaN;
    private double yMax = Double.NaN;

    private String title = "";
    private String xAxisTitle = "x";
    private String yAxisTitle = "y";
    private boolean equalAspect = true;

    private record PointData(double x, double y) {}
    private record LineData(double a, double b, double c) {}
    private record SegmentData(double x1, double y1, double x2, double y2) {}
    private record CircleData(double x, double y, double r) {}
    private record VectorData(double x, double y, double startX, double startY) {}

    public GeometryPlotter() {}

    public GeometryPlotter add(LongPoint p) { points.add(new PointData(p.x, p.y)); return this; }
    public GeometryPlotter add(DoublePoint p) { points.add(new PointData(p.x, p.y)); return this; }
    public GeometryPlotter add(IntPoint p) { points.add(new PointData(p.x, p.y)); return this; }
    public GeometryPlotter add(FractionPoint p) { points.add(new PointData(p.x().toDouble(), p.y().toDouble())); return this; }
    public GeometryPlotter add(IntFractionPoint p) { points.add(new PointData(p.x().toDouble(), p.y().toDouble())); return this; }

    public GeometryPlotter add(LongLine l) { lines.add(new LineData(l.a, l.b, l.c)); return this; }
    public GeometryPlotter add(IntLine l) { lines.add(new LineData(l.a, l.b, l.c)); return this; }
    public GeometryPlotter add(DoubleLine l) { lines.add(new LineData(l.a, l.b, l.c)); return this; }
    public GeometryPlotter add(LongOrientedLine l) { lines.add(new LineData(l.a, l.b, l.c)); return this; }

    public GeometryPlotter add(LongSegment s) { segments.add(new SegmentData(s.x1, s.y1, s.x2, s.y2)); return this; }
    public GeometryPlotter add(DoubleSegment s) { segments.add(new SegmentData(s.x1, s.y1, s.x2, s.y2)); return this; }

    public GeometryPlotter add(DoubleCircle c) { circles.add(new CircleData(c.center().x, c.center().y, c.radius())); return this; }

    public GeometryPlotter add(LongVector v) { return add(v, 0, 0); }
    public GeometryPlotter add(LongVector v, long startX, long startY) { vectors.add(new VectorData(v.x, v.y, startX, startY)); return this; }
    public GeometryPlotter add(LongVector v, LongPoint start) { return add(v, start.x, start.y); }
    public GeometryPlotter add(DoubleVector v) { return add(v, 0, 0); }
    public GeometryPlotter add(DoubleVector v, double startX, double startY) { vectors.add(new VectorData(v.x, v.y, startX, startY)); return this; }
    public GeometryPlotter add(DoubleVector v, DoublePoint start) { return add(v, start.x, start.y); }

    public GeometryPlotter add(LatticePolytope2D p) {
        for (int i = 0; i < p.vertices.length; i++) {
            LongPoint p1 = p.vertices[i];
            LongPoint p2 = p.vertices[(i + 1) % p.vertices.length];
            segments.add(new SegmentData(p1.x, p1.y, p2.x, p2.y));
        }
        return this;
    }

    /**
     * 実数多面体（凸多角形）をプロットに追加する。
     * <p>計算量: O(N) (Nは頂点数)</p>
     * @param p 実数多面体
     * @return このインスタンス
     */
    // 未テスト
    public GeometryPlotter add(DoublePolytope2D p) {
        for (int i = 0; i < p.vertices.length; i++) {
            DoublePoint p1 = p.vertices[i];
            DoublePoint p2 = p.vertices[(i + 1) % p.vertices.length];
            segments.add(new SegmentData(p1.x(), p1.y(), p2.x(), p2.y()));
        }
        return this;
    }

    public GeometryPlotter setRange(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        return this;
    }

    public GeometryPlotter setTitle(String title) { this.title = title; return this; }
    public GeometryPlotter setXAxisTitle(String title) { this.xAxisTitle = title; return this; }
    public GeometryPlotter setYAxisTitle(String title) { this.yAxisTitle = title; return this; }
    public GeometryPlotter setEqualAspect(boolean equalAspect) { this.equalAspect = equalAspect; return this; }

    public XYChart toChart() {
        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title(title)
                .xAxisTitle(xAxisTitle).yAxisTitle(yAxisTitle)
                .build();

        double curXMin = xMin, curXMax = xMax, curYMin = yMin, curYMax = yMax;
        if (Double.isNaN(curXMin) || Double.isNaN(curXMax) || Double.isNaN(curYMin) || Double.isNaN(curYMax)) {
            double[] autoBounds = calculateAutoBounds();
            if (Double.isNaN(curXMin)) curXMin = autoBounds[0];
            if (Double.isNaN(curXMax)) curXMax = autoBounds[1];
            if (Double.isNaN(curYMin)) curYMin = autoBounds[2];
            if (Double.isNaN(curYMax)) curYMax = autoBounds[3];
        }

        // Add points
        if (!points.isEmpty()) {
            List<Double> px = new ArrayList<>();
            List<Double> py = new ArrayList<>();
            for (PointData p : points) {
                px.add(p.x);
                py.add(p.y);
            }
            chart.addSeries("Points", px, py)
                    .setXYSeriesRenderStyle(org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter)
                    .setMarker(SeriesMarkers.CIRCLE)
                    .setLineStyle(SeriesLines.NONE);
        }

        // Add segments
        if (!segments.isEmpty()) {
            List<Double> sx = new ArrayList<>();
            List<Double> sy = new ArrayList<>();
            for (SegmentData s : segments) {
                sx.add(s.x1); sx.add(s.x2); sx.add(Double.NaN);
                sy.add(s.y1); sy.add(s.y2); sy.add(Double.NaN);
            }
            MarkerSeries series = (MarkerSeries) chart.addSeries("Segments", sx, sy);
            series.setLineStyle(SeriesLines.SOLID);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(Color.RED);
        }

        // Add lines
        for (int i = 0; i < lines.size(); i++) {
            LineData l = lines.get(i);
            if (l.b != 0) {
                double y1 = (-l.a * curXMin - l.c) / l.b;
                double y2 = (-l.a * curXMax - l.c) / l.b;
                MarkerSeries series = (MarkerSeries) chart.addSeries("Line " + i, new double[]{curXMin, curXMax}, new double[]{y1, y2});
                series.setLineStyle(SeriesLines.SOLID);
                series.setMarker(SeriesMarkers.NONE);
            } else if (l.a != 0) {
                double x = -l.c / l.a;
                MarkerSeries series = (MarkerSeries) chart.addSeries("Line " + i, new double[]{x, x}, new double[]{curYMin, curYMax});
                series.setLineStyle(SeriesLines.SOLID);
                series.setMarker(SeriesMarkers.NONE);
                series.setLineColor(Color.GRAY);
            }
        }

        // Add circles
        if (!circles.isEmpty()) {
            List<Double> cx = new ArrayList<>();
            List<Double> cy = new ArrayList<>();
            for (CircleData c : circles) {
                int samples = 100;
                for (int i = 0; i <= samples; i++) {
                    double theta = 2.0 * Math.PI * i / samples;
                    cx.add(c.x + c.r * Math.cos(theta));
                    cy.add(c.y + c.r * Math.sin(theta));
                }
                cx.add(Double.NaN);
                cy.add(Double.NaN);
            }
            MarkerSeries series = (MarkerSeries) chart.addSeries("Circles", cx, cy);
            series.setLineStyle(SeriesLines.SOLID);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(Color.BLUE);
        }

        // Add vectors
        if (!vectors.isEmpty()) {
            List<Double> vx = new ArrayList<>();
            List<Double> vy = new ArrayList<>();
            for (VectorData v : vectors) {
                double endX = v.startX + v.x;
                double endY = v.startY + v.y;
                vx.add(v.startX); vx.add(endX); vx.add(Double.NaN);
                vy.add(v.startY); vy.add(endY); vy.add(Double.NaN);

                // Simple arrow head
                double angle = Math.atan2(v.y, v.x);
                double headLen = Math.sqrt(v.x * v.x + v.y * v.y) * 0.1;
                vx.add(endX); vx.add(endX - headLen * Math.cos(angle - Math.PI/6)); vx.add(Double.NaN);
                vy.add(endY); vy.add(endY - headLen * Math.sin(angle - Math.PI/6)); vy.add(Double.NaN);
                vx.add(endX); vx.add(endX - headLen * Math.cos(angle + Math.PI/6)); vx.add(Double.NaN);
                vy.add(endY); vy.add(endY - headLen * Math.sin(angle + Math.PI/6)); vy.add(Double.NaN);
            }
            MarkerSeries series = (MarkerSeries) chart.addSeries("Vectors", vx, vy);
            series.setLineStyle(SeriesLines.SOLID);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(Color.BLACK);
        }

        if (equalAspect) {
            double xCenter = (curXMin + curXMax) / 2.0;
            double yCenter = (curYMin + curYMax) / 2.0;
            double halfRange = Math.max(curXMax - curXMin, curYMax - curYMin) / 2.0;
            halfRange *= 1.05;
            chart.getStyler().setXAxisMin(xCenter - halfRange);
            chart.getStyler().setXAxisMax(xCenter + halfRange);
            chart.getStyler().setYAxisMin(yCenter - halfRange);
            chart.getStyler().setYAxisMax(yCenter + halfRange);
        } else {
            chart.getStyler().setXAxisMin(curXMin);
            chart.getStyler().setXAxisMax(curXMax);
            chart.getStyler().setYAxisMin(curYMin);
            chart.getStyler().setYAxisMax(curYMax);
        }

        return chart;
    }

    public void draw() {
        XYChart chart = toChart();
        JFrame frame = new JFrame(title.isEmpty() ? "GeometryPlotter" : title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        XChartPanel<XYChart> panel = new XChartPanel<>(chart);
        if (equalAspect) {
            panel.setPreferredSize(new Dimension(600, 600));
        } else {
            panel.setPreferredSize(new Dimension(800, 600));
        }

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private double[] calculateAutoBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (PointData p : points) {
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
        }
        for (SegmentData s : segments) {
            minX = Math.min(minX, Math.min(s.x1, s.x2)); maxX = Math.max(maxX, Math.max(s.x1, s.x2));
            minY = Math.min(minY, Math.min(s.y1, s.y2)); maxY = Math.max(maxY, Math.max(s.y1, s.y2));
        }
        for (CircleData c : circles) {
            minX = Math.min(minX, c.x - c.r); maxX = Math.max(maxX, c.x + c.r);
            minY = Math.min(minY, c.y - c.r); maxY = Math.max(maxY, c.y + c.r);
        }
        for (VectorData v : vectors) {
            minX = Math.min(minX, Math.min(v.startX, v.startX + v.x)); maxX = Math.max(maxX, Math.max(v.startX, v.startX + v.x));
            minY = Math.min(minY, Math.min(v.startY, v.startY + v.y)); maxY = Math.max(maxY, Math.max(v.startY, v.startY + v.y));
        }

        if (Double.isInfinite(minX)) {
            minX = -10; maxX = 10; minY = -10; maxY = 10;
        } else {
            double dx = maxX - minX;
            double dy = maxY - minY;
            if (dx == 0) { minX -= 1; maxX += 1; } else { minX -= dx * 0.1; maxX += dx * 0.1; }
            if (dy == 0) { minY -= 1; maxY += 1; } else { minY -= dy * 0.1; maxY += dy * 0.1; }
        }

        return new double[]{minX, maxX, minY, maxY};
    }
}
