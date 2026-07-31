package library.util.seq;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class StringUtilsRemoveSubstringsTest {

    @Test
    public void testRemoveSubstrings() {
        assertArrayEquals(new String[]{"abcd"}, StringUtils.removeSubstrings(new String[]{"abc", "bc", "abcd"}));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.removeSubstrings(new String[]{"a", "b", "c"}));
        assertArrayEquals(new String[]{"ab"}, StringUtils.removeSubstrings(new String[]{"ab", "ab"}));
        assertArrayEquals(new String[]{"abc"}, StringUtils.removeSubstrings(new String[]{"abc", "ab", "bc", "b"}));
        assertArrayEquals(new String[]{"abcabc"}, StringUtils.removeSubstrings(new String[]{"abcabc", "abc"}));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.removeSubstrings(new String[]{"abc", "def"}));
        assertArrayEquals(new String[]{"banana"}, StringUtils.removeSubstrings(new String[]{"banana", "ana", "nan"}));
    }

    @Test
    public void testEmpty() {
        assertArrayEquals(new String[]{}, StringUtils.removeSubstrings(new String[]{}));
        assertArrayEquals(new String[]{}, StringUtils.removeSubstrings(null));
    }

    @Test
    public void testWithEmptyString() {
        // "" is a substring of any string.
        // If there is "a" and "", "" should be removed.
        assertArrayEquals(new String[]{"a"}, StringUtils.removeSubstrings(new String[]{"a", ""}));
        assertArrayEquals(new String[]{""}, StringUtils.removeSubstrings(new String[]{""}));
        assertArrayEquals(new String[]{""}, StringUtils.removeSubstrings(new String[]{"", ""}));
    }

    @Test
    public void testOrderPreservation() {
        assertArrayEquals(new String[]{"abcd", "efgh"}, StringUtils.removeSubstrings(new String[]{"abc", "abcd", "efg", "efgh"}));
        assertArrayEquals(new String[]{"efgh", "abcd"}, StringUtils.removeSubstrings(new String[]{"efgh", "efg", "abcd", "abc"}));
    }
}
