package library.util.polynomial;

import java.util.Arrays;

public class OnlineConvolution {
//https://yukicoder.me/submissions/1148948
	long[] f, g, h;
	int n = 0;
	long mod;
	PolynomialFpDynamic fp;

	public OnlineConvolution(int n, PolynomialFpDynamic fp) {
		f = new long[n];
		g = new long[n];
		h = new long[n];
		this.fp = fp;
		this.mod = fp.mod;
	}
	
	public long append(long a, long b) {
		/* appendされた列を
		 * [0][1  2][3  4  5  6][7  8  9  a  b  c  d  e]
		 * というように分解して、短い方の区間の長さに合わせて、区間同士の積を逐次計算する。
		 * 例えば [1  2], [3  4  5  6] の積は [3  4]  [5  6] に分割して計算する。
		 * [2^i-1 .. 2^(i+1)-2]という区間は
		 * 位置2(2^i-1)の値がappendされた直後には2乗を計算しないといけないが、
		 * 区間の末尾が2(2^i-1)なので良い。
		 * 長さ2^i同士の区間の積は N/2^i回起きるので全体で O(N log(N)^2)
		 */
		f[n] = a;
		g[n] = b;
		++n;
		
		int v = n - (Integer.highestOneBit(n) - 1);
		for (int len = Integer.lowestOneBit(v); len >= 1; len /= 2) {
			//n-1で終わる区間の長さlenを列挙
			for (int swap = 0; swap < 2; ++swap) {
				if (n - len != len - 1 || swap == 0) {
					long[] x = Arrays.copyOfRange(f, n - len, n);
					long[] y = Arrays.copyOfRange(g, len - 1, 2 * len - 1);
					long[] z = fp.mul(x, y);
					for (int i = 0; i < z.length && i + n - 1 < h.length; i++) {
						h[i + n - 1] += z[i];
						h[i + n - 1] %= mod;
					}
				}				
				{
					var tmp = f;
					f = g;
					g = tmp;
				}
			}
			
		}
		return h[n - 1];
		
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
