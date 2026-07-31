package library.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import library.util.algebra.instance.impl.GaussInt;
import library.util.algebra.strategy.GaussIntStrategy;

public class GaussIntEuclideanTest {
    @Test
    public void testBasic() {
        GaussInt a = new GaussInt(3, 1);
        GaussInt b = new GaussInt(1, 2);

        assertEquals(new GaussInt(4, 3), a.add(b));
        assertEquals(new GaussInt(2, -1), a.sub(b));
        assertEquals(new GaussInt(-3, -1), a.neg());
        assertEquals(new GaussInt(1, 7), a.mul(b));
        // (3+i)/(1+2i) = (3+i)(1-2i)/5 = (3+2 + i(1-6))/5 = (5-5i)/5 = 1-i
        assertEquals(new GaussInt(1, -1), a.div(b));
        assertEquals(new GaussInt(0, 0), a.rem(b));
        assertEquals(new GaussInt(0, 0), a.mod(b));
    }

    @Test
    public void testGCD() {
        GaussInt a = new GaussInt(5, 0);
        GaussInt b = new GaussInt(3, 4);
        GaussInt g = a.gcd(b);
        assertEquals(new GaussInt(2, 1), g);
    }

    @Test
    public void testStrategyGCD() {
        GaussIntStrategy strategy = new GaussIntStrategy();
        GaussInt a = new GaussInt(5, 0);
        GaussInt b = new GaussInt(3, 4);
        assertEquals(new GaussInt(2, 1), strategy.gcd(a, b));
    }

    @Test
    public void testExtGCD() {
        GaussIntStrategy strategy = new GaussIntStrategy();
        GaussInt a = new GaussInt(5, 0);
        GaussInt b = new GaussInt(3, 4);
        var res = strategy.extgcd(a, b);
        assertEquals(new GaussInt(2, 1), res.gcd());
        assertEquals(res.gcd(), strategy.add(strategy.mul(res.x(), a), strategy.mul(res.y(), b)));
    }

    @Test
    public void testExactDiv() {
        GaussInt a = new GaussInt(5, 0);
        GaussInt b = new GaussInt(2, 1);
        assertEquals(new GaussInt(2, -1), a.exactDiv(b));
    }

    @Test
    public void testNorm() {
        assertEquals(25, new GaussInt(3, 4).norm());
        assertEquals(25, new GaussInt(5, 0).norm());
    }

    @Test
    public void testConstants() {
        assertEquals(new GaussInt(0, 0), GaussInt.ZERO);
        assertEquals(new GaussInt(1, 0), GaussInt.ONE);
        assertEquals(new GaussInt(2, 0), GaussInt.TWO);
    }
}
