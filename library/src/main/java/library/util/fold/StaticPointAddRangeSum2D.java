package library.util.fold;

import library.util.collections.HashMultiSet;

/**
 * https://atcoder.jp/contests/abc260/submissions/72090740
 */
public class StaticPointAddRangeSum2D extends PrefixSum2D {

	public StaticPointAddRangeSum2D(int H, int W) {
		super(H, W);
	}
	
	public StaticPointAddRangeSum2D(int[][] a) {
		super(a.length, a[0].length);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				add(i, j, a[i][j]);
			}
		}
	}
	
	public StaticPointAddRangeSum2D(long[][] a) {
		super(a.length, a[0].length);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				add(i, j, a[i][j]);
			}
		}
	}
	
	boolean built=false;
	
	public void add(int i, int j, long val) {
		super.add(i, j, val);
	}
	
	@Override
	public void build() {
		super.build();
		built=true;
	}
	
	/**
	 * minH,maxHはinclusive
	 * minW,maxWはexclusive
	 * @param minH
	 * @param minW
	 * @param maxH
	 * @param maxW
	 * @return
	 */	
	public long getRangeSum(int minH, int minW, int maxH, int maxW) {
		if(!built) {
			build();
		}
		if(minH<0)minH=0;//オーバーフロー対策
		if(minW<0)minW=0;//オーバーフロー対策
		if(minH>=maxH) return 0;
		if(minW>=maxW) return 0;
		int[] h=new int[] {maxH-1, minH-1};
		int[] w=new int[] {maxW-1, minW-1};
		long ret=0;
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				ret += get(h[i], w[j]) * ((i ^ j) == 0 ? 1 : -1);
			}
		}
		return ret;
	}

	/**
	 * 和が v となる長方形区間の個数を返す。
	 *
	 * @param v ターゲットとなる和
	 * @return #{(h1, w1, h2, w2) | 0 <= h1 < h2 <= H, 0 <= w1 < w2 <= W, \sum_{i=h1}^{h2-1} \sum_{j=w1}^{w2-1} a_{i,j} = v}
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 和が v となる区間の個数を返す。</li>
	 *   <li>副作用: !built の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(min(H, W)^2 * max(H, W))</li>
	 *   <li>破壊的変更: なし（build() による内部状態の変化を除く）。</li>
	 * </ul>
	 */
	public long countRangeSum(long v) {
		if (!built) {
			build();
		}
		long res = 0;
		if (H <= W) {
			for (int h1 = 0; h1 < H; h1++) {
				for (int h2 = h1 + 1; h2 <= H; h2++) {
					HashMultiSet<Long> counts = new HashMultiSet<>();
					counts.add(0L);
					for (int w = 0; w < W; w++) {
						long current = a[h2 - 1][w] - (h1 == 0 ? 0 : a[h1 - 1][w]);
						res += counts.getValue(current - v);
						counts.add(current);
					}
				}
			}
		} else {
			for (int w1 = 0; w1 < W; w1++) {
				for (int w2 = w1 + 1; w2 <= W; w2++) {
					HashMultiSet<Long> counts = new HashMultiSet<>();
					counts.add(0L);
					for (int h = 0; h < H; h++) {
						long current = a[h][w2 - 1] - (w1 == 0 ? 0 : a[h][w1 - 1]);
						res += counts.getValue(current - v);
						counts.add(current);
					}
				}
			}
		}
		return res;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(HW)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	@Override
	public void dump() {
		System.out.println("StaticPointAddRangeSum2D { a: " + java.util.Arrays.deepToString(a) + ", built: " + built + " }");
	}
}
