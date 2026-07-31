package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class LongTreapMultiSetTest {

    @Test
    public void testBasicOperations() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        set.add(5L);
        set.add(3L);
        set.add(5L);
        set.add(7L);

        assertFalse(set.isEmpty());
        assertEquals(4, set.size());
        assertEquals(3, set.numberDistinctElements());

        assertEquals(2, set.count(5L));
        assertEquals(1, set.count(3L));
        assertEquals(1, set.count(7L));
        assertEquals(0, set.count(10L));

        assertTrue(set.contains(5L));
        assertFalse(set.contains(10L));

        assertTrue(set.remove(5L));
        assertEquals(1, set.count(5L));
        assertEquals(3, set.size());

        set.add(5L, 3L);
        assertEquals(4, set.count(5L));
        assertEquals(6, set.size());

        set.remove(5L, 2L);
        assertEquals(2, set.count(5L));
        assertEquals(4, set.size());

        set.remove(3L, 1L);
        assertFalse(set.contains(3L));
        assertEquals(2, set.numberDistinctElements());
    }

    @Test
    public void testNavigation() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        set.add(10L);
        set.add(20L);
        set.add(30L);

        assertEquals(10L, set.peekFirst());
        assertEquals(30L, set.peekLast());

        assertEquals(10L, set.floor(15L));
        assertEquals(20L, set.floor(20L));
        assertEquals(20L, set.ceil(15L));
        assertEquals(20L, set.ceil(20L));

        assertEquals(10L, set.lower(20L));
        assertEquals(30L, set.higher(20L));
        assertNull(set.lower(10L));
        assertNull(set.higher(30L));

        TreapMap.Entry first = set.peekFirstEntry();
        assertEquals(10L, first.key);
        assertEquals(1, first.value);

        TreapMap.Entry last = set.peekLastEntry();
        assertEquals(30L, last.key);
        assertEquals(1, last.value);
    }

    @Test
    public void testRankAndMex() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        // {0, 1, 1, 3, 3, 3}
        set.add(0L);
        set.add(1L, 2L);
        set.add(3L, 3L);

        assertEquals(6, set.size());
        assertEquals(0L, set.kthKey(0));
        assertEquals(1L, set.kthKey(1));
        assertEquals(1L, set.kthKey(2));
        assertEquals(3L, set.kthKey(3));
        assertEquals(3L, set.kthKey(4));
        assertEquals(3L, set.kthKey(5));
        assertNull(set.kthKey(6));

        assertEquals(1, set.countLeq(0L));
        assertEquals(3, set.countLeq(1L));
        assertEquals(3, set.countLeq(2L));
        assertEquals(6, set.countLeq(3L));

        assertEquals(6, set.countGeq(0L));
        assertEquals(5, set.countGeq(1L));
        assertEquals(3, set.countGeq(3L));

        // mex({0, 1, 1, 3, 3, 3}) = 2
        assertEquals(2L, set.mex());

        set.add(2L);
        // mex({0, 1, 1, 2, 3, 3, 3}) = 4
        assertEquals(4L, set.mex());

        set.remove(0L);
        // mex({1, 1, 2, 3, 3, 3}) = 0
        assertEquals(0L, set.mex());
    }

    @Test
    public void testPoll() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        set.add(10L);
        set.add(5L);
        set.add(15L);

        assertEquals(5L, set.pollFirst());
        assertEquals(15L, set.pollLast());
        assertEquals(10L, set.peekFirst());
        assertEquals(1, set.size());
    }

    @Test
    public void testNegativeAndLarge() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        set.add(-10L);
        set.add(-5L);
        set.add(0L);

        assertEquals(-10L, set.peekFirst());
        assertEquals(0L, set.peekLast());
        assertEquals(1L, set.mex()); // mex of {-10, -5, 0} is 1

        set.add(Long.MAX_VALUE);
        assertTrue(set.contains(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, set.peekLast());

        set.add(Long.MIN_VALUE);
        assertTrue(set.contains(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, set.peekFirst());
    }

    @Test
    public void testCopy() {
        LongTreapMultiSet emptySet = new LongTreapMultiSet();
        LongTreapMultiSet emptyCopy = emptySet.copy();
        assertEquals(emptySet, emptyCopy);
        assertEquals(0, emptyCopy.size());

        LongTreapMultiSet original = new LongTreapMultiSet();
        original.add(10L, 2L);
        original.add(5L, 1L);
        original.add(15L, 3L);

        LongTreapMultiSet copy = original.copy();
        assertEquals(original, copy);
        assertEquals(original.size(), copy.size());
        assertEquals(original.numberDistinctElements(), copy.numberDistinctElements());

        // Modify copy and assert independence
        copy.add(10L, 1L);
        assertEquals(3L, copy.count(10L));
        assertEquals(2L, original.count(10L));

        copy.add(20L, 5L);
        assertTrue(copy.contains(20L));
        assertFalse(original.contains(20L));

        copy.remove(5L);
        assertFalse(copy.contains(5L));
        assertTrue(original.contains(5L));
    }
}
