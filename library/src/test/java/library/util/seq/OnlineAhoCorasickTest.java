package library.util.seq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class OnlineAhoCorasickTest {

    @Test
    public void testAhoCorasickMatch() {
        AhoCorasick ac = new AhoCorasick(100);
        ac.add("ab");
        ac.add("bc");
        ac.add("abc");
        ac.add("a");
        ac.build();

        String text = "abcabc";
        int[] t = new int[text.length()];
        for (int i = 0; i < text.length(); i++) t[i] = text.charAt(i) - 'a';

        long[] result = ac.matchEach(t);
        // "ab": 2 (index 0, 3)
        // "bc": 2 (index 1, 4)
        // "abc": 2 (index 0, 3)
        // "a": 2 (index 0, 3)
        assertArrayEquals(new long[]{2, 2, 2, 2}, result);
        assertEquals(8, ac.match(t));
    }

    @Test
    public void testOnlineAhoCorasick() {
        OnlineAhoCorasick oac = new OnlineAhoCorasick();
        oac.add("ab");
        assertArrayEquals(new long[]{2}, oac.match("abcabc"));

        oac.add("bc");
        assertArrayEquals(new long[]{2, 2}, oac.match("abcabc"));

        oac.add("abc");
        assertArrayEquals(new long[]{2, 2, 2}, oac.match("abcabc"));

        oac.add("a");
        assertArrayEquals(new long[]{2, 2, 2, 2}, oac.match("abcabc"));
    }

    @Test
    public void testOverlapping() {
        OnlineAhoCorasick oac = new OnlineAhoCorasick();
        oac.add("aa");
        oac.add("aaa");
        // "aaaaa"
        // "aa": index 0, 1, 2, 3 -> 4 occurrences
        // "aaa": index 0, 1, 2 -> 3 occurrences
        assertArrayEquals(new long[]{4, 3}, oac.match("aaaaa"));
    }

    @Test
    public void testSuffix() {
        OnlineAhoCorasick oac = new OnlineAhoCorasick();
        oac.add("apple");
        oac.add("ple");
        oac.add("app");
        assertArrayEquals(new long[]{1, 1, 1}, oac.match("apple"));
    }

    @Test
    public void testMultipleBuckets() {
        OnlineAhoCorasick oac = new OnlineAhoCorasick();
        // Add 7 keywords to fill buckets (111 in binary)
        oac.add("a");
        oac.add("b");
        oac.add("c");
        oac.add("d");
        oac.add("e");
        oac.add("f");
        oac.add("g");

        long[] res = oac.match("abcdefg");
        assertArrayEquals(new long[]{1, 1, 1, 1, 1, 1, 1}, res);
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(42);
        int alphabetSize = 4;
        OnlineAhoCorasick oac = new OnlineAhoCorasick(alphabetSize, c -> c - 'a');
        List<String> keywords = new ArrayList<>();

        for (int t = 0; t < 50; t++) {
            // Add a random keyword
            int len = rnd.nextInt(5) + 1;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) sb.append((char)('a' + rnd.nextInt(alphabetSize)));
            String kw = sb.toString();
            keywords.add(kw);
            oac.add(kw);

            // Match against a random text
            int textLen = rnd.nextInt(20) + 10;
            StringBuilder textSb = new StringBuilder();
            for (int i = 0; i < textLen; i++) textSb.append((char)('a' + rnd.nextInt(alphabetSize)));
            String text = textSb.toString();

            long[] actual = oac.match(text);
            long[] expected = naiveMatch(keywords, text);
            assertArrayEquals(expected, actual, "Failed at iteration " + t + " with text: " + text);
        }
    }

    private long[] naiveMatch(List<String> keywords, String text) {
        long[] res = new long[keywords.size()];
        for (int i = 0; i < keywords.size(); i++) {
            String kw = keywords.get(i);
            int count = 0;
            for (int j = 0; j <= text.length() - kw.length(); j++) {
                if (text.substring(j, j + kw.length()).equals(kw)) {
                    count++;
                }
            }
            res[i] = count;
        }
        return res;
    }
}
