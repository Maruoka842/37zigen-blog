package library.util.polynomial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import library.util.Fp;
import library.util.MathUtils;

public class HolonomicSequenceTest {

    @Test
    public void testPrefixProductFactorial() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        long n = 1000;
        long expected = 1;
        for (int i = 1; i <= n; i++) {
            expected = expected * i % mod;
        }

        // P(x) = x + 1. prefixProduct(P, n) = P(0)P(1)...P(n-1) = 1*2*...*n = n!
        long[] coeffs = {1, 1};
        long actual = HolonomicSequence.prefixProduct(coeffs, n, poly);

        assertEquals(expected, actual);
    }

    @Test
    public void testPrefixProductLarge() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        long n = 10000;
        long expected = 1;
        for (int i = 1; i <= n; i++) {
            expected = expected * i % mod;
        }

        long[] coeffs = {1, 1};
        long actual = HolonomicSequence.prefixProduct(coeffs, n, poly);

        assertEquals(expected, actual);
    }

    @Test
    public void testFactorial() {
        long mod = 998244353;
        Fp fp = new Fp(mod);

        long[] testNs = {0, 1, 5, 100, 1000, 10000};
        for (long n : testNs) {
            long expected = 1;
            for (int i = 1; i <= n; i++) expected = expected * i % mod;
            assertEquals(expected, fp.facLarge(n), "Factorial failed for n=" + n);
        }

        assertEquals(0, fp.facLarge(mod));
        assertEquals(0, fp.facLarge(mod + 1));
    }

    @Test
    public void testPermutations() {
        long mod = 998244353;
        Fp fp = new Fp(mod);

        // Small n
        assertEquals(720, fp.permLarge(10, 3));

        // Large n, small k
        long n = 1_000_000_000_000L;
        long k = 5;
        long expected = 1;
        for (long i = 0; i < k; i++) expected = expected * ((n - i) % mod) % mod;
        assertEquals(expected, fp.permLarge(n, k));

        // Large n, large k
        n = 1_000_000_000_000L;
        k = 10000;
        // Verify with consistency
        long smallN = 200_000;
        long smallK = 10000;
        long expectedSmall = 1;
        for (long i = 0; i < smallK; i++) expectedSmall = expectedSmall * (smallN - i) % mod;
        assertEquals(expectedSmall, fp.permLarge(smallN, smallK));

        // Crossing mod
        assertEquals(0, fp.permLarge(mod, 2));
        assertEquals(0, fp.permLarge(mod + 1, 2)); // (mod+1)*mod = 0
        assertEquals(6, fp.permLarge(mod + 3, 2)); // (mod+3)*(mod+2) = 3*2 = 6
    }

    @Test
    public void testCombinations() {
        long mod = 998244353;
        Fp fp = new Fp(mod);

        assertEquals(1, fp.combLarge(10, 0));
        assertEquals(10, fp.combLarge(10, 1));
        assertEquals(45, fp.combLarge(10, 2));
        assertEquals(120, fp.combLarge(10, 3));
        assertEquals(210, fp.combLarge(10, 4));
        assertEquals(252, fp.combLarge(10, 5));
        assertEquals(210, fp.combLarge(10, 6));

        long n = 1_000_000_000;
        long k = 10000;
        // nCk = nPk / k!
        long nPk = fp.permLarge(n, k);
        long kFac = fp.facLarge(k);
        long expected = nPk * MathUtils.modInv(kFac, mod) % mod;
        assertEquals(expected, fp.combLarge(n, k));
    }

    @Test
    public void testExtend1D() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // factorial: a_{k+1} - (k+1)*a_k = 0
        // P_1(k) = 1, P_0(k) = -k - 1
        long[][] Ps = {
            {mod - 1, mod - 1}, // P_0(k) = -1 - k
            {1}                 // P_1(k) = 1
        };

        long[] initialValues = {1}; // a_0 = 0! = 1
        int targetLen = 10;
        long[] extended = HolonomicSequence.extend(initialValues, Ps, targetLen, poly);

        long[] expected = new long[targetLen];
        expected[0] = 1;
        for (int i = 1; i < targetLen; i++) {
            expected[i] = expected[i - 1] * i % mod;
        }

        for (int i = 0; i < targetLen; i++) {
            assertEquals(expected[i], extended[i]);
        }
    }

    @Test
    public void testGuessOneVariable() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x) = k! * x^k (mod mod)
        // satisfies: a_{k+1}(x) - (k+1)*x * a_k(x) = 0
        // P_1(k, x) = 1, P_0(k, x) = - (k+1)*x = -k*x - x
        int X = 5;
        int K_guess = 10;
        int K_all = 20;
        long[][] s_all = new long[X][K_all];
        long[][] s_guess = new long[X][K_guess];
        for (int x = 0; x < X; x++) {
            long fact = 1;
            long xPow = 1;
            for (int k = 0; k < K_all; k++) {
                s_all[x][k] = fact * xPow % mod;
                if (k < K_guess) {
                    s_guess[x][k] = s_all[x][k];
                }
                fact = fact * (k + 1) % mod;
                xPow = xPow * x % mod;
            }
        }

        long[][][] c = HolonomicSequence.guess(s_guess, 1, 1, 1, poly);
        org.junit.jupiter.api.Assertions.assertNotNull(c);
        int d = c.length - 1;
        int maxGK = c[0].length - 1;
        int maxGX = c[0][0].length - 1;
        assertEquals(1, d);
        assertEquals(1, maxGK);
        assertEquals(1, maxGX);

        long lead = c[1][0][0];
        long invLead = MathUtils.modInv(lead, mod);

        // Normalize
        for (int i = 0; i <= d; i++) {
            for (int r = 0; r <= maxGK; r++) {
                for (int s_val = 0; s_val <= maxGX; s_val++) {
                    c[i][r][s_val] = c[i][r][s_val] * invLead % mod;
                }
            }
        }

        // P_1(k, x) = 1
        assertEquals(1, c[1][0][0]);
        assertEquals(0, c[1][0][1]);
        assertEquals(0, c[1][1][0]);
        assertEquals(0, c[1][1][1]);

        // P_0(k, x) = -k*x - x => c[0][1][1] = -1, c[0][0][1] = -1
        assertEquals(0, c[0][0][0]);
        assertEquals(mod - 1, c[0][0][1]);
        assertEquals(0, c[0][1][0]);
        assertEquals(mod - 1, c[0][1][1]);

        // Verify with extend (1-parameter) beyond K_guess
        long[][] initVals = new long[X][d];
        for (int x = 0; x < X; x++) {
            for (int i = 0; i < d; i++) {
                initVals[x][i] = s_all[x][i];
            }
        }
        for (int x = 0; x < X; x++) {
            long[] extended = HolonomicSequence.extend(initVals[x], c, x, K_all, poly);
            for (int k = 0; k < K_all; k++) {
                assertEquals(s_all[x][k], extended[k], "Mismatch at x=" + x + ", k=" + k);
            }
        }
    }

    @Test
    public void testGuessAndExtendTwoVariablesSimple() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x, y) = (x + y)^k
        // satisfies: a_{k+1}(x, y) - (x + y)*a_k(x, y) = 0
        // P_1(k, x, y) = 1, P_0(k, x, y) = -x - y
        int X = 5; // x in [0, 4]
        int Y = 5; // y in [0, 4]
        int K_guess = 8;
        int K_all = 20;

        long[][][] s_guess = new long[X][Y][K_guess];
        long[][][] s_all = new long[X][Y][K_all];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                long base = (x + y) % mod;
                long cur = 1;
                for (int k = 0; k < K_all; k++) {
                    s_all[x][y][k] = cur;
                    if (k < K_guess) {
                        s_guess[x][y][k] = cur;
                    }
                    cur = cur * base % mod;
                }
            }
        }

        // Guess the 2-variable recurrence relation
        long[][][][] c = HolonomicSequence.guess(s_guess, 1, 1, 1, 1, poly);
        org.junit.jupiter.api.Assertions.assertNotNull(c);

        int d = c.length - 1;
        int gk = c[0].length - 1;
        int gx = c[0][0].length - 1;
        int gy = c[0][0][0].length - 1;

        // Verify with extend at non-degenerate/any selected points like (0, 3) and (3, 3)
        // Since P_1 is 1, no point is degenerate.
        int[][] pts = { {0, 0}, {0, 3}, {3, 0}, {3, 3}, {4, 4} };
        for (int[] pt : pts) {
            int targetX = pt[0];
            int targetY = pt[1];

            long[] initVals = new long[d];
            for (int i = 0; i < d; i++) {
                initVals[i] = s_all[targetX][targetY][i];
            }

            // Directly call the new 2-parameter extend method
            long[] extended = HolonomicSequence.extend(initVals, c, targetX, targetY, K_all, poly);
            for (int i = 0; i < K_all; i++) {
                assertEquals(s_all[targetX][targetY][i], extended[i], "Mismatch at x=" + targetX + ", y=" + targetY + ", i=" + i);
            }
        }
    }

    @Test
    public void testGuessTwoVariables() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x, y) = k! * x^k * y^k (mod mod)
        // satisfies: a_{k+1}(x, y) - (k+1)*x*y * a_k(x, y) = 0
        // P_1(k, x, y) = 1, P_0(k, x, y) = - (k+1)*x*y = -k*x*y - x*y
        int X = 4;
        int Y = 4;
        int K = 12;
        long[][][] s = new long[X][Y][K];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                long fact = 1;
                long xyPow = 1;
                long xy = (long) x * y % mod;
                for (int k = 0; k < K; k++) {
                    s[x][y][k] = fact * xyPow % mod;
                    fact = fact * (k + 1) % mod;
                    xyPow = xyPow * xy % mod;
                }
            }
        }

        long[][][][] c = HolonomicSequence.guess(s, 1, 1, 1, 1, poly);
        org.junit.jupiter.api.Assertions.assertNotNull(c);
        int d = c.length - 1;
        int maxGK = c[0].length - 1;
        int maxGX = c[0][0].length - 1;
        int maxGY = c[0][0][0].length - 1;
        assertEquals(1, d);
        assertEquals(1, maxGK);
        assertEquals(1, maxGX);
        assertEquals(1, maxGY);

        long lead = c[1][0][0][0];
        long invLead = MathUtils.modInv(lead, mod);

        // Normalize
        for (int i = 0; i <= d; i++) {
            for (int r = 0; r <= maxGK; r++) {
                for (int s_val = 0; s_val <= maxGX; s_val++) {
                    for (int t = 0; t <= maxGY; t++) {
                        c[i][r][s_val][t] = c[i][r][s_val][t] * invLead % mod;
                    }
                }
            }
        }

        // P_1(k, x, y) = 1
        assertEquals(1, c[1][0][0][0]);
        for (int r = 0; r <= 1; r++) {
            for (int s_val = 0; s_val <= 1; s_val++) {
                for (int t = 0; t <= 1; t++) {
                    if (r == 0 && s_val == 0 && t == 0) continue;
                    assertEquals(0, c[1][r][s_val][t]);
                }
            }
        }

        // P_0(k, x, y) = -k*x*y - x*y => c[0][1][1][1] = -1, c[0][0][1][1] = -1, others 0
        assertEquals(0, c[0][0][0][0]);
        assertEquals(0, c[0][0][0][1]);
        assertEquals(0, c[0][0][1][0]);
        assertEquals(mod - 1, c[0][0][1][1]);
        assertEquals(0, c[0][1][0][0]);
        assertEquals(0, c[0][1][0][1]);
        assertEquals(0, c[0][1][1][0]);
        assertEquals(mod - 1, c[0][1][1][1]);
    }

    private long nCr(long n, long r, long mod) {
        if (r < 0 || r > n) return 0;
        long num = 1;
        long den = 1;
        for (long i = 0; i < r; i++) {
            num = num * ((n - i) % mod) % mod;
            den = den * ((i + 1) % mod) % mod;
        }
        return num * MathUtils.modInv(den, mod) % mod;
    }

    @Test
    public void testGuessCombinationWithParameter() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_i(A) = (2i+A choose i)
        // Satisfies: (i+1)(i+A+1) a_{i+1}(A) - (2i+A+2)(2i+A+1) a_i(A) = 0
        // P_1(i, A) = i^2 + i*A + 2*i + A + 1
        // P_0(i, A) = -4*i^2 - 2*i*A - 6*i - A^2 - 3*A - 2
        int X = 6;  // A from 0 to 5
        int K_guess = 15; // i from 0 to 14
        int K_all = 25;   // i from 0 to 24
        long[][] s_all = new long[X][K_all];
        long[][] s_guess = new long[X][K_guess];
        for (int A = 0; A < X; A++) {
            for (int i = 0; i < K_all; i++) {
                s_all[A][i] = nCr(2 * i + A, i, mod);
                if (i < K_guess) {
                    s_guess[A][i] = s_all[A][i];
                }
            }
        }

        long[][][] c = HolonomicSequence.guess(s_guess, 1, 2, 2, poly);
        org.junit.jupiter.api.Assertions.assertNotNull(c);
        int d = c.length - 1;
        int maxGK = c[0].length - 1;
        int maxGX = c[0][0].length - 1;
        assertEquals(1, d);
        assertEquals(2, maxGK);
        assertEquals(2, maxGX);

        long lead = c[1][2][0]; // coefficient of i^2 in P_1
        long invLead = MathUtils.modInv(lead, mod);

        // Normalize
        for (int i = 0; i <= d; i++) {
            for (int r = 0; r <= maxGK; r++) {
                for (int s_val = 0; s_val <= maxGX; s_val++) {
                    c[i][r][s_val] = c[i][r][s_val] * invLead % mod;
                }
            }
        }

        // P_1(i, A) = i^2 + i*A + 2*i + A + 1
        assertEquals(1, c[1][2][0]); // i^2
        assertEquals(0, c[1][2][1]);
        assertEquals(0, c[1][2][2]);
        assertEquals(2, c[1][1][0]); // 2*i
        assertEquals(1, c[1][1][1]); // i*A
        assertEquals(0, c[1][1][2]);
        assertEquals(1, c[1][0][0]); // 1
        assertEquals(1, c[1][0][1]); // A
        assertEquals(0, c[1][0][2]);

        // P_0(i, A) = -4*i^2 - 4*i*A - 6*i - A^2 - 3*A - 2
        assertEquals((mod - 4) % mod, c[0][2][0]); // -4*i^2
        assertEquals(0, c[0][2][1]);
        assertEquals(0, c[0][2][2]);
        assertEquals((mod - 6) % mod, c[0][1][0]); // -6*i
        assertEquals((mod - 4) % mod, c[0][1][1]); // -4*i*A
        assertEquals(0, c[0][1][2]);
        assertEquals((mod - 2) % mod, c[0][0][0]); // -2
        assertEquals((mod - 3) % mod, c[0][0][1]); // -3*A
        assertEquals((mod - 1) % mod, c[0][0][2]); // -A^2

        // Verify with extend (1-parameter) beyond K_guess
        long[][] initVals = new long[X][d];
        for (int A = 0; A < X; A++) {
            for (int i = 0; i < d; i++) {
                initVals[A][i] = s_all[A][i];
            }
        }
        for (int A = 0; A < X; A++) {
            long[] extended = HolonomicSequence.extend(initVals[A], c, A, K_all, poly);
            for (int i = 0; i < K_all; i++) {
                assertEquals(s_all[A][i], extended[i], "Mismatch at A=" + A + ", i=" + i);
            }
        }
    }

    @Test
    public void testGuessTwoVariablesSymmetric() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_i(A, B) = [X^A Y^B] (XY + X + Y + X^-1 + Y^-1 + X^-1 Y^-1 + 2)^i
        int X_max = 3;  // A from 0 to 2
        int Y_max = 3;  // B from 0 to 2
        int K_max = 16; // i from 0 to 15 (used for guessing)
        int K_all = 26; // i from 0 to 25 (used for verification)

        // We compute by DP
        int offset = 30;
        int size = 2 * offset + 1;
        long[][][] dp = new long[K_all][size][size];
        dp[0][offset][offset] = 1;

        for (int i = 0; i < K_all - 1; i++) {
            for (int A = -i; A <= i; A++) {
                for (int B = -i; B <= i; B++) {
                    long val = dp[i][A + offset][B + offset];
                    if (val == 0) continue;

                    // transition to i+1
                    // self-loop with weight 2
                    dp[i+1][A + offset][B + offset] = (dp[i+1][A + offset][B + offset] + 2 * val) % mod;
                    // X Y
                    dp[i+1][A + 1 + offset][B + 1 + offset] = (dp[i+1][A + 1 + offset][B + 1 + offset] + val) % mod;
                    // X
                    dp[i+1][A + 1 + offset][B + offset] = (dp[i+1][A + 1 + offset][B + offset] + val) % mod;
                    // Y
                    dp[i+1][A + offset][B + 1 + offset] = (dp[i+1][A + offset][B + 1 + offset] + val) % mod;
                    // X^-1
                    dp[i+1][A - 1 + offset][B + offset] = (dp[i+1][A - 1 + offset][B + offset] + val) % mod;
                    // Y^-1
                    dp[i+1][A + offset][B - 1 + offset] = (dp[i+1][A + offset][B - 1 + offset] + val) % mod;
                    // X^-1 Y^-1
                    dp[i+1][A - 1 + offset][B - 1 + offset] = (dp[i+1][A - 1 + offset][B - 1 + offset] + val) % mod;
                }
            }
        }

        long[][][] s_guess = new long[X_max][Y_max][K_max];
        long[][][] s_all = new long[X_max][Y_max][K_all];
        for (int A = 0; A < X_max; A++) {
            for (int B = 0; B < Y_max; B++) {
                for (int i = 0; i < K_all; i++) {
                    s_all[A][B][i] = dp[i][A + offset][B + offset];
                    if (i < K_max) {
                        s_guess[A][B][i] = s_all[A][B][i];
                    }
                }
            }
        }
        long[][][][] c = HolonomicSequence.guess(s_guess, 2, 2, 2, 2, poly);
        if (c != null) {
            System.out.println("Relation found!");
            System.out.println("d=" + (c.length - 1) + ", maxGK=" + (c[0].length - 1) + ", maxGX=" + (c[0][0].length - 1) + ", maxGY=" + (c[0][0][0].length - 1));
            // Print all coefficients
            for (int j = 0; j < c.length; j++) {
                for (int r = 0; r < c[j].length; r++) {
                    for (int s_val = 0; s_val < c[j][r].length; s_val++) {
                        for (int t = 0; t < c[j][r][s_val].length; t++) {
                            long val = c[j][r][s_val][t];
                            if (val != 0) {
                                System.out.println("P_" + j + " coefficient: k^" + r + " A^" + s_val + " B^" + t + " = " + val);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No relation found within d=2, maxGK=2, maxGX=2, maxGY=2");
        }
        org.junit.jupiter.api.Assertions.assertNotNull(c);

        int d = c.length - 1;
        int gk = c[0].length - 1;
        int gx = c[0][0].length - 1;
        int gy = c[0][0][0].length - 1;

        // Verify with extend at A=0, B=0 where the recurrence does not degenerate, extending up to K_all
        long[] initVals00 = new long[d];
        for (int i = 0; i < d; i++) {
            initVals00[i] = s_all[0][0][i];
        }

        // Construct 1D Ps at A=0, B=0
        long[][] Ps_00 = new long[d + 1][gk + 1];
        for (int j = 0; j <= d; j++) {
            for (int r = 0; r <= gk; r++) {
                long[] coeffX = new long[gx + 1];
                for (int s_val = 0; s_val <= gx; s_val++) {
                    coeffX[s_val] = poly.evaluate(c[j][r][s_val], 0); // B=0
                }
                Ps_00[j][r] = poly.evaluate(coeffX, 0); // A=0
            }
        }

        long[] extended00 = HolonomicSequence.extend(initVals00, Ps_00, K_all, poly);
        for (int i = 0; i < K_all; i++) {
            assertEquals(s_all[0][0][i], extended00[i], "Mismatch at A=0, B=0, i=" + i);
        }
    }

    @Test
    public void testExtendWithStartingIndex() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // factorial starting from a_3 = 3! = 6
        // satisfies a_{k+1} - (k+1)*a_k = 0
        // Ps: P_1 = 1, P_0 = -k-1
        long[][] Ps = {
            {mod - 1, mod - 1}, // P_0 = -1 - k
            {1}                 // P_1 = 1
        };

        // startK = 3. initialValues: a_3 = 6 (length d = 1)
        long[] initialValues = { 6 };
        int startK = 3;
        int targetLen = 6; // elements computed: a_3, a_4, a_5, a_6, a_7, a_8
        long[] extended = HolonomicSequence.extend(initialValues, Ps, startK, targetLen, poly);

        long[] expected = { 6, 24, 120, 720, 5040, 40320 };
        for (int i = 0; i < targetLen; i++) {
            assertEquals(expected[i], extended[i], "Mismatch at index " + i);
        }
    }

    @Test
    public void testExtendWithStartingIndexParameter() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x) = k! * x^k (mod mod) starting from k=2
        // satisfies: a_{k+1}(x) - (k+1)*x * a_k(x) = 0
        // Ps: P_1 = 1, P_0 = - (k+1)*x = -k*x - x
        long[][][] c = {
            {
                {0, mod - 1}, // P_0: k^0 x^1 = -x
                {0, mod - 1}  // P_0: k^1 x^1 = -kx
            },
            {
                {1}           // P_1: k^0 x^0 = 1
            }
        };

        // For parameter x=3:
        // k=2: a_2(3) = 2! * 3^2 = 18
        // k=3: a_3(3) = 3! * 3^3 = 162
        long[] initialValues = { 18 };
        int startK = 2;
        int targetLen = 4; // computed: k=2, 3, 4, 5
        long[] extended = HolonomicSequence.extend(initialValues, c, 3L, startK, targetLen, poly);

        long[] expected = { 18, 162, 162 * 4 * 3 % mod, (162 * 4 * 3 % mod) * 5 * 3 % mod };
        for (int i = 0; i < targetLen; i++) {
            assertEquals(expected[i], extended[i], "Mismatch at index " + i);
        }
    }

    @Test
    public void testExtendWithStartingIndexTwoParameters() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x, y) = (x + y)^k
        // satisfies: a_{k+1}(x, y) - (x + y)*a_k(x, y) = 0
        // P_1 = 1
        // P_0 = -x - y
        long[][][][] Ps = new long[2][1][2][2];
        Ps[1][0][0][0] = 1;
        Ps[0][0][1][0] = mod - 1;
        Ps[0][0][0][1] = mod - 1;

        // For x=2, y=3, starting from k=4:
        // (x+y) = 5
        // a_4 = 5^4 = 625
        long[] initialValues = { 625 };
        int startK = 4;
        int targetLen = 4; // computed: k=4, 5, 6, 7
        long[] extended = HolonomicSequence.extend(initialValues, Ps, 2L, 3L, startK, targetLen, poly);

        long[] expected = { 625, 3125, 15625, 78125 };
        for (int i = 0; i < targetLen; i++) {
            assertEquals(expected[i], extended[i], "Mismatch at index " + i);
        }
    }

    @Test
    public void testExtendTwoVariablesSimple() {
        long mod = 998244353;
        PolynomialFpDynamic poly = PolynomialFpDynamic.nttFriendly(mod);

        // a_k(x, y) = (x + y)^k
        // satisfies: a_{k+1}(x, y) - (x + y)*a_k(x, y) = 0
        // P_1(k, x, y) = 1
        // P_0(k, x, y) = -x - y
        // c[j][r][s_val][t] is coefficient of k^r x^s_val y^t in P_j
        // d=1, gk=0, gx=1, gy=1
        long[][][][] Ps = new long[2][1][2][2];

        // P_1 = 1 => c[1][0][0][0] = 1
        Ps[1][0][0][0] = 1;

        // P_0 = -x - y => c[0][0][1][0] = -1 (coefficient of x^1 y^0), c[0][0][0][1] = -1 (coefficient of x^0 y^1)
        Ps[0][0][1][0] = mod - 1;
        Ps[0][0][0][1] = mod - 1;

        int X = 3;
        int Y = 3;
        int K = 5;
        long[][][] s = new long[X][Y][K];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                long base = (x + y) % mod;
                long cur = 1;
                for (int k = 0; k < K; k++) {
                    s[x][y][k] = cur;
                    cur = cur * base % mod;
                }
            }
        }

        // initial values: length d = 1
        long[][][] initVals = new long[X][Y][1];
        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                initVals[x][y][0] = s[x][y][0];
            }
        }

        for (int x = 0; x < X; x++) {
            for (int y = 0; y < Y; y++) {
                long[] extended = HolonomicSequence.extend(initVals[x][y], Ps, x, y, K, poly);
                for (int k = 0; k < K; k++) {
                    assertEquals(s[x][y][k], extended[k], "Mismatch at x=" + x + ", y=" + y + ", k=" + k);
                }
            }
        }
    }

    @Test
    public void testLaurentPolynomial2DRecurrence() {
        long mod = 998244353;
        Fp fp = new Fp(mod);

        // Generate small training data s[A][B][n] to guess the 2D relation
        // F(x, y) = x + y + 1/(xy)
        int X = 5;
        int Y = 5;
        int N = 35;
        long[][][] s = new long[X][Y][N];
        for (int A = 0; A < X; A++) {
            for (int B = 0; B < Y; B++) {
                for (int n = 0; n < N; n++) {
                    int num3 = n + 2 * A - B;
                    int nvm3 = n - A + 2 * B;
                    int nwm3 = n - A - B;
                    if (num3 % 3 != 0 || nvm3 % 3 != 0 || nwm3 % 3 != 0) {
                        s[A][B][n] = 0;
                        continue;
                    }
                    int u = num3 / 3;
                    int v = nvm3 / 3;
                    int w = nwm3 / 3;
                    if (u < 0 || v < 0 || w < 0) {
                        s[A][B][n] = 0;
                        continue;
                    }
                    long val = fp.fac(n);
                    val = val * fp.ifac(u) % mod;
                    val = val * fp.ifac(v) % mod;
                    val = val * fp.ifac(w) % mod;
                    s[A][B][n] = val;
                }
            }
        }

        PolynomialFpDynamic poly = PolynomialFpDynamic.MOD998244353;
        long[][][][] coeff = HolonomicSequence.guess(s, 3, 3, 3, 3, poly);
        org.junit.jupiter.api.Assertions.assertNotNull(coeff, "Failed to guess 2D relation");

        // Verify theoretical coefficient for P_3 (the leading coefficient)
        // P_3(k) = (k+3+2A-B)(k+3-A+2B)(k+3-A-B)
        // Coefficient of k^3 A^0 B^0 in P_3 should be 1
        long leadP3 = coeff[3][3][0][0];
        long invLead = MathUtils.modInv(leadP3, mod);

        // Normalize coeff so that coeff[3][3][0][0] = 1
        for (int i = 0; i < coeff.length; i++) {
            for (int r = 0; r < coeff[i].length; r++) {
                for (int sx = 0; sx < coeff[i][r].length; sx++) {
                    for (int ty = 0; ty < coeff[i][r][sx].length; ty++) {
                        coeff[i][r][sx][ty] = coeff[i][r][sx][ty] * invLead % mod;
                    }
                }
            }
        }

        // Expected P_3 coefficients:
        // k^3 A^0 B^0: 1
        assertEquals(1L, coeff[3][3][0][0]);
        // k^2 A^0 B^0: 9
        assertEquals(9L, coeff[3][2][0][0]);
        // k^0 A^3 B^0: 2
        assertEquals(2L, coeff[3][0][3][0]);
        // k^0 A^0 B^3: 2
        assertEquals(2L, coeff[3][0][0][3]);

        // Expected P_0 coefficients:
        // P_0 = -27(k+3)(k+2)(k+1) = -27k^3 - 162k^2 - 297k - 162
        // k^3 A^0 B^0: -27
        assertEquals((mod - 27) % mod, coeff[0][3][0][0]);
        // k^2 A^0 B^0: -162
        assertEquals((mod - 162) % mod, coeff[0][2][0][0]);

        // P_1 and P_2 should be 0
        for (int r = 0; r < coeff[1].length; r++) {
            for (int sx = 0; sx < coeff[1][r].length; sx++) {
                for (int ty = 0; ty < coeff[1][r][sx].length; ty++) {
                    assertEquals(0L, coeff[1][r][sx][ty]);
                    assertEquals(0L, coeff[2][r][sx][ty]);
                }
            }
        }

        // 3. Verify with a large A, B example (A=1200, B=1500)
        int targetA = 1200;
        int targetB = 1500;
        int startN = targetA + targetB; // 2700

        int numVerify = 15;
        long[] expected = new long[numVerify];
        for (int idx = 0; idx < numVerify; idx++) {
            int n = startN + idx;
            int num3 = n + 2 * targetA - targetB;
            int nvm3 = n - targetA + 2 * targetB;
            int nwm3 = n - targetA - targetB;
            if (num3 % 3 != 0 || nvm3 % 3 != 0 || nwm3 % 3 != 0) {
                expected[idx] = 0;
                continue;
            }
            int u = num3 / 3;
            int v = nvm3 / 3;
            int w = nwm3 / 3;
            if (u < 0 || v < 0 || w < 0) {
                expected[idx] = 0;
                continue;
            }
            long val = fp.fac(n);
            val = val * fp.ifac(u) % mod;
            val = val * fp.ifac(v) % mod;
            val = val * fp.ifac(w) % mod;
            expected[idx] = val;
        }

        long[] initial = new long[]{expected[0], expected[1], expected[2]};
        long[] extended = HolonomicSequence.extend(initial, coeff, targetA, targetB, startN, numVerify, poly);

        for (int i = 0; i < numVerify; i++) {
            assertEquals(expected[i], extended[i], "Mismatch at index " + i + " for large A, B");
        }
    }

}
