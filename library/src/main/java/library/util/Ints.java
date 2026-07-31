package library.util;

public class Ints {

	public static String toFibonacciString(int a) {
		long[]F=new long[92];
		F[0]=F[1]=1;
		for (int i = 2; i < F.length; i++) {
			F[i]=F[i-1]+F[i-2];
		}
		StringBuilder sb=new StringBuilder();
		for (int i = F.length - 1; i >= 1; i--) {
			if(a>=F[i]) {
				a-=F[i];
				sb.append("1");
			}else {
				sb.append("0");
			}
		}
		return sb.toString();
	}
	

	
	/***
	 * 32bit整数のペア　{a, b}　から64bit整数への全単射
	 * @param low
	 * @param high
	 * @return
	 */
	public static long packUnorderedPair(int a, int b) {
		long high = Math.min(a, b);
		long low = Math.max(a, b);
		return (high << 32) | low;
	}
	
	
	/***
	 * 32bit整数のペア(a, b)から64bit整数への全単射
	 * @param low
	 * @param high
	 * @return
	 */
	public static long pack(int low, int high) {
		return ((long) high << 32) | (low & ((1L<<32)-1));
	}
	
    public static long pack21bit(int a, int b, int c) {
    	return a | ((long)b << 21) | ((long)c << 42); 
    }
	
    /**
     * isFirstなら下位32bit
     * @param value
     * @param isFirst
     * @return
     */
	public static int unpack(long value, boolean isFirst) {
		if (isFirst) return (int) value;
		else return (int)(value >> 32);
	}
	
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
		return a & b / (Integer.highestOneBit(xor)) * Integer.highestOneBit(xor);
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
			if(Ints.bitAt(a, i)==1)ret[pointer++]=i;
		}
		return ret;
	}
	
	public static int bitmask(int length) {
		if (length > 32) throw new AssertionError();
		if (length == 32) return ~0;
		return (1 << length) - 1;
	}

}
