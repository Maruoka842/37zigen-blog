package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class KSumTest {

    @Test
    public void testTwoSum() {
        long[] a = {1, 2, 4, 8, 16};
        int[] res = KSum.twoSum(a, 6);
        assertNotNull(res);
        assertEquals(2, res.length);
        assertEquals(6, a[res[0]] + a[res[1]]);

        res = KSum.twoSum(a, 100);
        assertNull(res);
    }

    @Test
    public void testTwoSumRange() {
        long[] a = {1, 2, 4, 8, 16};
        int[] res = KSum.twoSumRange(a, 5, 7); // 2+4=6
        assertNotNull(res);
        long sum = a[res[0]] + a[res[1]];
        assertTrue(sum >= 5 && sum < 7);
    }

    @Test
    public void testThreeSum() {
        long[] a = {1, 2, 4, 8, 16};
        int[] res = KSum.threeSum(a, 7);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertEquals(7, a[res[0]] + a[res[1]] + a[res[2]]);
    }

    @Test
    public void testThreeSumRange() {
        long[] a = {1, 2, 4, 8, 16};
        int[] res = KSum.threeSumRange(a, 5, 8);
        assertNotNull(res);
        long sum = a[res[0]] + a[res[1]] + a[res[2]];
        assertTrue(sum >= 5 && sum < 8);
    }

    @Test
    public void testFourSum() {
        long[] a = {1, 2, 4, 8, 16, 32};
        int[] res = KSum.fourSum(a, 15);
        assertNotNull(res);
        assertEquals(4, res.length);
        assertEquals(15, a[res[0]] + a[res[1]] + a[res[2]] + a[res[3]]);
    }

    @Test
    public void testFiveSum() {
        long[] a = {1, 2, 4, 8, 16, 32, 64};
        int[] res = KSum.fiveSum(a, 31);
        assertNotNull(res);
        assertEquals(5, res.length);
        assertEquals(31, a[res[0]] + a[res[1]] + a[res[2]] + a[res[3]] + a[res[4]]);
    }

    @Test
    public void testFiveSumRange() {
        long[] a = {1, 2, 4, 8, 16, 32, 64};
        int[] res = KSum.fiveSumRange(a, 30, 32);
        assertNotNull(res);
        long sum = 0;
        for (int i : res) sum += a[i];
        assertTrue(sum >= 30 && sum < 32);
    }

    @Test
    public void testSixSum() {
        long[] a = {1, 2, 4, 8, 16, 32, 64, 128};
        int[] res = KSum.sixSum(a, 63);
        assertNotNull(res);
        assertEquals(6, res.length);
        assertEquals(63, a[res[0]] + a[res[1]] + a[res[2]] + a[res[3]] + a[res[4]] + a[res[5]]);
    }

    @Test
    public void testSixSumRange() {
        long[] a = {1, 2, 4, 8, 16, 32, 64, 128};
        int[] res = KSum.sixSumRange(a, 60, 65);
        assertNotNull(res);
        long sum = 0;
        for (int i : res) sum += a[i];
        assertTrue(sum >= 60 && sum < 65);
    }

    @Test
    public void testThreeSumFFTBasic() {
        // Trigger FFT branch: R = 49 <= 40000, N = 1000 >= 800
        long[] a = new long[1000];
        for (int i = 0; i < 1000; i++) {
            a[i] = i % 50;
        }
        int[] res = KSum.threeSum(a, 123);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
        assertEquals(123, a[res[0]] + a[res[1]] + a[res[2]]);

        // Target impossible
        res = KSum.threeSum(a, 1000);
        assertNull(res);
    }

    @Test
    public void testThreeSumFFTNegative() {
        // Trigger FFT branch with negative values
        long[] a = new long[1000];
        for (int i = 0; i < 1000; i++) {
            a[i] = (i % 50) - 25; // range [-25, 24] -> R = 49
        }
        int[] res = KSum.threeSum(a, -10);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
        assertEquals(-10, a[res[0]] + a[res[1]] + a[res[2]]);
    }

    @Test
    public void testThreeSumFFTDuplicates() {
        // Trigger FFT branch with heavily duplicated elements
        long[] a = new long[1000];
        Arrays.fill(a, 5L);
        int[] res = KSum.threeSum(a, 15L);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
        assertEquals(15L, a[res[0]] + a[res[1]] + a[res[2]]);

        // Not enough elements / impossible target
        res = KSum.threeSum(a, 16L);
        assertNull(res);
    }

    @Test
    public void testThreeSumFFTFallbackLargeN() {
        // Large N >= 1817 case to trigger and verify the modulo wrap-around fallback logic
        long[] a = new long[2000];
        for (int i = 0; i < 2000; i++) {
            a[i] = i % 10;
        }
        int[] res = KSum.threeSum(a, 27);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
        assertEquals(27, a[res[0]] + a[res[1]] + a[res[2]]);
    }

    @Test
    public void testThreeSumFFTLargeRU() {
        // R = 1,000,000, N = 1,000
        int N = 1000;
        long[] a = new long[N];
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < N; i++) {
            a[i] = rng.nextInt(1000001);
        }
        long target = a[5] + a[500] + a[995];
        int[] res = KSum.threeSum(a, target);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
        assertEquals(target, a[res[0]] + a[res[1]] + a[res[2]]);
    }

    @Test
    public void testThreeSumDynamicBranchingCoverage() {
        // Case 1: Naive is faster (e.g. N = 100, R = 50000). R logR factor is too large.
        int N1 = 100;
        long[] a1 = new long[N1];
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < N1; i++) {
            a1[i] = rng.nextInt(50000);
        }
        long target1 = a1[2] + a1[5] + a1[9];
        int[] res1 = KSum.threeSum(a1, target1);
        assertNotNull(res1);
        assertEquals(target1, a1[res1[0]] + a1[res1[1]] + a1[res1[2]]);

        // Case 2: FFT is faster (e.g. N = 5000, R = 10000)
        int N2 = 5000;
        long[] a2 = new long[N2];
        for (int i = 0; i < N2; i++) {
            a2[i] = rng.nextInt(10000);
        }
        long target2 = a2[100] + a2[2000] + a2[4000];
        int[] res2 = KSum.threeSum(a2, target2);
        assertNotNull(res2);
        assertEquals(target2, a2[res2[0]] + a2[res2[1]] + a2[res2[2]]);
    }

    @Test
    public void testThreeSumFFTExactMatch() {
        long[] a = new long[1000];
        for (int i = 0; i < 1000; i++) {
            a[i] = i % 5;
        }
        // distinct indices that sum to 7
        int[] res = KSum.threeSum(a, 7);
        assertNotNull(res);
        assertEquals(3, res.length);
        assertEquals(7, a[res[0]] + a[res[1]] + a[res[2]]);
        assertTrue(res[0] < res[1] && res[1] < res[2]);
    }

    @Test
    public void testThreeSumStress() {
        java.util.Random rng = new java.util.Random(1337);
        for (int iter = 0; iter < 500; iter++) {
            // Random N in [3, 2000]
            int n = rng.nextInt(1998) + 3;
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = rng.nextInt(2001) - 1000; // range [-1000, 1000]
            }

            long target;
            if (rng.nextBoolean()) {
                // Guaranteed solution
                int i = rng.nextInt(n);
                int j = rng.nextInt(n);
                while (j == i) j = rng.nextInt(n);
                int k = rng.nextInt(n);
                while (k == i || k == j) k = rng.nextInt(n);
                target = a[i] + a[j] + a[k];
            } else {
                // Random target
                target = rng.nextInt(6001) - 3000;
            }

            int[] resFFT = KSum.threeSum(a, target);
            int[] resNaive = KSum.threeSumRange(a, target, target + 1);

            if (resNaive == null) {
                assertNull(resFFT, "Mismatch at iteration " + iter + ": Naive says no solution, but FFT found a solution");
            } else {
                assertNotNull(resFFT, "Mismatch at iteration " + iter + ": Naive found a solution, but FFT returned null");
                assertEquals(3, resFFT.length);
                assertTrue(resFFT[0] < resFFT[1] && resFFT[1] < resFFT[2]);
                assertEquals(target, a[resFFT[0]] + a[resFFT[1]] + a[resFFT[2]], "FFT solution does not sum to target at iteration " + iter);
            }
        }
    }
}
