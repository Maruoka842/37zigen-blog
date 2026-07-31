package library.util.algebra.strategy;

import library.util.algebra.strategy.monoid.MonoidStrategy;

public interface GroupStrategy<T> extends MonoidStrategy<T> {
	T identity();
	T mul(T a, T b);
	T inverse(T a);
	boolean equals(T a, T b);
	
	default T pow(T a, long n) {
		T ret = identity();
		T c = n < 0 ? inverse(a) : a;
		if (n < 0) {
			if (n == Long.MIN_VALUE) {
				// Avoid overflow when using Math.abs
				ret = mul(ret, c);
				n = -(n + 1);
			} else {
				n = -n;
			}
		}
		while(n!=0) {
			if(n%2==1)ret=mul(ret, c);
			c=mul(c, c);
			n/=2;
		}
		return ret;
	}
}
