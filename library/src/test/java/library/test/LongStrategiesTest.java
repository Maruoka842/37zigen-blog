package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.ZStrategy;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.ZnStrategy;
import library.util.algebra.strategy.PolynomialRingStrategy;

public class LongStrategiesTest {

    @Test
    public void testLongEuclideanStrategy() {
        ZStrategy st = new ZStrategy();
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(5L, st.add(2L, 3L));
        assertEquals(6L, st.mul(2L, 3L));
        assertEquals(-2L, st.neg(2L));
        assertEquals(2L, st.div(7L, 3L));
        assertEquals(1L, st.mod(7L, 3L));
        assertEquals(7L, st.norm(7L));
        assertEquals(7L, st.norm(-7L));
        assertEquals(2L, st.exactDiv(6L, 3L));
        assertTrue(st.equals(5L, 5L));
    }

    @Test
    public void testLongZnStrategy() {
        ZnStrategy st = new ZnStrategy(7L);
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(5L, st.add(2L, 3L));
        assertEquals(1L, st.add(5L, 3L));
        assertEquals(6L, st.mul(2L, 3L));
        assertEquals(2L, st.mul(3L, 3L));
        assertEquals(5L, st.neg(2L));
        assertTrue(st.equals(10L, 3L));
    }

    @Test
    public void testLongFpStrategy() {
        FpStrategy st = new FpStrategy(7L);
        assertEquals(0L, st.zero());
        assertEquals(1L, st.one());
        assertEquals(4L, st.inv(2L)); // 2 * 4 = 8 = 1 mod 7
        assertEquals(5L, st.div(3L, 2L)); // 3 * 4 = 12 = 5 mod 7
        assertEquals(0L, st.mod(3L, 2L));
        assertEquals(1, st.norm(3L));
        assertEquals(0, st.norm(0L));
    }

    @Test
    public void testPolynomialWithLongEuclidean() {
        ZStrategy base = new ZStrategy();
        PolynomialRingStrategy<Long> poly = new PolynomialRingStrategy<>(base);

        Long[] p1 = {1L, 2L}; // 1 + 2x
        Long[] p2 = {3L, 4L}; // 3 + 4x

        Long[] sum = poly.add(p1, p2); // 4 + 6x
        assertArrayEquals(new Long[]{4L, 6L}, sum);

        Long[] prod = poly.mul(p1, p2); // (1+2x)(3+4x) = 3 + 10x + 8x^2
        assertArrayEquals(new Long[]{3L, 10L, 8L}, prod);

        // test divExact
        Long[] p3 = poly.mul(p1, p2);
        Long[] q = poly.exactDiv(p3, p1);
        assertArrayEquals(p2, q);
    }

    @Test
    public void testPolynomialWithLongZn() {
        ZnStrategy base = new ZnStrategy(7L);
        PolynomialRingStrategy<Long> poly = new PolynomialRingStrategy<>(base);

        Long[] p1 = {1L, 5L}; // 1 + 5x
        Long[] p2 = {3L, 4L}; // 3 + 4x

        Long[] sum = poly.add(p1, p2); // 4 + 9x = 4 + 2x mod 7
        assertArrayEquals(new Long[]{4L, 2L}, sum);

        Long[] prod = poly.mul(p1, p2); // (1+5x)(3+4x) = 3 + 19x + 20x^2 = 3 + 5x + 6x^2 mod 7
        assertArrayEquals(new Long[]{3L, 5L, 6L}, prod);
    }
}
