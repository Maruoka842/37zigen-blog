package library.util;

import java.util.Arrays;

import library.util.collections.ArrayOnQuotient;
import library.util.collections.IntArrayList;
import library.util.segtree.IntSumBinaryIndexedTree;

public class PrimeCount {
	static int sqrt;
	/**
	 * https://judge.yosupo.jp/submission/347216
	 * @param n
	 * @return
	 */
	public static long cnt(long n) {
		sqrt=(int)MathUtils.sqrt(n);
		int cbrt=1;
		while(1L*cbrt*cbrt*cbrt<=n)++cbrt;
		--cbrt;
		IntSumBinaryIndexedTree tree=new IntSumBinaryIndexedTree(1 + (int) (n / cbrt));
		
		IntArrayList lowerPrimes=new IntArrayList();
		IntArrayList upperPrimes=new IntArrayList();
		int[] sieved = new int[(int)(n / cbrt) + 1];
		Sieve.expandPrimes((int)(n / cbrt));
		for(int i=2;i<=sqrt;++i)if(Sieve.isPrime(i)) {
			if(i<=cbrt)
				lowerPrimes.add(i);
			else
				upperPrimes.add(i);
			for (int j = i; j <= n / cbrt; j += i) sieved[j]++;
		}
		for(int i=1;i<=n/cbrt;++i)if(sieved[i]==0)tree.add(i, 1);
		long ans = lowerPrimes.size() + upperPrimes.size();
		for(int i=upperPrimes.size()-1;i>=0;--i) {
			long p=upperPrimes.get(i);
			for(int j=(int)p;j<=n/cbrt;j+=p) {
				sieved[j]--;
				if(sieved[j]==0)tree.add(j, 1);
			}
			ans-=tree.fold(0, 1 + (int) (n / p));
		}
		long[]f=new long[2 * sqrt + 1];
		f[idFromQuotient(n, n)]=1;
		for (int i = lowerPrimes.size() - 1; i >= 0; --i) {
			long p = lowerPrimes.get(i);
			for (int j = (int) p; j <= n / cbrt; j += p) {
				sieved[j]--;
				if (sieved[j] == 0) {
					tree.add(j, 1);
				}
			}
			
			// v <= n/d < v + 1
			{
				long q = n / cbrt;
				long d = cbrt;
				while (true) {
					long nq = q / p;
					if (nq >= n / cbrt) {
						f[idFromQuotient(n, nq)]-=f[idFromQuotient(n, q)];
					} else {
						ans-= tree.fold(0, (int) nq + 1) * f[idFromQuotient(n, q)];
					}
					if (q == n)
						break;
					d = n / (q + 1);
					q = n / d;
				}
			}
			
		}
		for(int i=0;i<f.length;++i) {
			ans+=QuotientFromId(n, i)*f[i];
		}
		ans--;//1を除く
		return ans;
	}
	
	/***
	 *  quotientに対して狭義単調増加にid∈{0,1,...,2sqrt}を割り振る。全単射ではない。
	 * @param n
	 * @param quotient
	 * @return
	 */
	static int idFromQuotient(long n, long quotient) {
		if (quotient > n) throw new AssertionError();
		if (quotient <= sqrt) {
			return (int) quotient;
		} else {
			return 2 * sqrt + 1 - (int) (n / quotient);
		}
	}
	
	/***
	 *  idFromQuotientの逆関数。
	 * @param n
	 * @param id
	 * @return
	 */
	static long QuotientFromId(long n, int id) {
		if (id <= sqrt) return id;
		id = -(id - 2 * sqrt - 1);
		return n / id;
	}
	
	
	/**
	 * f(1)は定義域外として足していない。
	 * https://judge.yosupo.jp/submission/347232
	 * https://atcoder.jp/contests/abc370/submissions/72648385
	 * @param n
	 * @return
	 */
	public static ArrayOnQuotient primeSumToMultiplicativeFunctionSumOnFp(ArrayOnQuotient primeSum, multiplicativeFunction f, long mod) {
		long n=primeSum.getN();
		var ret=primeSum.copy();
		sqrt = (int)MathUtils.sqrt(n);
		Sieve.expandPrimes(sqrt+1);
		for (int p=sqrt;p>=2;--p) {
			if(Sieve.isPrime(p)) {
				// v の降順
				// v <= n/i < v + 1
				// n / (v + 1) < i <= n / v
				long b=(ret.getByQuotient(p-1)+f.apply(p, 1))%mod;//f(2)+f(3)+..+f(p)
				{
					long v =  n;
					long i = 1;
					while (true) {
						if (1L * p * p > v) break;
						for (long pe=p, e=1; p <= (v - 1) / pe; pe*=p,e++) {
							// p < lpf(x) を満たす f(p^e x) を足す。 
							ret.setByQuotient(v, (ret.getByQuotient(v)+f.apply(p, (int)e)*(ret.getByQuotient(v / pe) - b))%mod);
						}
						for (long pe=1L*p*p, e=2; pe <= v; pe*=p,e++) {
							// f(p^e) (e ≥ 2) を足す。 
							ret.setByQuotient(v, (ret.getByQuotient(v)+f.apply(p, (int)e))%mod);
						}
						
						if (v == 1)
							break;
						i = n / v + 1;
						v = n / i;
					}
				}
			}
		}
		return ret;
	}

	
	
	/**
	 * 乗法的関数fの累積和に対して、SUM f(i) for i=1..n/k for prime iを返す。
	 * https://atcoder.jp/contests/abc370/submissions/72648385
	 * @param prefixSum
	 * @return
	 */
	public static ArrayOnQuotient completelyMultiplicativeFunctionSumToPrimeSum(ArrayOnQuotient prefixSum) {
		long n=prefixSum.getN();
		sqrt = (int)MathUtils.sqrt(n);
		var ret=prefixSum.copy();
		if (ret.getByQuotient(1)!=0) {
			long a=ret.getByQuotient(1);
			// v <= n/i < v + 1
			// n / (v + 1) < i <= n / v
			{
				long v = 1;
				long i = n;
				while (true) {
					ret.addByQuotient(v, -a);
					if (v == n)
						break;
					i = n / (v + 1);
					v = n / i;
				}
			}
		}
		Sieve.expandPrimes(sqrt+1);
		for (int p=2;p<=sqrt;++p) {
			if(Sieve.isPrime(p)) {
				// v の降順
				// v <= n/i < v + 1
				// n / (v + 1) < i <= n / v
				long a=prefixSum.getByQuotient(p)-prefixSum.getByQuotient(p-1);//f(p)
				long b=ret.getByQuotient(p-1);//f(2)+f(3)+..+f(q) (qはp未満の最大の素数)
				{
					long v =  n;
					long i = 1;
					while (true) {
						if (1L * p * p > v) break;
						ret.addByQuotient(v, -a*(ret.getByQuotient(v / p) - b));
						if (v == 1)
							break;
						i = n / v + 1;
						v = n / i;
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * https://judge.yosupo.jp/submission/347232
	 * https://atcoder.jp/contests/abc370/submissions/72648385
	 * @param n
	 * @return
	 */
	public static ArrayOnQuotient cntForQuotients(long n) {
		sqrt = (int)MathUtils.sqrt(n);
		var arr=new ArrayOnQuotient(n);
		// v <= n/i < v + 1
		// n / (v + 1) < i <= n / v
		{
			long v = 1;
			long i = n;
			while (true) {
				arr.setByQuotient(v, v-1);
				if (v == n)
					break;
				i = n / (v + 1);
				v = n / i;
			}
		}
		return completelyMultiplicativeFunctionSumToPrimeSum(arr);
	}
	
	public interface multiplicativeFunction {
		long apply(long p, int e);
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
