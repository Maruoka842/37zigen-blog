package library.util.segtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class RangeMulRangeProductTest {
	private static final long MOD = 998244353;

	@Test
	public void testBasic() {
		RangeMulRangeProduct st = new RangeMulRangeProduct(5, MOD);
		st.build(new long[]{1, 2, 3, 4, 5});
		assertEquals(120, st.prod(0, 5));
		assertEquals(6, st.prod(0, 3));

		st.mul(1, 4, 2); // {1, 4, 6, 8, 5}
		assertEquals(1, st.get(0));
		assertEquals(4, st.get(1));
		assertEquals(6, st.get(2));
		assertEquals(8, st.get(3));
		assertEquals(5, st.get(4));
		assertEquals(960, st.prod(0, 5));

		st.mul(0, 3, 3); // {3, 12, 18, 8, 5}
		assertEquals(3, st.get(0));
		assertEquals(12, st.get(1));
		assertEquals(18, st.get(2));
		assertEquals(8, st.get(3));
		assertEquals(5, st.get(4));
		assertEquals(3 * 12 * 18 * 8 * 5 % MOD, st.prod(0, 5));
	}

	@Test
	public void testEdgeCases() {
		RangeMulRangeProduct st = new RangeMulRangeProduct(10, MOD);
		st.build(new long[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

		st.mul(0, 10, 0);
		assertEquals(0, st.prod(0, 10));
		assertEquals(0, st.get(5));

		st.mul(2, 5, 100); // 0 * 100 = 0
		assertEquals(0, st.get(3));

		RangeMulRangeProduct st2 = new RangeMulRangeProduct(10, MOD);
		st2.build(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		st2.mul(0, 10, 1);
		assertEquals(3628800, st2.prod(0, 10));

		assertEquals(1, st2.prod(5, 5));
	}

	@Test
	public void testRandom() {
		int N = 100;
		int Q = 1000;
		long[] a = new long[N];
		Random rand = new Random(42);
		for (int i = 0; i < N; i++) a[i] = rand.nextInt(1000) + 1;

		RangeMulRangeProduct st = new RangeMulRangeProduct(N, MOD);
		st.build(a);

		for (int q = 0; q < Q; q++) {
			int type = rand.nextInt(2);
			int l = rand.nextInt(N);
			int r = rand.nextInt(N - l + 1) + l;
			if (type == 0) {
				long x = rand.nextInt(1000);
				st.mul(l, r, x);
				for (int i = l; i < r; i++) {
					a[i] = a[i] * x % MOD;
				}
			} else {
				long expected = 1;
				for (int i = l; i < r; i++) {
					expected = expected * a[i] % MOD;
				}
				assertEquals(expected, st.prod(l, r), "Query " + q + " failed for range [" + l + ", " + r + ")");
			}
		}
	}

	@Test
	public void testLarge() {
		int N = 100000;
		RangeMulRangeProduct st = new RangeMulRangeProduct(N, MOD);
		st.mul(0, N, 2);
		// 2^N mod MOD
		long expected = 1;
		long base = 2;
		int exp = N;
		while (exp > 0) {
			if (exp % 2 == 1) expected = expected * base % MOD;
			base = base * base % MOD;
			exp /= 2;
		}
		assertEquals(expected, st.prod(0, N));
	}
}
