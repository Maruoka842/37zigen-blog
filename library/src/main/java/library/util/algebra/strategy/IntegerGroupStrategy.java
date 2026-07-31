package library.util.algebra.strategy;

public interface IntegerGroupStrategy {
	int identity();
	int mul(int  a, int b);
	int inverse(int a);
	
	default int pow(int a, long n) {
		int ret = identity();
		int c = n < 0 ? inverse(a) : a;
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
