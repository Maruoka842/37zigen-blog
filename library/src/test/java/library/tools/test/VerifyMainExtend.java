package library.tools.test;

import java.util.Arrays;
import library.util.Fp;
import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;

public class VerifyMainExtend {

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

    public static void testParameters(int gridD, int len, int maxD, int maxGK, int maxGX, int maxGY, int targetA, int targetB) {
        System.out.printf("Testing: gridD=%d, len=%d, maxD=%d, maxGK=%d, maxGX=%d, maxGY=%d, targetA=%d, targetB=%d%n",
                gridD, len, maxD, maxGK, maxGX, maxGY, targetA, targetB);

        long[][][] evals = new long[gridD][gridD][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < gridD; ++j) {
                for (int k = 0; k < gridD; ++k) {
                    evals[j][k][i] = f(i, j, k);
                }
            }
        }

        long[][][][] coeff = HolonomicSequence.guess(evals, maxD, maxGK, maxGX, maxGY, PolynomialFpDynamic.MOD998244353);
        if (coeff == null) {
            System.out.println("  -> guess returned NULL!");
            return;
        }

        int orderD = coeff.length - 1;
        int gk = coeff[0].length - 1;
        int gx = coeff[0][0].length - 1;
        int gy = coeff[0][0][0].length - 1;
        System.out.printf("  -> Found recurrence relation with d=%d, gk=%d, gx=%d, gy=%d%n", orderD, gk, gx, gy);

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
                System.out.println("  -> SUCCESS! All 100 terms match!");
            } else {
                System.out.println("  -> MISMATCH at index " + mismatchIdx + " (seq=" + seq[mismatchIdx] + ", H=" + H[mismatchIdx] + ")");
            }
        } catch (Exception e) {
            System.out.println("  -> EXCEPTION during extend: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int[] targetAs = {0, 1, 2, 5, 10, 50, 100};
        int[] targetBs = {0, 1, 2, 5, 10, 50, 100};

        for (int A : targetAs) {
            for (int B : targetBs) {
                if (A > B) continue;
                System.out.println("==================================================");
                testParameters(6, 30, 4, 4, 4, 4, A, B);
            }
        }
    }
}
