package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TreapSetTest {

    @Test
    public void testIsEmpty() {
        TreapSet<Integer> set = new TreapSet<>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        set.add(10);
        assertFalse(set.isEmpty());
        assertEquals(1, set.size());

        set.remove(10);
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }
}
