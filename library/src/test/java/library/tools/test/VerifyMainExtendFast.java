package library.tools.test;

import java.util.Arrays;
import library.util.Fp;
import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;

public class VerifyMainExtendFast {

    static long f(int i, int A, int B) {
        Fp fp = Fp.MOD998244353;
        long mod = fp.modulus();
        long ret = 0;
        for (int j = 0; j <= i; j++) {
            int x = i + A - j;
            int y = i + B - j;
            if (x < 0 || y < 0) continue;
            ret += fp.mul(fp.comb(i, j), fp.comb(i, x), fp.comb(i, y));
            ret %= mod;
        }
        return ret;
    }

    public static boolean testParams(int gridD, int len, int maxD, int maxGK, int maxGX, int maxGY, int targetA, int targetB) {
        long[][][] evals = new long[gridD][gridD][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < gridD; ++j) {
                for (int k = 0; k < gridD; ++k) {
                    evals[j][k][i] = f(i, j, k);
                }
            }
        }

        long startT = System.currentTimeMillis();
        long[][][][] coeff = HolonomicSequence.guess(evals, maxD, maxGK, maxGX, maxGY, PolynomialFpDynamic.MOD998244353);
        long elapsed = System.currentTimeMillis() - startT;

        if (coeff == null) {
            System.out.printf("[gridD=%2d, len=%2d, maxD=%d, maxGK=%d, maxGX=%d, maxGY=%d] -> targetA=%d, targetB=%d: GUESS NULL (took %dms)%n",
                    gridD, len, maxD, maxGK, maxGX, maxGY, targetA, targetB, elapsed);
            return false;
        }

        int orderD = coeff.length - 1;
        int gk = coeff[0].length - 1;
        int gx = coeff[0][0].length - 1;
        int gy = coeff[0][0][0].length - 1;

        long[] H = new long[100];
        for (int i = targetB; i < targetB + 100; i++) {
            H[i - targetB] = f(i, targetA, targetB);
        }

        int initCount = Math.max(orderD + 1, 10);
        long[] array = Arrays.copyOf(H, initCount);

        try {
            long[] seq = HolonomicSequence.extend(array, coeff, targetA, targetB, targetB, 100, PolynomialFpDynamic.MOD998244353);
            boolean ok = true;
            int mismatchIdx = -1;
            for (int i = 0; i < 100; i++) {
                if (seq[i] != H[i]) {
                    ok = false;
                    mismatchIdx = i;
                    break;
                }
            }
            if (ok) {
                System.out.printf("[gridD=%2d, len=%2d, maxD=%d, maxGK=%d, maxGX=%d, maxGY=%d] -> targetA=%d, targetB=%d: SUCCESS! (d=%d, gk=%d, gx=%d, gy=%d, took %dms)%n",
                        gridD, len, maxD, maxGK, maxGX, maxGY, targetA, targetB, orderD, gk, gx, gy, elapsed);
                return true;
            } else {
                System.out.printf("[gridD=%2d, len=%2d, maxD=%d, maxGK=%d, maxGX=%d, maxGY=%d] -> targetA=%d, targetB=%d: MISMATCH at %d (d=%d, gk=%d, gx=%d, gy=%d, took %dms)%n",
                        gridD, len, maxD, maxGK, maxGX, maxGY, targetA, targetB, mismatchIdx, orderD, gk, gx, gy, elapsed);
                return false;
            }
        } catch (Exception e) {
            System.out.printf("[gridD=%2d, len=%2d, maxD=%d, maxGK=%d, maxGX=%d, maxGY=%d] -> targetA=%d, targetB=%d: EXCEPTION %s (took %dms)%n",
                    gridD, len, maxD, maxGK, maxGX, maxGY, targetA, targetB, e.getMessage(), elapsed);
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Testing different gridD, len, and max degree configurations ===");

        int[][] configs = {
            // gridD, len, maxD, maxGK, maxGX, maxGY
            {4, 20, 2, 2, 2, 2},
            {4, 20, 3, 3, 2, 2},
            {5, 25, 3, 3, 3, 3},
            {6, 30, 3, 3, 3, 3},
            {6, 30, 4, 4, 3, 3},
            {8, 30, 4, 4, 3, 3},
            {10, 30, 3, 3, 2, 2},
            {10, 30, 4, 4, 3, 3},
        };

        int[][] targets = {
            {0, 0},
            {1, 1},
            {2, 3},
            {5, 5},
            {10, 10},
            {49, 4999}
        };

        for (int[] cfg : configs) {
            for (int[] tgt : targets) {
                testParams(cfg[0], cfg[1], cfg[2], cfg[3], cfg[4], cfg[5], tgt[0], tgt[1]);
            }
        }
    }
}
