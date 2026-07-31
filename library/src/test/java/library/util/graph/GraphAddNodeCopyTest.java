package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphAddNodeCopyTest {

    @Test
    public void testGraph() {
        Graph g = new Graph(2);
        g.addEdge(0, 1);
        int id = g.addNode();
        assertEquals(2, id);
        assertEquals(3, g.N);
        g.addEdge(0, 2);
        assertEquals(2, g.M);

        Graph copy = g.copy();
        assertEquals(g.N, copy.N);
        assertEquals(g.M, copy.M);

        copy.addEdge(1, 2);
        assertEquals(3, copy.M);
        assertEquals(2, g.M);

        boolean foundInCopy = false;
        for (int i = 0; i < copy.adj[1].size(); i++) {
            if (copy.adj[1].get(i) == 2) foundInCopy = true;
        }
        assertTrue(foundInCopy);

        for (int i = 0; i < g.adj[1].size(); i++) {
            assertNotEquals(2, g.adj[1].get(i));
        }
    }

    @Test
    public void testDigraph() {
        Digraph g = new Digraph(2);
        g.addEdge(0, 1);
        int id = g.addNode();
        assertEquals(2, id);
        assertEquals(3, g.N);
        g.addEdge(0, 2);
        assertEquals(2, g.M);

        Digraph copy = g.copy();
        assertEquals(g.N, copy.N);
        assertEquals(g.M, copy.M);

        copy.addEdge(1, 2);
        assertEquals(3, copy.M);
        assertEquals(2, g.M);

        boolean found = false;
        for (int i = 0; i < copy.adj[1].size(); i++) if (copy.adj[1].get(i) == 2) found = true;
        assertTrue(found);

        for (int i = 0; i < g.adj[1].size(); i++) {
            assertNotEquals(2, g.adj[1].get(i));
        }
    }

    @Test
    public void testLongValueGraph() {
        LongValueGraph g = new LongValueGraph(2);
        g.addEdge(0, 1, 10);
        int id = g.addNode();
        assertEquals(2, id);
        assertEquals(3, g.N);
        g.addEdge(0, 2, 20);
        assertEquals(2, g.M);

        LongValueGraph copy = g.copy();
        assertEquals(g.N, copy.N);
        assertEquals(g.M, copy.M);

        copy.addEdge(1, 2, 30);
        assertEquals(3, copy.M);
        assertEquals(2, g.M);

        boolean found = false;
        for (Edge e : copy.adj[1]) if (e.dst == 2) found = true;
        assertTrue(found);

        for (Edge e : g.adj[1]) {
            assertNotEquals(2, e.dst);
        }
    }

    @Test
    public void testLongValueDigraph() {
        LongValueDigraph g = new LongValueDigraph(2);
        g.addEdge(0, 1, 10);
        int id = g.addNode();
        assertEquals(2, id);
        assertEquals(3, g.N);
        g.addEdge(0, 2, 20);
        assertEquals(2, g.M);

        LongValueDigraph copy = g.copy();
        assertEquals(g.N, copy.N);
        assertEquals(g.M, copy.M);

        copy.addEdge(1, 2, 30);
        assertEquals(3, copy.M);
        assertEquals(2, g.M);

        boolean found = false;
        for (Edge e : copy.adj[1]) if (e.dst == 2) found = true;
        assertTrue(found);

        for (Edge e : g.adj[1]) {
            assertNotEquals(2, e.dst);
        }
    }

    @Test
    public void testAddNodeFromZero() {
        Graph g = new Graph(0);
        assertEquals(0, g.addNode());
        assertEquals(1, g.N);
        g.addEdge(0, 0);
        assertEquals(1, g.M);

        Digraph dg = new Digraph(0);
        assertEquals(0, dg.addNode());
        assertEquals(1, dg.N);
        dg.addEdge(0, 0);
        assertEquals(1, dg.M);

        LongValueGraph lvg = new LongValueGraph(0);
        assertEquals(0, lvg.addNode());
        assertEquals(1, lvg.N);
        lvg.addEdge(0, 0, 10);
        assertEquals(1, lvg.M);

        LongValueDigraph lvdg = new LongValueDigraph(0);
        assertEquals(0, lvdg.addNode());
        assertEquals(1, lvdg.N);
        lvdg.addEdge(0, 0, 10);
        assertEquals(1, lvdg.M);
    }
}
