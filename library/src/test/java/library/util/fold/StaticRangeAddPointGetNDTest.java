package library.util.fold;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StaticRangeAddPointGetNDTest {

    @Test
    public void test1D() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(5);
        s.rangeAdd(new int[]{1}, new int[]{4}, 10);
        s.rangeAdd(new int[]{2}, new int[]{5}, 5);

        assertEquals(0, s.get(new int[]{0}));
        assertEquals(10, s.get(new int[]{1}));
        assertEquals(15, s.get(new int[]{2}));
        assertEquals(15, s.get(new int[]{3}));
        assertEquals(5, s.get(new int[]{4}));
    }

    @Test
    public void test2D() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(3, 3);
        // (0,0) to (1,1)
        s.rangeAdd(new int[]{0, 0}, new int[]{2, 2}, 1);
        // (1,1) to (2,2)
        s.rangeAdd(new int[]{1, 1}, new int[]{3, 3}, 2);

        assertEquals(1, s.get(new int[]{0, 0}));
        assertEquals(1, s.get(new int[]{0, 1}));
        assertEquals(0, s.get(new int[]{0, 2}));
        assertEquals(1, s.get(new int[]{1, 0}));
        assertEquals(3, s.get(new int[]{1, 1}));
        assertEquals(2, s.get(new int[]{1, 2}));
        assertEquals(0, s.get(new int[]{2, 0}));
        assertEquals(2, s.get(new int[]{2, 1}));
        assertEquals(2, s.get(new int[]{2, 2}));
    }

    @Test
    public void test3D() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(2, 2, 2);
        s.rangeAdd(new int[]{0, 0, 0}, new int[]{2, 2, 2}, 1);
        s.rangeAdd(new int[]{0, 0, 0}, new int[]{1, 1, 1}, 10);

        assertEquals(11, s.get(new int[]{0, 0, 0}));
        assertEquals(1, s.get(new int[]{1, 1, 1}));
        assertEquals(1, s.get(new int[]{0, 0, 1}));
    }

    @Test
    public void test4D() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(2, 2, 2, 2);
        s.rangeAdd(new int[]{0, 0, 0, 0}, new int[]{2, 2, 2, 2}, 1);
        s.rangeAdd(new int[]{1, 1, 1, 1}, new int[]{2, 2, 2, 2}, 100);

        assertEquals(1, s.get(new int[]{0, 0, 0, 0}));
        assertEquals(101, s.get(new int[]{1, 1, 1, 1}));
    }

    @Test
    public void testEmptyRange() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(2, 2);
        s.rangeAdd(new int[]{0, 0}, new int[]{0, 2}, 100);
        assertEquals(0, s.get(new int[]{0, 0}));
    }

    @Test
    public void testOutOfBoundsGet() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(2, 2);
        s.rangeAdd(new int[]{0, 0}, new int[]{2, 2}, 1);
        assertEquals(0, s.get(new int[]{-1, 0}));
        assertEquals(0, s.get(new int[]{2, 0}));
    }

    @Test
    public void testOutOfBoundsRangeAdd() {
        StaticRangeAddPointGetND s = new StaticRangeAddPointGetND(3, 3);
        // Partially out of bounds
        s.rangeAdd(new int[]{-1, -1}, new int[]{1, 1}, 10);
        // Out of bounds on the upper side
        s.rangeAdd(new int[]{2, 2}, new int[]{5, 5}, 100);
        // Entirely out of bounds
        s.rangeAdd(new int[]{10, 10}, new int[]{20, 20}, 1000);

        assertEquals(10, s.get(new int[]{0, 0}));
        assertEquals(0, s.get(new int[]{0, 1}));
        assertEquals(0, s.get(new int[]{1, 0}));
        assertEquals(100, s.get(new int[]{2, 2}));
    }
}
