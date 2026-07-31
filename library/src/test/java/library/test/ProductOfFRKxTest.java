package library.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.Random;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import library.util.polynomial.PolynomialFp;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.MathUtils;

public class ProductOfFRKxTest {
    long mod = 998244353L;
    PolynomialFpDynamic polyDynamic = PolynomialFpDynamic.nttFriendly(mod);

    @Test
    public void testDynamic() {
        Random rand = new Random(42);
        int n = 100;
        long[] f = new long[n];
        f[0] = 1;
        for (int i = 1; i < n; i++) f[i] = rand.nextInt((int) mod);

        long r = rand.nextInt((int) mod);
        long M = 5;

        long[] expected = naiveProduct(f, r, M, n);
        long[] actual = polyDynamic.productOfFRKx(f, r, M);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testStatic() {
        Random rand = new Random(43);
        int n = 100;
        long[] f = new long[n];
        f[0] = 1;
        for (int i = 1; i < n; i++) f[i] = rand.nextInt((int) mod);

        long r = rand.nextInt((int) mod);
        long M = 5;

        long[] expected = naiveProduct(f, r, M, n);
        long[] actual = PolynomialFp.productOfFRKx(f, r, M);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testM0() {
        long[] f = {1, 2, 3};
        long[] actual = polyDynamic.productOfFRKx(f, 5, 0);
        assertArrayEquals(new long[]{1, 0, 0}, actual);
    }

    @Test
    public void testM1() {
        long[] f = {1, 2, 3};
        long[] actual = polyDynamic.productOfFRKx(f, 5, 1);
        assertArrayEquals(f, actual);
    }

    @Test
    public void testR1() {
        long[] f = {1, 2, 3};
        long[] actual = polyDynamic.productOfFRKx(f, 1, 3);
        long[] expected = polyDynamic.pow(f, 3);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testR0() {
        long[] f = {1, 2, 3};
        long[] actual = polyDynamic.productOfFRKx(f, 0, 3);
        // f(x) * f(0) * f(0) = f(x)
        assertArrayEquals(f, actual);
    }

    private long[] naiveProduct(long[] f, long r, long M, int n) {
        long[] res = {1};
        for (int k = 0; k < M; k++) {
            long rk = MathUtils.modPow(r, k, mod);
            long[] fk = new long[f.length];
            long rkPow = 1;
            for (int i = 0; i < f.length; i++) {
                fk[i] = f[i] * rkPow % mod;
                rkPow = rkPow * rk % mod;
            }
            res = polyDynamic.mul(res, fk);
            if (res.length > n) {
                res = Arrays.copyOf(res, n);
            }
        }
        if (res.length < n) {
            res = Arrays.copyOf(res, n);
        }
        return res;
    }
}
