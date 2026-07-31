package library.util.graph.tree;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CosizeTest {
    @Test
    public void testTreeCosize() {
        Tree tree = new Tree(3);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.rooted(0);
        // size(0)=3, size(1)=2, size(2)=1
        // New Tree.cosize(v) = N - size(v)
        // cosize(0) = 3 - 3 = 0
        // cosize(1) = 3 - 2 = 1
        // cosize(2) = 3 - 1 = 2
        assertEquals(0, tree.cosize(0));
        assertEquals(1, tree.cosize(1));
        assertEquals(2, tree.cosize(2));
    }

    @Test
    public void testLongValueTreeCosize() {
        LongValueTree tree = new LongValueTree(3);
        tree.addEdge(0, 1, 1);
        tree.addEdge(1, 2, 1);
        tree.rooted(0);
        // size(0)=3, size(1)=2, size(2)=1
        // LongValueForest.cosize(v) = N - size(v)
        // cosize(0) = 3 - 3 = 0
        // cosize(1) = 3 - 2 = 1
        // cosize(2) = 3 - 1 = 2
        assertEquals(0, tree.cosize(0));
        assertEquals(1, tree.cosize(1));
        assertEquals(2, tree.cosize(2));
    }
}
