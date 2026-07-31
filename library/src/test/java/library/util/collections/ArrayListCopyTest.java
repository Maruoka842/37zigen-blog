package library.util.collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

public class ArrayListCopyTest {

    @Test
    public void testIntArrayListCopy() {
        IntArrayList original = new IntArrayList();
        original.add(1);
        original.add(2);
        original.add(3);

        IntArrayList copy = original.copy();

        assertNotSame(original, copy);
        assertNotSame(original.a, copy.a);
        assertEquals(original.size(), copy.size());
        assertArrayEquals(original.toArray(), copy.toArray());

        copy.add(4);
        assertEquals(3, original.size());
        assertEquals(4, copy.size());

        original.set(0, 10);
        assertEquals(10, original.get(0));
        assertEquals(1, copy.get(0));
    }

    @Test
    public void testLongArrayListCopy() {
        LongArrayList original = new LongArrayList();
        original.add(1L);
        original.add(2L);
        original.add(3L);

        LongArrayList copy = original.copy();

        assertNotSame(original, copy);
        assertNotSame(original.a, copy.a);
        assertEquals(original.size(), copy.size());
        assertArrayEquals(original.toArray(), copy.toArray());

        copy.add(4L);
        assertEquals(3, original.size());
        assertEquals(4, copy.size());

        original.set(0, 10L);
        assertEquals(10L, original.get(0));
        assertEquals(1L, copy.get(0));
    }
}
