package library.util.linalg;

import java.util.Arrays;
import java.util.Random;

import library.util.ArrayUtils;
import library.util.MathUtils;
import library.util.polynomial.PolynomialFp;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.poset.BooleanLattice;

public class MatrixUtilsFp extends MatrixUtilsZn {

	public static long pfaffian(long[][]a, long mod) {
		//https://judge.yosupo.jp/submission/361431
		//https://atcoder.jp/contests/abc216/submissions/74255616
		if (a.length % 2 != 0)throw new AssertionError();
		if (a.length != a[0].length) throw new AssertionError();
		int n=a.length;
		long ret=1;
		/*
		 * a[0,1]=a[2,3]=..=1
		 * a[1,0]=a[3,2]=..=-1
		 * それ以外0
		 * となるようにする。
		 *  0 1 0 0 0 0 0
		 * -1 0 0 0 0 0 0
		 *  0 0 0 1 0 0 0
		 *  0 0-1 0 0 0 0
		 *  0 0 0 0 0 1 0
		 *  0 0 0 0-1 0 1
		 *  0 0 0 0 0-1 0
		 */

		for (int i = 0; i < n - 1; i += 2) {
			int pivot=i+1;
			while (pivot < n && a[pivot][i] == 0) ++pivot;
			if (pivot == n) return 0;
			if (i + 1 != pivot) {
				ArrayUtils.swap(a[i + 1], a[pivot]);
				ArrayUtils.swapColumns(i + 1, pivot, a);
				ret = (mod - 1) % mod * ret % mod;
			}
			if (a[i][i + 1] != 1) {
				long inv = MathUtils.modInv(a[i][i + 1], mod);
				ret = ret * a[i][i + 1] % mod;
				for (int j = 0; j < n; j++) {
					a[i + 1][j] = a[i + 1][j] * inv % mod;
					a[j][i + 1] = a[j][i + 1] * inv % mod;
				}
			}
			for (int j = i + 2; j < n; j++) {
				if (a[j][i] != 0) {
					long q = a[j][i];
					for (int k = 0; k < n; k++) {
						if (j==k) continue;
						a[j][k] += a[i+1][k] * q % mod;//a[i+1][i]=-1
						a[j][k] %= mod;
						if(a[j][k]==0) a[k][j]=0;
						else a[k][j] = mod-a[j][k];

					}
				}
			}
		}
		return ret;
	}

	/**
	 * スパース行列 {@code A} とランダムなベクトル {@code x, y} から
	 * Wiedemann列 {@code s[i] = x A^i y} を作り、その最小多項式を返す。
	 *
	 * <p>
	 * {@code entries[e] = {row, col, value}} は {@code A[row][col] += value} を表す。
	 * 返り値 {@code coeff} は
	 * {@code coeff[0] + coeff[1] x + ... + coeff[d] x^d} の順で、
	 * {@code coeff[d] = 1} に正規化する。
	 *
	 * <p>
	 * これはランダム射影されたスカラー列の最小多項式であり、行列の最小多項式そのものと
	 * 一致するとは限らない。ただし十分ランダムな {@code x, y} では高確率で一致する。
	 * 未テスト。計算量 {@code O(NZ + B(N))}、{@code Z = entries.length}、
	 * {@code B(N)} は長さ {@code 2N + 1} のBerlekamp-Masseyの計算量。
	 *
	 * @param n 行列サイズ
	 * @param entries スパース行列の非零要素
	 * @return {@code x A^i y} の最小多項式
	 */
	public static long[] minimalPolynomialOnFp(int n, long[][] entries) {
		Random rnd = new Random(1);
		long[] x = new long[n];
		long[] y = new long[n];
		for (int i = 0; i < n; i++) {
			x[i] = rnd.nextLong(PolynomialFp.mod);
			y[i] = rnd.nextLong(PolynomialFp.mod);
		}
		return minimalPolynomialOnFp(n, entries, x, y);
	}

	/**
	 * スパース行列 {@code A} と指定されたベクトル {@code x, y} から
	 * Wiedemann列 {@code s[i] = x A^i y} を作り、その最小多項式を返す。
	 * 未テスト。計算量 {@code O(NZ + B(N))}。
	 *
	 * @param n 行列サイズ
	 * @param entries {@code {row, col, value}} 形式の非零要素
	 * @param x 左から掛ける行ベクトル
	 * @param y 右から掛ける列ベクトル
	 * @return {@code x A^i y} の最小多項式
	 */
	public static long[] minimalPolynomialOnFp(int n, long[][] entries, long[] x, long[] y) {
		long mod = PolynomialFp.mod;
		if (x.length != n || y.length != n) throw new AssertionError();
		long[] v = new long[n];
		for (int i = 0; i < n; i++) {
			v[i] = y[i] % mod;
			if (v[i] < 0) v[i] += mod;
		}
		long[] normalizedX = new long[n];
		for (int i = 0; i < n; i++) {
			normalizedX[i] = x[i] % mod;
			if (normalizedX[i] < 0) normalizedX[i] += mod;
		}
		long[] s = new long[2 * n + 1];
		for (int i = 0; i < s.length; i++) {
			s[i] = dot(normalizedX, v, mod);
			if (i + 1 < s.length) v = sparseMatVecMul(n, entries, v, mod);
		}
		return PolynomialFp.berlekampMassey(s);
	}

	static long[] sparseMatVecMul(int n, long[][] entries, long[] v, long mod) {
		long[] ret = new long[n];
		for (long[] e : entries) {
			int row = (int) e[0];
			int col = (int) e[1];
			long value = e[2] % mod;
			if (value < 0) value += mod;
			ret[row] = (ret[row] + value * v[col]) % mod;
		}
		return ret;
	}

	static long dot(long[] a, long[] b, long mod) {
		long ret = 0;
		for (int i = 0; i < a.length; i++) ret = (ret + a[i] * b[i]) % mod;
		return ret;
	}

	/**
	 * スパース行列の行列式を {@link PolynomialFp#mod} 上で返す。
	 *
	 * <p>
	 * ランダムな対角行列 {@code D} で {@code B = A D} とし、
	 * Wiedemann列から {@code B} の最小多項式を求める。ランダム化により高確率で
	 * 最小多項式が特性多項式に一致するので、次数が {@code n} になった試行では
	 * その定数項から {@code det(B)} を得て、{@code det(A)=det(B)/det(D)} と戻す。
	 *
	 * <p>
	 * {@code entries[e] = {row, col, value}} は {@code A[row][col] += value} を表す。
	 * 計算量は1試行あたり {@code O(NZ + B(N))}。
	 *
	 * @param n 行列サイズ
	 * @param entries スパース行列の非零要素
	 * @return 行列式
	 */
	public static long sparseDeterminantOnFp(int n, long[][] entries) {
		//https://judge.yosupo.jp/submission/371599
		if (n == 0) return 1;
		long mod = PolynomialFp.mod;
		Random rnd = new Random(1);
		for (int trial = 0; trial < 5; trial++) {
			long[] diag = new long[n];
			long detDiag = 1;
			for (int i = 0; i < n; i++) {
				diag[i] = rnd.nextLong(1, mod);
				detDiag = detDiag * diag[i] % mod;
			}
			long[][] scaled = scaleSparseColumns(entries, diag, mod);
			long[] x = new long[n];
			long[] y = new long[n];
			for (int i = 0; i < n; i++) {
				x[i] = rnd.nextLong(mod);
				y[i] = rnd.nextLong(mod);
			}
			long[] minpoly = minimalPolynomialOnFp(n, scaled, x, y);
			if (minpoly[0] == 0) return 0;
			if (minpoly.length == n + 1) {
				long detB = minpoly[0];
				if ((n & 1) != 0 && detB != 0) detB = mod - detB;
				return detB * MathUtils.modInv(detDiag, mod) % mod;
			}
		}
		throw new AssertionError("failed to recover determinant by Wiedemann");
	}

	static long[][] scaleSparseColumns(long[][] entries, long[] diag, long mod) {
		long[][] ret = new long[entries.length][3];
		for (int i = 0; i < entries.length; i++) {
			int row = (int) entries[i][0];
			int col = (int) entries[i][1];
			long value = entries[i][2] % mod;
			if (value < 0) value += mod;
			ret[i][0] = row;
			ret[i][1] = col;
			ret[i][2] = value * diag[col] % mod;
		}
		return ret;
	}

	/**
	 * mod mod 上の正方行列 a の余因子行列 adj(a) を O(N^3) で返す。
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[][] modAdjugate(long[][] a, long mod) {
		// (adjA) A = A (adj A) = det A
		int n = a.length;
		if (n == 0) return new long[0][0];
		if (a[0].length != n) throw new AssertionError();
		if (n == 1) return new long[][] {{1 % mod}};

		long det = modDeterminant(a, mod);
		if (det != 0) {
			long[][] inv = inv(a, mod);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					inv[i][j] = inv[i][j] * det % mod;
				}
			}
			return inv;
		}

		// rank <= n-1
		long[][] xBases = nullSpace(a, mod);
		int nullity = xBases[0].length;
		if (nullity >= 2) {
			return new long[n][n];
		}
		// nullity == 1, rank == n-1
		long[] x = new long[n];
		for (int i = 0; i < n; i++) x[i] = xBases[i][0];

		long[][] yBases = nullSpace(MatrixUtilsZ.transpose(a), mod);
		long[] y = new long[n];
		for (int i = 0; i < n; i++) y[i] = yBases[i][0];

		int p = -1, q = -1;
		for (int i = 0; i < n; i++) if (x[i] != 0) { p = i; break; }
		for (int i = 0; i < n; i++) if (y[i] != 0) { q = i; break; }

		// adj(a)_{p,q} = (-1)^{p+q} det(M_{q,p})
		long[][] mqp = new long[n - 1][n - 1];
		for (int i = 0; i < n; i++) {
			if (i == q) continue;
			int row = i < q ? i : i - 1;
			for (int j = 0; j < n; j++) {
				if (j == p) continue;
				int col = j < p ? j : j - 1;
				mqp[row][col] = a[i][j];
			}
		}

		long detMqp = modDeterminant(mqp, mod);
		if ((p + q) % 2 == 1 && detMqp != 0) detMqp = mod - detMqp;

		long c = detMqp * MathUtils.modInv(x[p] * y[q] % mod, mod) % mod;
		long[][] adj = new long[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				adj[i][j] = c * x[i] % mod * y[j] % mod;
			}
		}
		return adj;
	}

	/**
	 * 与えられた行列 {@code a} を法 {@code mod} 上で掃き出し法により
	 * 既約行階段形（Reduced Row Echelon Form, RREF）に変換した行列を返す。
	 *<ul>
	 *   <li>零でない各行の先頭の非零成分、すなわちピボットは {@code 1} である。</li>
	 *   <li>ピボットを含む列では、ピボット以外の成分はすべて {@code 0} である。</li>
	 *   <li>ピボット列の位置は、下の行へ行くほど真に右へ進む。</li>
	 *   <li>零行が存在する場合、それらは非零行の下に現れる。</li>
	 * </ul>
	 *
	 * @param a
	 *        法 {@code mod} 上の行列
	 * @param mod
	 *        計算に用いる法(素数)
	 * @return
	 *        行列 {@code a} の既約行階段形を表す新しい行列
	 * @see
	 */
	public static long[][] reducedRowEchelonFormOnFp(long[][] a, long mod) {
		//https://atcoder.jp/contests/abc366/submissions/72615935 (mod 2, 正方行列)
		var b=ArrayUtils.copy(a);
		int n = b.length;
		if (n == 0) return b;
		int m = b[0].length;
		int rank=0;
		for(int i=0;i<m&&rank<n;++i) {
			{
				int j=rank;
				while(j<n&&b[j][i]==0)++j;
				if(j==n)continue;
				ArrayUtils.swap(b[rank], b[j]);
			}
			long inv = MathUtils.modInv(b[rank][i], mod);
			for (int k = i; k < m; k++) b[rank][k] = b[rank][k] * inv % mod;
			for(int j=0;j<n;++j) {
				if(rank==j||b[j][i]==0)continue;
				long factor = b[j][i];
				for (int k = i; k < m; k++) {
					b[j][k] = (b[j][k] - factor * b[rank][k] % mod + mod) % mod;
				}
			}
			++rank;
		}
		return b;
	}

	/**
	 * 行列 {@code a} の核空間の基底を並べた行列Bを返す。mod2以外未テスト。
	 * AB=0を満たす。B[*][j]がj番目の基底を表す。
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[][] nullSpace(long[][] a, long mod) {
		//@see https://atcoder.jp/contests/abc366/submissions/72615935 (mod 2, 正方行列)
		int n = a.length;
		if (n == 0) return new long[0][0];
		int m = a[0].length;
		long[][]b=reducedRowEchelonFormOnFp(a, mod);
		boolean[] isFree=new boolean[m];//自由変数か？非ゼロの行の最初にある1のある列に対応する変数が主変数。その他が自由変数。
		int[] pivotCol = new int[n];//pivotCol[i]=i行目の最初にある1の列
		Arrays.fill(pivotCol, m);
		Arrays.fill(isFree, true);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if(b[i][j]==1) {
					isFree[j]=false;
					pivotCol[i]=j;
					break;
				}
			}
		}
		int nullity=0;//核空間のランク
		for (int i = 0; i < isFree.length; i++) {
			if(isFree[i])++nullity;
		}
		long[][]ret=new long[m][nullity];//基底を縦ベクトルとして並べた行列を返す
		int pointer=0;
		for (int j = 0; j < m; j++) {
			if(isFree[j]) {
				//x_i=1としたとき他の変数は？
				for (int i = 0; i < n; i++) {
					if(pivotCol[i] < m && b[i][j]!=0)
						ret[pivotCol[i]][pointer]=(mod-b[i][j]) % mod;
				}
				ret[j][pointer]=1;
				pointer++;
			}
		}
		return ret;
	}

	/**
	 * https://judge.yosupo.jp/submission/344274
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long modDeterminant(long[][] a, long mod) {
		if (a.length != a[0].length) return 0;
		long[][] b = ArrayUtils.copy(a);
		long ret = 1;
		for(int i=0;i<a.length;++i) {
			{
				int j=i;
				while(j<a.length&&b[j][i]==0)++j;
				if(j==a.length) return 0;
				if (i!=j) {
					ArrayUtils.swap(b[i], b[j]);
					ret = ret * (mod - 1) % mod;
				}
			}
			ret = ret * b[i][i] % mod;
			long invBii=MathUtils.modInv(b[i][i], mod);
			for (int j = 0; j < b[i].length; ++j) {
				b[i][j] = b[i][j] * invBii % mod;
			}
			for(int j=i+1;j<a.length;++j) {
				long c = mod - b[j][i];
				b[j][i] = 0;
				for (int k = i+1; k < b[j].length; ++k) {
					b[j][k] = (b[j][k] + c * b[i][k]) % mod;
				}
			}
		}
		return ret;
	}

	/**
	 * Ax = b を満たす解 x と Ax = 0 の解空間の基底を返す。
	 * @param a 行列 A
	 * @param b ベクトル b
	 * @param mod
	 * @return {x, basis1, basis2, ...} as row vectors if exists, null otherwise. x and each basis is long[].
	 * @see https://judge.yosupo.jp/problem/system_of_linear_equations
	 */
	public static long[][] solveLinearEquation(long[][] a, long[] b, long mod) {
		//https://judge.yosupo.jp/submission/370972
		int n = a.length;
		int m = a[0].length;
		long[][] mat = new long[n][m + 1];
		for (int i = 0; i < n; i++) {
			System.arraycopy(a[i], 0, mat[i], 0, m);
			mat[i][m] = b[i];
		}

		long[][] rref = reducedRowEchelonFormOnFp(mat, mod);

		int rank = 0;
		int[] pivotCol = new int[n];
		Arrays.fill(pivotCol, -1);
		for (int i = 0; i < n; i++) {
			int j = 0;
			while (j < m + 1 && rref[i][j] == 0) j++;
			if (j == m + 1) continue;
			if (j == m) return null; // No solution
			pivotCol[rank] = j;
			rank++;
		}

		long[] x = new long[m];
		for (int i = 0; i < rank; i++) {
			x[pivotCol[i]] = rref[i][m];
		}

		boolean[] isPivot = new boolean[m];
		for (int i = 0; i < rank; i++) isPivot[pivotCol[i]] = true;
		int nullity = m - rank;
		long[][] res = new long[1 + nullity][m];
		res[0] = x;
		int idx = 1;
		for (int j = 0; j < m; j++) {
			if (!isPivot[j]) {
				res[idx][j] = 1;
				for (int i = 0; i < rank; i++) {
					res[idx][pivotCol[i]] = (mod - rref[i][j]) % mod;
				}
				idx++;
			}
		}
		return res;
	}


    /**
	 * 逆行列が存在しないときはnull
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[][] inv(long[][] a, long mod) {
		//https://judge.yosupo.jp/submission/370839
		if(a.length != a[0].length) return null;
		int n=a.length;
		long[][] b=new long[n][2 * n];
		for (int i=0;i<n;++i)b[i][n + i]=1;
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				b[i][j] = a[i][j];
			}
		}
		b = reducedRowEchelonFormOnFp(b, mod);
		for (int i = 0; i < n; ++i) if (b[i][i] != 1) return null;
		long[][] ret = new long[n][n];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				ret[i][j] = b[i][n + j];
			}
		}
		return ret;
	}


	public static int rank(long[][] a, long mod) {
		//https://judge.yosupo.jp/submission/370813
		if (a.length == 0 || a[0].length == 0) return 0;
		long[][] b = reducedRowEchelonFormOnFp(a, mod);
		int rank = 0;
		for (int i = 0; i < b.length; ++i) {
			for (int j = 0; j < b[0].length; ++j) {
				if (b[i][j] == 1) {
					++rank;
					break;
				}
			}
		}
		return rank;
	}


	/**
	 * B = P⁻¹AP s.t. B[i][j] = 0 if i ≥ j + 2 となる B を返す。
	 * @param A
	 * @param mod
	 * @return
	 */
	public static long[][] hessenbergReductionOnFp(long[][]A, long mod) {
		/*
		 * 左から掛けたとき、i行目にj行目を足す行列 P がある。
		 * P = [1 1]
		 *     [0 1]
		 * それに対して P⁻¹ は右から掛けるとj列目にi列目が(-1)倍して足される。
		 * P = [1 -1]
		 *     [0  1]
		 *
		 * 左から掛けたとき、i行目とj行目をswapする行列 P がある。
		 * P = [0 1]
		 *     [1 0]
		 * それに対して P⁻¹ = Pは右から掛けるとi列目とi列目がswapされる。
		 * P = [0 1]
		 *     [1 0]
		 */
		long[][]B=ArrayUtils.copy(A);
		if(B.length != B[0].length) throw new AssertionError();
		int N=B.length;
		for (int i = 0; i < N-2; i++) {
			int p=i+1;
			while(p<N && B[p][i]==0) ++p;
			if(p==N)continue;
			if(i+1!=p) {
				ArrayUtils.swap(B[i+1], B[p]);
				ArrayUtils.swapColumns(i+1, p, B);
			}
			// A[i+1][i] ≠ 0
			long inv = MathUtils.modInv(B[i+1][i], mod);
			for (int j = i+2; j < N; j++) {
				long c = inv * B[j][i] % mod;
				for (int k = 0; k < N; k++) {
					B[j][k] = (B[j][k] + B[i+1][k] * (mod - c)) % mod;
				}
				for (int k = 0; k < N; k++) {
					B[k][i+1] = (B[k][i+1] + B[k][j] * c) % mod;//i列目ではなく、i+1列目に足されるのでok
				}
			}
		}
		return B;
	}


	/**
	 * det(Ix-A)
	 * @param A
	 * @param mod
	 * @return
	 * https://judge.yosupo.jp/submission/344336
	 */
	public static long[] characteristicPolynomialOnFp(long[][]A, long mod) {
		if(A.length==0)return new long[] {1};
		if(A.length!=A[0].length) throw new AssertionError();
		long[][]B=hessenbergReductionOnFp(A, mod);
		int N=A.length;
		long[][]f=new long[N+1][N+1];
		for (int i = 0; i < f.length; i++) {
			f[i][i]=1;
		}
		//f[i][j] = Π A[k+1][k] for i ≤ k < j
		for (int w=1;w<=N;++w) {
			for (int i = 0; i+w <= N && i+1 < N; i++) {
				f[i][i+w]=f[i+1][i+w]*B[i+1][i]%mod;
			}
		}
		long[][] g = new long[N + 1][];
		g[0] = new long[] {1};
		for (int i = 1; i <= N; i++) g[i] = new long[i + 1];

		//g[i] = B の 0,1,..,i-1 行目と 0,1,..,i-1 列目からなる部分行列の特性多項式
		for (int i = 0; i < N; i++) {
			// g[i+1] += g[i] * (x - B[i][i])
			long negBii = (mod - B[i][i] % mod + mod) % mod;
			for (int k = 0; k < g[i].length; k++) {
				g[i + 1][k + 1] = (g[i + 1][k + 1] + g[i][k]) % mod;
				g[i + 1][k] = (g[i + 1][k] + g[i][k] * negBii) % mod;
			}

			for (int j = i + 1; j < N; j++) {
				long c = (mod - f[i][j] * B[i][j] % mod) % mod;
				// g[j+1] += g[i] * c
				for (int k = 0; k < g[i].length; k++) {
					g[j + 1][k] = (g[j + 1][k] + g[i][k] * c) % mod;
				}
			}
		}
		return g[N];
	}



	/**
	 * det(Ax+B)
	 * @param A
	 * @param B
	 * @return
	 * https://yukicoder.me/submissions/1143226
	 */
	public static long[]determinantAxPlusBOnFp(long[][]A, long[][]B, long mod){
		int N=A.length;
		if(A.length!=B.length)throw new AssertionError();
		if(N!=0) {
			if(A[0].length!=B[0].length)throw new AssertionError();
		}

		long[][]C=new long[N][2*N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				C[i][j]=A[i][j];
				C[i][j+N]=B[i][j];
			}
		}
		long d = 1;
		int offset = 0;
		for(int i=0;i<N;++i) {
			if(offset==N+1) {//特性多項式の次数は高々N。N+1次の特性多項式は存在しないので0を返す。
				return new long[]{0};
			}
			{

				int j=i;
				while(j<C.length&&C[j][i]==0)++j;
				if(j==C.length) {
					for (int k = 0; k < i; k++) {
						if (C[k][i] == 0) continue;
						long c=mod-C[k][i];
						for (int l = 0; l < N; l++) {
							C[l][i] = (C[l][i] + c * C[l][k]) % mod;
							C[l][i+N] = (C[l][i+N] + c * C[l][k+N]) % mod;
						}
					}
					ArrayUtils.swapColumns(i, i+N, C);
					offset++;
					i--;
					continue;
				}
				if (i != j) {
					d = (mod - d);
					ArrayUtils.swap(C[i], C[j]);
				}
			}
			d = d * C[i][i] % mod;
			long invCii=MathUtils.modInv(C[i][i], mod);
			for (int j=0;j<C[i].length;++j)C[i][j] = C[i][j] * invCii % mod;
			for(int j=0;j<C.length;++j) {
				if(i==j)continue;
				if(C[j][i]==0)continue;
				long c=mod-C[j][i];
				for(int k=i;k<C[j].length;++k) {
					C[j][k]=(C[j][k]+c*C[i][k])%mod;
				}
			}
		}
		for (int i = 0; i < N; i++) {
			C[i] = Arrays.copyOfRange(C[i], N, 2*N);
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (C[i][j] != 0)
					C[i][j] = mod - C[i][j];
			}
		}
		long[]ret=characteristicPolynomialOnFp(C, mod);
		for (int i = 0; i < ret.length; i++) {
			ret[i]=d*ret[i]%mod;
		}
		return Arrays.copyOfRange(ret, offset, ret.length);
	}


	/**
	 * 無向グラフの完全マッチングの重み和、
	 * すなわち隣接行列 {@code mat} の Hafnian を計算する。
	 *
	 * {@code mat[i][j]} は辺 {@code i-j} の重みを表す。
	 * {@code mat} は対称、かつ {@code mat[i][i] == 0} を仮定する。
	 *
	 * [1] A. Björklund, "Counting Perfect Matchings as Fast as Ryser",
	 *     Proc. of 23rd ACM-SIAM symposium on Discrete Algorithms, pp.914-921, 2012.
	 *
	 * 計算量: O(N^2 2^(N/2))
	 * 未テスト
	 *
	 * @param mat 隣接行列
	 * @param mod 法
	 * @return 完全マッチングの重み和
	 */
	public static long hafnian(long[][] mat, long mod) {
		//https://judge.yosupo.jp/submission/371243
	    int N = mat.length;
	    if (N % 2 != 0) return 0;
	    if (N == 0) return 1;
	    /*
	     * 入力グラフそのものは最初から固定である。
	     *
	     * つまり、mat[j][k] で表される本物の辺は、
	     * 最初から全て使える。
	     *
	     * このアルゴリズムで段階的に追加していくのは、
	     * 入力グラフの辺ではなく、
	     *
	     *   (N-2, N-1), (N-4, N-3), ...
	     *
	     * という「基準ペア辺」である。
	     *
	     * 基準ペア辺は、完全マッチングを分解して数えるために
	     * 後から重ねる補助線であり、数えたい本物のマッチング辺ではない。
	     *
	     * 頂点 0, 1 は最後まで基準ペアにしない。
	     */

	    /*
	     * B[j][k][S]： (j > k)
	     *
	     *   すでに追加済みの基準ペア集合のうち、
	     *   S に含まれる基準ペアを内部に使って、
	     *   j から k へ行く 1 本の「開いた交互パス」の重み和。
	     *
	     * ここで「交互パス」とは、
	     *
	     *   本物のマッチング辺
	     *   基準ペア辺
	     *   本物のマッチング辺
	     *   基準ペア辺
	     *   ...
	     *   本物のマッチング辺
	     *
	     * のように、本物のマッチング辺と基準ペア辺が交互に現れるパス。
	     *
	     * 重みとして掛けるのは、本物のマッチング辺の重みだけ。
	     * 基準ペア辺は補助線なので、重み 1 と考える。
	     *
	     * まだ基準ペアを 1 本も追加していない時点では、
	     * 内部に使える基準ペアは存在しない。
	     *
	     * したがって、S = empty の開いた交互パスは
	     *
	     *   j --本物のマッチング辺-- k
	     *
	     * という直接のパスだけである。
	     *
	     * よって
	     *
	     *   B[j][k][empty] = mat[j][k]
	     *
	     * で初期化する。
	     */
	    long[][][] B = new long[N][N][1];
	    for (int i = 0; i < N; i++) {
	        for (int j = 0; j < N; j++) {
	            B[i][j][0] = (mat[i][j] % mod + mod) % mod;
	        }
	    }

	    /*
	     * h[S]：
	     *
	     *   すでに追加済みの基準ペア集合のうち、
	     *   S に含まれる基準ペアをすべて使って、
	     *   閉じた交互サイクルたちを作る方法の重み和。
	     *
	     * 閉じた交互サイクルとは、
	     *
	     *   本物のマッチング辺
	     *   基準ペア辺
	     *   本物のマッチング辺
	     *   基準ペア辺
	     *   ...
	     *
	     * と交互に進んで、最後に元の頂点へ戻るサイクル。
	     *
	     * 空集合 S = empty は、
	     * 何も作らない 1 通りがあるので h[empty] = 1。
	     */
	    long[] h = {1};

	    /*
	     * ここから、基準ペア辺を後ろから 1 本ずつ追加していく。
	     *
	     * i 回目のループで追加する基準ペアは
	     *
	     *   r1 = N - 2i
	     *   r2 = r1 + 1
	     *
	     * である。
	     *
	     * 例：
	     *
	     *   i = 1 なら (N-2, N-1)
	     *   i = 2 なら (N-4, N-3)
	     *
	     * という順に追加する。
	     *
	     * 重要：
	     *   ここで更新しているのは入力グラフではない。
	     *   入力グラフ mat は最初から固定である。
	     *
	     *   更新しているのは、
	     *
	     *     - どの基準ペア辺まで補助線として追加したか
	     *     - その補助線を使って作れる開いた交互パス B
	     *     - その補助線を使って作れる閉じた交互サイクル集合 h
	     *
	     *   である。
	     */
	    for (int i = 1; i < N / 2; i++) {
	        int r1 = N - i * 2;
	        int r2 = r1 + 1;

	        /*
	         * 新しい基準ペア辺
	         *
	         *   r1 -- r2
	         *
	         * を補助線として追加する。
	         *
	         * まず、この新しい基準ペア辺を含む
	         * 閉じた交互サイクルを h に追加する。
	         *
	         * 新しい基準ペア辺を含む閉じたサイクルを作るには、
	         *
	         *   r2 から r1 へ行く開いた交互パス
	         *
	         * を 1 本作り、最後に補助線である基準ペア辺
	         *
	         *   r1 -- r2
	         *
	         * で閉じればよい。
	         *
	         * 図で書くと、
	         *
	         *   r2 ==本物/基準/本物/...== r1
	         *    \________________________/
	         *        新しい基準ペア辺
	         *
	         * という閉じた交互サイクルになる。
	         *
	         * r1-r2 が本物のマッチング辺として直接選ばれる場合も、
	         *
	         *   B[r2][r1][empty] = mat[r2][r1]
	         *
	         * に含まれている。
	         *
	         * この場合は、
	         *
	         *   本物の辺 r2-r1
	         *   +
	         *   基準ペア辺 r1-r2
	         *
	         * からなる長さ 2 の交互サイクルだと思えばよい。
	         *
	         * 既存の閉じたサイクルたちで使う基準ペア集合と、
	         * 今作る r2-r1 パスで使う基準ペア集合は重なってはいけない。
	         *
	         * そのため、集合を disjoint に分けて畳み込む subset convolution になる。
	         */
	        long[] hAdd = BooleanLattice.mul(h, B[r2][r1], mod);

	        /*
	         * h の添字集合に、新しい基準ペアを表す bit を 1 つ追加する。
	         *
	         * nextH の下半分：
	         *   新しい基準ペア (r1, r2) を使わない場合。
	         *   古い h をそのまま引き継ぐ。
	         *
	         * nextH の上半分：
	         *   新しい基準ペア (r1, r2) を使う場合。
	         *   上で作った hAdd を入れる。
	         */
	        long[] nextH = new long[1 << i];
	        System.arraycopy(h, 0, nextH, 0, h.length);
	        System.arraycopy(hAdd, 0, nextH, h.length, hAdd.length);
	        h = nextH;

	        /*
	         * 次に B[j][k] を更新する。
	         *
	         * 新しい基準ペア辺
	         *
	         *   r1 -- r2
	         *
	         * を内部に含む、古い頂点 j, k 間の開いた交互パスを作る。
	         *
	         * 新しい基準ペアを使うなら、パスの形は必ず次のどちらかになる。
	         *
	         * 形 1：
	         *
	         *   j 〜 r1 --基準ペア-- r2 〜 k
	         *
	         * 形 2：
	         *
	         *   j 〜 r2 --基準ペア-- r1 〜 k
	         *
	         * ここで「〜」の部分は、すでに計算済みの開いた交互パス。
	         *
	         * したがって重み和は、
	         *
	         *   B[r1][j] (*) B[r2][k]
	         *   +
	         *   B[r1][k] (*) B[r2][j]
	         *
	         * になる。
	         *
	         * (*) は subset convolution を表す。
	         *
	         * なぜなら、左側の開いたパスで使う基準ペア集合と、
	         * 右側の開いたパスで使う基準ペア集合は disjoint でなければならないから。
	         *
	         * subset convolution を高速に計算するため、
	         * B[r1][j], B[r2][j] を rank-lifted zeta 変換しておく。
	         */
	        long[][][] B1zeta = new long[r1][][];
	        long[][][] B2zeta = new long[r1][][];
	        for (int j = 0; j < r1; j++) {
	            B1zeta[j] = BooleanLattice.rankLiftedZeta(B[r1][j], mod);
	            B2zeta[j] = BooleanLattice.rankLiftedZeta(B[r2][j], mod);
	        }

	        for (int j = 0; j < r1; j++) {
	            for (int k = 0; k < j; k++) {
	                /*
	                 * この時点で、すでに追加済みの古い基準ペアは i - 1 個。
	                 *
	                 * 現在の B[j][k] は、
	                 * その i - 1 個の基準ペアだけを対象にした集合配列である。
	                 *
	                 * これから、新しい基準ペア (r1, r2) を表す bit を追加して、
	                 *
	                 *   - 新しい基準ペアを使わない開いたパス
	                 *   - 新しい基準ペアを使う開いたパス
	                 *
	                 * の 2 種類をまとめた新しい B[j][k] を作る。
	                 */
	                int n = i - 1;
	                int mask = 1 << n;

	                long[][] H = new long[n + 1][mask];

	                for (int s = 0; s < mask; s++) {
	                    for (int i2 = 0; i2 <= n; i2++) {
	                        if (B1zeta[j][i2][s] != 0) {
	                            for (int j2 = 0; i2 + j2 <= n; j2++) {
	                                H[i2 + j2][s] =
	                                    (H[i2 + j2][s]
	                                        + B1zeta[j][i2][s] * B2zeta[k][j2][s])
	                                    % mod;
	                            }
	                        }

	                        if (B1zeta[k][i2][s] != 0) {
	                            for (int j2 = 0; i2 + j2 <= n; j2++) {
	                                H[i2 + j2][s] =
	                                    (H[i2 + j2][s]
	                                        + B1zeta[k][i2][s] * B2zeta[j][j2][s])
	                                    % mod;
	                            }
	                        }
	                    }
	                }

	                /*
	                 * zeta 空間で rank 方向に畳み込んだ結果を、
	                 * 通常の集合配列に戻す。
	                 *
	                 * Sijk[S] は、
	                 *
	                 *   古い基準ペア集合 S を内部に使い、
	                 *   さらに新しい基準ペア (r1, r2) も内部に使って、
	                 *   j から k へ行く開いた交互パスの重み和
	                 *
	                 * を表す。
	                 *
	                 * つまり Sijk は、
	                 * 新しい基準ペアを「使う」場合の上半分に入る値である。
	                 */
	                long[] Sijk = BooleanLattice.rankLiftedMoebiusAndUnlift(H, mod);

	                /*
	                 * B[j][k] の添字集合に、新しい基準ペアを表す bit を 1 つ追加する。
	                 *
	                 * nextBjk の下半分：
	                 *   新しい基準ペア (r1, r2) を使わない場合。
	                 *   古い B[j][k] をそのまま引き継ぐ。
	                 *
	                 * nextBjk の上半分：
	                 *   新しい基準ペア (r1, r2) を使う場合。
	                 *   上で計算した Sijk を入れる。
	                 */
	                long[] nextBjk = new long[1 << i];

	                System.arraycopy(B[j][k], 0, nextBjk, 0, B[j][k].length);
	                System.arraycopy(Sijk, 0, nextBjk, B[j][k].length, Sijk.length);


	                B[j][k] = nextBjk;
	            }
	        }
	    }

	    /*
	     * すべての基準ペアを追加し終えると、
	     * 基準ペアにしていない特別な頂点は 0, 1 だけになる。
	     *
	     * 全ての基準ペア集合を U とする。
	     *
	     * 任意の完全マッチングに基準ペア辺を重ねると、
	     *
	     *   - 0 と 1 を結ぶ 1 本の開いた交互パス
	     *   - それ以外の閉じた交互サイクルたち
	     *
	     * に一意に分かれる。
	     *
	     * そこで、全基準ペア集合 U を
	     *
	     *   S       : 閉じた交互サイクルたちに使う基準ペア集合
	     *   U \ S   : 0-1 の開いた交互パスに使う基準ペア集合
	     *
	     * に分割する。
	     *
	     * この分割に対応する重みは、
	     *
	     *   h[S] * B[1][0][U \ S]
	     *
	     * である。
	     *
	     * これを全ての S について足せば、
	     * すべての完全マッチングをちょうど 1 回ずつ数えることになる。
	     */
	    long ret = 0;
	    int lastSize = h.length;
	    int fullMask = lastSize - 1;

	    for (int s = 0; s < lastSize; s++) {
	        int complement = fullMask ^ s;
	        ret = (ret + h[s] * B[1][0][complement]) % mod;
	    }

	    return ret;
	}

	/**
	 * 多項式行列 M(x) = Ax + B に対し、双線形形式 u^T adj(Ax + B) v を O(N^3) で計算する。
	 * 行列式補題 det(M + vu^T) = det(M) + u^T adj(M) v より、
	 * u^T adj(Ax + B) v = det(Ax + (B + vu^T)) - det(Ax + B)
	 * を利用する。
	 *
	 * @param A N x N 定数行列
	 * @param B N x N 定数行列
	 * @param u N 次ベクトル
	 * @param v N 次ベクトル
	 * @param mod 法
	 * @return u^T adj(Ax + B) v の係数配列 (res[i] は x^i の係数)
	 */
	public static long[] bilinearFormAdjugateAxPlusBOnFp(long[][] A, long[][] B, long[] u, long[] v, long mod) {
		int n = A.length;
		if (n == 0) return new long[0];
		if (B.length != n || u.length != n || v.length != n) throw new AssertionError();

		long[] q = determinantAxPlusBOnFp(A, B, mod);

		long[][] bNew = new long[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				bNew[i][j] = (B[i][j] + v[i] * u[j]) % mod;
			}
		}
		long[] qNew = determinantAxPlusBOnFp(A, bNew, mod);

		int resLen = Math.max(q.length, qNew.length);
		long[] res = new long[resLen];
		for (int i = 0; i < resLen; i++) {
			long val1 = i < qNew.length ? qNew[i] : 0;
			long val2 = i < q.length ? q[i] : 0;
			res[i] = (val1 - val2 + mod) % mod;
		}
		// 末尾のゼロをトリミング
		int deg = res.length - 1;
		while (deg >= 0 && res[deg] == 0) deg--;
		if (deg == -1) return new long[] {0};
		return Arrays.copyOf(res, deg + 1);
	}

	/**
	 * Sherman-Morrisonの公式を用いて、A^-1 から (A + uv^T)^-1 を mod 上で計算する。
	 * 更新後の行列が特異行列の場合は null を返す。
	 * @param invA Aの逆行列
	 * @param u 列ベクトル
	 * @param v 列ベクトル
	 * @param mod 法
	 * @return (A + uv^T)^-1
	 */
	public static long[][] invUpdateRank1(long[][] invA, long[] u, long[] v, long mod) {
		if (invA == null) return null;
		int n = invA.length;
		long[] nu = new long[n];
		for (int i = 0; i < n; i++) nu[i] = (u[i] % mod + mod) % mod;
		long[] nv = new long[n];
		for (int i = 0; i < n; i++) nv[i] = (v[i] % mod + mod) % mod;

		long[] w = new long[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				w[i] = (w[i] + invA[i][j] * nu[j]) % mod;
			}
		}
		long[] z = new long[n];
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				z[j] = (z[j] + nv[i] * invA[i][j]) % mod;
			}
		}
		long k = 0;
		for (int i = 0; i < n; i++) {
			k = (k + nv[i] * w[i]) % mod;
		}
		long den = (1 + k) % mod;
		if (den == 0) return null;
		long invDen = MathUtils.modInv(den, mod);
		long[][] res = new long[n][n];
		for (int i = 0; i < n; i++) {
			long factor = w[i] * invDen % mod;
			for (int j = 0; j < n; j++) {
				res[i][j] = (invA[i][j] - factor * z[j] % mod + mod) % mod;
			}
		}
		return res;
	}

	/**
	 * 行列の (r, c) 要素を prevVal から nextVal へ書き換えたときの逆行列を mod 上で計算する。
	 * @param invA Aの逆行列
	 * @param r 行インデックス
	 * @param c 列インデックス
	 * @param nextVal 更新後の値
	 * @param prevVal 更新前の値
	 * @param mod 法
	 * @return 更新後の逆行列
	 */
	public static long[][] invUpdatePoint(long[][] invA, int r, int c, long nextVal, long prevVal, long mod) {
		if (invA == null) return null;
		int n = invA.length;
		long[] u = new long[n];
		u[r] = (nextVal - prevVal) % mod;
		if (u[r] < 0) u[r] += mod;
		long[] v = new long[n];
		v[c] = 1;
		return invUpdateRank1(invA, u, v, mod);
	}

	/**
	 * 行列の第 r 行を prevRow から nextRow へ書き換えたときの逆行列を mod 上で計算する。
	 * @param invA Aの逆行列
	 * @param r 行インデックス
	 * @param nextRow 更新後の行ベクトル
	 * @param prevRow 更新前の行ベクトル
	 * @param mod 法
	 * @return 更新後の逆行列
	 */
	public static long[][] invUpdateRow(long[][] invA, int r, long[] nextRow, long[] prevRow, long mod) {
		if (invA == null) return null;
		int n = invA.length;
		long[] u = new long[n];
		u[r] = 1;
		long[] v = new long[n];
		for (int i = 0; i < n; i++) {
			v[i] = (nextRow[i] - prevRow[i]) % mod;
			if (v[i] < 0) v[i] += mod;
		}
		return invUpdateRank1(invA, u, v, mod);
	}

	/**
	 * 行列の第 c 列を prevCol から nextCol へ書き換えたときの逆行列を mod 上で計算する。
	 * @param invA Aの逆行列
	 * @param c 列インデックス
	 * @param nextCol 更新後の列ベクトル
	 * @param prevCol 更新前の列ベクトル
	 * @param mod 法
	 * @return 更新後の逆行列
	 */
	public static long[][] invUpdateCol(long[][] invA, int c, long[] nextCol, long[] prevCol, long mod) {
		if (invA == null) return null;
		int n = invA.length;
		long[] u = new long[n];
		for (int i = 0; i < n; i++) {
			u[i] = (nextCol[i] - prevCol[i]) % mod;
			if (u[i] < 0) u[i] += mod;
		}
		long[] v = new long[n];
		v[c] = 1;
		return invUpdateRank1(invA, u, v, mod);
	}

	/**
	 * 多項式行列 M(x) = Ax + I に対し、左ベクトル u との積 u^T adj(Ax + I) を計算する。
	 *
	 * u^T adj(Ax + I) は F_p[x]^{1 x N} の多項式ベクトルであり、その各成分の多項式（次数は高々 N-1）の
	 * 各係数を O(N^3) の計算量で求める。
	 *
	 * 未テスト。
	 * 計算量: O(N^3)
	 *
	 * @param A N x N の定数行列
	 * @param u 長さ N のベクトル
	 * @param mod 法
	 * @return u^T adj(Ax + I) の係数配列。res[i][j] は x^i の係数であり、j は 0 以上 N-1 以下の成分インデックス。
	 */
	public static long[][] leftVectorAdjugateAxPlusIOnFp(long[][] A, long[] u, long mod) {
		// 未テスト
		int N = A.length;
		if (u.length != N) {
			throw new AssertionError("Matrix and vector dimensions must match.");
		}
		if (N > 0 && A[0].length != N) {
			throw new AssertionError("Matrix must be square.");
		}

		if (N == 0) {
			return new long[0][0];
		}
		if (N == 1) {
			return new long[][] {{(u[0] % mod + mod) % mod}};
		}

		long[][] negAt = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				negAt[i][j] = (mod - A[j][i] % mod + mod) % mod;
			}
		}

		RationalVectorResult res = inverseIminusAxOnFp(negAt, u, mod);
		long[][] R = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				R[i][j] = res.numerators[j][i];
			}
		}

		return R;
	}

	/**
	 * 多項式行列 M(x) = Ax + B に対し、左ベクトル u^T との積 u^T adj(Ax + B) を計算する。
	 *
	 * u^T adj(Ax + B) は F[x]^{1 x N} の多項式ベクトルであり、その各成分の多項式（次数は高々 N-1）の
	 * 各係数を O(N^3) の計算量で求める。
	 *
	 * 未テスト。
	 * 計算量: O(N^3)
	 *
	 * @param A N x N の定数行列
	 * @param B N x N の定数行列
	 * @param u 長さ N のベクトル
	 * @param mod 法
	 * @return u^T adj(Ax + B) の係数配列。res[i][j] は x^i の係数であり、j は 0 以上 N-1 以下の成分インデックス。
	 */
	public static long[][] leftVectorAdjugateAxPlusBOnFp(long[][] A, long[][] B, long[] u, long mod) {
		// 未テスト
		int N = A.length;
		if (B.length != N || u.length != N) {
			throw new AssertionError("Matrix and vector dimensions must match.");
		}
		if (N > 0 && (A[0].length != N || B[0].length != N)) {
			throw new AssertionError("Matrices must be square.");
		}

		if (N == 0) {
			return new long[0][0];
		}
		if (N == 1) {
			return new long[][] {{(u[0] % mod + mod) % mod}};
		}

		long[][] At = new long[N][N];
		long[][] Bt = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				At[i][j] = A[j][i];
				Bt[i][j] = B[j][i];
			}
		}

		RationalVectorResult res;
		try {
			res = inverseAxPlusBOnFp(At, Bt, u, mod);
		} catch (ArithmeticException e) {
			if (e.getMessage() != null && e.getMessage().contains("rank <= N-2")) {
				return new long[N][N];
			}
			throw e;
		}

		Random rnd = new Random(42);
		long x0 = 0;
		long Q_val = 0;
		long detM = 0;
		while (Q_val == 0 || detM == 0) {
			x0 = rnd.nextLong(1, mod);

			// Evaluate Q(x0)
			Q_val = 0;
			long current_power = 1;
			for (int i = 0; i <= N; i++) {
				long term = res.denominator[i] * current_power % mod;
				Q_val = (Q_val + term) % mod;
				current_power = current_power * x0 % mod;
			}
			if (Q_val == 0) continue;

			// Construct M = A * x0 + B and compute detM
			long[][] M = new long[N][N];
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					long term = (A[i][j] * x0 + B[i][j]) % mod;
					M[i][j] = (term + mod) % mod;
				}
			}
			detM = modDeterminant(M, mod);
		}

		long k = detM * MathUtils.modInv(Q_val, mod) % mod;

		long[][] R = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				R[i][j] = res.numerators[j][i] * k % mod;
			}
		}

		return R;
	}
	
	/**
	 * 1 変数有理関数ベクトルを表す構造体。
	 */
	public static class RationalVectorResult {
		/** 各成分の分子多項式の係数。numerators[j][i] は第 j 成分の x^i の係数を表す。 */
		public final long[][] numerators;
		/** 共通分母多項式の係数。denominator[i] は x^i の係数を表す。 */
		public final long[] denominator;

		public RationalVectorResult(long[][] numerators, long[] denominator) {
			this.numerators = numerators;
			this.denominator = denominator;
		}
	}

	/**
	 * 有理関数ベクトル v(x) = (I - Ax)^-1 u を法 mod 上で計算する。
	 *
	 * v(x) = P(x) / Q(x) と表したとき、
	 * 共通分母多項式 Q(x) = det(I - Ax) (次数は高々 N) と、
	 * 分子多項式ベクトル P(x) = adj(I - Ax) u (各成分の次数は高々 N-1) を
	 * それぞれ多項式の係数配列として求める。
	 *
	 * 未テスト。
	 * 計算量: O(N^3)
	 *
	 * @param A N x N 定数行列
	 * @param u 長さ N の定数ベクトル
	 * @param mod 法
	 * @return 計算結果の有理関数ベクトルを表す RationalVectorResult
	 */
	public static RationalVectorResult inverseIminusAxOnFp(long[][] A, long[] u, long mod) {
		// 未テスト
		int N = A.length;
		if (u.length != N) {
			throw new AssertionError("Matrix and vector dimensions must match.");
		}
		if (N > 0 && A[0].length != N) {
			throw new AssertionError("Matrix must be square.");
		}

		if (N == 0) {
			return new RationalVectorResult(new long[0][0], new long[] {1 % mod});
		}
		// (I-Ax)^{-1} = adj(I-Ax)/det(I-Ax)

		// Q(x) = det(I - Ax)
		// det(I - Ax) = \sum_{i=0}^N c_{N-i} x^i, where \sum c_j \lambda^j = det(\lambda I - A).
		long[] charPoly = characteristicPolynomialOnFp(A, mod);
		long[] D = new long[N + 1];
		for (int i = 0; i <= N; i++) {
			D[i] = (charPoly[N - i] % mod + mod) % mod;
		}

		// Compute w_k = A^k u for k = 0 ... N-1
		long[][] W = new long[N][N];
		for (int j = 0; j < N; j++) {
			W[0][j] = (u[j] % mod + mod) % mod;
		}
		for (int k = 1; k < N; k++) {
			W[k] = mul(A, W[k - 1], mod);
		}
		
		// adj(I-Ax) = det(I - Ax)sum[k=0..N-1] x^k A^k mod x^N
		long[][] R = new long[N][N];//R[m] = [x^m] adj(I-Ax)u
		for (int m = 0; m < N; m++) {
			for (int i = 0; i <= m; i++) {
				long d = D[i];
				for (int j = 0; j < N; j++) {
					R[m][j] = (R[m][j] + d * W[m - i][j]) % mod;
				}
			}
		}

		// Format numerators[j][i] as the coefficient of x^i for component j
		long[][] numerators = new long[N][N];
		for (int j = 0; j < N; j++) {
			for (int i = 0; i < N; i++) {
				numerators[j][i] = R[i][j];
			}
		}

		return new RationalVectorResult(numerators, D);
	}

	/**
	 * 有理関数ベクトル v(x) = (Ax + B)^-1 u を法 mod 上で計算する。
	 *
	 * 計算量: O(N^3)
	 *
	 * @param A N x N 定数行列
	 * @param B N x N 定数行列
	 * @param u 長さ N の定数ベクトル
	 * @param mod 法
	 * @return 計算結果の有理関数ベクトルを表す RationalVectorResult
	 */
	public static RationalVectorResult inverseAxPlusBOnFp(long[][] A, long[][] B, long[] u, long mod) {
		// 未テスト
		int N = A.length;
		if (B.length != N || u.length != N) {
			throw new AssertionError("Matrix and vector dimensions must match.");
		}
		if (N > 0 && (A[0].length != N || B[0].length != N)) {
			throw new AssertionError("Matrices must be square.");
		}

		if (N == 0) {
			return new RationalVectorResult(new long[0][0], new long[] {1 % mod});
		}

		long[][] M0 = B;
		long detM0 = modDeterminant(M0, mod);
		long lambda0 = 0;

		if (detM0 == 0) {
			Random rnd = new Random(42);
			boolean found = false;
			double failProb = (double) N / mod;
			if (failProb < 0.1) failProb = 0.1;
			if (failProb > 0.99) failProb = 0.99;
			int trials = 0;
			while (Math.pow(failProb, trials) > 1e-5) {
				trials++;
			}
			if (trials < 5) trials = 5;
			if (trials > 1000) trials = 1000;
			//Ax+B=A(x-λ)+(Aλ+B)でAλ+Bが正則なものが見つかればOK
			for (int trial = 0; trial < trials; trial++) {
				long cand = rnd.nextLong(1, mod);
				long[][] candM0 = new long[N][N];
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < N; j++) {
						candM0[i][j] = (cand * A[i][j] + B[i][j]) % mod;
					}
				}
				long det = modDeterminant(candM0, mod);
				if (det != 0) {
					M0 = candM0;
					detM0 = det;
					lambda0 = cand;
					found = true;
					break;
				}
			}
			if (!found) {
				long x0 = rnd.nextLong(1, mod);
				long[][] candM0 = new long[N][N];
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < N; j++) {
						candM0[i][j] = (x0 * A[i][j] + B[i][j]) % mod;
					}
				}
				int r = rank(candM0, mod);
				if (r <= N - 2) {
					throw new ArithmeticException("singular pencil with rank <= N-2");
				} else {
					throw new ArithmeticException("singular pencil with rank N-1, need kernel interpolation");
				}
			}
		}
		// (A(x-λ)+M0)^{-1} u が求めるもの。
		long[][] invM0 = inv(M0, mod);
		long[][] C = mul(invM0, A, mod);

		// A_new = -C
		long[][] A_new = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				A_new[i][j] = (mod - C[i][j]) % mod;
			}
		}
		// (-C(x-λ)+I)^{-1} M0^{-1} u が求めるもの。
		// (A_new(x-λ)+I)^{-1} u_new が求めるもの。

		// u_new = invM0 * u
		long[] u_new = mul(invM0, u, mod);

		RationalVectorResult res = inverseIminusAxOnFp(A_new, u_new, mod);

		long c = (mod - lambda0) % mod;

		long[][] numerators = new long[N][N];
		var P=PolynomialFpDynamic.of(mod);
		for (int j = 0; j < N; j++) {
			numerators[j] = P.taylorShift(res.numerators[j], c);
		}

		// Shift denominator back
		var denominator = P.taylorShift(res.denominator, c);
		return new RationalVectorResult(numerators, denominator);
	}
}
