package library.util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StoerWagnerTest {

    @Test
    public void testSimpleGraph() {
        LongValueGraph g = new LongValueGraph(4);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 4);
        g.addEdge(3, 0, 5);
        // Cut (0,1), (2,3) -> 2 + 4 = 6
        // Cut (0,3), (1,2) -> 5 + 3 = 8
        // Cut (0,1), (0,3) -> 2 + 5 = 7
        // Cut (1,0), (1,2) -> 2 + 3 = 5
        // Cut (2,1), (2,3) -> 3 + 4 = 7
        // Cut (3,2), (3,0) -> 4 + 5 = 9
        assertEquals(5, StoerWagner.minCutValue(g));
    }

    @Test
    public void testDisconnectedGraph() {
        LongValueGraph g = new LongValueGraph(4);
        g.addEdge(0, 1, 10);
        g.addEdge(2, 3, 10);
        assertEquals(0, StoerWagner.minCutValue(g));
    }

    @Test
    public void testCompleteGraph() {
        LongValueGraph g = new LongValueGraph(3);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        assertEquals(2, StoerWagner.minCutValue(g));
    }

    @Test
    public void testLargerGraph() {
        // Example from some resource
        LongValueGraph g = new LongValueGraph(8);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 4, 3);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 4, 2);
        g.addEdge(1, 5, 2);
        g.addEdge(2, 3, 4);
        g.addEdge(2, 6, 2);
        g.addEdge(3, 6, 2);
        g.addEdge(3, 7, 2);
        g.addEdge(4, 5, 3);
        g.addEdge(5, 6, 1);
        g.addEdge(6, 7, 3);

        assertEquals(4, StoerWagner.minCutValue(g));
    }

    @Test
    public void testRestoration() {
        LongValueGraph g = new LongValueGraph(8);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 4, 3);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 4, 2);
        g.addEdge(1, 5, 2);
        g.addEdge(2, 3, 4);
        g.addEdge(2, 6, 2);
        g.addEdge(3, 6, 2);
        g.addEdge(3, 7, 2);
        g.addEdge(4, 5, 3);
        g.addEdge(5, 6, 1);
        g.addEdge(6, 7, 3);

        StoerWagner.Result res = StoerWagner.minCut(g);
        assertEquals(4, res.value());

        long actualCut = 0;
        for (int i = 0; i < g.N; i++) {
            for (Edge e : g.adj[i]) {
                if (res.side()[i] && !res.side()[e.dst]) {
                    actualCut += e.cost;
                }
            }
        }
        assertEquals(res.value(), actualCut);

        boolean hasTrue = false;
        boolean hasFalse = false;
        for (boolean b : res.side()) {
            if (b) hasTrue = true;
            else hasFalse = true;
        }
        assertTrue(hasTrue);
        assertTrue(hasFalse);
    }
}
