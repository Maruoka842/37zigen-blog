package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class MultiSetSetEqualsTest {

	@Test
	public void testHashMultiSetEquals() {
		HashMultiSet<String> s1 = new HashMultiSet<>();
		HashMultiSet<String> s2 = new HashMultiSet<>();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("apple");
		assertNotEquals(s1, s2);

		s2.add("apple");
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("banana", 2);
		s2.add("banana", 1);
		assertNotEquals(s1, s2);

		s2.add("banana", 1);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testTreeMultiSetEquals() {
		TreeMultiSet<String> s1 = new TreeMultiSet<>();
		TreeMultiSet<String> s2 = new TreeMultiSet<>();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("apple");
		assertNotEquals(s1, s2);

		s2.add("apple");
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("banana", 2);
		s2.add("banana", 1);
		assertNotEquals(s1, s2);

		s2.add("banana", 1);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testLastKMultiSetEquals() {
		BinaryOperator<Integer> add = (x, y) -> x + y;
		UnaryOperator<Integer> inv = x -> -x;
		LastKMultiSet<Integer> s1 = new LastKMultiSet<>(2, add, inv, 0);
		LastKMultiSet<Integer> s2 = new LastKMultiSet<>(2, add, inv, 0);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(5);
		assertNotEquals(s1, s2);

		s2.add(5);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(10);
		s2.add(10);
		assertEquals(s1, s2);

		s1.add(3);
		s2.add(3);
		assertEquals(s1, s2);
	}

	@Test
	public void testFirstKMultiSetEquals() {
		BinaryOperator<Integer> add = (x, y) -> x + y;
		UnaryOperator<Integer> inv = x -> -x;
		FirstKMultiSet<Integer> s1 = new FirstKMultiSet<>(2, add, inv, 0);
		FirstKMultiSet<Integer> s2 = new FirstKMultiSet<>(2, add, inv, 0);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(5);
		assertNotEquals(s1, s2);

		s2.add(5);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testIntTreapMultiSetEquals() {
		IntTreapMultiSet s1 = new IntTreapMultiSet();
		IntTreapMultiSet s2 = new IntTreapMultiSet();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(10);
		assertNotEquals(s1, s2);

		s2.add(10);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(20, 3);
		s2.add(20, 2);
		assertNotEquals(s1, s2);

		s2.add(20, 1);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testLongTreapMultiSetEquals() {
		LongTreapMultiSet s1 = new LongTreapMultiSet();
		LongTreapMultiSet s2 = new LongTreapMultiSet();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(100L);
		assertNotEquals(s1, s2);

		s2.add(100L);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(200L, 3);
		s2.add(200L, 2);
		assertNotEquals(s1, s2);

		s2.add(200L, 1);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testIntTreapSetEquals() {
		IntTreapSet s1 = new IntTreapSet();
		IntTreapSet s2 = new IntTreapSet();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(5);
		assertNotEquals(s1, s2);

		s2.add(5);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(10);
		s1.add(3);
		s2.add(3);
		s2.add(10);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testLongTreapSetEquals() {
		LongTreapSet s1 = new LongTreapSet();
		LongTreapSet s2 = new LongTreapSet();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(500L);
		assertNotEquals(s1, s2);

		s2.add(500L);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(1000L);
		s1.add(300L);
		s2.add(300L);
		s2.add(1000L);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testTreapSetEquals() {
		TreapSet<String> s1 = new TreapSet<>();
		TreapSet<String> s2 = new TreapSet<>();
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("hello");
		assertNotEquals(s1, s2);

		s2.add("hello");
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add("world");
		s1.add("abc");
		s2.add("abc");
		s2.add("world");
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	public void testShiftableTreapSetEquals() {
		ShiftableTreapSet.Strategy<Integer, Integer> strategy = new ShiftableTreapSet.Strategy<>() {
			@Override
			public Integer mergeA(Integer newer, Integer older) {
				return (newer == null ? 0 : newer) + (older == null ? 0 : older);
			}

			@Override
			public Integer mergeAX(Integer a, Integer b) {
				return (a == null ? 0 : a) + (b == null ? 0 : b);
			}
		};

		ShiftableTreapSet<Integer, Integer> s1 = new ShiftableTreapSet<>(strategy);
		ShiftableTreapSet<Integer, Integer> s2 = new ShiftableTreapSet<>(strategy);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(5);
		assertNotEquals(s1, s2);

		s2.add(5);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());

		s1.add(10);
		s1.add(2);
		s2.add(2);
		s2.add(10);
		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}
}
