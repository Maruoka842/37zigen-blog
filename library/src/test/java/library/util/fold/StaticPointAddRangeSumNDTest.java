package library.util.fold;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StaticPointAddRangeSumNDTest {

    @Test
    public void test1D() {
        StaticPointAddRangeSumND sp = new StaticPointAddRangeSumND(5);
        sp.add(new int[]{0}, 1);
        sp.add(new int[]{1}, 2);
        sp.add(new int[]{2}, 3);
        sp.add(new int[]{3}, 4);
        sp.add(new int[]{4}, 5);

        assertEquals(1, sp.getRangeSum(new int[]{0}, new int[]{1}));
        assertEquals(3, sp.getRangeSum(new int[]{0}, new int[]{2}));
        assertEquals(15, sp.getRangeSum(new int[]{0}, new int[]{5}));
        assertEquals(9, sp.getRangeSum(new int[]{1}, new int[]{4}));
        assertEquals(5, sp.getRangeSum(new int[]{4}, new int[]{5}));
    }

    @Test
    public void test2D() {
        StaticPointAddRangeSumND sp = new StaticPointAddRangeSumND(2, 2);
        sp.add(new int[]{0, 0}, 1);
        sp.add(new int[]{0, 1}, 2);
        sp.add(new int[]{1, 0}, 3);
        sp.add(new int[]{1, 1}, 4);

        assertEquals(1, sp.getRangeSum(new int[]{0, 0}, new int[]{1, 1}));
        assertEquals(3, sp.getRangeSum(new int[]{0, 0}, new int[]{1, 2}));
        assertEquals(4, sp.getRangeSum(new int[]{0, 0}, new int[]{2, 1}));
        assertEquals(10, sp.getRangeSum(new int[]{0, 0}, new int[]{2, 2}));
        assertEquals(4, sp.getRangeSum(new int[]{1, 1}, new int[]{2, 2}));
    }

    @Test
    public void test3D() {
        StaticPointAddRangeSumND sp = new StaticPointAddRangeSumND(2, 2, 2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    sp.add(new int[]{i, j, k}, 1);
                }
            }
        }
        assertEquals(1, sp.getRangeSum(new int[]{0, 0, 0}, new int[]{1, 1, 1}));
        assertEquals(8, sp.getRangeSum(new int[]{0, 0, 0}, new int[]{2, 2, 2}));
        assertEquals(4, sp.getRangeSum(new int[]{0, 0, 0}, new int[]{2, 2, 1}));
    }

    @Test
    public void test4D() {
        StaticPointAddRangeSumND sp = new StaticPointAddRangeSumND(2, 2, 2, 2);
        sp.add(new int[]{1, 1, 1, 1}, 100);
        assertEquals(100, sp.getRangeSum(new int[]{0, 0, 0, 0}, new int[]{2, 2, 2, 2}));
        assertEquals(100, sp.getRangeSum(new int[]{1, 1, 1, 1}, new int[]{2, 2, 2, 2}));
        assertEquals(0, sp.getRangeSum(new int[]{0, 0, 0, 0}, new int[]{1, 1, 1, 1}));
    }
}
