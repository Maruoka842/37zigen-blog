package library.util.graph.grid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PathFrontierTest {

    @Test
    public void testPathFrontier4Basics() {
        PathFrontier4 pf = PathFrontier4.getInitialState(2);
        assertEquals(2, pf.parent.length);
        assertEquals(0, pf.c);
        assertEquals(0, pf.deadEnds);
        assertEquals(0, pf.cycles);

        // Test hasVertex & isConnected
        assertFalse(pf.hasVertex(0));
        assertFalse(pf.hasVertex(1));
        assertFalse(pf.isConnected(0, 1));

        // Let's create a builder and connect some things
        PathFrontier4.Builder builder = pf.startVertex();
        PathFrontier4 next = builder.build();
        assertNotNull(next);
        assertTrue(next.hasVertex(0));
        assertFalse(next.hasVertex(1));

        // Test chmin
        PathFrontier4 minimizedPf = next.chminDeadEnds(10).chminCycles(5);
        assertEquals(0, minimizedPf.deadEnds);
        assertEquals(0, minimizedPf.cycles);

        PathFrontier4 highPf = new PathFrontier4(next.parent, next.c, next.hasDead, 15, 8);
        PathFrontier4 lowPf = highPf.chminDeadEnds(5).chminCycles(3);
        assertEquals(5, lowPf.deadEnds);
        assertEquals(3, lowPf.cycles);
    }

    @Test
    public void testPathFrontier8Basics() {
        PathFrontier8 pf = PathFrontier8.getInitialState(2);
        assertEquals(3, pf.parent.length);
        assertEquals(0, pf.c);
        assertEquals(0, pf.deadEnds);
        assertEquals(0, pf.cycles);

        assertFalse(pf.hasVertex(0));
        assertFalse(pf.isConnected(0, 1));

        PathFrontier8.Builder builder = pf.startVertex();
        PathFrontier8 next = builder.build();
        assertNotNull(next);
        assertTrue(next.hasVertex(0));

        PathFrontier8 highPf = new PathFrontier8(next.parent, next.c, next.hasDead, 15, 8);
        PathFrontier8 lowPf = highPf.chminDeadEnds(5).chminCycles(3);
        assertEquals(5, lowPf.deadEnds);
        assertEquals(3, lowPf.cycles);
    }

    @Test
    public void testGetPossibleConnections() {
        PathFrontier4 pf = PathFrontier4.getInitialState(2);
        // We test with empty/no neighbors.
        // Connecting to Direction.LEFT and Direction.UP (both have neighbor value 0 because pf.parent has all 0s)
        // Since neighbor values are 0, connect should return false in path-mode.
        // Therefore, any subset containing LEFT or UP will be invalid, except the empty subset (which doesn't connect to anything).
        // Thus, getPossibleConnections should return exactly 1 state (representing the empty subset).
        Iterable<PathFrontier4> possible = pf.getPossibleConnections(PathFrontier4.Direction.LEFT, PathFrontier4.Direction.UP);
        int count = 0;
        for (PathFrontier4 state : possible) {
            count++;
        }
        assertEquals(1, count);

        // Same for Frontier4 (non-path mode)
        Frontier4 f = Frontier4.getInitialState(2);
        // In non-path mode, connecting to active=false direction UP/LEFT with activate=false (default) does not mutate neighbor,
        // but let's check:
        // if neighborVal is 0, connect returns false unless activate is true.
        // So again, only the empty subset (not connecting to anything) succeeds.
        Iterable<Frontier4> possibleF = f.getPossibleConnections(Frontier4.Direction.LEFT, Frontier4.Direction.UP);
        int countF = 0;
        for (Frontier4 state : possibleF) {
            countF++;
        }
        assertEquals(1, countF);
    }
}
