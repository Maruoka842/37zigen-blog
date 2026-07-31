package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class PolynomialInfiniteProductTest {

    private static final long MOD = 998244353L;
    private final PolynomialFpDynamic2D poly2d = PolynomialFpDynamic2D.of(MOD);

    private long[][] naiveInfiniteProduct(long[][] P, long[][] Q, int nx, int ny) {
        long[][] res = new long[nx][ny];
        res[0][0] = 1;
        for (int i = 1; i < nx; i++) {
            // Construct P(x, x^i y) mod x^nx y^ny
            long[][] pi = new long[nx][ny];
            for (int r = 0; r < P.length; r++) {
                for (int s = 0; s < P[r].length; s++) {
                    int xDeg = r + i * s;
                    int yDeg = s;
                    if (xDeg < nx && yDeg < ny) {
                        pi[xDeg][yDeg] = (pi[xDeg][yDeg] + P[r][s]) % MOD;
                    }
                }
            }
            // Construct Q(x, x^i y) mod x^nx y^ny
            long[][] qi = new long[nx][ny];
            for (int r = 0; r < Q.length; r++) {
                for (int s = 0; s < Q[r].length; s++) {
                    int xDeg = r + i * s;
                    int yDeg = s;
                    if (xDeg < nx && yDeg < ny) {
                        qi[xDeg][yDeg] = (qi[xDeg][yDeg] + Q[r][s]) % MOD;
                    }
                }
            }
            // Invert Q(x, x^i y)
            long[][] qi_inv = poly2d.sparseInv(qi, nx, ny);
            // Factor = P(x, x^i y) * Q(x, x^i y)^{-1}
            long[][] factor = poly2d.mul(pi, qi_inv);
            // Truncate factor to nx x ny
            long[][] truncatedFactor = new long[nx][ny];
            for (int x = 0; x < nx; x++) {
                for (int y = 0; y < ny; y++) {
                    if (x < factor.length && y < factor[x].length) {
                        truncatedFactor[x][y] = factor[x][y];
                    }
                }
            }
            // Multiply res by truncatedFactor
            long[][] nextRes = poly2d.mul(res, truncatedFactor);
            res = new long[nx][ny];
            for (int x = 0; x < nx; x++) {
                for (int y = 0; y < ny; y++) {
                    if (x < nextRes.length && y < nextRes[x].length) {
                        res[x][y] = nextRes[x][y];
                    }
                }
            }
        }
        return res;
    }

    @Test
    public void testPartitions() {
        // P(x, z) = 1
        long[][] P = {{1}};
        // Q(x, z) = 1 - z
        long[][] Q = {{1, MOD - 1}};
        int nx = 15;
        int ny = 10;

        long[][] expected = naiveInfiniteProduct(P, Q, nx, ny);
        long[][] actual = poly2d.infiniteProductPdivQ(P, Q, nx, ny);

        for (int i = 0; i < nx; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    @Test
    public void testDistinctPartitions() {
        // P(x, z) = 1 + z
        long[][] P = {{1, 1}};
        // Q(x, z) = 1
        long[][] Q = {{1}};
        int nx = 15;
        int ny = 10;

        long[][] expected = naiveInfiniteProduct(P, Q, nx, ny);
        long[][] actual = poly2d.infiniteProductPdivQ(P, Q, nx, ny);

        for (int i = 0; i < nx; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    @Test
    public void testComplex2DPolynomials() {
        // P[r][0] and Q[r][0] must be equal to ensure convergence at y=0.
        // P(x, z) = (1 + 2 x + 4 x^2) + (3 + 5 x) z
        long[][] P = {
            {1, 3}, // x^0
            {2, 5}, // x^1
            {4}     // x^2
        };
        // Q(x, z) = (1 + 2 x + 4 x^2) + (1 + 7 x) z
        long[][] Q = {
            {1, 1}, // x^0
            {2, 0}, // x^1
            {4, 7}  // x^2
        };
        int nx = 12;
        int ny = 8;

        long[][] expected = naiveInfiniteProduct(P, Q, nx, ny);
        long[][] actual = poly2d.infiniteProductPdivQ(P, Q, nx, ny);

        for (int i = 0; i < nx; i++) {
            assertArrayEquals(expected[i], actual[i]);
        }
    }

    @Test
    public void testEdgeCasesAndValidation() {
        long[][] P = {{1, 1}};
        long[][] Q = {{1, 2}};

        // nx or ny equal to 0
        long[][] actualZeroX = poly2d.infiniteProductPdivQ(P, Q, 0, 5);
        assertArrayEquals(new long[0][0], actualZeroX);

        long[][] actualZeroY = poly2d.infiniteProductPdivQ(P, Q, 5, 0);
        assertArrayEquals(new long[0][0], actualZeroY);

        // invalid constant term cases
        long[][] P_bad = {{2, 1}};
        long[][] Q_bad = {{1, 1}};
        assertThrows(IllegalArgumentException.class, () -> {
            poly2d.infiniteProductPdivQ(P_bad, Q_bad, 5, 5);
        });

        long[][] P_good = {{1, 1}};
        long[][] Q_bad2 = {{MOD + 2, 1}}; // 2 mod MOD
        assertThrows(IllegalArgumentException.class, () -> {
            poly2d.infiniteProductPdivQ(P_good, Q_bad2, 5, 5);
        });

        // invalid P(x, 0) == Q(x, 0) case
        long[][] P_bad_x = {
            {1, 1},
            {2, 0}
        };
        long[][] Q_bad_x = {
            {1, 1},
            {3, 0} // Q[1][0] is 3 != P[1][0] which is 2
        };
        assertThrows(IllegalArgumentException.class, () -> {
            poly2d.infiniteProductPdivQ(P_bad_x, Q_bad_x, 5, 5);
        });
    }
}
