package library.util.graph;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import library.util.algebra.instance.FractionFieldElement;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.FieldStrategy;
import library.util.algebra.strategy.FractionFieldStrategy;
import library.util.algebra.strategy.GCDDomainStrategy;
import library.util.algebra.strategy.IntegralDomainStrategy;
import library.util.algebra.strategy.FpStrategy;
import library.util.algebra.strategy.ZStrategy;
import library.util.algebra.strategy.ZnStrategy;
import library.util.linalg.Matrix;
import library.util.linalg.MatrixUtilsFp;
import library.util.linalg.MatrixUtilsZ;
import library.util.polynomial.PolynomialFpDynamic;
import library.util.polynomial.PolynomialLong3D;

/**
 * Transfer Matrix Method を用いて、グラフ上のウォークの重み和の母関数を計算するライブラリです。
 */
public class TransferMatrixMethod {

	/**
	 * 始点 s から終点 t へのウォークの重み和の母関数を返します。
	 * 母関数の n 次の係数 [x^n] は、s から t への長さ n の全ウォークの重み和を表します。
	 * ウォークの重みは、通る辺の重みの積として定義されます。
	 *
	 * @param g 対象のグラフ
	 * @param s 始点
	 * @param t 終点
	 * @param mod 法
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static FractionFieldElement<long[]> fixedWalkGeneratingFunction(LongValueDigraph g, int s, int t, long mod) {
		int N = g.N;
		long[][] A = constructAdjacencyMatrix(g, mod);
		var polyMat = getPolynomialMatrix(A, mod);

		// 分母 Q(x) = det(I - xA)
		long[] Q = MatrixUtilsFp.determinantAxPlusBOnFp(polyMat.A, polyMat.B, mod);

		// 分子 P(x) = e_s^T adj(I - xA) e_t = [adj(I - xA)]_{st}
		long[] u = new long[N];
		long[] v = new long[N];
		u[s] = 1;
		v[t] = 1;
		long[] numerator = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(polyMat.A, polyMat.B, u, v, mod);

		return simplify(numerator, Q, mod);
	}

	/**
	 * 始点 s から任意の頂点へのウォークの重み和の母関数を返します。
	 * 母関数の n 次の係数 [x^n] は、s から任意の頂点への長さ n の全ウォークの重み和を表します。
	 *
	 * @param g 対象の グラフ
	 * @param s 始点
	 * @param mod 法
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static FractionFieldElement<long[]> fixedStartWalkGeneratingFunction(LongValueDigraph g, int s, long mod) {
		int N = g.N;
		long[][] A = constructAdjacencyMatrix(g, mod);
		var polyMat = getPolynomialMatrix(A, mod);

		// 分母 Q(x) = det(I - xA)
		long[] Q = MatrixUtilsFp.determinantAxPlusBOnFp(polyMat.A, polyMat.B, mod);

		// 分子 P(x) = e_s^T adj(I - xA) 1
		long[] u = new long[N];
		long[] v = new long[N];
		u[s] = 1;
		Arrays.fill(v, 1);
		long[] numerator = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(polyMat.A, polyMat.B, u, v, mod);

		return simplify(numerator, Q, mod);
	}

	/**
	 * 全ての頂点ペア間のウォークの重み和の合計の母関数を返します。
	 * 母関数の n 次の係数 [x^n] は、グラフ上の長さ n の全ウォークの重み和を表します。
	 *
	 * @param g 対象のグラフ
	 * @param mod 法
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static FractionFieldElement<long[]> freeWalkGeneratingFunction(LongValueDigraph g, long mod) {
		int N = g.N;
		long[][] A = constructAdjacencyMatrix(g, mod);
		var polyMat = getPolynomialMatrix(A, mod);

		// 分母 Q(x) = det(I - xA)
		long[] Q = MatrixUtilsFp.determinantAxPlusBOnFp(polyMat.A, polyMat.B, mod);

		// 分子 P(x) = 1^T adj(I - xA) 1
		long[] u = new long[N];
		long[] v = new long[N];
		Arrays.fill(u, 1);
		Arrays.fill(v, 1);
		long[] numerator = MatrixUtilsFp.bilinearFormAdjugateAxPlusBOnFp(polyMat.A, polyMat.B, u, v, mod);

		return simplify(numerator, Q, mod);
	}

	/**
	 * 全ての閉じたウォーク (Closed Walk) の重み和の母関数を返します。
	 * 母関数の n 次の係数 [x^n] は、長さ n (n >= 1) の全閉じたウォークの重み和 tr(A^n) を表します。
	 * n = 0 の係数は 0 となります。
	 *
	 * @param g 対象のグラフ
	 * @param mod 法
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static FractionFieldElement<long[]> closedWalkGeneratingFunction(LongValueDigraph g, long mod) {
		long[][] A = constructAdjacencyMatrix(g, mod);
		var polyMat = getPolynomialMatrix(A, mod);

		// 分母 Q(x) = det(I - xA)
		long[] Q = MatrixUtilsFp.determinantAxPlusBOnFp(polyMat.A, polyMat.B, mod);

		// sum_{n=1} tr(A^n) x^n = -x Q'(x) / Q(x)
		// 分子 P(x) = -x Q'(x)
		long[] numerator = new long[Q.length];
		for (int i = 1; i < Q.length; i++) {
			// Q(x) = sum q_i x^i
			// -x Q'(x) = sum (-i q_i) x^i
			numerator[i] = (mod - (i * Q[i] % mod)) % mod;
		}

		return simplify(numerator, Q, mod);
	}

	private static long[][] constructAdjacencyMatrix(LongValueDigraph g, long mod) {
		int N = g.N;
		long[][] A = new long[N][N];
		for (int u = 0; u < N; u++) {
			for (Edge e : g.adj[u]) {
				A[u][e.dst] = (A[u][e.dst] + (e.cost % mod + mod) % mod) % mod;
			}
		}
		return A;
	}

	/**
	 * 始点 s から終点 t へのウォークの重み和の母関数を返します。
	 *
	 * @param g 対象のグラフ
	 * @param s 始点
	 * @param t 終点
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static <T> FractionFieldElement<T> fixedWalkGeneratingFunction(ValueDigraph<T> g, int s, int t, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		// M = I - A
		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = matrix.det(M);

		// num = adj(M)_{st} = det(M + e_t e_s^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		M2[t][s] = strategy.add(M2[t][s], strategy.one());
		T detM2 = matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}

	/**
	 * 始点 s から任意の頂点へのウォークの重み和の母関数を返します。
	 *
	 * @param g 対象のグラフ
	 * @param s 始点
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static <T> FractionFieldElement<T> fixedStartWalkGeneratingFunction(ValueDigraph<T> g, int s, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = matrix.det(M);

		// num = e_s^T adj(M) 1 = det(M + 1 e_s^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		for (int i = 0; i < N; i++) {
			M2[i][s] = strategy.add(M2[i][s], strategy.one());
		}
		T detM2 = matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}

	/**
	 * 全ての頂点ペア間のウォークの重み和の合計の母関数を返します。
	 *
	 * @param g 対象のグラフ
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 */
	public static <T> FractionFieldElement<T> freeWalkGeneratingFunction(ValueDigraph<T> g, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = matrix.det(M);

		// num = 1^T adj(M) 1 = det(M + 1 1^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M2[i][j] = strategy.add(M2[i][j], strategy.one());
			}
		}
		T detM2 = matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}


	/**
	 * 始点 s から終点 t へのウォークの重み和の母関数を返します。
	 *
	 * @param g 対象の無向グラフ
	 * @param s 始点
	 * @param t 終点
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 * @complexity O(N^3)
	 */
	// 未テスト
	public static <T> FractionFieldElement<T> fixedWalkGeneratingFunction(ValueGraph<T> g, int s, int t, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		// M = I - A
		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = (strategy instanceof FieldStrategy) ? matrix.detGaussianSymmetric(M) : matrix.det(M);

		// num = adj(M)_{st} = det(M + e_t e_s^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		M2[t][s] = strategy.add(M2[t][s], strategy.one());
		T detM2 = (s == t && strategy instanceof FieldStrategy) ? matrix.detGaussianSymmetric(M2) : matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}

	/**
	 * 始点 s から任意の頂点へのウォークの重み和の母関数を返します。
	 *
	 * @param g 対象の無向グラフ
	 * @param s 始点
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 * @complexity O(N^3)
	 */
	// 未テスト
	public static <T> FractionFieldElement<T> fixedStartWalkGeneratingFunction(ValueGraph<T> g, int s, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = (strategy instanceof FieldStrategy) ? matrix.detGaussianSymmetric(M) : matrix.det(M);

		// num = e_s^T adj(M) 1 = det(M + 1 e_s^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		for (int i = 0; i < N; i++) {
			M2[i][s] = strategy.add(M2[i][s], strategy.one());
		}
		T detM2 = matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}

	/**
	 * 全ての頂点ペア間のウォークの重み和の合計の母関数を返します。
	 *
	 * @param g 対象の無向グラフ
	 * @param strategy 代数的構造の戦略
	 * @param <T> 重みの型
	 * @return 母関数の有理式表現 (分子/分母)
	 * @complexity O(N^3)
	 */
	// 未テスト
	public static <T> FractionFieldElement<T> freeWalkGeneratingFunction(ValueGraph<T> g, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = constructAdjacencyMatrix(g, strategy);
		Matrix<T> matrix = new Matrix<>(strategy);

		T[][] M = matrix.identity(N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M[i][j] = strategy.sub(M[i][j], A[i][j]);
			}
		}

		T den = (strategy instanceof FieldStrategy) ? matrix.detGaussianSymmetric(M) : matrix.det(M);

		// num = 1^T adj(M) 1 = det(M + 1 1^T) - det(M)
		T[][] M2 = copyMatrix(M, strategy);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M2[i][j] = strategy.add(M2[i][j], strategy.one());
			}
		}
		T detM2 = (strategy instanceof FieldStrategy) ? matrix.detGaussianSymmetric(M2) : matrix.det(M2);
		T num = strategy.sub(detM2, den);

		return simplify(num, den, strategy);
	}


	private static <T> T[][] constructAdjacencyMatrix(ValueDigraph<T> g, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = createArray(strategy, N, N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				A[i][j] = strategy.zero();
			}
		}
		for (int u = 0; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				A[u][e.dst()] = strategy.add(A[u][e.dst()], e.weight());
			}
		}
		return A;
	}

	private static <T> T[][] constructAdjacencyMatrix(ValueGraph<T> g, CommutativeRingStrategy<T> strategy) {
		int N = g.N;
		T[][] A = createArray(strategy, N, N);
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				A[i][j] = strategy.zero();
			}
		}
		for (int u = 0; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				A[u][e.dst()] = strategy.add(A[u][e.dst()], e.weight());
			}
		}
		return A;
	}

	private static <T> T[][] copyMatrix(T[][] data, CommutativeRingStrategy<T> strategy) {
		int n = data.length;
		int m = n == 0 ? 0 : data[0].length;
		T[][] copy = createArray(strategy, n, m);
		for (int i = 0; i < n; i++) {
			System.arraycopy(data[i], 0, copy[i], 0, m);
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[][] createArray(CommutativeRingStrategy<T> strategy, int r, int c) {
		return (T[][]) Array.newInstance(strategy.zero().getClass(), r, c);
	}

	private record PolynomialMatrix(long[][] A, long[][] B) {}

	private static PolynomialMatrix getPolynomialMatrix(long[][] A, long mod) {
		int N = A.length;
		// M(x) = I - xA = (-A)x + I
		long[][] A_poly = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (A[i][j] != 0) A_poly[i][j] = (mod - A[i][j]) % mod;
			}
		}
		long[][] B_poly = MatrixUtilsZ.longMatrixIdentity(N);
		return new PolynomialMatrix(A_poly, B_poly);
	}

	/**
	 * 各頂点 v に重み変数 x_v を対応させた、グラフ上の全ウォークの重み和の母関数を返します。
	 * 母関数の各項は、あるウォーク (v_1, v_2, ..., v_k) に対して (辺の重みの積) * x_{v_1} * x_{v_2} * ... * x_{v_k} を表します。
	 * 現在、頂点数 N <= 3 の場合にのみ対応しています。
	 *
	 * @param g 対象のグラフ (N <= 3)
	 * @param mod 法
	 * @return 母関数の有理式表現 (分子/分母)。係数は long[][][] で、[x_0の次数][x_1の次数][x_2の次数] に対応します。
	 */
	public static FractionFieldElement<long[][][]> multivariateFreeWalkGeneratingFunction(LongValueDigraph g, long mod) {
		return multivariateFreeWalkGeneratingFunctionInternal(g, new ZnStrategy(mod));
	}

	/**
	 * 各頂点 v に重み変数 x_v を対応させた、グラフ上の全ウォークの重み和の母関数を返します。
	 * 母関数の各項は、あるウォーク (v_1, v_2, ..., v_k) に対して (辺の重みの積) * x_{v_1} * x_{v_2} * ... * x_{v_k} を表します。
	 * 現在、頂点数 N <= 3 の場合にのみ対応しています。
	 *
	 * @param g 対象のグラフ (N <= 3)
	 * @return 母関数の有理式表現 (分子/分母)。係数は long[][][] で、[x_0の次数][x_1の次数][x_2の次数] に対応します。
	 */
	public static FractionFieldElement<long[][][]> multivariateFreeWalkGeneratingFunction(LongValueDigraph g) {
		return multivariateFreeWalkGeneratingFunctionInternal(g, new ZStrategy());
	}

	private static FractionFieldElement<long[][][]> multivariateFreeWalkGeneratingFunctionInternal(LongValueDigraph g, CommutativeRingStrategy<Long> baseStrategy) {
		int N = g.N;
		if (N > 3) {
			throw new UnsupportedOperationException("Multivariate generating function is only supported for N <= 3.");
		}
		long[][] W = constructAdjacencyMatrixInternal(g, baseStrategy);
		CommutativeRingStrategy<long[][][]> polyStrategy = PolynomialLong3D.strategy(baseStrategy);
		Matrix<long[][][]> matrix = new Matrix<>(polyStrategy);

		long[][][][] vars = new long[3][][][];
		vars[0] = new long[][][]{{{0}}, {{1}}};
		vars[1] = new long[][][]{{{0}, {1}}};
		vars[2] = new long[][][]{{{0, 1}}};

		long[][][][] x_vec = new long[N][][][];
		for (int i = 0; i < N; i++) x_vec[i] = vars[i];

		// M = I - WX
		// M_ij = delta_ij - W_ij * x_j
		long[][][][][] M = new long[N][N][][][];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				long[][][] term = polyStrategy.mul(new long[][][]{{{W[i][j]}}}, x_vec[j]);
				if (i == j) {
					M[i][j] = polyStrategy.sub(polyStrategy.one(), term);
				} else {
					M[i][j] = polyStrategy.neg(term);
				}
			}
		}

		// Matrix Determinant Lemma: u^T M^-1 v = (det(M + v u^T) - det(M)) / det(M)
		// Here u = x_vec, v = 1_vec (all ones)
		// M2 = M + v u^T => M2_ij = M_ij + 1 * x_j = M_ij + x_j
		long[][][][][] M2 = new long[N][N][][][];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				M2[i][j] = polyStrategy.add(M[i][j], x_vec[j]);
			}
		}

		long[][][] den = matrix.det(M);
		long[][][] detM2 = matrix.det(M2);
		long[][][] num = polyStrategy.sub(detM2, den);

		return simplify(num, den, polyStrategy);
	}

	@SuppressWarnings("unchecked")
	private static <T> FractionFieldElement<T> simplify(T num, T den, CommutativeRingStrategy<T> strategy) {
		if (strategy instanceof GCDDomainStrategy<?> gcdStrategy && strategy instanceof ExactDivRingStrategy<?> exactStrategy) {
			GCDDomainStrategy<T> gs = (GCDDomainStrategy<T>) gcdStrategy;
			ExactDivRingStrategy<T> es = (ExactDivRingStrategy<T>) exactStrategy;
			T g = gs.gcd(num, den);
			if (!strategy.equals(g, strategy.zero()) && !strategy.equals(g, strategy.one())) {
				num = es.exactDiv(num, g);
				den = es.exactDiv(den, g);
			}
		}
		if (strategy instanceof IntegralDomainStrategy) {
			return new FractionFieldElement<>(num, den, (IntegralDomainStrategy<T>) strategy);
		} else {
			return new FractionFieldElement<>(num, den, (FractionFieldStrategy<T>) null);
		}
	}

	private static FractionFieldElement<long[]> simplify(long[] num, long[] den, long mod) {
		return simplify(num, den, PolynomialFpDynamic.of(mod));
	}

	private static long[][] constructAdjacencyMatrixInternal(LongValueDigraph g, CommutativeRingStrategy<Long> strategy) {
		int N = g.N;
		long[][] A = new long[N][N];
		for (int u = 0; u < N; u++) {
			for (Edge e : g.adj[u]) {
				A[u][e.dst] = strategy.add(A[u][e.dst], e.cost);
			}
		}
		return A;
	}

	/**
	 * ステップ x における分数表現の遷移行列 M_x を表すヘルパークラスです。
	 * 遷移行列 M_x は、分子行列 A_x (サイズ: dim x dim、変数 `A[x]`) と、
	 * 共通の分母となるスカラー d_x = 1 - selfLoopSum[x+1] (変数 `d[x]`) を用いて、以下のように定義されます：
	 *
	 *     M_x = A_x / d_x
	 *
	 * @param <T> 重みの型
	*/
	private static class FractionalMatrix<T> {
		/**
		 * 分子となる行列 A (サイズ: dim x dim)。各要素は基底環 T の元です。
		 */
		T[][] A;
		/**
		 * 共通の分母となるスカラー d。基底環 T の元です。
		 */
		T d;

		/**
		 * 指定された分子行列と共通分母を持つ FractionalMatrix を構築します。
		 *
		 * @param A 分子行列
		 * @param d 共通分母
		 */
		FractionalMatrix(T[][] A, T d) {
			this.A = A;
			this.d = d;
		}
	}

	/**
	 * 頂点 0, 1, ..., n-1 が順に並び、自己ループおよび各頂点から 0 への戻り辺（バックエッジ）を許容する有向グラフにおいて、
	 * 頂点 0 から頂点 n-1 へのパスの重み和を、商体（FractionFieldElement）における
	 * ステップごとの遷移行列の積を用いて計算して返します。
	 *
	 * <ul>
	 *   <li>事前条件: すべての辺 a_i -> b_i について、b_i == 0 または a_i <= b_i が成り立つこと。</li>
	 *   <li>事前条件: strategy が IntegralDomainStrategy であること。</li>
	 *   <li>事後条件: なし。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N K^3)$。ここで $K = \max_{(u, v) \in E, v \neq 0, u \neq v} (v - u)$ （辺がない場合は $1$）。</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 *
	 * @param g 対象の有向グラフ (自己ループおよび 0 へのバックエッジを含む)
	 * @param strategy 代数的構造の戦略 (CommutativeRingStrategy)
	 * @param <T> 重みの型
	 * @return 頂点 0 から頂点 n-1 へのパスの重みの和（FractionFieldElement）
	 */
	public static <T> FractionFieldElement<T> pathWeightSumSequential(ValueDigraph<T> g, CommutativeRingStrategy<T> strategy) {
		//https://atcoder.jp/contests/abc317/submissions/77712405
		int N = g.N;
		if (!(strategy instanceof IntegralDomainStrategy)) {
			throw new UnsupportedOperationException("Strategy must be an IntegralDomainStrategy to calculate in the fraction field");
		}
		IntegralDomainStrategy<T> domainStrategy = (IntegralDomainStrategy<T>) strategy;
		FractionFieldStrategy<T> fracStrategy = new FractionFieldStrategy<>(domainStrategy);

		if (g.rawDiraph().bfsDistances(0)[N-1] >= N) {
			return fracStrategy.zero();
		}
		if (N <= 0) {
			return fracStrategy.zero();
		}

		// K = max_{(u, v) in E, v > 0, u != v} (v - u)
		int K = 1;
		for (int u = 0; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				if (e.dst() > 0 && e.dst() != u) {
					K = Math.max(K, e.dst() - u);
				}
			}
		}

		// Calculate sum of self loops for each vertex
		@SuppressWarnings("unchecked")
		T[] selfLoopSum = (T[]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N);
		for (int i = 0; i < N; i++) {
			selfLoopSum[i] = strategy.zero();
		}
		for (int u = 0; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				if (e.dst() == u) {
					selfLoopSum[u] = strategy.add(selfLoopSum[u], e.weight());
				}
			}
		}

		// F_x = (1 - selfLoopSum[x])^-1 in the fraction field
		@SuppressWarnings("unchecked")
		FractionFieldElement<T>[] F = (FractionFieldElement<T>[]) java.lang.reflect.Array.newInstance(FractionFieldElement.class, N);
		for (int x = 0; x < N; x++) {
			FractionFieldElement<T> oneMinusW = fracStrategy.of(strategy.sub(strategy.one(), selfLoopSum[x]), strategy.one());
			F[x] = fracStrategy.inv(oneMinusW);
		}

		if (N == 1) {
			return F[0];
		}

		// Sum of back-edges to 0 from each vertex u (where u > 0)
		@SuppressWarnings("unchecked")
		T[] backTo0Sum = (T[]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N);
		for (int i = 0; i < N; i++) {
			backTo0Sum[i] = strategy.zero();
		}
		for (int u = 1; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				if (e.dst() == 0) {
					backTo0Sum[u] = strategy.add(backTo0Sum[u], e.weight());
				}
			}
		}

		int dim = K + 1;

		// Construct transition matrices A_x of size dim * dim for x = 0 to N-2
		@SuppressWarnings("unchecked")
		T[][][] A = (T[][][]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N - 1, dim, dim);
		@SuppressWarnings("unchecked")
		T[] d = (T[]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N - 1);
		for (int x = 0; x < N - 1; x++) {
			d[x] = strategy.sub(strategy.one(), selfLoopSum[x + 1]);
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					A[x][i][j] = strategy.zero();
				}
			}
			// subdiagonal 1s for DAG states
			for (int i = 1; i < K; i++) {
				A[x][i][i - 1] = d[x];
			}
			// self-transition 1 for back-edge running sum
			A[x][K][K] = d[x];
		}

		//
		//       [ DP[x]      ]
		//       [ DP[x-1]    ]
		//V[x] = [ ...        ]
		//       [ DP[x-K+1]  ]
		//       [ C[x]       ]
		//
		// DP[i] は、頂点 0 の自己ループおよびすべての 0 へのバックエッジを通らないときの
		// 頂点 0 から頂点 i へのwalkの重み和。
		//　DP[v]=F[v] ∑_{uv in E} ​DP[u]w(uv)
		//
		// C = Σ_{u=0}^{x} DP[u] backTo0Sum[u]
		//
		// V[x+1] = M[x] V[x]


		// DP[dst]=F[dst] ∑_{uv in E} ​DP[u]w(u(dst))
		// の　​DP[u]w(u(dst)) を足す

		// Temporary array to store S_{x, j}
		@SuppressWarnings("unchecked")
		T[][] S = (T[][]) java.lang.reflect.Array.newInstance(strategy.zero().getClass(), N - 1, K);
		for (int x = 0; x < N - 1; x++) {
			for (int j = 0; j < K; j++) {
				S[x][j] = strategy.zero();
			}
		}

		for (int u = 0; u < N; u++) {
			for (ValueEdge<T> e : g.adj[u]) {
				int dst = e.dst();
				if (dst == u || dst == 0) continue; // 自己辺と0へのバックエッジを無視
				int x = dst - 1;
				if (x >= 0 && x < N - 1) {
					int j = dst - 1 - u;
					if (j >= 0 && j < K) {
						S[x][j] = strategy.add(S[x][j], e.weight());
					}
				}
			}
		}

		for (int x = 0; x < N - 1; x++) {
			for (int j = 0; j < K; j++) {
				A[x][0][j] = S[x][j];
				A[x][K][j] = strategy.mul(backTo0Sum[x + 1], S[x][j]);
			}
		}

		Matrix<T> matrix = new Matrix<>(strategy);
		java.util.Queue<FractionalMatrix<T>> que = new java.util.ArrayDeque<>();
		for (int x = 0; x < N - 1; x++) {
			que.add(new FractionalMatrix<>(A[x], d[x]));
		}

		// Repeatedly combine adjacent matrices pairwise using a Queue
		while (que.size() > 1) {
			int sz = que.size();
			for (int i = 0; i < sz; i += 2) {
				if (i + 1 < sz) {
					var a = que.poll();
					var b = que.poll();
					T[][] productA = matrix.mul(b.A, a.A);
					T productD = strategy.mul(b.d, a.d);
					que.add(new FractionalMatrix<>(productA, productD));
				} else {
					var a = que.poll();
					que.add(a);
				}
			}
		}

		FractionalMatrix<T> finalMatrix = que.poll();

		// V[0][0] is DP_DAG_unit_start[N-1]
		// V[K][0] is C_unit_start
		T N_DAG = finalMatrix.A[0][0];
		T N_C = finalMatrix.A[K][0];

		T d0 = strategy.sub(strategy.one(), selfLoopSum[0]);
		T d_final = finalMatrix.d;

		return fracStrategy.of(N_DAG, strategy.sub(strategy.mul(d0, d_final), N_C));
	}

	/**
	 * 始点 s から終点 t への長さ n のウォーク全体について、乗法重み(行列 A)の積と、
	 * ウォーク中の各辺に対応する加法コスト(行列 C)の総和との積を、全ウォークにわたって
	 * 足し合わせた値を n 次係数とする母関数を、有理式(分子/分母)として返します。
	 *
	 * <p>A[i][j] を辺 i→j の乗法重み w(e) の総和、C[i][j] を辺 i→j の w(e)*c(e) の総和とすると、
	 * n 次係数は Σ_{W: s→t, |W|=n} ( Π_{e∈W} w(e) ) × ( Σ_{e∈W} c(e) ) に等しくなります。</p>
	 *
	 * <p>特性多項式による漸化式で (I - xA)^{-1} 相当の多項式列を構成し、コスト行列 C を
	 * ウォーク中の1辺にだけ差し込んだ畳み込みの総和を分子、det(I - xA) の2乗を分母として
	 * 有理式を計算します。</p>
	 *
	 * 計算量: O(N^3)
	 *
	 * @param A   遷移行列 (A[i][j] は辺 i→j の乗法重み w(e) の総和)
	 * @param C   コスト付き遷移行列 (C[i][j] は辺 i→j の w(e)*c(e) の総和)
	 * @param s   始点のインデックス
	 * @param t   終点のインデックス
	 * @param mod 法とする素数
	 * @return 母関数を表す有理式 (分子/分母) の FractionFieldElement
	 * @throws IllegalArgumentException  A, C が正方行列でない、またはサイズが一致しない場合
	 * @throws IndexOutOfBoundsException s または t が [0, N) の範囲外の場合
	 */
	public static FractionFieldElement<long[]> walkGeneratingFunctionWithCost(long[][] A, long[][] C, int s, int t, long mod) {
		//  x(I-xA)^{-1}C(I-xA)^{-1}
		// =x adj(I-xA)C adj(I-xA)/det(I-xA)^2
		
		int N = A.length;
		if (N == 0) {
			return simplify(new long[0], new long[]{1}, mod);
		}
		if (A[0].length != N || C.length != N || C[0].length != N) {
			throw new IllegalArgumentException("Matrices must be square of the same size.");
		}
		if (s < 0 || s >= N || t < 0 || t >= N) {
			throw new IndexOutOfBoundsException("Source or target index out of bounds.");
		}

		long[] e_s = new long[N];
		e_s[s] = 1;
		long[][] AT = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				AT[i][j] = A[j][i];
			}
		}
		MatrixUtilsFp.RationalVectorResult resL = MatrixUtilsFp.inverseIminusAxOnFp(AT, e_s, mod);

		long[] e_t = new long[N];
		e_t[t] = 1;
		MatrixUtilsFp.RationalVectorResult resR = MatrixUtilsFp.inverseIminusAxOnFp(A, e_t, mod);

		long[][] T = new long[N][N];
		for (int k = 0; k < N; k++) {
			for (int i = 0; i < N; i++) {
				long sum = 0;
				for (int j = 0; j < N; j++) {
					sum = (sum + C[i][j] * resR.numerators[j][k]) % mod;
				}
				T[k][i] = sum;
			}
		}

		long[][] Lx = new long[N][N];
		long[][] Tx = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int k = 0; k < N; k++) {
				Lx[i][k] = resL.numerators[i][k];
				Tx[i][k] = T[k][i];
			}
		}

		PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
		long[] P = new long[0];
		for (int i = 0; i < N; i++) {
			long[] term = poly.mul(poly.resize(Lx[i]), poly.resize(Tx[i]));
			P = poly.add(P, term);
		}

		long[] num = poly.multiplyByX(P, 1);
		long[] den = poly.squared(resR.denominator);

		return simplify(num, den, mod);
	}

	/**
	 * 各辺 e が乗法重み {@code w(e)} と加法コスト {@code c(e)} を持つ有向グラフについて、
	 * 全点対（すべての始点 s と終点 t の組）に対する
	 *
	 * <pre>
	 * Σ_W w(W) c(W)
	 * </pre>
	 *
	 * を計算します。ただし、総和は s から t へのすべてのウォーク W にわたり、
	 *
	 * <pre>
	 * w(W) = ∏_{e∈W} w(e)
	 * c(W) = Σ_{e∈W} c(e)
	 * </pre>
	 *
	 * とします。
	 *
	 * <p>遷移行列 {@code A} およびコスト付き遷移行列 {@code C} を
	 *
	 * <pre>
	 * A[i][j] = Σ w(e)
	 * C[i][j] = Σ w(e)c(e)
	 * </pre>
	 *
	 * （総和はいずれも i→j の辺 e にわたる）とすると、本メソッドは
	 *
	 * <pre>
	 * (I - A)^(-1) C (I - A)^(-1)
	 * </pre>
	 *
	 * を計算します。
	 *
	 * <ul>
	 *   <li>事前条件: {@code I - A} が {@code mod} 上で正則である。</li>
	 *   <li>計算量: O(N^3)</li>
	 * </ul>
	 *
	 * @param A 遷移行列 ({@code A[i][j] = Σ w(e)})
	 * @param C コスト付き遷移行列 ({@code C[i][j] = Σ w(e)c(e)})
	 * @param mod 法（素数）
	 * @return 全点対間の重み付きコスト総和
	 * @throws ArithmeticException {@code I - A} が {@code mod} 上で正則でない場合
	 * @throws IllegalArgumentException 行列が同じサイズの正方行列でない場合
	 */
	// 未テスト
	public static long[][] walkWeightSumWithCost(long[][] A, long[][] C, long mod) {
		int N = A.length;
		if (N == 0) {
			return new long[0][0];
		}
		if (A[0].length != N || C.length != N || C[0].length != N) {
			throw new IllegalArgumentException("Matrices must be square of the same size.");
		}

		long[][] I = MatrixUtilsZ.longMatrixIdentity(N);
		long[][] I_minus_A = new long[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				long a_val = (A[i][j] % mod + mod) % mod;
				I_minus_A[i][j] = (I[i][j] - a_val + mod) % mod;
			}
		}

		long[][] X = MatrixUtilsFp.inv(I_minus_A, mod);
		if (X == null) {
			throw new ArithmeticException("Division by zero: denominator evaluates to 0.");
		}
		// X = (I - A)^{-1}
		// return X * C * X
		return MatrixUtilsFp.mul(MatrixUtilsFp.mul(X, C, mod), X, mod);
	}

}
