package library.util.seq;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GreedySCSTest {

    @Test
    public void testBasic() {
        List<String> input = Arrays.asList("AB", "BC", "CD");
        String result = GreedySCS.greedySuperstring(input);
        assertEquals("ABCD", result);
    }

    @Test
    public void testOverlap() {
        List<String> input = Arrays.asList("AB", "BA");
        String result = GreedySCS.greedySuperstring(input);
        assertTrue(result.equals("ABA") || result.equals("BAB"));
    }

    @Test
    public void testDuplicateAndSubstring() {
        List<String> input = Arrays.asList("ABC", "BC", "ABC");
        String result = GreedySCS.greedySuperstring(input);
        assertEquals("ABC", result);
    }

    @Test
    public void testNoOverlapExpansion() {
        // ["ZAB", "WAB", "ABX", "ABY"]
        // Overlap "AB" exists between 4 pairs: (ZAB, ABX), (ZAB, ABY), (WAB, ABX), (WAB, ABY)
        // All have overlap length 2.
        List<String> input = Arrays.asList("ZAB", "WAB", "ABX", "ABY");
        String result = GreedySCS.greedySuperstring(input);

        // Potential results depend on which pairs are merged first.
        // e.g., ZABX and WABY -> ZABXWABY or WABYZABX etc.
        // The key is that all original strings must be contained.
        for (String s : input) {
            assertTrue(result.contains(s), "Result should contain " + s + " but was " + result);
        }
        System.out.println("Result for ZAB, WAB, ABX, ABY: " + result);
    }

    @Test
    public void testRandomSmall() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int m = rnd.nextInt(10) + 1;
            List<String> input = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int len = rnd.nextInt(5) + 1;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < len; j++) {
                    sb.append((char)('A' + rnd.nextInt(3)));
                }
                input.add(sb.toString());
            }

            String result = GreedySCS.greedySuperstring(input);
            int[] path = GreedySCS.greedyPath(input);

            // Check all inputs are contained
            for (String s : input) {
                assertTrue(result.contains(s), "Result should contain " + s + " in test " + t);
            }

            // Check path length
            // Note: path is for REDUCED strings.
            // But greedyPath API returns original IDs.
            // We should check that all original IDs that are NOT substrings of others are in the path.
            // Actually, the requirement says "successor path visits all reduced strings exactly once".
            // Since we don't know which were reduced here, we can at least check path uniqueness and input inclusion.
            Set<Integer> seenIds = new HashSet<>();
            for (int id : path) {
                assertTrue(id >= 0 && id < input.size());
                assertTrue(seenIds.add(id), "Duplicate ID in path: " + id);
            }

            // Also verify that any original string NOT in the path is a substring of the result
            for (int i = 0; i < input.size(); i++) {
                assertTrue(result.contains(input.get(i)));
            }
        }
    }

    @Test
    public void testLongStrings() {
        List<String> input = Arrays.asList(
            "ABCDE", "CDEFG", "EFGHI", "GHIJK", "IJKLM"
        );
        String result = GreedySCS.greedySuperstring(input);
        assertEquals("ABCDEFGHIJKLM", result);
    }

    @Test
    public void testTwoStrings() {
        List<String> input = Arrays.asList("A", "B");
        String result = GreedySCS.greedySuperstring(input);
        assertTrue(result.equals("AB") || result.equals("BA"));
    }
}
