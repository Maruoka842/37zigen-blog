package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import library.util.algebra.strategy.ZStrategy;
import library.util.algebra.strategy.ZnStrategy;
import library.util.polynomial.PolynomialLong;
import library.util.polynomial.PolynomialLong2D;
import library.util.polynomial.PolynomialLong3D;

public class IntegralDomainDetectionTest {

	@Test
	public void testPolynomialLong() {
		CommutativeRingStrategy<Long> id = new ZStrategy();
		CommutativeRingStrategy<long[]> resId = PolynomialLong.Strategy(id);
		assertTrue(resId instanceof IntegralDomainStrategy, "PolynomialLong.ringStrategy should return IntegralDomainStrategy when input is IntegralDomainStrategy");

		CommutativeRingStrategy<Long> notId = new ZnStrategy(4);
		CommutativeRingStrategy<long[]> resNotId = PolynomialLong.Strategy(notId);
		assertFalse(resNotId instanceof IntegralDomainStrategy, "PolynomialLong.ringStrategy should NOT return IntegralDomainStrategy when input is NOT IntegralDomainStrategy");
	}

	@Test
	public void testPolynomialLong2D() {
		CommutativeRingStrategy<Long> id = new ZStrategy();
		CommutativeRingStrategy<long[][]> resId = PolynomialLong2D.strategy(id);
		assertTrue(resId instanceof IntegralDomainStrategy, "PolynomialLong2D.strategy should return IntegralDomainStrategy when input is IntegralDomainStrategy");

		CommutativeRingStrategy<Long> notId = new ZnStrategy(4);
		CommutativeRingStrategy<long[][]> resNotId = PolynomialLong2D.strategy(notId);
		assertFalse(resNotId instanceof IntegralDomainStrategy, "PolynomialLong2D.strategy should NOT return IntegralDomainStrategy when input is NOT IntegralDomainStrategy");
	}

	@Test
	public void testPolynomialLong3D() {
		CommutativeRingStrategy<Long> id = new ZStrategy();
		CommutativeRingStrategy<long[][][]> resId = PolynomialLong3D.strategy(id);
		assertTrue(resId instanceof IntegralDomainStrategy, "PolynomialLong3D.strategy should return IntegralDomainStrategy when input is IntegralDomainStrategy");

		CommutativeRingStrategy<Long> notId = new ZnStrategy(4);
		CommutativeRingStrategy<long[][][]> resNotId = PolynomialLong3D.strategy(notId);
		assertFalse(resNotId instanceof IntegralDomainStrategy, "PolynomialLong3D.strategy should NOT return IntegralDomainStrategy when input is NOT IntegralDomainStrategy");
	}
}
