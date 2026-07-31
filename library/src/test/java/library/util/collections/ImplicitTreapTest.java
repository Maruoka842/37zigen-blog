package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImplicitTreapTest {

    @Test
    public void testIsEmpty() {
        ImplicitTreap treap = new ImplicitTreap();
        assertTrue(treap.isEmpty());
        assertEquals(0, treap.size());

        treap.insert(0, 10);
        assertFalse(treap.isEmpty());
        assertEquals(1, treap.size());

        treap.erase(0);
        assertTrue(treap.isEmpty());
        assertEquals(0, treap.size());
    }
}
