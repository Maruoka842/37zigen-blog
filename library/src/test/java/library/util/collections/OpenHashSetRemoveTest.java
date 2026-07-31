package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OpenHashSetRemoveTest {

    @Test
    public void testOpenHashSetBasicRemove() {
        OpenHashSet<String> set = new OpenHashSet<>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        assertFalse(set.remove("apple"));

        set.add("apple");
        assertFalse(set.isEmpty());
        assertEquals(1, set.size());
        assertTrue(set.contains("apple"));

        assertTrue(set.remove("apple"));
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertFalse(set.contains("apple"));

        // Removing already removed
        assertFalse(set.remove("apple"));
    }

    @Test
    public void testOpenHashSetLinearProbingAndTombstones() {
        // Let's force linear probing by adding elements that hash to the same or nearby positions
        // Since we don't have direct access to internal array size or hash without reflection,
        // we can add multiple elements, remove some, and ensure correctness of operations.
        OpenHashSet<Integer> set = new OpenHashSet<>();

        for (int i = 0; i < 20; i++) {
            set.add(i);
        }
        assertEquals(20, set.size());

        // Remove even elements
        for (int i = 0; i < 20; i += 2) {
            assertTrue(set.remove(i));
        }
        assertEquals(10, set.size());

        // Verify remaining
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 1) {
                assertTrue(set.contains(i));
            } else {
                assertFalse(set.contains(i));
            }
        }

        // Add back some elements to see if tombstones are reused correctly and duplicates are prevented
        set.add(4); // was removed, should be re-added
        assertTrue(set.contains(4));
        assertEquals(11, set.size());

        set.add(4); // already added, size should not change
        assertEquals(11, set.size());

        set.add(5); // already exists, size should not change
        assertEquals(11, set.size());
    }

    @Test
    public void testLongOpenHashSetBasicRemove() {
        LongOpenHashSet set = new LongOpenHashSet();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        assertFalse(set.remove(10L));

        set.add(10L);
        assertFalse(set.isEmpty());
        assertEquals(1, set.size());
        assertTrue(set.contains(10L));

        assertTrue(set.remove(10L));
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertFalse(set.contains(10L));

        // Removing already removed
        assertFalse(set.remove(10L));
    }

    @Test
    public void testLongOpenHashSetZeroHandling() {
        LongOpenHashSet set = new LongOpenHashSet();
        assertFalse(set.contains(0L));

        set.add(0L);
        assertTrue(set.contains(0L));
        assertEquals(0, set.size()); // size doesn't include containsZero

        assertTrue(set.remove(0L));
        assertFalse(set.contains(0L));
        assertFalse(set.remove(0L));
    }

    @Test
    public void testLongOpenHashSetLinearProbingAndTombstones() {
        LongOpenHashSet set = new LongOpenHashSet();

        for (long i = 1; i <= 20; i++) {
            set.add(i);
        }
        assertEquals(20, set.size());

        // Remove even elements
        for (long i = 2; i <= 20; i += 2) {
            assertTrue(set.remove(i));
        }
        assertEquals(10, set.size());

        // Verify remaining
        for (long i = 1; i <= 20; i++) {
            if (i % 2 == 1) {
                assertTrue(set.contains(i));
            } else {
                assertFalse(set.contains(i));
            }
        }

        // Add back some elements to see if tombstones are reused correctly and duplicates are prevented
        set.add(4L); // was removed, should be re-added
        assertTrue(set.contains(4L));
        assertEquals(11, set.size());

        set.add(4L); // already added, size should not change
        assertEquals(11, set.size());

        set.add(5L); // already exists, size should not change
        assertEquals(11, set.size());
    }
}
