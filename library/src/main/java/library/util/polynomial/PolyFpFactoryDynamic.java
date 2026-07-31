package library.util.polynomial;

import java.util.Arrays;

import library.util.Fp;

public class PolyFpFactoryDynamic {
	public long mod;
	static Fp mo;
	
	public PolyFpFactoryDynamic(long mod) {
		this.mod = mod;
		mo = new Fp(mod);
	}
	
	/**
	 * (1+x)^n
	 * @param n
	 * @return
	 */
	public long[] binomials(int n) {
		long[] f=new long[n+1];
		for (int i = 0; i <= n; i++) {
			f[i]=mo.comb(n, i);
		}
		return f;
	}
    
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
