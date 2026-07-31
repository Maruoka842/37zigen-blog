package library.util.poset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.LongBinaryOperator;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.Itertools;
import library.util.MathUtils;
import library.util.Sieve;
import library.util.collections.ArrayOnQuotient;
import library.util.collections.IntArrayList;
import library.util.seq.SortedArrays;

public class DivisorLattice {
	
    
	/**
	 * 約数束 (divisor lattice) 上の関数を，
	 * その部分順序集合に制限した上での Möbius 変換を計算する。
	 *
	 * <p>{@code divs} を割り切り関係で順序付けられた集合とみなし，
	 * {@code a[i]} を {@code divs.get(i)} における関数値とする。
	 * このメソッドは，各 {@code x = divs.get(i)} に対して
	 *
	 * <pre>{@code
	 * a[i] = Σ f(d)  （d | x, d ∈ divs）
	 * }</pre>
	 *
	 * のように約数ゼータ変換された値 {@code a} から，元の関数 {@code f} を復元する。
	 *
	 * <p>すなわち，約数束におけるゼータ変換
	 *
	 * <pre>{@code
	 * g(x) = Σ f(d)  （d | x）
	 * }</pre>
	 *
	 * の逆変換を，整数全体ではなく {@code divs} が張る部分順序集合上で行う。
	 *
	 * <p>実装は，各素数 {@code p} による被覆関係
	 * {@code d → d * p} に沿って，ゼータ変換で加算された寄与を逆向きに打ち消すことで
	 * 元の値を復元している。
	 * ゼータ変換では小さい約数側から大きい倍数側へ値を伝播させるのに対し，
	 * このメソッドでは逆変換のために，添字を大きい側から小さい側へ逆順に走査する。
	 *
	 * <h2>前提条件</h2>
	 * <ul>
	 *   <li>{@code divs} は昇順にソートされていること</li>
	 *   <li>{@code a.length == divs.size()} であること</li>
	 *   <li>divs 上に張った x -> x*p 辺によるゼータ変換の逆変換</li>
	 * </ul>
	 *
	 * <h2>計算量</h2>
	 *  O(|divs| × |primes|)。
	 *
	 * <h2>備考</h2>
	 * このメソッドは {@link #zetaOnSubposet(long[], ArrayList, ArrayList, long)} の逆変換になっており，
	 * 適切な入力に対して
	 *
	 * <pre>{@code
	 * moebius(zeta(f, divs, primes, mod), divs, primes, mod)
	 * }</pre>
	 *
	 * は {@code f} を復元する。
	 *
	 * @param a      約数ゼータ変換後の関数値配列
	 * @param divs   約数集合（割り切り順序で扱う部分集合）
	 * @param primes 被覆関係を生成するための素数集合
	 * @param mod    加算・減算に用いる法
	 * @return Möbius 変換後の配列（元の関数値）
	 */
	public static long[] moebiusOnSubposet(long[] a, ArrayList<Long> divs, ArrayList<Long> primes, long mod) {
    	//https://atcoder.jp/contests/abc349/submissions/71431698
    	long[]b=Arrays.copyOf(a, a.length);
    	for (long p:primes) {
    		for (int i = divs.size() - 1; i >= 0; i--) {
    			if(divs.get(i)<=divs.get(divs.size()-1)/p) {
    				long nd=divs.get(i)*p;
				int id=SortedArrays.floor(divs, nd);
    				if(divs.get(id)!=nd)continue;
    				b[id]=(b[id]+mod-b[i])%mod;
    			}
			}
    	}
    	return b;
    }
	
	public static long[] moebiusOnSuposet(long[] a, IntArrayList divs, IntArrayList primes, long mod) {
    	//https://atcoder.jp/contests/abc349/submissions/71431698
    	long[]b=Arrays.copyOf(a, a.length);
    	for (long p:primes) {
    		for (int i = divs.size() - 1; i >= 0; i--) {
    			if(divs.get(i)<=divs.get(divs.size()-1)/p) {
    				long nd=divs.get(i)*p;
    				int id=SortedArrays.floor(divs, (int)nd);
    				if(divs.get(id)!=nd)continue;
    				b[id]=(b[id]+mod-b[i])%mod;
    			}
			}
    	}
    	return b;
    }

    
    /**
     * 約数束 (divisor lattice) 上の関数を，
     * その部分順序集合（特に下に閉じた部分集合）に制限した上でのゼータ変換を計算する。
     *
     * <p>{@code divs} を割り切り関係で順序付けられた集合とみなし，
     * {@code a[i]} を {@code divs.get(i)} における関数値とする。
     * このとき，各 {@code x = divs.get(i)} に対して
     *
     * <pre>{@code
     * b[i] = Σ a[j]  （divs.get(j) が x を割り切る）
     * }</pre>
     *
     * を満たす配列 {@code b} を返す。
     *
     * <p>すなわち，約数束におけるゼータ変換
     *
     * <pre>{@code
     * g(x) = Σ f(d)  （d | x）
     * }</pre>
     *
     * を，整数全体ではなく {@code divs} が張る部分順序集合上で計算する。
     *
     * <p>実装は，各素数 {@code p} による被覆関係
     * {@code d → d * p} を用いて値を上方へ伝播させることで，
     * 約数関係に沿った累積和を構成している。
     *
     * <h2>前提条件</h2>
     * <ul>
     *   <li>{@code divs} は昇順にソートされていること</li>
     *   <li>{@code a.length == divs.size()} であること</li>
     * </ul>
     *
     * <h2>計算量</h2>
     * O(|divs| × |primes|)。
     * @param a      関数値配列（{@code a[i]} は {@code divs.get(i)} に対応）
     * @param divs   約数集合（割り切り順序で扱う部分集合）
     * @param primes 被覆関係を生成するための素数集合
     * @param mod    加算に用いる法
     * @return 約数ゼータ変換後の配列
     */
    public static long[] zetaOnSubposet(long[] a, ArrayList<Long> divs, ArrayList<Long> primes, long mod) {
    	//https://atcoder.jp/contests/abc349/submissions/71431698
    	long[]b=Arrays.copyOf(a, a.length);
    	for (long p:primes) {
    		for (int i = 0; i < divs.size(); i++) {
    			if(divs.get(i)<=divs.get(divs.size()-1)/p) {
    				long nd=divs.get(i)*p;
				int id=SortedArrays.floor(divs, nd);
    				if(divs.get(id)!=nd)continue;
    				b[id]=(b[id]+b[i])%mod;
    			}
			}
    	}
    	return b;
    }

    /**
     * 約数束上の zeta 変換を、任意の二項演算で行う。
     * 更新は {@code b[id] = op.applyAsLong(b[id], b[i])} の順で適用される。
     * @param a 各約数に対応する値の配列
     * @param divs 昇順に並んだ約数列
     * @param primes 最大元の相異なる素因数列
     * @param op 伝播先の値と伝播元の値をマージする演算
     * @return zeta 変換後の配列
     */
    public static long[] zeta(long[] a, ArrayList<Long> divs, ArrayList<Long> primes, LongBinaryOperator op) {
    	//https://atcoder.jp/contests/abc349/submissions/71431698
    	long[]b=Arrays.copyOf(a, a.length);
    	for (long p:primes) {
    		for (int i = 0; i < divs.size(); i++) {
    			if(divs.get(i)<=divs.get(divs.size()-1)/p) {
    				long nd=divs.get(i)*p;
				int id=SortedArrays.floor(divs, nd);
    				if(divs.get(id)!=nd)continue;
    				b[id]=op.applyAsLong(b[id], b[i]);
    			}
			}
    	}
    	return b;
    }
    
    
    /**
     * 未検証！！！
     * @param a
     * @return
     */
    public static long[] moebius(long[] a) {
    	long[]b=Arrays.copyOf(a, a.length);
        for (int i = 1; i < a.length; ++i) {
            for (int j = 2*i; j < a.length; j+=i) {
                b[j] -= b[i];
            }
        }
    	return b;
    }
    
    /**
     * @param a
     * @return
     */
    public static long[] moebius(long[] a, long mod) {
    	long[]b=Arrays.copyOf(a, a.length);
        for (int i = 1; i < a.length; ++i) {
            for (int j = 2*i; j < a.length; j+=i) {
                b[j] -= b[i];
                b[j] = (b[j] + mod) % mod;
            }
        }
    	return b;
    }
    
    /**
     * @param a
     * @param mod
     * @return
     */
    public static long[] supsetMoebius(long[] a, long mod) {
    	//https://atcoder.jp/contests/abc248/submissions/72061542
    	long[]b=Arrays.copyOf(a, a.length);
    	Sieve.expandPrimes(a.length+1);
    	for (int i=2;i<a.length;++i) {
    		if(Sieve.isPrime(i)) {
    			for (int j = 1; i * j < a.length; j ++) {
					b[j] += mod - b[i * j];
					if (b[j] >= mod) b[j] -= mod;
    			}
    		}
    	}
    	return b;
    }
    
    
	/**
	 * a[a.length-1]は最大元として扱う。
	 * @param a
	 * @return
	 */
    public static long[] supsetMoebius(long[] a) {
    	//verified:https://atcoder.jp/contests/abc361/submissions/70345263
    	Sieve.expandPrimes(a.length);
    	long[] b=Arrays.copyOf(a, a.length - 1);
    	for (int i = 1; i < b.length; i++) {
			b[i] -= a[a.length - 1];
		}
    	for (int i = 2; i < b.length; i++) {
    		if(Sieve.isPrime(i)) {
				for (int j = 1; j <= (b.length-1)/i; j++) {
					b[j] -= b[i * j];
				}
			}
		}
    	return ArrayUtils.concat(b, new long[] {a[a.length - 1]});
    }
    

	/**
	 * @param a
	 * @return
	 */
    public static long[] supsetZeta(long[] a) {
    	//https://atcoder.jp/contests/abc393/submissions/75161322
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = 1; i < a.length; ++i) {
        	for (int j = 2*i; j < a.length; j+=i) {
				b[i]+=b[j];
			}
        }
    	return b;
    }

    public static long[] supsetZeta(long[] a, LongBinaryOperator op) {
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = 1; i < a.length; ++i) {
        	for (int j = 2*i; j < a.length; j+=i) {
        		b[i]=op.applyAsLong(b[i], b[j]);
			}
        }
    	return b;
    }

    public static long[] zeta(long[] a) {
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = a.length-1; i >=1; --i) {
        	for (int j = 2*i; j < a.length; j+=i) {
				b[j]+=b[i];
			}
        }
    	return b;
    }

    public static long[] zeta(long[] a, LongBinaryOperator op) {
    	//https://atcoder.jp/contests/abc393/submissions/75161322
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = a.length-1; i >=1; --i) {
        	for (int j = 2*i; j < a.length; j+=i) {
        		b[j]=op.applyAsLong(b[i], b[j]);
			}
        }
    	return b;
    }
    
    /**
     * 未テスト
     * c[k] = Σ[k = gcd(i, j)] a[i]b[j]
     * @param a
     * @param b
     * @return
     */
    public static long[] gcdConvolution(long[] a, long[] b) {
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	if (a.length != b.length) throw new AssertionError();
    	int n = a.length;
    	long[] a2 = supsetZeta(a);
    	long[] b2 = supsetZeta(b);
    	for (int i = 1; i < n; i++) {
			a2[i] *= b2[i];
		}
    	Sieve.expandPrimes(n + 1);
    	for (int i = 2; i < n; ++i) {
    		if(Sieve.isPrime(i)) {
    			for (int j = 1; i * j < n; j ++) {
					a2[j] -= a2[i * j];
    			}
    		}
    	}
    	return a2;
    }
    
    /**
     * 未テスト
     * c[k] = Σ[k = gcd(i, j)] a[i]b[j]
     * @param a
     * @param b
     * @param mod
     * @return
     */
    public static long[] gcdConvolution(long[] a, long[] b, long mod) {
    	//https://judge.yosupo.jp/submission/370196
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	if (a.length != b.length) throw new AssertionError();
    	int n = a.length;
    	long[] a2 = supsetZeta(a, (x, y) -> (x + y) % mod);
    	long[] b2 = supsetZeta(b, (x, y) -> (x + y) % mod);
    	for (int i = 1; i < n; i++) {
			a2[i] = a2[i] * b2[i] % mod;
		}
    	return supsetMoebius(a2, mod);
    }
    
    /**
     * 未テスト
     * c[k] = Σ[k = lcm(i, j)] a[i]b[j]
     * @param a
     * @param b
     * @return
     */
    public static long[] lcmConvolution(long[] a, long[] b) {
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	if (a.length != b.length) throw new AssertionError();
    	int n = a.length;
    	long[] a2 = zeta(a);
    	long[] b2 = zeta(b);
    	for (int i = 1; i < n; i++) {
			a2[i] *= b2[i];
		}
    	return moebius(a2);
    }
    
    /**
     * c[k] = Σ[k = lcm(i, j)] a[i]b[j]
     * @param a
     * @param b
     * @param mod
     * @return
     */
    public static long[] lcmConvolution(long[] a, long[] b, long mod) {
    	//https://judge.yosupo.jp/submission/370202
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	if (a.length != b.length) throw new AssertionError();
    	int n = a.length;
    	long[] a2 = zeta(a, (x, y) -> (x + y) % mod);
    	long[] b2 = zeta(b, (x, y) -> (x + y) % mod);
    	for (int i = 1; i < n; i++) {
			a2[i] = a2[i] * b2[i] % mod;
		}
    	return moebius(a2, mod);
    }

    public static long[] mul(long[] a, long[] b) {
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	long[]ret=new long[Math.max(a.length, b.length)];
    	for (int i = 1; i < a.length; i++) {
    		for (int j = 1; j <= (ret.length - 1) / i && j < b.length; j++) {
				ret[i * j] += a[i] * b[j];
			}
		}
    	return ret;
    }
    
    public static long[] mul(long[] a, long[] b, long mod) {
    	if (a[0] !=0 || b[0] != 0) throw new AssertionError();
    	long[]ret=new long[Math.max(a.length, b.length)];
    	for (int i = 1; i < a.length; i++) {
    		for (int j = 1; j <= (ret.length - 1) / i && j < b.length; j++) {
				ret[i * j] += a[i] * b[j];
				ret[i * j] %= mod;
			}
		}
    	return ret;
    }
    
    /**
     * O(n log n)
     * @param a
     * @param mod
     * @return
     */
    public static long[] inv(long[] a, long mod) {
    	Fp mo=new Fp(mod);
    	if (a[0] != 0) throw new AssertionError();
    	if (a[1] == 0) throw new AssertionError();
    	long[] b=new long[a.length];
    	long ia = mo.inv(a[1]);
    	b[1] = ia;
    	for (int i = 1; i + 1 < a.length; i++) {
    		//b[i]まで確定済み
    		for (int j = 2; i * j < a.length; j++) {
				b[i * j] -= a[j] * b[i];
				b[i * j] = mo.reduce(b[i * j]);
    		}
    		b[i + 1] = mo.reduce(b[i + 1] * ia);
    		//k ≠ 1 のとき 0 = ∑ a[u] b[v] for uv = k
    		//           0 = a[1]b[k] + ∑ a[u] b[v] for uv = k and u ≠ 1
    	}
    	return b;
    }
    
    /**
     * 
     * @param a
     * @param mod
     * @return
     * verified:https://atcoder.jp/contests/abc428/submissions/70321995
     */
    public static long[] log(long[] a, long mod) {
    	if (a[0] != 0) throw new AssertionError();
    	if (a[1] != 1) throw new AssertionError();
    	// F'=a'/a
    	return integrate(mul(derivative(a, mod), inv(a, mod), mod), mod);
    }
    
    public static long[] derivative(long[] a, long mod) {
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = 1; i < b.length; i++) {
    		b[i] = b[i] * Sieve.omega(i) % mod;
    	}
    	return b;
    }
    
    public static long[] integrate(long[] a, long mod) {
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = 1; i < b.length; i++) {
			b[i] = b[i] * MathUtils.modInv(Sieve.omega(i), mod) % mod;
		}
    	return b;
    }
    
    /**
     * 1+a+a^2+...
     * @param a
     * @param mod
     * @return
     */
    public static long[] geometricSum(long[] a, long mod) {
    	//https://atcoder.jp/contests/abc239/tasks/abc239_h (サンプルのみ)
    	if(a[0]!=0)throw new AssertionError();
    	if(a[1]==1)throw new AssertionError();
    	long[]b=Arrays.copyOf(a, a.length);
    	for (int i = 2; i < a.length; i++) {
			b[i]=mod-a[i];
		}
    	b[1]=(1+mod-a[1])%mod;
    	if(b[1]==1)
    		return inv(b, mod);
    	else {
    		long invB1=MathUtils.modInv(b[1], mod);
    		for (int i = 0; i < b.length; i++) {
				b[i]= invB1 * b[i] %mod;
			}
    		long[] ret=inv(b, mod);
    		for (int i = 0; i < ret.length; i++) {
				ret[i]=ret[i]*invB1%mod;
			}
    		return ret;
    	}
    }
    
    public static ArrayOnQuotient prefixSumOfInv(ArrayOnQuotient sumA, long mod) {
    	//https://atcoder.jp/contests/abc239/submissions/74378739
    	//https://judge.yosupo.jp/problem/dirichlet_inverse_and_prefix_sums
    	// b =  a ^ -1 を求める。a(1) = 1 と仮定する。
    	// δ = a * b より
    	// 1 = a(1) b(1)
    	// 0 = a(n) b(1) + Σ[xy=n,y≠1]a(x)b(y)
    	// よって
    	// b(n) =  1 - Σ[xy=n,y≠1]a(x)b(y)
    	
    	// ∑[n=1..N] b(n) 
    	// = 1 - Σ[n=1..N]Σ[xy=n,x≠1]a(x)b(y)
    	// = 1 - Σ[1 <= xy <= N,x≠1]a(x)b(y)
    	// = 1 - Σ[x=2..N]a(x)∑[y=1..N/x]b(y)
    	
    	// k <= N / x < k + 1
    	//  N / (k + 1) < x <= N / k
    	// より x ∈ {[N/k] : k ∈ {1,2,..,n}} に対する
    	// ∑[i=1..x] a(i) が必要。
    	
    	
    	long n=sumA.n();
    	
    	long a1 = sumA.getByQuotient(1);
    	long ia1 = MathUtils.modInv(a1, mod);
    	for (var range:Itertools.floorRange(n)) {
    		sumA.setByQuotient(range.quotient(), sumA.getByQuotient(range.quotient()) * ia1 % mod);
    	}
	int sqrtn = (int)MathUtils.sqrt(sumA.n());
    	int threshold = (int) (n / (sqrtn + 1));
    	long[] a = sumA.rawArray();
    	long[] b = new long[a.length];
    	for (long q = 1; q <= n; q = n / (n / (q + 1))) {
            // ∑[n=1..range.quotient()] b(n) を求める
            long sum = 1;
            for (long p = 1; p <= q; p = q / (q / (p + 1))) {
                // 1 - Σ[x=2..N]a(x)∑[y=1..N/x]b(y) の N/x を p とした場合
            	long lower = q / (p + 1) + 1;
            	long upper = q / p;
            	// 閉区間[lower, upper]を考える。
            	lower = Math.max(lower, 2);
            	if (lower <= upper) {
            		long u = b[(int)(p <= threshold ? (b.length - p) : (n / p - 1))];//sumB.getByQuotient(p)
//            		long v = sumA.getByQuotient(upper) - sumA.getByQuotient(lower - 1);
            		long v = a[(int)(upper <= threshold ? (a.length - upper) : (n / upper - 1))] - a[(int)(lower - 1 <= threshold ? (a.length - lower + 1) : (n / (lower - 1) - 1))];
            		sum = (sum - u * v) % mod;
            	}
                if (p == q) break;
            }
            if (sum < 0) {
                sum += mod;
            }
            b[(int)(q <= threshold ? (b.length - q) : (n / q - 1))] = sum;
//            sumB.setByQuotient(q, sum);
            if (n == q) break;
        }
    	for (int i = 0; i < b.length; i++) {
			b[i] = b[i] * ia1 % mod;
		}
    	var sumB=new ArrayOnQuotient(n);
    	sumB.data = b;
    	return sumB;
    }
    
    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
