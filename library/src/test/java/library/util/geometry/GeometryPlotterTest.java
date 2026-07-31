package library.util.geometry;

import org.junit.jupiter.api.Test;
import org.knowm.xchart.XYChart;
import static org.junit.jupiter.api.Assertions.*;

public class GeometryPlotterTest {

    @Test
    public void testAddDoublePolytope2D() {
        DoublePoint[] vertices = {
            new DoublePoint(0.0, 0.0),
            new DoublePoint(2.0, 0.0),
            new DoublePoint(0.0, 3.0)
        };
        DoublePolytope2D poly = new DoublePolytope2D(vertices);

        GeometryPlotter plotter = new GeometryPlotter();
        plotter.add(poly);

        XYChart chart = plotter.toChart();
        assertNotNull(chart);
        assertTrue(chart.getSeriesMap().containsKey("Segments"));

        var series = chart.getSeriesMap().get("Segments");
        double[] xData = series.getXData();
        double[] yData = series.getYData();

        // 3 segments, each having 2 endpoints and a NaN separator -> total 9 values
        assertEquals(9, xData.length);
        assertEquals(9, yData.length);

        // Verify some segment coordinates
        assertEquals(0.0, xData[0]);
        assertEquals(2.0, xData[1]);
        assertTrue(Double.isNaN(xData[2]));
    }
}
