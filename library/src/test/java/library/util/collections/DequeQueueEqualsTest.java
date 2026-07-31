package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DequeQueueEqualsTest {

	@Test
	public void testCharDequeEquals() {
		CharDeque d1 = new CharDeque();
		CharDeque d2 = new CharDeque();
		assertEquals(d1, d2);

		d1.addLast('a');
		d1.addLast('b');
		assertNotEquals(d1, d2);

		d2.addLast('a');
		d2.addLast('b');
		assertEquals(d1, d2);

		d2.pollFirst();
		assertNotEquals(d1, d2);

		assertEquals(d1, d1);
		assertNotEquals(d1, null);
		assertNotEquals(d1, "string");
	}

	@Test
	public void testDoubleDequeEquals() {
		DoubleDeque d1 = new DoubleDeque();
		DoubleDeque d2 = new DoubleDeque();
		assertEquals(d1, d2);

		d1.addLast(1.5);
		d1.addLast(2.5);
		assertNotEquals(d1, d2);

		d2.addLast(1.5);
		d2.addLast(2.5);
		assertEquals(d1, d2);

		d2.pollFirst();
		assertNotEquals(d1, d2);
	}

	@Test
	public void testIntDequeEquals() {
		IntDeque d1 = new IntDeque();
		IntDeque d2 = new IntDeque();
		assertEquals(d1, d2);

		d1.addLast(10);
		d1.addLast(20);
		assertNotEquals(d1, d2);

		d2.addLast(10);
		d2.addLast(20);
		assertEquals(d1, d2);

		d2.pollFirst();
		assertNotEquals(d1, d2);
	}

	@Test
	public void testLongDequeEquals() {
		LongDeque d1 = new LongDeque();
		LongDeque d2 = new LongDeque();
		assertEquals(d1, d2);

		d1.addLast(100L);
		d1.addLast(200L);
		assertNotEquals(d1, d2);

		d2.addLast(100L);
		d2.addLast(200L);
		assertEquals(d1, d2);

		d2.pollFirst();
		assertNotEquals(d1, d2);
	}

	@Test
	public void testObjectDequeEquals() {
		ObjectDeque<String> d1 = new ObjectDeque<>();
		ObjectDeque<String> d2 = new ObjectDeque<>();
		assertEquals(d1, d2);

		d1.addLast("hello");
		d1.addLast("world");
		assertNotEquals(d1, d2);

		d2.addLast("hello");
		d2.addLast("world");
		assertEquals(d1, d2);

		d2.pollFirst();
		assertNotEquals(d1, d2);
	}

	@Test
	public void testCharQueueEquals() {
		CharQueue q1 = new CharQueue();
		CharQueue q2 = new CharQueue();
		assertEquals(q1, q2);

		q1.add('x');
		assertNotEquals(q1, q2);

		q2.add('x');
		assertEquals(q1, q2);

		assertEquals(q1.hashCode(), q2.hashCode());
	}

	@Test
	public void testDoubleQueueEquals() {
		DoubleQueue q1 = new DoubleQueue();
		DoubleQueue q2 = new DoubleQueue();
		assertEquals(q1, q2);

		q1.add(3.14);
		assertNotEquals(q1, q2);

		q2.add(3.14);
		assertEquals(q1, q2);

		assertEquals(q1.hashCode(), q2.hashCode());
	}

	@Test
	public void testIntQueueEquals() {
		IntQueue q1 = new IntQueue();
		IntQueue q2 = new IntQueue();
		assertEquals(q1, q2);

		q1.add(42);
		assertNotEquals(q1, q2);

		q2.add(42);
		assertEquals(q1, q2);

		assertEquals(q1.hashCode(), q2.hashCode());
	}

	@Test
	public void testLongQueueEquals() {
		LongQueue q1 = new LongQueue();
		LongQueue q2 = new LongQueue();
		assertEquals(q1, q2);

		q1.add(1000L);
		assertNotEquals(q1, q2);

		q2.add(1000L);
		assertEquals(q1, q2);

		assertEquals(q1.hashCode(), q2.hashCode());
	}
}
