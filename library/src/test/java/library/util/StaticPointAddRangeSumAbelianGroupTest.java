package library.util;

import library.util.algebra.strategy.group.LongAddGroupStrategy;
import library.util.fold.StaticPointAddRangeSumAbelianGroup1D;
import library.util.fold.StaticPointAddRangeSumAbelianGroup2D;
import library.util.fold.StaticPointAddRangeSumAbelianGroup3D;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StaticPointAddRangeSumAbelianGroupTest {

    @Test
    public void test1D() {
        LongAddGroupStrategy group = new LongAddGroupStrategy();
        Long[] a = {1L, 2L, 3L, 2L, 1L};
        StaticPointAddRangeSumAbelianGroup1D<Long> sp = new StaticPointAddRangeSumAbelianGroup1D<>(a, group);

        assertEquals(3L, sp.getRangeSum(0, 2));
        assertEquals(3L, sp.getRangeSum(2, 3));
        assertEquals(9L, sp.getRangeSum(0, 5));

        // countRangeSum
        assertEquals(3, sp.countRangeSum(3L));
        assertEquals(2, sp.countRangeSum(5L));
        assertEquals(2, sp.countRangeSum(6L));
        assertEquals(1, sp.countRangeSum(9L));
        assertEquals(0, sp.countRangeSum(10L));
    }

    @Test
    public void test2D() {
        LongAddGroupStrategy group = new LongAddGroupStrategy();
        Long[][] a = {
            {1L, 2L},
            {3L, 4L}
        };
        StaticPointAddRangeSumAbelianGroup2D<Long> sp = new StaticPointAddRangeSumAbelianGroup2D<>(a, group);

        assertEquals(1L, sp.getRangeSum(0, 0, 1, 1));
        assertEquals(3L, sp.getRangeSum(0, 0, 1, 2));
        assertEquals(7L, sp.getRangeSum(1, 0, 2, 2));
        assertEquals(10L, sp.getRangeSum(0, 0, 2, 2));

        // countRangeSum
        assertEquals(1, sp.countRangeSum(1L));
        assertEquals(2, sp.countRangeSum(3L));
        assertEquals(1, sp.countRangeSum(10L));
    }

    @Test
    public void test3D() {
        LongAddGroupStrategy group = new LongAddGroupStrategy();
        Long[][][] a = new Long[2][2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    a[i][j][k] = 1L;
                }
            }
        }
        StaticPointAddRangeSumAbelianGroup3D<Long> sp = new StaticPointAddRangeSumAbelianGroup3D<>(a, group);

        assertEquals(1L, sp.rangeSum(0, 0, 0, 1, 1, 1));
        assertEquals(8L, sp.rangeSum(0, 0, 0, 2, 2, 2));
        assertEquals(4L, sp.rangeSum(0, 0, 0, 2, 2, 1));

        // countRangeSum
        assertEquals(8, sp.countRangeSum(1L));
        assertEquals(1, sp.countRangeSum(8L));
        assertEquals(6, sp.countRangeSum(4L));
    }
}
