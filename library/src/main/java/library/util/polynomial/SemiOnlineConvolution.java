package library.util.polynomial;

import java.util.Arrays;

/**
 * <p>
 * 固定された数列 {@code f} と、末尾に1要素ずつ追加されていく数列 {@code g} について、
 * その畳み込み
 * <pre>
 * h[n] = Σ_{k=0..n} f[k] * g[n-k] (mod mod)
 * </pre>
 * をオンラインで計算します。
 * </p>
 */
public class SemiOnlineConvolution {
//https://yukicoder.me/submissions/1148948
	long[] f, g, h;
	int n = 0;
	long mod;
	PolynomialFpDynamic fp;

	public SemiOnlineConvolution(long[] f, PolynomialFpDynamic fp) {
		this(f, f.length, fp);
	}

	public SemiOnlineConvolution(long[] f, int maxOnlineLen, PolynomialFpDynamic fp) {
		this.f = f.clone();
		this.fp = fp;
		this.mod = fp.mod;
		g = new long[maxOnlineLen];
		h = new long[f.length + maxOnlineLen];
	}
	
	public long append(long a) {
		/* appendされた列をセグ木状に分解する。
		 * [0  1  2  3  4  5  6  7]
		 * [0  1  2  3][4  5  6  7]
		 * [0  1][2  3][4  5][6  7]
		 * [0][1][2][3][4][5][6][7]
		 * 固定された列は
		 * [0][1  2][3  4  5  6][7  8  9  a  b  c  d  e]
		 * と分解。
		 * 同じ長さの区間同士で積を取る。
		 */
		g[n] = a;
		for (int i = 0; i < 30; i++) {
			if ((1 << i) - 1 >= f.length) break;
			long[] x = Arrays.copyOfRange(f, (1<<i)-1, Math.min(f.length, (1<<(i+1))-1));
			long[] y = Arrays.copyOfRange(g, n+1-(1<<i), n+1);
			long[] z = fp.mul(x, y);
			for (int j = 0; j < z.length && j + n < h.length; j++) {
				h[j + n] += z[j];
				if (h[j + n] >= mod) h[j + n] %= mod;
			}
			if ((n >> i) % 2 == 0) break;
		}
		
		n++;
		return h[n - 1];
		
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
