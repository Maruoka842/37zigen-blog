package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import library.util.graph.tree.Tree;

public class TreeTest {
	
	@Test
	public void testLongestPathLengths_SingleNode() {
		Tree t = new Tree(1);
		t.rooted(0);
		int[] expected = { 0 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_TwoNodes() {
		Tree t = new Tree(2);
		t.addEdge(0, 1);
		t.rooted(0);
		int[] expected = { 1, 1 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_StarGraph() {
		// 1-0-2, 0-3
		// 0 is center
		Tree t = new Tree(4);
		t.addEdge(0, 1);
		t.addEdge(0, 2);
		t.addEdge(0, 3);
		t.rooted(0);
		// Through 0: top heights are 1, 1 -> 2
		// Through 1: top heights are 2, 0 -> 2
		// Through 2: top heights are 2, 0 -> 2
		// Through 3: top heights are 2, 0 -> 2
		int[] expected = { 2, 2, 2, 2 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_PathGraph() {
		// 0-1-2-3
		Tree t = new Tree(4);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		t.addEdge(2, 3);
		t.rooted(0);
		// Through 0: height 3, 0 -> 3
		// Through 1: heights 1, 2 -> 3
		// Through 2: heights 2, 1 -> 3
		// Through 3: heights 3, 0 -> 3
		int[] expected = { 3, 3, 3, 3 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_ComplexTree() {
		// 0-1, 1-2, 1-3, 3-4, 3-5
		Tree t = new Tree(6);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		t.addEdge(1, 3);
		t.addEdge(3, 4);
		t.addEdge(3, 5);
		t.rooted(0);
		// Diameter path could be 0-1-3-4 (length 3), 0-1-3-5 (3), 2-1-3-4 (3), 2-1-3-5
		// (3), 4-3-5 (2)
		// Max path length in tree is 3.
		// Through 0: max branch heights 3, 0 -> 3
		// Through 1: branches (0) h=1, (2) h=1, (3-4/5) h=2. Top two: 2, 1 -> 3
		// Through 2: branch (1-3-4/5) h=3, 0 -> 3
		// Through 3: branches (1-0/2) h=2, (4) h=1, (5) h=1. Top two: 2, 1 -> 3
		// Through 4: branch (3-1-0/2) h=3, 0 -> 3
		// Through 5: branch (3-1-0/2) h=3, 0 -> 3
		int[] expected = { 3, 3, 3, 3, 3, 3 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_BranchyTree() {
		// 0-1-2-3-4
		// |
		// 5
		Tree t = new Tree(6);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		t.addEdge(2, 3);
		t.addEdge(3, 4);
		t.addEdge(2, 5);
		t.rooted(0);
		// Through 0: branch 3 -> 3
		// Through 1: branches (0) h=1, (2-3-4) h=3. Sum=4
		// Through 2: branches (1-0) h=2, (3-4) h=2, (5) h=1. Top two: 2, 2 -> 4
		// Through 3: branches (2-1-0) h=3, (4) h=1. Sum=4
		// Through 4: branch 4 -> 4
		// Through 5: branch (2-3-4) h=3. Sum=3? Wait. 5-2-3-4 is length 3.

		// Let's re-evaluate:
		// 0: path 0-1-2-3-4 length 4.
		// 1: path 0-1-2-3-4 length 4.
		// 2: path 0-1-2-3-4 length 4.
		// 3: path 0-1-2-3-4 length 4.
		// 4: path 4-3-2-1-0 length 4.
		// 5: path 5-2-3-4 length 3, or 5-2-1-0 length 3. Max is 3.
		int[] expected = { 4, 4, 4, 4, 4, 3 };
		assertArrayEquals(expected, t.longestPathLengths());
	}

	@Test
	public void testLongestPathLengths_RandomStressTest() {
		for (int t_case = 0; t_case < 100; t_case++) {
			int N = (int) (Math.random() * 50) + 2;
			Tree t = Tree.randomTree(N);
			int[] expected = longestPathLengthsNaive(t);
			int[] actual = t.longestPathLengths();
			assertArrayEquals(expected, actual, "Failed at random tree case " + t_case);
		}
	}

	@Test
	public void testPath() {
		int n = 5;
		Tree t = Tree.path(n);
		org.junit.jupiter.api.Assertions.assertEquals(n, t.N);
		org.junit.jupiter.api.Assertions.assertEquals(n - 1, t.M);
		for (int i = 0; i < n - 1; i++) {
			boolean found1 = false;
			for (int j = 0; j < t.adj[i].size(); j++) if (t.adj[i].get(j) == i + 1) found1 = true;
			org.junit.jupiter.api.Assertions.assertTrue(found1);
			boolean found2 = false;
			for (int j = 0; j < t.adj[i + 1].size(); j++) if (t.adj[i + 1].get(j) == i) found2 = true;
			org.junit.jupiter.api.Assertions.assertTrue(found2);
		}
	}

	@Test
	public void testStar() {
		int n = 5;
		Tree t = Tree.star(n);
		org.junit.jupiter.api.Assertions.assertEquals(n, t.N);
		org.junit.jupiter.api.Assertions.assertEquals(n - 1, t.M);
		for (int i = 1; i < n; i++) {
			boolean found1 = false;
			for (int j = 0; j < t.adj[0].size(); j++) if (t.adj[0].get(j) == i) found1 = true;
			org.junit.jupiter.api.Assertions.assertTrue(found1);
			boolean found2 = false;
			for (int j = 0; j < t.adj[i].size(); j++) if (t.adj[i].get(j) == 0) found2 = true;
			org.junit.jupiter.api.Assertions.assertTrue(found2);
		}
	}

	@Test
	public void testPathStarEdgeCases() {
		// n = 0
		Tree p0 = Tree.path(0);
		org.junit.jupiter.api.Assertions.assertEquals(0, p0.N);
		org.junit.jupiter.api.Assertions.assertEquals(0, p0.M);

		Tree s0 = Tree.star(0);
		org.junit.jupiter.api.Assertions.assertEquals(0, s0.N);
		org.junit.jupiter.api.Assertions.assertEquals(0, s0.M);

		// n = 1
		Tree p1 = Tree.path(1);
		org.junit.jupiter.api.Assertions.assertEquals(1, p1.N);
		org.junit.jupiter.api.Assertions.assertEquals(0, p1.M);

		Tree s1 = Tree.star(1);
		org.junit.jupiter.api.Assertions.assertEquals(1, s1.N);
		org.junit.jupiter.api.Assertions.assertEquals(0, s1.M);
	}

	@Test
	public void testSizeWithTemporaryRoot() {
		// Test on a small tree
		// 0 - 1 - 2
		//     |
		//     3
		Tree t = new Tree(4);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		t.addEdge(1, 3);
		t.rooted(0);

		// With original root = 0:
		// sizes: size(0)=4, size(1)=3, size(2)=1, size(3)=1

		// Temporary root = 0 (same as original root)
		org.junit.jupiter.api.Assertions.assertEquals(4, t.size(0, 0));
		org.junit.jupiter.api.Assertions.assertEquals(3, t.size(1, 0));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(2, 0));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(3, 0));

		// Temporary root = 1
		// When rooted at 1, tree looks like:
		//     1
		//   / | \
		//  0  2  3
		// Subtree sizes should be: size(1)=4, size(0)=1, size(2)=1, size(3)=1
		org.junit.jupiter.api.Assertions.assertEquals(4, t.size(1, 1));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(0, 1));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(2, 1));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(3, 1));

		// Temporary root = 2
		// When rooted at 2:
		//  2 - 1 - 0
		//      |
		//      3
		// Subtree sizes: size(2)=4, size(1)=3, size(0)=1, size(3)=1
		org.junit.jupiter.api.Assertions.assertEquals(4, t.size(2, 2));
		org.junit.jupiter.api.Assertions.assertEquals(3, t.size(1, 2));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(0, 2));
		org.junit.jupiter.api.Assertions.assertEquals(1, t.size(3, 2));
	}

	@Test
	public void testSizeWithTemporaryRootRandomStressTest() {
		for (int t_case = 0; t_case < 100; t_case++) {
			int N = (int) (Math.random() * 50) + 2;
			Tree t = Tree.randomTree(N);
			t.rooted(0); // arbitrary initial rooting

			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					int actual = t.size(i, j);
					int expected = sizeNaive(t, i, j);
					org.junit.jupiter.api.Assertions.assertEquals(expected, actual,
						"Failed at random tree case: " + t_case + " N: " + N + " v: " + i + " r: " + j);
				}
			}
		}
	}

	private int sizeNaive(Tree t, int v, int r) {
		// Calculate size of v when temporarily rooted at r using DFS
		boolean[] visited = new boolean[t.N];
		// DFS to count size of subtree of v.
		// In a rooted tree with root r, the subtree of v consists of all vertices u
		// such that the path from r to u passes through v.
		// Alternatively, we can start DFS at r. We want to find the component containing v when
		// the edge connecting v to its "parent" in the r-rooted tree is removed.
		// The parent of v is the neighbor of v on the path from v to r.
		// If v == r, the subtree size is N.
		if (v == r) return t.N;

		// Otherwise, find the unique neighbor of v on the path from v to r, which acts as v's parent.
		// Let's find the path from r to v.
		int[] path = t.path(r, v);
		int parentOfV = path[path.length - 2]; // the node just before v on path from r to v

		// The subtree of v is the component of v in the forest t \ { (v, parentOfV) }.
		return countSubtreeDfs(t, v, parentOfV, visited);
	}

	private int countSubtreeDfs(Tree t, int curr, int parent, boolean[] visited) {
		visited[curr] = true;
		int count = 1;
		for (int neighbor : t.adj[curr]) {
			if (neighbor != parent && !visited[neighbor]) {
				count += countSubtreeDfs(t, neighbor, curr, visited);
			}
		}
		return count;
	}

	private int[] longestPathLengthsNaive(Tree t) {
		int N = t.N;
		int[] ret = new int[N];
		for (int i = 0; i < N; i++) {
			t.rooted(i);
			int[] heights = t.heights();
			int h1 = 0, h2 = 0;
			// 頂点 i を根としたとき、i を通る最長パスは、i の子を根とする部分木の高さのうち
			// 上位2つ（に辺1つ分足したもの）の和である。
			for (int child : t.childs[i]) {
				int h = heights[child] + 1;
				if (h > h1) {
					h2 = h1;
					h1 = h;
				} else if (h > h2) {
					h2 = h;
				}
			}
			ret[i] = h1 + h2;
		}
		return ret;
	}
}
