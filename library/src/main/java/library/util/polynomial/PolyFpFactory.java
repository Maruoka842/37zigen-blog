package library.util.polynomial;

import java.util.Arrays;

import library.util.Fp;

public class PolyFpFactory {
	public static final long mod = 998244353L;
	static final Fp fp = Fp.MOD998244353;
	
	public static long[] catalan(int n) {
		long[] a=new long[n];
		a[0]=1;
		for (int i = 0; i < n - 1; i++) {
			a[i+1]=2*(2*i+1)*fp.inv(i+2)%mod*a[i]%mod;
		}
		return a;
	}
	
	/**
	 * C(x)^e
	 * @param n
	 * @param e
	 * @return
	 */
	public static long[] catalanPow(int n, int e) {
		if (e < 0) throw new AssertionError();
		long[]a = new long[n];
		a[0] = 1;
		if (e == 0) {
			return a;
		}
		for (int i = 1; i < n; i++) {
			a[i] = fp.comb(2 * i + e - 1, i + e - 1) * e % mod * fp.inv(i + e) % mod;
		}
		return a;
	}
	
	
	/**
	 * 1/(1-x{C(x)-C(-x)}/2)
	 * @param n
	 * @param e
	 * @return
	 */
	public static long[] catalanEvenUp(int n) {
		// C(x)=(1-√(1-4x)) / 2x
		// E = C_even
		// O = C_odd
		// と置く。
		
		//   1/(1-x{C(x)-C(-x)}/2)
		// = 4 / (2 + √(1 - 4x) + √(1 + 4x))
		// = 2 (2 - √(1 - 4x) - √(1 + 4x))/(1 - √(1 - 16x^2)) 
		// = (1 + √(1 - 16x^2))(2 - √(1 - 4x) + √(1 + 4x))/ (8x^2)
		// 分子は
		// 2(1 + √(1 - 16x^2)) -√(1 - 4x) - √(1 + 4x) - (1 - 4x)√(1 + 4x) - (1 + 4x)√(1 - 4x)
		//= 2(1 + √(1 - 16x^2)) + 4x{√(1 + 4x) - √(1 - 4x)} - 2{√(1 + 4x) - √(1 - 4x)}
		//= 2(-1 + √(1 - 16x^2)) + 4x{- √(1 - 4x) + √(1 + 4x)} + 2{2 - √(1 - 4x) - √(1 + 4x)}
		
		// 分母も合わせると
		// -2C(4x^2) + 2 E(x) + O(x)/x
		
		long[] f = catalan(2 * n);
		long[] a = new long[n];
		long pow2 = 2;
		for (int i = 0; i < n; i += 2) {
			a[i] = (f[i + 1] + 2 * f[i] - pow2 * f[i / 2]) % mod;
			if (a[i] < 0) a[i] += mod;
			pow2 = 4 * pow2 % mod;
		}
		return a;
		
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
