package library.util;

import java.util.Arrays;

import library.util.collections.LongArrayList;

public class FractionUtils {
	/**
	 *　返される配列 a は以下を満たす：
	 * x / y = a0 + 1 / (a1 + 1 / (a2 + ... + 1 / ak))
	 * @param x
	 * @param y
	 * @return
	 */
	public static long[] continuedFraction(long x, long y) {
		//https://atcoder.jp/contests/abc333/submissions/71927474
		long[]a=new long[100];
		int size=0;
		while(y!=0) {
			long q=x/y;
			x-=q*y;
			a[size++]=q;
			var tmp = x;
			x = y;
			y = tmp;
		}
		return Arrays.copyOf(a, size);
	}
	
	/**
	 * 未テスト
	 * @param a
	 * @param b
	 * @return
	 */
	public static long[][] convergents(long a, long b) {
	    long[] cf = continuedFraction(a, b);
	    int n = cf.length;
	    long[][] res = new long[n][2];

	    long p0 = 1, q0 = 0;
	    long p1 = cf[0], q1 = 1;
	    res[0] = new long[]{p1, q1};

	    for (int i = 1; i < n; i++) {
	        long ai = cf[i];
	        long p2 = ai * p1 + p0;
	        long q2 = ai * q1 + q0;
	        res[i] = new long[]{p2, q2};
	        p0 = p1; q0 = q1;
	        p1 = p2; q1 = q2;
	    }
	    return res;
	}
	
	/**
	 * a/b < p/q < c/d
	 * を満たすp/qのうち、qが最小のものを [p, q]の形で返す。
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return
	 */
	public static long[] strictlyBetween(long a, long b, long c, long d) {
		//https://atcoder.jp/contests/abc408/submissions/71899365
		if(d==0) {
			long q=a/b;
			return new long[] {q+1, 1};
		}
		long q0=a/b;
		long q1=c/d;
		if(q0!=q1) {
			if(!(c%d==0&&c/d==q0+1)) {
				return new long[] {q0+1, 1};
			} else {
				//q + x/y < ? < q+1
				var x=strictlyBetween(1, 1, b, a-q0*b);
				ArrayUtils.swap(0, 1, x);
				return new long[] {x[0]+q0*x[1], x[1]};
			}
		} else {
			var x=strictlyBetween(d, c-q1*d, b, a-q0*b);
			ArrayUtils.swap(0, 1, x);
			x[0]+=q0*x[1];
			return x;
		}
	}


	/**
	 * compare(a/b, c/d)を互除法で
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return
	 * https://atcoder.jp/contests/abc372/submissions/71815803
	 */
	public static int compareFractionByEuclid(long a, long b, long c, long d) {
		if(b<0) {
			a*=-1;
			b*=-1;
		}
		if(d<0) {
			c*=-1;
			d*=-1;
		}
		if(a==0&&c==0)return 0;
		if(Long.signum(a)!=Long.signum(c))return Long.compare(a, c);
		if(a<0&&c<0) {
			return -compareFractionByEuclid(-a, b, -c, d);
		}
		long q0=a/b;
		long q1=c/d;
		if(q0!=q1)return Long.compare(q0, q1);
		a-=q0*b;
		c-=q1*d;
		if(a==0&&c==0)return 0;
		if(a==0||c==0)return Long.compare(a, c);
		return -compareFractionByEuclid(b, a, d, c);
	}
	
	
	public static int compareFraction(long a, long b, long c, long d) {
		if(b<0) {
			a*=-1;
			b*=-1;
		}
		if(d<0) {
			c*=-1;
			d*=-1;
		}
		if(a==0&&c==0)return 0;
		if(Long.signum(a)!=Long.signum(c))return Long.compare(a, c);
		if(a<0&&c<0) {
			return -compareFraction(-a, b, -c, d);
		}
		//a/b vs c/d
		//a*d vs c*b
		long hi0=Math.multiplyHigh(a, d);
		long hi1=Math.multiplyHigh(c, b);
		int comp=Long.compare(hi0, hi1);
		if(comp!=0)return comp;
		return Long.compareUnsigned(a*d, c*b);
	}

	/**
	 * $\sqrt{D}$ の連分数展開を返します。
	 * 返される配列を $k$ とすると、 $\sqrt{D} = k_0 + 1 / (k_1 + 1 / (k_2 + \dots + 1 / (k_n + 1 / k_1 + \dots)))$
	 * $k_1, \dots, k_n$ が周期部分です。
	 * 未テスト
	 * 計算量: $O(\text{period length})$ 。周期の長さは $O(\sqrt{D})$ です。
	 * @param D 非負整数。
	 * @return 連分数展開の係数配列。
	 */
	public static long[] continuedFractionSqrt(long D) {
		if (D < 0) throw new IllegalArgumentException("D must be non-negative");
		long sqrtD = MathUtils.sqrt(D);
		LongArrayList ks = new LongArrayList();
		ks.add(sqrtD);

		// 各ステップの状態を (sqrt(D) + a) / b と表す。 b | D - a^2 が成り立つ。
		// a1, b1 は最初の周期の開始点
		long a1 = sqrtD;
		long b1 = D - sqrtD * sqrtD;
		if (b1 == 0) return ks.toArray();
		for (long a = a1, b = b1; ; ) {
			// k = floor((sqrt(D) + a) / b)
			long k = (sqrtD + a) / b;
			ks.add(k);
			// 次の状態 (sqrt(D) + a') / b' を計算する
			// 1 / ((sqrt(D) + a) / b - k) = b / (sqrt(D) - (k*b - a))
			// = b * (sqrt(D) + (k*b - a)) / (D - (k*b - a)^2)
			// a' = k*b - a
			// b' = (D - a'^2) / b
			a = k * b - a;
			b = (D - a * a) / b;
			if (a == a1 && b == b1) break;
		}
		return ks.toArray();
	}

}
