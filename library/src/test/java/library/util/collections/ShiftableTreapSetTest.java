package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShiftableTreapSetTest {

    @Test
    public void testIsEmpty() {
        ShiftableTreapSet.Strategy<Integer, Integer> strategy = new ShiftableTreapSet.Strategy<>() {
            @Override
            public Integer mergeA(Integer newer, Integer older) {
                return (newer == null ? 0 : newer) + (older == null ? 0 : older);
            }

            @Override
            public Integer mergeAX(Integer a, Integer b) {
                return (a == null ? 0 : a) + (b == null ? 0 : b);
            }
        };

        ShiftableTreapSet<Integer, Integer> set = new ShiftableTreapSet<>(strategy);
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
