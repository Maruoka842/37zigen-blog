package library.util.geometry;

import java.util.List;

public class DoubleSegment {
	public double x1, y1, x2, y2;
	
	public DoubleSegment(double x1, double y1, double x2, double y2) {
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
	}
	
	public DoubleSegment(DoublePoint p, DoublePoint q) {
		this(p.x, p.y, q.x, q.y);
	}

	/**
	 * 与えられた円 {@link DoubleCircle} とこの線分の交点を計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param circle 対象の円
	 * @return 円と線分の交点のリスト
	 */
	// 未テスト
	public List<DoublePoint> intersect(DoubleCircle circle) {
		return circle.intersect(this);
	}

	/**
	 * この線分と別のオブジェクトの同値性を判定します。
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
		if (!(obj instanceof DoubleSegment)) return false;
		DoubleSegment other = (DoubleSegment) obj;
		return Double.compare(x1, other.x1) == 0 &&
		       Double.compare(y1, other.y1) == 0 &&
		       Double.compare(x2, other.x2) == 0 &&
		       Double.compare(y2, other.y2) == 0;
	}

	/**
	 * この線分のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(x1, y1, x2, y2);
	}
}
