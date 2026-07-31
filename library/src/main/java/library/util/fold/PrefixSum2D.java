package library.util.fold;

abstract class PrefixSum2D {
	protected long[][] a;
	protected int H, W;
	
	public PrefixSum2D(int H, int W) {
		a=new long[H][W];
		this.H=H;
		this.W=W;
	}
	
	public void build() {
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j + 1 < a[0].length; ++j) {
				a[i][j + 1] += a[i][j];
			}
		}
		for (int j = 0; j < a[0].length; ++j) {
			for (int i = 0; i + 1 < a.length; ++i) {
				a[i + 1][j] += a[i][j];
			}
		}
	}
	
	protected void add(int i, int j, long val) {
		if (i < 0 || j < 0)throw new AssertionError();
		if (i >= a.length || j >= a[0].length) return;
		a[i][j] += val;
	}
	
	protected long get(int i, int j) {
		if (i < 0 || j < 0) return 0;
		i = Math.min(i, a.length - 1);
		j = Math.min(j, a[0].length - 1);
		return a[i][j];
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
	public void dump() {
		System.out.println("PrefixSum2D { a: " + java.util.Arrays.deepToString(a) + " }");
	}
}
