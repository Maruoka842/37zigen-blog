package library.util.linalg;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.collections.IntArrayList;
import library.util.collections.LongArrayList;

public class MatrixUtilsF2 {

	/**
	 * MSBから降順に掃き出し法をした基底を返す。基底は値の降順に並んでいる。
	 * @param a
	 * @return
	 */
	public static long[] bitBasisMSB(long[] a) {
		LongArrayList list=new LongArrayList(64);
		for (var v : a) {
			for (int i = 0; i < list.size(); i++) {
				long base = list.get(i);
				v = Long.compareUnsigned(v, v ^ base) < 0 ? v : v ^ base;
			}
			if (v != 0) list.add(v);
		}
		var ret=list.toArray();
		ArrayUtils.rsortUnsigned(ret);
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret.length; j++) {
				if(i==j)continue;
				long v=ret[i];
				long u=ret[j];
				long nv=v^u;
				if (Long.compareUnsigned(nv, v) < 0) ret[i]=nv;
			}
		}
		return ret;
	}
	
	
	/**
	 * 入力ベクトル列と同じ空間を張る F2 上の xor 線形基底を返す。
	 *
	 * <p>未テスト。計算量: O(NB)、B = {@link Integer#SIZE}。</p>
	 *
	 * @param a ベクトル列
	 * @return {@code <a>} の基底
	 */
	public static int[] bitBasisMSB(int[] a) {
		IntArrayList list=new IntArrayList(32);
		for (var v : a) {
			for (int i = 0; i < list.size(); i++) {
				int base = list.get(i);
				v = Integer.compareUnsigned(v, v ^ base) < 0 ? v : v ^ base;
			}
			if (v != 0) list.add(v);
		}
		var ret=list.toArray();
		ArrayUtils.rsortUnsigned(ret);
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret.length; j++) {
				if(i==j)continue;
				int v=ret[i];
				int u=ret[j];
				int nv=v^u;
				if (Integer.compareUnsigned(nv, v) < 0) ret[i]=nv;
			}
		}
		return ret;
	}
	
	/**
	 * F2 上のベクトル空間 {@code <u>} と {@code <v>} の共通部分の基底を返す。
	 * 値は {@code int} を unsigned int とみなした {@link Integer#SIZE} bit ベクトルとして扱う。
	 *
	 * <p>{@code Ux = Vy} と書けるベクトルを探す。F2 では {@code -V = V} なので、
	 * 係数ベクトル {@code (x, y)} は行列 {@code [U | V]} の零空間として求まる。
	 * 零空間の各基底ベクトルから {@code Ux} を復元すると、共通部分の基底が得られる。</p>
	 *
	 * <p>計算量: O((n + m) * B)、B = {@link Integer#SIZE}。</p>
	 *
	 * @param u 1つ目の空間を張るベクトル列
	 * @param v 2つ目の空間を張るベクトル列
	 * @return {@code <u> ∩ <v>} の基底
	 */
	public static int[] bitVectorSpaceIntersectionBasis(int[] u, int[] v) {
		//https://math.stackexchange.com/questions/25371/how-to-find-a-basis-for-the-intersection-of-two-vector-spaces-in-mathbbrn/1828391#1828391
		//https://judge.yosupo.jp/submission/371221
		final int B = Integer.SIZE;
		int[] uBasis = bitBasisMSB(u);
		int[] vBasis = bitBasisMSB(v);
		int vars = uBasis.length + vBasis.length;
		long[][] equations = new long[B][(vars + 63) >> 6];
		for (int b = 0; b < B; b++) {
			// bit b について、Ux と Vy の値が等しいという一次方程式を作る。
			for (int i = 0; i < uBasis.length; i++) {
				if (((uBasis[i] >>> b) & 1) != 0) equations[b][i >> 6] ^= 1L << (i & 63);
			}
			for (int i = 0; i < vBasis.length; i++) {
				int col = uBasis.length + i;
				if (((vBasis[i] >>> b) & 1) != 0) equations[b][col >> 6] ^= 1L << (col & 63);
			}
		}
		long[][] solutions = linearEquationMod2(equations, new long[(B + 63) >> 6], B, vars);
		int[] ans = new int[solutions.length - 1];
		for (int k = 1; k < solutions.length; k++) {
			// solution の U 側係数 x から Ux を復元する。これは同時に Vy でもある。
			int w = 0;
			for (int i = 0; i < uBasis.length; i++) {
				if (((solutions[k][i >> 6] >>> (i & 63)) & 1) != 0) w ^= uBasis[i];
			}
			ans[k - 1] = w;
		}
		return ans;
	}

	/**
	 * {@code val} を F2 上の xor 線形基底へ挿入できたかを返す。
	 *
	 * <p>未テスト。計算量: O(B)、B = {@code basis.length}。</p>
	 *
	 * @param val 挿入したいベクトル
	 * @param basis xor 線形基底。破壊的に更新される
	 * @return {@code val} が既存基底から独立なら {@code true}
	 */
	public static boolean addToBitBasis(int val, int[] basis) {
		for (int b = basis.length - 1; b >= 0; b--) {
			if (((val >>> b) & 1) == 0) continue;
			if (basis[b] == 0) {
				basis[b] = val;
				return true;
			}
			val ^= basis[b];
		}
		return false;
	}

	public static boolean[][] mul(boolean[][] a, boolean[][] b) {
		boolean[][] c=new boolean[a.length][b[0].length];
		for(int i=0;i<a.length;++i) {
			for(int j=0;j<b[0].length;++j) {
				for(int k=0;k<a[i].length;++k) {
					c[i][j]|=a[i][k]&&b[k][j];
				}
			}
		}
		return c;
	}

	public static boolean[][] add(boolean[][] a, boolean[][] b) {
		if (a.length != b.length) throw new AssertionError();
		if (a[0].length != b[0].length) throw new AssertionError();
		boolean[][] ret=new boolean[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				ret[i][j]=a[i][j]|b[i][j];
			}
		}
		return ret;
	}

	public static boolean[][] mod2Mul(boolean[][] a, boolean[][] b) {
		//https://judge.yosupo.jp/submission/370827
		if (a.length == 0) return new boolean[0][b.length == 0 ? 0 : b[0].length];
		if (a[0].length != b.length) throw new AssertionError();
		int n = a.length;
		int k = b.length;
		int m = b[0].length;
		for (int i = 0; i < n; i++) {
			if (a[i].length != k) throw new AssertionError();
		}
		for (int i = 0; i < k; i++) {
			if (b[i].length != m) throw new AssertionError();
		}
		return unpackBitMatrix(mod2Mul(packToBitMatrix(a), packToBitMatrix(b), n, k, m), n, m);
	}

	public static long[][] mod2Mul(long[][] a, long[][] b, int n, int k, int m) {
		// 未テスト
		if (a.length != n) throw new AssertionError();
		if (b.length != k) throw new AssertionError();
		int aCols = (k + 63) >> 6;
		int retCols = (m + 63) >> 6;
		long[][] bt = transposeBitMatrix(b, k, m);
		long[][] ret = new long[n][retCols];
		for (int i = 0; i < n; i++) {
			if (a[i].length < aCols) throw new AssertionError();
			for (int j = 0; j < m; j++) {
				int parity = 0;
				for (int w = 0; w < aCols; w++) {
					parity ^= Long.bitCount(a[i][w] & bt[j][w]);
				}
				if ((parity & 1) != 0) ret[i][j >> 6] |= 1L << (j & 63);
			}
		}
		return ret;
	}

	public static boolean[][] booleanMatrixIdentity(int n) {
		boolean[][] ret=new boolean[n][n];
		for(int i=0;i<n;++i)ret[i][i]=true;
		return ret;
	}

	public static boolean[][] pow(boolean[][] a, long n) {
		if(n==0) {
			return booleanMatrixIdentity(a.length);
		}
		boolean[][] ret=pow(mul(a, a), n/2);
		if(n%2==1)ret=mul(ret,a);
		return ret;
	}

	/**
	 * A^0+A^1+..+A^{N-1}
	 * @param A
	 * @param X
	 * @return
	 */
	public static boolean[][] geometricSum(boolean[][] A, long N) {
		if(N==0)return pow(A, N);
		if(N%2==0) {
			return mul(add(booleanMatrixIdentity(A.length),A),geometricSum(mul(A,A),N/2));
		}else {
			return add(booleanMatrixIdentity(A.length),mul(A,geometricSum(A,N-1)));
		}
	}

	/**
	 * {@code boolean[][]} 形式の行列を bit 形式 (long[][]) に変換する。
	 * @param a {@code boolean[][]} 形式の行列
	 * @return bit 形式 (long[][]) の行列
	 */
	public static long[][] packToBitMatrix(boolean[][] a) {
		if (a.length == 0) return new long[0][0];
		int n = a.length;
		int m = a[0].length;
		int cols = (m + 63) >> 6;
		long[][] b = new long[n][cols];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (a[i][j]) {
					b[i][j >> 6] |= 1L << (j & 63);
				}
			}
		}
		return b;
	}

	/**
	 * {@code boolean[][]} 形式の行列を転置しながら bit 形式 (long[][]) に変換する。
	 * @param a {@code boolean[][]} 形式の行列
	 * @return 転置後の bit 形式 (long[][]) の行列
	 */
	public static long[][] packToTransposedBitMatrix(boolean[][] a) {
		// 未テスト
		if (a.length == 0) return new long[0][0];
		int n = a.length;
		int m = a[0].length;
		long[][] b = new long[m][(n + 63) >> 6];
		for (int i = 0; i < n; i++) {
			if (a[i].length != m) throw new AssertionError();
			for (int j = 0; j < m; j++) {
				if (a[i][j]) {
					b[j][i >> 6] |= 1L << (i & 63);
				}
			}
		}
		return b;
	}

	/**
	 * {@code boolean[]} 形式のベクトルを bit 形式 (long[]) に変換する。
	 * @param a {@code boolean[]} 形式のベクトル
	 * @return bit 形式 (long[]) のベクトル
	 */
	public static long[] packToBitVector(boolean[] a) {
		// 未テスト
		long[] b = new long[(a.length + 63) >> 6];
		for (int i = 0; i < a.length; i++) {
			if (a[i]) b[i >> 6] |= 1L << (i & 63);
		}
		return b;
	}

	/**
	 * bit 形式 (long[][]) の行列を {@code boolean[][]} 形式に変換する。
	 * @param a bit 形式 (long[][]) の行列
	 * @param n 行数
	 * @param m 列数
	 * @return {@code boolean[][]} 形式の行列
	 */
	public static boolean[][] unpackBitMatrix(long[][] a, int n, int m) {
		// 未テスト
		if (a.length != n) throw new AssertionError();
		int cols = (m + 63) >> 6;
		boolean[][] b = new boolean[n][m];
		for (int i = 0; i < n; i++) {
			if (a[i].length < cols) throw new AssertionError();
			for (int j = 0; j < m; j++) {
				b[i][j] = (a[i][j >> 6] & (1L << (j & 63))) != 0;
			}
		}
		return b;
	}

	/**
	 * bit 形式 (long[][]) の行列を転置する。
	 * @param a bit 形式 (long[][]) の行列
	 * @param n 行数
	 * @param m 列数
	 * @return 転置後の bit 形式 (long[][]) の行列
	 */
	public static long[][] transposeBitMatrix(long[][] a, int n, int m) {
		// 未テスト
		if (a.length != n) throw new AssertionError();
		int cols = (m + 63) >> 6;
		long[][] b = new long[m][(n + 63) >> 6];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if ((a[i][j >> 6] & (1L << (j & 63))) != 0) {
					b[j][i >> 6] |= 1L << (i & 63);
				}
			}
		}
		return b;
	}

	/**
	 * {@code boolean[][]} 形式の行列の行列式を mod 2 で返す。
	 * @param a {@code boolean[][]} 形式の行列
	 * @return 行列式 (0 または 1)
	 */
	public static int determinantMod2(boolean[][] a) {
		//https://judge.yosupo.jp/submission/370824
		if (a.length == 0) return 1;
		if (a.length != a[0].length) return 0;
		return determinantMod2(packToBitMatrix(a), a.length);
	}

	/**
	 * 行列 {@code a} の行列式を mod 2 で返す。
	 * {@code a} は bit 形式 (long[][] で各行をパックしたもの) である必要がある。
	 * 行列 {@code a} は内部でコピーされるため、破壊されない。
	 * @param a bit 形式 (long[][]) の行列
	 * @param n 行列のサイズ
	 * @return 行列式 (0 または 1)
	 * @see https://judge.yosupo.jp/problem/matrix_det_mod_2
	 */
	public static int determinantMod2(long[][] a, int n) {
		long[][] b = ArrayUtils.copy(a);
		int cols = (n + 63) >> 6;
		for (int i = 0; i < n; i++) {
			int pivot = -1;
			int wordIdx = i >> 6;
			long bitMask = 1L << (i & 63);
			for (int j = i; j < n; j++) {
				if ((b[j][wordIdx] & bitMask) != 0) {
					pivot = j;
					break;
				}
			}
			if (pivot == -1) return 0;
			if (pivot != i) {
				long[] tmp = b[i];
				b[i] = b[pivot];
				b[pivot] = tmp;
			}
			long[] rowI = b[i];
			for (int j = i + 1; j < n; j++) {
				long[] rowJ = b[j];
				if ((rowJ[wordIdx] & bitMask) != 0) {
					for (int k = wordIdx; k < cols; k++) {
						rowJ[k] ^= rowI[k];
					}
				}
			}
		}
		return 1;
	}

	/**
	 * {@code boolean[][]} 形式の行列のランクを mod 2 で返す。
	 * @param a {@code boolean[][]} 形式の行列
	 * @return ランク
	 */
	public static int rankMod2(boolean[][] a) {
		//https://judge.yosupo.jp/submission/370838
		if (a.length == 0 || a[0].length == 0) return 0;
		int n = a.length;
		int m = a[0].length;
		if (n > m) return rankMod2(packToTransposedBitMatrix(a), m, n);
		return rankMod2(packToBitMatrix(a), n, m);
	}

	/**
	 * 行列 {@code a} のランクを mod 2 で返す。
	 * {@code a} は bit 形式 (long[][] で各行をパックしたもの) である必要がある。
	 * 行列 {@code a} は内部でコピーされるため、破壊されない。
	 * @param a
	 * @param n
	 * @param m
	 * @return
	 * @see https://judge.yosupo.jp/problem/rank_of_matrix_mod_2
	 */
	public static int rankMod2(long[][] a, int n, int m) {
		if (n > m) return rankMod2(transposeBitMatrix(a, n, m), m, n);
		long[][] b = ArrayUtils.copy(a);
		int cols = (m + 63) >> 6;
		int rank = 0;
		for (int i = 0; i < m && rank < n; i++) {
			int pivot = -1;
			int wordIdx = i >> 6;
			long bitMask = 1L << (i & 63);
			for (int j = rank; j < n; j++) {
				if ((b[j][wordIdx] & bitMask) != 0) {
					pivot = j;
					break;
				}
			}
			if (pivot != -1) {
				long[] tmp = b[rank];
				b[rank] = b[pivot];
				b[pivot] = tmp;
				long[] row = b[rank];
				for (int j = rank + 1; j < n; j++) {
					if ((b[j][wordIdx] & bitMask) != 0) {
						for (int k = wordIdx; k < cols; k++) {
							b[j][k] ^= row[k];
						}
					}
				}
				rank++;
			}
		}
		return rank;
	}

	/**
	 * {@code boolean[][]} 形式の行列の逆行列を mod 2 で返す。
	 * 逆行列が存在しない場合は null を返す。
	 * @param a {@code boolean[][]} 形式の行列
	 * @return 逆行列
	 */
	public static boolean[][] invMod2(boolean[][] a) {
		//https://judge.yosupo.jp/submission/370840
		if (a.length == 0) return new boolean[0][0];
		if (a.length != a[0].length) return null;
		int n = a.length;
		long[][] inv = invMod2(packToBitMatrix(a), n);
		if (inv == null) return null;
		return unpackBitMatrix(inv, n, n);
	}

	/**
	 * 行列 {@code a} の逆行列を mod 2 で返す。
	 * {@code a} は bit 形式 (long[][] で各行をパックしたもの) である必要がある。
	 * 逆行列が存在しない場合は null を返す。
	 * 行列 {@code a} は内部でコピーされるため、破壊されない。
	 * @param a
	 * @param n
	 * @return
	 * @see https://judge.yosupo.jp/problem/inverse_matrix_mod_2
	 */
	public static long[][] invMod2(long[][] a, int n) {
		long[][] left = ArrayUtils.copy(a);
		int cols = (n + 63) >> 6;
		long[][] right = new long[n][cols];
		for (int i = 0; i < n; i++) right[i][i >> 6] |= 1L << (i & 63);

		for (int i = 0; i < n; i++) {
			int pivot = -1;
			int wordIdx = i >> 6;
			long bitMask = 1L << (i & 63);
			for (int j = i; j < n; j++) {
				if ((left[j][wordIdx] & bitMask) != 0) {
					pivot = j;
					break;
				}
			}
			if (pivot == -1) return null;
			if (pivot != i) {
				long[] tmpL = left[i]; left[i] = left[pivot]; left[pivot] = tmpL;
				long[] tmpR = right[i]; right[i] = right[pivot]; right[pivot] = tmpR;
			}
			long[] rowLI = left[i];
			long[] rowRI = right[i];
			for (int j = 0; j < n; j++) {
				if (i != j && (left[j][wordIdx] & bitMask) != 0) {
					for (int k = wordIdx; k < cols; k++) left[j][k] ^= rowLI[k];
					for (int k = 0; k < cols; k++) right[j][k] ^= rowRI[k];
				}
			}
		}
		return right;
	}

	/**
	 * {@code boolean[][]} 形式の行列 A と {@code boolean[]} 形式 de ベクトル b について、
	 * Ax = b を満たす解 x と Ax = 0 の解空間の基底を返す。
	 * @param a 行列 A
	 * @param b ベクトル b
	 * @return {x, basis1, basis2, ...} if exists, null otherwise. x and each basis are row vectors.
	 */
	public static boolean[][] linearEquationMod2(boolean[][] a, boolean[] b) {
		// 未テスト
		if (a.length != b.length) throw new AssertionError();
		return linearEquationMod2(a, b, a.length == 0 ? 0 : a[0].length);
	}

	/**
	 * {@code boolean[][]} 形式の行列 A と {@code boolean[]} 形式 de ベクトル b について、
	 * Ax = b を満たす解 x と Ax = 0 の解空間の基底を返す。
	 * @param a 行列 A
	 * @param b ベクトル b
	 * @param m 列数
	 * @return {x, basis1, basis2, ...} if exists, null otherwise. x and each basis are row vectors.
	 */
	public static boolean[][] linearEquationMod2(boolean[][] a, boolean[] b, int m) {
		// 未テスト
		if (a.length != b.length) throw new AssertionError();
		int n = a.length;
		for (int i = 0; i < n; i++) {
			if (a[i].length != m) throw new AssertionError();
		}
		long[][] res = linearEquationMod2(packToBitMatrix(a), packToBitVector(b), n, m);
		if (res == null) return null;
		return unpackBitMatrix(res, res.length, m);
	}

	/**
	 * Ax = b を満たす解 x と Ax = 0 の解空間の基底を返す。
	 * @param a 行列 A (bit-packed long[][])
	 * @param b ベクトル b (bit-packed long[])
	 * @param n 行数
	 * @param m 列数
	 * @return {x, basis1, basis2, ...} if exists, null otherwise. x and each basis is a row vector represented as bit-packed long[].
	 * @see https://judge.yosupo.jp/problem/system_of_linear_equations_mod_2
	 */
	public static long[][] linearEquationMod2(long[][] a, long[] b, int n, int m) {
		int totalCols = m + 1;
		int totalWords = (totalCols + 63) >> 6;
		long[][] mat = new long[n][totalWords];
		for (int i = 0; i < n; i++) {
			System.arraycopy(a[i], 0, mat[i], 0, a[i].length);
			if ((b[i >> 6] & (1L << (i & 63))) != 0) {
				mat[i][m >> 6] |= 1L << (m & 63);
			}
		}

		int rank = 0;
		int[] pivotCol = new int[n];
		Arrays.fill(pivotCol, -1);
		for (int j = 0; j < m && rank < n; j++) {
			int pivot = -1;
			int wordIdx = j >> 6;
			long bitMask = 1L << (j & 63);
			for (int i = rank; i < n; i++) {
				if ((mat[i][wordIdx] & bitMask) != 0) {
					pivot = i;
					break;
				}
			}
			if (pivot != -1) {
				long[] tmp = mat[rank]; mat[rank] = mat[pivot]; mat[pivot] = tmp;
				long[] rowRank = mat[rank];
				for (int i = 0; i < n; i++) {
					if (i != rank && (mat[i][wordIdx] & bitMask) != 0) {
						for (int k = wordIdx; k < totalWords; k++) mat[i][k] ^= rowRank[k];
					}
				}
				pivotCol[rank] = j;
				rank++;
			}
		}

		for (int i = rank; i < n; i++) {
			if ((mat[i][m >> 6] & (1L << (m & 63))) != 0) return null; // No solution
		}

		long[] x = new long[(m + 63) >> 6];
		for (int i = 0; i < rank; i++) {
			if ((mat[i][m >> 6] & (1L << (m & 63))) != 0) {
				x[pivotCol[i] >> 6] |= 1L << (pivotCol[i] & 63);
			}
		}

		boolean[] isPivot = new boolean[m];
		for (int i = 0; i < rank; i++) isPivot[pivotCol[i]] = true;
		int nullity = m - rank;
		long[][] res = new long[1 + nullity][(m + 63) >> 6];
		res[0] = x;
		int idx = 1;
		for (int j = 0; j < m; j++) {
			if (!isPivot[j]) {
				res[idx][j >> 6] |= 1L << (j & 63);
				//自由変数 x[j] = 1 にしたときの核空間の基底ベクトルを作る。
				//x[pivotCol[i]] + mat[i][free1] * x[free1] + mat[i][free2] * x[free2] + ... = 0 より
				//x[pivotCol[i]] = mat[i][j]  for all i
				for (int i = 0; i < rank; i++) {
					if ((mat[i][j >> 6] & (1L << (j & 63))) != 0) {
						res[idx][pivotCol[i] >> 6] |= 1L << (pivotCol[i] & 63);
					}
				}
				idx++;
			}
		}
		return res;
	}
}
