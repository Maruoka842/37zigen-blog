package library.test;

import library.util.seq.StringUtils;
import java.util.Arrays;
import java.util.Random;

public class LongestCommonSubstringTest {
    public static void main(String[] args) {
        testChar();
        testInt();
        System.out.println("All tests passed!");
    }

    static void testChar() {
        Random rnd = new Random(42);
        for (int t = 0; t < 1000; t++) {
            int n = rnd.nextInt(20);
            int m = rnd.nextInt(20);
            char[] s = new char[n];
            char[] t2 = new char[m];
            for (int i = 0; i < n; i++) s[i] = (char) ('a' + rnd.nextInt(3));
            for (int i = 0; i < m; i++) t2[i] = (char) ('a' + rnd.nextInt(3));

            StringUtils.LCSResult lcs = StringUtils.longestCommonSubstring(s, t2);
            char[] res = lcs.len() == 0 ? new char[0] : Arrays.copyOfRange(s, lcs.p1(), lcs.p1() + lcs.len());
            char[] expected = naive(s, t2);

            if (res.length != expected.length) {
                throw new AssertionError("Length mismatch: expected " + expected.length + " but got " + res.length + " for " + new String(s) + " and " + new String(t2));
            }
            // Since there can be multiple LCS, we check if the result is actually a common substring and has the correct length.
            if (!isSubstring(res, s) || !isSubstring(res, t2)) {
                throw new AssertionError("Not a common substring: " + new String(res));
            }
            if (lcs.len() > 0) {
                if (!Arrays.equals(res, Arrays.copyOfRange(t2, lcs.p2(), lcs.p2() + lcs.len()))) {
                    throw new AssertionError("Mismatch between p1 and p2: " + new String(res) + " vs " + new String(Arrays.copyOfRange(t2, lcs.p2(), lcs.p2() + lcs.len())));
                }
            }
        }
    }

    static void testInt() {
        Random rnd = new Random(42);
        for (int t = 0; t < 1000; t++) {
            int n = rnd.nextInt(20);
            int m = rnd.nextInt(20);
            int[] s = new int[n];
            int[] t2 = new int[m];
            for (int i = 0; i < n; i++) s[i] = rnd.nextInt(5);
            for (int i = 0; i < m; i++) t2[i] = rnd.nextInt(5);

            StringUtils.LCSResult lcs = StringUtils.longestCommonSubstring(s, t2);
            int[] res = lcs.len() == 0 ? new int[0] : Arrays.copyOfRange(s, lcs.p1(), lcs.p1() + lcs.len());
            int[] expected = naive(s, t2);

            if (res.length != expected.length) {
                throw new AssertionError("Length mismatch: expected " + expected.length + " but got " + res.length);
            }
            if (!isSubstring(res, s) || !isSubstring(res, t2)) {
                throw new AssertionError("Not a common substring");
            }
            if (lcs.len() > 0) {
                if (!Arrays.equals(res, Arrays.copyOfRange(t2, lcs.p2(), lcs.p2() + lcs.len()))) {
                    throw new AssertionError("Mismatch between p1 and p2");
                }
            }
        }
    }

    static char[] naive(char[] s, char[] t) {
        int n = s.length;
        int m = t.length;
        int maxLen = 0;
        int start = -1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int len = 0;
                while (i + len < n && j + len < m && s[i + len] == t[j + len]) {
                    len++;
                }
                if (len > maxLen) {
                    maxLen = len;
                    start = i;
                }
            }
        }
        if (start == -1) return new char[0];
        return Arrays.copyOfRange(s, start, start + maxLen);
    }

    static int[] naive(int[] s, int[] t) {
        int n = s.length;
        int m = t.length;
        int maxLen = 0;
        int start = -1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int len = 0;
                while (i + len < n && j + len < m && s[i + len] == t[j + len]) {
                    len++;
                }
                if (len > maxLen) {
                    maxLen = len;
                    start = i;
                }
            }
        }
        if (start == -1) return new int[0];
        return Arrays.copyOfRange(s, start, start + maxLen);
    }

    static boolean isSubstring(char[] sub, char[] text) {
        if (sub.length == 0) return true;
        for (int i = 0; i <= text.length - sub.length; i++) {
            boolean ok = true;
            for (int j = 0; j < sub.length; j++) {
                if (text[i + j] != sub[j]) {
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }
        return false;
    }

    static boolean isSubstring(int[] sub, int[] text) {
        if (sub.length == 0) return true;
        for (int i = 0; i <= text.length - sub.length; i++) {
            boolean ok = true;
            for (int j = 0; j < sub.length; j++) {
                if (text[i + j] != sub[j]) {
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }
        return false;
    }
}
