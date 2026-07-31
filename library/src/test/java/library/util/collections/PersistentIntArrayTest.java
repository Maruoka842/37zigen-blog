package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersistentIntArrayTest {

    @Test
    public void testBasic() {
        int[] a = {1, 2, 3, 4, 5};
        PersistentIntArray pa = new PersistentIntArray(a);
        PersistentIntArray.Root r0 = pa.getRoot();

        assertEquals(5, pa.size());
        assertEquals(1, pa.get(r0, 0));
        assertEquals(3, pa.get(r0, 2));
        assertEquals(5, pa.get(r0, 4));

        PersistentIntArray.Root r1 = pa.set(r0, 2, 10);
        assertEquals(3, pa.get(r0, 2)); // original version should not change
        assertEquals(10, pa.get(r1, 2));
        assertEquals(1, pa.get(r1, 0));
        assertEquals(5, pa.get(r1, 4));
    }

    @Test
    public void testDefaultValue() {
        PersistentIntArray pa = new PersistentIntArray(10, -1);
        PersistentIntArray.Root r0 = pa.getRoot();

        assertEquals(10, pa.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(-1, pa.get(r0, i));
        }

        PersistentIntArray.Root r1 = pa.set(r0, 5, 42);
        assertEquals(-1, pa.get(r0, 5));
        assertEquals(42, pa.get(r1, 5));
    }

    @Test
    public void testPersistence() {
        PersistentIntArray pa = new PersistentIntArray(10, 0);
        PersistentIntArray.Root[] roots = new PersistentIntArray.Root[11];
        roots[0] = pa.getRoot();

        for (int i = 0; i < 10; i++) {
            roots[i + 1] = pa.set(roots[i], i, i + 1);
        }

        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j < 10; j++) {
                int expected = (j < i) ? (j + 1) : 0;
                assertEquals(expected, pa.get(roots[i], j), "Root " + i + " at index " + j + " failed");
            }
        }
    }

    @Test
    public void testBoundary() {
        PersistentIntArray pa1 = new PersistentIntArray(1, 42);
        assertEquals(42, pa1.get(pa1.getRoot(), 0));
        PersistentIntArray.Root r2 = pa1.set(pa1.getRoot(), 0, 43);
        assertEquals(42, pa1.get(pa1.getRoot(), 0));
        assertEquals(43, pa1.get(r2, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> new PersistentIntArray(1, 0).get(new PersistentIntArray(1, 0).getRoot(), 1));
        assertThrows(IndexOutOfBoundsException.class, () -> new PersistentIntArray(1, 0).get(new PersistentIntArray(1, 0).getRoot(), -1));
    }

    @Test
    public void testLarge() {
        int n = 100000;
        PersistentIntArray pa = new PersistentIntArray(n, 0);
        PersistentIntArray.Root r = pa.getRoot();
        for (int i = 0; i < n; i++) {
            if (i % 1000 == 0) {
                r = pa.set(r, i, i);
            }
        }

        for (int i = 0; i < n; i++) {
            if (i % 1000 == 0) {
                assertEquals(i, pa.get(r, i));
            } else {
                assertEquals(0, pa.get(r, i));
            }
        }
        pa.dump();
    }
}
