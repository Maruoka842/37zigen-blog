package library.util.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoordinateOrderedSet2DTest {

	@Test
	public void testSizeAndBasicOperations() {
		CoordinateOrderedSet2D set = new CoordinateOrderedSet2D();
		assertEquals(0, set.size());

		set.add(1, 2);
		assertEquals(1, set.size());
		assertTrue(set.contains(1, 2));

		// Adding the same coordinate should not increase size
		set.add(1, 2);
		assertEquals(1, set.size());

		set.add(1, 3);
		assertEquals(2, set.size());
		assertTrue(set.contains(1, 3));

		set.add(2, 2);
		assertEquals(3, set.size());
		assertTrue(set.contains(2, 2));

		// Removing non-existent coordinate
		assertFalse(set.remove(3, 3));
		assertEquals(3, set.size());

		// Removing existing coordinate
		assertTrue(set.remove(1, 2));
		assertEquals(2, set.size());
		assertFalse(set.contains(1, 2));

		// Removing the same coordinate again should fail and not decrease size
		assertFalse(set.remove(1, 2));
		assertEquals(2, set.size());

		// Query tests
		assertEquals(2, set.ceilXFixingY(1, 2));
		assertNull(set.ceilXFixingY(3, 2));
		assertEquals(2, set.floorXFixingY(2, 2));

		assertEquals(3, set.ceilYFixingX(1, 1));
		assertEquals(3, set.floorYFixingX(1, 4));
	}

	@Test
	public void testIsEmpty() {
		CoordinateOrderedSet2D set = new CoordinateOrderedSet2D();
		assertTrue(set.isEmpty());

		set.add(1, 2);
		assertFalse(set.isEmpty());

		set.remove(1, 2);
		assertTrue(set.isEmpty());
	}

	@Test
	public void testDump() {
		CoordinateOrderedSet2D set = new CoordinateOrderedSet2D();

		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		java.io.PrintStream originalOut = System.out;
		System.setOut(new java.io.PrintStream(out));
		try {
			set.dump();
			assertEquals("空集合\n", out.toString().replace("\r\n", "\n"));

			out.reset();
			set.add(2, 5);
			set.add(1, 3);
			set.add(2, 1);
			set.dump();

			String expected = "(1, 3)\n" +
			                  "(2, 1)\n" +
			                  "(2, 5)\n";
			assertEquals(expected, out.toString().replace("\r\n", "\n"));
		} finally {
			System.setOut(originalOut);
		}
	}
}
