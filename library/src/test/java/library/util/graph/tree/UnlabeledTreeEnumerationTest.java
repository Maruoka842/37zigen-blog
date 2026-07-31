package library.util.graph.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class UnlabeledTreeEnumerationTest {

    @Test
    public void testRootedCounts() {
        int maxN = 20;
        UnlabeledTreeEnumeration enu = UnlabeledTreeEnumeration.rooted(maxN);
        for (int n = 1; n <= maxN; n++) {
            assertEquals((int)UnlabeledTreeEnumeration.UNLABELED_ROOTED[n], enu.groupSize(n), "Rooted counts mismatch at n=" + n);
        }
    }

    @Test
    public void testUnrootedCounts() {
        int maxN = 20;
        UnlabeledTreeEnumeration enu = UnlabeledTreeEnumeration.unrooted(maxN);
        for (int n = 1; n <= maxN; n++) {
            int numUnrooted = 0;
            for (int x = 0; x < enu.groupSize(n); x++) {
                if (enu.isCentroid(n, x)) {
                    numUnrooted++;
                }
            }
            assertEquals((int)UnlabeledTreeEnumeration.UNLABELED_UNROOTED[n], numUnrooted, "Unrooted counts mismatch at n=" + n);
        }
    }

    @Test
    public void testParentArray() {
        UnlabeledTreeEnumeration enu = UnlabeledTreeEnumeration.rooted(4);

        // n=1
        assertArrayEquals(new int[]{-1}, enu.getParentArray(1, 0));

        // n=2
        assertArrayEquals(new int[]{-1, 0}, enu.getParentArray(2, 0));

        // n=3
        // T[3] has 2 elements
        // (1,0), (2,0) -> 0 -> (1,0) -> 0 -> root
        // (1,0), (1,0) -> 0 -> 0 -> root
        assertArrayEquals(new int[]{-1, 0, 0}, enu.getParentArray(3, 0));
        assertArrayEquals(new int[]{-1, 0, 1}, enu.getParentArray(3, 1));

        // n=4
        assertArrayEquals(new int[]{-1, 0, 0, 0}, enu.getParentArray(4, 0));
        assertArrayEquals(new int[]{-1, 0, 1, 0}, enu.getParentArray(4, 1));
        assertArrayEquals(new int[]{-1, 0, 1, 1}, enu.getParentArray(4, 2));
        assertArrayEquals(new int[]{-1, 0, 1, 2}, enu.getParentArray(4, 3));
    }

    @Test
    public void testGetTree() {
        UnlabeledTreeEnumeration enu = UnlabeledTreeEnumeration.rooted(4);
        Tree t = enu.getTree(4, 3);
        assertEquals(4, t.N);
        // parent array [-1, 0, 1, 2] means 3-2-1-0 path
        // edge (1,0), (2,1), (3,2)
        assertTrue(t.areIsomorphic(t, t));
    }
}
