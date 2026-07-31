package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CountGraphBridgelessTest {
    @Test
    public void testBridgelessSmall() {
        long mod = 998244353;
        // OEIS A095983: 0, 1, 0, 1, 10, 253, 11968, 1047613
        long[] expected = {0, 1, 0, 1, 10, 253, 11968, 1047613};
        long[] actual = CountGraph.countLabeledBridgeless(7, mod);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBridgelessSingle() {
        long mod = 998244353;
        long[] expected = {0, 1, 0, 1, 10, 253, 11968, 1047613};
        for (int n = 0; n <= 7; n++) {
            assertEquals(expected[n], CountGraph.countLabeledBridgelessSingle(n, mod), "Failed at n=" + n);
        }
    }

    @Test
    public void testConsistency() {
        long mod = 998244353;
        int N = 20;
        long[] batch = CountGraph.countLabeledBridgeless(N, mod);
        for (int n = 0; n <= N; n++) {
            assertEquals(batch[n], CountGraph.countLabeledBridgelessSingle(n, mod), "Inconsistency at n=" + n);
        }
    }
}
