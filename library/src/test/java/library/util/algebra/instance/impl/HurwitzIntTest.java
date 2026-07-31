package library.util.algebra.instance.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class HurwitzIntTest {

	@Test
	public void testArithmetic() {
		HurwitzInt a = HurwitzInt.fromLipschitz(1, 2, 3, 4);
		HurwitzInt b = HurwitzInt.fromLipschitz(5, 6, 7, 8);

		assertEquals(HurwitzInt.fromLipschitz(6, 8, 10, 12), a.add(b));
		assertEquals(HurwitzInt.fromLipschitz(-4, -4, -4, -4), a.sub(b));
		assertEquals(HurwitzInt.fromLipschitz(-1, -2, -3, -4), a.neg());
	}

	@Test
	public void testMultiplication() {
		HurwitzInt i = HurwitzInt.I;
		HurwitzInt j = HurwitzInt.J;
		HurwitzInt k = HurwitzInt.K;

		assertEquals(HurwitzInt.fromLipschitz(-1, 0, 0, 0), i.mul(i));
		assertEquals(HurwitzInt.fromLipschitz(-1, 0, 0, 0), j.mul(j));
		assertEquals(HurwitzInt.fromLipschitz(-1, 0, 0, 0), k.mul(k));
		assertEquals(k, i.mul(j));
		assertEquals(k.neg(), j.mul(i));

		HurwitzInt omega = HurwitzInt.OMEGA;
		assertEquals(new HurwitzInt(-1, 1, 1, 1), omega.mul(omega));
	}

	@Test
	public void testNormAndConj() {
		HurwitzInt a = HurwitzInt.fromLipschitz(1, 2, 3, 4);
		assertEquals(30, a.norm());
		assertEquals(HurwitzInt.fromLipschitz(1, -2, -3, -4), a.conj());

		HurwitzInt omega = HurwitzInt.OMEGA;
		assertEquals(1, omega.norm());
	}

	@Test
	public void testDivision() {
		HurwitzInt a = HurwitzInt.fromLipschitz(10, 20, 30, 40);
		HurwitzInt b = HurwitzInt.fromLipschitz(1, 1, 1, 1);

		// Right quotient: a = q*b + r
		HurwitzInt qR = a.divR(b);
		HurwitzInt rR = a.remR(b);
		assertEquals(a, qR.mul(b).add(rR));
		assertTrue(rR.norm() < b.norm());

		// Left quotient: a = b*q + r
		HurwitzInt qL = a.divL(b);
		HurwitzInt rL = a.remL(b);
		assertEquals(a, b.mul(qL).add(rL));
		assertTrue(rL.norm() < b.norm());
	}

	@Test
	public void testGCD() {
		HurwitzInt g = HurwitzInt.fromLipschitz(1, 2, 3, 4);

		// Left GCD: a = g*x, b = g*y
		HurwitzInt a = g.mul(HurwitzInt.fromLipschitz(1, 0, 1, 0));
		HurwitzInt b = g.mul(HurwitzInt.fromLipschitz(0, 1, 0, 1));
		HurwitzInt lgcd = HurwitzInt.leftGCD(a, b);
		assertTrue(a.remL(lgcd).isZero());
		assertTrue(b.remL(lgcd).isZero());

		// Right GCD: a = x*g, b = y*g
		HurwitzInt a2 = HurwitzInt.fromLipschitz(1, 0, 1, 0).mul(g);
		HurwitzInt b2 = HurwitzInt.fromLipschitz(0, 1, 0, 1).mul(g);
		HurwitzInt rgcd = HurwitzInt.rightGCD(a2, b2);
		assertTrue(a2.remR(rgcd).isZero());
		assertTrue(b2.remR(rgcd).isZero());
	}
}
