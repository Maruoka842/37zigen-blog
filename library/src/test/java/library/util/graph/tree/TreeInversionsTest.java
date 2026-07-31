package library.util.graph.tree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class TreeInversionsTest {

	@Test
	public void testSmall() {
		// 0-1
		Tree t = new Tree(2);
		t.addEdge(0, 1);
		long[] result = TreeInversions.countInversionsForAllRoots(t);
		// r=0: u=0: {0}, v>0 NO. u=1: {0,1}, v>1 NO. Sum=0.
		// r=1: u=0: {1,0}, v>0 YES(1). u=1: {1}, v>1 NO. Sum=1.
		assertArrayEquals(new long[]{0, 1}, result);
	}

	@Test
	public void testPath3() {
		// 0-1-2
		Tree t = new Tree(3);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		long[] result = TreeInversions.countInversionsForAllRoots(t);
		// r=0: u=0:{0}, u=1:{0,1}, u=2:{0,1,2}. All v <= u. Sum=0.
		// r=1: u=0:{1,0},v>0(v=1). u=1:{1}, u=2:{1,2}. Sum=1.
		// r=2: u=0:{2,1,0},v>0(v=1,2). u=1:{2,1},v>1(v=2). u=2:{2}. Sum=2+1=3.
		assertArrayEquals(new long[]{0, 1, 3}, result);
	}

	@Test
	public void testStar4() {
		// 0-1, 0-2, 0-3
		Tree t = new Tree(4);
		t.addEdge(0, 1);
		t.addEdge(0, 2);
		t.addEdge(0, 3);
		long[] result = TreeInversions.countInversionsForAllRoots(t);
		// r=0: All u have Anc={0, u}. No v in {0, u} s.t. v > u. Sum=0.
		// r=1:
		//  u=0: {1,0}, v>0(1).
		//  u=1: {1}.
		//  u=2: {1,0,2}, v>2 NO.
		//  u=3: {1,0,3}, v>3 NO.
		// Sum=1.
		// r=2:
		//  u=0: {2,0}, v>0(2).
		//  u=1: {2,0,1}, v>1(2).
		//  u=2: {2}.
		//  u=3: {2,0,3}, v>3 NO.
		// Sum=2.
		// r=3:
		//  u=0: {3,0}, v>0(3).
		//  u=1: {3,0,1}, v>1(3).
		//  u=2: {3,0,2}, v>2(3).
		//  u=3: {3}.
		// Sum=3.
		assertArrayEquals(new long[]{0, 1, 2, 3}, result);
	}

	@Test
	public void testReversePath4() {
		// 3-2-1-0
		Tree t = new Tree(4);
		t.addEdge(3, 2);
		t.addEdge(2, 1);
		t.addEdge(1, 0);
		long[] result = TreeInversions.countInversionsForAllRoots(t);
		// r=3:
		// u=0: {3,2,1,0}, v>0(3,2,1)
		// u=1: {3,2,1}, v>1(3,2)
		// u=2: {3,2}, v>2(3)
		// u=3: {3}
		// Sum=3+2+1=6.

		// r=2:
		// u=0: {2,1,0}, v>0(2,1)
		// u=1: {2,1}, v>1(2)
		// u=2: {2}
		// u=3: {2,3}, v>3 NO
		// Sum=2+1+0=3.

		// r=1:
		// u=0: {1,0}, v>0(1)
		// u=1: {1}
		// u=2: {1,2}, v>2 NO
		// u=3: {1,2,3}, v>3 NO
		// Sum=1.

		// r=0: 0.
		assertArrayEquals(new long[]{0, 1, 3, 6}, result);
	}

	@Test
	public void testRandomAgainstBruteForce() {
		Random rnd = new Random(1);
		for (int n = 1; n <= 9; n++) {
			for (int tc = 0; tc < 100; tc++) {
				Tree t = new Tree(n);
				List<int[]> edges = new ArrayList<>();
				for (int v = 1; v < n; v++) {
					int p = rnd.nextInt(v);
					t.addEdge(p, v);
					edges.add(new int[] {p, v});
				}
				assertArrayEquals(bruteForce(n, edges), TreeInversions.countInversionsForAllRoots(t));
			}
		}
	}

	private long[] bruteForce(int n, List<int[]> edges) {
		List<Integer>[] graph = new List[n];
		for (int i = 0; i < n; i++) graph[i] = new ArrayList<>();
		for (int[] e : edges) {
			graph[e[0]].add(e[1]);
			graph[e[1]].add(e[0]);
		}
		long[] res = new long[n];
		for (int root = 0; root < n; root++) {
			int[] parent = new int[n];
			Arrays.fill(parent, -1);
			Queue<Integer> queue = new ArrayDeque<>();
			parent[root] = root;
			queue.add(root);
			while (!queue.isEmpty()) {
				int v = queue.poll();
				for (int u : graph[v]) {
					if (parent[u] != -1) continue;
					parent[u] = v;
					queue.add(u);
				}
			}
			for (int u = 0; u < n; u++) {
				int v = u;
				while (true) {
					if (u < v) res[root]++;
					if (v == root) break;
					v = parent[v];
				}
			}
		}
		return res;
	}

}
