package library.util;

import org.junit.jupiter.api.Test;

import library.util.graph.grid.ColorfulPanelVisualizer;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

public class ColorfulPanelVisualizerTest {

    @SuppressWarnings("unchecked")
    private List<int[][]> getHistory(ColorfulPanelVisualizer vis) throws Exception {
        Field history = ColorfulPanelVisualizer.class.getDeclaredField("history");
        history.setAccessible(true);
        return (List<int[][]>) history.get(vis);
    }

    @Test
    public void testSample() {
        int N = 6;
        int K = 9;
        String[] s = {
            "515795",
            "153859",
            "833597",
            "333419",
            "333121",
            "533917"
        };
        int[][] grid = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                grid[i][j] = s[i].charAt(j) - '0';
            }
        }

        ColorfulPanelVisualizer vis = new ColorfulPanelVisualizer(N, K, grid);

        // Touch (5, 5) -> Color 1 (0-indexed: (4, 4) -> 1)
        vis.paintConnectedComponent(4, 4, 1);
        // Touch (5, 2) -> Color 1 (0-indexed: (4, 1) -> 1)
        vis.paintConnectedComponent(4, 1, 1);

        // To actually see it, you would call vis.draw(),
        // but we don't do that in automated tests.
    }

    @Test
    public void testPaintRectangle() throws Exception {
        int[][] grid = {
            {1, 1, 1, 1},
            {1, 2, 2, 1},
            {1, 2, 2, 1},
            {1, 1, 1, 1}
        };

        ColorfulPanelVisualizer vis = new ColorfulPanelVisualizer(4, 9, grid);
        vis.paintRectangle(1, 1, 3, 4, 5);

        List<int[][]> history = getHistory(vis);
        assertEquals(2, history.size());
        assertArrayEquals(new int[][]{
            {1, 1, 1, 1},
            {1, 5, 5, 5},
            {1, 5, 5, 5},
            {1, 1, 1, 1}
        }, history.get(1));
        assertArrayEquals(new int[][]{
            {1, 1, 1, 1},
            {1, 2, 2, 1},
            {1, 2, 2, 1},
            {1, 1, 1, 1}
        }, grid);
    }

}
