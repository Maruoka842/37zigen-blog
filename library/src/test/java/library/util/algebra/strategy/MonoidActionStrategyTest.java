package library.util.algebra.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import library.util.algebra.strategy.monoid.MonoidStrategy;

public class MonoidActionStrategyTest {

    static record Affine(long a, long b) {}

    static class AffineMonoid implements MonoidStrategy<Affine> {
        long mod;
        AffineMonoid(long mod) { this.mod = mod; }
        @Override public Affine identity() { return new Affine(1, 0); }
        @Override public Affine mul(Affine f, Affine g) {
            // f(g(x)) = f.a * (g.a * x + g.b) + f.b = (f.a * g.a) * x + (f.a * g.b + f.b)
            return new Affine((f.a * g.a) % mod, (f.a * g.b + f.b) % mod);
        }
    }

    static class AffineAction implements MonoidActionStrategy<Affine, Long> {
        AffineMonoid monoid;
        AffineAction(AffineMonoid monoid) { this.monoid = monoid; }
        @Override public MonoidStrategy<Affine> actingMonoidStrategy() { return monoid; }
        @Override public Long act(Affine f, Long x) {
            return (f.a * x + f.b) % monoid.mod;
        }
    }

    @Test
    public void testAffineAction() {
        long mod = 1000000007;
        AffineMonoid monoid = new AffineMonoid(mod);
        AffineAction action = new AffineAction(monoid);

        Affine f = new Affine(2, 3);
        long x = 10;
        assertEquals(23L, action.act(f, x));

        // f(f(x)) = 2(2x+3)+3 = 4x + 9
        assertEquals(49L, action.powAct(f, 2, x));

        // f^10(x)
        long res = x;
        for (int i = 0; i < 10; i++) res = action.act(f, res);
        assertEquals(res, action.powAct(f, 10, x));
    }

    @Test
    public void testDiscreteLog() {
        long mod = 1000000007;
        AffineMonoid monoid = new AffineMonoid(mod);
        AffineAction action = new AffineAction(monoid);

        Affine f = new Affine(2, 3);
        long x = 10;
        long target = action.powAct(f, 123, x);

        assertEquals(123L, action.discreteLog(f, x, target, 1000));
        assertEquals(-1L, action.discreteLog(f, x, target, 100));
    }

    @Test
    public void testSemigroupDiscreteLog() {
        long mod = 1000000007;
        AffineMonoid monoid = new AffineMonoid(mod);
        AffineAction action = new AffineAction(monoid);

        Affine f = new Affine(2, 3);
        long x = 10;

        // n=0 should not be found by semigroup discreteLog starting from 1
        long target0 = x;
        assertEquals(-1L, action.discreteLog(f, x, target0, monoid::mul, 1000));

        long target1 = action.act(f, x);
        assertEquals(1L, action.discreteLog(f, x, target1, monoid::mul, 1000));

        long target123 = action.powAct(f, 123, x);
        assertEquals(123L, action.discreteLog(f, x, target123, monoid::mul, 1000));
    }
}
