package library.util.mo;

/**
 * 
 * 多項式fと整数列Aが与える。
 * A[l:r)に対して、sum_i f(#{i : i　in A[l:r)}) を求める。 
 * verified:https://atcoder.jp/contests/abc293/submissions/70357331
 */
class PolyEvalAtFrequency extends MoCursor<Long> {
	int[]A;
	long[]cnt;
	long ret=0;
	int left=0;
	int right=0;
	
	public PolyEvalAtFrequency(int[] A) {
		this.A=A;
		cnt=new long[A.length];
	}
	
	long f(long x) {
		return x*(x-1)*(x-2)/6;
	}
	
	@Override
	public void addLeft(int i) {
		left--;
		ret-=f(cnt[A[left]]);
		cnt[A[left]]++;
		ret+=f(cnt[A[left]]);
	}
	
	@Override
	public void addRight(int i) {
		ret-=f(cnt[A[right]]);
		cnt[A[right]]++;
		ret+=f(cnt[A[right]]);
		right++;
	}

	@Override
	public void popLeft(int i) {
		ret-=f(cnt[A[left]]);
		cnt[A[left]]--;
		ret+=f(cnt[A[left]]);
		left++;
	}
	
	@Override
	public void popRight(int i) {
		right--;
		ret-=f(cnt[A[right]]);
		cnt[A[right]]--;
		ret+=f(cnt[A[right]]);
	}
	
	@Override
	public Long getValue() {
		return ret;
	}
	
}