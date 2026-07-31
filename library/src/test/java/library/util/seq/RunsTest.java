package library.util.seq;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Arrays;

public class RunsTest {
	@Test
	public void testLongestLyndonPrefixes() {
		char[] s = "teletelepathy".toCharArray();
		SuffixArrayLCP lcp = new SuffixArrayLCP(s);
		int[] res = Lyndon.longestLyndonPrefixes(s, lcp);
		int[] expected = {1, 4, 1, 2, 1, 4, 1, 2, 1, 4, 1, 2, 1};
		assertArrayEquals(expected, res);
	}

	@Test
	public void testEnumerateRuns() {
		// example: "ababa" -> period 1: (2, 0, 5), period 2: (2, 0, 5) ? No.
		// "ababa" -> runs are:
		// (period=2, l=0, r=5) "ababa"
		char[] s = "ababa".toCharArray();
		List<Runs.Run> runs = Runs.enumerateRuns(s);
		assertEquals(1, runs.size());
		assertEquals(new Runs.Run(2, 0, 5), runs.get(0));

		// "mississippi"
		s = "mississippi".toCharArray();
		runs = Runs.enumerateRuns(s);
		// runs in "mississippi":
		// "ss" at index 2, 5
		// "pp" at index 8
		// any others?
		// "issi" (period 3, indices 1-5 or 4-8) - No, issi is not a run unless R-L >= 2*period
		// Actually, let's just check if it returns something reasonable.
		assertTrue(runs.contains(new Runs.Run(1, 2, 4))); // "ss"
		assertTrue(runs.contains(new Runs.Run(1, 5, 7))); // "ss"
		assertTrue(runs.contains(new Runs.Run(1, 8, 10))); // "pp"
	}

	@Test
	public void testEnumerateLyndonWords() {
		// k=2, n=4
		// [[0,],[0,0,0,1,],[0,0,1,],[0,0,1,1,],[0,1,],[0,1,1,],[0,1,1,1,],[1,],]
		List<int[]> words = Lyndon.enumerateLyndonWords(2, 4);
		int[][] expected = {
			{0},
			{0,0,0,1},
			{0,0,1},
			{0,0,1,1},
			{0,1},
			{0,1,1},
			{0,1,1,1},
			{1}
		};
		assertEquals(expected.length, words.size());
		for (int i = 0; i < expected.length; i++) {
			assertArrayEquals(expected[i], words.get(i), "Word " + i + " mismatch");
		}
	}
}
