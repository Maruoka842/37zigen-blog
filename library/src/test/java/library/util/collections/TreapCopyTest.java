package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;

public class TreapCopyTest {

    @Test
    public void testTreapMapCopy() {
        TreapMap emptyMap = new TreapMap();
        TreapMap emptyCopy = emptyMap.copy();
        assertEquals(emptyMap, emptyCopy);
        assertEquals(0, emptyCopy.size());

        TreapMap original = new TreapMap();
        original.put(10, 100);
        original.put(5, 50);
        original.put(15, 150);

        TreapMap copy = original.copy();
        assertEquals(original, copy);
        assertEquals(original.size(), copy.size());

        // Assert structural equivalence
        assertTreapMapIdentical(original.getRoot(), copy.getRoot());

        // Verify independent state
        copy.put(10, 200);
        assertEquals(200, copy.get(10));
        assertEquals(100, original.get(10));

        copy.put(20, 200);
        assertTrue(copy.containsKey(20));
        assertFalse(original.containsKey(20));
    }

    private void assertTreapMapIdentical(TreapMap.Node n1, TreapMap.Node n2) {
        if (n1 == null && n2 == null) {
            return;
        }
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotSame(n1, n2);
        assertEquals(n1.key, n2.key);
        assertEquals(n1.val, n2.val);
        assertEquals(n1.priority, n2.priority);
        assertEquals(n1.size, n2.size);
        assertEquals(n1.sum, n2.sum);
        assertTreapMapIdentical(n1.left, n2.left);
        assertTreapMapIdentical(n1.right, n2.right);
    }

    @Test
    public void testTreapSetCopy() {
        TreapSet<String> emptySet = new TreapSet<>();
        TreapSet<String> emptyCopy = emptySet.copy();
        assertEquals(emptySet, emptyCopy);
        assertEquals(0, emptyCopy.size());

        TreapSet<String> original = new TreapSet<>();
        original.add("banana");
        original.add("apple");
        original.add("cherry");

        TreapSet<String> copy = original.copy();
        assertEquals(original, copy);
        assertEquals(original.size(), copy.size());

        assertTreapSetIdentical(original.root(), copy.root());

        copy.add("date");
        assertTrue(copy.contains("date"));
        assertFalse(original.contains("date"));

        copy.remove("apple");
        assertFalse(copy.contains("apple"));
        assertTrue(original.contains("apple"));
    }

    private <T extends Comparable<? super T>> void assertTreapSetIdentical(TreapSet<T>.Node n1, TreapSet<T>.Node n2) {
        if (n1 == null && n2 == null) {
            return;
        }
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotSame(n1, n2);
        assertEquals(n1.key(), n2.key());
        assertEquals(n1.priority, n2.priority);
        assertEquals(n1.size(), n2.size());
        assertTreapSetIdentical(n1.left, n2.left);
        assertTreapSetIdentical(n1.right, n2.right);
    }

    @Test
    public void testLongTreapSetCopy() {
        LongTreapSet emptySet = new LongTreapSet();
        LongTreapSet emptyCopy = emptySet.copy();
        assertEquals(emptySet, emptyCopy);
        assertEquals(0, emptyCopy.size());

        LongTreapSet original = new LongTreapSet();
        original.add(10L);
        original.add(5L);
        original.add(15L);

        LongTreapSet copy = original.copy();
        assertEquals(original, copy);
        assertEquals(original.size(), copy.size());

        assertLongTreapSetIdentical(original.root(), copy.root());

        copy.add(20L);
        assertTrue(copy.contains(20L));
        assertFalse(original.contains(20L));

        copy.remove(5L);
        assertFalse(copy.contains(5L));
        assertTrue(original.contains(5L));
    }

    private void assertLongTreapSetIdentical(LongTreapSet.Node n1, LongTreapSet.Node n2) {
        if (n1 == null && n2 == null) {
            return;
        }
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotSame(n1, n2);
        assertEquals(n1.key(), n2.key());
        assertEquals(n1.priority, n2.priority);
        assertEquals(n1.size(), n2.size());
        assertLongTreapSetIdentical(n1.left, n2.left);
        assertLongTreapSetIdentical(n1.right, n2.right);
    }
}
