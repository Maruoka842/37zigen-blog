package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertionDPTest {

    private static final long MOD = 1000000007L;

    @Test
    public void testRelativeRankSmall() {
        // Test N=1 (0-length string/signs)
        assertEquals(1, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[0], null, MOD));

        // Test N=3, "<>" (P1 < P2 > P3) -> expect 2
        assertEquals(2, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[]{-1, 1}, null, MOD));

        // Test N=3, "><" (P1 > P2 < P3) -> expect 2
        assertEquals(2, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[]{1, -1}, null, MOD));

        // Test N=3, "<<" (P1 < P2 < P3) -> expect 1
        assertEquals(1, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[]{-1, -1}, null, MOD));

        // Test N=4, "<><" (P1 < P2 > P3 < P4) -> expect 5
        assertEquals(5, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[]{-1, 1, -1}, null, MOD));

        // Test N=4, "???" or unconstrained -> expect N! = 24
        assertEquals(24, InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(new int[]{0, 0, 0}, null, MOD));
    }

    @Test
    public void testRelativeRankAgainstBruteForce() {
        // Run stress test for N from 1 to 7
        for (int n = 1; n <= 7; n++) {
            List<int[]> perms = generatePermutations(n);
            int numPatterns = 1 << (n - 1);
            if (n == 1) numPatterns = 1;

            for (int mask = 0; mask < numPatterns; mask++) {
                int[] signs = new int[n - 1];
                for (int i = 0; i < n - 1; i++) {
                    if (((mask >> i) & 1) == 1) {
                        signs[i] = -1; // '<'
                    } else {
                        signs[i] = 1;  // '>'
                    }
                }

                long expected = 0;
                for (int[] p : perms) {
                    boolean ok = true;
                    for (int i = 0; i < n - 1; i++) {
                        if (signs[i] < 0 && p[i] > p[i + 1]) ok = false;
                        if (signs[i] > 0 && p[i] < p[i + 1]) ok = false;
                    }
                    if (ok) expected++;
                }

                long actual = InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(signs, null, MOD);
                assertEquals(expected, actual, "Failed for n=" + n + ", signs=" + Arrays.toString(signs));
            }
        }
    }

    @Test
    public void testWeightedRelativeRankAgainstBruteForce() {
        // We will define custom weights where weights[i][q] = i + q
        for (int n = 2; n <= 6; n++) {
            List<int[]> perms = generatePermutations(n);
            int[] signs = new int[n - 1];
            // alternating signs: <, >, <, >, ...
            for (int i = 0; i < n - 1; i++) {
                signs[i] = (i % 2 == 0) ? -1 : 1;
            }

            long[][] weights = new long[n][n + 2];
            for (int i = 0; i < n; i++) {
                for (int q = 0; q <= i + 1; q++) {
                    weights[i][q] = i + q + 3; // custom weights
                }
            }

            // Brute force calculation
            long expected = 0;
            for (int[] p : perms) {
                // Check if sign constraints are met
                boolean ok = true;
                for (int i = 0; i < n - 1; i++) {
                    if (signs[i] < 0 && p[i] > p[i + 1]) ok = false;
                    if (signs[i] > 0 && p[i] < p[i + 1]) ok = false;
                }
                if (!ok) continue;

                // Calculate product of weights
                long weightProduct = 1;
                // For each element d from 2 to n, find its relative rank q in prefix p[0...d-1]
                for (int d = 2; d <= n; d++) {
                    int val = p[d - 1];
                    int q = 1;
                    for (int r = 0; r < d - 1; r++) {
                        if (p[r] < val) {
                            q++;
                        }
                    }
                    // Weight index is d-1 (since we are inserting the d-th element at step d-1)
                    weightProduct = weightProduct * weights[d - 1][q] % MOD;
                }
                expected = (expected + weightProduct) % MOD;
            }

            long actual = InsertionDP.RelativeRank.solvePermutationWithSignConstraintsWeighted(signs, weights, MOD);
            assertEquals(expected, actual, "Failed weighted relative rank for n=" + n);
        }
    }

    @Test
    public void testCountPermutationsWithAscents() {
        // Without duplicates
        int[] A = {1, 2, 3};
        assertEquals(1, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(A, 0, null, null, MOD));
        assertEquals(4, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(A, 1, null, null, MOD));
        assertEquals(1, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(A, 2, null, null, MOD));

        // With duplicates: {1, 1, 2}
        int[] B = {1, 1, 2};
        assertEquals(2, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(B, 0, null, null, MOD));
        assertEquals(4, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(B, 1, null, null, MOD));
        assertEquals(0, InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(B, 2, null, null, MOD));
    }

    @Test
    public void testCountPermutationsWithAscentsAgainstBruteForce() {
        int[][] testArrays = {
            {1, 2, 3, 4},
            {1, 1, 2, 2},
            {1, 1, 1, 2},
            {2, 3, 1, 2},
            {5, 5, 5, 5}
        };

        for (int[] original : testArrays) {
            int n = original.length;
            List<int[]> perms = generateIndexPermutations(n);

            for (int k = 0; k <= n; k++) {
                long expected = 0;
                for (int[] p : perms) {
                    int ascents = 0;
                    for (int i = 0; i < n - 1; i++) {
                        if (original[p[i]] < original[p[i + 1]]) {
                            ascents++;
                        }
                    }
                    if (ascents == k) {
                        expected++;
                    }
                }

                long actual = InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(original, k, null, null, MOD);
                assertEquals(expected, actual, "Failed for array=" + Arrays.toString(original) + ", k=" + k);
            }
        }
    }

    @Test
    public void testWeightedGapInsertionAgainstBruteForce() {
        int[][] testArrays = {
            {1, 2, 3},
            {1, 1, 2},
            {2, 1, 3, 4}
        };

        for (int[] original : testArrays) {
            int n = original.length;
            int[] sorted = original.clone();
            Arrays.sort(sorted);

            long[][] keepWeights = new long[n][n + 1];
            long[][] increaseWeights = new long[n][n + 1];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= n; j++) {
                    keepWeights[i][j] = i + j + 2;
                    increaseWeights[i][j] = i * 2 + j + 1;
                }
            }

            for (int k = 0; k <= n; k++) {
                // Brute-force with weights tracking
                List<int[]> perms = generateIndexPermutations(n);
                long expected = 0;

                for (int[] p : perms) {
                    // Reconstruct insertion steps and weights
                    long permWeight = 1;
                    // We insert sorted[0]...sorted[i-1] chronologically
                    // For each element sorted[i], we locate where it is placed relative to others in p
                    // And determine if it increases the number of ascents.
                    for (int i = 0; i < n; i++) {
                        int prevAscents = countAscentsInSubperm(original, p, i);
                        int nextAscents = countAscentsInSubperm(original, p, i + 1);
                        if (nextAscents == prevAscents) {
                            permWeight = permWeight * keepWeights[i][prevAscents] % MOD;
                        } else if (nextAscents == prevAscents + 1) {
                            permWeight = permWeight * increaseWeights[i][prevAscents] % MOD;
                        } else {
                            permWeight = 0; // Invalid transition
                        }
                    }

                    // Check if final ascents match k
                    int finalAscents = countAscentsInSubperm(original, p, n);
                    if (finalAscents == k) {
                        expected = (expected + permWeight) % MOD;
                    }
                }

                long actual = InsertionDP.GapInsertion.countPermutationsWithAscentsWeighted(original, k, keepWeights, increaseWeights, MOD);
                assertEquals(expected, actual, "Failed weighted gap insertion for array=" + Arrays.toString(original) + ", k=" + k);
            }
        }
    }

    private int countAscentsInSubperm(int[] original, int[] p, int length) {
        // Filter p to only contain elements sorted[0...length-1]
        int[] sorted = original.clone();
        Arrays.sort(sorted);
        boolean[] kept = new boolean[original.length];
        boolean[] usedSorted = new boolean[original.length];
        for (int i = 0; i < length; i++) {
            int targetVal = sorted[i];
            for (int idx = 0; idx < original.length; idx++) {
                if (!usedSorted[idx] && original[idx] == targetVal) {
                    kept[idx] = true;
                    usedSorted[idx] = true;
                    break;
                }
            }
        }

        List<Integer> sub = new ArrayList<>();
        for (int x : p) {
            if (kept[x]) {
                sub.add(original[x]);
            }
        }

        int ascents = 0;
        for (int i = 0; i < sub.size() - 1; i++) {
            if (sub.get(i) < sub.get(i + 1)) {
                ascents++;
            }
        }
        return ascents;
    }

    @Test
    public void testComponentSmall() {
        // N = 2, L = 2. Values: {1, 2}.
        int[] A2 = {1, 2};
        assertEquals(2, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A2, 2, null, MOD));

        // N = 3, L = 4. Values: {1, 2, 3}.
        int[] A3 = {1, 2, 3};
        assertEquals(6, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A3, 4, null, MOD));

        // N = 3, L = 2.
        assertEquals(2, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A3, 2, null, MOD));
    }

    @Test
    public void testComponentAgainstBruteForce() {
        int[][] testArrays = {
            {1, 2, 3},
            {1, 3, 2},
            {1, 1, 3},
            {4, 1, 2, 3},
            {1, 5, 2, 5}
        };

        for (int[] original : testArrays) {
            int n = original.length;
            List<int[]> perms = generateIndexPermutations(n);

            for (int l = 0; l <= 20; l++) {
                long expected = 0;
                for (int[] p : perms) {
                    long sum = 0;
                    for (int i = 0; i < n - 1; i++) {
                        sum += Math.abs(original[p[i]] - original[p[i + 1]]);
                    }
                    if (sum <= l) {
                        expected++;
                    }
                }

                long actual = InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(original, l, null, MOD);
                assertEquals(expected, actual, "Failed for array=" + Arrays.toString(original) + ", l=" + l);
            }
        }
    }

    @Test
    public void testComponentEdgeCases() {
        // N = 1
        int[] A1 = {1};
        assertEquals(1, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A1, 0, null, MOD));
        assertEquals(1, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A1, 10, null, MOD));
        assertEquals(0, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(A1, -1, null, MOD));

        // Empty array
        assertEquals(1, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(new int[0], 0, null, MOD));
        assertEquals(0, InsertionDP.Component.countPermutationsWithAbsoluteDifferenceSumWeighted(new int[0], -5, null, MOD));
    }

    @Test
    public void testWeightedPermutationsWithoutAdjacentConsecutiveValues() {
        for (int n = 2; n <= 6; n++) {
            long[][] weights = new long[n + 1][3];
            long expectedMultiplier = 1;
            for (int i = 0; i <= n; i++) {
                long w = i + 2;
                weights[i][0] = w;
                weights[i][1] = w;
                weights[i][2] = w;
                if (i >= 2) {
                    expectedMultiplier = expectedMultiplier * w % MOD;
                }
            }

            // Brute force unweighted valid count
            List<int[]> perms = generatePermutations(n);
            long validCount = 0;
            for (int[] p : perms) {
                boolean valid = true;
                for (int i = 0; i < n - 1; i++) {
                    if (Math.abs(p[i] - p[i + 1]) == 1) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    validCount++;
                }
            }

            long expected = validCount * expectedMultiplier % MOD;

            long actual = InsertionDP.Component.countPermutationsWithoutAdjacentConsecutiveValuesWeighted(n, weights, MOD);
            assertEquals(expected, actual, "Failed weighted Permutations II for n=" + n);
        }
    }

    // Helper: generates all permutations of {1, 2, ..., n}
    private List<int[]> generatePermutations(int n) {
        List<int[]> result = new ArrayList<>();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i + 1;
        permute(arr, 0, result);
        return result;
    }

    // Helper: generates all index permutations of size n {0, 1, ..., n-1}
    private List<int[]> generateIndexPermutations(int n) {
        List<int[]> result = new ArrayList<>();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        permute(arr, 0, result);
        return result;
    }

    private void permute(int[] arr, int k, List<int[]> result) {
        if (k == arr.length) {
            result.add(arr.clone());
            return;
        }
        for (int i = k; i < arr.length; i++) {
            swap(arr, i, k);
            permute(arr, k + 1, result);
            swap(arr, i, k);
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    @Test
    public void testSolveWeightedSequenceInsertionDP() {
        // N = 3, adjacentMergeOnly = true
        // Unweighted (weights = null or all 1)
        long[] resAdjacent = InsertionDP.Component.solveWeightedSequenceInsertionDP(3, null, true, MOD);
        assertEquals(3 + 1, resAdjacent.length);
        assertEquals(6, resAdjacent[3]); // 3 sequences of size 1 (3! = 6)
        assertEquals(12, resAdjacent[2]); // 2 sequences
        assertEquals(6, resAdjacent[1]); // 1 sequence (3! = 6 permutations)

        // adjacentMergeOnly = false
        long[] resAllPairs = InsertionDP.Component.solveWeightedSequenceInsertionDP(3, null, false, MOD);
        assertEquals(3 + 1, resAllPairs.length);
        assertEquals(6, resAllPairs[3]); // 3 sequences
        assertEquals(12, resAllPairs[2]); // 2 sequences
        assertEquals(8, resAllPairs[1]); // 1 sequence (construction histories)

        // Stress test against recursive history generator with randomized weights
        for (int n = 1; n <= 5; n++) {
            long[][][] weights = new long[n][n + 1][3];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= n; j++) {
                    weights[i][j][0] = i + j + 2;
                    weights[i][j][1] = i * 2 + j + 1;
                    weights[i][j][2] = i + j + 5;
                }
            }

            for (boolean adjacentOnly : new boolean[]{true, false}) {
                long[] expected = new long[n + 1];
                long wInit = (weights != null && weights.length > 0 && weights[0] != null && weights[0].length > 0) ? weights[0][0][0] % MOD : 1L;
                generateHistories(n, 1, 1, wInit, weights, adjacentOnly, expected);

                long[] actual = InsertionDP.Component.solveWeightedSequenceInsertionDP(n, weights, adjacentOnly, MOD);
                assertArrayEquals(expected, actual, "Failed for n=" + n + ", adjacentMergeOnly=" + adjacentOnly);
            }
        }
    }

    private void generateHistories(int N, int i, int j, long currentWeight, long[][][] weights, boolean adjacentMergeOnly, long[] expectedCounts) {
        if (i == N) {
            expectedCounts[j] = (expectedCounts[j] + currentWeight) % MOD;
            return;
        }

        long w0 = (weights != null && weights.length > i && weights[i] != null && weights[i].length > j) ? weights[i][j][0] % MOD : 1;
        long w1 = (weights != null && weights.length > i && weights[i] != null && weights[i].length > j) ? weights[i][j][1] % MOD : 1;
        long w2 = (weights != null && weights.length > i && weights[i] != null && weights[i].length > j) ? weights[i][j][2] % MOD : 1;

        // Choice 0: new seq (j -> j+1)
        {
            long ways = j + 1;
            generateHistories(N, i + 1, j + 1, currentWeight * ways % MOD * w0 % MOD, weights, adjacentMergeOnly, expectedCounts);
        }

        // Choice 1: extend (j -> j)
        {
            long ways = 2L * j;
            generateHistories(N, i + 1, j, currentWeight * ways % MOD * w1 % MOD, weights, adjacentMergeOnly, expectedCounts);
        }

        // Choice 2: merge (j -> j-1)
        if (j >= 2) {
            long ways = adjacentMergeOnly ? (j - 1) : (long) j * (j - 1);
            generateHistories(N, i + 1, j - 1, currentWeight * ways % MOD * w2 % MOD, weights, adjacentMergeOnly, expectedCounts);
        }
    }
}
