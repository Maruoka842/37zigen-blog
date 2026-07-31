package library.util;

import java.util.Objects;
/**
 * 未テスト
 */
public class IntFraction implements Comparable<IntFraction> {
	int numerator = 1;
	int denominator = 1;
	
	public static final IntFraction ZERO = new IntFraction(0, 1);
	public static final IntFraction ONE = new IntFraction(1, 1);
	
	public IntFraction(int numerator, int denominator) {
		if (denominator < 0) {
			numerator *= -1;
			denominator *= -1;
		}
        int g = MathUtils.gcd(Math.abs(numerator), denominator);
        numerator/=g; denominator/=g;
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public double toDouble() {
		return 1. * numerator / denominator;
	}
	
	public int numerator() {
		return numerator;
	}
	
	public int denominator() {
		return denominator;
	}
	
	public IntFraction max(IntFraction o) {
		if (compareTo(o) >= 0) return this;
		else return o;
	}
	
	public IntFraction min(IntFraction o) {
		if (compareTo(o) <= 0) return this;
		else return o;
	}
	
	public IntFraction mul(IntFraction o) {
		return new IntFraction(numerator*o.numerator,denominator*o.denominator);
	}
	
	public IntFraction add(IntFraction o) {
		return new IntFraction(numerator*o.denominator+o.numerator*denominator, denominator*o.denominator);
	}

	public IntFraction subtract(IntFraction o) {
		return new IntFraction(numerator*o.denominator-o.numerator*denominator, denominator*o.denominator);
	}
	
	public IntFraction abs(IntFraction o) {
		return new IntFraction(Math.abs(numerator), Math.abs(denominator));
	}
	
	@Override
	public int compareTo(IntFraction o) {
		return Long.compare(1L * numerator * o.denominator, 1L * denominator * o.numerator);
	}
	
	public int compareTo(int x) {
		return Long.compare(numerator, 1L * x * denominator);
	}
	
    
    @Override
    public boolean equals(Object obj) {
    	if(this==obj)return true;
    	if (obj instanceof IntFraction f) {
    		return numerator==f.numerator&&denominator==f.denominator;
    	} else {
    		return false;
    	}
    }
    
    public int signum() {
    	return Integer.signum(numerator);
    }
    
    /**
     * 未テスト
     * @return
     */
    public int floor() {
    	int q=numerator / denominator;
    	if (signum() < 0 && numerator % denominator != 0) q--;
    	return q;
    }
	
	
    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
	
}