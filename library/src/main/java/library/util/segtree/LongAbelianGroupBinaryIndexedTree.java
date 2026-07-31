package library.util.segtree;

import library.util.algebra.strategy.longs.LongAbelianGroupStrategy;

/**
 * 群上の Binary Indexed Tree (Fenwick Tree)。
 *
 * <p>長さ N の配列 A = (a_0, a_1, ..., a_{N-1}) に対し、モノイド上の操作に加え、以下の操作を O(log N) で行う。
 * <ul>
 *   <li>fold(l, r) = a_l * a_{l+1} * ... * a_{r-1}</li>
 * </ul>
 * ここで * は群の演算。
 * </p>
 *
 * <p>演算が可換である必要があることに注意。</p>
 */
public class LongAbelianGroupBinaryIndexedTree extends LongCommutativeMonoidBinaryIndexedTree {
	private final LongAbelianGroupStrategy groupStrategy;

	/**
	 * O(N)
	 * @param n 要素数
	 * @param strategy 群の戦略
	 */
	public LongAbelianGroupBinaryIndexedTree(int n, LongAbelianGroupStrategy strategy) {
		super(n, strategy);
		this.groupStrategy = strategy;
	}

	/**
	 * a_l * a_{l+1} * ... * a_{r-1} を計算する。
	 * O(log N)
	 * @param l 0-indexed, inclusive
	 * @param r 0-indexed, exclusive
	 * @return range product
	 */
	public long fold(int l, int r) {
		if (l >= r) return groupStrategy.identity();
		return groupStrategy.mul(prefixSum(r), groupStrategy.inverse(prefixSum(l)));
	}

	/**
	 * f(fold(l, r)) が真となる最大の r を返す。
	 *
	 * @param l 開始インデックス (0-indexed)
	 * @param f 判定式
	 * @return 最大の r
	 * 計算量: O(log N)
	 * 未テスト
	 */
	public int maximalRight(int l, java.util.function.LongPredicate f) {
		long prefixL = prefixSum(l);
		int a = 0;
		long res = strategy.identity();
		for (int i = Integer.highestOneBit(n); i >= 1; i /= 2) {
			if (a + i <= n) {
				long currentPrefix = strategy.mul(res, v[a + i]);
				if (a + i <= l || f.test(groupStrategy.mul(currentPrefix, groupStrategy.inverse(prefixL)))) {
					a += i;
					res = currentPrefix;
				}
			}
		}
		return a;
	}

	/**
	 * f(fold(l, r)) が真となる最小の l を返す。
	 * ここで fold(l, r) は A[l...r-1] の積。
	 * SegTree の仕様に合わせ、(l, r] に対する演算として、f(fold(l+1, r+1)) が真となる最小の l を返す。
	 *
	 * @param r 終了インデックス (0-indexed, inclusive)
	 * @param f 判定式
	 * @return 最小の l
	 * 計算量: O(log N)
	 * 未テスト
	 */
	public int minimalLeft(int r, java.util.function.LongPredicate f) {
		long prefixR = prefixSum(r + 1);
		int a = 0;
		long res = strategy.identity();
		for (int i = Integer.highestOneBit(n); i >= 1; i /= 2) {
			if (a + i <= r) {
				long nextRes = strategy.mul(res, v[a + i]);
				long rangeSum = groupStrategy.mul(prefixR, groupStrategy.inverse(nextRes));
				if (!f.test(rangeSum)) {
					a += i;
					res = nextRes;
				}
			}
		}
		return f.test(prefixR) ? -1 : a;
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
	@Override
	public void dump() {
		System.out.println("LongAbelianGroupBinaryIndexedTree { v: " + java.util.Arrays.toString(v) + ", n: " + n + " }");
	}
}
