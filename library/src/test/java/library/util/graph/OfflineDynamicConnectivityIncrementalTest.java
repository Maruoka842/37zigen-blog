package library.util.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import library.util.unionfind.UndoUnionFind;

public class OfflineDynamicConnectivityIncrementalTest {

    @Test
    public void testIncremental() {
        int n = 3;
        OfflineDynamicConnectivity odc = new OfflineDynamicConnectivity(n);

        // Time 1: add (0, 1)
        // Time 2: add (1, 2)
        // Time 3: remove (0, 1)
        // Time 4: remove (1, 2)
        odc.addEdge(0, 1, 1, 3);
        odc.addEdge(1, 2, 2, 4);

        for (int i = 0; i < 5; i++) odc.registerQueryTime(i);

        odc.build();
        UndoUnionFind uf = odc.getUnionFind();

        // Time 0
        odc.advanceTo(0);
        assertEquals(3, uf.numberConnectedComponents());
        assertFalse(uf.equiv(0, 1));

        // Time 1
        odc.advanceTo(1);
        assertEquals(2, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));
        assertFalse(uf.equiv(0, 2));

        // Time 2
        odc.advanceTo(2);
        assertEquals(1, uf.numberConnectedComponents());
        assertTrue(uf.equiv(0, 1));
        assertTrue(uf.equiv(0, 2));

        // Time 3
        odc.advanceTo(3);
        assertEquals(2, uf.numberConnectedComponents());
        assertFalse(uf.equiv(0, 1));
        assertTrue(uf.equiv(1, 2));

        // Time 4
        odc.advanceTo(4);
        assertEquals(3, uf.numberConnectedComponents());
        assertFalse(uf.equiv(1, 2));
    }

    @Test
    public void testNonDecreasingTime() {
        OfflineDynamicConnectivity odc = new OfflineDynamicConnectivity(2);
        odc.registerQueryTime(1);
        odc.registerQueryTime(2);
        odc.build();
        odc.advanceTo(1);
        assertThrows(IllegalArgumentException.class, () -> odc.advanceTo(0));
    }

    @Test
    public void testIllegalState() {
        OfflineDynamicConnectivity odc = new OfflineDynamicConnectivity(2);
        assertThrows(IllegalStateException.class, () -> odc.advanceTo(0));
    }
}
