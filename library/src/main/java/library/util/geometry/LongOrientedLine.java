package library.util.geometry;

import library.util.LongFraction;
import library.util.MathUtils;

public class LongOrientedLine {
	public long a, b, c;
	
	/**
	 * (x0,y0)から(x1,y1)へ向かう直線。進行方向に対して、左側が正、右側が負。
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public LongOrientedLine(long x0, long y0, long x1, long y1) {
		// pからqに向かう直線とすると直線の方程式は
		// cross(q-p, (x,y)-p) = 0
		// 左辺は、xが直線の左側のとき正になる。
		long dy=y1-y0;
		long dx=x1-x0;
		this.a=-dy;
		this.b=dx;
		this.c=-y0*x1+x0*y1;
		reduce();
	}
	
	public LongOrientedLine(LongPoint p0, LongPoint p1) {
		this(p0.x, p0.y, p1.x, p1.y);
	}
	
	/**
	 * ax+by+c=0
	 * @param a
	 * @param b
	 * @param c
	 */
	public LongOrientedLine(long a, long b, long c) {
		this.a=a;this.b=b;this.c=c;
		reduce();
	}
	
	private void reduce() {
		long g=MathUtils.gcd(a, MathUtils.gcd(b, c));
		a/=g;
		b/=g;
		c/=g;
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
	public static LongOrientedLine perpendicularBisector(long x0, long y0, long x1, long y1) {
		return new LongOrientedLine(-2*(-x1+x0), 2*(y1-y0), x0*x0-x1*x1+y0*y0-y1*y1);
	}
	
	public FractionPoint intersection(LongOrientedLine line) {
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
	 * 点(x, y)が直線上にあるかを返す。
	 * 128ビット精度の整数演算を用いてオーバーフローを回避している。
	 *
	 * @param x 点の x 座標
	 * @param y 点の y 座標
	 * @return 直線上にあるなら true
	 * @complexity O(1)
	 */
	public boolean onLine(long x, long y) {
		return orientedSide(x, y) == 0;
	}
	
	/**
	 * 左側にある点なら1、右側にある点なら-1、直線上なら0
	 * 128ビット精度の整数演算を用いてオーバーフローを回避している。
	 *
	 * @param x 点の x 座標
	 * @param y 点の y 座標
	 * @return 左側なら 1, 右側なら -1, 直線上なら 0
	 * @complexity O(1)
	 */
	public int orientedSide(long x, long y) {
		long axLo = a * x;
		long byLo = b * y;

		long lo1 = axLo + byLo;
		long lo2 = lo1 + c;

		long axHi = Math.multiplyHigh(a, x);
		long byHi = Math.multiplyHigh(b, y);

		long carry1 = Long.compareUnsigned(lo1, axLo) < 0 ? 1L : 0L;
		long carry2 = Long.compareUnsigned(lo2, lo1) < 0 ? 1L : 0L;

		long cHi = c < 0 ? -1L : 0L;

		long hi = axHi + byHi + cHi + carry1 + carry2;

		if (hi > 0) return 1;
		if (hi < 0) return -1;
		if (lo2 != 0) return 1;
		return 0;
	}
	
	/**
	 * 左側にある点なら1、右側にある点なら-1、直線上なら0
	 * @param x
	 * @param y
	 * @return
	 * https://atcoder.jp/contests/abc373/submissions/72814512
	 */
	public int orientedSide(LongPoint p) {
		return Long.signum(a*p.x+b*p.y+c);
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
		if (!(obj instanceof LongOrientedLine)) return false;
		LongOrientedLine other = (LongOrientedLine) obj;
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
