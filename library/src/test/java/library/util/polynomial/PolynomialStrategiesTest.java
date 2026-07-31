package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import library.util.algebra.strategy.EuclideanDomainStrategy;
import library.util.algebra.strategy.EuclideanDomainStrategy.ExtGCDResult;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.PolynomialQuotientRingStrategy;
import library.util.algebra.strategy.TruncatedPolynomialRingStrategy;

public class PolynomialStrategiesTest {

    static class FpField implements FieldStrategy<Long>, EuclideanDomainStrategy<Long>, ExactDivRingStrategy<Long> {
        final long mod;
        FpField(long mod) { this.mod = mod; }
        @Override public Long zero() { return 0L; }
        @Override public Long one() { return 1L; }
        @Override public Long add(Long a, Long b) { return (a + b) % mod; }
        @Override public Long sub(Long a, Long b) { return (a - b + mod) % mod; }
        @Override public Long mul(Long a, Long b) { return (a * b) % mod; }
        @Override public Long neg(Long a) { return (mod - a) % mod; }
        @Override public Long inv(Long a) {
            long b = mod, u = 1, v = 0;
            while (b > 0) {
                long t = a / b;
                a -= t * b; { long tmp = a; a = b; b = tmp; }
                u -= t * v; { long tmp = u; u = v; v = tmp; }
            }
            return (u % mod + mod) % mod;
        }
        @Override public boolean equals(Long a, Long b) { return a.equals(b); }
        @Override public Long div(Long a, Long b) { return FieldStrategy.super.div(a, b); }
        @Override public ExtGCDResult<Long> extgcd(Long a, Long b) { return FieldStrategy.super.extgcd(a, b); }
        @Override public Long mod(Long a, Long b) { return 0L; }
        @Override public long norm(Long a) { return a == 0 ? 0 : 1; }
        @Override public Long exactDiv(Long a, Long b) { return div(a, b); }
    }

    @Test
    void testTruncatedPolynomialRing() {
        FpField k = new FpField(998244353L);
        int n = 3;
        TruncatedPolynomialRingStrategy<Long> fps = new TruncatedPolynomialRingStrategy<>(k, n);

        // a = 1 + 2x + 3x^2
        Long[] a = {1L, 2L, 3L};
        // b = 4 + 5x + 6x^2
        Long[] b = {4L, 5L, 6L};

        // a + b = 5 + 7x + 9x^2
        Long[] sum = fps.add(a, b);
        assertArrayEquals(new Long[]{5L, 7L, 9L}, sum);

        // a * b = (1+2x+3x^2)(4+5x+6x^2) = 4 + (5+8)x + (6+10+12)x^2 + ... = 4 + 13x + 28x^2
        Long[] prod = fps.mul(a, b);
        assertArrayEquals(new Long[]{4L, 13L, 28L}, prod);

        // c = 1 + x
        Long[] c = {1L, 1L};
        // c^3 = (1+x)^3 = 1 + 3x + 3x^2 + x^3 -> truncated to 1 + 3x + 3x^2
        Long[] c2 = fps.mul(c, c);
        Long[] c3 = fps.mul(c2, c);
        assertArrayEquals(new Long[]{1L, 3L, 3L}, c3);
    }

    @Test
    void testPolynomialQuotientRing() {
        FpField k = new FpField(998244353L);
        // mod x^2 + 1
        Long[] modPoly = {1L, 0L, 1L};
        PolynomialQuotientRingStrategy<Long> ring = new PolynomialQuotientRingStrategy<>(k, modPoly);

        // a = x
        Long[] a = {0L, 1L};
        // a^2 = x^2 = -1 (mod x^2 + 1)
        Long[] a2 = ring.mul(a, a);
        // trim result should be {-1} effectively, but might have trailing zeros?
        // My trim method removes trailing zeros. -1 is {mod-1}.
        assertArrayEquals(new Long[]{998244352L}, a2);

        // (x+1)^2 = x^2 + 2x + 1 = 2x (mod x^2+1)
        Long[] b = {1L, 1L};
        Long[] b2 = ring.mul(b, b);
        assertArrayEquals(new Long[]{0L, 2L}, b2);
    }
}
