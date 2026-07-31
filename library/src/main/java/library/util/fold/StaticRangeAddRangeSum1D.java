package library.util.fold;

import java.util.Arrays;

import library.util.ArrayUtils;
/**
 * https://atcoder.jp/contests/abc332/submissions/72414191
 */
public class StaticRangeAddRangeSum1D {
	int N;
	long[]A;
	boolean isBuilt = false;
	
	/**
	 * 長さNの配列を用意する
	 * @param N
	 */
	public StaticRangeAddRangeSum1D(int N) {
		this.N = N;
		A = new long[N];
	}
	
	public void add(int leftInclusive, int rightExclusive, long val) {
		leftInclusive=Math.max(0, leftInclusive);
		if(leftInclusive>=rightExclusive)return;
		if(leftInclusive<N)A[leftInclusive]+=val;
		if(rightExclusive<N)A[rightExclusive]-=val;
	}
	
	public void build() {
		if (isBuilt) throw new AssertionError();
		A = ArrayUtils.prefixSum(A);
		A = ArrayUtils.prefixSum(A);
		isBuilt = true;
	}
	
	public long getRangeSum(int leftInclusive, int rightExclusive) {
		if (!isBuilt) {
			build();
		}
		leftInclusive = Math.max(leftInclusive, 0);
		rightExclusive=Math.min(rightExclusive, A.length);
		if (rightExclusive <= leftInclusive) return 0;
		return A[rightExclusive - 1] - (leftInclusive == 0 ? 0 : A[leftInclusive - 1]);
	}
	
	public long get(int i) {
		return getRangeSum(i, i+1);
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
