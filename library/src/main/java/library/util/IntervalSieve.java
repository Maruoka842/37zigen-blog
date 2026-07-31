package library.util;

import java.util.ArrayList;

public class IntervalSieve {
	
	/**
	 * 各 x∈[l, r)について、p^eを約数に持ち、e>0が最大なものついて、{p, e}を並べたものを返す
	 */
	public static ArrayList<long[]>[] enumeratePrimePowerDivisors(long l, long r) {
		int sqrt=(int)MathUtils.sqrt(r);
		var primes=new ArrayList<Integer>();
		Sieve.expandPrimes(sqrt+1);
		for (int i = 0; i <= sqrt; i++) {
			if(Sieve.isPrime(i)) {
				primes.add(i);
			}
		}
		ArrayList<long[]>[] ret=new ArrayList[(int)(r-l)];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=new ArrayList<>();
		}
		long[] a=new long[(int)(r-l)];
		for (int i = 0; i < r-l; i++) {
			a[i]=l+i;
		}
		for (long p : primes) {
			for (long j=p*Math.ceilDiv(l, p); j < r; j += p) {
				int e=0;
				while(a[(int)(j-l)]%p==0) {
					a[(int)(j-l)]/=p;
					++e;
				}
				if(e>0)ret[(int)(j-l)].add(new long[] {p, e});
			}
		}
		for (int i = 0; i < ret.length; i++) {
			if(a[i] > 1) ret[i].add(new long[] {a[i], 1});
		}
		return ret;
	}
	
	
}
