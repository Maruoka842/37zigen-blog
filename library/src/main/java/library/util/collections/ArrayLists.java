package library.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntFunction;

public class ArrayLists {

	public static int[] toIntArray(ArrayList<Integer> list) {
		int[] ret = new int[list.size()];
		Arrays.setAll(ret, i->list.get(i));
		return ret;
	}
	
    public static ArrayList<Integer>[] newArrayOfIntArrayLists(int n) {
        ArrayList<Integer>[] arr = new ArrayList[n];
        Arrays.setAll(arr, i -> new ArrayList<>());
        return arr;
    }
    
    @SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> T[] sortqToArray(ArrayList<T> list, IntFunction<T[]> arrayConstructor) {
	    return list.stream().distinct().sorted().toArray(arrayConstructor);
	}
}
