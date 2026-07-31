package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialFpDynamic2D;
import library.util.polynomial.PolynomialFpDynamic3D;
import library.util.polynomial.PolynomialFpDynamic4D;
import library.util.polynomial.PolynomialLong2D;

public class PolynomialFpDynamicMultivariateFactorTest {
	private static final PolynomialFpDynamic P1 = PolynomialFpDynamic.MOD998244353;
	private static final PolynomialFpDynamic2D P2 = PolynomialFpDynamic2D.MOD998244353;
	private static final PolynomialFpDynamic3D P3 = PolynomialFpDynamic3D.MOD998244353;
	private static final PolynomialFpDynamic4D P4 = new PolynomialFpDynamic4D(P1);

	@Test
	public void test4DNeverComeBack() {
		long[][][][] array = {
			    {
			        {
			            {0, 0, 1},
			            {0, 0, 0},
			            {1, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        },
			        {
			            {0, 0, 998244352},
			            {0, 0, 0},
			            {0, 0, 0}
			        }
			    },
			    {
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        }
			    },
			    {
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {998244352, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        },
			        {
			            {1, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        }
			    },
			    {
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 2, 0},
			            {0, 0, 0}
			        },
			        {
			            {0, 0, 0},
			            {0, 0, 0},
			            {0, 0, 0}
			        }
			    }
			};
		P4.factor(array);//squareFreePrimitiveで詰まっている
	}
	
	
	/** 2変数Wang/EEZ因数分解のランダム積復元テスト。未テスト。計算量: O(T * Factor2D) */
	@Test
	public void test2DFactorRandomProducts() {
		Random rnd = new Random(2026051901L);
		for (int t = 0; t < 30; t++) {
			long[][] f = P2.one();
			int cnt = 2 + rnd.nextInt(3);
			for (int i = 0; i < cnt; i++) f = P2.mul(f, randomLinear2D(rnd));
			if ((t & 3) == 0) f = P2.mul(f, randomLinear2D(rnd));
			assertFactorProduct2D(f);
		}
	}

	/** 3変数Wang/EEZ因数分解のランダム積復元テスト。未テスト。計算量: O(T * Factor3D) */
	@Test
	public void test3DFactorRandomProducts() {
		Random rnd = new Random(2026051902L);
		for (int t = 0; t < 20; t++) {
			long[][][] f = P3.one();
			int cnt = 2 + rnd.nextInt(3);
			for (int i = 0; i < cnt; i++) f = P3.mul(f, randomLinear3D(rnd));
			if ((t & 3) == 0) f = P3.mul(f, randomLinear3D(rnd));
			assertFactorProduct3D(f);
		}
	}

	/** 4変数Wang/EEZ因数分解のランダム積復元テスト。未テスト。計算量: O(T * Factor4D) */
	@Test
	public void test4DFactorRandomProducts() {
		Random rnd = new Random(2026051903L);
		for (int t = 0; t < 12; t++) {
			long[][][][] f = P4.one();
			int cnt = 2 + rnd.nextInt(2);
			for (int i = 0; i < cnt; i++) f = P4.mul(f, randomLinear4D(rnd));
			assertFactorProduct4D(f);
		}
	}

	/** 2変数Wang Trickの主係数モニック化と原始部分復元テスト。未テスト。計算量: O(Factor2D + Lift2D) */
	@Test
	public void test2DWangTrickLeadingCoefficient() {
		long[][] a = new long[][] { { 1 }, { 1, 1 } }; // (y + 1)x + 1
		long[][] b = new long[][] { { 3 }, { 2, 1 } }; // (y + 2)x + 3
		long[][] f = P2.mul(a, b);
		PolynomialFpDynamic2D.WangTransform2D transformed = P2.wangTrickForward(f, 2);
		assertTrue(P1.equals(new long[] { 2, 3, 1 }, transformed.leadingCoeff()));
		assertTrue(P2.equals(new long[][] { { 6, 9, 3 }, { 5, 4 }, { 1 } }, transformed.monicPolynomial()));

		PolynomialFpDynamic2D.Factor[] factors = P2.factorByWang(f);
		long[][] prod = P2.one();
		for (PolynomialFpDynamic2D.Factor e : factors) {
			for (int i = 0; i < e.multiplicity; i++) prod = P2.mul(prod, e.factor);
		}
		assertTrue(P2.equals(P2.monic(f), P2.monic(prod)));
	}

	/** 2変数Wang Trickのprimitiveランダム積復元テスト。未テスト。計算量: O(T * (Factor1D + Lift2D + Mul2D)) */
	@Test
	public void test2DWangTrickRandomPrimitiveProducts() {
		Random rnd = new Random(2026052803L);
		int tested = 0;
		for (int trial = 0; tested < 50 && trial < 200; trial++) {
			long[][] f = P2.one();
			int cnt = 2 + rnd.nextInt(4);
			for (int i = 0; i < cnt; i++) f = P2.mul(f, randomPrimitiveLinear2D(rnd));
			if (P1.deg(P2.contentX(f)) != 0) continue;
			PolynomialFpDynamic2D.Factor[] factors = P2.factorByWang(f);
			long[][] prod = P2.one();
			for (PolynomialFpDynamic2D.Factor e : factors) {
				for (int i = 0; i < e.multiplicity; i++) prod = P2.mul(prod, e.factor);
			}
			assertTrue(P2.equals(P2.monic(f), P2.monic(prod)), "Wang random product differs at trial " + trial);
			tested++;
		}
		assertEquals(50, tested);
	}
	
	@Test
	public void test2DFactor() {
		var P2=PolynomialFpDynamic2D.MOD998244353;
		long[][]f=new long[2][2];
		f[1][0]=1;
		f[1][1]=1;
		//x+xy=x(1+y)
		var factors = P2.factor(f);
		System.out.println(factors.factors().length);
		PolynomialLong2D.printPolyAsExpr("", factors.factors()[0].factor);
		assertEquals(2, factors.factors().length);
	}
	
	@Test
	public void test3DFactor() {
		long[][][]f=new long[1][2][2];
		f[0][1][0]=1;
		f[0][1][1]=1;
		//y+yz=y(1+z)
		var factors = P3.factor(f);
		assertEquals(2, factors.factors().length);
	}
	
	@Test
	public void test4DFactor() {
		long[][][][]f=new long[1][1][2][2];
		f[0][0][1][0]=1;
		f[0][0][1][1]=1;
		//z+zw=z(1+w)
		var factors = P4.factor(f);
		assertEquals(2, factors.factors().length);
	}
	
	@Test
	public void test3DWang() {
		//z^2 - x^2 - x^2 z^2 + x^2 y^2 + x^4
		long[][][] poly = new long[5][3][3];
		long mod=998244353;
		// 各項の係数を代入
		poly[4][0][0] = 1;   // x^4
		poly[2][2][0] = 1;   // x^2 * y^2
		poly[2][0][2] = mod-1;  // -x^2 * z^2
		poly[2][0][0] = mod-1;  // -x^2
		poly[0][0][2] = 1;   // z^2
		//z=1で評価だと失敗する
		var ans=P3.factorByWang(poly);
	}
	
	@Test
	public void test4DFactor2() {
		long[][][][]f=new long[3][1][1][3];
		f[2][0][0][0]=1;
		f[0][0][0][2]=1;
		//x^2+w^2
		//998244353 = 1 (mod 4), so -1 is a quadratic residue.
		//x^2+w^2 = (x - iw)(x + iw) where i^2 = -1.
		
		//w=1が評価点とする
		//t=w-1
		//(t+1)^2+x^2
		//=t^2+2t+1+x^2
		//=(x+i)(x-i)+t^2+2t
		
		var factors = P4.factor(f);
		assertEquals(2, factors.factors().length);
	}

	@Test
	public void test2DFactor2() {
		long[][]f=new long[3][3];
		f[2][0]=1;
		f[0][2]=1;
		//x^2+y^2
		//998244353 = 1 (mod 4), so x^2+y^2 splits.
		var factors = P2.factor(f);
		assertEquals(2, factors.factors().length);
	}

	@Test
	public void test2DFactor3() {
		long[][][]f=new long[3][3][3];
		f[2][0][0]=1;
		f[0][0][2]=1;
		//x^2+z^2
		//998244353 = 1 (mod 4).
		var factors = P3.factor(f);
		assertEquals(2, factors.factors().length);
	}

	@Test
	public void test2DFactorIrreducibleP3() {
		var P2_3 = PolynomialFpDynamic2D.of(3);
		long[][] f = new long[3][3];
		f[2][0] = 1;
		f[0][2] = 1;
		// x^2 + y^2 in F3.
		// At y=1, x^2 + 1 is irreducible in F3[x].
		var result = P2_3.factor(f);
		assertEquals(1, result.factors().length);
		assertTrue(P2_3.equals(P2_3.monic(f), result.factors()[0].factor));
	}

	@Test
	public void test3DFactorIrreducibleP3() {
		var P3_3 = new PolynomialFpDynamic3D(3);
		long[][][] f = new long[3][1][3];
		f[2][0][0] = 1;
		f[0][0][2] = 1;
		// x^2 + z^2 in F3.
		var result = P3_3.factor(f);
		assertEquals(1, result.factors().length);
		assertTrue(P3_3.equals(P3_3.monic(f), result.factors()[0].factor));
	}

	@Test
	public void test4DFactorIrreducibleP3() {
		var P4_3 = new PolynomialFpDynamic4D(3);
		long[][][][] f = new long[3][1][1][3];
		f[2][0][0][0] = 1;
		f[0][0][0][2] = 1;
		// x^2 + w^2 in F3.
		var result = P4_3.factor(f);
		assertEquals(1, result.factors().length);
		assertTrue(P4_3.equals(P4_3.monic(f), result.factors()[0].factor));
	}

	
	@Test
	public void test2DFactorMultiplicity() {
		// (x + y + 1)^2
		long[][] f = new long[][] { { 1, 1 }, { 1 } }; // x + y + 1
		long[][] f2 = P2.mul(f, f);
		assertFactorProduct2D(f2);
		PolynomialFpDynamic2D.Factor[] factors = P2.factor(f2).factors();
		assertTrue(factors.length == 1);
		assertTrue(factors[0].multiplicity == 2);
	}

	/** 3変数Wang Trickのprimitiveランダム積復元テスト。未テスト。計算量: O(T * (Factor2D + Lift3D + Mul3D)) */
	@Test
	public void test3DWangTrickRandomPrimitiveProducts() {
		Random rnd = new Random(2026052804L);
		int tested = 0;
		for (int trial = 0; tested < 20 && trial < 100; trial++) {
			long[][][] f = P3.one();
			int cnt = 2 + rnd.nextInt(2);
			for (int i = 0; i < cnt; i++) f = P3.mul(f, randomPrimitiveLinear3DForWang(rnd));
			if (!isConstant2D(P3.contentX(f))) continue;
			PolynomialFpDynamic3D.Factor[] factors = P3.factorByWang(f);
			long[][][] prod = P3.one();
			for (PolynomialFpDynamic3D.Factor e : factors) {
				for (int i = 0; i < e.multiplicity; i++) prod = P3.mul(prod, e.factor);
			}
			assertTrue(P3.equals(P3.monic(f), P3.monic(prod)), "3D Wang random product differs at trial " + trial);
			tested++;
		}
		assertEquals(20, tested);
	}

	/** 4変数Wang Trickのprimitiveランダム積復元テスト。未テスト。計算量: O(T * (Factor3D + Lift4D + Mul4D)) */
	@Test
	public void test4DWangTrickRandomPrimitiveProducts() {
		Random rnd = new Random(2026052805L);
		int tested = 0;
		for (int trial = 0; tested < 5 && trial < 50; trial++) {
			long[][][][] f = P4.one();
			for (int i = 0; i < 2; i++) f = P4.mul(f, randomPrimitiveLinear4DForWang(rnd));
			if (!isConstant3D(P4.contentX(f))) continue;
			PolynomialFpDynamic4D.Factor[] factors = P4.factorByWang(f);
			long[][][][] prod = P4.one();
			for (PolynomialFpDynamic4D.Factor e : factors) {
				for (int i = 0; i < e.multiplicity; i++) prod = P4.mul(prod, e.factor);
			}
			assertTrue(P4.equals(P4.monic(f), P4.monic(prod)), "4D Wang random product differs at trial " + trial);
			tested++;
		}
		assertEquals(5, tested);
	}

	@Test
	public void test3DFactorMultiplicity() {
		// (x + y + z + 1)^2
		long[][][] f = new long[][][] { { { 1, 1 }, { 1 } }, { { 1 } } }; // x + y + z + 1
		long[][][] f2 = P3.mul(f, f);
		assertFactorProduct3D(f2);
		PolynomialFpDynamic3D.Factor[] factors = P3.factor(f2).factors();
		assertTrue(factors.length == 1);
		assertTrue(factors[0].multiplicity == 2);
	}

	@Test
	public void test2DFactorIrreducibleManual() {
		// x^2 + y + 1
		long[][] f = new long[][] { { 1, 1 }, { 0 }, { 1 } };
		assertFactorProduct2D(f);
		PolynomialFpDynamic2D.Factor[] factors = P2.factor(f).factors();
		assertTrue(factors.length == 1);
		assertTrue(factors[0].multiplicity == 1);
	}

	@Test
	public void test2DFactorEdgeCases() {
		// Constant
		long[][] fConst = new long[][] { { 5 } };
		assertTrue(P2.factor(fConst).factors().length == 0);

		// Zero (if handled, usually factor returns empty or throws, but let's see)
		long[][] fZero = new long[0][0];
		assertTrue(P2.factor(fZero).factors().length == 0);

		// Univariate in 2D: x^2 - 1 = (x-1)(x+1)
		long[][] fUnivariate = new long[][] { { P1.mod - 1 }, { 0 }, { 1 } };
		assertFactorProduct2D(fUnivariate);
		assertTrue(P2.factor(fUnivariate).factors().length == 2);
	}

	@Test
	public void test2DFactorRandomProductsCoprime() {
		Random rnd = new Random(2026051904L);
		for (int t = 0; t < 10; t++) {
			long[][] a = randomNonConstantLinear2D(rnd);
			long[][] b = randomNonConstantLinear2D(rnd);
			long[][] c = P2.mul(a, b);
			assertFactorCoprime2D(c);
		}
	}

	/** 2変数Hensel liftのランダム積復元テスト。未テスト。計算量: O(T * (Factor1D + Lift2D + Mul2D)) */
	@Test
	public void test2DLiftRandomProducts() {
		Random rnd = new Random(2026052801L);
		for (int t = 0; t < 30; t++) {
			long[][] f = P2.one();
			int cnt = 2 + rnd.nextInt(3);
			for (int i = 0; i < cnt; i++) f = P2.mul(f, randomLiftLinear2D(rnd));
			f = P2.monic(f);
			long valY = findGoodLiftPoint(f, cnt);
			PolynomialFpDynamic.Factor[] factors = P1.factor(evalY(f, valY)).factors();
			PolynomialFpDynamic2D.Factor[] lifted = P2.liftFactors(f, factors, valY);
			long[][] prod = P2.one();
			for (PolynomialFpDynamic2D.Factor e : lifted) {
				for (int i = 0; i < e.multiplicity; i++) prod = P2.mul(prod, e.factor);
			}
			assertTrue(P2.equals(P2.monic(f), P2.monic(prod)), "Lifted product differs at trial " + t);
		}
	}

	/** 3変数Hensel liftのランダム積復元テスト。 */
	@Test
	public void test3DLiftRandomProducts() {
		Random rnd = new Random(2026052802L);
		for (int t = 0; t < 10; t++) {
			long[][][] f = P3.one();
			int cnt = 2 + rnd.nextInt(2);
			for (int i = 0; i < cnt; i++) f = P3.mul(f, randomLiftLinear3D(rnd));
			f = P3.monic(f);
			long valZ = findGoodLiftPoint3D(f, cnt);
			PolynomialFpDynamic2D.Factor[] factors = P2.factor(evalZ(f, valZ)).factors();
			PolynomialFpDynamic3D.Factor[] lifted = P3.liftFactors(f, factors, valZ);
			long[][][] prod = P3.one();
			for (PolynomialFpDynamic3D.Factor e : lifted) {
				for (int i = 0; i < e.multiplicity; i++) prod = P3.mul(prod, e.factor);
			}
			assertTrue(P3.equals(P3.monic(f), P3.monic(prod)), "Lifted product differs at trial " + t);
		}
	}

	@Test
	public void test3DFactorRandomProductsCoprime() {
		Random rnd = new Random(2026051905L);
		for (int t = 0; t < 100; t++) {
			long[][][] a = randomNonConstantLinear3D(rnd);
			long[][][] b = randomNonConstantLinear3D(rnd);
			long[][][] c = P3.mul(a, b);
			assertFactorCoprime3D(c);
		}
	}

	@Test
	public void test4DFactorRandomProductsCoprime() {
		Random rnd = new Random(2026051906L);
		for (int t = 0; t < 10; t++) {
			long[][][][] a = randomNonConstantLinear4D(rnd);
			long[][][][] b = randomNonConstantLinear4D(rnd);
			long[][][][] c = P4.mul(a, b);
			assertFactorCoprime4D(c);
		}
	}

	private long[][] randomPrimitiveLinear2D(Random rnd) {
		while (true) {
			long[][] f = new long[][] {
				{ 1 + rnd.nextInt(20), rnd.nextInt(20) },
				{ 1 + rnd.nextInt(20), rnd.nextBoolean() ? 1 + rnd.nextInt(20) : 0 }
			};
			if (P1.deg(P2.contentX(f)) == 0) return f;
		}
	}

	private long[][][] randomPrimitiveLinear3DForWang(Random rnd) {
		while (true) {
			long[][][] f = new long[][][] {
				{ { 1 + rnd.nextInt(5), rnd.nextInt(5) }, { rnd.nextInt(5) } },
				{ { 1 + rnd.nextInt(5), rnd.nextInt(5) } }
			};
			if (isConstant2D(P3.contentX(f))) return f;
		}
	}

	private long[][][][] randomPrimitiveLinear4DForWang(Random rnd) {
		while (true) {
			long[][][][] f = new long[][][][] {
				{ { { 1 + rnd.nextInt(3), rnd.nextInt(3) }, { rnd.nextInt(3) } }, { { rnd.nextInt(3) } } },
				{ { { 1 + rnd.nextInt(3), rnd.nextInt(3) } } }
			};
			if (isConstant3D(P4.contentX(f))) return f;
		}
	}

	private boolean isConstant2D(long[][] f) {
		f = P2.resize(f);
		return f.length == 1 && f[0].length == 1 && f[0][0] != 0;
	}

	private boolean isConstant3D(long[][][] f) {
		f = P3.resize(f);
		return f.length == 1 && f[0].length == 1 && f[0][0].length == 1 && f[0][0][0] != 0;
	}

	private long[][] randomNonConstantLinear2D(Random rnd) {
		while (true) {
			long[][] f = randomLinear2D(rnd);
			if (P2.degX(f) > 0 || P2.degY(f) > 0) return f;
		}
	}

	private long[][][] randomNonConstantLinear3D(Random rnd) {
		while (true) {
			long[][][] f = randomLinear3D(rnd);
			if (P3.degX(f) > 0 || P3.degY(f) > 0 || P3.degZ(f) > 0) return f;
		}
	}

	private long[][][][] randomNonConstantLinear4D(Random rnd) {
		while (true) {
			long[][][][] f = randomLinear4D(rnd);
			if (P4.degX(f) > 0 || P4.degY(f) > 0 || P4.degZ(f) > 0 || P4.degW(f) > 0) return f;
		}
	}

	private void assertFactorCoprime2D(long[][] f) {
		PolynomialFpDynamic2D.Factor[] factors = P2.factor(f).factors();
		int totalCount = 0;
		for (var e : factors) totalCount += e.multiplicity;
		assertTrue(totalCount >= 2, "Factor count should be at least 2");
		for (int i = 0; i < factors.length; i++) {
			for (int j = i + 1; j < factors.length; j++) {
				long[][] g = P2.gcd(factors[i].factor, factors[j].factor);
				assertTrue(P2.equals(P2.monic(g), P2.one()), "Factors should be coprime");
			}
		}
		assertFactorProduct2D(f);
	}

	private void assertFactorCoprime3D(long[][][] f) {
		PolynomialFpDynamic3D.Factor[] factors = P3.factor(f).factors();
		int totalCount = 0;
		for (var e : factors) totalCount += e.multiplicity;
		assertTrue(totalCount >= 2, "Factor count should be at least 2");
		for (int i = 0; i < factors.length; i++) {
			for (int j = i + 1; j < factors.length; j++) {
				long[][][] g = P3.gcd(factors[i].factor, factors[j].factor);
				assertTrue(P3.equals(P3.monic(g), P3.one()), "Factors should be coprime");
			}
		}
		assertFactorProduct3D(f);
	}

	private void assertFactorCoprime4D(long[][][][] f) {
		PolynomialFpDynamic4D.Factor[] factors = P4.factor(f).factors();
		int totalCount = 0;
		for (var e : factors) totalCount += e.multiplicity;
		assertTrue(totalCount >= 2, "Factor count should be at least 2");
		for (int i = 0; i < factors.length; i++) {
			for (int j = i + 1; j < factors.length; j++) {
				long[][][][] g = P4.gcd(factors[i].factor, factors[j].factor);
				assertTrue(P4.equals(P4.monic(g), P4.one()), "Factors should be coprime");
			}
		}
		assertFactorProduct4D(f);
	}

	private long findGoodLiftPoint(long[][] f, int expectedFactorCount) {
		for (long y = 0; y < 200; y++) {
			long[] fy = evalY(f, y);
			if (P1.deg(fy) != P2.degX(f)) continue;
			PolynomialFpDynamic.Factor[] factors = P1.factor(fy).factors();
			int totalCount = 0;
			boolean squareFree = true;
			for (PolynomialFpDynamic.Factor e : factors) {
				totalCount += e.multiplicity;
				squareFree &= e.multiplicity == 1;
			}
			if (squareFree && totalCount == expectedFactorCount) return y;
		}
		throw new AssertionError("good lift point was not found");
	}

	private long findGoodLiftPoint3D(long[][][] f, int expectedFactorCount) {
		for (long z = 0; z < 200; z++) {
			long[][] fz = evalZ(f, z);
			if (P2.degX(fz) != P3.degX(f)) continue;
			PolynomialFpDynamic2D.Factor[] factors = P2.factor(fz).factors();
			int totalCount = 0;
			boolean squareFree = true;
			for (PolynomialFpDynamic2D.Factor e : factors) {
				totalCount += e.multiplicity;
				squareFree &= e.multiplicity == 1;
			}
			if (squareFree && totalCount == expectedFactorCount) return z;
		}
		throw new AssertionError("good lift point was not found");
	}

	private long[] evalY(long[][] f, long y) {
		long[] res = new long[f.length];
		for (int i = 0; i < f.length; i++) res[i] = P1.eval(f[i], y);
		return P1.resize(res);
	}

	private long[][] evalZ(long[][][] f, long z) {
		long[][] res = new long[f.length][];
		for (int i = 0; i < f.length; i++) res[i] = P2.evalY(f[i], z);
		return P2.resize(res);
	}

	private void assertFactorProduct2D(long[][] f) {
		long[][] prod = P2.one();
		PolynomialFpDynamic2D.Factor[] factors = P2.factor(f).factors();
		for (PolynomialFpDynamic2D.Factor e : factors) {
			for (int i = 0; i < e.multiplicity; i++) prod = P2.mul(prod, e.factor);
			PolynomialFpDynamic2D.Factor[] ff = P2.factor(e.factor).factors();
			assertTrue(ff.length == 1 && ff[0].multiplicity == 1 && P2.equals(P2.monic(ff[0].factor), P2.monic(e.factor)), "Factor should be irreducible");
		}
		assertTrue(P2.equals(P2.monic(f), P2.monic(prod)));
	}

	private void assertFactorProduct3D(long[][][] f) {
		long[][][] prod = P3.one();
		PolynomialFpDynamic3D.Factor[] factors = P3.factor(f).factors();
		for (PolynomialFpDynamic3D.Factor e : factors) {
			for (int i = 0; i < e.multiplicity; i++) prod = P3.mul(prod, e.factor);
			PolynomialFpDynamic3D.Factor[] ff = P3.factor(e.factor).factors();
			assertTrue(ff.length == 1 && ff[0].multiplicity == 1 && P3.equals(P3.monic(ff[0].factor), P3.monic(e.factor)), "Factor should be irreducible");
		}
		assertTrue(P3.equals(P3.monic(f), P3.monic(prod)));
	}

	private void assertFactorProduct4D(long[][][][] f) {
		long[][][][] prod = P4.one();
		PolynomialFpDynamic4D.Factor[] factors = P4.factor(f).factors();
		for (PolynomialFpDynamic4D.Factor e : factors) {
			for (int i = 0; i < e.multiplicity; i++) prod = P4.mul(prod, e.factor);
			PolynomialFpDynamic4D.Factor[] ff = P4.factor(e.factor).factors();
			assertTrue(ff.length == 1 && ff[0].multiplicity == 1 && P4.equals(P4.monic(ff[0].factor), P4.monic(e.factor)), "Factor should be irreducible");
		}
		assertTrue(P4.equals(P4.monic(f), P4.monic(prod)));
	}

	private long[][] randomLinear2D(Random rnd) {
		long[][] f = new long[2][2];
		f[0][0] = nonzero(rnd);
		f[1][0] = nonzero(rnd);
		f[0][1] = nonzero(rnd);
		if (rnd.nextBoolean()) f[1][1] = rnd.nextInt(7);
		return P2.resize(f);
	}

	private long[][] randomLiftLinear2D(Random rnd) {
		long[][] f = new long[2][2];
		f[0][0] = nonzero(rnd);
		f[1][0] = nonzero(rnd);
		f[0][1] = nonzero(rnd);
		return P2.resize(f);
	}

	private long[][][] randomLiftLinear3D(Random rnd) {
		long[][][] f = new long[2][2][2];
		f[0][0][0] = nonzero(rnd);
		f[1][0][0] = nonzero(rnd);
		f[0][1][0] = nonzero(rnd);
		f[0][0][1] = nonzero(rnd);
		return P3.resize(f);
	}

	private long[][][] randomLinear3D(Random rnd) {
		long[][][] f = new long[2][2][2];
		f[0][0][0] = nonzero(rnd);
		f[1][0][0] = nonzero(rnd);
		f[0][1][0] = nonzero(rnd);
		f[0][0][1] = nonzero(rnd);
		if (rnd.nextBoolean()) f[1][1][0] = rnd.nextInt(5);
		if (rnd.nextBoolean()) f[0][1][1] = rnd.nextInt(5);
		return P3.resize(f);
	}

	private long[][][][] randomLinear4D(Random rnd) {
		long[][][][] f = new long[2][2][2][2];
		f[0][0][0][0] = nonzero(rnd);
		f[1][0][0][0] = nonzero(rnd);
		f[0][1][0][0] = nonzero(rnd);
		f[0][0][1][0] = nonzero(rnd);
		f[0][0][0][1] = nonzero(rnd);
		if (rnd.nextBoolean()) f[1][1][0][0] = rnd.nextInt(3);
		return P4.resize(f);
	}

	private long nonzero(Random rnd) {
		return 1 + rnd.nextInt(1000);
	}

	@Test
	public void testFactorNonPrimitive() {
		// f(x, y) = y(x^2 + x + 1)
		// x^2 + x + 1 is irreducible in F_998244353[x]
		long[][] f = new long[][] { { 0, 1 }, { 0, 1 }, { 0, 1 } };

		var result = P2.factor(f);
		assertEquals(2, result.factors().length);

		boolean foundY = false;
		boolean foundX2X1 = false;
		for (var factor : result.factors()) {
			if (P2.equals(factor.factor, P2.y())) foundY = true;
			if (P2.equals(factor.factor, new long[][] { { 1 }, { 1 }, { 1 } })) foundX2X1 = true;
		}
		assertTrue(foundY, "Should find factor y");
		assertTrue(foundX2X1, "Should find factor x^2 + x + 1");
	}

	@Test
	public void testFactorNonPrimitive3D() {
		// f(x, y, z) = z(x^2 + x + 1)
		long[][][] f = new long[][][] {
			{ { 0, 1 } },
			{ { 0, 1 } },
			{ { 0, 1 } }
		};
		var result = P3.factor(f);
		assertEquals(2, result.factors().length);
	}

	@Test
	public void testFactorNonPrimitive4D() {
		// f(x, y, z, w) = w(x^2 + x + 1)
		long[][][][] f = new long[][][][] {
			{ { { 0, 1 } } },
			{ { { 0, 1 } } },
			{ { { 0, 1 } } }
		};
		var result = P4.factor(f);
		assertEquals(2, result.factors().length);
	}

	@Test
	public void testIrreducibleX4YX2Y4() {
		// f(x, y) = x^4 + yx^2 + y^4
		// This polynomial is irreducible.

		// 2D
		long[][] f2 = new long[5][5];
		f2[4][0] = 1;
		f2[2][1] = 1;
		f2[0][4] = 1;
		var res2 = P2.factor(f2);
		assertEquals(1, res2.factors().length, "x^4 + yx^2 + y^4 should be irreducible in 2D");
		assertTrue(P2.equals(P2.monic(f2), res2.factors()[0].factor));

		// 3D
		long[][][] f3 = new long[5][5][1];
		f3[4][0][0] = 1;
		f3[2][1][0] = 1;
		f3[0][4][0] = 1;
		var res3 = P3.factor(f3);
		assertEquals(1, res3.factors().length, "x^4 + yx^2 + y^4 should be irreducible in 3D");
		assertTrue(P3.equals(P3.monic(f3), res3.factors()[0].factor));

		// 4D
		long[][][][] f4 = new long[5][5][1][1];
		f4[4][0][0][0] = 1;
		f4[2][1][0][0] = 1;
		f4[0][4][0][0] = 1;
		var res4 = P4.factor(f4);
		assertEquals(1, res4.factors().length, "x^4 + yx^2 + y^4 should be irreducible in 4D");
		assertTrue(P4.equals(P4.monic(f4), res4.factors()[0].factor));
	}
}
