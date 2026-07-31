package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class FourSquaresTest {

	@Test
	public void testSmall() {
		for (int i = 0; i <= 100; i++) {
			check(i);
		}
	}

	@Test
	public void testRandom() {
		Random rnd = new Random();
		for (int i = 0; i < 100; i++) {
			long n = Math.abs(rnd.nextLong()) % 1000000000000000L + 1;
			check(n);
		}
	}

	@Test
	public void testLarge() {
		long[] targets = {
			999999999999999989L, // a large prime
			1000000000000000000L,
			(1L << 60) - 1,
			(1L << 60)
		};
		for (long n : targets) {
			check(n);
		}
	}

	private void check(long n) {
		long[] res = FourSquares.solve(n);
		long sum = 0;
		for (long x : res) {
			sum += x * x;
		}
		assertEquals(n, sum, "Failed for n = " + n);
	}
}
