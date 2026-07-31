package library.test;

import library.util.SlopeTrick;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SlopeTrickTest {
    @Test
    public void testBasicAdd() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // f(x) = |x - 10|
        SlopeTrick.QueryResult res = st.getMin();
        assertEquals(0, res.min());
        assertEquals(10, res.lo());
        assertEquals(10, res.hi());
    }

    @Test
    public void testAddMultiple() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // f(x) = |x - 10|
        st.addAbs(20); // f(x) = |x - 10| + |x - 20|
        SlopeTrick.QueryResult res = st.getMin();
        assertEquals(10, res.min());
        assertEquals(10, res.lo());
        assertEquals(20, res.hi());
    }

    @Test
    public void testAddConst() {
        SlopeTrick st = new SlopeTrick();
        st.addConst(5);
        assertEquals(5, st.getMin().min());
    }

    @Test
    public void testRelu() {
        SlopeTrick st = new SlopeTrick();
        st.addRelu(10); // f(x) = max(0, x - 10)
        assertEquals(0, st.getMin().min());
        assertEquals(Long.MIN_VALUE / 3, st.getMin().lo()); // -INF
        assertEquals(10, st.getMin().hi());
    }

    @Test
    public void testIrelu() {
        SlopeTrick st = new SlopeTrick();
        st.addIrelu(10); // f(x) = max(0, 10 - x)
        assertEquals(0, st.getMin().min());
        assertEquals(10, st.getMin().lo());
        assertEquals(Long.MAX_VALUE / 3, st.getMin().hi()); // INF
    }

    @Test
    public void testTranslate() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // f(x) = |x - 10|
        st.translate(5); // f(x) = |(x - 5) - 10| = |x - 15|
        SlopeTrick.QueryResult res = st.getMin();
        assertEquals(0, res.min());
        assertEquals(15, res.lo());
        assertEquals(15, res.hi());
    }

    @Test
    public void testMoveCurve() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // f(x) = |x - 10|
        st.moveLeftCurve(2); // f(x) = min_{0 <= y <= 2} |x + y - 10|
        SlopeTrick.QueryResult res = st.getMin();
        assertEquals(0, res.min());
        assertEquals(8, res.lo());
        assertEquals(10, res.hi());

        st.moveRightCurve(3); // argmin becomes [8, 13]
        res = st.getMin();
        assertEquals(8, res.lo());
        assertEquals(13, res.hi());
    }

    @Test
    public void testGetDestructive() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10);
        st.addAbs(20);
        // f(x) = |x - 10| + |x - 20|
        assertEquals(20, st.getDestructive(5));

        st = new SlopeTrick();
        st.addAbs(10);
        st.addAbs(20);
        assertEquals(10, st.getDestructive(15));

        st = new SlopeTrick();
        st.addAbs(10);
        st.addAbs(20);
        assertEquals(20, st.getDestructive(25));
    }

    @Test
    public void testMerge() {
        SlopeTrick st1 = new SlopeTrick();
        st1.addAbs(10);
        SlopeTrick st2 = new SlopeTrick();
        st2.addAbs(20);

        st1.mergeDestructive(st2);
        SlopeTrick.QueryResult res = st1.getMin();
        assertEquals(10, res.min());
        assertEquals(10, res.lo());
        assertEquals(20, res.hi());
    }

    @Test
    public void testMergeSmallToLarge() {
        SlopeTrick st1 = new SlopeTrick();
        for (int i = 0; i < 10; i++) st1.addAbs(i);

        SlopeTrick st2 = new SlopeTrick();
        for (int i = 100; i < 120; i++) st2.addAbs(i);

        st1.mergeDestructive(st2);
        // st1 has 10 abs calls (20 elements), st2 has 20 abs calls (40 elements).
        // Each addAbs adds one element to L and one to R.
        assertEquals(60, st1.sizeL() + st1.sizeR());
    }

    @Test
    public void testGetAt() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10);
        st.addAbs(20);
        // f(x) = |x - 10| + |x - 20|
        assertEquals(20, st.getAt(5));
        assertEquals(10, st.getAt(15));
        assertEquals(20, st.getAt(25));
        assertEquals(10, st.getMin().min()); // Ensure not destroyed
    }
}
