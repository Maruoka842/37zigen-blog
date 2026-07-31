package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.ZnStrategy;
import library.util.polynomial.PolynomialLong;

public class PolynomialLongStrategyTest {

	@Test
	public void testRingStrategyWithZn() {
		long mod = 10;
		CommutativeRingStrategy<long[]> strategy = PolynomialLong.Strategy(new ZnStrategy(mod));

		long[] a = {7, 8}; // 7 + 8x
		long[] b = {5, 4}; // 5 + 4x

		// (7 + 8x) + (5 + 4x) = 12 + 12x = 2 + 2x (mod 10)
		long[] sum = strategy.add(a, b);
		assertArrayEquals(new long[]{2, 2}, sum);

		// (7 + 8x) * (5 + 4x) = 35 + (28 + 40)x + 32x^2 = 35 + 68x + 32x^2 = 5 + 8x + 2x^2 (mod 10)
		long[] prod = strategy.mul(a, b);
		assertArrayEquals(new long[]{5, 8, 2}, prod);
	}

	@Test
	public void testTruncatedStrategy() {
		long mod = 100;
		// Modulo x^2
		CommutativeRingStrategy<long[]> strategy = PolynomialLong.truncatedStrategy(new ZnStrategy(mod), 2);

		long[] a = {1, 1}; // 1 + x
		// (1 + x)^2 = 1 + 2x + x^2 = 1 + 2x (mod x^2)
		long[] a2 = strategy.mul(a, a);
		assertArrayEquals(new long[]{1, 2}, a2);

		// (1 + x)^3 = 1 + 3x + 3x^2 + x^3 = 1 + 3x (mod x^2)
		long[] a3 = strategy.mul(a2, a);
		assertArrayEquals(new long[]{1, 3}, a3);
	}

	@Test
	public void testRingStrategyEquals() {
		CommutativeRingStrategy<long[]> strategy = PolynomialLong.Strategy(new library.util.algebra.strategy.ZStrategy());

		long[] a = {1, 2, 0};
		long[] b = {1, 2};
		assertTrue(strategy.equals(a, b));

		long[] c = {1, 3};
		assertFalse(strategy.equals(a, c));
	}

	@Test
	public void testTruncatedStrategyEquals() {
		// Modulo x^2
		CommutativeRingStrategy<long[]> strategy = PolynomialLong.truncatedStrategy(new library.util.algebra.strategy.ZStrategy(), 2);

		long[] a = {1, 2, 3};
		long[] b = {1, 2, 4};
		// Both are 1 + 2x (mod x^2)
		assertTrue(strategy.equals(a, b));

		long[] c = {1, 3};
		assertFalse(strategy.equals(a, c));
	}
}
