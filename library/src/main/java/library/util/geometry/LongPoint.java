package library.util.geometry;

import java.util.Objects;

public class LongPoint {
	long x, y;
	
	public static LongPoint origin=new LongPoint(0, 0);
	
	public LongPoint(long x, long y) {
		this.x=x;
		this.y=y;
	}
	
	public long x() {
		return x;
	}
	
	public long y() {
		return y;
	}
	
	public LongVector sub(LongPoint o) {
		return new LongVector(x - o.x, y - o.y);
	}
	
	public long squaredDist() {
		return x*x+y*y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj)return true;
		if(!(obj instanceof LongPoint))return false;
		LongPoint p=(LongPoint)obj;
		return x==p.x&&y==p.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}
}
