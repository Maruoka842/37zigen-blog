package library.util.geometry;

import java.util.Objects;

import library.util.LongFraction;

public class FractionPoint {
	LongFraction x, y;
	public FractionPoint(LongFraction x, LongFraction y) {
		this.x=x;
		this.y=y;
	}
	
	public LongFraction x() {
		return x;
	}

	public LongFraction y() {
		return y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (obj instanceof FractionPoint p) {
			return x.equals(p.x) && y.equals(p.y);
		} else {
			return false;
		}
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
