package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import library.util.game.ImpartialGames;
import library.util.graph.Graph;

public class GreenHackenbushTest {

    @Test
    public void testSimpleTree() {
        // 0 - 1 (ground)
        Graph g = new Graph(2);
        g.addEdge(0, 1);
        int[] ground = {1};
        ImpartialGames.GreenHackenbush game = new ImpartialGames.GreenHackenbush();
        ImpartialGames.GreenHackenbush.State state = new ImpartialGames.GreenHackenbush.State(g, ground);

        assertEquals(1, game.grundy(state));

        // nextStates should have one state: empty graph
        Iterable<ImpartialGames.GreenHackenbush.State> nexts = game.nextStates(state);
        int count = 0;
        for (ImpartialGames.GreenHackenbush.State next : nexts) {
            count++;
            assertEquals(0, next.graph().M);
            assertEquals(0, game.grundy(next));
        }
        assertEquals(1, count);
    }

    @Test
    public void testCycle() {
        // 0 - 1 - 2 - 0, ground at 0
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        int[] ground = {0};
        ImpartialGames.GreenHackenbush game = new ImpartialGames.GreenHackenbush();
        ImpartialGames.GreenHackenbush.State state = new ImpartialGames.GreenHackenbush.State(g, ground);

        // Cycle 0-1-2-0 should be fused into 0.
        // But Fusion Principle says a cycle of edges is equivalent to a single node.
        // Actually, the Sprague-Grundy value of a cycle of edges is 0 if we think of it as a single node with no edges.
        // Wait, Green Hackenbush on graphs:
        // Fusion Principle: any circuit may be replaced by a single node without changing the Grundy-value of the graph.
        // If 0-1-2-0 is a cycle and 0 is ground, fusing 1 and 2 into 0 means we have no edges left.
        // So grundy should be 0.
        // Correction: the fused odd cycle leaves one loop-equivalent move, so grundy should be 1.
        assertEquals(1, game.grundy(state));

        // nextStates:
        // Cut (0,1): Remaining 0-2 (ground 0), (1,2) is removed because 1,2 not reachable from 0.
        // Cut (1,2): Remaining 0-1 and 0-2 (ground 0).
        // Cut (2,0): Remaining 0-1 (ground 0), (1,2) is removed because 2 not reachable from 0.

        Iterable<ImpartialGames.GreenHackenbush.State> nexts = game.nextStates(state);
        List<Long> grundies = new ArrayList<>();
        for (ImpartialGames.GreenHackenbush.State next : nexts) {
            grundies.add(game.grundy(next));
        }
        Collections.sort(grundies);
        // Expect cuts to lead to:
        // Cut (0,1) -> Path 0-2-1 remains. Grundy 2.
        // Cut (1,2) -> Edges (0,1), (0,2) remain. Both are edges from ground. Grundy 1 ^ 1 = 0.
        // Cut (2,0) -> Path 0-1-2 remains. Grundy 2.
        assertEquals(Arrays.asList(0L, 2L, 2L), grundies);
    }

    @Test
    public void testEvenCycle() {
        // 0 - 1 - 2 - 3 - 0, ground at 0
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 0);
        int[] ground = {0};
        ImpartialGames.GreenHackenbush game = new ImpartialGames.GreenHackenbush();
        ImpartialGames.GreenHackenbush.State state = new ImpartialGames.GreenHackenbush.State(g, ground);

        assertEquals(0, game.grundy(state));
    }

    @Test
    public void testYGraph() {
        // 0 (ground) - 1, 1 - 2, 1 - 3
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        int[] ground = {0};
        ImpartialGames.GreenHackenbush game = new ImpartialGames.GreenHackenbush();
        ImpartialGames.GreenHackenbush.State state = new ImpartialGames.GreenHackenbush.State(g, ground);

        // grundy(2) = 0, grundy(3) = 0
        // g(1) = (g(2)+1) ^ (g(3)+1) = 1 ^ 1 = 0
        // g(0) = g(1)+1 = 1
        assertEquals(1, game.grundy(state));
    }
}
