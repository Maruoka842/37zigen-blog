package library.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class IntTreapSetHigherLowerTest {

    @Test
    public void testEmpty() {
        IntTreapSet set = new IntTreapSet();
        assertNull(set.lower(10));
        assertNull(set.higher(10));
        assertNull(set.floor(10));
        assertNull(set.ceil(10));
    }

    @Test
    public void testSingleElement() {
        IntTreapSet set = new IntTreapSet();
        set.add(10);

        // lower
        assertNull(set.lower(10));
        assertEquals(10, (int)set.lower(11));
        assertNull(set.lower(9));

        // higher
        assertNull(set.higher(10));
        assertEquals(10, (int)set.higher(9));
        assertNull(set.higher(11));

        // floor
        assertEquals(10, (int)set.floor(10));
        assertEquals(10, (int)set.floor(11));
        assertNull(set.floor(9));

        // ceil
        assertEquals(10, (int)set.ceil(10));
        assertEquals(10, (int)set.ceil(9));
        assertNull(set.ceil(11));
    }

    @Test
    public void testMultipleElements() {
        IntTreapSet set = new IntTreapSet();
        set.add(10);
        set.add(20);
        set.add(30);

        // lower
        assertNull(set.lower(10));
        assertEquals(10, (int)set.lower(15));
        assertEquals(10, (int)set.lower(20));
        assertEquals(20, (int)set.lower(25));
        assertEquals(20, (int)set.lower(30));
        assertEquals(30, (int)set.lower(35));

        // higher
        assertEquals(10, (int)set.higher(5));
        assertEquals(20, (int)set.higher(10));
        assertEquals(20, (int)set.higher(15));
        assertEquals(30, (int)set.higher(20));
        assertEquals(30, (int)set.higher(25));
        assertNull(set.higher(30));
    }

    @Test
    public void testBoundary() {
        IntTreapSet set = new IntTreapSet();
        set.add(Integer.MIN_VALUE);
        set.add(Integer.MAX_VALUE);

        assertNull(set.lower(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, (int)set.higher(Integer.MIN_VALUE));

        assertNull(set.higher(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, (int)set.lower(Integer.MAX_VALUE));
    }

    @Test
    public void testRandom() {
        java.util.TreeSet<Integer> expected = new java.util.TreeSet<>();
        IntTreapSet actual = new IntTreapSet();
        java.util.Random rnd = new java.util.Random(42);

        for (int i = 0; i < 1000; i++) {
            int v = rnd.nextInt(2000) - 1000;
            if (rnd.nextBoolean()) {
                expected.add(v);
                actual.add(v);
            } else {
                expected.remove(v);
                actual.remove(v);
            }

            int q = rnd.nextInt(2000) - 1000;
            assertEquals(expected.lower(q), actual.lower(q), "lower failed for " + q);
            assertEquals(expected.higher(q), actual.higher(q), "higher failed for " + q);
            assertEquals(expected.floor(q), actual.floor(q), "floor failed for " + q);
            assertEquals(expected.ceiling(q), actual.ceil(q), "ceil failed for " + q);
        }
    }

    @Test
    public void testCopy() {
        // Test copying empty set
        IntTreapSet emptySet = new IntTreapSet();
        IntTreapSet emptyCopy = emptySet.copy();
        assertEquals(emptySet, emptyCopy);
        assertEquals(0, emptyCopy.size());

        // Test copying single element set
        IntTreapSet singleSet = new IntTreapSet();
        singleSet.add(42);
        IntTreapSet singleCopy = singleSet.copy();
        assertEquals(singleSet, singleCopy);
        assertEquals(1, singleCopy.size());
        assertEquals(singleSet.root().priority, singleCopy.root().priority);

        // Modify the copy and check independence
        singleCopy.add(24);
        assertEquals(2, singleCopy.size());
        assertEquals(1, singleSet.size());

        // Test copying multi-element set
        IntTreapSet multiSet = new IntTreapSet();
        java.util.Random rnd = new java.util.Random(12345);
        for (int i = 0; i < 50; i++) {
            multiSet.add(rnd.nextInt(100));
        }

        IntTreapSet multiCopy = multiSet.copy();
        assertEquals(multiSet, multiCopy);
        assertEquals(multiSet.size(), multiCopy.size());

        // Structural and priority check
        assertIdenticalStructure(multiSet.root(), multiCopy.root());

        // Verify independent state
        multiCopy.add(999);
        org.junit.jupiter.api.Assertions.assertTrue(multiCopy.contains(999));
        org.junit.jupiter.api.Assertions.assertFalse(multiSet.contains(999));
    }

    private void assertIdenticalStructure(IntTreapSet.Node n1, IntTreapSet.Node n2) {
        if (n1 == null && n2 == null) {
            return;
        }
        org.junit.jupiter.api.Assertions.assertNotNull(n1);
        org.junit.jupiter.api.Assertions.assertNotNull(n2);
        org.junit.jupiter.api.Assertions.assertNotSame(n1, n2); // ensure they are different objects
        assertEquals(n1.key(), n2.key());
        assertEquals(n1.priority, n2.priority);
        assertEquals(n1.size(), n2.size());
        assertIdenticalStructure(n1.left, n2.left);
        assertIdenticalStructure(n1.right, n2.right);
    }
}
