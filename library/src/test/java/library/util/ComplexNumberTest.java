package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import library.util.algebra.instance.FieldElement;
import library.util.algebra.instance.impl.ComplexNumber;

class ComplexNumberTest {

	private static final double EPS = 1e-9;

	@Test
	void testBasicArithmetic() {
		ComplexNumber a = new ComplexNumber(1, 2);
		ComplexNumber b = new ComplexNumber(3, 4);

		// add
		assertEquals(new ComplexNumber(4, 6), a.add(b));
		// sub
		assertEquals(new ComplexNumber(-2, -2), a.sub(b));
		// mul
		// (1+2i)(3+4i) = 3 + 4i + 6i - 8 = -5 + 10i
		assertEquals(new ComplexNumber(-5, 10), a.mul(b));
		// div
		// (-5+10i) / (1+2i) = (-5+10i)(1-2i) / 5 = (-5 + 10i + 10i + 20) / 5 = (15 + 20i) / 5 = 3 + 4i
		assertEquals(b, new ComplexNumber(-5, 10).div(a));
	}

	@Test
	void testAlgebraicProperties() {
		ComplexNumber a = new ComplexNumber(1, 2);
		assertTrue(a instanceof FieldElement);
		assertEquals(ComplexNumber.ONE, a.mul(a.inv()));
		assertEquals(ComplexNumber.ZERO, a.add(a.neg()));
	}

	@Test
	void testTranscendental() {
		ComplexNumber z = new ComplexNumber(0, Math.PI);
		ComplexNumber ez = z.exp();
		// e^(i*PI) = -1
		assertEquals(-1.0, ez.re(), EPS);
		assertEquals(0.0, ez.im(), EPS);

		ComplexNumber logM1 = new ComplexNumber(-1, 0).log();
		// log(-1) = i*PI
		assertEquals(0.0, logM1.re(), EPS);
		assertEquals(Math.PI, logM1.im(), EPS);
	}

	@Test
	void testSqrt() {
		ComplexNumber z = new ComplexNumber(0, 2);
		ComplexNumber s = z.sqrt();
		// sqrt(2i) = 1 + i
		assertEquals(1.0, s.re(), EPS);
		assertEquals(1.0, s.im(), EPS);
	}

	@Test
	void testPolar() {
		ComplexNumber p = ComplexNumber.polar(2, Math.PI / 3);
		assertEquals(1.0, p.re(), EPS);
		assertEquals(Math.sqrt(3), p.im(), EPS);
	}
}
