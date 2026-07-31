package library.test;

import library.util.seq.AllSubstringsLCS;
import java.util.Random;

public class AllSubstringsLCSStressTest {
    public static void main(String[] args) {
        testRandom();
        System.out.println("Stress test passed!");
    }

    static void testRandom() {
        Random rand = new Random(12345);
        int testCases = 100;
        int maxLen = 30;

        for (int t = 0; t < testCases; t++) {
            int na = rand.nextInt(maxLen) + 1;
            int nb = rand.nextInt(maxLen) + 1;
            String S = generateRandomString(rand, na);
            String T = generateRandomString(rand, nb);

            AllSubstringsLCS lcs = new AllSubstringsLCS(S, T);

            for (int i = 0; i <= na; i++) {
                for (int j = 0; j <= nb; j++) {
                    for (int k = j; k <= nb; k++) {
                        int expected = naiveLCS(S.substring(0, i), T.substring(j, k));
                        int actual = lcs.query(i, j, k);

                        if (expected != actual) {
                            System.err.println("Failed on:");
                            System.err.println("S = " + S);
                            System.err.println("T = " + T);
                            System.err.println("i = " + i + ", j = " + j + ", k = " + k);
                            System.err.println("S[0:i) = " + S.substring(0, i));
                            System.err.println("T[j:k) = " + T.substring(j, k));
                            System.err.println("Expected: " + expected + ", Actual: " + actual);
                            throw new RuntimeException("Mismatch found!");
                        }
                    }
                }
            }
        }
    }

    static String generateRandomString(Random rand, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) ('a' + rand.nextInt(3))); // use small alphabet to get more matches
        }
        return sb.toString();
    }

    static int naiveLCS(String s, String t) {
        int n = s.length();
        int m = t.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (s.charAt(i) == t.charAt(j)) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }
        return dp[n][m];
    }
}
