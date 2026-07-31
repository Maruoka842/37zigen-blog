package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.ArrayUtils;

public class PolynomialDouble {

	/**
	 * 未テスト
	 * @return
	 */
	public static double[] zero() {
		return new double[0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static double[] one() {
		return new double[] { 1.0 };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static double[] x() {
		return new double[] { 0.0, 1.0 };
	}
	
	static double[][] bitreversedRootsRe = new double[30][];
	static double[][] bitreversedRootsIm = new double[30][];
	static double[][] bitreversedInvRootsRe = new double[30][];
	static double[][] bitreversedInvRootsIm = new double[30][];

	static synchronized void prepareRoots(int n) {
		int sz = Integer.numberOfTrailingZeros(n);
		if (bitreversedRootsRe[sz] != null) return;
		bitreversedRootsRe[sz] = new double[n];
		bitreversedRootsIm[sz] = new double[n];
		bitreversedInvRootsRe[sz] = new double[n];
		bitreversedInvRootsIm[sz] = new double[n];

		for (int len = 1; len < n; len <<= 1) {
			double angle = Math.PI / len;
			for (int i = 0; i < len; i++) {
				int revI = 0;
				if (len > 1) {
					int tempI = i;
					for (int j = 0; j < Integer.numberOfTrailingZeros(len); j++) {
						revI = (revI << 1) | (tempI & 1);
						tempI >>= 1;
					}
				}
				double a = angle * i;
				bitreversedRootsRe[sz][len + revI] = Math.cos(a);
				bitreversedRootsIm[sz][len + revI] = Math.sin(a);
				bitreversedInvRootsRe[sz][len + revI] = Math.cos(-a);
				bitreversedInvRootsIm[sz][len + revI] = Math.sin(-a);
			}
		}
	}

	public static void fftTobitReversed(double[] re, double[] im) {
		int n = re.length;
		int sz = Integer.numberOfTrailingZeros(n);
		if (bitreversedRootsRe[sz] == null) prepareRoots(n);
		for (int m = 1, t = n / 2; m <= n / 2; m *= 2, t /= 2) {
			for (int i = 0, k = 0; i < m; ++i, k += 2 * t) {
				double sre = bitreversedRootsRe[sz][m + i];
				double sim = bitreversedRootsIm[sz][m + i];
				for (int j = k; j < k + t; ++j) {
					double ure = re[j];
					double uim = im[j];
					double vre = re[j + t] * sre - im[j + t] * sim;
					double vim = re[j + t] * sim + im[j + t] * sre;
					re[j] = ure + vre;
					im[j] = uim + vim;
					re[j + t] = ure - vre;
					im[j + t] = uim - vim;
				}
			}
		}
	}

	public static void ifftFromBitreversed(double[] re, double[] im) {
		int n = re.length;
		int sz = Integer.numberOfTrailingZeros(n);
		if (bitreversedInvRootsRe[sz] == null) prepareRoots(n);
		for (int m = n / 2, t = 1; m >= 1; m /= 2, t *= 2) {
			for (int i = 0, k = 0; i < m; ++i, k += 2 * t) {
				double sre = bitreversedInvRootsRe[sz][m + i];
				double sim = bitreversedInvRootsIm[sz][m + i];
				for (int j = k; j < k + t; ++j) {
					double ure = re[j];
					double uim = im[j];
					double vre = re[j + t];
					double vim = im[j + t];
					re[j] = ure + vre;
					im[j] = uim + vim;
					double tre = ure - vre;
					double tim = uim - vim;
					re[j + t] = tre * sre - tim * sim;
					im[j + t] = tre * sim + tim * sre;
				}
			}
		}
		double invN = 1.0 / n;
		for (int i = 0; i < n; i++) {
			re[i] *= invN;
			im[i] *= invN;
		}
	}

	public static double[] inv(double[] f) {
		if (f[0] == 0) throw new AssertionError();
		// fg=1
		double[] g = new double[f.length];
		g[0]=1/f[0];
		for (int i = 1; i < f.length; i++) {
			double sum=0;
			for (int j = 1; j <= i; j++) {
				sum+=f[j]*g[i-j];
			}
			g[i]=-sum/f[0];
		}
		return g;
	}
	
	public static double[] add(double[] f, double[] g) {
		double[] h=new double[Math.max(f.length, g.length)];
		for (int i = 0; i < f.length; i++) {
			h[i]=f[i];
		}
		for (int i = 0; i < g.length; i++) {
			h[i]+=g[i];
		}
		return h;
	}
	
	public static double[] mulNaive(double[] f, double[] g) {
		double[] h = new double[f.length + g.length - 1];
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < g.length; j++) {
				h[i+j]+=f[i]*g[j];
			}
		}
		return h;
	}

	public static double[] mulFFT(double[] f, double[] g) {
		int n = 1;
		int len = f.length + g.length - 1;
		while (n < len) n *= 2;
		double[] reF = Arrays.copyOf(f, n);
		double[] imF = new double[n];
		double[] reG = Arrays.copyOf(g, n);
		double[] imG = new double[n];
		fftTobitReversed(reF, imF);
		fftTobitReversed(reG, imG);
		for (int i = 0; i < n; i++) {
			double re = reF[i] * reG[i] - imF[i] * imG[i];
			double im = reF[i] * imG[i] + imF[i] * reG[i];
			reF[i] = re;
			imF[i] = im;
		}
		ifftFromBitreversed(reF, imF);
		return Arrays.copyOf(reF, len);
	}

	public static double[] mul(double[] f, double[] g) {
		if (f.length == 0 || g.length == 0) return new double[0];
		if (f.length + g.length - 1 <= 512) {
			return mulNaive(f, g);
		} else {
			return mulFFT(f, g);
		}
	}
	
	/**
	 * 1/(1-f) を返す
	 */
	public static double[] geometricSeries(double[] f) {
		double[] g=new double[f.length];
		for (int i = 0; i < g.length; i++) {
			g[i]=-f[i];
		}
		g[0]+=1;
		g=inv(g);
		g=Arrays.copyOf(g, f.length);
		return g;
	}
	
	/**
	 * さいころを振った時kが出る確率がf[k]である。出た目の和がn以上になるまで振る時の、振る回数の期待値g[n]の母関数
	 * g=x/(1-x) 1/(1-f) を返す。g.length = f.length。
	 * @param f
	 * @return
	 * 
	 * verified:https://atcoder.jp/contests/abc382/tasks/abc382_e
	 */
	public static double[] expectedStepsGF(double[] f) {
        double[] g=PolynomialDouble.geometricSeries(f);
        g=ArrayUtils.prefixSum(g);
        double[]ret=new double[f.length+1];
        for (int i = 0; i < f.length; i++) {
        	ret[i + 1]=g[i];
		}
        return ret;
	}
	
	
    public static double[] sparseInv(double[] a) {
    	if(a[0]==0)throw new AssertionError();
    	ArrayList<Integer> degs=new ArrayList<>();
    	ArrayList<Double> coefs=new ArrayList<>();
    	for (int i = 1; i < a.length; i++) {
			if(a[i]!=0) {
				degs.add(i);
				coefs.add(a[i]);
			}
		}
    	double[]b=new double[a.length];
    	double constInv=-1/a[0];
    	b[0]=1/a[0];
    	for (int i = 1; i < a.length; i++) {
    		for (int j=0;j<degs.size();++j) {
    			int deg=degs.get(j);
    			if(i-deg<0)break;
    			b[i]+=coefs.get(j)*b[i-deg];
    		}
    		b[i]=constInv*b[i];
    	}
    	return b;
    }
	
	
	/**
	 * 未テスト
	 * $f(x)$ を返す。
	 * @param f 多項式 $f(x) = \sum f_i x^i$ の係数
	 * @param x 評価点
	 * @return $f(x)$
	 * 計算量: $O(N)$
	 */
	public static double evaluate(double[] f, double x) {
		double res = 0;
		for (int i = f.length - 1; i >= 0; i--) {
			res = res * x + f[i];
		}
		return res;
	}

	/**
	 * 未テスト
	 * $f'(x)$ を返す。
	 * @param f 多項式の係数
	 * @return $f'(x)$
	 * 計算量: $O(N)$
	 */
	public static double[] differentiate(double[] f) {
		if (f.length <= 1) return new double[0];
		double[] g = new double[f.length - 1];
		for (int i = 1; i < f.length; i++) {
			g[i - 1] = f[i] * i;
		}
		return g;
	}

	/**
	 * 未テスト
	 * $f(x) = 0$ の実根を昇順に列挙する。
	 * $f'(x) = 0$ の根を利用して、各単調増加・減少区間を二分探索する。
	 * @param f 多項式の係数
	 * @return 実根の配列（昇順、重複なし）
	 * 計算量: $O(N^3 \times \text{iterations})$
	 */
	public static double[] realRoots(double[] f) {
		int n = f.length;
		while (n > 0 && Math.abs(f[n - 1]) < 1e-13) n--;
		if (n <= 1) return new double[0];
		double[] g = new double[n];
		System.arraycopy(f, 0, g, 0, n);
		return realRootsRecursive(g);
	}

	private static double[] realRootsRecursive(double[] f) {
		int n = f.length;
		if (n == 2) {
			return new double[] { -f[0] / f[1] };
		}
		double[] df = differentiate(f);
		double[] dRoots = realRootsRecursive(df);

		double R = 1.0;
		for (int i = 0; i < n - 1; i++) {
			R = Math.max(R, 1.0 + Math.abs(f[i] / f[n - 1]));
		}

		double[] bounds = new double[dRoots.length + 2];
		bounds[0] = -R;
		System.arraycopy(dRoots, 0, bounds, 1, dRoots.length);
		bounds[bounds.length - 1] = R;

		double[] roots = new double[n - 1];
		int count = 0;
		for (int i = 0; i < bounds.length - 1; i++) {
			double a = bounds[i];
			double b = bounds[i + 1];
			double fa = evaluate(f, a);
			double fb = evaluate(f, b);
			double root = Double.NaN;
			if (Math.abs(fa) < 1e-12) {
				root = a;
			} else if (Math.abs(fb) < 1e-12) {
				root = b;
			} else if ((fa < 0 && fb > 0) || (fa > 0 && fb < 0)) {
				for (int iter = 0; iter < 100; iter++) {
					double m = (a + b) / 2;
					double fm = evaluate(f, m);
					if ((fa <= 0 && fm >= 0) || (fa >= 0 && fm <= 0)) {
						b = m;
						fb = fm;
					} else {
						a = m;
						fa = fm;
					}
				}
				root = (a + b) / 2;
			}
			if (!Double.isNaN(root)) {
				if (count == 0 || root - roots[count - 1] > 1e-8) {
					roots[count++] = root;
				}
			}
		}

		return Arrays.copyOf(roots, count);
	}

	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
