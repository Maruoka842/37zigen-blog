package library.util.segtree;

import library.util.MathUtils;

/**
 * https://atcoder.jp/contests/abc331/submissions/71403494
 */
public class RollingHash{
	final long[] pow;
	final long[] ipow;
	final long mod;
	SegTreelong seg;
	
    public RollingHash(int N, long radix, long mod) {
    	seg=new SegTreelong(N, (x, y)->(x+y)%mod, 0);
    	this.mod = mod;
    	pow = new long[Math.max(2, N)];
    	ipow = new long[Math.max(2, N)];
    	pow[0]=1;
    	ipow[0]=1;
    	pow[1]=radix;
    	ipow[1]=MathUtils.modInv(radix, mod);
    	for (int i = 2; i < N; i++) {
			pow[i]=pow[i-1]*pow[1]%mod;
			ipow[i]=ipow[i-1]*ipow[1]%mod;
    	}
    }
    
    public void build(long[] a) {
    	long[]b=new long[a.length];
    	for (int i = 0; i < b.length; i++) {
			b[i]=a[i]*pow[i]%mod;
		}
    	seg.build(b);
    }

    public void set(int i, long val) {
        seg.set(i, val * pow[i] % mod);
    }
    
    public long fold(int l, int r) {
    	return seg.fold(l, r) * ipow[l] % mod;
    }
    
}