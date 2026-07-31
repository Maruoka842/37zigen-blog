package library.util.segtree;
/**
 * https://atcoder.jp/contests/past20-open/submissions/71594346
 */
public class RangeAddRangeSum {
	SegTreelong sum0;
	SegTreelong sum1;
	int N;
	
	public RangeAddRangeSum(int N) {
		sum0=SegTreeFactory.sum(N);
		sum1=SegTreeFactory.sum(N);
		this.N=N;
	}
	
	/**
	 * [l, r)にvalを足す
	 * @param l
	 * @param r
	 * @param val
	 */
	public void add(int l, int r, long val) {
		l=Math.max(l, 0);
		r=Math.min(r, N);
		if(l>=r)return;
		if(r<=0)return;
		sum0.mul(l, val-val*l);
		sum1.mul(l, +val);
		if(r<N) {
			sum0.mul(r, -val+val*r);
			sum1.mul(r, -val);
		}
	}
	
	/**
	 * 未テスト
	 * @param i
	 * @param val
	 */
	public void set(int i, long val) {
		long preVal = get(i);
		add(i, i+1, -preVal + val);
	}
	
	public long get(int i) {
		return sum(i, i+1);
	}
	
	public long sum(int l, int r) {
		long v0=sum0.fold(l ,r);
		long v1=sum1.fold(0, r)*(r-1)-sum1.fold(0, l)*Math.max(0, (l-1));
		return v0+v1;
	}
	
	public void clear() {
		sum0.clear();
		sum1.clear();
	}
	
	/**
	 * 未テスト
	 */
	public void dump() {
		System.out.print("[");
		for (int i = 0; i < N; i++) {
			if (i > 0) System.out.print(", ");
			System.out.print(get(i));
		}
		System.out.println("]");
	}
	
}