package library.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

import library.util.collections.IntArrayList;
import library.util.collections.LongArrayList;

public class ArrayListRandomTest {
    @Test
    public void randomIntArrayListTest() {
        Random rnd = new Random(42);
        IntArrayList list = new IntArrayList();
        ArrayList<Integer> expected = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            int op = rnd.nextInt(4);
            if (op == 0) {
                int v = rnd.nextInt();
                list.add(v);
                expected.add(v);
            } else if (op == 1) {
                int n = rnd.nextInt(100);
                int[] data = new int[n];
                for (int j = 0; j < n; j++) data[j] = rnd.nextInt();
                list.addAll(data);
                for (int d : data) expected.add(d);
            } else if (op == 2) {
                IntArrayList other = new IntArrayList();
                int n = rnd.nextInt(100);
                for (int j = 0; j < n; j++) {
                    int v = rnd.nextInt();
                    other.add(v);
                }
                list.addAll(other);
                for (int j = 0; j < other.size(); j++) expected.add(other.get(j));
            } else {
                int[] arr = list.toArray();
                assertEquals(expected.size(), arr.length);
                for (int j = 0; j < arr.length; j++) {
                    assertEquals(expected.get(j), arr[j]);
                }
            }
            assertEquals(expected.size(), list.size());
        }
    }

    @Test
    public void randomLongArrayListTest() {
        Random rnd = new Random(42);
        LongArrayList list = new LongArrayList();
        ArrayList<Long> expected = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            int op = rnd.nextInt(2);
            if (op == 0) {
                long v = rnd.nextLong();
                list.add(v);
                expected.add(v);
            } else {
                long[] arr = list.toArray();
                assertEquals(expected.size(), arr.length);
                for (int j = 0; j < arr.length; j++) {
                    assertEquals(expected.get(j), arr[j]);
                }
            }
            assertEquals(expected.size(), list.size());
        }
    }
}
