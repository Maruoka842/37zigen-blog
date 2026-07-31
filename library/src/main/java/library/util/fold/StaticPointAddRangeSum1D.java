package library.util.fold;

import library.util.ArrayUtils;
import library.util.collections.HashMultiSet;

public class StaticPointAddRangeSum1D {
	int N;
	long[]A;
	boolean isBuilt = false;
	
	/**
	 * 長さNの配列を用意する
	 * @param N
	 */
	public StaticPointAddRangeSum1D(int N) {
		this.N = N;
		A = new long[N];
	}
	
	/**
	 * 座標iにa[i]を足して初期化する
	 * @param N
	 */
	public StaticPointAddRangeSum1D(int[] a) {
		this.N = a.length;
		A = new long[N];
		for (int i = 0; i < A.length; i++) {
			add(i, a[i]);
		}
	}

	public StaticPointAddRangeSum1D(long[] a) {
		this.N = a.length;
		A = new long[N];
		for (int i = 0; i < A.length; i++) {
			add(i, a[i]);
		}
	}

	
	public void add(int i, long val) {
		A[i]+=val;
	}
	
	void build() {
		if (isBuilt) throw new AssertionError();
		A = ArrayUtils.prefixSum(A);
		isBuilt = true;
	}
	
	/**
	 * verified:https://atcoder.jp/contests/abc347/submissions/71277574
	 */	
	public long getRangeSum(int leftInclusive, int rightExclusive) {
		if (!isBuilt) {
			build();
		}
		leftInclusive=Math.max(leftInclusive, 0);
		rightExclusive=Math.min(rightExclusive, A.length);
		if (rightExclusive <= leftInclusive) return 0;
		return A[rightExclusive - 1] - (leftInclusive == 0 ? 0 : A[leftInclusive - 1]);
	}
	
	public void clear() {
		isBuilt=false;
		for (int i = 0; i < A.length; i++) {
			A[i]=0;
		}
	}

	/**
	 * 和が v となる区間の個数を返す。
	 *
	 * @param v ターゲットとなる和
	 * @return #{(l, r) | 0 <= l < r <= N, \sum_{i=l}^{r-1} a_i = v}
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 和が v となる区間の個数を返す。</li>
	 *   <li>副作用: !isBuilt の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(N)</li>
	 *   <li>破壊的変更: なし（build() による内部状態の変化を除く）。</li>
	 * </ul>
	 */
	public long countRangeSum(long v) {
		if (!isBuilt) {
			build();
		}
		HashMultiSet<Long> counts = new HashMultiSet<>();
		counts.add(0L);
		long res = 0;
		for (long s : A) {
			res += counts.getValue(s - v);
			counts.add(s);
		}
		return res;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("StaticPointAddRangeSum1D { A: " + java.util.Arrays.toString(A) + ", isBuilt: " + isBuilt + " }");
	}
}
