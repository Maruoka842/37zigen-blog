package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OpenHashArrayTest {
    @Test
    public void testIntArraySet() {
        OpenHashSet<int[]> set = new OpenHashSet<>();
        int[] a1 = {1, 2, 3};
        int[] a2 = {1, 2, 3};
        set.add(a1);
        assertTrue(set.contains(a1));
        assertTrue(set.contains(a2), "Should contain a2 which is equal to a1 by value");
    }

    @Test
    public void testLongArraySet() {
        OpenHashSet<long[]> set = new OpenHashSet<>();
        long[] a1 = {1L, 2L, 3L};
        long[] a2 = {1L, 2L, 3L};
        set.add(a1);
        assertTrue(set.contains(a1));
        assertTrue(set.contains(a2), "Should contain a2 which is equal to a1 by value");
    }

    @Test
    public void testIntArrayMap() {
        OpenHashMap<int[], String> map = new OpenHashMap<>();
        int[] a1 = {1, 2, 3};
        int[] a2 = {1, 2, 3};
        map.put(a1, "value");
        assertTrue(map.containsKey(a1));
        assertTrue(map.containsKey(a2), "Should contain a2 which is equal to a1 by value");
        assertEquals("value", map.get(a2));
    }
}
