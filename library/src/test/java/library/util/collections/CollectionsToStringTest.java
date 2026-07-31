package library.util.collections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import library.util.RefineSet;
import library.util.BitArray;

public class CollectionsToStringTest {

    @Test
    public void testLongArrayList() {
        LongArrayList list = new LongArrayList();
        list.add(10L);
        list.add(20L);
        String s = list.toString();
        assertTrue(s.contains("10"));
        assertTrue(s.contains("20"));
    }

    @Test
    public void testLongDeque() {
        LongDeque deque = new LongDeque();
        deque.addLast(5L);
        deque.addLast(15L);
        String s = deque.toString();
        assertTrue(s.contains("5"));
        assertTrue(s.contains("15"));
    }

    @Test
    public void testLongQueue() {
        LongQueue queue = new LongQueue();
        queue.add(3L);
        queue.add(7L);
        String s = queue.toString();
        assertTrue(s.contains("3"));
        assertTrue(s.contains("7"));
    }

    @Test
    public void testIntArrayWithNegativeIndex() {
        IntArrayWithNegativeIndex arr = new IntArrayWithNegativeIndex(5);
        arr.set(42, 2);
        String s = arr.toString();
        assertTrue(s.contains("shape"));
        assertTrue(s.contains("data"));
        assertTrue(s.contains("42"));
    }

    @Test
    public void testIntLinkedListArray() {
        IntLinkedListArray lla = new IntLinkedListArray(10);
        lla.insertSingleAfter(2, 1);
        String s = lla.toString();
        assertTrue(s.contains("list dump"));
    }

    @Test
    public void testIntTreapMultiSet() {
        IntTreapMultiSet ms = new IntTreapMultiSet();
        ms.add(10);
        ms.add(20);
        String s = ms.toString();
        assertTrue(s.contains("10"));
        assertTrue(s.contains("20"));
    }

    @Test
    public void testLongTreapMultiSet() {
        LongTreapMultiSet ms = new LongTreapMultiSet();
        ms.add(100L);
        ms.add(200L);
        String s = ms.toString();
        assertTrue(s.contains("100"));
        assertTrue(s.contains("200"));
    }

    @Test
    public void testTreeMultiSet() {
        TreeMultiSet<Integer> ms = new TreeMultiSet<>();
        ms.add(5);
        ms.add(10);
        String s = ms.toString();
        assertTrue(s.contains("5"));
        assertTrue(s.contains("10"));
    }

    @Test
    public void testFirstKMultiSet() {
        FirstKMultiSet<Integer> ms = new FirstKMultiSet<>(3, Integer::sum, x -> -x, 0);
        ms.add(5);
        ms.add(10);
        String s = ms.toString();
        assertTrue(s.contains("FirstKMultiSet"));
    }

    @Test
    public void testLastKMultiSet() {
        LastKMultiSet<Integer> ms = new LastKMultiSet<>(3, Integer::sum, x -> -x, 0);
        ms.add(5);
        ms.add(10);
        String s = ms.toString();
        assertTrue(s.contains("LastKMultiSet"));
    }

    @Test
    public void testIntPairHashMap() {
        IntPairHashMap<String> map = new IntPairHashMap<>();
        map.put(1, 2, "hello");
        String s = map.toString();
        assertTrue(s.contains("(1, 2)=hello"));
    }

    @Test
    public void testIntPairHashMultiset() {
        IntPairHashMultiset ms = new IntPairHashMultiset();
        ms.add(1, 2, 3);
        String s = ms.toString();
        assertTrue(s.contains("(1, 2):3"));
    }

    @Test
    public void testIntPairHashSet() {
        IntPairHashSet set = new IntPairHashSet();
        set.add(1, 2);
        String s = set.toString();
        assertTrue(s.contains("(1, 2)"));
    }

    @Test
    public void testLongOpenHashSet() {
        LongOpenHashSet set = new LongOpenHashSet();
        set.add(42L);
        String s = set.toString();
        assertTrue(s.contains("42"));
    }

    @Test
    public void testPersistentArray() {
        PersistentArray<String> arr = new PersistentArray<>(3, "hello");
        PersistentArray.Root r = arr.getRoot();
        String s1 = arr.toString();
        String s2 = arr.toString(r);
        assertTrue(s1.contains("PersistentArray"));
        assertTrue(s2.contains("hello, hello, hello"));
    }

    @Test
    public void testPersistentIntArray() {
        PersistentIntArray arr = new PersistentIntArray(3, 42);
        PersistentIntArray.Root r = arr.getRoot();
        String s1 = arr.toString();
        String s2 = arr.toString(r);
        assertTrue(s1.contains("PersistentIntArray"));
        assertTrue(s2.contains("42, 42, 42"));
    }

    @Test
    public void testPartiallyRetroactivePriorityQueue() {
        PartiallyRetroactivePriorityQueue pq = new PartiallyRetroactivePriorityQueue(5);
        String s = pq.toString();
        assertTrue(s.contains("PartiallyRetroactivePriorityQueue"));
        assertTrue(s.contains("deadsSum="));
    }

    @Test
    public void testSquareRootDecompositionBBST() {
        SquareRootDecompositionBBST bbst = new SquareRootDecompositionBBST(new long[]{1, 2, 3});
        String s = bbst.toString();
        assertTrue(s.contains("SquareRootDecompositionBBST"));
        assertTrue(s.contains("[1, 2, 3]"));
    }

    @Test
    public void testWeightedLeafBBST() {
        WeightedLeafBBST bbst = new WeightedLeafBBST(10);
        int r = bbst.newLeaf(42L);
        String s1 = bbst.toString();
        String s2 = bbst.toString(r);
        assertTrue(s1.contains("WeightedLeafBBST"));
        assertTrue(s2.contains("[42]"));
    }

    @Test
    public void testRefineSet() {
        RefineSet rs = new RefineSet(3);
        String s = rs.toString();
        assertTrue(s.contains("{0,1,2}"));
    }

    @Test
    public void testBitArray() {
        BitArray ba = new BitArray(10);
        ba.set(3);
        ba.build();
        String s = ba.toString();
        assertTrue(s.contains("BitArray"));
        assertTrue(s.contains("prefixSum"));
    }
}
