package library.util.geometry;

import library.util.IntFraction;
import library.util.LongFraction;
import library.util.MathUtils;

public class IntLine {
	public int a, b, c;
	
	/**
	 * (x0,y0),(x1,y1)を結ぶ直線
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public IntLine(int x0, int y0, int x1, int y1) {
		this.a=y0-y1;
		this.b=-(x0-x1);
		this.c=-y0*x1+x0*y1;
		reduce();
	}
	
	public IntLine(IntPoint p0, IntPoint p1) {
		this(p0.x, p0.y, p1.x, p1.y);
	}
	
	/**
	 * ax+by+c=0
	 * @param a
	 * @param b
	 * @param c
	 */
	public IntLine(int a, int b, int c) {
		this.a=a;this.b=b;this.c=c;
		reduce();
	}
	
	private void reduce() {
		long g=MathUtils.gcd(a, MathUtils.gcd(b, c));
		a/=g;
		b/=g;
		c/=g;
		if(a<0) {
			a*=-1;
			b*=-1;
			c*=-1;
		}
		if(a==0) {
			if(b<0) {
				b*=-1;
				c*=-1;
			}
		}
	}
	
	/**
	 * 未テスト
	 * @return
	 */
	public LongFraction yInterceptAsFraction() {
		return new LongFraction(-c, b);
	}

	/**
	 * 未テスト
	 * @return
	 */
	public LongFraction xInterceptAsFraction() {
		return new LongFraction(-c, a);
	}
	
	public double yIntercept() {
		return -1.*c/b;
	}
	
	public double xIntercept() {
		return -1.*c/a;
	}
	
	/**
	 * https://atcoder.jp/contests/abc220/submissions/71970424
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static LongLine perpendicularBisector(int x0, int y0, int x1, int y1) {
		return new LongLine(-2*(-x1+x0), 2*(y1-y0), 1L*x0*x0-1L*x1*x1+1L*y0*y0-1L*y1*y1);
	}
	
	/**
	 * 二つの直線の式ax+by+c=0のa,b,cの絶対値の最大値をA,B,Cとすると、
	 * 交点(x,y)の各分子、分母の絶対値は
	 * |分母| ≤ 2AB
	 * |分子| ≤ 2BC
	 * @param line
	 * @return
	 */
	public FractionPoint intersection(IntLine line) {
		// ax+by+c=0
		// a'x+b'y+c=0
		//を逆行列で解く
		long det=a*line.b-b*line.a;
		if (det == 0) {
	        // 直線が平行または一致
	        throw new UnsupportedOperationException("未実装でござる");
	    }
		LongFraction x=new LongFraction(-line.b*c+b*line.c, det);
		LongFraction y=new LongFraction(line.a*c-a*line.c, det);
		return new FractionPoint(x, y);
	}
	
	/**
	 * 二つの直線の式ax+by+c=0のa,b,cの絶対値の最大値をA,B,Cとすると、
	 * 交点(x,y)の各分子、分母の絶対値は
	 * |分母| ≤ 2AB
	 * |分子| ≤ 2max(A,B)C
	 * @param line
	 * @return
	 */
	public IntFractionPoint intersectionAsIntFraction(IntLine line) {
		// ax+by+c=0
		// a'x+b'y+c=0
		//を逆行列で解く
		int det=a*line.b-b*line.a;
		if (det == 0) {
	        // 直線が平行または一致
	        throw new UnsupportedOperationException("未実装でござる");
	    }
		IntFraction x=new IntFraction(-line.b*c+b*line.c, det);
		IntFraction y=new IntFraction(line.a*c-a*line.c, det);
		return new IntFractionPoint(x, y);
	}
	
	public boolean isParallel(IntLine line) {
		long det=1L*a*line.b-1L*b*line.a;
		return det==0;
	}
	
	public IntFraction xOfintersectionAsIntFraction(IntLine line) {
		// ax+by+c=0
		// a'x+b'y+c=0
		//を逆行列で解く
		int det=a*line.b-b*line.a;
		if (det == 0) {
	        // 直線が平行または一致
	        throw new UnsupportedOperationException("未実装でござる");
	    }
		IntFraction x=new IntFraction(-line.b*c+b*line.c, det);
		return x;
	}


	
	public long flooredXOfIntersectionAsLong(IntLine line) {
		// ax+by+c=0
		// a'x+b'y+c=0
		//を逆行列で解く
		long det=a*line.b-b*line.a;
		if (det == 0) {
	        // 直線が平行または一致
	        throw new UnsupportedOperationException("未実装でござる");
	    }
		long a=(-line.b*c+b*line.c);
		if(det<0) {
			det*=-1;a*=-1;
		}
		long q=a/det;
		if(a < 0 && a%det!=0)q--;
		return q;
	}

	/**
	 * doubleの有効数字は53bit。|a|,|b|,|c|≤6e7程度まで順序が保たれる。
	 * @param line
	 * @return
	 */
	public double xOfIntersectionAsDouble(IntLine line) {
		// ax+by+c=0
		// a'x+b'y+c=0
		//を逆行列で解く
		long det=a*line.b-b*line.a;
		if (det == 0) {
	        // 直線が平行または一致
	        throw new UnsupportedOperationException("未実装でござる");
	    }
		return 0.+1.*(-line.b*c+b*line.c)/det;
	}

	
	
	/**
	 * 点(x, y)が直線上にあるかを返す。
	 * 128ビット精度の整数演算を用いてオーバーフローを回避している。
	 *
	 * @param x 点の x 座標
	 * @param y 点の y 座標
	 * @return 直線上にあるなら true
	 * @complexity O(1)
	 */
	public boolean onLine(long x, long y) {
		long axLo = a * x;
		long byLo = b * y;

		long lo1 = axLo + byLo;
		long lo2 = lo1 + c;

		// 下位64bitが非ゼロなら、真の値も絶対に 0 ではない
		if (lo2 != 0) return false;

		// ここから上位64bitだけ確認する
		long axHi = Math.multiplyHigh((long)a, x);
		long byHi = Math.multiplyHigh((long)b, y);

		long carry1 = Long.compareUnsigned(lo1, axLo) < 0 ? 1L : 0L;
		long carry2 = Long.compareUnsigned(lo2, lo1) < 0 ? 1L : 0L;

		long cHi = c < 0 ? -1L : 0L;

		long hi = axHi + byHi + cHi + carry1 + carry2;

		return hi == 0;
	}
	
	public boolean onLine(LongPoint p) {
		return onLine(p.x, p.y);
	}
	
	/**
	 * ax+by+c=0より傾きは-a/b
	 * @return
	 */
	public double slopeAsDouble() {
		return -1.*a/b;
	}
	
	@Override
	public String toString() {
		return a+"x"+(b>=0?"+":"")+b+"y+"+c+"=0";
	}

	/**
	 * この直線と別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param obj 比較対象のオブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof IntLine)) return false;
		IntLine other = (IntLine) obj;
		return a == other.a && b == other.b && c == other.c;
	}

	/**
	 * この直線のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(a, b, c);
	}
}
