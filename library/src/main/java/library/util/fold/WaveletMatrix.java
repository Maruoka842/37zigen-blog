package library.util.fold;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.BitArray;
import library.util.Ints;
import library.util.Longs;

public class WaveletMatrix {
	int w;//何bit使うか
	final int n;
	final BitArray[] rank1;
	final int[] mid;//mid[i]=iビット目が0であるものの個数
	int[] inverseMapping;
	final long maxA;
	
	public WaveletMatrix(int[] a) {
		if(ArrayUtils.min(a) < 0) throw new AssertionError();
		n = a.length;
		maxA=ArrayUtils.max(a);
		w=1;
		while(1L<<w <= maxA) w++;
		rank1 = new BitArray[w];
		mid = new int[w];
		int[] sorted = new int[n];
		Arrays.setAll(sorted, i -> a[i]);
		inverseMapping = new int[n];
		Arrays.setAll(inverseMapping, i->i);
		for (int i = w - 1; i >= 0; --i) {
			rank1[i] = new BitArray(n);
			for (int j = 0; j < n; j++) {
				if (Ints.bitAt(sorted[j], i) == 1)
					rank1[i].set(j);
			}
			rank1[i].build();
			int[] nsorted=new int[n];
			int[] nInverseMapping=new int[n];
			int pointer=0;
			//wビット目でボックスソート
			for (int j = 0; j < n; j++) {
				if (Ints.bitAt(sorted[j], i) == 0) {
					nsorted[pointer] = sorted[j];
					nInverseMapping[pointer++]=inverseMapping[j];
				}
			}
			mid[i] = pointer;
			for (int j = 0; j < n; j++) {
				if (Ints.bitAt(sorted[j], i) == 1) {
					nsorted[pointer] = sorted[j];
					nInverseMapping[pointer++]=inverseMapping[j];
				}
			}
			sorted = nsorted;
			inverseMapping = nInverseMapping;
		}
	}

	
	
	public WaveletMatrix(long[] a) {
		if(ArrayUtils.min(a) < 0) throw new AssertionError();
		n = a.length;
		maxA=ArrayUtils.max(a);
		w=1;
		while(1L<<w <= maxA) w++;
		rank1 = new BitArray[w];
		mid = new int[w];
		long[] sorted = new long[n];
		inverseMapping = new int[n];
		Arrays.setAll(sorted, i -> a[i]);
		Arrays.setAll(inverseMapping, i->i);
		for (int i = w - 1; i >= 0; --i) {
			rank1[i] = new BitArray(n);
			for (int j = 0; j < n; j++) {
				if (Longs.bitAt(sorted[j], i) == 1)
					rank1[i].set(j);
			}
			rank1[i].build();
			long[] nsorted=new long[n];
			int[] nInverseMapping=new int[n];
			int pointer=0;
			//wビット目でボックスソート
			for (int j = 0; j < n; j++) {
				if (Longs.bitAt(sorted[j], i) == 0) {
					nsorted[pointer] = sorted[j];
					nInverseMapping[pointer++]=inverseMapping[j];
				}
			}
			mid[i] = pointer;
			for (int j = 0; j < n; j++) {
				if (Longs.bitAt(sorted[j], i) == 1) {
					nsorted[pointer] = sorted[j];
					nInverseMapping[pointer++]=inverseMapping[j];
				}
			}
			sorted=nsorted;
			inverseMapping=nInverseMapping;
		}
	}
	
	/**
	 * height+1, height+2, .., w-1番目までのbitを用いてソートしたとき、ソート後のa[0, i)のheight番目のbitにvは何個含まれているかを返す。
	 * @param i
	 * @param v
	 * @param height
	 * @return
	 */
	final private int rank(int i, int v, int height) {
		if (i <= 0)	 return 0;
		return v == 1 ? rank1[height].prefixSum(i) : (i - rank1[height].prefixSum(i));

	}
	
	/**
	 * height+1, height+2, .., w-1番目までのbitを用いてソートしたとき、ソート後のa[l, r)のheight番目のbitにvは何個含まれているかを返す。
	 * @param l
	 * @param r
	 * @param v
	 * @param height
	 * @return
	 */
	final private int rank(int l, int r, int v, int height) {
		if (l >= r) return 0;
		return rank(r, v, height) - rank(l, v, height);
	}

	/**
	 * x ∈ a[l, r) で lo ≤ x < hi の数を数えて返す
	 * @param l
	 * @param r
	 * @param lo
	 * @param hi
	 * @return
	 * https://atcoder.jp/contests/abc324/submissions/72310401
	 */
	final public int countRange(int l, int r, long lo, long hi) {
		if(lo>=hi) return 0;
		if(l>=r) return 0;
		return Math.max(0, countLess(l, r, hi) - countLeq(l, r, lo - 1)); 
	}
	
	/**
	 * a[start], a[start+1], .., a[n-1] のうち、 lo ≤ x < hi であるものからなる部分列を考える。
	 * その先頭から k 番目 (0-indexed) の要素が、元の数列で何番目にあるかを返す。
	 * 存在しない場合は-1
	 */
	final public int kthIndexInRangeFilteredSuffix(int start, long lo, long hi, int k) {
		//https://atcoder.jp/contests/abc324/submissions/75193548
		if (start < 0 || start > n) return -1;
		if (lo >= hi) return -1;
		if (k < 0) return -1;
		int total = countRange(start, n, lo, hi);
		if (total <= k) return -1;
		int ng = start - 1;
		int ok = n;
		while (ok - ng > 1) {
			int m = (ok + ng) >>> 1;
			if (countRange(start, m, lo, hi) >= k + 1) {
				ok = m;
			} else {
				ng = m;
			}
		}
		return ok - 1;
	}
	
	
	/**
	 * a[l, r) における k 番目 (0-indexed) に小さい値を返す。
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public long quantile(int l, int r, int k) {
		if (l >= r || r - l <= k) throw new AssertionError(); 
		return quantile(l, r, k, w - 1);
	}
	
	
	/**
	 * a[l, r) における k 番目 (0-indexed) に小さい値のindexを返す。
	 * 同じ値に関しては、インデックスが小さいほど、小さいとする。
	 */
	final public int quantileIndex(int l, int r, int k) {
		if (l >= r) throw new AssertionError("区間["+l+","+r+")は空");
		if (r - l <= k) throw new AssertionError("区間["+l+","+r+")の"+k+"番目の要素は存在しない"); 
		//上のビットから順に決める
		for (int h = w-1; h >= 0; h--) {
			int l0 = rank(l, 0, h);
			int r0 = rank(r, 0, h);
			int nz = r0 - l0;
			if (nz - 1 >= k) {
				//heightビット目が0確定
				l = l0;
				r = r0;
			} else {
				//heightビット目が1確定
				l += mid[h] - l0;
				r += mid[h] - r0;
				k -= nz;
			}
			//heightビット目でソートする。
		}
		return inverseMapping[l + k];

	}
	
	
	
	/**
	 * a[l, r) における k 番目 (0-indexed) に小さい値のindexを返す。
	 * 同じ値に関しては、インデックスが小さいほど、大きいとする。
	 * @param l
	 * @param r
	 * @param k
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public int quantileIndexStableDesc(int l, int r, int k) {
		//上のビットから順に決める
		for (int h = w - 1; h >= 0; h--) {
			int l0 = rank(l, 0, h);
			int r0 = rank(r, 0, h);
			int nz = r0 - l0;
			if (nz - 1 >= k) {
				//heightビット目が0確定
				l = l0;
				r = r0;
			} else {
				//heightビット目が1確定
				l += mid[h] - l0;
				r += mid[h] - r0;
				k -= nz;
			}
			//heightビット目でソートする。
		}
		return inverseMapping[r - 1 - k];
	}

	
	
	/**
	 * x ∈ a[l, r) のうち、 0 ≤ x< lo であるものの個数を数える
	 * @param l
	 * @param r
	 * @param lo
	 * 
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public int countLess(int l, int r, long lo) {
		if (r - l <= 0) return 0;
		if (lo < 0) return 0;
		if (lo > maxA) return r - l;
		int cnt = 0;
		for (int h = w - 1; h >= 0; h--) {
			if (Longs.bitAt(lo, h) == 0) {
				//hビット目が0確定
				l = rank(l, 0, h);
				r = rank(r, 0, h);
			} else {
				//hビット目が1確定
				int l1 = rank(l, 1, h);
				int r1 = rank(r, 1, h);
				cnt += (r - l) - (r1 - l1);
				//hビット目が0であるものの個数を足す
				l = l1 + mid[h];
				r = r1 + mid[h];
			}
		}
		return cnt;
	}
	
	/**
	 * x ∈ a[l, r) のうち、 0 ≤ x ≤ lo であるものの個数を数える
	 * @param l
	 * @param r
	 * @param lo
	 * 
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public int countLeq(int l, int r, long lo) {
		return countLess(l, r, lo+1);
	}
	
	
	/**
	 * x ∈ a[l, r) のうち、 lo < x であるものの個数を数える
	 * @param l
	 * @param r
	 * @param lo
	 * 
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public int countGreater(int l, int r, long lo) {
		return r - l - countLeq(l, r, lo);
	}
	
	/**
	 * x ∈ a[l, r) のうち、 lo ≤ x であるものの個数を数える
	 * @param l
	 * @param r
	 * @param lo
	 * 
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final public int countGeq(int l, int r, long lo) {
		return r - l - countLess(l, r, lo);
	}
	
	/**
	 * height+1,height+2,..,w-1番目のビットを用いてソートしたとき、ソート後のaでk番目(0-indexed)に小さい数を返す
	 * @param l
	 * @param r
	 * @param k
	 * @param height
	 * @return
	 * verified:https://atcoder.jp/contests/abc431/submissions/70815921
	 */
	final private long quantile(int l, int r, int k, int height) {
		//上のビットから順に決める
		long ret = 0;
		for (int h = height; h >= 0; h--) {
			int l0 = rank(l, 0, h);
			int r0 = rank(r, 0, h);
			int nz = r0 - l0;
			if (nz - 1 >= k) {
				//heightビット目が0確定
				l = l0;
				r = r0;
			} else {
				//heightビット目が1確定
				ret |= 1L << h;
				l += mid[h] - l0;
				r += mid[h] - r0;
				k -= nz;
			}
			//heightビット目でソートする。
		}
		return ret;
	}
	
	
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}