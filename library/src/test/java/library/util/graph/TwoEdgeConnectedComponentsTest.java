package library.util.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TwoEdgeConnectedComponentsTest {
    @Test
    public void testSimple() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        TwoEdgeConnectedComponents ecc = new TwoEdgeConnectedComponents(g);
        assertEquals(1, ecc.getComponentCount());
        assertEquals(0, ecc.getComponentId(0));
        assertEquals(0, ecc.getComponentId(1));
        assertEquals(0, ecc.getComponentId(2));
    }

    @Test
    public void testWithBridge() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        g.addEdge(2, 3);
        TwoEdgeConnectedComponents ecc = new TwoEdgeConnectedComponents(g);
        assertEquals(2, ecc.getComponentCount());
        assertEquals(ecc.getComponentId(0), ecc.getComponentId(1));
        assertEquals(ecc.getComponentId(1), ecc.getComponentId(2));
        assertNotEquals(ecc.getComponentId(2), ecc.getComponentId(3));
        assertEquals(1, ecc.getBridges().size());
    }

    @Test
    public void testDisconnected() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(2, 3);
        TwoEdgeConnectedComponents ecc = new TwoEdgeConnectedComponents(g);
        assertEquals(4, ecc.getComponentCount());
        assertEquals(2, ecc.getBridges().size());
    }

    @Test
    public void testBridgeBlockTree() {
        Graph g = new Graph(6);
        // Component 1: 0-1-2-0
        g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0);
        // Bridge: 2-3
        g.addEdge(2, 3);
        // Component 2: 3-4-5-3
        g.addEdge(3, 4); g.addEdge(4, 5); g.addEdge(5, 3);

        TwoEdgeConnectedComponents ecc = new TwoEdgeConnectedComponents(g);
        assertEquals(2, ecc.getComponentCount());
        Graph tree = ecc.bridgeBlockTree();
        assertEquals(2, tree.N);
        assertEquals(1, tree.M);
    }
}
