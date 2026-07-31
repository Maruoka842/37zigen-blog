package library.util.seq;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TrieTest {

    @Test
    public void testBasic() {
        Trie trie = new Trie(26);
        trie.add("abc".toCharArray());
        trie.add("ab".toCharArray());
        trie.add("a".toCharArray());

        assertTrue(trie.contains("abc".toCharArray()));
        assertTrue(trie.contains("ab".toCharArray()));
        assertTrue(trie.contains("a".toCharArray()));
        assertFalse(trie.contains("abcd".toCharArray()));
        assertFalse(trie.contains("b".toCharArray()));

        assertEquals(3, trie.size());
    }

    @Test
    public void testIntArray() {
        Trie trie = new Trie(10);
        trie.add(new int[]{1, 2, 3});
        trie.add(new int[]{1, 2});

        assertTrue(trie.contains(new int[]{1, 2, 3}));
        assertTrue(trie.contains(new int[]{1, 2}));
        assertFalse(trie.contains(new int[]{1}));

        assertEquals(2, trie.size());
    }

    @Test
    public void testMaxPrefixMatchLength() {
        Trie trie = new Trie(26);
        trie.add("a".toCharArray());
        trie.add("abc".toCharArray());
        trie.add("ab".toCharArray());

        assertEquals(3, trie.maxPrefixMatchLength("abcd"));
        assertEquals(2, trie.maxPrefixMatchLength("ab"));
        assertEquals(1, trie.maxPrefixMatchLength("a"));
        assertEquals(0, trie.maxPrefixMatchLength("b"));
        assertEquals(0, trie.maxPrefixMatchLength(""));
    }

    @Test
    public void testMaxPrefixMatchLengthEmpty() {
        Trie trie = new Trie(26);
        trie.add("".toCharArray());

        assertEquals(0, trie.maxPrefixMatchLength("abc"));
        assertEquals(0, trie.maxPrefixMatchLength(""));

        trie.add("abc".toCharArray());
        assertEquals(3, trie.maxPrefixMatchLength("abcd"));
        assertEquals(0, trie.maxPrefixMatchLength("b"));
    }

    @Test
    public void testMaxPrefixMatchLengthInt() {
        Trie trie = new Trie(10);
        trie.add(new int[]{1});
        trie.add(new int[]{1, 2, 3});

        assertEquals(3, trie.maxPrefixMatchLength(new int[]{1, 2, 3, 4}));
        assertEquals(1, trie.maxPrefixMatchLength(new int[]{1, 2, 4}));
        assertEquals(0, trie.maxPrefixMatchLength(new int[]{2, 1, 2, 3}));
    }
}
