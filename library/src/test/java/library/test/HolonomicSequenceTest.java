package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;

public class HolonomicSequenceTest {

	/**
	 * 階乗 n! mod 998244353 の計算テスト。
	 * n! = \prod_{i=1}^n i = \prod_{i=0}^{n-1} (i+1)
	 * P(x) = x + 1 の累積積として計算できる。
	 */
	@Test
	public void testFactorial() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// a(n) = n * a(n-1)
		// M(i) = (i+1)
		long[] coeffs = {1, 1}; // P(x) = 1 + 1*x

		assertEquals(1, HolonomicSequence.prefixProduct(coeffs, 0, poly)); // 0! = 1
		assertEquals(1, HolonomicSequence.prefixProduct(coeffs, 1, poly)); // 1! = 1
		assertEquals(2, HolonomicSequence.prefixProduct(coeffs, 2, poly)); // 2! = 2
		assertEquals(6, HolonomicSequence.prefixProduct(coeffs, 3, poly)); // 3! = 6
		assertEquals(24, HolonomicSequence.prefixProduct(coeffs, 4, poly)); // 4! = 24
		assertEquals(120, HolonomicSequence.prefixProduct(coeffs, 5, poly)); // 5! = 120

		// 大きな n での計算（O(√n log n) の威力を確認）
		long n = 100000;
		long expected = 1;
		for (int i = 1; i <= n; i++) expected = expected * i % poly.mod;
		assertEquals(expected, HolonomicSequence.prefixProduct(coeffs, n, poly));
	}

	/**
	 * フィボナッチ数列 F_n mod 998244353 の計算テスト。
	 * 定数係数の線形漸化式は、多項式の次数が 0 の遷移行列を用いたホロノミック数列とみなせる。
	 */
	@Test
	public void testFibonacci() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// F_{n+1} = F_n + F_{n-1}
		// [F_{n+1}] = [1 1] [F_n    ]
		// [F_n    ]   [1 0] [F_{n-1}]
		// 遷移行列 M(i) = [[1, 1], [1, 0]] (定数行列)
		long[][][] matrixPoly = {
			{{1}, {1}}, // M[0][0]=1, M[0][1]=1
			{{1}, {0}}  // M[1][0]=1, M[1][1]=0
		};
		long[] initial = {1, 0}; // v_0 = [F_1, F_0]^T = [1, 0]^T

		// n = 1: M(0) v_0 = [1, 1]^T -> F_2 = 1, F_1 = 1
		assertArrayEquals(new long[]{1, 1}, HolonomicSequence.nthTerm(initial, matrixPoly, 1, poly));
		// n = 2: M(1)M(0) v_0 = [2, 1]^T -> F_3 = 2, F_2 = 1
		assertArrayEquals(new long[]{2, 1}, HolonomicSequence.nthTerm(initial, matrixPoly, 2, poly));
		// n = 3: F_4 = 3, F_3 = 2
		assertArrayEquals(new long[]{3, 2}, HolonomicSequence.nthTerm(initial, matrixPoly, 3, poly));

		// 大きな n での整合性確認
		long n = 1000;
		long f0 = 0, f1 = 1;
		for (int i = 0; i < n; i++) {
			long next = (f0 + f1) % poly.mod;
			f0 = f1;
			f1 = next;
		}
		// 結果は [F_{n+1}, F_n]
		assertArrayEquals(new long[]{f1, f0}, HolonomicSequence.nthTerm(initial, matrixPoly, n, poly));
	}

	/**
	 * 完全順列（攪乱順列） D_n mod 998244353 の計算テスト。
	 * 漸化式が n に依存する典型的なホロノミック数列。
	 */
	@Test
	public void testDerangements() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		// D_n = (n-1)(D_{n-1} + D_{n-2})
		// [D_{n+1}] = [n   n] [D_n    ]
		// [D_n    ]   [1   0] [D_{n-1}]
		// M(i) = [[i+1, i+1], [1, 0]] (i を用いた多項式行列)
		long[][][] matrixPoly = {
			{{1, 1}, {1, 1}}, // M[0][0] = 1+i, M[0][1] = 1+i
			{{1}, {0}}        // M[1][0] = 1,   M[1][1] = 0
		};
		long[] initial = {0, 1}; // v_0 = [D_1, D_0]^T = [0, 1]^T

		// n = 1: M(0)v_0 = [[1,1],[1,0]][0,1]^T = [1,0]^T -> D_2=1, D_1=0
		assertArrayEquals(new long[]{1, 0}, HolonomicSequence.nthTerm(initial, matrixPoly, 1, poly));
		// n = 2: M(1)[1,0]^T = [[2,2],[1,0]][1,0]^T = [2,1]^T -> D_3=2, D_2=1
		assertArrayEquals(new long[]{2, 1}, HolonomicSequence.nthTerm(initial, matrixPoly, 2, poly));
		// n = 3: D_4 = (4-1)(2+1) = 9
		assertArrayEquals(new long[]{9, 2}, HolonomicSequence.nthTerm(initial, matrixPoly, 3, poly));

		// 大きな n での整合性確認
		long n = 1000;
		long d0 = 1, d1 = 0;
		for (int i = 2; i <= n + 1; i++) {
			long next = (long)(i - 1) * (d0 + d1) % poly.mod;
			d0 = d1;
			d1 = next;
		}
		assertArrayEquals(new long[]{d1, d0}, HolonomicSequence.nthTerm(initial, matrixPoly, n, poly));
	}

	/**
	 * ランダムな P-recursive 数列を用いたテスト。
	 * 行列サイズ 5x5 以下、多項式の次数 3 次以下のケースで、O(n) の愚直計算と比較する。
	 */
	@Test
	public void testRandomRecurrence() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		Random rnd = new Random(42);
		int trials = 5;
		for (int t = 0; trials > t; t++) {
			int d = rnd.nextInt(5) + 1; // 行列サイズ 1..5
			int D = rnd.nextInt(4);     // 次数 0..3
			long[][][] matrixPoly = new long[d][d][D + 1];
			for (int i = 0; i < d; i++) {
				for (int j = 0; j < d; j++) {
					for (int k = 0; k <= D; k++) {
						matrixPoly[i][j][k] = rnd.nextInt((int) poly.mod);
					}
				}
			}
			long[] initial = new long[d];
			for (int i = 0; i < d; i++) initial[i] = rnd.nextInt((int) poly.mod);

			long n = 500 + rnd.nextInt(500); // BSGS が動く程度の n

			// 1. O(sqrt n log n) で計算
			long[] actual = HolonomicSequence.nthTerm(initial, matrixPoly, n, poly);

			// 2. O(n) で愚直に計算
			long[] expected = initial.clone();
			for (long i = 0; i < n; i++) {
				long[][] M = new long[d][d];
				for (int r = 0; r < d; r++) {
					for (int c = 0; c < d; c++) {
						M[r][c] = poly.evaluate(matrixPoly[r][c], i);
					}
				}
				long[] next = new long[d];
				for (int r = 0; r < d; r++) {
					for (int c = 0; c < d; c++) {
						next[r] = (next[r] + M[r][c] * expected[c]) % poly.mod;
					}
				}
				expected = next;
			}

			assertArrayEquals(expected, actual, "Trial " + t + " failed for n=" + n);
		}
	}

	/**
	 * exp(x) = \sum x^n/n! の第 n 項を計算する。
	 * 母関数 A(x) = exp(x) は A'(x) - A(x) = 0 を満たす。
	 * Q_1(x) = 1, Q_0(x) = -1
	 */
	@Test
	public void testDfiniteExp() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		long[][] Q = {
			{poly.mod - 1}, // Q_0(x) = -1
			{1}             // Q_1(x) = 1
		};
		long[] initial = {1}; // a_0 = 1

		for (int n = 0; n <= 10; n++) {
			long expected = 1;
			for (int i = 1; i <= n; i++) expected = expected * library.util.MathUtils.modInv(i, poly.mod) % poly.mod;
			assertEquals(expected, HolonomicSequence.nthTermOfDfinite(initial, Q, n, poly), "Failed at n=" + n);
		}

		long n = 1000;
		long expected = 1;
		for (int i = 1; i <= n; i++) expected = expected * library.util.MathUtils.modInv(i, poly.mod) % poly.mod;
		assertEquals(expected, HolonomicSequence.nthTermOfDfinite(initial, Q, n, poly));
	}

	/**
	 * cos(x) = \sum (-1)^n x^{2n}/(2n)! の第 n 項を計算する。
	 * 母関数 A(x) = cos(x) は A''(x) + A(x) = 0 を満たす。
	 * Q_2(x) = 1, Q_1(x) = 0, Q_0(x) = 1
	 */
	@Test
	public void testDfiniteCos() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		long[][] Q = {
			{1}, // Q_0(x) = 1
			{0}, // Q_1(x) = 0
			{1}  // Q_2(x) = 1
		};
		long[] initial = {1, 0}; // a_0 = 1, a_1 = 0

		for (int n = 0; n <= 10; n++) {
			long expected;
			if (n % 2 == 1) {
				expected = 0;
			} else {
				int k = n / 2;
				expected = 1;
				for (int i = 1; i <= 2 * k; i++) expected = expected * library.util.MathUtils.modInv(i, poly.mod) % poly.mod;
				if (k % 2 == 1) expected = (poly.mod - expected) % poly.mod;
			}
			assertEquals(expected, HolonomicSequence.nthTermOfDfinite(initial, Q, n, poly), "Failed at n=" + n);
		}
	}

	/**
	 * カタラン数 C_n = \frac{1}{n+1} \binom{2n}{n} の第 n 項を計算する。
	 * 母関数 A(x) = \sum C_n x^n は (1-4x)A'(x) + (1-2x)A(x) = C_0 / x? いや
	 * カタラン数の母関数は A(x) = \frac{1-\sqrt{1-4x}}{2x} で、
	 * x(1-4x)A'(x) + (1-2x)A(x) - 1 = 0 を満たす。
	 * 斉次にするため微分すると、x(1-4x)A''(x) + (2-6x)A'(x) - 2A(x) = 0.
	 * Q_2(x) = x(1-4x) = x - 4x^2, Q_1(x) = 2 - 6x, Q_0(x) = -2
	 */
	@Test
	public void testDfiniteCatalan() {
		PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
		long[][] Q = {
			{poly.mod - 2},       // Q_0(x) = -2
			{2, poly.mod - 10},   // Q_1(x) = 2 - 10x
			{0, 1, poly.mod - 4}  // Q_2(x) = x - 4x^2
		};
		long[] initial = {1, 1}; // C_0 = 1, C_1 = 1

		long[] c = new long[21];
		c[0] = 1;
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j <= i; j++) {
				c[i + 1] = (c[i + 1] + c[j] * c[i - j]) % poly.mod;
			}
		}

		for (int n = 0; n <= 20; n++) {
			assertEquals(c[n], HolonomicSequence.nthTermOfDfinite(initial, Q, n, poly), "Failed at n=" + n);
		}
	}
}
