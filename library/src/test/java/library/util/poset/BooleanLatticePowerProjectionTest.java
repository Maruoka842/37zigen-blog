package library.util.poset;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import library.util.Fp;
import library.util.MathUtils;

public class BooleanLatticePowerProjectionTest {

	public static long[] transposedSubsetConvolutionNaive(long[] s, long[] x, long mod) {
		int n = x.length;
		long[] y = new long[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if ((i & j) == i) { // j is a superset of i
					int diff = j ^ i; // j \ i
					y[i] = (y[i] + x[j] * s[diff]) % mod;
				}
			}
		}
		return y;
	}

	public static long[] powerProjectionOfSpsNaive(long[] wt, long[] s, int M, long mod) {
		int n = s.length;
		long[] res = new long[M];
		long[] current = new long[n];
		current[0] = 1; // a^0 = {1, 0, 0, ..., 0}
		for (int k = 0; k < M; k++) {
			long sum = 0;
			for (int i = 0; i < n; i++) {
				sum = (sum + wt[i] * current[i]) % mod;
			}
			res[k] = sum;
			if (k + 1 < M) {
				current = BooleanLattice.mulNaive(current, s, mod);
			}
		}
		return res;
	}

	public static long[] powerProjectionOfSpsEgfNaive(long[] wt, long[] s, long mod) {
		int n = s.length;
		int N = MathUtils.floorLog2(n);
		int M = N + 1;
		long[] pProj = powerProjectionOfSpsNaive(wt, s, M, mod);
		Fp fp = new Fp(mod);
		long[] res = new long[M];
		for (int k = 0; k < M; k++) {
			res[k] = pProj[k] * fp.ifac(k) % mod;
		}
		return res;
	}

	@Test
	public void testTransposedSubsetConvolution() {
		long mod = 998244353;
		int n = 3; // array size 8
		Random rnd = new Random(12345);
		long[] s = new long[1 << n];
		long[] x = new long[1 << n];
		for (int i = 0; i < (1 << n); i++) {
			s[i] = rnd.nextInt((int) mod);
			x[i] = rnd.nextInt((int) mod);
		}

		long[] resNew = BooleanLattice.transposedSubsetConvolution(s, x, mod);
		long[] resNaive = transposedSubsetConvolutionNaive(s, x, mod);

		assertArrayEquals(resNaive, resNew);
	}

	@Test
	public void testPowerProjectionOfSpsEgf() {
		long mod = 998244353;
		int n = 3; // array size 8
		Random rnd = new Random(23456);
		long[] s = new long[1 << n];
		long[] wt = new long[1 << n];
		for (int i = 0; i < (1 << n); i++) {
			s[i] = rnd.nextInt((int) mod);
			wt[i] = rnd.nextInt((int) mod);
		}
		s[0] = 0; // s[0] must be 0 for EGF power projection

		long[] resNew = BooleanLattice.powerProjectionOfSpsEgf(wt, s, mod);
		long[] resNaive = powerProjectionOfSpsEgfNaive(wt, s, mod);

		assertArrayEquals(resNaive, resNew);
	}

	@Test
	public void testPowerProjectionOfSps() {
		long mod = 998244353;
		int n = 3; // array size 8
		int M = 5;
		Random rnd = new Random(34567);
		long[] s = new long[1 << n];
		long[] wt = new long[1 << n];
		for (int i = 0; i < (1 << n); i++) {
			s[i] = rnd.nextInt((int) mod);
			wt[i] = rnd.nextInt((int) mod);
		}

		long[] resNew = BooleanLattice.powerProjectionOfSps(wt, s, M, mod);
		long[] resNaive = powerProjectionOfSpsNaive(wt, s, M, mod);

		assertArrayEquals(resNaive, resNew);
	}

}
