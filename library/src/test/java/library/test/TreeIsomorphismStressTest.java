package library.test;

import library.util.graph.tree.Tree;
import java.util.Arrays;

public class TreeIsomorphismStressTest {
    public static void main(String[] args) {
        testSmall();
    }

    static void testSmall() {
        // Simple case: two paths
        Tree t1 = new Tree(3);
        t1.addEdge(0, 1);
        t1.addEdge(1, 2);
        t1.rooted(0);
        
        int[] iso1 = t1.isomorphismClassificationOnlyBranches();
        System.out.println("t1 subtrees iso classes: " + Arrays.toString(iso1));
        
        Tree t2 = new Tree(3);
        t2.addEdge(0, 2);
        t2.addEdge(2, 1);
        t2.rooted(0);
        
        int[] iso2 = t2.isomorphismClassificationOnlyBranches();
        System.out.println("t2 subtrees iso classes: " + Arrays.toString(iso2));

        Tree t3 = new Tree(7);
        t3.addEdge(0, 1);
        t3.addEdge(0, 2);
        t3.addEdge(1, 3);
        t3.addEdge(1, 4);
        t3.addEdge(2, 5);
        t3.addEdge(2, 6);
        t3.rooted(0);
        
        int[] iso3 = t3.isomorphismClassificationOnlyBranches();
        System.out.println("t3 subtrees iso classes: " + Arrays.toString(iso3));
        
        if (iso3[1] != iso3[2]) throw new RuntimeException("Subtree 1 and 2 should be isomorphic");
        if (iso3[3] != iso3[4] || iso3[3] != iso3[5] || iso3[3] != iso3[6]) throw new RuntimeException("All leaves should be isomorphic");
        if (iso3[0] == iso3[1]) throw new RuntimeException("Root and children should not be isomorphic");
        
        System.out.println("Small test passed!");
    }
}
