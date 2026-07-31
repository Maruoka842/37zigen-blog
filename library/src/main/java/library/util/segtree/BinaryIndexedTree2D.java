package library.util.segtree;

import java.util.Arrays;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * 2次元 Binary Indexed Tree (Fenwick Tree)。
 * 点更新と長方形領域の和取得を O(log H log W) で行う。
 */
public class BinaryIndexedTree2D {
	private final long[][] a;
	private final LongBinaryOperator op;
	private final LongUnaryOperator inv;
	private final long identity;
	private final int H, W;

	/**
	 * HxW の 2次元 Binary Indexed Tree を構築する。
	 * 加法群 (long, +, -, 0) を使用する。
	 * @param H 高さ
	 * @param W 幅
	 */
	public BinaryIndexedTree2D(int H, int W) {
		this(H, W, (x, y) -> x + y, x -> -x, 0L);
	}

	/**
	 * HxW の 2次元 Binary Indexed Tree を構築する。
	 * @param H 高さ
	 * @param W 幅
	 * @param op 二項演算（アーベル群の演算である必要がある）
	 * @param inv 逆元
	 * @param identity 単位元
	 */
	public BinaryIndexedTree2D(int H, int W, LongBinaryOperator op, LongUnaryOperator inv, long identity) {
		this.H = H;
		this.W = W;
		this.a = new long[H + 1][W + 1];
		if (identity != 0L) {
			for (int i = 0; i <= H; i++) {
				Arrays.fill(a[i], identity);
			}
		}
		this.op = op;
		this.inv = inv;
		this.identity = identity;
	}

	/**
	 * 座標 (h, w) に val を作用させる。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 0 <= h < H, 0 <= w < W</li>
	 *   <li>事後条件: A_{h,w} = op(A_{h,w}, val)</li>
	 *   <li>計算量: O(log H log W)</li>
	 *   <li>破壊的変更: あり</li>
	 * </ul>
	 * @param h 行インデックス (0-indexed)
	 * @param w 列インデックス (0-indexed)
	 * @param val 作用させる値
	 */
	public void add(int h, int w, long val) {
		if (h < 0 || h >= H || w < 0 || w >= W) return;
		for (int i = h + 1; i <= H; i += i & -i) {
			for (int j = w + 1; j <= W; j += j & -j) {
				a[i][j] = op.applyAsLong(a[i][j], val);
			}
		}
	}

	/**
	 * 領域 [0, h) x [0, w) の総和を返す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事後条件: \sum_{i=0}^{h-1} \sum_{j=0}^{w-1} A_{i,j} を返す。</li>
	 *   <li>計算量: O(log H log W)</li>
	 *   <li>破壊的変更: なし</li>
	 * </ul>
	 * @param h 高さ (0-indexed, exclusive)
	 * @param w 幅 (0-indexed, exclusive)
	 * @return 領域の和
	 */
	public long prefixSum(int h, int w) {
		long res = identity;
		h = Math.min(h, H);
		w = Math.min(w, W);
		for (int i = h; i > 0; i -= i & -i) {
			for (int j = w; j > 0; j -= j & -j) {
				res = op.applyAsLong(res, a[i][j]);
			}
		}
		return res;
	}

	/**
	 * 領域 [h1, h2) x [w1, w2) の総和を返す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事後条件: \sum_{i=h1}^{h2-1} \sum_{j=w1}^{w2-1} A_{i,j} を返す。</li>
	 *   <li>計算量: O(log H log W)</li>
	 *   <li>破壊的変更: なし</li>
	 * </ul>
	 * @param h1 開始行 (0-indexed, inclusive)
	 * @param w1 開始列 (0-indexed, inclusive)
	 * @param h2 終了行 (0-indexed, exclusive)
	 * @param w2 終了列 (0-indexed, exclusive)
	 * @return 領域の和
	 */
	public long sum(int h1, int w1, int h2, int w2) {
		if (h1 >= h2 || w1 >= w2) return identity;
		long res = prefixSum(h2, w2);
		res = op.applyAsLong(res, inv.applyAsLong(prefixSum(h1, w2)));
		res = op.applyAsLong(res, inv.applyAsLong(prefixSum(h2, w1)));
		res = op.applyAsLong(res, prefixSum(h1, w1));
		return res;
	}

	/**
	 * 指定した座標 (h, w) の現在の値を取得する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(log H log W)</li>
	 *   <li>破壊的変更: なし</li>
	 * </ul>
	 * @param h 行インデックス (0-indexed)
	 * @param w 列インデックス (0-indexed)
	 * @return 座標 (h, w) の値
	 */
	public long get(int h, int w) {
		return sum(h, w, h + 1, w + 1);
	}

	/**
	 * 全ての要素を identity で初期化する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(HW)</li>
	 *   <li>破壊的変更: あり</li>
	 * </ul>
	 */
	public void clear() {
		for (int i = 0; i <= H; i++) {
			Arrays.fill(a[i], identity);
		}
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
		System.out.println("BinaryIndexedTree2D { a: " + java.util.Arrays.deepToString(a) + ", H: " + H + ", W: " + W + " }");
	}
}
