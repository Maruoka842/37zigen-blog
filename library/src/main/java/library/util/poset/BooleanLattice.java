package library.util.poset;

import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.Ints;
import library.util.MathUtils;
import library.util.polynomial.PolynomialFpDynamic;

public class BooleanLattice {
	/**
	 * https://judge.yosupo.jp/submission/358736
	 * @param a
	 * @param b
	 * @param mod
	 * @return
	 */
	public static long[] compose(long[]a, long[]b, long mod) {
		// a(b) = Σ a^(i)(b[0]) * (b-b[0])^i /i!
		int n = MathUtils.floorLog2(b.length);
		long[] powb0 = new long[a.length+1];// powb0[i] = b[0] ^ i
		powb0[0] = 1;
		for (int i = 0; i < powb0.length - 1; i++) {
			powb0[i + 1] = b[0] * powb0[i] % mod;
		}
		long[][] c = new long[n + 1][1];//c[i] = a^(i)(b[0])
		for (int i = 0; i <= n && i <= a.length; i++) {
			for (int j = i; j < a.length; j++) {
				long fac = 1;
				for (int k = 0; k < i; k++) {
					fac = fac * (j - k) % mod;
				}
				c[i][0] += fac * a[j] % mod * powb0[j - i];
				c[i][0] %= mod;
			}
		}
		// d = b - b[0] と置く。
		// a(b) = c(d) = Σ c[i] * d^i /i!
		
		// c[k] = a^(k)(b) ただしx_{i+1},x_{i+2},..,x_{n-1}は0とする
		//　となるようにテイラー展開を用いて更新する。
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n - i; j++) {
				long[]g = Arrays.copyOfRange(b, 1 << i, 1 << (i + 1));
				long[]h = mul(c[j + 1], g, mod);
				c[j] = Arrays.copyOf(c[j], 2 * c[j].length);
				for (int k = 0; k < h.length; k++) {
					c[j][k | (1 << i)] = h[k];
				}
			}
		}
		return c[0];
	}
	
	/**
	 * 集合関数 a の指数関数 exp(a) を計算する。
	 * ただし、a[0] = 0 である必要がある。
	 * exp(a0 + a1 X) = exp(a0) + exp(a0) a1 X の関係を用いて、
	 * ビットごとに再帰的に計算する。
	 * 計算量: O(N^2 2^N) (N = log2(as.length))
	 * @param as 集合関数
	 * @param mod 法
	 * @return exp(as)
	 */
	public static long[] exp(long[] as, long mod) {
		if (as[0] != 0) throw new AssertionError();
		int n = MathUtils.floorLog2(as.length);
		long[] bs = {1};
		for (int m = 0; m < n; m++) {
			long[] a_part = Arrays.copyOfRange(as, 1 << m, 1 << (m + 1));
			long[] cs = mul(bs, a_part, mod);
			long[] next_bs = new long[bs.length + cs.length];
			System.arraycopy(bs, 0, next_bs, 0, bs.length);
			System.arraycopy(cs, 0, next_bs, bs.length, cs.length);
			bs = next_bs;
		}
		return bs;
	}

	public static long[] mul(long[]a, long[]b, long mod) {
		//https://judge.yosupo.jp/submission/371224
		long[][] zetaA = rankLiftedZeta(a, mod);
		long[][] zetaB = rankLiftedZeta(b, mod);
		int n = zetaA.length - 1;
		int mask = 1 << n;
		long[][] h = new long[n + 1][mask];
		for (int s = 0; s < mask; s++) {
			for (int i = 0; i <= n; i++) {
				if (zetaA[i][s] == 0) continue;
				for (int j = 0; i + j <= n; j++) {
					h[i + j][s] = (h[i + j][s] + zetaA[i][s] * zetaB[j][s]) % mod;
				}
			}
		}
		return rankLiftedMoebiusAndUnlift(h, mod);
	}

	/**
	 * 集合関数 a の Rank-Lifted Zeta 変換を計算する。
	 * 集合のサイズ k ごとに、そのサイズの集合の部分集合の値を累積する。
	 * 計算量: O(N^2 2^N) (N = log2(a.length))
	 * @param a 集合関数
	 * @param mod 法
	 * @return zeta[k][S] (size(S)=k のとき a[S], それ以外は 0) を Zeta 変換した二次元配列
	 */
	public static long[][] rankLiftedZeta(long[] a, long mod) {
		//https://judge.yosupo.jp/submission/371243
		int n = MathUtils.floorLog2(a.length);
		int mask = 1 << n;
		long[][] zeta = new long[n + 1][mask];
		for (int i = 0; i < mask; i++) {
			zeta[Integer.bitCount(i)][i] = a[i];
		}
		for (int k = 0; k <= n; k++) {
			for (int i = 0; i < n; i++) {
				for (int s = 0; s < mask; s++) {
					if ((s & (1 << i)) == 0) {
						zeta[k][s | (1 << i)] += zeta[k][s];
						if (zeta[k][s | (1 << i)] >= mod) zeta[k][s | (1 << i)] -= mod;
					}
				}
			}
		}
		return zeta;
	}

	/**
	 * 未テスト
	 * Rank-Lifted Zeta 変換された二次元配列に対し、Rank-Lifted Moebius 逆変換を施して元の集合関数を復元する。
	 * 計算量: O(N^2 2^N)
	 * @param zeta Rank-Lifted Zeta 変換後の二次元配列
	 * @param mod 法
	 * @return 復元された集合関数
	 */
	public static long[] rankLiftedMoebiusAndUnlift(long[][] zeta, long mod) {
		int n = zeta.length - 1;
		int mask = 1 << n;
		for (int k = 0; k <= n; k++) {
			for (int i = 0; i < n; i++) {
				for (int s = mask - 1; s >= 0; s--) {
					if ((s & (1 << i)) != 0) {
						zeta[k][s] -= zeta[k][s ^ (1 << i)];
						if (zeta[k][s] < 0) zeta[k][s] += mod;
					}
				}
			}
		}
		long[] res = new long[mask];
		for (int i = 0; i < mask; i++) {
			res[i] = zeta[Integer.bitCount(i)][i];
		}
		return res;
	}
	
	
	/**
	 * c[S]=Σ[S=U∪V]a[U]b[V]
	 * O(n2^n)
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] orConvolution(long[] a, long[] b) {
		var a2=zeta(a);
		var b2=zeta(b);
		for (int i = 0; i < a2.length; i++) {
			a2[i]*=b2[i];
		}
		return moebius(a2);
	}
	
	/**
	 * 未テスト
	 * c[S]=Σ[S=U∩V]a[U]b[V]
	 * O(n2^n)
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[] andConvolution(long[] a, long[] b) {
		long[] a2 = supsetZeta(a);
		long[] b2 = supsetZeta(b);
		for (int i = 0; i < a2.length; i++) {
			a2[i] *= b2[i];
		}
		return supsetMoebius(a2);
	}
	
	/**
	 * c[S]=Σ[S=U∩V]a[U]b[V]
	 * O(n2^n)
	 * @param a
	 * @param b
	 * @param mod
	 * @return
	 */
	public static long[] andConvolution(long[] a, long[] b, long mod) {
		//https://judge.yosupo.jp/submission/370183
		long[] a2 = supsetZeta(a, mod);
		long[] b2 = supsetZeta(b, mod);
		for (int i = 0; i < a2.length; i++) {
			a2[i] = a2[i] * b2[i] % mod;
		}
		return supsetMoebius(a2, mod);
	}
	
	/**
	 * <p>
	 * {@code a[S] == true} は部分集合 {@code S} が利用可能であることを表す。
	 * このメソッドは、利用可能な部分集合のみを用いて全体集合
	 * {@code {0,1,...,n-1}} を被覆するために必要な集合数の最小値を返す。
	 * </p>
	 * O(2^n)
	 * @param a 利用可能な部分集合を表す配列。
	 *          {@code a[S]} が {@code true} なら部分集合 {@code S} を選択可能。
	 *          配列長は {@code 2^n} でなければならない。
	 * @return 全体集合を被覆するために必要な集合数の最小値。
	 *         被覆できない場合は {@code -1} を返す。
	 */
	public static int minmumSetCoverSize(boolean[] a) {
		// https://atcoder.jp/contests/past18-open/editorial/11427
		int n=MathUtils.floorLog2(a.length);
		long[]f=new long[1<<n];
		for (int i = 0; i < a.length; i++) {
			f[i]=a[i]?1:0;
		}
		long[]dp=new long[1<<n];
		Arrays.fill(dp, 1);
		long[]zeta=zeta(f);
		for (int i = 1; i <= n; i++) {//i乗を計算する
			for (int j = 0; j < 1 << (n - i + 1); j++) {
				dp[j]=dp[j]*zeta[j];
			}
			// val = mobius(dp) [(1 << n) - 1)] 
			long val=0;
			for (int j = 0; j < 1 << (n - i + 1); j++) {
				if((n - i + 1 -Integer.bitCount(j))%2==0) {
					val+=dp[j];
				} else {
					val-=dp[j];
				}
			}
			if(val>0)return i;
			for (int j = 0; j < 1 << (n - i); j++) {
				dp[j] = -dp[j] + dp[j | (1 << (n - i))];
			}
		}
		return -1;
	}

	
	/**
	 * O(3^n)
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[] log(long[] a, long mod) {
		if (a[0] != 1) throw new AssertionError();
		long[] b = Arrays.copyOf(a, a.length);
		b[0] = 0;
		int n=MathUtils.floorLog2(a.length);
		for (int s = 1; s < 1 << n; ++s) {
			int mask=(1<<n)-1-s;
			for (int t = 0; t <= mask; t=(t-mask)&mask) {
				//subsetを昇順に舐める。
				//s=mask^tと置くと
				//(t-mask)&mask
				//=(-s)&mask
				//=(2^n-1-(s-1))&mask
				
				if(t>s) {
					b[s|t]-=b[t]*b[s];
					b[s|t]%=mod;
					if(b[s|t]<0)b[s|t]+=mod;
				}
    			if(t==mask)break;
    		}
    	}
		return b;
	}
	
	/**
	 * O(3^n)
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[] expNaive(long[] a, long mod) {
		if(a[0]!=0)throw new AssertionError();
		int n = MathUtils.floorLog2(a.length);
		long[] b = new long[a.length];
		b[0]=1;
		for (int s = 1; s < 1 << n; ++s) {
    		for (int t = s; t >= 1; t=(t-1)&s) {
    			if(t>(s^t)) {
    				b[s]+=a[t]*b[s^t];
    				b[s]%=mod;
    			}
    			if(t==0)break;
    		}
    	}
		return b;
	}
	
	public static long[] pow(long[] a, int k, long mod) {
		if(k == 0) {
			long[]ret=new long[a.length];
			ret[0]=1;
			return ret;
		}
		var ret = pow(mulNaive(a, a, mod), k/2, mod);
		if (k % 2 == 1) {
			ret = mulNaive(ret, a, mod);
		}
		return ret;
	}
	
	public static long[] pow(long[] a, int k) {
		if(k == 0) {
			long[]ret=new long[a.length];
			ret[0]=1;
			return ret;
		}
		var ret = pow(mulNaive(a, a), k/2);
		if (k % 2 == 1) {
			ret = mulNaive(ret, a);
		}
		return ret;
	}
	
	public static long[] add(long[] a, long[] b, long mod) {
		long[] c=new long[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i]=(a[i]+b[i])%mod;
		}
		return c;
	}
	
	/**
	 * c[s] = Σ (a[s \ t] * b[t])
	 * O(3^n)
	 * @param a
	 * @param b
	 * @param mod
	 * @return
	 */
    public static long[] mulNaive(long[] a, long[] b, long mod) {
    	int n = MathUtils.floorLog2(a.length);
    	long[] c = new long[a.length];
    	for (int s = 0; s < 1 << n; ++s) {
    		for (int t = s; t >= 0; t=(t-1)&s) {
    			c[s]+=a[s^t]*b[t];
    			c[s]%=mod;
    			if(t==0)break;
    		}
    	}
    	return c;
    }
    
    
    public static long[] mulNaive(long[] a, long[] b) {
    	int n = MathUtils.floorLog2(a.length);
    	long[] c = new long[a.length];
    	for (int s = 0; s < 1 << n; ++s) {
    		for (int t = s; t >= 0; t=(t-1)&s) {
    			c[s]+=a[s^t]*b[t];
    			if(t==0)break;
    		}
    	}
    	return c;
    }
    
    public static long[] zeta(long[] a, long mod) {
    	long[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]+=b[s];
    			b[s|(1<<i)]%=mod;
    		}
    	}
    	return b;
    }
    
    /**
     * + を logical or で定義
     * @param a
     * @return
     */
    public static boolean[] zeta(boolean[] a) {
    	boolean[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]|=b[s];
    		}
    	}
    	return b;
    }
    
    
    public static long[] zeta(long[] a) {
    	long[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]+=b[s];
    		}
    	}
    	return b;
    }
    
    public static double[] zeta(double[] a) {
    	double[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]+=b[s];
    		}
    	}
    	return b;
    }
    
    /**
     * 未テスト
     * b[S] = Σ[S ⊆ T] a[T]
     * @param a
     * @return
     */
    public static long[] supsetZeta(long[] a) {
    	long[] b = Arrays.copyOf(a, a.length);
    	int N = MathUtils.floorLog2(a.length);
    	for (int i = 0; i < N; ++i) {
    		for (int s = 0; s < 1 << N; ++s) {
    			int ns = s | (1 << i);
    			if (ns == s)
    				continue;
    			b[s] += b[ns];
    		}
    	}
    	return b;
    }
    
    /**
     * 未テスト
     * b[S] = Σ[S ⊆ T] a[T]
     * @param a
     * @param mod
     * @return
     */
    public static long[] supsetZeta(long[] a, long mod) {
    	//https://judge.yosupo.jp/submission/370183
    	long[] b = Arrays.copyOf(a, a.length);
    	int N = MathUtils.floorLog2(a.length);
    	for (int i = 0; i < N; ++i) {
    		for (int s = 0; s < 1 << N; ++s) {
    			int ns = s | (1 << i);
    			if (ns == s)
    				continue;
    			b[s] += b[ns];
    			b[s] %= mod;
    		}
    	}
    	return b;
    }
    
	public static long[] moebius(long[] a) {
		int n = Integer.numberOfTrailingZeros(a.length);
		long[] b = Arrays.copyOf(a, a.length);
		for (int i = 0; i < n; ++i) {
			for (int s = (1 << n) - 1; s >= 0; --s) {
				int ns = s | (1 << i);
				if (ns == s)
					continue;
				b[ns] -= b[s];
			}
		}
		return b;
	}
	
	
	public static long[] moebius(long[] a, long mod) {
		int n = Integer.numberOfTrailingZeros(a.length);
		long[] b = Arrays.copyOf(a, a.length);
		for (int i = 0; i < n; ++i) {
			for (int s = (1 << n) - 1; s >= 0; --s) {
				int ns = s | (1 << i);
				if (ns == s)
					continue;
				b[ns] += mod - b[s];
				if (b[ns] >= mod) b[ns] -= mod;
			}
		}
		return b;
	}
	
	
	
	/***
	 * a(S) = sum[S ⊆ T] b(T)
	 * 
	 * @param a
	 * @return
	 */
	public static long[] supsetMoebius(long[] a) {
		long[] b = Arrays.copyOf(a, a.length);
		int n = Integer.numberOfTrailingZeros(a.length);
		for (int i = 0; i < n; ++i) {
			for (int s = 0; s < 1 << n; ++s) {
				int ns = s | (1 << i);
				if (ns == s)
					continue;
				b[s] -= b[ns];
			}
		}
		return b;
	}
	
	/***
	 * a(S) = sum[S ⊆ T] b(T)
	 * 
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long[] supsetMoebius(long[] a, long mod) {
		//https://judge.yosupo.jp/submission/370183
		long[] b = Arrays.copyOf(a, a.length);
		int n = Integer.numberOfTrailingZeros(a.length);
		for (int i = 0; i < n; ++i) {
			for (int s = 0; s < 1 << n; ++s) {
				int ns = s | (1 << i);
				if (ns == s)
					continue;
				b[s] -= b[ns];
				b[s] %= mod;
				if (b[s] < 0)
					b[s] += mod;
			}
		}
		return b;
	}

	/***
	 * Σa[i]x^i = Σb[i](1+x)^i としたときの b を返す。 
	 * a = Σf(S)x^#S, b = Σg(S)x^#S, g(S) = Σ[S ⊆ T] f(T) としたときこの関係を満たす
	 * @param a
	 * @return
	 */
	public static long[] supsetRankedMoebius(long[] a) {
		long[] b = new long[a.length];
		for (int i = a.length - 1; i >= 0; --i) {
			for (int j = b.length - 1; j >= 0; --j) {
				b[j] = -b[j] + (j > 0 ? b[j - 1] : 0);
			}
			b[0] += a[i];
		}
		return b;
	}
	

	public static long[] rankedMoebius(long[] a) {
		long[] revA=a.clone();
		ArrayUtils.reverse(revA);
		revA=supsetRankedMoebius(revA);
		ArrayUtils.reverse(revA);
		return revA;
	}
	

	
	public static long[] supsetRankedMoebius(long[] a, long mod) {
		long[] b = new long[a.length];
		for (int i = a.length - 1; i >= 0; --i) {
			for (int j = b.length - 1; j >= 0; --j) {
				b[j] = -b[j] + (j > 0 ? b[j - 1] : 0);
				b[j]%=mod;
			}
			b[0] += a[i];
			b[0]%=mod;
		}
		for (int i = 0; i < b.length; i++) {
			b[i]=(b[i]%mod+mod)%mod;
		}
		return b;
	}
	
	
	/** Σa[i][j]x^iy^j = Σb[i](1+x)^i(1+y)^j としたときの b を返す。 
	 */
	public static long[][] supsetRankedMoebius(long[][] a, long mod) {
		//https://atcoder.jp/contests/abc242/submissions/74986770
		long[][] b = new long[a.length][a[0].length];
		Fp fp=new Fp(mod);
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k <= i; k++) {
					for (int l = 0; l <= j; l++) {
						b[k][l]+=fp.mul(fp.comb(i, k), fp.comb(j, l), a[i][j], (i-k)%2==1?(mod-1):1, (j-l)%2==1?(mod-1):1);
						b[k][l]%=mod;
					}
				}
			}
		}
		return b;
	}
	
	
	/**
	 * g(S) = Σ_{T ⊆ S} f(T) とする。
	 * {@code a[k] = Σ_{|S| = k} g(S)} が与えられたとき、
	 * このメソッドは
	 * {@code b[k] = Σ_{|S| = k} f(S)}
	 * を満たす配列 {@code b} を返す。
	 *
	 * @param a {@code a[k] = Σ_{|S| = k} g(S)}
	 * @param mod 法
	 * @return ランク付き Möbius 逆変換後の配列
	 * @see #supsetRankedMoebius(long[], long)
	 * @see #rankedMoebius(long[], long)
	 */
	public static long[] rankedMoebius(long[] a, long mod) {
		long[] revA=a.clone();
		ArrayUtils.reverse(revA);
		revA=supsetRankedMoebius(revA, mod);
		ArrayUtils.reverse(revA);
		return revA;
	}
	
	
	public static long[][] rankedMoebius(long[][] a, long mod) {
		long[][]revA=ArrayUtils.copy(a);
		ArrayUtils.rotate180(revA);
		revA=supsetRankedMoebius(revA, mod);
		ArrayUtils.rotate180(revA);
		return revA;
	}
	
	
	/**
	 * a(S) = sum[S ⊆ T] b(T)について、sumをbitorで定義
	 * @return
	 */
	public static int[] bitorZeta(int[] a) {
		int[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]|=b[s];
    		}
    	}
    	return b;
	}
	
	/**
	 * a(S) = sum[S ⊆ T] b(T)について、sumをbitorで定義
	 * @return
	 */
	public static long[] bitorZeta(long[] a) {
		long[]b=Arrays.copyOf(a, a.length);
    	int N=MathUtils.floorLog2(a.length);
    	for (int i=0;i<N;++i) {
    		for(int s=0;s<1<<N;++s) {
    			if(Ints.bitAt(s, i)==1)continue;
    			b[s|(1<<i)]|=b[s];
    		}
    	}
    	return b;
	}

	/**
	 * 未テスト
	 * y[B] = Σ_{B　∩　U=　∅　} s[U]x[B　∪　U] 
	 * <p>O(N^2 2^N) （N = log_2(s.length)）</p>
	 * @param s 集合べき級数
	 * @param x 入力配列（転置写像への入力）
	 * @param mod 法
	 * @return 転置写像による像 y
	 */
	public static long[] transposedSubsetConvolution(long[] s, long[] x, long mod) {
		long[] revX = x.clone();
		ArrayUtils.reverse(revX);
		long[] conv = mul(revX, s, mod);
		ArrayUtils.reverse(conv);
		return conv;
	}

	/**
	 * 未テスト
	 * EGF 版の集合べき級数の Power Projection を計算する。
	 * 定数項が 0 である集合べき級数 s と、重み配列 w に対し、
	 * ∑_t w[t] [x^t] (s^k / k!) を k = 0, 1, ..., N について一括計算する。
	 *
	 * <p>計算量: O(N^2 2^N) （N = log_2(s.length)）</p>
	 *
	 * @param w 
	 * @param s 定数項が 0 の集合べき級数
	 * @param mod 法
	 * @return 評価結果の配列（長さ N + 1）
	 */
	public static long[] powerProjectionOfSpsEgf(long[] w, long[] s, long mod) {
		int N = MathUtils.floorLog2(s.length);
		if (s.length != (1 << N) || w.length != (1 << N)) {
			throw new IllegalArgumentException("Length of s and wt must be 2^N");
		}
		if (s[0] != 0) {
			throw new IllegalArgumentException("Constant term of s must be 0");
		}
		long[] y = new long[N + 1];
		y[0] = w[0];
		long[] dp = w.clone();
		for (int i = 0; i < N; i++) {
			//i個のブロックを確定済み
			//dp[T] = まだ選んでいない要素の集合がTであるような状態の重み
			long[] newdp = new long[1 << (N - 1 - i)];
			for (int j = 0; j < N - i; j++) {
				//jは今回確定するブロックの最大元
				//j+1,j+2,..,N-1はすでに確定済み
				long[] a = Arrays.copyOfRange(s, 1 << j, 2 << j);
				long[] b = Arrays.copyOfRange(dp, 1 << j, 2 << j);
				b = transposedSubsetConvolution(a, b, mod);
				for (int k = 0; k < b.length; k++) {
					newdp[k] = (newdp[k] + b[k]) % mod;
				}
			}
			dp = newdp;
			y[1 + i] = dp[0];
		}
		return y;
	}

	/**
	 * 未テスト
	 * 集合べき級数の Power Projection を計算する（多項式と集合べき級数の合成の転置）。
	 * 集合べき級数 s と、錘（weight）の配列 wt に対し、
	 * ∑_t wt[t] [x^t] s^k を k = 0, 1, ..., M - 1 について一括計算する。
	 *
	 * <p>計算量: O(N^2 2^N + M log M)（NTT-friendly mod の場合）または O(N^2 2^N + M^2) （N = log_2(s.length)）</p>
	 *
	 * @param wt 錘（weight）の配列
	 * @param s 集合べき級数
	 * @param M 計算する累乗数（k = 0, ..., M - 1）
	 * @param mod 法
	 * @return 評価結果の配列（長さ M）
	 */
	public static long[] powerProjectionOfSps(long[] wt, long[] s, int M, long mod) {
		int N = MathUtils.floorLog2(s.length);
		if (s.length != (1 << N) || wt.length != (1 << N)) {
			throw new IllegalArgumentException("Length of s and wt must be 2^N");
		}
		long c = s[0];
		long[] sPrime = s.clone();
		sPrime[0] = 0;
		// c を s の定数項として s' = s - c とする。
		// (c + s')^k / k! = comb(k, i) c^i (s')^{k-i) 
		// g[i] = c^i / i! とすると
		// (c + s')^k / k! = g[i] (s')^{k-i) / (k - i)! 
		long[] x = powerProjectionOfSpsEgf(wt, sPrime, mod);

		Fp fp = new Fp(mod);
		long[] g = new long[M];
		long pow = 1;
		for (int i = 0; i < M; i++) {
			g[i] = pow * fp.ifac(i) % mod;
			pow = pow * c % mod;
		}

		PolynomialFpDynamic poly = PolynomialFpDynamic.of(mod);
		long[] conv = poly.mul(x, g);

		long[] res = new long[M];
		for (int i = 0; i < M; i++) {
			long val = i < conv.length ? conv[i] : 0;
			res[i] = val * fp.fac(i) % mod;
		}
		return res;
	}

    static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
    
}
