package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.LongBinaryOperator;

public class CollectionsEqualsTest {

	@Test
	public void testIntPairHashSetEqualsAndHashCode() {
		IntPairHashSet set1 = new IntPairHashSet();
		IntPairHashSet set2 = new IntPairHashSet();

		// Equals on empty sets
		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		set1.add(1, 2);
		set1.add(3, 4);

		assertNotEquals(set1, set2);

		set2.add(1, 2);
		set2.add(3, 4);

		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		// Self equality and null
		assertEquals(set1, set1);
		assertNotEquals(set1, null);
		assertNotEquals(set1, "some string");
	}

	@Test
	public void testLongOpenHashSetEqualsAndHashCode() {
		LongOpenHashSet set1 = new LongOpenHashSet();
		LongOpenHashSet set2 = new LongOpenHashSet();

		// Empty sets
		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		set1.add(0L); // containsZero = true
		set1.add(42L);

		assertNotEquals(set1, set2);

		set2.add(0L);
		set2.add(42L);

		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		set1.add(100L);
		assertNotEquals(set1, set2);

		set2.add(100L);
		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		// Self equality and null
		assertEquals(set1, set1);
		assertNotEquals(set1, null);
		assertNotEquals(set1, "different type");
	}

	@Test
	public void testOpenHashSetEqualsAndHashCode() {
		OpenHashSet<String> set1 = new OpenHashSet<>();
		OpenHashSet<String> set2 = new OpenHashSet<>();

		// Empty sets
		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		set1.add("hello");
		set1.add("world");

		assertNotEquals(set1, set2);

		set2.add("world");
		set2.add("hello");

		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		set1.add("java");
		assertNotEquals(set1, set2);

		set2.add("java");
		assertEquals(set1, set2);
		assertEquals(set1.hashCode(), set2.hashCode());

		// Self equality and null
		assertEquals(set1, set1);
		assertNotEquals(set1, null);
		assertNotEquals(set1, "not a set");
	}

	@Test
	public void testIntPairHashMapEqualsAndHashCode() {
		IntPairHashMap<String> map1 = new IntPairHashMap<>();
		IntPairHashMap<String> map2 = new IntPairHashMap<>();

		// Empty maps
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put(1, 2, "val1");
		map1.put(3, 4, "val2");

		assertNotEquals(map1, map2);

		map2.put(1, 2, "val1");
		map2.put(3, 4, "val2");

		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put(1, 2, "updated");
		assertNotEquals(map1, map2);

		map2.put(1, 2, "updated");
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		// Self equality and null
		assertEquals(map1, map1);
		assertNotEquals(map1, null);
		assertNotEquals(map1, "not a map");
	}

	@Test
	public void testOpenHashMapEqualsAndHashCode() {
		OpenHashMap<String, Integer> map1 = new OpenHashMap<>();
		OpenHashMap<String, Integer> map2 = new OpenHashMap<>();

		// Empty maps
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put("one", 1);
		map1.put("two", 2);

		assertNotEquals(map1, map2);

		map2.put("one", 1);
		map2.put("two", 2);

		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put("one", 100);
		assertNotEquals(map1, map2);

		map2.put("one", 100);
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		// Test map value being null
		OpenHashMap<String, Integer> mapWithNull1 = new OpenHashMap<>();
		OpenHashMap<String, Integer> mapWithNull2 = new OpenHashMap<>();
		mapWithNull1.put("nullKey", null);
		assertNotEquals(mapWithNull1, map1);

		mapWithNull2.put("nullKey", null);
		assertEquals(mapWithNull1, mapWithNull2);
		assertEquals(mapWithNull1.hashCode(), mapWithNull2.hashCode());

		// Self equality and null
		assertEquals(map1, map1);
		assertNotEquals(map1, null);
		assertNotEquals(map1, "other type");
	}

	@Test
	public void testTreapMapEqualsAndHashCode() {
		TreapMap map1 = new TreapMap();
		TreapMap map2 = new TreapMap();

		// Empty maps
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put(10L, 100L);
		map1.put(5L, 50L);
		map1.put(20L, 200L);

		assertNotEquals(map1, map2);

		map2.put(20L, 200L);
		map2.put(10L, 100L);
		map2.put(5L, 50L);

		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.put(10L, 999L);
		assertNotEquals(map1, map2);

		map2.put(10L, 999L);
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		// Self equality and null
		assertEquals(map1, map1);
		assertNotEquals(map1, null);
		assertNotEquals(map1, "not treap");
	}

	@Test
	public void testTrieMapEqualsAndHashCode() {
		TrieMap map1 = new TrieMap();
		TrieMap map2 = new TrieMap();

		// Empty trie maps
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.add("abc".toCharArray(), 100L);
		map1.add("def".toCharArray(), 200L);

		assertNotEquals(map1, map2);

		map2.add("abc".toCharArray(), 100L);
		map2.add("def".toCharArray(), 200L);

		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		map1.add("abc".toCharArray(), 999L);
		assertNotEquals(map1, map2);

		map2.add("abc".toCharArray(), 999L);
		assertEquals(map1, map2);
		assertEquals(map1.hashCode(), map2.hashCode());

		// Self equality and null
		assertEquals(map1, map1);
		assertNotEquals(map1, null);
		assertNotEquals(map1, "not a trie");
	}

	@Test
	public void testLeafyBinaryTrieFromTopBitsEqualsAndHashCode() {
		LeafyBinaryTrieFromTopBits trie1 = new LeafyBinaryTrieFromTopBits(30);
		LeafyBinaryTrieFromTopBits trie2 = new LeafyBinaryTrieFromTopBits(30);

		// Empty
		assertEquals(trie1, trie2);
		assertEquals(trie1.hashCode(), trie2.hashCode());

		trie1.add(5L);
		trie1.add(10L);

		assertNotEquals(trie1, trie2);

		trie2.add(5L);
		trie2.add(10L);

		assertEquals(trie1, trie2);
		assertEquals(trie1.hashCode(), trie2.hashCode());

		// Test remove / lazy deletion (subtreeHit handling)
		trie1.remove(5L);
		assertNotEquals(trie1, trie2);

		trie2.remove(5L);
		assertEquals(trie1, trie2);
		assertEquals(trie1.hashCode(), trie2.hashCode());

		// Check bitLength difference
		LeafyBinaryTrieFromTopBits trie3 = new LeafyBinaryTrieFromTopBits(15);
		assertNotEquals(trie1, trie3);

		// Self equality and null
		assertEquals(trie1, trie1);
		assertNotEquals(trie1, null);
		assertNotEquals(trie1, "not trie");
	}
}
