package library.util.polynomial;

import library.util.algebra.strategy.CommutativeRingStrategy;

/**
 * 2変数の多項式（係数はlong）
 */
public class PolynomialLong2D {

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] zero() {
		return new long[0][0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] one() {
		return new long[][] { { 1 } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] x() {
		return new long[][] { { 0 }, { 1 } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] y() {
		return new long[][] { { 0, 1 } };
	}

	/**
	 * 多項式の加算 a + b を行う。
	 * 計算量: O(NM) (N, M は最大次数)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a + b
	 */
	public static long[][] add(long[][] a, long[][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		for (int i = 0; i < a.length; i++) m = Math.max(m, a[i].length);
		for (int i = 0; i < b.length; i++) m = Math.max(m, b[i].length);
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				long valA = (i < a.length && j < a[i].length) ? a[i][j] : 0;
				long valB = (i < b.length && j < b[i].length) ? b[i][j] : 0;
				c[i][j] = valA + valB;
			}
		}
		return c;
	}

	/**
	 * 多項式の減算 a - b を行う。
	 * 計算量: O(NM) (N, M は最大次数)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a - b
	 */
	public static long[][] subtract(long[][] a, long[][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		for (int i = 0; i < a.length; i++) m = Math.max(m, a[i].length);
		for (int i = 0; i < b.length; i++) m = Math.max(m, b[i].length);
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				long valA = (i < a.length && j < a[i].length) ? a[i][j] : 0;
				long valB = (i < b.length && j < b[i].length) ? b[i][j] : 0;
				c[i][j] = valA - valB;
			}
		}
		return c;
	}

	/**
	 * 多項式の乗算 a * b を行う（ナイーブな実装）。
	 * 計算量: O(N1 * M1 * N2 * M2)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a * b
	 */
	public static long[][] mulNaive(long[][] a, long[][] b) {
		if (a.length == 0 || b.length == 0) return new long[0][0];
		int m0 = 0;
		for (int i = 0; i < a.length; i++) m0 = Math.max(m0, a[i].length);
		int m1 = 0;
		for (int i = 0; i < b.length; i++) m1 = Math.max(m1, b[i].length);
		if (m0 == 0 || m1 == 0) return new long[0][0];

		long[][] c = new long[a.length + b.length - 1][m0 + m1 - 1];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] == 0) continue;
				for (int k = 0; k < b.length; k++) {
					for (int l = 0; l < b[k].length; l++) {
						c[i + k][j + l] += a[i][j] * b[k][l];
					}
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の乗算 a * b を行う。
	 * 計算量: O(N1 * M1 * N2 * M2)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a * b
	 */
	public static long[][] mul(long[][] a, long[][] b) {
		return mulNaive(a, b);
	}

	public static CommutativeRingStrategy<long[][]> strategy(CommutativeRingStrategy<Long> strategy) {
		if (strategy instanceof library.util.algebra.strategy.IntegralDomainStrategy) {
			return new PolynomialLong2DIntegralDomainStrategy(strategy);
		}
		return new PolynomialLong2DRingStrategy(strategy);
	}

	public static class PolynomialLong2DRingStrategy implements CommutativeRingStrategy<long[][]> {
		protected final CommutativeRingStrategy<Long> strategy;

		public PolynomialLong2DRingStrategy(CommutativeRingStrategy<Long> strategy) {
			this.strategy = strategy;
		}

		@Override public long[][] zero() { return new long[0][0]; }
		@Override public long[][] one() { return new long[][]{{strategy.one()}}; }
		@Override public long[][] add(long[][] a, long[][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0;
			for (int i = 0; i < a.length; i++) m = Math.max(m, a[i].length);
			for (int i = 0; i < b.length; i++) m = Math.max(m, b[i].length);
			long[][] c = new long[n][m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					long valA = (i < a.length && j < a[i].length) ? a[i][j] : strategy.zero();
					long valB = (i < b.length && j < b[i].length) ? b[i][j] : strategy.zero();
					c[i][j] = strategy.add(valA, valB);
				}
			}
			return c;
		}
		@Override public long[][] mul(long[][] a, long[][] b) {
			if (a.length == 0 || b.length == 0) return zero();
			int m0 = 0;
			for (int i = 0; i < a.length; i++) m0 = Math.max(m0, a[i].length);
			int m1 = 0;
			for (int i = 0; i < b.length; i++) m1 = Math.max(m1, b[i].length);
			if (m0 == 0 || m1 == 0) return zero();
			long[][] c = new long[a.length + b.length - 1][m0 + m1 - 1];
			for (int i = 0; i < a.length; i++) {
				for (int j = 0; j < a[i].length; j++) {
					if (strategy.equals(a[i][j], strategy.zero())) continue;
					for (int k = 0; k < b.length; k++) {
						for (int l = 0; l < b[k].length; l++) {
							c[i + k][j + l] = strategy.add(c[i + k][j + l], strategy.mul(a[i][j], b[k][l]));
						}
					}
				}
			}
			return c;
		}
		@Override public long[][] neg(long[][] a) {
			int n = a.length;
			if (n == 0) return zero();
			int m = 0;
			for (int i = 0; i < n; i++) m = Math.max(m, a[i].length);
			long[][] c = new long[n][m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < a[i].length; j++) c[i][j] = strategy.neg(a[i][j]);
			}
			return c;
		}
		@Override public boolean equals(long[][] a, long[][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0;
			for (int i = 0; i < a.length; i++) m = Math.max(m, a[i].length);
			for (int i = 0; i < b.length; i++) m = Math.max(m, b[i].length);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					long va = (i < a.length && j < a[i].length) ? a[i][j] : strategy.zero();
					long vb = (i < b.length && j < b[i].length) ? b[i][j] : strategy.zero();
					if (!strategy.equals(va, vb)) return false;
				}
			}
			return true;
		}
	}

	public static class PolynomialLong2DIntegralDomainStrategy extends PolynomialLong2DRingStrategy implements library.util.algebra.strategy.IntegralDomainStrategy<long[][]> {
		public PolynomialLong2DIntegralDomainStrategy(CommutativeRingStrategy<Long> strategy) {
			super(strategy);
		}
	}

	public static void printPolyAsExpr(String label, long[][] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x", "y" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				long coeff = arr[i][j];
				if (coeff == 0) continue;

				if (!isFirst && coeff > 0) {
					sb.append(" + ");
				} else if (!isFirst && coeff < 0) {
					sb.append(" - ");
					coeff = -coeff;
				} else if (isFirst && coeff < 0) {
					sb.append("-");
					coeff = -coeff;
				}

				StringBuilder varPart = new StringBuilder();
				int[] powers = { i, j };
				for (int v = 0; v < 2; v++) {
					if (powers[v] > 0) {
						varPart.append(vars[v]);
						if (powers[v] > 1) {
							varPart.append("^").append(powers[v]);
						}
						varPart.append(" ");
					}
				}

				if (varPart.length() == 0) {
					sb.append(coeff);
				} else {
					if (coeff != 1) {
						sb.append(coeff).append("*");
					}
					sb.append(varPart.toString().trim());
				}

				isFirst = false;
			}
		}

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}

	public record Factor(long[][] factor, int multiplicity) {}
	public record FactorResult(long leadingCoeff, Factor[] factors) {}

	private static boolean isZero(long[][] a) {
		if (a == null || a.length == 0) return true;
		for (long[] row : a) {
			if (row != null) {
				for (long x : row) {
					if (x != 0) return false;
				}
			}
		}
		return true;
	}

	private static cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> toRings2D(long[][] a) {
		java.util.List<cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger>> terms = new java.util.ArrayList<>();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) continue;
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] != 0) {
					terms.add(new cc.redberry.rings.poly.multivar.Monomial<>(new int[]{i, j}, cc.redberry.rings.bigint.BigInteger.valueOf(a[i][j])));
				}
			}
		}
		return cc.redberry.rings.poly.multivar.MultivariatePolynomial.create(2, cc.redberry.rings.Rings.Z, cc.redberry.rings.poly.multivar.MonomialOrder.LEX, terms);
	}

	private static long[][] fromRings2D(cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> poly) {
		if (poly.isZero()) {
			return new long[0][0];
		}
		int maxDegX = 0;
		int maxDegY = 0;
		for (cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger> m : poly) {
			maxDegX = Math.max(maxDegX, m.exponents[0]);
			maxDegY = Math.max(maxDegY, m.exponents[1]);
		}
		long[][] res = new long[maxDegX + 1][maxDegY + 1];
		for (cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger> m : poly) {
			res[m.exponents[0]][m.exponents[1]] = m.coefficient.longValue();
		}
		return res;
	}

	/**
	 * 未テスト
	 * 多項式を整数環 Z 上で因数分解する。
	 * cc.redberry.rings の既存ライブラリを用いて、2変数多項式の既約因数分解を計算する。
	 * 計算量: 入力多項式の次数に関して多項式時間（cc.redberry.rings の計算量に準じる）。
	 *
	 * @param inputf 因数分解する2変数多項式の係数配列。inputf[i][j] は x^i * y^j の係数を表す。
	 * @return 因数分解結果を格納した FactorResult
	 * @throws IllegalArgumentException 入力がゼロ多項式、または null の場合
	 */
	public static FactorResult factor(long[][] inputf) {
		if (inputf == null || isZero(inputf)) {
			throw new IllegalArgumentException("Cannot factorize zero polynomial");
		}
		cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> poly = toRings2D(inputf);

		cc.redberry.rings.poly.PolynomialFactorDecomposition<cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger>> decomposed =
			cc.redberry.rings.poly.multivar.MultivariateFactorization.Factor(poly);

		long leadingCoeff = ((cc.redberry.rings.bigint.BigInteger) decomposed.unit.cc()).longValue();

		int size = decomposed.size();
		Factor[] factors = new Factor[size];
		for (int i = 0; i < size; i++) {
			factors[i] = new Factor(fromRings2D(decomposed.get(i)), decomposed.getExponent(i));
		}

		return new FactorResult(leadingCoeff, factors);
	}
}
