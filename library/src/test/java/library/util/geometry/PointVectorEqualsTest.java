package library.util.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PointVectorEqualsTest {

	@Test
	public void testDoublePointEquals() {
		DoublePoint p1 = new DoublePoint(1.23, 4.56);
		DoublePoint p2 = new DoublePoint(1.23, 4.56);
		DoublePoint p3 = new DoublePoint(1.23, 7.89);
		DoublePoint p4 = new DoublePoint(9.99, 4.56);

		assertEquals(p1, p1);
		assertEquals(p1, p2);
		assertEquals(p2, p1);
		assertNotEquals(p1, p3);
		assertNotEquals(p1, p4);
		assertNotEquals(p1, null);
		assertNotEquals(p1, "string");

		assertEquals(p1.hashCode(), p2.hashCode());
	}

	@Test
	public void testIntPointEquals() {
		IntPoint p1 = new IntPoint(10, -20);
		IntPoint p2 = new IntPoint(10, -20);
		IntPoint p3 = new IntPoint(10, 30);
		IntPoint p4 = new IntPoint(40, -20);

		assertEquals(p1, p1);
		assertEquals(p1, p2);
		assertEquals(p2, p1);
		assertNotEquals(p1, p3);
		assertNotEquals(p1, p4);
		assertNotEquals(p1, null);
		assertNotEquals(p1, "string");

		assertEquals(p1.hashCode(), p2.hashCode());
	}

	@Test
	public void testDoubleVectorEquals() {
		DoubleVector v1 = new DoubleVector(0.5, -0.25);
		DoubleVector v2 = new DoubleVector(0.5, -0.25);
		DoubleVector v3 = new DoubleVector(0.5, 0.5);
		DoubleVector v4 = new DoubleVector(-0.5, -0.25);

		assertEquals(v1, v1);
		assertEquals(v1, v2);
		assertEquals(v2, v1);
		assertNotEquals(v1, v3);
		assertNotEquals(v1, v4);
		assertNotEquals(v1, null);
		assertNotEquals(v1, "string");

		assertEquals(v1.hashCode(), v2.hashCode());
	}

	@Test
	public void testLongVectorEquals() {
		LongVector v1 = new LongVector(100L, 200L);
		LongVector v2 = new LongVector(100L, 200L);
		LongVector v3 = new LongVector(100L, 300L);
		LongVector v4 = new LongVector(400L, 200L);

		assertEquals(v1, v1);
		assertEquals(v1, v2);
		assertEquals(v2, v1);
		assertNotEquals(v1, v3);
		assertNotEquals(v1, v4);
		assertNotEquals(v1, null);
		assertNotEquals(v1, "string");

		assertEquals(v1.hashCode(), v2.hashCode());
	}
}
