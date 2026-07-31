package library.util.poset;

import library.util.Fp;

public class PartitionLattice {
	/**
	 * 分割 σ に対して
	 *     g(σ) = ∏_{B ∈ σ} f(B)
	 * と書けるような関数 g のメビウス変換を返す
	 * @param f
	 * @param mod
	 * @return
	 */
	public static long[] supsetMoebius(long[] f, long mod) {
		//https://atcoder.jp/contests/abc236/submissions/74344075
		if(f[0]!=0)throw new AssertionError();
		Fp fp=new Fp(mod);
		long[] g=f.clone();
		for (int i = 1; i < g.length; i++) {
			int sz=Integer.bitCount(i);
			g[i]=g[i]*fp.fac(sz-1)%mod;
			if(sz%2==0)g[i]=(mod-1)*g[i]%mod;
		}
		g=BooleanLattice.exp(g, mod);
		return g;
	}
}
