package library.util.fold;

import library.util.ArrayUtils;

public class CircularLongSum {
	final long[] a;
	long[] prefixSum;
	int N;
	
	public CircularLongSum(long[] a) {
		this.a  = a;
		this.N = a.length;
	}
	
	public CircularLongSum(int[] a) {
		this.a  = new long[a.length];
		for (int i = 0; i < a.length; i++) {
			this.a[i]=a[i];
		}
		this.N = a.length;
	}

	
	
	public long get(int id) {
		id = id % a.length;
		if (id < 0) id += a.length;
		return a[id];
	}
	
	/**
	 * a（入力配列)を無限個並べた配列上での累積和
	 * @param leftInclusive
	 * @param rightExclusive
	 * @return
	 */
	public long sum(long leftInclusive, long rightExclusive) {
		//https://atcoder.jp/contests/past23-open/submissions/74722191
		if (leftInclusive > rightExclusive) throw new AssertionError();
		if (prefixSum == null) 	build();
		long len = rightExclusive - leftInclusive;
		long q = len / N;
		leftInclusive = (leftInclusive % N + N) % N;
		rightExclusive = leftInclusive + len % N;
		return prefixSum[N - 1] * q + (rightExclusive == 0 ? 0 : prefixSum[(int)rightExclusive - 1]) - (leftInclusive == 0 ? 0 : prefixSum[(int)leftInclusive - 1]);
	}
	
	private void build() {
		this.prefixSum = ArrayUtils.prefixSum(ArrayUtils.concat(a, a));
	}
	
	/**
	 * sum(0, i+1) >= v となる最小の i を返す。
	 * https://atcoder.jp/contests/abc346/submissions/71435864
	 * @param v
	 * @return
	 */
	public long ceil(long v) {
		if (prefixSum == null) build();
		long ok=Math.ceilDiv(v, prefixSum[a.length-1]) * a.length;
		long ng=-1;
		while(ok-ng!=1) {
			long id=(ok+ng)/2;
			if(sum(0, id+1) >= v) {
				ok = id;
			} else {
				ng = id;
			}
		}
		return ok;
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
		System.out.println("CircularLongSum { a: " + java.util.Arrays.toString(a) + ", prefixSum: " + (prefixSum == null ? "null" : java.util.Arrays.toString(prefixSum)) + " }");
	}
}
