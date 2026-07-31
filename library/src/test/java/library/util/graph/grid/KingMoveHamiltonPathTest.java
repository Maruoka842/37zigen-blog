package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class KingMoveHamiltonPathTest {
	@Test
	public void testTimeoutCases() {
		int[] H_vals = {10, 20, 50, 100, 200};
		int[] W_vals = {10, 20, 50, 100, 200};
		for (int H : H_vals) {
			for (int W : W_vals) {
				int[][] cases = {
					{0, 0, 0, 1},
					{0, 0, H-1, W-1},
					{0, 0, H/2, W/2},
					{H/2, W/2, H/2 + 1, W/2},
					{0, 0, 0, W-1}
				};
				for (int[] c : cases) {
					int x0 = c[0], y0 = c[1], x1 = c[2], y1 = c[3];
					if (KingMoveHamiltonPath.existsKingMoveHamiltonPath(x0, y0, x1, y1, H, W)) {
						long t0 = System.currentTimeMillis();
						int[][] res = KingMoveHamiltonPath.kingMoveHamiltonPath(x0, y0, x1, y1, H, W);
						long t1 = System.currentTimeMillis();
						assertNotNull(res);
						validatePath(res, H, W, x0, y0, x1, y1);
						if (t1 - t0 > 100) {
							System.out.printf("Slow case: H=%d, W=%d, start=(%d,%d), goal=(%d,%d) took %d ms\n", H, W, x0, y0, x1, y1, t1 - t0);
						}
					}
				}
			}
		}
	}

	@Test
	public void testRandomPaths() {
		Random rnd = new Random(42);
		for (int i = 0; i < 50; i++) {
			int H = rnd.nextInt(50) + 1;
			int W = rnd.nextInt(50) + 1;
			int x0 = rnd.nextInt(H);
			int y0 = rnd.nextInt(W);
			int x1 = rnd.nextInt(H);
			int y1 = rnd.nextInt(W);
			if (KingMoveHamiltonPath.existsKingMoveHamiltonPath(x0, y0, x1, y1, H, W)) {
				int[][] res = KingMoveHamiltonPath.kingMoveHamiltonPath(x0, y0, x1, y1, H, W);
				assertNotNull(res);
				validatePath(res, H, W, x0, y0, x1, y1);
			}
		}
	}

	private void validatePath(int[][] path, int H, int W, int x0, int y0, int x1, int y1) {
		assertEquals(H * W, path.length);
		assertEquals(x0, path[0][0]);
		assertEquals(y0, path[0][1]);
		assertEquals(x1, path[H*W - 1][0]);
		assertEquals(y1, path[H*W - 1][1]);

		boolean[][] visited = new boolean[H][W];
		for (int i = 0; i < path.length; i++) {
			int r = path[i][0];
			int c = path[i][1];
			assertTrue(r >= 0 && r < H);
			assertTrue(c >= 0 && c < W);
			assertFalse(visited[r][c], "Visited cell twice: (" + r + ", " + c + ")");
			visited[r][c] = true;
			if (i > 0) {
				int pr = path[i-1][0];
				int pc = path[i-1][1];
				int dr = Math.abs(r - pr);
				int dc = Math.abs(c - pc);
				assertTrue(dr <= 1 && dc <= 1 && (dr > 0 || dc > 0), "Invalid king move from (" + pr + "," + pc + ") to (" + r + "," + c + ")");
			}
		}
	}
}
