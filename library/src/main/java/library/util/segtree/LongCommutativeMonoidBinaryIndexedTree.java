package library.util.segtree;

import java.util.Arrays;
import library.util.algebra.strategy.longs.LongCommutativeMonoidStrategy;

/**
 * モノイド上の Binary Indexed Tree (Fenwick Tree)。
 *
 * <p>長さ N の配列 A = (a_0, a_1, ..., a_{N-1}) に対し、以下の操作を O(log N) で行う。
 * <ul>
 *   <li>a_i = a_i * v (i は 0-indexed)</li>
 *   <li>prefixSum(k) = a_0 * a_1 * ... * a_{k-1}</li>
 * </ul>
 * ここで * はモノイドの演算。
 * </p>
 *
 * <p>演算が可換である必要があることに注意。</p>
 */
public class LongCommutativeMonoidBinaryIndexedTree {
	protected final int n;
	protected final long[] v;
	protected final LongCommutativeMonoidStrategy strategy;

	/**
	 * O(N)
	 * @param n 要素数
	 * @param strategy モノイドの戦略
	 */
	public LongCommutativeMonoidBinaryIndexedTree(int n, LongCommutativeMonoidStrategy strategy) {
		this.n = n;
		this.v = new long[n + 1];
		this.strategy = strategy;
		Arrays.fill(v, strategy.identity());
	}

	/**
	 * a_i = a_i * val を行う。
	 * O(log N)
	 * @param i 0-indexed
	 * @param val 加える値
	 */
	public void add(int i, long val) {
		for (i++; i <= n; i += i & -i) {
			v[i] = strategy.mul(v[i], val);
		}
	}

	/**
	 * a_0 * a_1 * ... * a_{i-1} を計算する。
	 * O(log N)
	 * @param i 0-indexed
	 * @return prefix sum
	 */
	public long prefixSum(int i) {
		long res = strategy.identity();
		for (; i > 0; i -= i & -i) {
			res = strategy.mul(res, v[i]);
		}
		return res;
	}

	/**
	 * 全ての要素を単位元で初期化する。
	 * O(N)
	 */
	public void clear() {
		Arrays.fill(v, strategy.identity());
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
		System.out.println("LongCommutativeMonoidBinaryIndexedTree { v: " + java.util.Arrays.toString(v) + ", n: " + n + " }");
	}
}
