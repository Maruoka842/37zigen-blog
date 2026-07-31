package library.util.fold;

/**
 * verified:https://atcoder.jp/contests/abc312/submissions/44022590
 */
public class StaticRangeAddPointGet3D extends PrefixSum3D {
	public StaticRangeAddPointGet3D(int len0, int len1, int len2) {
		super(len0, len1, len2);
	}	
	
	/**
	 * minI,minJ,minKはinclusive
	 * maxI,maxJ,maxKはexclusive
	 */	
	public void rangeAdd(int minI, int minJ, int minK, int maxI, int maxJ, int maxK, long val) {
		int[] is=new int[] {minI, maxI};
		int[] js=new int[] {minJ, maxJ};
		int[] ks=new int[] {minK, maxK};
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				for (int k = 0; k < 2; k++) {
					super.add(is[i], js[j], ks[k], val * ((i ^ j ^ k) == 0 ? 1 : -1));
				}
			}
		}
	}
	
	public long get(int i, int j, int k) {
		return super.get(i, j, k);
	}
}