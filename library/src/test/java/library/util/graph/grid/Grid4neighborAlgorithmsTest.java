package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class Grid4neighborAlgorithmsTest {

    @Test
    public void testManualSmallGridBFS() {
        int R = 2;
        int C = 3;
        boolean[][] V = new boolean[R][C - 1];
        boolean[][] H = new boolean[R - 1][C];

        V[0][0] = true; // wall between (0,0) and (0,1)
        V[1][1] = true; // wall between (1,1) and (1,2)
        H[0][1] = true; // wall between (0,1) and (1,1)

        int[][] dist = Grid4neighborAlgorithms.bfs(R, C, V, H, 0, 0);

        assertEquals(0, dist[0][0]);
        assertEquals(1, dist[1][0]);
        assertEquals(2, dist[1][1]);
        assertEquals(Integer.MAX_VALUE, dist[0][1]);
        assertEquals(Integer.MAX_VALUE, dist[0][2]);
        assertEquals(Integer.MAX_VALUE, dist[1][2]);
    }

    @Test
    public void testManualSmallGridIcefloor() {
        int R = 2;
        int C = 3;
        boolean[][] V = new boolean[R][C - 1];
        boolean[][] H = new boolean[R - 1][C];

        V[0][0] = true;
        V[1][1] = true;
        H[0][1] = true;

        int[][][] dist = Grid4neighborAlgorithms.bfsIcefloor(R, C, V, H, 0, 0);
        assertNotNull(dist);
        assertEquals(4, dist.length);
        assertEquals(R, dist[0].length);
        assertEquals(C, dist[0][0].length);
    }

    @Test
    public void testEquivalenceWithGrid2D() {
        Random rnd = new Random(12345);
        int R = 15;
        int C = 15;

        for (int t = 0; t < 10; t++) {
            char[][] map = new char[R][C];
            boolean[][] isObstacle = new boolean[R][C];

            for (int i = 0; i < R; i++) {
                for (int j = 0; j < C; j++) {
                    if (rnd.nextDouble() < 0.25) {
                        map[i][j] = '#';
                        isObstacle[i][j] = true;
                    } else {
                        map[i][j] = '.';
                        isObstacle[i][j] = false;
                    }
                }
            }

            int si = rnd.nextInt(R);
            int sj = rnd.nextInt(C);
            map[si][sj] = '.';
            isObstacle[si][sj] = false;

            Grid2D g2d = new Grid2D(map);
            int[][] d2d = g2d.bfs(si, sj);
            int[][][] d2dIce = g2d.bfs_icefloor(si, sj);

            int[][] d4n = Grid4neighborAlgorithms.bfs(R, C, null, null, isObstacle, si, sj);
            int[][][] d4nIce = Grid4neighborAlgorithms.bfsIcefloor(R, C, null, null, isObstacle, si, sj);

            for (int i = 0; i < R; i++) {
                for (int j = 0; j < C; j++) {
                    assertEquals(d2d[i][j], d4n[i][j]);
                }
            }

            for (int dir = 0; dir < 4; dir++) {
                for (int i = 0; i < R; i++) {
                    for (int j = 0; j < C; j++) {
                        assertEquals(d2dIce[dir][i][j], d4nIce[dir][i][j]);
                    }
                }
            }
        }
    }

    @Test
    public void testFindPathStandard() {
        int R = 3;
        int C = 3;
        boolean[][] V = new boolean[R][C - 1];
        boolean[][] H = new boolean[R - 1][C];

        H[0][1] = true;
        H[1][1] = true;
        V[1][0] = true;

        int[][] path = Grid4neighborAlgorithms.findPath(R, C, V, H, 0, 1, 2, 1);
        assertNotNull(path);

        assertEquals(5, path.length);
        assertArrayEquals(new int[]{0, 1}, path[0]);
        assertArrayEquals(new int[]{0, 2}, path[1]);
        assertArrayEquals(new int[]{1, 2}, path[2]);
        assertArrayEquals(new int[]{2, 2}, path[3]);
        assertArrayEquals(new int[]{2, 1}, path[4]);

        V[0][0] = true;
        H[0][0] = true;
        int[][] unreached = Grid4neighborAlgorithms.findPath(R, C, V, H, 0, 0, 2, 2);
        assertNull(unreached);

        int[][] trivialPath = Grid4neighborAlgorithms.findPath(R, C, V, H, 1, 1, 1, 1);
        assertNotNull(trivialPath);
        assertEquals(1, trivialPath.length);
        assertArrayEquals(new int[]{1, 1}, trivialPath[0]);
    }

    @Test
    public void testFindPathIcefloor() {
        int R = 3;
        int C = 3;
        boolean[][] V = new boolean[R][C - 1];
        boolean[][] H = new boolean[R - 1][C];

        int[][] pathRight = Grid4neighborAlgorithms.findPathIcefloor(R, C, V, H, 0, 0, 0, 2);
        assertNotNull(pathRight);
        assertEquals(3, pathRight.length);
        assertArrayEquals(new int[]{0, 0}, pathRight[0]);
        assertArrayEquals(new int[]{0, 1}, pathRight[1]);
        assertArrayEquals(new int[]{0, 2}, pathRight[2]);

        V[0][1] = true;
        int[][] pathWithWall = Grid4neighborAlgorithms.findPathIcefloor(R, C, V, H, 0, 0, 0, 1);
        assertNotNull(pathWithWall);
        assertEquals(2, pathWithWall.length);
        assertArrayEquals(new int[]{0, 0}, pathWithWall[0]);
        assertArrayEquals(new int[]{0, 1}, pathWithWall[1]);

        V[0][0] = true;
        H[0][0] = true;
        int[][] unreachable = Grid4neighborAlgorithms.findPathIcefloor(R, C, V, H, 0, 0, 2, 2);
        assertNull(unreachable);
    }

    @Test
    public void testIcefloorWithStopping() {
        int R = 3;
        int C = 3;
        boolean[][] V = new boolean[R][C - 1];
        boolean[][] H = new boolean[R - 1][C];

        // Let's test standard findPathIcefloor vs findPathIcefloorWithStopping.
        // Under standard sliding, if we start at (0,0) and slide right, we MUST slide all the way to (0,2).
        // If the target is (1,1), under standard sliding, we cannot reach it directly because sliding right goes to (0,2),
        // sliding down from (0,2) goes to (2,2), sliding left from (2,2) goes to (2,0), etc.
        // But with stopping allowed:
        // We can slide right from (0,0), stop at (0,1), then turn down and slide to (1,1)!

        int[][] stdPath = Grid4neighborAlgorithms.findPathIcefloor(R, C, V, H, 0, 0, 1, 1);
        // Under standard rules, (1,1) is unreachable from (0,0) on a 3x3 empty grid!
        assertNull(stdPath);

        // With stopping allowed:
        int[][] stopPath = Grid4neighborAlgorithms.findPathIcefloorWithStopping(R, C, V, H, 0, 0, 1, 1);
        assertNotNull(stopPath);

        // Expected path is either:
        // Option 1: (0,0) -> (0,1) -> (1,1)
        // Option 2: (0,0) -> (1,0) -> (1,1)
        assertEquals(3, stopPath.length);
        assertArrayEquals(new int[]{0, 0}, stopPath[0]);
        assertArrayEquals(new int[]{1, 1}, stopPath[2]);
        assertTrue(
            (stopPath[1][0] == 0 && stopPath[1][1] == 1) ||
            (stopPath[1][0] == 1 && stopPath[1][1] == 0),
            "Intermediate cell should be (0,1) or (1,0)"
        );
    }
}
