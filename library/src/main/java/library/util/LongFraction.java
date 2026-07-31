package library.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class LongFraction implements Comparable<LongFraction> {
	long numerator = 1;
	long denominator = 1;
	
	public static final LongFraction ZERO = new LongFraction(0, 1);
	public static final LongFraction ONE = new LongFraction(1, 1);
	
	public LongFraction(long numerator, long denominator) {
		if (denominator < 0) {
			numerator *= -1;
			denominator *= -1;
		}
        long g = MathUtils.gcd(Math.abs(numerator), denominator);
        numerator/=g; denominator/=g;
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	private LongFraction() {
		
	}
	
	public static LongFraction ofCoprime(long numerator, long denominator) {
		if (denominator < 0) {
			numerator *= -1;
			denominator *= -1;
		}
		LongFraction ret=new LongFraction();
		ret.numerator = numerator;
		ret.denominator = denominator;
		return ret;
	}
	
	public double toDouble() {
		return 1. * numerator / denominator;
	}
	
	public long numerator() {
		return numerator;
	}
	
	public long denominator() {
		return denominator;
	}
	
	public LongFraction max(LongFraction o) {
		if (compareTo(o) >= 0) return this;
		else return o;
	}
	
	public LongFraction min(LongFraction o) {
		if (compareTo(o) <= 0) return this;
		else return o;
	}
	
	public LongFraction mul(LongFraction o) {
		return new LongFraction(numerator*o.numerator,denominator*o.denominator);
	}
	
	public LongFraction add(LongFraction o) {
		return new LongFraction(numerator*o.denominator+o.numerator*denominator, denominator*o.denominator);
	}

	public LongFraction sub(LongFraction o) {
		return new LongFraction(numerator*o.denominator-o.numerator*denominator, denominator*o.denominator);
	}
	
	public LongFraction abs() {
		return new LongFraction(Math.abs(numerator), Math.abs(denominator));
	}
	
	@Override
	public int compareTo(LongFraction o) {
		return FractionUtils.compareFraction(numerator, denominator, o.numerator, o.denominator);
	}
	
	public int fastCompareTo(LongFraction o) {
		if (Long.signum(numerator) != Long.signum(o.numerator)) {
			return Long.compare(numerator, o.numerator);
		}
		return Long.compare(Math.abs(numerator * o.denominator), Math.abs(denominator * o.numerator)) * Long.signum(numerator);
	}
	
    
    @Override
    public boolean equals(Object obj) {
    	if(this==obj)return true;
    	if (obj instanceof LongFraction f) {
    		return numerator==f.numerator&&denominator==f.denominator;
    	} else {
    		return false;
    	}
    }
    
    public int signum() {
    	return Long.signum(numerator);
    }
    
    /**
     * 未テスト
     * @return
     */
    public long floor() {
    	long q=numerator / denominator;
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

	/**
	 * 順序 n のファレイ数列 F_n を 0/1 から 1/1 まで昇順に生成するイテレータを返す。
	 * F_n = \{ \frac{a}{b} : 0 \le a \le b \le n, \gcd(a, b) = 1 \}
	 * 計算量: O(n^2)
	 * @param n 順序
	 * @return F_n を順に返す Iterable
	 */
	public static Iterable<LongFraction> fareySequence(long n) {
		//https://atcoder.jp/contests/joisc2008/submissions/77113894
		return () -> new Iterator<>() {
			long a = 0, b = 1;
			long c = 1, d = n;
			boolean end = false;

			@Override
			public boolean hasNext() {
				return !end;
			}

			@Override
			public LongFraction next() {
				if (!hasNext()) throw new NoSuchElementException();
				LongFraction res = new LongFraction(a, b);
				if (a == 1 && b == 1) {
					end = true;
				} else {
					long q = (n + b) / d;
					long e = q * c - a;
					long f = q * d - b;
					a = c;
					b = d;
					c = e;
					d = f;
				}
				return res;
			}
		};
	}

}
