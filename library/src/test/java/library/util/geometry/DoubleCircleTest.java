package library.util.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DoubleCircleTest {

	private static final double EPS = 1e-9;

	@Test
	public void testFromDiameterNormal() {
		DoublePoint a = new DoublePoint(1.0, 2.0);
		DoublePoint b = new DoublePoint(5.0, 5.0);
		DoubleCircle circle = DoubleCircle.fromDiameter(a, b);

		// Center is at (3.0, 3.5)
		assertEquals(3.0, circle.center().x(), EPS);
		assertEquals(3.5, circle.center().y(), EPS);
		// Diameter length is sqrt((5-1)^2 + (5-2)^2) = sqrt(16 + 9) = 5.0, so radius is 2.5
		assertEquals(2.5, circle.radius(), EPS);
	}

	@Test
	public void testFromDiameterDegenerate() {
		DoublePoint a = new DoublePoint(3.14, -2.71);
		DoubleCircle circle = DoubleCircle.fromDiameter(a, a);

		assertEquals(3.14, circle.center().x(), EPS);
		assertEquals(-2.71, circle.center().y(), EPS);
		assertEquals(0.0, circle.radius(), EPS);
	}

	@Test
	public void testAreaAndCircumference() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 3.0);
		assertEquals(Math.PI * 9.0, circle.area(), EPS);
		assertEquals(2.0 * Math.PI * 3.0, circle.circumference(), EPS);

		DoubleCircle unitCircle = new DoubleCircle(new DoublePoint(1.2, -3.4), 1.0);
		assertEquals(Math.PI, unitCircle.area(), EPS);
		assertEquals(2.0 * Math.PI, unitCircle.circumference(), EPS);
	}

	@Test
	public void testCircumcircleNormal() {
		// Right triangle: a(0, 0), b(4, 0), c(0, 3)
		// Hypotenuse is 5, circumcenter should be (2, 1.5), radius should be 2.5
		DoublePoint a = new DoublePoint(0, 0);
		DoublePoint b = new DoublePoint(4, 0);
		DoublePoint c = new DoublePoint(0, 3);

		DoubleCircle circle = DoubleCircle.circumcircle(a, b, c);

		assertEquals(2.0, circle.center().x(), EPS);
		assertEquals(1.5, circle.center().y(), EPS);
		assertEquals(2.5, circle.radius(), EPS);

		// Distances from center to all three points should be equal to radius
		assertEquals(2.5, circle.center().sub(a).norm(), EPS);
		assertEquals(2.5, circle.center().sub(b).norm(), EPS);
		assertEquals(2.5, circle.center().sub(c).norm(), EPS);
	}

	@Test
	public void testCircumcircleEquilateral() {
		// Equilateral-like triangle
		DoublePoint a = new DoublePoint(0, 0);
		DoublePoint b = new DoublePoint(2, 0);
		DoublePoint c = new DoublePoint(1, Math.sqrt(3));

		DoubleCircle circle = DoubleCircle.circumcircle(a, b, c);

		assertEquals(1.0, circle.center().x(), EPS);
		assertEquals(1.0 / Math.sqrt(3), circle.center().y(), EPS);
		assertEquals(2.0 / Math.sqrt(3), circle.radius(), EPS);

		assertEquals(circle.radius(), circle.center().sub(a).norm(), EPS);
		assertEquals(circle.radius(), circle.center().sub(b).norm(), EPS);
		assertEquals(circle.radius(), circle.center().sub(c).norm(), EPS);
	}

	@Test
	public void testCircumcircleCollinear() {
		// Three collinear points
		DoublePoint a = new DoublePoint(0, 0);
		DoublePoint b = new DoublePoint(1, 1);
		DoublePoint c = new DoublePoint(2, 2);

		assertNull(DoubleCircle.circumcircle(a, b, c));

		// Coincident points
		assertNull(DoubleCircle.circumcircle(a, a, b));
	}

	@Test
	public void testIntersectionNoIntersection() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		// Segment completely outside the circle
		DoubleSegment seg1 = new DoubleSegment(2.0, 0.0, 3.0, 0.0);
		assertTrue(circle.intersect(seg1).isEmpty());

		// Segment completely inside the circle
		DoubleSegment seg2 = new DoubleSegment(-0.5, 0.0, 0.5, 0.0);
		assertTrue(circle.intersect(seg2).isEmpty());
	}

	@Test
	public void testIntersectionTwoIntersections() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		DoubleSegment seg = new DoubleSegment(-2.0, 0.0, 2.0, 0.0);
		var pts = circle.intersect(seg);
		assertEquals(2, pts.size());
		assertEquals(-1.0, pts.get(0).x(), EPS);
		assertEquals(0.0, pts.get(0).y(), EPS);
		assertEquals(1.0, pts.get(1).x(), EPS);
		assertEquals(0.0, pts.get(1).y(), EPS);
	}

	@Test
	public void testIntersectionOneIntersection() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		// One endpoint inside, one outside
		DoubleSegment seg = new DoubleSegment(0.0, 0.0, 2.0, 0.0);
		var pts = circle.intersect(seg);
		assertEquals(1, pts.size());
		assertEquals(1.0, pts.get(0).x(), EPS);
		assertEquals(0.0, pts.get(0).y(), EPS);
	}

	@Test
	public void testIntersectionTangent() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		// Segment tangent to the circle
		DoubleSegment seg = new DoubleSegment(-2.0, 1.0, 2.0, 1.0);
		var pts = circle.intersect(seg);
		assertEquals(1, pts.size());
		assertEquals(0.0, pts.get(0).x(), EPS);
		assertEquals(1.0, pts.get(0).y(), EPS);
	}

	@Test
	public void testIntersectionEndpointsOnCircle() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		DoubleSegment seg = new DoubleSegment(-1.0, 0.0, 1.0, 0.0);
		var pts = circle.intersect(seg);
		assertEquals(2, pts.size());
		assertEquals(-1.0, pts.get(0).x(), EPS);
		assertEquals(0.0, pts.get(0).y(), EPS);
		assertEquals(1.0, pts.get(1).x(), EPS);
		assertEquals(0.0, pts.get(1).y(), EPS);
	}

	@Test
	public void testIntersectionDegenerateSegment() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(0, 0), 1.0);
		// Point on circle
		DoubleSegment segOn = new DoubleSegment(1.0, 0.0, 1.0, 0.0);
		var ptsOn = circle.intersect(segOn);
		assertEquals(1, ptsOn.size());
		assertEquals(1.0, ptsOn.get(0).x(), EPS);
		assertEquals(0.0, ptsOn.get(0).y(), EPS);

		// Point off circle
		DoubleSegment segOff = new DoubleSegment(2.0, 0.0, 2.0, 0.0);
		var ptsOff = circle.intersect(segOff);
		assertTrue(ptsOff.isEmpty());
	}

	@Test
	public void testNegativeRadiusValidation() {
		assertThrows(IllegalArgumentException.class, () -> {
			new DoubleCircle(new DoublePoint(0, 0), -1.0);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			new DoubleCircle(new DoublePoint(1.2, -3.4), -0.00001);
		});
	}

	@Test
	public void testContains() {
		DoubleCircle circle = new DoubleCircle(new DoublePoint(1.0, 2.0), 3.0);

		// Inside points
		assertTrue(circle.contains(new DoublePoint(1.0, 2.0))); // center
		assertTrue(circle.contains(new DoublePoint(2.0, 3.0))); // inside
		assertTrue(circle.contains(new DoublePoint(1.0, 4.9))); // inside, near boundary

		// Boundary points
		assertTrue(circle.contains(new DoublePoint(4.0, 2.0))); // on boundary
		assertTrue(circle.contains(new DoublePoint(1.0, 5.0))); // on boundary
		assertTrue(circle.contains(new DoublePoint(1.0, 5.0000000001))); // slightly outside but within 1e-9 tolerance

		// Outside points
		assertFalse(circle.contains(new DoublePoint(5.0, 2.0))); // outside
		assertFalse(circle.contains(new DoublePoint(1.0, 6.0))); // outside
	}

	@Test
	public void testCircleThrough2PointsTangentToLine() {
		// Chord AB parallel to line: points (0, 2) and (2, 2) on one side, line y = 0
		DoublePoint a = new DoublePoint(0, 2);
		DoublePoint b = new DoublePoint(2, 2);
		DoubleLine l = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(1, 0)); // y = 0

		var circles = GeometryUtils.circleThrough2PointsTangentToLine(a, b, l);
		// There should be exactly 1 solution because AB is parallel to y=0
		assertEquals(1, circles.size());
		for (DoubleCircle c : circles) {
			// Passes through a
			assertEquals(c.radius(), c.center().sub(a).norm(), EPS);
			// Passes through b
			assertEquals(c.radius(), c.center().sub(b).norm(), EPS);
			// Tangent to y = 0 means y-coordinate of center is either r or -r depending on side.
			// Since points are at y=2 and line at y=0, center should have positive y = r
			assertEquals(c.radius(), Math.abs(c.center().y()), EPS);
		}

		// Non-parallel chord case: points (0, 2) and (2, 3), line y = 0
		DoublePoint a_non = new DoublePoint(0, 2);
		DoublePoint b_non = new DoublePoint(2, 3);
		var circles_non = GeometryUtils.circleThrough2PointsTangentToLine(a_non, b_non, l);
		assertEquals(2, circles_non.size());
		for (DoubleCircle c : circles_non) {
			assertEquals(c.radius(), c.center().sub(a_non).norm(), EPS);
			assertEquals(c.radius(), c.center().sub(b_non).norm(), EPS);
			assertEquals(c.radius(), Math.abs(c.center().y()), EPS);
		}

		// Case where perpendicular bisector of AB is parallel to line:
		// a = (1, 0), b = (1, 2), line l = y = 2.
		// Perpendicular bisector is y = 1. Distance from bisector to line is 1.0.
		// Unique solution is circle centered at (1, 1) with radius 1.0.
		DoublePoint a2 = new DoublePoint(1, 0);
		DoublePoint b2 = new DoublePoint(1, 2);
		DoubleLine l2 = new DoubleLine(new DoublePoint(0, 2), new DoublePoint(1, 2)); // y = 2
		var circles2 = GeometryUtils.circleThrough2PointsTangentToLine(a2, b2, l2);
		assertEquals(1, circles2.size());
		DoubleCircle c2 = circles2.get(0);
		assertEquals(1.0, c2.radius(), EPS);
		assertEquals(1.0, c2.center().x(), EPS);
		assertEquals(1.0, c2.center().y(), EPS);

		// Returns null on degenerate input
		assertNull(GeometryUtils.circleThrough2PointsTangentToLine(a, a, l));
		DoubleLine degenerateLine = new DoubleLine(a, a);
		assertNull(GeometryUtils.circleThrough2PointsTangentToLine(a, b, degenerateLine));
	}

	@Test
	public void testCircleThroughPointTangentTo2Lines() {
		// Intersecting lines: x-axis and y-axis. Point (1, 1).
		// Expect 2 circles: inside the first quadrant, tangent to both axes and passing through (1, 1)
		DoublePoint p = new DoublePoint(1, 1);
		DoubleLine l1 = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(1, 0)); // y = 0
		DoubleLine l2 = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(0, 1)); // x = 0

		var circles = GeometryUtils.circleThroughPointTangentTo2Lines(p, l1, l2);
		// Under our implementation, we get 2 circles in the first quadrant for (1, 1)
		assertTrue(circles.size() >= 2);
		for (DoubleCircle c : circles) {
			// Tangent to l1
			assertEquals(c.radius(), Math.abs(c.center().y()), EPS);
			// Tangent to l2
			assertEquals(c.radius(), Math.abs(c.center().x()), EPS);
			// Passes through p
			assertEquals(c.radius(), c.center().sub(p).norm(), EPS);
		}

		// Parallel lines: y = 0 and y = 2. Point (1, 1)
		// Distance between lines is 2, so r = 1.
		DoublePoint p2 = new DoublePoint(1, 1);
		DoubleLine l1_p = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(1, 0)); // y = 0
		DoubleLine l2_p = new DoubleLine(new DoublePoint(0, 2), new DoublePoint(1, 2)); // y = 2
		var circles2 = GeometryUtils.circleThroughPointTangentTo2Lines(p2, l1_p, l2_p);
		// Expect 2 solutions (centered at (0, 1) and (2, 1))
		assertEquals(2, circles2.size());
		for (DoubleCircle c2 : circles2) {
			assertEquals(1.0, c2.radius(), EPS);
			assertEquals(1.0, c2.center().y(), EPS);
			assertEquals(1.0, c2.center().sub(p2).norm(), EPS);
		}

		// Returns null on degenerate line
		DoubleLine degenerateLine = new DoubleLine(p2, p2);
		assertNull(GeometryUtils.circleThroughPointTangentTo2Lines(p2, degenerateLine, l2_p));
	}

	@Test
	public void testIntersectIdenticalCircles() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(1.0, 2.0), 3.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(1.0, 2.0), 3.0);
		var result = c1.intersect(c2);
		assertEquals(1, result.size());
		assertEquals(c1, result.get(0));
	}

	@Test
	public void testCircleCircleIntersectSeparated() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 1.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(3.0, 0.0), 1.0);
		assertTrue(c1.intersect(c2).isEmpty());
	}

	@Test
	public void testCircleCircleIntersectConcentric() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 3.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(0.0, 0.0), 1.0);
		assertTrue(c1.intersect(c2).isEmpty());
		assertTrue(c2.intersect(c1).isEmpty());
	}

	@Test
	public void testCircleCircleIntersectOneContained() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 5.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(1.0, 0.0), 2.0);
		assertTrue(c1.intersect(c2).isEmpty());
		assertTrue(c2.intersect(c1).isEmpty());
	}

	@Test
	public void testCircleCircleIntersectExternallyTangent() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 2.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(5.0, 0.0), 3.0);
		var pts = c1.intersect(c2);
		assertEquals(1, pts.size());
		DoublePoint p0 = (DoublePoint) pts.get(0);
		assertEquals(2.0, p0.x(), EPS);
		assertEquals(0.0, p0.y(), EPS);
	}

	@Test
	public void testCircleCircleIntersectInternallyTangent() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 5.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(2.0, 0.0), 3.0);
		var pts = c1.intersect(c2);
		assertEquals(1, pts.size());
		DoublePoint p0 = (DoublePoint) pts.get(0);
		assertEquals(5.0, p0.x(), EPS);
		assertEquals(0.0, p0.y(), EPS);
	}

	@Test
	public void testCircleCircleIntersectGeneral() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 5.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(6.0, 0.0), 5.0);
		var pts = c1.intersect(c2);
		assertEquals(2, pts.size());
		// Center x = 3.0, y = +/- sqrt(25 - 9) = +/- 4.0
		// Both points should be evaluated
		DoublePoint p1 = (DoublePoint) pts.get(0);
		DoublePoint p2 = (DoublePoint) pts.get(1);
		assertEquals(3.0, p1.x(), EPS);
		assertEquals(4.0, Math.abs(p1.y()), EPS);
		assertEquals(3.0, p2.x(), EPS);
		assertEquals(4.0, Math.abs(p2.y()), EPS);
		assertNotEquals(p1.y(), p2.y(), EPS);
	}

	@Test
	public void testCircleCircleIntersectionAreaSeparated() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 1.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(3.0, 0.0), 1.0);
		assertEquals(0.0, c1.intersectionArea(c2), EPS);
	}

	@Test
	public void testCircleCircleIntersectionAreaContained() {
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 5.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(1.0, 0.0), 2.0);
		assertEquals(Math.PI * 4.0, c1.intersectionArea(c2), EPS);
		assertEquals(Math.PI * 4.0, c2.intersectionArea(c1), EPS);
	}

	@Test
	public void testCircleCircleIntersectionAreaGeneral() {
		// General intersection area
		DoubleCircle c1 = new DoubleCircle(new DoublePoint(0.0, 0.0), 2.0);
		DoubleCircle c2 = new DoubleCircle(new DoublePoint(2.0, 0.0), 2.0);
		// With R = r = 2, d = 2:
		// cos1 = (4 + 4 - 4) / 8 = 0.5 => alpha1 = pi / 3 (60 degrees)
		// theta1 = 2pi / 3
		// area of segment = 0.5 * 4 * (2pi/3 - sin(2pi/3)) = 2 * (2pi/3 - sqrt(3)/2) = 4pi/3 - sqrt(3)
		// Total intersection area is twice the segment area: 8pi/3 - 2*sqrt(3)
		double expected = 8.0 * Math.PI / 3.0 - 2.0 * Math.sqrt(3.0);
		assertEquals(expected, c1.intersectionArea(c2), EPS);
	}

	@Test
	public void testIncircle() {
		// Right triangle formed by:
		// l1: y = 0 (bottom side)
		// l2: x = 0 (left side)
		// l3: 3x + 4y = 12 (hypotenuse)
		// Vertices are: (0, 0), (4, 0), (0, 3)
		// Area = 6, Perimeter = 12, Inradius r = Area / semiperimeter = 6 / 6 = 1.0
		// Incenter is (1, 1)
		DoubleLine l1 = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(1, 0)); // y = 0
		DoubleLine l2 = new DoubleLine(new DoublePoint(0, 0), new DoublePoint(0, 1)); // x = 0
		DoubleLine l3 = new DoubleLine(new DoublePoint(4, 0), new DoublePoint(0, 3)); // 3x + 4y = 12

		DoubleCircle c = GeometryUtils.incircle(l1, l2, l3);
		assertNotNull(c);
		assertEquals(1.0, c.radius(), EPS);
		assertEquals(1.0, c.center().x(), EPS);
		assertEquals(1.0, c.center().y(), EPS);

		// Parallel line case (no triangle formed)
		DoubleLine l4 = new DoubleLine(new DoublePoint(0, 1), new DoublePoint(1, 1)); // y = 1, parallel to l1
		assertNull(GeometryUtils.incircle(l1, l4, l3));
	}
}
