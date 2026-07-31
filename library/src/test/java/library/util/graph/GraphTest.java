package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GraphTest {

    @Test
    public void testCopy() {
        Graph g1 = new Graph(3);
        g1.addEdge(0, 1);
        g1.addEdge(1, 2);

        Graph g2 = g1.copy();

        // Check if basic properties are the same
        assertEquals(g1.N, g2.N);
        assertEquals(g1.M, g2.M);

        // Check if adjacency lists are the same
        for (int i = 0; i < g1.N; i++) {
            assertEquals(g1.adj[i].size(), g2.adj[i].size());
            for (int j = 0; j < g1.adj[i].size(); j++) {
                assertEquals(g1.adj[i].get(j), g2.adj[i].get(j));
            }
            // Ensure they are not the same object
            assertNotSame(g1.adj[i], g2.adj[i]);
        }

        // Modify g1 and check if g2 is unchanged
        g1.addEdge(0, 2);
        assertEquals(3, g1.M);
        assertEquals(2, g2.M);
        assertEquals(2, g1.adj[0].size());
        assertEquals(1, g2.adj[0].size());
        assertEquals(2, g1.adj[2].size());
        assertEquals(1, g2.adj[2].size());

        // Modify g2 and check if g1 is unchanged
        g2.addEdge(2, 2); // self loop
        assertEquals(3, g2.M);
        assertEquals(3, g1.M);
        assertEquals(2, g2.adj[2].size()); // originally 1 (from 1-2 edge), now 2 (from 2-2 loop)
        assertEquals(2, g1.adj[2].size()); // g1 was 2 from g1.addEdge(0, 2)
    }

    @Test
    public void testCopyEmpty() {
        Graph g1 = new Graph(0);
        Graph g2 = g1.copy();
        assertEquals(0, g2.N);
        assertEquals(0, g2.M);
    }

    @Test
    public void testCopyNoEdges() {
        Graph g1 = new Graph(5);
        Graph g2 = g1.copy();
        assertEquals(5, g2.N);
        assertEquals(0, g2.M);
        for(int i=0; i<5; i++) {
            assertEquals(0, g2.adj[i].size());
        }
    }
}
