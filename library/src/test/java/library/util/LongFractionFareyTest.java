package library.util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongFractionFareyTest {

    @Test
    public void testFareyOrder1() {
        List<LongFraction> expected = List.of(
            new LongFraction(0, 1),
            new LongFraction(1, 1)
        );
        List<LongFraction> actual = new ArrayList<>();
        for (LongFraction f : LongFraction.fareySequence(1)) {
            actual.add(f);
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testFareyOrder2() {
        List<LongFraction> expected = List.of(
            new LongFraction(0, 1),
            new LongFraction(1, 2),
            new LongFraction(1, 1)
        );
        List<LongFraction> actual = new ArrayList<>();
        for (LongFraction f : LongFraction.fareySequence(2)) {
            actual.add(f);
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testFareyOrder3() {
        List<LongFraction> expected = List.of(
            new LongFraction(0, 1),
            new LongFraction(1, 3),
            new LongFraction(1, 2),
            new LongFraction(2, 3),
            new LongFraction(1, 1)
        );
        List<LongFraction> actual = new ArrayList<>();
        for (LongFraction f : LongFraction.fareySequence(3)) {
            actual.add(f);
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testFareyOrder5() {
        List<LongFraction> expected = List.of(
            new LongFraction(0, 1),
            new LongFraction(1, 5),
            new LongFraction(1, 4),
            new LongFraction(1, 3),
            new LongFraction(2, 5),
            new LongFraction(1, 2),
            new LongFraction(3, 5),
            new LongFraction(2, 3),
            new LongFraction(3, 4),
            new LongFraction(4, 5),
            new LongFraction(1, 1)
        );
        List<LongFraction> actual = new ArrayList<>();
        for (LongFraction f : LongFraction.fareySequence(5)) {
            actual.add(f);
        }
        assertEquals(expected, actual);
    }
}
