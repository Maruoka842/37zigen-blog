package library.util.fold;

import java.util.Arrays;
import java.util.function.LongBinaryOperator;

import library.util.MathUtils;

public class SparseTableLong {
	private long[][] accum;
	LongBinaryOperator op;

	public SparseTableLong(long[] v, LongBinaryOperator op) {
		this.op = op;
		build(v);
	}
	
	private void build(long[] v) {
		int log = 0;
		while (1<<log < v.length) log++;
		accum = new long[log + 1][v.length];
		this.accum[0] = v.clone();
		for (int i = 0; i+1 <= log; i++) {
			for (int j = 0; j + (1 << (i+1)) <= v.length; j++) {
				accum[i+1][j] = op.applyAsLong(accum[i][j], accum[i][j + (1 << i)]);
			}
		}
	}
	
	/**
	 * [l, r)
	 * @param l
	 * @param r
	 * @return
	 */
	public long fold(int l, int r) {
		int w=MathUtils.floorLog2(r-l);
		return op.applyAsLong(accum[w][l], accum[w][r-(1<<w)]);
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \log N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("SparseTableLong { accum: " + java.util.Arrays.deepToString(accum) + " }");
	}
}
