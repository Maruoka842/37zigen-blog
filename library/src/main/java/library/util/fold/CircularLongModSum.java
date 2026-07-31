package library.util.fold;

import library.util.ArrayUtils;

public class CircularLongModSum {
	final long[] a;
	long[] prefixSum;
	int N;
	long mod;
	
	public CircularLongModSum(long[] a, long mod) {
		this.a  = a;
		this.N = a.length;
		this.mod = mod;
	}
	
	public CircularLongModSum(int[] a, long mod) {
		this.a  = new long[a.length];
		for (int i = 0; i < a.length; i++) {
			this.a[i]=a[i];
		}
		this.N = a.length;
		this.mod = mod;
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
		if (leftInclusive > rightExclusive) throw new AssertionError();
		if (prefixSum == null) 	build();
		long len = rightExclusive - leftInclusive;
		long q = len / N;
		leftInclusive = (leftInclusive % N + N) % N;
		rightExclusive = leftInclusive + len % N;
		long ret=prefixSum[N - 1] * (q % mod)+ (rightExclusive == 0 ? 0 : prefixSum[(int)rightExclusive - 1]) - (leftInclusive == 0 ? 0 : prefixSum[(int)leftInclusive - 1]);
		ret = (ret % mod + mod) % mod;
		return ret;
	}
	
	private void build() {
		this.prefixSum = ArrayUtils.prefixModSum(ArrayUtils.concat(a, a), mod);
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし.</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("CircularLongModSum { a: " + java.util.Arrays.toString(a) + ", prefixSum: " + (prefixSum == null ? "null" : java.util.Arrays.toString(prefixSum)) + ", mod: " + mod + " }");
	}
}
