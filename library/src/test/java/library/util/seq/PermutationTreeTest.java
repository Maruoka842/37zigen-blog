package library.util.seq;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PermutationTreeTest {

    @Test
    public void testIdentity() {
        int[] P = {0, 1, 2};
        PermutationTree pt = new PermutationTree(P);
        PermutationTree.Node root = pt.nodes.get(pt.root);
        assertEquals(PermutationTree.NodeType.JoinAsc, root.tp);
        assertEquals(0, root.L);
        assertEquals(3, root.R);
        assertEquals(3, root.child.size());
    }

    @Test
    public void testReverse() {
        int[] P = {2, 1, 0};
        PermutationTree pt = new PermutationTree(P);
        PermutationTree.Node root = pt.nodes.get(pt.root);
        assertEquals(PermutationTree.NodeType.JoinDesc, root.tp);
        assertEquals(0, root.L);
        assertEquals(3, root.R);
        assertEquals(3, root.child.size());
    }

    @Test
    public void testCut() {
        int[] P = {1, 3, 0, 2};
        PermutationTree pt = new PermutationTree(P);
        PermutationTree.Node root = pt.nodes.get(pt.root);
        assertEquals(PermutationTree.NodeType.Cut, root.tp);
        assertEquals(0, root.L);
        assertEquals(4, root.R);
        assertEquals(4, root.child.size());
    }

    @Test
    public void testMixed() {
        int[] P = {0, 2, 4, 1, 3};
        PermutationTree pt = new PermutationTree(P);
        PermutationTree.Node root = pt.nodes.get(pt.root);
        // [0, 2, 4, 1, 3]
        // 0 is Leaf, [2, 4, 1, 3] is Cut? No.
        // [2, 4, 1, 3] values are {1, 2, 3, 4}, so it's a common interval.
        // Actually {0, 2, 4, 1, 3} is [0, 5) values {0, 1, 2, 3, 4}.
        // 0 is [0, 1) values {0}.
        // [2, 4, 1, 3] is [1, 5) values {1, 2, 3, 4}.
        // So root should be JoinAsc with children [0, 1) and [1, 5).
        assertEquals(PermutationTree.NodeType.JoinAsc, root.tp);
        assertEquals(2, root.child.size());

        PermutationTree.Node child1 = pt.nodes.get(root.child.get(0));
        assertEquals(PermutationTree.NodeType.Leaf, child1.tp);
        assertEquals(0, child1.mini);

        PermutationTree.Node child2 = pt.nodes.get(root.child.get(1));
        assertEquals(PermutationTree.NodeType.Cut, child2.tp);
        assertEquals(4, child2.child.size());
    }

    @Test
    public void testSingle() {
        int[] P = {0};
        PermutationTree pt = new PermutationTree(P);
        PermutationTree.Node root = pt.nodes.get(pt.root);
        assertEquals(PermutationTree.NodeType.Leaf, root.tp);
        assertEquals(0, root.L);
        assertEquals(1, root.R);
    }
}
