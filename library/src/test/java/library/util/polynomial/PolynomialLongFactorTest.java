package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PolynomialLongFactorTest {

	@Test
	public void test1DFactorBasic() {
		// x^2 - 5x + 6 = (x - 3)(x - 2)
		long[] p = {6, -5, 1};
		PolynomialLong.FactorResult res = PolynomialLong.factor(p);

		assertEquals(1, res.leadingCoeff());
		assertEquals(2, res.factors().length);

		// The factors should be { -3, 1 } and { -2, 1 } (in some order)
		long[] f1 = res.factors()[0].factor();
		long[] f2 = res.factors()[1].factor();
		assertEquals(1, res.factors()[0].multiplicity());
		assertEquals(1, res.factors()[1].multiplicity());

		boolean match1 = (ArraysEquals1D(f1, new long[]{-3, 1}) && ArraysEquals1D(f2, new long[]{-2, 1})) ||
		                 (ArraysEquals1D(f1, new long[]{-2, 1}) && ArraysEquals1D(f2, new long[]{-3, 1})) ||
		                 (ArraysEquals1D(f1, new long[]{3, -1}) && ArraysEquals1D(f2, new long[]{2, -1})) ||
		                 (ArraysEquals1D(f1, new long[]{2, -1}) && ArraysEquals1D(f2, new long[]{3, -1}));

		assertTrue(match1, "Expected factors (x-3) and (x-2)");
	}

	@Test
	public void test1DFactorWithContent() {
		// 6x^2 + 15x + 6 = 3 * (x + 2) * (2x + 1)
		long[] p = {6, 15, 6};
		PolynomialLong.FactorResult res = PolynomialLong.factor(p);

		assertEquals(3, res.leadingCoeff());
		assertEquals(2, res.factors().length);

		long[] f1 = res.factors()[0].factor();
		long[] f2 = res.factors()[1].factor();

		boolean match = (ArraysEquals1D(f1, new long[]{2, 1}) && ArraysEquals1D(f2, new long[]{1, 2})) ||
		                (ArraysEquals1D(f1, new long[]{1, 2}) && ArraysEquals1D(f2, new long[]{2, 1}));
		assertTrue(match, "Expected factors (x+2) and (2x+1)");
	}

	@Test
	public void test1DFactorNegative() {
		// -2x^2 - 5x - 2 = -1 * (x + 2) * (2x + 1)
		long[] p = {-2, -5, -2};
		PolynomialLong.FactorResult res = PolynomialLong.factor(p);

		assertEquals(-1, res.leadingCoeff());
		assertEquals(2, res.factors().length);
	}

	@Test
	public void test1DConstant() {
		long[] p = {5};
		PolynomialLong.FactorResult res = PolynomialLong.factor(p);
		assertEquals(5, res.leadingCoeff());
		assertEquals(0, res.factors().length);
	}

	@Test
	public void test1DZeroException() {
		assertThrows(IllegalArgumentException.class, () -> {
			PolynomialLong.factor(new long[0]);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			PolynomialLong.factor(new long[]{0, 0, 0});
		});
	}

	@Test
	public void test2DFactorBasic() {
		// x^2 - y^2 = (x - y)(x + y)
		// x^2: i=2, j=0 => p[2][0] = 1
		// -y^2: i=0, j=2 => p[0][2] = -1
		long[][] p = new long[3][3];
		p[2][0] = 1;
		p[0][2] = -1;

		PolynomialLong2D.FactorResult res = PolynomialLong2D.factor(p);
		assertEquals(1, res.leadingCoeff());
		assertEquals(2, res.factors().length);

		long[][] f1 = res.factors()[0].factor();
		long[][] f2 = res.factors()[1].factor();

		// Factors should be (x - y) and (x + y)
		// (x - y) => x^1: [1][0]=1, y^1: [0][1]=-1 (or multiplied by -1)
		// (x + y) => x^1: [1][0]=1, y^1: [0][1]=1 (or multiplied by -1)
		boolean hasXMinusY = isXMinusY(f1) || isXMinusY(f2);
		boolean hasXPlusY = isXPlusY(f1) || isXPlusY(f2);

		assertTrue(hasXMinusY, "Should contain x - y factor");
		assertTrue(hasXPlusY, "Should contain x + y factor");
	}

	private boolean isXMinusY(long[][] f) {
		if (f.length < 2 || f[0].length < 2) return false;
		long val1 = f[1][0];
		long val2 = f[0][1];
		return (val1 == 1 && val2 == -1) || (val1 == -1 && val2 == 1);
	}

	private boolean isXPlusY(long[][] f) {
		if (f.length < 2 || f[0].length < 2) return false;
		long val1 = f[1][0];
		long val2 = f[0][1];
		return (val1 == 1 && val2 == 1) || (val1 == -1 && val2 == -1);
	}

	@Test
	public void test2DZeroException() {
		assertThrows(IllegalArgumentException.class, () -> {
			PolynomialLong2D.factor(new long[0][0]);
		});
	}

	@Test
	public void test3DFactorBasic() {
		// xyz = x * y * z
		// x^1 y^1 z^1 => p[1][1][1] = 1
		long[][][] p = new long[2][2][2];
		p[1][1][1] = 1;

		PolynomialLong3D.FactorResult res = PolynomialLong3D.factor(p);
		assertEquals(1, res.leadingCoeff());
		assertEquals(3, res.factors().length); // x, y, z are irreducible factors

		for (int i = 0; i < 3; i++) {
			assertEquals(1, res.factors()[i].multiplicity());
		}
	}

	@Test
	public void test3DZeroException() {
		assertThrows(IllegalArgumentException.class, () -> {
			PolynomialLong3D.factor(new long[0][0][0]);
		});
	}

	private boolean ArraysEquals1D(long[] a, long[] b) {
		return java.util.Arrays.equals(a, b);
	}
}
