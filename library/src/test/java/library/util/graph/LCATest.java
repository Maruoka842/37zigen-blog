package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.graph.tree.LCA;
import library.util.graph.tree.LongValueTree;
import library.util.graph.tree.Tree;

public class LCATest {

	@Test
	public void testLCA() {
		int n = 7;
		Tree t = new Tree(n);
		t.addEdge(0, 1);
		t.addEdge(0, 2);
		t.addEdge(1, 3);
		t.addEdge(1, 4);
		t.addEdge(2, 5);
		t.addEdge(2, 6);
		t.rooted(0);

		LCA lca = new LCA(t);

		assertEquals(0, lca.lca(3, 6));
		assertEquals(1, lca.lca(3, 4));
		assertEquals(0, lca.lca(3, 2));
		assertEquals(2, lca.lca(5, 6));
		assertEquals(1, lca.lca(1, 3));
		assertEquals(0, lca.lca(0, 4));

		assertEquals(4, lca.dist(3, 6));
		assertEquals(2, lca.dist(3, 4));
		assertEquals(4, lca.dist(3, 5));
		assertEquals(1, lca.dist(0, 1));
		assertEquals(0, lca.dist(3, 3));
	}

	@Test
	public void testLCA2() {
		int n = 5;
		Tree t = new Tree(n);
		t.addEdge(0, 1);
		t.addEdge(1, 2);
		t.addEdge(2, 3);
		t.addEdge(3, 4);
		t.rooted(0);

		LCA lca = new LCA(t);
		assertEquals(0, lca.lca(0, 4));
		assertEquals(2, lca.lca(2, 4));
		assertEquals(3, lca.lca(3, 4));

		assertEquals(4, lca.dist(0, 4));
		assertEquals(2, lca.dist(2, 4));
	}

	@Test
	public void testLongValueTreeLCA() {
		int n = 4;
		LongValueTree t = new LongValueTree(n);
		t.addEdge(0, 1, 10);
		t.addEdge(0, 2, 20);
		t.addEdge(1, 3, 30);
		t.rooted(0);

		LCA lca = new LCA(t);
		assertEquals(0, lca.lca(2, 3));
		assertEquals(1, lca.lca(1, 3));
		assertEquals(0, lca.lca(0, 3));

		assertEquals(3, lca.dist(2, 3));
		assertEquals(1, lca.dist(1, 3));
		assertEquals(2, lca.dist(2, 1));

		assertEquals(60, lca.weightedDist(2, 3));
		assertEquals(30, lca.weightedDist(1, 3));
		assertEquals(30, lca.weightedDist(2, 1));
	}

	@Test
	public void testRandom() {
		for (int t = 0; t < 10; t++) {
			int n = 100;
			Tree tree = Tree.randomTree(n);
			tree.rooted(0);
			LCA lca = new LCA(tree);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					assertEquals(tree.lca(i, j), lca.lca(i, j));
					assertEquals(tree.dist(i, j), lca.dist(i, j));
				}
			}
		}
	}

	@Test
	public void testBug() {
		int n = 3;
		Tree t = new Tree(n);
		t.addEdge(1, 0);
		t.addEdge(1, 2);
		t.rooted(1);

		LCA lca = new LCA(t);
		assertEquals(1, lca.lca(0, 2));
		assertEquals(1, lca.lca(0, 1));
		assertEquals(1, lca.lca(1, 2));
		assertEquals(0, lca.lca(0, 0));
	}

	@Test
	public void testRandomLongValueTree() {
		for (int t = 0; t < 10; t++) {
			int n = 100;
			Tree tree = Tree.randomTree(n);
			LongValueTree lvt = new LongValueTree(n);
			for (var e : tree.edges()) {
				lvt.addEdge(e[0], e[1], (long) (Math.random() * 1000));
			}
			lvt.rooted(0);
			LCA lca = new LCA(lvt);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					assertEquals(lvt.lca(i, j), lca.lca(i, j));
					assertEquals(lvt.rawDist(i, j), lca.dist(i, j));
					assertEquals(lvt.dist(i, j), lca.weightedDist(i, j));
				}
			}
		}
	}
}
