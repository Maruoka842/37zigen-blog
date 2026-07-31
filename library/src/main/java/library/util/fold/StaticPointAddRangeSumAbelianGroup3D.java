package library.util.fold;

import java.util.Arrays;

import library.util.algebra.strategy.AbelianGroupStrategy;
import library.util.collections.HashMultiSet;

/**
 * 3次元の静的な点更新・区間和取得（アーベル群）
 * @param <T>
 */
public class StaticPointAddRangeSumAbelianGroup3D<T> {
	private final int N, M, L;
	private final T[][][] A;
	private final AbelianGroupStrategy<T> group;
	private boolean built = false;

	/**
	 * NxMxLの配列を用意する
	 * @param len0
	 * @param len1
	 * @param len2
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup3D(int len0, int len1, int len2, AbelianGroupStrategy<T> group) {
		this.N = len0;
		this.M = len1;
		this.L = len2;
		this.group = group;
		this.A = (T[][][]) new Object[N][M][L];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				Arrays.fill(A[i][j], group.identity());
			}
		}
	}

	/**
	 * 三次元配列aで初期化する
	 * @param a
	 * @param group
	 */
	public StaticPointAddRangeSumAbelianGroup3D(T[][][] a, AbelianGroupStrategy<T> group) {
		this.N = a.length;
		this.M = a[0].length;
		this.L = a[0][0].length;
		this.group = group;
		this.A = (T[][][]) new Object[N][M][];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				this.A[i][j] = a[i][j].clone();
			}
		}
	}

	/**
	 * 座標(i, j, k)にvalを足す
	 * @param i
	 * @param j
	 * @param k
	 * @param val
	 */
	public void add(int i, int j, int k, T val) {
		if (built) throw new AssertionError();
		if (i < 0 || j < 0 || k < 0 || i >= N || j >= M || k >= L) return;
		A[i][j][k] = group.mul(A[i][j][k], val);
	}

	/**
	 * 累積和を構築する
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 三次元累積和を構築する。</p>
	 * <p>副作用: Aの内容が書き換えられ、builtがtrueになる。</p>
	 * <p>計算量: O(NML)</p>
	 * <p>破壊的変更: あり。</p>
	 * 未テスト
	 */
	public void build() {
		if (built) throw new AssertionError();
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				for (int k = 1; k < L; k++) {
					A[i][j][k] = group.mul(A[i][j][k - 1], A[i][j][k]);
				}
			}
		}
		for (int k = 0; k < L; k++) {
			for (int j = 0; j < M; j++) {
				for (int i = 1; i < N; i++) {
					A[i][j][k] = group.mul(A[i - 1][j][k], A[i][j][k]);
				}
			}
		}
		for (int i = 0; i < N; i++) {
			for (int k = 0; k < L; k++) {
				for (int j = 1; j < M; j++) {
					A[i][j][k] = group.mul(A[i][j - 1][k], A[i][j][k]);
				}
			}
		}
		built = true;
	}

	private T get(int i, int j, int k) {
		if (i < 0 || j < 0 || k < 0) return group.identity();
		i = Math.min(i, N - 1);
		j = Math.min(j, M - 1);
		k = Math.min(k, L - 1);
		return A[i][j][k];
	}

	/**
	 * [minI, maxI) x [minJ, maxJ) x [minK, maxK) の和を返す
	 * @param minI
	 * @param minJ
	 * @param minK
	 * @param maxI
	 * @param maxJ
	 * @param maxK
	 * @return
	 *
	 * <p>事前条件: なし。</p>
	 * <p>事後条件: 指定された直方体領域の和を返す。</p>
	 * <p>副作用: !built の場合、内部で build() を呼び出す。</p>
	 * <p>計算量: O(1)</p>
	 * <p>破壊的変更: build() が呼ばれる場合のみあり。</p>
	 * 未テスト
	 */
	public T rangeSum(int minI, int minJ, int minK, int maxI, int maxJ, int maxK) {
		if (!built) build();
		if (minI >= maxI || minJ >= maxJ || minK >= maxK) return group.identity();

		int[] is = {maxI - 1, minI - 1};
		int[] js = {maxJ - 1, minJ - 1};
		int[] ks = {maxK - 1, minK - 1};

		T res = group.identity();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					T val = get(is[i], js[j], ks[k]);
					if ((i ^ j ^ k) == 0) {
						res = group.mul(res, val);
					} else {
						res = group.mul(res, group.inverse(val));
					}
				}
			}
		}
		return res;
	}

	/**
	 * 和が v となる直方体区間の個数を返す。
	 *
	 * @param v ターゲットとなる和
	 * @return #{(i1, j1, k1, i2, j2, k2) | 0 <= i1 < i2 <= N, 0 <= j1 < j2 <= M, 0 <= k1 < k2 <= L, \sum_{i=i1}^{i2-1} \sum_{j=j1}^{j2-1} \sum_{k=k1}^{k2-1} a_{i,j,k} = v}
	 *
	 * <p>契約:</p>
	 * <ul>
	 *   <li>事前条件: なし。</li>
	 *   <li>事後条件: 和が v となる区間の個数を返す。</li>
	 *   <li>副作用: !built の場合、内部で build() を呼び出す。</li>
	 *   <li>計算量: O(N_a^2 N_b^2 N_c) ただし {N_a, N_b, N_c} = {N, M, L} かつ N_c は最大値。</li>
	 *   <li>破壊的変更: なし（build() による内部状態の変化を除く）。</li>
	 * </ul>
	 * 未テスト
	 */
	public long countRangeSum(T v) {
		if (!built) build();
		long res = 0;
		if (L >= N && L >= M) {
			for (int i1 = 0; i1 < N; i1++) {
				for (int i2 = i1 + 1; i2 <= N; i2++) {
					for (int j1 = 0; j1 < M; j1++) {
						for (int j2 = j1 + 1; j2 <= M; j2++) {
							HashMultiSet<T> counts = new HashMultiSet<>();
							counts.add(group.identity());
							for (int k = 0; k < L; k++) {
								T cur = A[i2 - 1][j2 - 1][k];
								if (i1 > 0) cur = group.mul(cur, group.inverse(A[i1 - 1][j2 - 1][k]));
								if (j1 > 0) cur = group.mul(cur, group.inverse(A[i2 - 1][j1 - 1][k]));
								if (i1 > 0 && j1 > 0) cur = group.mul(cur, A[i1 - 1][j1 - 1][k]);

								T target = group.mul(cur, group.inverse(v));
								res += counts.getValue(target);
								counts.add(cur);
							}
						}
					}
				}
			}
		} else if (M >= N && M >= L) {
			for (int i1 = 0; i1 < N; i1++) {
				for (int i2 = i1 + 1; i2 <= N; i2++) {
					for (int k1 = 0; k1 < L; k1++) {
						for (int k2 = k1 + 1; k2 <= L; k2++) {
							HashMultiSet<T> counts = new HashMultiSet<>();
							counts.add(group.identity());
							for (int j = 0; j < M; j++) {
								T cur = A[i2 - 1][j][k2 - 1];
								if (i1 > 0) cur = group.mul(cur, group.inverse(A[i1 - 1][j][k2 - 1]));
								if (k1 > 0) cur = group.mul(cur, group.inverse(A[i2 - 1][j][k1 - 1]));
								if (i1 > 0 && k1 > 0) cur = group.mul(cur, A[i1 - 1][j][k1 - 1]);

								T target = group.mul(cur, group.inverse(v));
								res += counts.getValue(target);
								counts.add(cur);
							}
						}
					}
				}
			}
		} else {
			for (int j1 = 0; j1 < M; j1++) {
				for (int j2 = j1 + 1; j2 <= M; j2++) {
					for (int k1 = 0; k1 < L; k1++) {
						for (int k2 = k1 + 1; k2 <= L; k2++) {
							HashMultiSet<T> counts = new HashMultiSet<>();
							counts.add(group.identity());
							for (int i = 0; i < N; i++) {
								T cur = A[i][j2 - 1][k2 - 1];
								if (j1 > 0) cur = group.mul(cur, group.inverse(A[i][j1 - 1][k2 - 1]));
								if (k1 > 0) cur = group.mul(cur, group.inverse(A[i][j2 - 1][k1 - 1]));
								if (j1 > 0 && k1 > 0) cur = group.mul(cur, A[i][j1 - 1][k1 - 1]);

								T target = group.mul(cur, group.inverse(v));
								res += counts.getValue(target);
								counts.add(cur);
							}
						}
					}
				}
			}
		}
		return res;
	}
}
