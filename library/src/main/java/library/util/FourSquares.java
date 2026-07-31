package library.util;

import java.math.BigInteger;
import java.util.Random;
import library.util.algebra.instance.impl.BigGaussInt;
import library.util.algebra.instance.impl.BigHurwitzInt;

/**
 * Pollack–Treviño アルゴリズムによる Lagrange の四平方和定理の実装。
 * 正整数 N に対し、X^2 + Y^2 + Z^2 + W^2 = N を満たす整数 X, Y, Z, W を求める。
 */
public class FourSquares {

	/**
	 * N = X^2 + Y^2 + Z^2 + W^2 となる [X, Y, Z, W] を返す。
	 * @param N 正整数
	 * @return 四平方の配列
	 */
	public static long[] solve(long N) {
		if (N < 0) throw new IllegalArgumentException("N must be non-negative");
		if (N == 0) return new long[] { 0, 0, 0, 0 };

		int e = 0;
		long n = N;
		while (n > 0 && n % 2 == 0) {
			n /= 2;
			e++;
		}

		long[] resN = solveOdd(n);
		BigInteger[] q = new BigInteger[4];
		for (int i = 0; i < 4; i++) q[i] = BigInteger.valueOf(resN[i]);

		// q = (1+i)^e * q_0
		// (1+i) * (X + Yi + Zj + Wk) = (X - Y) + (X + Y)i + (Z - W)j + (Z + W)k
		for (int i = 0; i < e; i++) {
			BigInteger X = q[0], Y = q[1], Z = q[2], W = q[3];
			q[0] = X.subtract(Y);
			q[1] = X.add(Y);
			q[2] = Z.subtract(W);
			q[3] = Z.add(W);
		}

		long[] res = new long[4];
		for (int i = 0; i < 4; i++) res[i] = Math.abs(q[i].longValueExact());
		return res;
	}

	private static long[] solveOdd(long n) {
		if (n == 1) return new long[] { 1, 0, 0, 0 };

		BigInteger bn = BigInteger.valueOf(n);
		double logn = Math.log(n);
		int T = (int) Math.floor(logn);
		if (T < 2) T = 2;

		BigInteger M = BigInteger.ONE;
		for (int p = 2; p <= T; p++) {
			if (MathUtils.isPrime(p)) {
				M = M.multiply(BigInteger.valueOf(p));
			}
		}

		Random rnd = new Random();
		BigInteger p;
		BigInteger s = null;

		while (true) {
			// Step 2: p = Mnk - 1
			// k is odd, k < n^5.
			// For implementation convenience, we can use a smaller k if it works.
			BigInteger k;
			if (n < 1000) {
				k = BigInteger.valueOf(rnd.nextInt(1000000) * 2 + 1);
			} else {
				// Random odd k up to n
				k = new BigInteger(bn.bitLength(), rnd).setBit(0);
			}

			p = M.multiply(bn).multiply(k).subtract(BigInteger.ONE);
			if (p.isProbablePrime(20)) {
				// Step 3: s^2 \equiv -1 (mod p)
				// Since p \equiv 1 (mod 4), s = u^((p-1)/4) mod p
				for (int attempt = 0; attempt < 100; attempt++) {
					BigInteger u = new BigInteger(p.bitLength(), rnd).mod(p);
					if (u.signum() == 0) continue;
					BigInteger exp = p.subtract(BigInteger.ONE).shiftRight(2);
					s = u.modPow(exp, p);
					if (s.multiply(s).add(BigInteger.ONE).mod(p).signum() == 0) {
						break;
					}
					s = null;
				}
				if (s != null) break;
			}
		}

		// Step 4: Gaussian GCD
		BigGaussInt gp = new BigGaussInt(p, BigInteger.ZERO);
		BigGaussInt gsi = new BigGaussInt(s, BigInteger.ONE);
		BigGaussInt g = BigGaussInt.gcd(gp, gsi);
		BigInteger A = g.a().abs();
		BigInteger B = g.b().abs();

		// Step 5: q = A + Bi + j
		BigHurwitzInt q = new BigHurwitzInt(A.shiftLeft(1), B.shiftLeft(1), BigInteger.valueOf(2), BigInteger.ZERO);

		// Step 6: rightGCD(n, q)
		BigHurwitzInt hn = BigHurwitzInt.fromLipschitz(bn, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
		BigHurwitzInt d = BigHurwitzInt.rightGCD(hn, q);

		// d should have norm n. Now find a unit u such that u*d is Lipschitz.
		for (BigHurwitzInt unit : BigHurwitzInt.units()) {
			BigHurwitzInt cand = unit.mul(d);
			if (cand.isLipschitz()) {
				BigInteger[] lipschitz = cand.toLipschitz();
				long[] res = new long[4];
				for (int i = 0; i < 4; i++) res[i] = lipschitz[i].longValueExact();
				return res;
			}
		}

		throw new RuntimeException("Failed to find Lipschitz integer");
	}
}
