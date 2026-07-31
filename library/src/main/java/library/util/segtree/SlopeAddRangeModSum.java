package library.util.segtree;

import library.util.Fp;

public class SlopeAddRangeModSum {
	SegTreelong sum0;
	SegTreelong sum1;
	SegTreelong sum2;
	int N;
	long i2;
	Fp fp;
	long mod;
	
	
	public SlopeAddRangeModSum(int N, long mod) {
		sum0=SegTreeFactory.modSum(N, mod);
		sum1=SegTreeFactory.modSum(N, mod);
		sum2=SegTreeFactory.modSum(N, mod);
		this.N=N;
		fp = new Fp(mod);
		i2 = fp.inv(2);
		this.mod = mod;
	}
	
	/**
	 * [l, r) に a(x-l) + bを足す
	 * @param l
	 * @param r
	 * @param val
	 */
	public void add(int l, int r, long a, long b) {
		l=Math.max(l, 0);
		r=Math.min(r, N);
		if(l>=r)return;
		if(r<=0)return;
		//[l, r) に a(x-l) + bを足すと、[0:i)の和は
		// 0 <= i <= l : 変化なし 
		// l <= i < r :    b(i-l+1) + a(0 + 1 + .. + (i - l))
		//                =b(i-l+1)+a(i-l+1)(i-l)/2
		//                =a/2 i^2 + (b + a/2 - al) i + b(-l+1)+a(-l+1)(-l)/2
		
		//累積和を管理する
		long a2 = a * i2 % mod;
		long q0=(1L * l * (l - 1) % mod * a2 + b * (-l + 1))% mod;
		long q1=(b + a2 - a * l) % mod;
		long q2=a2;
		sum0.mul(l, q0);
		sum1.mul(l, q1);
		sum2.mul(l, q2);
		if(r<N) {
			sum0.mul(r, -q0);
			sum1.mul(r, -q1);
			sum2.mul(r, -q2);
			int len = r-l;
			long v=b*len%mod+a*len%mod*(len-1)%mod*i2%mod;
			v%=mod;
			sum0.mul(r, v);
		}
	}
	
	public long get(int i) {
		return sum(i, i+1);
	}
	
	//https://atcoder.jp/contests/abc256/submissions/73826804
	public long sum(int l, int r) {
		long v0=sum0.fold(l ,r);
		long v1=sum1.fold(0, r) * (r - 1) - sum1.fold(0, l) * Math.max(0, l - 1);
		long v2=sum2.fold(0, r) * fp.pow(r-1, 2) - sum2.fold(0, l) * fp.pow(Math.max(0, l - 1), 2);
		long ans=fp.reduce(v0 + v1 + v2);
		return ans;
	}

	/**
	 * 内部状態を標準出力に出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 特になし。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	// 未テスト
	public void dump() {
		System.out.println("SlopeAddRangeModSum { N: " + N + ", mod: " + mod + " }");
		System.out.print("  sum0: ");
		sum0.dump();
		System.out.print("  sum1: ");
		sum1.dump();
		System.out.print("  sum2: ");
		sum2.dump();
	}
}
