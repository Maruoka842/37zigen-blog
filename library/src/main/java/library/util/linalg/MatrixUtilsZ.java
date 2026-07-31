package library.util.linalg;

import java.util.Arrays;

import library.util.ArrayUtils;

public class MatrixUtilsZ {

	public static long[][] mul(long[][] a, long[][] b, long[][] c) {
		return mul(mul(a, b), c);
	}

	public static long[][] mul(long[][] a, long[][] b) {
		int n = a.length;
		if (n == 0) return new long[0][0];
		if (b.length == 0) return new long[n][0];
		int m = b[0].length;
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			long[] rowA = a[i];
			long[] rowC = c[i];
			int k_lim = Math.min(rowA.length, b.length);
			for (int k = 0; k < k_lim; k++) {
				long aik = rowA[k];
				if (aik == 0) continue;
				long[] rowB = b[k];
				int j_lim = Math.min(m, rowB.length);
				for (int j = 0; j < j_lim; j++) {
					rowC[j] += aik * rowB[j];
				}
			}
		}
		return c;
	}

	public static long[][] transpose(long[][] a) {
		int n = a.length;
		if (n == 0) return new long[0][0];
		int m = a[0].length;
		long[][] b = new long[m][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				b[j][i] = a[i][j];
			}
		}
		return b;
	}

	public static long[][] longMatrixIdentity(int n) {
		long[][] ret=new long[n][n];
		for(int i=0;i<n;++i)ret[i][i]=1;
		return ret;
	}

	public record SmithResult(long[][] U, long[][] S, long[][] V, int rank) {}

	public record HermiteResult(long[][] U, long[][] H, int rank) {}

	/**
	 * 整数行列 A のスミス標準形 (Smith Normal Form) を計算する。
	 * UAV = S を満たすユニモジュラ行列 U, V と、対角行列 S を返す。
	 * S の対角要素 s_i は s_i | s_{i+1} を満たす。
	 *
	 * <p>計算量: O(nm min(n, m) log(max |Aij|))。</p>
	 *
	 * @param a 行列 A
	 * @return {U, S, V, rank}
	 */
	public static SmithResult smithNormalForm(long[][] a) {
		int n = a.length;
		if (n == 0) return new SmithResult(new long[0][0], new long[0][0], new long[0][0], 0);
		int m = a[0].length;
		long[][] S = ArrayUtils.copy(a);
		long[][] U = longMatrixIdentity(n);
		long[][] V = longMatrixIdentity(m);

		int minNM = Math.min(n, m);
		for (int k = 0; k < minNM; k++) {//S[k][k]を確定させる
			while (true) {
				int pi = -1, pj = -1;
				//絶対値最小をS[k][k]に持ってくる
				for (int i = k; i < n; i++) {
					for (int j = k; j < m; j++) {
						if (S[i][j] != 0) {
							if (pi == -1) {
								pi = i; pj = j;
							} else {
								long v1 = Math.abs(S[i][j]);
								long v2 = Math.abs(S[pi][pj]);
								if (v1 < v2) {
									pi = i; pj = j;
								}
							}
						}
					}
				}
				if (pi == -1) break;
				if (pi != k) {
					ArrayUtils.swap(k, pi, S);
					ArrayUtils.swap(k, pi, U);
				}
				if (pj != k) {
					ArrayUtils.swapColumns(k, pj, S);
					ArrayUtils.swapColumns(k, pj, V);
				}

				boolean changed = false;
				//行基本変形
				for (int i = k + 1; i < n; i++) {
					if (S[i][k] != 0) {
						long q = S[i][k] / S[k][k];
						if (q != 0) {
							for (int j = k; j < m; j++) S[i][j] -= q * S[k][j];
							for (int j = 0; j < n; j++) U[i][j] -= q * U[k][j];
						}
						if (S[i][k] != 0) changed = true;
					}
				}
				//列基本変形
				for (int j = k + 1; j < m; j++) {
					if (S[k][j] != 0) {
						long q = S[k][j] / S[k][k];
						if (q != 0) {
							for (int i = k; i < n; i++) S[i][j] -= q * S[i][k];
							for (int i = 0; i < m; i++) V[i][j] -= q * V[i][k];
						}
						if (S[k][j] != 0) changed = true;
					}
				}
				if (changed) continue;
				//S[k][k]が右下の要素をすべて割り切らなければならない
				boolean divisible = true;
				out:for (int i = k + 1; i < n; i++) {
					for (int j = k + 1; j < m; j++) {
						if (S[i][j] % S[k][k] != 0) {
							for (int l = k; l < m; l++) S[k][l] += S[i][l];
							for (int l = 0; l < n; l++) U[k][l] += U[i][l];
							divisible = false;
							break out;
						}
					}
				}
				if (divisible) break;
			}
			if (S[k][k] < 0) {
				for (int j = k; j < m; j++) S[k][j] = -S[k][j];
				for (int j = 0; j < n; j++) U[k][j] = -U[k][j];
			}
		}
		int rank = 0;
		while (rank < minNM && S[rank][rank] != 0) rank++;
		return new SmithResult(U, S, V, rank);
	}

	/**
	 * 整数行列 {@code A} を行基本変形によって行エルミート標準形
	 * (row Hermite normal form; row HNF) に変換する。
	 *
	 * <p>ユニモジュラ行列 {@code U} と行列 {@code H} を計算し、
	 * {@code U A = H} を満たす。
	 *
	 * <p>返される {@code H} は例えば次のような形になる。
	 *
	 * <pre>{@code
	 * [ 2  1  3  0 ]
	 * [ 0  3  4  0 ]
	 * [ 0  0  5  0 ]
	 * [ 0  0  0  0 ]
	 * }</pre>
	 *
	 * この例では、
	 * <ul>
	 *   <li>非零行は先頭に集まり、零行は末尾にある。</li>
	 *   <li>ピボットは 2, 3, 5 であり、上から下へ進むにつれて右へ移る。</li>
	 *   <li>ピボットはすべて正である。</li>
	 *   <li>各ピボット列では、ピボットより上の成分はその列のピボット未満
	 *       （{@code [0, pivot)}）に正規化されている
	 *       （例えば第 2 列では {@code 1 < 3}、第 3 列では {@code 3, 4 < 5}）。</li>
	 * </ul>
	 *
	 * @param a 入力行列
	 * @return {@code u} はユニモジュラ行列 {@code U}、
	 *         {@code h} は行エルミート標準形 {@code H}、
	 *         {@code rank} は階数
	 */
	public static HermiteResult rowHermiteNormalForm(long[][] a) {
		// 未テスト
		int n = a.length;
		if (n == 0) return new HermiteResult(new long[0][0], new long[0][0], 0);
		int m = a[0].length;
		if (m == 0) return new HermiteResult(longMatrixIdentity(n), new long[n][0], 0);

		long[][] H = ArrayUtils.copy(a);
		long[][] U = longMatrixIdentity(n);

		int currRow = 0;
		for (int j = 0; j < m; j++) {
			if (currRow == n) break;
			while (true) {
				int minRow = -1;
				long minVal = Long.MAX_VALUE;
				int nonZeroCount = 0;
				for (int i = currRow; i < n; i++) {
					if (H[i][j] != 0) {
						nonZeroCount++;
						long absVal = Math.abs(H[i][j]);
						if (absVal < minVal) {
							minVal = absVal;
							minRow = i;
						}
					}
				}
				if (nonZeroCount == 0) {
					break;
				}
				if (nonZeroCount == 1) {
					if (minRow != currRow) {
						ArrayUtils.swap(currRow, minRow, H);
						ArrayUtils.swap(currRow, minRow, U);
					}
					break;
				}
				for (int i = currRow; i < n; i++) {
					if (i != minRow && H[i][j] != 0) {
						long q = H[i][j] / H[minRow][j];
						if (q != 0) {
							for (int col = 0; col < m; col++) {
								H[i][col] -= q * H[minRow][col];
							}
							for (int col = 0; col < n; col++) {
								U[i][col] -= q * U[minRow][col];
							}
						}
					}
				}
			}

			if (H[currRow][j] != 0) {
				if (H[currRow][j] < 0) {
					for (int col = 0; col < m; col++) {
						H[currRow][col] = -H[currRow][col];
					}
					for (int col = 0; col < n; col++) {
						U[currRow][col] = -U[currRow][col];
					}
				}
				for (int i = 0; i < currRow; i++) {
					long q = Math.floorDiv(H[i][j], H[currRow][j]);
					if (q != 0) {
						for (int col = 0; col < m; col++) {
							H[i][col] -= q * H[currRow][col];
						}
						for (int col = 0; col < n; col++) {
							U[i][col] -= q * U[currRow][col];
						}
					}
				}
				currRow++;
			}
		}
		return new HermiteResult(U, H, currRow);
	}

	/**
	 * 行列 {@code A} を列基本変形によって列エルミート標準形へ変換する。
	 * 
	 * <p>ユニモジュラ行列 {@code V} と {@code H} を計算し、
	 * {@code A V = H} を満たす。
	 *
	 * <p>{@code H} の形は例えば次のようになる。
	 *
	 * <pre>{@code
	 * [ 2  0  0  0 ]
	 * [ 1  3  0  0 ]
	 * [ 0  2  5  0 ]
	 * [ 0  0  0  0 ]
	 * }</pre>
	 *
	 * この例では、
	 * <ul>
	 * <li>非零列は左側に集まる。</li>
	 * <li>各列の最初の非零要素（2, 3, 5）がピボットであり、
	 *     左から右へ進むにつれて下の行へ移る。</li>
	 * <li>ピボットは正である。</li>
	 * <li>ピボット行では、左側の成分は対応するピボット未満
	 *     （1 &lt; 3, 2 &lt; 5）に正規化されている。</li>
	 * </ul>
	 *
	 * @param a 入力行列
	 * @return {@code result.v} はユニモジュラ行列 {@code V}、
	 *         {@code result.h} は列エルミート標準形 {@code H}、
	 *         {@code result.rank} は階数
	 */
	public static HermiteResult columnHermiteNormalForm(long[][] a) {
		// 未テスト
		long[][] aT = transpose(a);
		HermiteResult hr = rowHermiteNormalForm(aT);
		long[][] Hc = transpose(hr.H());
		long[][] V = transpose(hr.U());
		return new HermiteResult(V, Hc, hr.rank());
	}

	/**
	 * 整数上の連立一次方程式 Ax = b を解く。
	 * Ax = b を満たす整数解 x と、Ax = 0 の整数解空間（核）の基底を返す。
	 *
	 * <p>計算量: O(nm min(n, m) log(max |Aij|))。</p>
	 *
	 * @param a 行列 A
	 * @param b ベクトル b
	 * @return {x, basis1, basis2, ...} の形式。解が存在しない場合は null。
	 *         res[0] が特殊解、res[1...] が核の基底ベクトル。
	 */
	public static long[][] linearEquationOnZ(long[][] a, long[] b) {
		int n = a.length;
		if (n == 0) return null;
		int m = a[0].length;
		SmithResult sr = smithNormalForm(a);
		long[][] U = sr.U();
		long[][] S = sr.S();
		long[][] V = sr.V();
		int rank = sr.rank();
		// Ax = b
		// UAx = Ub
		long[] Ub = new long[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				Ub[i] += U[i][j] * b[j];
			}
		}
		// x = Vy 
		// UAVy = Ub
		// Sy = Ub
		long[] y = new long[m];
		for (int i = 0; i < rank; i++) {
			if (Ub[i] % S[i][i] != 0) return null;
			y[i] = Ub[i] / S[i][i];
		}
		for (int i = rank; i < n; i++) {
			if (Ub[i] != 0) return null;
		}

		long[][] res = new long[1 + (m - rank)][m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < rank; j++) {
				res[0][i] += V[i][j] * y[j];//x = Vy
			}
		}
		for (int k = 0; k < m - rank; k++) {
			for (int i = 0; i < m; i++) {
				res[k + 1][i] = V[i][rank + k];//自由変数t_k=1
			}
		}
		return res;
	}


	public static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

}
