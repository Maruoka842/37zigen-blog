package library.tools.test;

import java.util.Arrays;
import library.util.Fp;
import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;

public class VerifyGridSize {

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

    public static void testGrid(int gridD, int len, int targetA, int targetB) {
        System.out.printf("--- Testing gridD=%d, len=%d for targetA=%d, targetB=%d ---%n", gridD, len, targetA, targetB);

        long[][][] evals = new long[gridD][gridD][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < gridD; ++j) {
                for (int k = 0; k < gridD; ++k) {
                    evals[j][k][i] = f(i, j, k);
                }
            }
        }

        long startT = System.currentTimeMillis();
        // maxD=4, maxGK=4, maxGX=4, maxGY=4
        long[][][][] coeff = HolonomicSequence.guess(evals, 4, 4, 4, 4, PolynomialFpDynamic.MOD998244353);
        long elapsed = System.currentTimeMillis() - startT;

        if (coeff == null) {
            System.out.printf("  -> GUESS NULL (took %dms)%n", elapsed);
            return;
        }

        int orderD = coeff.length - 1;
        int gk = coeff[0].length - 1;
        int gx = coeff[0][0].length - 1;
        int gy = coeff[0][0][0].length - 1;
        System.out.printf("  -> Found recurrence: d=%d, gk=%d, gx=%d, gy=%d (took %dms)%n", orderD, gk, gx, gy, elapsed);

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
                System.out.printf("  -> SUCCESS! All 100 terms match!%n");
            } else {
                System.out.printf("  -> MISMATCH at index %d (seq=%d, H=%d)%n", mismatchIdx, seq[mismatchIdx], H[mismatchIdx]);
            }
        } catch (Exception e) {
            System.out.printf("  -> EXCEPTION during extend: %s%n", e.getMessage());
        }
    }

    public static void main(String[] args) {
        int targetA = 49;
        int targetB = 4999;

        System.out.printf("Target A=%d, B=%d%n", targetA, targetB);

        int[] gridDs = {6, 7, 8, 9, 10, 11, 12};
        for (int g : gridDs) {
            testGrid(g, 35, targetA, targetB);
        }
    }
}
