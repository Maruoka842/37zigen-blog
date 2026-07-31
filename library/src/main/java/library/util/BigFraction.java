package library.util;

import java.math.BigInteger;

public class BigFraction implements Comparable<BigFraction> {
	BigInteger numerator;
	BigInteger denominator;
	
	public static final BigFraction ZERO = new BigFraction(0, 1);
	
	public BigFraction(long numerator, long denominator) {
		if (denominator < 0) {
			numerator *= -1;
			denominator *= -1;
		}
		this.numerator = BigInteger.valueOf(numerator);
		this.denominator = BigInteger.valueOf(denominator);
	}
	
	
	
	public BigFraction(BigInteger numerator, BigInteger denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
		if (this.denominator.signum() < 0) {
			this.numerator = this.numerator.negate();
			this.denominator = this.denominator.negate();
		}
	}
	
	public double toDouble() {
		double ret=numerator.multiply(BigInteger.valueOf(10).pow(18)).divide(denominator).doubleValue();
		ret=ret/MathUtils.pow(10, 18);
		return ret;
	}
	
	public BigFraction max(BigFraction o) {
		if (compareTo(o) >= 0) return this;
		else return o;
	}
	
	public BigFraction min(BigFraction o) {
		if (compareTo(o) <= 0) return this;
		else return o;
	}
	
	@Override
	public int compareTo(BigFraction o) {
		if (numerator.signum() != o.numerator.signum()) {
			return numerator.compareTo(o.numerator);
		}
		return numerator.multiply(o.denominator).compareTo(denominator.multiply(o.numerator)) * numerator.signum();
	}
	
	public BigFraction add(BigFraction o) {
		return new BigFraction(numerator.multiply(o.denominator).add(o.numerator.multiply(denominator)), denominator.multiply(o.denominator));
	}

	public BigFraction subtract(BigFraction o) {
		return new BigFraction(numerator.multiply(o.denominator).subtract(o.numerator.multiply(denominator)), denominator.multiply(o.denominator));
	}
	
	public BigFraction abs() {
		return new BigFraction(numerator.abs(), denominator);
	}
	
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    BigFraction other = (BigFraction) obj;
	    return numerator.multiply(other.denominator)
	    		.equals(other.numerator.multiply(denominator));
	}

	
    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
	
}
