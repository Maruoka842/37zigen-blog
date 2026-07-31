package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.collections.CharDeque;
import library.util.collections.DoubleDeque;
import library.util.collections.IntDeque;
import library.util.collections.LongDeque;
import library.util.collections.ObjectDeque;

public class DequeStressTest {
    private static final int ITERATIONS = 3000;
    private static final Random random = new Random();

    @Test
    public void testIntDeque() {
        IntDeque deque = new IntDeque();
        ArrayList<Integer> reference = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            int op = random.nextInt(10);
            int val = random.nextInt(1000);
            switch (op) {
                case 0: deque.addFirst(val); reference.add(0, val); break;
                case 1: deque.addLast(val); reference.add(val); break;
                case 2: if (!reference.isEmpty()) assertEquals((int)reference.remove(0), deque.pollFirst()); break;
                case 3: if (!reference.isEmpty()) assertEquals((int)reference.remove(reference.size() - 1), deque.pollLast()); break;
                case 4: if (!reference.isEmpty()) {
                    assertEquals((int)reference.get(0), deque.peekFirst());
                    assertEquals((int)reference.get(reference.size() - 1), deque.peekLast());
                } break;
                case 5:
                    assertEquals(reference.size(), deque.size());
                    assertEquals(reference.isEmpty(), deque.isEmpty());
                    assertEquals(!reference.isEmpty(), deque.isNonEmpty());
                    break;
                case 6: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    assertEquals((int)reference.get(idx), deque.get(idx));
                } break;
                case 7: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    deque.set(idx, val);
                    reference.set(idx, val);
                } break;
                case 8: deque.clear(); reference.clear(); break;
                case 9: {
                    IntDeque copy = deque.copy();
                    assertEquals(deque.size(), copy.size());
                    for (int j = 0; j < deque.size(); j++) assertEquals(deque.get(j), copy.get(j));
                } break;
            }
            assertArrayEquals(reference.stream().mapToInt(Integer::intValue).toArray(), deque.toArray());
        }
    }

    @Test
    public void testLongDeque() {
        LongDeque deque = new LongDeque();
        ArrayList<Long> reference = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            int op = random.nextInt(10);
            long val = random.nextLong();
            switch (op) {
                case 0: deque.addFirst(val); reference.add(0, val); break;
                case 1: deque.addLast(val); reference.add(val); break;
                case 2: if (!reference.isEmpty()) assertEquals((long)reference.remove(0), deque.pollFirst()); break;
                case 3: if (!reference.isEmpty()) assertEquals((long)reference.remove(reference.size() - 1), deque.pollLast()); break;
                case 4: if (!reference.isEmpty()) {
                    assertEquals((long)reference.get(0), deque.peekFirst());
                    assertEquals((long)reference.get(reference.size() - 1), deque.peekLast());
                } break;
                case 5:
                    assertEquals(reference.size(), deque.size());
                    assertEquals(reference.isEmpty(), deque.isEmpty());
                    assertEquals(!reference.isEmpty(), deque.isNonEmpty());
                    break;
                case 6: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    assertEquals((long)reference.get(idx), deque.get(idx));
                } break;
                case 7: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    deque.set(idx, val);
                    reference.set(idx, val);
                } break;
                case 8: deque.clear(); reference.clear(); break;
                case 9: {
                    LongDeque copy = deque.copy();
                    assertEquals(deque.size(), copy.size());
                    for (int j = 0; j < deque.size(); j++) assertEquals(deque.get(j), copy.get(j));
                } break;
            }
            assertArrayEquals(reference.stream().mapToLong(Long::longValue).toArray(), deque.toArray());
        }
    }

    @Test
    public void testDoubleDeque() {
        DoubleDeque deque = new DoubleDeque();
        ArrayList<Double> reference = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            int op = random.nextInt(10);
            double val = random.nextDouble();
            switch (op) {
                case 0: deque.addFirst(val); reference.add(0, val); break;
                case 1: deque.addLast(val); reference.add(val); break;
                case 2: if (!reference.isEmpty()) assertEquals((double)reference.remove(0), deque.pollFirst()); break;
                case 3: if (!reference.isEmpty()) assertEquals((double)reference.remove(reference.size() - 1), deque.pollLast()); break;
                case 4: if (!reference.isEmpty()) {
                    assertEquals((double)reference.get(0), deque.peekFirst());
                    assertEquals((double)reference.get(reference.size() - 1), deque.peekLast());
                } break;
                case 5:
                    assertEquals(reference.size(), deque.size());
                    assertEquals(reference.isEmpty(), deque.isEmpty());
                    assertEquals(!reference.isEmpty(), deque.isNonEmpty());
                    break;
                case 6: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    assertEquals((double)reference.get(idx), deque.get(idx));
                } break;
                case 7: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    deque.set(idx, val);
                    reference.set(idx, val);
                } break;
                case 8: deque.clear(); reference.clear(); break;
                case 9: {
                    DoubleDeque copy = deque.copy();
                    assertEquals(deque.size(), copy.size());
                    for (int j = 0; j < deque.size(); j++) assertEquals(deque.get(j), copy.get(j));
                } break;
            }
            assertArrayEquals(reference.stream().mapToDouble(Double::doubleValue).toArray(), deque.toArray());
        }
    }

    @Test
    public void testCharDeque() {
        CharDeque deque = new CharDeque();
        ArrayList<Character> reference = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            int op = random.nextInt(10);
            char val = (char) ('a' + random.nextInt(26));
            switch (op) {
                case 0: deque.addFirst(val); reference.add(0, val); break;
                case 1: deque.addLast(val); reference.add(val); break;
                case 2: if (!reference.isEmpty()) assertEquals((char)reference.remove(0), deque.pollFirst()); break;
                case 3: if (!reference.isEmpty()) assertEquals((char)reference.remove(reference.size() - 1), deque.pollLast()); break;
                case 4: if (!reference.isEmpty()) {
                    assertEquals((char)reference.get(0), deque.peekFirst());
                    assertEquals((char)reference.get(reference.size() - 1), deque.peekLast());
                } break;
                case 5:
                    assertEquals(reference.size(), deque.size());
                    assertEquals(reference.isEmpty(), deque.isEmpty());
                    assertEquals(!reference.isEmpty(), deque.isNonEmpty());
                    break;
                case 6: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    assertEquals((char)reference.get(idx), deque.get(idx));
                } break;
                case 7: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    deque.set(idx, val);
                    reference.set(idx, val);
                } break;
                case 8: deque.clear(); reference.clear(); break;
                case 9: {
                    CharDeque copy = deque.copy();
                    assertEquals(deque.size(), copy.size());
                    for (int j = 0; j < deque.size(); j++) assertEquals(deque.get(j), copy.get(j));
                } break;
            }
            char[] expected = new char[reference.size()];
            for(int j=0; j<reference.size(); j++) expected[j] = reference.get(j);
            assertArrayEquals(expected, deque.toArray());
        }
    }

    @Test
    public void testObjectDeque() {
        ObjectDeque<String> deque = new ObjectDeque<>();
        ArrayList<String> reference = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            int op = random.nextInt(10);
            String val = "v" + random.nextInt(1000);
            switch (op) {
                case 0: deque.addFirst(val); reference.add(0, val); break;
                case 1: deque.addLast(val); reference.add(val); break;
                case 2: if (!reference.isEmpty()) assertEquals(reference.remove(0), deque.pollFirst()); break;
                case 3: if (!reference.isEmpty()) assertEquals(reference.remove(reference.size() - 1), deque.pollLast()); break;
                case 4: if (!reference.isEmpty()) {
                    assertEquals(reference.get(0), deque.peekFirst());
                    assertEquals(reference.get(reference.size() - 1), deque.peekLast());
                } break;
                case 5:
                    assertEquals(reference.size(), deque.size());
                    assertEquals(reference.isEmpty(), deque.isEmpty());
                    assertEquals(!reference.isEmpty(), deque.isNonEmpty());
                    break;
                case 6: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    assertEquals(reference.get(idx), deque.get(idx));
                } break;
                case 7: if (!reference.isEmpty()) {
                    int idx = random.nextInt(reference.size());
                    deque.set(idx, val);
                    reference.set(idx, val);
                } break;
                case 8: deque.clear(); reference.clear(); break;
                case 9: {
                    ObjectDeque<String> copy = deque.copy();
                    assertEquals(deque.size(), copy.size());
                    for (int j = 0; j < deque.size(); j++) assertEquals(deque.get(j), copy.get(j));
                } break;
            }
            assertArrayEquals(reference.toArray(), deque.toArray());
        }
    }

    @Test
    public void testIterator() {
        IntDeque deque = new IntDeque();
        ArrayList<Integer> reference = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int val = random.nextInt(1000);
            deque.addLast(val);
            reference.add(val);
        }

        Iterator<Integer> it1 = reference.iterator();
        Iterator<Integer> it2 = deque.iterator();
        while(it1.hasNext()) {
            assertTrue(it2.hasNext());
            assertEquals(it1.next(), it2.next());
        }
        assertFalse(it2.hasNext());
    }

    @Test
    public void testExceptions() {
        IntDeque deque = new IntDeque();
        assertThrows(NoSuchElementException.class, () -> deque.pollFirst());
        assertThrows(NoSuchElementException.class, () -> deque.pollLast());
        assertThrows(NoSuchElementException.class, () -> deque.peekFirst());
        assertThrows(NoSuchElementException.class, () -> deque.peekLast());
        assertThrows(IndexOutOfBoundsException.class, () -> deque.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> deque.set(0, 1));
    }
}
