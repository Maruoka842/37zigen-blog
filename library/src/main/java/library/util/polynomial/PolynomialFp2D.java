package library.util.polynomial;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.ArrayUtils;
import library.util.Fp;
import library.util.MathUtils;

public class PolynomialFp2D {
	final static long mod = 998244353L;
	static Fp mo = Fp.MOD998244353;

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] zero() {
		return new long[0][0];
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] one() {
		return new long[][] { { 1 } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] x() {
		return new long[][] { { 0 }, { 1 } };
	}

	/**
	 * 未テスト
	 * @return
	 */
	public static long[][] y() {
		return new long[][] { { 0, 1 } };
	}
	
	
	
	public static long[][] sqrt(long[][] f) {
		if(f[0][0]!=1)throw new AssertionError();
		int maxlenX = f.length;
		int maxlenY = 0;
		for (int i = 0; i < f.length; i++) {
			maxlenY = Math.max(maxlenY, f[i].length);
		}
		long[][]g=new long[1][1];
		g[0][0]=1;
		long inv2=MathUtils.modInv(2, mod);
		for (int len = 1; len < maxlenX + maxlenY - 1; len *= 2) {
			int nlenX = Math.min(maxlenX, 2 * len);
			int nlenY = Math.min(maxlenY, 2 * len);
			long[][] truncatedF = ArrayUtils.copyOf(f, nlenX, nlenY);
			var fInvG = mul(truncatedF, inv(ArrayUtils.copyOf(g, nlenX, nlenY)));
			g = ArrayUtils.copyOf(g, nlenX, nlenY);
			//  g ← (g+f/g)/2
			for (int i = 0; i < nlenX; i++) {
				for (int j = 0; j < nlenY; j++) {
					if (i + j < len) {
						continue;
					} else if (i + j >= 2 * len) {
						g[i][j] = 0;
					} else {
						g[i][j] = inv2 * fInvG[i][j] % mod;
					}
				}
			}
		}
		return g;
	}
	
	/**
	 * 未テスト
	 * @param f
	 * @return
	 */
	public static long[][] sparseInv(long[][] f) {
		if(f[0][0]==0)throw new AssertionError();
	    // 非ゼロ項（0,0 を除く）
	    ArrayList<Integer> dx = new ArrayList<>();
	    ArrayList<Integer> dy = new ArrayList<>();
	    ArrayList<Long> coef = new ArrayList<>();
	    for (int i = 0; i < f.length; i++) {
	        for (int j = 0; j < f[i].length; j++) {
	            if (i == 0 && j == 0) continue;
	            if (f[i][j] != 0) {
	                dx.add(i);
	                dy.add(j);
	                coef.add(f[i][j]);
	            }
	        }
	    }

	    long[][] g = new long[f.length][f[0].length];

	    long inv00 = MathUtils.modInv(f[0][0], mod);
	    g[0][0] = inv00;

	    for (int i = 0; i < f.length; i++) {
	        for (int j = 0; j < f[i].length; j++) {
	            if (i == 0 && j == 0) continue;

	            long sum = 0;

	            for (int k = 0; k < dx.size(); k++) {
	                int x = dx.get(k);
	                int y = dy.get(k);
	                if (i >= x && j >= y) {
	                    sum += coef.get(k) * g[i - x][j - y];
	                    sum %= mod;
	                }
	            }

	            g[i][j] = (mod - sum) * inv00 % mod;
	        }
	    }
	    return g;
	}

	
	
	public static long[][] inv(long[][] f) {
		if(f[0][0]==0)throw new AssertionError();
		int maxlenX = f.length;
		int maxlenY = 0;
		for (int i = 0; i < f.length; i++) {
			maxlenY = Math.max(maxlenY, f[i].length);
		}
		long[][]g=new long[1][1];
		g[0][0]=MathUtils.modInv(f[0][0], mod);
		for (int len = 1; len < maxlenX + maxlenY - 1; len *= 2) {
			int nlenX = Math.min(maxlenX, 2 * len);
			int nlenY = Math.min(maxlenY, 2 * len);
			long[][] truncatedF = ArrayUtils.copyOf(f, nlenX, nlenY);
			var gg = mul(g, g);
			gg = ArrayUtils.copyOf(gg, nlenX, nlenY);
			var ggf = mul(gg, truncatedF);
			g = ArrayUtils.copyOf(g, nlenX, nlenY);
			//  g ← 2g-g²a
			for (int i = 0; i < nlenX; i++) {
				for (int j = 0; j < nlenY; j++) {
					if (i + j < len) {
						continue;
					} else if (i + j >= 2 * len) {
						g[i][j] = 0;
					} else {
						if (ggf[i][j] != 0)
							g[i][j] = mod - ggf[i][j];
						else
							g[i][j] = 0;
					}
				}
			}
		}
		return g;
	}
	
	public static long[][]mul(long[][]a, long[][] b, long[][] c) {
		return mul(a, mul(b, c));
	}
	
	public static long[][] mul(long[][] a, long[][] b) {
		if (Math.min(a.length * a[0].length, b.length * b[0].length) < 16) return mulNaive(a, b);
		int m0=1;
		int m1=1;
		for (int i = 0; i < a.length; i++) {
			m0=Math.max(m0, a[i].length);
		}
		for (int i = 0; i < b.length; i++) {
			m1=Math.max(m1, b[i].length);
		}
		long[]f=new long[(m0+m1-1)*a.length];
		long[]g=new long[(m0+m1-1)*b.length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				f[i*(m0+m1-1)+j]=a[i][j];
			}
		}
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				g[i*(m0+m1-1)+j]=b[i][j];
			}
		}
		long[]h=PolynomialFp.mul(f, g);
		long[][]c=new long[a.length+b.length-1][m0+m1-1];
		for (int i = 0; i < h.length; i++) {
			if(h[i]!=0) {
				c[i/(m0+m1-1)][i%(m0+m1-1)]=h[i];
			}
		}
		return c;
	}
	

	public static long[][] mulNaive(long[][] a, long[][] b) {
		int m0=1;
		int m1=1;
		for (int i = 0; i < a.length; i++) {
			m0=Math.max(m0, a[i].length);
		}
		for (int i = 0; i < b.length; i++) {
			m1=Math.max(m1, b[i].length);
		}
		long[][]c=new long[a.length+b.length-1][m0+m1-1];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				for (int k = 0; k < b.length; k++) {
					for (int l = 0; l < b[k].length; l++) {
						c[i+k][j+l]+=a[i][j]*b[k][l];
						c[i+k][j+l]%=mod;
					}
				}
			}
		}
		return c;
	}
	
	/**
	 * [x^n] f(x,y)/g(x,y)
	 * @param a
	 * @param n
	 * @return
	 * https://noshi91.hatenablog.com/entry/2024/03/16/224034
	 */
	public static long[] fixingXofRational(long[][]f, long[][] g, int n) {
		//https://atcoder.jp/contests/abc345/submissions/72158358
		if (g[0][0] != 1) throw new AssertionError();
		while (n != 0) {
			long[][] negatedG = ArrayUtils.copy(g);
			for (int i = 0; i < negatedG.length; i++) {
				for (int j = 0; j < negatedG[i].length; j++) {
					if(i%2==1)negatedG[i][j]=negatedG[i][j]*(mod-1)%mod;
				}
			}
			f= mul(f, negatedG);
			g= mul(g, negatedG);
			long[][] nf = new long[Math.min(n/2 + 1, (f.length+1)/2)][(f[0].length+1)/2];
			long[][] ng = new long[Math.min(n/2 + 1, (g.length+1)/2)][(g[0].length+1)/2];
			// [x^n] f(x^2)/g(x^2)
			// x は 2i ≤ n ⇔ i ≤ [n/2] 次までしかいらない。
			for (int i = (int) (n%2); i < f.length && i / 2 < nf.length; i += 2) {
				nf[i / 2] = f[i];
			}
			for (int i = 0; i < g.length && i / 2 < ng.length; i += 2) {
				ng[i / 2] = g[i];
			}
			f = nf;
			g = ng;
			n /= 2;
		}
		return f[0];
	}
	
	public static class PartialFractionInYOfGeometricProduct {
		/** 極 1-yx^a の a。 */
		public int exponent;
		/** 同じ a の出現回数。極 (1-yx^a) の重複度。 */
		public int multiplicity;
		/** 各 C_{a,r}(x) を表すときに共通に使う x だけの分母。定数項は 1。 */
		public long[] denominator;
		/**
		 * numeratorByOrder[r] / denominator が C_{a,r}(x)。
		 *
		 * F(x,y) の部分分数分解における、この a の寄与は
		 *   sum_{r=1}^{multiplicity} C_{a,r}(x) / (1-yx^a)^r
		 * である。numeratorByOrder[0] は対応する r がないので使わない。
		 */
		public long[][] numeratorByOrder;
		public PartialFractionInYOfGeometricProduct(int exponent, int multiplicity, long[] denominator, long[][] numeratorByOrder) {
			this.exponent = exponent;
			this.multiplicity = multiplicity;
			this.denominator = denominator;
			this.numeratorByOrder = numeratorByOrder;
		}
	}
	
	/**
	 * a_i = exponents[i] として、
	 * prod_i 1/(1-yx^{a_i}) を y について部分分数分解する。未テスト
	 *
	 * <p>
	 * 戻り値の各要素 e は、次の形の同じ極をまとめた項を表す。
	 * </p>
	 *
	 * <pre>
	 * sum_{r=1}^{e.multiplicity}
	 *   e.numeratorByOrder[r](x) / (e.denominator(x) * (1-yx^{e.exponent})^r)
	 * </pre>
	 *
	 * <p>
	 * {@code numeratorByOrder[0]} は使わない。入力の {@code exponents} は正整数を仮定する。
	 * </p>
	 * 
	 * aの長さをnとしてO(n^4 max(a) log(n max(a))) ぐらい？
	 *
	 * @param exponents
	 * @return y に関する部分分数分解
	 */
	public static PartialFractionInYOfGeometricProduct[] partialFractionInYOfGeometricProduct(int[] exponents) {
		//https://atcoder.jp/contests/ndpc/submissions/75539735
		// まず同じ a_i をまとめる。
		// m_a=count[a] とすると、対象は
		//   F(x,y)=prod_a (1-yx^a)^(-m_a)
		// になる。
		int max = ArrayUtils.max(exponents);
		int[] count = new int[max + 1];
		for (int a : exponents) {
			if (a <= 0) throw new AssertionError();
			count[a]++;
		}
		ArrayList<Integer> distinctExponents = new ArrayList<>();
		for (int a = 1; a <= max; a++) if (count[a] > 0) distinctExponents.add(a);
		PartialFractionInYOfGeometricProduct[] ret = new PartialFractionInYOfGeometricProduct[distinctExponents.size()];
		for (int i = 0; i < distinctExponents.size(); i++) {
			int a = distinctExponents.get(i);
			int ma = count[a];
			// 固定した a について、t=1-yx^a とおく。
			// y=(1-t)x^{-a} を代入すると
			//   F = t^{-m_a} H_a(t,x)
			//   H_a(t,x)=prod_{b!=a} (1-(1-t)x^{b-a})^{-m_b}
			// H_a を t のべき t^j で展開したときに必要なのは j=0..m_a-1 だけなので、mod t^m_a で計算
			// denominator は H_a(t,x) の各 t^j 係数で共通に使う x だけの分母。
			// あとで numeratorByOrder[r] / denominator として C_{a,r}(x) を返す。
			long[] denominator = new long[] {1};
			// r[j] は、後で共通分母 denominator を掛けた状態の [t^j] H_a(t,x) の分子部分。
			// ここではまだ b<a 由来の x^{leftShift} と符号 (-1)^{leftMultiplicity} は掛けていない。
			long[][] r = new long[ma][];
			r[0] = new long[] {1};
			for (int j = 1; j <= ma - 1; j++) r[j] = new long[0];
			// b<a の項では x^{b-a}=x^{-e} が出る。
			//   1-(1-t)x^{-e} = -x^{-e}((1-t)-x^e)
			// と変形して負の x 次数を消すため、分母・分子に
			//   (-1)^{m_b} x^{e m_b}
			// を掛ける。この m_b, e m_b の累積和が leftMultiplicity と leftShift。
			int leftMultiplicity = 0;
			int leftShift = 0;
			for (int b : distinctExponents) {
				if (b == a) continue;
				int mb = count[b];
				int e = Math.abs(b - a);
				// b > a の場合
				// (1-(1-t)x^e)^{-m_b}	
				//=(1-x^e)^(-m_b)　(1+tx^e/(1-x^e))^(-m_b)
				
				// b < a の場合
				// (1-(1-t)x^(-e))^{-m_b}	
				//=x^(mb e)(-1)^(m_b) (1-x^e-t)^{-m_b}
				//=x^(mb e)(-1)^(m_b) (1-x^e)^(-m_b) (1-t/(1-x^e))^{-m_b}
				
				// 各 b の局所展開で、分母を Π_b (1-x^e)^{m_b+ma-1} に揃える。
				denominator = PolynomialFp.mul(denominator, oneMinusXPow(e, mb + ma - 1));
				if (b < a) {
					leftMultiplicity += mb;
					leftShift += e * mb;
				}
				// factor[q] は、この b の分子の寄与を t^q の係数ごとに持つ。
				// 共通分母 (1-x^e)^{m_b+ma-1} を先に払った分子として
				//   combrep(m_b, q) (1-x^e)^{ma-1-q}
				// が出る。
				long[][] factor = new long[ma][];
				for (int q = 0; q <= ma - 1; q++) {
					long[] poly = oneMinusXPow(e, ma - 1 - q);
					long coef = mo.combrep(mb, q);
					if (b > a) {
						// b>a のときは
						//   (1-(1-t)x^e)^(-m_b)
						// を t で展開するので、t^q 係数に (-x^e)^q が掛かる。
						poly = PolynomialFp.multiplyByX(poly, e * q);
						if ((q & 1) == 1 && coef != 0) coef = mod - coef;
					}
					factor[q] = PolynomialFp.mul(poly, coef);
				}
				// ここまで処理した b たちの積を、mod t^{ma} で計算
				r = multiplyTruncatedByX(r, factor, ma - 1);
			}
			long[][] numeratorByOrder = new long[ma + 1][];
			numeratorByOrder[0] = new long[0];
			long sign = (leftMultiplicity & 1) == 0 ? 1 : mod - 1;
			for (int j = 0; j <= ma - 1; j++) {
				numeratorByOrder[ma - j] = PolynomialFp.mul(PolynomialFp.multiplyByX(r[j], leftShift), sign);
			}
			ret[i] = new PartialFractionInYOfGeometricProduct(a, ma, denominator, numeratorByOrder);
		}
		return ret;
	}
	
	/**
	 * t 多項式を maxT 次までで掛ける。未テスト
	 */
	static long[][] multiplyTruncatedByX(long[][] f, long[][] g, int maxT) {
		// f[i], g[j] はそれぞれ t^i, t^j の係数で、各係数は x の多項式。
		// 普通の畳み込みをするが、必要なのは t^0..t^maxT だけなので i+j>maxT は計算しない。
		long[][] ret = new long[maxT + 1][];
		for (int i = 0; i <= maxT; i++) ret[i] = new long[0];
		for (int i = 0; i <= maxT; i++) {
			if (f[i].length == 0) continue;
			for (int j = 0; i + j <= maxT; j++) {
				if (g[j].length == 0) continue;
				ret[i + j] = PolynomialFp.add(ret[i + j], PolynomialFp.mul(f[i], g[j]));
			}
		}
		return ret;
	}
	
	/**
	 * (1-x^e)^power を作る。未テスト
	 */
	static long[] oneMinusXPow(int e, int power) {
		// 二項定理:
		//   (1-x^e)^power = sum_i binom(power,i)(-1)^i x^{ei}
		long[] ret = new long[e * power + 1];
		for (int i = 0; i <= power; i++) {
			long coef = mo.comb(power, i);
			if ((i & 1) == 1) coef = mod - coef;
			ret[e * i] = coef;
		}
		return ret;
	}
	
	static void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
