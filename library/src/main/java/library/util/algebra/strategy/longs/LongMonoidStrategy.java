package library.util.algebra.strategy.longs;

@SuppressWarnings("hiding")
public interface LongMonoidStrategy {
	long identity();
	long mul(long a, long b);
	default long pow(long a, long n) {
		if (n < 0) throw new AssertionError();
		long ret=identity();
		while(n!=0) {
			if(n%2==1)ret=mul(ret, a);
			a=mul(a, a);
			n/=2;
		}
		return ret;
	}
}
