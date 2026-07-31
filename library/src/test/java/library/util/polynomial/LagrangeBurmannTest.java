package library.util.polynomial;

import org.junit.jupiter.api.Test;
import library.util.Fp;
import library.util.MathUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LagrangeBurmannTest {
    private static final long mod = 998244353L;
    private static final Fp fp = Fp.MOD998244353;

    @Test
    public void testSimpleLinear() {
        // A(x) = x, B(x) = x/(1-x), G(y) = y/(1+y)
        // A(B(x)) = x/(1-x) = x + x^2 + x^3 + ...
        long[] A = {0, 1};
        long[] G = {0, 1, 1}; // y + y^2 is not quite right, G(y) = y/(1+y) = y - y^2 + y^3 - ...
        int maxN = 10;
        long[] G_poly = new long[maxN + 1];
        for (int i = 1; i <= maxN; i++) {
            G_poly[i] = (i % 2 == 1) ? 1 : mod - 1;
        }

        for (int n = 1; n <= maxN; n++) {
            long result = PolynomialFp.lagrangeBurmann(A, G_poly, n);
            assertEquals(1L, result, "Failed at n=" + n);
        }
        assertEquals(0L, PolynomialFp.lagrangeBurmann(A, G_poly, 0));
    }

    @Test
    public void testExpExp() {
        // A(x) = exp(x), B(x) = exp(x) - 1, G(y) = ln(1+y)
        // A(B(x)) = exp(exp(x) - 1) = sum B_n * x^n / n!
        // B_n are Bell numbers: 1, 1, 2, 5, 15, 52, 203, ...
        int maxN = 7;
        long[] A = new long[maxN + 1];
        long[] G = new long[maxN + 1];
        for (int i = 0; i <= maxN; i++) {
            A[i] = fp.ifac(i);
            if (i >= 1) {
                G[i] = (i % 2 == 1) ? fp.inv(i) : (mod - fp.inv(i)) % mod;
            }
        }

        long[] bell = {1, 1, 2, 5, 15, 52, 203, 877};
        for (int n = 0; n <= maxN; n++) {
            long result = PolynomialFp.lagrangeBurmann(A, G, n);
            long expected = bell[n] * fp.ifac(n) % mod;
            assertEquals(expected, result, "Failed at n=" + n);
        }
    }

    @Test
    public void testPower() {
        // A(x) = x^3, B(x) = x, G(y) = y
        long[] A = {0, 0, 0, 1};
        long[] G = {0, 1};
        assertEquals(0L, PolynomialFp.lagrangeBurmann(A, G, 0));
        assertEquals(0L, PolynomialFp.lagrangeBurmann(A, G, 1));
        assertEquals(0L, PolynomialFp.lagrangeBurmann(A, G, 2));
        assertEquals(1L, PolynomialFp.lagrangeBurmann(A, G, 3));
        assertEquals(0L, PolynomialFp.lagrangeBurmann(A, G, 4));
    }
}
