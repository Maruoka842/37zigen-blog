package library.util;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.algebra.strategy.longs.LongFieldStrategy;
import library.util.polynomial.HolonomicSequence;
import library.util.polynomial.PolynomialFpDynamic;

public class Fp extends Zn implements LongFieldStrategy {
	/** 998244353 を法とする有限体 F_998244353。 */
	public static final Fp MOD998244353 = new Fp(998244353);
	public static final Fp MOD1000000007 = new Fp(1000000007);
	
	public Fp(long mod) {
		super(mod);
	}

	/** fac(n) 用に遅延初期化される階乗テーブル。 */
	int[] fac = new int[0];
	/** ifac(n) 用に遅延初期化される逆階乗テーブル。 */
	int[] ifac = new int[0];
	/** inv(n) 用に遅延初期化される逆元テーブル。 */
	int[] inv = new int[0];
	/** fib(n) 用に遅延初期化されるフィボナッチ数テーブル。 */
	long[] fib = new long[0];
	
	/**
	 * fac, ifac, invを長さnまで延長
	 * @param n
	 */
	public void expand(int n) {
		fac = new int[n];
		ifac = new int[n];
		inv = new int[n];
		Arrays.fill(fac, 1);
		Arrays.fill(ifac, 1);
		Arrays.fill(inv, 1);
		for (int i = 2; i < n; ++i) {
			fac[i] = (int) (i * (fac[i - 1] & 0xffffffffL) % mod);
			inv[i] = (int) (mod - (mod / i) * (inv[(int) (mod % i)] & 0xffffffffL) % mod);
			ifac[i] = (int) ((inv[i] & 0xffffffffL) * (ifac[i - 1] & 0xffffffffL) % mod);
		}
	}
	
	public long fac(int n) {
		if (fac.length <= n) {
			expand(Math.max(2 * fac.length, n + 1));
		}
		return fac[n] & 0xffffffffL;
	}
	
	public long ifac(int n) {
		if (ifac.length <= n) {
			expand(Math.max(2 * ifac.length, n + 1));
		}
		return ifac[n] & 0xffffffffL;
	}
	
	/**
	 * n < 0 でもok
	 * @param n
	 * @return
	 */
	public long inv(long n) {
		if(n < 0) {
			n = reduce(n);
		}
		return n < inv.length ?  inv[(int) n] & 0xffffffffL : MathUtils.modInv(n, mod);
	}

	/**
	 * 事前条件: b != 0 in F_mod。返り値 r は r = a / b in F_mod。
	 * 未テスト
	 * 計算量: O(log mod)
	 * @param a 被除数
	 * @param b 除数
	 * @return a / b mod mod
	 */
	@Override
	public long div(long a, long b) {
		return mul(a, inv(b));
	}

	/**
	 * 事前条件: b != 0 in F_mod。返り値 r は r = 0。
	 * 未テスト
	 * 計算量: O(1)
	 * @param a 被除数
	 * @param b 除数
	 * @return 0
	 */
	@Override
	public long mod(long a, long b) {
		if (equals(b, zero())) throw new ArithmeticException("Division by zero");
		return zero();
	}

	/**
	 * 返り値 r は a = 0 なら r = 0、そうでなければ r = 1。
	 * 未テスト
	 * 計算量: O(1)
	 * @param a 対象
	 * @return a のノルム
	 */
	@Override
	public long norm(long a) {
		return equals(a, zero()) ? 0 : 1;
	}

	/**
	 * 返り値 u は a = 0 なら u = 1、そうでなければ u = a。
	 * 未テスト
	 * 計算量: O(1)
	 * @param a 対象
	 * @return a = u * canonical(a) を満たす単元 u
	 */
	@Override
	public long canonicalUnit(long a) {
		if (equals(a, zero())) return one();
		return a;
	}

	
	/**
	 * comb(n+k-1,k)を返す。n=k=0のときは1を返す。
	 * @param n
	 * @param k
	 * @return
	 */
	public long combrep(int n, int k) {
		if (k < 0) return 0;
		if (n == 0 && k == 0) return 1;
		return comb(n + k - 1, k);
	}
	
	public long combrepNaive(long n, long k) {
		if (k < 0) return 0;
		if (n == 0 && k == 0) return 1;
		return combNaive(n + k - 1, k);
	}
	
	/**
	 * comb(0, 0)=1とする。
	 * @param n
	 * @param k
	 * @return
	 */
	public long comb(int n, int k) {
		if (k < 0 || n - k < 0) return 0;
		return fac(n) * ifac(k) % mod * ifac(n - k) % mod;
	}
	
	/**
	 * comb(n, k)(-1)^k
	 * @param n
	 * @param k
	 * @return
	 */
	public long signedComb(int n, int k) {
		long ret = comb(n, k);
		if (k % 2 == 1 && ret != 0) ret = mod - ret;
		return ret;
	}
	
	public long combNaive(long n, long k) {
		//https://yukicoder.me/submissions/1153305
		if (k < 0 || n - k < 0) return 0;
		k = Math.min(k, n - k);
		long ret = 1;
		n %= mod;
		for (int i = 0; i < k; i++) {
			ret = ret * (n - i) % mod;
			ret = ret * inv(i + 1) % mod;
		}
		ret = reduce(ret);
		return ret;
	}
	
	/**
	 * n!/(n-k)!
	 * @param n
	 * @param k
	 * @return
	 */
	public long perm(int n, int k) {
		if(k>n||k<0)return 0;
		if(n<=100000000) {
			return fac(n) * ifac(n - k) % mod;
		} else {
			long ret = 1;
			for (int i = n-k+1; i <= n; i++) {
				ret=ret*i%mod;
			}
			return ret;
		}
	}
	
	public long iperm(int n, int k) {
		if(k>n||k<0)return 0;
		return ifac(n) * fac(n - k) % mod;
	}
	
	public long fib(int n) {
		if (fib.length <= n) {
			fib = new long[2 * Integer.highestOneBit(Math.max(1, n))];
			fib[0] = 1;
			fib[1] = 1;
			for (int i = 2; i < fib.length; ++i) {
				fib[i] = (fib[i - 1] + fib[i - 2]) % mod;
			}
		}
		return fib[n];
	}
	
	/**
	 * a は 32bit 整数を仮定
	 */
	public long pow(long a, long n) {
		if (n < 0) {
			a = inv(a);
			n = -n;
		}
		return MathUtils.modPow(a, n, mod);
	}
	
	/**
	 * doubleの仮数部が52bitなので、そのあたりまでは動く...はず....
	 * @param a
	 * @param b
	 * @return
	 */
    public long mul_52bit(long a, long b) {
    	long ret=a*b-(long)(1.*a*b/mod)*mod;
    	while(ret<0)ret+=mod;
    	while(ret>=mod)ret-=mod;
    	return ret;
    }
    
	/**
	 * doubleの仮数部が52bitなので、そのあたりまでは動く...はず....
	 * @param a
	 * @param b
	 * @return
	 */
    public long pow_52bit(long a, long n) {
    	if(n==0)return 1;
    	if(n==1)return a;
    	long ret=pow_52bit(mul_52bit(a,a),n/2);
    	if(n%2==1)ret=mul_52bit(ret, a);
    	return ret;
    }
    
    /**
     * a^i = 1 となる最大の i >= 1 を返す。
     * mod-1の約数をprimeDivisorsでメソッドに渡す
     * @param a
     * @return
     */
    public long order_52bit(long a, ArrayList<Long> primeDivisors) {
    	long order = mod - 1;
    	for (long p : primeDivisors) {
    		long preOrder = order;
    		while (order % p == 0) {
    			order /= p;
    		}
    		while (pow_52bit(a, order) != 1) {
    			order*=p;
    		}
			a = pow_52bit(a, preOrder / order);
    	}
    	return order;
    }
    
    
    public String restoreRational(long v) {
    	if(v==0)return "0";
    	v=reduce(v);
    	for (long a=1;a*a<=mod;++a) {
    		for (long b=1;b*b<=mod;++b) {
    			if(a==v*b%mod) {
    				return a+"/"+b;
    			}
    			if((mod-a)==v*b%mod) {
    				return "-"+a+"/"+b;
    			}
    		}
    	}
    	throw new AssertionError();
    }
    
    
	/**
	 * start+(start+1)+..+(start+n-1)
	 * @param a
	 * @param b
	 * @return
	 */
	public long pow1Sum(long start, long length) {
		if(length<=0) return 0;
		length%=mod;
		start%=mod;
		long ret=length*(length-1+2*start)%mod*inv(2)%mod;
		return reduce(ret);
	}
	
	
	public long[][] mulTable(int n) {
		long[][] ret=new long[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				ret[i][j]=1L*i*j%mod;
			}
		}
		return ret;
	}
	
    /** always x + e >= y
     *  (a, b) -> (c, d)
     *  (1, 0), (0, 1) 
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @return
     */
    public long mirror(int a, int b, int c, int d, int e){
    	//https://atcoder.jp/contests/abc216/submissions/74255616
        if (a + e < b || c + e < d) return 0;
        if (a > c || b > d) return 0;
        a += e;
        c += e;
        long ret=comb(c + d - a - b, c - a) - comb(c + d - a - b, c - b + 1); 
        ret=reduce(ret);
        return ret;
    }
    
    
    
    /** always x + e >= y
     *  (0, 0) -> (c, d)
     *  未テスト
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @return
     */
    public long mirror(int c, int d, int e){
        if (e < 0 || c + e < d) return 0;
        if (0 > c || 0 > d) return 0;
        long ret=comb(c + d, c) - comb(c + d, c + e + 1); 
        ret=reduce(ret);
        return ret;
    }
    
    /**
     * (1, 1), (1, -1) のステップを合計で n 個使って、(0, 0) から (n, level) に行く方法の数。
     * ただし、全ての途中の点 (i, y_i) で y_i >= level を満たす必要がある。
     * 計算量: O(1)
     * @param n ステップ数
     * @param level 到着点の y 座標
     * @return 方法の数
     */
    public long mirror(int n, int level) {
        if ((n + level) % 2 != 0 || Math.abs(level) > n) return 0;
        int x = (n + level) / 2;
        int y = (n - level) / 2;
        return mirror(x, y, -level);
    }

	/**
	 * n! mod mod を O(√n log n) で計算する。
	 * @param n 階乗を求める数
	 * @return n! mod mod
	 */
	public long facLarge(long n) {
		if (n >= mod) return 0;
		if (n < fac.length) return fac((int) n);
		PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
		return HolonomicSequence.prefixProduct(new long[] {1, 1}, n, poly);
	}

	/**
	 * nPk mod mod を O(√k log k) で計算する。
	 * @param n 総数
	 * @param k 選択する数
	 * @return nPk mod mod
	 */
	public long permLarge(long n, long k) {
		if (k < 0 || n < k) return 0;
		if (k == 0) return 1;
		if (n / mod > (n - k) / mod) return 0;
		if (n < fac.length && k < fac.length) {
			return fac((int) n) * ifac((int) (n - k)) % mod;
		}
		PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
		return HolonomicSequence.prefixProduct(new long[] {n % mod, mod - 1}, k, poly);
	}

	/**
	 * nCk mod mod を O(√k log k) で計算する。
	 * @param n 総数
	 * @param k 選択する数
	 * @return nCk mod mod
	 */
	public long combLarge(long n, long k) {
		if (k < 0 || n < k) return 0;
		if (k == 0 || k == n) return 1;
		if (k > n / 2) k = n - k;
		if (n < fac.length) return comb((int) n, (int) k);
		long num = permLarge(n, k);
		long den = facLarge(k);
		return num * MathUtils.modInv(den, mod) % mod;
	}

	/**
	 * リュカの定理を用いて、法 mod が素数のときの二項係数 comb(n, k) mod mod を計算する。
	 *
	 * 計算量: mod が小さい（10^6 未満）場合は O(mod + log_mod n)、
	 *         mod が大きい場合は O(sqrt(mod) log(mod) log_mod n)
	 *
	 * @param n 総数
	 * @param k 選択する数
	 * @return comb(n, k) mod mod
	 */
	public long combLucas(long n, long k) {
		if (k < 0 || n < k) return 0;
		if (mod <= 1) return 0;
		long ans = 1;
		long tempN = n;
		long tempK = k;
		while (tempN > 0 || tempK > 0) {
			long ni = tempN % mod;
			long ki = tempK % mod;
			if (ni < ki) return 0;
			long digitComb = 1;
			if (ki > 0 && ni != ki) {
				long targetK = Math.min(ki, ni - ki);
				if (ni < fac.length) {
					digitComb = comb((int) ni, (int) ki);
				} else if (mod < 1000000) {
					digitComb = comb((int) ni, (int) ki);
				} else if (targetK <= 1000) {
					digitComb = combNaive(ni, ki);
				} else {
					digitComb = combLarge(ni, ki);
				}
			}
			ans = ans * digitComb % mod;
			tempN /= mod;
			tempK /= mod;
		}
		return ans;
	}

	/**
	 * 負の二項係数に対応した comb(n, k)。
	 * n < 0 の場合は (-1)^k * comb(-n+k-1, k) を返す。
	 *
	 * 計算量: O(1)
	 * @param n 総数
	 * @param k 選択する数
	 * @return comb(n, k) mod mod
	 */
	public long combGeneralized(int n, int k) {
		if (k < 0) return 0;
		if (n >= 0) return comb(n, k);
		return signedComb(-n + k - 1, k);
	}

	/**
	 * 多項係数を計算する。
	 * $\frac{(\sum k_i)!}{\prod k_i!}$
	 *
	 * 計算量: $O(\sum k_i)$
	 * @param ks
	 * @return
	 */
	public long multinomial(int... ks) {
		int n = 0;
		for (int k : ks) {
			if (k < 0) return 0;
			n += k;
		}
		if (n == 0) return 1;
		long res = fac(n);
		for (int k : ks) {
			res = res * ifac(k) % mod;
		}
		return res;
	}

    /**
     * 等比重み付き冪和
     *
     *   sum_{i=0}^{n-1} i^d r^i
     *
     * を mod 上で返す。
     *
     * r = 1 のときは冪和の多項式補間、r != 1 のときは
     * (1 - r x)^{d + 1} を掛けた差分で項数を O(d) 個に落として計算する。
     *
     * @param r 等比の公比
     * @param d 冪の次数。d >= 0 を仮定する。
     * @param n 和の項数。n <= 0 のときは 0 を返す。
     * @return {@code sum_{i=0}^{n-1} i^d r^i} の mod
     *
     * 計算量: O(d log d + log n)
     */
    public long geometricPowerSum(long r, int d, long n) {
    	//https://judge.yosupo.jp/submission/371786
		if (r == 1) {
			long ans = 0;
			int deg = d + 1;
			long[] f = new long[deg + 1];
			for (int i = 1; i <= deg; ++i) {
				f[i] = pow(i, d);
			}
			for (int i = 1; i < f.length; ++i) {
				f[i] += f[i - 1];
				f[i] %= mod;
			}
			long  point = n - 1;
			if (point < 0) return 0;
			if (point < f.length) {
				ans = f[(int) point];
			} else {
				point %= mod;
				for (int i = 0; i < f.length; ++i) {
					f[i] = f[i] * ifac(i) % mod * ifac(deg - i) % mod;
					if ((deg - i) % 2 == 1) f[i] = mod - f[i];
				}
				long lp = 1;
				long rp = 1;
				for (int i = 0; i < f.length; ++i) {
					f[i] = f[i] * lp % mod;
					lp = lp * (point - i) % mod;
				}
				for (int i = f.length - 1; i >= 0; --i) {
					f[i] = f[i] * rp % mod;
					rp = rp * (point - i) % mod;
				}
				for (int i = 0; i < f.length; ++i) {
					ans = (ans + f[i]) % mod;
				}
			}
			if (n > 0) ans = (ans + mod + pow(r, 0) * pow(0, d) % mod) % mod;
			return ans;
		}
		long[] sum = new long[d + 2];
		{
			long pwr = 1;
			for (int i = 0; i <= d + 1; ++i) {
				sum[i] = (i % 2 == 0 ? comb(d + 1, i) : -comb(d + 1, i)) * pwr % mod;
				pwr = pwr * r % mod;
			}
			
		}
		for (int i = 1; i < sum.length; ++i) {
			sum[i] = (sum[i] + sum[i - 1]) % mod;
		}
		long ans = 0;
		int m1 = (int) Math.min(n - 1, d + 1);
		long m2 = Math.max(m1 + 1, n);
		{// m1 次以下
			long pwr = r;
			for (int i = 1; i <= m1; ++i) {
				ans += pow(i , d) * pwr % mod * sum[m1 - i] % mod;
				ans %= mod;
				pwr = pwr * r % mod;
			}
		}
		{// m2 次以上 (n - 1) + (d + 1) 次以下
			long src = Math.max(0, m2 - (d + 1));
			long pwr = pow(r, src);
			for (long i = Math.max(0, m2 - (d + 1)); i <= n - 1; ++i) {
				long add = pow(i % mod , d) * pwr % mod * (sum[(int) Math.min(d + 1, n + d - i)] - (m2 - i - 1 >= 0 ? sum[(int)(m2 - i - 1)] : 0)) % mod;
				ans += add;
				ans %= mod;
				pwr = pwr * r % mod;
			}
		}
		ans = ans * inv(pow(1 - r, (d + 1))) % mod;
		if (n > 0) ans = (ans + mod + pow(r, 0) * pow(0, d) % mod) % mod;
		return ans;
	}
    
    
    /**
     * 等比重み付き冪和
     *
     *   sum_{n=0}^{∞} n^d r^n
     *
     * を mod 上で返す。
     *
     * 公式
     *
     *   sum_{n=0}^{∞} n^d x^n = A_d(x) / (1 - x)^{d + 1}
     *
     * を使う。ただし A_d(x) は Eulerian polynomial 型の分子で、
     *
     *   A_d(x) = (1 - x)^{d + 1} sum_{n=0}^{∞} n^d x^n
     *
     * で定義される多項式。
     *
     * 計算量: 畳み込み部分 O(d)。i^d の列挙は線形篩と素数ごとの pow で行う。
     *
     * 注意:
     * - r != 1 mod mod を仮定する。
     * - pow(0, 0) = 1 として扱う実装なら、d = 0 のときも
     *   sum_{n=0}^{∞} r^n = 1 / (1 - r)
     *   になる。
     */
    public long infiniteGeometricPowerSum(long r, int d) {
    	//https://judge.yosupo.jp/submission/371780
        r %= mod;
        if (r < 0) r += mod;
        if (d == 0) {
        	//A_d=1となるので場合分け
        	return inv((1 + mod - r) % mod);
        }

        long[] pref = new long[d + 2];

        /*
         * pref[i] に
         *
         *   (-1)^i C(d + 1, i) r^i
         *
         * を入れる。
         *
         * これは (1 - r x)^{d + 1} の係数に対応する。
         */
        long pwr = 1;
        for (int i = 0; i <= d + 1; ++i) {
            long coef = comb(d + 1, i) * pwr % mod;
            if ((i & 1) == 1) coef = -coef;
            pref[i] = coef % mod;
            pwr = pwr * r % mod;
        }

        /*
         * pref[k] =
         *
         *   sum_{i=0}^{k} (-1)^i C(d + 1, i) r^i
         *
         * にする。
         */
        for (int i = 1; i < pref.length; ++i) {
            pref[i] = (pref[i] + pref[i - 1]) % mod;
        }

        /*
         * 分子 A_d(r) を計算する。
         *　A_d(x) = (1 - x)^{d + 1} sum_{n=0}^{∞} n^d x^n　で A_d は d 次式。
         * 展開すると
         *
         *   A_d(r)
         *   = sum_{i=1}^{d} i^d r^i
         *       sum_{j=0}^{d+1-i} (-1)^j C(d+1, j) r^j
         */
        long numerator = 0;
        pwr = r;
        for (int i = 1; i <= d; ++i) {
            numerator += pow(i, d) * pwr % mod * pref[d - i] % mod;
            numerator %= mod;
            pwr = pwr * r % mod;
        }

        /*
         * sum n^d r^n = A_d(r) / (1 - r)^{d + 1}
         */
        long denominator = pow(1 - r, d + 1);
        long ans = numerator * inv(denominator) % mod;

        if (ans < 0) ans += mod;
        return ans;
    }
    

}
