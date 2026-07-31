package library.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class ArrayStatisticsTest {

    @Test
    public void testBasic() {
        long[] a = {2, 1, 3, 0};
        // query: L, R, C, D
        long[][] queries = {
            {0, 4, 2, 3}, // L=0, R=4, C=2, D=3
            {1, 3, 5, 2}, // L=1, R=3, C=5, D=2
            {2, 2, 1, 1}, // L=2, R=2 (empty)
            {0, 2, 0, 1}  // L=0, R=2, C=0, D=1
        };

        long[] expected = solveNaive(a, queries);
        long[] actual = ArrayStatistics.countPrefixMinWithThresholdAndIndexShift(a, queries);

        assertArrayEquals(expected, actual);

        // Test with List overload
        List<long[]> qList = new ArrayList<>();
        for (long[] q : queries) {
            qList.add(q);
        }
        long[] actualList = ArrayStatistics.countPrefixMinWithThresholdAndIndexShift(a, qList);
        assertArrayEquals(expected, actualList);
    }

    @Test
    public void testTargetedCases() {
        long[] a = {10, 5, 2, 8, 3, 1, 9};
        // Queries targeting specific active/inactive C and boundaries
        long[][] queries = {
            {0, 7, 0, 0},   // C=0 is active, D=0
            {0, 7, 100, 0}, // C=100 is inactive (since max of array is 10), D=0
            {0, 3, 4, 5},   // C=4 is active, J is 1 (since a[1]=5 > 4, but a[2]=2 <= 4, so minSeg.firstLeqPos(0, 4) is 2, Math.min(3, 2) = 2)
            {1, 6, 4, 3},   // C=4 is active, firstLeqPos(1, 4) is 2 (since a[2]=2 <= 4)
            {3, 7, 5, 12},  // Large D, potentially no elements satisfy D <= i + min
            {0, 7, 2, 3},   // Boundary checks
            {2, 5, 2, 4}
        };

        long[] expected = solveNaive(a, queries);
        long[] actual = ArrayStatistics.countPrefixMinWithThresholdAndIndexShift(a, queries);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testStress() {
        Random rand = new Random(42);
        int numIterations = 1000;
        for (int iter = 0; iter < numIterations; iter++) {
            int n = rand.nextInt(100) + 1;
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = rand.nextInt(n);
            }

            int q = rand.nextInt(50) + 1;
            long[][] queries = new long[q][4];
            List<long[]> qList = new ArrayList<>();
            for (int i = 0; i < q; i++) {
                int L = rand.nextInt(n);
                int R = rand.nextInt(n - L + 1) + L;
                long C = rand.nextInt(n + 10);
                long D = rand.nextInt(2 * n + 10);
                queries[i] = new long[]{L, R, C, D};
                qList.add(queries[i]);
            }

            long[] expected = solveNaive(a, queries);
            long[] actualArray = ArrayStatistics.countPrefixMinWithThresholdAndIndexShift(a, queries);
            long[] actualList = ArrayStatistics.countPrefixMinWithThresholdAndIndexShift(a, qList);

            assertArrayEquals(expected, actualArray, "Mismatch in iteration " + iter);
            assertArrayEquals(expected, actualList, "Mismatch in list version iteration " + iter);
        }
    }

    private long[] solveNaive(long[] a, long[][] queries) {
        int Q = queries.length;
        long[] ans = new long[Q];
        for (int q = 0; q < Q; q++) {
            int L = (int) queries[q][0];
            int R = (int) queries[q][1];
            long C = queries[q][2];
            long D = queries[q][3];
            if (L >= R) {
                ans[q] = 0;
                continue;
            }
            long count = 0;
            for (int i = L; i < R; i++) {
                long minVal = C;
                for (int j = L; j <= i; j++) {
                    if (a[j] < minVal) {
                        minVal = a[j];
                    }
                }
                if (D <= i + minVal) {
                    count++;
                }
            }
            ans[q] = count;
        }
        return ans;
    }
}
