package library.util;

public class Doubles {

	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param x
	 * @return
	 */
	public static long orderPreservingMaptoLong(double x) {
	    x+=0.;//-0.0対策
		long b = Double.doubleToRawLongBits(x);
	    long sign = b >> 63;//符号。 0 のとき非負。 -1 のとき負。
	    if (sign >= 0) return b;
	    else return b ^ Long.MAX_VALUE;
	}
	
	/**
	 * https://atcoder.jp/contests/abc344/submissions/72456625
	 * @param x
	 * @return
	 */
	public static double longToDouble(long x) {
		long b = (x >= 0) ? x : (x ^ Long.MAX_VALUE);
	    return Double.longBitsToDouble(b);
	}
}
