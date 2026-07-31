package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialLong2D;
import library.util.polynomial.PolynomialLong3D;

public class PolynomialLongMultivariateTest {

	@Test
	public void test2DAdd() {
		long[][] a = {{1, 2}, {3, 4}};
		long[][] b = {{5, 6, 7}, {8, 9}};
		long[][] expected = {{6, 8, 7}, {11, 13, 0}};
		long[][] actual = PolynomialLong2D.add(a, b);
		assertArrayEquals2D(expected, actual);
	}

	@Test
	public void test2DSubtract() {
		long[][] a = {{10, 20}, {30, 40}};
		long[][] b = {{1, 2, 3}, {4, 5}};
		long[][] expected = {{9, 18, -3}, {26, 35, 0}};
		long[][] actual = PolynomialLong2D.subtract(a, b);
		assertArrayEquals2D(expected, actual);
	}

	@Test
	public void test2DMul() {
		long[][] a = {{1, 2}, {3}};
		long[][] b = {{4, 5}};
		long[][] expected = {{4, 13, 10}, {12, 15, 0}};
		long[][] actual = PolynomialLong2D.mul(a, b);
		assertArrayEquals2D(expected, actual);
	}

	@Test
	public void test3DAdd() {
		long[][][] a = {{{1, 2}, {3}}, {{4}}};
		long[][][] b = {{{5}, {6, 7}}, {{0}, {8}}};
		long[][][] expected = {{{6, 2}, {9, 7}}, {{4, 0}, {8, 0}}};
		long[][][] actual = PolynomialLong3D.add(a, b);
		assertArrayEquals3D(expected, actual);
	}

	@Test
	public void test3DSubtract() {
		long[][][] a = {{{10}}};
		long[][][] b = {{{1, 2}}};
		long[][][] expected = {{{9, -2}}};
		long[][][] actual = PolynomialLong3D.subtract(a, b);
		assertArrayEquals3D(expected, actual);
	}

	@Test
	public void test3DMul() {
		long[][][] polyX = {{{1}}, {{1}}}; // 1 + x
		long[][][] polyY = {{{1}, {1}}};    // 1 + y
		long[][][] polyZ = {{{1, 1}}};       // 1 + z

		long[][][] polyXY = PolynomialLong3D.mul(polyX, polyY);
		long[][][] res = PolynomialLong3D.mul(polyXY, polyZ);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					assertEquals(1, res[i][j][k]);
				}
			}
		}
	}

	private void assertArrayEquals2D(long[][] expected, long[][] actual) {
		assertEquals(expected.length, actual.length, "N dimension mismatch");
		for (int i = 0; i < expected.length; i++) {
			assertArrayEquals(expected[i], actual[i], "M dimension mismatch at i=" + i);
		}
	}

	private void assertArrayEquals3D(long[][][] expected, long[][][] actual) {
		assertEquals(expected.length, actual.length, "N dimension mismatch");
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length, "M dimension mismatch at i=" + i);
			for (int j = 0; j < expected[i].length; j++) {
				assertArrayEquals(expected[i][j], actual[i][j], "L dimension mismatch at i=" + i + ", j=" + j);
			}
		}
	}
}
