package library.util.collections;

import library.util.Itertools;
import library.util.MathUtils;

/**
 * https://atcoder.jp/contests/abc132/submissions/71767377
 */
public class ArrayOnQuotient {
	private long n;
	int sqrtn;
	public long[]data;// floor(n/i)が降順に並んでいる
	public ArrayOnQuotient(long n) {
		this.setN(n);
		sqrtn = (int)MathUtils.sqrt(n);
		data=new long[2*sqrtn];
	}
	
	public long n() {
		return getN();
	}
	
	/**
	 * a[floor(N/i)]=val
	 * @param i
	 * @param val
	 */
	public void setByDivisor(long i, long val) {
		if(i<=0)throw new AssertionError();
		data[idFromDivisor(i)]=val;
	}
	
	
	/**
	 * a[i]=val
	 * @param quotient
	 * @param val
	 */
	public void setByQuotient(long quotient, long val) {
		data[idFromQuotient(quotient)]=val;
	}
	
	public void addByQuotient(long quotient, long val) {
		data[idFromQuotient(quotient)]+=val;
	}

	
	/**
	 * return a[floor(N/i)]
	 * @param i
	 * @param coeffs
	 */
	public long getByDivisor(long i) {
		if(i<=0)throw new AssertionError();
		return data[idFromDivisor(i)];
	}
	
	/**
	 * q=floor[n/k] ≤ i となる最大のqを取りdata[q]を返す。
	 * @param i
	 * @return
	 */
	public long getByQuotient(long i) {
		if(i<=0)throw new AssertionError();
		return data[idFromQuotient(i)];
	}
	
	public int idFromDivisor(long divisor) {
		if (divisor <= sqrtn) return (int) (divisor-1);
		else return (int) (data.length-getN()/divisor);
	}
	
	public int idFromQuotient(long quotient) {
		// k <= n/i < k + 1
		// n/(k+1) < i <= n/k
		return idFromDivisor(getN() / quotient);
	}

	/**
	 *  a[floor(v/i)] = Σ[k=floor(v/i)≤floor(v/i)] a[k]
	 * @return
	 */
	public ArrayOnQuotient prefixSum() {
		var ret=copy();
		// v <= n/i < v + 1
		// n / (v + 1) < i <= n / v
		long v=1;
		long i=getN();
		int preId=-1;
		while(true) {
			int id=idFromDivisor(i);
			if(preId!=-1) {
				ret.data[id] += ret.data[preId];
			}
			preId = id;
			if(v==getN())break;
			i=getN()/(v+1);
			v=getN()/i;
		}
		return ret;
	}
	
	
	public ArrayOnQuotient suffixSum() {
		var ret=copy();
		// v <= n/i < v + 1
		// n / (v + 1) < i <= n / v
		long v=getN();
		long i=1;
		int preId=-1;
		while(true) {
			int id=idFromDivisor(i);
			if(preId!=-1) {
				ret.data[id] += ret.data[preId];
			}
			preId = id;
			if(v==1)break;
			i=getN()/v+1;
			v=getN()/i;
		}
		return ret;
	}
	
	public long[] rawArray() {
		return data;
	}
	
	/**
	 * L = data.length とすると
	 * n/d とインデックスの対応は d を n/d を保ったまま取れる最大値として
	 * d ≤ √n ならば d-1;
	 * さもなくば L - n / d;

	 * 商とインデックスの対応は
	 * q ≥  1 + [n / ([√n] + 1)] ならば floor(n / q) - 1
	 * q ≤  [n / ([√n] + 1)] ならば L - q
	 * 
	 * これは
	 * d <= n / q < d + 1 より
	 * d ≤ √n ⇔  [n / q] ≤ [√n]
	 * n / q < [√n] + 1
	 * q > n / ([√n] + 1)
	 * q >= 1 + [n / ([√n] + 1)]
	 */
	public ArrayOnQuotient copy() {
		ArrayOnQuotient ret=new ArrayOnQuotient(getN());
		ret.data=data.clone();
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for (var range : Itertools.floorRange(getN())) {
			sb.append("a["+range.quotient()+"]="+getByQuotient(range.quotient())+"\n");
		}
		return sb.toString();
	}
	
	public void dump() {
		System.out.println(toString());
	}

	/**
	 * @return the n
	 */
	public long getN() {
		return n;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(long n) {
		this.n = n;
	}
	
}