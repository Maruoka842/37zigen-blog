package library.util.graph.tree;

import library.util.algebra.strategy.group.LongAddGroupStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EulerTourPathTest {
    @Test
    public void testVertexPathSum() {
        // Star graph: 0 is root, neighbors are 1, 2, 3
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(0, 3);
        tree.rooted(0);

        EulerTourPath<Long> et = new EulerTourPath<>(tree, new LongAddGroupStrategy());
        et.setVertexValue(0, 10L);
        et.setVertexValue(1, 1L);
        et.setVertexValue(2, 2L);
        et.setVertexValue(3, 3L);

        // Paths from root
        assertEquals(11L, et.foldPathVertex(0, 1));
        assertEquals(12L, et.foldPathVertex(0, 2));
        assertEquals(13L, et.foldPathVertex(0, 3));

        // Paths between leaves
        assertEquals(13L, et.foldPathVertex(1, 2)); // 1-0-2: 1 + 10 + 2 = 13
        assertEquals(14L, et.foldPathVertex(1, 3)); // 1-0-3: 1 + 10 + 3 = 14
        assertEquals(15L, et.foldPathVertex(2, 3)); // 2-0-3: 2 + 10 + 3 = 15

        // Update
        et.updateVertexValue(0, 5L); // 10 -> 15
        assertEquals(16L, et.foldPathVertex(0, 1));
        assertEquals(18L, et.foldPathVertex(1, 2)); // 1 + 15 + 2 = 18
    }

    @Test
    public void testEdgePathSum() {
        // Path graph: 0-1-2-3, rooted at 0
        Tree tree = new Tree(4);
        tree.addEdge(0, 1);
        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.rooted(0);

        EulerTourPath<Long> et = new EulerTourPath<>(tree, new LongAddGroupStrategy());
        // Edges: (0,1)=100, (1,2)=20, (2,3)=3
        et.setEdgeValue(0, 1, 100L);
        et.setEdgeValue(1, 2, 20L);
        et.setEdgeValue(2, 3, 3L);

        assertEquals(100L, et.foldPathEdge(0, 1));
        assertEquals(120L, et.foldPathEdge(0, 2));
        assertEquals(123L, et.foldPathEdge(0, 3));
        assertEquals(23L, et.foldPathEdge(1, 3));
        assertEquals(20L, et.foldPathEdge(1, 2));

        // Update edge
        et.updateEdgeValue(1, 2, 5L); // 20 -> 25
        assertEquals(128L, et.foldPathEdge(0, 3));
    }

    @Test
    public void testGeneralTree() {
        // Tree:
        //     0
        //    / \
        //   1   2
        //  / \
        // 3   4
        Tree tree = new Tree(5);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(1, 3);
        tree.addEdge(1, 4);
        tree.rooted(0);

        EulerTourPath<Long> et = new EulerTourPath<>(tree, new LongAddGroupStrategy());
        for (int i = 0; i < 5; i++) {
            et.setVertexValue(i, (long) (i + 1));
        }
        // Values: 0:1, 1:2, 2:3, 3:4, 4:5

        assertEquals(1L + 2L + 4L, et.foldPathVertex(0, 3)); // 0-1-3
        assertEquals(4L + 2L + 5L, et.foldPathVertex(3, 4)); // 3-1-4
        assertEquals(4L + 2L + 1L + 3L, et.foldPathVertex(3, 2)); // 3-1-0-2

        // Edge values
        et = new EulerTourPath<>(tree, new LongAddGroupStrategy());
        et.setEdgeValue(0, 1, 10L);
        et.setEdgeValue(0, 2, 20L);
        et.setEdgeValue(1, 3, 30L);
        et.setEdgeValue(1, 4, 40L);

        // Edges are: e(0,1)=10, e(0,2)=20, e(1,3)=30, e(1,4)=40
        assertEquals(10L, et.foldPathEdge(0, 1));
        assertEquals(10L + 30L, et.foldPathEdge(0, 3));
        assertEquals(30L + 40L, et.foldPathEdge(3, 4));
        assertEquals(30L + 10L + 20L, et.foldPathEdge(3, 2));
    }
}
