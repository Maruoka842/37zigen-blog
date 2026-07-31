package library.util.geometry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

public class GeometryEqualsHashCodeTest {

    private <T> void assertStandardEqualsAndHashCode(T a, T b, T c) {
        // Reflexivity
        assertTrue(a.equals(a));

        // Symmetry and Equality
        if (a.equals(b)) {
            assertTrue(b.equals(a));
            assertEquals(a.hashCode(), b.hashCode());
        } else {
            assertFalse(b.equals(a));
        }

        // Transitivity
        if (a.equals(b) && b.equals(c)) {
            assertTrue(a.equals(c));
        }

        // Null and Class type checks
        assertFalse(a.equals(null));
        assertFalse(a.equals("Not an instance"));
    }

    @Test
    public void testDoubleLine() {
        DoubleLine l1 = new DoubleLine(0.0, 0.0, 1.0, 2.0);
        l1.a = 2.0; l1.b = 4.0; l1.c = 6.0;
        DoubleLine l2 = new DoubleLine(0.0, 0.0, 1.0, 2.0);
        l2.a = 2.0; l2.b = 4.0; l2.c = 6.0;
        DoubleLine l3 = new DoubleLine(0.0, 0.0, 1.0, 2.0);
        l3.a = 2.0; l3.b = 4.0; l3.c = 6.0;

        assertStandardEqualsAndHashCode(l1, l2, l3);

        DoubleLine lDiff = new DoubleLine(0.0, 0.0, 1.0, 2.0);
        lDiff.a = 3.0; lDiff.b = 4.0; lDiff.c = 6.0;
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testIntLine() {
        IntLine l1 = new IntLine(1, 2, 3);
        IntLine l2 = new IntLine(2, 4, 6); // reduced to 1x+2y+3=0
        IntLine l3 = new IntLine(1, 2, 3);

        assertStandardEqualsAndHashCode(l1, l2, l3);
        assertTrue(l1.equals(l2));

        IntLine lDiff = new IntLine(1, 2, 4);
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testLongLine() {
        LongLine l1 = new LongLine(1L, 2L, 3L);
        LongLine l2 = new LongLine(2L, 4L, 6L); // reduced
        LongLine l3 = new LongLine(1L, 2L, 3L);

        assertStandardEqualsAndHashCode(l1, l2, l3);
        assertTrue(l1.equals(l2));

        LongLine lDiff = new LongLine(1L, 2L, 4L);
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testLongOrientedLine() {
        LongOrientedLine l1 = new LongOrientedLine(1L, 2L, 3L);
        LongOrientedLine l2 = new LongOrientedLine(2L, 4L, 6L); // reduced
        LongOrientedLine l3 = new LongOrientedLine(1L, 2L, 3L);

        assertStandardEqualsAndHashCode(l1, l2, l3);
        assertTrue(l1.equals(l2));

        LongOrientedLine lDiff = new LongOrientedLine(1L, 2L, 4L);
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testDoubleSegment() {
        DoubleSegment s1 = new DoubleSegment(1.0, 2.0, 3.0, 4.0);
        DoubleSegment s2 = new DoubleSegment(1.0, 2.0, 3.0, 4.0);
        DoubleSegment s3 = new DoubleSegment(1.0, 2.0, 3.0, 4.0);

        assertStandardEqualsAndHashCode(s1, s2, s3);
        assertTrue(s1.equals(s2));

        DoubleSegment sDiff = new DoubleSegment(1.0, 2.0, 3.0, 5.0);
        assertFalse(s1.equals(sDiff));
    }

    @Test
    public void testLongSegment() {
        LongSegment s1 = new LongSegment(1L, 2L, 3L, 4L);
        LongSegment s2 = new LongSegment(1L, 2L, 3L, 4L);
        LongSegment s3 = new LongSegment(1L, 2L, 3L, 4L);

        assertStandardEqualsAndHashCode(s1, s2, s3);
        assertTrue(s1.equals(s2));

        LongSegment sDiff = new LongSegment(1L, 2L, 3L, 5L);
        assertFalse(s1.equals(sDiff));
    }

    @Test
    public void testDoublePolytope2D() {
        DoublePoint p1 = new DoublePoint(0, 0);
        DoublePoint p2 = new DoublePoint(2, 0);
        DoublePoint p3 = new DoublePoint(1, 2);

        DoublePolytope2D poly1 = new DoublePolytope2D(new DoublePoint[] {p1, p2, p3});
        // Shifted starting vertex
        DoublePolytope2D poly2 = new DoublePolytope2D(new DoublePoint[] {p2, p3, p1});
        DoublePolytope2D poly3 = new DoublePolytope2D(new DoublePoint[] {p3, p1, p2});

        assertStandardEqualsAndHashCode(poly1, poly2, poly3);
        assertTrue(poly1.equals(poly2));
        assertTrue(poly2.equals(poly3));
        assertEquals(poly1.hashCode(), poly2.hashCode());
        assertEquals(poly2.hashCode(), poly3.hashCode());

        DoublePolytope2D polyDiff = new DoublePolytope2D(new DoublePoint[] {p1, p2, new DoublePoint(1, 3)});
        assertFalse(poly1.equals(polyDiff));
    }

    @Test
    public void testLatticePolytope2D() {
        LongPoint p1 = new LongPoint(0, 0);
        LongPoint p2 = new LongPoint(2, 0);
        LongPoint p3 = new LongPoint(1, 2);

        LatticePolytope2D poly1 = new LatticePolytope2D(new LongPoint[] {p1, p2, p3});
        // Shifted starting vertex
        LatticePolytope2D poly2 = new LatticePolytope2D(new LongPoint[] {p2, p3, p1});
        LatticePolytope2D poly3 = new LatticePolytope2D(new LongPoint[] {p3, p1, p2});

        assertStandardEqualsAndHashCode(poly1, poly2, poly3);
        assertTrue(poly1.equals(poly2));
        assertTrue(poly2.equals(poly3));
        assertEquals(poly1.hashCode(), poly2.hashCode());
        assertEquals(poly2.hashCode(), poly3.hashCode());

        LatticePolytope2D polyDiff = new LatticePolytope2D(new LongPoint[] {p1, p2, new LongPoint(1, 3)});
        assertFalse(poly1.equals(polyDiff));
    }

    @Test
    public void testIncrementalConvexHull() {
        IncrementalConvexHull h1 = new IncrementalConvexHull();
        h1.add(0, 0); h1.add(2, 0); h1.add(1, 2);

        IncrementalConvexHull h2 = new IncrementalConvexHull();
        h2.add(0, 0); h2.add(2, 0); h2.add(1, 2);

        IncrementalConvexHull h3 = new IncrementalConvexHull();
        h3.add(0, 0); h3.add(2, 0); h3.add(1, 2);

        assertStandardEqualsAndHashCode(h1, h2, h3);
        assertTrue(h1.equals(h2));

        IncrementalConvexHull hDiff = new IncrementalConvexHull();
        hDiff.add(0, 0); hDiff.add(2, 0); hDiff.add(1, 3);
        assertFalse(h1.equals(hDiff));
    }

    @Test
    public void testMonotonicConvexHullTrick() {
        MonotonicConvexHullTrick m1 = new MonotonicConvexHullTrick();
        m1.add(1, 5); m1.add(2, 3);

        MonotonicConvexHullTrick m2 = new MonotonicConvexHullTrick();
        m2.add(1, 5); m2.add(2, 3);

        MonotonicConvexHullTrick m3 = new MonotonicConvexHullTrick();
        m3.add(1, 5); m3.add(2, 3);

        assertStandardEqualsAndHashCode(m1, m2, m3);
        assertTrue(m1.equals(m2));

        MonotonicConvexHullTrick mDiff = new MonotonicConvexHullTrick();
        mDiff.add(1, 5); mDiff.add(2, 4);
        assertFalse(m1.equals(mDiff));

        // Test inner Line equals/hashCode
        MonotonicConvexHullTrick.Line l1 = new MonotonicConvexHullTrick.Line(1, 2);
        MonotonicConvexHullTrick.Line l2 = new MonotonicConvexHullTrick.Line(1, 2);
        MonotonicConvexHullTrick.Line lDiff = new MonotonicConvexHullTrick.Line(1, 3);
        assertTrue(l1.equals(l2));
        assertEquals(l1.hashCode(), l2.hashCode());
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testUpperEnvelope() {
        UpperEnvelope u1 = new UpperEnvelope();
        u1.registerLine(new LongLine(1L, 1L, 1L));
        u1.registerLine(new LongLine(2L, 1L, 0L));

        UpperEnvelope u2 = new UpperEnvelope();
        u2.registerLine(new LongLine(1L, 1L, 1L));
        u2.registerLine(new LongLine(2L, 1L, 0L));

        UpperEnvelope u3 = new UpperEnvelope();
        u3.registerLine(new LongLine(1L, 1L, 1L));
        u3.registerLine(new LongLine(2L, 1L, 0L));

        assertStandardEqualsAndHashCode(u1, u2, u3);
        assertTrue(u1.equals(u2));

        UpperEnvelope uDiff = new UpperEnvelope();
        uDiff.registerLine(new LongLine(1L, 1L, 1L));
        assertFalse(u1.equals(uDiff));
    }

    @Test
    public void testLineLowerEnvelope() {
        LineLowerEnvelope l1 = new LineLowerEnvelope();
        l1.registerLine(new LongLine(1L, 1L, 1L));
        l1.registerLine(new LongLine(2L, 1L, 0L));

        LineLowerEnvelope l2 = new LineLowerEnvelope();
        l2.registerLine(new LongLine(1L, 1L, 1L));
        l2.registerLine(new LongLine(2L, 1L, 0L));

        LineLowerEnvelope l3 = new LineLowerEnvelope();
        l3.registerLine(new LongLine(1L, 1L, 1L));
        l3.registerLine(new LongLine(2L, 1L, 0L));

        assertStandardEqualsAndHashCode(l1, l2, l3);
        assertTrue(l1.equals(l2));

        LineLowerEnvelope lDiff = new LineLowerEnvelope();
        lDiff.registerLine(new LongLine(1L, 1L, 1L));
        assertFalse(l1.equals(lDiff));
    }

    @Test
    public void testSegmentLowerEnvelope() {
        SegmentLowerEnvelope s1 = new SegmentLowerEnvelope();
        s1.add(0, 10, 1, 5);

        SegmentLowerEnvelope s2 = new SegmentLowerEnvelope();
        s2.add(0, 10, 1, 5);

        SegmentLowerEnvelope s3 = new SegmentLowerEnvelope();
        s3.add(0, 10, 1, 5);

        assertStandardEqualsAndHashCode(s1, s2, s3);
        assertTrue(s1.equals(s2));

        SegmentLowerEnvelope sDiff = new SegmentLowerEnvelope();
        sDiff.add(0, 10, 1, 6);
        assertFalse(s1.equals(sDiff));

        // Test inner Segment equals/hashCode
        SegmentLowerEnvelope.Segment seg1 = s1.new Segment(1, 2, 3, 4);
        SegmentLowerEnvelope.Segment seg2 = s1.new Segment(1, 2, 3, 4);
        SegmentLowerEnvelope.Segment segDiff = s1.new Segment(1, 2, 3, 5);
        assertTrue(seg1.equals(seg2));
        assertEquals(seg1.hashCode(), seg2.hashCode());
        assertFalse(seg1.equals(segDiff));
    }

    @Test
    public void testCoordinateOrderedSet2D() {
        CoordinateOrderedSet2D c1 = new CoordinateOrderedSet2D();
        c1.add(1, 2); c1.add(3, 4);

        CoordinateOrderedSet2D c2 = new CoordinateOrderedSet2D();
        c2.add(1, 2); c2.add(3, 4);

        CoordinateOrderedSet2D c3 = new CoordinateOrderedSet2D();
        c3.add(1, 2); c3.add(3, 4);

        assertStandardEqualsAndHashCode(c1, c2, c3);
        assertTrue(c1.equals(c2));

        CoordinateOrderedSet2D cDiff = new CoordinateOrderedSet2D();
        cDiff.add(1, 2); cDiff.add(3, 5);
        assertFalse(c1.equals(cDiff));
    }
}
