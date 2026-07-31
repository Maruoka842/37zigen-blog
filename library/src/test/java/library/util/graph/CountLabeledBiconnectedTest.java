package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CountLabeledBiconnectedTest {
	@Test
	public void testCountLabeledBiconnected() {
		long mod = 998244353;
		// A013922: 0, 1, 1, 10, 238, 11368, 1014888, 166537616
		// Note: OEIS starts from n=1.
		// n=1: 0
		// n=2: 1
		// n=3: 1
		// n=4: 10
		// n=5: 238
		// n=6: 11368
		// n=7: 1014888
		// n=8: 166537616
		long[] expected = {0, 0, 1, 1, 10, 238, 11368, 1014888, 166537616};
		long[] actual = CountGraph.countLabeledBiconnected(8, mod);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testCountLabeledBiconnectedSingle() {
		long mod = 998244353;
		long[] expected = {0, 0, 1, 1, 10, 238, 11368, 1014888, 166537616};
		for (int n = 1; n <= 8; n++) {
			assertEquals(expected[n], CountGraph.countLabeledBiconnectedSingle(n, mod), "Failed at n=" + n);
		}
	}
}
