package library.util.geometry;

public class DoublePoint implements GeometricObject {
	double x, y;
	
	public static DoublePoint origin=new DoublePoint(0, 0);
	
	public DoublePoint(double x, double y) {
		this.x=x;
		this.y=y;
	}
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public DoubleVector sub(DoublePoint o) {
		return new DoubleVector(x - o.x, y - o.y);
	}

	public DoublePoint add(DoubleVector v) {
		return new DoublePoint(x + v.x, y + v.y);
	}

	/**
	 * この点と別のオブジェクトの同値性を判定します。
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
		if (!(obj instanceof DoublePoint)) return false;
		DoublePoint other = (DoublePoint) obj;
		return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
	}

	/**
	 * この点のハッシュコードを計算します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @return ハッシュコード
	 */
	// 未テスト
	@Override
	public int hashCode() {
		return java.util.Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return "("+x+","+y+")";
	}

}
