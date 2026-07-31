package library.util.geometry;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class GeometryUtilsTest {

	private static final double EPS = 1e-9;

	@Test
	public void testMinimizeMaxDistanceToPolygons() {
		// Test case 1: Single polygon (triangle)
		// Point minimizing max distance to a single polygon should be any point inside it, having distance 0.
		DoublePoint[] tri = {
			new DoublePoint(0.0, 0.0),
			new DoublePoint(4.0, 0.0),
			new DoublePoint(0.0, 3.0)
		};
		List<DoublePolytope2D> list1 = List.of(new DoublePolytope2D(tri));
		DoublePoint opt1 = GeometryUtils.minimizeMaxDistanceToPolygons(list1);
		double dist1 = new DoublePolytope2D(tri).distance(opt1);
		assertEquals(0.0, dist1, 1e-6);

		// Test case 2: Overlapping polygons
		// Intersection is non-empty. Optimal point should be in the intersection, max distance = 0.
		DoublePoint[] rect1 = {
			new DoublePoint(0.0, 0.0),
			new DoublePoint(2.0, 0.0),
			new DoublePoint(2.0, 2.0),
			new DoublePoint(0.0, 2.0)
		};
		DoublePoint[] rect2 = {
			new DoublePoint(1.0, 1.0),
			new DoublePoint(3.0, 1.0),
			new DoublePoint(3.0, 3.0),
			new DoublePoint(1.0, 3.0)
		};
		List<DoublePolytope2D> list2 = List.of(new DoublePolytope2D(rect1), new DoublePolytope2D(rect2));
		DoublePoint opt2 = GeometryUtils.minimizeMaxDistanceToPolygons(list2);
		double d1 = new DoublePolytope2D(rect1).distance(opt2);
		double d2 = new DoublePolytope2D(rect2).distance(opt2);
		assertEquals(0.0, Math.max(d1, d2), 1e-6);

		// Test case 3: Two disjoint single-point polygons (points)
		// A = (0, 0), B = (4, 0). Optimal point should be (2, 0).
		DoublePoint[] pA = {new DoublePoint(0.0, 0.0)};
		DoublePoint[] pB = {new DoublePoint(4.0, 0.0)};
		List<DoublePolytope2D> list3 = List.of(new DoublePolytope2D(pA), new DoublePolytope2D(pB));
		DoublePoint opt3 = GeometryUtils.minimizeMaxDistanceToPolygons(list3);
		assertEquals(2.0, opt3.x(), 1e-6);
		assertEquals(0.0, opt3.y(), 1e-6);

		// Test case 4: Three disjoint single-point polygons forming equilateral triangle
		// (0,0), (2,0), (1, sqrt(3)).
		// Centroid is (1, 1/sqrt(3)). Distance to all three is 2/sqrt(3).
		DoublePoint[] p1 = {new DoublePoint(0.0, 0.0)};
		DoublePoint[] p2 = {new DoublePoint(2.0, 0.0)};
		DoublePoint[] p3 = {new DoublePoint(1.0, Math.sqrt(3.0))};
		List<DoublePolytope2D> list4 = List.of(new DoublePolytope2D(p1), new DoublePolytope2D(p2), new DoublePolytope2D(p3));
		DoublePoint opt4 = GeometryUtils.minimizeMaxDistanceToPolygons(list4);
		assertEquals(1.0, opt4.x(), 1e-6);
		assertEquals(1.0 / Math.sqrt(3.0), opt4.y(), 1e-6);

		// Test case 5: Four disjoint single-point polygons at (0,0), (0,2), (4,0), (4,2)
		// The optimal point should be uniquely (2.0, 1.0) because the maximum distance to the corners
		// is minimized at the center of the bounding box.
		DoublePoint[] corner1 = {new DoublePoint(0.0, 0.0)};
		DoublePoint[] corner2 = {new DoublePoint(0.0, 2.0)};
		DoublePoint[] corner3 = {new DoublePoint(4.0, 0.0)};
		DoublePoint[] corner4 = {new DoublePoint(4.0, 2.0)};
		List<DoublePolytope2D> list5 = List.of(
			new DoublePolytope2D(corner1),
			new DoublePolytope2D(corner2),
			new DoublePolytope2D(corner3),
			new DoublePolytope2D(corner4)
		);
		DoublePoint opt5 = GeometryUtils.minimizeMaxDistanceToPolygons(list5);
		assertEquals(2.0, opt5.x(), 1e-6);
		assertEquals(1.0, opt5.y(), 1e-6);
	}

	@Test
	public void testMinimizeMaxDistanceAcuteTriangle() {
		// For an acute triangle A(0,0), B(6,0), C(2,4), the unique point that minimizes the
		// maximum distance to the vertices is the circumcenter.
		// Circumcenter equation:
		// x = 3 (perpendicular bisector of AB)
		// Perpendicular bisector of AC: y - 2 = -0.5 * (x - 1) => y = -0.5*x + 2.5
		// At x = 3: y = 1.0. So optimal point is exactly (3, 1) with distance sqrt(10) ≈ 3.16227766.
		DoublePoint[] pA = {new DoublePoint(0.0, 0.0)};
		DoublePoint[] pB = {new DoublePoint(6.0, 0.0)};
		DoublePoint[] pC = {new DoublePoint(2.0, 4.0)};
		List<DoublePolytope2D> polygons = List.of(
			new DoublePolytope2D(pA),
			new DoublePolytope2D(pB),
			new DoublePolytope2D(pC)
		);
		DoublePoint opt = GeometryUtils.minimizeMaxDistanceToPolygons(polygons);
		assertEquals(3.0, opt.x(), 1e-6);
		assertEquals(1.0, opt.y(), 1e-6);
	}

	@Test
	public void testMinimizeMaxDistanceObtuseTriangle() {
		// For an obtuse triangle A(0,0), B(6,0), C(1,1), the unique point minimizing the
		// maximum distance to the vertices is the midpoint of the longest side AB, which is (3, 0).
		// Maximum distance is exactly 3.0.
		DoublePoint[] pA = {new DoublePoint(0.0, 0.0)};
		DoublePoint[] pB = {new DoublePoint(6.0, 0.0)};
		DoublePoint[] pC = {new DoublePoint(1.0, 1.0)};
		List<DoublePolytope2D> polygons = List.of(
			new DoublePolytope2D(pA),
			new DoublePolytope2D(pB),
			new DoublePolytope2D(pC)
		);
		DoublePoint opt = GeometryUtils.minimizeMaxDistanceToPolygons(polygons);
		assertEquals(3.0, opt.x(), 1e-6);
		assertEquals(0.0, opt.y(), 1e-6);
	}

	@Test
	public void testTernarySearchStressAndLocalOptimality() {
		// Stress test with random convex polygons and mathematically prove global/local optimality.
		java.util.Random rnd = new java.util.Random(42);
		int numTests = 20;
		for (int t = 0; t < numTests; t++) {
			int numPolygons = rnd.nextInt(3) + 3; // 3 to 5 polygons
			List<DoublePolytope2D> polygons = new java.util.ArrayList<>();
			double minX = 0, maxX = 100, minY = 0, maxY = 100;
			for (int i = 0; i < numPolygons; i++) {
				int numPoints = rnd.nextInt(5) + 3; // 3 to 7 points
				DoublePoint[] pts = new DoublePoint[numPoints];
				for (int j = 0; j < numPoints; j++) {
					pts[j] = new DoublePoint(rnd.nextDouble() * 100, rnd.nextDouble() * 100);
				}
				polygons.add(DoublePolytope2D.fromPoints(pts));
			}

			DoublePoint opt = GeometryUtils.minimizeMaxDistanceToPolygons(polygons);
			double optVal = evaluateMaxDistance(opt, polygons);

			// 1. Verify against 1000 random samples in the bounding box
			for (int sample = 0; sample < 1000; sample++) {
				DoublePoint randPt = new DoublePoint(rnd.nextDouble() * 100, rnd.nextDouble() * 100);
				double sampleVal = evaluateMaxDistance(randPt, polygons);
				assertTrue(sampleVal >= optVal - 1e-6, "Random sample at " + randPt + " has distance " + sampleVal + " which is strictly smaller than optVal " + optVal);
			}

			// 2. Verify local neighborhood grid of fine resolution
			double[] steps = {-1e-5, 0.0, 1e-5};
			for (double dx : steps) {
				for (double dy : steps) {
					DoublePoint neighbor = new DoublePoint(opt.x() + dx, opt.y() + dy);
					double neighborVal = evaluateMaxDistance(neighbor, polygons);
					assertTrue(neighborVal >= optVal - 1e-9, "Local neighbor at " + neighbor + " has distance " + neighborVal + " which is strictly smaller than optVal " + optVal);
				}
			}
		}
	}

	private double evaluateMaxDistance(DoublePoint p, List<DoublePolytope2D> polygons) {
		double maxDist = 0.0;
		for (DoublePolytope2D poly : polygons) {
			double d = poly.distance(p);
			if (d > maxDist) {
				maxDist = d;
			}
		}
		return maxDist;
	}

	@Test
	public void testInvalidInput() {
		assertThrows(IllegalArgumentException.class, () -> {
			GeometryUtils.minimizeMaxDistanceToPolygons(List.of());
		});
		assertThrows(IllegalArgumentException.class, () -> {
			List<DoublePolytope2D> emptyList = new ArrayList<>();
			emptyList.add(new DoublePolytope2D(new DoublePoint[0]));
			GeometryUtils.minimizeMaxDistanceToPolygons(emptyList);
		});
	}
}
