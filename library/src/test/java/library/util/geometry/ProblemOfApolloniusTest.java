package library.util.geometry;

import org.junit.jupiter.api.Test;

import library.util.geometry.DoubleCircle;
import library.util.geometry.DoublePoint;
import library.util.geometry.GeometryUtils;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ProblemOfApolloniusTest {

    @Test
    public void testApollonius() {
        // Example: Three circles centered at (0, 10), (10, 0), (-10, 0) with radius 5
        // We look for a circle that is tangent to all three.
        DoublePoint c1 = new DoublePoint(0, 10);
        double r1 = 5;
        DoublePoint c2 = new DoublePoint(10, 0);
        double r2 = 5;
        DoublePoint c3 = new DoublePoint(-10, 0);
        double r3 = 5;

        // External tangency for all (-1, -1, -1)
        List<DoubleCircle> results = GeometryUtils.problemOfApollonius(c1, r1, c2, r2, c3, r3, -1, -1, -1);

        assertFalse(results.isEmpty(), "Should find at least one circle");

        for (DoubleCircle sol : results) {
            // Verify tangency: distance between centers should be r_sol + r_i
            checkTangency(sol, c1, r1, -1);
            checkTangency(sol, c2, r2, -1);
            checkTangency(sol, c3, r3, -1);
        }
    }

    @Test
    public void testApolloniusInternal() {
        DoublePoint c1 = new DoublePoint(0, 5);
        double r1 = 1;
        DoublePoint c2 = new DoublePoint(5, 0);
        double r2 = 1;
        DoublePoint c3 = new DoublePoint(-5, 0);
        double r3 = 1;

        // Internal tangency for all (1, 1, 1)
        List<DoubleCircle> results = GeometryUtils.problemOfApollonius(c1, r1, c2, r2, c3, r3, 1, 1, 1);

        for (DoubleCircle sol : results) {
            checkTangency(sol, c1, r1, 1);
            checkTangency(sol, c2, r2, 1);
            checkTangency(sol, c3, r3, 1);
        }
    }

    private void checkTangency(DoubleCircle sol, DoublePoint c, double r, int sgn) {
        double d = sol.center().sub(c).norm();
        if (sgn == -1) {
            // External: dist = r_sol + r
            assertEquals(sol.radius() + r, d, 1e-9, "External tangency failed");
        } else {
            // Internal: dist = |r_sol - r|
            assertEquals(Math.abs(sol.radius() - r), d, 1e-9, "Internal tangency failed");
        }
    }
}
