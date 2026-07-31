package library.util.seq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class RSKTest {

    @Test
    public void testIdentity() {
        int[] a = {0, 1, 2};
        Permutation.RSKResult res = Permutation.rsk(a);
        Assertions.assertArrayEquals(new int[][]{{0, 1, 2}}, res.P());
        Assertions.assertArrayEquals(new int[][]{{0, 1, 2}}, res.Q());
    }

    @Test
    public void testReverse() {
        int[] a = {2, 1, 0};
        Permutation.RSKResult res = Permutation.rsk(a);
        Assertions.assertArrayEquals(new int[][]{{0}, {1}, {2}}, res.P());
        Assertions.assertArrayEquals(new int[][]{{0}, {1}, {2}}, res.Q());
    }

    @Test
    public void testExample() {
        int[] a = {1, 0, 2};
        Permutation.RSKResult res = Permutation.rsk(a);
        // Step-by-step:
        // 1: P=[[1]], Q=[[0]]
        // 0: 0 bumps 1. P=[[0], [1]], Q=[[0], [1]]
        // 2: P=[[0, 2], [1]], Q=[[0, 2], [1]]
        Assertions.assertArrayEquals(new int[][]{{0, 2}, {1}}, res.P());
        Assertions.assertArrayEquals(new int[][]{{0, 2}, {1}}, res.Q());
    }

    @Test
    public void testStandardYoungTableauProperties() {
        int[] p = {2, 4, 1, 3, 0};
        Permutation.RSKResult res = Permutation.rsk(p);

        verifyRSKProperties(p, res);
    }

    private void verifyRSKProperties(int[] a, Permutation.RSKResult res) {
        int[][] P = res.P();
        int[][] Q = res.Q();

        // 1. Same shape
        Assertions.assertEquals(P.length, Q.length);
        for (int i = 0; i < P.length; i++) {
            Assertions.assertEquals(P[i].length, Q[i].length);
        }

        // 2. Standard Young Tableau properties for P
        for (int i = 0; i < P.length; i++) {
            for (int j = 0; j < P[i].length - 1; j++) {
                Assertions.assertTrue(P[i][j] < P[i][j+1], "Row not increasing");
            }
        }
        for (int j = 0; j < P[0].length; j++) {
            for (int i = 0; i < P.length - 1; i++) {
                if (j < P[i+1].length) {
                    Assertions.assertTrue(P[i][j] < P[i+1][j], "Column not increasing");
                }
            }
        }

        // 3. Standard Young Tableau properties for Q
        for (int i = 0; i < Q.length; i++) {
            for (int j = 0; j < Q[i].length - 1; j++) {
                Assertions.assertTrue(Q[i][j] < Q[i][j+1], "Row not increasing in Q");
            }
        }
        for (int j = 0; j < Q[0].length; j++) {
            for (int i = 0; i < Q.length - 1; i++) {
                if (j < Q[i+1].length) {
                    Assertions.assertTrue(Q[i][j] < Q[i+1][j], "Column not increasing in Q");
                }
            }
        }

        // 4. LIS length
        int lisLen = computeLISLen(a);
        Assertions.assertEquals(lisLen, P[0].length, "First row length should be LIS length");
    }

    private int computeLISLen(int[] a) {
        int n = a.length;
        if (n == 0) return 0;
        int[] tails = new int[n];
        int size = 0;
        for (int x : a) {
            int i = 0, j = size;
            while (i != j) {
                int m = (i + j) / 2;
                if (tails[m] < x)
                    i = m + 1;
                else
                    j = m;
            }
            tails[i] = x;
            if (i == size) size++;
        }
        return size;
    }
}
