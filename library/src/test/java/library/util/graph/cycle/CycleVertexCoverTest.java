package library.util.graph.cycle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CycleVertexCoverTest {

    @Test
    public void testCount() {
        long mod = 1_000_000_007L;
        assertEquals(1, CountCycleVertexCover.count(1, mod));
        assertEquals(3, CountCycleVertexCover.count(2, mod));
        assertEquals(4, CountCycleVertexCover.count(3, mod));
        assertEquals(7, CountCycleVertexCover.count(4, mod));
        assertEquals(11, CountCycleVertexCover.count(5, mod));
        assertEquals(18, CountCycleVertexCover.count(6, mod));
    }

}
