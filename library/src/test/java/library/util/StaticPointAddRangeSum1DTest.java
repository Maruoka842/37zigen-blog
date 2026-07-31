package library.util;

import org.junit.jupiter.api.Test;

import library.util.fold.StaticPointAddRangeSum1D;
import library.util.fold.StaticPointAddRangeSum2D;
import library.util.fold.StaticPointAddRangeSum3D;

import static org.junit.jupiter.api.Assertions.*;

public class StaticPointAddRangeSum1DTest {

    @Test
    public void testCountRangeSumBasic() {
        long[] a = {1, 2, 3, 2, 1};
        StaticPointAddRangeSum1D sp = new StaticPointAddRangeSum1D(a);

        // v = 3: {1, 2}, {3}, {2, 1}
        assertEquals(3, sp.countRangeSum(3));

        // v = 5: {2, 3}, {3, 2}
        assertEquals(2, sp.countRangeSum(5));

        // v = 6: {1, 2, 3}, {3, 2, 1}
        assertEquals(2, sp.countRangeSum(6));

        // v = 9: {1, 2, 3, 2, 1}
        assertEquals(1, sp.countRangeSum(9));

        // v = 10: none
        assertEquals(0, sp.countRangeSum(10));
    }

    @Test
    public void testCountRangeSumWithZeros() {
        long[] a = {0, 0, 0};
        StaticPointAddRangeSum1D sp = new StaticPointAddRangeSum1D(a);

        // v = 0: [0, 1), [0, 2), [0, 3), [1, 2), [1, 3), [2, 3)
        assertEquals(6, sp.countRangeSum(0));

        // v = 1: none
        assertEquals(0, sp.countRangeSum(1));
    }

    @Test
    public void testCountRangeSumWithNegatives() {
        long[] a = {1, -1, 1, -1};
        StaticPointAddRangeSum1D sp = new StaticPointAddRangeSum1D(a);

        // Prefixes: P[0]=0, P[1]=1, P[2]=0, P[3]=1, P[4]=0

        // v = 0: P[j]=P[i] for i < j
        // (0, 2), (0, 4), (2, 4) for value 0
        // (1, 3) for value 1
        // Total 4
        assertEquals(4, sp.countRangeSum(0));

        // v = 1: P[j]-P[i]=1 for i < j
        // (0, 1), (0, 3), (2, 3)
        // Total 3
        assertEquals(3, sp.countRangeSum(1));

        // v = -1: P[j]-P[i]=-1 for i < j
        // (1, 2), (1, 4), (3, 4)
        // Total 3
        assertEquals(3, sp.countRangeSum(-1));
    }

    @Test
    public void testEmpty() {
        StaticPointAddRangeSum1D sp = new StaticPointAddRangeSum1D(0);
        assertEquals(0, sp.countRangeSum(0));
        assertEquals(0, sp.countRangeSum(1));
    }

    @Test
    public void testClear() {
	long[] a = {1, 2, 3};
        StaticPointAddRangeSum1D sp = new StaticPointAddRangeSum1D(a);
        // a = {1, 2, 3}, P = {0, 1, 3, 6}
        // x = 3: (0, 2), (2, 3). Total 2.
        assertEquals(2, sp.countRangeSum(3));
        sp.clear();
        // After clear, a = {0, 0, 0}, P = {0, 0, 0, 0}
        assertEquals(0, sp.countRangeSum(3));
        assertEquals(6, sp.countRangeSum(0));
    }

    @Test
    public void testCountRangeSum2D() {
        long[][] a = {
            {1, 2},
            {3, 4}
        };
        StaticPointAddRangeSum2D sp = new StaticPointAddRangeSum2D(a);
        // v = 1: (0, 0, 1, 1) -> 1
        assertEquals(1, sp.countRangeSum(1));
        // v = 3: (0, 0, 1, 2) -> 1+2=3, (1, 0, 2, 1) -> 3. Total 2.
        assertEquals(2, sp.countRangeSum(3));
        // v = 10: (0, 0, 2, 2) -> 1+2+3+4=10. Total 1.
        assertEquals(1, sp.countRangeSum(10));
    }

    @Test
    public void testCountRangeSum3D() {
        long[][][] a = new long[2][2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    a[i][j][k] = 1;
                }
            }
        }
        StaticPointAddRangeSum3D sp = new StaticPointAddRangeSum3D(a);
        // v = 1: 8 cells
        assertEquals(8, sp.countRangeSum(1));
        // v = 8: 1 whole cube
        assertEquals(1, sp.countRangeSum(8));
        // v = 4: 6 faces of 2x2x1 or 1x2x2 or 2x1x2
        // 2 faces of 2x2x1: [0, 2)x[0, 2)x[0, 1) and [0, 2)x[0, 2)x[1, 2)
        // 2 faces of 2x1x2: [0, 2)x[0, 1)x[0, 2) and [0, 2)x[1, 2)x[0, 2)
        // 2 faces of 1x2x2: [0, 1)x[0, 2)x[0, 2) and [1, 2)x[0, 2)x[0, 2)
        assertEquals(6, sp.countRangeSum(4));
    }
}
