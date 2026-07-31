package library.util.polynomial;

import library.util.algebra.strategy.CommutativeRingStrategy;

/**
 * 3変数の多項式（係数はlong）
 */
public class PolynomialLong3D {

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][] zero() {
		return new long[0][0][0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][] one() {
		return new long[][][] { { { 1 } } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][] x() {
		long[][][] ret=new long[2][1][1];
		ret[1][0][0]=1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][] y() {
		long[][][] ret=new long[1][2][1];
		ret[0][1][0]=1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][] z() {
		long[][][] ret=new long[1][1][2];
		ret[0][0][1]=1;
		return ret;
	}
	
	/**
	 * 多項式の加算 a + b を行う。
	 * 計算量: O(NML) (N, M, L は各変数の最大次数)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a + b
	 */
	public static long[][][] add(long[][][] a, long[][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		int l = 0;
		for (int i = 0; i < a.length; i++) {
			m = Math.max(m, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				l = Math.max(l, a[i][j].length);
			}
		}
		for (int i = 0; i < b.length; i++) {
			m = Math.max(m, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				l = Math.max(l, b[i][j].length);
			}
		}
		long[][][] c = new long[n][m][l];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int k = 0; k < l; k++) {
					long valA = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					long valB = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : 0;
					c[i][j][k] = valA + valB;
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の減算 a - b を行う。
	 * 計算量: O(NML) (N, M, L は各変数の最大次数)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a - b
	 */
	public static long[][][] subtract(long[][][] a, long[][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		int l = 0;
		for (int i = 0; i < a.length; i++) {
			m = Math.max(m, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				l = Math.max(l, a[i][j].length);
			}
		}
		for (int i = 0; i < b.length; i++) {
			m = Math.max(m, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				l = Math.max(l, b[i][j].length);
			}
		}
		long[][][] c = new long[n][m][l];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int k = 0; k < l; k++) {
					long valA = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : 0;
					long valB = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : 0;
					c[i][j][k] = valA - valB;
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の乗算 a * b を行う（ナイーブな実装）。
	 * 計算量: O(N1*M1*L1 * N2*M2*L2)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a * b
	 */
	public static long[][][] mulNaive(long[][][] a, long[][][] b) {
		if (a.length == 0 || b.length == 0) return new long[0][0][0];
		int ma = 0, la = 0;
		for (int i = 0; i < a.length; i++) {
			ma = Math.max(ma, a[i].length);
			for (int j = 0; j < a[i].length; j++) la = Math.max(la, a[i][j].length);
		}
		int mb = 0, lb = 0;
		for (int i = 0; i < b.length; i++) {
			mb = Math.max(mb, b[i].length);
			for (int j = 0; j < b[i].length; j++) lb = Math.max(lb, b[i][j].length);
		}
		if (ma == 0 || la == 0 || mb == 0 || lb == 0) return new long[0][0][0];

		long[][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1];
		for (int i1 = 0; i1 < a.length; i1++) {
			for (int j1 = 0; j1 < a[i1].length; j1++) {
				for (int k1 = 0; k1 < a[i1][j1].length; k1++) {
					if (a[i1][j1][k1] == 0) continue;
					for (int i2 = 0; i2 < b.length; i2++) {
						for (int j2 = 0; j2 < b[i2].length; j2++) {
							for (int k2 = 0; k2 < b[i2][j2].length; k2++) {
								c[i1 + i2][j1 + j2][k1 + k2] += a[i1][j1][k1] * b[i2][j2][k2];
							}
						}
					}
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の乗算 a * b を行う。
	 * 計算量: O(N1*M1*L1 * N2*M2*L2)
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a * b
	 */
	public static long[][][] mul(long[][][] a, long[][][] b) {
		return mulNaive(a, b);
	}

	public static CommutativeRingStrategy<long[][][]> strategy(CommutativeRingStrategy<Long> strategy) {
		if (strategy instanceof library.util.algebra.strategy.IntegralDomainStrategy) {
			return new PolynomialLong3DIntegralDomainStrategy(strategy);
		}
		return new PolynomialLong3DRingStrategy(strategy);
	}

	public static class PolynomialLong3DRingStrategy implements CommutativeRingStrategy<long[][][]> {
		protected final CommutativeRingStrategy<Long> strategy;

		public PolynomialLong3DRingStrategy(CommutativeRingStrategy<Long> strategy) {
			this.strategy = strategy;
		}

		@Override public long[][][] zero() { return new long[0][0][0]; }
		@Override public long[][][] one() { return new long[][][]{{{strategy.one()}}}; }
		@Override public long[][][] add(long[][][] a, long[][][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0;
			int l = 0;
			for (int i = 0; i < a.length; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) l = Math.max(l, a[i][j].length);
			}
			for (int i = 0; i < b.length; i++) {
				m = Math.max(m, b[i].length);
				for (int j = 0; j < b[i].length; j++) l = Math.max(l, b[i][j].length);
			}
			long[][][] c = new long[n][m][l];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					for (int k = 0; k < l; k++) {
						long valA = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : strategy.zero();
						long valB = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : strategy.zero();
						c[i][j][k] = strategy.add(valA, valB);
					}
				}
			}
			return c;
		}
		@Override public long[][][] mul(long[][][] a, long[][][] b) {
			if (a.length == 0 || b.length == 0) return zero();
			int ma = 0, la = 0;
			for (int i = 0; i < a.length; i++) {
				ma = Math.max(ma, a[i].length);
				for (int j = 0; j < a[i].length; j++) la = Math.max(la, a[i][j].length);
			}
			int mb = 0, lb = 0;
			for (int i = 0; i < b.length; i++) {
				mb = Math.max(mb, b[i].length);
				for (int j = 0; j < b[i].length; j++) lb = Math.max(lb, b[i][j].length);
			}
			if (ma == 0 || la == 0 || mb == 0 || lb == 0) return zero();
			long[][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1];
			for (int i1 = 0; i1 < a.length; i1++) {
				for (int j1 = 0; j1 < a[i1].length; j1++) {
					for (int k1 = 0; k1 < a[i1][j1].length; k1++) {
						if (strategy.equals(a[i1][j1][k1], strategy.zero())) continue;
						for (int i2 = 0; i2 < b.length; i2++) {
							for (int j2 = 0; j2 < b[i2].length; j2++) {
								for (int k2 = 0; k2 < b[i2][j2].length; k2++) {
									c[i1 + i2][j1 + j2][k1 + k2] = strategy.add(c[i1 + i2][j1 + j2][k1 + k2], strategy.mul(a[i1][j1][k1], b[i2][j2][k2]));
								}
							}
						}
					}
				}
			}
			return c;
		}
		@Override public long[][][] neg(long[][][] a) {
			int n = a.length;
			if (n == 0) return zero();
			int m = 0, l = 0;
			for (int i = 0; i < n; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) l = Math.max(l, a[i][j].length);
			}
			long[][][] c = new long[n][m][l];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < a[i].length; j++) {
					for (int k = 0; k < a[i][j].length; k++) c[i][j][k] = strategy.neg(a[i][j][k]);
				}
			}
			return c;
		}
		@Override public boolean equals(long[][][] a, long[][][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0, l = 0;
			for (int i = 0; i < a.length; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) l = Math.max(l, a[i][j].length);
			}
			for (int i = 0; i < b.length; i++) {
				m = Math.max(m, b[i].length);
				for (int j = 0; j < b[i].length; j++) l = Math.max(l, b[i][j].length);
			}
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					for (int k = 0; k < l; k++) {
						long va = (i < a.length && j < a[i].length && k < a[i][j].length) ? a[i][j][k] : strategy.zero();
						long vb = (i < b.length && j < b[i].length && k < b[i][j].length) ? b[i][j][k] : strategy.zero();
						if (!strategy.equals(va, vb)) return false;
					}
				}
			}
			return true;
		}
	}

	public static class PolynomialLong3DIntegralDomainStrategy extends PolynomialLong3DRingStrategy implements library.util.algebra.strategy.IntegralDomainStrategy<long[][][]> {
		public PolynomialLong3DIntegralDomainStrategy(CommutativeRingStrategy<Long> strategy) {
			super(strategy);
		}
	}

	public static void printPolyAsExpr(String label, long[][][] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x", "y", "z" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				for (int k = 0; k < arr[i][j].length; k++) {
					long coeff = arr[i][j][k];
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
					int[] powers = { i, j, k };
					for (int v = 0; v < 3; v++) {
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
		}

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}

	public record Factor(long[][][] factor, int multiplicity) {}
	public record FactorResult(long leadingCoeff, Factor[] factors) {}

	private static boolean isZero(long[][][] a) {
		if (a == null || a.length == 0) return true;
		for (long[][] matrix : a) {
			if (matrix != null) {
				for (long[] row : matrix) {
					if (row != null) {
						for (long x : row) {
							if (x != 0) return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> toRings3D(long[][][] a) {
		java.util.List<cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger>> terms = new java.util.ArrayList<>();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) continue;
			for (int j = 0; j < a[i].length; j++) {
				if (a[i][j] == null) continue;
				for (int k = 0; k < a[i][j].length; k++) {
					if (a[i][j][k] != 0) {
						terms.add(new cc.redberry.rings.poly.multivar.Monomial<>(new int[]{i, j, k}, cc.redberry.rings.bigint.BigInteger.valueOf(a[i][j][k])));
					}
				}
			}
		}
		return cc.redberry.rings.poly.multivar.MultivariatePolynomial.create(3, cc.redberry.rings.Rings.Z, cc.redberry.rings.poly.multivar.MonomialOrder.LEX, terms);
	}

	private static long[][][] fromRings3D(cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> poly) {
		if (poly.isZero()) {
			return new long[0][0][0];
		}
		int maxDegX = 0;
		int maxDegY = 0;
		int maxDegZ = 0;
		for (cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger> m : poly) {
			maxDegX = Math.max(maxDegX, m.exponents[0]);
			maxDegY = Math.max(maxDegY, m.exponents[1]);
			maxDegZ = Math.max(maxDegZ, m.exponents[2]);
		}
		long[][][] res = new long[maxDegX + 1][maxDegY + 1][maxDegZ + 1];
		for (cc.redberry.rings.poly.multivar.Monomial<cc.redberry.rings.bigint.BigInteger> m : poly) {
			res[m.exponents[0]][m.exponents[1]][m.exponents[2]] = m.coefficient.longValue();
		}
		return res;
	}

	/**
	 * 未テスト
	 * 多項式を整数環 Z 上で因数分解する。
	 * cc.redberry.rings の既存ライブラリを用いて、3変数多項式の既約因数分解を計算する。
	 * 計算量: 入力多項式の次数に関して多項式時間（cc.redberry.rings の計算量に準じる）。
	 *
	 * @param inputf 因数分解する3変数多項式の係数配列。inputf[i][j][k] は x^i * y^j * z^k の係数を表す。
	 * @return 因数分解結果を格納した FactorResult
	 * @throws IllegalArgumentException 入力がゼロ多項式、または null の場合
	 */
	public static FactorResult factor(long[][][] inputf) {
		if (inputf == null || isZero(inputf)) {
			throw new IllegalArgumentException("Cannot factorize zero polynomial");
		}
		cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger> poly = toRings3D(inputf);

		cc.redberry.rings.poly.PolynomialFactorDecomposition<cc.redberry.rings.poly.multivar.MultivariatePolynomial<cc.redberry.rings.bigint.BigInteger>> decomposed =
			cc.redberry.rings.poly.multivar.MultivariateFactorization.Factor(poly);

		long leadingCoeff = ((cc.redberry.rings.bigint.BigInteger) decomposed.unit.cc()).longValue();

		int size = decomposed.size();
		Factor[] factors = new Factor[size];
		for (int i = 0; i < size; i++) {
			factors[i] = new Factor(fromRings3D(decomposed.get(i)), decomposed.getExponent(i));
		}

		return new FactorResult(leadingCoeff, factors);
	}
}
