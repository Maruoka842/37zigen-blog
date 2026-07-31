package library.util.segtree;

import java.util.Arrays;

/**
 * 区間更新（乗算）・区間積を O(log N) で行うセグメント木。
 *
 * <p>yosupo氏のブログ (https://yosupo.hatenablog.com/entry/2024/03/19/213339) に基づく実装。
 * 遅延伝搬を明示的に行わない手法（非可換性がない場合に利用可能）を用い、
 * 累乗計算を工夫することで各クエリ O(log N) を達成する。</p>
 *
 * <p>ノード \( u \) は自身の区間の総積 \( a_u \) と、その区間全体に掛かっている係数 \( b_u \) を持つ。
 * \( a_u \) は自身の subtree 内のすべての \( b \) を反映済みだが、先祖の \( b \) は未反映である。
 * クエリ範囲 \([l, r)\) の総積は、分解された各ノード \( u \) について
 * \( a_u \times (\prod_{v \in ancestors(u)} b_v)^{len(u)} \) の積となる。</p>
 */
public class RangeMulRangeProduct {
	/** セグメント木のサイズ（要素数以上の最小の 2 冪）。 */
	private final int n;
	/** セグメント木の高さ (log2 n)。 */
	private final int H;
	private final int inputN;
	private final long mod;
	private final long[] a; // a_u: 区間の総積（自身の subtree 内の b は反映済み、先祖の b は未反映）
	private final long[] b; // b_u: そのノード全体に掛かっている係数

	/**
	 * 指定されたサイズと法でセグメント木を初期化する。
	 * 計算量: O(N)
	 * @param n_ 要素数
	 * @param mod 法
	 */
	public RangeMulRangeProduct(int n_, long mod) {
		this.inputN = n_;
		int tempN = 1;
		int tempH = 0;
		while (tempN < n_) {
			tempN <<= 1;
			tempH++;
		}
		this.n = tempN;
		this.H = tempH;
		this.mod = mod;
		this.a = new long[2 * n];
		this.b = new long[2 * n];
		Arrays.fill(a, 1 % mod);
		Arrays.fill(b, 1 % mod);
	}

	/**
	 * 初期値を設定する。初期値が指定されない要素は 1 とされる。
	 * 計算量: O(N)
	 * @param initial 初期値の配列
	 */
	public void build(long[] initial) {
		for (int i = 0; i < n; i++) {
			a[n + i] = (i < initial.length) ? initial[i] % mod : 1 % mod;
			b[n + i] = 1 % mod;
		}
		for (int i = n - 1; i >= 1; i--) {
			a[i] = a[2 * i] * a[2 * i + 1] % mod;
			b[i] = 1 % mod;
		}
	}

	/**
	 * 半開区間 [l, r) の各要素を x 倍する。
	 * 計算量: O(log N)
	 * 未テスト
	 * @param l 左端（含む）
	 * @param r 右端（含まない）
	 * @param x 乗数
	 */
	public void mul(int l, int r, long x) {
		if (l < 0) l = 0;
		if (r > inputN) r = inputN;
		if (l >= r) return;
		x %= mod;
		if (x < 0) x += mod;
		if (x == 1 % mod) return;

		long[] x_powers = new long[H + 1];
		x_powers[0] = x;
		for (int i = 1; i <= H; i++) {
			x_powers[i] = x_powers[i - 1] * x_powers[i - 1] % mod;
		}

		mul(1, 0, n, l, r, x_powers, H);
	}

	private long mul(int u, int L, int R, int l, int r, long[] x_powers, int h) {
		if (l <= L && R <= r) {
			b[u] = b[u] * x_powers[0] % mod;
			long res = x_powers[h];
			a[u] = a[u] * res % mod;
			return res;
		}
		int mid = (L + R) / 2;
		long res = 1;
		if (l < mid) res = res * mul(2 * u, L, mid, l, r, x_powers, h - 1) % mod;
		if (r > mid) res = res * mul(2 * u + 1, mid, R, l, r, x_powers, h - 1) % mod;
		a[u] = a[u] * res % mod;
		return res;
	}

	/**
	 * 半開区間 [l, r) の総積を求める。
	 * 計算量: O(log N)
	 * 未テスト
	 * @param l 左端（含む）
	 * @param r 右端（含まない）
	 * @return 総積 mod mod
	 */
	public long prod(int l, int r) {
		if (l < 0) l = 0;
		if (r > inputN) r = inputN;
		if (l >= r) return 1 % mod;
		long[] Q = new long[H + 1];
		Arrays.fill(Q, 1 % mod);
		long res_a = prod(1, 0, n, l, r, 1 % mod, H, Q);

		long res_b_powers = 1;
		for (int k = H; k >= 0; k--) {
			res_b_powers = res_b_powers * res_b_powers % mod;
			if (Q[k] != 1 % mod) res_b_powers = res_b_powers * Q[k] % mod;
		}

		return res_a * res_b_powers % mod;
	}

	private long prod(int u, int L, int R, int l, int r, long current_b, int h, long[] Q) {
		if (l <= L && R <= r) {
			Q[h] = Q[h] * current_b % mod;
			return a[u];
		}
		int mid = (L + R) / 2;
		long next_b = current_b * b[u] % mod;
		long res = 1;
		if (l < mid) res = res * prod(2 * u, L, mid, l, r, next_b, h - 1, Q) % mod;
		if (r > mid) res = res * prod(2 * u + 1, mid, R, l, r, next_b, h - 1, Q) % mod;
		return res;
	}

	/**
	 * インデックス i の要素の値を返す。
	 * 計算量: O(log N)
	 * 未テスト
	 * @param i インデックス
	 * @return a[i] mod mod
	 */
	public long get(int i) {
		return prod(i, i + 1);
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
		System.out.println("RangeMulRangeProduct { inputN: " + inputN + ", n: " + n + ", a: " + java.util.Arrays.toString(a) + ", b: " + java.util.Arrays.toString(b) + " }");
	}
}
