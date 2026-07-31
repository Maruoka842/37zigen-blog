package library.util.linalg;

import library.util.ArrayUtils;

public class MatrixUtilsZn {

	public static long[][] add(long[][] a, long[][] b, long mod) {
		if(a.length!=b.length)throw new AssertionError();
		if(a[0].length!=b[0].length)throw new AssertionError();
		long[][]c=new long[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				c[i][j]+=a[i][j]+b[i][j];
				if(c[i][j]>=mod)c[i][j]-=mod;
			}
		}
		return c;
	}
	
	
	public static long[] mul(long[][] a, long[] vec, long mod) {
		int n=vec.length;
		long[]ret=new long[n];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				ret[i]+=a[i][j]*vec[j];
				ret[i]%=mod;
			}
		}
		return ret;
	}
	
	public static long[] mul(long[] vec, long[][] a, long mod) {
		int n=vec.length;
		long[]ret=new long[n];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				ret[j]+=vec[i]*a[i][j];
				ret[j]%=mod;
			}
		}
		return ret;
	}

	
	
	public static long[][] mul(long[][] a, long[][] b, long mod) {
		int n = a.length;
		if (n == 0) return new long[0][0];
		if (b.length == 0) return new long[n][0];
		int m = b[0].length;
		long[][] c = new long[n][m];
		for (int i = 0; i < n; i++) {
			long[] rowA = a[i];
			long[] rowC = c[i];
			int k_lim = Math.min(rowA.length, b.length);
			for (int k = 0; k < k_lim; k++) {
				long v = rowA[k];
				if (v == 0) continue;
				long[] rowB = b[k];
				int j_lim = Math.min(m, rowB.length);
				for (int j = 0; j < j_lim; j++) {
					rowC[j] = (rowC[j] + v * rowB[j]) % mod;
				}
			}
		}
		return c;
	}

	public static long[][] pow(long[][] a, long n, long mod) {
		if(n==0) {
			long[][] ret=new long[a.length][a.length];
			for(int i=0;i<a.length;++i)ret[i][i]=1;
			return ret;
		}
		long[][] ret=pow(mul(a, a, mod), n/2, mod);
		if(n%2==1)ret=mul(ret,a, mod);
		return ret;
	}

	/**
	 * 任意の法 {@code mod} 上で正方行列 {@code a} の行列式を返す。
	 * 逆元を使わず、ユークリッドの互除法と行基本変形で下三角成分を消す。
	 * 計算量は {@code O(N^3 log mod)}。
	 * @param a
	 * @param mod
	 * @return
	 */
	public static long determinantByEuclid(long[][] a, long mod) {
		//https://judge.yosupo.jp/submission/370900
		if (a.length == 0) return 1 % mod;
		if (a.length != a[0].length) return 0;
		long[][] b = ArrayUtils.copy(a);
		int n = b.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				b[i][j] %= mod;
				if (b[i][j] < 0) b[i][j] += mod;
			}
		}
		long ret = 1 % mod;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				while (b[j][i] != 0) {
					long q = b[i][i] / b[j][i];
					for (int k = i; k < n; k++) {
						b[i][k] = (b[i][k] - q * b[j][k]) % mod;
						if (b[i][k] < 0) b[i][k] += mod;
					}
					ArrayUtils.swap(b[i], b[j]);
					if (ret != 0) ret = mod - ret;
				}
			}
			if (b[i][i] == 0) return 0;
			ret = ret * b[i][i] % mod;
		}
		return ret;
	}
}
