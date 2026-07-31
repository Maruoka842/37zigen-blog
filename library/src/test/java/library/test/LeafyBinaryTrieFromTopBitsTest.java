package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.collections.LeafyBinaryTrieFromTopBits;

public class LeafyBinaryTrieFromTopBitsTest {
    @Test
    public void testKthSmallestRandom() {
        Random rnd = new Random(42);
        LeafyBinaryTrieFromTopBits trie = new LeafyBinaryTrieFromTopBits(16);
        ArrayList<Long> expected = new ArrayList<>();

        for (int t = 0; t < 5000; t++) {
            int op = rnd.nextInt(3);
            if (op == 0) {
                long v = rnd.nextInt(1 << 16);
                trie.add(v);
                expected.add(v);
            } else if (op == 1) {
                long v = rnd.nextInt(1 << 16);
                boolean got = trie.remove(v);
                boolean want = expected.remove(v);
                assertEquals(want, got, "remove mismatch at step=" + t + " value=" + v);
            } else {
                Collections.sort(expected);
                assertEquals(expected.size(), trie.size(), "size mismatch at step=" + t);
                for (int k = 0; k < expected.size(); k++) {
                    assertEquals(expected.get(k).longValue(), trie.kthSmallest(k),
                            "kth mismatch at step=" + t + " k=" + k);
                }
                assertThrows(AssertionError.class, () -> trie.kthSmallest(-1));
                assertThrows(AssertionError.class, () -> trie.kthSmallest(trie.size()));
            }
        }
    }

    @Test
    public void testXorKthSmallestRandom() {
        Random rnd = new Random(123);
        LeafyBinaryTrieFromTopBits trie = new LeafyBinaryTrieFromTopBits(16);
        ArrayList<Long> values = new ArrayList<>();

        for (int t = 0; t < 3000; t++) {
            int op = rnd.nextInt(3);
            if (op == 0) {
                long v = rnd.nextInt(1 << 16);
                trie.add(v);
                values.add(v);
            } else if (op == 1) {
                long v = rnd.nextInt(1 << 16);
                boolean got = trie.remove(v);
                boolean want = values.remove(v);
                assertEquals(want, got, "remove mismatch at step=" + t + " value=" + v);
            } else if (!values.isEmpty()) {
                long xorValue = rnd.nextInt(1 << 16);
                ArrayList<Long> expected = new ArrayList<>();
                for (long x : values) expected.add(x ^ xorValue);
                Collections.sort(expected);
                for (int k = 0; k < expected.size(); k++) {
                    assertEquals(expected.get(k).longValue(), trie.xorKthSmallest(xorValue, k),
                            "xorKth mismatch at step=" + t + " k=" + k + " xorValue=" + xorValue);
                }
                assertThrows(AssertionError.class, () -> trie.xorKthSmallest(xorValue, -1));
                assertThrows(AssertionError.class, () -> trie.xorKthSmallest(xorValue, trie.size()));
            }
        }
    }
}
