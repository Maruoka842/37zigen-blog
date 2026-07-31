package library.util.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DoublePolytope2DTest {

	private static final double EPS = 1e-9;

	@Test
	public void testAreaAndScaleTranslate() {
		// A right triangle at (0,0), (4,0), (0,3) in CCW order
		DoublePoint[] vertices = {
			new DoublePoint(0.0, 0.0),
			new DoublePoint(4.0, 0.0),
			new DoublePoint(0.0, 3.0)
		};
		DoublePolytope2D poly = new DoublePolytope2D(vertices);

		assertEquals(12.0, poly.twiceArea(), EPS);
		assertEquals(6.0, poly.area(), EPS);

		// Scale by 2.5
		DoublePolytope2D scaled = poly.scale(2.5);
		assertEquals(6.0 * 2.5 * 2.5, scaled.area(), EPS);

		// Translate by (10, -5)
		DoublePolytope2D translated = poly.translate(10.0, -5.0);
		assertEquals(6.0, translated.area(), EPS);
		assertEquals(10.0, translated.vertices[0].x(), EPS);
		assertEquals(-5.0, translated.vertices[0].y(), EPS);
	}

	@Test
	public void testFromPoints() {
		DoublePoint[] points = {
			new DoublePoint(1.0, 1.0),
			new DoublePoint(1.0, 1.0),
			new DoublePoint(0.0, 0.0),
			new DoublePoint(4.0, 0.0),
			new DoublePoint(0.0, 3.0),
			new DoublePoint(2.0, 0.0),
			new DoublePoint(1.0, 0.5) // interior
		};
		DoublePolytope2D poly = DoublePolytope2D.fromPoints(points);
		assertEquals(3, poly.vertices.length);
		assertEquals(6.0, poly.area(), EPS);
	}

	@Test
	public void testMinkowskiSum() {
		// S1: unit square [0,1]x[0,1]
		DoublePoint[] v1 = {
			new DoublePoint(0.0, 0.0),
			new DoublePoint(1.0, 0.0),
			new DoublePoint(1.0, 1.0),
			new DoublePoint(0.0, 1.0)
		};
		DoublePolytope2D p1 = new DoublePolytope2D(v1);

		// S2: unit square [0,1]x[0,1]
		DoublePolytope2D p2 = new DoublePolytope2D(v1);

		// Minkowski sum of two [0,1] squares should be [0,2] square (area 4.0)
		DoublePolytope2D sum = p1.minkowskiSum(p2);
		assertEquals(4.0, sum.area(), EPS);
	}

	@Test
	public void testDistance() {
		// 1-point polygon
		DoublePolytope2D poly1 = new DoublePolytope2D(new DoublePoint[]{new DoublePoint(1.0, 2.0)});
		assertEquals(5.0, poly1.distance(new DoublePoint(1.0, 7.0)), EPS);

		// 2-point polygon
		DoublePolytope2D poly2 = new DoublePolytope2D(new DoublePoint[]{new DoublePoint(0.0, 0.0), new DoublePoint(4.0, 0.0)});
		assertEquals(3.0, poly2.distance(new DoublePoint(2.0, 3.0)), EPS);

		// 3-point CCW polygon (Triangle)
		DoublePolytope2D poly3 = new DoublePolytope2D(new DoublePoint[]{
			new DoublePoint(0.0, 0.0),
			new DoublePoint(4.0, 0.0),
			new DoublePoint(0.0, 3.0)
		});

		// Point inside
		assertEquals(0.0, poly3.distance(new DoublePoint(1.0, 1.0)), EPS);

		// Point on boundary
		assertEquals(0.0, poly3.distance(new DoublePoint(2.0, 0.0)), EPS);
		assertEquals(0.0, poly3.distance(new DoublePoint(2.0, 1.5)), EPS);

		// Point outside, closest to a vertex (e.g. near (0,0))
		assertEquals(2.0, poly3.distance(new DoublePoint(-2.0, 0.0)), EPS);

		// Point outside, closest to hypotenuse
		// Hypotenuse line: 3x + 4y - 12 = 0. Closest point on line from (4,3) is projection.
		// Distance from (4,3) to line is |3(4)+4(3)-12| / 5 = 12/5 = 2.4.
		assertEquals(2.4, poly3.distance(new DoublePoint(4.0, 3.0)), EPS);
	}

	@Test
	public void testDist() {
		DoublePolytope2D square = new DoublePolytope2D(new DoublePoint[]{
			new DoublePoint(0.0, 0.0),
			new DoublePoint(2.0, 0.0),
			new DoublePoint(2.0, 2.0),
			new DoublePoint(0.0, 2.0)
		});

		DoublePolytope2D separated = new DoublePolytope2D(new DoublePoint[]{
			new DoublePoint(5.0, 0.0),
			new DoublePoint(7.0, 0.0),
			new DoublePoint(7.0, 2.0),
			new DoublePoint(5.0, 2.0)
		});
		assertEquals(3.0, square.dist(separated), EPS);
		assertEquals(3.0, separated.dist(square), EPS);

		DoublePolytope2D crossing = new DoublePolytope2D(new DoublePoint[]{
			new DoublePoint(0.5, -1.0),
			new DoublePoint(1.5, -1.0),
			new DoublePoint(1.5, 3.0),
			new DoublePoint(0.5, 3.0)
		});
		assertEquals(0.0, square.dist(crossing), EPS);

		DoublePolytope2D point = new DoublePolytope2D(new DoublePoint[]{new DoublePoint(5.0, 5.0)});
		assertEquals(Math.sqrt(18.0), square.dist(point), EPS);
	}

	@Test
	public void testDistAgainstMinkowskiDifference() {
		java.util.Random random = new java.util.Random(24680);
		for (int testCase = 0; testCase < 100; testCase++) {
			DoublePoint[] firstPoints = new DoublePoint[20];
			DoublePoint[] secondPoints = new DoublePoint[20];
			for (int i = 0; i < 20; i++) {
				firstPoints[i] = new DoublePoint(random.nextDouble() * 20.0 - 10.0, random.nextDouble() * 20.0 - 10.0);
				secondPoints[i] = new DoublePoint(random.nextDouble() * 20.0 - 10.0, random.nextDouble() * 20.0 - 10.0);
			}
			DoublePolytope2D first = DoublePolytope2D.fromPoints(firstPoints);
			DoublePolytope2D second = DoublePolytope2D.fromPoints(secondPoints);
			DoublePolytope2D difference = first.minkowskiSum(second.scale(-1.0));
			assertEquals(difference.distance(DoublePoint.origin), first.dist(second), 1e-8);
		}
	}

	@Test
	public void testLogNDistanceCorrectnessStress() {
		java.util.Random rnd = new java.util.Random(12345);
		int numPolygons = 50;
		for (int t = 0; t < numPolygons; t++) {
			int numPoints = rnd.nextInt(25) + 3; // 3 to 27 points
			DoublePoint[] pts = new DoublePoint[numPoints];
			for (int j = 0; j < numPoints; j++) {
				pts[j] = new DoublePoint(rnd.nextDouble() * 200 - 100, rnd.nextDouble() * 200 - 100);
			}
			DoublePolytope2D poly = DoublePolytope2D.fromPoints(pts);

			// Test 500 random points for each polygon
			for (int sample = 0; sample < 500; sample++) {
				DoublePoint p = new DoublePoint(rnd.nextDouble() * 300 - 150, rnd.nextDouble() * 300 - 150);

				// Calculate true linear distance manually using segments
				double linearDist = Double.POSITIVE_INFINITY;
				int n = poly.vertices.length;
				for (int i = 0; i < n; i++) {
					// We can use a simple manual projection or the private distToSegment of a dummy 2-point polytope
					DoublePolytope2D seg = new DoublePolytope2D(new DoublePoint[]{poly.vertices[i], poly.vertices[(i+1)%n]});
					double d = seg.distance(p);
					if (d < linearDist) {
						linearDist = d;
					}
				}

				double binaryDist = poly.distance(p);

				boolean actualInside = false;
				boolean hasPos = false;
				boolean hasNeg = false;
				for (int i = 0; i < n; i++) {
					DoublePoint curr = poly.vertices[i];
					DoublePoint next = poly.vertices[(i + 1) % n];
					double cross = (next.x() - curr.x()) * (p.y() - curr.y()) - (next.y() - curr.y()) * (p.x() - curr.x());
					if (cross > 1e-9) hasPos = true;
					if (cross < -1e-9) hasNeg = true;
				}
				if (!hasPos || !hasNeg) {
					actualInside = true;
				}

				if (Math.abs(linearDist - binaryDist) > 1e-9 && !actualInside) {
					System.out.println("FAILURE CAPTURED:");
					System.out.println("Point P: " + p);
					System.out.println("Polygon vertices:");
					for (int i = 0; i < n; i++) {
						System.out.println("  V[" + i + "]: " + poly.vertices[i]);
					}
					System.out.println("linearDist: " + linearDist);
					System.out.println("binaryDist: " + binaryDist);
					int[] tangentsVal = poly.tangents(p);
					System.out.println("Tangents: L=" + tangentsVal[0] + ", R=" + tangentsVal[1]);
				}

				if (actualInside) {
					assertEquals(0.0, binaryDist, 1e-9);
				} else {
					assertEquals(linearDist, binaryDist, 1e-9, "Mismatch at point " + p + " for polygon of size " + poly.vertices.length);
				}
			}
		}
	}
}
