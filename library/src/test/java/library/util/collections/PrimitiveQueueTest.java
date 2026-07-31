package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PrimitiveQueueTest {

    @Test
    public void testCharQueue() {
        CharQueue q = new CharQueue();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());

        q.add('a');
        q.add('b');
        q.add('c');

        assertFalse(q.isEmpty());
        assertEquals(3, q.size());
        assertEquals('a', q.peek());
        assertEquals('a', q.get(0));
        assertEquals('b', q.get(1));
        assertEquals('c', q.get(2));

        assertEquals('a', q.poll());
        assertEquals(2, q.size());
        assertEquals('b', q.peek());

        CharQueue copy = q.copy();
        assertEquals(2, copy.size());
        assertEquals('b', copy.poll());
        assertEquals('c', copy.poll());
        assertTrue(copy.isEmpty());

        assertFalse(q.isEmpty());
        assertEquals(2, q.size());
    }

    @Test
    public void testDoubleQueue() {
        DoubleQueue q = new DoubleQueue();
        assertTrue(q.isEmpty());

        q.add(1.1);
        q.add(2.2);

        assertEquals(2, q.size());
        assertEquals(1.1, q.peek(), 1e-9);
        assertEquals(1.1, q.poll(), 1e-9);
        assertEquals(2.2, q.peek(), 1e-9);
    }

    @Test
    public void testLongQueue() {
        LongQueue q = new LongQueue();
        assertTrue(q.isEmpty());

        q.add(10L);
        q.add(20L);

        assertEquals(2, q.size());
        assertEquals(10L, q.peek());
        assertEquals(10L, q.poll());
        assertEquals(20L, q.peek());
    }
}
