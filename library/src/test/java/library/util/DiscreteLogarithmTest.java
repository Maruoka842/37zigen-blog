package library.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import library.util.algebra.strategy.monoid.MonoidStrategy;
import library.util.algebra.strategy.GroupStrategy;

public class DiscreteLogarithmTest {

    @Test
    public void testDiscreteLogModBasic() {
        // 2^n = 8 (mod 13) -> n = 3
        assertEquals(3, DiscreteLogarithm.discreteLogMod(2, 8, 13));
        // 3^n = 1 (mod 13) -> n = 0
        assertEquals(0, DiscreteLogarithm.discreteLogMod(3, 1, 13));
        // 3^n = 1 (mod 13) n >= 1 -> n = 3
        // 3^1=3, 3^2=9, 3^3=27=1
        assertEquals(3, DiscreteLogarithm.discreteLogModNonzero(3, 1, 13));

        // 2^n = 3 (mod 13) -> n = 4
        assertEquals(4, DiscreteLogarithm.discreteLogMod(2, 3, 13));
    }

    @Test
    public void testDiscreteLogModNonCoprime() {
        // 2^n = 8 (mod 12)
        // 2^0 = 1
        // 2^1 = 2
        // 2^2 = 4
        // 2^3 = 8 -> n = 3
        assertEquals(3, DiscreteLogarithm.discreteLogMod(2, 8, 12));

        // 2^n = 4 (mod 12)
        assertEquals(2, DiscreteLogarithm.discreteLogMod(2, 4, 12));

        // 2^n = 2 (mod 12)
        assertEquals(1, DiscreteLogarithm.discreteLogMod(2, 2, 12));

        // 2^n = 6 (mod 12) -> no solution
        assertEquals(-1, DiscreteLogarithm.discreteLogMod(2, 6, 12));
    }

    @Test
    public void testDiscreteLogModLarge() {
        // 1234567^n = 9876543 (mod 1000000007)
        long x = 1234567;
        long md = 1000000007;
        long n = 123456;
        long y = MathUtils.modPow(x, n, md);
        assertEquals(n, DiscreteLogarithm.discreteLogMod(x, y, md));
    }

    @Test
    public void testDiscreteLogGeneric() {
        // Test with modular addition as monoid action
        // s + n*a = t (mod md)
        long a = 3;
        long s = 5;
        long t = 14;
        long md = 17;
        // 5 + 3n = 14 (mod 17)
        // 3n = 9 (mod 17) -> n = 3
        long res = DiscreteLogarithm.discreteLog(
            a,
            s,
            t,
            (f, cur) -> (f + cur) % md,
            (f1, f2) -> (f1 + f2) % md,
            md
        );
        assertEquals(3, res);
    }

    @Test
    public void testNoSolution() {
        // 2^n mod 13 is never 0
        assertEquals(-1, DiscreteLogarithm.discreteLogMod(2, 0, 13));
        // 2^n mod 12 is in {1, 2, 4, 8}
        assertEquals(-1, DiscreteLogarithm.discreteLogMod(2, 7, 12));
    }

    @Test
    public void testEdgeCases() {
        // s = t
        assertEquals(0, DiscreteLogarithm.discreteLogMod(2, 1, 13));
        // 0^0 = 1, 0^1 = 0, so 0^n = 0 starts from n=1
        assertEquals(1, DiscreteLogarithm.discreteLogMod(0, 0, 13));

        // maxSearch = 0
        assertEquals(-1, DiscreteLogarithm.discreteLogMod(2, 8, 13, 0));
        assertEquals(0, DiscreteLogarithm.discreteLogMod(2, 1, 13, 0));

        // y = 0
        assertEquals(1, DiscreteLogarithm.discreteLogMod(0, 0, 10)); // 0^1 = 0
        assertEquals(2, DiscreteLogarithm.discreteLogMod(10, 0, 100)); // 10^2 = 100 = 0
    }

    @Test
    public void testStrategyDiscreteLog() {
        // Test GroupStrategy.discreteLog
        long md = 17;
        GroupStrategy<Long> group = new GroupStrategy<Long>() {
            public Long identity() { return 1L; }
            public Long mul(Long a, Long b) { return a * b % md; }
            public Long inverse(Long a) { return MathUtils.modInv(a, md); }
            public boolean equals(Long a, Long b) { return a.equals(b); }
        };

        // 3^n = 13 (mod 17)
        // 3^0=1, 3^1=3, 3^2=9, 3^3=27=10, 3^4=30=13
        assertEquals(4, group.discreteLog(3L, 13L, 100));

        // Test MonoidStrategy.discreteLog
        MonoidStrategy<Long> monoid = new MonoidStrategy<Long>() {
            public Long identity() { return 1L; }
            public Long mul(Long a, Long b) { return a * b % md; }
        };
        assertEquals(4, monoid.discreteLog(3L, 13L, 100));
    }
}
