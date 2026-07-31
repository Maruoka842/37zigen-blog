package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;

public class MinPlusTest {

	private final Random rnd = new Random(42);

	@Test
	public void testConvolveConvexConvexBasic() {
		// Hand-crafted basic convex arrays
		long[] a = {0, 1, 3, 6}; // slopes: 1, 2, 3
		long[] b = {0, 2, 5, 9}; // slopes: 2, 3, 4

		long[] expected = naiveConvolve(a, b);
		long[] actual = MinPlus.convolveConvexConvex(a, b);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testConvolveConvexConvexWithLeadingTrailingInf() {
		long INF = Long.MAX_VALUE;
		long[] a = {INF, INF, 0, 1, 3, 6, INF};
		long[] b = {INF, 0, 2, 5, 9, INF, INF};

		long[] expected = naiveConvolve(a, b);
		long[] actual = MinPlus.convolveConvexConvex(a, b);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testConvolveConvexConvexAllInf() {
		long INF = Long.MAX_VALUE;
		long[] a = {INF, INF, INF};
		long[] b = {INF, INF};

		long[] expected = naiveConvolve(a, b);
		long[] actual = MinPlus.convolveConvexConvex(a, b);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testConvolveConvexConvexSingleElement() {
		long[] a = {5};
		long[] b = {3};

		long[] expected = naiveConvolve(a, b);
		long[] actual = MinPlus.convolveConvexConvex(a, b);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testConvolveConvexConvexSingleElementWithInf() {
		long INF = Long.MAX_VALUE;
		long[] a = {INF, 5, INF};
		long[] b = {INF, INF, 3, INF};

		long[] expected = naiveConvolve(a, b);
		long[] actual = MinPlus.convolveConvexConvex(a, b);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testConvolveConvexConvexRandomStress() {
		for (int trial = 0; trial < 2000; trial++) {
			int lenA = rnd.nextInt(30) + 1;
			int leadA = rnd.nextInt(5);
			int trailA = rnd.nextInt(5);

			int lenB = rnd.nextInt(30) + 1;
			int leadB = rnd.nextInt(5);
			int trailB = rnd.nextInt(5);

			long[] a = generateRandomConvex(lenA, leadA, trailA);
			long[] b = generateRandomConvex(lenB, leadB, trailB);

			long[] expected = naiveConvolve(a, b);
			long[] actual = MinPlus.convolveConvexConvex(a, b);

			assertArrayEquals(expected, actual, "Failed on trial " + trial + "\na: " + Arrays.toString(a) + "\nb: " + Arrays.toString(b));
		}
	}

	private long[] generateRandomConvex(int len, int numInfLeading, int numInfTrailing) {
		long[] res = new long[len + numInfLeading + numInfTrailing];
		Arrays.fill(res, Long.MAX_VALUE);
		if (len == 0) return res;
		long val = rnd.nextInt(100);
		long slope = rnd.nextInt(10) - 5;
		res[numInfLeading] = val;
		for (int i = 1; i < len; i++) {
			slope += rnd.nextInt(5) + 1; // strictly increasing slopes
			val += slope;
			res[numInfLeading + i] = val;
		}
		return res;
	}

	private long[] naiveConvolve(long[] a, long[] b) {
		long[] c = new long[a.length + b.length - 1];
		Arrays.fill(c, Long.MAX_VALUE);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				if (a[i] == Long.MAX_VALUE || b[j] == Long.MAX_VALUE) continue;
				c[i + j] = Math.min(c[i + j], a[i] + b[j]);
			}
		}
		return c;
	}
}
