package library.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;
import java.util.function.Predicate;

import library.util.algebra.instance.impl.GaussInt;
import library.util.collections.IntArrayList;
import library.util.collections.IntDeque;
import library.util.collections.LongArrayList;
import library.util.collections.LongDeque;

public class MathUtils {

	/**
	 * 累乗を繰り返す演算</b>のことをテトラトレーションと呼び、{@code a ↑↑ n} と表記します。{@code a ↑↑ n} % modを返す。
	 * <pre>
	 * a ↑↑ 1 = a
	 * a ↑↑ 2 = a^a
	 * a ↑↑ 3 = a^(a^a)
	 * a ↑↑ 4 = a^(a^(a^a))
	 * </pre>
	 * 
	 * @param a
	 * @param n
	 * @param mod
	 * @return
	 */
	public static long tetrationMod(long a, long n, long mod) {
		//https://judge.yosupo.jp/submission/371902
		if (mod == 1) return 0;
		if (a == 0) {
			// 0 ↑↑ 1 = 0
			// 0 ↑↑ 2 = 0^0 = 1
			// 0 ↑↑ 3 = 0^(0^0) = 0
			// 0 ↑↑ 4 = 0^(a^(a^a)) = 1
			return (n % 2) ^ 1;
		}
		if (a == 1) return 1;
		if (n == 0) return 1;
		if (n == 1) return a % mod;
		
		// a >= 2, n >= 2
		
		long x = MathUtils.saturatingTetration(a, n - 1);
		if (x != Long.MAX_VALUE) {
			return modPow(a, x, mod);
		} else {
			long coprimeMod = MathUtils.coprimePart(mod, a % mod);
			long period=MathUtils.totient(coprimeMod);
			long u = MathUtils.modPow(a % coprimeMod, tetrationMod(a, n - 1, period), coprimeMod);
			long v = (mod / coprimeMod);
			long ans= u * v % mod * MathUtils.modInv(v, coprimeMod) % mod;
			return ans;
		}
	}
	
	public static long[] pows(long a, int n) {
		long[]ret=new long[n];
		ret[0]=1;
		for (int i = 0; i < n-1; i++) {
			ret[i+1]=a*ret[i];
		}
		return ret;
	}
	
	/**
	 * ニュートン法を用いて、sqrtを計算する。 x-f(x)/f'(x)=x-(x**2-n)/2x=x-x/2+n/(2x)=(x*x+n)/(2x)
	 * 計算量: O(log log a)
	 */
	public static long sqrt(long a) {
		if (a == 0)
			return 0;
		long ret = (long) Math.sqrt(a) + 2;
		while (true) {
			long nret = (ret + a / ret) / 2;
			if (nret >= ret)
				break;
			ret = nret;
		}
		return ret;
	}
	
	public static long sqrtCeil(long a) {
		if (a < 0) throw new AssertionError();
		if (a == 0) return 0;
		return sqrt(a - 1) + 1;
	}

	public static long ceil(long a, long b) {
		return (a + b - 1) / b;
	}
	
	/**
	 * 未テスト
	 */
	public static long roundDiv(long a, long b) {
		if (b == 0) throw new ArithmeticException("/ by zero");
		if (b < 0) {
			a *= -1;
			b *= -1;
		}
		long q = a / b;
		long r = a % b;
		if (Math.abs(r) >= (b + 1) / 2) q += Long.signum(r);
		return q;
	}

	public static long gcd(long a, long b, long c, long d) {
		return gcd(a, gcd(b, c, d));
	}
	
	public static long gcd(long a, long b, long c) {
		return gcd(a, gcd(b, c));
	}
	
	public static long gcd(long a, long b) {
		a = Math.abs(a);
		b = Math.abs(b);
		if (a == 0)
			return b;
		return gcd(b % a, a);
	}

	public static int gcd(int a, int b) {
		a = Math.abs(a);
		b = Math.abs(b);
		if (a == 0)
			return b;
		return gcd(b % a, a);
	}

	/**
	 * aの、bと互いに素な部分を返す。
	 * すなわち、aの素因数のうち、bも持っているものをすべて取り除いた値を返す。
	 * 未テスト
	 * 計算量: O(log(a + b))
	 * @param a
	 * @param b
	 * @return
	 */
	public static long coprimePart(long a, long b) {
		if (a == 0) return 0;
		long res = a;
		a = Math.abs(a);
		b = Math.abs(b);
		long g = gcd(a, b);
		while (g > 1) {
			while (a % g == 0) a /= g;
			g = gcd(a, g);
		}
		a = Math.abs(a);
		return res > 0 ? a : -a;
	}

	/**
	 * aの、bと互いに素な部分を返す。
	 * すなわち、aの素因数のうち、bも持っているものをすべて取り除いた値を返す。
	 * 未テスト
	 * 計算量: O(log(a + b))
	 * @param a
	 * @param b
	 * @return
	 */
	public static int coprimePart(int a, int b) {
		if (a == 0) return 0;
		int res = a;
		a = Math.abs(a);
		b = Math.abs(b);
		int g = gcd(a, b);
		while (g > 1) {
			while (a % g == 0) a /= g;
			g = gcd(a, g);
		}
		a = Math.abs(a);
		return res > 0 ? a : -a;
	}

	public static long modPow(long a, long n, long mod) {
		if (n < 0) {
			long inv = MathUtils.modInv(a, mod);
			return modPow(inv, -n, mod);
		}
		if (n == 0)
			return 1;
		return modPow(a * a % mod, n / 2, mod) * (n % 2 == 1 ? a : 1) % mod;
	}

	public static long pow(long a, long n) {
		if (n == 0)
			return 1;
		return pow(a * a, n / 2) * (n % 2 == 1 ? a : 1);
	}

	public static long modKthRoot(int a, int k, int mod) {
		if (k == 0)
			return a == 1 ? 1 : -1;
		if (k > 0 && a == 0)
			return 0;
		long g = gcd(k, mod - 1);
		if (modPow(a, (mod - 1) / g, mod) != 1)
			return -1;
		long res = modPow(a, modInv(k / g, (mod - 1) / g), mod);
		for (long div = 2; div * div <= g; ++div) {
			int sz = 0;
			while (g % div == 0) {
				g /= div;
				++sz;
			}
			if (sz > 0) {
				res = peth_root(res, div, sz, mod);
			}
		}
		if (g > 1)
			res = peth_root(res, g, 1, mod);
		return res;
	}

	public static long modKthRoot(long a, int k, long mod) {
		if (mod <= Integer.MAX_VALUE)
			return modKthRoot((int) a, k, (int) mod);
		if (k == 0)
			return a == 1 ? 1 : -1;
		if (k > 0 && a == 0)
			return 0;
		long inv = -invMod2pow64(mod);
		long r2 = montgomeryR2Long(mod);
		long one = toMontgomeryLong(1, mod, inv, r2);
		long aM = toMontgomeryLong(a % mod, mod, inv, r2);

		long g = gcd(k, mod - 1);
		if (montgomeryPowLong(aM, (mod - 1) / g, mod, inv, one) != one)
			return -1;
		aM = montgomeryPowLong(aM, modInv(k / g, (mod - 1) / g), mod, inv, one);
		for (long div = 2; div * div <= g; ++div) {
			int sz = 0;
			while (g % div == 0) {
				g /= div;
				++sz;
			}
			if (sz > 0) {
				aM = peth_root_montgomery(aM, div, sz, mod, inv, r2, one);
			}
		}
		if (g > 1)
			aM = peth_root_montgomery(aM, g, 1, mod, inv, r2, one);
		return fromMontgomeryLong(aM, mod, inv);
	}

	private static long peth_root(long a, long p, int e, long mod) {
		long q = mod - 1;
		int s = 0;
		while (q % p == 0) {
			q /= p;
			++s;
		}
		long pe = modPow(p, e, mod);
		long ans = modPow(a, ((pe - 1) * modInv(q, pe) % pe * q + 1) / pe, mod);
		long c = 2;
		while (modPow(c, (mod - 1) / p, mod) == 1)
			++c;
		c = modPow(c, q, mod);
		HashMap<Long, Integer> map = new HashMap<>();
		long add = 1;
		int v = (int) Math.sqrt(p) + 1;
		long mul = modPow(c, v * modPow(p, s - 1, mod - 1), mod);
		for (int i = 0; i <= v; ++i) {
			map.put(add, i);
			add = add * mul % mod;
		}
		mul = modInv(modPow(c, modPow(p, s - 1, mod - 1), mod), mod);
		out: for (int i = e; i < s; ++i) {
			long err = modInv(modPow(ans, pe, mod), mod) * a % mod;
			long target = modPow(err, modPow(p, s - 1 - i, mod - 1), mod);
			for (int j = 0; j <= v; ++j) {
				if (map.containsKey(target % mod)) {
					int x = map.get(target % mod);
					ans = ans * modPow(c, (j + v * x) * modPow(p, i - e, mod - 1) % (mod - 1), mod) % mod;
					continue out;
				}
				target = target * mul % mod;
			}
			throw new AssertionError();
		}
		return ans;
	}

	/**
	 * $(ax + b) \bmod m \le c$ を満たす最小の非負整数 $x$ を返します。
	 *
	 * <p>事前条件: $m \ge 1$</p>
	 * <p>事後条件: 返り値 $x$ は $0 \le x$ を満たし、かつ $(ax + b) \bmod m \le c$ を満たす最小のものである。
	 * 満たす $x$ が存在しない場合は $-1$ を返す。</p>
	 * <p>計算量: $O(\log m)$</p>
	 *
	 * @param a 係数 $a$
	 * @param b 定数項 $b$
	 * @param c 上限 $c$
	 * @param m 法 $m$
	 * @return $(ax + b) \bmod m \le c$ を満たす最小の $x$
	 */
	// 未テスト
	public static int minXLinearMod(int a, int b, int c, int m) {
		//https://atcoder.jp/contests/arc224/submissions/77450592
		if (m <= 0) throw new AssertionError();
		a = (a % m + m) % m;
		b = (b % m + m) % m;
		if (c < 0) return -1;
		if (c >= m - 1) return 0;
		return (int) solveMinX(a, b, c, m);
	}

	private static long solveMinX(long a, long b, long c, long m) {
		if (a == 0) {
			if (b <= c) return 0;
			return -1;
		}
		if (b <= c) return 0;

		if (c >= a) {
			long k = (a + b - c + m - 1) / m;
			long x = (k * m - b + a - 1) / a;
			return x;
		} else {
			long ap = m % a;
			long bp = (c - b) % a;
			if (bp < 0) {
				bp += a;
			}
			long bpp = (ap + bp) % a;
			long kPrime = solveMinX(ap, bpp, c, a);
			if (kPrime == -1) return -1;
			long k = kPrime + 1;
			long x = (k * m - b + a - 1) / a;
			return x;
		}
	}

	private static long peth_root_montgomery(long aM, long p, int e, long mod, long inv, long r2, long one) {
		long q = mod - 1;
		int s = 0;
		while (q % p == 0) {
			q /= p;
			++s;
		}
		long pe_val = 1;
		for (int i = 0; i < e; i++)
			pe_val *= p;

		long inv_q_pe = modInv(q, pe_val);
		long exponent = BigInteger.valueOf(pe_val - 1).multiply(BigInteger.valueOf(inv_q_pe)).remainder(BigInteger.valueOf(pe_val))
				.multiply(BigInteger.valueOf(q)).add(BigInteger.ONE).divide(BigInteger.valueOf(pe_val)).longValue();
		long ansM = montgomeryPowLong(aM, exponent, mod, inv, one);
		long c = 2;
		while (montgomeryPowLong(toMontgomeryLong(c, mod, inv, r2), (mod - 1) / p, mod, inv, one) == one)
			++c;
		long cM = montgomeryPowLong(toMontgomeryLong(c, mod, inv, r2), q, mod, inv, one);
		HashMap<Long, Integer> map = new HashMap<>();
		long addM = one;
		int v = (int) Math.sqrt(p) + 1;

		long ps_1 = 1;
		for (int i = 0; i < s - 1; i++)
			ps_1 *= p;

		long mul_exponent = v * ps_1;
		long mulM = montgomeryPowLong(cM, mul_exponent, mod, inv, one);
		for (int i = 0; i <= v; ++i) {
			map.put(addM, i);
			addM = montgomeryMulLong(addM, mulM, mod, inv);
		}

		long inv_cM_ps_1_val = modInv(fromMontgomeryLong(montgomeryPowLong(cM, ps_1, mod, inv, one), mod, inv), mod);
		long inv_mulM = toMontgomeryLong(inv_cM_ps_1_val, mod, inv, r2);

		out: for (int i = e; i < s; ++i) {
			long ans_pe_M = montgomeryPowLong(ansM, pe_val, mod, inv, one);
			long inv_ans_pe_val = modInv(fromMontgomeryLong(ans_pe_M, mod, inv), mod);
			long errM = montgomeryMulLong(toMontgomeryLong(inv_ans_pe_val, mod, inv, r2), aM, mod, inv);

			long pi_e_s_1 = 1;
			for (int j = 0; j < s - 1 - i; j++)
				pi_e_s_1 *= p;
			long targetM = montgomeryPowLong(errM, pi_e_s_1, mod, inv, one);
			for (int j = 0; j <= v; ++j) {
				if (map.containsKey(targetM)) {
					int x = map.get(targetM);
					long pi_e = 1;
					for (int j2 = 0; j2 < i - e; j2++)
						pi_e *= p;
					long exp = (j + (long) v * x) % (mod - 1) * (pi_e % (mod - 1)) % (mod - 1);
					ansM = montgomeryMulLong(ansM, montgomeryPowLong(cM, exp, mod, inv, one), mod, inv);
					continue out;
				}
				targetM = montgomeryMulLong(targetM, inv_mulM, mod, inv);
			}
			throw new AssertionError();
		}
		return ansM;
	}

	/**
	 * 拡張ユークリッドの互除法で逆元を求める。
	 * 
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long modInv(long a, long mod) {
		a = (a % mod + mod) % mod;
		long[] f0 = new long[] { 1, 0, mod };
		long[] f1 = new long[] { 0, 1, a };
		while (f1[2] != 0) {
			long q = f0[2] / f1[2];
			for (int i = 0; i < 3; i++) {
				f0[i] -= q * f1[i];
			}
			ArrayUtils.swap(f0, f1);
		}
		return f0[1] < 0 ? (mod + f0[1]) : f0[1];
	}

	/**
	 * k = floor(N/i) が等しい区間 1 <= l <= i <= r <= N について組 [l, r, k] を k の昇順（iの降順）に返す。
	 */
	public static ArrayList<long[]> floorRange(long N) {
		ArrayList<long[]> range = new ArrayList<>();
		long quotient = 1;
		while (true) {
			long upper = N / quotient;
			long lower = N / (quotient + 1) + 1;
			range.add(new long[] { lower, upper, quotient });

			if (lower == 1)
				break;
			quotient = N / (lower - 1);
		}
		return range;
	}

	/**
	 * a = sum[i] b[i] 10^i となる b を返す
	 * 
	 * @param a
	 * @return
	 */
	int[] digitsArray(long a) {
		int[] ret = new int[40];
		for (int i = 0; i < 40; ++i) {
			ret[i] = (int) (a % 10);
			a /= 10;
			if (a == 0)
				return Arrays.copyOf(ret, i + 1);
		}
		throw new AssertionError();
	}

	/** 高度合成数の探索に使用する素数のリスト */
	private static final long[] SMALL_PRIMES = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 };

	/**
	 * $N$ 以下の正整数のうち、約数の個数が最大であるものを返す。
	 * 複数存在する場合は、そのうち最小のものを返す。
	 *
	 * <p>事前条件: $n \ge 1$</p>
	 * <p>事後条件: 返り値 $x$ は $1 \le x \le n$ を満たし、かつ任意の $1 \le y \le n$ について $d(x) \ge d(y)$（$d(n)$ は約数関数）を満たす。
	 * また、$d(x) = d(y)$ なる $y$ について $x \le y$ が成り立つ。</p>
	 * <p>副作用: なし</p>
	 * <p>計算量: 指数時間（探索空間は極めて限定的。$n=10^{18}$ で 5ms 程度）</p>
	 *
	 * @param n 上限 $N$
	 * @return 約数の個数が最大の $N$ 以下の整数
	 */
	public static long highlyCompositeNumber(long n) {
		if (n <= 0) return 0;
		long[] res = { 1, 1 }; // {num, divisors}
		dfsHighlyComposite(n, 0, 1, 1, 62, res);
		return res[0];
	}

	/**
	 * 高度合成数を探索するDFS。
	 *
	 * @param n 上限
	 * @param primeIdx 現在の素数のインデックス
	 * @param currentNum 現在の数値
	 * @param currentDivisors 現在の約数の個数
	 * @param lastExp 直前の素数の指数
	 * @param res 結果を格納する配列 {最良の数値, 最大の約数個数}
	 */
	private static void dfsHighlyComposite(long n, int primeIdx, long currentNum, long currentDivisors, int lastExp, long[] res) {
		if (currentDivisors > res[1] || (currentDivisors == res[1] && currentNum < res[0])) {
			res[1] = currentDivisors;
			res[0] = currentNum;
		}

		long p = SMALL_PRIMES[primeIdx];
		long nextNum = currentNum;
		for (int e = 1; e <= lastExp; e++) {
			if (n / p < nextNum) break;
			nextNum *= p;
			dfsHighlyComposite(n, primeIdx + 1, nextNum, currentDivisors * (e + 1), e, res);
		}
	}

	/**
	 * 約数を昇順で返す
	 * 
	 * @param n
	 * @return
	 */
	public static ArrayList<Long> divisors(long n) {
		ArrayList<Long> ret = new ArrayList<>();
		for (long i = 1; i * i <= n; ++i) {
			if (n % i == 0) {
				ret.add(i);
				if (i != n / i)
					ret.add(n / i);
			}
		}
		Collections.sort(ret);
		return ret;
	}
	
	/**
	 * 約数を昇順で返す
	 * 未テスト
	 * @param n
	 * @return
	 */
	public static IntArrayList divisors(int n) {
		IntArrayList ret=new IntArrayList();
		for (int i = 1; 1L * i * i <= n; ++i) {
			if (n % i == 0) {
				ret.add(i);
				if (i != n / i)
					ret.add(n / i);
			}
		}
		ret.sort();
		return ret;
	}

	public static LongArrayList primeDivisors(long n) {
		LongArrayList ret = new LongArrayList();
		for (long p : factor(n).keySet())
			ret.add(p);
		return ret;
	}
	
	
	public static IntArrayList primeDivisors(int n) {
		IntArrayList ret = new IntArrayList();
		for (int i = 2; 1L * i * i <= n; ++i) {
			int e = 0;
			while (n % i == 0) {
				n /= i;
				++e;
			}
			if (e != 0)
				ret.add(i);
		}
		if (n != 1)
			ret.add(n);
		return ret;
	}

	/**
	 * オイラーのφ関数。nと互いに素なn以下の正整数の個数を返す。
	 * 未テスト
	 * 計算量: O(√n)
	 * @param n
	 * @return
	 */
	public static int totient(int n) {
		int res = n;
		for (int p : primeDivisors(n)) {
			res -= res / p;
		}
		return res;
	}

	/**
	 * オイラーのφ関数。nと互いに素なn以下の正整数の個数を返す。
	 * 未テスト
	 * 計算量: Pollard's rhoにより O(n^1/4)
	 * @param n
	 * @return
	 */
	public static long totient(long n) {
		long res = n;
		for (long p : primeDivisors(n)) {
			res -= res / p;
		}
		return res;
	}

	/**
	 * modが素数のとき、modの原始根を返す。
	 * 未テスト
	 */
	public static int primitiveRoot(int mod) {
		if (mod == 2) return 1;
		IntArrayList primeDivisors = primeDivisors(mod - 1);
		Random rnd=new Random();
		while (true) {
			int g=rnd.nextInt(2, mod);
			boolean ok = true;
			for (int p : primeDivisors) {
				if (modPow(g, (mod - 1) / p, mod) == 1) {
					ok = false;
					break;
				}
			}
			if (ok) return g;
		}
	}
	
	/**
	 * modが素数のとき、modの原始根を返す。
	 */
	public static long primitiveRoot(long mod) {
		//https://judge.yosupo.jp/submission/370385
		if (mod <= Integer.MAX_VALUE) return primitiveRoot((int)mod);
		if (mod == 2) return 1;
		LongArrayList primeDivisors = primeDivisors(mod - 1);
		long inv = -invMod2pow64(mod);
		long r2 = montgomeryR2Long(mod);
		long one = toMontgomeryLong(1, mod, inv, r2);
		Random rnd=new Random();
		while (true) {
			long g = rnd.nextLong(2, mod);
			boolean ok = true;
			long gm = toMontgomeryLong(g, mod, inv, r2);
			for (long p : primeDivisors) {
				if (montgomeryPowLong(gm, (mod - 1) / p, mod, inv, one) == one) {
					ok = false;
					break;
				}
			}
			if (ok) return g;
		}
	}
	
	
	
	public static Map<Long, Integer> factor(long n) {
		Map<Long, Integer> ret = new TreeMap<>();
		for (long i = 2; i <= n / i && i <= 1000; ++i) {
			int e = 0;
			while (n % i == 0) {
				n /= i;
				++e;
			}
			if (e != 0)
				ret.put(i, e);
		}
		LongDeque que = new LongDeque();
		if (n != 1)
			que.addLast(n);
		while (!que.isEmpty()) {
			long m = que.pollFirst();
			long a = findFactor(m);
			if (m == a) {
				ret.merge(a, 1, Integer::sum);
			} else {
				que.addLast(a);
				que.addLast(m / a);
			}
		}
		return ret;
	}

	/**
	 * N = a^2 + b^2 を満たす非負整数の組 (a, b) をすべて返す。
	 * 未テスト
	 * 計算量: Pollard's rho による素因数分解 O(N^1/4) + バリエーションの生成
	 * 係数の絶対値が 32 bit 以上の場合、GaussInt の中間計算がオーバーフローする場合がある。
	 */
	public static ArrayList<long[]> enumerateAsTwoSquare(long n) {
		//https://judge.yosupo.jp/submission/371927
		if (n < 0) return new ArrayList<>();
		if (n == 0) {
			ArrayList<long[]> res = new ArrayList<>();
			res.add(new long[] { 0, 0 });
			return res;
		}

		Map<Long, Integer> factors = factor(n);
		GaussInt base = GaussInt.ONE;
		ArrayList<GaussInt> variants = new ArrayList<>();
		variants.add(GaussInt.ONE);

		for (Map.Entry<Long, Integer> entry : factors.entrySet()) {
			long p = entry.getKey();
			int e = entry.getValue();
			if (p == 2) {
				for (int i = 0; i < e; i++) base = base.mul(new GaussInt(1, 1));
			} else if (p % 4 == 3) {
				if (e % 2 != 0) return new ArrayList<>();
				for (int i = 0; i < e / 2; i++) base = base.mul(new GaussInt(p, 0));
			} else {
				// p % 4 == 1
				Map<GaussInt, Integer> gFactors = new GaussInt(p, 0).factor();
				var it = gFactors.keySet().iterator();
				GaussInt pi = it.next();
				GaussInt piConj = it.next(); 
				
				ArrayList<GaussInt> nextVariants = new ArrayList<>();
				GaussInt[] piPows = new GaussInt[e + 1];
				GaussInt[] piConjPows = new GaussInt[e + 1];
				piPows[0] = GaussInt.ONE;
				piConjPows[0] = GaussInt.ONE;
				for (int i = 1; i <= e; i++) {
					piPows[i] = piPows[i - 1].mul(pi);
					piConjPows[i] = piConjPows[i - 1].mul(piConj);
				}
				for (GaussInt v : variants) {
					for (int h = 0; h <= e; h++) {
						nextVariants.add(v.mul(piPows[h]).mul(piConjPows[e - h]));
					}
				}
				variants = nextVariants;
			}
		}

		HashSet<Long> set = new HashSet<>();
		for (GaussInt v : variants) {
			GaussInt z = base.mul(v);
			long a = Math.abs(z.re());
			long b = Math.abs(z.im());
			set.add(Math.min(a, b) << 32 | Math.max(a, b));
		}

		ArrayList<long[]> result = new ArrayList<>();
		for (long packed : set) {
			long a = packed >> 32;
			long b = packed & 0xFFFFFFFFL;
			result.add(new long[] { a, b });
			if (a != b) {
				result.add(new long[] { b, a });
			}
		}
		return result;
	}

	/**
	 * isLeft.test(i) = True となる最大の i を返す。 isLeft.test(left) = True,
	 * isLeft.test(right) = False と仮定して動作する。
	 *
	 * @param left
	 * @param right
	 * @param isLeft
	 * @return
	 */
	public static long binarySearch(long left, long right, Predicate<Long> isLeft) {
		while ((right - left) != 1) {
			long middle = left + (right - left) / 2;
			if (isLeft.test(middle)) {
				left = middle;
			} else {
				right = middle;
			}
		}
		return left;
	}

	public record Result<Key, Value>(Key key, Value value) {
	}

	/**
	 * 最小値を探す。(r-l)<=tol/2となるまで回す。
	 * 
	 * @param f
	 * @param l
	 * @param r
	 * @return
	 */
	public static Result<Double, Double> minimizeByTernarySearch(DoubleUnaryOperator f, double l, double r,
			double tol) {
		while (r - l > tol / 2) {
			double lm = l + (r - l) / 3;
			double rm = r - (r - l) / 3;
			if (f.applyAsDouble(lm) >= f.applyAsDouble(rm)) {
				l = lm;
			} else {
				r = rm;
			}
		}
		return new Result<Double, Double>(l, f.applyAsDouble(l));
	}

	/**
	 * l<=x<=rの下で最小値f(x)とそれを達成するxを探す。
	 * 
	 * @param f
	 * @param l
	 * @param r
	 * @return
	 */
	public static Result<Long, Double> ternarySearch(LongToDoubleFunction f, long l, long r) {
		while (r - l >= 3) {
			long lm = l + (r - l) / 3;
			long rm = r - (r - l) / 3;
			if (f.applyAsDouble(lm) >= f.applyAsDouble(rm)) {
				l = lm;
			} else {
				r = rm;
			}
		}
		long argmin = l;
		double minValue = f.applyAsDouble(l);
		for (long i = l; i <= r; ++i) {
			double v = f.applyAsDouble(i);
			if (v < minValue) {
				minValue = v;
				argmin = i;
			}
		}
		return new Result<Long, Double>(argmin, minValue);
	}

	/***
	 * min{ax + b mod m : 0<=x<=n}を返す。
	 * 
	 * @param a
	 * @param b
	 * @param m
	 */
	public static long minLinearMod(long a, long b, long m, long n) {
		if (!(0 <= a && a < m && (0 <= b && b < m)))
			throw new AssertionError();
		long ret = b;
		if (a == 0 || n < (m - b + a - 1) / a)
			return ret;
		if (b >= a) {
			long dx = (m - b + a - 1) / a;
			b = a * dx + b - m;
			n -= dx;
			return Math.min(minLinearMod(a, b, m, n), ret);
		}
		if (n == 0)
			return ret;
		long nextN = (a * n + b) / m;
		b = b - m * nextN;
		b = (b % a + a) % a;
		ret = Math.min(ret, minLinearMod(m % a, b, a, nextN));
		return ret;
	}

	/**
	 * $0 \le x \le n$ において $ax + b \pmod m$ を最小化する最小の非負整数 $x$ を返します。
	 *
	 * <p>事前条件: $0 \le a < m$, $0 \le b < m$, $n \ge 0$</p>
	 * <p>事後条件: 返り値 $x$ は $0 \le x \le n$ を満たし、かつ任意の $0 \le y \le n$ について
	 * $(ax + b) \pmod m \le (ay + b) \pmod m$ を満たす。また、そのような $x$ のうち最小のものである。</p>
	 * <p>計算量: $O(\log m)$</p>
	 *
	 * @param a 係数 $a$
	 * @param b 定数項 $b$
	 * @param m 法 $m$
	 * @param n 上限 $n$
	 * @return $ax + b \pmod m$ を最小化する最小の $x$
	 */
	// 未テスト
	public static long argminLinearMod(long a, long b, long m, long n) {
		if (!(0 <= a && a < m && (0 <= b && b < m) && n >= 0))
			throw new AssertionError();
		long c = minLinearMod(a, b, m, n);
		long target = (c - b) % m;
		if (target < 0) {
			target += m;
		}
		long ans = Zn.solveLinearCongruence(a, target, m);
		if (ans == -1) {
			throw new AssertionError("No solution found for linear congruence");
		}
		return ans;
	}

	/**
	 * x=0のときは-1
	 * @param x
	 * @return
	 */
	public static int floorLog2(long x) {
		if (x == 0)
			return -1;
		return 63 - Long.numberOfLeadingZeros(x);
	}

	
	/**
	 * @param n
	 * @param base
	 * @return
	 */
	public static int floorLog10(long n) {
		return floorLog(n, 10);
	}

	
	/**
	 * テストしていない！！！危険！！
	 * 
	 * @param n
	 * @param base
	 * @return
	 */
	public static int floorLog(long n, long base) {
		int e = 0;
		while (n >= base) {
			e++;
			n /= base;
		}
		return e;
	}

	/**
	 * 20!=2432902008176640000までしかlongに収まらない
	 * 
	 * @param n
	 * @return
	 */
	public static long factorial(int n) {
		if (n > 21 || n < 0)
			throw new AssertionError();
		long ret = 1;
		for (int i = 1; i <= n; i++) {
			ret = ret * i;
		}
		return ret;
	}

	public static long lcm(long a, long b) {
		return a / gcd(a, b) * b;
	}

	public static long lcm(long a, long b, long c) {
		return lcm(a, lcm(b, c));
	}
	
	public static long saturatingLcm(long a, long b) {
		//https://atcoder.jp/contests/abc236/submissions/74343035
		a=Math.abs(a);
		b=Math.abs(b);
		return saturatingMul(a/gcd(a,b), b);
	}

	/**
	 * Long.MIN_VALUEを-∞, Long.MAX_VALUEを+∞として演算
	 * @param a
	 * @param b
	 * @return
	 */
	public static long saturatingAdd(long a, long b) {
		//https://atcoder.jp/contests/abc303/submissions/73659954
		if (a == Long.MAX_VALUE && b == Long.MIN_VALUE)
			throw new AssertionError();
		if (a == Long.MIN_VALUE && b == Long.MAX_VALUE)
			throw new AssertionError();
		if (a == Long.MAX_VALUE)
			return Long.MAX_VALUE;
		if (b == Long.MAX_VALUE)
			return Long.MAX_VALUE;
		if (a == Long.MIN_VALUE)
			return Long.MIN_VALUE;
		if (b == Long.MIN_VALUE)
			return Long.MIN_VALUE;
		if (a > 0 && b > 0) {
			// a + b <= INF
			if (a <= Long.MAX_VALUE - b) {
				return a + b;
			} else {
				return Long.MAX_VALUE;
			}
		} else if (a < 0 && b < 0) {
			if (a >= Long.MIN_VALUE - b) {
				// a + b >= -INF
				// a >= -INF - b
				return a + b;
			} else {
				return Long.MIN_VALUE;
			}
		}
		return a + b;
	}
	
	public static long saturatingMul(long a, long b, long c) {
		return saturatingMul(saturatingMul(a, b), c);
	}
	
	/**
	 * Long.MIN_VALUEを-∞, Long.MAX_VALUEを+∞として演算
	 * @param a
	 * @param b
	 * @return
	 */
	public static long saturatingMul(long a, long b) {
		//https://atcoder.jp/contests/abc303/submissions/73659954
		if (a == 0 || b == 0)
			return 0;
		if (a < 0 && b < 0) {
			if (a == Long.MIN_VALUE || b == Long.MIN_VALUE)
				return Long.MAX_VALUE;
			return saturatingMul(-a, -b);
		}
		if (a > 0 && b > 0) {
			if (a <= Long.MAX_VALUE / b)
				return a * b;
			return Long.MAX_VALUE;
		}
		if (a < 0 && b > 0) {
			if (a == Long.MIN_VALUE || b == Long.MAX_VALUE)
				return Long.MIN_VALUE;
			if (a >= Long.MIN_VALUE / b)
				return a * b;
			return Long.MIN_VALUE;
		}
		if (a > 0 && b < 0) {
			if (a == Long.MAX_VALUE || b == Long.MIN_VALUE)
				return Long.MIN_VALUE;
			if (b >= Long.MIN_VALUE / a)
				return a * b;
			return Long.MIN_VALUE;
		}
		throw new AssertionError();
	}

	public static long saturatingPow(long a, long n) {
		if (n == 0)
			return 1;
		long x = saturatingPow(a, n / 2);
		x = saturatingMul(x, x);
		if (n % 2 == 1)
			x = saturatingMul(a, x);
		return x;
	}

	/**
	 * aのn階指数塔 (tetration) a↑↑n を計算する。
	 * 結果がLong.MAX_VALUEを超える場合はLong.MAX_VALUEを返す。
	 * 計算量: O(min(n, 64) * log(Long.MAX_VALUE))
	 * @param a 底 (a >= 0)
	 * @param n 指数塔の高さ (n >= 0)
	 * @return a↑↑n
	 */
	public static long saturatingTetration(long a, long n) {
		//https://judge.yosupo.jp/submission/371902
		if (a < 0 || n < 0) throw new AssertionError();
		if (n == 0) return 1;
		if (a == 0) return n % 2 == 0 ? 1 : 0;
		if (a == 1) return 1;
		if (n == 1) return a;
		// a >= 2
		long res = a;
		for (long i = 1; i < n; i++) {
			res = saturatingPow(a, res);
			if (res == Long.MAX_VALUE) break;
		}
		return res;
	}

	public static long integerRoot(long x, int k) {
		if (k < 0)
			throw new AssertionError();
		if (k == 0)
			return 1;
		if (k == 1)
			return x;
		if (k == 2)
			return sqrt(x);
		return binarySearch(0, Integer.MAX_VALUE, v -> saturatingPow(v, k) <= x);
	}

	/**
	 * {@code a^n <= limit} かを unsigned 64bit 整数として判定する。未テスト
	 *
	 * @param a 底
	 * @param n 指数
	 * @param limit 上限
	 * @return {@code a^n <= limit} なら {@code true}
	 */
	private static boolean powIsLeqUnsigned(long a, int n, long limit) {
		if (a == 0)
			return true;
		long ret = 1;
		for (int i = 0; i < n; i++) {
			if (Long.compareUnsigned(ret, Long.divideUnsigned(limit, a)) <= 0) {
				//a * ret <= limit
				ret *= a;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * unsigned 64bit 整数 {@code x} に対して、{@code ret^k <= x} を満たす最大の
	 * unsigned 64bit 整数 {@code ret} を返す。未テスト
	 * 返り値は {@code long} のビット列で unsigned 64bit 整数を表す。
	 *
	 * @param x 根を求める unsigned 64bit 整数
	 * @param k 指数
	 * @return {@code floor(x^(1/k))}
	 */
	public static long integerRootUnsigned(long x, int k) {
		//https://judge.yosupo.jp/submission/370410
		if (k < 0)
			throw new AssertionError();
		if (k == 0)
			return 1;
		if (k == 1)
			return x;
		if (k >= 64)
			return x == 0 ? 0 : 1;
		return binarySearch(0, 1L << 32, v -> powIsLeqUnsigned(v, k, x));
	}
	
	public static int max(int a, int b) {
		return Math.max(a, b);
	}
	
	public static int max(int a, int b, int c) {
		return Math.max(a, max(b, c));
	}
	
	public static int max(int a, int b, int c, int d) {
		return Math.max(a, max(b, c, d));
	}
	
	public static int max(int a, int b, int c, int d, int e, int f) {
		return Math.max(a, max(b, c, d, e, f));
	}
	
	public static int max(int a, int b, int c, int d, int e) {
		return Math.max(a, max(b, c, d, e));
	}
	
	public static long max(long a, long b) {
		return Math.max(a, b);
	}

	public static long max(long a, long b, long c) {
		return Math.max(Math.max(a, b), c);
	}

	public static long max(long a, long b, long c, long d) {
		return max(Math.max(a, b), c, d);
	}
	
	public static long max(long a, long b, long c, long d, long e) {
		return max(Math.max(a, b), c, d, e);
	}	

	public static long min(long a, long b) {
		return Math.min(a, b);
	}

	public static long min(long a, long b, long c) {
		return min(min(a, b), c);
	}

	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static int min(int a, int b, int c) {
		return min(min(a, b), c);
	}
	
	public static int min(int a, int b, int c, int d) {
		return min(min(a, b, c), d);
	}
	
	public static int min(int a, int b, int c, int d, int e) {
		return min(min(a, b, c, d), e);
	}


	/**
	 * O(min(k, n-k))
	 * C(n,i)=C(n,i-1)(n-i+1)/iで計算
	 * @param n
	 * @param k
	 * @return
	 */
	public static long comb(int n, int k) {
		if (k < 0 || n - k < 0)
			return 0;
		if (k > n / 2)
			return comb(n, n - k);
		if (k == 0)
			return 1;
		if (k == 1)
			return n;
		if (k == 2)
			return 1L * n * (n - 1) / 2;
		if (k > n - k)
			return comb(n, n - k);
		long ans = 1;
		for (int i = 1; i <= k; i++) {
			// C(n,i)=C(n,i-1)(n-i+1)/i
			ans = ans * (n - i + 1) / i;
		}
		return ans;
	}

	public static double combDouble(int n, int k) {
		if (k < 0 || n - k < 0)
			return 0;
		if (k > n / 2)
			return comb(n, n - k);
		if (k == 0)
			return 1;
		if (k == 1)
			return n;
		if (k == 2)
			return 1L * n * (n - 1) / 2;
		if (k > n - k)
			return comb(n, n - k);
		double ans = 1;
		for (int i = 1; i <= k; i++) {
			// C(n,i)=C(n,i-1)(n-i+1)/i
			ans = 1. * ans * (n - i + 1) / i;
		}
		return ans;
	}

	public static long combrep(int n, int k) {
		return comb(n + k - 1, k);
	}

	/**
	 * 多項係数を計算する。
	 * $\frac{(\sum k_i)!}{\prod k_i!}$
	 *
	 * 計算量: $O(\sum k_i)$
	 * @param ks
	 * @return
	 */
	public static long multinomial(int... ks) {
		for (int k : ks) {
			if (k < 0) return 0;
		}
		long res = 1;
		int currentN = 0;
		for (int k : ks) {
			currentN += k;
			res *= comb(currentN, k);
		}
		return res;
	}

	public static double facDouble(int n) {
		double ret = 1;
		for (int i = 1; i <= n; i++) {
			ret *= i;
		}
		return ret;
	}

	/**
	 * 二次方程式 ax^2 + bx + c = 0 を解きます。
	 * 数値的に安定した解法を使用します。
	 *
	 * @param a x^2の係数
	 * @param b xの係数
	 * @param c 定数項
	 * @return 実数解の配列（解がない場合は空の配列）
	 */
	public static double[] quadraticSolve(double a, double b, double c) {
		if (a == 0) {
			if (b == 0) {
				return new double[0];
			}
			return new double[] { -c / b };
		}
		double D = b * b - 4 * a * c;
		if (D < 0) {
			return new double[0];
		} else if (D == 0) {
			return new double[] { -b / (2 * a) };
		} else {
			double sqrtD = Math.sqrt(D);
			double q = -0.5 * (b + Math.copySign(sqrtD, b));
			return new double[] { q / a, c / q };
		}
	}

	/**
	 * ax+by=gcd(a,b)となる[x,y,gcd(a,b)]を返す。
	 * gcd(a,b)>=0である。
	 * a, b > 1, gcd(a, b)=1 とすると、|x|≤a/2, |b|≤b/2が成り立つ（らしい）。
	 * 
	 * @param a
	 * @param b
	 * @return [x, y, gcd(a,b)]
	 */
	public static long[] extgcd(long a, long b) {
		long[] f0 = new long[] { 1, 0, a };
		long[] f1 = new long[] { 0, 1, b };
		while (f1[2] != 0) {
			long q = f0[2] / f1[2];
			for (int i = 0; i < 3; i++) {
				f0[i] -= q * f1[i];
			}
			ArrayUtils.swap(f0, f1);
		}
		if (f0[2] < 0) {
			f0[0] *= -1;
			f0[1] *= -1;
			f0[2] *= -1;
		}
		return f0;
	}
	
	/**
	 * ax+by=cとなる[x,y]を返す。
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] solveLinearDiophatine(long a, long b, long c) {
		if(a==0&&b==0&&c!=0)return null;
		if(c==0)return new long[] {0, 0};
		int asign=Long.signum(a);
		int bsign=Long.signum(b);
		a=Math.abs(a);
		b=Math.abs(b);
		var xy=extgcd(a, b);
		if (c%xy[2]!=0) {
			return null;
		}
		a/=xy[2];
		b/=xy[2];
		c/=xy[2];
		long x=1L*xy[0]%b*(c%b)%b;
		long y=(c-a*x)/b;
		return new long[] {asign*x, bsign*y};
	}

	

	public static boolean isPrime(int mod) {
		if (mod == 2)
			return true;
		if (mod == 1 || mod % 2 == 0)
			return false;
		int[] testNumbers = new int[] { 2, 7, 61 };
		int pow2 = Integer.lowestOneBit(mod - 1);
		int log = MathUtils.floorLog2(pow2);
		for (int a : testNumbers) {
			a %= mod;
			if (a == 0)
				continue;

			long x = MathUtils.modPow(a, (mod - 1) / pow2, mod);
			if (x == 1 || x == mod - 1)
				continue;
			for (int i = 0; i < log; i++) {
				x = x * x % mod;
				if (x == mod - 1)
					break;
				if (i == log - 1)
					return false;
			}
		}
		return true;
	}
	
	/**
	 * @param n
	 * @return
	 */
	public static boolean isPrime(long n) {
		//https://judge.yosupo.jp/submission/370351
		if (n <= 1) return false;
		if (n <= Integer.MAX_VALUE) return isPrime((int)n);
		if (n == 2)
			return true;
		if (n % 2 == 0)
			return false;
		//n < 2^64 に対して 2,325,9375,28178,450775,9780504,1795265022 は十分らしい。
		//https://ceur-ws.org/Vol-1326/020-Forisek.pdf
		final long[] MILLER_RABIN_WITNESSES_LONG = new long[] { 2, 325, 9375, 28178, 450775, 9780504, 1795265022 };
		long pow2 = Long.lowestOneBit(n - 1);
		int log = MathUtils.floorLog2(pow2);
		long d = (n - 1) / pow2;
		long inv = -invMod2pow64(n);
		long r2 = montgomeryR2Long(n);
		long one = toMontgomeryLong(1, n, inv, r2);
		long minusOne = toMontgomeryLong(n - 1, n, inv, r2);
		for (long a : MILLER_RABIN_WITNESSES_LONG) {
			a %= n;
			if (a == 0)
				continue;

			long x = montgomeryPowLong(toMontgomeryLong(a, n, inv, r2), d, n, inv, one);
			if (x == one || x == minusOne)
				continue;
			boolean ok = false;
			for (int i = 1; i < log; i++) {
				x = montgomeryMulLong(x, x, n, inv);
				if (x == minusOne) {
					ok = true;
					break;
				}
			}
			if (!ok)
				return false;
		}
		return true;
	}
	
	/**
	 * 未テスト
	 * @param a
	 * @param n
	 * @param mod
	 * @return
	 */
	private static long montgomeryPowLong(long aMontgomery, long n, long mod, long inv, long oneMontgomery) {
		long ret = oneMontgomery;
		while (n > 0) {
			if ((n & 1) == 1)
				ret = montgomeryMulLong(ret, aMontgomery, mod, inv);
			aMontgomery = montgomeryMulLong(aMontgomery, aMontgomery, mod, inv);
			n >>= 1;
		}
		return ret;
	}
	
	/**
	 * Montgomery表現同士の積を返す。R=2^64として、
	 * {@code a = xR mod mod}, {@code b = yR mod mod} のとき
	 * {@code xyR mod mod} を返す。未テスト
	 * @param a Montgomery表現の値
	 * @param b Montgomery表現の値
	 * @param mod 奇数の法
	 * @param inv {@code -mod^{-1} mod 2^64}
	 * @return Montgomery表現の積
	 */
	private static long montgomeryMulLong(long a, long b, long mod, long inv) {
		return montgomeryReduce128bit(unsignedMultiplyHigh(a, b), a * b, mod, inv);
	}
	
	/**
	 * 128bit整数 {@code high * 2^64 + low} に Montgomery reduction を行う。
	 * R=2^64として、返り値は {@code (high * 2^64 + low) / R mod mod}。
	 * {@code low + (low * inv) * mod} が R で割り切れることを利用する。未テスト
	 * @param high 積の上位64bit
	 * @param low 積の下位64bit
	 * @param mod 奇数の法
	 * @param inv {@code -mod^{-1} mod 2^64}
	 * @return Montgomery reduction 後の値
	 */
	private static long montgomeryReduce128bit(long high, long low, long mod, long inv) {
		// Z/modZ × Z/RZ で、Z/modZは保ちながら、Z/RZ側を0に射影してから、Rで割る。
		long q = low * inv;//(low*inv) mod R
		long qModHigh = unsignedMultiplyHigh(q, mod);
		long carry = low == 0 ? 0 : 1;
		long ret = high + qModHigh + carry;
		if (Long.compareUnsigned(ret, mod) >= 0) {
			ret -= mod;
			// T = high * 2^64 + low < mod^2
			// T + qmod < mod^2 + Rmod
			// (T + qmod) / R < 2mod
			// よって高々1回modを引けばよい。
		}
		return ret;
	}
	
	/**
	 * unsigned 64bit整数で -a^{-1} mod 2^64　を返す。
	 * 未テスト
	 * @param a
	 * @return
	 */
	public static long invMod2pow64(long a) {
		if (a % 2 == 0) throw new AssertionError();
		// まず inv = a^{-1} mod 2^64 を求める。
		// a = 1 mod 2 より
		// a^2 = (2k+1)(2k+1) = 4k(k+1) + 1 = 1 mod 8
		long inv = a;
		//forループでは
		//初期:  3 bit 正しい
		//1回目: 6 bit
		//2回目: 12 bit
		//3回目: 24 bit
		//4回目: 48 bit
		//5回目: 96 bit
		for (int i = 0; i < 5; i++) {
			inv *= 2 - a * inv;//ニュートン法
		}
		return inv;
	}
	
	/**
	 * 通常表現の {@code x} を Montgomery表現 {@code xR mod mod} に変換する。
	 * {@code r2 = R^2 mod mod} として {@code montgomeryMulLong(x, r2)} を計算する。未テスト
	 * @param x 通常表現の値
	 * @param mod 奇数の法
	 * @param inv {@code -mod^{-1} mod 2^64}
	 * @param r2 {@code R^2 mod mod}
	 * @return Montgomery表現の値
	 */
	private static long toMontgomeryLong(long x, long mod, long inv, long r2) {
		return montgomeryMulLong(x, r2, mod, inv);
	}
	
	private static long fromMontgomeryLong(long aMontgomery, long mod, long inv) {
		return montgomeryReduce128bit(0, aMontgomery, mod, inv);
	}
	
	/**
	 * 2^128 modulo modを返す。未テスト
	 * @param mod
	 * @return
	 */
	private static long montgomeryR2Long(long mod) {
		long ret = (1L << 62) % mod;
		for (int i = 0; i < 66; i++) {
			ret = ret >= mod - ret ? ret - (mod - ret) : ret + ret;
		}
		return ret;
	}
	
	/**
	 * 0 <= a, b < mod のとき、a + b mod mod を返す。未テスト
	 */
	private static long modAdd(long a, long b, long mod) {
		return a >= mod - b ? a - (mod - b) : a + b;
	}

	static int findFactor(int n) {
		if (isPrime(n))
			return n;
		if (n%2==0)return 2;
		Random rnd = new Random();
		while (true) {
			long c = rnd.nextLong(1, n);
			long x = rnd.nextLong(1, n);
			long y = x;
			long d = 1;
			while (d == 1 && d != n) {
				x = (x * x + c) % n;
				y = (y * y + c) % n;
				y = (y * y + c) % n;
				d = MathUtils.gcd(n, Math.abs(x - y));
				if (d != 1 && d != n)
					return (int) d;
			}
		}
	}
	
	/**
	 * Pollard's rhoでnの非自明な因数を返す。nが素数ならnを返す。
	 * 未テスト
	 */
	static long findFactor(long n) {
		if (n <= Integer.MAX_VALUE) return findFactor((int)n);
		if (isPrime(n))
			return n;
		if (n % 2 == 0)
			return 2;
		Random rnd = new Random();
		long inv = -invMod2pow64(n);
		long r2 = montgomeryR2Long(n);
		while (true) {
			long c = toMontgomeryLong(rnd.nextLong(1, n), n, inv, r2);
			long x = toMontgomeryLong(rnd.nextLong(1, n), n, inv, r2);
			long y = x;
			long d = 1;
			while (d == 1 && d != n) {
				x = modAdd(montgomeryMulLong(x, x, n, inv), c, n);
				y = modAdd(montgomeryMulLong(y, y, n, inv), c, n);
				y = modAdd(montgomeryMulLong(y, y, n, inv), c, n);
				long diff = x >= y ? x - y : y - x;
				d = gcd(diff, n);
				if (d != 1 && d != n)
					return d;
			}
		}
	}

	public static Map<Integer, Integer> factor(int n) {
		Map<Integer, Integer> ret = new TreeMap<>();
		for (long i = 2; 1L * i * i <= n && i <= 1000; ++i) {
			int e = 0;
			while (n % i == 0) {
				n /= i;
				++e;
			}
			if (e != 0)
				ret.put((int) i, e);
		}
		IntDeque que=new IntDeque();
		if (n != 1)
			que.addLast(n);
		while (!que.isEmpty()) {
			int m = que.pollFirst();
			int a = findFactor(m);
			if (m == a) {
				ret.merge(a, 1, Integer::sum);
			} else {
				que.addLast(a);
				que.addLast(m / a);
			}
		}
		return ret;
	}
	
	public BigInteger cbrt(BigInteger a) {
		if(a.equals(BigInteger.ZERO))return BigInteger.ZERO;
		var absA=a.abs();
		BigInteger ok=BigInteger.ZERO;
		BigInteger ng=BigInteger.ONE.shiftLeft((absA.bitLength()+2)/3);
		while(!ng.subtract(BigInteger.ONE).equals(ok)) {
			var m=ok.add(ng).divide(BigInteger.TWO);
			if(m.multiply(m).multiply(m).compareTo(absA) <= 0) ok=m;
			else ng=m;
		}
		return a.signum() == -1 ?  ok.negate() : ok;
	}

	/**
	 * start+(start+1)+..+(start+length-1)
	 * @param a
	 * @param b
	 * @return
	 */
	public static long pow1Sum(long start, long length) {
		if(length<=0) return 0;
		return length*(length-1+2*start)/2;
	}
	
	public static long saturatingPow1Sum(long start, long length) {
		//https://atcoder.jp/contests/abc303/submissions/73659954
		if(length<=0) return 0;
		long a, b;
		if (length % 2 == 0) {
			a = length / 2;
			b = saturatingAdd(length - 1, saturatingMul(2, start));
		} else {
			a = length;
			b = saturatingAdd((length - 1) / 2, start);
		}
		return saturatingMul(a, b);
	}
	/**
	 * sum ax+b for x = start,..,start+length-1
	 * @param a
	 * @param b
	 * @param start
	 * @param length
	 * @return
	 */
	public static long saturatingAxPlusBSum(long a, long b, long start, long length) {
		//https://atcoder.jp/contests/abc303/submissions/73784615
		if(length<=0) return 0;
		long ret = MathUtils.saturatingPow1Sum(start, length);
        ret = MathUtils.saturatingMul(a, ret);
        ret = MathUtils.saturatingAdd(ret, MathUtils.saturatingMul(b, length));
        return ret;
	}
	
	
	public static long pow0Sum(long start, long length) {
		if(length<=0) return 0;
		return length;
	}
	
	/**
	 * start^2+(start+1)^2+..+(start+length-1)^2
	 * 未テスト
	 * @param a
	 * @param b
	 * @return
	 */
	public static long pow2Sum(long start, long length) {
		if(length<=0) return 0;
		// sum[i=0..n-1](a+i)^2=sum[i=0..n-1](a^2+2ai+i^2) = na^2+an(n-1)+(2n-1)(n-1)/6
		return length*start*start+start*length*(length-1)+length*(length-1) / 2 *(2*length-1)/3;
	}
	
	/**
	 * start^3+(start+1)^3+..+(start+length-1)^3
	 */
	public static long pow3Sum(long start, long length) {
	    if (length <= 0) return 0;

	    long n = length;
	    long a = start;
	    
	    
	    //sum[i=0..n-1](a + i)^3
	    //=sum[i=0..n-1] a^3 + 3(a^2)i + 3a(i^2) + i^3

	    long s1 = n * (n - 1) / 2;                 // sum i
	    long s2 = n * (n - 1) /2 * (2 * n - 1) / 3;   // sum i^2
	    long s3 = s1 * s1;                         // sum i^3

	    return n * a * a * a
	         + 3 * a * a * s1
	         + 3 * a * s2
	         + s3;
	}


	
	/**
	 * start^q+(start+1)^q+..+(start+length-1)^q
	 * @param a
	 * @param b
	 * @return
	 */
	public static long powSum(long start, long length, long q) {
		if(length<=0) return 0;
		if(q==0) {
			return length;
		} else if (q==1) {
			return pow1Sum(start, length);
		} else if (q==2) {
			return pow2Sum(start, length);
		} else if (q==3) {
			return pow3Sum(start, length);
		} else {
			throw new AssertionError();
		}
	}

	/**
	 * vをfloor(v/2),ceil(v/2)に置き換える操作をv ≥ lowerBoundを保ったまま可能な限り行う。
	 * 例:v=7,lowerBound=2のとき、2,2,3をMapで返す。
	 * @param v
	 * @param lowerBound
	 * @return
	 */
	public static Map<Long, Long> splitWithLowerBound(long v, long lowerBound) {
		if (v < lowerBound) throw new AssertionError();
		lowerBound = Math.max(lowerBound, 1);
		TreeMap<Long, Long> map=new TreeMap<>();
		if(v==lowerBound) {
			map.put(v, 1L);
			return map;
		}
		int e=MathUtils.floorLog2(v/lowerBound);
		map.put(1+(v>>e), v%(1L<<e));
		map.put((v>>e), (1L<<e)-v%(1L<<e));
		while (true) {
			var lastEntry= map.lastEntry();
			if(lastEntry.getKey() >= 2 * lowerBound) {
				long lo=lastEntry.getKey() / 2;
				long hi=lastEntry.getKey() - lo;
				map.remove(lastEntry.getKey());
				map.merge(lo, lastEntry.getValue(), Long::sum);
				map.merge(hi, lastEntry.getValue(), Long::sum);
			} else {
				break;
			}
		}
		return map;
	}
	
    /**
     * Returns as a {@code long} the most significant 64 bits of the unsigned
     * 128-bit product of two unsigned 64-bit factors.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @see #multiplyHigh
     * @since 18
     */
    public static long unsignedMultiplyHigh(long x, long y) {
        // Compute via multiplyHigh() to leverage the intrinsic
        long result = Math.multiplyHigh(x, y);
        result += (y & (x >> 63)); // equivalent to `if (x < 0) result += y;`
        result += (x & (y >> 63)); // equivalent to `if (y < 0) result += x;`
        return result;
    }

	
	static void tr(Object... objects) {
		System.out.println(Arrays.deepToString(objects));
	}

}
