package library.util;

import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * Floor sum, floor product, and linear modulo counting utilities.
 */
public class FloorSum {

	/**
	 * sum[x=0..n-1]floor((ax+b)/m)
	 * =#{(x, y): ax+b ≥ my, 0　≤　x　≤　n-1}-#{(x, y): 0　≤　x　≤　n-1, y ≤ 0}
	 * a,b負でも可。n<2^32の場合はmod 2^64で正しい結果を返すはず...。
	 * @param a
	 * @param b
	 * @param m
	 * @param n
	 * @return
	 * https://judge.yosupo.jp/submission/338540
	 * https://atcoder.jp/contests/abc372/submissions/71815803
	 * https://atcoder.jp/contests/abc443/submissions/72922348
	 */
	public static long floorSum(long a, long b, long m, long n) {
		if (n <= 0) return 0;
		if(m==0)throw new AssertionError();
		if(b<0) {
			long q=Math.ceilDiv(-b, m);
			b+=q*m;
			return floorSum(a, b, m, n)-q*n;
		}
		if(a<0) {
			long q=Math.ceilDiv(-a, m);
			a+=q*m;
			return floorSum(a, b, m, n)-q*(n*(n-1)/2);//q(0+1+..+n-1)で調整
		}
		if(a==0) return (b/m)*n;
		if(a>=m) {
			long q=a/m;
			return (n*(n-1)/2)*q+floorSum(a-q*m, b, m, n);
		}
		if(b>=m) {
			long q=b/m;
			return q*n+floorSum(a, b-q*m, m, n);
		}
		// ax+b ≥ my, 0　≤　x　≤　n-1, 1 ≤ y ≤ [(a(n-1)+b)/m]を満たす(x,y)の数え上げ
		// P=[(a(n-1)+b)/m]と置く
		// ax+b ≥ my, 0　≤　x　≤　n-1, 1 ≤ y ≤ P
		// -ax+b+(n-1)a ≥ mP-my, 0　≤　x　≤　n-1, 0 ≤ y ≤ P-1
		// my+b+(n-1)a-mP≥ ax, 0　≤　x　≤　n-1, 0 ≤ y ≤ P-1
		// my+b+na-mP≥ ax, 1　≤　x　≤　n, 0 ≤ y ≤ P-1
		// Σ floor((my+b+na-mP)/a) for 0≤y≤P-1
		long P=(a*(n-1)+b)/m;
		return floorSum(m, b+n*a-m*P, a, P);
	}

	/**
	 * count i such that 0 <= i < n and (a*i+b mod m) < t.
	 * mod m is the floor mod, i.e., x mod m = x - m * floor(x/m).
	 * @param n
	 * @param m
	 * @param a
	 * @param b
	 * @param t
	 * @return
	 */
	public static long countLinearModLess(long n, long m, long a, long b, long t) {
		if (m <= 0) throw new ArithmeticException("/ by zero or negative mod");
		if (t <= 0) return 0;
		if (t >= m) return n <= 0 ? 0 : n;
		return floorSum(a, b, m, n) - floorSum(a, b - t, m, n);
	}

	/**
	 * $0 \le i < n$ かつ $(a \cdot i + b \bmod m) \le t$ を満たす整数 $i$ の個数を数え上げる。
	 * ただし $x \bmod m = x - m \lfloor x/m \rfloor$ とする。
	 *
	 * <p>計算量: $O(\log m)$</p>
	 *
	 * @param n 項数 ($n \ge 0$)
	 * @param m 法 ($m > 0$)
	 * @param a 係数
	 * @param b 定数項
	 * @param t 閾値
	 * @return 条件を満たす $i$ の個数
	 */
	public static long countLinearModLessOrEqual(long n, long m, long a, long b, long t) {
		if (m <= 0) throw new ArithmeticException("/ by zero or negative mod");
		if (t < 0) return 0;
		if (t >= m - 1) return n <= 0 ? 0 : n;
		return countLinearModLess(n, m, a, b, t + 1);
	}

	/**
	 * $0 \le i < n$ かつ $(a \cdot i + b \bmod m) > t$ を満たす整数 $i$ の個数を数え上げる。
	 * ただし $x \bmod m = x - m \lfloor x/m \rfloor$ とする。
	 *
	 * <p>計算量: $O(\log m)$</p>
	 *
	 * @param n 項数 ($n \ge 0$)
	 * @param m 法 ($m > 0$)
	 * @param a 係数
	 * @param b 定数項
	 * @param t 閾値
	 * @return 条件を満たす $i$ の個数
	 */
	public static long countLinearModGreater(long n, long m, long a, long b, long t) {
		if (m <= 0) throw new ArithmeticException("/ by zero or negative mod");
		if (n <= 0) return 0;
		if (t < 0) return n;
		if (t >= m - 1) return 0;
		return n - countLinearModLessOrEqual(n, m, a, b, t);
	}

	/**
	 * $0 \le i < n$ かつ $(a \cdot i + b \bmod m) \ge t$ を満たす整数 $i$ の個数を数え上げる。
	 * ただし $x \bmod m = x - m \lfloor x/m \rfloor$ とする。
	 *
	 * <p>計算量: $O(\log m)$</p>
	 *
	 * @param n 項数 ($n \ge 0$)
	 * @param m 法 ($m > 0$)
	 * @param a 係数
	 * @param b 定数項
	 * @param t 閾値
	 * @return 条件を満たす $i$ の個数
	 */
	public static long countLinearModGreaterOrEqual(long n, long m, long a, long b, long t) {
		//https://atcoder.jp/contests/abc283/submissions/77310164
		if (m <= 0) throw new ArithmeticException("/ by zero or negative mod");
		if (n <= 0) return 0;
		if (t <= 0) return n;
		if (t >= m) return 0;
		return n - countLinearModLess(n, m, a, b, t);
	}

	/**
	 * count (x, y) s.t. ax+by <= c and from x0 <= x < x1 and y0 <= y < y1
	 * @param x0
	 * @param x1
	 * @param y0
	 * @param y1
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 * https://atcoder.jp/contests/abc372/submissions/71815803
	 */
	public static long countPointsInHalPlaneRectIntersection(long x0, long x1, long y0, long y1, long a, long b, long c) {
		// count (x, y) s.t. ax+by <= c and x0 <= x < x1 and y0 <= y < y1
		c-=a*x0+b*y0;
		x1-=x0;
		y1-=y0;
		x0=0;
		y0=0;
		// count (x, y) s.t. ax+by <= c and 0 <= x < x1 and 0 <= y < y1
		if(x1<=0||y1<=0)return 0;
		if(a<=0||b<=0)throw new AssertionError();
		if(c<0)return 0;
		x1 = Math.min(x1, c/a+1);
		y1 = Math.min(y1, c/b+1);
		if(a*(x1-1)+b*(y1-1) <= c) {
			return x1*y1;
		}
		// count (x, y) s.t. ax+by <= c  and 0 <= x < x1 and 0 <= y < y1
		//  ax + b(y1-1) ≥ c ⇔ x ≥ (c - b(y1-1)) / a のとき自動的に y ≤ y1 - 1が満たされる
		// もう一方の軸も同様。
		// count (x, y) s.t. ax+by <= c and 0 <= x, y
		// count (x, y) s.t. 0 <= y <= (c-ax)/b  and 0 <= x
		// count (x, y) s.t. 0 <= y <= (c-a[c/a]+ax)/b  and 0 <= x <= [c/a]
		// 1+c/a+Σ [(c-a[c/a]+ax)/b]  and 0 <= x < c/a+1
		long x2=Math.ceilDiv(c-b*(y1-1), a);
		long y2=Math.ceilDiv(c-a*(x1-1), b);
		// x2 ≤ x and y2 ≤ y では ax+by≤cだけ考えればよい。
		c-=x2*a+y2*b;
		long ret=0;
		if(c>=0) ret+=(1+c/a+floorSum(a, c-a*(c/a), b, c/a+1));
		ret+=x2*y1;
		ret+=y2*x1;
		ret-=x2*y2;
		return ret;
	}

	/**
	 * $G[p][q] = \sum_{0 \le i < n, (ai+b \pmod m) < t} i^p \lfloor \frac{ai+b}{m} \rfloor^q$ を $0 \le p + q \le 1$ について計算する。
	 *
	 * @param n 項数 ($n \ge 0$)
	 * @param m 法 ($m > 0$)
	 * @param a 係数
	 * @param b 定数項
	 * @param t しきい値
	 * @return $G[0][0]$, $G[1][0]$, $G[0][1]$ を格納した 2D 配列。$G[0][1]$ は $q=1$ のモーメント。
	 */
	public static long[][] halfplaneMomentsLinearModLess1(long n, long m, long a, long b, long t) {
		if (m <= 0) throw new ArithmeticException("/ by zero or negative mod");
		long[][] ret = new long[2][];
		ret[0] = new long[2];
		ret[1] = new long[1];
		if (n <= 0 || t <= 0) return ret;
		if (t > m) t = m;

		long qa = Math.floorDiv(a, m);
		long ra = Math.floorMod(a, m);
		long qb = Math.floorDiv(b, m);
		long rb = Math.floorMod(b, m);

		// H(i) = qa * i + qb + H_red(i)
		// Condition: (ra * i + rb mod m) < t
		// Indicator I(i) = floor((ra * i + rb) / m) - floor((ra * i + rb - t) / m)

		long[][] f = halfplaneMoments1Safe(ra, rb, m, n);
		long[][] g = halfplaneMoments1Safe(ra, rb - t, m, n);

		long g00_red = f[0][0] - g[0][0];
		long g10_red = f[1][0] - g[1][0];
		long g01_red = f[0][1] - g[0][1];

		ret[0][0] = g00_red;
		ret[1][0] = g10_red;
		// G[0][1] = sum_{i:cond} (qa * i + qb + H_red(i))
		ret[0][1] = qa * g10_red + qb * g00_red + g01_red;

		return ret;
	}

	/**
	 * $H(i) = \lfloor \frac{ai+b}{m} \rfloor$ としたとき、以下のモーメントを $0 \le p+q \le 1$ について計算する。
	 * <ul>
	 *   <li>ret[0][0]: $\sum_{0 \le i < n} H(i)$</li>
	 *   <li>ret[1][0]: $\sum_{0 \le i < n} i H(i)$</li>
	 *   <li>ret[0][1]: $\sum_{0 \le i < n} \binom{H(i)+1}{2}$</li>
	 * </ul>
	 * $a \ge 0, b$ は任意, $m > 0, n \ge 0$ に対応する。
	 *
	 * @param a 係数
	 * @param b 定数項
	 * @param m 法
	 * @param n 項数
	 * @return モーメントを格納した 2D 配列
	 */
	private static long[][] halfplaneMoments1Safe(long a, long b, long m, long n) {
		if (n <= 0) return new long[][] {new long[2], new long[1]};
		long q = Math.floorDiv(b, m);
		long r = Math.floorMod(b, m);
		long[][] res = halfplaneMoments1(a, r, m, n);
		long[][] ret = new long[2][];
		ret[0] = new long[2];
		ret[1] = new long[1];
		long s0 = n;
		long s1 = n * (n - 1) / 2;
		ret[0][0] = q * s0 + res[0][0];
		ret[1][0] = q * s1 + res[1][0];
		ret[0][1] = q * (q + 1) / 2 * n + q * res[0][0] + res[0][1];
		return ret;
	}

	/**
	 * H(x)=floor((ax+b)/m)とする。
	 * F(p, q) = Σ x^p H(x)^{q+1}を0≤p+q≤1について返す。
	 * @param a
	 * @param b
	 * @param m
	 * @param n
	 * @return
	 */
	public static long[][] floorSumOfPolynomial1(long a, long b, long m, long n) {
		long[][]G=halfplaneMoments1(a, b, m, n);
		G[0][1]=2*G[0][1]-G[0][0];
		return G;
	}

	/**
	 *  <pre>F(p, q)= sum x^p y^q for ax+b ≥ my, 0　≤　x　≤　n-1
	 *       -sum x^p y^q for 0　≤　x　≤　n-1, y ≤ 0</pre>
	 * F(p, q) (0≤p+q≤1) を配列として返す。
	 * H(x)=floor((ax+b)/m)とすると
	 * q=0のとき
	 * <pre>F(p, q) = Σ[x=0..n-1] x^p H</pre>
	 * q=1のとき
	 * <pre>F(p, q) = Σ[x=0..n-1] x^p H(H+1)/2</pre>
	 * @param a
	 * @param b
	 * @param m
	 * @param n
	 * @return
	 */
	public static long[][] halfplaneMoments1(long a, long b, long m, long n) {
		long[][]ret=new long[2][];
		ret[0]=new long[2];
		ret[1]=new long[1];
		if (n <= 0) {
			return ret;
		}
		if(m==0)throw new AssertionError();
		if(b<0) {
			throw new AssertionError();
		}
		if(a<0) {
			throw new AssertionError();
		}
		if (a == 0) {
			// F(p, q) = sum x^i y^j for 1 ≤ y ≤ m/b, 0　≤　x　≤　n-1
			for (int i = 0; i < 2; i++) {
				for (int j = 0; i + j < 2; j++) {
					ret[i][j]=MathUtils.powSum(0, n, i)*MathUtils.powSum(1, b/m, j);
				}
			}
			return ret;
		}
		if(a>=m) {
			/*
			 *   Σ x^i y^j FOR (a+mc)x+b ≥ my, 0　≤　x　≤　n-1, 1 ≤ y
			 *  =Σ x^i y^j FOR (a+mc)x+b ≥ my ≥ mcx+1, 0　≤　x　≤　n-1, 1 ≤ y
			 *  +Σ x^i y^j FOR mcx ≥ my , 0　≤　x　≤　n-1, 1 ≤ y
			 *
			 *   Σ x^i y^j FOR mcx ≥ my , 0　≤　x　≤　n-1, 1 ≤ y
			 *  =Σ x^i y^j FOR cx ≥ y , 0　≤　x　≤　n-1, 1 ≤ y
			 *  =Σ[x=0..n-1] x^i Σ[y=1..cx] y^j
			 *
			 *
			 *   Σ x^i y^j FOR (a+mc)x+b ≥ my ≥ mcx+1, 0　≤　x　≤　n-1, 1 ≤ y
			 *  =Σ x^i (y+cx)^j FOR a+b ≥ my ≥ 1, 0　≤　x　≤　n-1, 1 ≤ y
			 */

			long c=a/m;
			a-=c*m;
			long[][]f=halfplaneMoments1(a, b, m, n);
			for (int i = 0; i < 2; i++) {
				for (int j = 0; i+j < 2; j++) {
					for (int k = 0; k <= j; ++k) {
						ret[i][j]+=MathUtils.comb(j, k)*MathUtils.pow(c, j-k)*f[i+j-k][k];
					}
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; i+j < 2; j++) {
					// Σ[x=0..n-1] x^i Σ[y=1..cx] y^j

					if(j==0) {
						// j=0の場合
						// Σ[x=0..n-1] cx^{i+1}
						ret[i][j]+=c*MathUtils.powSum(0, n, i+1);
					} else if (j==1) {
						// j=1の場合
						// Σ[x=0..n-1] x^i cx(cx+1)/2
						long s0=c*c*MathUtils.powSum(0, n, i+2);
						long s1=c*MathUtils.powSum(0, n, i+1);
						ret[i][j]+=(s0+s1)/2;
					} else {
						throw new AssertionError();
					}
				}
			}
			return ret;
		} else if (b >= m) {
			/*
			 *   Σ x^i y^j FOR ax+b+mc ≥ my, 0　≤　x　≤　n-1, 1 ≤ y
			 *  =Σ x^i y^j FOR 0　≤　x　≤　n-1, 1 ≤ y ≤ c
			 *  +Σ x^i (y+c)^j FOR ax+b ≥ my, 0　≤　x　≤　n-1, 1 ≤ y
			 */
			long c=b/m;
			b-=m*c;
			long[][]f=halfplaneMoments1(a, b, m, n);
			for (int i = 0; i < 2; i++) {
				for (int j = 0; i+j < 2; j++) {
					ret[i][j]+=MathUtils.powSum(0, n, i)*MathUtils.powSum(1, c, j);
				}
			}
			for (int i = 0; i < 2; i++) {
				for (int j = 0; i+j < 2; j++) {
					for (int k = 0; k <= j; k++) {
						ret[i][j]+=MathUtils.comb(j, k)*f[i][k]*MathUtils.pow(c, j-k);
					}
				}
			}
			return ret;
		}
		// P=[(a(n-1)+b)/m]と置く
		// Σ x^i y^j FOR ax+b ≥ my, 0　≤　x　≤　n-1, 1 ≤ y ≤ P
		//=Σ (n-1-x)^i (P-y)^j FOR -ax+b+(n-1)a ≥ mP-my, 0　≤　x　≤　n-1, 0 ≤ y ≤ P-1
		//=Σ (n-1-x)^i (P-y)^j FOR my+b+(n-1)a-mP≥ ax, 0　≤　x　≤　n-1, 0 ≤ y ≤ P-1
		//=Σ (n-1-x)^i (P-y)^j FOR my+b+(n-1)a-mP≥ ax, 0　≤　x　≤　n-1, 0 ≤ y ≤ P-1
		//=Σ (n-x)^i (P-y)^j FOR my+b+na-mP≥ ax, 1　≤　x　≤　n, 0 ≤ y ≤ P-1
		long P=(a*(n-1)+b)/m;
		long[][] f=halfplaneMoments1(m, b+n*a-m*P, a, P);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; i+j < 2; j++) {
				for (int k = 0; k <= i; k++) {
					for (int l = 0; l <= j; l++) {
						ret[i][j]+=MathUtils.comb(i, k)*MathUtils.comb(j, l)*MathUtils.pow(n, i-k)*MathUtils.pow(P, j-l)*((k+l)%2==1?-1:1)*f[l][k];
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Σ[i=0..n-1] floor((a i + b1) / m) floor((a i + b2) / m)
	 * b1, b2 <= m しか実装してない
	 * @param a
	 * @param b1
	 * @param b2
	 * @param m
	 * @param n
	 * @return
	 * https://atcoder.jp/contests/abc402/submissions/72949967
	 */
	public static long secondOrderFloorSum(long a, long b1, long b2, long m, long n) {
		if(b1>b2) {
			{
				var tmp = b1;
				b1 = b2;
				b2 = tmp;
			}
		}
		if(b1>=m||b2>=m)throw new AssertionError();
		long[][]f=floorSumOfPolynomial1(a, b1, m, n);
		long[][]g=floorSumOfPolynomial1(a, b2, m, n);
		long ret=0;
		ret+=(f[0][1]+f[0][0])/2;
		ret+=(g[0][1]-g[0][0])/2;
		return ret;
	}

	/**
	 * f(x)=floor((ax+b)/m)としたときの
	 * y^f(0) Π[i=0..n-1] xy^{f(i+1)-f(i)}
	 * @param a
	 * @param b
	 * @param m
	 * @param n
	 * @param x
	 * @param y
	 * @param st
	 * @return
	 * https://atcoder.jp/contests/abc429/submissions/72963661
	 */
	public static <T> T floorProd(long a, long b, long m, long n, T x, T y, MonoidStrategy<T> st) {
		if (n==0) return st.pow(y, b/m);
		if (b >= m) {
			long c=b/m;
			b-=m*c;
			return st.mul(st.pow(y, c), floorProd(a, b, m, n, x, y, st));
		} else if (a >= m){
			long c=a/m;
			a-=c*m;
			return floorProd(a, b, m, n, st.mul(x, st.pow(y, c)), y, st);
		} else {
			long ymax=(a*n+b)/m;
			if (ymax == 0) {
				return st.pow(x, n);
			}
			//(a*x+b)/m ≥ ymax
			// a*x+b ≥ ymax * m
			long argX=Math.max(0, Math.ceilDiv(m*ymax-b, a));
			return st.mul(st.mul(floorProd(m, m-b+a-1, a, (a*n+b)/m-1, y, x, st), y), st.pow(x, n-argX));
		}
	}
}
