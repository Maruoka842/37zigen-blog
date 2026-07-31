package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class ErdosGinzburgZivTest {

    @Test
    public void testSmallN() {
        testWithN(1);
        testWithN(2);
        testWithN(3);
        testWithN(4);
        testWithN(5);
        testWithN(6);
        testWithN(7);
        testWithN(8);
        testWithN(9);
        testWithN(10);
    }

    @Test
    public void testLargeN() {
        testWithN(100);
        testWithN(101); // prime
    }

    private void testWithN(int N) {
        Random rnd = new Random(N);
        for (int t = 0; t < 10; t++) {
            int[] A = new int[2 * N - 1];
            for (int i = 0; i < A.length; i++) {
                A[i] = rnd.nextInt(10000);
            }
            int[] indices = ErdosGinzburgZiv.solve(N, A);
            assertEquals(N, indices.length);

            long sum = 0;
            boolean[] used = new boolean[2 * N - 1];
            for (int idx : indices) {
                assertTrue(idx >= 0 && idx < 2 * N - 1);
                assertTrue(!used[idx], "Indices must be unique");
                used[idx] = true;
                sum += A[idx];
            }
            assertEquals(0, sum % N, "Sum must be a multiple of N for N=" + N + ", sum=" + sum);
        }
    }

    @Test
    public void testDuplicateValues() {
        int N = 5;
        int[] A = {1, 1, 1, 1, 1, 2, 2, 2, 2}; // 2N-1 = 9
        int[] indices = ErdosGinzburgZiv.solve(N, A);
        assertEquals(5, indices.length);
        long sum = 0;
        for (int idx : indices) sum += A[idx];
        assertEquals(0, sum % N);
    }

    @Test
    public void testAllSameValues() {
        int N = 5;
        int[] A = {2, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] indices = ErdosGinzburgZiv.solve(N, A);
        assertEquals(5, indices.length);
        long sum = 0;
        for (int idx : indices) sum += A[idx];
        assertEquals(0, sum % N);
    }
}
