package library.util.fold;

import library.util.collections.HashMultiSet;

/**
 * https://atcoder.jp/contests/abc280/submissions/73020483
 */
public class StaticPointAddRangeSum3D extends PrefixSum3D {
	public StaticPointAddRangeSum3D(int len0, int len1, int len2) {
		super(len0, len1, len2);
	}	
	
	public StaticPointAddRangeSum3D(int[][][] a) {
		super(a.length, a[0].length, a[0][0].length);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < a[i][j].length; k++) {
					add(i,j,k,a[i][j][k]);
				}
			}
		}
	}
	
	public StaticPointAddRangeSum3D(long[][][] a) {
		super(a.length, a[0].length, a[0][0].length);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < a[i][j].length; k++) {
					add(i,j,k,a[i][j][k]);
				}
			}
		}
	}	
	
	
	
	boolean built=false;
	
	/**
	 * minI,minJ,minKはinclusive
	 * maxI,maxJ,maxKはexclusive
	 */	
	public long rangeSum(int minI, int minJ, int minK, int maxI, int maxJ, int maxK) {
		if(!built) {
			super.build();
			built=true;
		}
		int[] is=new int[] {maxI - 1, minI - 1};
		int[] js=new int[] {maxJ - 1, minJ - 1};
		int[] ks=new int[] {maxK - 1, minK - 1};
		if(minI>=maxI || minJ>=maxJ || minK>=maxK)return 0;
		long ret = 0;
		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				for (int k = 0; k < 2; k++) {
					ret += super.get(is[i], js[j], ks[k]) * ((i ^ j ^ k) == 0 ? 1 : -1);
				}
			}
		}
		return ret;
	}
	
	public void add(int i, int j, int k, long val) {
		if(built)throw new AssertionError();
		super.add(i, j, k, val);
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
	 */
	public long countRangeSum(long v) {
		if (!built) {
			super.build();
			built = true;
		}
		int N = a.length;
		int M = a[0].length;
		int L = a[0][0].length;

		long res = 0;
		if (L >= N && L >= M) {
			// L is largest
			for (int i1 = 0; i1 < N; i1++) {
				for (int i2 = i1 + 1; i2 <= N; i2++) {
					for (int j1 = 0; j1 < M; j1++) {
						for (int j2 = j1 + 1; j2 <= M; j2++) {
							HashMultiSet<Long> counts = new HashMultiSet<>();
							counts.add(0L);
							for (int k = 0; k < L; k++) {
								long cur = a[i2 - 1][j2 - 1][k]
										- (i1 == 0 ? 0 : a[i1 - 1][j2 - 1][k])
										- (j1 == 0 ? 0 : a[i2 - 1][j1 - 1][k])
										+ (i1 == 0 || j1 == 0 ? 0 : a[i1 - 1][j1 - 1][k]);
								res += counts.getValue(cur - v);
								counts.add(cur);
							}
						}
					}
				}
			}
		} else if (M >= N && M >= L) {
			// M is largest
			for (int i1 = 0; i1 < N; i1++) {
				for (int i2 = i1 + 1; i2 <= N; i2++) {
					for (int k1 = 0; k1 < L; k1++) {
						for (int k2 = k1 + 1; k2 <= L; k2++) {
							HashMultiSet<Long> counts = new HashMultiSet<>();
							counts.add(0L);
							for (int j = 0; j < M; j++) {
								long cur = a[i2 - 1][j][k2 - 1]
										- (i1 == 0 ? 0 : a[i1 - 1][j][k2 - 1])
										- (k1 == 0 ? 0 : a[i2 - 1][j][k1 - 1])
										+ (i1 == 0 || k1 == 0 ? 0 : a[i1 - 1][j][k1 - 1]);
								res += counts.getValue(cur - v);
								counts.add(cur);
							}
						}
					}
				}
			}
		} else {
			// N is largest
			for (int j1 = 0; j1 < M; j1++) {
				for (int j2 = j1 + 1; j2 <= M; j2++) {
					for (int k1 = 0; k1 < L; k1++) {
						for (int k2 = k1 + 1; k2 <= L; k2++) {
							HashMultiSet<Long> counts = new HashMultiSet<>();
							counts.add(0L);
							for (int i = 0; i < N; i++) {
								long cur = a[i][j2 - 1][k2 - 1]
										- (j1 == 0 ? 0 : a[i][j1 - 1][k2 - 1])
										- (k1 == 0 ? 0 : a[i][j2 - 1][k1 - 1])
										+ (j1 == 0 || k1 == 0 ? 0 : a[i][j1 - 1][k1 - 1]);
								res += counts.getValue(cur - v);
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