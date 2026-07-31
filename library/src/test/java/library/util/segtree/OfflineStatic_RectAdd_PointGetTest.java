package library.util.segtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class OfflineStatic_RectAdd_PointGetTest {

    @Test
    public void testBasic() {
        OfflineStatic_RectAdd_PointGet st = new OfflineStatic_RectAdd_PointGet();
        // [10, 20) x [10, 20) に 1 を加算
        st.rectAdd(10, 10, 20, 20, 1);
        st.build();

        // x=15 における y=15 の値は 1
        assertEquals(1L, st.get(15, 15));
        // x=15 における y=5 の値は 0
        assertEquals(0L, st.get(15, 5));
        // x=15 における y=25 の値は 0
        assertEquals(0L, st.get(15, 25));
        // x=15 における y=10 (inclusive) の値は 1
        assertEquals(1L, st.get(15, 10));
        // x=15 における y=20 (exclusive) の値は 0
        assertEquals(0L, st.get(15, 20));
    }

    @Test
    public void testMultipleRectangles() {
        OfflineStatic_RectAdd_PointGet st = new OfflineStatic_RectAdd_PointGet();
        // [0, 10) x [0, 10) に 1
        st.rectAdd(0, 0, 10, 10, 1);
        // [5, 15) x [5, 15) に 2
        st.rectAdd(5, 5, 15, 15, 2);
        st.build();

        // x=2: [0, 10)x[0, 10) のみ反映。 y=7 で 1.
        assertEquals(1L, st.get(2, 7));

        // x=7: 両方反映。
        // y in [0, 5) -> value 1
        // y in [5, 10) -> value 1+2=3
        // y in [10, 15) -> value 2
        assertEquals(1L, st.get(7, 3));
        assertEquals(3L, st.get(7, 7));
        assertEquals(2L, st.get(7, 12));
        assertEquals(0L, st.get(7, 17));

        // x=12: [0, 10)x[0, 10) は終了。 [5, 15)x[5, 15) のみ。
        assertEquals(2L, st.get(12, 10));
        assertEquals(0L, st.get(12, 3));
    }

    @Test
    public void testBase() {
        OfflineStatic_RectAdd_PointGet st = new OfflineStatic_RectAdd_PointGet();
        st.addAll(5);
        st.rectAdd(0, 0, 10, 10, 1);
        st.build();

        // x=5
        // y=5 -> 5 + 1 = 6
        assertEquals(6L, st.get(5, 5));
        // y=15 -> 5
        assertEquals(5L, st.get(5, 15));
    }

    @Test
    public void testNegative() {
        OfflineStatic_RectAdd_PointGet st = new OfflineStatic_RectAdd_PointGet();
        st.rectAdd(0, 0, 10, 10, -1);
        st.build();

        assertEquals(-1L, st.get(5, 5));
    }
}
