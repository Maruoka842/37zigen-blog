package library.util.poset;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.Ints;
import library.util.MathUtils;

public class BooleanLatticeMinPlus {
    /**
     * f+f^2+f^3+..
     * @param f
     * @return
     */
	public static long[] geometricSum(long[] f) {
    	if (f[0]!=Long.MAX_VALUE)throw new AssertionError();
    	int N=MathUtils.floorLog2(f.length);
    	long[]ret=ArrayUtils.copy(f);
    	for (int s = 0; s < 1<<N; s++) {
			for (int t = s; t >= 0; t = (t - 1) & s) {
				int a=t;
				int b=s^t;
				if(ret[a]!=Long.MAX_VALUE&&ret[b]!=Long.MAX_VALUE) {
					ret[s]=Math.min(ret[s], ret[a]+ret[b]);
				}
				if (t==0)break;
			}
		}
    	return ret;
    }
	
    /**
     * fg
     * O(3^N)
     * @param f
     * @return
     */
	public static long[] mul(long[] f, long[] g) {
		int N=MathUtils.floorLog2(f.length);
    	long[] h=new long[f.length];
    	Arrays.fill(h, Long.MAX_VALUE);
    	for (int s = 0; s < 1<<N; s++) {
			for (int t = s; t >= 0; t = (t - 1) & s) {
				int a=t;
				int b=s^t;
				if(f[a]!=Long.MAX_VALUE&&g[b]!=Long.MAX_VALUE) {
					h[s]=Math.min(h[s], f[a]+g[b]);
				}
				if (t==0)break;
			}
		}
    	return h;
    }
	
    /**
     * fg
     * @param f
     * @return
     */
	public static long[] pow(long[] f, int n) {
		if(n==0) {
			long[]ret=new long[f.length];
			Arrays.fill(ret, Long.MAX_VALUE);
			ret[0]=0;
			return ret;
		} else {
			if (n%2==0)
				return pow(mul(f, f), n/2);
			else 
				return mul(pow(mul(f, f), n/2), f);
		}
    }
	
	
    /**
     * fg
     * O(3^N)
     * @param f
     * @return
     */
	public static double[] mul(double[] f, double[] g) {
		//https://atcoder.jp/contests/abc332/submissions/75397078
		int N=MathUtils.floorLog2(f.length);
    	double[] h=new double[f.length];
    	Arrays.fill(h, Double.POSITIVE_INFINITY);
    	for (int s = 0; s < 1<<N; s++) {
			for (int t = s; t >= 0; t = (t - 1) & s) {
				int a=t;
				int b=s^t;
				if(f[a]!=Double.POSITIVE_INFINITY &&g[b]!=Double.POSITIVE_INFINITY) {
					h[s]=Math.min(h[s], f[a]+g[b]);
				}
				if (t==0)break;
			}
		}
    	return h;
    }
	
    /**
     * @param f
     * @return
     */
	public static double[] pow(double[] f, int n) {
		if(n==0) {
			double[]ret=new double[f.length];
			Arrays.fill(ret, Double.POSITIVE_INFINITY);
			ret[0]=0;
			return ret;
		} else {
			if (n%2==0)
				return pow(mul(f, f), n/2);
			else 
				return mul(pow(mul(f, f), n/2), f);
		}
    }
 
    
    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
