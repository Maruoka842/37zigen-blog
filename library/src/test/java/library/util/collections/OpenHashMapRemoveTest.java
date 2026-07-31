package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OpenHashMapRemoveTest {
    @Test
    public void removeReturnsOldValueAndDeletesOnlyTarget() {
        OpenHashMap<Integer, String> map = new OpenHashMap<>();
        map.put(1, "one");
        map.put(17, "seventeen");

        assertEquals("one", map.remove(1));
        assertFalse(map.containsKey(1));
        assertEquals("seventeen", map.get(17));
        assertEquals(1, map.size());
    }

    @Test
    public void putAfterTombstoneUpdatesExistingLaterKey() {
        OpenHashMap<Integer, String> map = new OpenHashMap<>();
        map.put(1, "one");
        map.put(17, "seventeen");
        assertEquals("one", map.remove(1));

        map.put(17, "updated");

        assertEquals(1, map.size());
        assertEquals("updated", map.get(17));
        int entries = 0;
        for (OpenHashMap.Entry<Integer, String> entry : map.entrySet()) {
            assertEquals(17, entry.key);
            assertEquals("updated", entry.value);
            entries++;
        }
        assertEquals(1, entries);
    }

    @Test
    public void mergeAfterTombstoneUpdatesExistingLaterKey() {
        OpenHashMap<Integer, Integer> map = new OpenHashMap<>();
        map.put(1, 10);
        map.put(17, 20);
        assertEquals(10, map.remove(1));

        assertEquals(25, map.merge(17, 5, Integer::sum));

        assertEquals(1, map.size());
        assertEquals(25, map.get(17));
    }

    @Test
    public void removedArrayKeyDoesNotBreakProbeChain() {
        OpenHashMap<int[], String> map = new OpenHashMap<>(new Hash.Strategy<>() {
            @Override
            public int hashCode(int[] o) {
                return 0;
            }

            @Override
            public boolean equals(int[] a, int[] b) {
                return java.util.Arrays.equals(a, b);
            }
        });
        int[] a = {1, 2, 3};
        int[] b = {4, 5, 6};
        map.put(a, "a");
        map.put(b, "b");

        assertEquals("a", map.remove(new int[] {1, 2, 3}));

        assertFalse(map.containsKey(new int[] {1, 2, 3}));
        assertEquals("b", map.get(new int[] {4, 5, 6}));
    }
}
