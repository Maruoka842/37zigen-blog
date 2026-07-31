package library.util.polynomial;

import library.util.algebra.strategy.CommutativeRingStrategy;

/**
 * 4変数の多項式（係数はlong）
 */
public class PolynomialLong4D {

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] zero() {
		return new long[0][0][0][0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] one() {
		return new long[][][][]{{{{1}}}};
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] x() {
		long[][][][] ret=new long[2][1][1][1];
		ret[1][0][0][0]=1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] y() {
		long[][][][] ret=new long[1][2][1][1];
		ret[0][1][0][0]=1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] z() {
		long[][][][] ret=new long[1][1][2][1];
		ret[0][0][1][0]=1;
		return ret;
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][][][] w() {
		long[][][][] ret=new long[1][1][1][2];
		ret[0][0][0][1]=1;
		return ret;
	}
	
	/**
	 * 多項式の加算 a + b を行う。
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a + b
	 */
	public static long[][][][] add(long[][][][] a, long[][][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		int l = 0;
		int k = 0;
		for (int i = 0; i < a.length; i++) {
			m = Math.max(m, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				l = Math.max(l, a[i][j].length);
				for (int p = 0; p < a[i][j].length; p++) {
					k = Math.max(k, a[i][j][p].length);
				}
			}
		}
		for (int i = 0; i < b.length; i++) {
			m = Math.max(m, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				l = Math.max(l, b[i][j].length);
				for (int p = 0; p < b[i][j].length; p++) {
					k = Math.max(k, b[i][j][p].length);
				}
			}
		}
		long[][][][] c = new long[n][m][l][k];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int p = 0; p < l; p++) {
					for (int q = 0; q < k; q++) {
						long valA = (i < a.length && j < a[i].length && p < a[i][j].length && q < a[i][j][p].length) ? a[i][j][p][q] : 0;
						long valB = (i < b.length && j < b[i].length && p < b[i][j].length && q < b[i][j][p].length) ? b[i][j][p][q] : 0;
						c[i][j][p][q] = valA + valB;
					}
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の減算 a - b を行う。
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a - b
	 */
	public static long[][][][] subtract(long[][][][] a, long[][][][] b) {
		int n = Math.max(a.length, b.length);
		int m = 0;
		int l = 0;
		int k = 0;
		for (int i = 0; i < a.length; i++) {
			m = Math.max(m, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				l = Math.max(l, a[i][j].length);
				for (int p = 0; p < a[i][j].length; p++) {
					k = Math.max(k, a[i][j][p].length);
				}
			}
		}
		for (int i = 0; i < b.length; i++) {
			m = Math.max(m, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				l = Math.max(l, b[i][j].length);
				for (int p = 0; p < b[i][j].length; p++) {
					k = Math.max(k, b[i][j][p].length);
				}
			}
		}
		long[][][][] c = new long[n][m][l][k];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int p = 0; p < l; p++) {
					for (int q = 0; q < k; q++) {
						long valA = (i < a.length && j < a[i].length && p < a[i][j].length && q < a[i][j][p].length) ? a[i][j][p][q] : 0;
						long valB = (i < b.length && j < b[i].length && p < b[i][j].length && q < b[i][j][p].length) ? b[i][j][p][q] : 0;
						c[i][j][p][q] = valA - valB;
					}
				}
			}
		}
		return c;
	}

	/**
	 * 多項式の乗算 a * b を行う（ナイーブな実装）。
	 * @param a 多項式a
	 * @param b 多項式b
	 * @return a * b
	 */
	public static long[][][][] mulNaive(long[][][][] a, long[][][][] b) {
		if (a.length == 0 || b.length == 0) return new long[0][0][0][0];
		int ma = 0, la = 0, ka = 0;
		for (int i = 0; i < a.length; i++) {
			ma = Math.max(ma, a[i].length);
			for (int j = 0; j < a[i].length; j++) {
				la = Math.max(la, a[i][j].length);
				for (int p = 0; p < a[i][j].length; p++) ka = Math.max(ka, a[i][j][p].length);
			}
		}
		int mb = 0, lb = 0, kb = 0;
		for (int i = 0; i < b.length; i++) {
			mb = Math.max(mb, b[i].length);
			for (int j = 0; j < b[i].length; j++) {
				lb = Math.max(lb, b[i][j].length);
				for (int p = 0; p < b[i][j].length; p++) kb = Math.max(kb, b[i][j][p].length);
			}
		}
		if (ma == 0 || la == 0 || ka == 0 || mb == 0 || lb == 0 || kb == 0) return new long[0][0][0][0];

		long[][][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1][ka + kb - 1];
		for (int i1 = 0; i1 < a.length; i1++) {
			for (int j1 = 0; j1 < a[i1].length; j1++) {
				for (int l1 = 0; l1 < a[i1][j1].length; l1++) {
					for (int k1 = 0; k1 < a[i1][j1][l1].length; k1++) {
						if (a[i1][j1][l1][k1] == 0) continue;
						for (int i2 = 0; i2 < b.length; i2++) {
							for (int j2 = 0; j2 < b[i2].length; j2++) {
								for (int l2 = 0; l2 < b[i2][j2].length; l2++) {
									for (int k2 = 0; k2 < b[i2][j2][l2].length; k2++) {
										c[i1 + i2][j1 + j2][l1 + l2][k1 + k2] += a[i1][j1][l1][k1] * b[i2][j2][l2][k2];
									}
								}
							}
						}
					}
				}
			}
		}
		return c;
	}

	public static CommutativeRingStrategy<long[][][][]> strategy(CommutativeRingStrategy<Long> strategy) {
		if (strategy instanceof library.util.algebra.strategy.IntegralDomainStrategy) {
			return new PolynomialLong4DIntegralDomainStrategy(strategy);
		}
		return new PolynomialLong4DRingStrategy(strategy);
	}

	private static class PolynomialLong4DRingStrategy implements CommutativeRingStrategy<long[][][][]> {
		protected final CommutativeRingStrategy<Long> strategy;

		public PolynomialLong4DRingStrategy(CommutativeRingStrategy<Long> strategy) {
			this.strategy = strategy;
		}

		@Override public long[][][][] zero() { return new long[0][0][0][0]; }
		@Override public long[][][][] one() { return new long[][][][]{{{{strategy.one()}}}}; }
		@Override public long[][][][] add(long[][][][] a, long[][][][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0, l = 0, k = 0;
			for (int i = 0; i < a.length; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) {
					l = Math.max(l, a[i][j].length);
					for (int p = 0; p < a[i][j].length; p++) k = Math.max(k, a[i][j][p].length);
				}
			}
			for (int i = 0; i < b.length; i++) {
				m = Math.max(m, b[i].length);
				for (int j = 0; j < b[i].length; j++) {
					l = Math.max(l, b[i][j].length);
					for (int p = 0; p < b[i][j].length; p++) k = Math.max(k, b[i][j][p].length);
				}
			}
			long[][][][] c = new long[n][m][l][k];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					for (int p = 0; p < l; p++) {
						for (int q = 0; q < k; q++) {
							long valA = (i < a.length && j < a[i].length && p < a[i][j].length && q < a[i][j][p].length) ? a[i][j][p][q] : strategy.zero();
							long valB = (i < b.length && j < b[i].length && p < b[i][j].length && q < b[i][j][p].length) ? b[i][j][p][q] : strategy.zero();
							c[i][j][p][q] = strategy.add(valA, valB);
						}
					}
				}
			}
			return c;
		}
		@Override public long[][][][] mul(long[][][][] a, long[][][][] b) {
			if (a.length == 0 || b.length == 0) return zero();
			int ma = 0, la = 0, ka = 0;
			for (int i = 0; i < a.length; i++) {
				ma = Math.max(ma, a[i].length);
				for (int j = 0; j < a[i].length; j++) {
					la = Math.max(la, a[i][j].length);
					for (int p = 0; p < a[i][j].length; p++) ka = Math.max(ka, a[i][j][p].length);
				}
			}
			int mb = 0, lb = 0, kb = 0;
			for (int i = 0; i < b.length; i++) {
				mb = Math.max(mb, b[i].length);
				for (int j = 0; j < b[i].length; j++) {
					lb = Math.max(lb, b[i][j].length);
					for (int p = 0; p < b[i][j].length; p++) kb = Math.max(kb, b[i][j][p].length);
				}
			}
			if (ma == 0 || la == 0 || ka == 0 || mb == 0 || lb == 0 || kb == 0) return zero();
			long[][][][] c = new long[a.length + b.length - 1][ma + mb - 1][la + lb - 1][ka + kb - 1];
			for (int i1 = 0; i1 < a.length; i1++) {
				for (int j1 = 0; j1 < a[i1].length; j1++) {
					for (int l1 = 0; l1 < a[i1][j1].length; l1++) {
						for (int k1 = 0; k1 < a[i1][j1][l1].length; k1++) {
							if (strategy.equals(a[i1][j1][l1][k1], strategy.zero())) continue;
							for (int i2 = 0; i2 < b.length; i2++) {
								for (int j2 = 0; j2 < b[i2].length; j2++) {
									for (int l2 = 0; l2 < b[i2][j2].length; l2++) {
										for (int k2 = 0; k2 < b[i2][j2][l2].length; k2++) {
											c[i1 + i2][j1 + j2][l1 + l2][k1 + k2] = strategy.add(c[i1 + i2][j1 + j2][l1 + l2][k1 + k2], strategy.mul(a[i1][j1][l1][k1], b[i2][j2][l2][k2]));
										}
									}
								}
							}
						}
					}
				}
			}
			return c;
		}
		@Override public long[][][][] neg(long[][][][] a) {
			int n = a.length;
			if (n == 0) return zero();
			int m = 0, l = 0, k = 0;
			for (int i = 0; i < n; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) {
					l = Math.max(l, a[i][j].length);
					for (int p = 0; p < a[i][j].length; p++) k = Math.max(k, a[i][j][p].length);
				}
			}
			long[][][][] c = new long[n][m][l][k];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < a[i].length; j++) {
					for (int p = 0; p < a[i][j].length; p++) {
						for (int q = 0; q < a[i][j][p].length; q++) c[i][j][p][q] = strategy.neg(a[i][j][p][q]);
					}
				}
			}
			return c;
		}
		@Override public boolean equals(long[][][][] a, long[][][][] b) {
			int n = Math.max(a.length, b.length);
			int m = 0, l = 0, k = 0;
			for (int i = 0; i < a.length; i++) {
				m = Math.max(m, a[i].length);
				for (int j = 0; j < a[i].length; j++) {
					l = Math.max(l, a[i][j].length);
					for (int p = 0; p < a[i][j].length; p++) k = Math.max(k, a[i][j][p].length);
				}
			}
			for (int i = 0; i < b.length; i++) {
				m = Math.max(m, b[i].length);
				for (int j = 0; j < b[i].length; j++) {
					l = Math.max(l, b[i][j].length);
					for (int p = 0; p < b[i][j].length; p++) k = Math.max(k, b[i][j][p].length);
				}
			}
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					for (int p = 0; p < l; p++) {
						for (int q = 0; q < k; q++) {
							long va = (i < a.length && j < a[i].length && p < a[i][j].length && q < a[i][j][p].length) ? a[i][j][p][q] : strategy.zero();
							long vb = (i < b.length && j < b[i].length && p < b[i][j].length && q < b[i][j][p].length) ? b[i][j][p][q] : strategy.zero();
							if (!strategy.equals(va, vb)) return false;
						}
					}
				}
			}
			return true;
		}
	}

	private static class PolynomialLong4DIntegralDomainStrategy extends PolynomialLong4DRingStrategy implements library.util.algebra.strategy.IntegralDomainStrategy<long[][][][]> {
		public PolynomialLong4DIntegralDomainStrategy(CommutativeRingStrategy<Long> strategy) {
			super(strategy);
		}
	}
	
	
	public static void printPolyAsExpr(String label, long[][][][] arr) {
	    System.out.println("=== " + label + " ===");
	    StringBuilder sb = new StringBuilder();
	    String[] vars = {"x", "y", "z", "w"};
	    boolean isFirst = true;

	    for (int i = 0; i < arr.length; i++) {
	        for (int j = 0; j < arr[i].length; j++) {
	            for (int k = 0; k < arr[i][j].length; k++) {
	                for (int l = 0; l < arr[i][j][k].length; l++) {
	                    long coeff = arr[i][j][k][l];
	                    if (coeff == 0) continue;

	                    // 2項以降で、正の数なら "+" を補う（負の数は自動で "-" が付く）
	                    if (!isFirst && coeff > 0) {
	                        sb.append(" + ");
	                    } else if (!isFirst && coeff < 0) {
	                        sb.append(" - ");
	                        coeff = -coeff; // 符号は出力したので絶対値にする
	                    } else if (isFirst && coeff < 0) {
	                        sb.append("-");
	                        coeff = -coeff;
	                    }

	                    // 変数の文字列を構築 (例: x^2 y z^3)
	                    StringBuilder varPart = new StringBuilder();
	                    int[] powers = {i, j, k, l};
	                    for (int v = 0; v < 4; v++) {
	                        if (powers[v] > 0) {
	                            varPart.append(vars[v]);
	                            if (powers[v] > 1) {
	                                varPart.append("^").append(powers[v]);
	                            }
	                            varPart.append(" ");
	                        }
	                    }

	                    // 係数と変数の結合処理
	                    if (varPart.length() == 0) {
	                        // 定数項の場合
	                        sb.append(coeff);
	                    } else {
	                        // 変数がある場合、係数が 1 なら省略
	                        if (coeff != 1) {
	                            sb.append(coeff).append("*");
	                        }
	                        sb.append(varPart.toString().trim());
	                    }

	                    isFirst = false;
	                }
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
}
