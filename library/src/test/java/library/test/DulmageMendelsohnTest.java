package library.test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import library.util.graph.BipartiteMatching;
import library.util.graph.BipartiteMatching.DMGroup;
import org.junit.jupiter.api.Test;

public class DulmageMendelsohnTest {

    @Test
    public void testExample() {
        // (2, 2, [(0,0), (0,1), (1,0)])
        int L = 2, R = 2;
        BipartiteMatching bm = new BipartiteMatching(L, R);
        bm.addEdge(0, 0);
        bm.addEdge(0, 1);
        bm.addEdge(1, 0);
        bm.calc();

        List<DMGroup> groups = bm.dmDecomposition();

        // Expected: [([],[]),([0,],[1,]),([1,],[0,]),([],[]),]
        // W0 (R-heavy): empty
        // W1: (0, 1)
        // W2: (1, 0)
        // W(k+1) (L-heavy): empty

        assertEquals(4, groups.size());
        assertTrue(groups.get(0).left().isEmpty());
        assertTrue(groups.get(0).right().isEmpty());

        assertEquals(1, groups.get(1).left().size());
        assertEquals(0, groups.get(1).left().get(0));
        assertEquals(1, groups.get(1).right().size());
        assertEquals(1, groups.get(1).right().get(0));

        assertEquals(1, groups.get(2).left().size());
        assertEquals(1, groups.get(2).left().get(0));
        assertEquals(1, groups.get(2).right().size());
        assertEquals(0, groups.get(2).right().get(0));

        assertTrue(groups.get(3).left().isEmpty());
        assertTrue(groups.get(3).right().isEmpty());
    }

    @Test
    public void testWithUnmatched() {
        // L=2, R=2, edge=[(0,0)]
        // Matching: (0, 0)
        // Unmatched L: 1
        // Unmatched R: 1
        // G_DM: 0->0, 1->0
        // SCC: {1}, {0, 0}, {1}
        // W0: {R1} because R1 is unmatched
        // W(k+1): {L1} because L1 is unmatched
        // W1: {L0, R0}

        int L = 2, R = 2;
        BipartiteMatching bm = new BipartiteMatching(L, R);
        bm.addEdge(0, 0);
        bm.calc();

        List<DMGroup> groups = bm.dmDecomposition();

        assertEquals(3, groups.size());
        // W0: ([], [1])
        assertTrue(groups.get(0).left().isEmpty());
        assertEquals(1, groups.get(0).right().size());
        assertEquals(1, groups.get(0).right().get(0));

        // W1: ([0], [0])
        assertEquals(1, groups.get(1).left().size());
        assertEquals(0, groups.get(1).left().get(0));
        assertEquals(1, groups.get(1).right().size());
        assertEquals(0, groups.get(1).right().get(0));

        // W2: ([1], [])
        assertEquals(1, groups.get(2).left().size());
        assertEquals(1, groups.get(2).left().get(0));
        assertTrue(groups.get(2).right().isEmpty());
    }

    @Test
    public void testLongAlternatingPath() {
        // L=3, R=3
        // Edges: (0,0), (1,0), (1,1), (2,1)
        // Max Matching: (0,0), (1,1)
        // Matched: L0-R0, L1-R1
        // Unmatched: L2, R2
        // Alternating path from L2: L2 -> R1 -> L1 -> R0 -> L0
        // All {L0, L1, L2, R0, R1} should be in W(k+1)
        // R2 is unmatched and isolated, should be in W0

        int L = 3, R = 3;
        BipartiteMatching bm = new BipartiteMatching(L, R);
        bm.addEdge(0, 0);
        bm.addEdge(1, 0);
        bm.addEdge(1, 1);
        bm.addEdge(2, 1);
        bm.calc();

        List<DMGroup> groups = bm.dmDecomposition();

        // W0: ([], [2])
        // W(k+1): ([0,1,2], [0,1])

        assertEquals(2, groups.size());

        // W0
        assertTrue(groups.get(0).left().isEmpty());
        assertEquals(1, groups.get(0).right().size());
        assertEquals(2, groups.get(0).right().get(0));

        // W(k+1) (which is groups.get(1) here as k=0)
        assertEquals(3, groups.get(1).left().size());
        assertEquals(2, groups.get(1).right().size());
    }
}
