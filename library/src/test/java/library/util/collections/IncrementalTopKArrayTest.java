package library.util.collections;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

public class IncrementalTopKArrayTest {

    static class Item {
        final String key;
        final int value;

        Item(String key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    @Test
    public void testDistinctTrueBasic() {
        IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3);
        assertTrue(arr.isEmpty());
        assertEquals(0, arr.size());

        assertTrue(arr.add(10));
        assertEquals(1, arr.size());
        assertEquals(10, arr.max());

        assertTrue(arr.add(5));
        assertEquals(2, arr.size());
        assertEquals(10, arr.max());
        assertEquals(5, arr.secondMax());

        assertTrue(arr.add(20));
        assertEquals(3, arr.size());
        assertEquals(20, arr.max());
        assertEquals(10, arr.secondMax());

        // Adding duplicate (already distinct)
        assertFalse(arr.add(10));
        assertEquals(3, arr.size());

        // Adding element smaller than all elements
        assertFalse(arr.add(3));
        assertEquals(3, arr.size());

        // Adding larger element when full
        assertTrue(arr.add(15));
        assertEquals(3, arr.size());
        assertEquals(20, arr.max());
        assertEquals(15, arr.secondMax());

        // Iterate
        List<Integer> list = new ArrayList<>();
        for (int v : arr) {
            list.add(v);
        }
        assertEquals(List.of(20, 15, 10), list);
    }

    @Test
    public void testDistinctFalseBasic() {
        // distinct = false mode
        IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3, false);
        assertTrue(arr.isEmpty());

        assertTrue(arr.add(10));
        assertTrue(arr.add(5));
        assertTrue(arr.add(10)); // Duplicate allowed
        assertEquals(3, arr.size());
        assertEquals(10, arr.max());
        assertEquals(10, arr.secondMax());

        // Now full with [10, 10, 5]. Add 10.
        assertTrue(arr.add(10));
        assertEquals(3, arr.size());
        assertEquals(10, arr.max());
        assertEquals(10, arr.secondMax());

        List<Integer> list = new ArrayList<>();
        for (int v : arr) {
            list.add(v);
        }
        assertEquals(List.of(10, 10, 10), list);

        // Add 20. It should replace the last 10.
        assertTrue(arr.add(20));
        list.clear();
        for (int v : arr) {
            list.add(v);
        }
        assertEquals(List.of(20, 10, 10), list);

        // Add 5. It is smaller than the smallest (10), so should return false.
        assertFalse(arr.add(5));
    }

    @Test
    public void testDistinctTrueWithCustomObject() {
        Comparator<Item> comp = (a, b) -> Integer.compare(a.value, b.value);
        IncrementalTopKArray<Item> arr = new IncrementalTopKArray<>(3, comp, (a, b) -> a.key.equals(b.key), true);

        assertTrue(arr.add(new Item("A", 10)));
        assertTrue(arr.add(new Item("B", 5)));

        // Add B with smaller value -> should return false and not update
        assertFalse(arr.add(new Item("B", 3)));
        assertEquals(2, arr.size());
        assertEquals(10, arr.max().value);
        assertEquals(5, arr.secondMax().value);

        // Add B with larger value -> should update and bubble up
        assertTrue(arr.add(new Item("B", 12)));
        assertEquals(2, arr.size());
        assertEquals("B", arr.max().key);
        assertEquals(12, arr.max().value);
        assertEquals("A", arr.secondMax().key);
        assertEquals(10, arr.secondMax().value);
    }

    @Test
    public void testDistinctFalseWithCustomObject() {
        Comparator<Item> comp = (a, b) -> Integer.compare(a.value, b.value);
        IncrementalTopKArray<Item> arr = new IncrementalTopKArray<>(3, comp, (a, b) -> a.key.equals(b.key), false);

        assertTrue(arr.add(new Item("A", 10)));
        assertTrue(arr.add(new Item("B", 5)));

        // Add B with smaller value -> since distinct is false, it's treated as a new element
        assertTrue(arr.add(new Item("B", 3)));
        assertEquals(3, arr.size());
        assertEquals("A", arr.max().key);
        assertEquals(10, arr.max().value);
        assertEquals("B", arr.secondMax().key);
        assertEquals(5, arr.secondMax().value);

        // Add B with larger value -> should be inserted and push the smallest out
        assertTrue(arr.add(new Item("B", 12)));
        assertEquals(3, arr.size());
        assertEquals("B", arr.max().key);
        assertEquals(12, arr.max().value);
        assertEquals("A", arr.secondMax().key);
        assertEquals(10, arr.secondMax().value);
    }

    @Test
    public void testMaxOrDefaultAndSecondMaxOrDefault() {
        IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3);
        assertEquals(-99, arr.maxOrDefault(-99));
        assertEquals(-99, arr.secondMaxOrDefault(-99));

        arr.add(10);
        assertEquals(10, arr.maxOrDefault(-99));
        assertEquals(-99, arr.secondMaxOrDefault(-99));

        arr.add(20);
        assertEquals(20, arr.maxOrDefault(-99));
        assertEquals(10, arr.secondMaxOrDefault(-99));
    }

    @Test
    public void testMaxIfRemovedOrDefault() {
        // Distinct = true
        {
            IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3);
            assertEquals(-1, arr.maxIfRemovedOrDefault(5, -1));

            arr.add(10);
            assertEquals(-1, arr.maxIfRemovedOrDefault(10, -1));
            assertEquals(10, arr.maxIfRemovedOrDefault(5, -1));

            arr.add(20);
            assertEquals(10, arr.maxIfRemovedOrDefault(20, -1));
            assertEquals(20, arr.maxIfRemovedOrDefault(10, -1));
            assertEquals(20, arr.maxIfRemovedOrDefault(5, -1));
        }

        // Distinct = false
        {
            IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3, false);
            arr.add(20);
            arr.add(20);
            arr.add(10);

            // Removing 20 (only one) -> the remaining maximum is still 20
            assertEquals(20, arr.maxIfRemovedOrDefault(20, -1));
            assertEquals(20, arr.maxIfRemovedOrDefault(10, -1));

            arr.clear();
            arr.add(20);
            arr.add(10);
            // Removing 20 -> remaining maximum is 10
            assertEquals(10, arr.maxIfRemovedOrDefault(20, -1));
        }
    }

    @Test
    public void testExceptionsAndNull() {
        IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3);
        assertThrows(NullPointerException.class, () -> arr.add(null));
        assertThrows(NoSuchElementException.class, () -> arr.max());
        assertThrows(NoSuchElementException.class, () -> arr.secondMax());

        arr.add(10);
        assertThrows(NoSuchElementException.class, () -> arr.secondMax());

        assertThrows(NullPointerException.class, () -> new IncrementalTopKArray<>(3, null, Object::equals));
    }

    @Test
    public void testDumpAndClear() {
        IncrementalTopKArray<Integer> arr = new IncrementalTopKArray<>(3);
        arr.add(10);
        arr.add(20);
        arr.dump(); // should output to system out without error

        arr.clear();
        assertTrue(arr.isEmpty());
        assertEquals(0, arr.size());
    }
}
