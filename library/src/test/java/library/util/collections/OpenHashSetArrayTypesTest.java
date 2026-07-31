package library.util.collections;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OpenHashSetArrayTypesTest {

    @Test
    public void testCharArrayDefaultStrategy() {
        OpenHashSet<char[]> set = new OpenHashSet<>();
        char[] a1 = {'h', 'e', 'l', 'l', 'o'};
        char[] a2 = {'h', 'e', 'l', 'l', 'o'};
        char[] b = {'w', 'o', 'r', 'l', 'd'};

        set.add(a1);
        assertTrue(set.contains(a2)); // same content, should be true
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size()); // duplicate should not increase size

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testBooleanArrayDefaultStrategy() {
        OpenHashSet<boolean[]> set = new OpenHashSet<>();
        boolean[] a1 = {true, false, true};
        boolean[] a2 = {true, false, true};
        boolean[] b = {false, false, true};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testByteArrayDefaultStrategy() {
        OpenHashSet<byte[]> set = new OpenHashSet<>();
        byte[] a1 = {1, 2, 3};
        byte[] a2 = {1, 2, 3};
        byte[] b = {4, 5, 6};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testShortArrayDefaultStrategy() {
        OpenHashSet<short[]> set = new OpenHashSet<>();
        short[] a1 = {10, 20};
        short[] a2 = {10, 20};
        short[] b = {30, 40};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testFloatArrayDefaultStrategy() {
        OpenHashSet<float[]> set = new OpenHashSet<>();
        float[] a1 = {1.5f, 2.5f};
        float[] a2 = {1.5f, 2.5f};
        float[] b = {3.5f, 4.5f};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testDoubleArrayDefaultStrategy() {
        OpenHashSet<double[]> set = new OpenHashSet<>();
        double[] a1 = {1.23, 4.56};
        double[] a2 = {1.23, 4.56};
        double[] b = {7.89, 0.12};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testObjectArrayDefaultStrategy() {
        OpenHashSet<Object[]> set = new OpenHashSet<>();
        Object[] a1 = {"abc", 123};
        Object[] a2 = {"abc", 123};
        Object[] b = {"def", 456};

        set.add(a1);
        assertTrue(set.contains(a2));
        assertFalse(set.contains(b));

        set.add(a2);
        assertEquals(1, set.size());

        assertTrue(set.remove(a2));
        assertFalse(set.contains(a1));
        assertEquals(0, set.size());
    }

    @Test
    public void testExplicitArrayStrategies() {
        // Test explicit HashStrategies for arrays
        OpenHashSet<char[]> charSet = new OpenHashSet<>(HashStrategies.CHAR_ARRAY);
        charSet.add(new char[]{'a', 'b'});
        assertTrue(charSet.contains(new char[]{'a', 'b'}));

        OpenHashSet<boolean[]> boolSet = new OpenHashSet<>(HashStrategies.BOOLEAN_ARRAY);
        boolSet.add(new boolean[]{true, true});
        assertTrue(boolSet.contains(new boolean[]{true, true}));

        OpenHashSet<byte[]> byteSet = new OpenHashSet<>(HashStrategies.BYTE_ARRAY);
        byteSet.add(new byte[]{1, 2});
        assertTrue(byteSet.contains(new byte[]{1, 2}));

        OpenHashSet<short[]> shortSet = new OpenHashSet<>(HashStrategies.SHORT_ARRAY);
        shortSet.add(new short[]{10});
        assertTrue(shortSet.contains(new short[]{10}));

        OpenHashSet<float[]> floatSet = new OpenHashSet<>(HashStrategies.FLOAT_ARRAY);
        floatSet.add(new float[]{1.0f});
        assertTrue(floatSet.contains(new float[]{1.0f}));

        OpenHashSet<double[]> doubleSet = new OpenHashSet<>(HashStrategies.DOUBLE_ARRAY);
        doubleSet.add(new double[]{1.0});
        assertTrue(doubleSet.contains(new double[]{1.0}));

        OpenHashSet<Object[]> objSet = new OpenHashSet<>(HashStrategies.OBJECT_ARRAY);
        objSet.add(new Object[]{"hello"});
        assertTrue(objSet.contains(new Object[]{"hello"}));
    }
}
