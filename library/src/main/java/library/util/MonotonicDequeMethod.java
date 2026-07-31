package library.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import org.graphstream.graph.implementations.SingleGraph;

public class MonotonicDequeMethod {
	
    /**
     * aからいくつか(0個でもよい)の数を選び、その和を最小化したい。
     * ただし、選ばない数はw個以下しか連続しない。
     * @param a
     * @param w
     * @return
     * verified:https://atcoder.jp/contests/abc334/submissions/48748089
     */
	public static double minSumWithSkipLimit(double[] a, int w) {
    	double[]A=Arrays.copyOf(a, a.length+1);
    	double[]dp=new double[A.length+1];//dp[i]=a[0:i)まで選び、かつ、a[i-1]を選んでいる時の最小値
    	var dq=new ArrayDeque<Integer>();
    	dq.addLast(0);
    	for (int i = 0; i < A.length; i++) {
			if(!dq.isEmpty() && i-dq.peekFirst()>=w+1)dq.pollFirst();
			dp[i+1]=dp[dq.peekFirst()]+A[i];
			while(!dq.isEmpty() && dp[i+1] < dp[dq.peekLast()]) {
				dq.pollLast();
			}
			dq.addLast(i+1);
    	}
    	return dp[A.length];
    }

	
	
    /**
     * a[i] = Σ[j≤i] max(f[j],f[j+1],..,f[i])
     * @param f
     * @return
     */
    public static long[] rangemaxsumFixingEnd(long[] f) {
    	//https://atcoder.jp/contests/abc359/submissions/71072257
    	long[]g=f.clone();
    	for (int i = 0; i < f.length; i++) {
			g[i]*=-1;
		}
    	long[]ret=rangeminsumFixingEnd(g);
    	for (int i = 0; i < ret.length; i++) {
			ret[i]*=-1;
		}
    	return ret;
    }

	
	
    
    /**
     * a[i] = Σ[j≤i] min(f[j],f[j+1],..,f[i])
     * @param f
     * @return
     * verified:https://atcoder.jp/contests/abc353/tasks/abc353_e
     */
    public static long[] rangeminsumFixingEnd(long[] f) {
        ArrayDeque<long[]> dq = new ArrayDeque<>();// value, width
        long[]a=new long[f.length];
        long sum = 0;
        for (int i = 0; i < f.length; i++) {
            long width = 1;
            while (!dq.isEmpty() && (dq.peekLast()[0] >= f[i])) {
                sum -= dq.peekLast()[0] * dq.peekLast()[1];
                width += dq.peekLast()[1];
                dq.pollLast();
            } 
            dq.addLast(new long[]{ f[i], width });
            sum += f[i] * width;
            a[i] += sum;
        }
        return a;
    }
    
    /**
     * a[i] = Σ[j≥i] min(f[i],f[i+1],..,f[j])
     * @param f
     * @return
     */
    public static long[] rangeminsumFixingStart(long[] f) {
    	long[]g=f.clone();
    	ArrayUtils.reverse(g);
    	long[]a=rangeminsumFixingEnd(g);
    	ArrayUtils.reverse(a);
    	return a;
    }
}
