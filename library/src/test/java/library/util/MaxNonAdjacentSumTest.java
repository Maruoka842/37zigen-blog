package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

public class MaxNonAdjacentSumTest {

    private long[] brute(long[] as, long inf) {
        int n = as.length;
        int maxK = (n + 1) / 2;
        long[][] dp = new long[n + 1][maxK + 1];
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], -inf);
        }
        dp[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int k = 0; k <= maxK; k++) {
                // don't pick as[i-1]
                dp[i][k] = Math.max(dp[i][k], dp[i - 1][k]);
                // pick as[i-1]
                if (k > 0) {
                    long prev = (i == 1) ? (k == 1 ? 0 : -inf) : dp[i - 2][k - 1];
                    if (prev != -inf) {
                        dp[i][k] = Math.max(dp[i][k], prev + as[i - 1]);
                    }
                }
            }
        }
        return dp[n];
    }

    @Test
    public void testSmallExhaustive() {
        for (int n = 0; n <= 6; n++) {
            for (int p = 0; p < Math.pow(8, n); p++) {
                long[] as = new long[n];
                int tempP = p;
                for (int i = 0; i < n; i++) {
                    as[i] = (tempP % 8) - 4;
                    tempP /= 8;
                }
                long[] expected = brute(as, 1001001001001001001L);
                long[] actual = MaxWeightIndependentSetOnPath.solve(as);
                assertArrayEquals(expected, actual, "Failed for n=" + n + " as=" + Arrays.toString(as));

                // Verify concavity: actual[k+1] - actual[k] >= actual[k+2] - actual[k+1]
                for (int k = 0; k + 2 < actual.length; k++) {
                    assertTrue(actual[k + 1] - actual[k] >= actual[k + 2] - actual[k + 1],
                        "Concavity failed for n=" + n + " as=" + Arrays.toString(as) + " at k=" + k);
                }
            }
        }
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int caseId = 0; caseId < 100; caseId++) {
            int n = 1 + rnd.nextInt(100);
            long[] as = new long[n];
            for (int i = 0; i < n; i++) {
                as[i] = rnd.nextInt();
            }
            long[] expected = brute(as, 1001001001001001001L);
            long[] actual = MaxWeightIndependentSetOnPath.solve(as);
            assertArrayEquals(expected, actual);

            for (int k = 0; k + 2 < actual.length; k++) {
                assertTrue(actual[k + 1] - actual[k] >= actual[k + 2] - actual[k + 1]);
            }
        }
    }

    @Test
    public void testEmpty() {
        long[] as = new long[0];
        long[] actual = MaxWeightIndependentSetOnPath.solve(as);
        assertArrayEquals(new long[]{0}, actual);
    }

    @Test
    public void testSingle() {
        long[] as = new long[]{10};
        long[] actual = MaxWeightIndependentSetOnPath.solve(as);
        assertArrayEquals(new long[]{0, 10}, actual);

        as = new long[]{-5};
        actual = MaxWeightIndependentSetOnPath.solve(as);
        assertArrayEquals(new long[]{0, -5}, actual);
    }
}
