package library.util.segtree;

/**
 * 点集合 a に対して
 * get(i) = min_j (|i - j| + a[j])
 * set(i, v) = a[i] <- v
 * を行うデータ構造。
 *
 * 変形すると
 * j <= i: |i-j| + a[j] = i + (a[j] - j)
 * j >= i: |i-j| + a[j] = -i + (a[j] + j)
 * なので、prefix の min(a[j]-j) と suffix の min(a[j]+j) を管理すればよい。
 * 
 * 未テスト
 */
public class L1DistanceMinQuery {
	public static final long INF = Long.MAX_VALUE / 3;

	private final int n;
	// leftMin[j] = a[j] - j を持つ。j <= i 側の候補を処理する。
	private final SegTreelong leftMin;
	// rightMin[j] = a[j] + j を持つ。j >= i 側の候補を処理する。
	private final SegTreelong rightMin;

	/**
	 * 長さ {@code n} の配列を、全要素 {@link #INF} で初期化して構築する。
	 *
	 * @param n 配列長
	 */
	public L1DistanceMinQuery(int n) {
		this.n = n;
		this.leftMin = new SegTreelong(n, Math::min, INF);
		this.rightMin = new SegTreelong(n, Math::min, INF);
		this.leftMin.fill(INF);
		this.rightMin.fill(INF);
	}

	/**
	 * 初期配列 {@code a} から構築する。
	 *
	 * @param a 初期値配列
	 */
	public L1DistanceMinQuery(long[] a) {
		this(a.length);
		build(a);
	}

	/**
	 * 配列全体を {@code a} で再構築する。
	 *
	 * @param a 再構築に使う配列
	 * @throws IllegalArgumentException 長さが構築時の長さと一致しない場合
	 */
	public void build(long[] a) {
		if (a.length != n) throw new IllegalArgumentException("length mismatch");
		long[] left = new long[n];
		long[] right = new long[n];
		for (int i = 0; i < n; i++) {
			left[i] = encodeLeft(i, a[i]);
			right[i] = encodeRight(i, a[i]);
		}
		leftMin.build(left);
		rightMin.build(right);
	}

	/**
	 * {@code a[i] = v} に更新する。
	 *
	 * @param i 更新位置
	 * @param v 更新後の値
	 * @throws IndexOutOfBoundsException {@code i} が範囲外の場合
	 */
	public void set(int i, long v) {
		checkIndex(i);
		leftMin.set(i, encodeLeft(i, v));
		rightMin.set(i, encodeRight(i, v));
	}

	/**
	 * {@code min_j (|i-j| + a[j])} を返す。
	 *
	 * @param i 問い合わせ位置
	 * @return {@code min_j (|i-j| + a[j])}
	 * @throws IndexOutOfBoundsException {@code i} が範囲外の場合
	 */
	public long get(int i) {
		checkIndex(i);
		long ans = INF;
		// j <= i なら |i-j| + a[j] = i + (a[j] - j)
		long left = leftMin.fold(0, i + 1);
		if (left < INF) ans = Math.min(ans, left + i);
		// j >= i なら |i-j| + a[j] = -i + (a[j] + j)
		long right = rightMin.fold(i, n);
		if (right < INF) ans = Math.min(ans, right - i);
		return ans;
	}

	/**
	 * 円環長 {@code n} における
	 * {@code min_j (min(|i-j|, n-|i-j|) + a[j])} を返す。
	 *
	 * @param i 問い合わせ位置
	 * @return 円環距離を使った最小値
	 * @throws IndexOutOfBoundsException {@code i} が範囲外の場合
	 */
	public long getCircular(int i) {
		checkIndex(i);
		long ans = INF;
		int half = n / 2;

		// j <= i のうち、右（プラス）へ進む方が近い区間 [max(0, i-half), i]
		int directLeftL = Math.max(0, i - half);
		long directLeft = leftMin.fold(directLeftL, i + 1);
		if (directLeft < INF) ans = Math.min(ans, directLeft + i);

		// j <= i のうち、左（マイナス）へ進む方が近い区間 [0, max(0, i-half))
		long wrapLeft = rightMin.fold(0, directLeftL);
		if (wrapLeft < INF) ans = Math.min(ans, wrapLeft + (n - i));

		// j >= i のうち、左へ進む方が近い区間 [i, min(n, i+half+1))
		int directRightR = Math.min(n, i + half + 1);
		long directRight = rightMin.fold(i, directRightR);
		if (directRight < INF) ans = Math.min(ans, directRight - i);

		// j >= i のうち、右へ進む方が近い区間 [min(n, i+half+1), n)
		long wrapRight = leftMin.fold(directRightR, n);
		if (wrapRight < INF) ans = Math.min(ans, wrapRight + (n + i));

		return ans;
	}

	/**
	 * 現在の生配列の値 {@code a[i]} を返す。
	 *
	 * @param i 取得位置
	 * @return 現在の {@code a[i]}。未設定なら {@link #INF}
	 * @throws IndexOutOfBoundsException {@code i} が範囲外の場合
	 */
	public long rawGet(int i) {
		checkIndex(i);
		long left = leftMin.get(i);
		if (left >= INF) return INF;
		return left + i;
	}

	/**
	 * 配列長を返す。
	 *
	 * @return 配列長
	 */
	public int size() {
		return n;
	}

	private long encodeLeft(int i, long v) {
		if (v >= INF) return INF;
		return v - i;
	}

	private long encodeRight(int i, long v) {
		if (v >= INF - i) return INF;
		return v + i;
	}

	private void checkIndex(int i) {
		if (i < 0 || i >= n) {
			throw new IndexOutOfBoundsException("index " + i + " is out of range [0, " + (n - 1) + "]");
		}
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
		System.out.println("L1DistanceMinQuery { n: " + n + " }");
		System.out.print("  leftMin: ");
		leftMin.dump();
		System.out.print("  rightMin: ");
		rightMin.dump();
	}
}
