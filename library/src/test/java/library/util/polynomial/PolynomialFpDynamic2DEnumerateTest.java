package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class PolynomialFpDynamic2DEnumerateTest {

	private long[] enumerateCoefficientsDirect(
		PolynomialFpDynamic2D poly2d, long[][] g, int alpha, int beta, int gamma, int delta, int N
	) {
		long[] ans = new long[N + 1];
		long[][] cur = poly2d.one();
		for (int i = 0; i <= N; i++) {
			int targetX = alpha * i + beta;
			int targetY = gamma * i + delta;
			if (targetX >= 0 && targetY >= 0 && targetX < cur.length && cur[targetX] != null && targetY < cur[targetX].length) {
				ans[i] = cur[targetX][targetY];
			} else {
				ans[i] = 0;
			}
			if (i < N) {
				cur = poly2d.mul(cur, g);
			}
		}
		return ans;
	}

	@Test
	public void testClass1Example1() {
		// g = (x+y)(y+3x)(xy+1) -> (x + y) * (3x + y) * (xy + 1)
		long mod = 998244353L;
		PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.of(mod);

		long[][] P1 = { { 0 }, { 1 } }; // x
		long[][] Q1 = { { 0, 1 } };    // y
		long[][] P2 = { { 0, 1 } };    // y
		long[][] Q2 = { { 0 }, { 3 } }; // 3x
		long[][] P3_xy = new long[2][2];
		P3_xy[1][1] = 1;               // xy
		long[][] Q3 = { { 1 } };       // 1

		long[][] G1 = poly2d.add(P1, Q1);
		long[][] G2 = poly2d.add(P2, Q2);
		long[][] G3 = poly2d.add(P3_xy, Q3);

		int alpha = 1;
		int beta = 1;
		int gamma = 1;
		int delta = 1;
		int N = 10;

		long[] ansFast = poly2d.enumerateCoefficientsClass1(G1, G2, G3, alpha, beta, gamma, delta, N);
		long[][] g = poly2d.mul(G1, poly2d.mul(G2, G3));
		long[] ansDirect = enumerateCoefficientsDirect(poly2d, g, alpha, beta, gamma, delta, N);

		assertArrayEquals(ansDirect, ansFast);
	}

	@Test
	public void testClass1RandomTrials() {
		long mod = 998244353L;
		PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.of(mod);
		Random rand = new Random(42);

		for (int trial = 0; trial < 15; trial++) {
			long c1 = rand.nextInt(5) + 1;
			long c2 = rand.nextInt(5) + 1;
			long[][] P1 = { { 0 }, { c1 } }; // c1 * x
			long[][] Q1 = { { 0, c2 } };    // c2 * y

			long[][] G1 = poly2d.add(P1, Q1);

			long c3 = rand.nextInt(5) + 1;
			long c4 = rand.nextInt(5) + 1;
			long[][] P2 = { { 0, c3 } };    // c3 * y
			long[][] Q2 = { { 0 }, { c4 } }; // c4 * x

			long[][] G2 = poly2d.add(P2, Q2);

			long c5 = rand.nextInt(5) + 1;
			long c6 = rand.nextInt(5) + 1;
			long[][] P3_xy = new long[2][2]; P3_xy[1][1] = c5; // c5 * xy
			long[][] Q3 = { { c6 } };       // c6

			long[][] G3 = poly2d.add(P3_xy, Q3);

			int alpha = 1;
			int beta = 1 + rand.nextInt(3);
			int gamma = 1;
			int delta = 1 + rand.nextInt(3);
			int N = 20;

			long[] ansFast = poly2d.enumerateCoefficientsClass1(G1, G2, G3, alpha, beta, gamma, delta, N);
			long[][] g = poly2d.mul(G1, poly2d.mul(G2, G3));
			long[] ansDirect = enumerateCoefficientsDirect(poly2d, g, alpha, beta, gamma, delta, N);

			assertArrayEquals(ansDirect, ansFast);
		}
	}


	@Test
	public void testClass1StressRobustness() {
		long mod = 998244353L;
		PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.of(mod);
		Random rand = new Random(12345);

		for (int trial = 0; trial < 100; trial++) {
			// Helper to generate a random distinct binomial
			long[][] G1 = generateRandomBinomial(poly2d, rand);
			long[][] G2 = generateRandomBinomial(poly2d, rand);
			long[][] G3 = generateRandomBinomial(poly2d, rand);

			int alpha = rand.nextInt(11) - 5; // [-5, 5]
			int beta = rand.nextInt(11) - 5;  // [-5, 5]
			int gamma = rand.nextInt(11) - 5; // [-5, 5]
			int delta = rand.nextInt(11) - 5; // [-5, 5]
			int N = rand.nextInt(30) + 1;    // [1, 100]

			long[] ansFast = poly2d.enumerateCoefficientsClass1(G1, G2, G3, alpha, beta, gamma, delta, N);
			long[][] g = poly2d.mul(G1, poly2d.mul(G2, G3));
			long[] ansDirect = enumerateCoefficientsDirect(poly2d, g, alpha, beta, gamma, delta, N);

			assertArrayEquals(ansDirect, ansFast, "Mismatch at trial " + trial + " with alpha=" + alpha + ", beta=" + beta + ", gamma=" + gamma + ", delta=" + delta + ", N=" + N);
		}
	}

	private long[][] generateRandomBinomial(PolynomialFpDynamic2D poly2d, Random rand) {
		int dx1 = rand.nextInt(11); // [0, 10]
		int dy1 = rand.nextInt(11); // [0, 10]
		int dx2 = rand.nextInt(11); // [0, 10]
		int dy2 = rand.nextInt(11); // [0, 10]
		while (dx1 == dx2 && dy1 == dy2) {
			dx2 = rand.nextInt(11);
			dy2 = rand.nextInt(11);
		}
		long c1 = rand.nextInt(20) + 1; // [1, 20]
		long c2 = rand.nextInt(20) + 1; // [1, 20]

		long[][] P = new long[dx1 + 1][dy1 + 1];
		P[dx1][dy1] = c1;
		long[][] Q = new long[dx2 + 1][dy2 + 1];
		Q[dx2][dy2] = c2;

		return poly2d.add(P, Q);
	}

	@Test
	public void testClass1IPlusTConvolutionPath() {
		long mod = 998244353L;
		PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.of(mod);
		Random rand = new Random(999);

		for (int trial = 0; trial < 1000; trial++) {
			long[][] G1 = generateRandomBinomial(poly2d, rand);
			long[][] G2 = generateRandomBinomial(poly2d, rand);
			long[][] G3 = generateRandomBinomial(poly2d, rand);

			int alpha = rand.nextInt(11) - 5;
			int beta = rand.nextInt(11) - 5;
			int gamma = rand.nextInt(11) - 5;
			int delta = rand.nextInt(11) - 5;
			int N = 10;

			long[] ansFast = poly2d.enumerateCoefficientsClass1(G1, G2, G3, alpha, beta, gamma, delta, N);
			long[][] g = poly2d.mul(G1, poly2d.mul(G2, G3));
			long[] ansDirect = enumerateCoefficientsDirect(poly2d, g, alpha, beta, gamma, delta, N);

			assertArrayEquals(ansDirect, ansFast);
		}
	}
}
