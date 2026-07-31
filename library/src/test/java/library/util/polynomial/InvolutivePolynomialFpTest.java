package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

public class InvolutivePolynomialFpTest {

	@Test
	public void testFftAndIfftAndMulWithMultipleModuli() {
		long[] moduli = {998244353L, 1000000007L, 998244353L * 2 + 1};
		Random rng = new Random(42);

		for (long mod : moduli) {
			InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

			// Test both N <= MUL_NAIVE_THRESHOLD and N > MUL_NAIVE_THRESHOLD
			int[] sizes = {4, 8, 16, 32, 64};
			for (int n : sizes) {
				long[] a = new long[n];
				long[] b = new long[n];
				for (int i = 0; i < n; i++) {
					a[i] = rng.nextInt((int) Math.min(mod, 100000));
					b[i] = rng.nextInt((int) Math.min(mod, 100000));
				}

				// Verify FFT/IFFT roundtrip restores original scaled elements (fft followed by ifft and dividing by N)
				long[] aFFT = a.clone();
				poly.fft(aFFT);
				long[] aIFFT = aFFT.clone();
				poly.ifft(aIFFT);

				library.util.Fp mo = new library.util.Fp(mod);
				long invN = mo.inv(n);
				for (int i = 0; i < n; i++) {
					long restored = aIFFT[i] * invN % mod;
					assertEquals(a[i] % mod, restored);
				}

				// Verify mul vs mulNaive vs mulFFT directly
				long[] mulCombined = poly.mul(a, b);
				long[] mulNaive = poly.mulNaive(a, b);
				long[] mulFFT = poly.mulFFT(a, b);
				assertArrayEquals(mulNaive, mulCombined, "mul output should produce correct results under mod " + mod + " for N=" + n);
				assertArrayEquals(mulFFT, mulCombined, "mul output should match mulFFT under mod " + mod + " for N=" + n);
			}
		}
	}

	@Test
	public void testEvenModulusFallback() {
		// Even modulus where 2 does not have modular inverse
		long mod = 1000000008L;
		InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

		// N = 32 (which is > MUL_NAIVE_THRESHOLD, but mod % 2 == 0 so it must use mulNaive instead of failing in mulFFT)
		int n = 32;
		long[] a = new long[n];
		long[] b = new long[n];
		Random rng = new Random(42);
		for (int i = 0; i < n; i++) {
			a[i] = rng.nextInt((int) (mod - 1));
			b[i] = rng.nextInt((int) (mod - 1));
		}

		long[] result = poly.mul(a, b);
		long[] expected = poly.mulNaive(a, b);
		assertArrayEquals(expected, result, "Multiplication under even modulus should fallback to naive and succeed");
	}

	@Test
	public void testAddAndInverse() {
		long mod = 998244353L;
		InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

		long[] a = {1, 2, 3, 4};
		long[] b = {5, 6, 7, 8};
		long[] sum = poly.add(a, b);
		long[] expectedSum = {6, 8, 10, 12};
		assertArrayEquals(expectedSum, sum);

		// test inverse: x * inverse(x) should be [1, 0, 0, 0] (unit element)
		long[] x = {10, 3, 5, 2};
		long[] invX = poly.inverse(x);
		long[] prod = poly.mul(x, invX);
		long[] expectedProd = {1, 0, 0, 0};
		assertArrayEquals(expectedProd, prod);
	}

	@Test
	public void testGeometricSeries() {
		long mod = 998244353L;
		InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

		// geometricSeries computes a^0 + a^1 + a^2
		long[] a = {2, 0, 0, 0}; // just a constant term
		long[] res = poly.geometricSeries(a, 3);
		// expected: 2^0 + 2^1 + 2^2 = 1 + 2 + 4 = 7
		long[] expected = {7, 0, 0, 0};
		assertArrayEquals(expected, res);
	}

	@Test
	public void testMulUnequalAndNonPowerOfTwoAndEmpty() {
		long mod = 998244353L;
		InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

		// 1. Unequal lengths
		long[] a1 = {1, 2, 3};
		long[] b1 = {4, 5};
		// Under the hood, length should be treated up to power-of-two = 4
		// a1 is treated as {1, 2, 3, 0}
		// b1 is treated as {4, 5, 0, 0}
		// Products:
		// i=0:
		//   j=0: (i^j=0): a1[0]*b1[0] = 4
		//   j=1: (i^j=1): a1[0]*b1[1] = 5
		// i=1:
		//   j=0: (i^j=1): a1[1]*b1[0] = 8
		//   j=1: (i^j=0): a1[1]*b1[1] = 10
		// i=2:
		//   j=0: (i^j=2): a1[2]*b1[0] = 12
		//   j=1: (i^j=3): a1[2]*b1[1] = 15
		// Expected sums:
		// c[0] = 4 + 10 = 14
		// c[1] = 5 + 8 = 13
		// c[2] = 12
		// c[3] = 15
		long[] expected1 = {14, 13, 12, 15};

		long[] resNaive1 = poly.mulNaive(a1, b1);
		long[] resFFT1 = poly.mulFFT(a1, b1);
		long[] resMul1 = poly.mul(a1, b1);

		assertArrayEquals(expected1, resNaive1);
		assertArrayEquals(expected1, resFFT1);
		assertArrayEquals(expected1, resMul1);

		// 2. Non-power-of-two, identical lengths
		long[] a2 = {1, 2, 3};
		long[] b2 = {4, 5, 6};
		// Treated as {1, 2, 3, 0} and {4, 5, 6, 0}
		// Products:
		// i=0:
		//   j=0 (0): 4
		//   j=1 (1): 5
		//   j=2 (2): 6
		// i=1:
		//   j=0 (1): 8
		//   j=1 (0): 10
		//   j=2 (3): 12
		// i=2:
		//   j=0 (2): 12
		//   j=1 (3): 15
		//   j=2 (0): 18
		// Expected sums:
		// c[0] = 4 + 10 + 18 = 32
		// c[1] = 5 + 8 = 13
		// c[2] = 6 + 12 = 18
		// c[3] = 12 + 15 = 27
		long[] expected2 = {32, 13, 18, 27};

		long[] resNaive2 = poly.mulNaive(a2, b2);
		long[] resFFT2 = poly.mulFFT(a2, b2);
		long[] resMul2 = poly.mul(a2, b2);

		assertArrayEquals(expected2, resNaive2);
		assertArrayEquals(expected2, resFFT2);
		assertArrayEquals(expected2, resMul2);

		// 3. One or both empty
		long[] empty = {};
		// Max length of empty and a1 is 3 -> p = 4. Since one operand is empty, all coefficients should be 0.
		long[] resEmpty1 = poly.mul(empty, a1);
		long[] resEmpty2 = poly.mul(b1, empty); // Max length of b1 and empty is 2 -> p = 2.
		long[] resEmpty3 = poly.mul(empty, empty);

		assertEquals(4, resEmpty1.length);
		assertArrayEquals(new long[]{0, 0, 0, 0}, resEmpty1);

		assertEquals(2, resEmpty2.length);
		assertArrayEquals(new long[]{0, 0}, resEmpty2);

		assertEquals(0, resEmpty3.length);

		long[] resEmptyNaive1 = poly.mulNaive(empty, a1);
		assertEquals(4, resEmptyNaive1.length);
		assertArrayEquals(new long[]{0, 0, 0, 0}, resEmptyNaive1);

		long[] resEmptyFFT1 = poly.mulFFT(empty, a1);
		assertEquals(4, resEmptyFFT1.length);
		assertArrayEquals(new long[]{0, 0, 0, 0}, resEmptyFFT1);
	}

	@Test
	public void testSubsetXorSum() {
		long mod = 998244353L;
		InvolutivePolynomialFp poly = new InvolutivePolynomialFp(mod);

		int[] a = {1, 2};
		// subsets: {}, {1}, {2}, {1, 2}
		// xor sums: 0, 1, 2, 3
		// counts of xor sums:
		// sum 0: 1 (from {})
		// sum 1: 1 (from {1})
		// sum 2: 1 (from {2})
		// sum 3: 1 (from {1, 2})
		long[] res = poly.subsetXorSum(a);
		assertEquals(1, res[0]);
		assertEquals(1, res[1]);
		assertEquals(1, res[2]);
		assertEquals(1, res[3]);
		// Other slots should be 0
		for (int i = 4; i < res.length; i++) {
			assertEquals(0, res[i]);
		}
	}
}
