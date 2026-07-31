package library.util.geometry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class BoundedCircleTest {

    @Test
    public void testIntPoint4PointCCW() {
        IntPoint a = new IntPoint(100, 0);
        IntPoint b = new IntPoint(0, 100);
        IntPoint c = new IntPoint(-100, 0);

        // Inside
        assertEquals(BoundedSide.ON_BOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, 0)));
        assertEquals(BoundedSide.ON_BOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, 50)));

        // Boundary
        assertEquals(BoundedSide.ON_BOUNDARY, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, -100)));
        assertEquals(BoundedSide.ON_BOUNDARY, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(100, 0)));

        // Outside
        assertEquals(BoundedSide.ON_UNBOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, 200)));
        assertEquals(BoundedSide.ON_UNBOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(200, 200)));
    }

    @Test
    public void testIntPoint4PointCW() {
        IntPoint a = new IntPoint(-100, 0);
        IntPoint b = new IntPoint(0, 100);
        IntPoint c = new IntPoint(100, 0);

        // Inside
        assertEquals(BoundedSide.ON_BOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, 0)));

        // Boundary
        assertEquals(BoundedSide.ON_BOUNDARY, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, -100)));

        // Outside
        assertEquals(BoundedSide.ON_UNBOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(a, b, c, new IntPoint(0, 200)));
    }

    @Test
    public void testIntPoint4PointCollinearException() {
        IntPoint a = new IntPoint(0, 0);
        IntPoint b = new IntPoint(1, 1);
        IntPoint c = new IntPoint(2, 2);
        IntPoint d = new IntPoint(3, 3);

        assertThrows(IllegalArgumentException.class, () -> {
            GeometryUtils.sideOfBoundedCircle(a, b, c, d);
        });
    }

    @Test
    public void testIntPoint3PointDiameter() {
        IntPoint p = new IntPoint(-10, 0);
        IntPoint q = new IntPoint(10, 0);

        // Inside
        assertEquals(BoundedSide.ON_BOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(0, 0)));
        assertEquals(BoundedSide.ON_BOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(5, 5)));

        // Boundary
        assertEquals(BoundedSide.ON_BOUNDARY, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(0, 10)));
        assertEquals(BoundedSide.ON_BOUNDARY, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(-10, 0)));

        // Outside
        assertEquals(BoundedSide.ON_UNBOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(0, 11)));
        assertEquals(BoundedSide.ON_UNBOUNDED_SIDE, GeometryUtils.sideOfBoundedCircle(p, q, new IntPoint(10, 10)));
    }
}
