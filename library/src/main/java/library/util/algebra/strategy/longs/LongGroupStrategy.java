package library.util.algebra.strategy.longs;

public interface LongGroupStrategy extends LongMonoidStrategy {
	long identity();
	long mul(long  a, long b);
	long inverse(long a);
	
	default long pow(long a, long n) {
		long ret = identity();
		long c = n < 0 ? inverse(a) : a;
		if (n < 0) {
			if (n == Long.MIN_VALUE) {
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
