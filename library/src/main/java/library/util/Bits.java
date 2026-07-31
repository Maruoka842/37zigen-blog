package library.util;

public class Bits {

	public static int bitAt(int binary, int pos) {
		if (pos >= 32) return 0;
		return (binary >>> pos) % 2;
	}
	
	/**
	 * 例えば100111と100101では100100を返す。
	 * @param a
	 * @param b
	 * @return
	 */
	public static int lcp(int a, int b) {
		int xor = a ^ b;
		return (a & b) / (Integer.highestOneBit(xor)) * Integer.highestOneBit(xor);
	}
	
	/**
	 * bitが立っている位置を昇順に並べた配列を返す。
	 * @param a
	 * @return
	 */
	public static int[] bitPositions(int a) {
		int pointer=0;
		int[] ret=new int[Integer.bitCount(a)];
		for(int i=0;i<32;++i) {
			if(Bits.bitAt(a, i)==1)ret[pointer++]=i;
		}
		return ret;
	}
	
	public static int bitmask(int length) {
		if (length > 32) throw new AssertionError();
		if (length == 32) return ~0;
		return (1 << length) - 1;
	}
	
	/**
	 * https://atcoder.jp/contests/abc328/submissions/72391492
	 * @param x
	 * @param l
	 * @param r
	 * @return
	 */
	public static int extractBits(int x, int l, int r) {
		return (x >>> l) & ((1 << (r - l)) - 1);
	}

}
