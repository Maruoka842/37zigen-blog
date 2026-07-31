package library.util.geometry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;

public class IncrementalConvexHullTest {

    private List<LongPoint> pruneCollinear(List<LongPoint> pts) {
        if (pts.size() < 3) return pts;
        List<LongPoint> res = new ArrayList<>();
        for (int i = 0; i < pts.size(); i++) {
            LongPoint prev = res.isEmpty() ? pts.get(pts.size() - 1) : res.get(res.size() - 1);
            LongPoint curr = pts.get(i);
            LongPoint next = pts.get((i + 1) % pts.size());

            long dx0 = curr.x() - prev.x();
            long dy0 = curr.y() - prev.y();
            long dx1 = next.x() - curr.x();
            long dy1 = next.y() - curr.y();
            if (dx0 * dy1 - dy0 * dx1 == 0) {
                continue;
            }
            res.add(curr);
        }
        if (res.size() < 3) return res;
        List<LongPoint> res2 = new ArrayList<>();
        for (int i = 0; i < res.size(); i++) {
            LongPoint prev = res2.isEmpty() ? res.get(res.size() - 1) : res2.get(res2.size() - 1);
            LongPoint curr = res.get(i);
            LongPoint next = res.get((i + 1) % res.size());
            long dx0 = curr.x() - prev.x();
            long dy0 = curr.y() - prev.y();
            long dx1 = next.x() - curr.x();
            long dy1 = next.y() - curr.y();
            if (dx0 * dy1 - dy0 * dx1 == 0) {
                continue;
            }
            res2.add(curr);
        }
        return res2;
    }

    private List<LongPoint> canonicalize(List<LongPoint> pts) {
        if (pts == null || pts.isEmpty()) return new ArrayList<>();
        int minIdx = 0;
        for (int i = 1; i < pts.size(); i++) {
            LongPoint p = pts.get(i);
            LongPoint minP = pts.get(minIdx);
            if (p.x() < minP.x() || (p.x() == minP.x() && p.y() < minP.y())) {
                minIdx = i;
            }
        }
        List<LongPoint> rotated = new ArrayList<>();
        for (int i = 0; i < pts.size(); i++) {
            rotated.add(pts.get((minIdx + i) % pts.size()));
        }
        return rotated;
    }

    private void assertHullEquals(List<LongPoint> expected, List<LongPoint> actual) {
        List<LongPoint> expCan = canonicalize(pruneCollinear(expected));
        List<LongPoint> actCan = canonicalize(pruneCollinear(actual));
        assertEquals(expCan.size(), actCan.size(), "Sizes do not match");
        for (int i = 0; i < expCan.size(); i++) {
            assertEquals(expCan.get(i), actCan.get(i), "Mismatch at index " + i);
        }
    }

    @Test
    public void testBasic() {
        IncrementalConvexHull ich = new IncrementalConvexHull();
        assertTrue(ich.add(0, 0));
        assertTrue(ich.add(2, 0));
        assertTrue(ich.add(1, 1));

        // Duplicate or redundant inside addition should return false
        assertFalse(ich.add(1, 0)); // inside the triangle (0,0)-(2,0)-(1,1)
        assertFalse(ich.add(1, 1)); // duplicate vertex

        List<LongPoint> vertices = ich.getVertices();
        List<LongPoint> expected = Arrays.asList(
            new LongPoint(0, 0),
            new LongPoint(2, 0),
            new LongPoint(1, 1)
        );
        assertHullEquals(expected, vertices);
        assertEquals(2L, ich.getDoubleArea()); // area = 1, so double area = 2

        // Check containment
        assertTrue(ich.contains(0, 0));
        assertTrue(ich.contains(2, 0));
        assertTrue(ich.contains(1, 1));
        assertTrue(ich.contains(1, 0)); // on boundary
        assertTrue(ich.contains(1, 1)); // on vertex
        assertFalse(ich.contains(1, 2)); // outside upper
        assertFalse(ich.contains(1, -1)); // outside lower
    }

    @Test
    public void testSquare() {
        IncrementalConvexHull ich = new IncrementalConvexHull();
        ich.add(0, 0);
        ich.add(10, 0);
        ich.add(10, 10);
        ich.add(0, 10);

        List<LongPoint> vertices = ich.getVertices();
        List<LongPoint> expected = Arrays.asList(
            new LongPoint(0, 0),
            new LongPoint(10, 0),
            new LongPoint(10, 10),
            new LongPoint(0, 10)
        );
        assertHullEquals(expected, vertices);
        assertEquals(4, ich.size());
        assertEquals(200L, ich.getDoubleArea()); // area of 10x10 square is 100, so double area is 200
    }

    @Test
    public void testCollinearPruning() {
        IncrementalConvexHull ich = new IncrementalConvexHull();
        assertTrue(ich.add(0, 0));
        assertTrue(ich.add(2, 0));
        assertFalse(ich.add(1, 0)); // on the segment (0,0)-(2,0) - redundant, so returns false

        // Collinear vertices are considered redundant during pruning or shouldn't prevent convex properties
        List<LongPoint> vertices = ich.getVertices();
        // Since (1,0) is collinear on (0,0)-(2,0), standard convex hull would prune it to just endpoints or we do
        List<LongPoint> expected = Arrays.asList(
            new LongPoint(0, 0),
            new LongPoint(2, 0)
        );
        assertHullEquals(expected, vertices);
    }

    @Test
    public void testExtremePoints() {
        IncrementalConvexHull ich = new IncrementalConvexHull();
        ich.add(0, 0);
        ich.add(10, 0);
        ich.add(0, 10);
        ich.add(10, 10);

        // Square vertices: (0,0), (10,0), (10,10), (0,10)
        // dx=1, dy=0 => extreme point should be x=10, y=10 (or y=0)
        LongPoint p1 = ich.getExtremePoint(1, 0);
        assertNotNull(p1);
        assertEquals(10, p1.x());

        // dx=0, dy=1 => extreme point should be y=10
        LongPoint p2 = ich.getExtremePoint(0, 1);
        assertNotNull(p2);
        assertEquals(10, p2.y());

        // dx=-1, dy=0 => extreme point should be x=0
        LongPoint p3 = ich.getExtremePoint(-1, 0);
        assertNotNull(p3);
        assertEquals(0, p3.x());

        // dx=0, dy=-1 => extreme point should be y=0
        LongPoint p4 = ich.getExtremePoint(0, -1);
        assertNotNull(p4);
        assertEquals(0, p4.y());

        // dx=1, dy=1 => (10,10)
        LongPoint p5 = ich.getExtremePoint(1, 1);
        assertNotNull(p5);
        assertEquals(new LongPoint(10, 10), p5);

        // dx=-1, dy=-1 => (0,0)
        LongPoint p6 = ich.getExtremePoint(-1, -1);
        assertNotNull(p6);
        assertEquals(new LongPoint(0, 0), p6);

        // Test getExtremePointMin and getMinDotProduct
        // dx=1, dy=0 => min point should be x=0
        LongPoint pMin1 = ich.getExtremePointMin(1, 0);
        assertNotNull(pMin1);
        assertEquals(0, pMin1.x());
        assertEquals(0L, ich.getMinDotProduct(1, 0));

        // dx=0, dy=1 => min point should be y=0
        LongPoint pMin2 = ich.getExtremePointMin(0, 1);
        assertNotNull(pMin2);
        assertEquals(0, pMin2.y());
        assertEquals(0L, ich.getMinDotProduct(0, 1));

        // dx=-1, dy=0 => min point should be x=10
        LongPoint pMin3 = ich.getExtremePointMin(-1, 0);
        assertNotNull(pMin3);
        assertEquals(10, pMin3.x());
        assertEquals(-10L, ich.getMinDotProduct(-1, 0));

        // dx=0, dy=-1 => min point should be y=10
        LongPoint pMin4 = ich.getExtremePointMin(0, -1);
        assertNotNull(pMin4);
        assertEquals(10, pMin4.y());
        assertEquals(-10L, ich.getMinDotProduct(0, -1));

        // dx=1, dy=1 => min point is (0,0) with dot product 0
        LongPoint pMin5 = ich.getExtremePointMin(1, 1);
        assertNotNull(pMin5);
        assertEquals(new LongPoint(0, 0), pMin5);
        assertEquals(0L, ich.getMinDotProduct(1, 1));

        // dx=-1, dy=-1 => min point is (10,10) with dot product -20
        LongPoint pMin6 = ich.getExtremePointMin(-1, -1);
        assertNotNull(pMin6);
        assertEquals(new LongPoint(10, 10), pMin6);
        assertEquals(-20L, ich.getMinDotProduct(-1, -1));
    }

    @Test
    public void testStressRandom() {
        Random rnd = new Random(42);
        for (int t = 0; t < 10; t++) {
            IncrementalConvexHull ich = new IncrementalConvexHull();
            List<LongPoint> pointsAdded = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                long x = rnd.nextLong(-1000, 1000);
                long y = rnd.nextLong(-1000, 1000);
                LongPoint p = new LongPoint(x, y);
                pointsAdded.add(p);
                ich.add(p);

                // Compute standard convex hull of all points added so far
                LongPoint[] staticHull = GeometryUtils.convexHull(pointsAdded.toArray(new LongPoint[0]));
                List<LongPoint> expectedHull = pruneCollinear(Arrays.asList(staticHull));

                assertHullEquals(expectedHull, ich.getVertices());
                assertEquals(expectedHull.size(), ich.size());

                // Calculate expected double area
                long expectedArea2 = 0L;
                if (expectedHull.size() >= 3) {
                    for (int j = 0; j < expectedHull.size(); j++) {
                        LongPoint curr = expectedHull.get(j);
                        LongPoint next = expectedHull.get((j + 1) % expectedHull.size());
                        long t1 = curr.x() * next.y();
                        long t2 = curr.y() * next.x();
                        expectedArea2 += (t1 - t2);
                    }
                    expectedArea2 = Math.abs(expectedArea2);
                }
                assertEquals(expectedArea2, ich.getDoubleArea());

                // Check containment of all added points
                for (LongPoint pt : pointsAdded) {
                    assertTrue(ich.contains(pt), "Hull should contain point " + pt);
                }

                // Check some random points outside
                for (int j = 0; j < 10; j++) {
                    long rx = rnd.nextLong(-2000, 2000);
                    long ry = rnd.nextLong(-2000, 2000);
                    boolean isInsideStatic = GeometryUtils.isInside(new LongPoint(rx, ry), staticHull);
                    if (isInsideStatic) {
                        assertTrue(ich.contains(rx, ry));
                    }
                }

                // Verify extreme points
                for (int d = 0; d < 10; d++) {
                    long dx = rnd.nextLong(-100, 100);
                    long dy = rnd.nextLong(-100, 100);
                    if (dx == 0 && dy == 0) continue;

                    Long maxDot = null;
                    Long minDot = null;
                    LongPoint bestPt = null;
                    LongPoint worstPt = null;
                    for (LongPoint pt : expectedHull) {
                        long dot = pt.x() * dx + pt.y() * dy;
                        if (maxDot == null || dot > maxDot) {
                            maxDot = dot;
                            bestPt = pt;
                        }
                        if (minDot == null || dot < minDot) {
                            minDot = dot;
                            worstPt = pt;
                        }
                    }

                    Long actualMaxDot = ich.getMaxDotProduct(dx, dy);
                    assertNotNull(actualMaxDot);
                    assertEquals(maxDot, actualMaxDot, "Max dot product mismatch for direction (" + dx + ", " + dy + ")");

                    LongPoint actualExtreme = ich.getExtremePoint(dx, dy);
                    assertNotNull(actualExtreme);
                    long actualExtremeDot = actualExtreme.x() * dx + actualExtreme.y() * dy;
                    assertEquals(maxDot, actualExtremeDot, "Extreme point does not achieve max dot product");

                    Long actualMinDot = ich.getMinDotProduct(dx, dy);
                    assertNotNull(actualMinDot);
                    assertEquals(minDot, actualMinDot, "Min dot product mismatch for direction (" + dx + ", " + dy + ")");

                    LongPoint actualExtremeMin = ich.getExtremePointMin(dx, dy);
                    assertNotNull(actualExtremeMin);
                    long actualExtremeMinDot = actualExtremeMin.x() * dx + actualExtremeMin.y() * dy;
                    assertEquals(minDot, actualExtremeMinDot, "Extreme point min does not achieve min dot product");
                }
            }
        }
    }

    @Test
    public void testCopy() {
        IncrementalConvexHull ich = new IncrementalConvexHull();
        ich.add(0, 0);
        ich.add(10, 0);
        ich.add(0, 10);

        IncrementalConvexHull copy = ich.copy();

        // Check identical status
        assertEquals(ich.size(), copy.size());
        assertEquals(ich.getDoubleArea(), copy.getDoubleArea());
        assertHullEquals(ich.getVertices(), copy.getVertices());

        // Mutate original
        ich.add(10, 10);
        assertEquals(4, ich.size());
        // Copy should remain unchanged
        assertEquals(3, copy.size());

        // Mutate copy
        copy.add(5, 5); // duplicate/redundant, shouldn't change size
        assertEquals(3, copy.size());
        copy.add(-5, 5);
        assertEquals(4, copy.size());
        // Original should remain unchanged regarding copy's mutation
        assertEquals(4, ich.size());
        assertFalse(ich.contains(-5, 5));
        assertTrue(copy.contains(-5, 5));
    }
}
