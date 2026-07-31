package library.util.segtree;

import library.util.algebra.strategy.longs.LongAddAbelianGroupStrategy;

public class RangeAddPointGetBinaryIndexedTree {
	LongAbelianGroupBinaryIndexedTree bit;
	int n;
	public RangeAddPointGetBinaryIndexedTree(int n) {
		this.bit = new LongAbelianGroupBinaryIndexedTree(n, LongAddAbelianGroupStrategy.STRATEGY);
		this.n = n;
	}

	public void rangeAdd(int l, int r, long val) {
		l = Math.max(0, l);
		r = Math.min(n, r);
		if (l >= r) return;
		bit.add(l, val);
		bit.add(r, -val);
	}

	public long get(int i) {
		if (i < 0 || i >= n) return 0;
		return bit.prefixSum(i + 1);
	}

	public void set(int i, long val) {
		if (i < 0 || i >= n) return;
		long diff = val - get(i);
		rangeAdd(i, i + 1, diff);
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
		System.out.print("RangeAddPointGetBinaryIndexedTree { n: " + n + ", bit: ");
		bit.dump();
		System.out.println(" }");
	}
}
