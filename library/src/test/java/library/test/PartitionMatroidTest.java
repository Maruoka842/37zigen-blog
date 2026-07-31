package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.graph.MatroidIntersection;
import library.util.graph.PartitionMatroid;

public class PartitionMatroidTest {

    @Test
    public void testBasic() {
        int M = 5;
        int[][] parts = {{0, 1, 2}, {3, 4}};
        int[] R = {1, 1};
        PartitionMatroid pm = new PartitionMatroid(M, parts, R);

        assertEquals(5, pm.size());

        boolean[] I = new boolean[5];
        I[0] = true;
        I[3] = true;
        pm.set(I);

        // Adding 1 to {0, 3} should form a circuit with 0
        IntArrayList c1 = pm.circuit(1);
        assertEquals(2, c1.size());
        int[] c1_arr = c1.toArray();
        Arrays.sort(c1_arr);
        assertArrayEquals(new int[]{0, 1}, c1_arr);

        // Adding 2 should form a circuit with 0
        IntArrayList c2 = pm.circuit(2);
        assertEquals(2, c2.size());
        int[] c2_arr = c2.toArray();
        Arrays.sort(c2_arr);
        assertArrayEquals(new int[]{0, 2}, c2_arr);

        // Adding 4 should form a circuit with 3
        IntArrayList c4 = pm.circuit(4);
        assertEquals(2, c4.size());
        int[] c4_arr = c4.toArray();
        Arrays.sort(c4_arr);
        assertArrayEquals(new int[]{3, 4}, c4_arr);
    }

    @Test
    public void testUnassigned() {
        int M = 3;
        int[][] parts = {{0}};
        int[] R = {1};
        // 1 and 2 are unassigned, so they should be in their own parts with capacity 1.
        PartitionMatroid pm = new PartitionMatroid(M, parts, R);

        boolean[] I = new boolean[3];
        I[1] = true;
        pm.set(I);

        // 1 is already in I, adding 1 again (well, the circuit tool is for e NOT in I typically)
        // But the logic should handle it. If 1 is in I and capacity is 1, cnt[belong[1]] == 0.
        // circuit(1) will return {1, 1}? Wait, the C++ code says if (I[e] and cnt[belong[e]] == 0) circuits[belong[e]].push_back(e);
        // So if I={1}, cnt[belong[1]] = 0, circuits[belong[1]] = {1}. circuit(1) = {1, 1}.
        // This is consistent with the contract of circuit(e) which is "unique circuit in I union {e}".
        // If e is already in I, I union {e} = I, which is independent, so it should return empty?
        // Let's re-read the contract in Matroid.java.
        // "I ∪ {e} に含まれる唯一のサーキット（基本閉路）を返す。I ∪ {e} が独立な場合は空のリストを返す。"
        // If e is in I, and I is independent, I union {e} is I, which is independent.
        // So it should return empty.
        // My implementation:
        // if (cnt[p] == 0) { ... add(e); return ret; }
        // If e is in I, and cnt[p] was 1, now cnt[p] is 0. circuits[p] contains e.
        // So ret will contain e, then we add e again. ret = {e, e}.
        // This is WRONG if e is already in I.
        // However, MatroidIntersection only calls circuit(e) for e NOT in I.
        /*
		for (int e = 0; e < n; e++) {
			if (I[e]) continue;
			IntArrayList c1 = m1.circuit(e);
            ...
        */
        // Let's check e not in I.
        IntArrayList c0 = pm.circuit(0);
        assertTrue(c0.isEmpty());

        IntArrayList c2 = pm.circuit(2);
        assertTrue(c2.isEmpty());
    }

    @Test
    public void testCapacityZero() {
        int M = 2;
        int[][] parts = {{0}, {1}};
        int[] R = {0, 1};
        PartitionMatroid pm = new PartitionMatroid(M, parts, R);

        pm.set(new boolean[2]);
        IntArrayList c0 = pm.circuit(0);
        assertEquals(1, c0.size());
        assertEquals(0, c0.get(0)); // {0} is a circuit because capacity is 0

        IntArrayList c1 = pm.circuit(1);
        assertTrue(c1.isEmpty());
    }

    @Test
    public void testIntersection() {
        // Matching in a bipartite graph can be seen as intersection of two partition matroids.
        // Left side constraints, Right side constraints.
        // 2 nodes on left, 2 nodes on right.
        // Edges: (L0, R0), (L0, R1), (L1, R0)
        // M = 3 edges.
        // Edge 0: L0-R0, Edge 1: L0-R1, Edge 2: L1-R0

        // M1 (Left side): parts = {{0, 1}, {2}}, R = {1, 1}
        int[][] parts1 = {{0, 1}, {2}};
        int[] R1 = {1, 1};
        PartitionMatroid m1 = new PartitionMatroid(3, parts1, R1);

        // M2 (Right side): parts = {{0, 2}, {1}}, R = {1, 1}
        int[][] parts2 = {{0, 2}, {1}};
        int[] R2 = {1, 1};
        PartitionMatroid m2 = new PartitionMatroid(3, parts2, R2);

        boolean[] result = MatroidIntersection.solve(m1, m2);
        int count = 0;
        for (boolean b : result) if (b) count++;
        assertEquals(2, count);
        // Possible matchings: {Edge 1, Edge 2} = {(L0, R1), (L1, R0)}
        assertTrue(result[1] && result[2]);
        assertFalse(result[0]);
    }
}
