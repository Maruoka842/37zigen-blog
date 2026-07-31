package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import library.util.game.ImpartialGames;
import library.util.graph.Graph;

public class GreenHackenbushTreeTest {

    @Test
    public void testSimpleTree() {
        // 0 - 1 (ground)
        Graph g = new Graph(2);
        g.addEdge(0, 1);
        int root = 1;
        ImpartialGames.GreenHackenbushTree game = new ImpartialGames.GreenHackenbushTree();
        ImpartialGames.GreenHackenbushTree.State state = new ImpartialGames.GreenHackenbushTree.State(g, root);

        assertEquals(1, game.grundy(state));

        // nextStates should have one state: empty graph
        Iterable<ImpartialGames.GreenHackenbushTree.State> nexts = game.nextStates(state);
        int count = 0;
        for (ImpartialGames.GreenHackenbushTree.State next : nexts) {
            count++;
            assertEquals(0, next.tree().M);
            assertEquals(0, game.grundy(next));
        }
        assertEquals(1, count);
    }

    @Test
    public void testPath() {
        // 2 - 1 - 0 (ground)
        Graph g = new Graph(3);
        g.addEdge(2, 1);
        g.addEdge(1, 0);
        int root = 0;
        ImpartialGames.GreenHackenbushTree game = new ImpartialGames.GreenHackenbushTree();
        ImpartialGames.GreenHackenbushTree.State state = new ImpartialGames.GreenHackenbushTree.State(g, root);

        // grundy(2) = 0
        // g(1) = g(2)+1 = 1
        // g(0) = g(1)+1 = 2
        assertEquals(2, game.grundy(state));

        Iterable<ImpartialGames.GreenHackenbushTree.State> nexts = game.nextStates(state);
        List<Long> grundies = new ArrayList<>();
        for (ImpartialGames.GreenHackenbushTree.State next : nexts) {
            grundies.add(game.grundy(next));
        }
        Collections.sort(grundies);
        // Cuts:
        // Cut (1,2) -> 1-0 remains. Grundy 1.
        // Cut (0,1) -> Nothing remains. Grundy 0.
        assertEquals(Arrays.asList(0L, 1L), grundies);
    }

    @Test
    public void testStar() {
        // 0 (ground) - 1, 0 - 2, 0 - 3
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 3);
        int root = 0;
        ImpartialGames.GreenHackenbushTree game = new ImpartialGames.GreenHackenbushTree();
        ImpartialGames.GreenHackenbushTree.State state = new ImpartialGames.GreenHackenbushTree.State(g, root);

        // g(1)=0, g(2)=0, g(3)=0
        // g(0) = (0+1) ^ (0+1) ^ (0+1) = 1 ^ 1 ^ 1 = 1
        assertEquals(1, game.grundy(state));

        Iterable<ImpartialGames.GreenHackenbushTree.State> nexts = game.nextStates(state);
        List<Long> grundies = new ArrayList<>();
        for (ImpartialGames.GreenHackenbushTree.State next : nexts) {
            grundies.add(game.grundy(next));
        }
        Collections.sort(grundies);
        // Any cut leaves 2 edges from ground. Grundy 1 ^ 1 = 0.
        assertEquals(Arrays.asList(0L, 0L, 0L), grundies);
    }

    @Test
    public void testYGraph() {
        // 0 (ground) - 1, 1 - 2, 1 - 3
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        int root = 0;
        ImpartialGames.GreenHackenbushTree game = new ImpartialGames.GreenHackenbushTree();
        ImpartialGames.GreenHackenbushTree.State state = new ImpartialGames.GreenHackenbushTree.State(g, root);

        // grundy(2) = 0, grundy(3) = 0
        // g(1) = (0+1) ^ (0+1) = 0
        // g(0) = 0+1 = 1
        assertEquals(1, game.grundy(state));

        Iterable<ImpartialGames.GreenHackenbushTree.State> nexts = game.nextStates(state);
        List<Long> grundies = new ArrayList<>();
        for (ImpartialGames.GreenHackenbushTree.State next : nexts) {
            grundies.add(game.grundy(next));
        }
        Collections.sort(grundies);
        // Cuts:
        // Cut (1,2) -> 0-1-3 remains. Grundy 2.
        // Cut (1,3) -> 0-1-2 remains. Grundy 2.
        // Cut (0,1) -> nothing remains. Grundy 0.
        assertEquals(Arrays.asList(0L, 2L, 2L), grundies);
    }
}
