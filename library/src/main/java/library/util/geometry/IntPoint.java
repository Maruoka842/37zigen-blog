package library.util.geometry;

public class IntPoint {
	int x, y;
	
	public static IntPoint origin=new IntPoint(0, 0);
	
	public IntPoint(int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
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
		if (!(obj instanceof IntPoint)) return false;
		IntPoint other = (IntPoint) obj;
		return x == other.x && y == other.y;
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
