package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.GraphicMatroid;
import library.util.graph.MatroidIntersection;

public class GraphicMatroidTest {

	@Test
	public void testCircuit() {
		// 0-1, 1-2, 2-0 (Triangle)
		int V = 3;
		int[] u = {0, 1, 2};
		int[] v = {1, 2, 0};
		GraphicMatroid gm = new GraphicMatroid(V, u, v);

		boolean[] I = new boolean[3];
		I[0] = true;
		I[1] = true;
		// I = {0-1, 1-2}

		gm.set(I);
		IntArrayList circuit = gm.circuit(2); // Adding 2-0 should form a circuit {2, 0, 1}
		assertEquals(3, circuit.size());
		int[] edges = circuit.toArray();
		Arrays.sort(edges);
		assertArrayEquals(new int[]{0, 1, 2}, edges);
	}

	@Test
	public void testSelfLoop() {
		int V = 1;
		int[] u = {0};
		int[] v = {0};
		GraphicMatroid gm = new GraphicMatroid(V, u, v);

		gm.set(new boolean[1]);
		IntArrayList circuit = gm.circuit(0);
		assertEquals(1, circuit.size());
		assertEquals(0, circuit.get(0));
	}

	@Test
	public void testDisconnected() {
		// 0-1, 2-3
		int V = 4;
		int[] u = {0, 2};
		int[] v = {1, 3};
		GraphicMatroid gm = new GraphicMatroid(V, u, v);

		boolean[] I = {true, false};
		gm.set(I);

		// Adding 2-3 (edge 1) shouldn't form a circuit
		assertTrue(gm.circuit(1).isEmpty());
	}

	@Test
	public void testMaximumSpanningTree() {
		// Maximum Spanning Tree using Matroid Intersection (with a trivial second matroid)
		int V = 4;
		int[] u = {0, 0, 1, 1, 2};
		int[] v = {1, 2, 2, 3, 3};
		long[] weights = {10, 20, 5, 15, 10}; // MST should pick edges with weights 20, 15, 10

		GraphicMatroid gm = new GraphicMatroid(V, u, v);
		TrivialMatroid tm = new TrivialMatroid(u.length);

		boolean[] res = MatroidIntersection.solve(gm, tm, negate(weights));

		int count = 0;
		long totalWeight = 0;
		for (int i = 0; i < weights.length; i++) {
			if (res[i]) {
				count++;
				totalWeight += weights[i];
			}
		}

		assertEquals(3, count);
		assertEquals(45, totalWeight); // 20 + 15 + 10
	}

	private long[] negate(long[] a) {
		long[] res = new long[a.length];
		for (int i = 0; i < a.length; i++) res[i] = -a[i];
		return res;
	}

	private void assertArrayEquals(int[] expected, int[] actual) {
		Arrays.sort(expected);
		Arrays.sort(actual);
		org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
	}

	static class TrivialMatroid implements library.util.graph.Matroid {
		int n;
		TrivialMatroid(int n) { this.n = n; }
		@Override public int size() { return n; }
		@Override public void set(boolean[] I) {}
		@Override public IntArrayList circuit(int e) { return new IntArrayList(); }
	}
}
