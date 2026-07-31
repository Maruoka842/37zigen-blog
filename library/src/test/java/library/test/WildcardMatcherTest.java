package library.test;

import library.util.seq.WildcardMatcher;
import java.util.Arrays;

public class WildcardMatcherTest {
    public static void main(String[] args) {
        testBasic();
        testWildcard();
        testBothWildcard();
        System.out.println("WildcardMatcher tests passed!");
    }

    private static void testBasic() {
        String text = "ababcaba";
        String pattern = "aba";
        boolean[] res = WildcardMatcher.match(text, pattern, '?');
        boolean[] expected = {true, false, false, false, false, true};
        if (!Arrays.equals(res, expected)) {
            throw new RuntimeException("Basic failed: " + Arrays.toString(res));
        }
    }

    private static void testWildcard() {
        String text = "ababcaba";
        String pattern = "a?a";
        boolean[] res = WildcardMatcher.match(text, pattern, '?');
        // indices of matches in "ababcaba" for "a?a":
        // 0: "aba" - match
        // 1: "bab" - no match
        // 2: "abc" - match (c matches ? is false, wait, ? matches c is true)
        // a?a matches:
        // text[0..2] = "aba", pattern="a?a" -> match
        // text[1..3] = "bab", pattern="a?a" -> no match (b != a)
        // text[2..4] = "abc", pattern="a?a" -> no match (c != a)
        // text[3..5] = "bca", pattern="a?a" -> no match (b != a)
        // text[4..6] = "cab", pattern="a?a" -> no match (c != a)
        // text[5..7] = "aba", pattern="a?a" -> match
        boolean[] expected = {true, false, false, false, false, true};
        if (!Arrays.equals(res, expected)) {
            throw new RuntimeException("Wildcard failed: " + Arrays.toString(res));
        }
    }

    private static void testBothWildcard() {
        String text = "a?cde";
        String pattern = "?b?";
        boolean[] res = WildcardMatcher.match(text, pattern, '?');
        // text: a?cde, pattern: ?b?
        // 0: "a?c" matches "?b?" -> a match ?, ? match b, c match ? -> true
        // 1: "?cd" matches "?b?" -> ? match ?, c match b, d match ? -> false
        // 2: "cde" matches "?b?" -> c match ?, d match b, e match ? -> false
        boolean[] expected = {true, false, false};
        if (!Arrays.equals(res, expected)) {
            throw new RuntimeException("BothWildcard failed: " + Arrays.toString(res));
        }
    }
}
