package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.Fp;
import library.util.Zn;
import library.util.algebra.strategy.longs.LongEuclideanDomainStrategy;
import library.util.algebra.strategy.longs.LongZStrategy;

public class LongsStrategiesTest {

    @Test
    public void testLongEuclideanStrategy() {
        LongZStrategy st = new LongZStrategy();
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(5L, st.add(2L, 3L));
        assertEquals(6L, st.mul(2L, 3L));
        assertEquals(-2L, st.neg(2L));
        assertEquals(2L, st.div(7L, 3L));
        assertEquals(1L, st.mod(7L, 3L));
        assertEquals(7L, st.norm(7L));
        assertEquals(7L, st.norm(-7L));
        assertEquals(2L, st.divExact(6L, 3L));
        assertTrue(st.equals(5L, 5L));

        LongEuclideanDomainStrategy.ExtGCDResult res = st.extgcd(12, 18);
        assertEquals(6, res.gcd());
        assertEquals(12 * res.x() + 18 * res.y(), res.gcd());
    }

    @Test
    public void testLongZnStrategy() {
        Zn st = new Zn(7L);
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(5L, st.add(2L, 3L));
        assertEquals(1L, st.add(5L, 3L));
        assertEquals(6L, st.mul(2L, 3L));
        assertEquals(2L, st.mul(3L, 3L));
        assertEquals(5L, st.neg(2L));
        assertTrue(st.equals(10L, 10L));
        assertTrue(st.equals(3L, 3L));
    }

    @Test
    public void testFpStrategy() {
        Fp st = new Fp(7L);
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(4L, st.inv(2L)); // 2 * 4 = 8 = 1 mod 7
        assertEquals(5L, st.div(3L, 2L)); // 3 * 4 = 12 = 5 mod 7
        assertEquals(0L, st.mod(3L, 2L));
        assertEquals(1, st.norm(3L));
        assertEquals(0, st.norm(0L));
    }
}
