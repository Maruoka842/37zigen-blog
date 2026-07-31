package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.Matroid;
import library.util.graph.MatroidUnion;
import library.util.graph.MatroidUnion.MatroidUnionResult;

public class MatroidUnionTest {

	static class PartitionMatroid implements Matroid {
		int n;
		int[] elementToGroup;
		int[] groupCapacity;
		boolean[] I;
		int[] currentGroupCount;

		PartitionMatroid(int n, int numGroups, int[] elementToGroup, int[] groupCapacity) {
			this.n = n;
			this.elementToGroup = elementToGroup;
			this.groupCapacity = groupCapacity;
			this.currentGroupCount = new int[numGroups];
		}

		@Override
		public int size() {
			return n;
		}

		@Override
		public void set(boolean[] I) {
			this.I = I.clone();
			for (int i = 0; i < currentGroupCount.length; i++) currentGroupCount[i] = 0;
			for (int i = 0; i < n; i++) {
				if (I[i]) currentGroupCount[elementToGroup[i]]++;
			}
		}

		@Override
		public IntArrayList circuit(int e) {
			int group = elementToGroup[e];
			if (currentGroupCount[group] < groupCapacity[group]) {
				return new IntArrayList();
			}
			IntArrayList res = new IntArrayList();
			res.add(e);
			for (int i = 0; i < n; i++) {
				if (I[i] && elementToGroup[i] == group) {
					res.add(i);
				}
			}
			return res;
		}
	}

	@Test
	public void testUnweightedMatroidUnion() {
		// Example: 2-colorable graph edge subset? No, let's use partition matroids.
		// Ground set: {0, 1, 2, 3}
		// M1: Partition {0, 1}, {2, 3} each capacity 1
		// M2: Partition {0, 2}, {1, 3} each capacity 1
		int n = 4;
		int[] eToG1 = {0, 0, 1, 1};
		int[] cap1 = {1, 1};
		int[] eToG2 = {0, 1, 0, 1};
		int[] cap2 = {1, 1};

		PartitionMatroid m1 = new PartitionMatroid(n, 2, eToG1, cap1);
		PartitionMatroid m2 = new PartitionMatroid(n, 2, eToG2, cap2);

		MatroidUnionResult res = MatroidUnion.solve(m1, m2);
		assertEquals(4, res.size());
	}

	@Test
	public void testWeightedMatroidUnion() {
		// Ground set: {0, 1, 2}
		// M1: any 1 element
		// M2: any 1 element
		// Weights: {10, 5, 100}
		// Max union size is 2. Minimum weight union should be {0, 1} with weight 15.
		int n = 3;
		int[] eToG = {0, 0, 0};
		int[] cap = {1};
		long[] weights = {10, 5, 100};

		PartitionMatroid m1 = new PartitionMatroid(n, 1, eToG, cap);
		PartitionMatroid m2 = new PartitionMatroid(n, 1, eToG, cap);

		MatroidUnionResult res = MatroidUnion.solve(m1, m2, weights);
		assertEquals(2, res.size());

		long totalWeight = 0;
		for (int i = 0; i < n; i++) {
			if (res.i1()[i] || res.i2()[i]) {
				totalWeight += weights[i];
			}
		}
		assertEquals(15, totalWeight);
	}
}
