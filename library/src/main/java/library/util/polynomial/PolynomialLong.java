package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;
import library.util.algebra.strategy.CommutativeRingStrategy;

public class PolynomialLong {

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] zero() {
		return new long[0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] one() {
		return new long[] { 1 };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[] x() {
		return new long[] { 0, 1 };
	}

	public static long[] add(long[] a, long[] b) {
		long[] ret = new long[Math.max(a.length, b.length)];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (i < a.length ? a[i] : 0) + (i < b.length ? b[i] : 0);
		}
		return ret;
	}

	/**
	 * 未テスト
	 * @param a
	 * @param b
	 * @return
	 */
	public static ArrayList<Long> add(ArrayList<Long> a, ArrayList<Long> b) {
		ArrayList<Long> c = new ArrayList<>();
		for (int i = 0; i < Math.max(a.size(), b.size()); i++) {
			c.add((i < a.size() ? a.get(i) : 0) + (i < b.size() ? b.get(i) : 0));
		}
		return c;
	}

	public static long[] subtract(long[] a, long[] b) {
		long[] ret = new long[Math.max(a.length, b.length)];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (i < a.length ? a[i] : 0) - (i < b.length ? b[i] : 0);
		}
		return ret;
	}

	public static long[] mulNaive(long[] a, long[] b) {
		if (a.length == 0 || b.length == 0) return new long[0];
		long[] ret = new long[a.length + b.length - 1];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < b.length; ++j) {
				ret[i + j] += a[i] * b[j];
			}
		}
		return ret;
	}

	public static long[] mul(long[] a, long[] b) {
		return mulNaive(a, b);
	}

	/**
	 * 多項式h[0],h[1],..に対して
	 * [x^n] h[0](x)h[1](x^m)h[2](x^{2m})...を求める。
	 * @param n
	 * @param h
	 * @param base
	 * @return
	 */
	public static long nthMahler(long n, long[][] h, int base) {
		long[] g = new long[]{1};

		for (int i = 0; i < h.length; i++) {
			if (n == 0) {
				g[0] = g[0] * h[i][0];
				continue;
			}
			int r = (int) (n % base);
			n /= base;
			long[] f = PolynomialLong.mul(g, h[i]);
			long[] ng = new long[1 + (f.length - 1 - r) / base];
			for (int j = r; j < f.length; j += base) {
				ng[j / base] = f[j];
			}
			g = ng;
		}
		return g[0];
	}

	public static long[] ones(int n) {
		long[] a = new long[n];
		Arrays.fill(a, 1);
		return a;
	}

	static void tr(Object... objects) {
		System.out.println(Arrays.deepToString(objects));
	}

	public static long[] mul(int[] a, int[] b) {
		if (a.length == 0 || b.length == 0) return new long[0];
		long[] ret = new long[a.length + b.length - 1];
		for (int i = 0; i < a.length; ++i) {
			for (int j = 0; j < b.length; ++j) {
				ret[i + j] += 1L * a[i] * b[j];
			}
		}
		return ret;
	}

	/**
	 * a div b
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] div(long[] a, long[] b) {
		//https://atcoder.jp/contests/abc245/submissions/74409658
		int degB = b.length - 1;
		while (degB >= 0 && b[degB] == 0) degB--;
		if (degB == -1) throw new ArithmeticException("Division by zero polynomial");
		long[] ret = new long[Math.max(0, a.length - b.length) + 1];
		long[] r = a.clone();
		for (int i = r.length - 1; i >= degB; i--) {
			if (r[i] != 0) {
				long q = r[i] / b[degB];
				for (int j = 0; j <= degB; j++) {
					r[i - (degB - j)] -= q * b[j];
				}
				ret[i - degB] = q;
			}
		}
		return ret;
	}

	public static CommutativeRingStrategy<long[]> Strategy(CommutativeRingStrategy<Long> strategy) {
		if (strategy instanceof library.util.algebra.strategy.IntegralDomainStrategy) {
			return new PolynomialLongIntegralDomainStrategy(strategy);
		}
		return new PolynomialLongRingStrategy(strategy);
	}

	public static class PolynomialLongRingStrategy implements CommutativeRingStrategy<long[]> {
		protected final CommutativeRingStrategy<Long> strategy;

		public PolynomialLongRingStrategy(CommutativeRingStrategy<Long> strategy) {
			this.strategy = strategy;
		}

		@Override public long[] zero() { return new long[0]; }
		@Override public long[] one() { return new long[]{strategy.one()}; }
		@Override public long[] add(long[] a, long[] b) {
			long[] ret = new long[Math.max(a.length, b.length)];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = strategy.add(i < a.length ? a[i] : strategy.zero(), i < b.length ? b[i] : strategy.zero());
			}
			return trim(ret);
		}
		@Override public long[] mul(long[] a, long[] b) {
			if (a.length == 0 || b.length == 0) return zero();
			long[] ret = new long[a.length + b.length - 1];
			for (int i = 0; i < a.length; ++i) {
				if (strategy.equals(a[i], strategy.zero())) continue;
				for (int j = 0; j < b.length; ++j) {
					ret[i + j] = strategy.add(ret[i + j], strategy.mul(a[i], b[j]));
				}
			}
			return trim(ret);
		}
		@Override public long[] neg(long[] a) {
			long[] ret = new long[a.length];
			for (int i = 0; i < a.length; i++) ret[i] = strategy.neg(a[i]);
			return trim(ret);
		}
		@Override public boolean equals(long[] a, long[] b) {
			int n = Math.max(a.length, b.length);
			for (int i = 0; i < n; i++) {
				long va = i < a.length ? a[i] : strategy.zero();
				long vb = i < b.length ? b[i] : strategy.zero();
				if (!strategy.equals(va, vb)) return false;
			}
			return true;
		}
		/**
		 * 未テスト
		 * 末尾の零係数を取り除き、多項式の表現を正規化する。
		 * 計算量: O(n)
		 */
		protected long[] trim(long[] a) {
			int n = a.length;
			while (n > 0 && strategy.equals(a[n - 1], strategy.zero())) n--;
			return n == a.length ? a : Arrays.copyOf(a, n);
		}
	}

	public static class PolynomialLongIntegralDomainStrategy extends PolynomialLongRingStrategy implements library.util.algebra.strategy.IntegralDomainStrategy<long[]> {
		public PolynomialLongIntegralDomainStrategy(CommutativeRingStrategy<Long> strategy) {
			super(strategy);
		}
	}

	public static CommutativeRingStrategy<long[]> truncatedStrategy(CommutativeRingStrategy<Long> strategy, int n) {
		CommutativeRingStrategy<long[]> base = Strategy(strategy);
		return new CommutativeRingStrategy<long[]>() {
			@Override public long[] zero() { return base.zero(); }
			@Override public long[] one() { return base.one(); }
			@Override public long[] add(long[] a, long[] b) { return truncate(base.add(a, b), n); }
			@Override public long[] mul(long[] a, long[] b) { return truncate(base.mul(a, b), n); }
			@Override public long[] neg(long[] a) { return truncate(base.neg(a), n); }
			@Override public boolean equals(long[] a, long[] b) { return base.equals(truncate(a, n), truncate(b, n)); }
			private long[] truncate(long[] a, int n) {
				return a.length <= n ? a : Arrays.copyOf(a, n);
			}
		};
	}

	public record Factor(long[] factor, int multiplicity) {}
	public record FactorResult(long leadingCoeff, Factor[] factors) {}

	private static boolean isZero(long[] a) {
		if (a == null || a.length == 0) return true;
		for (long x : a) {
			if (x != 0) return false;
		}
		return true;
	}

	/**
	 * 未テスト
	 * 多項式を整数環 Z 上で因数分解する。
	 * cc.redberry.rings の既存ライブラリを用いて、1変数多項式の既約因数分解を計算する。
	 * 計算量: 入力多項式の次数に関して多項式時間（cc.redberry.rings の計算量に準じる）。
	 *
	 * @param inputf 因数分解する1変数多項式の係数配列。係数は昇べきの順。
	 * @return 因数分解結果を格納した FactorResult
	 * @throws IllegalArgumentException 入力がゼロ多項式、または null の場合
	 */
	public static FactorResult factor(long[] inputf) {
		if (inputf == null || isZero(inputf)) {
			throw new IllegalArgumentException("Cannot factorize zero polynomial");
		}
		cc.redberry.rings.poly.univar.UnivariatePolynomial<cc.redberry.rings.bigint.BigInteger> poly =
			cc.redberry.rings.poly.univar.UnivariatePolynomial.create(cc.redberry.rings.Rings.Z, inputf);

		cc.redberry.rings.poly.PolynomialFactorDecomposition<cc.redberry.rings.poly.univar.UnivariatePolynomial<cc.redberry.rings.bigint.BigInteger>> decomposed =
			cc.redberry.rings.poly.univar.UnivariateFactorization.Factor(poly);

		long leadingCoeff = ((cc.redberry.rings.bigint.BigInteger) decomposed.unit.cc()).longValue();

		int size = decomposed.size();
		Factor[] factors = new Factor[size];
		for (int i = 0; i < size; i++) {
			cc.redberry.rings.poly.univar.UnivariatePolynomial<cc.redberry.rings.bigint.BigInteger> f = decomposed.get(i);
			int deg = f.degree();
			long[] fArr = new long[deg + 1];
			for (int j = 0; j <= deg; j++) {
				fArr[j] = f.get(j).longValue();
			}
			factors[i] = new Factor(fArr, decomposed.getExponent(i));
		}

		return new FactorResult(leadingCoeff, factors);
	}
}
