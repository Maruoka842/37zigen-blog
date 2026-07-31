package library.util.geometry;

public class DoubleVector {
	double x;
	double y;
	public static final DoubleVector ZERO= new DoubleVector(0, 0);
	public static final DoubleVector EX= new DoubleVector(1, 0);
	public static final DoubleVector EY= new DoubleVector(0, 1);

	
	
	public DoubleVector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public DoubleVector sub(DoubleVector vec) {
		return new DoubleVector(x-vec.x, y-vec.y);
	}
	
	public DoubleVector add(DoubleVector vec) {
		return new DoubleVector(x+vec.x, y+vec.y);
	}
	
	public DoubleVector mul(double a) {
		return new DoubleVector(a * x, a * y);
	}
	
	public DoubleVector normalize() {
		double n = norm();
		return new DoubleVector(x / n, y / n);
	}
	
	public double norm() {
		return Math.hypot(x, y);
	}
	
	public double distance(DoubleVector vec) {
		double dx = x - vec.x;
		double dy = y - vec.y;
		return Math.hypot(dx, dy);
	}
	
	public DoubleVector rotate(double angle) {
		double sin=Math.sin(angle);
		double cos=Math.cos(angle);
		return new DoubleVector(cos * x - sin * y, sin * x + cos * y);
	}
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public DoubleVector copy() {
		return new DoubleVector(x, y);
	}
	
	public double atan() {
		return Math.atan2(y, x);
	}
	
	public double cross(DoubleVector v) {
		return x * v.y - y * v.x;
	}
	
	public double squaredLength() {
		return x * x + y * y;
	}
	
	
	  /**
     * このベクトルを基準としたときのvの偏角が180度未満か(0~360度として)
     * @param v
     * @return
     */
    public boolean isPolarAngleLes180(DoubleVector v) {
    	double c=cross(v);
    	return c>0 || (c==0 && dot(v)>0) ;
    }
    
	public double dot(DoubleVector v) {
		return x * v.x + y * v.y;
	}

	/**
	 * このベクトルと別のオブジェクトの同値性を判定します。
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
		if (!(obj instanceof DoubleVector)) return false;
		DoubleVector other = (DoubleVector) obj;
		return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
	}

	/**
	 * このベクトルのハッシュコードを計算します。
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
