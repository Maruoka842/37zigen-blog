package library.util.monoid.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import library.util.monoid.MaxAndCount;

class MonoidOperatorTest {
    @Test
    void testMaxAndCountMonoid() {
        MaxAndCount p1 = new MaxAndCount(10, 2);
        MaxAndCount p2 = new MaxAndCount(20, 1);
        MaxAndCount p3 = new MaxAndCount(20, 3);

        // test merge via MonoidOperator::merge
        MaxAndCount res1 = MonoidOperator.merge(p1, p2);
        assertEquals(20, res1.max);
        assertEquals(1, res1.count);

        MaxAndCount res2 = MonoidOperator.merge(p2, p3);
        assertEquals(20, res2.max);
        assertEquals(4, res2.count);

        // test MonoidElement methods
        assertEquals(20, p1.mul(p2).max);
        assertEquals(1, p1.mul(p2).count);

        assertEquals(Long.MIN_VALUE, p1.one().max);
        assertEquals(0, p1.one().count);
    }
}
