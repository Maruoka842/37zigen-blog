package library.util.geometry;

public class LongVector {
	long x;
	long y;
	
	public static final LongVector ZERO= new LongVector(0, 0);
	public static final LongVector EX= new LongVector(1, 0);
	public static final LongVector EY= new LongVector(0, 1);
	
	public LongVector(long x, long y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * 反時計回りに90度回転
	 * https://atcoder.jp/contests/abc251/submissions/72065074
	 * @return
	 */
	public LongVector rot90() {
		return new LongVector(-y, x);
	}
	
	/**
	 * 反時計回りに180度回転
	 * @return
	 */
	public LongVector rot180() {
		return new LongVector(-x, y);
	}
	
	/**
	 * 反時計回りに270度回転
	 * @return
	 */
	public LongVector rot270() {
		return new LongVector(-y, -x);
	}
	
	public LongVector sub(LongVector vec) {
		return new LongVector(x-vec.x, y-vec.y);
	}
	
	public LongVector add(LongVector vec) {
		return new LongVector(x+vec.x, y+vec.y);
	}

	public LongVector add(LongPoint p) {
		return new LongVector(x+p.x, y+p.y);
	}
	
	public LongPoint toPoint() {
		return new LongPoint(x, y);
	}

	
	public LongVector mul(long a) {
		return new LongVector(a * x, a * y);
	}
	
	public long squaredLength() {
		return x * x + y * y;
	}
	
	public double length() {
		return Math.sqrt(x*x+y*y);
	}
	
	public long x() {
		return x;
	}
	
	public long y() {
		return y;
	}
	
	public long cross(LongVector v) {
		return x * v.y - y * v.x;
	}
	
	public long dot(LongVector v) {
		return x * v.x + y * v.y;
	}
	
    /**
     * このベクトルを基準としたときのvの偏角が90度未満か(0~360度として)
     * @param v
     * @return
     */
	public boolean isPolarAngleLes90(LongVector v) {
    	return cross(v)>=0 && dot(v)>0;
    }
    
    /**
     * このベクトルを基準としたときのvの偏角が90度以下か(0~360度として)
     * @param v
     * @return
     */
    public boolean isPolarAngleLeq90(LongVector v) {
    	return cross(v)>=0 && dot(v)>=0;
    }
        
    /**
     * このベクトルを基準としたときのvの偏角が180度未満か(0~360度として)
     * @param v
     * @return
     */
    public boolean isPolarAngleLes180(LongVector v) {
    	long c=cross(v);
    	return c>0 || (c==0 && dot(v)>0) ;
    }
    
    /**
     * このベクトルを基準としたときのvの偏角が180度以下か(0~360度として)
     * @param v
     * @return
     */
    public boolean isPolarAngleLeq180(LongVector v) {
    	long c=cross(v);
    	return c >= 0;
    }
    
	/**
	 * このベクトルと別のオブジェクトの同値性を判定します。
	 *
	 * <p>計算量: $O(1)$</p>
	 *
	 * @param obj 比較対象 of オブジェクト
	 * @return 同値であれば true, そうでなければ false
	 */
	// 未テスト
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongVector)) return false;
		LongVector other = (LongVector) obj;
		return x == other.x && y == other.y;
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
