package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OpenHashSetFoldTest {
    @Test
    public void testEmptySet() {
        OpenHashSet<Integer> set = new OpenHashSet<>();
        Integer res = set.fold(Integer::sum);
        assertNull(res);
    }

    @Test
    public void testSingleElement() {
        OpenHashSet<Integer> set = new OpenHashSet<>();
        set.add(42);
        Integer res = set.fold((a, b) -> {
            fail("Accumulator should not be called for a single element");
            return a + b;
        });
        assertEquals(42, res);
    }

    @Test
    public void testSumFold() {
        OpenHashSet<Integer> set = new OpenHashSet<>();
        set.add(10);
        set.add(20);
        set.add(30);

        Integer sum = set.fold(Integer::sum);
        assertEquals(60, sum);
    }

    @Test
    public void testStringConcat() {
        OpenHashSet<String> set = new OpenHashSet<>();
        set.add("apple");
        set.add("banana");

        String res = set.fold((a, b) -> a + "," + b);
        assertTrue(res.equals("apple,banana") || res.equals("banana,apple"));
    }
}
