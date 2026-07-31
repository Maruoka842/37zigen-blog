package library.util;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import library.util.seq.Permutation;

public class ArrayUtils {
	
	public static boolean all(char[] a, char v) {
		for (int i = 0; i < a.length; i++) {
			if(a[i]!=v)return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(int[] a, int v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != v) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(long[] a, long v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != v) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(double[] a, double v) {
		for (int i = 0; i < a.length; i++) {
			if (Double.compare(a[i], v) != 0) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(boolean[] a, boolean v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != v) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(float[] a, float v) {
		for (int i = 0; i < a.length; i++) {
			if (Float.compare(a[i], v) != 0) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(byte[] a, byte v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != v) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定された値と等しいかを判定する。
	 * forall i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean all(short[] a, short v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != v) return false;
		}
		return true;
	}

	/**
	 * 配列のすべての要素が指定されたオブジェクトと等しいかを判定する。
	 * forall i in [0, a.length), Objects.equals(a[i], v)
	 * 計算量: O(N)
	 *
	 * @param <T> 要素の型
	 * @param a 配列
	 * @param v 比較する値
	 * @return すべての要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static <T> boolean all(T[] a, T v) {
		for (int i = 0; i < a.length; i++) {
			if (!java.util.Objects.equals(a[i], v)) return false;
		}
		return true;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(char[] a, char v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(int[] a, int v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(long[] a, long v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(double[] a, double v) {
		for (int i = 0; i < a.length; i++) {
			if (Double.compare(a[i], v) == 0) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(boolean[] a, boolean v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(float[] a, float v) {
		for (int i = 0; i < a.length; i++) {
			if (Float.compare(a[i], v) == 0) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(byte[] a, byte v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定された値と等しいかを判定する。
	 * exists i in [0, a.length), a[i] == v
	 * 計算量: O(N)
	 *
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static boolean any(short[] a, short v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return true;
		}
		return false;
	}

	/**
	 * 配列の少なくとも1つの要素が指定されたオブジェクトと等しいかを判定する。
	 * exists i in [0, a.length), Objects.equals(a[i], v)
	 * 計算量: O(N)
	 *
	 * @param <T> 要素の型
	 * @param a 配列
	 * @param v 比較する値
	 * @return 少なくとも1つの要素が等しい場合は true、それ以外は false
	 */
	// 未テスト
	public static <T> boolean any(T[] a, T v) {
		for (int i = 0; i < a.length; i++) {
			if (java.util.Objects.equals(a[i], v)) return true;
		}
		return false;
	}

	public static void decrementAll(int[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j]--;
			}
		}
	}
	
	public static void decrementAll(long[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i]--;
		}
	}
	
	public static void incrementAll(long[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i]++;
		}
	}
	
	public static void decrementAll(int[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i]--;
		}
	}
	
	public static void incrementAll(int[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i]++;
		}
	}
	
	public static <T> void swap(int i, int j, T[] A) {
		if (i == j)
			return;
		T tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
	}

	
	public static void swap(int i, int j, int[] A) {
		if (i == j)
			return;
		int tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
	}

	public static void swap(int i, int j, long[] A) {
		if (i == j)
			return;
		long tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
	}

	public static void swap(int i, int j, double[] A) {
		if (i == j)
			return;
		double tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
	}

	public static void swap(int i, int j, char[] A) {
		if (i == j)
			return;
		char tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
	}
	
	

	public static void swap(long[] A, long[] B) {
		if (A.length != B.length)
			throw new AssertionError();
		for (int i = 0; i < A.length; i++) {
			long tmp = A[i];
			A[i] = B[i];
			B[i] = tmp;
		}
	}
	
	public static void swapColumns(int i, int j, long[][] a) {
		if(i==j)return;
		for (int k = 0; k < a.length; k++) {
			var tmp = a[k][i];
			a[k][i] = a[k][j];
			a[k][j] = tmp;
		}
	}

	public static <T> void swapColumns(int i, int j, T[][] a) {
		if (i == j) return;
		for (int k = 0; k < a.length; k++) {
			T tmp = a[k][i];
			a[k][i] = a[k][j];
			a[k][j] = tmp;
		}
	}

	/**
	 * {0,1,..,n-1} から {start, start+1, .., end - 1} へのランダムな写像
	 * 
	 * @param lowerBound (inclusive)
	 * @param upperBound (exclusive)
	 * @param length
	 * @return
	 */
	public static int[] randomIntArray(int lowerBound, int upperBound, int length) {
		int[] ret = new int[length];
		Random rnd = new Random();
		Arrays.setAll(ret, i -> rnd.nextInt(lowerBound, upperBound));
		return ret;
	}
	
	
	public static int[][] randomIntTable(int lowerBound, int upperBound, int H, int W) {
		int[][] ret = new int[H][];
		for (int i = 0; i < H; i++) {
			ret[i]=randomIntArray(lowerBound, upperBound, W);
		}
		return ret;
	}
	
	public static long[][] randomLongTable(long lowerBound, long upperBound, int H, int W) {
		long[][] ret = new long[H][];
		for (int i = 0; i < H; i++) {
			ret[i]=randomLongArray(lowerBound, upperBound, W);
		}
		return ret;
	}
	
	
	/**
	 * {1,2,..,n} から {start, start+1, .., end - 1} へのランダムな写像
	 * 
	 * @param lowerBound (inclusive)
	 * @param upperBound (exclusive)
	 * @param length
	 * @return
	 */
	public static char[] randomCharArray(int length) {
		char[] ret = new char[length];
		Random rnd = new Random();
		for (int i = 0; i < ret.length; i++) {
			ret[i]=(char)('a'+rnd.nextInt(26));
		}
		return ret;
	}
	
	
	/**
	 * @param lowerBound (inclusive)
	 * @param upperBound (exclusive)
	 * @param length
	 * @return
	 */
	public static char[] randomABArray(int length) {
		char[] ret = new char[length];
		Random rnd = new Random();
		for (int i = 0; i < ret.length; i++) {
			ret[i]=(char)('a'+rnd.nextInt(2));
		}
		return ret;
	}
	
	/**
	 * {1,2,..,n} から [lower, upper) へのランダムな写像
	 * 
	 * @param lowerBound (inclusive)
	 * @param upperBound (exclusive)
	 * @param length
	 * @return
	 */
	public static double[] randomDoubleArray(double lowerBound, double upperBound, int length) {
		double[] ret = new double[length];
		Random rnd = new Random();
		Arrays.setAll(ret, i -> rnd.nextDouble(lowerBound, upperBound));
		return ret;
	}

	/**
	 * {1,2,..,n} から {start, start+1, .., end - 1} へのランダムな写像
	 * 
	 * @param start  (inclusive)
	 * @param end    (exclusive)
	 * @param length
	 * @return
	 */
	public static long[] randomLongArray(long start, long end, int length) {
		long[] ret = new long[length];
		Random rnd = new Random();
		Arrays.setAll(ret, i -> rnd.nextLong(start, end));
		return ret;
	}

	/**
	 * {start, start+1, .., end - 1}^n における辞書順の次の要素にAを変更する。
	 * 
	 * @param start  (inclusive)
	 * @param end    (exclusive)
	 * @param length
	 * @return
	 */
	public static boolean nextArray(int[] A, int start, int end) {
		int t = A.length - 1;
		while (t >= 0 && A[t] == end - 1)
			--t;
		if (t == -1)
			return false;
		A[t]++;
		for (int i = t + 1; i < A.length; ++i)
			A[i] = start;
		return true;
	}

	public static int[] range(int startInclusive, int endExclusive) {
		return IntStream.range(startInclusive, endExclusive).toArray();
	}
	
	public static boolean checkBounds(int i, int[] a) {
		return 0 <= i && i < a.length;
	}
	
	public static boolean checkBounds(int i, int j, int[][] a) {
		return 0 <= i && i < a.length && checkBounds(j, a[i]);
	}
	
	public static boolean checkBounds(int i, int j, int k, int[][][] a) {
		return 0 <= i && i < a.length && checkBounds(j, k, a[i]);
	}
	
	public static boolean checkBounds(int i, int j, int k, int l, int[][][][] a) {
		return 0 <= i && i < a.length && checkBounds(j, k, l, a[i]);
	}
	
	public static boolean checkBounds(int i, int j, int k, int l, int m, int[][][][][] a) {
		return 0 <= i && i < a.length && checkBounds(j, k, l, m, a[i]);
	}
	
	public static boolean checkBounds(int i, int j, int k, int l, int m, int n, int[][][][][][] a) {
		return 0 <= i && i < a.length && checkBounds(j, k, l, m, n, a[i]);
	}
	
   public static boolean checkBounds(int i, long[] a) {
        return 0 <= i && i < a.length;
    }

    public static boolean checkBounds(int i, int j, long[][] a) {
        return 0 <= i && i < a.length && checkBounds(j, a[i]);
    }

    public static boolean checkBounds(int i, int j, int k, long[][][] a) {
        return 0 <= i && i < a.length && checkBounds(j, k, a[i]);
    }

    public static boolean checkBounds(int i, int j, int k, int l, long[][][][] a) {
        return 0 <= i && i < a.length && checkBounds(j, k, l, a[i]);
    }

    public static boolean checkBounds(int i, int j, int k, int l, int m, long[][][][][] a) {
        return 0 <= i && i < a.length && checkBounds(j, k, l, m, a[i]);
    }

    public static boolean checkBounds(int i, int j, int k, int l, int m, int n, long[][][][][][] a) {
        return 0 <= i && i < a.length && checkBounds(j, k, l, m, n, a[i]);
    }

	public static <T> void fill(T[] a, T v) {
		Arrays.fill(a, v);
	}

	public static <T> void fill(T[][] a, T v) {
		for (int i = 0; i < a.length; i++) {
			fill(a[i],v);
		}
	}
	
	public static <T> void fill(T[][][] a, T v) {
		for (int i = 0; i < a.length; i++) {
			fill(a[i],v);
		}
	}
	
	public static <T> void fill(T[][][][] a, T v) {
		for (int i = 0; i < a.length; i++) {
			fill(a[i],v);
		}
	}
    
	
	public static void fill(int[] a, int v) {
		Arrays.fill(a, v);
	}
	
	public static void fill(int[][] a, int v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	
	public static void fill(int[][][] a, int v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	

	public static void fill(int[][][][] a, int v) {
		for (int i = 0; i < a.length; ++i) {
			fill(a[i], v);
		}
	}

	public static void fill(int[][][][][] a, int v) {
		for (int i = 0; i < a.length; ++i) {
			fill(a[i], v);
		}
	}

	public static void fill(int[][][][][][] a, int v) {
		for (int i = 0; i < a.length; ++i) {
			fill(a[i], v);
		}
	}

	public static void fill(double[][] a, double v) {
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				a[i][j] = v;
			}
		}
	}
	

	public static void fill(char[][] a, char v) {
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				a[i][j] = v;
			}
		}
	}


	public static void fill(long[] a, long v) {
		Arrays.fill(a, v);
	}

	public static void fill(long[][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}

	public static void fill(long[][][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	
	public static void fill(long[][][][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	
	public static void fill(long[][][][][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	
	public static void fill(long[][][][][][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}
	
	public static void fill(long[][][][][][][] a, long v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}


	public static void fill(double[][][] a, double v) {
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				for (int k = 0; k < a[i][j].length; ++k) {
					a[i][j][k] = v;
				}
			}
		}
	}

	public static void fill(char[][][] a, char v) {
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				for (int k = 0; k < a[i][j].length; ++k) {
					a[i][j][k] = v;
				}
			}
		}
	}
	
	public static void fill(boolean[] a, boolean v) {
		Arrays.fill(a, v);
	}
	
	public static void fill(boolean[][] a, boolean v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}

	public static void fill(boolean[][][] a, boolean v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}

	public static void fill(boolean[][][][] a, boolean v) {
		for (int i = 0; i < a.length; ++i) {
			ArrayUtils.fill(a[i], v);
		}
	}


	public static void fill(Object array, long value) {
		if (array == null)
			return;
		if (array instanceof long[]) {
			long[] arr = (long[]) array;
			for (int i = 0; i < arr.length; i++)
				arr[i] = value;
		} else {
			int len = Array.getLength(array);
			for (int i = 0; i < len; i++) {
				fill(Array.get(array, i), value);
			}
		}
	}

	public static void reverse(int[] a) {
		int s = 0;
		int t = a.length - 1;
		while (s < t) {
			swap(s, t, a);
			++s;
			--t;
		}
	}

	public static void reverse(long[] a) {
		int s = 0;
		int t = a.length - 1;
		while (s < t) {
			swap(s, t, a);
			++s;
			--t;
		}
	}

	public static void reverse(double[] a) {
		int s = 0;
		int t = a.length - 1;
		while (s < t) {
			swap(s, t, a);
			++s;
			--t;
		}
	}
	/**
	 * swap(a[i][j], a[i][n-1-j]) for all j
	 * @param a
	 */
	public static void reverseHolizontally(int[][] a) {
		for (int i = 0; i < a.length; i++) {
			ArrayUtils.reverse(a[i]);
		}
	}
	
	public static void reverseVertically(int[][] a) {
		for (int i = 0; 2 * i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                var tmp = a[i][j];
                a[i][j] = a[a.length - 1 - i][j];
                a[a.length - 1 - i][j] = tmp;
            }
        }
	}
	
	public static void reverseVertically(long[][] a) {
		for (int i = 0; 2 * i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                var tmp = a[i][j];
                a[i][j] = a[a.length - 1 - i][j];
                a[a.length - 1 - i][j] = tmp;
            }
        }
	}

	
	
	
	

	
	
	

	/**
	 * 配列を連結する
	 * 
	 * @param a
	 * @return
	 */
	public static long[] concat(long[]... a) {
		int len = 0;
		for (int i = 0; i < a.length; ++i) {
			len += a[i].length;
		}
		int src = 0;
		long[] ret = new long[len];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				ret[src + j] = a[i][j];
			}
			src += a[i].length;
		}
		return ret;
	}

	public static int[] concat(int[]... a) {
		int len = 0;
		for (int i = 0; i < a.length; ++i) {
			len += a[i].length;
		}
		int src = 0;
		int[] ret = new int[len];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				ret[src + j] = a[i][j];
			}
			src += a[i].length;
		}
		return ret;
	}

	
	
	public static String[] concat(String[]... a) {
		int len = 0;
		for (int i = 0; i < a.length; ++i) {
			len += a[i].length;
		}
		int src = 0;
		String[] ret = new String[len];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				ret[src + j] = a[i][j];
			}
			src += a[i].length;
		}
		return ret;
	}

	
	public static boolean equals(long[] a, long[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i)
			if (a[i] != b[i])
				return false;
		return true;
	}
	
	public static boolean equals(int[] a, int[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i)
			if (a[i] != b[i])
				return false;
		return true;
	}
	
	public static boolean equals(double[] a, double[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i)
			if (a[i] != b[i])
				return false;
		return true;
	}
	
	public static boolean equals(char[] a, char[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i)
			if (a[i] != b[i])
				return false;
		return true;
	}
	
	/**
	 * ?をワイルドカードとして配列が等しいか判定
	 * @param a
	 * @param b
	 * @return
	 */
    boolean equalsWithWildCard(char[] a, char[] b) {
    	if(a.length != b.length) return false;
    	for (int i = 0; i < a.length; i++) {
    		if(a[i]!='?'&&b[i]!='?'&&a[i]!=b[i])return false;
		}
    	return true;
    }

	
	public static boolean equals(boolean[] a, boolean[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i)
			if (a[i] != b[i])
				return false;
		return true;
	}

	
	public static boolean equals(int[][] a, int[][] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (!equals(a[i], b[i])) return false;
		}
		return true;
	}
	
	public static boolean equals(long[][] a, long[][] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (!equals(a[i], b[i])) return false;
		}
		return true;
	}
	
	public static boolean equals(boolean[][] a, boolean[][] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (!equals(a[i], b[i])) return false;
		}
		return true;
	}
	
	
	public static boolean equals(char[][] a, char[][] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; ++i) {
			if (!equals(a[i], b[i])) return false;
		}
		return true;
	}


	/**
	 * a.length が2冪を仮定
	 * 
	 * @param a
	 */
	public static void bitReveseOrder(long[] a) {
		int n = a.length;
		int cur = 0;
		for (int i = 0; i < n; ++i) {
			if (cur < i) {
				swap(i, cur, a);
			}
			for (int k = n / 2; k > (cur ^= k); k /= 2)
				;
		}
	}

	/**
	 * a.length が2冪を仮定
	 * 
	 * @param a
	 */
	public static void bitReveseOrder(int[] a) {
		int n = a.length;
		int cur = 0;
		for (int i = 0; i < n; ++i) {
			if (cur < i) {
				swap(i, cur, a);
			}
			for (int k = n / 2; k > (cur ^= k); k /= 2)
				;
		}
	}

	/**
	 * b[i] = a[1] + a[2] + .. + a[i]
	 * 
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[] prefixModSum(long[] a, long mod) {
		long[] b = new long[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = ((i == 0 ? 0 : b[i - 1]) + a[i]) % mod;
		}
		return b;
	}
	
	public static long[] prefixModSum(int[] a, long mod) {
		long[] b = new long[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = ((i == 0 ? 0 : b[i - 1]) + a[i]) % mod;
		}
		return b;
	}
	
	
	/**
	 * b[i] = min(a[1], a[2], .., a[i])
	 * 
	 * @param a
	 * @return
	 */
	public static long[] prefixMin(long[] a) {
		long[] b = a.clone();
		for (int i = 1; i < b.length; ++i) {
			b[i]=Math.min(b[i], b[i-1]);
		}
		return b;
	}
	
	
	/**
	 * @param a
	 * @return
	 */
	public static long[] suffixMax(long[] a) {
		long[] b = a.clone();
		for (int i = b.length-2; i>=0; --i) {
			b[i]=Math.max(b[i], b[i+1]);
		}
		return b;
	}
	
	
	/**
	 * b[i] = max_{j≠i} a[j]
	 * aの長さが1のときはLong.MIN_VALUE/3を返す。
	 * @param a
	 * @return
	 */
	public static long[] maxExceptSelf(long[] a) {
		long max=ArrayUtils.max(a);
		int cntMax=0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == max)++cntMax;
		}
		long[]ret=new long[a.length];
		Arrays.fill(ret, max);
		if (cntMax>=2) {
			return ret;
		} else {
			long secondMax = Long.MIN_VALUE/3;
			for (int i = 0; i < a.length; i++) {
				if (a[i] != max && secondMax < a[i]) {
					secondMax = a[i];
				}
			}
			for (int i = 0; i < a.length; i++) {
				if(a[i]==max)ret[i]=secondMax;
			}
			return ret;
		}
	}
	
	
	
	/**
	 * b[i] = min_{j≠i} a[j]
	 * @param a
	 * @return
	 */
	public static long[] minExceptSelf(long[] a) {
		long min = ArrayUtils.min(a);
		int cntMin = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == min) ++cntMin;
		}
		long[] ret = new long[a.length];
		Arrays.fill(ret, min);
		if (cntMin >= 2) {
			return ret;
		} else {
			long secondMin = Long.MAX_VALUE;
			for (int i = 0; i < a.length; i++) {
				if (a[i] != min && a[i] < secondMin) {
					secondMin = a[i];
				}
			}
			for (int i = 0; i < a.length; i++) {
				if (a[i] == min) ret[i] = secondMin;
			}
			return ret;
		}
	}

	/**
	 * b[i] = min_{j≠i} a[j]
	 * @param a
	 * @return
	 */
	public static int[] minExceptSelf(int[] a) {
		int min = ArrayUtils.min(a);
		int cntMin = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == min) ++cntMin;
		}
		int[] ret = new int[a.length];
		Arrays.fill(ret, min);
		if (cntMin >= 2) {
			return ret;
		} else {
			int secondMin = Integer.MAX_VALUE;
			for (int i = 0; i < a.length; i++) {
				if (a[i] != min && a[i] < secondMin) {
					secondMin = a[i];
				}
			}
			for (int i = 0; i < a.length; i++) {
				if (a[i] == min) ret[i] = secondMin;
			}
			return ret;
		}
	}

	/**
	 * 2番目に大きい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int secondMax(int[] a) {
		int max1 = Integer.MIN_VALUE;
		int max2 = Integer.MIN_VALUE;
		for (int x : a) {
			if (x > max1) {
				max2 = max1;
				max1 = x;
			} else if (x > max2) {
				max2 = x;
			}
		}
		return max2;
	}

	/**
	 * 2番目に大きい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static long secondMax(long[] a) {
		long max1 = Long.MIN_VALUE;
		long max2 = Long.MIN_VALUE;
		for (long x : a) {
			if (x > max1) {
				max2 = max1;
				max1 = x;
			} else if (x > max2) {
				max2 = x;
			}
		}
		return max2;
	}

	/**
	 * 2番目に大きい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static double secondMax(double[] a) {
		double max1 = Double.NEGATIVE_INFINITY;
		double max2 = Double.NEGATIVE_INFINITY;
		for (double x : a) {
			if (x > max1) {
				max2 = max1;
				max1 = x;
			} else if (x > max2) {
				max2 = x;
			}
		}
		return max2;
	}

	/**
	 * 2番目に小さい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int secondMin(int[] a) {
		int min1 = Integer.MAX_VALUE;
		int min2 = Integer.MAX_VALUE;
		for (int x : a) {
			if (x < min1) {
				min2 = min1;
				min1 = x;
			} else if (x < min2) {
				min2 = x;
			}
		}
		return min2;
	}

	/**
	 * 2番目に小さい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static long secondMin(long[] a) {
		long min1 = Long.MAX_VALUE;
		long min2 = Long.MAX_VALUE;
		for (long x : a) {
			if (x < min1) {
				min2 = min1;
				min1 = x;
			} else if (x < min2) {
				min2 = x;
			}
		}
		return min2;
	}

	/**
	 * 2番目に小さい値を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static double secondMin(double[] a) {
		double min1 = Double.POSITIVE_INFINITY;
		double min2 = Double.POSITIVE_INFINITY;
		for (double x : a) {
			if (x < min1) {
				min2 = min1;
				min1 = x;
			} else if (x < min2) {
				min2 = x;
			}
		}
		return min2;
	}

	
	
	
	/**
	 * b[i] = max(a[1], a[2], .., a[i])
	 * 
	 * @param a
	 * @return
	 */
	public static long[] prefixMax(long[] a) {
		long[] b = a.clone();
		for (int i = 1; i < b.length; ++i) {
			b[i]=Math.max(b[i], b[i-1]);
		}
		return b;
	}
	
	/**
	 * b[i] = max(a[1], a[2], .., a[i])
	 * 
	 * @param a
	 * @return
	 */
	public static void prefixMaxInplace(long[] a) {
		//https://atcoder.jp/contests/abc347/submissions/74038487
		for (int i = 1; i < a.length; ++i) {
			a[i]=Math.max(a[i], a[i-1]);
		}
	}
	
	public static void suffixMaxInplace(long[] a) {
		//https://atcoder.jp/contests/abc347/submissions/74038515
		for (int i = a.length-2; i >= 0; --i) {
			a[i]=Math.max(a[i], a[i+1]);
		}
	}
	
	/**
	 * 未テスト
	 * @param a
	 */
	public static void prefixMaxInplace(int[] a) {
	    for (int i = 1; i < a.length; ++i) {
	        a[i] = Math.max(a[i], a[i - 1]);
	    }
	}

	/**
	 * 未テスト
	 * @param a
	 */
	public static void suffixMaxInplace(int[] a) {
	    for (int i = a.length - 2; i >= 0; --i) {
	        a[i] = Math.max(a[i], a[i + 1]);
	    }
	}

	/**
	 * 未テスト
	 * @param a
	 */
	public static void prefixMinInplace(long[] a) {
	    for (int i = 1; i < a.length; ++i) {
	        a[i] = Math.min(a[i], a[i - 1]);
	    }
	}

	/**
	 * 未テスト
	 * @param a
	 */
	public static void suffixMinInplace(long[] a) {
	    for (int i = a.length - 2; i >= 0; --i) {
	        a[i] = Math.min(a[i], a[i + 1]);
	    }
	}

	/**
	 * 未テスト
	 * @param a
	 */
	public static void prefixMinInplace(int[] a) {
	    for (int i = 1; i < a.length; ++i) {
	        a[i] = Math.min(a[i], a[i - 1]);
	    }
	}

	/**
	 * @param a
	 */
	public static void suffixMinInplace(int[] a) {
		//https://atcoder.jp/contests/abc452/submissions/74695652
		for (int i = a.length - 2; i >= 0; --i) {
	        a[i] = Math.min(a[i], a[i + 1]);
	    }
	}

	
	/**
	 * b[i] = max(a[1], a[2], .., a[i])
	 * 
	 * @param a
	 * @return
	 */
	public static int[] prefixMax(int[] a) {
		int[] b = a.clone();
		for (int i = 1; i < b.length; ++i) {
			b[i]=Math.max(b[i], b[i-1]);
		}
		return b;
	}
	
	
	public static long[][] prefixMax(long[][] a) {
		long[][] b = copy(a);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				if(i>0)b[i][j]=Math.max(b[i][j], b[i-1][j]);
				if(j>0)b[i][j]=Math.max(b[i][j], b[i][j-1]);
			}
		}
		return b;
	}
	
	public static long[][] prefixMaxFromLeftBottom(long[][] a) {
		long[][] b = copy(a);
		for (int i = a.length-1; i >= 0; i--) {
			for (int j = 0; j < a[i].length; j++) {
				if(i+1<a.length)b[i][j]=Math.max(b[i][j], b[i+1][j]);
				if(j>0)b[i][j]=Math.max(b[i][j], b[i][j-1]);
			}
		}
		return b;
	}

	
	/**
	 * b[i] = a[1] + a[2] + .. + a[i]
	 * 
	 * @param a
	 * @return
	 */
	public static long[] prefixSum(long[] a) {
		long[] b = new long[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = (i == 0 ? 0 : b[i - 1]) + a[i];
		}
		return b;
	}
	

	public static long[] prefixSum(int[] a) {
		long[] b = new long[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = (i == 0 ? 0 : b[i - 1]) + a[i];
		}
		return b;
	}
	
	/**
	 * b[i][j] = Σ[i'≤i and j'≤j] a[i'][j']
	 * 
	 * @param a
	 * @return
	 */
	public static long[][] prefixSum(long[][] a) {
		long[][] b = new long[a.length][];
		for (int i = 0; i < b.length; ++i) {
			b[i] = ArrayUtils.prefixSum(a[i]);
		}
		for (int i = 1; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				b[i][j] += b[i-1][j];
			}
		}
		return b;
	}
	
	
	/**
	 * prefixSumの逆操作
	 * 
	 * a[i][j] = Σ[i'≤i and j'≤j] b[i'][j']
	 * 
	 * 
	 * 
	 * @param a
	 * @return
	 */
	public static long[][] prefixDiff(long[][] a) {
		//a(x,y)=b(x,y)/(1-x)(1-y)
		long[][] b = new long[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				b[i][j]=a[i][j]-(i==0?0:a[i-1][j])-(j==0?0:a[i][j-1])+(i==0||j==0?0:a[i-1][j-1]);
			}
		}
		return b;
	}
	
	
	/**
	 * b[i][j][k] = Σ[i'≤i and j'≤j and k' ≤ k] a[i'][j'][k']
	 * 
	 * @param a
	 * @return
	 */
	public static long[][][] prefixSum(long[][][] a) {
		long[][][] b = new long[a.length][][];
		for (int i = 0; i < b.length; ++i) {
			b[i] = ArrayUtils.prefixSum(a[i]);
		}
		for (int i = 1; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < a[i][j].length; k++) {
					b[i][j][k] += b[i-1][j][k];
				}
			}
		}
		return b;
	}
	

	/**
	 * b[i] = a[1] + a[2] + .. + a[i - 1]
	 * 
	 * @param a
	 * @return
	 */
	public static long[] prefixSumFromZERO(long[] a) {
		long[] b = new long[a.length + 1];
		for (int i = 1; i < b.length; ++i) {
			b[i] = b[i - 1] + a[i - 1];
		}
		return b;
	}
	
	/**
	 * b[i] = a[1] + a[2] + .. + a[i - 1]
	 * 
	 * @param a
	 * @return
	 */
	public static long[] prefixSumFromZERO(int[] a) {
		long[] b = new long[a.length + 1];
		for (int i = 1; i < b.length; ++i) {
			b[i] = b[i - 1] + a[i - 1];
		}
		return b;
	}

	public static double[] prefixSum(double[] a) {
		double[] b = new double[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = (i == 0 ? 0 : b[i - 1]) + a[i];
		}
		return b;
	}
	
	/**
	 * 長さa.length+1のsuffix sumを返す。a.length番目は0。
	 * @param a
	 * @return
	 */
	public static long[] suffixSumFromEmpty(long[] a) {
		long[] b = new long[a.length+1];
		for (int i = b.length - 2; i >= 0; --i) {
			b[i] = (i == b.length - 1 ? 0 : b[i + 1]) + a[i];
		}
		return b;
	}
	
	/**
	 * 長さa.length+1のsuffix sumを返す。a.length番目は0。
	 * @param a
	 * @return
	 */
	public static long[] suffixSumFromEmpty(int[] a) {
		long[] b = new long[a.length+1];
		for (int i = b.length - 2; i >= 0; --i) {
			b[i] = (i == b.length - 1 ? 0 : b[i + 1]) + a[i];
		}
		return b;
	}
	
	/**
	 * 長さa.length+1のsuffix sumを返す。a.length番目は0。
	 * @param a
	 * @return
	 */
	public static long[] suffixModSum(int[] a, long mod) {
		long[] b = new long[a.length+1];
		for (int i = b.length - 2; i >= 0; --i) {
			b[i] = (i == b.length - 1 ? 0 : b[i + 1]) + a[i];
			b[i]%=mod;
		}
		return b;
	}

	public static boolean[] prefixAnd(boolean[] a) {
		boolean[] b = new boolean[a.length];
		for (int i = 0; i < b.length; ++i) {
			b[i] = (i == 0 ? true : b[i - 1]) && a[i];
		}
		return b;
	}
	
	public static boolean[] prefixAndFromEmpty(boolean[] a) {
		//https://atcoder.jp/contests/abc386/submissions/74419228
		boolean[] b = new boolean[a.length + 1];
		b[0] = true;
		for (int i = 0; i < a.length; ++i) {
			b[i + 1] = b[i] && a[i];
		}
		return b;
	}
	
	public static boolean[] suffixAnd(boolean[] a) {
		boolean[] b = new boolean[a.length];
		for (int i = b.length - 1; i >= 0; --i) {
			b[i] = (i == a.length - 1 ? true : b[i + 1]) && a[i];
		}
		return b;
	}
	
	public static boolean[] suffixAndFromEmpty(boolean[] a) {
		//https://atcoder.jp/contests/abc386/submissions/74419228
		boolean[] b = new boolean[a.length + 1];
		b[b.length - 1] = true;
		for (int i = a.length - 1; i >= 0; --i) {
			b[i] = b[i + 1] && a[i];
		}
		return b;
	}
	
	/**
	 * a[i]=b[0]+b[1]+..+b[i]が与えられたき
	 * b[i]=a[i]-a[i-1]
	 * となるbを返す。b.length=a.length。
	 * @param a
	 * @return
	 */
	public static long[] prefixDiff(int[] a) {
		long[]b=new long[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			b[i]=a[i]-(i==0?0:a[i-1]);
		}
		return b;
	}
	
	/**
	 * a[i]=b[0]+b[1]+..+b[i]が与えられたき
	 * b[i]=a[i]-a[i-1]
	 * となる長さa.lengthのbを返す。ただしa[-1]=0
	 * @param a
	 * @return
	 */
	public static long[] prefixDiff(long[] a) {
		long[]b=new long[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			b[i]=a[i]-(i==0?0:a[i-1]);
		}
		return b;
	}
	
	/**
	 * a[i]=b[0] xor b[1] xor ..xor b[i]が与えられたき
	 * b[i]=a[i] xor a[i-1]
	 * となるbを返す。
	 * @param b
	 * @return
	 */
	public static int[] xordiff(int[] a) {
		int[]b=new int[a.length];
		for (int i = b.length - 1; i >= 0; i--) {
			b[i]=a[i]^(i==0?0:a[i-1]);
		}
		return b;
	}
	
	public static long[][] copyOf(long[][] a, int length0, int length1) {
		long[][]b=new long[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}
	
	public static long[][][] copyOf(long[][][] a, int length0, int length1, int length2) {
		long[][][]b=new long[length0][length1][length2];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				for (int k = 0; k < Math.min(a[i][j].length, length2); ++k) {
					b[i][j][k]=a[i][j][k];
				}
			}
		}
		return b;
	}
	
	public static long[][][][] copyOf(long[][][][] a, int length0, int length1, int length2, int length3) {
		long[][][][]b=new long[length0][length1][length2][length3];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				for (int k = 0; k < Math.min(a[i][j].length, length2); ++k) {
					for (int l = 0; l < Math.min(a[i][j][k].length, length3); ++l) {
						b[i][j][k][l]=a[i][j][k][l];
					}
				}
			}
		}
		return b;
	}
	
	public static int[][] copyOf(int[][] a, int length0, int length1) {
		int[][]b=new int[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static short[][] copyOf(short[][] a, int length0, int length1) {
		short[][]b=new short[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static byte[][] copyOf(byte[][] a, int length0, int length1) {
		byte[][]b=new byte[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static char[][] copyOf(char[][] a, int length0, int length1) {
		char[][]b=new char[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static boolean[][] copyOf(boolean[][] a, int length0, int length1) {
		boolean[][]b=new boolean[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static float[][] copyOf(float[][] a, int length0, int length1) {
		float[][]b=new float[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}

	public static double[][] copyOf(double[][] a, int length0, int length1) {
		double[][]b=new double[length0][length1];
		for (int i = 0; i < Math.min(a.length, length0); i++) {
			for (int j = 0; j < Math.min(a[i].length, length1); j++) {
				b[i][j]=a[i][j];
			}
		}
		return b;
	}
	
	public static int[][] copyOfRange2D(int[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    int[][] b = new int[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static long[][] copyOfRange2D(long[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    long[][] b = new long[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static short[][] copyOfRange2D(short[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    short[][] b = new short[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static byte[][] copyOfRange2D(byte[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    byte[][] b = new byte[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static char[][] copyOfRange2D(char[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
		//https://atcoder.jp/contests/abc455/submissions/75278963
	    char[][] b = new char[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static boolean[][] copyOfRange2D(boolean[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    boolean[][] b = new boolean[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static float[][] copyOfRange2D(float[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    float[][] b = new float[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static double[][] copyOfRange2D(double[][] a, int rowFrom, int colFrom, int rowTo, int colTo) {
	    double[][] b = new double[rowTo - rowFrom][colTo - colFrom];
	    for (int i = rowFrom; i < rowTo; i++) {
	    	for (int j = colFrom; j < colTo; ++j) {
	    		b[i - rowFrom][j - colFrom] = a[i][j];
	    	}
	    }
	    return b;
	}

	public static int[] copy(int[] a) {
		return Arrays.copyOf(a, a.length);
	}

	public static long[] copy(long[] a) {
		return Arrays.copyOf(a, a.length);
	}
	
	public static char[] copy(char[] cs) {
		return Arrays.copyOf(cs, cs.length);
	}
	
	public static boolean[] copy(boolean[] a) {
		return Arrays.copyOf(a, a.length);
	}
	
	public static boolean[][] copy(boolean[][] a) {
		boolean[][] b=new boolean[a.length][];
		for (int i = 0; i < a.length; i++) {
			b[i]=ArrayUtils.copy(a[i]);
		}
		return b;
	}
	
	public static long[] tolong(int[] a) {
		long[] ret=new long[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[i]=a[i];
		}
		return ret;
	}
	
	public static long[][] tolong(int[][] a) {
		long[][] ret=new long[a.length][];
		for (int i = 0; i < a.length; i++) {
			ret[i] = tolong(a[i]);
		}
		return ret;
	}

	public static int[][] copy(int[][] a) {
		int[][] b = new int[a.length][];
		Arrays.setAll(b, i -> Arrays.copyOf(a[i], a[i].length));
		return b;
	}

	public static char[][] copy(char[][] a) {
		char[][] b = new char[a.length][];
		Arrays.setAll(b, i -> Arrays.copyOf(a[i], a[i].length));
		return b;
	}

	public static long[][] copy(long[][] a) {
		long[][] b = new long[a.length][];
		Arrays.setAll(b, i -> Arrays.copyOf(a[i], a[i].length));
		return b;
	}
	
	public static long[][][] copy(long[][][] a) {
		long[][][] b = new long[a.length][][];
		for (int i = 0; i < b.length; i++) {
			b[i]=ArrayUtils.copy(a[i]);	
		}
		return b;
	}
	
	public static long[][][][] copy(long[][][][] a) {
		long[][][][] b = new long[a.length][][][];
		for (int i = 0; i < b.length; i++) {
			b[i]=ArrayUtils.copy(a[i]);	
		}
		return b;
	}

	public static char[][] rotateRightJagged(char[][] a, char pad) {
		//https://atcoder.jp/contests/abc366/submissions/73573787
		int N=a.length;
        int len = 0;
        for (int i = 0; i < N; i++) {
        	len = Math.max(len, a[i].length);
        }
        char[][] ret = new char[len][N];
        ArrayUtils.fill(ret, pad);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < a[i].length; j++) {
                ret[j][(N - 1) - i] = a[i][j];
            }
        }
        for (int i = 0; i < ret.length; i++) {
          int last=ret[i].length-1;
          while(ret[i][last]==pad)last--;
          ret[i]=Arrays.copyOf(ret[i], last+1);
        }
        return ret;
	}
	
	public static char[][] rotateRightGrid(char[][] a) {
		char[][] b = new char[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][a.length - 1 - i] = a[i][j];
			}
		}
		return b;
	}

	public static int[][] rotateRightGrid(int[][] a) {
		int[][] b = new int[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][a.length - 1 - i] = a[i][j];
			}
		}
		return b;
	}
	
	public static long[][] rightRotateGrid(long[][] a) {
		long[][] b = new long[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][a.length - 1 - i] = a[i][j];
			}
		}
		return b;
	}

	/**
	 * 未テスト
	 *
	 * 2次元配列を反時計回りに90度回転した2次元配列を返します。
	 * 長さ H, W の2次元配列 a に対し、戻り値の配列 b は W x H の2次元配列であり、
	 * すべての 0 <= i < H, 0 <= j < W に対し b[W - 1 - j][i] = a[i][j] が成り立ちます。
	 *
	 * 計算量: O(HW)
	 *
	 * @param a 反時計回りに回転させる2次元配列
	 * @return 反時計回りに90度回転した2次元配列
	 */
	public static char[][] rotateLeftGrid(char[][] a) {
		char[][] b = new char[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[a[i].length - 1 - j][i] = a[i][j];
			}
		}
		return b;
	}

	/**
	 * 未テスト
	 *
	 * 2次元配列を反時計回りに90度回転した2次元配列を返します。
	 * 長さ H, W の2次元配列 a に対し、戻り値の配列 b は W x H の2次元配列であり、
	 * すべての 0 <= i < H, 0 <= j < W に対し b[W - 1 - j][i] = a[i][j] が成り立ちます。
	 *
	 * 計算量: O(HW)
	 *
	 * @param a 反時計回りに回転させる2次元配列
	 * @return 反時計回りに90度回転した2次元配列
	 */
	public static int[][] rotateLeftGrid(int[][] a) {
		int[][] b = new int[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[a[i].length - 1 - j][i] = a[i][j];
			}
		}
		return b;
	}

	/**
	 * 未テスト
	 *
	 * 2次元配列を反時計回りに90度回転した2次元配列を返します。
	 * 長さ H, W の2次元配列 a に対し、戻り値の配列 b は W x H の2次元配列であり、
	 * すべての 0 <= i < H, 0 <= j < W に対し b[W - 1 - j][i] = a[i][j] が成り立ちます。
	 *
	 * 計算量: O(HW)
	 *
	 * @param a 反時計回りに回転させる2次元配列
	 * @return 反時計回りに90度回転した2次元配列
	 */
	public static long[][] rotateLeftGrid(long[][] a) {
		long[][] b = new long[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[a[i].length - 1 - j][i] = a[i][j];
			}
		}
		return b;
	}
	
	public static void rotate180(long[][] a) {
	    int n = a.length;
	    int m = a[0].length;
	    for (int i = 0; i < (n + 1) / 2; i++) {
	        for (int j = 0; j < m; j++) {
	            int ni = n - 1 - i;
	            int nj = m - 1 - j;
	            if (i < ni || (i == ni && j < nj)) {
	            	var tmp = a[i][j];
	            	a[i][j] = a[ni][nj];
	            	a[ni][nj] = tmp;
	            }
	        }
	    }
	}
	
	public static void rotate180(int[][] a) {
	    int n = a.length;
	    int m = a[0].length;
	    for (int i = 0; i < (n + 1) / 2; i++) {
	        for (int j = 0; j < m; j++) {
	            int ni = n - 1 - i;
	            int nj = m - 1 - j;
	            if (i < ni || (i == ni && j < nj)) {
	            	var tmp = a[i][j];
	            	a[i][j] = a[ni][nj];
	            	a[ni][nj] = tmp;
	            }
	        }
	    }
	}
	
	public static void rotate180(char[][] a) {
		//https://atcoder.jp/contests/abc455/submissions/75278963
	    int n = a.length;
	    int m = a[0].length;
	    for (int i = 0; i < (n + 1) / 2; i++) {
	        for (int j = 0; j < m; j++) {
	            int ni = n - 1 - i;
	            int nj = m - 1 - j;
	            if (i < ni || (i == ni && j < nj)) {
	            	var tmp = a[i][j];
	            	a[i][j] = a[ni][nj];
	            	a[ni][nj] = tmp;
	            }
	        }
	    }
	}
	
	public static double sum(double[] a) {
		double ret = 0;
		for (double val : a)
			ret += val;
		return ret;
	}

	
	public static long sum(int[] a) {
		long ret = 0;
		for (int val : a)
			ret += val;
		return ret;
	}
	
	public static long sum(int[][] a) {
		long ret = 0;
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				ret+=a[i][j];
			}
		}
		return ret;
	}

	public static long sum(long[] a) {
		long ret = 0;
		for (long val : a)
			ret += val;
		return ret;
	}
	
	public static long sum(List<? extends Number> list) {
	    long ret = 0;
	    for (Number val : list) {
	        ret += val.longValue();
	    }
	    return ret;
	}
	
	public static long modSum(long[] a, long mod) {
		Fp mo=new Fp(mod);
		long ret = 0;
		for (long val : a) {
			ret += val;
			ret=mo.reduce(ret);
		}
		return ret;
	}
	
	
	public static long modSum(long[][] a, long mod) {
		Fp mo=new Fp(mod);
		long ret = 0;
		for (long[] vals : a) {
			ret += modSum(vals, mod);
			ret=mo.reduce(ret);
		}
		return ret;
	}
	
	public static long modSum(long[][][] a, long mod) {
		Fp mo=new Fp(mod);
		long ret = 0;
		for (var vals : a) {
			ret += modSum(vals, mod);
			ret=mo.reduce(ret);
		}
		return ret;
	}

	public static long product(long[] a) {
		long ret = 1;
		for (long val : a)
			ret *= val;
		return ret;
	}
	
	public static long product(int[] a) {
		long ret = 1;
		for (long val : a)
			ret *= val;
		return ret;
	}

	
	public static long bitand(long[] a) {
		long ret = Longs.bitmask(64);
		for (long val : a)
			ret&=val;
		return ret;
	}
	
	public static long bitor(long[] a) {
		long ret = 0;
		for (long val : a)
			ret|=val;
		return ret;
	}
	
	/**
	 * 配列を連結する
	 * 
	 * @param a
	 * @return
	 */
	public static char[] concat(char[]... a) {
		int len = 0;
		for (int i = 0; i < a.length; ++i) {
			len += a[i].length;
		}
		int src = 0;
		char[] ret = new char[len];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				ret[src + j] = a[i][j];
			}
			src += a[i].length;
		}
		return ret;
	}

	public static void reverse(char[] z) {
		int s = 0;
		int t = z.length - 1;
		while (s < t) {
			ArrayUtils.swap(s, t, z);
			++s;
			--t;
		}
	}
	

	public static <T> void reverse(T[] z) {
		int s = 0;
		int t = z.length - 1;
		while (s < t) {
			ArrayUtils.swap(s, t, z);
			++s;
			--t;
		}
	}


	/***
	 * min以上max未満の整数からなる配列aを0以上の整数に送る全単射な関数。 
	 * 長さも覚えている。aの辞書順に0,1,2,..を割り当てている。0は空文字列。
	 * https://atcoder.jp/contests/abc305/submissions/72134375
	 * @param minInclusive
	 * @param maxInclusive
	 * @param a
	 * @return
	 */
	public static long lexrank(int minInclusive, int maxExclusive, int[] a) {
		// r=maxExclusive-minInclusive と置くと
		// 長さ n=a.length未満（0含む）の数はk=1+r+..+r^{n-1}=(1-r^n)/(1-r)
		long r=maxExclusive-minInclusive;
		int n=a.length;
		long k=(MathUtils.pow(r, n)-1)/(r-1);
		long sum = 0;
		for (int i = 0; i < a.length; ++i) {
			sum *= r;
			sum += a[i] - minInclusive;
		}
		return sum+k;
	}
	
	
	
	/***
	 * min以上max未満の整数からなる配列aを0以上の整数に送る全単射な関数。 
	 * 長さも覚えている。aの辞書順に0,1,2,..を割り当てている。0は空文字列。
	 * 
	 * @param minInclusive
	 * @param maxInclusive
	 * @param a
	 * @return
	 */
	public static long lexrank(long minInclusive, long maxExclusive, long[] a) {
		// r=maxExclusive-minInclusive と置くと
		// 長さ n=a.length未満（0含む）の数はk=1+r+..+r^{n-1}=(1-r^n)/(1-r)
		long r=maxExclusive-minInclusive;
		int n=a.length;
		long k=(MathUtils.pow(r, n)-1)/(r-1);
		long sum = 0;
		for (int i = 0; i < a.length; ++i) {
			sum *= r;
			sum += a[i] - minInclusive;
		}
		return sum+k;
	}

	public static long rollingHash(int radix, int[] a) {
		long sum = 0;
		for (int i = 0; i < a.length; ++i) {
			sum *= radix;
			sum += a[i];
		}
		return sum;
	}
	
	public static int[] unrollingHash(long hash, int radix) {
		if(hash<=0)throw new AssertionError();
		ArrayList<Integer>ret=new ArrayList<>();
		while(hash!=0) {
			ret.add((int)(hash%radix));
			hash/=radix;
		}
		Collections.reverse(ret);
		return ret.stream().mapToInt(Integer::intValue).toArray();
	}
	
	public static int[] unrollingHash(long hash, int radix, int length) {
		if(hash<0)throw new AssertionError();
		ArrayList<Integer>ret=new ArrayList<>();
		for (int i=0;i<length; ++i) {
			ret.add((int)(hash%radix));
			hash/=radix;
		}
		Collections.reverse(ret);
		return ret.stream().mapToInt(Integer::intValue).toArray();
	}
	

	public static int[] subSequence(int[] a, int subset) {
		int[] ret = new int[Integer.bitCount(subset)];
		int idx = 0;
		for (int i = 0; i < a.length; ++i) {
			if ((subset >> i) % 2 == 1) {
				ret[idx++] = a[i];
			}
		}
		return ret;
	}

	public static long[] subSequence(long[] a, int subset) {
		long[] ret = new long[Integer.bitCount(subset)];
		int idx = 0;
		for (int i = 0; i < a.length; ++i) {
			if ((subset >> i) % 2 == 1) {
				ret[idx++] = a[i];
			}
		}
		return ret;
	}

	public static double[] subSequence(double[] a, int subset) {
		double[] ret = new double[Integer.bitCount(subset)];
		int idx = 0;
		for (int i = 0; i < a.length; ++i) {
			if ((subset >> i) % 2 == 1) {
				ret[idx++] = a[i];
			}
		}
		return ret;
	}

	public static char[] subSequence(char[] a, int subset) {
		char[] ret = new char[Integer.bitCount(subset)];
		int idx = 0;
		for (int i = 0; i < a.length; ++i) {
			if ((subset >> i) % 2 == 1) {
				ret[idx++] = a[i];
			}
		}
		return ret;
	}
	
	public static void sort(int[] a, Comparator<Integer> cmp) {
        Integer[] A = new Integer[a.length];
        for (int i = 0; i < a.length; i++) {
			A[i]=a[i];
		}
        Arrays.sort(A, cmp);
        for (int i = 0; i < a.length; i++) a[i] = A[i];
        return;
	}	
	
	
	public static void sort(long[] a, Comparator<Long> cmp) {
        Long[] A = new Long[a.length];
        for (int i = 0; i < a.length; i++) {
			A[i]=a[i];
		}
        Arrays.sort(A, cmp);
        for (int i = 0; i < a.length; i++) a[i] = A[i];
        return;
	}

	
	/***
	 * a[i] = max a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMax(int[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}
	
	/***
	 * a[i] = max a となるiのリストを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int[] argMaxes(int[] a) {
		int[] ret = new int[a.length];
		int size = 0;
		int max=max(a);
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == max) ret[size++]=i;
		}
		return Arrays.copyOf(ret, size);
	}
	
	
	/***
	 * a[i] = min a となる最大のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int lastArgMin(int[] a) {
		int ret = a.length-1;
		for (int i = a.length-1; i >= 0; --i) {
			if (a[i] < a[ret])
				ret = i;
		}
		return ret;
	}
	
	
	
	/***
	 * a[i] = max a となる最大のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int lastArgMax(int[] a) {
		int ret = a.length-1;
		for (int i = a.length-1; i >= 0; --i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}
	
	
	/***
	 * a[i] = max a となる最大のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int lastArgMax(long[] a) {
		int ret = a.length-1;
		for (int i = a.length-1; i >= 0; --i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}
	
	
	/***
	 * a[i] = max a となる最大のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int lastArgMax(double[] a) {
		int ret = a.length-1;
		for (int i = a.length-1; i >= 0; --i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}
	
	/***
	 * a[i] = max a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMax(long[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}

	/***
	 * a[i] = max a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMax(double[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > a[ret])
				ret = i;
		}
		return ret;
	}

	/***
	 * a[i] = min a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMin(int[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < a[ret])
				ret = i;
		}
		return ret;
	}

	/***
	 * a[i] = min a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMin(long[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < a[ret])
				ret = i;
		}
		return ret;
	}

	/***
	 * a[i] = min a となる最小のiを返す。
	 * 
	 * @param a
	 * @return
	 */
	public static int argMin(double[] a) {
		int ret = 0;
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < a[ret])
				ret = i;
		}
		return ret;
	}




	/***
	 * while (s <= t && f.test(s)) ++s; return s;
	 * 
	 * @param i
	 * @param indices
	 * @param f
	 * @return
	 */
	public static int maxIncrement(int s, int t, Predicate<Integer> f) {
		while (s <= t && f.test(s))
			++s;
		return s;
	}

	/***
	 * [s, t] while (t >= s && f.test(t)) --t;
	 * 
	 * @param i
	 * @param indices
	 * @param f
	 * @return
	 */
	public static int maxDecrement(int s, int t, Predicate<Integer> f) {
		while (t >= s && f.test(t)) {
			--t;
		}
		return t;
	}

	/***
	 * b = [a[shift], a[shift+1],.., a[a.length-1], a[0], ..a[shift-1]] となる配列を返す。
	 * shift < 0 でも動く
	 * 
	 * @param shift
	 * @param a
	 * @return
	 */
	public static int[] rotateLeft(int[] a, int shift) {
		shift = shift % a.length;
		if (shift < a.length)
			shift += a.length;
		int[] b = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = a[(i + shift) % a.length];
		}
		return b;
	}

	
	
	/***
	 * b = [a[shift], a[shift+1],.., a[a.length-1], a[0], ..a[shift-1]] となる配列を返す。
	 * shift < 0 でも動く
	 * 
	 * @param shift
	 * @param a
	 * @return
	 */
	public static long[] rotateLeft(long[] a, int shift) {
		shift = shift % a.length;
		if (shift < a.length)
			shift += a.length;
		long[] b = new long[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = a[(i + shift) % a.length];
		}
		return b;
	}
	
	
	/***
	 * rotateLeft(a, -shift)
	 * 
	 * @param shift
	 * @param a
	 * @return
	 */
	public static int[] rotateRight(int[] a, int shift) {
		return rotateLeft(a, -shift);
	}
	
	/***
	 * b = [a[shift], a[shift+1],.., a[a.length-1], a[0], ..a[shift-1]] となる配列を返す。
	 * shift < 0 でも動く
	 * 
	 * @param shift
	 * @param a
	 * @return
	 */
	public static char[] rotateLeft(char[] a, int shift) {
		shift = shift % a.length;
		if (shift < a.length)
			shift += a.length;
		char[] b = new char[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = a[(i + shift) % a.length];
		}
		return b;
	}
	
	public static double max(double[] a) {
		double ret = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, a[i]);
		}
		return ret;
	}
	
	/**
	 * 空のときはInteger.MAX_VALUE
	 * @param a
	 * @return
	 */
	public static int min(int[] a) {
		int ret = Integer.MAX_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.min(ret, a[i]);
		}
		return ret;
	}

	public static long max(long[] a) {
		long ret = Long.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, a[i]);
		}
		return ret;
	}

	public static long max(long[][] a) {
		long ret = Long.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}

	public static int max(int[][] a) {
		int ret = Integer.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}

	public static int max(int[][][] a) {
		int ret = Integer.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}

	public static int max(int[][][][] a) {
		int ret = Integer.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}
	

	public static long max(long[][][] a) {
		long ret = Long.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}

	public static long max(long[][][][] a) {
		long ret = Long.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, max(a[i]));
		}
		return ret;
	}

	public static int max(int... a) {
		int ret = Integer.MIN_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.max(ret, a[i]);
		}
		return ret;
	}

	public static long min(long... a) {
		long ret = Long.MAX_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.min(ret, a[i]);
		}
		return ret;
	}

	
	public static long min(long[][] a) {
		long ret = Long.MAX_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.min(ret, min(a[i]));
		}
		return ret;
	}

	public static int min(int[][] a) {
		int ret = Integer.MAX_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.min(ret, min(a[i]));
		}
		return ret;
	}
	
	public static int min(int[][][] a) {
		int ret = Integer.MAX_VALUE;
		for (int i = 0; i < a.length; ++i) {
			ret = Math.min(ret, min(a[i]));
		}
		return ret;
	}


	/**
	 * (a[i], b[i])の辞書順でソート
	 * @param a
	 * @param b
	 */
	public static void sort(int[] a, int[] b) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, (i,j)->Arrays.compare(new int[] {a[i],b[i]}, new int[]{a[j], b[j]}));
		int[] na = new int[a.length];
		int[] nb = new int[b.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
		}
	}
	
	
	/**
	 * (a[i], b[i], c[i], d[i])の辞書順でソート
	 * @param a
	 * @param b
	 */
	public static void sort(int[] a, int[] b, int[] c, int[] d) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, (i,j)->Arrays.compare(new int[] {a[i], b[i], c[i], d[i]}, new int[]{a[j], b[j], c[j], d[j]}));
		int[] na = new int[a.length];
		int[] nb = new int[b.length];
		int[] nc = new int[c.length];
		int[] nd = new int[d.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
			nc[i] = c[id[i]];
			nd[i] = d[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
			c[i] = nc[i];
			d[i] = nd[i];
		}
	}
	
	/**
	 * (a[i], b[i], c[i])の辞書順でソート
	 * @param a
	 * @param b
	 */
	public static void sort(int[] a, int[] b, int[] c) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, (i,j)->Arrays.compare(new int[] {a[i], b[i], c[i]}, new int[]{a[j], b[j], c[j]}));
		int[] na = new int[a.length];
		int[] nb = new int[b.length];
		int[] nc = new int[c.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
			nc[i] = c[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
			c[i] = nc[i];
		}
	}

	
	/**
	 * (a[i], b[i])の辞書順でソート
	 * @param a
	 * @param b
	 */
	void _sort(long[] a, long[] b) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, (i,j)->Arrays.compare(new long[] {a[i],b[i]}, new long[]{a[j], b[j]}));
		long[] na = new long[a.length];
		long[] nb = new long[b.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
		}
	}
	
	/**
	 */
	public static void sort(long[] a, long[] b) {
		//https://atcoder.jp/contests/abc344/submissions/72456625
		//Arrays.sort(id, (i,j)->Arrays.compare(new long[] {a[i],b[i]}, new long[]{a[j], b[j]}));だとめっちゃ遅い
		sortByKeyStable(b, a);
		sortByKeyStable(a, b);
	}
	
	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param keys
	 * @param values
	 */
	public static void sortByKeyStable(long[] keys, long[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    long[] nkeys = new long[n];
	    long[] nvalues = new long[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 64; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 56) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}
	
	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param keys
	 * @param values
	 */
	public static void rsortByKeyStable(long[] keys, long[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    long[] nkeys = new long[n];
	    long[] nvalues = new long[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 64; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 56) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = B-2; i >=0; i--) cnt[i] += cnt[i + 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = B-2; i >=0; i--) cnt[i] += cnt[i + 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}
	
	
	
	
	/**
	 * @param keys
	 * @param values
	 */
	public static void rsortByKeyStable(int[] keys, int[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    int[] nkeys = new int[n];
	    int[] nvalues = new int[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 32; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 24) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = B-2; i >=0; i--) cnt[i] += cnt[i + 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = B-2; i >=0; i--) cnt[i] += cnt[i + 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}

	
	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param keys
	 * @param values
	 */
	public static void sortByKeyStable(int[] keys, long[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    int[] nkeys = new int[n];
	    long[] nvalues = new long[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 32; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 24) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}

	
	
	public static void sortByKeyStable(int[] keys, int[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    int[] nkeys = new int[n];
	    int[] nvalues = new int[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 32; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 24) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}

	
	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param keys
	 * @param values
	 */
	public static void sortByKeyStable(long[] keys, int[] values) {
	    if(keys.length!=values.length)throw new AssertionError();
		int n = keys.length;
	    long[] nkeys = new long[n];
	    int[] nvalues = new int[n];

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 64; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 56) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask) ^ 128;
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int b = (int) ((keys[i] >>> shift) & mask);
	                nkeys[--cnt[b]] = keys[i];
	                nvalues[cnt[b]] = values[i];
	            }
	        }
	        var tmpArray = keys;
			keys = nkeys;
			nkeys = tmpArray;
	        var tmpArray2 = values;
			values = nvalues;
			nvalues = tmpArray2;
	    }
	}
	
	
	public static void rsort(int[] a) {
		Arrays.sort(a);
		reverse(a);
	}
	
	public static void rsort(long[] a) {
		Arrays.sort(a);
		reverse(a);
	}
	
	public static void rsort(double[] a) {
		Arrays.sort(a);
		reverse(a);
	}
	
	public static void rsort(char[] a) {
		Arrays.sort(a);
		reverse(a);
	}
	
	/**
	 * a[k] に昇順で k 番目の要素が来るように並び替える。
	 * <p>実行後は以下を満たす。
	 * <ul>
	 *   <li>{@code a[k]} は配列全体を昇順にソートしたときの k 番目の要素である。</li>
	 *   <li>すべての {@code i < k} に対して {@code a[i] <= a[k]}。</li>
	 *   <li>すべての {@code i > k} に対して {@code a[i] >= a[k]}。</li>
	 * </ul>
	 * 平均 O(N)
	 * @param a
	 * @param k
	 */
	public static void nthElement(int[] a, int k) {
		if (k < 0 || k >= a.length) return;
		int l = 0, r = a.length;
		Random rnd = new Random();
		while (r - l > 1) {
			int pivotIdx = l + rnd.nextInt(r - l);
			int pivot = a[pivotIdx];
			swap(pivotIdx, r - 1, a);
			int i = l;
			for (int j = l; j < r - 1; j++) {
				// [l, i) < pivot, [i, j) >= pivot
				if (a[j] < pivot) {
					swap(i++, j, a);
				}
			}
			swap(i, r - 1, a);
			// [l, i) < pivot, a[i] == pivot, (i, r) >= pivot
			if (i == k) return;
			if (i < k) l = i + 1;
			else r = i;
		}
	}

	/**
	 * a[k] に昇順で k 番目の要素が来るように並び替える。
	 * <p>実行後は以下を満たす。
	 * <ul>
	 *   <li>{@code a[k]} は配列全体を昇順にソートしたときの k 番目の要素である。</li>
	 *   <li>すべての {@code i < k} に対して {@code a[i] <= a[k]}</li>
	 *   <li>すべての {@code i > k} に対して {@code a[i] >= a[k]}</li>
	 * </ul>
	 * 平均 O(N)
	 * @param a
	 * @param k
	 */
	public static void nthElement(long[] a, int k) {
		if (k < 0 || k >= a.length) return;
		int l = 0, r = a.length;
		Random rnd = new Random();
		while (r - l > 1) {
			int pivotIdx = l + rnd.nextInt(r - l);
			long pivot = a[pivotIdx];
			swap(pivotIdx, r - 1, a);
			int i = l;
			for (int j = l; j < r - 1; j++) {
				// [l, i) < pivot, [i, j) >= pivot
				if (a[j] < pivot) {
					swap(i++, j, a);
				}
			}
			swap(i, r - 1, a);
			// [l, i) < pivot, a[i] == pivot, (i, r) >= pivot
			if (i == k) return;
			if (i < k) l = i + 1;
			else r = i;
		}
	}

	/**
	 * a[0...k] に上位 k + 1 個の要素が並ぶようにする。
	 * 平均 O(N)
	 * @param a
	 * @param k 0-indexed のインデックス
	 */
	public static void topK(int[] a, int k) {
		if (k < 0 || k >= a.length) return;
		int l = 0, r = a.length;
		Random rnd = new Random();
		while (r - l > 1) {
			int pivotIdx = l + rnd.nextInt(r - l);
			int pivot = a[pivotIdx];
			swap(pivotIdx, r - 1, a);
			int i = l;
			for (int j = l; j < r - 1; j++) {
				// [l, i) > pivot, [i, j) <= pivot
				if (a[j] > pivot) {
					swap(i++, j, a);
				}
			}
			swap(i, r - 1, a);
			// [l, i) > pivot, a[i] == pivot, (i, r) <= pivot
			if (i == k) return;
			if (i < k) l = i + 1;
			else r = i;
		}
	}

	/**
	 * a[0...k] に上位 k + 1 個の要素が並ぶようにする。
	 * 平均 O(N)
	 * @param a
	 * @param k 0-indexed のインデックス
	 */
	public static void topK(long[] a, int k) {
		if (k < 0 || k >= a.length) return;
		int l = 0, r = a.length;
		Random rnd = new Random();
		while (r - l > 1) {
			int pivotIdx = l + rnd.nextInt(r - l);
			long pivot = a[pivotIdx];
			swap(pivotIdx, r - 1, a);
			int i = l;
			for (int j = l; j < r - 1; j++) {
				// [l, i) > pivot, [i, j) <= pivot
				if (a[j] > pivot) {
					swap(i++, j, a);
				}
			}
			swap(i, r - 1, a);
			// [l, i) > pivot, a[i] == pivot, (i, r) <= pivot
			if (i == k) return;
			if (i < k) l = i + 1;
			else r = i;
		}
	}

	/**
	 * Arrays.comapreでソート
	 */
	public static void sort(int[][] a) {
		Arrays.sort(a, (x, y)->Arrays.compare(x, y));
	}

	/**
	 * Arrays.comapreでソート
	 * https://hos.ac/blog/#blog0002
	 */
	public static void sort(char[][] a) {
		Arrays.sort(a, (x, y)->Arrays.compare(x, y));
	}
	
	/**
	 * Arrays.comapreでソート
	 */
	public static void sort(long[][] a) {
		Arrays.sort(a, (x, y)->Arrays.compare(x, y));
	}
	
	/**
	 * Arrays.comapreでソート
	 */
	public static void sort(double[][] a) {
		Arrays.sort(a, (x, y)->Arrays.compare(x, y));
	}
	
	/**
	 * -Arrays.comapreでソート
	 */
	public static void rsort(int[][] a) {
		Arrays.sort(a, (x, y)->-Arrays.compare(x, y));
	}
	
	/**
	 * -Arrays.comapreでソート
	 */
	public static void rsort(long[][] a) {
		Arrays.sort(a, (x, y)->-Arrays.compare(x, y));
	}
	
	/**
	 * -Arrays.comapreでソート
	 */
	public static void rsort(double[][] a) {
		Arrays.sort(a, (x, y)->-Arrays.compare(x, y));
	}
	
	/**
	 * -Arrays.comapreでソート
	 */
	public static void rsort(char[][] a) {
		Arrays.sort(a, (x, y)->-Arrays.compare(x, y));
	}
	

	/**
	 * cmpは添え字i,jを受け取り(a[i],b[i]),(a[j],b[j])の比較結果を返す関数を渡す。、
	 * @param a
	 * @param b
	 * @param cmp
	 */
	public static void sort(int[] a, int[] b, Comparator<Integer> cmp) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, cmp);
		int[] na = new int[a.length];
		int[] nb = new int[b.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
		}
	}
	
	/**
	 * cmpは添え字i,jを受け取り(a[i],b[i],c[i]),(a[j],b[j],c[j])の比較結果を返す関数を渡す。
	 * @param a
	 * @param b
	 * @param cmp
	 */
	public static void sort(int[] a, int[] b, long[]c, Comparator<Integer> cmp) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, cmp);
		int[] na = new int[a.length];
		int[] nb = new int[b.length];
		long[] nc=new long[c.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
			nc[i] = c[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
			c[i] = nc[i];
		}
	}


	public static void sort(long[] a, long[] b, Comparator<Integer> cmp) {
		Integer[] id = new Integer[a.length];
		Arrays.setAll(id, i -> i);
		Arrays.sort(id, cmp);
		long[] na = new long[a.length];
		long[] nb = new long[b.length];
		for (int i = 0; i < a.length; ++i) {
			na[i] = a[id[i]];
			nb[i] = b[id[i]];
		}
		for (int i = 0; i < a.length; ++i) {
			a[i] = na[i];
			b[i] = nb[i];
		}
	}

	/***
	 * c[i] = a[i] + b[i] とする配列 c を返す。
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] add(int[] a, int[] b) {
		if (a.length != b.length)
			throw new AssertionError();
		int[] c = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	/***
	 * 各行の長さが同じでない場合非対応
	 */
	public static char[][] transpose(char[][] a) {
		char[][] b = new char[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][i] = a[i][j];
			}
		}
		return b;
	}

	/***
	 * 各行の長さが同じでない場合非対応
	 */
	public static int[][] transpose(int[][] a) {
		int[][] b = new int[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][i] = a[i][j];
			}
		}
		return b;
	}

	/***
	 * 各行の長さが同じでない場合非対応
	 */
	public static long[][] transpose(long[][] a) {
		long[][] b = new long[a[0].length][a.length];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < a[i].length; ++j) {
				b[j][i] = a[i][j];
			}
		}
		return b;
	}

	public static long[] modAdd(long[] a, long[] b, long mod) {
		if (a.length != b.length)
			throw new AssertionError();
		long[] c = new long[a.length];
		for (int i = 0; i < a.length; ++i) {
			c[i] = a[i] + b[i];
			if (c[i] >= mod)
				c[i] -= mod;
		}
		return c;
	}

	public static long[] modMul(long[] a, long scalar, long mod) {
		long[] b = new long[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = a[i] * scalar % mod;
		}
		return b;
	}

	public static long[] modSub(long[] a, long[] b, long mod) {
		if (a.length != b.length)
			throw new AssertionError();
		long[] c = new long[a.length];
		for (int i = 0; i < a.length; ++i) {
			c[i] = a[i] - b[i];
			if (c[i] < 0)
				c[i] += mod;
		}
		return c;
	}

	/**
	 * 0,1,..,n-1からなるランダムな順列
	 * 
	 * @param n
	 * @return
	 */
	public static int[] randomPermutation(int n) {
		Random rnd = new Random(n);
		int[] a = new int[n];
		Arrays.setAll(a, i -> i);
		for (int i = 0; i < n; i++) {
			int j = rnd.nextInt(i, n);
			ArrayUtils.swap(i, j, a);
		}
		return a;
	}

	/**
	 * ret[i][j]=a[i*W+j];
	 * 
	 * @param a
	 * @param H
	 * @param W
	 * @return
	 */
	public static int[][] reshape(int[] a, int H, int W) {
		if (a.length != H * W)
			throw new AssertionError();
		int[][] ret = new int[H][W];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				ret[i][j] = a[i * W + j];
			}
		}
		return ret;
	}
	
	
	/**
	 * ret[i][j]=a[i*W+j];
	 * 
	 * @param a
	 * @param H
	 * @param W
	 * @return
	 */
	public static long[][] reshape(long[] a, int H, int W) {
		if (a.length != H * W)
			throw new AssertionError();
		long[][] ret = new long[H][W];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				ret[i][j] = a[i * W + j];
			}
		}
		return ret;
	}

	/**
	 * a[i][j]=ret[i*W+j]; reshapeの逆関数
	 * 
	 * @param a
	 * @return
	 */
	public static int[] flatten(int[][] a) {
		int H = a.length;
		int W = a[0].length;
		int[] ret = new int[H * W];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				ret[i * W + j] = a[i][j];
			}
		}
		return ret;
	}

	
	public static int[] sortqInts(ArrayList<Integer> list) {
		int[]a=new int[list.size()];
		for (int i = 0; i < a.length; i++) {
			a[i]=list.get(i);
		}
		return sortq(a);
	}
	
	public static long[] sortqLongs(ArrayList<Long> list) {
		long[]a=new long[list.size()];
		for (int i = 0; i < a.length; i++) {
			a[i]=list.get(i);
		}
		return sortq(a);
	}

	
	public static <T> T[] sortq(T[] a, Comparator<T> comp) {
		if (a.length==0)return a.clone();
		T[] b=a.clone();
		Arrays.sort(b, comp);
		int pointer=1;
		for (int i = 1; i < b.length; i++) {
			if (comp.compare(b[pointer-1], b[i]) != 0) {
				b[pointer++]=b[i];
			}
		}
		return Arrays.copyOf(b, pointer);
	}
		
	public static int[] sortq(int[] a) {
		if(a.length==0)return new int[0];
		int[]b=a.clone();
		Arrays.sort(b);
		int pointer=1;
		for (int i = 1; i < b.length; i++) {
			if (b[pointer-1]!=b[i]) {
				b[pointer++]=b[i];
			}
		}
		return Arrays.copyOf(b, pointer);
	}
	
	public static long[] sortq(long[] a) {
		if(a.length == 0) return new long[0];
		long[]b=a.clone();
		Arrays.sort(b);
		int pointer=1;
		for (int i = 1; i < b.length; i++) {
			if (b[pointer-1]!=b[i]) {
				b[pointer++]=b[i];
			}
		}
		return Arrays.copyOf(b, pointer);
	}
	
	public record KV1 (long[] keys, long[] values){
	}
	public record KV2 (long[] keys, int[] values){
	}
	public record KV3 (int[] keys, int[] values){
	}
	
	/**
	 * keysでソートし、keysが同じ値のvaluesをopで畳み込む
	 * @param keys
	 * @param values
	 * @param op
	 * @return
	 */
	public static KV1 sortqFold(long[] keys, long[] values, LongBinaryOperator op) {
		if(keys.length != values.length) throw new AssertionError();
		if(keys.length==0) return new KV1(new long[0], new long[0]);
		int[]order=argSort(keys);
		long[] a=keys.clone();
		long[] b=values.clone();
		for (int i = 0; i < keys.length; i++) {
			a[i]=keys[order[i]];
			b[i]=values[order[i]];
		}
		int pointer=1;
		for (int i = 1; i < a.length; i++) {
			if (a[pointer-1]!=a[i]) {
				a[pointer]=a[i];
				b[pointer]=b[i];
				pointer++;
			} else {
				b[pointer-1]=op.applyAsLong(b[pointer-1], b[i]);
			}
		}
		a=Arrays.copyOf(a, pointer);
		b=Arrays.copyOf(b, pointer);
		return new KV1(a, b);
	}
	
	/**
	 * 未テスト
	 * @param a
	 * @param comp
	 */
	public static void quickSort(int[] a, IntComparator comp) {
		quickSort(a, 0, a.length-1, comp);
	}
	
	/**
	 * 未テスト
	 * [l,r]は閉区間
	 * @param a
	 * @param l
	 * @param r
	 * @param comp
	 */
	private static void quickSort(int[] a, int l, int r, IntComparator comp) {
		if (l >= r) return;
		int pivot = a[(l + r) >>> 1];
		int i = l;
		int j = r;
		while (i <= j) {
	        while (comp.compare(a[i], pivot) < 0) i++;
	        while (comp.compare(a[j], pivot) > 0) j--;
	        if (i <= j) {
	            int tmp = a[i];
	            a[i] = a[j];
	            a[j] = tmp;
	            i++; j--;
	        }
	    }
	    quickSort(a, l, j, comp);
	    quickSort(a, i, r, comp);
	}
	
	public static void sortByKeyUnstable(int[] keys, long[] values) {
	    quickSort(keys, values, 0, keys.length - 1);
	}
	
	public static void sortByKeyUnstable(long[] keys, int[] values) {
	    quickSort(keys, values, 0, keys.length - 1);
	}

	static void quickSort(int[] k, long[] v, int l, int r) {
	    while (l < r) {
	        int i = l, j = r;
	        long pivot = k[(l + r) >>> 1];
	        while (i <= j) {
	            while (k[i] < pivot) i++;
	            while (k[j] > pivot) j--;
	            if (i <= j) {
	                int tk = k[i]; k[i] = k[j]; k[j] = tk;
	                long tv = v[i]; v[i] = v[j]; v[j] = tv;
	                i++; j--;
	            }
	        }
	        if (j - l < r - i) {
	            quickSort(k, v, l, j);
	            l = i;
	        } else {
	            quickSort(k, v, i, r);
	            r = j;
	        }
	    }
	}

	
	static void quickSort(long[] k, int[] v, int l, int r) {
	    while (l < r) {
	        int i = l, j = r;
	        long pivot = k[(l + r) >>> 1];
	        while (i <= j) {
	            while (k[i] < pivot) i++;
	            while (k[j] > pivot) j--;
	            if (i <= j) {
	                long tk = k[i]; k[i] = k[j]; k[j] = tk;
	                int tv = v[i]; v[i] = v[j]; v[j] = tv;
	                i++; j--;
	            }
	        }
	        if (j - l < r - i) {
	            quickSort(k, v, l, j);
	            l = i;
	        } else {
	            quickSort(k, v, i, r);
	            r = j;
	        }
	    }
	}
	
	
	/**
	 * O(8n)
	 * @param keys
	 * @return
	 * https://atcoder.jp/contests/abc230/submissions/71986347
	 */
	public static int[] argSort(long[] keys) {
	    int n = keys.length;
	    int[] idx = new int[n];
	    int[] tmp = new int[n];

	    for (int i = 0; i < n; i++) idx[i] = i;

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 64; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 56) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[idx[i]] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int id = idx[i];
	                int b = (int) ((keys[id] >>> shift) & mask) ^ 128;
	                tmp[--cnt[b]] = id;
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[idx[i]] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int id = idx[i];
	                int b = (int) ((keys[id] >>> shift) & mask);
	                tmp[--cnt[b]] = id;
	            }
	        }

	        int[] swap = idx;
	        idx = tmp;
	        tmp = swap;
	    }
	    return idx;
	}


	
	public static int[] argSortByRadix(int[] keys) {
	    int n = keys.length;
	    int[] idx = new int[n];
	    int[] tmp = new int[n];

	    for (int i = 0; i < n; i++) idx[i] = i;

	    final int B = 256;
	    final int mask=B-1;
	    int[] cnt = new int[B];
	    for (int shift = 0; shift < 32; shift += 8) {
	        Arrays.fill(cnt, 0);
	        if (shift == 24) {
	            // 最上位 byte（符号対応）
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[idx[i]] >>> shift) & mask) ^ 128;
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int id = idx[i];
	                int b = (int) ((keys[id] >>> shift) & mask) ^ 128;
	                tmp[--cnt[b]] = id;
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                int b = (int) ((keys[idx[i]] >>> shift) & mask);
	                cnt[b]++;
	            }
	            for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
	            for (int i = n - 1; i >= 0; i--) {
	                int id = idx[i];
	                int b = (int) ((keys[id] >>> shift) & mask);
	                tmp[--cnt[b]] = id;
	            }
	        }

	        int[] swap = idx;
	        idx = tmp;
	        tmp = swap;
	    }
	    return idx;
	}
	
	
	public static KV3 sortqFold(int[] keys, int[] values, IntBinaryOperator op) {
		if(keys.length != values.length) throw new AssertionError();
		if(keys.length==0) return new KV3(new int[0], new int[0]);
		int n=keys.length;
		int[] a=new int[n];
		int[] b=new int[n];
		int[]order=argSort(keys);
		for (int i = 0; i < n; i++) {
			a[i]=keys[order[i]];
			b[i]=values[order[i]];
		}
		int pointer=1;
		for (int i = 1; i < a.length; i++) {
			if (a[pointer-1]!=a[i]) {
				a[pointer]=a[i];
				b[pointer]=b[i];
				pointer++;
			} else {
				b[pointer-1]=op.applyAsInt(b[pointer-1], b[i]);
			}
		}
		a=Arrays.copyOf(a, pointer);
		b=Arrays.copyOf(b, pointer);
		return new KV3(a, b);
	}
	
	
	
	public static KV2 sortqFold(long[] keys, int[] values, IntBinaryOperator op) {
		if(keys.length != values.length) throw new AssertionError();
		if(keys.length==0) return new KV2(new long[0], new int[0]);
		int n=keys.length;
		long[] a=new long[n];
		int[] b=new int[n];
		int[]order=argSort(keys);
		for (int i = 0; i < n; i++) {
			a[i]=keys[order[i]];
			b[i]=values[order[i]];
		}
		int pointer=1;
		for (int i = 1; i < a.length; i++) {
			if (a[pointer-1]!=a[i]) {
				a[pointer]=a[i];
				b[pointer]=b[i];
				pointer++;
			} else {
				b[pointer-1]=op.applyAsInt(b[pointer-1], b[i]);
			}
		}
		a=Arrays.copyOf(a, pointer);
		b=Arrays.copyOf(b, pointer);
		return new KV2(a, b);
	}

	
	
	public static String[] sortq(String[] a) {
		return Arrays.stream(a).distinct().sorted().toArray(String[]::new);
	}

	public static char[][] sortq(char[][] a) {
		//https://atcoder.jp/contests/abc343/submissions/74210454
		char[][]b=copy(a);
		Arrays.sort(b, (x, y) -> Arrays.compare(x, y));
	    int pointer = 1;
	    for (int i = 1; i < b.length; i++) {
	        if (!Arrays.equals(b[pointer - 1], b[i])) {
	            b[pointer++] = b[i];
	        }
	    }
	    return Arrays.copyOf(b, pointer);
	}

	/**
	 * a[b[i]]が昇順に並ぶようなbを返す。a[i]=a[j]のときはi < jとなるようにする。
	 * @param a
	 * @return
	 */
	public static int[] argSort(int[] a) {
		int[][] x = new int[a.length][2];
		for (int i = 0; i < a.length; i++) {
			x[i][0] = a[i];
			x[i][1] = i;
		}
		Arrays.sort(x, (p, q) -> Arrays.compare(p, q));
		int[] ret = new int[a.length];
		for (int i = 0; i < x.length; i++) {
			ret[i] = x[i][1];
		}
		return ret;
	}

	public static int[] argRSort(int[] a) {
		int[]ret=argSort(a);
		reverse(ret);
		return ret;
	}
	
	public static int[] argRSort(long[] a) {
		int[]ret=argSort(a);
		reverse(ret);
		return ret;
	}

	/**
	 * unsigned int として昇順にソートする。
	 * O(N)
	 * @param a
	 */
	public static void sortUnsigned(int[] a) {
		int n = a.length;
		if (n <= 1) return;
		int[] b = new int[n];
		final int B = 256;
		final int mask = B - 1;
		int[] cnt = new int[B];
		int[] src = a;
		int[] dst = b;
		for (int shift = 0; shift < 32; shift += 8) {
			Arrays.fill(cnt, 0);
			for (int i = 0; i < n; i++) {
				int bucket = (src[i] >>> shift) & mask;
				cnt[bucket]++;
			}
			for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
			for (int i = n - 1; i >= 0; i--) {
				int bucket = (src[i] >>> shift) & mask;
				dst[--cnt[bucket]] = src[i];
			}
			int[] tmp = src;
			src = dst;
			dst = tmp;
		}
	}

	/**
	 * unsigned long として昇順にソートする。
	 * O(N)
	 * @param a
	 */
	public static void sortUnsigned(long[] a) {
		int n = a.length;
		if (n <= 1) return;
		long[] b = new long[n];
		final int B = 256;
		final int mask = B - 1;
		int[] cnt = new int[B];
		long[] src = a;
		long[] dst = b;
		for (int shift = 0; shift < 64; shift += 8) {
			Arrays.fill(cnt, 0);
			for (int i = 0; i < n; i++) {
				int bucket = (int) ((src[i] >>> shift) & mask);
				cnt[bucket]++;
			}
			for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
			for (int i = n - 1; i >= 0; i--) {
				int bucket = (int) ((src[i] >>> shift) & mask);
				dst[--cnt[bucket]] = src[i];
			}
			long[] tmp = src;
			src = dst;
			dst = tmp;
		}
	}

	/**
	 * unsigned int として降順にソートする。
	 * O(N)
	 * @param a
	 */
	public static void rsortUnsigned(int[] a) {
		sortUnsigned(a);
		reverse(a);
	}

	/**
	 * unsigned long として降順にソートする。
	 * O(N)
	 * @param a
	 */
	public static void rsortUnsigned(long[] a) {
		sortUnsigned(a);
		reverse(a);
	}

	/**
	 * unsigned int として a[idx[i]] が昇順になるようなインデックスの配列を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int[] argSortUnsigned(int[] a) {
		int n = a.length;
		int[] idx = new int[n];
		for (int i = 0; i < n; i++) idx[i] = i;
		if (n <= 1) return idx;
		int[] tmpIdx = new int[n];
		final int B = 256;
		final int mask = B - 1;
		int[] cnt = new int[B];
		int[] srcIdx = idx;
		int[] dstIdx = tmpIdx;
		for (int shift = 0; shift < 32; shift += 8) {
			Arrays.fill(cnt, 0);
			for (int i = 0; i < n; i++) {
				int bucket = (a[srcIdx[i]] >>> shift) & mask;
				cnt[bucket]++;
			}
			for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
			for (int i = n - 1; i >= 0; i--) {
				int bucket = (a[srcIdx[i]] >>> shift) & mask;
				dstIdx[--cnt[bucket]] = srcIdx[i];
			}
			int[] tmp = srcIdx;
			srcIdx = dstIdx;
			dstIdx = tmp;
		}
		return srcIdx;
	}

	/**
	 * unsigned long として a[idx[i]] が昇順になるようなインデックスの配列を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int[] argSortUnsigned(long[] a) {
		int n = a.length;
		int[] idx = new int[n];
		for (int i = 0; i < n; i++) idx[i] = i;
		if (n <= 1) return idx;
		int[] tmpIdx = new int[n];
		final int B = 256;
		final int mask = B - 1;
		int[] cnt = new int[B];
		int[] srcIdx = idx;
		int[] dstIdx = tmpIdx;
		for (int shift = 0; shift < 64; shift += 8) {
			Arrays.fill(cnt, 0);
			for (int i = 0; i < n; i++) {
				int bucket = (int) ((a[srcIdx[i]] >>> shift) & mask);
				cnt[bucket]++;
			}
			for (int i = 1; i < B; i++) cnt[i] += cnt[i - 1];
			for (int i = n - 1; i >= 0; i--) {
				int bucket = (int) ((a[srcIdx[i]] >>> shift) & mask);
				dstIdx[--cnt[bucket]] = srcIdx[i];
			}
			int[] tmp = srcIdx;
			srcIdx = dstIdx;
			dstIdx = tmp;
		}
		return srcIdx;
	}

	/**
	 * unsigned int として a[idx[i]] が降順になるようなインデックスの配列を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int[] argRSortUnsigned(int[] a) {
		int[] ret = argSortUnsigned(a);
		reverse(ret);
		return ret;
	}

	/**
	 * unsigned long として a[idx[i]] が降順になるようなインデックスの配列を返す。
	 * O(N)
	 * @param a
	 * @return
	 */
	public static int[] argRSortUnsigned(long[] a) {
		int[] ret = argSortUnsigned(a);
		reverse(ret);
		return ret;
	}

	/**
	 * https://hos.ac/blog/#blog0002
	 * @param S
	 * @return
	 * verified:
	 */
	public static int[] argSort(char[][] S) {
		return ArrayUtils.argSort(S, (x, y)->Arrays.compare(x, y));
	}

	
	
	/**
	 * b[i]=(a[i]が安定ソート後に何番目か)となるbを返す
	 * b=inverse(argsort(a))である
	 * @param a
	 * @return
	 */
	public static int[] rank(long[] a) {
		return Permutation.inverse(argSort(a));
	}
	
	/**
	 * b[i]=(a[i]が安定ソート後に何番目か)となるbを返す
	 * b=inverse(argsort(a))である
	 * @param a
	 * @return
	 */
	public static int[] rank(int[] a) {
		return Permutation.inverse(argSort(a));
	}
	
	/**
	 * b[i]=(a[i]がソート後に何番目か)となるbを返す
	 * b=inverse(argsort(a))である
	 * @param a
	 * @return
	 */
	public static int[] rank(char[] a) {
		return Permutation.inverse(argSort(a));
	}
	
	
	

	
	public static <T> int[] argSort(T[] a, Comparator<T> comp) {
		Integer[] order=new Integer[a.length];
		for (int i = 0; i < a.length; i++) {
			order[i]=i;
		}
		Arrays.sort(order, (x, y)->comp.compare(a[x], a[y]));
		int[]ret=new int[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[i]=order[i].intValue();
		}
		return ret;
	}

	
	
	public static int[] argSort(char[] a) {
		Integer[] order=new Integer[a.length];
		for (int i = 0; i < a.length; i++) {
			order[i]=i;
		}
		Arrays.sort(order, (x, y)->Character.compare(a[x], a[y]));
		int[]ret=new int[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[i]=order[i].intValue();
		}
		return ret;
	}

	public static <T extends Comparable<? super T>> int[] argSort(T[] a) {
		Integer[] order=new Integer[a.length];
		for (int i = 0; i < a.length; i++) {
			order[i]=i;
		}
		Arrays.sort(order, (x, y)->a[x].compareTo(a[y]));
		int[]ret=new int[a.length];
		for (int i = 0; i < a.length; i++) {
			ret[i]=order[i].intValue();
		}
		return ret;
	}
	
	
	/**
	 * 下に凸なランダムな配列
	 * 
	 * @param n
	 * @return
	 */
	long[] randomConcaveLongArray(int n) {
		long[] a = ArrayUtils.randomLongArray(-10, 10, n);
		Arrays.sort(a);
		a = ArrayUtils.prefixSum(a);
		Random rnd = new Random();
		long base = rnd.nextLong(100);
		for (int i = 0; i < n; i++) {
			a[i] += base;
		}
		return a;
	}

	
	/**
	 * a配列の要素の総和を返す
	 * @param a
	 * @return
	 */
	public static long sum(long[][] a) {
		long v=0;
		for (int i = 0; i < a.length; i++) {
			v+=sum(a[i]);
		}
		return v;
	}
	
	public static long[] infs(int size) {
		long[]ret=new long[size];
		Arrays.fill(ret, Long.MAX_VALUE);
		return ret;
	}
	
	public static long[] ninfs(int size) {
		long[]ret=new long[size];
		Arrays.fill(ret, Long.MIN_VALUE);
		return ret;
	}
	
	public static long[] full(long val, int size) {
		long[]ret=new long[size];
		ArrayUtils.fill(ret, val);
		return ret;
	}
	
	public static int[] full(int val, int size) {
		int[]ret=new int[size];
		ArrayUtils.fill(ret, val);
		return ret;
	}
	

    /**[a,b)[c,d)を入れ替える。
     * 0,1,..,a-1, 
     * @param arr
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public static void swapRange(int[] arr, int a, int b, int c, int d) {
    	if (!(a<b&&b<=c&&c<d))throw new AssertionError();
        int len = d - a;
        int[] tmp = new int[len];

        for (int i = 0; i < len; i++) {
            tmp[i] = arr[a + i];
        }
        
        for (int i = 0; i < d - c; i++) {
            arr[a+i] = tmp[c-a+i];
        }
        for (int i = 0; i < c-b; i++) {
			arr[a+d-c+i] = tmp[b-a+i];
		}
        
        for (int i = 0; i < b - a; i++) {
            arr[a+d-b+i] = tmp[i];
        }
    }
    
    
    public static void swapRange(boolean[] arr, int a, int b, int c, int d) {
        if (!(a < b && b <= c && c < d)) throw new AssertionError();
        int len = d - a;
        boolean[] tmp = new boolean[len];

        for (int i = 0; i < len; i++) {
            tmp[i] = arr[a + i];
        }
        
        for (int i = 0; i < d - c; i++) {
            arr[a+i] = tmp[c-a+i];
        }
        for (int i = 0; i < c-b; i++) {
			arr[a+d-c+i] = tmp[b-a+i];
		}
        for (int i = 0; i < b - a; i++) {
            arr[a+d-b+i] = tmp[i];
        }

    
    }

    /**
     * swapRange(arr, a, b, c, d)の逆関数
     * @param arr
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public static void swapRangeInverse(int[] arr, int a, int b, int c, int d) {
        if (!(((a < b) && (b <= c)) && (c < d))) {
            throw new AssertionError();
        }
        int len = d - a;
        int[] tmp = new int[len];
        for (int i = 0; i < len; i++) tmp[i] = arr[a + i];
        
        for (int i = 0; i < b - a; ++i) {
        	arr[a + i] = tmp[(a+d-b) - a + i];
        }
        for (int i = 0; i < c-b; i++) {
			arr[b + i] = tmp[d-c+i];
		}
        for (int i = 0; i < d - c; ++i) {
        	arr[c + i] = tmp[i];
        }
    }

    public static void swapRangeInverse(boolean[] arr, int a, int b, int c, int d) {
        if (!(((a < b) && (b <= c)) && (c < d))) {
            throw new AssertionError();
        }
        int len = d - a;
        boolean[] tmp = new boolean[len];
        for (int i = 0; i < len; i++) tmp[i] = arr[a + i];

        for (int i = 0; i < b - a; ++i) {
        	arr[a + i] = tmp[(a+d-b) - a + i];
        }
        for (int i = 0; i < c-b; i++) {
			arr[b + i] = tmp[d-c+i];
		}
        for (int i = 0; i < d - c; ++i) {
        	arr[c + i] = tmp[i];
        }
    }

	



    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static char[] take(char[] s, int[] indices) {
    	char[]b=new char[indices.length];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=s[indices[i]];
		}
    	return b;
    }
    
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static int[][] take(int[][] s, int[] indices) {
    	int[][]b=new int[indices.length][];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=s[indices[i]].clone();
		}
    	return b;
    }
    
    
    public static char[][] take(char[][] s, int[] indices) {
    	char[][]b=new char[indices.length][];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=s[indices[i]].clone();
		}
    	return b;
    }
    
    
    public static char[][] take(char[][] s, ArrayList<Integer> indices) {
    	char[][]b=new char[indices.size()][];
    	for (int i = 0; i < indices.size(); i++) {
			b[i]=s[indices.get(i)].clone();
		}
    	return b;
    }
    
    /**
     * b[i][j]=a[indices1[i]][indces2[j]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static int[][] take(int[][] s, int[] indices1, int[] indices2) {
    	int[][]b=new int[indices1.length][indices2.length];
    	for (int i = 0; i < indices1.length; i++) {
    		for (int j = 0; j < indices2.length; j++) {
				b[i][j]=s[indices1[i]][indices2[j]];
			}
		}
    	return b;
    }
    

    
    /**
     * b[i][j]=a[indices1[i]][indces2[j]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static long[][] take(long[][] s, int[] indices1, int[] indices2) {
    	long[][]b=new long[indices1.length][indices2.length];
    	for (int i = 0; i < indices1.length; i++) {
    		for (int j = 0; j < indices2.length; j++) {
				b[i][j]=s[indices1[i]][indices2[j]];
			}
		}
    	return b;
    }
    
    /**
     * b[i][j]=a[indices1[i]][indces2[j]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static long[][] take(long[][] s, ArrayList<Integer> indices1, ArrayList<Integer> indices2) {
    	long[][]b=new long[indices1.size()][indices2.size()];
    	for (int i = 0; i < indices1.size(); i++) {
    		for (int j = 0; j < indices2.size(); j++) {
				b[i][j]=s[indices1.get(i)][indices2.get(j)];
			}
		}
    	return b;
    }
    
    
    
    
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static int[] take(int[] s, Integer[] indices) {
    	int[]b=new int[indices.length];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=s[indices[i]];
		}
    	return b;
    }
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static<T> T[] take(T[] s, Integer[] indices) {
        T[] b = Arrays.copyOf(s, indices.length);
        for (int i = 0; i < indices.length; i++) {
            b[i] = s[indices[i]];
        }
        return b;
    }
    

    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static int[] take(int[] s, int[] indices) {
    	int[]b=new int[indices.length];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=s[indices[i]];
		}
    	return b;
    }

    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param s
     * @param indices
     * @return
     */
    public static int[] take(int[] s, ArrayList<Integer> indices) {
    	int[]b=new int[indices.size()];
    	for (int i = 0; i < indices.size(); i++) {
			b[i]=s[indices.get(i)];
		}
    	return b;
    }
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param a
     * @param indices
     * @return
     */
    public static long[] take(long[] a, int[] indices) {
    	long[]b=new long[indices.length];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=a[indices[i]];
		}
    	return b;
    }
    
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param a
     * @param indices
     * @return
     */
    public static long[] take(long[] a, ArrayList<Integer> indices) {
    	long[]b=new long[indices.size()];
    	for (int i = 0; i < indices.size(); i++) {
			b[i]=a[indices.get(i)];
		}
    	return b;
    }
    
    /**
     * b[i]=a[indices[i]]となるbを返す。
     * @param a
     * @param indices
     * @return
     */
    public static long[] take(long[] a, Integer[] indices) {
    	long[]b=new long[indices.length];
    	for (int i = 0; i < indices.length; i++) {
			b[i]=a[indices[i]];
		}
    	return b;
    }

    
    /**
     * aの部分文字列でpatternと一致するものを数える。
     * @param a
     * @param pattern
     * @return
     */
    public static long countSubsequenceMatches(int[] a, int[] pattern) {
    	long[]dp=new long[pattern.length+1];
    	dp[0]=1;
    	for (int i = 0; i < a.length; i++) {
    		long[]ndp=Arrays.copyOf(dp, dp.length);
    		for (int j = 0; j < pattern.length; j++) {//何文字マッチしたか
				if(a[i]==pattern[j]) {
					ndp[j+1]+=dp[j];
				}
			}
    		dp=ndp;	
    	}
    	return dp[pattern.length];
    }

    /**
     * a[i]=vとなる最初のindexを返す。存在しないときは-1を返す。
     * @param v
     * @param a
     * @return
     */
    public static int indexOf(int[] a, int v) {
    	int i=0;
    	while(i<a.length && a[i] != v) {
    		++i;
    	}
    	if (i==a.length)return -1;
    	return i;
    }
    
    /**
     * a[i]=vとなる最初のindexを返す。存在しないときは-1を返す。
     * @param v
     * @param a
     * @return
     */
    public static int indexOf(char[] a, char v) {
    	int i=0;
    	while(i<a.length && a[i] != v) {
    		++i;
    	}
    	if (i==a.length)return -1;
    	return i;
    }
    
    /**
     * a[i]=vとなる最初のindexを返す。存在しないときは-1を返す。
     * @param v
     * @param a
     * @return
     */
    public static int indexOf(long v, long[] a) {
    	int i=0;
    	while(i<a.length && a[i] != v) {
    		++i;
    	}
    	if (i==a.length)return -1;
    	return i;
    }
    
    

    /**
     * a[i]=vとなる最後のindexを返す。存在しないときは-1を返す。
     * @param v
     * @param a
     * @return
     */
    public static int lastIndexOf(int[] a, int v) {
    	int i=a.length-1;
    	while(i>=0&&a[i]!=v) {
    		--i;
    	}
    	return i;
    }
    
    public static String to01(boolean[] a) {
    	char[] cs=new char[a.length];
    	for (int i = 0; i < a.length; i++) {
			if(a[i])cs[i]='0';
			else cs[i]='1';
		}
    	return String.valueOf(cs);
    }
    
    
    public static int[] compress(int[] a) {
    	int[][]b=new int[a.length][2];
    	for (int i = 0; i < a.length; i++) {
    		b[i][0]=a[i];
    		b[i][1]=i;
		}
    	sort(b);
    	int[]ret=new int[a.length];
    	int v=0;
    	for (int i = 0; i < b.length; i++) {
			int j=i;
			while(j+1<b.length && b[i][0]==b[j+1][0])++j;
			for (int k = i; k <= j; k++) {
				ret[b[k][1]] = v;
			}
			v++;
			i=j;
    	}
    	return ret;
    }
    
    
    public static int[] compress(long[] a) {
    	long[][]b=new long[a.length][2];
    	for (int i = 0; i < a.length; i++) {
    		b[i][0]=a[i];
    		b[i][1]=i;
		}
    	sort(b);
    	int[]ret=new int[a.length];
    	int v=0;
    	for (int i = 0; i < b.length; i++) {
			int j=i;
			while(j+1<b.length && b[i][0]==b[j+1][0])++j;
			for (int k = i; k <= j; k++) {
				ret[(int)b[k][1]] = v;
			}
			v++;
			i=j;
    	}
    	return ret;
    }

    
    public static <T> boolean isSortable(T[] arr, Comparator<T> comp) {
    	Integer[] order=new Integer[arr.length];
    	for (int i = 0; i < order.length; i++) {
			order[i]=i;
		}
    	try {
    		Arrays.sort(order, (i, j)->comp.compare(arr[i], arr[j]));
    		return true;
    	}catch (IllegalArgumentException e) {
    		return false;
    	}
    }
    
    
    public static <T> boolean trySort(T[] arr, Comparator<T> comp) {
    	try {
    		Arrays.sort(arr, comp);
    		return true;
    	}catch (IllegalArgumentException e) {
    		return false;
    	}
    }
    /**
     * https://atcoder.jp/contests/abc283/submissions/72123920
     * @param a
     * @return
     */
    public static long[] xorbase(long[] a) {
		long[]ret=new long[64];
		int size=0;
		Queue<Long>que=new ArrayDeque<>();
		for (int i = 0; i < a.length; i++) {
			if(a[i]==0)continue;
			que.add(a[i]);
		}
		while(!que.isEmpty()) {
			long v=que.poll();
			ret[size++]=v;
			for (int i = que.size()-1; i >= 0; i--) {
				long u=que.poll();
				u=Math.min(u, u^v);
				if(u!=0)que.add(u);
			}
		}
		ret=Arrays.copyOf(ret, size);
		for (int i = 0; i < ret.length; i++) {
			Arrays.sort(ret);
			for (int j = 0; j < ret.length; j++) {
				if(j==ret.length-1-i)continue;
				ret[j]=Math.min(ret[j], ret[j]^ret[ret.length-1-i]);
			}
		}
		return Arrays.copyOf(ret, size);
	}
    
    
    public static void pointwiseMax(long[] values, long[] lowerBounds) {
		if(values.length < lowerBounds.length) throw new AssertionError();
		for (int i = 0; i < lowerBounds.length; i++) {
			values[i]=Math.max(values[i], lowerBounds[i]);
		}
	}
    
    public static void pointwiseMin(long[] values, long[] upperBounds) {
		if(values.length < upperBounds.length) throw new AssertionError();
		for (int i = 0; i < upperBounds.length; i++) {
			values[i]=Math.min(values[i], upperBounds[i]);
		}
	}
    
    public static void shuffle(int[] a) {
    	Random rnd=new Random();
    	for (int i = 0; i < a.length; i++) {
			int j=rnd.nextInt(i, a.length);
			if(i!=j) {
				var tmp = a[i];
				a[i] = a[j];
				a[j] = tmp;
			}
    	}
    }
    
    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}
