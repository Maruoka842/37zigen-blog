package library.util;

public class Nimber {
	static long[][] memo=new long[256][256];
	static {
		for(int i=0;i<256;++i)for(int j=0;j<256;++j) {
			memo[i][j]=-1;
		}
	}
	
	public static long add(long x, long y) {
		return x ^ y;
	}
	
	public static long mul(long x, long y) {
		if(x==0||y==0)return 0;
		if(x==1)return y;
		if(y==1)return x;
		int k=0;
		if(0<=x&&x<256&&0<=y&&y<256) {
			if(memo[(int)x][(int)y]!=-1) return memo[(int)x][(int)y];
		}
		while(k + 1 <= 5 && ((x>>>(1<<(k+1))) != 0 || (y>>>(1<<(k+1))) != 0)) k++;
		long A=1L<<(1<<k);
		long a=x>>>(1<<k);
		long b=x&(A-1);
		long c=y>>>(1<<k);
		long d=y&(A-1);
		
		//x=aA+b
		//y=cB+d
		long ret=0;
		ret^=mul(mul(a, c), A>>>1);
		long bd=mul(b, d);
		ret^=bd;
		//ac+ad+bc=(a+b)(c+d)+bd
		ret^= A * (mul(a^b, c^d) ^ bd);
		if(0<=x&&x<256&&0<=y&&y<256) {
			memo[(int)x][(int)y]=ret;
		}
		return ret;
	}
	
	public static long pow(long a, long n) {
		if (n == 0) return 1;
		long ret = pow(mul(a, a), n / 2);
		if (n % 2 == 1) ret = mul(ret, a);
		return ret;
	}
}
