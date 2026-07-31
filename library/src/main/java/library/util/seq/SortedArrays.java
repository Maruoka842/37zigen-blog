package library.util.seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import library.util.collections.IntArrayList;

public class SortedArrays {
	/**
	 * ソート済み配列a, bをマージした配列を返す。a, bは昇順にソートされているとする。
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] merge(int[] a, int[] b) {
		int[] c = new int[a.length + b.length];
		int i = 0;
		int j = 0;
		int k = 0;
		while (i < a.length && j < b.length) {
			if (a[i] <= b[j]) c[k++] = a[i++];
			else c[k++] = b[j++];
		}
		while (i < a.length) c[k++] = a[i++];
		while (j < b.length) c[k++] = b[j++];
		return c;
	}
	
	/**
	 * ソート済み配列a, bをマージした配列を返す。a, bは昇順にソートされているとする。
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] merge(long[] a, long[] b) {
		long[] c = new long[a.length + b.length];
		int i = 0;
		int j = 0;
		int k = 0;
		while (i < a.length && j < b.length) {
			if (a[i] <= b[j]) c[k++] = a[i++];
			else c[k++] = b[j++];
		}
		while (i < a.length) c[k++] = a[i++];
		while (j < b.length) c[k++] = b[j++];
		return c;
	}
	
	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 */
	public static int floor(int[] a, int key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a[m] <= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int floor(char[][] a, char[] key) {
		return floor(a, key, Arrays::compare);
	}

	/**
	 * a[i] < key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int lower(char[][] a, char[] key) {
		return lower(a, key, Arrays::compare);
	}

	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int floor(int[][] a, int[] key) {
		return floor(a, key, Arrays::compare);
	}

	/**
	 * a[i] < key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int lower(int[][] a, int[] key) {
		return lower(a, key, Arrays::compare);
	}

	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int floor(long[][] a, long[] key) {
		return floor(a, key, Arrays::compare);
	}

	/**
	 * a[i] < key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int lower(long[][] a, long[] key) {
		return lower(a, key, Arrays::compare);
	}
	
	
	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 */
	public static int floor(IntArrayList a, int key) {
		int ok = -1;
		int ng = a.size();
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a.get(m) <= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int floor(T[] a, T key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a[m].compareTo(key) <= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	

	/**
	 * a[i] < key となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int lower(T[] a, T key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a[m].compareTo(key) < 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	

	/**
	 * a[i] < key となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int lower(List<T> a, T key) {
		int ok = -1;
		int ng = a.size();
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a.get(m).compareTo(key) < 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * a[i] <= key となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int floor(ArrayList<T> a, T key) {
		int ok = -1;
		int ng = a.size();
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a.get(m).compareTo(key) <= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}


	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int ceil(int[] a, int key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (a[m] >= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int ceil(char[][] a, char[] key) {
		return ceil(a, key, Arrays::compare);
	}

	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int higher(char[][] a, char[] key) {
		return higher(a, key, Arrays::compare);
	}

	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int ceil(int[][] a, int[] key) {
		return ceil(a, key, Arrays::compare);
	}

	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int higher(int[][] a, int[] key) {
		return higher(a, key, Arrays::compare);
	}

	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int ceil(long[][] a, long[] key) {
		return ceil(a, key, Arrays::compare);
	}

	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * @param a
	 * @param key
	 * @return
	 * $O(L \log N)$ (L: key.length, N: a.length)
	 */
	public static int higher(long[][] a, long[] key) {
		return higher(a, key, Arrays::compare);
	}

	
	
	/**
	 * key <= a[i] となる最大の i を返す。aは降順にソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70816641
	 */
	public static int ceilDesc(long[] a, long key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a[m] >= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	
	
	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int ceil(T[] a, T key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (a[m].compareTo(key) >= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T extends Comparable<? super T>> int ceil(ArrayList<T> a, T key) {
		int ok = a.size();
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (a.get(m).compareTo(key) >= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	
	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T> int ceil(T[] a, T key, Comparator<T> comp) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a[m]) <= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key <= a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T> int ceil(ArrayList<T> a, T key, Comparator<T> comp) {
		//https://atcoder.jp/contests/abc212/submissions/74399058
		int ok = a.size();
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a.get(m)) <= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * 未テスト
	 * @param <T>
	 * @param a
	 * @param key
	 * @param comp
	 * @return
	 */
	public static <T> int higher(ArrayList<T> a, T key, Comparator<T> comp) {
		int ok = a.size();
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a.get(m)) < 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T> int higher(T[] a, T key, Comparator<T> comp) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a[m]) < 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int higher(long[] a, long key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (key < a[m])
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	
	
	
	
	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int higher(ArrayList<Long> a, long key) {
		int ok = a.size();
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (key < a.get(m))
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int higher(ArrayList<Integer> a, int key) {
		int ok = a.size();
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (key < a.get(m))
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	
	
	/**
	 * key >= a[i] となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T> int floor(T[] a, T key, Comparator<T> comp) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a[m]) >= 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * key > a[i] となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static <T> int lower(T[] a, T key, Comparator<T> comp) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (comp.compare(key, a[m]) > 0)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	
	/**
	 * key > a[i] となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int lower(long[] a, long key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (key > a[m])
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * key > a[i] となる最大の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int lower(int[] a, int key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (key > a[m])
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * a[i] <= key となる最大の i を返す。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int floor(long[] a, long key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (a[m] <= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	/**
	 * key <= a[i] となる最小の i を返す。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int ceil(long[] a, long key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (a[m] >= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	
	/**
	 * comp(key, a[i]) = 0 となる最小の i を返す。存在しなければ-1
	 * @param a
	 * @param key
	 * @return
	 * https://atcoder.jp/contests/abc442/submissions/72739506
	 */
	public static <T> int indexOf(T[] a, T key, Comparator<T> comp) {
		int ret=SortedArrays.lower(a, key, comp)+1;
		if(0 <= ret && ret < a.length && comp.compare(a[ret], key) == 0) return ret;
		else return -1;
	}
	
	
	/**
	 * key = a[i] となる最小の i を返す。存在しなければ-1
	 * 未テスト
	 * @param a
	 * @param key
	 * @return
	 */
	public static int indexOf(long[] a, long key) {
		int ret=lower(a, key)+1;
		if(0 <= ret && ret < a.length && a[ret] == key) return ret;
		else return -1;
	}
	
	/**
	 * key = a[i] となる最小の i を返す。存在しなければ-1
	 * 未テスト
	 * @param a
	 * @param key
	 * @return
	 */
	public static int indexOf(int[] a, int key) {
		int ret=lower(a, key)+1;
		if(0 <= ret && ret < a.length && a[ret] == key) return ret;
		else return -1;
	}
	
	
	/**
	 * key = a[i] となる最後の i を返す。存在しなければ-1
	 * 未テスト
	 * @param a
	 * @param key
	 * @return
	 */
	public static int lastIndexOf(long[] a, long key) {
		int ret=higher(a, key)-1;
		if(0 <= ret && ret < a.length && a[ret] == key) return ret;
		else return -1;
	}
	
	
	/**
	 * key = a[i] となる最後の i を返す。存在しなければ-1
	 * 未テスト
	 * @param a
	 * @param key
	 * @return
	 */
	public static int lastIndexOf(int[] a, int key) {
		int ret=higher(a, key)-1;
		if(0 <= ret && ret < a.length && a[ret] == key) return ret;
		else return -1;
	}
	
	
	/**
	 * key < a[i] となる最小の i を返す。aはソートされているとする。
	 * 
	 * @param a
	 * @param key
	 * @return
	 */
	public static int higher(int[] a, int key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (key < a[m])
				ok = m;
			else
				ng = m;
		}
		return ok;
	}
	
	/**
	 * rankは0-origin. aに含まれないlowerBound以上の数のうち、rank番目(0-origin)に小さい数を返す
	 * @param a
	 * @param k
	 * @return
	 */
	public static long kthOnComplementSet(long[] a, long rank, long lowerBound) {
		//https://atcoder.jp/contests/abc440/submissions/72388760
		int L = floor(a, lowerBound - 1);
        int ok = L;
        int ng = a.length;
        while (Math.abs(ok - ng) != 1) {
            int mid = (ok + ng) / 2;
            long v = (a[mid] - (lowerBound - 1)) - (mid - L);// [lowerBound:a[mid]] に何個のaに含まれない数があるか。
            if (v < rank+1) {
                ok = mid;
            } else {
                ng = mid;
            }
        }
        if (ok == -1) {
        	return lowerBound + rank;
        } else {
        	long x = a[ok] - (lowerBound - 1) - (ok - L);// [lowerBound:a[ok]] に何個のaに含まれない数があるか。
        	return a[ok] + (rank+1) - x;
        }
	}
	
	public static int countLeq(long[] a, long v) {
		//https://atcoder.jp/contests/abc437/submissions/73654891
		return floor(a, v) + 1;
	}
	
	public static int countLess(long[] a, long v) {
		//https://atcoder.jp/contests/abc437/submissions/73654891
		return lower(a, v) + 1;
	}
	
	public static int countGeq(long[] a, long v) {
		//https://atcoder.jp/contests/abc437/submissions/73654891
		return a.length - countLess(a, v);
	}
	
	public static int countGreater(long[] a, long v) {
		//https://atcoder.jp/contests/abc437/submissions/73654891
		return a.length - countLeq(a, v);
	}

	/**
	 * key <= a[i] となる最小の i を返す。
	 *
	 * @param a
	 * @param key
	 * @return
	 *
	 * 計算量: O(log N)
	 * // 未テスト
	 */
	public static int ceil(double[] a, double key) {
		int ok = a.length;
		int ng = -1;
		while (ok - ng > 1) {
			int m = (ok + ng) / 2;
			if (a[m] >= key)
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	/**
	 * key > a[i] となる最大の i を返す。aはソートされているとする。
	 *
	 * @param a
	 * @param key
	 * @return
	 *
	 * 計算量: O(log N)
	 * // 未テスト
	 */
	public static int lower(double[] a, double key) {
		int ok = -1;
		int ng = a.length;
		while (ng - ok > 1) {
			int m = (ok + ng) / 2;
			if (key > a[m])
				ok = m;
			else
				ng = m;
		}
		return ok;
	}

	/**
	 * key <= a[i] となる要素の個数を返す。aはソートされているとする。
	 *
	 * @param a
	 * @param v
	 * @return
	 *
	 * 計算量: O(log N)
	 * // 未テスト
	 */
	public static int countGeq(double[] a, double v) {
		return a.length - (lower(a, v) + 1);
	}
	
}