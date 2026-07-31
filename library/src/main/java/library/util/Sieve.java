package library.util;

import java.util.Arrays;

import library.util.collections.IntArrayList;

public class Sieve {
	static boolean[] isPrime = new boolean[0];
	static int[] totient = new int[0];
	static int[] omega = new int[0];
	static int[] lpf = new int[0];
	
	public static void expandPrimes(int n) {
		if (n < isPrime.length) return;
		n = 2 * Integer.highestOneBit(Math.max(1, n));
		isPrime=new boolean[n];
    	Arrays.fill(isPrime, true);
    	isPrime[0] = isPrime[1] = false;
    	for (int i = 2; i < isPrime.length; ++i) {
    		if (isPrime[i]) {
    			for (int j = 2 * i; j < isPrime.length; j += i) {
    				isPrime[j] = false;
    			}
    		}
    	}
	}
	
	public static boolean isPrime(int n) {
		if (n >= 1e8) throw new AssertionError("Miller-Rabin素数判定法を実装しろ");
		expandPrimes(n);
		return isPrime[n];
	}
	
	public static int totient(int n) {
		if (n >= totient.length) {
			totient=new int[2 * Integer.highestOneBit(n)];
			for (int i = 0; i < totient.length; i++) {
				totient[i] = i;
			}
			expandPrimes(n);
			for (int i = 0; i < totient.length; i++) {
				if (isPrime(i)) {
					for (int j = i; j < totient.length; j+=i) {
						totient[j]-=totient[j]/i;
					}
				}
			}
		}
		return totient[n];
	}
	
	/**
	 *  素因数分解 n = prod {p_i}^{e_i} について e_0 + e_1 + ... を返す。 
	 * @param n
	 * @return
	 */
	public static int omega(int n) {
		if (omega.length <= n) {
			if (lpf.length <= n) {
				lpf = new int[2 * Integer.highestOneBit(n)];
				Arrays.setAll(lpf, i -> i);
				for (int i = 2; i < lpf.length; i++) {
					if (i == lpf[i]) {
						for (int j = 2 * i; j < lpf.length; j += i) {
							lpf[j] = Math.min(lpf[j], i);
						}
					}
				}
			}
			omega = new int[2*Integer.highestOneBit(n)];
			for (int i = 2; i < omega.length; i++) {
				omega[i] = omega[i/lpf[i]] + 1;
			}
		}
		return omega[n];
	}
	
	/**
	 * N-1以下の数について約数の個数を列挙する
	 * @param N
	 * @return
	 */
	public static int[] enumdivisorNums(int N) {
		int[]ret=new int[N];
		for (int i = 1; i < N; i++) {
			for (int j = i; j < N; j+=i) {
				ret[j]++;
			}
		}
		return ret;
	}
	
	/**
	 * N-1以下の数について約数を列挙する
	 * @param N
	 * @return
	 */
	public static IntArrayList[] enumdivisors(int N) {
		IntArrayList[]divisors=new IntArrayList[N];
		for (int i = 0; i < divisors.length; i++) {
			divisors[i]=new IntArrayList();
		}
		for (int i = 1; i < N; i++) {
			for (int j = i; j < N; j+=i) {
				divisors[j].add(i);
			}
		}
		return divisors;
	}
	
	public static int[] mus(int N) {
		expandPrimes(N);
		int[]mu=new int[N];
		Arrays.fill(mu, 1);
		mu[0]=0;
		for (int i = 0; i < N; i++) {
			if(isPrime[i]) {
				for (int j=i;j<N;j+=i) {
					mu[j]=-mu[j];
					if(j%(1L*i*i)==0)mu[j]=0;
				}
			}
		}
		return mu;
	}
	
}
