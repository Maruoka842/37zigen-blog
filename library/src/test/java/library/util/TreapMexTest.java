package library.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.collections.IntTreapSet;
import library.util.collections.LongTreapMultiSet;
import library.util.collections.LongTreapSet;

public class TreapMexTest {

    @Test
    public void testIntTreapSetMex() {
        IntTreapSet set = new IntTreapSet();
        assertEquals(0, set.mex());

        set.add(-1);
        set.add(-2);
        assertEquals(0, set.mex());

        set.add(0);
        assertEquals(1, set.mex());

        set.add(1);
        assertEquals(2, set.mex());

        set.add(3);
        assertEquals(2, set.mex());

        set.add(2);
        assertEquals(4, set.mex());

        set.remove(1);
        assertEquals(1, set.mex());

        set.add(1);
        assertEquals(4, set.mex());

        set.add(100);
        assertEquals(4, set.mex());
    }

    @Test
    public void testLongTreapMultiSetMex() {
        LongTreapMultiSet set = new LongTreapMultiSet();
        assertEquals(0L, set.mex());

        set.add(-1L);
        set.add(-5L);
        assertEquals(0L, set.mex());

        set.add(0L);
        set.add(0L); // Duplicate
        assertEquals(1L, set.mex());

        set.add(1L);
        assertEquals(2L, set.mex());

        set.add(3L);
        assertEquals(2L, set.mex());

        set.add(2L);
        set.add(2L); // Duplicate
        assertEquals(4L, set.mex());

        set.remove(1L);
        assertEquals(1L, set.mex());

        set.add(1L);
        assertEquals(4L, set.mex());

        set.add(100L);
        assertEquals(4L, set.mex());
    }

    @Test
    public void testLongTreapSetMex() {
        LongTreapSet set = new LongTreapSet();
        assertEquals(0L, set.mex());

        set.add(-1L);
        set.add(-10L);
        assertEquals(0L, set.mex());

        set.add(0L);
        assertEquals(1L, set.mex());

        set.add(1L);
        assertEquals(2L, set.mex());

        set.add(3L);
        assertEquals(2L, set.mex());

        set.add(2L);
        assertEquals(4L, set.mex());

        set.remove(0L);
        assertEquals(0L, set.mex());

        set.add(0L);
        assertEquals(4L, set.mex());
    }

    @Test
    public void testLargeIntTreapSetMex() {
        IntTreapSet set = new IntTreapSet();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            set.add(i);
        }
        assertEquals(n, set.mex());

        set.remove(500);
        assertEquals(500, set.mex());

        set.add(500);
        assertEquals(n, set.mex());
    }
}
