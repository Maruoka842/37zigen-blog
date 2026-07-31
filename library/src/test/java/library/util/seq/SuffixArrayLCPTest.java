package library.util.seq;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.Arrays;

public class SuffixArrayLCPTest {

    @Test
    public void testBasic() {
        char[] s = "banana".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);

        assertEquals(3, salcp.lcp(1, 3)); // "anana" and "ana" -> "ana"
        assertEquals(0, salcp.lcp(0, 1)); // "banana" and "anana" -> ""
        assertEquals(2, salcp.lcp(2, 4)); // "nana" and "na" -> "na"

        assertEquals(15, salcp.countDistinctSubstrings());
    }

    @Test
    public void testEquals() {
        char[] s = "abracadabra".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);

        assertTrue(salcp.equals(0, 4, 7, 11)); // "abra" == "abra"
        assertFalse(salcp.equals(0, 4, 1, 5));  // "abra" != "brac"
        assertTrue(salcp.equals(0, 1, 7, 8));   // "a" == "a"
        assertTrue(salcp.equals(0, 0, 1, 1));   // "" == ""

        // Edge cases at the end of the string
        int n = s.length;
        assertTrue(salcp.equals(n, n, 0, 0));
        assertTrue(salcp.equals(n, n, n, n));
        assertFalse(salcp.equals(n, n, 0, 1));
    }

    @Test
    public void testCompare() {
        char[] s = "banana".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);

        // "ana" vs "banana"
        assertTrue(salcp.compare(1, 4, 0, 6) < 0);
        // "ana" vs "anana"
        assertTrue(salcp.compare(3, 6, 1, 6) < 0);
        // "banana" vs "banana"
        assertEquals(0, salcp.compare(0, 6, 0, 6));
        // "nana" vs "na"
        assertTrue(salcp.compare(2, 6, 4, 6) > 0);

        // Edge cases with empty substrings
        int n = s.length;
        assertEquals(0, salcp.compare(n, n, 0, 0));
        assertTrue(salcp.compare(n, n, 0, 1) < 0);
        assertTrue(salcp.compare(0, 1, n, n) > 0);
    }

    @Test
    public void testCompareSuffix() {
        char[] s = "banana".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);

        // a (5) < ana (3) < anana (1) < banana (0) < na (4) < nana (2)
        assertTrue(salcp.compareSuffix(5, 3) < 0);
        assertTrue(salcp.compareSuffix(3, 1) < 0);
        assertTrue(salcp.compareSuffix(1, 0) < 0);
        assertTrue(salcp.compareSuffix(0, 4) < 0);
        assertTrue(salcp.compareSuffix(4, 2) < 0);
        assertEquals(0, salcp.compareSuffix(1, 1));

        // Edge cases with index n (empty suffix)
        int n = s.length;
        assertTrue(salcp.compareSuffix(n, 0) < 0);
        assertTrue(salcp.compareSuffix(0, n) > 0);
        assertEquals(0, salcp.compareSuffix(n, n));
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = rnd.nextInt(50) + 1;
            int[] s = new int[n];
            for (int i = 0; i < n; i++) s[i] = rnd.nextInt(5);
            SuffixArrayLCP salcp = new SuffixArrayLCP(s);

            for (int i = 0; i < 100; i++) {
                int l1 = rnd.nextInt(n + 1);
                int r1 = rnd.nextInt(n + 1);
                if (l1 > r1) { int tmp = l1; l1 = r1; r1 = tmp; }
                int l2 = rnd.nextInt(n + 1);
                int r2 = rnd.nextInt(n + 1);
                if (l2 > r2) { int tmp = l2; l2 = r2; r2 = tmp; }

                int[] sub1 = Arrays.copyOfRange(s, l1, r1);
                int[] sub2 = Arrays.copyOfRange(s, l2, r2);

                assertEquals(Arrays.equals(sub1, sub2), salcp.equals(l1, r1, l2, r2));
                assertEquals(Integer.signum(Arrays.compare(sub1, sub2)), Integer.signum(salcp.compare(l1, r1, l2, r2)), "Compare failed for " + Arrays.toString(sub1) + " and " + Arrays.toString(sub2));
            }

            for (int i = 0; i < 100; i++) {
                int p1 = rnd.nextInt(n + 1);
                int p2 = rnd.nextInt(n + 1);
                int[] suf1 = Arrays.copyOfRange(s, p1, n);
                int[] suf2 = Arrays.copyOfRange(s, p2, n);
                assertEquals(Integer.signum(Arrays.compare(suf1, suf2)), Integer.signum(salcp.compareSuffix(p1, p2)));
            }
        }
    }

    @Test
    public void testEmpty() {
        char[] s = "".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);
        assertEquals(0, salcp.countDistinctSubstrings());
        assertEquals(0, salcp.lcp(0, 0));
        assertTrue(salcp.equals(0, 0, 0, 0));
        assertEquals(0, salcp.compare(0, 0, 0, 0));
        assertEquals(0, salcp.compareSuffix(0, 0));
    }

    @Test
    public void testQueryByRank() {
        char[] s = "banana".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);
        // SA:
        // 5: a
        // 3: ana
        // 1: anana
        // 0: banana
        // 4: na
        // 2: nana

        int[] sa = salcp.suffixArray();
        // rank 0: index 5 ("a")
        // rank 1: index 3 ("ana")
        // rank 2: index 1 ("anana")
        // rank 3: index 0 ("banana")
        // rank 4: index 4 ("na")
        // rank 5: index 2 ("nana")

        assertEquals(1, salcp.lcpByRank(0, 1)); // "a" vs "ana" -> 1
        assertEquals(1, salcp.lcpByRank(0, 2)); // "a" vs "anana" -> 1
        assertEquals(3, salcp.lcpByRank(1, 2)); // "ana" vs "anana" -> 3
        assertEquals(0, salcp.lcpByRank(2, 3)); // "anana" vs "banana" -> 0
        assertEquals(2, salcp.lcpByRank(4, 5)); // "na" vs "nana" -> 2

        assertEquals(6, salcp.lcpByRank(3, 3)); // rank 3 is index 0 "banana", length 6
    }

    @Test
    public void testOccurrenceRange() {
        char[] s = "abracadabra".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);
        // "abra" is at s[0:4] and s[7:11]
        int[] range = salcp.occurrenceRange(0, 4);
        assertEquals(2, range[1] - range[0]);

        for (int r = range[0]; r < range[1]; r++) {
            int start = salcp.suffixAtRank(r);
            assertTrue(new String(s).substring(start).startsWith("abra"));
        }

        // "a"
        range = salcp.occurrenceRange(0, 1);
        assertEquals(5, range[1] - range[0]); // "a", "abra" (x2), "acadabra", "adabra"

        // Not exists
        range = salcp.occurrenceRange(0, 5); // "abrac"
        assertEquals(1, range[1] - range[0]);

        int[] none = salcp.occurrenceRange(1, 3); // "br"
        assertEquals(2, none[1] - none[0]);
    }

    @Test
    public void testGetters() {
        char[] s = "banana".toCharArray();
        SuffixArrayLCP salcp = new SuffixArrayLCP(s);
        int[] sa = salcp.suffixArray();
        for (int i = 0; i < sa.length; i++) {
            assertEquals(sa[i], salcp.suffixAtRank(i));
            assertEquals(i, salcp.rankOfSuffix(sa[i]));
        }
    }
}
