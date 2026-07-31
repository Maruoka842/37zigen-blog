package library.util.collections;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class HashMultiSetTest {

    @Test
    public void testAddAndSize() {
        HashMultiSet<String> set = new HashMultiSet<>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        set.add("a");
        assertFalse(set.isEmpty());
        assertEquals(1, set.size());
        assertEquals(1, set.getValue("a"));

        set.add("a", 2);
        assertEquals(1, set.size()); // size() returns map.size(), which is distinct elements
        assertEquals(3, set.getValue("a"));

        set.add("b");
        assertEquals(2, set.size());
        assertEquals(1, set.getValue("b"));
    }

    @Test
    public void testRemove() {
        HashMultiSet<String> set = new HashMultiSet<>();
        set.add("a", 3);
        set.add("b", 1);

        set.remove("a");
        assertEquals(2, set.getValue("a"));
        assertEquals(2, set.size());

        set.remove("a", 2);
        assertEquals(0, set.getValue("a"));
        assertEquals(1, set.size());

        set.remove("b");
        assertTrue(set.isEmpty());
    }

    @Test
    public void testDump() {
        HashMultiSet<String> set = new HashMultiSet<>();
        PrintStream originalOut = System.out;
        try {
            // Test empty
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            set.dump();
            assertEquals("空集合" + System.lineSeparator(), out.toString());

            // Test non-empty
            set.add("a", 2);
            out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            set.dump();
            assertEquals("a が 2個" + System.lineSeparator(), out.toString());
        } finally {
            // Reset System.out
            System.setOut(originalOut);
        }
    }

    @Test
    public void testToArray() {
        HashMultiSet<String> set = new HashMultiSet<>();
        set.add("a", 2);
        set.add("b", 1);

        String[] arr = set.toArray(String[]::new);
        Arrays.sort(arr);
        assertArrayEquals(new String[]{"a", "a", "b"}, arr);
    }
}
