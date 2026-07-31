package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

public class MaxWeightIndependentSetWithDistanceKTest {

    @Test
    public void testSmall() {
        long[] a = {1, 10, 1, 1, 10, 1};
        int K = 3;
        // K=3, N=6. max size = (6+3-1)/3 = 2.
        // k=0: 0
        // k=1: 10 (at index 1 or 4)
        // k=2: 10 + 10 = 20 (indices 1 and 4, distance |4-1|=3 >= 3)
        long[] expected = {0, 10, 20};
        assertArrayEquals(expected, MaxWeightIndependentSetWithDistanceK.solveAll(a, K));

        for (int k = 0; k <= 2; k++) {
            assertEquals(expected[k], MaxWeightIndependentSetWithDistanceK.solveK(a, K, k));
        }
    }

    @Test
    public void testK1() {
        long[] a = {1, 2, 3, 4, 5};
        int K = 1;
        // All can be selected.
        // k=0: 0
        // k=1: 5
        // k=2: 5+4=9
        // k=3: 5+4+3=12
        // k=4: 5+4+3+2=14
        // k=5: 5+4+3+2+1=15
        long[] expected = {0, 5, 9, 12, 14, 15};
        assertArrayEquals(expected, MaxWeightIndependentSetWithDistanceK.solveAll(a, K));
        for (int k = 0; k <= 5; k++) {
            assertEquals(expected[k], MaxWeightIndependentSetWithDistanceK.solveK(a, K, k));
        }
    }

    @Test
    public void testK2() {
        long[] a = {1, 10, 2, 11, 3};
        int K = 2;
        // Non-adjacent.
        // k=0: 0
        // k=1: 11
        // k=2: 10+11=21
        // k=3: 1+2+3=6
        long[] expected = {0, 11, 21, 6};
        assertArrayEquals(expected, MaxWeightIndependentSetWithDistanceK.solveAll(a, K));
        for (int k = 0; k <= 3; k++) {
            assertEquals(expected[k], MaxWeightIndependentSetWithDistanceK.solveK(a, K, k));
        }
    }

    @Test
    public void testNegative() {
        long[] a = {-1, -10, -1, -1, -10, -1};
        int K = 3;
        long[] expected = {0, -1, -2};
        assertArrayEquals(expected, MaxWeightIndependentSetWithDistanceK.solveAll(a, K));
        for (int k = 0; k <= 2; k++) {
            assertEquals(expected[k], MaxWeightIndependentSetWithDistanceK.solveK(a, K, k));
        }
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int t = 0; t < 20; t++) {
            int n = rnd.nextInt(50) + 1;
            int K = rnd.nextInt(5) + 1;
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = rnd.nextInt(200) - 100;
            }

            long[] all = MaxWeightIndependentSetWithDistanceK.solveAll(a, K);
            int maxK = (n + K - 1) / K;
            assertEquals(maxK + 1, all.length);

            for (int k = 0; k <= maxK; k++) {
                long resK = MaxWeightIndependentSetWithDistanceK.solveK(a, K, k);
                assertEquals(all[k], resK, "Failed at n=" + n + ", K=" + K + ", k=" + k + ", a=" + Arrays.toString(a));
            }
        }
    }
}
