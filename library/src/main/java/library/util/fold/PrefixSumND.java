package library.util.fold;

/**
 * N次元累積和の基底クラス。
 */
abstract class PrefixSumND {
	protected final long[] data;
	protected final int[] dims;
	protected final int[] strides;
	protected final int N;

	/**
	 * 各次元のサイズを指定して構築する。
	 * @param dims 各次元のサイズ
	 */
	public PrefixSumND(int... dims) {
		this.N = dims.length;
		this.dims = dims.clone();
		this.strides = new int[N];
		long total = 1;
		for (int i = N - 1; i >= 0; i--) {
			strides[i] = (int) total;
			total *= dims[i];
		}
		if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Total size too large");
		this.data = new long[(int) total];
	}

	/**
	 * 累積和を構築する。
	 * O(N * Π dims)
	 */
	protected void build() {
		for (int k = 0; k < N; k++) {
			int s = strides[k];
			int d = dims[k];
			for (int i = 0; i < data.length; i++) {
				if ((i / s) % d > 0) {
					data[i] += data[i - s];
				}
			}
		}
	}

	/**
	 * 指定した座標に値を加算する。
	 * @param coords 座標
	 * @param val 値
	 */
	protected void add(int[] coords, long val) {
		int idx = 0;
		for (int i = 0; i < N; i++) {
			if (coords[i] < 0 || coords[i] >= dims[i]) return;
			idx += coords[i] * strides[i];
		}
		data[idx] += val;
	}

	/**
	 * 指定した座標（0から各座標までの閉領域）の累積和を取得する。
	 * 負の座標が含まれる場合は 0 を返す。
	 * @param coords 座標
	 * @return 累積和
	 */
	protected long getPrefixSum(int[] coords) {
		int idx = 0;
		for (int i = 0; i < N; i++) {
			if (coords[i] < 0) return 0;
			int c = Math.min(coords[i], dims[i] - 1);
			idx += c * strides[i];
		}
		return data[idx];
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(\prod dims)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("PrefixSumND { dims: " + java.util.Arrays.toString(dims) + ", data: " + java.util.Arrays.toString(data) + " }");
	}
}
