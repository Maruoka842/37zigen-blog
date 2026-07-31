package library.util.polynomial;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.MathUtils;
import library.util.Zn;
import library.util.algebra.strategy.CommutativeRingStrategy;
import library.util.algebra.strategy.EuclideanDomainStrategy;
import library.util.algebra.strategy.ExactDivRingStrategy;
import library.util.algebra.strategy.UFDStrategy;

public class PolynomialFpDynamic implements EuclideanDomainStrategy<long[]>, UFDStrategy<long[]>, ExactDivRingStrategy<long[]> {
	public final long mod;
	Fp fp;

	public Fp getFp() { return fp; }

	// NTT関連フィールド
	public final boolean isNTTFriendly;
	public final long primitiveRoot;
	public final int maxPow2;
	long[][] bitreversedRoots;
	long[][] bitreversedInvRoots;
	public static final int FFT_NAIVE_THRESHOLD = 128;
	public static final int FFT_MIN_LENGTH_THRESHOLD = 10;

	/** 998244353 = 119×2^23+1, 原始根3 */
	public static final PolynomialFpDynamic MOD998244353 = new PolynomialFpDynamic(998244353L, 3);
	/** 469762049 = 7×2^26+1, 原始根3 */
	public static final PolynomialFpDynamic MOD469762049 = new PolynomialFpDynamic(469762049L, 3);
	/** 167772161 = 5×2^25+1, 原始根3 */
	public static final PolynomialFpDynamic MOD167772161 = new PolynomialFpDynamic(167772161L, 3);
	/** 754974721 = 45×2^24+1, 原始根11 */
	public static final PolynomialFpDynamic MOD754974721 = new PolynomialFpDynamic(754974721L, 11);
	/** 1004535809 = 479×2^21+1, 原始根3 */
	public static final PolynomialFpDynamic MOD1004535809 = new PolynomialFpDynamic(1004535809L, 3);

	/**
	 * NTT-friendly な素数 mod 用の多項式演算器を作る。
	 * 計算量 O(sqrt(mod)) 程度（原始根探索のため）。
	 */
	public static PolynomialFpDynamic nttFriendly(long mod) { return new PolynomialFpDynamic(mod, MathUtils.primitiveRoot(mod)); }

	public static PolynomialFpDynamic of(long mod) {
		if (mod == 998244353L) return MOD998244353;
		if (mod == 469762049L) return MOD469762049;
		if (mod == 167772161L) return MOD167772161;
		if (mod == 754974721L) return MOD754974721;
		if (mod == 1004535809L) return MOD1004535809;
		return new PolynomialFpDynamic(mod);
	}

	private PolynomialFpDynamic(long mod) {
		this.mod = mod;
		fp = new Fp(mod);
		this.isNTTFriendly = false;
		this.primitiveRoot = 0;
		this.maxPow2 = 0;
	}

	/**
	 * 多項式の非ゼロ項数を指定した上限まで数える。
	 * @param a 多項式
	 * @param limit 上限
	 * @return 非ゼロ項数。上限を超える場合は limit + 1
	 *
	 * <p>計算量: O(N)
	 */
	public int countTerms(long[] a, int limit) {
		int count = 0;
		for (long v : a) {
			if (fp.reduce(v) != 0) {
				count++;
				if (count > limit) return count;
			}
		}
		return count;
	}

	/**
	 * NTT-friendly素数用コンストラクタ。primitiveRootはmodの原始根であることを仮定する。
	 */
	public PolynomialFpDynamic(long mod, long primitiveRoot) {
		this.mod = mod;
		fp = new Fp(mod);
		this.isNTTFriendly = true;
		this.primitiveRoot = primitiveRoot;
		this.maxPow2 = Long.numberOfTrailingZeros(mod - 1);
		this.bitreversedRoots = new long[maxPow2 + 1][];
		this.bitreversedInvRoots = new long[maxPow2 + 1][];
	}

	long addMod(long a, long b) {
		long sum = a + b;
		return sum >= mod ? sum - mod : sum;
	}

	long subMod(long a, long b) {
		long diff = a - b;
		return diff < 0 ? diff + mod : diff;
	}


	void prepareRoots(int n) {
		if (Integer.bitCount(n) != 1) throw new AssertionError();
		int sz = Integer.numberOfTrailingZeros(n);
		if (sz > maxPow2) throw new AssertionError("NTT length exceeds mod - 1 power of two");
		if (bitreversedRoots[sz] != null) return;
		long root = MathUtils.modPow(primitiveRoot, (mod - 1) / n, mod);
		long iroot = MathUtils.modInv(root, mod);
		bitreversedRoots[sz] = new long[n];
		bitreversedInvRoots[sz] = new long[n];
		for (int n_ = n / 2; n_ >= 1; n_ /= 2, root = root * root % mod, iroot = iroot * iroot % mod) {
			long w = 1, iw = 1;
			for (int j = 0; j < n_; ++j) {
				bitreversedRoots[sz][n_ + j] = w;
				bitreversedInvRoots[sz][n_ + j] = iw;
				w = w * root % mod;
				iw = iw * iroot % mod;
			}
			int cur = 0;
			for (int j = 0; j < n_; ++j) {
				if (cur < j) {
					ArrayUtils.swap(n_ + cur, n_ + j, bitreversedRoots[sz]);
					ArrayUtils.swap(n_ + cur, n_ + j, bitreversedInvRoots[sz]);
				}
				for (int k = n_ / 2; k > (cur ^= k); k /= 2) ;
			}
		}
	}

	public void fftToBitReversed(long[] a) {
		int n = a.length;
		int sz = Integer.numberOfTrailingZeros(n);
		prepareRoots(n);
		for (int m = 1, t = n / 2; m <= n / 2; m *= 2, t /= 2) {
			for (int i = 0, k = 0; i < m; ++i, k += 2 * t) {
				long s = bitreversedRoots[sz][m + i];
				for (int j = k; j < k + t; ++j) {
					long u = a[j], v = a[j + t] * s % mod;
					a[j] = addMod(u, v);
					a[j + t] = subMod(u, v);
				}
			}
		}
	}

	public void ifftFromBitReversed(long[] a) {
		long invN = MathUtils.modInv(a.length, mod);
		int n = a.length;
		int sz = Integer.numberOfTrailingZeros(n);
		prepareRoots(n);
		for (int m = n / 2, t = 1; m >= 1; m /= 2, t *= 2) {
			for (int i = 0, k = 0; i < m; ++i, k += 2 * t) {
				long s = bitreversedInvRoots[sz][m + i];
				if (m == 1) s = s * invN % mod;
				for (int j = k; j < k + t; ++j) {
					long u = a[j], v = a[j + t];
					if (m == 1) a[j] = (u + v) * invN % mod;
					else a[j] = addMod(u, v);
					a[j + t] = (u + mod - v) * s % mod;
				}
			}
		}
	}

	public long[] mulFFT(long[] a, long[] b) {
		if (a.length == 0 || b.length == 0) return new long[0];
		int n = 1;
		int len = a.length + b.length - 1;
		while (n < len) n *= 2;
		if (Integer.numberOfTrailingZeros(n) > maxPow2) throw new AssertionError("NTT length exceeds mod - 1 power of two");
		long[] fa = new long[n], fb = new long[n];
		for (int i = 0; i < a.length; i++) fa[i] = fp.reduce(a[i]);
		for (int i = 0; i < b.length; i++) fb[i] = fp.reduce(b[i]);
		prepareRoots(n);
		fftToBitReversed(fa);
		fftToBitReversed(fb);
		for (int i = 0; i < n; ++i) fa[i] = fa[i] * fb[i] % mod;
		ifftFromBitReversed(fa);
		return Arrays.copyOf(fa, len);
	}
	
	
	
	
	/**
	 * f(x+c)
	 * @param c
	 * @return
	 * https://atcoder.jp/contests/abc389/submissions/73282621
	 */
	public long[] taylorShift(long[] f, long c) {
		// f(x+c) = sum f(c)^{i}/i!x^i
		// f(c)^{i} = sum k!f[k]  c^{k-i}/(k-i)!
		long[] a = new long[f.length];
		long[] b = new long[f.length];
		for (int i = 0; i < f.length; i++) {
			a[i] = fp.fac(i) * f[i] % mod;
		}
		b[0] = 1;
		for (int i = 1; i < f.length; i++) {
			b[i] = fp.mul(b[i - 1], c, fp.inv(i));
		}
		//n-1-i+j=k
		//i-j=k
		ArrayUtils.reverse(a);
		long[] differentiated = mul(a, b);
		differentiated = Arrays.copyOf(differentiated, f.length);
		ArrayUtils.reverse(differentiated);
		long[] ret = new long[f.length];
		for (int i = 0; i < f.length; ++i) {
			ret[i] = differentiated[i] * fp.ifac(i) % mod;
		}
		return ret;
	}
	
	
	/**
	 * 0 <= a[i], b[i] < mod を仮定する。
	 * @param a
	 * @param b
	 * @return
	 */
	public long[] mulNaive(long[] a, long[] b) {
		long[] c = new long[a.length + b.length - 1];
		for (int i = 0; i < a.length; i++) {
			if (a[i] == 0) continue;
			for (int j = 0; j < b.length; j++) {
				if (b[j] == 0) continue;
				c[i + j] = (c[i + j] + a[i] * b[j]) % mod;
			}
		}
		return c;
	}

	private long[] mulCRT(long[] a, long[] b) {
		int n = a.length, m = b.length;
		long m1 = 998244353L, m2 = 469762049L, m3 = 167772161L;
		double maxVal = (double) Math.min(n, m) * (mod - 1) * (mod - 1);
		int k_count = 3;
		if (maxVal < m1) k_count = 1;
		else if (maxVal < (double) m1 * m2) k_count = 2;

		long[] res1 = MOD998244353.mulFFT(a, b);
		if (k_count == 1) {
			for (int i = 0; i < res1.length; i++) res1[i] %= mod;
			return res1;
		}
		long[] res2 = MOD469762049.mulFFT(a, b);
		if (k_count == 2) {
			int len = res1.length;
			long[] res = new long[len];
			long[] v = new long[2];
			long[] ms = {m1, m2};
			for (int i = 0; i < len; i++) {
				v[0] = res1[i]; v[1] = res2[i];
				res[i] = Zn.crt(v, ms) % mod;
			}
			return res;
		}
		long[] res3 = MOD167772161.mulFFT(a, b);
		int len = res1.length;
		long[] res = new long[len];
		long inv123 = MathUtils.modInv(m1 % m3 * (m2 % m3) % m3, m3);
		long m12m = (m1 % mod) * (m2 % mod) % mod;
		long[] v = new long[2];
		long[] ms = {m1, m2};
		for (int i = 0; i < len; i++) {
			v[0] = res1[i]; v[1] = res2[i];
			long x12 = Zn.crt(v, ms);
			long k3 = (res3[i] - x12 % m3 + m3) % m3 * inv123 % m3;
			res[i] = (x12 % mod + (k3 % mod) * m12m % mod) % mod;
		}
		return res;
	}

	/**
	 * 返す配列の長さは a.length + b.length - 1
	 * ただし、a.length = 0 or b.length = 0 の場合は長さ 0
	 * {@inheritDoc}
	 */
	@Override
	public long[] mul(long[] a, long[] b) {
		if (a.length == 0 || b.length == 0) return new long[0];
		if (a.length == 1 && b.length == 1) return new long[]{a[0] * b[0] % mod};

		int n = a.length, m = b.length;
		if (n + m - 1 <= 128) return mulNaive(a, b);

		int sparseThreshold = 50;
		int countB = countTerms(b, sparseThreshold);
		if (countB <= sparseThreshold) return sparseMul(a, getTerms(b, countB), b.length);
		int countA = countTerms(a, sparseThreshold);
		if (countA <= sparseThreshold) return sparseMul(b, getTerms(a, countA), a.length);

		if (isNTTFriendly && n + m - 1 > FFT_NAIVE_THRESHOLD && Math.min(n, m) > FFT_MIN_LENGTH_THRESHOLD) {
			return mulFFT(a, b);
		} else if (!isNTTFriendly && n + m - 1 > 128) {
			return mulCRT(a, b);
		}
		return mulNaive(a, b);
	}

	/**
	 * 多項式の自乗 $a(x)^2$ を計算する。
	 *
	 * <p>計算量: $O(N \log N)$ (または非 NTT フレンドリーな場合 $O(N \log N)$、素朴な方法では $O(N^2)$)</p>
	 *
	 * @param a 多項式の係数配列
	 * @return $a(x)^2$
	 */
	// 未テスト
	public long[] squared(long[] a) {
		if (a.length == 0) return new long[0];
		if (a.length == 1) return new long[]{a[0] * a[0] % mod};

		int len = 2 * a.length - 1;
		if (isNTTFriendly && len > FFT_NAIVE_THRESHOLD) {
			return squaredFFT(a);
		} else if (!isNTTFriendly && len > 128) {
			return mulCRT(a, a);
		}
		return squaredNaive(a);
	}

	/**
	 * 多項式の自乗 $a(x)^2$ を NTT を用いて計算する。
	 *
	 * <p>計算量: $O(N \log N)$</p>
	 *
	 * @param a 多項式の係数配列
	 * @return $a(x)^2$
	 */
	// 未テスト
	private long[] squaredFFT(long[] a) {
		if (a.length == 0) return new long[0];
		int n = 1;
		int len = 2 * a.length - 1;
		while (n < len) n *= 2;
		if (Integer.numberOfTrailingZeros(n) > maxPow2) throw new AssertionError("NTT length exceeds mod - 1 power of two");
		long[] fa = new long[n];
		for (int i = 0; i < a.length; i++) fa[i] = fp.reduce(a[i]);
		prepareRoots(n);
		fftToBitReversed(fa);
		for (int i = 0; i < n; ++i) fa[i] = fa[i] * fa[i] % mod;
		ifftFromBitReversed(fa);
		return Arrays.copyOf(fa, len);
	}

	/**
	 * 多項式の自乗 $a(x)^2$ を $O(N^2)$ の素朴な方法で計算する。
	 *
	 * <p>計算量: $O(N^2)$</p>
	 *
	 * @param a 多項式の係数配列
	 * @return $a(x)^2$
	 */
	// 未テスト
	public long[] squaredNaive(long[] a) {
		int len = 2 * a.length - 1;
		long[] ret = new long[len];
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == 0) continue;
			for (int j = i + 1; j < a.length; ++j) {
				if (a[j] == 0) continue;
				ret[i + j] = (ret[i + j] + 2 * a[i] * a[j]) % mod;
			}
		}
		for (int i = 0; i < a.length; ++i) {
			if (a[i] == 0) continue;
			ret[2 * i] = (ret[2 * i] + a[i] * a[i]) % mod;
		}
		return ret;
	}

	/**
	 * a(x)b(x) mod m(x)を返す。未テスト。
	 */
	public long[] mulMod(long[] a, long[] b, long[] m) {
		return mod(mul(a, b), m);
	}
	
	public long[] powFull(long[] a, int n) {
		if (n == 0) return new long[] { 1 };
		if (n == 1) return a.clone();
		int d = deg(a);
		if (d == -1) return new long[0];
		if (d == 0) return new long[] { MathUtils.modPow(a[0], n, mod) };
		if (d <= 40) {
			return sparsePow(a, d * n + 1, n);
		}
		long[] ret=new long[1];
		ret[0]=1;
		long[] b=a.clone();
		while(n!=0) {
			if(n%2==1) {
				ret=mul(ret, b);
			}
			n/=2;
			if(n==0)break;
			b=mul(b,b);
		}
		return ret;
	}

	
	@Override
	public long[] zero() { return new long[0]; }

	@Override
	public long[] one() { return new long[] {1}; }

	/**
	 * 未テスト
	 * @return
	 */
	public long[] x() {
		return new long[] { 0, 1 };
	}

	@Override
	public long[] add(long[] a,long[] b) {
		long[] ret=new long[Math.max(a.length, b.length)];
		for (int i=0;i<ret.length;++i) {
			ret[i]=(i<a.length?a[i]:0)+(i<b.length?b[i]:0);
			if(ret[i]>=mod)ret[i]-=mod;
		}
		return ret;
	}

	@Override
	public long[] neg(long[] a) {
		long[] ret = new long[a.length];
		for (int i = 0; i < a.length; i++) {
			if (a[i] != 0) ret[i] = mod - a[i];
		}
		return ret;
	}

	@Override
	public boolean equals(long[] a, long[] b) {
		int n = Math.max(a.length, b.length);
		for (int i = 0; i < n; i++) {
			long va = i < a.length ? a[i] : 0;
			long vb = i < b.length ? b[i] : 0;
			if (va != vb) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PolynomialFpDynamic that = (PolynomialFpDynamic) o;
		return mod == that.mod;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(mod);
	}

	@Override
	public long norm(long[] a) {
		return deg(a) + 1;
	}

	@Override
	public long[] canonicalUnit(long[] a) {
		int d = deg(a);
		if (d == -1) return one();
		return new long[] {a[d]};
	}

	/**
	 * a(x)-b(x)を返す。未テスト。
	 */
	@Override
	public long[] sub(long[] a,long[] b) {
		if (a.length == 1 && b.length == 1) {
			long diff = a[0] - b[0];
			if (diff < 0) diff += mod;
			return new long[]{diff};
		}
		long[] ret=new long[Math.max(a.length, b.length)];
		for (int i=0;i<ret.length;++i) {
			ret[i]=(i<a.length?a[i]:0)-(i<b.length?b[i]:0);
			if (ret[i]<0) ret[i]+=mod;
		}
		return ret;
	}

	/**
	 * 多項式の非ゼロ項数を返す。
	 * @param a 多項式
	 * @return 非ゼロ項数
	 *
	 * <p>計算量: O(N)
	 */
	public int countTerms(long[] a) {
		int count = 0;
		for (long v : a) if (fp.reduce(v) != 0) count++;
		return count;
	}

	/**
	 * 多項式の次数を返す。ゼロ多項式なら-1。未テスト。
	 */
	public int deg(long[] a) {
		int n = a.length;
		if (n == 0) return -1;
		if (a[n - 1] != 0) return n - 1;
		for (int i = n - 2; i >= 0; i--) {
			if (a[i] != 0) return i;
		}
		return -1;
	}

	/**
	 * 多項式 f が 0 かを判定する。
	 * @param f 多項式
	 * @return f が 0 なら true
	 *
	 * <p>計算量: O(deg f)
	 */
	public boolean isZero(long[] f) {
		return deg(f) == -1;
	}

	/**
	 * 末尾の0を削った長さにする。未テスト。
	 */
	public long[] resize(long[] a) {
		return Arrays.copyOf(a, Math.max(0, deg(a)) + 1);
	}

	/**
	 * 最高次係数を1に正規化する。未テスト。
	 */
	public long[] monic(long[] a) {
		int deg = deg(a);
		if (deg == -1) return new long[0];
		if (deg == 0) return new long[]{1};
		long inv = fp.inv(a[deg]);
		long[] res = new long[deg + 1];
		for (int i = 0; i < deg; i++) res[i] = a[i] * inv % mod;
		res[deg] = 1;
		return res;
	}

	/**
	 * a(x) / b(x) の商を返す。未テスト。
	 */
	public long[] divNaive(long[] a, long[] b) {
		int degA=deg(a);
		int degB=deg(b);
		if (degB == -1) throw new ArithmeticException("division by zero polynomial");
		if (degA < degB) return new long[] {0};
		long[] r=Arrays.copyOf(a, degA + 1);
		long[] q=new long[degA - degB + 1];
		long invB=fp.inv(b[degB]);
		for (int i = degA; i >= degB; i--) {
			if (r[i] == 0) continue;
			long c=r[i]*invB%mod;
			q[i-degB]=c;
			for (int j = 0; j <= degB; j++) {
				r[i-degB+j]-=c*b[j]%mod;
				if (r[i-degB+j]<0) r[i-degB+j]+=mod;
			}
		}
		return resize(q);
	}

	/**
	 * a(x) mod b(x)を返す。未テスト。
	 */
	public long[] modNaive(long[] a, long[] b) {
		int degA=deg(a);
		int degB=deg(b);
		if (degB == -1) throw new ArithmeticException("division by zero polynomial");
		if (degA < degB) return resize(a);
		long[] r=Arrays.copyOf(a, degA + 1);
		long invB=fp.inv(b[degB]);
		for (int i = degA; i >= degB; i--) {
			if (r[i] == 0) continue;
			long c=r[i]*invB%mod;
			for (int j = 0; j <= degB; j++) {
				r[i-degB+j]-=c*b[j]%mod;
				if (r[i-degB+j]<0) r[i-degB+j]+=mod;
			}
		}
		return resize(r);
	}

	public static class DivModResult{
		public long[] q;
		public long[] r;
		public DivModResult(long[] q, long[] r) {
			this.q=q;
			this.r=r;
		}
	}

	/**
	 * a(x)をb(x)で割った商と余りを返す。未テスト。
	 */
	public DivModResult divmod(long[] a, long[] b) {
		var q = div(a, b);
		var r = sub(a, mul(q, b));
		r = resize(r);
		return new DivModResult(q, r);
	}

	/**
	 * monicなgcdを返す。未テスト。
	 */
	public long[] gcdNaive(long[] a, long[] b) {
		a=resize(a);
		b=resize(b);
		while (deg(b) != -1) {
			long[] r=modNaive(a, b);
			a=b;
			b=r;
		}
		return monic(a);
	}

	/**
	 * a(x)^e mod m(x)を返す。未テスト。
	 */
	public long[] powMod(long[] a, long e, long[] m) {
		long[] ret=mod(new long[] {1}, m);
		long[] base=mod(a, m);
		for (int i = 63 - Long.numberOfLeadingZeros(e); i >= 0; i--) {
			ret=mulMod(ret, ret, m);
			if (((e >>> i) & 1) != 0) ret=mulMod(ret, base, m);
		}
		return ret;
	}

	/**
	 * Equal-Degree Factorization用の累乗を返す。未テスト。
	 */
	long[] equalDegreePower(long[] a, int degree, long[] m) {
		long[] x=powMod(a, (mod - 1) / 2, m);
		long[] ret=mod(new long[] {1}, m);
		for (int i = 0; i < degree; i++) {
			ret=mulMod(ret, x, m);
			if (i + 1 < degree) x=powMod(x, mod, m);
		}
		return ret;
	}

	/**
	 * p=2のEqual-Degree Factorization用traceを返す。未テスト。
	 */
	long[] equalDegreeTraceFp2(long[] a, int degree, long[] m) {
		long[] ret=new long[] {0};
		long[] cur=mod(a, m);
		for (int i = 0; i < degree; i++) {
			ret=add(ret, cur);
			if (i + 1 < degree) cur=mulMod(cur, cur, m);
		}
		return mod(ret, m);
	}

	/**
	 * ランダム多項式を返す。未テスト。
	 */
	long[] randomPolynomial(int len, Random rnd) {
		long[] ret=new long[len];
		for (int i = 0; i < len; i++) {
			ret[i]=Math.floorMod(rnd.nextLong(), mod);
		}
		return resize(ret);
	}

	/**
	 * nの相異なる素因数を返す。未テスト。
	 */
	ArrayList<Integer> primeDivisors(int n) {
		ArrayList<Integer> ret=new ArrayList<>();
		for (int p = 2; p * p <= n; p++) {
			if (n % p == 0) {
				ret.add(p);
				while (n % p == 0) n/=p;
			}
		}
		if (n > 1) ret.add(n);
		return ret;
	}

	/**
	 * fが既約多項式かRabinの既約判定で調べる。未テスト。
	 */
	public boolean isIrreducible(long[] inputf) {
		long[] f=monic(inputf);
		int n=deg(f);
		if (n <= 0) return false;
		long[] x=new long[] {0, 1};
		long[] xMod=mod(x, f);
		long[] h=x;
		for (int p: primeDivisors(n)) {
			int repeat=n / p;
			h=x;
			for (int i = 0; i < repeat; i++) h=powMod(h, mod, f);
			if (deg(gcd(f, sub(h, xMod))) > 0) return false;
		}
		h=x;
		for (int i = 0; i < n; i++) h=powMod(h, mod, f);
		return deg(sub(h, xMod)) == -1;
	}

	/**
	 * 次数degreeのランダムなmonic既約多項式を返す。未テスト。
	 */
	public long[] randomIrreduciblePolynomial(int degree) {
		return randomIrreduciblePolynomial(degree, new Random());
	}

	/**
	 * 次数degreeのランダムなmonic既約多項式を返す。未テスト。
	 */
	public long[] randomIrreduciblePolynomial(int degree, Random rnd) {
		if (degree <= 0) throw new AssertionError();
		while (true) {
			long[] f=new long[degree + 1];
			for (int i = 0; i < degree; i++) f[i]=Math.floorMod(rnd.nextLong(), mod);
			f[degree]=1;
			if (isIrreducible(f)) return f;
		}
	}

	/**
	 * Equal-Degree Factorization。未テスト。
	 */
	public long[][] factorEqualDegree(long[] inputf, int degree) {
		if (degree <= 0) throw new AssertionError();
		long[] f=monic(inputf);
		int deg=deg(f);
		if (deg <= 0) return new long[0][];
		if (deg % degree != 0) throw new AssertionError();
		Random rnd=new Random();
		ArrayList<long[]> ret=new ArrayList<>();
		Queue<long[]> que=new ArrayDeque<>();
		que.add(f);
		while (!que.isEmpty()) {
			long[] cur=monic(que.poll());
			int curDeg=deg(cur);
			if (curDeg == degree) {
				ret.add(cur);
				continue;
			}
			if (curDeg % degree != 0) throw new AssertionError();
			long[] g;
			do {
				long[] a=randomPolynomial(curDeg, rnd);
				long[] h;
				if (mod == 2) {
					h=equalDegreeTraceFp2(a, degree, cur);
				} else {
					h=sub(equalDegreePower(a, degree, cur), new long[] {1});
				}
				g=gcd(cur, h);
			} while (deg(g) <= 0 || deg(g) == curDeg);
			que.add(g);
			que.add(div(cur, g));
		}
		return ret.toArray(new long[ret.size()][]);
	}

	public static class Factor {
		public long[] factor;
		public int multiplicity;
		public Factor(long[] factor, int multiplicity) {
			this.factor=factor;
			this.multiplicity=multiplicity;
		}
	}

	/**
	 * 多項式の因数分解結果を保持するレコード。
	 * @param leadingCoeff 主係数
	 * @param factors 既約因子の配列（すべて monic）
	 */
	public record FactorResult(long leadingCoeff, Factor[] factors) {}

	/**
	 * square-free な分母を持つ有理関数の項 a/g。
	 */
	public record SquareFreeTerm(long[] a, long[] g) {}

	/**
	 * エルミート簡約の結果。
	 * f/g = q + (c/d)' + \sum logPart[i].a / logPart[i].g
	 */
	public record HermiteResult(long[] q, long[] c, long[] d, SquareFreeTerm[] logPart) {}

	/**
	 * 多項式を monic な既約因子と重複度に分解する。
	 */
	public FactorResult factor(long[] inputf) {
		inputf = resize(inputf);
		int d = deg(inputf);
		if (d == -1) return new FactorResult(0, new Factor[0]);
		long leading = inputf[d];
		long[] f = monic(inputf);
		if (d == 0) return new FactorResult(leading, new Factor[0]);
		
		// 1. 各項を微分して GCD を取り、べき乗成分（重複因子）を分離する
		// ここでは簡略化のため、まず重複度を考慮せずに既約分解を目指す
		ArrayList<Factor> ret=new ArrayList<>();
		
		// 2. 既約分解のメインループ
		long[] x=new long[] {0, 1};
		long[] h=x;
		
		// f を破壊的に更新していくためコピー
		long[] currentF = Arrays.copyOf(f, f.length);
		
		for (int degree = 1; deg(currentF) > 0 && 2 * degree <= deg(f); degree++) {
			h=powMod(h, mod, f);
			long[] g=gcd(currentF, sub(h, x));
			if (deg(g) > 0) {
				for (long[] irred: factorEqualDegree(g, degree)) {
					int multiplicity=0;
					while (deg(currentF) >= deg(irred)) {
						DivModResult divmod=divmod(currentF, irred);
						if (deg(divmod.r) != -1) break;
						currentF=divmod.q;
						multiplicity++;
					}
					if (multiplicity > 0) {
						ret.add(new Factor(irred, multiplicity));
					}
				}
				if (deg(currentF) <= 0) break;
				h=mod(h, currentF);
			}
		}
		if (deg(currentF) > 0) {
			// 残った部分が既約であるか、高次のべき乗である可能性がある
			// 本来的には square-free 分解が必要だが、ここでは残りを 1 つの因子として追加
			ret.add(new Factor(monic(currentF), 1));
		}
		
		// 重複して登録された因子をまとめる
		ArrayList<Factor> finalRet = new ArrayList<>();
		for (Factor fact : ret) {
			boolean found = false;
			for (Factor existing : finalRet) {
				if (Arrays.equals(existing.factor, fact.factor)) {
					existing.multiplicity += fact.multiplicity;
					found = true;
					break;
				}
			}
			if (!found) finalRet.add(fact);
		}
		
		return new FactorResult(leading, finalRet.toArray(new Factor[finalRet.size()]));
	}
	
	
    
	public long[] differentiate(long[] a) {
		long[] ret = new long[a.length];
		for (int i = 1; i < a.length; ++i) ret[i - 1] = i * a[i] % mod;
		return ret;
	}

	/**
	 * 多項式 a(x) を repeat 回微分する。
	 * @param a 多項式
	 * @param repeat 微分回数
	 * @return a^{(repeat)}(x)
	 *
	 * <p>計算量: O(n)
	 * <p>未テスト
	 */
	public long[] diff(long[] a, int repeat) {
		if (repeat < 0) throw new IllegalArgumentException("repeat must be non-negative");
		if (repeat == 0) return a.clone();
		if (repeat >= a.length) return new long[0];
		long[] ret = new long[a.length - repeat];
		for (int i = repeat; i < a.length; i++) {
			ret[i - repeat] = fp.perm(i, repeat) * a[i] % mod;
		}
		return ret;
	}

	public long[] integrate(long[] a) {
		long[] ret = new long[a.length];
		for (int i = 0; i + 1 < a.length; ++i) ret[i + 1] = MathUtils.modInv(i + 1, mod) * a[i] % mod;
		return ret;
	}

	public long[] invNaive(long[] a) {
		long[] g = new long[a.length];
		long inv0 = MathUtils.modInv(a[0], mod);
		g[0] = inv0;
		for (int i = 1; i < a.length; i++) {
			long sum = 0;
			for (int j = 1; j <= i; j++) if (j < a.length) sum = (sum + a[j] * g[i - j]) % mod;
			g[i] = sum == 0 ? 0 : (mod - sum) % mod * inv0 % mod;
		}
		return g;
	}

	public long[] invFFT(long[] a) {
		long[] g = new long[] {MathUtils.modInv(a[0], mod)};
		for (int len = 1; len < a.length; len *= 2) {
			long[] fftG = Arrays.copyOf(g, len * 4);
			long[] fftA = new long[4 * len];
			System.arraycopy(a, 0, fftA, 0, Math.min(2 * len, a.length));
			prepareRoots(4 * len);
			fftToBitReversed(fftG);
			fftToBitReversed(fftA);
			for (int i = 0; i < fftG.length; ++i) fftG[i] = fftG[i] * fftG[i] % mod * fftA[i] % mod;
			ifftFromBitReversed(fftG);
			for (int i = 0; i < len; ++i) fftG[i] = g[i];
			for (int i = len; i < 2 * len; ++i) if (fftG[i] != 0) fftG[i] = mod - fftG[i];
			g = Arrays.copyOf(fftG, Math.min(a.length, 2 * len));
		}
		return g;
	}

	public long[] inv(long[] a) {
		return isNTTFriendly ? invFFT(a) : invNaive(a);
	}

	public long[] log(long[] a) {
		return integrate(Arrays.copyOf(mul(differentiate(a), inv(a)), a.length));
	}

	public long[] expNaive(long[] a) {
		if (a[0] != 0) throw new AssertionError();
		int n = a.length;
		long[] g = new long[n];
		long[] df = new long[n];
		for (int i = 0; i < n; i++) df[i] = i * a[i] % mod;
		g[0] = 1;
		for (int i = 1; i < n; i++) {
			long sum = 0;
			for (int j = 1; j <= i; j++) sum = (sum + df[j] * g[i - j]) % mod;
			g[i] = sum * fp.inv(i) % mod;
		}
		return g;
	}

	public long[] expFFT(long[] a) {
		if (a[0] != 0) throw new AssertionError();
		long[] g = new long[] {1};
		for (int len = 1; len < a.length; len *= 2) {
			long[] tmp = sub(Arrays.copyOf(a, Math.min(2 * len, a.length)), log(Arrays.copyOf(g, Math.min(2 * len, a.length))));
			tmp[0] = addMod(tmp[0], 1);
			g = Arrays.copyOf(mul(g, tmp), Math.min(2 * len, a.length));
		}
		return Arrays.copyOf(g, a.length);
	}

	public long[] exp(long[] a) {
		return isNTTFriendly ? expFFT(a) : expNaive(a);
	}

	/**
	 * 多項式の Plethystic exponential (PE) を入力多項式 f の長さで計算する。
	 * f(x) の Plethystic exponential は以下で定義される。
	 * PE[f](x) = exp( sum_{k=1}^{N-1} f(x^k) / k ) mod x^N (N = f.length)
	 *
	 * 事前条件: f(0) = 0 mod mod
	 *
	 * @param f 多項式
	 * @return 計算された PE[f](x) mod x^N
	 * @throws IllegalArgumentException f(0) != 0 mod mod の場合
	 *
	 * <p>計算量: O(N log N) (NTTが利用可能な場合), O(N^2) (その他)
	 */
	// 未テスト
	public long[] plethysticExponential(long[] f) {
		return plethysticExponential(f, f.length);
	}

	/**
	 * PE[f]
	 * = prod 1/(1 - x^i)^f[i] mod x^n
	 * = exp( sum_{k=1}^{n-1} f(x^k) / k ) mod x^n
	 * 事前条件: f(0) = 0 mod mod, n >= 1
	 *
	 * @param f 多項式
	 * @param n 計算する項数
	 * @return 計算された PE[f](x) mod x^n
	 * @throws IllegalArgumentException f(0) != 0 mod mod または n <= 0 の場合
	 *
	 * <p>計算量: O(n log n) (NTTが利用可能な場合), O(n^2) (その他)
	 */
	// 未テスト
	public long[] plethysticExponential(long[] f, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if (f.length > 0 && fp.reduce(f[0]) != 0) {
			throw new IllegalArgumentException("f[0] must be 0");
		}
		if (n == 1) {
			return new long[] {1};
		}
		
		// = sum_k 1/k sum_i f[i] x^{i k}
		// = sum_i f[i] sum_k x^{i k} / k
		// = sum_i f[i] * log(1/(1 - x^i)) なぜなら log(1/(1-z)) = sum_{k>=1} z^k/k
		//   f = exp( sum_{k=1}^{n-1} f(x^k) / k )
		
		fp.expand(n);
		long[] g = new long[n];
		int limit = Math.min(f.length, n);
		// sum_i f[i] sum_k x^{i k} / k
		for (int i = 1; i < limit; i++) {
			long val = fp.reduce(f[i]);
			if (val == 0) continue;
			for (int k = 1; i * k < n; k++) {
				long term = val * fp.inv(k) % mod;
				g[i * k] = (g[i * k] + term) % mod;
			}
		}
		return exp(g);
	}

	/**
	 * PL[f](x) = sum_{k=1}^{N-1} (mu(k) / k) * log(f(x^k)) mod x^N (N = f.length, mu(k) はメビウス関数)
	 *
	 * 事前条件: f(0) = 1 mod mod
	 *
	 * @param f 多項式
	 * @return 計算された PL[f](x) mod x^N
	 * @throws IllegalArgumentException f(0) != 1 mod mod の場合
	 *
	 * <p>計算量: O(N log N) (NTTが利用可能な場合), O(N^2) (その他)
	 */
	// 未テスト
	public long[] plethysticLogarithm(long[] f) {
		return plethysticLogarithm(f, f.length);
	}

	/**
	 * PL[f](x) = sum_{k=1}^{n-1} (mu(k) / k) * ln(f(x^k)) mod x^n (mu(k) はメビウス関数)
	 *
	 * 事前条件: f(0) = 1 mod mod, n >= 1
	 *
	 * @param f 多項式
	 * @param n 計算する項数
	 * @return 計算された PL[f](x) mod x^n
	 * @throws IllegalArgumentException f(0) != 1 mod mod または n <= 0 の場合
	 *
	 * <p>計算量: O(n log n) (NTTが利用可能な場合), O(n^2) (その他)
	 */
	// 未テスト
	public long[] plethysticLogarithm(long[] f, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if (f.length == 0 || fp.reduce(f[0]) != 1) {
			throw new IllegalArgumentException("f[0] must be 1");
		}
		if (n == 1) {
			return new long[] {0};
		}
		
		//log f = sum_k 1/k sum_i g[i] x^{i k}
		//これを sum F[i]x^i と置くと
		// nF[n] = sum_{k | n} n / k g[n / k]

		long[] fPadded = new long[n];
		for (int i = 0; i < Math.min(f.length, n); i++) {
			fPadded[i] = fp.reduce(f[i]);
		}
		long[] g = log(fPadded); // g[0] is 0
		long[] G = new long[n];
		for (int i = 1; i < n; i++) {
			G[i] = g[i] * i % mod;
		}
		long[] H = library.util.poset.DivisorLattice.moebius(G, mod);
		long[] h = new long[n];
		fp.fac(n);
		for (int i = 1; i < n; i++) {
			h[i] = H[i] * fp.inv(i) % mod;
		}
		return h;
	}

	/**
	 * 多項式の Cycle Plethystic exponential を入力多項式 a の長さで計算する。
	 * a(x) の Cycle Plethystic exponential は以下で定義される。
	 * CYC_PE[a](x) = sum_{k=1}^{N-1} (phi(k) / k) * -ln(1 - a(x^k)) mod x^N (N = a.length, phi(k) はオイラーのφ関数)
	 *
	 * 事前条件: a(0) = 0 mod mod
	 *
	 * @param a 多項式
	 * @return 計算された CYC_PE[a](x) mod x^N
	 * @throws IllegalArgumentException a(0) != 0 mod mod の場合
	 *
	 * <p>計算量: O(N log N) (NTTが利用可能な場合), O(N^2) (その他)
	 */
	// 未テスト
	public long[] cyclePlethysticExponential(long[] a) {
		return cyclePlethysticExponential(a, a.length);
	}

	/**
	 * 多項式の Cycle Plethystic exponential を指定された長さ n で計算する。
	 * a(x) の Cycle Plethystic exponential は以下で定義される。
	 * CYC_PE[a](x) = sum_{k=1}^{n-1} (phi(k) / k) * -ln(1 - a(x^k)) mod x^n (phi(k) はオイラーのφ関数)
	 *
	 * 事前条件: a(0) = 0 mod mod, n >= 1
	 *
	 * @param a 多項式
	 * @param n 計算する項数
	 * @return 計算された CYC_PE[a](x) mod x^n
	 * @throws IllegalArgumentException a(0) != 0 mod mod または n <= 0 の場合
	 *
	 * <p>計算量: O(n log n) (NTTが利用可能な場合), O(n^2) (その他)
	 */
	// 未テスト
	public long[] cyclePlethysticExponential(long[] a, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if (a.length > 0 && fp.reduce(a[0]) != 0) {
			throw new IllegalArgumentException("a[0] must be 0");
		}
		if (n == 1) {
			return new long[] {0};
		}
		// Expand primes and totient in Sieve up to n
		library.util.Sieve.expandPrimes(n);
		fp.expand(n);

		// Compute C(x) = -ln(1 - a(x)) mod x^n
		long[] oneMinusA = new long[n];
		oneMinusA[0] = 1;
		for (int i = 1; i < Math.min(a.length, n); i++) {
			oneMinusA[i] = (mod - fp.reduce(a[i])) % mod;
		}
		long[] logOneMinusA = log(oneMinusA);
		long[] C = new long[n];
		for (int i = 1; i < n; i++) {
			C[i] = (mod - logOneMinusA[i]) % mod;
		}

		// Compute B(x) = sum_{k=1}^{n-1} (phi(k)/k) * C(x^k) mod x^n
		long[] B = new long[n];
		for (int k = 1; k < n; k++) {
			long phiK = library.util.Sieve.totient(k) % mod;
			long termCoeff = phiK * fp.inv(k) % mod;
			for (int i = 1; i * k < n; i++) {
				B[i * k] = (B[i * k] + termCoeff * C[i]) % mod;
			}
		}
		return B;
	}

	/**
	 * 多項式の Cycle Plethystic logarithm を入力多項式 b の長さで計算する。
	 * b(x) の Cycle Plethystic logarithm は CYC_PE の逆変換であり、以下を満たす。
	 * CYC_PE[CYC_PL[b]](x) = b(x) mod x^N (N = b.length)
	 *
	 * 事前条件: b(0) = 0 mod mod
	 *
	 * @param b 多項式
	 * @return 計算された CYC_PL[b](x) mod x^N
	 * @throws IllegalArgumentException b(0) != 0 mod mod の場合
	 *
	 * <p>計算量: O(N log N) (NTTが利用可能な場合), O(N^2) (その他)
	 */
	// 未テスト
	public long[] cyclePlethysticLogarithm(long[] b) {
		return cyclePlethysticLogarithm(b, b.length);
	}

	/**
	 * 多項式の Cycle Plethystic logarithm を指定された長さ n で計算する。
	 * b(x) の Cycle Plethystic logarithm は CYC_PE の逆変換であり、以下を満たす。
	 * CYC_PE[CYC_PL[b]](x) = b(x) mod x^n
	 *
	 * 事前条件: b(0) = 0 mod mod, n >= 1
	 *
	 * @param b 多項式
	 * @param n 計算する項数
	 * @return 計算された CYC_PL[b](x) mod x^n
	 * @throws IllegalArgumentException b(0) != 0 mod mod または n <= 0 の場合
	 *
	 * <p>計算量: O(n log n) (NTTが利用可能な場合), O(n^2) (その他)
	 */
	// 未テスト
	public long[] cyclePlethysticLogarithm(long[] b, int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}
		if (b.length > 0 && fp.reduce(b[0]) != 0) {
			throw new IllegalArgumentException("b[0] must be 0");
		}
		if (n == 1) {
			return new long[] {0};
		}
		library.util.Sieve.totient(n);
		fp.fac(n);

		// Pad/truncate b to length n and compute Bprime[i] = i * b[i] % mod
		long[] Bprime = new long[n];
		for (int i = 1; i < Math.min(b.length, n); i++) {
			Bprime[i] = fp.reduce(b[i]) * i % mod;
		}

		// Compute Cprime[m] = Bprime[m] - sum_{k|m, k>1} phi(k) Cprime[m/k] mod mod
		long[] Cprime = new long[n];
		for (int i = 1; i < n; i++) {
			Cprime[i] = Bprime[i];
		}
		for (int i = 1; i < n; i++) {
			for (int j = 2; i * j < n; j++) {
				long phiJ = library.util.Sieve.totient(j) % mod;
				long val = phiJ * Cprime[i] % mod;
				Cprime[i * j] = (Cprime[i * j] - val + mod) % mod;
			}
		}

		// Compute C[i] = Cprime[i] * i^-1 mod mod
		long[] C = new long[n];
		for (int i = 1; i < n; i++) {
			C[i] = Cprime[i] * fp.inv(i) % mod;
		}

		// Recover a(x) = 1 - exp(-C(x)) mod x^n
		long[] negC = new long[n];
		for (int i = 1; i < n; i++) {
			negC[i] = (mod - C[i]) % mod;
		}
		long[] expNegC = exp(negC);
		long[] a = new long[n];
		a[0] = 0;
		for (int i = 1; i < n; i++) {
			a[i] = (mod - expNegC[i]) % mod;
		}
		return a;
	}

	/**
	 * $\prod_{k=0}^{M-1} f(r^k x) \pmod{x^n}$ を計算する。
	 * $f(0) = 1$ であることを仮定する。
	 *
	 * @param f 多項式
	 * @param r 公比
	 * @param M 積をとる項数
	 * @return $\prod_{k=0}^{M-1} f(r^k x) \pmod{x^n}$
	 *
	 * <p>計算量: O(n \log n)
	 */
	public long[] productOfFRKx(long[] f, long r, long M) {
		if (f.length == 0) return new long[0];
		if (fp.reduce(f[0]) != 1) throw new AssertionError("f[0] must be 1");
		if (M == 0) {
			long[] ret = new long[f.length];
			ret[0] = 1;
			return ret;
		}
		int n = f.length;
		long[] g = log(f);
		long rm = MathUtils.modPow(fp.reduce(r), M, mod);
		long ri = 1;
		long rmi = 1;
		long redR = fp.reduce(r);
		for (int i = 1; i < n; i++) {
			ri = ri * redR % mod;
			rmi = rmi * rm % mod;
			if (ri == 1) {
				g[i] = g[i] * (M % mod) % mod;
			} else {
				g[i] = g[i] * subMod(rmi, 1) % mod * MathUtils.modInv(subMod(ri, 1), mod) % mod;
			}
		}
		return exp(g);
	}

	public long[] sqrt(long[] a) {
		long b = MathUtils.modKthRoot(a[0], 2, mod);
		long[] ret = mul(a, MathUtils.modInv(a[0], mod));
		ret = log(ret);
		ret = mul(ret, MathUtils.modInv(2, mod));
		ret = exp(ret);
		return mul(ret, b);
	}

	public long[] mul(long[] a, long b) {
		long[] ret = new long[a.length];
		for (int i = 0; i < a.length; i++) ret[i] = a[i] * b % mod;
		return ret;
	}

	public long[] pow(long[] a, long m) {
		int len = a.length;
		if (m == 0) { long[] ret = new long[len]; ret[0] = 1; return ret; }
		if (m == 1) return a.clone();
		if (m == 2) return squared(a);
		int s = 0;
		while (s < a.length && a[s] == 0) ++s;
		if (s == a.length) return a.clone();
		long[] aa = s != 0 ? Arrays.copyOfRange(a, s, a.length) : a.clone();
		long b = MathUtils.modInv(aa[0], mod);
		for (int i = 0; i < aa.length; i++) aa[i] = b * aa[i] % mod;
		aa = log(aa);
		for (int i = 0; i < aa.length; i++) aa[i] = m % mod * aa[i] % mod;
		aa = exp(aa);
		b = MathUtils.modPow(MathUtils.modInv(b, mod), m % (mod - 1), mod);
		for (int i = 0; i < aa.length; i++) aa[i] = b * aa[i] % mod;
		long[] ret = new long[len];
		if (s <= (len - 1) / m) {
			for (long i = (long) s * m; i < len && i - s * m < aa.length; ++i)
				ret[(int) i] = aa[(int) (i - s * m)];
		}
		return ret;
	}

	/**
	 * f(-x)
	 * @param f
	 * @return
	 */
	public long[] negatedX(long[] f) {
		long[] g = Arrays.copyOf(f, f.length);
		for (int i = 1; i < g.length; i += 2) if (g[i] != 0) g[i] = mod - g[i];
		return g;
	}

	public long[] divideByX(long[] f, int repeat) {
		return Arrays.copyOfRange(f, repeat, f.length);
	}

	public long[] multiplyByX(long[] f, int repeat) {
		long[] ret = new long[f.length + repeat];
		for (int i = 0; i < f.length; ++i) ret[repeat + i] = f[i];
		return ret;
	}

	public long[] cyclicmulFFT(long[] a, long[] b, int n) {
		if (!isNTTFriendly) throw new AssertionError("cyclicmulFFT requires NTT-friendly mod");
		if (Integer.bitCount(n) != 1) throw new AssertionError();
		long[] A = new long[n], B = new long[n];
		for (int i = 0; i < a.length; i++) A[i % n] = addMod(A[i % n], fp.reduce(a[i]));
		for (int i = 0; i < b.length; i++) B[i % n] = addMod(B[i % n], fp.reduce(b[i]));
		prepareRoots(n);
		fftToBitReversed(A);
		fftToBitReversed(B);
		for (int i = 0; i < n; ++i) A[i] = A[i] * B[i] % mod;
		ifftFromBitReversed(A);
		return Arrays.copyOf(A, n);
	}

	public long[] cyclicPowFFT(long[] a, long m, int n) {
		if (!isNTTFriendly) throw new AssertionError("cyclicPowFFT requires NTT-friendly mod");
		if (Integer.bitCount(n) != 1) throw new AssertionError();
		long[] A = new long[n];
		for (int i = 0; i < a.length; i++) A[i % n] = addMod(A[i % n], fp.reduce(a[i]));
		prepareRoots(n);
		fftToBitReversed(A);
		for (int i = 0; i < A.length; ++i) A[i] = MathUtils.modPow(A[i], m, mod);
		ifftFromBitReversed(A);
		return Arrays.copyOf(A, n);
	}

	public long[] evenMul(long[] f) {
		if (f.length == 1) return new long[] {f[0] * f[0] % mod};
		if (!isNTTFriendly || f.length + f.length - 1 <= FFT_NAIVE_THRESHOLD) {
			long[] ret = new long[2 * f.length - 1];
			for (int i = 0; i < f.length; i += 2)
				for (int j = i + 2; j < f.length; j += 2) { ret[i + j] = (ret[i + j] + f[i] * f[j] * 2) % mod; }
			for (int i = 0; i < f.length; i += 2) ret[2 * i] = (ret[2 * i] + f[i] * f[i]) % mod;
			for (int i = 1; i < f.length; i += 2)
				for (int j = i + 2; j < f.length; j += 2) { ret[i + j] = (ret[i + j] + mod - f[i] * f[j] * 2 % mod) % mod; }
			for (int i = 1; i < f.length; i += 2) ret[2 * i] = (ret[2 * i] + mod - f[i] * f[i] % mod) % mod;
			return ret;
		}
		int n = 1;
		while (n < f.length + f.length - 1) n *= 2;
		prepareRoots(n);
		prepareRoots(n / 2);
		long[] fft = Arrays.copyOf(f, n);
		for (int i = 0; i < fft.length; i++) fft[i] = fp.reduce(fft[i]);
		fftToBitReversed(fft);
		long[] fft2 = new long[n];
		for (int i = 0; i < fft.length; i++) fft2[i] = fft[(i ^ 1) % n];
		for (int i = 0; 2 * i < fft.length; i++) fft[i] = fft[2 * i] * fft2[2 * i] % mod;
		fft = Arrays.copyOf(fft, n / 2);
		ifftFromBitReversed(fft);
		long[] ret = new long[f.length + f.length - 1];
		for (int i = 0; 2 * i < ret.length; i++) ret[2 * i] = fft[i];
		return ret;
	}

	public long nth(long n, long[] numerator, long[] denominator) {
		if (numerator.length == 0) return 0;
		if (denominator[0] != 1) throw new AssertionError();
		while (n != 0) {
			long[] a = Arrays.copyOf(denominator, denominator.length);
			for (int i = 1; i < a.length; i += 2) if (a[i] != 0) a[i] = mod - a[i];
			numerator = mul(numerator, a);
			denominator = evenMul(denominator);
			long[] num2 = new long[(numerator.length + 1) / 2];
			long[] den2 = new long[(denominator.length + 1) / 2];
			for (int i = (int) (n % 2); i < numerator.length; i += 2) num2[i / 2] = numerator[i];
			for (int i = 0; i < denominator.length; i += 2) den2[i / 2] = denominator[i];
			numerator = num2;
			denominator = den2;
			n /= 2;
		}
		return (numerator[0] + mod) % mod;
	}

	public long[] validShiftedDotProducts(long[] a, long[] b) {
		long[] A = a.clone();
		ArrayUtils.reverse(A);
		if (!isNTTFriendly) {
			long[] full = mul(A, b);
			return Arrays.copyOfRange(full, a.length - 1, b.length);
		}
		int len = 1;
		while (len < b.length) len *= 2;
		A = cyclicmulFFT(A, b, len);
		return Arrays.copyOfRange(A, a.length - 1, b.length);
	}

	public long[] fullShiftedDotProducts(long[] a, long[] b) {
		long[] A = a.clone();
		ArrayUtils.reverse(A);
		return mul(A, b);
	}

	public long[] mulAll(long[][] f) {
		if (f.length == 0) return new long[] { 1 };
		long[][] copy = f.clone();
		Arrays.sort(copy, (a, b) -> Arrays.compare(a, b));
		Queue<long[]> pq = new ArrayDeque<>();
		for (int i = 0; i < copy.length; ) {
			int j = i;
			while (j < copy.length && Arrays.equals(copy[i], copy[j])) j++;
			int count = j - i;
			if (count == 1) {
				pq.add(copy[i]);
			} else {
				pq.add(powFull(copy[i], count));
			}
			i = j;
		}
		while (pq.size() >= 2) {
			pq.add(mul(pq.poll(), pq.poll()));
		}
		return pq.peek();
	}

	/**
	 * 与えられたすべての多項式の積を計算する。
	 *
	 * <p>計算量: $O(N \log^2 N)$ （$N$ は総次数）</p>
	 *
	 * @param f 多項式のリスト
	 * @return すべての多項式の積
	 */
	// 未テスト
	public long[] mulAll(List<long[]> f) {
		return mulAll(f.toArray(new long[0][]));
	}

	public long[][] sumRationals(long[][][] rationals) {
		Queue<long[][]> que = new ArrayDeque<>();
		for (long[][] r : rationals) que.add(r);
		while (que.size() >= 2) {
			long[][] a = que.poll(), b = que.poll();
			que.add(new long[][] {add(mul(a[0], b[1]), mul(a[1], b[0])), mul(a[1], b[1])});
		}
		return que.poll();
	}

	public long[] multipointEval(long[] a, long[] points) {
		int M = points.length;
		int len = 1;
		while (len < M) len *= 2;
		long[][] mods = new long[2 * len][];
		long[][] modded = new long[2 * len][];
		for (int i = 0; i < len; i++)
			mods[i + len] = i < M ? new long[] {(mod - points[i]) % mod, 1} : new long[] {1};
		for (int i = len - 1, e = 0; i >= 1; i--) {
			if (mods[2*i].length == mods[2*i+1].length && mods[2*i].length == 1+(1<<e) && e >= 10 && isNTTFriendly) {
				mods[i] = cyclicmulFFT(mods[2*i], mods[2*i+1], 1<<(e+1));
				mods[i] = Arrays.copyOf(mods[i], 1+(1<<(e+1)));
				mods[i][0]--;
				if (mods[i][0] < 0) mods[i][0] += mod;
				mods[i][1<<(e+1)] = 1;
			} else {
				mods[i] = mul(mods[2*i], mods[2*i+1]);
			}
		}
		modded[1] = mod(a, mods[1]);
		for (int i = 2; i < 2 * len; i++) modded[i] = mod(modded[i/2], mods[i]);
		long[] ret = new long[M];
		for (int i = 0; i < M; i++) if (modded[i+len].length > 0) ret[i] = modded[i+len][0];
		return ret;
	}

	public long[] interpolate(long[] x, long[] y) {
		if (x.length != y.length) throw new AssertionError();
		int n = x.length;
		long[][] a = new long[n][2];
		for (int i = 0; i < n; i++) a[i] = new long[] {(mod - x[i]) % mod, 1};
		long[] f = mulAll(a);
		long[] df = differentiate(f);
		long[] evals = multipointEval(df, x);
		long[][][] b = new long[n][2][2];
		for (int i = 0; i < n; i++) {
			b[i][0] = new long[] {y[i] * MathUtils.modInv(evals[i], mod) % mod};
			b[i][1] = new long[] {(mod - x[i]) % mod, 1};
		}
		long[][] q = sumRationals(b);
		long[] h = div(mul(f, q[0]), q[1]);
		return Arrays.copyOf(h, n);
	}

	/**
	 * Cauchy Interpolation.
	 * Finds a rational function r(x)/t(x) such that t(u_i) != 0 and r(u_i)/t(u_i) = v_i for 0 <= i < n,
	 * deg r < k and deg t <= n - k.
	 * O(N log^2 N)
	 * @param x coordinates u_i
	 * @param y values v_i
	 * @param k degree bound for r
	 * @return {r, t}
	 */
	public long[][] cauchyInterpolation(long[] x, long[] y, int k) {
		int n = x.length;
		long[][] factors = new long[n][2];
		for (int i = 0; i < n; i++) {
			factors[i] = new long[] {subMod(0, fp.reduce(x[i])), 1};
		}
		long[] M = mulAll(factors);
		long[] G = interpolate(x, y);
		EuclidCrossHalfResult res = euclidCrossHalfFast(M, G, k);
		long[] r = res.cur.r;
		long[] t = res.cur.y;
		if (isZero(t) || deg(gcd(r, t)) > 0) return null;
		return new long[][] {r, t};
	}

	public long[] samplePointShift(long[] y, long c, int M) {
		if (M < 0) throw new AssertionError();
		int N = y.length;
		long[] ret = new long[M];
		if (N == 0 || M == 0) return ret;
		if (N == 1) { Arrays.fill(ret, y[0]); return ret; }
		c %= mod;
		if (c < 0) c += mod;
		long[] weights = new long[N];
		for (int i = 0; i < N; i++) {
			long w = y[i] * fp.ifac(i) % mod * fp.ifac(N - 1 - i) % mod;
			if ((N - 1 - i) % 2 == 1 && w != 0) w = mod - w;
			weights[i] = w;
		}
		long[] invs = new long[N + M - 1];
		for (int i = 0; i < invs.length; i++) {
			long v = fp.reduce(c - N + 1 + i);
			invs[i] = MathUtils.modInv(v, mod);
		}
		long[] sums = mul(weights, invs);
		long prod = 1;
		int zeroCount = 0;
		for (int r = -(N - 1); r <= 0; r++) {
			long v = fp.reduce(c + r);
			if (v == 0) zeroCount++;
			else prod = prod * v % mod;
		}
		for (int t = 0; t < M; t++) {
			long xi = fp.reduce(c + t);
			if (xi >= 0 && xi < N) {
				ret[t] = y[(int) xi];
			} else if (zeroCount == 0) {
				ret[t] = prod * sums[N - 1 + t] % mod;
			}
			if (t + 1 < M) {
				long out = fp.reduce(c + t - (N - 1));
				if (out == 0) zeroCount--;
				else prod = prod * MathUtils.modInv(out, mod) % mod;
				long in = fp.reduce(c + t + 1);
				if (in == 0) zeroCount++;
				else prod = prod * in % mod;
			}
		}
		return ret;
	}

	@Override
	public long[] exactDiv(long[] a, long[] b) {
		return div(a, b);
	}

	@Override
	public long[] div(long[] a, long[] b) {
		if (b.length == 1) {
			if (b[0] == 0) throw new ArithmeticException("Division by zero");
			if (b[0] == 1) return a.clone();
			long inv = fp.inv(b[0]);
			long[] res = new long[a.length];
			for (int i = 0; i < a.length; i++) res[i] = a[i] * inv % mod;
			return res;
		}
		int degA = deg(a), degB = deg(b);
		if (isNTTFriendly && degA - degB + 1 > FFT_NAIVE_THRESHOLD && degB >= 10) return divFast(a, b);
		return divNaive(a, b);
	}

	public long[] divFast(long[] a, long[] b) {
		int degA = deg(a), degB = deg(b);
		if (degA < degB) return new long[] {0};
		long[] ra = resize(a), rb = resize(b);
		ArrayUtils.reverse(ra);
		ArrayUtils.reverse(rb);
		rb = Arrays.copyOf(rb, degA + 1);
		long[] q = mul(ra, inv(rb));
		q = Arrays.copyOf(q, degA - degB + 1);
		ArrayUtils.reverse(q);
		return q;
	}

	@Override
	public long[] mod(long[] a, long[] b) {
		int degA = deg(a), degB = deg(b);
		if (isNTTFriendly && degA - degB + 1 > FFT_NAIVE_THRESHOLD) return modFast(a, b);
		return modNaive(a, b);
	}

	public long[] modFast(long[] a, long[] b) {
		long[] q = divFast(a, b);
		return resize(sub(a, mul(b, q)));
	}

	public class HalfGcdResult {
		public long[] p00, p01, p10, p11;
		public HalfGcdResult(long[] p00, long[] p01, long[] p10, long[] p11) {
			this.p00 = p00; this.p01 = p01; this.p10 = p10; this.p11 = p11;
		}
		public long[][] apply(long[] a, long[] b) {
			return new long[][] {
				resize(add(mul(p00, a), mul(p01, b))),
				resize(add(mul(p10, a), mul(p11, b)))
			};
		}
		HalfGcdResult swapColumns() { return new HalfGcdResult(p01, p00, p11, p10); }
	}

	HalfGcdResult identityMatrix() { return new HalfGcdResult(new long[]{1}, new long[]{0}, new long[]{0}, new long[]{1}); }

	HalfGcdResult leftMulEuclideanStep(HalfGcdResult mat, long[] q) {
		return new HalfGcdResult(mat.p10, mat.p11, sub(mat.p00, mul(q, mat.p10)), sub(mat.p01, mul(q, mat.p11)));
	}

	HalfGcdResult multiplyMatrix(HalfGcdResult a, HalfGcdResult b) {
		return new HalfGcdResult(
			resize(add(mul(a.p00, b.p00), mul(a.p01, b.p10))),
			resize(add(mul(a.p00, b.p01), mul(a.p01, b.p11))),
			resize(add(mul(a.p10, b.p00), mul(a.p11, b.p10))),
			resize(add(mul(a.p10, b.p01), mul(a.p11, b.p11)))
		);
	}

	/**
	 * Half-GCDの結果を保持するクラス。
	 * 行列 $M$ と、それに対応するユークリッド商列、および変換後の多項式 $(c, d)^T = M(a, b)^T$ を格納する。
	 */
	public class HalfGcdResultWithQuotients {
		/** 2x2 変換行列の各成分。 */
		public long[] p00, p01, p10, p11;
		/** 変換行列に対応するユークリッド商のリスト（時系列順）。 */
		public List<long[]> quotients;
		/** 変換後の多項式ペア。$(c, d)^T = M(a, b)^T$ を満たす。 */
		public long[] c, d;
		public HalfGcdResultWithQuotients(long[] p00, long[] p01, long[] p10, long[] p11, List<long[]> quotients, long[] c, long[] d) {
			this.p00 = p00; this.p01 = p01; this.p10 = p10; this.p11 = p11;
			this.quotients = quotients;
			this.c = c; this.d = d;
		}
		/**
		 * 変換行列を多項式ペア $(a, b)$ に作用させる。
		 * @param a 多項式 $a$
		 * @param b 多項式 $b$
		 * @return $(c, d)^T = M(a, b)^T$
		 */
		public long[][] apply(long[] a, long[] b) {
			return new long[][] {
				resize(add(mul(p00, a), mul(p01, b))),
				resize(add(mul(p10, a), mul(p11, b)))
			};
		}
		/**
		 * 列を入れ替えた新しい結果を返す。$\deg a < \deg b$ の場合の初期化に使用する。
		 * @return 列を入れ替えた結果
		 */
		HalfGcdResultWithQuotients swapColumns() {
			List<long[]> newQs = new ArrayList<>();
			newQs.add(new long[0]);
			newQs.addAll(quotients);
			return new HalfGcdResultWithQuotients(p01, p00, p11, p10, newQs, c, d);
		}
	}

	HalfGcdResultWithQuotients identityMatrixWithQuotients() {
		return new HalfGcdResultWithQuotients(new long[]{1}, new long[]{0}, new long[]{0}, new long[]{1}, new ArrayList<>(), null, null);
	}

	HalfGcdResultWithQuotients leftMulEuclideanStepWithQuotients(HalfGcdResultWithQuotients mat, long[] q) {
		List<long[]> newQs = new ArrayList<>(mat.quotients);
		newQs.add(q);
		return new HalfGcdResultWithQuotients(mat.p10, mat.p11, sub(mat.p00, mul(q, mat.p10)), sub(mat.p01, mul(q, mat.p11)), newQs, null, null);
	}

	HalfGcdResultWithQuotients multiplyMatrixWithQuotients(HalfGcdResultWithQuotients a, HalfGcdResultWithQuotients b) {
		List<long[]> newQs = new ArrayList<>(b.quotients);
		newQs.addAll(a.quotients);
		return new HalfGcdResultWithQuotients(
			resize(add(mul(a.p00, b.p00), mul(a.p01, b.p10))),
			resize(add(mul(a.p00, b.p01), mul(a.p01, b.p11))),
			resize(add(mul(a.p10, b.p00), mul(a.p11, b.p10))),
			resize(add(mul(a.p10, b.p01), mul(a.p11, b.p11))),
			newQs, null, null
		);
	}

	HalfGcdResult halfGcdNaiveOrdered(long[] a, long[] b) {
		int threshold = deg(a) / 2;
		HalfGcdResult mat = identityMatrix();
		while (deg(b) > threshold) {
			DivModResult dm = divmod(a, b);
			mat = leftMulEuclideanStep(mat, dm.q);
			a = b; b = dm.r;
		}
		return mat;
	}

	public HalfGcdResult halfGcd(long[] a, long[] b) {
		a = resize(a); b = resize(b);
		int degA = deg(a), degB = deg(b);
		if (degB == -1) return identityMatrix();
		if (degA < degB) return halfGcd(b, a).swapColumns();
		if (degB <= degA / 2) return identityMatrix();
		if (degA <= 128) return halfGcdNaiveOrdered(a, b);
		int threshold = degA / 2, shift = (degA + 1) / 2;
		HalfGcdResult mat = halfGcd(divideByX(a, shift), divideByX(b, shift));
		long[][] cd = mat.apply(a, b);
		long[] c = cd[0], d = cd[1];
		if (deg(d) <= threshold) return mat;
		DivModResult dm = divmod(c, d);
		mat = leftMulEuclideanStep(mat, dm.q);
		c = d; d = dm.r;
		if (deg(d) <= threshold) return mat;
		int secondShift = 2 * threshold - deg(c);
		if (secondShift < 0) throw new AssertionError();
		return multiplyMatrix(halfGcd(divideByX(c, secondShift), divideByX(d, secondShift)), mat);
	}

	/**
	 * 愚直な方法で Half-GCD のステップを進め、商列を記録する。
	 * @param a 多項式 $a$
	 * @param b 多項式 $b$
	 * @return Half-GCD 結果
	 */
	HalfGcdResultWithQuotients halfGcdNaiveOrderedWithQuotients(long[] a, long[] b) {
		int threshold = deg(a) / 2;
		HalfGcdResultWithQuotients mat = identityMatrixWithQuotients();
		while (deg(b) > threshold) {
			DivModResult dm = divmod(a, b);
			mat = leftMulEuclideanStepWithQuotients(mat, dm.q);
			a = b; b = dm.r;
		}
		mat.c = a; mat.d = b;
		return mat;
	}

	/**
	 * Half-GCD アルゴリズムを用いて、商列を記録しながら変換行列を計算する。
	 * $\deg a = n, \deg b < n$ に対して、$\deg d \le n/2$ となるまでユークリッド互除法を進める。
	 *
	 * <p>計算量: $O(M(n) \log n)$
	 *
	 * @param a 多項式 $a$
	 * @param b 多項式 $b$
	 * @return Half-GCD 結果（商列を含む）
	 */
	public HalfGcdResultWithQuotients halfGcdWithQuotients(long[] a, long[] b) {
		a = resize(a); b = resize(b);
		int degA = deg(a), degB = deg(b);
		if (degB == -1) {
			HalfGcdResultWithQuotients res = identityMatrixWithQuotients();
			res.c = a; res.d = b;
			return res;
		}
		if (degA < degB) {
			HalfGcdResultWithQuotients res = halfGcdWithQuotients(b, a).swapColumns();
			long[][] cd = res.apply(a, b);
			res.c = cd[0]; res.d = cd[1];
			return res;
		}
		if (degB <= degA / 2) {
			HalfGcdResultWithQuotients res = identityMatrixWithQuotients();
			res.c = a; res.d = b;
			return res;
		}
		if (degA <= 128) return halfGcdNaiveOrderedWithQuotients(a, b);
		int threshold = degA / 2, shift = (degA + 1) / 2;
		HalfGcdResultWithQuotients mat = halfGcdWithQuotients(divideByX(a, shift), divideByX(b, shift));
		long[][] cd = mat.apply(a, b);
		long[] c = cd[0], d = cd[1];
		if (deg(d) <= threshold) {
			mat.c = c; mat.d = d;
			return mat;
		}
		DivModResult dm = divmod(c, d);
		mat = leftMulEuclideanStepWithQuotients(mat, dm.q);
		c = d; d = dm.r;
		if (deg(d) <= threshold) {
			mat.c = c; mat.d = d;
			return mat;
		}
		int secondShift = 2 * threshold - deg(c);
		if (secondShift < 0) throw new AssertionError();
		HalfGcdResultWithQuotients mat2 = halfGcdWithQuotients(divideByX(c, secondShift), divideByX(d, secondShift));
		HalfGcdResultWithQuotients res = multiplyMatrixWithQuotients(mat2, mat);
		long[][] cd2 = mat2.apply(c, d);
		res.c = cd2[0]; res.d = cd2[1];
		return res;
	}

	/**
	 * Returns the quotient sequence produced by the Euclidean algorithm on (a, b) naively.
	 *
	 * <p>計算量: O(N^2)
	 */
	public List<long[]> quotientSequenceNaive(long[] a, long[] b) {
		List<long[]> qs = new ArrayList<>();
		a = resize(a);
		b = resize(b);
		while (deg(b) != -1) {
			DivModResult dm = divmod(a, b);
			qs.add(dm.q);
			a = b;
			b = dm.r;
		}
		return qs;
	}

	/**
	 * Returns the quotient sequence produced by the Euclidean algorithm on (a, b).
	 *
	 * If a = q0 b + r1,
	 *    b = q1 r1 + r2,
	 *    r1 = q2 r2 + r3, ...
	 * then this method returns [q0, q1, q2, ...].
	 *
	 * <p>計算量: O(M(N) \log N)
	 */
	public List<long[]> quotientSequenceFast(long[] a, long[] b) {
		a = resize(a); b = resize(b);
		if (isZero(b)) return new ArrayList<>();
		if (deg(a) < deg(b)) {
			DivModResult dr = divmod(a, b);
			List<long[]> res = new ArrayList<>();
			res.add(dr.q);
			res.addAll(quotientSequenceFast(b, dr.r));
			return res;
		}
		HalfGcdResultWithQuotients h = halfGcdWithQuotients(a, b);
		List<long[]> ans = new ArrayList<>(h.quotients);
		long[] c = h.c;
		long[] d = h.d;
		if (isZero(d)) return ans;
		DivModResult dr = divmod(c, d);
		ans.add(dr.q);
		ans.addAll(quotientSequenceFast(d, dr.r));
		return ans;
	}

	@Override
	public long[] gcd(long[] a, long[] b) {
		int degA = deg(a);
		int degB = deg(b);
		if (degA == -1) return monic(b);
		if (degB == -1) return monic(a);
		if (degA == 0 || degB == 0) return new long[]{1};

		a = resize(a); b = resize(b);
		if (degA < degB) { long[] t = a; a = b; b = t; }
		while (deg(b) != -1) {
			if (!isNTTFriendly || Math.max(deg(a), deg(b)) <= 3072) return gcdNaive(a, b);
			HalfGcdResult mat = halfGcd(a, b);
			long[][] cd = mat.apply(a, b);
			a = cd[0]; b = cd[1];
			if (deg(b) == -1) break;
			DivModResult dm = divmod(a, b);
			a = b; b = dm.r;
			if (deg(a) < deg(b)) { long[] t = a; a = b; b = t; }
		}
		return monic(a);
	}


	/**
	 * 拡張ユークリッド互除法により、$f(x)s(x) + g(x)t(x) = \text{gcd}(f, g)$ を満たす $s, t, \text{gcd}(f, g)$ を計算する。
	 *
	 * <p>数学的仕様:
	 * <ul>
	 *   <li>$f(x) \cdot \text{result.x}() + g(x) \cdot \text{result.y}() = \text{result.gcd}()$</li>
	 *   <li>$\text{result.gcd}()$ はモニック（最高次係数が1）、または $0$。</li>
	 * </ul>
	 *
	 * <p>事前条件:
	 * <ul>
	 *   <li>{@code f != null && g != null}</li>
	 * </ul>
	 *
	 * <p>事後条件:
	 * <ul>
	 *   <li>戻り値は上記数学的仕様を満たす。</li>
	 * </ul>
	 *
	 * <p>副作用:
	 * <ul>
	 *   <li>なし。</li>
	 * </ul>
	 *
	 * <p>計算量:
	 * <ul>
	 *   <li>$n = \max(\deg f, \deg g)$ としたとき、$O(n \log^2 n)$。</li>
	 *   <li>実装詳細: 次数が 3072 以下の場合は $O(n^2)$ の素朴な互除法、それ以外は Half-GCD 法を用いる。</li>
	 * </ul>
	 *
	 * <p>破壊的変更:
	 * <ul>
	 *   <li>引数 {@code f, g} は変更されない。</li>
	 * </ul>
	 *
	 * <p>参照共有・所有権:
	 * <ul>
	 *   <li>戻り値の {@link EuclideanDomainStrategy.ExtGCDResult} に含まれる各配列は新しく生成されたものであり、呼び出し側が所有権を持つ。</li>
	 * </ul>
	 *
	 * <p>例外・未定義条件:
	 * <ul>
	 *   <li>$f, g$ がともに $0$ の場合、{@code x, y, gcd} はすべて $0$（空配列または {@code {0}}）となる。</li>
	 * </ul>
	 *
	 * @param f 多項式 $f(x)$ の係数配列
	 * @param g 多項式 $g(x)$ の係数配列
	 * @return $s(x), t(x), \text{gcd}(f, g)$ を格納した結果オブジェクト
	 */
	private record HalfGcdWithResultant(HalfGcdResult mat, long factor) {}

	/**
	 * Euclid列が次数境界 $\deg(a)/2$ を初めて下回る場所を、終結式の因子と共にHalf-GCDで探す。
	 * 終結式の性質 $\text{Res}(f, g) = (-1)^{nm} \text{lc}(g)^{n-k} \text{Res}(g, r)$ に基づき、
	 * 余剰の計算ステップごとに発生する係数と符号の累積値を {@code factor} として保持する。
	 *
	 * <p>計算量: $O(M(N) \log N)$
	 *
	 * @param a 多項式 a
	 * @param b 多項式 b
	 * @param offset 次数のオフセット（剰余計算時の符号決定に使用）
	 * @return 変形行列と蓄積された終結式因子のペア
	 */
	private HalfGcdWithResultant resultantHalfGcd(long[] a, long[] b, int offset) {
		a = resize(a); b = resize(b);
		int degA = deg(a), degB = deg(b);
		if (degA < degB || degB == -1 || degB <= degA / 2) return new HalfGcdWithResultant(identityMatrix(), 1);

		int threshold = degA / 2;
		int shift = (degA + 1) / 2;

		HalfGcdWithResultant hgr1 = resultantHalfGcd(divideByX(a, shift), divideByX(b, shift), offset + shift);
		long[][] cd = hgr1.mat.apply(a, b);
		long[] c = cd[0], d = cd[1];
		long factor = hgr1.factor;
		HalfGcdResult mat = hgr1.mat;

		if (deg(d) <= threshold) return new HalfGcdWithResultant(mat, factor);

		DivModResult dm = divmod(c, d);
		int n = deg(c), m = deg(d), k = deg(dm.r);
		long stepFactor = fp.pow(d[m], n - k);
		if ((n + offset) % 2 == 1 && (m + offset) % 2 == 1) {
			stepFactor = stepFactor == 0 ? 0 : mod - stepFactor;
		}
		factor = factor * stepFactor % mod;
		mat = leftMulEuclideanStep(mat, dm.q);
		c = d; d = dm.r;

		if (deg(d) <= threshold) return new HalfGcdWithResultant(mat, factor);

		int secondShift = 2 * threshold - deg(c);
		HalfGcdWithResultant hgr2 = resultantHalfGcd(divideByX(c, secondShift), divideByX(d, secondShift), offset + secondShift);
		factor = factor * hgr2.factor % mod;
		mat = multiplyMatrix(hgr2.mat, mat);

		return new HalfGcdWithResultant(mat, factor);
	}

	/**
	 * 2つの多項式 $f(x), g(x)$ の終結式 (Resultant) $\text{Res}(f, g)$ を計算する。
	 * $\text{Res}(f, g) = a_n^m b_m^n \prod_{i, j} (\alpha_i - \beta_j) = a_n^m \prod_{i=1}^n g(\alpha_i)$
	 * ($a_n, b_m$ は主係数、$\alpha_i, \beta_j$ は根) で定義される。
	 * 計算には $\text{Res}(f, g) = (-1)^{nm} \text{lc}(g)^{n-k} \text{Res}(g, r)$ ($r = f \pmod g$) の関係を用いる。
	 *
	 * <p>計算量: $O(N \log^2 N)$
	 *
	 * @param f 多項式 $f(x)$
	 * @param g 多項式 $g(x)$
	 * @return 終結式 $\text{Res}(f, g)$
	 */
	public long resultant(long[] f, long[] g) {
		f = resize(f); g = resize(g);
		long res = 1;
		while (true) {
			int n = deg(f), m = deg(g);
			if (n == -1 || m == -1) return 0;
			if (m == 0) return res * fp.pow(g[0], n) % mod;

			HalfGcdWithResultant hgr = resultantHalfGcd(f, g, 0);
			res = res * hgr.factor % mod;
			long[][] cd = hgr.mat.apply(f, g);
			f = cd[0]; g = cd[1];

			n = deg(f); m = deg(g);
			if (m == -1) return 0;
			if (m == 0) continue;

			DivModResult dm = divmod(f, g);
			int k = deg(dm.r);
			long stepFactor = fp.pow(g[m], n - k);
			if (n % 2 == 1 && m % 2 == 1) stepFactor = stepFactor == 0 ? 0 : mod - stepFactor;
			res = res * stepFactor % mod;
			f = g; g = dm.r;
		}
	}

	@Override
	public EuclideanDomainStrategy.ExtGCDResult<long[]> extgcd(long[] f, long[] g) {
		f = resize(f); g = resize(g);
		long[] a = f, b = g;
		long[] x0 = new long[]{1}, y0 = new long[]{0}, x1 = new long[]{0}, y1 = new long[]{1};
		if (Math.max(deg(a), deg(b)) <= 3072) {
			while (deg(b) != -1) {
				DivModResult dm = divmod(a, b);
				long[] nx = sub(x0, mul(dm.q, x1)), ny = sub(y0, mul(dm.q, y1));
				a = b; b = dm.r; x0 = x1; y0 = y1; x1 = resize(nx); y1 = resize(ny);
			}
		} else {
			if (deg(a) < deg(b)) {
				long[] t = a; a = b; b = t; t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t;
			}
			while (deg(b) != -1) {
				HalfGcdResult mat = halfGcd(a, b);
				long[][] cd = mat.apply(a, b);
				long[] nx0 = resize(add(mul(mat.p00, x0), mul(mat.p01, x1)));
				long[] ny0 = resize(add(mul(mat.p00, y0), mul(mat.p01, y1)));
				long[] nx1 = resize(add(mul(mat.p10, x0), mul(mat.p11, x1)));
				long[] ny1 = resize(add(mul(mat.p10, y0), mul(mat.p11, y1)));
				a = cd[0]; b = cd[1]; x0 = nx0; y0 = ny0; x1 = nx1; y1 = ny1;
				if (deg(b) == -1) break;
				DivModResult dm = divmod(a, b);
				nx1 = resize(sub(x0, mul(dm.q, x1)));
				ny1 = resize(sub(y0, mul(dm.q, y1)));
				a = b; b = dm.r; x0 = x1; y0 = y1; x1 = nx1; y1 = ny1;
				if (deg(a) < deg(b)) {
					long[] t = a; a = b; b = t; t = x0; x0 = x1; x1 = t; t = y0; y0 = y1; y1 = t;
				}
			}
		}
		a = resize(a);
		int d = deg(a);
		if (d == -1) return new EuclideanDomainStrategy.ExtGCDResult<>(new long[]{0}, new long[]{0}, new long[]{0});
		long inv = MathUtils.modInv(a[d], mod);
		return new EuclideanDomainStrategy.ExtGCDResult<>(resize(mul(x0, inv)), resize(mul(y0, inv)), resize(mul(a, inv)));
	}

	/**
	 * A(x) D(x) = B(x) mod C(x) を満たす多項式 D(x) を求める。
	 * 解が存在する場合は deg(D) < deg(C / gcd(A, C)) を満たす一意な解 D(x) を返す。
	 * ただし、deg(C / gcd(A, C)) = 0 の場合は解は任意だから、空配列（ゼロ多項式）を返す。
	 * 解が存在しない場合は null を返す。
	 *
	 * <p>計算量: N = max(deg A, deg B, deg C) としたとき、O(N log^2 N)。
	 *
	 * @param a 多項式 A(x) の係数配列
	 * @param b 多項式 B(x) の係数配列
	 * @param c 多項式 C(x) の係数配列
	 * @return D(x) の係数配列。解が存在しない場合は null
	 * @throws ArithmeticException C(x) がゼロ多項式の場合
	 */
	// 未テスト
	public long[] solveLinearCongruence(long[] a, long[] b, long[] c) {
		a = resize(a);
		b = resize(b);
		c = resize(c);

		if (isZero(c)) {
			throw new ArithmeticException("modulo by zero polynomial");
		}

		EuclideanDomainStrategy.ExtGCDResult<long[]> res = extgcd(a, c);
		long[] g = res.gcd();
		DivModResult dmB = divmod(b, g);
		if (!isZero(dmB.r)) {
			return null;
		}
		long[] q = dmB.q;
		long[] modulus = div(c, g);
		if (deg(modulus) == 0) {
			return new long[0];
		}
		long[] particular = mul(q, res.x());
		return mod(particular, modulus);
	}

	public static class SquareFreeFactor {
		public long[] factor;
		public int multiplicity;
		public SquareFreeFactor(long[] factor, int multiplicity) { this.factor = factor; this.multiplicity = multiplicity; }
	}

	/**
	 * 多項式 f が square-free（重因子を持たない）か判定する。
	 * deg(gcd(f, df/dx)) == 0 かどうかで判定する。
	 *
	 * @param inputf 多項式 f
	 * @return f が square-free なら true
	 *
	 * <p>計算量: O(M(deg f) log(deg f))
	 * <p>未テスト
	 */
	public boolean isSquareFree(long[] inputf) {
		long[] f = resize(inputf);
		int d = deg(f);
		if (d == -1) return false;
		if (d == 0) return true;
		long[] df = differentiate(f);
		if (deg(df) == -1) return false;
		long[] g = gcd(f, df);
		return deg(g) == 0;
	}

	/**
	 * 有理関数 f/g のエルミート簡約。
	 * f(x)/g(x) = q(x) + (c(x)/d(x))' + \sum a_i(x)/g_i(x)
	 * ここで g_i は square-free かつ互いに素。
	 *
	 * <p>計算量: O(M(n) \log n), n = \max(\deg f, \deg g)
	 * <p>未テスト
	 *
	 * @param f 分子
	 * @param g 分母
	 * @return エルミート簡約の結果
	 */
	public HermiteResult hermiteReduction(long[] f, long[] g) {
		g = resize(g);
		int degG = deg(g);
		if (degG == -1) throw new ArithmeticException("Division by zero polynomial");
		long invG = fp.inv(g[degG]);
		g = monic(g);
		f = mul(f, invG);

		// 多項式部分 q と proper な有理関数部分 h/g に分離
		DivModResult dm = divmod(f, g);
		long[] q = dm.q;
		long[] h = dm.r;

		if (deg(h) == -1) {
			return new HermiteResult(q, new long[0], new long[] { 1 }, new SquareFreeTerm[0]);
		}

		// 分母の square-free 分解
		SquareFreeFactor[] sff = factorSquareFree(g);
		Factor[] factors = new Factor[sff.length];
		for (int i = 0; i < sff.length; i++) {
			factors[i] = new Factor(sff[i].factor, sff[i].multiplicity);
		}

		// 部分分数分解: h/g = \sum H_{ij} / g_i^j
		var pfd = PartialFractionDecomposition.decompose(h, factors, this);
		q = add(q, pfd.polynomialPart);

		List<long[][]> rationalParts = new ArrayList<>();
		List<SquareFreeTerm> logParts = new ArrayList<>();

		for (var term : pfd.terms) {
			long[] G = term.factor;
			long[][] hs = term.numerators;
			int i = hs.length - 1;

			// 重複極の指数を下げる
			long[] Gp = differentiate(G);
			var eg = extgcd(G, Gp); // uG + vGp = 1

			long[] combinedNum = zero();
			long[] H = zero();
			for (int j = i; j >= 2; j--) {
				if (hs[j] != null) H = add(H, hs[j]);
				if (deg(H) == -1) continue;

				// sG + tGp = H, deg t < deg G となる s, t を求める
				// t = (H * v) mod G
				long[] t = mod(mul(H, eg.y()), G);
				// s = (H - tGp) / G
				long[] s = div(sub(H, mul(t, Gp)), G);

				// 有理関数部分の項: -t / ((j-1) * G^(j-1))
				long invJm1 = fp.inv(j - 1);
				long[] termNum = mul(t, mod - invJm1);
				// 通分して combinedNum / G^(i-1) に加算
				// 現在の項 termNum / G^(j-1) = (termNum * G^(i-j)) / G^(i-1)
				combinedNum = add(combinedNum, mul(termNum, powFull(G, i - j)));

				// 次のループの H = s + t' / (j-1)
				H = add(s, mul(differentiate(t), invJm1));
			}
			if (i >= 1 && hs[1] != null) H = add(H, hs[1]);

			if (deg(combinedNum) != -1) {
				rationalParts.add(new long[][] { combinedNum, powFull(G, i - 1) });
			}
			if (deg(H) != -1) {
				logParts.add(new SquareFreeTerm(H, G));
			}
		}

		long[] c, d;
		if (rationalParts.isEmpty()) {
			c = zero();
			d = one();
		} else {
			long[][][] rpArray = new long[rationalParts.size()][2][];
			for (int i = 0; i < rationalParts.size(); i++) rpArray[i] = rationalParts.get(i);
			long[][] combined = sumRationals(rpArray);
			c = combined[0];
			d = combined[1];
		}

		return new HermiteResult(q, c, d, logParts.toArray(new SquareFreeTerm[0]));
	}

	public SquareFreeFactor[] factorSquareFree(long[] inputf) {
		long[] f = resize(inputf);
		if (deg(f) <= 0) return new SquareFreeFactor[0];
		long[] df = differentiate(f);
		long[] g = gcd(f, df);
		if (deg(g) <= 0) return new SquareFreeFactor[] {new SquareFreeFactor(monic(f), 1)};
		long[] w = divFast(f, g);
		long[] c = divFast(df, g);
		long[] d = sub(c, differentiate(w));
		ArrayList<SquareFreeFactor> ret = new ArrayList<>();
		for (int multiplicity = 1; deg(w) > 0; multiplicity++) {
			long[] a = gcd(w, d);
			if (deg(a) > 0) ret.add(new SquareFreeFactor(a, multiplicity));
			w = divFast(w, a);
			c = divFast(d, a);
			d = sub(c, differentiate(w));
		}
		return ret.toArray(new SquareFreeFactor[0]);
	}

	/**
	 * 多項式 f の GFF (Greatest Factorial Factorization) を計算する。
	 * f = monic(f) = \prod_{i=1}^k [g_i]_i = \prod_{i=1}^k g_i(x)g_i(x-1)...g_i(x-i+1)
	 * と分解される monic な多項式の配列 g を返す。
	 * ここで [g]_i は下降階乗冪多項式。
	 * Gerhard の Yun 型アルゴリズムを用いる。
	 *
	 * <p>計算量: O(k * M(deg f) log(deg f))
	 *
	 * @param f 多項式
	 * @return g_i の配列。g[i] が g_{i+1} に対応する。
	 */
	public long[][] gff(long[] f) {
		f = monic(f);
		if (deg(f) <= 0) return new long[0][];

		long[] v = div(f, gcd(taylorShift(f, mod - 1), f));
		long[] w = div(f, gcd(f, taylorShift(f, 1)));

		List<long[]> g = new ArrayList<>();
		while (deg(v) > 0) {
			long[] gi = gcd(v, w);
			g.add(gi);
			v = div(v, gi);
			w = div(taylorShift(w, 1), taylorShift(gi, 1));
		}
		return g.toArray(new long[0][]);
	}

	/**
	 * 線形漸化式を満たす数列の第 $k$ 項 $a_k \pmod{\text{mod}}$ を計算する。
	 *
	 * <p>数列 $a_n$ は以下の線形漸化式を満たす：
	 * $$a_n = \sum_{i=1}^d c_i a_{n-i} \pmod{\text{mod}} \quad (n \ge d)$$
	 * ここで $d$ は漸化式の次数であり、初期項 $a_0, a_1, \dots, a_{d-1}$ と漸化式係数 $c_1, c_2, \dots, c_d$ が与えられる。</p>
	 *
	 * <p>Bostan-Mori アルゴリズムを用いて、有理母関数 $\frac{P(x)}{Q(x)}$ の $x^k$ の係数を効率的に抽出する。</p>
	 *
	 * <p>計算量: $O(d \log d \log k)$</p>
	 *
	 * @param initial 初期項の配列 $a = [a_0, a_1, \dots, a_{d-1}]$ (長さ $d$)
	 * @param recurrence 漸化式係数の配列 $c = [c_1, c_2, \dots, c_d]$ (長さ $d$)
	 * @param k 求めたい項のインデックス $k \ge 0$
	 * @return 第 $k$ 項 $a_k \pmod{\text{mod}}$ ($0 \le a_k < \text{mod}$)
	 * @throws AssertionError {@code initial.length != recurrence.length} の場合
	 */
	public long kthTermOfLinearlyRecurrentSequence(long[] initial, long[] recurrence, long k) {
		int d = recurrence.length;
		if (initial.length != d) throw new AssertionError();
		long[] denominator = new long[d + 1];
		denominator[0] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			denominator[i + 1] = (mod - c) % mod;
		}
		long[] numerator = Arrays.copyOf(mul(initial, denominator), d);
		return nth(k, numerator, denominator);
	}

	// 未テスト
	/**
	 * 指定された一部の項を事前に定めた値（sparse assign）に置き換えた線形漸化式数列の、第 $k$ 項を計算する。
	 *
	 * <p>基本的には $a_n = \sum_{i=1}^d c_i a_{n-i} \pmod{\text{mod}}$ ($n \ge d$) であるが、
	 * 指定されたインデックス $j \in \text{indices}$ の項については漸化式を適用せず、対応する $v \in \text{values}$ を直接代入する。
	 *
	 * <p>計算量: $O(M d \log d \log k)$。ただし $M$ は指定された割当（assign）のうち $\le k$ であるものの個数。</p>
	 *
	 * @param initial 初期項 $a = [a_0, a_1, \dots, a_{d-1}]$ (長さ $d$)
	 * @param recurrence 漸化式係数 $c = [c_1, c_2, \dots, c_d]$ (長さ $d$)
	 * @param indices 置き換える項のインデックス配列
	 * @param values 置き換える値の配列
	 * @param k 求めたい項のインデックス $k \ge 0$
	 * @return 第 $k$ 項の値 $a_k \pmod{\text{mod}}$ ($0 \le a_k < \text{mod}$)
	 * @throws AssertionError 配列の長さが不整合、または $k < 0$ などの場合
	 */
	public long kthTermOfLinearlyRecurrentSequenceWithSparseAssign(long[] initial, long[] recurrence, long[] indices, long[] values, long k) {
		if (k < 0) throw new AssertionError();
		int d = recurrence.length;
		if (initial.length != d || d == 0) throw new AssertionError();
		if (indices == null || values == null || indices.length != values.length) throw new AssertionError();
		for (long idx : indices) {
			if (idx < 0) throw new AssertionError();
		}
		ArrayUtils.sort(indices, values);
		long[] state = Arrays.copyOf(initial, d);
		for (int i = 0; i < d; i++) {
			state[i] = state[i] % mod;
			if (state[i] < 0) state[i] += mod;
		}

		long curr = 0; // Represents the start index of the current window state [curr, curr + d)
		for (int i = 0; i < indices.length; i++) {
			long j = indices[i];
			long v = fp.reduce(values[i]);
			if (j > k) {
				break;
			}
			if (j < curr + d) {
				state[(int) (j - curr)] = v;
			} else {
				// Transition window to [j - d + 1, j + 1)
				long startIdx = j - d + 1;
				long[] nextState = consecutiveTermsOfLinearlyRecurrentSequence(state, recurrence, startIdx - curr, d);
				for (int l = 0; l < d; l++) {
					state[l] = nextState[l];
				}
				curr = startIdx;
				state[d - 1] = v;
			}
		}

		if (k < curr + d) {
			return state[(int) (k - curr)];
		} else {
			return kthTermOfLinearlyRecurrentSequence(state, recurrence, k - curr);
		}
	}

	// 未テスト
	/**
	 * 遷移ウェイト数列 $a_i$ ($i \ge 1$) が有理多項式 $P(x)/Q(x)$ の係数として与えられ、
	 * 漸化式 $c_n = \sum_{i \ge 1} c_{n-i} a_i$ を満たし、初期項が有限配列 `initial`（長さ $I$）で与えられ、
	 * かつ指定されたいくつかの $n \in \text{indices}$ で $c_n = 0$ に強制的に上書きされる数列 $c_n$ の第 $k$ 項 $c_k \pmod{\text{mod}}$ を計算する。
	 *
	 * <p>関係式 $C(x) (1 - A(x)) = U(x)$ に基づき、Bostan-Moriを組み合わせた高速なウィンドウスライドアルゴリズムを用いて $c_k$ を求める。</p>
	 *
	 * <p>計算量: $O(M (d \log^2 d + d \log d \log(\text{gap})) + I \log I)$。ただし $d = \max(\deg P, \deg Q)$ であり、
	 * $M$ は指定された割当（assign）のうち $\le k$ であるものの個数。</p>
	 *
	 * @param P 分子多項式 $P(x)$ の係数配列。ただし $P(0) \equiv 0 \pmod{\text{mod}}$ である必要がある
	 * @param Q 分母多項式 $Q(x)$ の係数配列。ただし $Q(0) \not\equiv 0 \pmod{\text{mod}}$ である必要がある
	 * @param initial 有限の初期項の配列
	 * @param indices $c_n = 0$ に強制上書きされるインデックス配列
	 * @param k 求めたい項のインデックス $k \ge 0$
	 * @return 第 $k$ 項の値 $c_k \pmod{\text{mod}}$ ($0 \le c_k < \text{mod}$)
	 * @throws IllegalArgumentException $P(0) \not\equiv 0 \pmod{\text{mod}}$ の場合
	 * @throws ArithmeticException $Q(x) = 0$ または $Q(0) \equiv 0 \pmod{\text{mod}}$ の場合
	 * @throws AssertionError 配列の不整合、または $k < 0$ などの場合
	 */
	public long kthTermOfCWithSparseZero(long[] P, long[] Q, long[] initial, long[] indices, long k) {
		if (indices == null) throw new AssertionError();
		long[] values = new long[indices.length];
		return kthTermOfCWithSparseAssign(P, Q, initial, indices, values, k);
	}

	// 未テスト
	/**
	 * 遷移ウェイト数列 $a_i$ ($i \ge 1$) が有理多項式 $P(x)/Q(x)$ の係数として与えられ、
	 * 漸化式 $c_n = \sum_{i \ge 1} c_{n-i} a_i$ を満たし、初期項が有限配列 `initial`（長さ $I$）で与えられ、
	 * かつ指定されたいくつかの $n \in \text{indices}$ で $c_n = \text{values}[j]$ に強制的に上書きされる数列 $c_n$ の第 $k$ 項 $c_k \pmod{\text{mod}}$ を計算する。
	 *
	 * <p>関係式 $C(x) (1 - A(x)) = U(x)$ に基づき、Bostan-Moriを組み合わせた高速なウィンドウスライドアルゴリズムを用いて $c_k$ を求める。</p>
	 *
	 * <p>計算量: $O(M (d \log^2 d + d \log d \log(\text{gap})) + I \log I)$。ただし $d = \max(\deg P, \deg Q)$ であり、
	 * $M$ は指定された割当（assign）のうち $\le k$ であるものの個数。</p>
	 *
	 * @param P 分子多項式 $P(x)$ の係数配列。ただし $P(0) \equiv 0 \pmod{\text{mod}}$ である必要がある
	 * @param Q 分母多項式 $Q(x)$ の係数配列。ただし $Q(0) \not\equiv 0 \pmod{\text{mod}}$ である必要がある
	 * @param initial 有限の初期項の配列
	 * @param indices 強制上書きされるインデックス配列
	 * @param values 強制上書きされる値の配列
	 * @param k 求めたい項のインデックス $k \ge 0$
	 * @return 第 $k$ 項の値 $c_k \pmod{\text{mod}}$ ($0 \le c_k < \text{mod}$)
	 * @throws IllegalArgumentException $P(0) \not\equiv 0 \pmod{\text{mod}}$ の場合
	 * @throws ArithmeticException $Q(x) = 0$ または $Q(0) \equiv 0 \pmod{\text{mod}}$ の場合
	 * @throws AssertionError 配列の不整合、または $k < 0$ などの場合
	 */
	public long kthTermOfCWithSparseAssign(long[] P, long[] Q, long[] initial, long[] indices, long[] values, long k) {
		if (k < 0) throw new AssertionError();
		if (P == null || Q == null || initial == null || indices == null || values == null) throw new AssertionError();
		if (indices.length != values.length) throw new AssertionError();
		int I = initial.length;
		if (I == 0) throw new AssertionError();
		for (long idx : indices) {
			if (idx < 0) throw new AssertionError();
		}
		if (P.length > 0 && fp.reduce(P[0]) != 0) {
			throw new IllegalArgumentException("a_0 must be zero");
		}

		if (Q.length == 0 || deg(Q) < 0) throw new ArithmeticException("Division by zero");
		long q0 = fp.reduce(Q[0]);
		if (q0 != 1) {
			throw new IllegalArgumentException("Q[0] must be congruent to 1 modulo mod");
		}

		long[] reducedP = Arrays.stream(P).map(fp::reduce).toArray();
		long[] reducedQ = Arrays.stream(Q).map(fp::reduce).toArray();

		final long[] sortedIndices = indices.clone();
		final long[] sortedValues = values.clone();
		ArrayUtils.sort(sortedIndices, sortedValues);

		long[] newInitial = initial.clone();
		int firstNonInitialAssign = 0;
		while (firstNonInitialAssign < sortedIndices.length && sortedIndices[firstNonInitialAssign] < I) {
			newInitial[(int) sortedIndices[firstNonInitialAssign]] = sortedValues[firstNonInitialAssign];
			firstNonInitialAssign++;
		}
		final int finalFirstNonInitialAssign = firstNonInitialAssign;

		// C(x) = U(x) / (1 - A(x))
		// C(x) = U(x) / (1 - P(x) / Q(x))
		// C(x) (Q(x) - P(x)) = U(x) Q(x)
		
		// R(x) = Q(x) - P(x) と置くと
		// C(x) R(x) = U(x) Q(x)
		
		long[] R = sub(reducedQ, reducedP);
		
		int d = Math.max(1, Math.max(deg(reducedQ), deg(R)));

		// recurrence coefficients gamma_i for 1 <= i <= d
		long[] recurrence = new long[d];
		for (int i = 1; i <= d; i++) {
			long qVal = (i < R.length) ? R[i] : 0;
			recurrence[i - 1] = (mod - qVal) % mod;
		}

		// Since indices is assumed to be strictly monotonically increasing (no duplicates, sorted)


		// C(x) R(x) = U(x) Q(x)
		// U = CR / Q
		long[] U = mul(mul(newInitial, R), inv(Arrays.copyOf(reducedQ, I)));
		U=Arrays.copyOf(U, I);

		// H = U * Q
		long[] H = mul(U, reducedQ);
		
		// CR = H

		// G(x) = (Q / R) mod x^(2d)
		long[] G = Arrays.copyOf(mul(reducedQ, inv(Arrays.copyOf(R, 2 * d))), 2 * d);

		// 初期項（インデックス $0 \le i < I$）および上書き指定されたインデックス（`indices`）のうち、
		// $k$ 以下のものを昇順にマージしたリスト。
		// これらはすべて、値が強制決定（上書き）される位置（override/force対象）を表す。
		java.util.List<Long> allOverridesList = new java.util.ArrayList<>();
		for (long iVal = 0; iVal < I && iVal <= k; iVal++) allOverridesList.add(iVal);
		for (int i = finalFirstNonInitialAssign; i < sortedIndices.length && sortedIndices[i] <= k; i++) allOverridesList.add(sortedIndices[i]);

		long[] invR_d = inv(Arrays.copyOf(R, d));

		class SlideHelper {
			long curr;
			long[] state;
			long maxProcessed = -1;
			int indicesPtr = finalFirstNonInitialAssign;
			int overridePtr = 0;

			// 過去の上書き操作（補正値 $e$）が将来の各インデックスへ与える強制寄与（フィードバック） $H \pmod{\text{mod}}$ を保持する両端キュー。
			// インデックス `curr` 以降の値を保持し、`forceDeque.get(idx)` が絶対インデックス `curr + idx` の値を表す。
			final library.util.collections.LongDeque forceDeque = new library.util.collections.LongDeque();

			/**
			 * インデックス $n$ における現在の強制寄与の値 $H(n) \pmod{\text{mod}}$ を取得する。
			 *
			 * <p>計算量: $O(1)$</p>
			 *
			 * @param n 対象インデックス
			 * @return $H(n) \pmod{\text{mod}}$
			 */
			long getForce(long n) {
				if (n < curr) return 0L;
				long rel = n - curr;
				if (rel >= forceDeque.size()) {
					return 0L;
				}
				return forceDeque.get((int) rel);
			}

			/**
			 * インデックス $n$ における現在の強制寄与の値 $H(n) \pmod{\text{mod}}$ を更新（加算）する。
			 *
			 * <p>計算量: 償却 $O(1)$</p>
			 *
			 * @param n 対象インデックス
			 * @param val 加算する値
			 */
			void addForce(long n, long val) {
				if (n < curr) return;
				long rel = n - curr;
				while (rel >= forceDeque.size()) {
					forceDeque.addLast(0L);
				}
				forceDeque.set((int) rel, (forceDeque.get((int) rel) + val) % mod);
			}

			/**
			 * ウィンドウ開始位置 `curr` を `targetCurr` まで進め、それより古い強制寄与のデータを deque から破棄する。
			 *
			 * <p>計算量: $O(\text{targetCurr} - \text{curr})$</p>
			 *
			 * @param targetCurr 新しいウィンドウ開始位置
			 */
			void shiftDeque(long targetCurr) {
				if (targetCurr < curr) throw new AssertionError();
				if (targetCurr == curr) return;
				long diff = targetCurr - curr;
				for (long i = 0; i < diff && !forceDeque.isEmpty(); i++) {
					forceDeque.pollFirst();
				}
				curr = targetCurr;
			}

			/**
			 * 指定された初期状態、処理済みの最大インデックス、および開始位置を用いて SlideHelper を初期化する。
			 *
			 * <p>計算量: $O(d)$</p>
			 *
			 * @param initialState サイズ $d$ の初期状態配列
			 * @param lastProc すでに処理された最大の上書きインデックス
			 * @param startCurr ウィンドウの開始位置（通常は $-d$）
			 */
			SlideHelper(long[] initialState, long lastProc, long startCurr) {
				this.state = initialState.clone();
				this.maxProcessed = lastProc;
				this.curr = startCurr;
			}

			/**
			 * [0, startN)が処理済み。
			 * {@code state} には現在ブロック {@code [startN, startN+d)} に属する強制代入はまだ反映されていない。
			 * {@code state = c[startN .. startN + d)} まで処理する。
			 *
			 * <p>求める項 {@code k} がこのブロック内に含まれる場合は、
			 * 強制代入まで反映した正しい {@code c_k} を返す。
			 * そうでなければ状態を次のブロックへ進めて {@code -1} を返す。</p>
			 *
			 * <p>計算量: {@code O(d log^2 d)}</p>
			 *
			 * @param startN 現在処理するブロックの先頭インデックス
			 * @return {@code k ∈ [startN, startN + d)} のときは {@code c_k}、
			 *         それ以外の場合は {@code -1}
			 */	
			long processBlock(long startN) {
				// Compute cHomogeneous of length d starting from startN + d using consecutiveTermsOfLinearlyRecurrentSequence
				long[] cHomogeneous = consecutiveTermsOfLinearlyRecurrentSequence(state, recurrence, d, d);
				
				// Setup H_prime of size d for n in [startN + d, startN + 2d - 1]
				long[] H_prime = new long[d];
				for (int i = 0; i < d; i++) {
					H_prime[i] = getForce(startN + d + i);
				}
				
				//　C*R = U*Q
				// U に e x^t を足すと C は (e x^t * Q / R) = e x^t Gだけ増える。 
				
				//　過去の代入の影響を計算。
				// すでに H = (ΔH)Q で Q が掛かってるので R で割るだけ。
				// C_force_prime = H_prime * invR_d mod x^d
				long[] C_force_prime = Arrays.copyOf(mul(H_prime, invR_d), d);

				// Compute cTemp = cHomogeneous + C_force
				long[] cTemp = new long[2 * d];
				for (int i = 0; i < d; i++) {
					cTemp[i] = state[i];
				}
				for (int i = 0; i < d; i++) {
					cTemp[d + i] = (cHomogeneous[i] + C_force_prime[i]) % mod;
				}

				// Setup target and isOverride arrays of size d (since overrides are within startN to startN + d - 1)
				boolean[] isOverride = new boolean[d];
				long[] target = new long[d];
				long maxOverrideInBlock = -1;
				while (overridePtr < allOverridesList.size()) {
					long n = allOverridesList.get(overridePtr);
					if (n >= startN + d) break;
					int offset = (int) (n - startN);
					isOverride[offset] = true;
					while (indicesPtr < sortedIndices.length && sortedIndices[indicesPtr] < n) indicesPtr++;
					target[offset] = indicesPtr < sortedIndices.length && sortedIndices[indicesPtr] == n ? fp.reduce(sortedValues[indicesPtr]) : fp.reduce(newInitial[(int) n]);
					maxOverrideInBlock = n;
					overridePtr++;
				}

				long[] e = new long[d];
				if (maxOverrideInBlock >= 0) {
					// e は [startN, startN+d) に対応する U の係数
					// e について SemiOnlineConvolution で解く。
					long[] F_conv = new long[d];
					// G = Q / R
					//　C*R = U*Q
					// U に e x^t を足すと C は (e x^t * Q / R) = e x^t Gだけ増える。 
					// Gの定数項は1
					System.arraycopy(G, 1, F_conv, 0, Math.min(d, G.length - 1));
					SemiOnlineConvolution semiOnlineConv = new SemiOnlineConvolution(F_conv, PolynomialFpDynamic.this);

					for (int i = 0; i < d; i++) {
						long convSum = 0;
						if (i > 0) {
							convSum = semiOnlineConv.append(e[i - 1]);
						}
						if (isOverride[i]) {
							long targetVal = (target[i] - cTemp[i] + mod) % mod;
							e[i] = (targetVal - convSum + 2 * mod) % mod;
						} else {
							e[i] = 0;
						}
					}
				}

				long[] prod = Arrays.copyOf(mul(e, G), 2 * d);

				if (k < startN + d) {
					return (cTemp[(int)(k - startN)] + prod[(int)(k - startN)]) % mod;
				}

				// Update state for startN + d to startN + 2d - 1
				for (int i = 0; i < d; i++) {
					state[i] = (cTemp[d + i] + prod[d + i]) % mod;
				}
				shiftDeque(startN + d);

				if (maxOverrideInBlock >= 0) {
					// Update forceMap for future forces
					long[] forceContrib = mul(e, reducedQ);
					for (int i = d; i < forceContrib.length; i++) {
						addForce(startN + i, forceContrib[i]);
					}
					maxProcessed = Math.max(maxProcessed, maxOverrideInBlock);
				}
				return -1;
			}

			/**
			 * 現在のウィンドウの開始位置 `curr` を `targetCurr` まで進める。
			 *
			 * <p>すでに処理された上書きインデックスの影響範囲内（`curr <= maxProcessed`）では
			 * ブロックごとに逐次的に処理を進め、影響範囲を脱出した後は
			 * Bostan-Mori アルゴリズムを用いて一気に単調に高速スキップ（fast-forward）する。</p>
			 *
			 * <p>計算量: $O(B \cdot d \log^2 d + d \log d \log(\text{gap}))$。ここで $B$ は処理する逐次ブロック数、$\text{gap}$ は高速スキップする距離。</p>
			 *
			 * @param targetCurr 目標とするウィンドウの開始位置
			 */
			void advanceTo(long targetCurr) {
				if (targetCurr <= curr) return;
				while (curr < targetCurr && curr <= maxProcessed) {
					processBlock(curr);
				}
				if (curr < targetCurr) {
					long[] nextState = consecutiveTermsOfLinearlyRecurrentSequence(state, recurrence, targetCurr - curr, d);
					state = nextState;
					shiftDeque(targetCurr);
				}
			}
		}

		long[] initialState = new long[d];
		SlideHelper helper = new SlideHelper(initialState, -1, -d);
		for (int i = 0; i < H.length; i++) {
			helper.addForce(i, H[i]);
		}

		for (long jr : allOverridesList) {
			if (jr <= helper.maxProcessed) {
				continue;
			}
			long targetCurr = jr - d + 1;
			helper.advanceTo(targetCurr);

			if (jr > helper.maxProcessed) {
				long res = helper.processBlock(helper.curr);
				if (res != -1) {
					return res;
				}
			}
		}

		if (k >= helper.curr + d) {
			long targetCurr = k - d + 1;
			helper.advanceTo(targetCurr);
		}

		return helper.state[(int)(k - helper.curr)];
	}

	// 未テスト
	/**
	/**
	 * 分母多項式 Q(x) を固定し、各項 c_n ごとに異なる遷移係数
	 * a_1^(n), a_2^(n), ... を用いて数列 c_0, c_1, ..., c_K を計算する。
	 *
	 * <p>各 n に対し、遷移係数列 a_i^(n) は
	 *
	 *     Σ_{i≥1} a_i^(n) x^i = P^(n)(x) / Q(x)
	 *
	 * によって定義される。
	 * 初期項 c_0, ..., c_{I-1} は {@code initial} により与えられ、
	 * n ≥ I に対して
	 *
	 *     c_n = Σ_{i≥1} a_i^(n) c_{n-i}
	 *
	 * を満たすものとする。</p>	 *
	 * <p>計算量: $O(\sum_{n=0}^K \deg(P^{(n)}) + K \cdot \deg(Q))$</p>
	 *
	 * @param P_all 各ステップ $n \in [0, K]$ における分子多項式 $P^{(n)}(x)$ の係数配列の配列。長さ $K + 1$
	 * @param Q0 分母多項式 $Q(x)$ の係数配列
	 * @param initial 初期項 $c_0, \dots, c_{I-1}$
	 * @param K 求めたい項の最大インデックス $K \ge 0$
	 * @return $c_0, \dots, c_{K} \pmod{\text{mod}}$ の配列
	 */
	// 未テスト
	public long[] allTermsOfTimeVaryingLinearyReucrrentSequence(
			long[][] P_all, long[] Q0, long[] initial,
			int K) {
		if (K < 0) throw new AssertionError();
		if (P_all == null || Q0 == null || initial == null) {
			throw new AssertionError();
		}
		if (P_all.length < K + 1) {
			throw new IllegalArgumentException("P_all length must be at least K + 1");
		}
		int I = initial.length;
		if (I == 0) throw new AssertionError();
		if (Q0.length == 0 || fp.reduce(Q0[0]) != 1) {
			throw new IllegalArgumentException("Q0[0] must be congruent to 1");
		}

		long[] currQn = new long[Q0.length];
		for (int j = 0; j < Q0.length; j++) {
			currQn[j] = fp.reduce(Q0[j]);
		}

		long[] c = new long[K + 1];
		long[] E = new long[K + 1];

		for (int n = 0; n <= K; n++) {
			long[] Pn = P_all[n];
			if (Pn == null) throw new NullPointerException("P_all[" + n + "] is null");
			if (Pn.length > 0 && fp.reduce(Pn[0]) != 0) {
				throw new IllegalArgumentException("Pn[0] must be zero");
			}
			// C(x) = E(x) Q(x)
			// となる E を管理する
			
			// C(x)P^(n)(x) - E(x)Q(x) = 0
			if (n < I) {
				c[n] = fp.reduce(initial[n]);
				long En = c[n];
				for (int j = 1; j < currQn.length; j++) {
					if (j <= n) {
						En = (En - currQn[j] * E[n - j] % mod + mod) % mod;
					}
				}
				E[n] = En;
			} else {
				//  (P^(n)/Q)*C
				// =(P^(n)E
				long cn = 0;
				for (int j = 1; j < Pn.length; j++) {
					if (j <= n) {
						cn = (cn + fp.reduce(Pn[j]) * E[n - j]) % mod;
					}
				}
				c[n] = cn;

				long En = cn;
				for (int j = 1; j < currQn.length; j++) {
					if (j <= n) {
						En = (En - currQn[j] * E[n - j] % mod + mod) % mod;
					}
				}
				E[n] = En;
			}
		}

		return c;
	}


	/**
	 * 線形漸化式を満たす数列の第 $k$ 項から始まる連続する $m$ 項 $a_k, a_{k+1}, \dots, a_{k+m-1} \pmod{\text{mod}}$ を一括して計算する。
	 *
	 * <p>数列 $a_n$ は以下の線形漸化式を満たす：
	 * $$a_n = \sum_{i=1}^d c_i a_{n-i} \pmod{\text{mod}} \quad (n \ge d)$$
	 * 初期項 $a_0, a_1, \dots, a_{d-1}$ と漸化式係数 $c_1, c_2, \dots, c_d$ が与えられたとき、
	 * まず $x^k \bmod P(x)$ の係数（特性多項式による射影）を計算し、
	 * 有理母関数から得られるプレフィックス項 $a_0, a_1, \dots, a_{m+d-2}$ との
	 * 高速なスライド積（有効シフト内積）を計算することで、第 $k$ 項から始まる連続 $m$ 項を一括して求める。</p>
	 *
	 * <p>計算量: $O(d \log d \log k + (d + m) \log(d + m))$</p>
	 *
	 * @param initial 初期項 of sequence $a = [a_0, a_1, \dots, a_{d-1}]$ (長さ $d$)
	 * @param recurrence 漸化式係数の配列 $c = [c_1, c_2, \dots, c_d]$ (長さ $d$)
	 * @param k 開始項のインデックス $k \ge 0$
	 * @param m 求める項数 $m \ge 0$
	 * @return 第 $k$ 項から第 $k+m-1$ 項までの配列 (長さ $m$)
	 * @throws AssertionError {@code k < 0}、{@code m < 0}、{@code initial.length != recurrence.length}、または {@code d == 0} の場合
	 */
	public long[] consecutiveTermsOfLinearlyRecurrentSequence(long[] initial, long[] recurrence, long k, int m) {
		if (k < 0 || m < 0) throw new AssertionError();
		int d = recurrence.length;
		if (initial.length != d || d == 0) throw new AssertionError();
		if (m == 0) return new long[0];
		long[] denominator = new long[d + 1];
		denominator[0] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			denominator[i + 1] = (mod - c) % mod;
		}
		long[] numerator = Arrays.copyOf(mul(initial, denominator), d);
		int len = m + d - 1;
		long[] prefix = Arrays.copyOf(mul(numerator, inv(Arrays.copyOf(denominator, len))), len);
		long[] coefficients = extendedLinearlyRecurrentSequenceCoefficients(recurrence, k);
		return validShiftedDotProducts(coefficients, prefix);
	}

	/**
	 * 任意の初期項を持つ同じ線形漸化式において、第 $k$ 項以降の項を最初の $d$ 項の線形結合で表すための係数配列 $b$ を計算する。
	 *
	 * <p>具体的には、漸化式 $a_n = \sum_{i=1}^d c_i a_{n-i} \pmod{\text{mod}}$ に対して、
	 * 任意のシフト $j \ge 0$ において以下が成立するような長さ $d$ の係数配列 $b = [b_0, b_1, \dots, b_{d-1}]$ を求める：
	 * $$a_{k+j} = \sum_{i=0}^{d-1} b_i a_{j+i} \pmod{\text{mod}}$$
	 * これは、特性多項式 $P(x) = x^d - \sum_{i=1}^d c_i x^{d-i}$ に対する剰余環 $\mathbb{F}_p[x]/(P(x))$ における
	 * 多項式 $x^k \bmod P(x)$ の係数 $[x^0, x^1, \dots, x^{d-1}]$ に等しい。</p>
	 *
	 * <p>計算量: $O(d \log d \log k)$</p>
	 *
	 * @param recurrence 漸化式係数の配列 $c = [c_1, c_2, \dots, c_d]$ (長さ $d$)
	 * @param k シフトするステップ数 $k \ge 0$
	 * @return 線形結合の係数配列 $b = [b_0, b_1, \dots, b_{d-1}]$ (長さ $d$)
	 */
	public long[] extendedLinearlyRecurrentSequenceCoefficients(long[] recurrence, long k) {
		int d = recurrence.length;
		long[] characteristic = new long[d + 1];
		characteristic[d] = 1;
		for (int i = 0; i < d; i++) {
			long c = recurrence[i] % mod;
			if (c < 0) c += mod;
			characteristic[d - 1 - i] = (mod - c) % mod;
		}
		return Arrays.copyOf(powMod(new long[]{0, 1}, k, characteristic), d);
	}

	// berlekampMassey 関連クラス
	public static class EuclidRow {
		public long[] r, u, y;
		public EuclidRow(long[] r, long[] u, long[] y) { this.r = r; this.u = u; this.y = y; }
	}
	public static class EuclidCrossHalfResult {
		public EuclidRow prev, cur;
		public EuclidCrossHalfResult(EuclidRow prev, EuclidRow cur) { this.prev = prev; this.cur = cur; }
	}

	public EuclidRow euclidNextRow(EuclidRow prev, EuclidRow cur) {
		DivModResult dm = divmod(prev.r, cur.r);
		return new EuclidRow(dm.r, sub(prev.u, mul(dm.q, cur.u)), sub(prev.y, mul(dm.q, cur.y)));
	}

	public EuclidCrossHalfResult euclidCrossHalfNaive(long[] a, long[] b, int h) {
		EuclidRow prev = new EuclidRow(a, new long[]{1}, new long[]{0});
		EuclidRow cur = new EuclidRow(b, new long[]{0}, new long[]{1});
		while (deg(cur.r) >= h) { EuclidRow next = euclidNextRow(prev, cur); prev = cur; cur = next; }
		return new EuclidCrossHalfResult(prev, cur);
	}

	/**
	 * Euclid列が指定した次数境界 {@code h} を初めて下回る場所をhalf-gcdで探す。
	 * O(M(N) log N)
	 */
	public EuclidCrossHalfResult euclidCrossHalfFast(long[] a, long[] b, int h) {
		return selectedRemainder(a, b, h);
	}

	public EuclidCrossHalfResult selectedRemainder(long[] a, long[] b, int h) {
		a = resize(a); b = resize(b);
		int n = deg(a);
		if (deg(b) < h) return new EuclidCrossHalfResult(new EuclidRow(a, new long[]{1}, new long[]{0}), new EuclidRow(b, new long[]{0}, new long[]{1}));
		if (n <= 128) return euclidCrossHalfNaive(a, b, h);
		int half = (n + 1) / 2;
		if (h <= half) {
			HalfGcdResult mat = halfGcd(a, b);
			long[][] cd = mat.apply(a, b);
			EuclidRow row0 = new EuclidRow(cd[0], mat.p00, mat.p01);
			EuclidRow row1 = new EuclidRow(cd[1], mat.p10, mat.p11);
			if (deg(row1.r) < h) return new EuclidCrossHalfResult(row0, row1);
			EuclidRow row2 = euclidNextRow(row0, row1);
			if (deg(row2.r) < h) return new EuclidCrossHalfResult(row1, row2);
			EuclidCrossHalfResult sub = selectedRemainder(row1.r, row2.r, h);
			return composeEuclidRows(sub, row1, row2);
		}
		int shift = 2 * h - n;
		EuclidCrossHalfResult high = selectedRemainder(divideByX(a, shift), divideByX(b, shift), h - shift);
		EuclidRow prev = applyEuclidRow(high.prev, a, b);
		EuclidRow cur = applyEuclidRow(high.cur, a, b);
		if (deg(cur.r) >= h) throw new AssertionError("selected remainder did not cross boundary");
		if (deg(prev.r) < h) throw new AssertionError("selected remainder overshot boundary");
		return new EuclidCrossHalfResult(prev, cur);
	}

	public EuclidCrossHalfResult composeEuclidRows(EuclidCrossHalfResult sub, EuclidRow row0, EuclidRow row1) {
		return new EuclidCrossHalfResult(composeEuclidRow(sub.prev, row0, row1), composeEuclidRow(sub.cur, row0, row1));
	}

	public EuclidRow composeEuclidRow(EuclidRow row, EuclidRow row0, EuclidRow row1) {
		return new EuclidRow(row.r, resize(add(mul(row.u, row0.u), mul(row.y, row1.u))), resize(add(mul(row.u, row0.y), mul(row.y, row1.y))));
	}

	public EuclidRow applyEuclidRow(EuclidRow row, long[] a, long[] b) {
		return new EuclidRow(resize(add(mul(row.u, a), mul(row.y, b))), row.u, row.y);
	}

	public long[] berlekampMassey(long[] s) {
		int m = s.length;
		long[] a = new long[m + 1];
		a[m] = 1;
		long[] b = Arrays.copyOf(s, m);
		for (int i = 0; i < m; i++) { b[i] %= mod; if (b[i] < 0) b[i] += mod; }
		b = resize(b);
		if (deg(b) == -1) return new long[]{1};
		EuclidCrossHalfResult cross = selectedRemainder(a, b, (m + 1) / 2);
		EuclidRow prev = cross.prev, cur = cross.cur;
		EuclidRow next = deg(cur.r) == -1 ? null : euclidNextRow(prev, cur);
		// best candidate
		EuclidRow[] rows = {prev, cur, next};
		long[] bestC = null; int bestL = Integer.MAX_VALUE;
		for (EuclidRow row : rows) {
			if (row == null || row.y.length == 0 || row.y[0] == 0) continue;
			int nominalDegree = Math.max(deg(row.y), deg(row.r) + 1);
			if (nominalDegree < bestL) { bestL = nominalDegree; bestC = row.y; }
		}
		if (bestC == null) throw new AssertionError();
		long[] C = Arrays.copyOf(bestC, bestL + 1);
		long inv = MathUtils.modInv(C[0], mod);
		for (int i = 0; i < C.length; i++) C[i] = C[i] * inv % mod;
		long[] coeff = new long[bestL + 1];
		for (int i = 0; i <= bestL; i++) { coeff[i] = C[bestL - i] % mod; if (coeff[i] < 0) coeff[i] += mod; }
		return coeff;
	}

	public long[] monomialToNewtonBasis(long[] a, long[] p) {
		if (a.length != p.length) throw new AssertionError();
		int len = 1;
		while (len < a.length) len *= 2;
		long[][] mods = new long[2 * len][];
		long[][] modded = new long[2 * len][];
		for (int i = 0; i < len; i++)
			mods[i + len] = i < a.length ? new long[]{(mod - p[i]) % mod, 1} : new long[]{1};
		for (int i = len - 1; i >= 1; i--) mods[i] = mul(mods[2*i], mods[2*i+1]);
		modded[1] = mod(a, mods[1]);
		for (int i = 1; 2*i < modded.length; i++) {
			DivModResult res = divmod(modded[i], mods[2*i]);
			modded[2*i] = res.r;
			modded[2*i+1] = res.q;
		}
		long[] ret = new long[a.length];
		for (int i = 0; i < a.length; i++) ret[i] = modded[len + i].length > 0 ? modded[len + i][0] : 0;
		return ret;
	}

	public long[] newtonToMonomialBasis(long[] a, long[] p) {
		if (a.length != p.length) throw new AssertionError();
		int len = 1;
		while (len < a.length) len *= 2;
		long[][] mods = new long[2 * len][];
		long[][] built = new long[2 * len][];
		for (int i = 0; i < len; i++) {
			if (i < a.length) {
				mods[i + len] = new long[]{(mod - p[i]) % mod, 1};
				built[i + len] = new long[]{a[i]};
			} else {
				mods[i + len] = new long[]{1};
				built[i + len] = new long[]{0};
			}
		}
		for (int i = len - 1; i >= 1; i--) {
			if (i != 1) mods[i] = mul(mods[2*i], mods[2*i+1]);
			built[i] = add(built[2*i], mul(mods[2*i], built[2*i+1]));
		}
		return Arrays.copyOf(built[1], a.length);
	}

	/**
	 * f(g(x)) mod x^n。g[0]=0 を仮定する。
	 */
	public long[] comp(long[] f, long[] g, int n) {
		if (g[0] != 0) throw new AssertionError();
		long[] num = resize(f);
		int m = num.length;
		ArrayUtils.reverse(num);
		int degG = deg(g);
		if (degG == -1) { long[] ret = new long[n]; ret[0] = f[0]; return ret; }
		long[][] den = new long[degG + 1][2];
		den[0][0] = 1;
		for (int i = 0; i <= degG; i++) den[i][1] = (mod - g[i]) % mod;
		PolynomialFpDynamic2D p2d = PolynomialFpDynamic2D.of(this);
		long[][] x = comp2d(m - 1, m, n, num, den, p2d);
		long[] ret = new long[x.length];
		for (int i = 0; i < x.length; i++) ret[i] = x[i][0];
		return ret;
	}

	long[][] comp2d(int l, int r, int n, long[] p, long[][] q, PolynomialFpDynamic2D p2d) {
		if (n == 0) return new long[1][1];
		if (n == 1) {
			long[] a = mul(p, inv(q[0]));
			long[][] ret = new long[1][r - l];
			for (int i = l; i < r; i++) ret[0][i - l] = i < a.length ? a[i] : 0;
			return ret;
		}
		long[][] negatedQ = ArrayUtils.copy(q);
		for (int i = 0; i < negatedQ.length; i++)
			for (int j = 0; j < negatedQ[i].length; j++)
				if (i % 2 == 1) negatedQ[i][j] = negatedQ[i][j] * (mod - 1) % mod;
		int e = Math.max(0, l - (q[0].length - 1));
		q = p2d.mul(q, negatedQ);
		long[][] v = new long[(n + 1) / 2][q[0].length];
		for (int i = 0; i < n && i < q.length; i += 2)
			for (int j = 0; j < q[i].length; j++) v[i / 2][j] = q[i][j];
		long[][] x = comp2d(e, r, (n + 1) / 2, p, v, p2d);
		long[][] a = new long[x.length * 2][x[0].length];
		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < x[i].length; j++) a[2 * i][j] = x[i][j];
		a = p2d.mul(a, negatedQ);
		long[][] ret = new long[n][r - l];
		for (int i = 0; i < n && i < a.length; i++)
			for (int j = l - e; j < r - e && j < a[i].length; j++) ret[i][j - (l - e)] = a[i][j];
		return ret;
	}

	/**
	 * f の合成逆関数 g（g(f(x))=x）を返す。f[0]=0, f[1]≠0 を仮定する。
	 */
	public long[] compInverse(long[] f) {
		if (f[0] != 0) throw new AssertionError();
		long inv1 = MathUtils.modInv(f[1], mod);
		long[] b = f.clone();
		long c = inv1;
		for (int i = 1; i < f.length; i++) { b[i] = f[i] * c % mod; c = c * inv1 % mod; }
		long[][] h = new long[f.length + 1][2];
		h[0][0] = 1;
		for (int i = 0; i < f.length; i++) if (b[i] != 0) h[i][1] = mod - b[i];
		int n = f.length + 1;
		PolynomialFpDynamic2D p2d = PolynomialFpDynamic2D.of(this);
		long[] fPow = p2d.fixingXofRational(new long[][] {{1}}, h, n);
		for (int i = 1; i < fPow.length; i++) fPow[i] = fPow[i] * n % mod * fp.inv(i) % mod;
		fPow = Arrays.copyOf(fPow, n + 1);
		ArrayUtils.reverse(fPow);
		fPow = pow(fPow, mod - MathUtils.modInv(n, mod));
		long[] ret = new long[f.length];
		for (int i = 1; i < f.length; i++) ret[i] = fPow[i - 1] * inv1 % mod;
		return ret;
	}
	

	 /** f の合成逆関数 g（g(f(x))=x）を Newton 法で求める。
	 * f(0)=0, f'(0)≠0 を仮定する。
	 * 
	 * @param n 求める次数
	 * @param comp 多項式合成関数 g -> f(g(x)) mod x^n
	 * @return g(x) mod x^n
	 * 
	 * <p>計算量: O(T(n))。ここで T(n) は compose の計算量。
	 * 
	 */
	/**
	 * Lagrange-Bürmannの公式を用いて、多項式の合成 A(B(x)) の第 n 係数を計算する。
	 * B(x) の合成逆関数 G(y) = B⁻¹(y) が既知であるときに使用する。
	 *
	 * <p>事前条件:
	 * <ul>
	 *   <li>G(0) = 0</li>
	 *   <li>G'(0) ≠ 0</li>
	 * </ul>
	 * </p>
	 *
	 * @param A 多項式 A(x) の係数配列
	 * @param G B(x) の合成逆関数 G(y) の係数配列
	 * @param n 求めたい係数の次数
	 * @return [xⁿ] A(B(x))
	 *
	 * <p>計算量: O(n log n)</p>
	 */
	public long lagrangeBurmann(long[] A, long[] G, int n) {
		if (n == 0) return A.length > 0 ? fp.reduce(A[0]) : 0;
		if (n < 0) return 0;
		long[] Adiff = differentiate(A);
		if (G.length < 2 || fp.reduce(G[0]) != 0 || fp.reduce(G[1]) == 0) {
			throw new IllegalArgumentException("G must satisfy G(0) = 0 and G'(0) != 0");
		}
		long[] H = divideByX(G, 1);
		long[] HinvN = pow(inv(Arrays.copyOf(H, n)), n);
		long[] Res = mul(Arrays.copyOf(Adiff, n), HinvN);
		long coeff = (n - 1 < Res.length) ? Res[n - 1] : 0;
		return fp.reduce(coeff) * fp.inv(n) % mod;
	}

	public long[] compInverseNewton(int n, Composition comp) {
		/* 
		 * 汎関数 Φ(g) = f(g) - x の根を求める Newton 法を考える。
		 * g_k が真の解 g* に対して g* = g_k + ε  となるとき、
		 * Φ(g*) = Φ(g_k + ε) = Φ(g_k) + εDΦ_{g_k} + O(ε^2)
		 * ε = -Φ(g_k)/DΦ_{g_k}
		 *   = -(f(g_k) - x) / f'(g_k)
		 */	
		
		long[]g=new long[1];
		for (int len = 1; len < n; len <<= 1) {
			int nextLen = Math.min(len << 1, n);
			
			// h = f(g) - x mod x^{nextLen}
			long[] h = comp.apply(g, nextLen);
			if (h.length <= 1) h = Arrays.copyOf(h, 2);
			h[1] = subMod(h[1], 1);
			// f'(g) mod x^{nextLen}
			long[] fpg = comp.diff(this, g, nextLen);
			// delta = h / fpg mod x^{nextLen}
			long[] inv_fpg = inv(Arrays.copyOf(fpg, nextLen));
			long[] delta = mul(h, inv_fpg);
			delta = Arrays.copyOf(delta, nextLen);
			
			// g = g - delta mod x^{nextLen}
			g = sub(Arrays.copyOf(g, nextLen), delta);
		}
		return Arrays.copyOf(g, n);
	}

	/**
	 * 分離変数形微分方程式 df/dx = G(f) H(x), f(0) = f0 を解く。
	 * f(x) = f0 + A^{-1}(B(x)) mod x^deg へ帰着して計算する。
	 * A'(x) = 1 / G(f0 + x), A(0) = 0
	 * B(x) = ∫ H(x) dx, B(0) = 0
	 * 
	 * @param G y の形式的べき級数
	 * @param H x の形式的べき級数
	 * @param f0 初期値
	 * @param deg 求める次数 (mod x^deg)
	 * @return f(x) mod x^deg
	 * 計算量: O(deg log^2 deg)
	 */
	public long[] solveSeparableODE(long[] G, long[] H, long f0, int deg) {
		// df/dx = G(f) H(x)
		// 1/G(f) df = H(x)dx
		// ∫_0^u 1/G(f+f0) df = ∫_0^x H(x) dx
		if (deg <= 0) return new long[0];
		if (deg == 1) return new long[] {fp.reduce(f0)};
		
		// G_shifted(u) = G(f0 + u) mod u^deg
		long[] gPadded = Arrays.copyOf(G, deg);
		long[] gShifted = taylorShift(gPadded, f0);

		// A'(u) = 1 / G(f0 + u) mod u^deg
		long[] aPrime = inv(gShifted);
		
		// A(u) = ∫ A'(u) du mod u^deg
		long[] a = integrate(aPrime);

		// B(x) = ∫ H(x) dx mod x^deg
		long[] hPadded = Arrays.copyOf(H, deg);
		long[] b = integrate(hPadded);

		// u(x) = A^{-1}(B(x)) mod x^deg
		long[] aInv = compInverse(Arrays.copyOf(a, deg + 1));
		long[] u = comp(aInv, b, deg);

		// f(x) = f0 + u(x)
		u[0] = addMod(u[0], fp.reduce(f0));
		return u;
	}

	/**
	 * 形式的冪級数における常微分方程式 f' = G(x, f) を表すインターフェース。
	 */
	public interface DifferentialEquation {
		/**
		 * G(x, f(x)) mod x^n を返す。
		 * @param f 現在の f
		 * @param n 求める次数
		 * @return G(x, f(x)) mod x^n
		 */
		long[] apply(long[] f, int n);

		/**
		 * (∂G/∂f)(x, f(x)) mod x^n を返す。
		 * つまり、G(x, y) を y について偏微分した関数に f(x) を代入したものを返す。
		 * @param f 現在の f
		 * @param n 求める次数
		 * @return (∂G/∂f)(x, f(x)) mod x^n
		 */
		long[] applyDerivative(long[] f, int n);
	}

	/**
	 * 常微分方程式 f' = G(x, f), f(0) = f0 を Newton 法で解く。
	 *
	 * <p>方程式 Φ(f) = f' - G(x, f) = 0 に対して Newton 法を適用する。
	 * 現在の近似を f_k とし、f = f_k + h として一次展開すると：
	 * Φ(f_k + h) ≈ Φ(f_k) + Φ'(f_k) h = (f_k' - G(x, f_k)) + (h' - (∂G/∂f)(x, f_k)) h = 0
	 * したがって、修正項 h は以下の一次線形 ODE を満たす：
	 * h' - A h = r
	 * ここで A = (∂G/∂f)(x, f_k), r = G(x, f_k) - f_k' である。
	 *
	 * <p>この線形 ODE は積分因子 exp(-∫ A dx) を用いて以下のように解ける：
	 * h = exp(∫ A dx) ∫ (r exp(-∫ A dx)) dx
	 *
	 * <p>計算量: O(T(n) + M(n))
	 * ここで T(n) は G(x, f) および ∂_y G(x, f) の計算量、M(n) は多項式乗算の計算量。
	 *
	 * @param n 求める次数 (mod x^n)
	 * @param f0 初期値 f(0)
	 * @param eq 微分方程式を表すオブジェクト
	 * @return f(x) mod x^n
	 */
	public long[] solveDifferentialEquationNewton(int n, long f0, DifferentialEquation eq) {
		if (n <= 0) return new long[0];
		f0 = fp.reduce(f0);
		long[] f = new long[] {f0};
		for (int len = 1; len < n; len <<= 1) {
			int nextLen = Math.min(len << 1, n);

			// A = (∂G/∂f)(x, f) mod x^len
			long[] a = eq.applyDerivative(f, len);
			long[] negatedA = new long[len];
			for (int i = 0; i < len; i++) {
				if (a[i] != 0) negatedA[i] = mod - a[i];
			}

			// r = G(x, f) - f' mod x^{nextLen - 1}
			long[] b = eq.apply(f, nextLen - 1);
			long[] fp = differentiate(f);
			long[] r = sub(b, fp);
			r = Arrays.copyOf(r, nextLen - 1);

			// 修正項 h は h' - Ah = r, h(0) = 0 を満たす
			// つまり h' + (-A)h = r
			long[] h = solveLinearODE(negatedA, r, 0, nextLen);

			// f = f + h mod x^{nextLen}
			f = add(Arrays.copyOf(f, nextLen), h);

			// 定数項のチェック
			assert f[0] == f0 : "Constant term f(0) must be f0";
			assert h[0] == 0 : "Newton correction h(0) must be 0";
		}
		return Arrays.copyOf(f, n);
	}

	/**
	 * 1階線形微分方程式 f' + P(x)f = Q(x), f(0) = f0 を解く。
	 * f(x) = exp(-∫ P dx) (f0 + ∫ Q exp(∫ P dx) dx) mod x^deg
	 * 
	 * @param P x の形式的べき級数
	 * @param Q x の形式的べき級数
	 * @param f0 初期値
	 * @param deg 求める次数 (mod x^deg)
	 * @return f(x) mod x^deg
	 * 計算量: O(deg log deg)
	 */
	public long[] solveLinearODE(long[] P, long[] Q, long f0, int deg) {
		if (deg <= 0) return new long[0];
		if (deg == 1) return new long[] {fp.reduce(f0)};

		// iP = ∫ P dx mod x^deg
		long[] iP = integrate(Arrays.copyOf(P, deg));
		
		// expIP = exp(∫ P dx) mod x^deg
		long[] expIP = exp(Arrays.copyOf(iP, deg));
		
		// Integrand = Q * expIP mod x^deg
		long[] integrand = mul(Arrays.copyOf(Q, deg), expIP);
		integrand = Arrays.copyOf(integrand, deg);
		
		// I = ∫ Q exp(∫ P dx) dx mod x^deg
		long[] I = integrate(integrand);
		I = Arrays.copyOf(I, deg);
		
		// I = f0 + I
		I[0] = addMod(I[0], fp.reduce(f0));
		
		// f = exp(-∫ P dx) * I mod x^deg
		// exp(-iP) = 1 / expIP
		long[] invExpIP = inv(expIP);
		long[] f = mul(invExpIP, I);
		
		return Arrays.copyOf(f, deg);
	}

	/**
	 * f(x) の根（Fp 上）を重複度込みで返す。
	 * DDFのdegree=1のステップだけ実行し、一次因子をEDFで分解して根を取り出す。
	 * @param f 多項式
	 * @return 根の配列（重複あり）
	 */
	public long[] roots(long[] f) {
		//https://judge.yosupo.jp/submission/372104
		f = monic(f);
		if (deg(f) <= 0) return new long[0];
		ArrayList<Long> ret = new ArrayList<>();
		long[] x = new long[]{0, 1};
		long[] h = powMod(x, mod, f);
		long[] g = gcd(f, sub(h, x));
		if (deg(g) > 0) {
			for (long[] factor : factorEqualDegree(g, 1)) {
				long root = factor[0] == 0 ? 0 : mod - factor[0];
				while (deg(f) >= 1) {
					DivModResult dm = divmod(f, factor);
					if (deg(dm.r) != -1) break;
					f = dm.q;
					ret.add(root);
				}
			}
		}
		long[] arr = new long[ret.size()];
		for (int i = 0; i < arr.length; i++) arr[i] = ret.get(i);
		return arr;
	}
	
	
	
	
	/**
	 * 多項式 f(x) = sum a_i x^i を x で評価する。ホーナー法を使用。
	 */
	public long evaluate(long[] a, long x) {
		long res = 0;
		x = fp.reduce(x);
		for (int i = a.length - 1; i >= 0; i--) {
			res = (res * x + a[i]) % mod;
		}
		return res;
	}

	/**
	 * 多項式 f(x) = sum a_i x^i を等比数列点 x_k = c^k (k=0,1,...,m-1) で評価する。
	 * Chirp Z-Transform (Bluestein's algorithm) を用いて O((n+m) log(n+m)) で計算する。
	 * @param coeffs 多項式の係数配列 [a_0, a_1, ..., a_{n-1}]
	 * @param c 等比数列の公比
	 * @param m 評価点の数
	 * @return f(c^0), f(c^1), ..., f(c^{m-1}) の配列
	 */
	/**
	 * 多項式 f を点 x で評価する。
	 * @param f 多項式
	 * @param x 評価点
	 * @return f(x)
	 */
	public long eval(long[] f, long x) {
		long res = 0;
		for (int i = f.length - 1; i >= 0; i--) {
			res = (res * x + fp.reduce(f[i])) % mod;
		}
		return res;
	}
	
	/**
	 * x=c^i (i=0,1,..,m-1) の評価値を返す。
	 * @param coeffs
	 * @param c
	 * @param m
	 * @return
	 */
	public long[] evaluateAtGeometricProgression(long[] coeffs, long c, int m) {
		//https://judge.yosupo.jp/submission/372401
		if (coeffs.length == 0 || m == 0) return new long[0];
		
		int n = coeffs.length;
		
		if (c == 0) {
			long[] result = new long[m];
			for (int i = 0; i < coeffs.length; i++) {
				result[0] += coeffs[i];
				result[0] %= mod;
			}
			for (int i = 1; i < result.length; i++) {
				result[i] = fp.reduce(coeffs[0]);
			}
			return result;
		}
		
		// c = 1 の場合は単に全ての係数の和
		if (c == 1) {
			long sum = 0;
			for (long v : coeffs) sum = addMod(sum, fp.reduce(v));
			long[] result = new long[m];
			Arrays.fill(result, sum);
			return result;
		}
		
		// 必要な三角数の指数範囲: 最大で t_{n+m-2}
		int maxIdx = n + m - 2;
		
		// t_i = i(i-1)/2
		// c^{t_i} と c^{-t_i} を事前計算
		// 漸化式: t_{i+1} = t_i + i より c^{t_{i+1}} = c^{t_i} * c^i
		long[] cPowT = new long[maxIdx + 1]; // c^{t_i}
		long[] cInvPowT = new long[maxIdx + 1]; // c^{-t_i}
		
		long cInv = MathUtils.modInv(c, mod);
		cPowT[0] = 1;
		cInvPowT[0] = 1;
		
		long cPowI = 1;     // c^i
		long cInvPowI = 1;  // c^{-i}
		for (int i = 1; i <= maxIdx; i++) {
			cPowT[i] = cPowT[i - 1] * cPowI % mod;
			cInvPowT[i] = cInvPowT[i - 1] * cInvPowI % mod;
			cPowI = cPowI * c % mod;
			cInvPowI = cInvPowI * cInv % mod;
		}
		
		// ik = t_{i+k}-t_i-t_k
		// sum_i a_i c^{ik} = sum_i a_i c^{t_{i+k}-t_i-t_k}
		// sum_i a_i c^{ik} = c^{-t_k}  sum_i a_i c^{t_{i+k}-t_i} for each 0 <= k <= m-1
		
		
		// Step 1: y_i = coeffs[i] * c^{-t_i}, 反転して A[i] = y_{n-1-i}
		long[] A = new long[n];
		for (int i = 0; i < n; i++) {
			long y_i = fp.reduce(coeffs[i]) * cInvPowT[i] % mod;
			A[n - 1 - i] = y_i;
		}
		
		// Step 2: v_j = c^{t_j} (j=0..n+m-2)
		int vLen = n + m - 1;
		long[] V = new long[vLen];
		for (int j = 0; j < vLen; j++) {
			V[j] = cPowT[j];
		}
		
		// Step 3: 畳み込み A * V
		long[] conv = mul(A, V);
		
		// Step 4: 結果の第 n-1+k 項を取り出し、c^{-t_k} を掛ける
		long[] result = new long[m];
		for (int k = 0; k < m; k++) {
			result[k] = conv[n - 1 + k] * cInvPowT[k] % mod;
		}
		
		return result;
	}

	/**
	 * 標本点が等比数列 a, aq, aq^2, ..., aq^{n-1} を成す場合に補間多項式を計算する。
	 * Inverse Chirp Z-Transform (ICZT)。時間計算量 Θ(n log n)。
	 * @param v 標本値の配列 [v_0, v_1, ..., v_{n-1}]
	 * @param a 等比数列の初項（a ≠ 0）
	 * @param q 等比数列の公比（q^i ≠ 1 (1 ≤ i ≤ n) を仮定）
	 * @return n 次未満の多項式 f で f(a q^i) = v[i] を満たす係数配列 [f_0, ..., f_{n-1}]
	 */
	public long[] interpolateAtGeometricProgression(long[] v, long a, long q) {
		//https://judge.yosupo.jp/submission/372503
		//https://noshi91.github.io/algorithm-encyclopedia/polynomial-interpolation-geometric#noredirect
		int n = v.length;
		if (n == 0) return new long[0];
		a = fp.reduce(a);
		q = fp.reduce(q);
		// s[i] = prod_{j=1}^{i} (1 - q^j)
		long[] s = new long[n + 1];
		s[0] = 1;
		long qj = q;
		for (int i = 1; i <= n; i++) {
			s[i] = s[i - 1] * fp.reduce(1 - qj) % mod;
			qj = qj * q % mod;
		}

		// 分母 prod_{j≠i} (q^i - q^j) を Θ(n) で計算
		long[] qPows = new long[n + 1];
		qPows[0] = 1;
		for (int i = 1; i <= n; i++) qPows[i] = qPows[i - 1] * q % mod;

		long[] denom = new long[n];
		long qe = 1; // q^{i(i-1)/2 + i(n-i-1)}
		for (int i = 0; i < n; i++) {
			long sign = (i % 2 == 0) ? 1 : mod - 1;
			long d = sign * qe % mod;
			d = d * s[i] % mod;
			d = d * s[n - 1 - i] % mod;
			denom[i] = fp.reduce(d);
			if (i + 1 < n) {
				qe = qe * qPows[n - i - 2] % mod;
			}
		}

		// w_i = v_i / denom_i
		long[] w = new long[n];
		for (int i = 0; i < n; i++) {
			w[i] = fp.reduce(v[i]) * MathUtils.modInv(denom[i], mod) % mod;
		}

		// P(x) = prod_{i=0}^{n-1} (1 - q^i x) を Cauchy binomial theorem で計算
		// When q^n = 1, P(x) = 1 - x^n
		// Otherwise use Cauchy binomial theorem
		long[] P = new long[n + 1];
		if (qPows[n] == 1) {
		    // q^n = 1 implies P(x) = 1 - x^n
		    P[0] = 1;
		    P[n] = mod - 1;
		} else {
			long[] prefix = new long[n + 1];
			prefix[0] = s[0];
			for (int i = 1; i <= n; i++) prefix[i] = prefix[i - 1] * s[i] % mod;
			long invPrefix = MathUtils.modInv(prefix[n], mod);
			long[] invS = new long[n + 1];
			for (int i = n; i >= 1; i--) {
				invS[i] = prefix[i - 1] * invPrefix % mod;
				invPrefix = invPrefix * s[i] % mod;
			}
			invS[0] = invPrefix;
	
			long qTri = 1;
			long sN = s[n];
			for (int k = 0; k <= n; k++) {
				long sign = (k % 2 == 0) ? 1 : mod - 1;
				long p = sign * qTri % mod;
				p = p * sN % mod;
				p = p * invS[k] % mod;
				p = p * invS[n - k] % mod;
				P[k] = fp.reduce(p);
				if (k < n) qTri = qTri * qPows[k] % mod;
			}
		}
		// g(y) = sum w_i y^i を点 1, q, q^2, ..., q^{n-1} で評価（chirp z-transform）
		long[] S = evaluateAtGeometricProgression(w, q, n);

		// f^R(x) = P(x) * S(x) (mod x^n)
		long[] fR = mul(P, S);
		fR = Arrays.copyOf(fR, n);

		// f(x) は f^R(x) の係数を逆向きに並べたもの
		long[] f = new long[n];
		for (int i = 0; i < n; i++) {
			f[i] = fR[n - 1 - i];
		}

		// a ≠ 1 の場合、k 次の係数を a^{-k} 倍する
		if (a != 1) {
			long aInv = MathUtils.modInv(a, mod);
			long aInvPow = 1;
			for (int k = 0; k < n; k++) {
				f[k] = f[k] * aInvPow % mod;
				aInvPow = aInvPow * aInv % mod;
			}
		}

		return f;
	}
	
	


	/** [x^N] g(x) f(x)^m を m = 0, 1, ..., N-1 について一括計算します。
	 * f(0) = 0 かつ f'(0) ≠ 0 を満たし、その逆関数 h(w) = f^{-1}(w) mod w^{N+2} が与えられる必要があります。
	 * 
	 * @param h f(x) の逆関数 f^{-1}(w) (サイズ N+2 以上)
	 * @param g 多項式 g(x)
	 * @param N 抽出する x の次数
	 * @return 長さ N の配列 (m 番目の要素が [x^N] g(x) f(x)^m)
	 * 
	 * <p>計算量: O(N log N) (または関数合成の計算量)
	 */
	public long[] coeffBatchExtraction(long[] h, long[] g, int N) {
		return coeffBatchExtraction(h, comp(g, h, N + 1), N, N);
	}

	/**
	 * [x^N] G(x) f(x)^m を m = 0, 1, ..., K-1 について一括計算します。
	 * 
	 * @param h f(x) の逆関数 f^{-1}(w) (サイズ N+2 以上)
	 * @param gh 多項式 G(h(w)) (サイズ N+1 以上)
	 * @param N 抽出する x の次数
	 * @param K 計算する m の数
	 * @return 長さ K の配列 (m 番目の要素が [x^N] G(f(x)) f(x)^m)
	 * 
	 * <p>計算量: O(N log N)
	 */
	public long[] coeffBatchExtraction(long[] h, long[] gh, int N, int K) {
		if (N < 0) return new long[0];
		if (h.length < N + 2) throw new AssertionError();
		// [x^N] A(x)
		//=Res_x A(x) / x^(N+1)
		//=Res_y A(h(y)) h'(y) / h(y)^(N+1)
		//=[y^N] A(h(y)) h'(y) y^{N+1} / h(y)^(N+1)
		
		// A(x) = G(x) f(x)^m と置くと
		// [x^N] A(x) 
		//=[y^N] G(h(y)) y^m h'(y) (y / h(y))^(N+1)
		//=[y^(N-m)] G(h(y)) h'(y) (y / h(y))^(N+1)
		
		
		// h'(y)
		long[] hp = differentiate(h);

		// h(y)/y
		long[] h_div_y = new long[N + 1];
		for (int i = 0; i <= N; i++) {
			h_div_y[i] = h[i + 1];
		}

		long[] y_div_h = inv(h_div_y);
		long[] y_div_h_pow = pow(y_div_h, N + 1);

		// A = gh * hp * y_div_h_pow mod w^{N+1}
		long[] A = mul(gh, hp);
		A = Arrays.copyOf(A, N + 1);
		A = mul(A, y_div_h_pow);
		A = Arrays.copyOf(A, N + 1);

		// B_m = [w^{N-m}] P(w)
		long[] B = new long[K];
		for (int m = 0; m < K; m++) {
			if (N - m >= 0 && N - m < A.length) B[m] = A[N - m];
		}
		return B;
	}

	/**
	 * [x^N] f(x)^i を i = 0, 1, ..., N-1 について一括計算します。
	 * f(0) = 0 かつ f'(0) ≠ 0 を満たし、その逆関数 h(w) = f^{-1}(w) mod w^{N+2} が与えられる必要があります。
	 * 
	 * @param h f(x) の逆関数 f^{-1}(w) (サイズ N+2 以上)
	 * @param N 抽出する x の次数
	 * @return 長さ N の配列 (i 番目の要素が [x^N] f(x)^i)
	 * 
	 * <p>計算量: O(N log N)
	 */
	public long[] powersBatchExtraction(long[] h, int N) {
		return coeffBatchExtraction(h, new long[] { 1 }, N, N);
	}

	/**
	 * [x^N] g(x) (a + f(x))^i を i = 0, 1, ..., M について一括計算します。
	 * f(0) = 0 かつ f'(0) ≠ 0 を満たし、その逆関数 h(w) = f^{-1}(w) mod w^{N+1} が与えられる必要があります。
	 * 
	 * @param h f(x) の逆関数 f^{-1}(w) (サイズ N+1 以上)
	 * @param g 多項式 g(x)
	 * @param a 定数 a
	 * @param N 抽出する x の次数
	 * @param M 計算する最大の i
	 * @return 長さ M+1 の配列 (i 番目の要素が [x^N] g(x) (a + f(x))^i)
	 * 
	 * <p>計算量: O(N (log N)^2)
	 */
	public long[] solveLagrange(long[] h, long[] g, long a, int N, int M) {
		return solveLagrangeByGH(h, comp(g, h, N + 1), a, N, M);
	}

	/**
	 * [x^N] G(x) (a + f(x))^i を i = 0, 1, ..., M について一括計算します。
	 * f(0) = 0 かつ f'(0) ≠ 0 を満たし、その逆関数 h(w) = f^{-1}(w) mod w^{N+1} が与えられる必要があります。
	 * 
	 * @param h f(x) の逆関数 f^{-1}(w) (サイズ N+2 以上)
	 * @param gh 多項式 G(h(w)) (サイズ N+1 以上)
	 * @param a 定数 a
	 * @param N 抽出する x の次数
	 * @param M 計算する最大の i
	 * @return 長さ M+1 の配列 (i 番目の要素が [x^N] G(x) (a + f(x))^i)
	 * 
	 * <p>計算量: O(N log N + M log M)
	 */
	public long[] solveLagrangeByGH(long[] h, long[] gh, long a, int N, int M) {
		if (h.length <= N + 1 || gh.length <= N) throw new AssertionError();
		if (N < 0 || M < 0) return new long[0];
		// 1. ラグランジュ反転による一括抽出 [x^N] G(x) f(x)^m
		// m > N では [x^N] G(x) f(x)^m = 0 なので m <= min(M, N) までで十分
		long[] B = coeffBatchExtraction(h, gh, N, Math.min(M, N) + 1);

		// 2. 二項展開の畳み込み
		// C_k = a^k / k!
		long[] C = new long[M + 1];
		C[0] = 1;
		long a_red = fp.reduce(a);
		for (int k = 1; k <= M; k++) {
			C[k] = C[k - 1] * a_red % mod * fp.inv(k) % mod;
		}

		// D_m = B_m / m!
		long[] D = new long[B.length];
		for (int m = 0; m < B.length; m++) {
			D[m] = B[m] * fp.ifac(m) % mod;
		}

		// A = C * D
		long[] A_conv = mul(C, D);

		// A_i = A_conv[i] * i!
		long[] res = new long[M + 1];
		for (int i = 0; i <= M; i++) {
			if (i < A_conv.length) res[i] = A_conv[i] * fp.fac(i) % mod;
		}

		return res;
	}	
	/**
	 * 多項式の列 f0, f1, ..., f{N-1} および g0, g1, ..., g{N-1} を与え、
	 * \sum_{i=0}^N (\prod_{j=0}^{i-1} f_j) (\prod_{j=i}^{N-1} g_j) を計算する。
	 * O(N log^2 N)、N は次数の総和。
	 * @param F 多項式の列
	 * @param G 多項式の列
	 * @return 総和多項式
	 */
	public long[] sumOfPrefixSuffixProducts(long[][] F, long[][] G) {
		int n = F.length;
		if (n == 0) return new long[] {1};
		return sumOfPrefixSuffixProductsDfs(F, G, 0, n).p;
	}

	private static class SumOfPrefixSuffixProductsResult {
		long[] p, f, g;
		//res.f = F[l] * F[l+1] * ... * F[r-1]
		//res.g = G[l] * G[l+1] * ... * G[r-1]
		//res.p = (G[l]*G[l+1]*...*G[r-1])       // i = l   (すべてg)
		//+ (F[l]*G[l+1]*...*G[r-1])             // i = l+1
	    //+ (F[l]*F[l+1]*G[l+2]*...*G[r-1])      // i = l+2
	    //...
	    //+ (F[l]*F[l+1]*...*F[r-2]*G[r-1])      // i = r-1
	    //+ (F[l]*F[l+1]*...*F[r-1])             // i = r   (すべてf)
		SumOfPrefixSuffixProductsResult(long[] p, long[] f, long[] g) {
			this.p = p;
			this.f = f;
			this.g = g;
		}
	}

	private SumOfPrefixSuffixProductsResult sumOfPrefixSuffixProductsDfs(long[][] F, long[][] G, int l, int r) {
		if (r == l + 1) {
			return new SumOfPrefixSuffixProductsResult(add(F[l], G[l]), F[l], G[l]);
		}
		int m = (l + r) / 2;
		SumOfPrefixSuffixProductsResult left = sumOfPrefixSuffixProductsDfs(F, G, l, m);
		SumOfPrefixSuffixProductsResult right = sumOfPrefixSuffixProductsDfs(F, G, m, r);
		//pの和は∑[i=l..m]+∑[i=m..r]でi=mがダブっているので引き算
		long[] p = add(mul(left.p, right.g), mul(left.f, sub(right.p, right.g)));
		long[] f = mul(left.f, right.f);
		long[] g = mul(left.g, right.g);
		return new SumOfPrefixSuffixProductsResult(p, f, g);
	}
	
	
	/**
	 * 多項式合成 f(g(x)) mod x^n を行う関数インターフェース。
	 */
	@FunctionalInterface
	public interface Composition {
		/**
		 * @param g 合成される多項式
		 * @param n 求める次数
		 * @return f(g(x)) mod x^n
		 */
		long[] apply(long[] g, int n);
		/**
		 * f'(g(x)) mod x^n を求める。
		 * f'(g(x)) = (f(g(x)))' / g'(x) として計算する。
		 * 
		 * @param poly 多項式演算器
		 * @param g 合成される多項式
		 * @param n 求める次数
		 * @return f'(g(x)) mod x^n
		 */
		default long[] diff(PolynomialFpDynamic poly, long[] g, int n) {
			if (poly.deg(g) == -1) {
				long[]ret=new long[n];
				ret[0]=apply(new long[] {0,  1}, 2)[1];
				return ret;
			}
			long[] H = apply(g, n + 1);
			long[] Hp = poly.differentiate(H);
			long[] gp = poly.differentiate(g);
			long[] fpg = poly.mul(Hp, poly.inv(Arrays.copyOf(gp, n)));
			return Arrays.copyOf(fpg, n);
		}
	}
	
	
	public record Term(int d, long v) {}

	/**
	 * 稀な多項式（項数が少ない多項式）との乗算 a(x) * sparsePoly(x) を行う。
	 * @param a 多項式
	 * @param sparsePoly 稀な多項式
	 * @return a(x) * sparsePoly(x)
	 *
	 * <p>計算量: O(NK)。ここで N = deg a, K = (sparsePoly の非ゼロ項数)。
	 */
	public long[] sparseMul(long[] a, long[] sparsePoly) {
		if (a.length == 0 || sparsePoly.length == 0) return new long[0];
		ArrayList<Term> terms = new ArrayList<>();
		for (int i = 0; i < sparsePoly.length; i++) {
			long v = fp.reduce(sparsePoly[i]);
			if (v != 0) terms.add(new Term(i, v));
		}
		return sparseMul(a, terms, sparsePoly.length);
	}

	/**
	 * 非ゼロ項がリスト形式で与えられた稀な多項式との乗算を行う。
	 * @param a 多項式
	 * @param sparseTerms 稀な多項式の非ゼロ項のリスト
	 * @param sparseLen 稀な多項式の元の長さ
	 * @return a(x) * sparsePoly(x)
	 *
	 * <p>計算量: O(NK)
	 */
	public long[] sparseMul(long[] a, ArrayList<Term> sparseTerms, int sparseLen) {
		if (a.length == 0 || sparseLen == 0) return new long[0];
		long[] res = new long[a.length + sparseLen - 1];
		if (sparseTerms.isEmpty()) return res;
		for (int i = 0; i < a.length; i++) {
			long v = fp.reduce(a[i]);
			if (v == 0) continue;
			for (Term t : sparseTerms) {
				res[i + t.d] = (res[i + t.d] + v * t.v) % mod;
			}
		}
		return res;
	}

	/**
	 * 多項式の非ゼロ項のリストを返す。
	 * @param p 多項式
	 * @return 非ゼロ項のリスト
	 */
	public ArrayList<Term> getTerms(long[] p) {
		ArrayList<Term> terms = new ArrayList<>();
		for (int i = 0; i < p.length; i++) {
			long v = fp.reduce(p[i]);
			if (v != 0) terms.add(new Term(i, v));
		}
		return terms;
	}

	/**
	 * 多項式の非ゼロ項のリストを、予測される容量を指定して返す。
	 * @param p 多項式
	 * @param initialCapacity 初期容量
	 * @return 非ゼロ項のリスト
	 */
	public ArrayList<Term> getTerms(long[] p, int initialCapacity) {
		ArrayList<Term> terms = new ArrayList<>(initialCapacity);
		for (int i = 0; i < p.length; i++) {
			long v = fp.reduce(p[i]);
			if (v != 0) terms.add(new Term(i, v));
		}
		return terms;
	}

	/**
	 * 稀な多項式（項数が少ない多項式）の逆元を O(nk) で求める。
	 * ここで n は求める次数、k は入力多項式 f の非ゼロ項数である。
	 * @param f 多項式
	 * @param n 求める次数
	 * @return f(x)^{-1} mod x^n
	 */
	public long[] sparseInv(long[] f, int n) {
		if (n <= 0) return new long[0];
		if (f.length == 0 || f[0] == 0) throw new ArithmeticException("f[0] must be non-zero");
		long[] res = new long[n];
		ArrayList<Term> terms = new ArrayList<>();
		for (int i = 1; i < f.length && i < n; i++) {
			if (f[i] != 0) terms.add(new Term(i, fp.reduce(f[i])));
		}
		long inv0 = MathUtils.modInv(fp.reduce(f[0]), mod);
		res[0] = inv0;
		for (int i = 1; i < n; i++) {
			long tmp = 0;
			for (Term t : terms) {
				if (t.d > i) break;
				tmp = (tmp + t.v * res[i - t.d]) % mod;
			}
			res[i] = tmp == 0 ? 0 : (mod - tmp) % mod * inv0 % mod;
		}
		return res;
	}

	/**
	 * 稀な多項式の対数を O(nk) で求める。
	 * ここで n は求める次数、k は入力多項式 f の非ゼロ項数である。
	 * @param f 多項式
	 * @param n 求める次数
	 * @return log(f(x)) mod x^n
	 */
	public long[] sparseLog(long[] f, int n) {
		if (n <= 0) return new long[0];
		if (f.length == 0 || f[0] == 0) throw new ArithmeticException("f[0] must be non-zero");
		long[] inv = sparseInv(f, n);
		ArrayList<Term> dfTerms = new ArrayList<>();
		for (int i = 1; i < f.length && i < n; i++) {
			if (f[i] != 0) dfTerms.add(new Term(i - 1, fp.reduce(f[i]) * i % mod));
		}
		long[] res = new long[n];
		for (int i = 0; i < n; i++) {
			if (inv[i] == 0) continue;
			for (Term t : dfTerms) {
				int j = i + t.d + 1;
				if (j >= n) break;
				res[j] = (res[j] + inv[i] * t.v % mod * MathUtils.modInv(j, mod)) % mod;
			}
		}
		return res;
	}

	/**
	 * 稀な多項式の指数関数を O(nk) で求める。
	 * ここで n は求める次数、k は入力多項式 f の非ゼロ項数である。
	 * @param f 多項式
	 * @param n 求める次数
	 * @return exp(f(x)) mod x^n
	 */
	public long[] sparseExp(long[] f, int n) {
		if (n <= 0) return new long[0];
		if (f.length > 0 && fp.reduce(f[0]) != 0) throw new ArithmeticException("f[0] must be zero");
		ArrayList<Term> dfTerms = new ArrayList<>();
		for (int i = 1; i < f.length && i < n; i++) {
			if (f[i] != 0) dfTerms.add(new Term(i - 1, fp.reduce(f[i]) * i % mod));
		}
		long[] res = new long[n];
		res[0] = 1;
		for (int i = 1; i < n; i++) {
			long tmp = 0;
			for (Term t : dfTerms) {
				if (t.d > i - 1) break;
				tmp = (tmp + t.v * res[i - 1 - t.d]) % mod;
			}
			res[i] = tmp % mod * MathUtils.modInv(i, mod) % mod;
		}
		return res;
	}

	/**
	 * 稀な多項式の平方根を O(nk) で求める。
	 * ここで n は求める次数、k は入力多項式 f の非ゼロ項数である。
	 * @param f 多項式
	 * @param n 求める次数
	 * @return sqrt(f(x)) mod x^n
	 */
	public long[] sparseSqrt(long[] f, int n) {
		if (n <= 0) return new long[0];
		int d0 = 0;
		while (d0 < f.length && d0 < n && f[d0] == 0) d0++;
		if (d0 == f.length || d0 >= n) return new long[n];
		if (d0 % 2 != 0) return null;
		long sqrt0 = MathUtils.modKthRoot(fp.reduce(f[d0]), 2, mod);
		if (sqrt0 == -1) return null;
		if (sqrt0 * 2 > mod) sqrt0 = mod - sqrt0;

		ArrayList<Term> terms = new ArrayList<>();
		long inv0 = MathUtils.modInv(fp.reduce(f[d0]), mod);
		for (int i = d0 + 1; i < f.length && i < n; i++) {
			if (f[i] != 0) terms.add(new Term(i - d0, fp.reduce(f[i]) * inv0 % mod));
		}
		long[] res = new long[n];
		int bias = d0 / 2;
		res[bias] = sqrt0;
		long inv2 = MathUtils.modInv(2, mod);
		for (int d = 0; bias + d + 1 < n; d++) {
			long tmp = 0;
			for (Term t : terms) {
				if (t.d > d + 1) break;
				int j = d - t.d;
				if (j >= 0) tmp = (tmp + mod - t.v * res[bias + j + 1] % mod * (j + 1) % mod) % mod;
				j = d - (t.d - 1);
				if (j >= 0) tmp = (tmp + t.v * t.d % mod * res[bias + j] % mod * inv2 % mod) % mod;
			}
			res[bias + d + 1] = tmp * MathUtils.modInv(d + 1, mod) % mod;
		}
		return res;
	}

	/**
	 * 疎な多項式の {@code k} 乗を求める。
	 *
	 * <p>入力多項式の非零項数を {@code m}、求める次数を {@code n} とすると、
	 * 計算量は {@code O(nm)} である。
	 *
	 * <p>返される多項式は {@code f(x)^k mod x^n} である。
	 *
	 * @param f 多項式
	 * @param n 求める項数（{@code x^n} で打ち切る）
	 * @param k 非負整数の指数
	 * @return {@code f(x)^k mod x^n}
	 */
	public long[] sparsePow(long[] f, int n, long k) {
		if (n <= 0) return new long[0];
		if (k == 0) { long[] res = new long[n]; res[0] = 1; return res; }
		int d0 = 0;
		while (d0 < f.length && d0 < n && fp.reduce(f[d0]) == 0) d0++;
		if (d0 == f.length || d0 >= n) return new long[n];
		if (d0 > 0 && (n - 1) / d0 < k) return new long[n];
		int bias = (int) (d0 * k);
		if (bias >= n) return new long[n];

		ArrayList<Term> terms = new ArrayList<>();
		for (int i = d0 + 1; i < f.length && i < n; i++) {
			long v = fp.reduce(f[i]);
			if (v != 0) terms.add(new Term(i - d0, v));
		}
		long[] res = new long[n];
		long f0 = fp.reduce(f[d0]);
		res[bias] = MathUtils.modPow(f0, k % (mod - 1), mod);
		long inv0 = fp.inv(f0);
		long kMod = k % mod;
		for (int d = 0; bias + d + 1 < n; d++) {
			long tmp = 0;
			for (Term t : terms) {
				int j = d - t.d;
				if (j >= 0) tmp = (tmp + mod - t.v * res[bias + j + 1] % mod * (j + 1) % mod) % mod;
				j = d - (t.d - 1);
				if (j >= 0) tmp = (tmp + t.v * t.d % mod * res[bias + j] % mod * kMod % mod) % mod;
			}
			res[bias + d + 1] = tmp * inv0 % mod * fp.inv(d + 1) % mod;
		}
		return res;
	}
	
	
	/**
	 * 多項式を式として表示する。
	 * @param label ラベル
	 * @param arr 多項式の係数配列
	 *
	 * <p>計算量: O(N)
	 * <p>未テスト
	 */
	public void printPolyAsExpr(String label, long[] arr) {
		System.out.println("=== " + label + " ===");
		StringBuilder sb = new StringBuilder();
		String[] vars = { "x" };
		boolean isFirst = true;

		for (int i = 0; i < arr.length; i++) {
			long coeff = fp.reduce(arr[i]);
			if (coeff == 0) continue;

			if (!isFirst) {
				sb.append(" + ");
			}

			StringBuilder varPart = new StringBuilder();
			if (i > 0) {
				varPart.append(vars[0]);
				if (i > 1) {
					varPart.append("^").append(i);
				}
				varPart.append(" ");
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

		if (isFirst) {
			System.out.println("0");
		} else {
			System.out.println(sb.toString());
		}
		System.out.println();
	}
	
	
	
	/**
	 *  [x^n] poly(x)mahler(x)mahler(x^m)mahler(x^{2m})...
	 * @param poly
	 * @param mahler
	 * @return
	 */
	public static long nthMahler(String n, int m, long[] poly, long[] mahler, long mod) {
		if (mahler[0] != 1) throw new AssertionError();
		if (m == 10) return nthMahlerBase10(n, poly, mahler);
		BigInteger nBigInt=new BigInteger(n);
		BigInteger mBigInt=BigInteger.valueOf(m);
		long[] g = Arrays.copyOf(poly, poly.length);
		while (!nBigInt.equals(BigInteger.ZERO)) {
			int r = nBigInt.mod(mBigInt).intValue();
			long[] f = PolynomialFp.mul(g, mahler);
			long[] ng = new long[1 + (f.length - 1 - r) / m];
			for (int i = r; i < f.length; i += m) {
				ng[i / m] = f[i];
			}
			g = ng;
			nBigInt = nBigInt.divide(mBigInt);
		}
		return g[0];
	}
	/**
	 *  [x^n] poly(x)mahler(x)mahler(x^m)mahler(x^{2m})...を求める。
	 *  poly, mahlerの次数が(m-1)d, d-1ならば計算量はM(d-1,(m-1)d)log_m(N)
	 *  ただし、M(a,b)は次数a,bの多項式の掛け算の計算量 
	 * @param poly
	 * @param mahler
	 * @return
	 */
	public static long nthMahlerBase10(String n, long[] poly, long[] mahler) {
		if (mahler[0] != 1) throw new AssertionError();
		int m = 10;
		long[] g = Arrays.copyOf(poly, poly.length);
		for (int i = n.length()-1; i >= 0; --i) {
			int r = (int) (n.charAt(i) - '0');
			long[] f = PolynomialFp.mul(g, mahler);
			long[] ng = new long[1 + (f.length - 1 - r) / m];
			for (int j = r; j < f.length; j += m) {
				ng[j / m] = f[j];
			}
			g = ng;
		}
		return g[0];
	}
	
	
    /**
     *  多項式f[0],f[1],..に対して
     * 	[x^n] f[0](x)f[1](x^m)f[2](x^{2m})...を求める。
     *  verified:https://atcoder.jp/contests/abc356/submissions/71206386
     * @param n
     * @param poly
     * @param mahler
     * @return
     */
	public long nthMahler(long n, long[][] h, int base) {
		long[] g = new long[] {1};
		
		for (int i = 0; i < h.length; i++) {
			if (n == 0) {
				g[0] = g[0] * h[i][0] % mod;
				continue;
			}
			int r = (int) (n % base);
			n /= base;
			long[] f = PolynomialFp.mul(g, h[i]);
			long[] ng = new long[1 + (f.length - 1 - r) / base];
			for (int j = r; j < f.length; j += base) {
				ng[j / base] = f[j];
			}
			g = ng;
		}
		return g[0];
	}
	
	
	/**
	 * ans[k] = [x^N] x^(s k) / (1 - a x^p - b x^(q k + r)) を k=0..N について返す。未テスト
	 * q>0, r>=0, p>0, s>0 を仮定する。満たさない場合はエラー。//未テスト
	 * 計算量は O(N log N)。
	 * @param N
	 * @param s
	 * @param p
	 * @param q
	 * @param r
	 * @param a
	 * @param b
	 * @return
	 */
	public long[] geometricSumOfxPlusXpowkFixingNForeachK(int N, int s, int p, int q, int r, long a, long b) {
		if (N < 0 || s <= 0 || p <= 0 || q <= 0 || r < 0) throw new AssertionError();
		a %= mod;
		if (a < 0) a += mod;
		b %= mod;
		if (b < 0) b += mod;
		long[] powA = new long[N + 1];
		long[] powB = new long[N + 1];
		powA[0] = 1;
		powB[0] = 1;
		for (int i = 0; i < N; i++) {
			powA[i + 1] = powA[i] * a % mod;
			powB[i + 1] = powB[i] * b % mod;
		}
		long[] ans = new long[N + 1];
		for (int k = 0; k <= N; k++) {
			/*
			 * x^(s k) / (1 - a x^p - b x^(q k + r))
			 * = x^(s k) Σ_{t>=0} (a x^p + b x^(q k + r))^t
			 * = x^(s k) Σ_{i,j>=0} binom(i+j, j) (a x^p)^i (b x^(q k + r))^j
			 * = Σ_{i,j>=0} binom(i+j, j) a^i b^j x^(s k + p i + j(q k + r)).
			 * したがって [x^N] に寄与するのは
			 *   s k + p i + j(q k + r) = N
			 * つまり
			 *   i = (N - s k - j(q k + r)) / p
			 * が非負整数になる j だけ。
			 *
			 * 固定した k では j を
			 *   s k + j(q k + r) <= N
			 * の範囲だけ調べるので O((N - s k) / (q k + r) + 1)。
			 * step=0 となる k=0,r=0 だけは
			 *   1 / (1 - b - a x^p)
			 * = 1/(1-b) * 1/(1 - a/(1-b) x^p)
			 * として別処理する。b=1 なら定数項が 0 なのでエラー。
			 *
			 * それ以外の総計算量は
			 *   O(N + Σ_{0<=k<=N, qk+r>0} (N-sk)/(qk+r))。
			 * q>0, r>=0, s>0 なので Σ N/(qk+r) = O(N log N)。
			 */
			long base = 1L * s * k;
			long step = 1L * q * k + r;
			if (step == 0) {
				//  1 / (1 - b - a x^p)
				// = 1/(1-b) * 1/(1 - a/(1-b) x^p)
				if (b == 1) throw new AssertionError();
				if (N % p == 0) {
					int i = N / p;
					long inv = fp.inv(1 - b);
					ans[k] = powA[i] * fp.pow(inv, i + 1) % mod;
				}
				continue;
			}
			if (base > N) continue;
			long sum = 0;
			for (long jLong = 0, deg = base; deg <= N; jLong++, deg += step) {
				// 上の式 s k + p i + j(q k + r) = N で、
				//   deg = s k + j(q k + r), rem = N - deg = p i。
				long rem = N - deg;
				if (rem % p != 0) continue;
				int i = (int) (rem / p);
				int j = (int) jLong;
				// term = binom(i+j, j) a^i b^j。
				long term = fp.comb(i + j, j) * powA[i] % mod * powB[j] % mod;
				sum += term;
				if (sum >= mod) sum -= mod;
			}
			ans[k] = sum;
		}
		return ans;
	}

	/**
	 * 剰余環 F_p[x] / (x^n) における多項式環の演算を規定する truncated polynomial strategy を返す。
	 * すべての演算結果および入力は次数 n 未満（配列の長さが n 以下）に切り詰められ、末尾の零係数は除去される。
	 *
	 * <p>事前条件:
	 * <ul>
	 *   <li>n >= 0</li>
	 * </ul>
	 *
	 * <p>事後条件:
	 * <ul>
	 *   <li>戻り値は CommutativeRingStrategy&lt;long[]&gt; を実装する。</li>
	 *   <li>各演算の戻り値の配列の長さは Math.max(0, deg(result) + 1) であり、n 以下である。</li>
	 * </ul>
	 *
	 * <p>計算量:
	 * <ul>
	 *   <li>zero(): O(1)</li>
	 *   <li>one(): O(1)</li>
	 *   <li>add(a, b): O(n)</li>
	 *   <li>sub(a, b): O(n)</li>
	 *   <li>mul(a, b): O(n log n) (または非 NTT フレンドリーな場合 O(n log n) / O(n^2))</li>
	 *   <li>neg(a): O(n)</li>
	 *   <li>equals(a, b): O(n)</li>
	 * </ul>
	 *
	 * @param n 切り捨て次数境界。多項式は x^n で割った余りに切り捨てられる。
	 * @return truncated polynomial strategy
	 * @throws IllegalArgumentException n < 0 の場合
	 */
	// 未テスト
	public CommutativeRingStrategy<long[]> truncatedStrategy(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("n must be non-negative");
		}
		return new CommutativeRingStrategy<long[]>() {
			private long[] truncateAndTrim(long[] a, int limit) {
				if (a.length <= limit) {
					return a;
				}
				return Arrays.copyOf(a, limit);
			}

			@Override
			public long[] zero() {
				return PolynomialFpDynamic.this.zero();
			}

			@Override
			public long[] one() {
				return truncateAndTrim(PolynomialFpDynamic.this.one(), n);
			}

			@Override
			public long[] add(long[] a, long[] b) {
				return truncateAndTrim(PolynomialFpDynamic.this.add(truncateAndTrim(a, n), truncateAndTrim(b, n)), n);
			}

			@Override
			public long[] sub(long[] a, long[] b) {
				return truncateAndTrim(PolynomialFpDynamic.this.sub(truncateAndTrim(a, n), truncateAndTrim(b, n)), n);
			}

			@Override
			public long[] mul(long[] a, long[] b) {
				return truncateAndTrim(PolynomialFpDynamic.this.mul(truncateAndTrim(a, n), truncateAndTrim(b, n)), n);
			}

			@Override
			public long[] neg(long[] a) {
				return truncateAndTrim(PolynomialFpDynamic.this.neg(truncateAndTrim(a, n)), n);
			}

			@Override
			public boolean equals(long[] a, long[] b) {
				return PolynomialFpDynamic.this.equals(truncateAndTrim(a, n), truncateAndTrim(b, n));
			}
		};
	}

	/**
	 * 有理関数 P(x)/Q(x) の、次数 k から k+m-1 までの連続する m 個の係数 [x^i] P(x)/Q(x) を一括して計算する。
	 *
	 * 数学的仕様:
	 * 戻り値の配列の i 番目の要素（0 <= i < m）は、形式的べき級数 P(x)/Q(x) の x^(k+i) の係数に等しい。
	 *
	 * 計算量: O((m + d log m) log(m + d) + d log d log k)。ただし d = max(deg P, deg Q)。
	 *
	 * @param P 分子多項式 P(x)
	 * @param Q 分母多項式 Q(x)
	 * @param k 開始項 of sequence k
	 * @param m 求める項数 m
	 * @return 第 k 項から第 k+m-1 項までの係数の配列
	 */
	// 未テスト
	public long[] consecutiveTermsOfRationalFunction(long[] P, long[] Q, long k, int m) {
		if (k < 0 || m < 0) throw new AssertionError();
		if (m == 0) return new long[0];
		if (Q.length == 0 || fp.reduce(Q[0]) != 1) {
			throw new IllegalArgumentException("Q[0] must be congruent to 1");
		}

		int degP = deg(P);
		long start = k - (degP == -1 ? 0 : degP);
		long l = Math.max(0L, start) - 1;
		long r = k + m - 1;

		long[] C = consecutiveTermsOfInv(Q, l, r);
		long[] prod = mul(P, C);//prodの添え字+(l+1)=元の次数
		return Arrays.copyOfRange(prod, (int) (k - l - 1), (int) (k - l - 1 + m));
	}

	/**
	 * 形式的べき級数 1/F(x) の、半開区間 (l, r] に属する次数の係数一式を一括して計算する。
	 *
	 * 数学的仕様:
	 * 戻り値の配列の i 番目の要素（0 <= i < r - l）は、1/F(x) の x^(l+1+i) の係数に等しい。
	 *
	 * 計算量: O((m + d log m) log(m + d))。ただし d = deg F, m = r - l。
	 *
	 * @param F 多項式 F(x)
	 * @param l 区間の左端 l
	 * @param r 区間の右端 r
	 * @return 区間 (l, r] に属する次数の係数の配列
	 */
	// 未テスト
	private long[] consecutiveTermsOfInv(long[] F, long l, long r) {
		if (F.length == 0 || fp.reduce(F[0]) != 1) {
			throw new IllegalArgumentException("F[0] must be congruent to 1");
		}
		int d = deg(F);
		if (d == -1) {
			throw new ArithmeticException("Division by zero polynomial");
		}
		if (l >= r) {
			return new long[0];
		}
		if (d == 0 || r <= 0) {
		    long[] res = new long[(int) (r - l)];
		    if (r == 0) {
		        res[(int) (-l - 1)] = 1;
		    }
		    return res;
		}
		//  [x^(l:r]] 1/F(x)
		// =[x^(l:r]] F(-x)/F(x)F(-x)
		// =[x^(l:r]] F(-x)/V(x^2)
		
		// d=deg(F)として
		// l+1 <= 2i+d 
		// (l+1-d)/2 <= i
		// (l-1-d)/2+1 <= i
		// lprime + 1 <= i
		long[] F_neg = negatedX(F);
		long[] prod = mul(F, F_neg);
		long[] V = new long[(prod.length + 1) / 2];
		for (int i = 0; i < V.length; i++) {
			V[i] = prod[2 * i];
		}
		long lPrime = Math.ceilDiv(l-1-d, 2);
		long rPrime = Math.floorDiv(r, 2);

		long[] C = consecutiveTermsOfInv(V, lPrime, rPrime);

		int bSize = (int) (r - (l - d));
		long[] B = new long[bSize];//B[i]= [x^(l-d+1+i)] 1/V(x^2)

		for (int i = 0; i < C.length; i++) {
		    long degree = 2 * (lPrime + 1 + i);//C[i]の次数
		    int idx = (int) (degree - (l - d + 1));
		    if (0 <= idx && idx < bSize) {
		        B[idx] = C[i];
		    }
		}
		int size = (int) (r - l);
		long[] G = negatedX(F);
		long[] conv = mul(G, B);
		if (conv.length == size) return conv;
		else return Arrays.copyOfRange(conv, d, d + size);
	}

	/**
	 * Bostan-Mori 法を用いて x^k mod F(x) を計算する。
	 *
	 * 戻り値の多項式 R(x) は、x^k = Q(x) F(x) + R(x) かつ deg(R) < deg(F) を満たす。
	 *
	 * 計算量: O(d log d log k)。ただし d = deg F。
	 *
	 * @param k 冪数 k >= 0
	 * @param F 除数多項式 F(x)
	 * @return x^k mod F(x) の係数配列
	 */
	// 未テスト
	public long[] xpowMod(long k, long[] F) {
		if (k < 0) throw new IllegalArgumentException("k must be non-negative");
		int d = deg(F);
		if (d == -1) {
			throw new ArithmeticException("Division by zero polynomial");
		}
		if (d == 0) {
			return new long[0];
		}
		if (k < d) {
			long[] res = new long[(int) k + 1];
			res[(int) k] = 1;
			return res;
		}
		// Fが与えられたとき 
		// x^k = Q(x) F(x) + R(x) となる R を求めたい。
		// xを1/xで置き換えると 
		// 1 = x^kQ(1/x)F(1/x)+x^kR(1/x)
		// 1 = revQ revF +x^{k-d+1}(d次未満の多項式)
		// revQ = revF^{-1} mod x^{k-d+1}
		// よって、revF^{-1} = revQ + x^{k-d+1}Hと置けて
		// 1 = (revF^{-1} - x^{k-d+1}H) revF + x^k R(1/x)
		// H revF = x^{d-1} R(1/x)
		long[] F_rev = F.clone();
		ArrayUtils.reverse(F_rev);

		long fd = fp.reduce(F[d]);
		long inv_fd = fp.inv(fd);

		long[] G = new long[d + 1];
		for (int i = 0; i <= d; i++) {
			G[i] = F_rev[i] * inv_fd % mod;
			if (G[i] < 0) G[i] += mod;
		}
		// 1/G(x) の x^{k-d+1}, x^{k-d+2}, …, x^{k} の d 個の連続する係数
		long[] terms = consecutiveTermsOfInv(G, k - d, k+1);
		for (int i = 0; i < terms.length; i++) {
			terms[i] = terms[i] * inv_fd % mod;
		}
		long[] R_rev = mul(terms, F_rev);
		long[] ret=new long[d];
		for (int i = 0; i < R_rev.length; i++) {
			// d-1-j=i
			// j=d-1-i
			if (d-1-i >= 0) ret[i]=R_rev[d-1-i];
		}
		return resize(ret);
	}

	/**
	 * Bostan-Mori 法を用いて (x^k - 1)/(x - 1) mod F(x) を計算する。
	 *
	 * 戻り値の多項式 R(x) は、sum_{i=0}^{k-1} x^i = Q(x) F(x) + R(x) かつ deg(R) < deg(F) を満たす。
	 *
	 * 計算量: O(d log d log k)。ただし d = deg F。
	 *
	 * @param k 冪数 k >= 0
	 * @param F 除数多項式 F(x)
	 * @return (x^k - 1)/(x - 1) mod F(x) の係数配列
	 */
	// 未テスト
	public long[] geometricSumMod(long k, long[] F) {
		if (k < 0) throw new IllegalArgumentException("k must be non-negative");
		int d = deg(F);
		if (d == -1) {
			throw new ArithmeticException("Division by zero polynomial");
		}
		if (d == 0) {
			return new long[0];
		}
		if (k < d) {
			long[] res = new long[(int) k];
			Arrays.fill(res, 1);
			return res;
		}

		// P(x) = F(x) * (x - 1)
		long[] P = new long[d + 2];
		for (int i = 0; i <= d + 1; i++) {
			long f_prev = (i > 0 && i - 1 <= d) ? F[i - 1] : 0;
			long f_curr = (i <= d) ? F[i] : 0;
			P[i] = (f_prev - f_curr) % mod;
			if (P[i] < 0) P[i] += mod;
		}

		long[] A = xpowMod(k, P);

		int len = Math.max(1, A.length);
		long[] R = new long[len];
		System.arraycopy(A, 0, R, 0, A.length);
		R[0] = (R[0] - 1 + mod) % mod;

		long[] T = new long[R.length - 1];
		long cur = 0;
		for (int i = 0; i < T.length; i++) {
			cur = (cur - R[i]) % mod;
			if (cur < 0) cur += mod;
			T[i] = cur;
		}

		return resize(T);
	}

	/**
	 * A(x)Y(x) = B(x) mod (x^N - 1) において、
	 * 与えられた A, N, B mod A, Blow = B mod x^L から
	 * Y mod x^L を計算する。
	 *
	 * A(0) != 0 を仮定する。
	 *
	 * 計算量: O(M(k) log N + kL)。ただし k = deg A。
	 *
	 * @param A 多項式 A(x) の係数配列
	 * @param N 法指数 N
	 * @param BmodA B mod A の係数配列
	 * @param Blow (B mod (x^N - 1))_{<L} の係数配列
	 * @param L 求める Y の項数 L
	 * @return Y_{<L} の係数配列。解が存在しない場合は null
	 * @throws IllegalArgumentException A(0) == 0 の場合、または N <= 0, L <= 0 の場合
	 */
	// 未テスト
	public long[] solveCyclicCongruenceLowTerms(long[] A, long N, long[] BmodA, long[] Blow, int L) {
		if (N <= 0) {
			throw new IllegalArgumentException("N must be positive");
		}
		if (L <= 0) {
			throw new IllegalArgumentException("L must be positive");
		}
		if (A.length == 0 || fp.reduce(A[0]) == 0) {
			throw new IllegalArgumentException("A(0) must be non-zero");
		}
		if (BmodA == null || Blow == null) {
			throw new NullPointerException("BA and Blow must not be null");
		}
		//A(x)Y(x) = B(x) + Q(x)(x^N - 1)
		long[] r = xpowMod(N, A);
		r[0] = (mod - 1 + r[0]) % mod;
		//Q・r ≡ -B   (mod A)
		long[] Q = solveLinearCongruence(r, neg(BmodA), A);
		if (Q == null) {
			return null;
		}
		//mod x^L で見ると K(x)・x^N は消えて
		//A(x)Y(x) ≡ B(x) - Q(x)   (mod x^L)
		
		var Y=mul(sub(Blow, Q), inv(Arrays.copyOf(A, L)));
		return Arrays.copyOf(Y, L);
	}

	/**
	 * AY ≡ B (mod x^N - 1) を満たす最小次数解 Y の [x^R] Y を計算する。
	 * 
	 * k = deg A としたとき、計算量 O(M(k) log N + M(k) log R + M(k) log k)
	 *
	 * @param A 多項式 A(x) の係数配列
	 * @param N 法指数 N
	 * @param BA B mod A の係数配列
	 * @param BleqR_modA (B mod x^{R+1}) mod A の係数配列
	 * @param R 求めたい係数の次数 R
	 * @return 第 R 係数 Y_R
	 */
	// 未テスト
	public long solveCyclicCongruencePoint(long[] A, long N, long[] BA, long[] BleqR_modA, long R) {
		if (N <= 0) {
			throw new IllegalArgumentException("N must be positive");
		}
		if (A.length == 0 || fp.reduce(A[0]) == 0) {
			throw new IllegalArgumentException("A(0) must be non-zero");
		}
		int k = deg(A);
		if (k <= 0) {
			throw new IllegalArgumentException("deg(A) must be positive");
		}
		if (R < 0 || R >= N) {
			return 0L;
		}
		long[] r = xpowMod(N, A);
		long[] r_minus_1 = r.clone();
		if (r_minus_1.length == 0) {
			r_minus_1 = new long[]{0};
		}
		r_minus_1[0] = subMod(r_minus_1[0], 1);
		// AY ≡ B (mod r - 1)
		// AY = B + (r - 1)Q
		// Q = (-B mod A) (r - 1)^{-1} mod A 
		
		
		long[] Q = solveLinearCongruence(r_minus_1, neg(BA), A);
		if (Q == null) {
			throw new ArithmeticException("No solution for Q exists");
		}
		
		// Y = (B - Q) / A (mod x^N)
		// B を B mod x^{R+1} mod A で置き換えてよい
		long[] R0 = sub(BleqR_modA, Q);
		long invA0 = fp.inv(fp.reduce(A[0]));
		long[] normA = mul(A, invA0);
		long[] normR0 = mul(R0, invA0);
		return nth(R, normR0, normA);
	}

	/**
	 * オンラインで与えられる多項式 A(x) の乗法逆元 B(x) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineInv {
		private final PolynomialFpDynamic fp;
		private final long mod;
		private final OnlineConvolution cnv;
		private long inv_a0;
		private long[] f;
		private int i = 0;

		public OnlineInv(int N, PolynomialFpDynamic fp) {
			this.fp = fp;
			this.mod = fp.mod;
			this.cnv = new OnlineConvolution(N, fp);
			this.f = new long[N];
		}

		public long append(long a_i) {
			if (i == 0) {
				if (a_i == 0) {
					throw new ArithmeticException("Division by zero (constant term is zero)");
				}
				f[0] = fp.getFp().inv(a_i);
				inv_a0 = mod - f[0];
				i++;
				return f[0];
			}
			long S_i_minus_1 = cnv.append(a_i, f[i - 1]);
			f[i] = S_i_minus_1 * inv_a0 % mod;
			i++;
			return f[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 A(x) の自然対数 B(x) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineLog {
		private final PolynomialFpDynamic fp;
		private final long mod;
		private final OnlineConvolution cnv;
		private long[] b;
		private long[] H;
		private long a_prev;
		private int i = 0;

		public OnlineLog(int N, PolynomialFpDynamic fp) {
			this.fp = fp;
			this.mod = fp.mod;
			this.cnv = new OnlineConvolution(N, fp);
			this.b = new long[N];
			this.H = new long[N];
		}

		public long append(long a_i) {
			if (i == 0) {
				if (fp.getFp().reduce(a_i) != 1) {
					throw new IllegalArgumentException("constant term must be 1");
				}
				b[0] = 0;
				a_prev = a_i;
				i++;
				return 0;
			}
			if (i == 1) {
				H[0] = a_i;
				b[1] = H[0];
				a_prev = a_i;
				i++;
				return b[1];
			}
			long S_i_minus_2 = cnv.append(H[i - 2], a_prev);
			H[i - 1] = (i * a_i - S_i_minus_2 + mod) % mod;
			b[i] = H[i - 1] * fp.getFp().inv(i) % mod;
			a_prev = a_i;
			i++;
			return b[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 A(x) の指数関数 B(x) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineExp {
		private final PolynomialFpDynamic fp;
		private final long mod;
		private final OnlineConvolution cnv;
		private long[] f;
		private int i = 0;

		public OnlineExp(int N, PolynomialFpDynamic fp) {
			this.fp = fp;
			this.mod = fp.mod;
			this.cnv = new OnlineConvolution(N, fp);
			this.f = new long[N];
		}

		public long append(long a_i) {
			if (i == 0) {
				if (fp.getFp().reduce(a_i) != 0) {
					throw new IllegalArgumentException("constant term must be 0");
				}
				f[0] = 1;
				i++;
				return f[0];
			}
			long D_i_minus_1 = i * a_i % mod;
			long S_i_minus_1 = cnv.append(D_i_minus_1, f[i - 1]);
			f[i] = S_i_minus_1 * fp.getFp().inv(i) % mod;
			i++;
			return f[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 A(x) の平方根 B(x) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineSqrt {
		private final PolynomialFpDynamic fp;
		private final long mod;
		private final OnlineConvolution cnv;
		private long[] f;
		private long inv_2f0;
		private long f0;
		private int i = 0;

		public OnlineSqrt(int N, PolynomialFpDynamic fp) {
			this.fp = fp;
			this.mod = fp.mod;
			this.cnv = new OnlineConvolution(N, fp);
			this.f = new long[N];
		}

		public long append(long a_i) {
			if (i == 0) {
				f0 = MathUtils.modKthRoot(a_i, 2, mod);
				if (f0 == -1) {
					throw new IllegalArgumentException("no square root exists");
				}
				f[0] = f0;
				inv_2f0 = fp.getFp().inv(2 * f0 % mod);
				i++;
				return f0;
			}
			if (i == 1) {
				f[1] = a_i * inv_2f0 % mod;
				i++;
				return f[1];
			}
			long S_i_minus_2 = cnv.append(f[i - 1], f[i - 1]);
			f[i] = (a_i - S_i_minus_2 + mod) % mod * inv_2f0 % mod;
			i++;
			return f[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 A(x) の m 乗 B(x) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlinePow {
		private final long m;
		private final PolynomialFpDynamic fp;
		private final long mod;
		private OnlineLog logSolver;
		private OnlineExp expSolver;
		private long f0;
		private long inv_a0;
		private int i = 0;

		public OnlinePow(int N, long m, PolynomialFpDynamic fp) {
			this.m = m;
			this.fp = fp;
			this.mod = fp.mod;
			this.logSolver = new OnlineLog(N, fp);
			this.expSolver = new OnlineExp(N, fp);
		}

		public long append(long a_i) {
			if (i == 0) {
				if (a_i == 0) {
					throw new ArithmeticException("constant term must be non-zero");
				}
				f0 = MathUtils.modPow(a_i, m % (mod - 1), mod);
				inv_a0 = fp.getFp().inv(a_i);
				logSolver.append(1);
				expSolver.append(0);
				i++;
				return f0;
			}
			long scaled_a_i = a_i * inv_a0 % mod;
			long L_i = logSolver.append(scaled_a_i);
			long mL_i = L_i * (m % mod) % mod;
			long E_i = expSolver.append(mL_i);
			long f_i = E_i * f0 % mod;
			i++;
			return f_i;
		}
	}

	/**
	 * 固定された多項式 g(x) と、オンラインで与えられる多項式 f(x) の合成 H(x) = f(g(x)) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^3 N)
	 */
	// 未テスト
	public static class SemiOnlineComposition {
		private final PolynomialFpDynamic polygen;
		private final long mod;
		private final int N;
		private final long[] g;
		private final long[] f;
		private final long[] H;
		private int i = 0;

		private SemiOnlineComposition firstHalf;
		private SemiOnlineComposition subComposition;
		private SemiOnlineConvolution semiConv;
		private long[] f0_g0;

		public SemiOnlineComposition(int N, long[] g, PolynomialFpDynamic fp) {
			this.N = N;
			this.polygen = fp;
			this.mod = fp.mod;
			this.g = Arrays.copyOf(g, N);
			this.f = new long[N];
			this.H = new long[N];
			if (N > 1) {
				int M = (N + 1) / 2;
				this.firstHalf = new SemiOnlineComposition(M, Arrays.copyOf(g, M), fp);
			}
		}

		public long append(long f_i) {
			f[i] = f_i;
			if (N == 1) {
				H[0] = f_i;
				i++;
				return H[0];
			}
			int M = (N + 1) / 2;
			int K = N - M;
			if (i < M) {
				H[i] = firstHalf.append(f_i);
			} else {
				if (i == M) {
					long[] f0 = Arrays.copyOf(f, M);
					long[] g0 = Arrays.copyOf(g, M);

					f0_g0 = polygen.comp(f0, g0, N);
					for (int j = M; j < N; j++) {
						H[j] = f0_g0[j];
					}

					long[] f0_prime = polygen.differentiate(f0);
					long[] D = polygen.comp(f0_prime, g0, K);

					long[] g1 = new long[K];
					for (int j = 0; j < K; j++) {
						g1[j] = g[M + j];
					}
					long[] D_g1 = polygen.mul(D, g1);
					for (int j = M; j < N && j - M < D_g1.length; j++) {
						H[j] = (H[j] + D_g1[j - M]) % mod;
					}

					long[] hat_g0 = new long[K];
					for (int j = 1; j < Math.min(M, K + 1); j++) {
						hat_g0[j - 1] = g[j];
					}
					long[] P_g = Arrays.copyOf(polygen.pow(hat_g0, (long) M), K);

					subComposition = new SemiOnlineComposition(K, Arrays.copyOf(g0, K), polygen);
					semiConv = new SemiOnlineConvolution(P_g, K, polygen);
				}
				long comp_out = subComposition.append(f_i);
				long contribution = semiConv.append(comp_out);
				H[i] = (H[i] + contribution) % mod;

				if (i == 2 * M - 1) {
					long extra = (long) M * polygen.fp.pow(g[1], M - 1) % mod * f[M] % mod * g[M] % mod;
					H[i] = (H[i] + extra) % mod;
				}
			}
			i++;
			return H[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 f(x) と g(x) の合成 H(x) = f(g(x)) mod x^N を逐次計算する。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^3 N)
	 */
	// 未テスト
	public static class OnlineComposition {
		private final PolynomialFpDynamic polyfactory;
		private final long mod;
		private final int N;
		private final int M;
		private final long[] f;
		private final long[] g;
		private final long[] H;
		private int i = 0;

		private OnlineComposition firstHalf;
		private SemiOnlineComposition semiComp;
		private SemiOnlineConvolution semiConv1;
		private SemiOnlineConvolution semiConv2;
		private long[] f0_g0;

		public OnlineComposition(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.polyfactory = fp;
			this.mod = fp.mod;
			this.M = (N + 1) / 2;
			this.f = new long[N];
			this.g = new long[N];
			this.H = new long[N];
			if (N > 1) {
				this.firstHalf = new OnlineComposition(M, fp);
			}
		}

		public long append(long f_i, long g_i) {
			//m=n/2でf=f0+x^m f1,g=g0+x^m g1として
			// f(g)=f0(g0)+x^m[(g0/x)^m f1(g0)+f0'(g0)g1]。
			// 前半確定後、semi合成f1(g0)とsemi積f0'(g0)*g1を分割統治。O(n log^3 n)
			f[i] = f_i;
			g[i] = g_i;
			if (i == 0 && g[0]!= 0) throw new IllegalArgumentException("g0 must be 0");

			if (N == 1) {
				H[0] = f_i;
				i++;
				return H[0];
			}

			int K = N - M;

			if (i < M) {
				H[i] = firstHalf.append(f_i, g_i);
			} else {
				if (i == M) {
					//f(g)=f0(g0)+x^m[(g0/x)^m f1(g0)+f0'(g0)g1]
					//のx^m[]のうち既知の部分を計算していく
					
					// [x^[m..n)] f0(g0)を計算
					long[] f0 = Arrays.copyOf(f, M);
					long[] g0 = Arrays.copyOf(g, M);

					f0_g0 = polyfactory.comp(f0, g0, N);
					for (int j = M; j < N; j++) {
						H[j] = f0_g0[j];
					}
					// D = f0'(g0) mod x^m
					long[] f0_prime = polyfactory.differentiate(f0);
					long[] D = polyfactory.comp(f0_prime, g0, K);

					long[] hat_g0 = new long[K];
					for (int j = 1; j < Math.min(M, K + 1); j++) {
						hat_g0[j - 1] = g[j];
					}
					long[] P_g = Arrays.copyOf(polyfactory.pow(hat_g0, (long) M), K);

					semiComp = new SemiOnlineComposition(K, Arrays.copyOf(g0, K), polyfactory);
					semiConv1 = new SemiOnlineConvolution(D, K, polyfactory);
					semiConv2 = new SemiOnlineConvolution(P_g, K, polyfactory);
				}

				long term1 = semiConv1.append(g_i);
				long comp_out = semiComp.append(f_i);
				long term2 = semiConv2.append(comp_out);

				H[i] = (H[i] + term1 + term2) % mod;

				if (i == 2 * M - 1) {
					//(g0+x^m g1)^m を展開したときの m g0^{m-1}x^m g1f1(g0)
					long extra = (long) M * polyfactory.fp.pow(g[1], M - 1) % mod * f[M] % mod * g[M] % mod;
					H[i] = (H[i] + extra) % mod;
				}
			}

			i++;
			return H[i - 1];
		}
	}

	/**
	 * オンラインで与えられる多項式 f(x) の Plethystic exponential (PE) を逐次計算する。
	 * f(x) の Plethystic exponential は以下で定義される。
	 * PE[f](x) = exp( sum_{k=1}^{N-1} f(x^k) / k ) mod x^N
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlinePlethysticExponential {
		//https://atcoder.jp/contests/abc230/submissions/77934789
		/** 多項式演算器 */
		private final PolynomialFpDynamic fp;
		/** 法 */
		private final long mod;
		/** 求める次数境界 */
		private final int N;
		/** オンライン指数関数ソルバ */
		private final OnlineExp expSolver;
		/** 中間多項式 g の係数配列 */
		private final long[] g;
		/** 現在の次数 */
		private int i = 0;

		/**
		 * 指定された次数境界 N でソルバを構築する。
		 * @param N 次数境界
		 * @param fp 多項式演算器
		 */
		public OnlinePlethysticExponential(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.fp = fp;
			this.mod = fp.mod;
			this.expSolver = new OnlineExp(N, fp);
			this.g = new long[N];
		}

		/**
		 * 多項式 f(x) の第 i 項目 f_i を追加し、PE[f](x) の第 i 項目を計算して返す。
		 * 事前条件: f[0] = 0 mod mod
		 * @param f_i 追加する項の係数
		 * @return PE[f](x) の第 i 項の係数
		 * @throws IllegalArgumentException f[0] != 0 mod mod の場合
		 */
		public long append(long f_i) {
			if (i == 0) {
				if (fp.getFp().reduce(f_i) != 0) {
					throw new IllegalArgumentException("f[0] must be 0");
				}
				i++;
				return expSolver.append(0);
			}
			long val = fp.getFp().reduce(f_i);
			g[i] = (g[i] + val) % mod;
			for (int k = 2; i * k < N; k++) {
				long term = val * fp.getFp().inv(k) % mod;
				g[i * k] = (g[i * k] + term) % mod;
			}
			long res = expSolver.append(g[i]);
			i++;
			return res;
		}
	}

	/**
	 * オンラインで与えられる多項式 f(x) の Plethystic logarithm (PL) を逐次計算する。
	 * PL[f](x) = sum_{k=1}^{N-1} (mu(k) / k) * ln(f(x^k)) mod x^N
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlinePlethysticLogarithm {
		/** 多項式演算器 */
		private final PolynomialFpDynamic fp;
		/** 法 */
		private final long mod;
		/** 求める次数境界 */
		private final int N;
		/** オンライン対数関数ソルバ */
		private final OnlineLog logSolver;
		/** メビウス反転用の中間係数配列 */
		private final long[] H;
		/** 現在の次数 */
		private int i = 0;

		/**
		 * 指定された次数境界 N でソルバを構築する。
		 * @param N 次数境界
		 * @param fp 多項式演算器
		 */
		public OnlinePlethysticLogarithm(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.fp = fp;
			this.mod = fp.mod;
			this.logSolver = new OnlineLog(N, fp);
			this.H = new long[N];
		}

		/**
		 * 多項式 f(x) の第 i 項目 f_i を追加し、PL[f](x) の第 i 項目を計算して返す。
		 * 事前条件: f[0] = 1 mod mod
		 * @param f_i 追加する項の係数
		 * @return PL[f](x) の第 i 項の係数
		 * @throws IllegalArgumentException f[0] != 1 mod mod の場合
		 */
		public long append(long f_i) {
			if (i == 0) {
				if (fp.getFp().reduce(f_i) != 1) {
					throw new IllegalArgumentException("f[0] must be 1");
				}
				logSolver.append(f_i);
				i++;
				return 0;
			}
			long g_i = logSolver.append(f_i);
			long G_i = g_i * i % mod;
			H[i] = (H[i] + G_i) % mod;
			for (int k = 2; i * k < N; k++) {
				H[i * k] = (H[i * k] - H[i] + mod) % mod;
			}
			long res = H[i] * fp.getFp().inv(i) % mod;
			i++;
			return res;
		}
	}

	/**
	 * オンラインで与えられる多項式 a(x) の Cycle Plethystic exponential を逐次計算する。
	 * CYC_PE[a](x) = sum_{k=1}^{N-1} (phi(k) / k) * -ln(1 - a(x^k)) mod x^N
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineCyclePlethysticExponential {
		/** 多項式演算器 */
		private final PolynomialFpDynamic fp;
		/** 法 */
		private final long mod;
		/** 求める次数境界 */
		private final int N;
		/** オンライン対数関数ソルバ */
		private final OnlineLog logSolver;
		/** 中間多項式 B の係数配列 */
		private final long[] B;
		/** 現在の次数 */
		private int i = 0;

		/**
		 * 指定された次数境界 N でソルバを構築する。
		 * @param N 次数境界
		 * @param fp 多項式演算器
		 */
		public OnlineCyclePlethysticExponential(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.fp = fp;
			this.mod = fp.mod;
			this.logSolver = new OnlineLog(N, fp);
			this.B = new long[N];
			library.util.Sieve.expandPrimes(N);
		}

		/**
		 * 多項式 a(x) の第 i 項目 a_i を追加し、CYC_PE[a](x) の第 i 項目を計算して返す。
		 * 事前条件: a[0] = 0 mod mod
		 * @param a_i 追加する項の係数
		 * @return CYC_PE[a](x) の第 i 項の係数
		 * @throws IllegalArgumentException a[0] != 0 mod mod の場合
		 */
		public long append(long a_i) {
			if (i == 0) {
				if (fp.getFp().reduce(a_i) != 0) {
					throw new IllegalArgumentException("a[0] must be 0");
				}
				logSolver.append(1);
				i++;
				return 0;
			}
			long u_i = (mod - fp.getFp().reduce(a_i)) % mod;
			long L_i = logSolver.append(u_i);
			long C_i = (mod - L_i) % mod;
			B[i] = (B[i] + C_i) % mod;
			for (int k = 2; i * k < N; k++) {
				long phiK = library.util.Sieve.totient(k) % mod;
				long termCoeff = phiK * fp.getFp().inv(k) % mod;
				B[i * k] = (B[i * k] + termCoeff * C_i) % mod;
			}
			long res = B[i];
			i++;
			return res;
		}
	}

	/**
	 * オンラインで与えられる多項式 b(x) の Cycle Plethystic logarithm (CYC_PL) を逐次計算する。
	 * CYC_PE[CYC_PL[b]](x) = b(x) mod x^N を満たす。
	 *
	 * <p>計算量: 1項追加あたり平均 O(log^2 N)
	 */
	// 未テスト
	public static class OnlineCyclePlethysticLogarithm {
		/** 多項式演算器 */
		private final PolynomialFpDynamic fp;
		/** 法 */
		private final long mod;
		/** 求める次数境界 */
		private final int N;
		/** オンライン指数関数ソルバ */
		private final OnlineExp expSolver;
		/** メビウス反転のための累積和 */
		private final long[] acc;
		/** 現在の次数 */
		private int i = 0;

		/**
		 * 指定された次数境界 N でソルバを構築する。
		 * @param N 次数境界
		 * @param fp 多項式演算器
		 */
		public OnlineCyclePlethysticLogarithm(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.fp = fp;
			this.mod = fp.mod;
			this.expSolver = new OnlineExp(N, fp);
			this.acc = new long[N];
			library.util.Sieve.expandPrimes(N);
		}

		/**
		 * 多項式 b(x) の第 i 項目 b_i を追加し、CYC_PL[b](x) の第 i 項目を計算して返す。
		 * 事前条件: b[0] = 0 mod mod
		 * @param b_i 追加する項の係数
		 * @return CYC_PL[b](x) の第 i 項の係数
		 * @throws IllegalArgumentException b[0] != 0 mod mod の場合
		 */
		public long append(long b_i) {
			if (i == 0) {
				if (fp.getFp().reduce(b_i) != 0) {
					throw new IllegalArgumentException("b[0] must be 0");
				}
				expSolver.append(0);
				i++;
				return 0;
			}
			long Bprime_i = fp.getFp().reduce(b_i) * i % mod;
			long Cprime_i = (Bprime_i - acc[i] + 2 * mod) % mod;
			long C_i = Cprime_i * fp.getFp().inv(i) % mod;
			long D_i = (mod - C_i) % mod;

			long E_i = expSolver.append(D_i);
			long a_i = (mod - E_i) % mod;

			for (int j = 2; i * j < N; j++) {
				long phiJ = library.util.Sieve.totient(j) % mod;
				long val = phiJ * Cprime_i % mod;
				acc[i * j] = (acc[i * j] + val) % mod;
			}

			i++;
			return a_i;
		}
	}

	public static class OnlinePowerProjection {
		private final PolynomialFpDynamic fp; // 多項式演算器
		private final long mod; // 法
		private final int N; // 求める項数
		private final long[] W; // W(x) の係数配列
		private final long[] g; // g(x) の係数配列
		final long[] R; // 計算結果を格納する配列
		int cnt = 0; // 現在処理中の項数

		private final int M;
		private final int K;
		private final OnlinePowerProjection firstHalf;
		private final OnlinePow hSolver;
		private final OnlineConvolution A_solver;

		/**
		 * 指定されたサイズ N と多項式演算器で OnlinePowerProjection を構築する。
		 *
		 * @param N 求める項数
		 * @param fp 多項式演算器
		 */
		// 未テスト
		public OnlinePowerProjection(int N, PolynomialFpDynamic fp) {
			this.N = N;
			this.fp = fp;
			this.mod = fp.mod;
			this.W = new long[N];
			this.g = new long[N];
			this.R = new long[N];
			this.M = (N + 1) / 2;
			this.K = N - M;
			if (N > 1) {
				this.firstHalf = new OnlinePowerProjection(K, fp);
				this.hSolver = new OnlinePow(K, M, fp);
				this.A_solver = new OnlineConvolution(K, fp);
			} else {
				this.firstHalf = null;
				this.hSolver = null;
				this.A_solver = null;
			}
		}

		/**
		 * W_i と g_i を追加し、新しく確定した結果の項を返す。
		 *
		 * <p>
		 * 計算量: 1項追加あたり均し O(log^2 N)
		 * </p>
		 *
		 * @param W_i W(x) の N - 1 - i 次の係数
		 * @param g_i g(x) の i + 1 次の係数
		 * @return 新しく確定した項
		 */
		// 未テスト
		public long append(long W_i, long g_i) {
			if (g[0] != 0) {
				throw new IllegalArgumentException("g0 must be 0");
			}
			if (cnt >= N) {
				return 0;
			}
			W[N - 1 - cnt] = (W_i % mod + mod) % mod;
			if (cnt + 1 < N) {
				g[cnt + 1] = (g_i % mod + mod) % mod;
			}

			if (N == 1) {
				R[0] = W_i;
				cnt++;
				return W_i;
			}

			if (cnt < K) {
				long h_cnt = hSolver.append(g_i);
				long A_cnt = A_solver.append(h_cnt, (W_i % mod + mod) % mod);
				long firstHalf_val = firstHalf.append(A_cnt, g_i);
				R[N - 1 - cnt] = firstHalf_val;
				cnt++;
				return firstHalf_val;
			} else {
				int j = N - 1 - cnt;
				long val = 0;
				if (j == 0) {
					val = W[0];
				} else {
					long[] g_pow = fp.pow(g, j);
					for (int i = j; i < N; i++) {
						val = (val + W[i] * g_pow[i]) % mod;
					}
				}
				R[j] = val;
				cnt++;
				return val;
			}
		}

		/**
		 * 最終的な結果の配列 R を取得する。
		 * R の j 番目の要素は sum_{i=0}^{N-1} W_i [x^i] (g(x))^j mod mod である。
		 *
		 * <p>
		 * 計算量: O(N log^2 N)
		 * </p>
		 *
		 * @return 長さ N の結果配列 R
		 */
		// 未テスト
		public long[] getResult() {
			return R;
		}
	}

	/**
	 * 固定された多項式 g(x) に対し、オンラインで与えられる多項式 W(x) との
	 * 半オンラインべき乗投影（パワープロジェクション）を計算するクラス。
	 *
	 * <p>
	 * 入力 W(x) は W_N-1, W_N-2, ..., W_0 の順にオンラインで与えられ、
	 * 順次 [x^i] (W(x) * g(x)^j) に対応する出力を返します。
	 * </p>
	 */
	// 未テスト
	public static class SemiOnlinePowerProjectionFixingPowerBase {
		private final PolynomialFpDynamic polygen; // 多項式演算器
		private final long mod; // 法
		private final int N; // 求める項数
		private final long[] R; // 計算結果を格納する配列 (逆順に格納)
		private int cnt = 0; // 現在処理中の項数

		// 再帰的な子構造 (N > 1 の場合のみ)
		private final long[] g; // 固定された多項式 g(x)
		private final SemiOnlinePowerProjectionFixingPowerBase b0_0_solver; // onlinePP(g0, w0, M)
		private final SemiOnlinePowerProjectionFixingPowerBase b1_solver;   // onlinePP(g0, MP(u, w1), K)

		// 半オンライン畳み込み
		private final SemiOnlineConvolution b1_conv_solver;

		// 途中データの一時配列
		private final long[] w1;
		private final long[] b0_1_R;
		private final long[] t_R;

		// 事前計算の定数
		private long v_m_1;

		/**
		 * 指定されたサイズ N, 固定多項式 g, 多項式演算器 fp で構築する。
		 *
		 * @param N 求める項数
		 * @param g 固定された多項式 g(x)
		 * @param polygen 多項式演算器
		 */
		// 未テスト
		public SemiOnlinePowerProjectionFixingPowerBase(int N, long[] g, PolynomialFpDynamic polygen) {
			this.polygen = polygen;
			this.mod = polygen.mod;
			this.N = N;
			this.R = new long[N];
			this.g = Arrays.copyOf(g, N);

			if (N > 1) {
				int M = (N + 1) / 2;
				int K = N - M;
				long[] g0 = Arrays.copyOfRange(this.g, 0, M);
				long[] g1 = Arrays.copyOfRange(this.g, M, M + K);
				this.w1 = new long[K];
				this.b0_1_R = new long[M];
				this.t_R = new long[M];

				// hat_g0 = (g0/x) mod x^K
				long[] hat_g0 = Arrays.copyOf(polygen.divideByX(g0, 1), K);
				// u = (g0/x)^M mod x^K
				long[] u = Arrays.copyOf(polygen.pow(hat_g0, (long) M), K);

				// 再帰的な子構造の初期化
				this.b0_0_solver = new SemiOnlinePowerProjectionFixingPowerBase(M, g0, polygen);
				this.b1_solver = new SemiOnlinePowerProjectionFixingPowerBase(K, g0, polygen);

				// 半オンライン畳み込みの初期化
				this.b1_conv_solver = new SemiOnlineConvolution(u, K, polygen);

				// v_m_1 の事前計算 (N が偶数の場合のみ)
				if (N % 2 == 0) {
					long g0_1 = (M == 1) ? 1 : g0[1];
					long g0_1_pow = (M == 1) ? 1 : MathUtils.modPow(g0_1, M - 1, mod);
					this.v_m_1 = (M % mod) * g0_1_pow % mod * g1[0] % mod;
				} else {
					this.v_m_1 = 0;
				}
			} else {
				this.w1 = null;
				this.b0_1_R = null;
				this.t_R = null;
				this.b0_0_solver = null;
				this.b1_solver = null;
				this.b1_conv_solver = null;
				this.v_m_1 = 0;
			}
		}

		/**
		 * 多項式 W(x) の次の係数 W_i を追加し、対応するパワープロジェクションの結果を計算して返す。
		 *
		 * <p>
		 * 計算量: 1項追加あたり均し O(log^2 N)
		 * </p>
		 *
		 * @param W_i 追加する W(x) の係数。この W_i は W(x) の N - 1 - i 次の係数である（ここで i はこれまでに append された要素数）。
		 * @return 計算された結果
		 */
		// 未テスト
		public long append(long W_i) {
			W_i = (W_i % mod + mod) % mod;
			if (cnt >= N) {
				return 0;
			}

			if (N == 1) {
				R[0] = W_i;
				cnt++;
				return W_i;
			}

			int M = (N + 1) / 2;
			int K = N - M;
			//   <w0+x^m w1, (g0 +x^m g1)^j>
			// = <w0+x^m w1, g0^j +j x^m g0^(j-1) g1>
			if (cnt < K) {
				w1[cnt] = W_i;
				// j >= m とすると
				//  <x^m w1, g0^j>
				// =<w1, g0^{j-m}*(g0/x)^m>
				// =<((g0/x)^m)^T * w1, g0^{j-m}>
				// f=(g0/x)^m)^T * w1
				// f^T=(g0/x)^m) * w1^T
				long b1_conv_val = b1_conv_solver.append(W_i);
				
				long b1_val = b1_solver.append(b1_conv_val);
				
				if (cnt == K - 1) {
					long contribution = v_m_1 * w1[0] % mod;
					b1_val = (b1_val + contribution) % mod;
				}

				R[N - 1 - cnt] = b1_val;
				cnt++;
				return b1_val;
			} else {
				int j = cnt - K;
				if (cnt == K) {
					long[] w1_normal = w1.clone();
					ArrayUtils.reverse(w1_normal);

					long[] A_trunc = new long[N];
					System.arraycopy(w1_normal, 0, A_trunc, M, K);

					long[] g0 = Arrays.copyOfRange(g, 0, M);
					long[] g1 = Arrays.copyOfRange(g, M, M + K);

					//<x^m w1, g0^j>
					long[] A_trunc_rev = A_trunc.clone();
					ArrayUtils.reverse(A_trunc_rev);
					long[] b0_1_full = polygen.powerProjection(A_trunc_rev, g0, M);
					for (int i = 0; i < M; i++) {
						b0_1_R[i] = b0_1_full[M - 1 - i];
					}
					//    <w1 x^m, j x^m g0^(j-1) g1>
					// = j<g1^T w1, g0^(j-1)>
					long[] t_full = polygen.tMulSameSize(g1, w1_normal);
					long[] t_full_rev = t_full.clone();
					ArrayUtils.reverse(t_full_rev);
					long[] t_online = polygen.powerProjection(t_full_rev, g0, M);
					for (int i = 0; i < M; i++) {
						t_R[i] = t_online[M - 1 - i];
					}
				}

				long w0_val = (W_i % mod + mod) % mod;

				// b0_0 = onlinePP(g0, w0, M)
				long b0_0_val = b0_0_solver.append(w0_val);

				// k = M - 1 - j
				int k = M - 1 - j;
				long b_der_val = 0;
				if (k > 0) {
					b_der_val = (long) k * t_R[j + 1] % mod;
				}

				long b0_val = (b0_0_val + b0_1_R[j] + b_der_val) % mod;

				R[N - 1 - cnt] = b0_val;
				cnt++;
				return b0_val;
			}
		}

		public long[] getResult() {
			return R;
		}
	}

	/**
	 * 同じサイズの多項式の転置乗算を計算する。
	 *
	 * <p>
	 * 計算量: O(L log L)
	 * </p>
	 *
	 * @param u 多項式 u
	 * @param a 多項式 a
	 * @return 転置乗算結果の配列
	 */
	// 未テスト
	long[] tMulSameSize(long[] u, long[] a) {
		int L = a.length;
		if (L == 0) return new long[0];
		long[] padded = new long[2 * L - 1];
		System.arraycopy(a, 0, padded, 0, L);
		return validShiftedDotProducts(u, padded);
	}

	/**
	 * g0^exp // x^exp mod x^size を計算する。
	 *
	 * <p>
	 * 計算量: O(size log(size))
	 * </p>
	 *
	 * @param g0 多項式 g0
	 * @param exp 指数 exp
	 * @param size 求める項数 size
	 * @return 計算結果の配列
	 */
	// 未テスト
	long[] computeU(long[] g0, int exp, int size) {
		if (size <= 0) return new long[0];
		int len = exp + size;
		long[] padded = new long[len];
		System.arraycopy(g0, 0, padded, 0, Math.min(g0.length, len));
		long[] g_pow = pow(padded, exp);
		long[] res = new long[size];
		System.arraycopy(g_pow, exp, res, 0, size);
		return res;
	}

	// 未テスト
	private boolean isAllZero(long[] a) {
		for (long x : a) {
			if (x != 0) return false;
		}
		return true;
	}

	/**
	 * TruncPP を再帰的に計算する。
	 *
	 * <p>
	 * 計算量: O(n log^2 n)
	 * </p>
	 *
	 * @param A 多項式 A
	 * @param n サイズ n
	 * @param k パラメータ k
	 * @param g0 多項式 g0
	 * @return 計算結果の配列
	 */
	// 未テスト
	long[] truncPP(long[] A, int n, int k, long[] g0) {
		if (isAllZero(A)) {
			return new long[k];
		}
		if (k == 1) {
			return new long[] { A[0] };
		}
		int k1 = k / 2;
		long[] low = truncPP(A, n, k1, g0);

		long[] u_k1 = computeU(g0, k1, n - k1);
		long[] A_shift = Arrays.copyOfRange(A, k1, n);
		long[] A2 = tMulSameSize(u_k1, A_shift);

		long[] high = truncPP(A2, n - k1, k - k1, g0);

		long[] res = new long[k];
		System.arraycopy(low, 0, res, 0, k1);
		System.arraycopy(high, 0, res, k1, k - k1);
		return res;
	}

	/**
	 * SemiPP を再帰的に計算する。
	 *
	 * <p>
	 * 計算量: O(n log^2 n)
	 * </p>
	 *
	 * @param g0 多項式 g0
	 * @param a 多項式 a
	 * @param n サイズ n
	 * @return 計算結果の配列
	 */
	// 未テスト
	long[] semiPP(long[] g0, long[] a, int n) {
		if (isAllZero(a)) {
			return new long[n];
		}
		if (n == 1) {
			return new long[] { a[0] };
		}
		int k = n / 2;
		long[] a0 = Arrays.copyOfRange(a, 0, k);
		long[] a1 = Arrays.copyOfRange(a, k, n);

		long[] u_k = computeU(g0, k, n - k);
		long[] t = tMulSameSize(u_k, a1);
		long[] b1 = semiPP(g0, t, n - k);

		long[] b0_0 = semiPP(g0, a0, k);
		long[] A = new long[n];
		System.arraycopy(a1, 0, A, k, n - k);
		long[] A_rev = A.clone();
		ArrayUtils.reverse(A_rev);
		long[] b0_1 = powerProjection(A_rev, g0, k);

		long[] b0 = new long[k];
		for (int i = 0; i < k; i++) {
			b0[i] = (b0_0[i] + b0_1[i]) % mod;
		}

		long[] res = new long[n];
		System.arraycopy(b0, 0, res, 0, k);
		System.arraycopy(b1, 0, res, k, n - k);
		return res;
	}


	/**
	 * OnlinePP を再帰的に計算する。
	 *
	 * <p>
	 * 計算量: O(n log^2 n)
	 * </p>
	 *
	 * @param g 多項式 g
	 * @param a 多項式 a
	 * @param n サイズ n
	 * @return 計算結果 of array
	 */
	// 未テスト
	long[] onlinePP(long[] g, long[] a, int n) {
		if (n == 1) {
			return new long[] { a[0] };
		}
		int m = n / 2;
		long[] g0 = Arrays.copyOfRange(g, 0, m);
		long[] g1 = Arrays.copyOfRange(g, m, n);
		long[] a0 = Arrays.copyOfRange(a, 0, m);
		long[] a1 = Arrays.copyOfRange(a, m, n);

		long[] a_tilde = new long[n];
		System.arraycopy(a1, 0, a_tilde, m, m);

		long[] semi = semiPP(g0, a_tilde, n);
		long[] b_mid = Arrays.copyOfRange(semi, 0, m);
		long[] b1 = Arrays.copyOfRange(semi, m, n);

		long[] b0_0 = onlinePP(g0, a0, m);
		long[] t = tMulSameSize(g1, a1);
		t = onlinePP(g0, t, m);

		long[] b_der = new long[m];
		for (int i = 1; i < m; i++) {
			b_der[i] = t[i - 1] * i % mod;
		}

		// Add v contribution to b1 in O(log m) time
		long g0_1 = (m == 1) ? 1 : g0[1];
		long g0_1_pow = (m == 1) ? 1 : MathUtils.modPow(g0_1, m - 1, mod);
		long v_m_1 = (m % mod) * g0_1_pow % mod * g1[0] % mod;
		b1[0] = (b1[0] + v_m_1 * a1[m - 1]) % mod;

		long[] b0 = new long[m];
		for (int i = 0; i < m; i++) {
			b0[i] = (b0_0[i] + b_mid[i] + b_der[i]) % mod;
		}

		long[] res = new long[n];
		System.arraycopy(b0, 0, res, 0, m);
		System.arraycopy(b1, 0, res, m, m);
		return res;
	}

	/**
	 * 与えられた多項式 v の偶数項または奇数項を NTT 領域で抽出する。
	 *
	 * <p>
	 * 計算量: O(|v|)
	 * </p>
	 *
	 * @param v 多項式の NTT 表現の配列
	 * @param odd 偶数項を抽出する場合は 0, 奇数項を抽出する場合は 1
	 * @return 抽出された多項式の NTT 表現 of array
	 */
	// 未テスト
	private long[] pickEvenOdd(long[] v, int odd) {
		int len = v.length;
		int z = len / 2;
		long[] res = new long[z];
		long half = fp.inv(2);
		if (odd == 0) {
			for (int i = 0; i < z; i++) {
				long sum = addMod(v[i * 2], v[i * 2 + 1]);
				res[i] = sum * half % mod;
			}
		} else {
			long e = MathUtils.modPow(primitiveRoot, (mod - 1) / (2L * z), mod);
			long ie = MathUtils.modInv(e, mod);
			long[] es = new long[z];
			es[0] = half;
			int curSz = 1;
			while (curSz < z) {
				long[] n_es = new long[curSz * 2];
				for (int i = 0; i < curSz; i++) {
					n_es[i * 2] = es[i];
					n_es[i * 2 + 1] = es[i] * ie % mod;
				}
				ie = ie * ie % mod;
				es = n_es;
				curSz *= 2;
			}
			for (int i = 0; i < z; i++) {
				long diff = subMod(v[i * 2], v[i * 2 + 1]);
				res[i] = diff * es[i] % mod;
			}
		}
		return res;
	}

	/**
	 * 多項式 f(x) と多項式 g(x) に対し、
	 * 各 i = 0, 1, ..., m - 1 について [x^(n - 1)] (g(x) * f(x)^i) を O(m * n * deg(f)) で計算する。
	 *
	 * <p>
	 * 計算量: O(m * n * deg(f))
	 * </p>
	 *
	 * @param g 多項式 g(x) の係数配列
	 * @param f 多項式 f(x) の係数配列 (f[0] = 0 である必要がある)
	 * @param m 求める項数
	 * @return 各 [x^(n - 1)] (g(x) * f(x)^i) の係数配列 of array (長さ m)
	 */
	// 未テスト
	public long[] powerProjectionNaive(long[] g, long[] f, int m) {
		if (m <= 0) return new long[0];
		if (f.length > 0 && fp.reduce(f[0]) != 0) {
			throw new IllegalArgumentException("f[0] must be 0");
		}
		if (g.length == 0) {
			return new long[m];
		}
		long[] ans = new long[m];
		long[] cur = new long[g.length];
		for (int i = 0; i < g.length; i++) {
			cur[i] = fp.reduce(g[i]);
		}
		ans[0] = (g.length > 0) ? cur[g.length - 1] : 0;
		for (int i = 1; i < m; i++) {
			cur = mul(cur, f);
			if (g.length - 1 < cur.length) {
				ans[i] = fp.reduce(cur[g.length - 1]);
			} else {
				ans[i] = 0;
			}
		}
		return ans;
	}

	/**
	 * 多項式 f(x) と多項式 g(x) に対し、
	 * 各 i = 0, 1, ..., m - 1 について [x^(n - 1)] (g(x) * f(x)^i) を計算する。
	 * ここで n は g(x) の長さ以上の最小の2冪である。
	 * f[0] = 0 である必要がある。
	 * NTT フレンドリーな素数 mod の下では高速なアルゴリズムを用い、それ以外では O(m * n * deg(f)) の O-記法計算量の
	 * ナイーブなアルゴリズムをフォールバックとして用いる。
	 *
	 * <p>
	 * 計算量: mod が NTT フレンドリーな場合 O(n log^2 n + m log m)、それ以外の場合 O(m * n * deg(f))
	 * </p>
	 *
	 * @param g 多項式 g(x) の係数配列
	 * @param f 多項式 f(x) の係数配列 (f[0] = 0 である必要がある)
	 * @param m 求める項数
	 * @return 各 [x^(n - 1)] (g(x) * f(x)^i) の係数配列 of array (長さ m)
	 */
	// 未テスト
	public long[] powerProjection(long[] g, long[] f, int m) {
		if (m <= 0) {
			return new long[0];
		}
		if (f.length > 0 && fp.reduce(f[0]) != 0) {
			throw new IllegalArgumentException("f[0] must be 0");
		}
		if (!isNTTFriendly) {
			return powerProjectionNaive(g, f, m);
		}
		if (g.length == 0) {
			return new long[m];
		}

		int ind = g.length - 1;
		int n = 1;
		while (n < g.length) {
			n *= 2;
		}

		long[] gArr = new long[n];
		long[] fArr = new long[n];
		for (int i = 0; i < Math.min(n, g.length); i++) {
			gArr[i] = fp.reduce(g[i]);
		}
		for (int i = 0; i < Math.min(n, f.length); i++) {
			fArr[i] = fp.reduce(f[i]);
		}

		long[] hold_g = new long[n];
		long[] hold_f = new long[n];

		// g(x) / (y - f(x))
		for (int i = 0; i < n; i++) {
			fArr[i] = subMod(0, fArr[i]);
		}

		int nk = n;
		while (nk != 1) {
			System.arraycopy(gArr, 0, hold_g, 0, n);
			System.arraycopy(fArr, 0, hold_f, 0, n);

			// resize g and f to 4 * n
			long[] g4 = new long[4 * n];
			long[] f4 = new long[4 * n];

			for (int i = n / nk - 1; i >= 0; i--) {
				for (int j = nk - 1; j >= 0; j--) {
					g4[i * nk * 2 + j] = gArr[i * nk + j];
					if (i > 0) {
						gArr[i * nk + j] = 0;
					}
					f4[i * nk * 2 + j] = fArr[i * nk + j];
					if (i > 0) {
						fArr[i * nk + j] = 0;
					}
				}
			}

			// fft of size 4n
			fftToBitReversed(g4);
			fftToBitReversed(f4);

			// pointwise multiplication
			for (int i = 0; i < 2 * n; i++) {
				long g_even = g4[i * 2];
				long g_odd = g4[i * 2 + 1];
				long f_even = f4[i * 2];
				long f_odd = f4[i * 2 + 1];

				g4[i * 2] = g_even * f_odd % mod;
				g4[i * 2 + 1] = g_odd * f_even % mod;
				f4[i * 2] = f_even * f_odd % mod;
				f4[i * 2 + 1] = f4[i * 2];
			}

			// FPS_pick_even_odd
			long[] g2 = pickEvenOdd(g4, (ind & 1));
			long[] f2 = pickEvenOdd(f4, 0);

			// ifft of size 2n
			ifftFromBitReversed(g2);
			ifftFromBitReversed(f2);

			// resize back and prepare for adding hold terms
			gArr = new long[2 * n];
			fArr = new long[2 * n];
			System.arraycopy(g2, 0, gArr, 0, 2 * n);
			System.arraycopy(f2, 0, fArr, 0, 2 * n);

			// y ^ nk
			for (int i = 0; i < n; i++) {
				if (((ind + i + 1) & 1) != 0) {
					int destIdx = n + (i / nk) * nk + (i & (nk - 1)) / 2;
					gArr[destIdx] = addMod(gArr[destIdx], hold_g[i]);
				}
				if ((i & 1) == 0) {
					int destIdx = n + (i / nk) * nk + (i & (nk - 1)) / 2;
					fArr[destIdx] = addMod(fArr[destIdx], hold_f[i] * 2 % mod);
				}
			}

			nk /= 2;

			long[] nextG = new long[n];
			long[] nextF = new long[n];
			for (int i = 0; i < n; i++) {
				nextG[i] = gArr[(i / nk) * nk * 2 + (i & (nk - 1))];
				nextF[i] = fArr[(i / nk) * nk * 2 + (i & (nk - 1))];
			}
			gArr = nextG;
			fArr = nextF;
			ind /= 2;
		}

		long[] finalF = new long[n + 1];
		System.arraycopy(fArr, 0, finalF, 0, n);
		finalF[n] = 1;

		long[] revG = new long[n];
		long[] revF = new long[n + 1];
		for (int i = 0; i < n; i++) {
			revG[i] = gArr[n - 1 - i];
		}
		for (int i = 0; i <= n; i++) {
			revF[i] = finalF[n - i];
		}

		// resize revG to m
		long[] revGResized = new long[m];
		System.arraycopy(revG, 0, revGResized, 0, Math.min(n, m));

		long[] invRevF = inv(Arrays.copyOf(revF, m));
		long[] ans = mul(revGResized, invRevF);
		if (ans.length > m) {
			ans = Arrays.copyOf(ans, m);
		} else if (ans.length < m) {
			long[] tmp = new long[m];
			System.arraycopy(ans, 0, tmp, 0, ans.length);
			ans = tmp;
		}

		return ans;
	}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
