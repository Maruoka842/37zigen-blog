package library.util.segtree;

import java.util.Arrays;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;

public class SegTreelong {
	int n;//2冪
	int inputN;
	public long[] v;
	private long identity;
  private LongBinaryOperator op;

	public SegTreelong(int n, LongBinaryOperator op, long identiy) {
		this.n = Integer.highestOneBit(n);
		if (this.n != n) this.n *= 2;
		this.inputN = n;
		v = new long[2 * this.n];
		this.identity = identiy;
		for(int i=0;i<v.length;++i) {
			v[i]=identity;
		}
		this.setOp(op);
	}
	
	public void fill(long val) {
		for (int i = 0; i < inputN; i++) {
			v[id(i, i + 1)] = val;
		}
		for (int i = n - 1; i >= 1; i--) {
			v[i] = getOp().applyAsLong(v[2*i],v[2*i+1]);
		}
	}
	
	public void build(int[] a) {
		for (int i = 0; i < a.length; i++) {
			v[id(i, i + 1)] = a[i];
		}
		for (int i = n - 1; i >= 1; i--) {
			v[i] = getOp().applyAsLong(v[2*i],v[2*i+1]);
		}
	}
	
	public void build(long[] a) {
		for (int i = 0; i < a.length; i++) {
			v[id(i, i + 1)] = a[i];
		}
		for (int i = n - 1; i >= 1; i--) {
			v[i] = getOp().applyAsLong(v[2*i],v[2*i+1]);
		}
	}
	
	public void set(int k, long t) {
		k = id(k, k + 1);
		v[k]=t;
		while (k != id(0, n)) {
			k /= 2;
			v[k] = getOp().applyAsLong(v[2 * k], v[2 * k + 1]);
		}
	}
	
	public void mul(int k, long t) {
		set(k, getOp().applyAsLong(v[id(k, k+1)], t));
	}

	/**
	 * 全ての要素を単位元 {@code identity} で初期化する。
	 *
	 * <p>副作用: {@code v} の全ての要素が {@code identity} に書き換えられる。</p>
	 * <p>計算量: O(n)</p>
	 * 未テスト
	 */
	public void clear() {
		Arrays.fill(v, identity);
	}

	// [a2^k, (a+1)2^k)
	int id(int a, int b) {
		int w = b - a;
		return n / w + a / w;
	}
	
	/**
	 * O(1)
	 * @return
	 */
	public long prodAll() {
		return fold(0, n);
	}
	
	
	/***
	 * id の逆関数。
	 * x = id(a, b) のとき rangeFromID(x) = new int[] {a, b}
	 * @param id
	 * @return
	 */
	int[] rangeFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return new int[] {a, a + w};
	}
	
	/**
	 *  [l, n)をfold
	 * @param l
	 * @return
	 */
	public long suffixFold(int l) {
		return fold(l, n);
	}
	
	public long fold(int l, int r) {
		r=Math.min(r, n);
		l=Math.max(l, 0);
		if (r - l <= 0) return identity;
		int ml = l + Integer.lowestOneBit(l);
		int mr = r - Integer.lowestOneBit(r);
		if (l < ml && ml <= r) {
			return getOp().applyAsLong(v[id(l, ml)], fold(ml, r));
		} else {
			return getOp().applyAsLong(fold(l, mr), v[id(mr, r)]);
		}
	}
	
	
	
	
	/**
	 * f.test(fold(l, r)) が真となる最大の r を返す。そのような r が存在しなければ l を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * @param l
	 * @param f
	 * @return
	 */
	public int maximalRight(int l, LongPredicate f) {
		if (l>=n) throw new AssertionError();
		int r = l + 1;
		int id = id(l, r);
		long prd = v[id];
		if (!f.test(prd)) return l;
		while (true) {
			while (id != 1 && id % 2 == 1) id /= 2;
			if (id == 1) return Math.min(r, inputN);
			while (id % 2 == 0) {
				id /= 2;
				long nprd = getOp().applyAsLong(prd, v[2 * id + 1]);
				if (f.test(nprd)) {
					r = rangeFromId(2 * id + 1)[1];
					prd = nprd;
				} else {
					id = 2 * id + 1;
					while (2 * id < v.length) {
						nprd = getOp().applyAsLong(prd, v[2 * id]);
						if (f.test(nprd)) {
							prd = nprd;
							r = rangeFromId(2 * id)[1];
							id = 2 * id + 1;
						} else {
							id = 2 * id;
						}
					}
					return Math.min(r, inputN);
				}
			}
		}
	}

	
	/**
	 * (l, r] に対する演算。f.test(fold(l+1, r+1)) が真となる最小の l を返す。そのような l が存在しなければ r を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * @param l
	 * @param f
	 * @return
	 */
	public int minimalLeft(int r, LongPredicate f) {
		if(r<0)throw new AssertionError();
		r++;
		r=Math.min(r, n);
		int l = r - 1;
		int id = id(l, r);
		long prd = v[id];
		if (!f.test(prd)) return r-1;
		while (true) {
			while (id % 2 == 0) id /= 2;
			if (id == 1) return l-1;
			while (id % 2 == 1 && id != 1) {
				id /= 2;
				long nprd = getOp().applyAsLong(prd, v[2 * id]);
				if (f.test(nprd)) {
					l = rangeFromId(2 * id)[0];
					prd = nprd;
				} else {
					id = 2 * id;
					while (2 * id < v.length) {
						nprd = getOp().applyAsLong(prd, v[2 * id + 1]);
						if (f.test(nprd)) {
							prd = nprd;
							l = rangeFromId(2 * id + 1)[0];
							id = 2 * id;
						} else {
							id = 2 * id + 1;
						}
					}
					return l-1;
				}
			}
		}
	}

	
	
	long op(long a, long b) {
		if (a == identity) return b;
		if (b == identity) return a;
		return getOp().applyAsLong(a, b);
	}
	

	public long identity() {
    	return identity;
    }
    
	
	public long get(int i) {
		return v[id(i, i+1)];
	}
	
	public void dump() {
		System.out.println("セグメント木の各要素");
		for (int w = 1; w <= n; w *= 2) {
			for (int i = 0; i < w; ++i) {
				
				System.out.print((v[i + w] == identity()?"e":v[i+w]) + " ".repeat((n == w ? 1 : 2 * n / w - 1)));
			}
			System.out.println();
		}
		System.out.print("v=");
		for (int i = 0; i < inputN; i++) {
			long v=get(i);
			System.out.print((v==identity()?"e":v)+(i==inputN-1?"\n":" "));
		}
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	/**
	 * @return the op
	 */
	public LongBinaryOperator getOp() {
		return op;
	}

	/**
	 * @param op the op to set
	 */
	public void setOp(LongBinaryOperator op) {
		this.op = op;
	}

}
