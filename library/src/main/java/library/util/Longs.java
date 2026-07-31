package library.util;

public class Longs {
	
	public int[] toDigitArray(long a) {
		if(a==0)return new int[] {0};
		if(a<0)throw new AssertionError();
		int[]ret=new int[MathUtils.floorLog10(a)+1];
		int pointer=0;
		while(a!=0) {
			ret[pointer++]=(int)(a%10);
			a/=10;
		}
		return ret;
	}
	
	/**
	 * 例えば100111と100101では100100を返す。
	 * @param a
	 * @param b
	 * @return
	 */
	public static long binaryLcp(long a, long b) {
		long xor = a ^ b;
		return a & b / (Long.highestOneBit(xor)) * Long.highestOneBit(xor);
	}
	
	/**
	 * 負数の63bit目(0-origin)は1とする。
	 * @param binary
	 * @param pos
	 * @return
	 */
	public static int bitAt(long binary, int pos) {
		if (pos >= 64) return 0;
		return (int) ((binary >>> pos) % 2);
	}
	
	public static long bitmask(int length) {
		if (length > 64) throw new AssertionError();
		if (length == 64) return ~0L;
		return (1L << length) - 1;
	}

	
	/**
	 * bitが立っている位置を昇順に並べた配列を返す。
	 * @param a
	 * @return
	 */
	public static int[] bitPositions(long a) {
		int pointer=0;
		int[] ret=new int[Long.bitCount(a)];
		for(int i=0;i<64;++i) {
			if(Longs.bitAt(a, i)==1)ret[pointer++]=i;
		}
		return ret;
	}
	
	public static long bitPositionsToLong(int[] a) {
		long ret=0;
		for (int v : a) {
			ret |= 1L << v;
		}
		return ret;
	}
	
	public static long nondecrasingIntArrayToLong(int[] a) {
		long ret=0;
		for (int i = 0; i < a.length; i++) {
			ret |= 1L << (a[i]+i);
		}
		return ret;
	}
	
	
	
	
	public static String toBinaryStringAsMatrix(long v, int width, int height) {
		String ret="";
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				ret+=String.valueOf(Longs.bitAt(v, i*width+j));
			}
			ret+="\n";
		}
		return ret;
	}
	
}
