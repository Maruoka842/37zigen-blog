package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SlopeTrickVisualizationTest {

    @Test
    public void testDiscardOutside() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // L: {10}, R: {10}
        st.addAbs(20); // L: {10, 20}, R: {10, 20}
        st.addAbs(30); // L: {10, 20, 30}, R: {10, 20, 30}

        assertEquals(3, st.sizeL());
        assertEquals(3, st.sizeR());

        st.discardOutside(15, 25); // Only 20 should remain

        assertEquals(1, st.sizeL());
        assertEquals(1, st.sizeR());

        SlopeTrick.QueryResult res = st.getMin();
        assertEquals(20, res.lo());
        assertEquals(20, res.hi());
    }

    @Test
    public void testDiscardOutsideWithDisplacement() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10); // L: {10}, R: {10}
        st.translate(5); // Shift right by 5 -> change points at 15

        // Before discard: change point at 15
        st.discardOutside(10, 20); // 15 is in [10, 20)
        assertEquals(1, st.sizeL());
        assertEquals(1, st.sizeR());

        st.discardOutside(20, 30); // 15 is outside [20, 30)
        assertEquals(0, st.sizeL());
        assertEquals(0, st.sizeR());
    }

    @Test
    public void testMoveCurveInf() {
        SlopeTrick st = new SlopeTrick();
        st.addAbs(10);
        st.addAbs(20);

        assertEquals(2, st.sizeL());
        assertEquals(2, st.sizeR());

        st.moveRightCurve(); // Clear R
        assertEquals(2, st.sizeL());
        assertEquals(0, st.sizeR());

        st.moveLeftCurve(); // Clear L
        assertEquals(0, st.sizeL());
        assertEquals(0, st.sizeR());
    }
}
