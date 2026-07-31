package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CountGraphTest {
    @Test
    public void testCountLabeledDAG() {
        long mod = 998244353L;
        int n = 6;
        long[] expected = {1, 1, 3, 25, 543, 29281, 3781503};
        long[] actual = CountGraph.countLabeledDAG(n, mod);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCountLabeledBiconnectedSingle() {
        long mod = 998244353L;
        // OEIS A013922: 0, 1, 1, 10, 238, 11368, 1014888
        org.junit.jupiter.api.Assertions.assertEquals(0, CountGraph.countLabeledBiconnectedSingle(1, mod));
        org.junit.jupiter.api.Assertions.assertEquals(1, CountGraph.countLabeledBiconnectedSingle(2, mod));
        org.junit.jupiter.api.Assertions.assertEquals(1, CountGraph.countLabeledBiconnectedSingle(3, mod));
        org.junit.jupiter.api.Assertions.assertEquals(10, CountGraph.countLabeledBiconnectedSingle(4, mod));
        org.junit.jupiter.api.Assertions.assertEquals(238, CountGraph.countLabeledBiconnectedSingle(5, mod));
        org.junit.jupiter.api.Assertions.assertEquals(11368, CountGraph.countLabeledBiconnectedSingle(6, mod));
        org.junit.jupiter.api.Assertions.assertEquals(1014888, CountGraph.countLabeledBiconnectedSingle(7, mod));
    }

    @Test
    public void testCountUnlabeled() {
        long mod = 998244353L;
        long[] expecteds = {
            1, 1, 2, 4, 11, 34, 156, 1044, 12346, 274668,
            12005168, 20753511, 380854347, 849549682, 209104826,
            721826955, 509875705, 766808107, 374514196, 553750285
        };
        for (int n = 0; n < expecteds.length; n++) {
            long actual = CountGraph.countUnlabeled(n, mod);
            assertEquals(expecteds[n], actual, "Failed for n = " + n);
        }
    }

    @Test
    public void testCountUnlabeledConnected() {
        long mod = 998244353L;
        int n = 10;
        long[] actual = CountGraph.countUnlabeledConnected(n, mod);
        // OEIS A001349 values: 1, 1, 2, 6, 21, 112, 853, 11117, 261080, 11716571
        // (with actual[0] = 0 per standard convention/implementation details)
        long[] expected = {0, 1, 1, 2, 6, 21, 112, 853, 11117, 261080, 11716571};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCountKnPartitionFixedK() {
        long mod = 998244353L;

        // Verify edge cases and small values
        // k = 0
        long[] res0 = CountGraph.countKnPartitionFixedK(0, 5, mod);
        assertArrayEquals(new long[] {1, 0, 0, 0, 0, 0}, res0);

        // k = 1
        // S(0, 1) = 0, S(n, 1) = 1 for n >= 1
        long[] res1 = CountGraph.countKnPartitionFixedK(1, 5, mod);
        assertArrayEquals(new long[] {0, 1, 1, 1, 1, 1}, res1);

        // k = 2
        // S(0, 2) = 0, S(1, 2) = 0, S(2, 2) = 1, S(3, 2) = 3, S(4, 2) = 7, S(5, 2) = 15
        long[] res2 = CountGraph.countKnPartitionFixedK(2, 5, mod);
        assertArrayEquals(new long[] {0, 0, 1, 3, 7, 15}, res2);

        // k > maxN
        long[] resBigK = CountGraph.countKnPartitionFixedK(6, 5, mod);
        assertArrayEquals(new long[] {0, 0, 0, 0, 0, 0}, resBigK);

        // k < 0
        long[] resNegK = CountGraph.countKnPartitionFixedK(-1, 5, mod);
        assertArrayEquals(new long[] {0, 0, 0, 0, 0, 0}, resNegK);

        // maxN < 0
        long[] resNegN = CountGraph.countKnPartitionFixedK(2, -1, mod);
        assertArrayEquals(new long[0], resNegN);

        // Stress check / consistency check with countKnPartition
        // countKnPartition(n, maxBlocks, mod) returns array of size maxBlocks+1, where S(n, k) is at index k.
        // countKnPartitionFixedK(k, maxN, mod) returns array of size maxN+1, where S(n, k) is at index n.
        int testMax = 50;
        long[][] S = new long[testMax + 1][testMax + 1];
        for (int n = 0; n <= testMax; n++) {
            long[] row = CountGraph.countKnPartition(n, testMax, mod);
            for (int k = 0; k <= testMax; k++) {
                S[n][k] = row[k];
            }
        }

        for (int k = 0; k <= testMax; k++) {
            long[] resFixedK = CountGraph.countKnPartitionFixedK(k, testMax, mod);
            assertEquals(testMax + 1, resFixedK.length);
            for (int n = 0; n <= testMax; n++) {
                assertEquals(S[n][k], resFixedK[n], "Mismatch at n=" + n + ", k=" + k);
            }
        }
    }
}
