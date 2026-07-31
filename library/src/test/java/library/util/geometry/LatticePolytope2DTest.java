package library.util.geometry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import library.util.geometry.LatticePolytope2D.Location;

public class LatticePolytope2DTest {

    @Test
    public void testSquare() {
        // Unit square (0,0), (1,0), (1,1), (0,1)
        LongPoint[] vertices = {
            new LongPoint(0, 0),
            new LongPoint(1, 0),
            new LongPoint(1, 1),
            new LongPoint(0, 1)
        };
        LatticePolytope2D poly = new LatticePolytope2D(vertices);

        assertEquals(2, poly.twiceArea());
        assertEquals(4, poly.boundaryLatticePoints());
        assertEquals(0, poly.interiorLatticePoints());
        assertEquals(4, poly.countLatticePoints());

        // scale by 2
        LatticePolytope2D poly2 = poly.scale(2);
        // (0,0), (2,0), (2,2), (0,2)
        assertEquals(8, poly2.twiceArea());
        assertEquals(8, poly2.boundaryLatticePoints());
        assertEquals(1, poly2.interiorLatticePoints());
        assertEquals(9, poly2.countLatticePoints());

        assertEquals(9, poly.countLatticePoints(2));
    }

    @Test
    public void testTriangle() {
        // Triangle (0,0), (2,0), (0,2)
        LongPoint[] vertices = {
            new LongPoint(0, 0),
            new LongPoint(2, 0),
            new LongPoint(0, 2)
        };
        LatticePolytope2D poly = new LatticePolytope2D(vertices);

        assertEquals(4, poly.twiceArea()); // Area = 2
        assertEquals(6, poly.boundaryLatticePoints()); // (0,0)-(2,0): 2, (2,0)-(0,2): 2, (0,2)-(0,0): 2
        assertEquals(0, poly.interiorLatticePoints()); // 2 = I + 6/2 - 1 => I = 0
        assertEquals(6, poly.countLatticePoints());
    }

    @Test
    public void testLocate() {
        // Square (0,0), (2,0), (2,2), (0,2)
        LongPoint[] vertices = {
            new LongPoint(0, 0),
            new LongPoint(2, 0),
            new LongPoint(2, 2),
            new LongPoint(0, 2)
        };
        LatticePolytope2D poly = new LatticePolytope2D(vertices);

        assertEquals(Location.INSIDE, poly.locate(new LongPoint(1, 1)));
        assertEquals(Location.ON_BOUNDARY, poly.locate(new LongPoint(0, 0)));
        assertEquals(Location.ON_BOUNDARY, poly.locate(new LongPoint(2, 2)));
        assertEquals(Location.ON_BOUNDARY, poly.locate(new LongPoint(0, 1)));
        assertEquals(Location.ON_BOUNDARY, poly.locate(new LongPoint(2, 1)));

        assertEquals(Location.OUTSIDE, poly.locate(new LongPoint(-1, 1)));
        assertEquals(Location.OUTSIDE, poly.locate(new LongPoint(3, 1)));
        assertEquals(Location.OUTSIDE, poly.locate(new LongPoint(1, -1)));
        assertEquals(Location.OUTSIDE, poly.locate(new LongPoint(1, 3)));
    }

    @Test
    public void testMinkowskiSum() {
        // Square1 (0,0)-(1,0)-(1,1)-(0,1)
        LatticePolytope2D poly1 = new LatticePolytope2D(new LongPoint[]{
            new LongPoint(0, 0),
            new LongPoint(1, 0),
            new LongPoint(1, 1),
            new LongPoint(0, 1)
        });
        // Square2 (0,0)-(1,0)-(1,1)-(0,1)
        LatticePolytope2D poly2 = new LatticePolytope2D(new LongPoint[]{
            new LongPoint(0, 0),
            new LongPoint(1, 0),
            new LongPoint(1, 1),
            new LongPoint(0, 1)
        });

        LatticePolytope2D sum = poly1.minkowskiSum(poly2);
        // Result should be (0,0)-(2,0)-(2,2)-(0,2)
        assertEquals(8, sum.twiceArea());
        assertEquals(8, sum.boundaryLatticePoints());
        assertEquals(9, sum.countLatticePoints());

        assertEquals(Location.ON_BOUNDARY, sum.locate(new LongPoint(0, 0)));
        assertEquals(Location.ON_BOUNDARY, sum.locate(new LongPoint(2, 2)));
        assertEquals(Location.INSIDE, sum.locate(new LongPoint(1, 1)));
    }

    @Test
    public void testFromPoints() {
        LongPoint[] points = {
            new LongPoint(0, 0),
            new LongPoint(1, 0),
            new LongPoint(2, 0),
            new LongPoint(2, 1),
            new LongPoint(2, 2),
            new LongPoint(1, 2),
            new LongPoint(0, 2),
            new LongPoint(0, 1),
            new LongPoint(1, 1) // internal
        };
        LatticePolytope2D poly = LatticePolytope2D.fromPoints(points);
        // Should be (0,0), (2,0), (2,2), (0,2)
        assertEquals(4, poly.vertices.length);
        assertEquals(8, poly.twiceArea());
    }
}
