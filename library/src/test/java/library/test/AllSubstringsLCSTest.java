package library.test;

import library.util.seq.AllSubstringsLCS;
import java.util.Random;

public class AllSubstringsLCSTest {
    public static void main(String[] args) {
        Random rand = new Random(42);
        int tests = 1000;
        int maxLen = 30;
        
        for (int t = 0; t < tests; t++) {
            int na = rand.nextInt(maxLen) + 1;
            int nb = rand.nextInt(maxLen) + 1;
            char[] S = new char[na];
            char[] T = new char[nb];
            for (int i = 0; i < na; i++) S[i] = (char)('a' + rand.nextInt(4));
            for (int i = 0; i < nb; i++) T[i] = (char)('a' + rand.nextInt(4));
            
            AllSubstringsLCS asLCS = new AllSubstringsLCS(S, T);
            
            for (int q = 0; q < 50; q++) {
                int i = rand.nextInt(na + 1); // prefix of S: [0, i)
                int j = rand.nextInt(nb + 1);
                int k = rand.nextInt(nb + 1);
                if (j > k) { int tmp = j; j = k; k = tmp; }
                
                int expected = naiveLCS(new String(S).substring(0, i), new String(T).substring(j, k));
                int actual = asLCS.query(i, j, k);
                
                if (expected != actual) {
                    System.out.println("WA!");
                    System.out.println("S = " + new String(S) + ", prefix = " + i);
                    System.out.println("T = " + new String(T) + ", T[" + j + ":" + k + "]");
                    System.out.println("Expected: " + expected + ", Actual: " + actual);
                    return;
                }
            }
        }
        System.out.println("Stress Test Passed!");
    }
    
    static int naiveLCS(String s, String t) {
        int n = s.length(), m = t.length();
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
