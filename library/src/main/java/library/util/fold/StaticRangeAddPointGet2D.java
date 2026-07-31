package library.util.fold;

import java.util.Arrays;
/**
 * https://atcoder.jp/contests/abc168/submissions/71754151
 */
public class StaticRangeAddPointGet2D extends PrefixSum2D {
	public StaticRangeAddPointGet2D(int H, int W) {
		super(H, W);
	}
	
	boolean built=false;
	
	@Override
	public void build() {
		if(built)throw new AssertionError();
		super.build();
		built=true;
	}
	
	/**
	 * minH, minW は全てinclusive
	 * maxH, maxW は全てexclusive
	 * @param minH
	 * @param minW
	 * @param maxH
	 * @param maxW
	 * @return
	 */	
	public void rangeAdd(int minH, int minW, int maxH, int maxW, long val) {
		int[] h=new int[] {minH,maxH};
		int[] w=new int[] {minW,maxW};
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				super.add(h[i], w[j], val * ((i ^ j) == 0 ? 1 : -1));
			}
		}
	}
	
	public long get(int i, int j) {
		if(!built) {
			build();
		}
		return super.get(i, j);
	}
	
	
	public String toString() {
	    if (!built) {
	        build();
	    }
	    StringBuilder sb = new StringBuilder();
	    sb.append("StaticRangeAddPointGet2D[\n");
	    for (int i = 0; i < H; i++) {
	        sb.append("  ");
	        for (int j = 0; j < W; j++) {
	            if (j > 0) sb.append(' ');
	            sb.append(get(i, j));
	        }
	        sb.append('\n');
	    }
	    sb.append(']');
	    return sb.toString();
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}