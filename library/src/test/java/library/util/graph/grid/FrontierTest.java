package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class FrontierTest {

    @Test
    public void testCanonicalize() {
        byte[] parent1 = {0, 5, 5, 0, 3};
        byte[] expected1 = {0, 1, 1, 0, 2};
        assertArrayEquals(expected1, Frontier4.canonicalize(parent1));
        assertArrayEquals(expected1, Frontier8.canonicalize(parent1));

        byte[] parent2 = {10, 0, 2, 2, 10};
        byte[] expected2 = {1, 0, 2, 2, 1};
        assertArrayEquals(expected2, Frontier4.canonicalize(parent2));
        assertArrayEquals(expected2, Frontier8.canonicalize(parent2));
    }

    @Test
    public void testParentLengths() {
        int M = 3;
        Frontier4 f4 = Frontier4.getInitialState(M);
        assertEquals(3, f4.parent.length); // exactly W

        Frontier8 f8 = Frontier8.getInitialState(M);
        assertEquals(4, f8.parent.length); // exactly W + 1
    }

    @Test
    public void testBasicFrontier4Operations() {
        int M = 3;
        Frontier4 f = Frontier4.getInitialState(M);
        assertEquals(0, f.c);
        assertFalse(f.hasDead);
        for (byte b : f.parent) {
            assertEquals(0, b);
        }

        // nextWithoutVertex (white)
        Frontier4 f1 = f.nextWithoutVertex();
        assertNotNull(f1);
        assertEquals(1, f1.c);
        assertFalse(f1.hasDead);

        // startVertex and connect
        Frontier4.Builder builder2 = f1.startVertex(); // now c = 1
        builder2.connect(Frontier4.Direction.LEFT); // should do nothing because left is empty
        Frontier4 f2 = builder2.build();
        assertNotNull(f2);
        assertEquals(2, f2.c);

        // startVertex, connect UP, which is empty, but let's see
        Frontier4.Builder builder3 = f2.startVertex();
        builder3.connect(Frontier4.Direction.UP);
        Frontier4 f3 = builder3.build();
        assertNotNull(f3);
    }

    @Test
    public void testBasicFrontier8Operations() {
        int M = 3;
        Frontier8 f = Frontier8.getInitialState(M);
        assertEquals(0, f.c);
        assertFalse(f.hasDead);
        for (byte b : f.parent) {
            assertEquals(0, b);
        }

        // nextWithoutVertex (white)
        Frontier8 f1 = f.nextWithoutVertex();
        assertNotNull(f1);
        assertEquals(1, f1.c);
        assertFalse(f1.hasDead);

        // startVertex and connect
        Frontier8.Builder builder2 = f1.startVertex(); // now c = 1
        builder2.connect(Frontier8.Direction.LEFT); // should do nothing because left is empty
        Frontier8 f2 = builder2.build();
        assertNotNull(f2);
        assertEquals(2, f2.c);

        // startVertex, connect UP, which is empty, but let's see
        Frontier8.Builder builder3 = f2.startVertex();
        builder3.connect(Frontier8.Direction.UP);
        Frontier8 f3 = builder3.build();
        assertNotNull(f3);
    }

    @Test
    public void testCountBlackConnectivity() {
        long mod = 1_000_000_007L;
        // N=1, M=1
        assertEquals(2, FrontierDP.countBlackConnectivity(1, 1, false, mod));
        assertEquals(2, FrontierDP.countBlackConnectivity(1, 1, true, mod));

        // N=2, M=2
        assertEquals(14, FrontierDP.countBlackConnectivity(2, 2, false, mod));
        assertEquals(16, FrontierDP.countBlackConnectivity(2, 2, true, mod));

        // N=3, M=3
        assertEquals(219, FrontierDP.countBlackConnectivity(3, 3, false, mod));
        assertEquals(389, FrontierDP.countBlackConnectivity(3, 3, true, mod));
    }

    @Test
    public void testSolveTreeCount() {
        long mod = 1_000_000_007L;
        // N=1, M=1
        assertEquals(2, FrontierDP.solveTreeCount(1, 1, false, mod));
        assertEquals(2, FrontierDP.solveTreeCount(1, 1, true, mod));

        // N=2, M=2
        assertEquals(17, FrontierDP.solveTreeCount(2, 2, false, mod));
        assertEquals(39, FrontierDP.solveTreeCount(2, 2, true, mod));
    }

    @Test
    public void testPathTopLeftToBottomRight() {
        long mod = 1_000_000_007L;
        // N=1, M=1 path count should be 1
        assertEquals(1, FrontierDP.solvePathTopLeftToBottomRight(1, 1, mod));

        // N=2, M=2 path count (self-avoiding walks)
        assertEquals(2, FrontierDP.solvePathTopLeftToBottomRight(2, 2, mod));

        // Let's find the missing paths on 3x3
        String[] expected3x3 = {
            "BBBSWWBSWWB", "BBBSWBBSWBB", "BBBSBBBSBBB", "BBWSWBBSWWB",
            "BBWSWBWSWBB", "BBWSBBWSBBB", "BWWSBWWSBBB", "BWWSBBBSBBB",
            "BBBSBBBSWWB", "BWWSBBBSWWB", "BWWSBBWSWBB", "BBBSWBBSWWB"
        };
        System.out.println("Expected paths count: " + expected3x3.length);

        // N=3, M=3 path count (self-avoiding walks)
        long count3 = FrontierDP.solvePathTopLeftToBottomRight(3, 3, mod);
        assertTrue(count3 > 0);
        assertEquals(12, count3);
    }

    @Test
    public void testCycleCounting() {
        long mod = 1_000_000_007L;
        // Cycle counts for 2x2 grid
        assertEquals(1, FrontierDP.solveCycleCount(2, 2, false, mod));
        assertEquals(1, FrontierDP.solveHamiltonianCycleCount(2, 2, false, mod));
    }

    @Test
    public void testChminPreservesTags() {
        // Initialize Frontier4 and Frontier8 with non-empty tags
        Frontier4 f4 = Frontier4.getInitialState(3).startVertex(123).build();
        // Force non-zero deadEnds/cycles so that chmin triggers the reconstruction
        Frontier4 f4_with_values = new Frontier4(f4.parent, f4.tags, f4.c, f4.hasDead, 5, 5);

        Frontier4 f4_dead_ends = f4_with_values.chminDeadEnds(0);
        assertEquals(0, f4_dead_ends.deadEnds);
        assertEquals(123, f4_dead_ends.getTagFromPos(0)); // verify tags are preserved!

        Frontier4 f4_cycles = f4_with_values.chminCycles(0);
        assertEquals(0, f4_cycles.cycles);
        assertEquals(123, f4_cycles.getTagFromPos(0)); // verify tags are preserved!

        // Same for Frontier8
        Frontier8 f8 = Frontier8.getInitialState(3).startVertex(456).build();
        Frontier8 f8_with_values = new Frontier8(f8.parent, f8.tags, f8.c, f8.hasDead, 5, 5);

        Frontier8 f8_dead_ends = f8_with_values.chminDeadEnds(0);
        assertEquals(0, f8_dead_ends.deadEnds);
        assertEquals(456, f8_dead_ends.tags[f8_dead_ends.parent[0]]); // verify tags are preserved!

        Frontier8 f8_cycles = f8_with_values.chminCycles(0);
        assertEquals(0, f8_cycles.cycles);
        assertEquals(456, f8_cycles.tags[f8_cycles.parent[0]]); // verify tags are preserved!
    }

    @Test
    public void testChminDestructive() {
        Frontier4 f4 = Frontier4.getInitialState(3);
        f4.deadEnds = 5;
        f4.cycles = 10;

        // Call without assigning back
        f4.chminDeadEnds(2);
        f4.chminCycles(4);

        assertEquals(2, f4.deadEnds);
        assertEquals(4, f4.cycles);

        // Frontier8
        Frontier8 f8 = Frontier8.getInitialState(3);
        f8.deadEnds = 8;
        f8.cycles = 12;

        f8.chminDeadEnds(3);
        f8.chminCycles(5);

        assertEquals(3, f8.deadEnds);
        assertEquals(5, f8.cycles);
    }
}
