package library.util.geometry;

import java.util.Objects;

import library.util.IntFraction;
import library.util.LongFraction;

public class IntFractionPoint {
	IntFraction x, y;
	public IntFractionPoint(IntFraction x, IntFraction y) {
		this.x=x;
		this.y=y;
	}
	
	public IntFraction x() {
		return x;
	}

	public IntFraction y() {
		return y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (obj instanceof IntFractionPoint p) {
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
