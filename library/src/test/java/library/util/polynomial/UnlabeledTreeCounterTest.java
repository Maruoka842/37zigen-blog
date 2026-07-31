package library.util.polynomial;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class UnlabeledTreeCounterTest {

    @Test
    public void testCountRooted() {
        // OEIS A000081: 0, 1, 1, 2, 4, 9, 20, 48, 115, 286, 719
        long[] expected = {0, 1, 1, 2, 4, 9, 20, 48, 115, 286, 719};
        long[] actual = UnlabeledTreeCounter.countRooted(10);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testCountUnrooted() {
        // OEIS A000055: 0, 1, 1, 1, 2, 3, 6, 11, 23, 47, 106
        long[] expected = {0, 1, 1, 1, 2, 3, 6, 11, 23, 47, 106};
        long[] actual = UnlabeledTreeCounter.countUnrooted(10);
        assertArrayEquals(expected, actual);
    }
}
