package library.util.fold;

import java.util.Arrays;
import java.util.function.LongBinaryOperator;

import library.util.ArrayUtils;
import library.util.MathUtils;
/**
 * H=W=1000で1GB超えた....
 */
public class SparseTable2DLong {
	private long[][][][] accum;
	LongBinaryOperator op;
	int H, W;
	
	public SparseTable2DLong(long[][] v, LongBinaryOperator op) {
		this.op = op;
		build(v);
	}
	
	private void build(long[][] v) {
		this.H = v.length;
		this.W = v[0].length;
		int logH = 0;
		int logW = 0;
		while (1<<logH < H) logH++;
		while (1<<logW < W) logW++;
		accum = new long[logH + 1][logW + 1][v.length][v[0].length];
		this.accum[0][0] = ArrayUtils.copy(v);
		for (int i = 0; i+1 <= logH; i++) {
			for (int h = 0; h+(1<<(i+1)) <= H; h++) {
				for (int w = 0; w < W; w++) {
					accum[i+1][0][h][w] = op.applyAsLong(accum[i][0][h][w], accum[i][0][h+(1<<i)][w]);
				}
			}
		}
		for(int i=0;i<=logH;++i) {
			for (int j = 0; j+1 <= logW; j++) {
				for (int h = 0; h < H; h++) {
					for (int w = 0; w +(1<<(j+1)) <= W; w++) {
						accum[i][j+1][h][w] = op.applyAsLong(accum[i][j][h][w], accum[i][j][h][w+(1<<j)]);
					}
				}
			}
		}
	}
	
	/**
	 * [i0, i1) × [j0, j1)
	 */
	public long fold(int i0, int j0, int i1, int j1) {
		int wi=MathUtils.floorLog2(i1-i0);
		int wj=MathUtils.floorLog2(j1-j0);
		return op.applyAsLong(op.applyAsLong(accum[wi][wj][i0][j0], accum[wi][wj][i0][j1-(1<<wj)]), op.applyAsLong(accum[wi][wj][i1-(1<<wi)][j0], accum[wi][wj][i1-(1<<wi)][j1-(1<<wj)]));
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(HW \log H \log W)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("SparseTable2DLong { accum: " + java.util.Arrays.deepToString(accum) + " }");
	}
}