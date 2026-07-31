package library.util.unionfind;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

public class UnionFindUtilsTest {

	@Test
	public void testSolveWithUnions() {
		// 5 vertices: 0, 1, 2, 3, 4
		// Unions sequence:
		// 1: (0, 1)
		// 2: (1, 2)
		// 3: (3, 4)
		// 4: (2, 3)
		int N = 5;
		int[][] unions = {
			{0, 1},
			{1, 2},
			{3, 4},
			{2, 3}
		};

		// Queries:
		// q0: 0 and 0 -> already connected (0)
		// q1: 0 and 1 -> connected after step 1 (1)
		// q2: 0 and 2 -> connected after step 2 (2)
		// q3: 0 and 4 -> connected after step 4 (4)
		// q4: 0 and 3 -> connected after step 4 (4)
		int[] s = {0, 0, 0, 0, 0};
		int[] t = {0, 1, 2, 4, 3};

		int[] expected = {0, 1, 2, 4, 4};
		int[] actual = UnionFindUtils.solve(N, unions, s, t);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testEdgeCases() {
		// Single vertex, no unions
		int N = 1;
		int[][] unions = new int[0][0];
		int[] s = {0};
		int[] t = {0};
		int[] expected = {0};
		int[] actual = UnionFindUtils.solve(N, unions, s, t);
		assertArrayEquals(expected, actual);
	}
}
