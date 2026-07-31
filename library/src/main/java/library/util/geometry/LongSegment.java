package library.util.geometry;

import library.util.Intervals;

/**
 * 方向付きの線分
 */
public class LongSegment {
	long x1, y1, x2, y2;
	
	public LongSegment(long x1, long y1, long x2, long y2) {
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
	}
	
	/**
	 * verified:https://atcoder.jp/contests/past16-open/submissions/71386535
	 * @param segment
	 * @return
	 */
	public boolean isIntersect(LongSegment segment) {
		if (isParallel(segment)) {
			if (!Intervals.hasOverlap(Math.min(x1, x2), Math.max(x1, x2)+1, Math.min(segment.x1, segment.x2), Math.max(segment.x1, segment.x2)+1)) {
				return false;
			}
			if (!Intervals.hasOverlap(Math.min(y1, y2), Math.max(y1, y2)+1, Math.min(segment.y1, segment.y2), Math.max(segment.y1, segment.y2)+1)) {
				return false;
			}
			var vec=new LongVector(x2-x1, y2-y1);
			long cross=vec.cross(new LongVector(segment.x1-x2, segment.y1-y2));
			return cross==0;
		}
		{
			var vec=new LongVector(x2-x1, y2-y1);
			long cross0=vec.cross(new LongVector(segment.x1-x2, segment.y1-y2));
			long cross1=vec.cross(new LongVector(segment.x2-x2, segment.y2-y2));
			if(cross0>0&&cross1>0)return false;
			if(cross0<0&&cross1<0)return false;
		}
		{
			var vec=new LongVector(segment.x2-segment.x1, segment.y2-segment.y1);
			long cross0=vec.cross(new LongVector(x1-segment.x2, y1-segment.y2));
			long cross1=vec.cross(new LongVector(x2-segment.x2, y2-segment.y2));
			if(cross0>0&&cross1>0)return false;
			if(cross0<0&&cross1<0)return false;
		}
		return true;
	}
	
	public boolean isParallel(LongSegment segment) {
		return new LongVector(x1-x2, y1-y2).cross(new LongVector(segment.x1-segment.x2, segment.y1-segment.y2))==0;
	}

	/**
	 * この線分と別のオブジェクト of 同値性を判定します。
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
		if (!(obj instanceof LongSegment)) return false;
		LongSegment other = (LongSegment) obj;
		return x1 == other.x1 && y1 == other.y1 && x2 == other.x2 && y2 == other.y2;
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
