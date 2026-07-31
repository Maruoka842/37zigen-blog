package library.util.fold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import library.util.seq.SortedArrays;

/**
 * StaticSlopeAddPointGet1D と同等の一変数の区間1次関数（スロープ）加算および点更新・取得クエリをサポートするデータ構造です。
 * 座標が非常に大きい場合（最大 10^18 程度）でも、オフラインで ArrayList に変更点を追加してソートし、
 * 尺取り法（2ポインタ）による座標圧縮を行うことで、メモリを抑えつつ高速に動作します。
 *
 * <p>契約:</p>
 * <ul>
 *   <li>事前条件: 特になし。</li>
 *   <li>事後条件: なし。</li>
 *   <li>副作用: なし。</li>
 *   <li>計算量: 構築（build）は O(M log M)、1点取得（get）は O(log M)（M は変更点の総数）。</li>
 * </ul>
 */
public class StaticSlopeAddPointGetLarge1D {

	/**
	 * 変更点情報を表す内部クラス。
	 * X >= x なる座標 X に対して
	 * b_0(X-x+1)+b_1
	 * を足す。
	 */
	private static class Update implements Comparable<Update> {
		final long x;
		long b0;
		long b1;

		Update(long x, long b0, long b1) {
			this.x = x;
			this.b0 = b0;
			this.b1 = b1;
		}

		@Override
		public int compareTo(Update o) {
			return Long.compare(this.x, o.x);
		}
	}

	/**
	 * 追加された高レベルな操作情報を表すインターフェース。
	 */
	private interface Operation {
		long getValue(long x);
		long getSlopeAtInfLeft();
	}

	/** -inf における傾き */
	private long initSlope = 0L;

	/** -inf における切片（x = 0 における値） */
	private long initOffset = 0L;

	/** 変更情報のリスト */
	private final List<Update> updates = new ArrayList<>();

	/** 追加された操作のリスト */
	private final List<Operation> operations = new ArrayList<>();

	/** ソートされたユニークな変更点の座標配列 */
	private long[] keys;

	/** 各変更点における累積の 1 次係数の総和 */
	private long[] SD;

	/** 各変更点における評価値の累積総和 */
	private long[] F;

	/** 構築済みフラグ */
	private boolean isBuilt = false;

	/**
	 * 最小 of 座標境界を 0 として初期化します。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public StaticSlopeAddPointGetLarge1D() {
	}

	/**
	 * 半開区間 [l, r) に a * (x - l) + b を加算します。
	 *
	 * @param l 区間の左端（包括）
	 * @param r 区間の右端（除外）
	 * @param a 傾き
	 * @param b 切片
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。l < r であること（l >= r の場合は何も行いません）。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addSlope(final long l, final long r, final long a, final long b) {
		if (isBuilt) {
			throw new IllegalStateException("Cannot modify the structure after build() has been called.");
		}
		if (l >= r) {
			return;
		}
		updates.add(new Update(l, b, 0L));
		updates.add(new Update(l + 1, 0L, a));
		updates.add(new Update(r, 0L, -a));
		updates.add(new Update(r, -(a * (r - l - 1) + b), 0L));
		operations.add(new Operation() {
			@Override
			public long getValue(long x) {
				if (x < l || x >= r) {
					return 0L;
				}
				return a * (x - l) + b;
			}

			@Override
			public long getSlopeAtInfLeft() {
				return 0L;
			}
		});
	}

	/**
	 * val * |x - center| を加算します。
	 *
	 * @param val 加算する値（スケール）
	 * @param center 中心点
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addDistanceFrom(final long val, final long center) {
		if (isBuilt) {
			throw new IllegalStateException("Cannot modify the structure after build() has been called.");
		}
		updates.add(new Update(center + 1, 0L, 2 * val));
		operations.add(new Operation() {
			@Override
			public long getValue(long x) {
				return val * Math.abs(x - center);
			}

			@Override
			public long getSlopeAtInfLeft() {
				return -val;
			}
		});
	}

	/**
	 * 半開区間 [l, r) と [l2, r2) の共通部分に対して a * (x - l) + b を加算します。
	 *
	 * @param l 区間の左端
	 * @param r 区間の右端
	 * @param a 傾き
	 * @param b 切片
	 * @param l2 切り詰め基準区間の左端
	 * @param r2 切り詰め基準区間の右端
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	private void addSlopeTruncated(long l, long r, long a, long b, long l2, long r2) {
		if (l < l2) {
			// 導出:
			// 加算する関数を f(x) = a * (x - l) + b とする。
			// l < l2 の場合、新たな開始点 l' = l2 における関数の値 f(l') からスタートする必要がある。
			// 新たな関数を f'(x) = a * (x - l') + b' と表したとき、任意の x に対して f'(x) = f(x) が成り立つためには、
			// a * (x - l2) + b' = a * (x - l) + b
			// a * x - a * l2 + b' = a * x - a * l + b
			// b' = a * l2 - a * l + b
			// b' = a * (l2 - l) + b
			// となり、新たな切片 b' は b += a * (l2 - l) で求まる。
			b += a * (l2 - l);
			l = l2;
		}
		r = Math.min(r, r2);
		if (l >= r) {
			return;
		}
		addSlope(l, r, a, b);
	}

	/**
	 * 円環上で center からの距離に a を掛けた値 a * min(|x - center|, N - |x - center|) を加算します。
	 *
	 * @param a 傾きの強さ
	 * @param center 中心点
	 * @param N 円環のサイズ（周長）
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addCircularDistanceFrom(long a, long center, long N) {
		center = (center % N + N) % N;
		for (long c : new long[] { center - N, center }) {
			addSlopeTruncated(c, c + (N + 1) / 2, a, 0, 0, N);
			addSlopeTruncated(c + (N + 1) / 2, c + N, -a, (N / 2) * a, 0, N);
		}
	}

	/**
	 * 円環上を正の向き（インデックスが増加する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(x) = a * ((x - center) mod N) (0 <= x < N) です。
	 *
	 * @param a 傾き
	 * @param center 中心点
	 * @param N 円環のサイズ（周長）
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addCircularDistanceFromPositive(long a, long center, long N) {
		center = (center % N + N) % N;
		for (long c : new long[] { center - N, center }) {
			addSlopeTruncated(c, c + N, a, 0, 0, N);
		}
	}

	/**
	 * 円環上を負の向き（インデックスが減少する方向）にのみ移動する場合の、center からの距離に a を乗じた値を加算します。
	 * 加算される関数は f(x) = a * ((center - x) mod N) (0 <= x < N) です。
	 *
	 * @param a 傾き
	 * @param center 中心点
	 * @param N 円環のサイズ（周長）
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addCircularDistanceFromNegative(long a, long center, long N) {
		center = (center % N + N) % N;
		for (long c : new long[] { center - N, center }) {
			addSlopeTruncated(c + 1, c + 1 + N, -a, (N - 1) * a, 0, N);
		}
	}

	/**
	 * x >= b + 1 の領域に対し、a * max(0, x - b) を加算します。
	 *
	 * @param a 傾き
	 * @param b 基準となる座標
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addRightRamp(final long a, final long b) {
		if (isBuilt) {
			throw new IllegalStateException("Cannot modify the structure after build() has been called.");
		}
		updates.add(new Update(b + 1, 0L, a));
		operations.add(new Operation() {
			@Override
			public long getValue(long x) {
				return a * Math.max(0L, x - b);
			}

			@Override
			public long getSlopeAtInfLeft() {
				return 0L;
			}
		});
	}

	/**
	 * a * max(0, b - x) を加算します。
	 *
	 * @param a 傾き
	 * @param b 基準となる座標
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること。</li>
	 *   <li>計算量: O(1)</li>
	 * </ul>
	 */
	// 未テスト
	public void addLeftRamp(final long a, final long b) {
		if (isBuilt) {
			throw new IllegalStateException("Cannot modify the structure after build() has been called.");
		}
		updates.add(new Update(b + 1, 0L, a));
		operations.add(new Operation() {
			@Override
			public long getValue(long x) {
				return a * Math.max(0L, b - x);
			}

			@Override
			public long getSlopeAtInfLeft() {
				return -a;
			}
		});
	}

	/**
	 * 加算された情報を構築し、get クエリが実行可能な状態にします。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: 構築（build）前であること（すでに構築済みの場合は AssertionError を投げます）。</li>
	 *   <li>計算量: O(M log M) （M は変更点の数）</li>
	 * </ul>
	 */
	// 未テスト
	public void build() {
		if (isBuilt) {
			throw new AssertionError("Already built.");
		}

		int n = updates.size();
		if (n == 0) {
			keys = new long[0];
			F = new long[0];
			SD = new long[0];
			long totalValAt0 = 0L;
			for (Operation op : operations) {
				totalValAt0 += op.getValue(0L);
			}
			initOffset = totalValAt0;
			isBuilt = true;
			return;
		}

		// 変更点を座標順にソート
		Collections.sort(updates);

		// 尺取り法（2ポインタ）でユニークな座標の個数をカウント
		int m = 0;
		for (int i = 0; i < n; ) {
			int j = i;
			while (j < n && updates.get(j).x == updates.get(i).x) {
				j++;
			}
			m++;
			i = j;
		}

		keys = new long[m];
		F = new long[m];
		SD = new long[m];

		long[] b0 = new long[m];
		long[] b1 = new long[m];

		// 尺取り法（2ポインタ）でユニークな座標ごとに変更情報をマージ
		int idx = 0;
		for (int i = 0; i < n; ) {
			int j = i;
			long currentX = updates.get(i).x;
			long sumB0 = 0;
			long sumB1 = 0;
			while (j < n && updates.get(j).x == currentX) {
				sumB0 += updates.get(j).b0;
				sumB1 += updates.get(j).b1;
				j++;
			}
			keys[idx] = currentX;
			b0[idx] = sumB0;
			b1[idx] = sumB1;
			idx++;
			i = j;
		}

		initSlope = 0L;
		for (Operation op : operations) {
			initSlope += op.getSlopeAtInfLeft();
		}

		if (m > 0) {
			long totalValAtKeys0 = 0L;
			for (Operation op : operations) {
				totalValAtKeys0 += op.getValue(keys[0]);
			}
			F[0] = totalValAtKeys0;
			initOffset = F[0] - b0[0] - b1[0] - initSlope * keys[0];
			SD[0] = initSlope + b1[0];

			for (int i = 1; i < m; i++) {
				long x_curr = keys[i];
				long x_prev = keys[i - 1];

				SD[i] = SD[i - 1] + b1[i];
				F[i] = F[i - 1] + (x_curr - x_prev) * SD[i - 1] + b0[i] + b1[i];
			}
		} else {
			long totalValAt0 = 0L;
			for (Operation op : operations) {
				totalValAt0 += op.getValue(0L);
			}
			initOffset = totalValAt0;
		}

		isBuilt = true;
	}

	/**
	 * 座標 x における累積加算値を返します。
	 *
	 * @param x 取得する座標
	 * @return 累積値
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし（内部で未構築の場合は自動で build() を呼び出します）。</li>
	 *   <li>計算量: O(log M) （M はユニークな変更点の数）</li>
	 * </ul>
	 */
	// 未テスト
	public long get(long x) {
		if (!isBuilt) {
			build();
		}
		if (keys.length == 0) {
			return initSlope * x + initOffset;
		}

		int idx = SortedArrays.floor(keys, x);

		if (idx < 0) {
			return initSlope * x + initOffset;
		}

		return F[idx] + (x - keys[idx]) * SD[idx];
	}

	/**
	 * 内部状態を出力します（デバッグ用）。
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>計算量: O(M) （M はユニークな変更点の数）</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		if (!isBuilt) {
			build();
		}
		System.out.println("StaticSlopeAddPointGetLarge1D {");
		System.out.println("  keys: " + Arrays.toString(keys));
		System.out.println("  SD  : " + Arrays.toString(SD));
		System.out.println("  F   : " + Arrays.toString(F));
		System.out.println("}");
	}
}
