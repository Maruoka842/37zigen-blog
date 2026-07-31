package library.util.linalg;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.Itertools;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.EuclideanDomainStrategy;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.RingStrategy;
import library.util.algebra.strategy.SemiRingStrategy;
import library.util.seq.Permutation;

public class Matrix<T> {
	private final SemiRingStrategy<T> strategy;
	private final Class<T> clazz;

	@SuppressWarnings("unchecked")
	public Matrix(SemiRingStrategy<T> strategy) {
		this.strategy = strategy;
		this.clazz = (Class<T>) strategy.zero().getClass();
	}

	@SuppressWarnings("unchecked")
	private T[][] createArray(int r, int c) {
		return (T[][]) java.lang.reflect.Array.newInstance(clazz, r, c);
	}

	private T[][] copyData(T[][] data) {
		int rows = data.length;
		int cols = rows == 0 ? 0 : data[0].length;
		T[][] newData = createArray(rows, cols);
		for (int i = 0; i < rows; i++) {
			newData[i] = Arrays.copyOf(data[i], cols);
		}
		return newData;
	}

	public T[][] identity(int n) {
		T[][] data = createArray(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				data[i][j] = (i == j) ? strategy.one() : strategy.zero();
			}
		}
		return data;
	}

	public T[][] add(T[][] a, T[][] b) {
		int rows = a.length;
		int cols = rows == 0 ? 0 : a[0].length;
		if (rows != b.length || cols != (b.length == 0 ? 0 : b[0].length)) throw new IllegalArgumentException("Matrix dimensions must match for addition");
		T[][] newData = createArray(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				newData[i][j] = strategy.add(a[i][j], b[i][j]);
			}
		}
		return newData;
	}

	public T[][] mul(T[][] a, T[][] b) {
		int rowsA = a.length;
		int colsA = rowsA == 0 ? 0 : a[0].length;
		int rowsB = b.length;
		int colsB = rowsB == 0 ? 0 : b[0].length;
		if (colsA != rowsB) throw new IllegalArgumentException("Matrix dimensions must match for multiplication");
		T[][] newData = createArray(rowsA, colsB);
		T zero = strategy.zero();
		for (int i = 0; i < rowsA; i++) {
			T[] rowA = a[i];
			T[] rowNew = newData[i];
			Arrays.fill(rowNew, zero);
			for (int k = 0; k < colsA; k++) {
				T aik = rowA[k];
				if (strategy.equals(aik, zero)) continue;
				T[] rowB = b[k];
				for (int j = 0; j < colsB; j++) {
					rowNew[j] = strategy.add(rowNew[j], strategy.mul(aik, rowB[j]));
				}
			}
		}
		return newData;
	}

	public T[][] pow(T[][] a, long exp) {
		int rows = a.length;
		if (rows == 0 || rows != a[0].length) throw new UnsupportedOperationException("Matrix must be square for exponentiation");
		if (exp < 0) {
			return pow(inv(a), -exp);
		}
		T[][] res = identity(rows);
		T[][] base = a;
		while (exp > 0) {
			if ((exp & 1) == 1) res = mul(res, base);
			base = mul(base, base);
			exp >>= 1;
		}
		return res;
	}

	/**
	 * 対称行列に特化したガウスの消去法による行列式計算。
	 *
	 * @param data 正方対称行列
	 * @return 行列式
	 * @complexity O(n^3) (乗算回数: 約 n^3 / 6 回。通常の detGaussian は約 n^3 / 3 回であり、定数倍が半分になる)
	 * @precondition strategy が FieldStrategy であること、かつ行列が正方対称行列であること
	 */
	// 未テスト
	public T detGaussianSymmetric(T[][] data) {
		int n = data.length;
		if (n == 0) return strategy.one();
		if (n != data[0].length) throw new UnsupportedOperationException("Matrix must be square for determinant");
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for detGaussianSymmetric");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[][] mat = copyData(data);
		T res = s.one();
		for (int i = 0; i < n; i++) {
			if (s.equals(mat[i][i], s.zero())) {
				int pivot = -1;
				for (int p = i + 1; p < n; p++) {
					if (!s.equals(mat[p][p], s.zero())) {
						pivot = p;
						break;
					}
				}
				if (pivot != -1) {
					swapRowsAndCols(mat, i, pivot);
				} else {
					int targetC = -1;
					for (int c = i + 1; c < n; c++) {
						if (!s.equals(mat[i][c], s.zero())) {
							targetC = c;
							break;
						}
					}
					if (targetC == -1) {
						return s.zero();
					}
					for (int k = i; k < n; k++) {
						mat[i][k] = s.add(mat[i][k], mat[targetC][k]);
					}
					mat[i][i] = s.add(mat[i][i], mat[i][targetC]);
					for (int k = i + 1; k < n; k++) {
						mat[k][i] = mat[i][k];
					}
					if (s.equals(mat[i][i], s.zero())) {
						// 標数2の体（GF(2)など）では 2x = 0 となるため、加算操作によって対角成分を非ゼロにできない。
						// この場合は残りの活動中の部分行列（サイズ n-i）を取り出して通常の detGaussian で計算し、これまでの積 res と掛け合わせる。
						int subSize = n - i;
						T[][] subMat = createArray(subSize, subSize);
						for (int r = 0; r < subSize; r++) {
							for (int c = 0; c < subSize; c++) {
								subMat[r][c] = mat[i + r][i + c];
							}
						}
						T subDet = detGaussian(subMat);
						return s.mul(res, subDet);
					}
				}
			}
			res = s.mul(res, mat[i][i]);
			T inv = s.inv(mat[i][i]);
			for (int j = i; j < n; j++) {
				mat[i][j] = s.mul(mat[i][j], inv);
			}
			for (int j = i + 1; j < n; j++) {
				T factor = mat[j][i];
				if (s.equals(factor, s.zero())) continue;
				for (int k = j; k < n; k++) {
					mat[j][k] = s.sub(mat[j][k], s.mul(factor, mat[i][k]));
				}
			}
			for (int j = i + 1; j < n; j++) {
				for (int k = j + 1; k < n; k++) {
					mat[k][j] = mat[j][k];
				}
			}
		}
		return res;
	}

	/**
	 * 行列が対称行列であるかを判定する。
	 *
	 * @param data 行列
	 * @return 対称行列であれば true, そうでなければ false
	 * @complexity O(n^2)
	 */
	// 未テスト
	public boolean isSymmetric(T[][] data) {
		int n = data.length;
		for (int i = 0; i < n; i++) {
			if (data[i].length != n) return false;
			for (int j = i + 1; j < n; j++) {
				if (!strategy.equals(data[i][j], data[j][i])) {
					return false;
				}
			}
		}
		return true;
	}

	private void swapRowsAndCols(T[][] mat, int i, int j) {
		if (i == j) return;
		T[] tmp = mat[i];
		mat[i] = mat[j];
		mat[j] = tmp;
		for (int r = 0; r < mat.length; r++) {
			T t = mat[r][i];
			mat[r][i] = mat[r][j];
			mat[r][j] = t;
		}
	}

	public T det(T[][] data) {
		int rows = data.length;
		if (rows == 0) return strategy.one();
		if (rows != data[0].length) throw new UnsupportedOperationException("Matrix must be square for determinant");
		if (!(strategy instanceof CommutativeRingStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a CommutativeRingStrategy for determinant");
		}
		if (strategy instanceof FieldStrategy) {
			return detGaussian(data);
		} else if (strategy instanceof EuclideanDomainStrategy) {
			return detEuclidean(data);
		} else if (strategy instanceof ExactDivRingStrategy) {
			return detBareiss(data);
		} else {
			if (rows >= 6) {
				return detMahajanVinay(data);
			} else {
				return detLeibniz(data);
			}
		}
	}

	/**
	 * Mahajan-Vinay Algorithm (Division-free O(n^4) determinant calculation).
	 * Rote, Günter. "Division-free algorithms for the determinant and the Pfaffian: algebraic and combinatorial approaches."
	 * Computational Discrete Mathematics: Advanced Lectures. Berlin, Heidelberg: Springer Berlin Heidelberg, 2001. 119-135.
	 */
	public T detMahajanVinay(T[][] data) {
		CommutativeRingStrategy<T> s = (CommutativeRingStrategy<T>) strategy;
		int n = data.length;
		@SuppressWarnings("unchecked")
		T[][][] dp = (T[][][]) java.lang.reflect.Array.newInstance(clazz, n, n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					dp[i][j][k] = s.zero();
				}
			}
		}

		for (int c0 = 0; c0 < n; c0++) {
			dp[0][c0][c0] = s.one();
		}

		for (int l = 0; l < n - 1; l++) {
			for (int c = 0; c < n; c++) {
				for (int c0 = 0; c0 <= c; c0++) {
					T val = dp[l][c][c0];
					if (s.equals(val, s.zero())) continue;

					// 遷移①：今のクローを伸ばす (c -> cp)
					for (int cp = c0 + 1; cp < n; cp++) {
						dp[l + 1][cp][c0] = s.add(dp[l + 1][cp][c0], s.mul(val, data[c][cp]));
					}

					// 遷移②：今のクローを閉じて (c -> c0)、新しいクローを始める (c0')
					T closedWeight = s.neg(s.mul(val, data[c][c0]));
					for (int c0p = c0 + 1; c0p < n; c0p++) {
						dp[l + 1][c0p][c0p] = s.add(dp[l + 1][c0p][c0p], closedWeight);
					}
				}
			}
		}

		T sum = s.zero();
		for (int c = 0; c < n; c++) {
			for (int c0 = 0; c0 <= c; c0++) {
				T val = dp[n - 1][c][c0];
				if (!s.equals(val, s.zero())) {
					sum = s.add(sum, s.mul(val, s.neg(data[c][c0])));
				}
			}
		}

		return (n % 2 == 1) ? s.neg(sum) : sum;
	}

	public T detGaussian(T[][] data) {
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		int n = data.length;
		T[][] mat = copyData(data);
		T res = s.one();
		for (int i = 0; i < n; i++) {
			int pivot = i;
			while (pivot < n && s.equals(mat[pivot][i], s.zero())) pivot++;
			if (pivot == n) return s.zero();
			if (pivot != i) {
				T[] tmp = mat[i]; mat[i] = mat[pivot]; mat[pivot] = tmp;
				res = s.neg(res);
			}
			res = s.mul(res, mat[i][i]);
			T inv = s.inv(mat[i][i]);
			for (int j = i; j < n; j++) mat[i][j] = s.mul(mat[i][j], inv);
			for (int j = i + 1; j < n; j++) {
				T factor = mat[j][i];
				for (int k = i; k < n; k++) {
					mat[j][k] = s.sub(mat[j][k], s.mul(factor, mat[i][k]));
				}
			}
		}
		return res;
	}

	public T detLeibniz(T[][] data) {
		RingStrategy<T> s = (RingStrategy<T>) strategy;
		int n = data.length;
		T res = s.zero();
		for (int[] p : Itertools.permutations(n)) {
			T term = s.one();
			for (int i = 0; i < n; i++) {
				term = s.mul(term, data[i][p[i]]);
			}
			if (Permutation.sign(p) == -1) term = s.neg(term);
			res = s.add(res, term);
		}
		return res;
	}

	public T detBareiss(T[][] data) {
		ExactDivRingStrategy<T> s = (ExactDivRingStrategy<T>) strategy;
		int n = data.length;
		T[][] mat = copyData(data);
		T prevPivot = s.one();
		boolean sign = false;
		for (int k = 0; k < n; k++) {
			int pivot = k;
			while (pivot < n && s.equals(mat[pivot][k], s.zero())) pivot++;
			if (pivot == n) return s.zero();
			if (pivot != k) {
				T[] tmp = mat[k]; mat[k] = mat[pivot]; mat[pivot] = tmp;
				sign = !sign;
			}
			for (int i = k + 1; i < n; i++) {
				for (int j = k + 1; j < n; j++) {
					T val = s.sub(s.mul(mat[k][k], mat[i][j]), s.mul(mat[i][k], mat[k][j]));
					mat[i][j] = s.exactDiv(val, prevPivot);
				}
			}
			prevPivot = mat[k][k];
		}
		T res = mat[n - 1][n - 1];
		return sign ? s.neg(res) : res;
	}

	public T detEuclidean(T[][] data) {
		EuclideanDomainStrategy<T> s = (EuclideanDomainStrategy<T>) strategy;
		int n = data.length;
		T[][] mat = copyData(data);
		boolean sign = false;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				while (!s.equals(mat[j][i], s.zero())) {
					T q = s.div(mat[i][i], mat[j][i]);
					for (int k = i; k < n; k++) {
						mat[i][k] = s.sub(mat[i][k], s.mul(q, mat[j][k]));
					}
					T[] tmp = mat[i]; mat[i] = mat[j]; mat[j] = tmp;
					sign = !sign;
				}
			}
			if (s.equals(mat[i][i], s.zero())) return s.zero();
		}
		T res = s.one();
		for (int i = 0; i < n; i++) res = s.mul(res, mat[i][i]);
		return sign ? s.neg(res) : res;
	}

	public record SmithResult<T>(T[][] U, T[][] S, T[][] V, int rank) {}

	/**
	 * Smith Normal Form calculation for Euclidean Domain.
	 * UAV = S where U, V are unimodular matrices and S is a diagonal matrix.
	 * S[i][i] divides S[i+1][i+1].
	 * @param a
	 * @return SmithResult
	 */
	public SmithResult<T> smithNormalForm(T[][] a) {
		if (!(strategy instanceof EuclideanDomainStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a EuclideanDomainStrategy for Smith Normal Form");
		}
		EuclideanDomainStrategy<T> s = (EuclideanDomainStrategy<T>) strategy;
		int n = a.length;
		if (n == 0) return new SmithResult<>(createArray(0, 0), createArray(0, 0), createArray(0, 0), 0);
		int m = a[0].length;
		T[][] S = copyData(a);
		T[][] U = identity(n);
		T[][] V = identity(m);

		int minNM = Math.min(n, m);
		for (int k = 0; k < minNM; k++) {
			while (true) {
				int pi = -1, pj = -1;
				long minNorm = Long.MAX_VALUE;
				for (int i = k; i < n; i++) {
					for (int j = k; j < m; j++) {
						if (!s.equals(S[i][j], s.zero())) {
							long norm = s.norm(S[i][j]);
							if (pi == -1 || norm < minNorm) {
								pi = i; pj = j; minNorm = norm;
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
				for (int i = k + 1; i < n; i++) {
					if (!s.equals(S[i][k], s.zero())) {
						T q = s.div(S[i][k], S[k][k]);
						if (!s.equals(q, s.zero())) {
							for (int j = k; j < m; j++) S[i][j] = s.sub(S[i][j], s.mul(q, S[k][j]));
							for (int j = 0; j < n; j++) U[i][j] = s.sub(U[i][j], s.mul(q, U[k][j]));
						}
						if (!s.equals(S[i][k], s.zero())) changed = true;
					}
				}
				for (int j = k + 1; j < m; j++) {
					if (!s.equals(S[k][j], s.zero())) {
						T q = s.div(S[k][j], S[k][k]);
						if (!s.equals(q, s.zero())) {
							for (int i = k; i < n; i++) S[i][j] = s.sub(S[i][j], s.mul(q, S[i][k]));
							for (int i = 0; i < m; i++) V[i][j] = s.sub(V[i][j], s.mul(q, V[i][k]));
						}
						if (!s.equals(S[k][j], s.zero())) changed = true;
					}
				}
				if (changed) continue;

				boolean divisible = true;
				out: for (int i = k + 1; i < n; i++) {
					for (int j = k + 1; j < m; j++) {
						if (!s.equals(s.mod(S[i][j], S[k][k]), s.zero())) {
							for (int l = k; l < m; l++) S[k][l] = s.add(S[k][l], S[i][l]);
							for (int l = 0; l < n; l++) U[k][l] = s.add(U[k][l], U[i][l]);
							divisible = false;
							break out;
						}
					}
				}
				if (divisible) break;
			}
			// canonicalize
			if (!s.equals(S[k][k], s.zero())) {
				T unit = s.canonicalUnit(S[k][k]);
				if (!s.equals(unit, s.one())) {
					for (int j = k; j < m; j++) S[k][j] = s.div(S[k][j], unit);
					for (int j = 0; j < n; j++) U[k][j] = s.div(U[k][j], unit);
				}
			}
		}
		int rank = 0;
		while (rank < minNM && !s.equals(S[rank][rank], s.zero())) rank++;
		return new SmithResult<>(U, S, V, rank);
	}

	/**
	 * 与えられた行列 {@code a} を既約行階段形（Reduced Row Echelon Form, RREF）に変換した行列を返す。
	 * @param a 行列
	 * @return a の既約行階段形を表す新しい行列
	 */
	public T[][] reducedRowEchelonForm(T[][] a) {
		int n = a.length;
		if (n == 0) return copyData(a);
		int m = a[0].length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for RREF");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[][] b = copyData(a);
		int rank = 0;
		for (int i = 0; i < m && rank < n; i++) {
			int pivot = rank;
			while (pivot < n && s.equals(b[pivot][i], s.zero())) pivot++;
			if (pivot == n) continue;

			T[] tmp = b[rank]; b[rank] = b[pivot]; b[pivot] = tmp;

			T inv = s.inv(b[rank][i]);
			for (int k = i; k < m; k++) b[rank][k] = s.mul(b[rank][k], inv);
			for (int j = 0; j < n; j++) {
				if (rank == j || s.equals(b[j][i], s.zero())) continue;
				T factor = b[j][i];
				for (int k = i; k < m; k++) {
					b[j][k] = s.sub(b[j][k], s.mul(factor, b[rank][k]));
				}
			}
			rank++;
		}
		return b;
	}

	/**
	 * Ax = b を満たす解 x と Ax = 0 の解空間の基底を返す。
	 * @param a 行列 A
	 * @param b ベクトル b
	 * @return {x, basis1, basis2, ...} if exists, null otherwise. x and each basis are row vectors.
	 */
	public T[][] linearEquation(T[][] a, T[] b) {
		if (strategy instanceof FieldStrategy) {
			return linearEquationField(a, b);
		} else if (strategy instanceof EuclideanDomainStrategy) {
			return linearEquationEuclidean(a, b);
		} else {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy or EuclideanDomainStrategy for linearEquation");
		}
	}

	public T[][] linearEquationField(T[][] a, T[] b) {
		int n = a.length;
		int m = n == 0 ? 0 : a[0].length;
		if (n != b.length) throw new IllegalArgumentException("Matrix and vector dimensions must match");
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;

		T[][] mat = createArray(n, m + 1);
		for (int i = 0; i < n; i++) {
			System.arraycopy(a[i], 0, mat[i], 0, m);
			mat[i][m] = b[i];
		}

		T[][] rref = reducedRowEchelonForm(mat);

		int rank = 0;
		int[] pivotCol = new int[n];
		Arrays.fill(pivotCol, -1);
		for (int i = 0; i < n; i++) {
			int j = 0;
			while (j < m + 1 && s.equals(rref[i][j], s.zero())) j++;
			if (j == m + 1) continue;
			if (j == m) return null; // No solution
			pivotCol[rank] = j;
			rank++;
		}

		T[] x = createVector(m);
		Arrays.fill(x, s.zero());
		for (int i = 0; i < rank; i++) {
			x[pivotCol[i]] = rref[i][m];
		}

		boolean[] isPivot = new boolean[m];
		for (int i = 0; i < rank; i++) isPivot[pivotCol[i]] = true;
		int nullity = m - rank;
		T[][] res = createArray(1 + nullity, m);
		res[0] = x;
		int idx = 1;
		for (int j = 0; j < m; j++) {
			if (!isPivot[j]) {
				T[] basis = createVector(m);
				Arrays.fill(basis, s.zero());
				basis[j] = s.one();
				for (int i = 0; i < rank; i++) {
					basis[pivotCol[i]] = s.neg(rref[i][j]);
				}
				res[idx] = basis;
				idx++;
			}
		}
		return res;
	}

	public T[][] linearEquationEuclidean(T[][] a, T[] b) {
		int n = a.length;
		if (n == 0) return null;
		int m = a[0].length;
		if (n != b.length) throw new IllegalArgumentException("Matrix and vector dimensions must match");
		EuclideanDomainStrategy<T> s = (EuclideanDomainStrategy<T>) strategy;

		SmithResult<T> sr = smithNormalForm(a);
		T[][] U = sr.U();
		T[][] S = sr.S();
		T[][] V = sr.V();
		int rank = sr.rank();

		// Ax = b => UAx = Ub => UAVy = Ub => Sy = Ub where x = Vy
		T[] Ub = createVector(n);
		for (int i = 0; i < n; i++) {
			T sum = s.zero();
			for (int j = 0; j < n; j++) {
				sum = s.add(sum, s.mul(U[i][j], b[j]));
			}
			Ub[i] = sum;
		}

		T[] y = createVector(m);
		for (int i = 0; i < rank; i++) {
			if (!s.equals(s.mod(Ub[i], S[i][i]), s.zero())) return null;
			y[i] = s.div(Ub[i], S[i][i]);
		}
		for (int i = rank; i < n; i++) {
			if (!s.equals(Ub[i], s.zero())) return null;
		}

		T[][] res = createArray(1 + (m - rank), m);
		T[] x = createVector(m);
		Arrays.fill(x, s.zero());
		for (int i = 0; i < m; i++) {
			T sum = s.zero();
			for (int j = 0; j < rank; j++) {
				sum = s.add(sum, s.mul(V[i][j], y[j]));
			}
			x[i] = sum;
		}
		res[0] = x;

		for (int k = 0; k < m - rank; k++) {
			T[] basis = createVector(m);
			for (int i = 0; i < m; i++) {
				basis[i] = V[i][rank + k];
			}
			res[k + 1] = basis;
		}
		return res;
	}

	/**
	 * 行列 {@code a} の核空間の基底を並べた行列Bを返す。
	 * AB=0を満たす。B[*][j]がj番目の基底を表す。
	 * @param a 行列
	 * @return 核空間の基底を縦ベクトルとして並べた行列
	 */
	public T[][] nullSpace(T[][] a) {
		int n = a.length;
		if (n == 0) return createArray(0, 0);
		int m = a[0].length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for nullSpace");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;

		T[][] b = reducedRowEchelonForm(a);
		boolean[] isFree = new boolean[m];
		Arrays.fill(isFree, true);
		int[] pivotCol = new int[n];
		Arrays.fill(pivotCol, m);
		int rank = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (s.equals(b[i][j], s.one())) {
					isFree[j] = false;
					pivotCol[i] = j;
					rank++;
					break;
				}
			}
		}

		int nullity = m - rank;
		T[][] ret = createArray(m, nullity);
		int pointer = 0;
		for (int j = 0; j < m; j++) {
			if (isFree[j]) {
				for (int i = 0; i < n; i++) {
					if (pivotCol[i] < m) {
						ret[pivotCol[i]][pointer] = s.neg(b[i][j]);
					}
				}
				ret[j][pointer] = s.one();
				pointer++;
			}
		}
		// fill zeros
		T zero = s.zero();
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < nullity; j++) {
				if (ret[i][j] == null) ret[i][j] = zero;
			}
		}
		return ret;
	}

	public T[][] inv(T[][] data) {
		int n = data.length;
		if (n == 0 || n != data[0].length) throw new UnsupportedOperationException("Matrix must be square for inversion");
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for inversion");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[][] left = copyData(data);
		T[][] right = identity(n);

		for (int i = 0; i < n; i++) {
			int pivot = i;
			while (pivot < n && s.equals(left[pivot][i], s.zero())) pivot++;
			if (pivot == n) return null; // Singular matrix
			T[] tmpL = left[i]; left[i] = left[pivot]; left[pivot] = tmpL;
			T[] tmpR = right[i]; right[i] = right[pivot]; right[pivot] = tmpR;

			T inv = s.inv(left[i][i]);
			for (int j = 0; j < n; j++) {
				left[i][j] = s.mul(left[i][j], inv);
				right[i][j] = s.mul(right[i][j], inv);
			}

			for (int j = 0; j < n; j++) {
				if (i != j) {
					T factor = left[j][i];
					for (int k = 0; k < n; k++) {
						left[j][k] = s.sub(left[j][k], s.mul(factor, left[i][k]));
						right[j][k] = s.sub(right[j][k], s.mul(factor, right[i][k]));
					}
				}
			}
		}
		return right;
	}

	public T[][] solve(T[][] a, T[][] b) {
		int n = a.length;
		int m = n == 0 ? 0 : a[0].length;
		int bn = b.length;
		int k = bn == 0 ? 0 : b[0].length;
		if (n != bn) throw new IllegalArgumentException("Matrix and vector dimensions must match");
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for solving linear equations");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[][] mat = createArray(n, m + k);
		for (int i = 0; i < n; i++) {
			System.arraycopy(a[i], 0, mat[i], 0, m);
			System.arraycopy(b[i], 0, mat[i], m, k);
		}

		int rank = 0;
		for (int j = 0; j < m && rank < n; j++) {
			int pivot = rank;
			while (pivot < n && s.equals(mat[pivot][j], s.zero())) pivot++;
			if (pivot == n) continue;
			T[] tmp = mat[rank]; mat[rank] = mat[pivot]; mat[pivot] = tmp;

			T inv = s.inv(mat[rank][j]);
			for (int l = j; l < m + k; l++) mat[rank][l] = s.mul(mat[rank][l], inv);

			for (int i = 0; i < n; i++) {
				if (i != rank) {
					T factor = mat[i][j];
					for (int l = j; l < m + k; l++) {
						mat[i][l] = s.sub(mat[i][l], s.mul(factor, mat[rank][l]));
					}
				}
			}
			rank++;
		}

		for (int i = rank; i < n; i++) {
			for (int j = m; j < m + k; j++) {
				if (!s.equals(mat[i][j], s.zero())) return null; // No solution
			}
		}

		T[][] res = createArray(m, k);
		int r = 0;
		for (int j = 0; j < m; j++) {
			boolean isPivot = false;
			if (r < rank) {
				int pj = 0;
				while (pj < m && s.equals(mat[r][pj], s.zero())) pj++;
				if (pj == j) isPivot = true;
			}
			if (isPivot) {
				for (int l = 0; l < k; l++) res[j][l] = mat[r][m + l];
				r++;
			} else {
				for (int l = 0; l < k; l++) res[j][l] = s.zero();
			}
		}
		return res;
	}

	/**
	 * Sherman-Morrisonの公式を用いて、A^-1 から (A + uv^T)^-1 を計算する。
	 * (A + uv^T)^-1 = A^-1 - (A^-1 u v^T A^-1) / (1 + v^T A^-1 u)
	 * 更新後の行列が特異行列の場合は null を返す。
	 * @param invA Aの逆行列
	 * @param u 列ベクトル
	 * @param v 列ベクトル (v^T として用いる)
	 * @return (A + uv^T)^-1
	 */
	public T[][] invUpdateRank1(T[][] invA, T[] u, T[] v) {
		/* (A + BC)^-1
		 * =(I+A^{-1}BC)^{-1}A^{-1} 
		 * 
		 * (I - XY)^{-1} 
		 * = ∑(XY)^i
		 * = I + X{∑(YX)^i}Y
		 * = I + X(I-YX)^{-1}Y
		 * を用いると
		 * 
		 * (I+A^{-1}BC)^{-1}A^{-1} 
		 * =(I - A^{-1}B(I+CA^{-1}B)^{-1}C)A^{-1} 
		 * =A^{-1} - A^{-1}B(I+CA^{-1}B)^{-1}CA^{-1} 
		 */
		if (invA == null) return null;
		int n = invA.length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for Sherman-Morrison update");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;

		T[] w = createVector(n);
		for (int i = 0; i < n; i++) {
			T sum = s.zero();
			for (int j = 0; j < n; j++) {
				sum = s.add(sum, s.mul(invA[i][j], u[j]));
			}
			w[i] = sum;
		}

		T[] z = createVector(n);
		for (int j = 0; j < n; j++) {
			T sum = s.zero();
			for (int i = 0; i < n; i++) {
				sum = s.add(sum, s.mul(v[i], invA[i][j]));
			}
			z[j] = sum;
		}

		T k = s.zero();
		for (int i = 0; i < n; i++) {
			k = s.add(k, s.mul(v[i], w[i]));
		}

		T den = s.add(s.one(), k);
		if (s.equals(den, s.zero())) return null;

		T invDen = s.inv(den);
		T[][] res = createArray(n, n);
		for (int i = 0; i < n; i++) {
			T factor = s.mul(w[i], invDen);
			for (int j = 0; j < n; j++) {
				res[i][j] = s.sub(invA[i][j], s.mul(factor, z[j]));
			}
		}
		return res;
	}

	/**
	 * 行列の (r, c) 要素を prevVal から nextVal へ書き換えたときの逆行列を計算する。
	 * @param invA Aの逆行列
	 * @param r 行インデックス
	 * @param c 列インデックス
	 * @param nextVal 更新後の値
	 * @param prevVal 更新前の値
	 * @return 更新後の逆行列
	 */
	public T[][] invUpdatePoint(T[][] invA, int r, int c, T nextVal, T prevVal) {
		if (invA == null) return null;
		int n = invA.length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for Sherman-Morrison update");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[] u = createVector(n);
		Arrays.fill(u, s.zero());
		u[r] = s.sub(nextVal, prevVal);
		T[] v = createVector(n);
		Arrays.fill(v, s.zero());
		v[c] = s.one();
		return invUpdateRank1(invA, u, v);
	}

	/**
	 * 行列の第 r 行を prevRow から nextRow へ書き換えたときの逆行列を計算する。
	 * @param invA Aの逆行列
	 * @param r 行インデックス
	 * @param nextRow 更新後の行ベクトル
	 * @param prevRow 更新前の行ベクトル
	 * @return 更新後の逆行列
	 */
	public T[][] invUpdateRow(T[][] invA, int r, T[] nextRow, T[] prevRow) {
		if (invA == null) return null;
		int n = invA.length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for Sherman-Morrison update");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[] u = createVector(n);
		Arrays.fill(u, s.zero());
		u[r] = s.one();
		T[] v = createVector(n);
		for (int i = 0; i < n; i++) v[i] = s.sub(nextRow[i], prevRow[i]);
		return invUpdateRank1(invA, u, v);
	}

	/**
	 * 行列の第 c 列を prevCol から nextCol へ書き換えたときの逆行列を計算する。
	 * @param invA Aの逆行列
	 * @param c 列インデックス
	 * @param nextCol 更新後の列ベクトル
	 * @param prevCol 更新前の列ベクトル
	 * @return 更新後の逆行列
	 */
	public T[][] invUpdateCol(T[][] invA, int c, T[] nextCol, T[] prevCol) {
		if (invA == null) return null;
		int n = invA.length;
		if (!(strategy instanceof FieldStrategy)) {
			throw new UnsupportedOperationException("Strategy must be a FieldStrategy for Sherman-Morrison update");
		}
		FieldStrategy<T> s = (FieldStrategy<T>) strategy;
		T[] u = createVector(n);
		for (int i = 0; i < n; i++) u[i] = s.sub(nextCol[i], prevCol[i]);
		T[] v = createVector(n);
		Arrays.fill(v, s.zero());
		v[c] = s.one();
		return invUpdateRank1(invA, u, v);
	}

	@SuppressWarnings("unchecked")
	private T[] createVector(int n) {
		return (T[]) java.lang.reflect.Array.newInstance(clazz, n);
	}
}
