package library.util.geometry;

import library.util.LongFraction;
import library.util.MathUtils;

public class DoubleLine {
	public double a, b, c;
	
	/**
	 * (x0,y0),(x1,y1)を結ぶ直線
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public DoubleLine(double x0, double y0, double x1, double y1) {
		this.a=y0-y1;
		this.b=-(x0-x1);
		this.c=-y0*x1+x0*y1;
	}
	
	public DoubleLine(DoublePoint p0, DoublePoint p1) {
		this(p0.x, p0.y, p1.x, p1.y);
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
		if (!(obj instanceof DoubleLine)) return false;
		DoubleLine other = (DoubleLine) obj;
		return Double.compare(a, other.a) == 0 &&
		       Double.compare(b, other.b) == 0 &&
		       Double.compare(c, other.c) == 0;
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
