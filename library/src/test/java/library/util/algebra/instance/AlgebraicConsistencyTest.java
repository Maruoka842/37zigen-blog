package library.util.algebra.instance;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.algebra.instance.impl.FpElement;
import library.util.algebra.instance.impl.ZElement;

class AlgebraicConsistencyTest {

	@Test
	void testZ() {
		ZElement a = new ZElement(10);
		ZElement b = new ZElement(20);

		// MonoidElement (as add)
		ZElement c = a.add(b);
		assertEquals(30, c.val);

		ZElement d = a.neg();
		assertEquals(-10, d.val);

		// SemiRingElement
		ZElement e = a.mul(b);
		assertEquals(200, e.val);

		// RingElement
		ZElement f = a.sub(b);
		assertEquals(-10, f.val);

		// EuclideanDomainElement
		ZElement g = new ZElement(7);
		ZElement h = new ZElement(3);
		assertEquals(2, g.div(h).val);
		assertEquals(1, g.mod(h).val);
		assertEquals(7, g.norm());

		// Type hierarchy
		assertTrue(a instanceof RingElement);
		assertTrue(a instanceof EuclideanDomainElement);
	}

	@Test
	void testFp() {
		FpElement a = new FpElement(3, 7);
		FpElement b = new FpElement(5, 7);

		// add
		assertEquals(1, a.add(b).val);

		// mul
		assertEquals(1, a.mul(b).val);

		// neg
		assertEquals(4, a.neg().val);

		// inv
		assertEquals(5, a.inv().val);

		// Type hierarchy
		assertTrue(a instanceof FieldElement);
		assertTrue(a instanceof RingElement);
	}

	@Test
	void testPolynomial() {
		ZStrategy zs = new ZStrategy();
		ZElement[] c1 = {new ZElement(1), new ZElement(2)};
		ZElement[] c2 = {new ZElement(3), new ZElement(4)};
		PolynomialRingElement<ZElement> p1 = new PolynomialRingElement<>(c1, zs); // 1 + 2x
		PolynomialRingElement<ZElement> p2 = new PolynomialRingElement<>(c2, zs); // 3 + 4x

		// add
		PolynomialRingElement<ZElement> p3 = p1.add(p2);
		assertEquals(4, p3.coeffs[0].val);
		assertEquals(6, p3.coeffs[1].val);

		// mul
		PolynomialRingElement<ZElement> p4 = p1.mul(p2); // (1+2x)(3+4x) = 3 + 4x + 6x + 8x^2 = 3 + 10x + 8x^2
		assertEquals(3, p4.coeffs[0].val);
		assertEquals(10, p4.coeffs[1].val);
		assertEquals(8, p4.coeffs[2].val);

		// Type hierarchy
		assertTrue(p1 instanceof PolynomialElement);
		assertTrue(p1 instanceof IntegralDomainElement);
		assertFalse(p1 instanceof EuclideanDomainElement);
	}

	@Test
	void testComplexNumber() {
		library.util.algebra.instance.impl.ComplexNumber a = new library.util.algebra.instance.impl.ComplexNumber(1, 2);
		library.util.algebra.instance.impl.ComplexNumber b = new library.util.algebra.instance.impl.ComplexNumber(3, 4);

		// FieldElement
		library.util.algebra.instance.impl.ComplexNumber c = a.add(b);
		assertEquals(4, c.re());
		assertEquals(6, c.im());

		library.util.algebra.instance.impl.ComplexNumber d = a.mul(b);
		assertEquals(-5, d.re());
		assertEquals(10, d.im());

		// Type hierarchy
		assertTrue(a instanceof FieldElement);
		assertTrue(a instanceof library.util.algebra.instance.ExactDivRingElement);
	}

	private static class ZStrategy implements library.util.algebra.strategy.CommutativeRingStrategy<ZElement> {
		@Override public ZElement add(ZElement a, ZElement b) { return a.add(b); }
		@Override public ZElement sub(ZElement a, ZElement b) { return a.sub(b); }
		@Override public ZElement mul(ZElement a, ZElement b) { return a.mul(b); }
		@Override public ZElement neg(ZElement a) { return a.neg(); }
		@Override public ZElement zero() { return new ZElement(0); }
		@Override public ZElement one() { return new ZElement(1); }
		@Override public boolean equals(ZElement a, ZElement b) { return a.val == b.val; }
	}
}
