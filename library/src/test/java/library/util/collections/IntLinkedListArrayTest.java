package library.util.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class IntLinkedListArrayTest {

    @Test
    public void testSignedDist() {
        IntLinkedListArray lla = new IntLinkedListArray(10);

        // 0 -> 1 -> 2 -> 3
        lla.addEdge(0, 1);
        lla.addEdge(1, 2);
        lla.addEdge(2, 3);

        // 5 -> 6
        lla.addEdge(5, 6);

        // x = y
        assertEquals(0, lla.signedDist(0, 0));
        assertEquals(0, lla.signedDist(2, 2));

        // y is reachable from x
        assertEquals(1, lla.signedDist(0, 1));
        assertEquals(2, lla.signedDist(0, 2));
        assertEquals(3, lla.signedDist(0, 3));
        assertEquals(1, lla.signedDist(1, 2));
        assertEquals(2, lla.signedDist(1, 3));
        assertEquals(1, lla.signedDist(2, 3));

        // x is reachable from y
        assertEquals(-1, lla.signedDist(1, 0));
        assertEquals(-2, lla.signedDist(2, 0));
        assertEquals(-3, lla.signedDist(3, 0));
        assertEquals(-1, lla.signedDist(2, 1));
        assertEquals(-2, lla.signedDist(3, 1));
        assertEquals(-1, lla.signedDist(3, 2));

        // unreachable
        assertEquals(Integer.MAX_VALUE, lla.signedDist(0, 5));
        assertEquals(Integer.MAX_VALUE, lla.signedDist(5, 0));
        assertEquals(Integer.MAX_VALUE, lla.signedDist(0, 4));
        assertEquals(Integer.MAX_VALUE, lla.signedDist(4, 0));

        assertEquals(1, lla.signedDist(5, 6));
        assertEquals(-1, lla.signedDist(6, 5));
    }

    @Test
    public void testSignedDistLarge() {
        int n = 1000;
        IntLinkedListArray lla = new IntLinkedListArray(n);
        for (int i = 0; i < n - 1; i++) {
            lla.addEdge(i, i + 1);
        }

        assertEquals(n - 1, lla.signedDist(0, n - 1));
        assertEquals(-(n - 1), lla.signedDist(n - 1, 0));
        assertEquals(500, lla.signedDist(200, 700));
        assertEquals(-500, lla.signedDist(700, 200));
    }
}
