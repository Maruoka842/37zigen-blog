package library.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import org.graphstream.graph.implementations.SingleGraph;

public class TwoPointerMethod {
	/**
	 *  u[i]+v[j]>=xとなる(i,j)の個数を返す。
	 *  u, vはソートされていなければならない。
	 */
	public static long countGeq(double[]u, double[] v, double x) {
		for (int i = 0; i + 1 < u.length; i++) {
			if (!(u[i]<=u[i+1]))throw new AssertionError();
		}
		for (int i = 0; i + 1 < v.length; i++) {
			if (!(v[i]<=v[i+1]))throw new AssertionError();
		}
		double[] a = u;
		double[] b = v;
		if (u.length > v.length) {
			a = v;
			b = u;
		}
		long cnt = 0;
		int logB = Math.max(0, MathUtils.floorLog2(b.length));
		if ((long) a.length * logB < a.length + b.length) {
			for (int i = 0; i < a.length; i++) {
				cnt += library.util.seq.SortedArrays.countGeq(b, x - a[i]);
			}
		} else {
			int pointer = 0;
			for (int i = b.length - 1; i >= 0; i--) {
				while(pointer < a.length && b[i]+a[pointer]<x)++pointer;
				cnt+=a.length-pointer;
			}
		}
		return cnt;
	}
	
	
	/**
	 *  u[i]+v[j]>=xとなる(i,j)の個数を返す。
	 *  u, vはソートされていなければならない。
	 *  https://atcoder.jp/contests/abc366/submissions/71357778
	 */
	public static long countGeq(long[]u, long[] v, long x) {
		for (int i = 0; i + 1 < u.length; i++) {
			if (!(u[i]<=u[i+1]))throw new AssertionError();
		}
		for (int i = 0; i + 1 < v.length; i++) {
			if (!(v[i]<=v[i+1]))throw new AssertionError();
		}
		long[] a = u;
		long[] b = v;
		if (u.length > v.length) {
			a = v;
			b = u;
		}
		long cnt = 0;
		int logB = Math.max(0, MathUtils.floorLog2(b.length));
		if ((long) a.length * logB < a.length + b.length) {
			for (int i = 0; i < a.length; i++) {
				cnt += library.util.seq.SortedArrays.countGeq(b, x - a[i]);
			}
		} else {
			int pointer = 0;
			for (int i = b.length - 1; i >= 0; i--) {
				while(pointer < a.length && b[i]+a[pointer]<x)++pointer;
				cnt+=a.length-pointer;
			}
		}
		return cnt;
	}

	
	
	/**
	 * aは非負整数列とする。
	 * 各iに対して
	 * a[i]+..+a[j] <= x
	 * となる最大のjを求めて、b[i]=j+1とした配列を返す。
	 * つまり、固定されたiに対して[i,b[i])の和がx以下となる極大な区間を取る。 
	 * @param a
	 * @return
	 * verified:https://atcoder.jp/contests/abc130/submissions/70480848
	 */
	public static int[] leqSumRangeFixingStart(int[]a, long x) {
        int last=0;
        long sum=0;
        int[]ret=new int[a.length];
        for (int i = 0; i < a.length; i++) {
        	last=Math.max(last, i);
			while(last<a.length && sum+a[last] <= x) {
				sum+=a[last];
				++last;
			}
			ret[i]=last;
			if(last != i)sum-=a[i];
        }
        return ret;
	}
	
	
	/**
	 * aは非負整数列とする。
	 * 各iに対して
	 * a[i]+..+a[j] <= x
	 * となる最大のjを求めて、b[i]=j+1とした配列を返す。
	 * つまり、固定されたiに対して[i,b[i])の和がx以下となる極大な区間を取る。 
	 * @param a
	 * @return
	 */
	public static int[] leqSumRangeFixingStart(long[]a, long x) {
        int last=0;
        long sum=0;
        int[]ret=new int[a.length];
        for (int i = 0; i < a.length; i++) {
        	last=Math.max(last, i);
			while(last<a.length && sum+a[last] <= x) {
				sum+=a[last];
				++last;
			}
			ret[i]=last;
			if(last != i)sum-=a[i];
        }
        return ret;
	}
	
	
	
	/**
	 * 各iに対して
	 * a[i]=a[i+1]=..=a[j]
	 * となる最大のjを求めて、b[i]=j+1とした配列を返す。
	 * つまり、固定されたiに対して[i,b[i])の要素が全てxとなる極大な区間を取る。 
	 * @param a
	 * @return
	 */
	public static int[] eqRange(char[]a) {
        int[]ret=new int[a.length];
        for (int i = a.length-1; i >= 0; --i) {
        	ret[i]=i+1;
        	if (i != a.length-1 && a[i] == a[i + 1]) ret[i]=ret[i+1];
        }
        return ret;
	}
	
	/**
	 * 各iに対して
	 * a[i]=a[i+1]=..=a[j]
	 * となる最大のjを求めて、b[i]=j+1とした配列を返す。
	 * つまり、固定されたiに対して[i,b[i])の要素が全てxとなる極大な区間を取る。 
	 * @param a
	 * @return
	 */
	public static int[] eqRange(int[]a) {
        int[]ret=new int[a.length];
        for (int i = a.length-1; i >= 0; --i) {
        	ret[i]=i+1;
        	if (i != a.length-1 && a[i] == a[i + 1]) ret[i]=ret[i+1];
        }
        return ret;
	}
	
	
	/**
	 * 各iに対して
	 * a[i]=a[i+1]=..=a[j]
	 * となる最大のjを求めて、b[i]=j+1とした配列を返す。
	 * つまり、固定されたiに対して[i,b[i])の要素が全てxとなる極大な区間を取る。 
	 * @param a
	 * @return
	 */
	public static int[] eqRange(long[]a) {
        int[]ret=new int[a.length];
        for (int i = a.length-1; i >= 0; --i) {
        	ret[i]=i+1;
        	if (i != a.length-1 && a[i] == a[i + 1]) ret[i]=ret[i+1];
        }
        return ret;
	}
	
	/**
	 * 各iに対して
	 * a[i]=a[i-1]=..=a[j]
	 * となる最小のjを求めて、b[i]=j-1とした配列を返す。
	 * つまり、固定されたiに対して(b[i],i]の要素が全てxとなる極大な区間を取る。 
	 * @param a
	 * @return
	 */
	public static int[] eqRangeFixingEnd(char[]a) {
        int[]ret=new int[a.length];
        for (int i = 0; i < a.length; ++i) {
        	ret[i]=i-1;
        	if (i != 0 && a[i] == a[i - 1]) ret[i]=ret[i-1];
        }
        return ret;
	}


}
