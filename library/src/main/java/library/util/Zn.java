package library.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import library.util.collections.LongArrayList;
import library.util.algebra.strategy.longs.LongCommutativeRingStrategy;

public class Zn implements LongCommutativeRingStrategy {
	/** 剰余環 Z/modZ の法。 */
	final long mod;
	/** combRow で遅延初期化される mod の素因数列。 */
	LongArrayList primeDivisors;
	
	public Zn(long mod) {
		this.mod = mod;
	}
	
	public long modulus() {
		return this.mod;
	}

	/**
	 * 返り値 r は r = 0。
	 * 未テスト
	 * 計算量: O(1)
	 * @return 0
	 */
	@Override
	public long zero() {
		return 0;
	}

	/**
	 * 返り値 r は r ≡ 1 (mod mod) かつ 0 ≤ r < mod。
	 * 未テスト
	 * 計算量: O(1)
	 * @return 1 mod mod
	 */
	@Override
	public long one() {
		return 1 % mod;
	}
	
	public long pow(long a, long n) {
		if (n < 0) throw new AssertionError();
		return MathUtils.modPow(a, n, mod);
	}
	
	/**
	 * f(x)=Ax+B とする。f^i(S) = G となる最小の非負整数 i を返す。そのような i が存在しない場合-1。
	 * @param A
	 * @param B
	 * @param S
	 * @param G
	 * @return
	 */
	public long log(long A, long B, long S, long G) {
		//https://atcoder.jp/contests/abc270/submissions/74380078
		if(S==G) {
			return 0;
		}
		if(A==0) {
			if(G==B) {
				return 1;
			}else {
				return -1;
			}
		}
		
		long invA=MathUtils.modInv(A, mod);
		if (A*invA%mod!=1) throw new AssertionError();
		// A^(qx+r) == G
		// (A^q)^x == G A^(-r)
		HashMap<Long, Long>map=new HashMap<>();
		int sqrtp=(int)MathUtils.sqrt(mod)+1;
		{
			long z=G;
			map.put(z, 0L);
			for (int i = 1; i <= sqrtp; i++) {
				// z=Aw+B
				z=(z-B)*invA%mod;
				z=reduce(z);
				if(map.containsKey(z)) {
					break;
				} else {
					map.put(z, 1L*i);
				}
			}
			
		}
		
		long a=A;
		long b=B;
		for (int i = 2; i <= sqrtp; i++) {
			long na=a*A%mod;
			long nb=(B+A*b)%mod;
			a=na;
			b=nb;
		}
		long ans=Long.MAX_VALUE;
		long x=S;
		for (int i = 0; i <= sqrtp; i++) {
			if(map.containsKey(x)) {
				long r=map.get(x);
				ans=Math.min(ans, sqrtp*i+r);
			}
			x=(a*x+b)%mod;
		}
		if(ans==Long.MAX_VALUE) {
			return -1;
		}else {
			return ans;
		}
	}
	
	/**
	 * a+(dir)x=bとなる最小のx。
	 * dir ∈ {-1, 1} でないときエラー。
	 * @param a
	 * @param b
	 * @param dir
	 * @return
	 */
	public long dist(long a, long b, int dir) {
		//https://atcoder.jp/contests/abc376/submissions/74353459
		if (dir != 1 && dir != -1) throw new AssertionError();
		if (dir == -1) {
			{
				{
					var tmp = a;
					a = b;
					b = tmp;
				}
			}
		}
		long ret = (b - a) % mod;
		if (ret < 0) ret += mod;
		return ret;
	}
	
	/**
	 * (sum[i=0..X-1] A^i) mod M
	 * @param A
	 * @param n
	 * @param M
	 * @return
	 */
	public long geometricSum(long A, long n) {
		//https://atcoder.jp/contests/abc448/submissions/73914454
		//https://atcoder.jp/contests/abc293/submissions/73939096
		if(n==0)return 0;
		A%=mod;
		if(n%2==0) {
			return (1+A)*geometricSum(A*A%mod,n/2)%mod;
		}else {
			return (1+A*geometricSum(A,n-1))%mod;
		}
	}
	
	
	/**
	 * {@code dp[i][j] = C(i, j) = i! / (j! (i-j)!)} を
	 * {@code 0 ≤ i ≤ N, 0 ≤ j ≤ i} について満たす(N+1)×(N+1)配列を返す。
	 * @param N
	 * @return
	 */
	public long[][] combTable(int N) {
		long[][] ret=new long[N+1][N+1];
		ret[0]=new long[1];
		ret[0][0]=1;
		for (int i = 1; i <= N; i++) {
			ret[i][0]=1;
			ret[i][i]=1;
			for (int j = 1; j < i; j++) {
				ret[i][j]=(ret[i-1][j]+ret[i-1][j-1])%mod;
			}
		}
		return ret;
	}
	
	/**
	 * a[i]=N!/(i!(N-i)!) % modとなる配列を返す。
	 * 分母・分子に含まれる mod の素因数を指数管理しながら計算。
	 * O(N*(modの素因数の数))
	 * @param N
	 * @return
	 * verified:https://atcoder.jp/contests/abc284/submissions/70904096
	 */
	public long[] combRow(int N) {
		Map<Long, Long> map=new HashMap<>();
		long[] ret=new long[N+1];
		ret[0]=1;
		if(primeDivisors == null) primeDivisors = MathUtils.primeDivisors(mod);
		long comb=1;
		for (int i = 1; i <= N; i++) {
			{
				long a=N-i+1;
				long b=i;
				for (long p:primeDivisors) {
					long e=0;
					while(a%p==0) {
						a/=p;++e;
					}
					if(e>0)map.merge(p, e, Long::sum);
				}
				for (long p:primeDivisors) {
					long e=0;
					while(b%p==0) {
						b/=p;++e;
					}
					if(e>0)map.merge(p, -e, Long::sum);
				}
				comb=comb*a%mod*MathUtils.modInv(b, mod)%mod;
			}
			ret[i] = comb;
			for (var es:map.entrySet()) {
				ret[i]=ret[i]*MathUtils.modPow(es.getKey(), es.getValue(), mod)%mod;
			}
			
		}
		return ret;
	}
	
	/**
	 * ax=b mod mを満たすxを返す。存在しないときは-1を返す。
	 * 返り値をx₀とすると解は
	 * x₀ + (m / gcd(m, a)) i for i = 0.. m / gcd(m, a) - 1
	 * の m / gcd(m, a) 個ある。
	 * @param a
	 * @param b
	 * @return
	 */
	public static long solveLinearCongruence(long a, long b, long m) {
		//https://atcoder.jp/contests/abc446/submissions/73502859
		long g=MathUtils.gcd(a, m);
		if(b%g!=0)return -1;
		a/=g;
		b/=g;
		m/=g;
		return MathUtils.modInv(a, m)*b%m;
	}
	
	
	/**
	 * ax=b mod mを満たすxを返す。存在しないときは-1を返す。
	 * 返り値をx₀とすると解は
	 * x₀ + (m / gcd(m, a)) i for i = 0.. m / gcd(m, a) - 1
	 * の m / gcd(m, a) 個ある。
	 * @param a
	 * @param b
	 * @return
	 */
	public long solveLinearCongruence(long a, long b) {
		long g=MathUtils.gcd(a, mod);
		if(b%g!=0)return -1;
		a/=g;
		b/=g;
		return MathUtils.modInv(a, mod / g)*b%mod;
	}
	
	
	/**
	 * x mod m[i] == a[i] から x を復元する。
	 * mが互いに素でない場合でも可能。
	 * 2数ずつCRTするとO(N^2log max(a))からO(N log max(a))になるが...
	 * 実装の軽いO(N^2)はこれhttps://yukicoder.me/problems/no/3396/editorial。
	 * @param a
	 * @param m
	 * @return
	 * verified:https://atcoder.jp/contests/abc193/submissions/70562931
	 */
	public static long crtComposites(long[] a, long[] m) {
		int N=a.length;
		long[] A=Arrays.copyOf(a, a.length);
		long[] M=Arrays.copyOf(m, m.length);
		for (int i = 0; i < N; i++) {
			A[i]%=M[i];
			if(A[i]<0)A[i]+=M[i];
		}
		for (int i = 0; i < N; i++) {
			for (int j = i+1; j < N; j++) {
				long g=MathUtils.gcd(M[i], M[j]);
				if(A[i]%g!=A[j]%g)return -1;
				while (g != 1) {
					long gi=MathUtils.gcd(M[i]/g, g);
					long gj=MathUtils.gcd(M[j]/g, g);
					if (gi == 1 && gj == 1) break;
					M[j] /= gi;
					M[i] /= gj;
					g=MathUtils.gcd(M[i], M[j]);
				}
				M[i] /= MathUtils.gcd(M[i], M[j]);
				A[i] %= M[i];
				A[j] %= M[j];
			}
		}
		return crt(A, M);
	}

	
	
	/**
	 * x mod m[i] == a[i] から x を復元する。
	 * m[i]は互いに素を仮定して計算している。
	 * 互いに素でないとバグる
	 * @param a
	 * @param m
	 * @return
	 * verified:https://atcoder.jp/contests/abc286/tasks/abc286_f
	 */
	public static long crt(long[] a, long[] m) {
		//x=c[0]+c[1]m[0]+c[2]m[0]m[1]+..
		// 0<= c[i] < m[i+1]
		//の形で管理する
		//このとき
		//x <=m[0]-1+(m[1]-1)m[0]+..+(m[i]-1)m[1]m[2]..m[i-1]<= m[0]m[1]..m[i]-1
		
		int N=a.length;
		
		
		long fac=1;
		long x = 0;
		for (int i = 0; i < N; i++) {
			// fac=m[1]m[2]..m[i-1]として
			// x%m[i]+(fac)*c[i]==a[i]
			Fp mo=new Fp(m[i]);
			long c=mo.reduce(a[i]%m[i]-x%m[i])*MathUtils.modInv(fac%m[i], m[i]);
			c=mo.reduce(c);
			x=x+fac*c;
			fac*=m[i];
		}
		return x;
	}
	
	
	public static long crt(int[] a, int[] m) {
		//x=c[0]+c[1]m[0]+c[2]m[0]m[1]+..
		// 0<= c[i] < m[i+1]
		//の形で管理する
		//このとき
		//x <=m[0]-1+(m[1]-1)m[0]+..+(m[i]-1)m[1]m[2]..m[i-1]<= m[0]m[1]..m[i]-1
		
		int N=a.length;
		
		
		long fac=1;
		long x = 0;
		for (int i = 0; i < N; i++) {
			// fac=m[1]m[2]..m[i-1]として
			// x%m[i]+(fac)*c[i]==a[i]
			Fp mo=new Fp(m[i]);
			long c=mo.reduce(a[i]%m[i]-x%m[i])*MathUtils.modInv(fac%m[i], m[i]);
			c=mo.reduce(c);
			x=x+fac*c;
			fac*=m[i];
		}
		return x;
	}
	
	class Sol {
		long xstep;
		long ystep;
		long m;
		public Sol(long xstep, long ystep, long m) {
			
		}
	}
	
	
	/**
	 * ax + by = 0 の解の構造を返す。
	 *　解は 
	 * x = (xstep) t + m u 
	 * y = (ystep) t + m v 
	 * t = 0..m-1
	 * u,v = 0..mod/m-1
	 * (xstep, ystep) は xstep 最小となるように取っている。
	 * 
	 * 未テスト
	 * 
	 * @param a
	 * @param b
	 * @param m
	 * @return
	 */
	Sol solveAxPlusByEq0(long a, long b) {
		long gcd=MathUtils.gcd(a, b, mod);
		a /= gcd;
		b /= gcd;
		long m = mod / gcd;
		// ax + by = 0 mod m
		// gcd(a, b, m) = 1
		long g = MathUtils.gcd(b, m);
		a = a * MathUtils.modInv(b / g, m) % m;
		b = 1;
		/*
		 * ax + gy = 0 mod (m / gcd)
		 * ⇔
		 * x = g * t mod (m / gcd)
		 * y = -a * t mod (m / gcd)
		 * a か g のどちらかが (m / gcd) と互いに素なので m / gcd 通り。
		 * (x, y) = (g, -a) が (x, y) ≠ (0, 0) となり、x が最小の解。
		 * mod m に持ち上げると
		 * x = b * t mod (m / gcd) + (m / gcd) u (u = 0..gcd-1)
		 * y = -g * t mod (m / gcd) + (m / gcd) v (v = 0..gcd-1)
		 * となり、 (m / gcd) * gcd^2 = m (gcd) 通り
		 */
		return new Sol(g, (m - a % m) % m, mod / gcd);
	}
	
	
	
    /**
     * sum[i=1..n] ia^i を mod で割った余りを返す。
     * modが合成数でも動く。
     * O(log n)
     * @param a
     * @param n
     * @return
     * verified:https://atcoder.jp/contests/abc129/submissions/70553934
     */
	public long arithmeticoGeometricSum(long a, long n) {
    	a%=mod;
    	if(n==0)return 0;
    	if(a==0)return 0;
    	long[] f=new long[70];
    	long[] g=new long[70];
    	long[] A=new long[70];//A[i]=a^{2^i}
    	long[] TWO=new long[70];//TWO[i]=2^i
    	
    	//f[i]=sum[i=0..(2^i)-1]ia^i
    	//g[i]=sum[i=0..(2^i)-1] a^i
    	f[0]=0;
    	f[1]=a;
    	g[0]=1;
    	g[1]=1+a;
    	A[0]=a;
    	TWO[0]=1;
    	for (int i = 1; i < A.length; i++) {
			A[i]=A[i-1]*A[i-1]%mod;
			TWO[i]=TWO[i-1]*2%mod;
		}
    	for (int i = 2; i < f.length; i++) {
    		// sum[j=2^{i-1}..(2^i-1)} ja^j
    		//=sum[j=0..(2^{i-1}-1)} (2^{i-1}+j)a^(2^{i-1}+j)
    		//=a^{i-1}(2^{i-1}sum[j=0..(2^{i-1}-1)} a^j + sum[j=0..(2^{i-1}-1)} ja^j )
    		f[i]=f[i-1]+mul(A[i-1], f[i-1]+g[i-1]*TWO[i-1]%mod);
			f[i]%=mod;
			g[i]=mul(g[i-1], 1 + A[i-1]);
    	}
    	long ans=0;
    	long s=0;
    	long res=n+1;
		long prefix=1;
    	while (res!=0) {
			long t=Long.highestOneBit(res);
    		//sum[i=s..s+t-1] ia^i
			ans+=mul(s, prefix, g[MathUtils.floorLog2(t)]);
			ans+=mul(prefix, f[MathUtils.floorLog2(t)]);
			ans%=mod;
			prefix=prefix*A[MathUtils.floorLog2(t)]%mod;
			s=s+t;
			res-=t;
    	}
    	return ans;
    }
	
	@Override
	public long add(long a, long b) {
		long ret = (a + b) % mod;
		if (ret < 0) ret += mod;
		return ret;
	}
	
	public long sub(long a, long b) {
		long ret = (a - b) % mod;
		if (ret < 0) ret += mod;
		return ret;
	}
	
	@Override
	public long mul(long a, long b) {
		return a*b%mod;
	}

	public long mul(long a, long b, long c) {
		return a*b%mod*c%mod;
	}
	
	public long mul(long a, long b, long c, long d) {
		return mul(a, b, c) * d % mod;
	}
	
	public long mul(long a, long b, long c, long d, long e) {
		return mul(a, b, c, d) * e % mod;
	}
	
	public long mul(long a, long b, long c, long d, long e, long f) {
		return mul(a, b, c, d, e) * f % mod;
	}
	
	/**
	 * 返り値 r は r ≡ -a (mod mod) かつ 0 ≤ r < mod。
	 * 未テスト
	 * 計算量: O(1)
	 * @param a 被減元
	 * @return -a mod mod
	 */
	@Override
	public long neg(long a) {
		return a == 0 ? 0 : mod - a;
	}

	/**
	 * 返り値 r は r ⇔ a = b。
	 * 未テスト
	 * 計算量: O(1)
	 * @param a 比較対象
	 * @param b 比較対象
	 * @return a == b
	 */
	@Override
	public boolean equals(long a, long b) {
		return a == b;
	}

	/***
	 * 剰余を取り、0以上mod未満の値を返す。
	 * @param a
	 * @return
	 */
	public long reduce(long a) {
		a %= mod;
		if (a < 0) a += mod;
		return a;
	}
}
