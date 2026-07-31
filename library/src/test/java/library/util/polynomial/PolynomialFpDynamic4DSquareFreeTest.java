package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic4D;

public class PolynomialFpDynamic4DSquareFreeTest {

	@Test
	public void contentRecursionAndMerge_y2_x1cubed_x2() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] f = poly.mul(pow(poly, y(poly), 2), poly.mul(pow(poly, linearX(poly, 1), 3), linearX(poly, 2)));
		assertReconstruction(poly, f);
		assertMultiplicityCount(poly.squareFreeDecomposition(f), Map.of(1, 1, 2, 1, 3, 1));
	}

	@Test
	public void mergeCorrectness_y2_x1squared() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] f = poly.mul(pow(poly, y(poly), 2), pow(poly, linearX(poly, 1), 2));
		PolynomialFpDynamic4D.Factor[] sqf = poly.squareFreeDecomposition(f);
		assertEquals(1, sqf.length);
		assertEquals(2, sqf[0].multiplicity);
		assertReconstruction(poly, f);
	}

	@Test
	public void nontrivialMultivariateFactor() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] xpyz = poly.add(x(poly), poly.add(y(poly), z(poly)));
		long[][][][] xpw = poly.add(x(poly), w(poly));
		long[][][][] f = poly.mul(pow(poly, xpyz, 2), pow(poly, xpw, 3));
		assertReconstruction(poly, f);
		assertMultiplicityCount(poly.squareFreeDecomposition(f), Map.of(2, 1, 3, 1));
	}

	@Test
	public void contentOnlyRepeatedFactor_y4_x1() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] f = poly.mul(pow(poly, y(poly), 4), linearX(poly, 1));
		assertReconstruction(poly, f);
		assertMultiplicityCount(poly.squareFreeDecomposition(f), Map.of(1, 1, 4, 1));
	}

	@Test
	public void irreducibleSquareFreeLikePolynomial_singleMultiplicity() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] f = poly.add(poly.add(pow(poly, x(poly), 2), pow(poly, y(poly), 2)),
				poly.add(pow(poly, z(poly), 2), poly.add(pow(poly, w(poly), 2), scalar(poly, 1))));
		PolynomialFpDynamic4D.Factor[] sqf = poly.squareFreeDecomposition(f);
		assertReconstruction(poly, f);
		assertTrue(sqf.length >= 1);
		for (var e : sqf) assertEquals(1, e.multiplicity);
	}

	@Test
	public void alreadySquareFreeButReducible_allMultiplicityOne() {
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(998244353L);
		long[][][][] f = poly.mul(poly.add(x(poly), y(poly)), poly.mul(poly.add(x(poly), z(poly)), poly.add(x(poly), w(poly))));
		PolynomialFpDynamic4D.Factor[] sqf = poly.squareFreeDecomposition(f);
		assertReconstruction(poly, f);
		for (var e : sqf) assertEquals(1, e.multiplicity);
	}

	@Test
	public void randomAffineLinearFactorsWithGroupingCheck() {
		long mod = 998244353L;
		PolynomialFpDynamic4D poly = new PolynomialFpDynamic4D(mod);
		Random rnd = new Random(20260521);
		for (int tc = 0; tc < 40; tc++) {
			long[][][][] l1 = affineLinear(poly, rnd);
			long[][][][] l2 = affineLinear(poly, rnd);
			long[][][][] l3 = affineLinear(poly, rnd);
			long[][][][] content = poly.mul(pow(poly, y(poly), 2), pow(poly, z(poly), 2));
			long[][][][] f = poly.mul(content, poly.mul(pow(poly, l1, 2), poly.mul(pow(poly, l2, 2), pow(poly, l3, 3))));
			PolynomialFpDynamic4D.Factor[] sqf = poly.squareFreeDecomposition(f);
			assertReconstruction(poly, f);
			Map<Integer, Integer> cnt = multiplicityCount(sqf);
			assertTrue(cnt.getOrDefault(2, 0) >= 1, "need multiplicity 2 group");
			assertTrue(cnt.getOrDefault(3, 0) >= 1, "need multiplicity 3 group");
		}
	}

	private static void assertReconstruction(PolynomialFpDynamic4D poly, long[][][][] f0) {
		long[][][][] f = poly.monic(f0);
		PolynomialFpDynamic4D.Factor[] sqf = poly.squareFreeDecomposition(f);
		long[][][][] rebuilt = poly.one();
		for (PolynomialFpDynamic4D.Factor e : sqf) rebuilt = poly.mul(rebuilt, pow(poly, e.factor, e.multiplicity));
		assertTrue(poly.equals(f, poly.monic(rebuilt)));
	}

	private static void assertMultiplicityCount(PolynomialFpDynamic4D.Factor[] sqf, Map<Integer, Integer> expected) {
		assertEquals(expected, multiplicityCount(sqf));
	}

	private static Map<Integer, Integer> multiplicityCount(PolynomialFpDynamic4D.Factor[] sqf) {
		Map<Integer, Integer> m = new HashMap<>();
		for (var e : sqf) m.merge(e.multiplicity, 1, Integer::sum);
		return m;
	}

	private static long[][][][] affineLinear(PolynomialFpDynamic4D poly, Random rnd) {
		long mod = poly.mod;
		long a = randNonZero(rnd, mod), b = randNonZero(rnd, mod), c = randNonZero(rnd, mod), d = randNonZero(rnd, mod);
		return poly.add(x(poly), poly.add(poly.mul(y(poly), scalar(poly, a)),
				poly.add(poly.mul(z(poly), scalar(poly, b)), poly.add(poly.mul(w(poly), scalar(poly, c)), scalar(poly, d)))));
	}

	private static long randNonZero(Random rnd, long mod) {
		long v = Math.floorMod(rnd.nextLong(), mod);
		return v == 0 ? 1 : v;
	}
	private static long[][][][] linearX(PolynomialFpDynamic4D poly, long c) { return poly.add(x(poly), scalar(poly, c)); }
	private static long[][][][] scalar(PolynomialFpDynamic4D poly, long c) { return new long[][][][] {{{{Math.floorMod(c, poly.mod)}}}}; }
	private static long[][][][] x(PolynomialFpDynamic4D poly) { return new long[][][][] {{{{0}}}, {{{1}}}}; }
	private static long[][][][] y(PolynomialFpDynamic4D poly) { return new long[][][][] {{{{0}, {1}}}}; }
	private static long[][][][] z(PolynomialFpDynamic4D poly) { return new long[][][][] {{{{0}, {0, 1}}}}; }
	private static long[][][][] w(PolynomialFpDynamic4D poly) { return new long[][][][] {{{{0, 1}}}}; }

	private static long[][][][] pow(PolynomialFpDynamic4D poly, long[][][][] a, int e) {
		long[][][][] r = poly.one(), b = a; int p = e;
		while (p > 0) {
			if ((p & 1) == 1) r = poly.mul(r, b);
			b = poly.mul(b, b); p >>= 1;
		}
		return r;
	}
}
