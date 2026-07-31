package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import library.util.graph.Graph;
import org.junit.jupiter.api.Test;

public class GraphRemoveEdgeTest {

    @Test
    public void testRemoveSimpleEdge() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.removeEdge(0, 1);
        assertEquals(1, g.M);
        assertEquals(0, g.adj[0].size());
        assertEquals(1, g.adj[1].size());
        assertEquals(2, g.adj[1].get(0));
        assertEquals(1, g.adj[2].size());
        assertEquals(1, g.adj[2].get(0));
    }

    @Test
    public void testRemoveOnlyOneOfMultipleEdges() {
        Graph g = new Graph(2);
        g.addEdge(0, 1);
        g.addEdge(0, 1);
        g.removeEdge(0, 1);
        assertEquals(1, g.M);
        assertEquals(1, g.adj[0].size());
        assertEquals(1, g.adj[0].get(0));
        assertEquals(1, g.adj[1].size());
        assertEquals(0, g.adj[1].get(0));
    }

    @Test
    public void testRemoveLoop() {
        Graph g = new Graph(1);
        g.addEdge(0, 0);
        g.removeEdge(0, 0);
        assertEquals(0, g.M);
        assertEquals(0, g.adj[0].size());
    }

    @Test
    public void testRemoveMissingEdgeThrowsAssertionError() {
        Graph g = new Graph(2);
        assertThrows(AssertionError.class, () -> g.removeEdge(0, 1));
    }
}
