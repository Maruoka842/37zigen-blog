package library.util.fold;

/**
 * 剰余環 Z/mZ 上での 1次元傾き加算および一点取得を O(1) で処理する静的データ構造。
 *
 * <p>数学的表記: 1次元配列 A の第 i 要素に対して、区間 [l, r) や円環上での傾き、距離に応じた加算を O(1) で行い、
 * ビルド後に任意の第 i 要素 A[i] mod M の値を O(1) で取得します。</p>
 */
public class StaticSlopeAddPointGet1DZn {
	/** 配列の要素数。 */
	int N;
	/** 法 m。 */
	long mod;
	/** 1次式の定数項 A(x) = Σb0[i]x^i。 */
	long[] b0;
	/** 1次式の傾き項 B(x) = Σb1[i]x^i。 */
	long[] b1;
	/** ビルド後の最終結果配列 A[i] mod M。 */
	long[] sum;
	/** すでにビルドされたかどうかの状態フラグ。 */
	boolean isBuilt = false;

	/**
	 * コンストラクタ。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(N)</li>
	 * </ul>
	 *
	 * @param N 配列の要素数
	 * @param mod 法
	 */
	// 未テスト
	public StaticSlopeAddPointGet1DZn(int N, long mod) {
		if (mod <= 0) throw new IllegalArgumentException("mod must be positive");
		this.N = N;
		this.mod = mod;
		b0 = new long[N];
		b1 = new long[N];
		sum = new long[N];
	}

	/**
	 * i番目(0-indexed)の要素に val * |i - center| mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param val 加算する値
	 * @param center 距離の基準点
	 */
	// 未テスト
	public void addDistanceFrom(long val, int center) {
		if (isBuilt) throw new IllegalStateException("Already built");
		val = (val % mod + mod) % mod;
		long negVal = (mod - val) % mod;
		long twoVal = (val * 2) % mod;
		long term0 = (val * (center + 1)) % mod;

		addAtB1(0, negVal);
		addAtB1(center + 1, twoVal);
		addAtB0(0, term0);
	}

	/**
	 * 円環上で center からの距離に a を掛けた値 a * min(|i-center|, N-|i-center|) mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param a 加算する値の係数
	 * @param center 距離 of 基準点
	 */
	// 未テスト
	public void addCircularDistanceFrom(long a, int center) {
		if (isBuilt) throw new IllegalStateException("Already built");
		center = (center % N + N) % N;
		long termRight = ((N / 2) * (a % mod)) % mod;
		if (termRight < 0) termRight += mod;

		for (int c : new int[] {center - N, center}) {
			addSlope(c, c + (N + 1) / 2, a, 0);
			addSlope(c + (N + 1) / 2, c + N, -a, termRight);
		}
	}

	/**
	 * 円環上を正の向き（インデックスが増加する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(i) = a * ((i - center) mod N) (0 <= i < N) です。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param a 加算する値の係数
	 * @param center 距離の基準点
	 */
	// 未テスト
	public void addCircularDistanceFromPositive(long a, int center) {
		if (isBuilt) throw new IllegalStateException("Already built");
		center = (center % N + N) % N;
		for (int c : new int[] { center - N, center }) {
			addSlope(c, c + N, a, 0);
		}
	}

	/**
	 * 円環上を負の向き（インデックスが減少する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(i) = a * ((center - i) mod N) (0 <= i < N) です。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param a 加算する値の係数
	 * @param center 距離の基準点
	 */
	// 未テスト
	public void addCircularDistanceFromNegative(long a, int center) {
		if (isBuilt) throw new IllegalStateException("Already built");
		center = (center % N + N) % N;
		long term = ((N - 1) * (a % mod)) % mod;
		if (term < 0) term += mod;
		for (int c : new int[] { center - N, center }) {
			addSlope(c + 1, c + 1 + N, -a, term);
		}
	}

	/**
	 * i番目(0-indexed)の要素に a * max(0, i-b) mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param a 加算する値の係数
	 * @param b 起点
	 */
	// 未テスト
	public void addRightRamp(long a, int b) {
		if (isBuilt) throw new IllegalStateException("Already built");
		if (b >= N - 1) return;
		addAtB1(b + 1, a);
	}

	/**
	 * i番目(0-indexed)の要素に a * max(0, -i+b) mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param a 加算する値の係数
	 * @param b 起点
	 */
	// 未テスト
	public void addLeftRamp(long a, int b) {
		if (isBuilt) throw new IllegalStateException("Already built");
		long term0 = (a % mod) * (b + 1) % mod;
		addAtB0(0, term0);
		addAtB1(0, -a);
		addAtB1(b + 1, a);
	}

	/**
	 * [l, r) に定数 c mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param l 区間の左端（包含）
	 * @param r 区間の右端（非包含）
	 * @param c 加算する定数
	 */
	// 未テスト
	public void addConstant(int l, int r, long c) {
		addSlope(l, r, 0, c);
	}

	/**
	 * min(x - l, r - x, a) * scale を [l, r] に加算する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param l 範囲の左端
	 * @param r 範囲の右端（包含）
	 * @param a 山の最大高さ
	 * @param scale 拡大係数
	 */
	// 未テスト
	public void addMountain(int l, int r, int a, long scale) {
		//https://atcoder.jp/contests/abc468/submissions/77893587
		if (isBuilt) throw new IllegalStateException("Already built");
		if (l > r || a <= 0) return;
		scale = (scale % mod + mod) % mod;

		int mL = Math.min(a, (r - l + 1) / 2);
		if (mL > 0) {
			// Left ascending slope on [l, l + mL)
			addSlope(l, l + mL, scale, 0);
		}

		// Flat peak on [l + mL, r - mL + 1)
		int peakL = l + mL;
		int peakR = r - mL + 1;
		if (peakL < peakR) {
			long peakVal = (mL * scale) % mod;
			addConstant(peakL, peakR, peakVal);
		}

		if (mL > 0) {
			// Right descending slope on [r - mL + 1, r + 1)
			long startVal = ((mL - 1) * scale) % mod;
			addSlope(r - mL + 1, r + 1, -scale, startVal);
		}
	}

	/**
	 * [l, r) に (a * (x - l) + b) mod M を足す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param l 区間の左端（包含）
	 * @param r 区間の右端（非包含）
	 * @param a 傾き
	 * @param b 初項
	 */
	// 未テスト
	public void addSlope(int l, int r, long a, long b) {
		if (isBuilt) throw new IllegalStateException("Already built");
		a = (a % mod + mod) % mod;
		b = (b % mod + mod) % mod;

		if (l < 0) {
			long term = (a * (-l)) % mod;
			b = (b + term) % mod;
			l = 0;
		}
		r = Math.min(r, N);
		if (l >= r) return;
		if (l >= N) return;

		addAtB0(l, b);
		addAtB1(l + 1, a);
		if (r < N) {
			addAtB1(r, -a);
			long termSubtract = (a * (r - l - 1) + b) % mod;
			addAtB0(r, -termSubtract);
		}
	}

	/**
	 * 累積和を計算してデータ構造をビルドする。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(N)</li>
	 * </ul>
	 */
	// 未テスト
	public void build() {
		if (isBuilt) throw new AssertionError("Already built");

		long s = 0;
		for (int i = 0; i < N; i++) {
			s = (s + b1[i]) % mod;
			sum[i] = s;
		}
		for (int i = 0; i < N; i++) {
			sum[i] = (sum[i] + b0[i]) % mod;
		}
		s = 0;
		for (int i = 0; i < N; i++) {
			s = (s + sum[i]) % mod;
			sum[i] = s;
		}
		isBuilt = true;
	}

	/**
	 * 添字 i の要素の値を返す。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 *
	 * @param i 取得する位置の添字
	 * @return A[i] mod M
	 */
	// 未テスト
	public long get(int i) {
		if (!isBuilt) {
			build();
		}
		return sum[i];
	}

	/**
	 * 全要素の値を配列として取得する。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(N) またはビルド済みの場合は O(1)</li>
	 * </ul>
	 *
	 * @return ビルド後の配列
	 */
	// 未テスト
	public long[] values() {
		if (!isBuilt) {
			build();
		}
		return sum;
	}

	/**
	 * 内部状態を標準出力に出力するデバッグ用メソッド。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(N)</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("b0");
		for (int i = 0; i < b0.length; i++) {
			System.out.print(b0[i] + (i == b0.length - 1 ? "\n" : " "));
		}
		System.out.println("b1");
		for (int i = 0; i < b1.length; i++) {
			System.out.print(b1[i] + (i == b1.length - 1 ? "\n" : " "));
		}
		if (isBuilt) {
			System.out.println("ビルド後の値");
			for (int i = 0; i < sum.length; i++) {
				System.out.print(sum[i] + (i == sum.length - 1 ? "\n" : " "));
			}
		}
	}

	/**
	 * b0の指定位置に加算するプライベートヘルパー。
	 */
	private void addAtB0(int idx, long val) {
		if (idx < 0 || idx >= N) return;
		long nv = (b0[idx] + val) % mod;
		if (nv < 0) nv += mod;
		b0[idx] = nv;
	}

	/**
	 * b1の指定位置に加算するプライベートヘルパー。
	 */
	private void addAtB1(int idx, long val) {
		if (idx < 0 || idx >= N) return;
		long nv = (b1[idx] + val) % mod;
		if (nv < 0) nv += mod;
		b1[idx] = nv;
	}
}
