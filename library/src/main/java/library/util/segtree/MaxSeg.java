package library.util.segtree;

public class MaxSeg extends SegTreelong {
//https://atcoder.jp/contests/abc234/submissions/74021821
	public MaxSeg(int n) {
		super(n, Long::max, Long.MIN_VALUE/3);
	}
	
	/**
	 * from ≤ i かつ a[i] ≥ v を満たす最小の i を返す。存在しないときは a.length を返す。
	 * @param from
	 * @param v
	 * @return
	 */
	public int firstGeqPos(int from, long v) {
		 return super.maximalRight(from, x -> x < v);
	}
	
	/**
	 * from ≥ i かつ a[i] ≥ v を満たす最大の i を返す。存在しないときは -1 を返す。
	 * @param from
	 * @param v
	 * @return
	 */	public int lastGeqPos(int from, long v) {
		return super.minimalLeft(from, x -> x < v);
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
		System.out.print("MaxSeg: ");
		super.dump();
	}
}
