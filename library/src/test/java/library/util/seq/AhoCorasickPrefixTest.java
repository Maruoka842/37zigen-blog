package library.util.seq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AhoCorasickPrefixTest {

    @Test
    public void testAhoCorasickPrefixMatch() {
        AhoCorasick ac = new AhoCorasick(100);
        ac.add("a");
        ac.add("abc");
        ac.add("ab");
        ac.build();

        assertEquals(3, ac.maxPrefixMatchLength("abcd"));
        assertEquals(2, ac.maxPrefixMatchLength("ab"));
        assertEquals(1, ac.maxPrefixMatchLength("a"));
        assertEquals(-1, ac.maxPrefixMatchLength("b"));
        assertEquals(-1, ac.maxPrefixMatchLength(""));
    }

    @Test
    public void testAhoCorasickPrefixMatchNoBuild() {
        // build() before maxPrefixMatchLength should not be strictly required as it uses Trie edges
        AhoCorasick ac = new AhoCorasick(100);
        ac.add("abc");
        ac.add("a");

        assertEquals(3, ac.maxPrefixMatchLength("abcd"));
        assertEquals(1, ac.maxPrefixMatchLength("ax"));
        assertEquals(-1, ac.maxPrefixMatchLength("b"));
    }

    @Test
    public void testOnlineAhoCorasickPrefixMatch() {
        OnlineAhoCorasick oac = new OnlineAhoCorasick();
        oac.add("a");
        oac.add("abc");

        assertEquals(3, oac.maxPrefixMatchLength("abcd"));

        oac.add("abcdef");
        assertEquals(6, oac.maxPrefixMatchLength("abcdefg"));
        assertEquals(3, oac.maxPrefixMatchLength("abcde"));
        assertEquals(-1, oac.maxPrefixMatchLength("bcde"));
    }

    @Test
    public void testCharactersOutsideRange() {
        AhoCorasick ac = new AhoCorasick(100);
        ac.add("a");

        // "a" matches, then 'z'+1 is out of range
        assertEquals(1, ac.maxPrefixMatchLength(new int[]{'a' - 'a', 26}));
        // first char out of range
        assertEquals(-1, ac.maxPrefixMatchLength(new int[]{26, 'a' - 'a'}));
        // negative char
        assertEquals(-1, ac.maxPrefixMatchLength(new int[]{-1, 'a' - 'a'}));
    }

    @Test
    public void testEmptyPattern() {
        AhoCorasick ac = new AhoCorasick(100);
        ac.add("");

        assertEquals(0, ac.maxPrefixMatchLength("abc"));
        assertEquals(0, ac.maxPrefixMatchLength(""));

        ac.add("abc");
        assertEquals(3, ac.maxPrefixMatchLength("abcd"));
        assertEquals(0, ac.maxPrefixMatchLength("b"));
    }
}
