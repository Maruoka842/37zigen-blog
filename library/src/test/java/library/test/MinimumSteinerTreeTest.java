package library.test;

import java.util.List;
import library.util.graph.Edge;
import library.util.graph.LongValueGraph;
import library.util.graph.MinimumSteinerTree;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinimumSteinerTreeTest {

    @Test
    public void testSmallGraph() {
        // Simple graph: 0-1 (10), 1-2 (10), 0-2 (15)
        // Terminals: 0, 1, 2
        // MST should be 0-1 and 1-2 or 0-2 and something...
        // 0-1 (10), 1-2 (10) -> total 20
        // 0-2 (15), 0-1 (10) -> total 25
        // 0-2 (15), 1-2 (10) -> total 25
        // Steiner tree for {0, 1, 2} is MST.
        LongValueGraph g = new LongValueGraph(3);
        g.addEdge(0, 1, 10);
        g.addEdge(1, 2, 10);
        g.addEdge(0, 2, 15);
        int[] terminals = {0, 1, 2};

        MinimumSteinerTree.Result resDP = MinimumSteinerTree.solveDP(g, terminals);
        assertEquals(20, resDP.cost());
        assertEquals(2, resDP.edges().size());

        MinimumSteinerTree.Result resDense = MinimumSteinerTree.solveDense(g, terminals);
        assertEquals(20, resDense.cost());
        assertEquals(2, resDense.edges().size());
    }

    @Test
    public void testSteinerPoint() {
        // Graph with a Steiner point
        // 0-3 (10), 1-3 (10), 2-3 (10), 0-1 (30), 1-2 (30), 2-0 (30)
        // Terminals: 0, 1, 2. Steiner point: 3
        // Steiner tree should use vertex 3: 0-3, 1-3, 2-3 -> total 30
        LongValueGraph g = new LongValueGraph(4);
        g.addEdge(0, 3, 10);
        g.addEdge(1, 3, 10);
        g.addEdge(2, 3, 10);
        g.addEdge(0, 1, 30);
        g.addEdge(1, 2, 30);
        g.addEdge(2, 0, 30);
        int[] terminals = {0, 1, 2};

        MinimumSteinerTree.Result resDP = MinimumSteinerTree.solveDP(g, terminals);
        assertEquals(30, resDP.cost());

        MinimumSteinerTree.Result resDense = MinimumSteinerTree.solveDense(g, terminals);
        assertEquals(30, resDense.cost());

        MinimumSteinerTree.Result res = MinimumSteinerTree.solve(g, terminals);
        assertEquals(30, res.cost());
    }

    @Test
    public void testSingleTerminal() {
        LongValueGraph g = new LongValueGraph(3);
        g.addEdge(0, 1, 10);
        int[] terminals = {0};
        MinimumSteinerTree.Result res = MinimumSteinerTree.solve(g, terminals);
        assertEquals(0, res.cost());
        assertTrue(res.edges().isEmpty());
    }

    @Test
    public void testDisconnected() {
        LongValueGraph g = new LongValueGraph(3);
        g.addEdge(0, 1, 10);
        int[] terminals = {0, 2};
        MinimumSteinerTree.Result res = MinimumSteinerTree.solve(g, terminals);
        assertTrue(res.cost() >= Long.MAX_VALUE / 3);
    }
}
