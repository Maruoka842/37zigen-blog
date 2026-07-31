package library.util.segtree;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import library.util.collections.IntArrayList;

/***
 * 
 * @param <T>
 * 指定されなければ op は null を単位元として扱う。
 * op は、単位元の掛け算を実装する必要はない。
 */
public class SegTree<T> {
	int n;//2冪
	int inputN;
	public T[] v;
	private T identity=null;
    java.util.function.BinaryOperator<T> op;

	public SegTree(int n_, java.util.function.BinaryOperator<T> op) {
		this.inputN = n_;
		n = Integer.highestOneBit(n_) << 1;
		v = (T[]) new Object[2 * n];
		for(int i=0;i<v.length;++i) {
			v[i]=identity;
		}
		this.op = op;
	}
	
	public SegTree(int n, java.util.function.BinaryOperator<T> op, T identiy) {
		this.n = Integer.highestOneBit(n) << 1;
		this.inputN = n;
		v = (T[]) new Object[2 * this.n];
		this.identity = identiy;
		for(int i=0;i<v.length;++i) {
			v[i]=identity;
		}
		this.op = op;
	}
	
	public void build(T[] a) {
		for (int i = 0; i < a.length; i++) {
			v[id(i, i + 1)] = a[i];
		}
		for (int i = n - 1; i >= 1; i--) {
			v[i] = op(v[2*i],v[2*i+1]);
		}
	}
	
	public void set(int k, T t) {
		k = id(k, k + 1);
		v[k]=t;
		while (k != id(0, n)) {
			k /= 2;
			v[k] = op(v[2 * k], v[2 * k + 1]);
		};
	}
	
	public void mul(int k, T t) {
		set(k, op(v[id(k, k+1)], t));
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
	public T prodAll() {
		return fold(0, n);
	}
	
	
	/***
	 * id の逆関数。
	 * x = id(a, b) のとき rangeFromID(x) = new int[] {a, b}
	 * @param id
	 * @return
	 */
	public int[] rangeFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return new int[] {a, a + w};
	}
	
	/***
	 * id の逆関数。
	 * x = id(a, b) のとき rangeFromID(x) = new int[] {a, b}
	 * @param id
	 * @return
	 */
	public static int[] rangeFromId(int id, int n) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return new int[] {a, a + w};
	}
	
	public IntArrayList rangeToNodes(int l, int r) {
		/*
		 * 1
		 * 2 3
		 * 4567
		 */
		l += n;
		r += n;
		IntArrayList L = new IntArrayList();
		IntArrayList R = new IntArrayList();
		while (l < r) {
			if(l%2==1) {
				L.add(l++);
			}
			if(r%2==0) {
				R.add(--r);
			}
			l/=2;
			r/=2;
		}
		for (int i = R.size() - 1; i >= 0; i--) {
			L.add(R.get(i));
		}
		return L;
	}

	
	public static IntArrayList rangeToNodes(int l, int r, int n) {
		/*
		 * 1
		 * 2 3
		 * 4567
		 */
		l += n;
		r += n;
		IntArrayList L = new IntArrayList();
		IntArrayList R = new IntArrayList();
		while (l < r) {
			if(l%2==1) {
				L.add(l++);
			}
			if(r%2==1) {
				R.add(--r);
			}
			l/=2;
			r/=2;
		}
		for (int i = R.size() - 1; i >= 0; i--) {
			L.add(R.get(i));
		}
		return L;
	}

	
	
	public T fold(int l, int r) {
		r=Math.min(r, n);
		l=Math.max(l, 0);
		if (r - l <= 0) return identity;
		int ml = l + Integer.lowestOneBit(l);
		int mr = r - Integer.lowestOneBit(r);
		if (l < ml && ml <= r) {
			return op(v[id(l, ml)], fold(ml, r));
		} else {
			return op(fold(l, mr), v[id(mr, r)]);
		}
	}
	
	
	
	public <X> X fold(int l, int r, Function<T, X> f, BinaryOperator<X> xop, X identityX) {
		if (r - l <= 0) return identityX;
		int ml = l + Integer.lowestOneBit(l);
		int mr = r - Integer.lowestOneBit(r);
		if (l < ml && ml <= r) {
			return xop.apply(f.apply(v[id(l, ml)]), f.apply(fold(ml, r)));
		} else {
			return xop.apply(f.apply(fold(l, mr)), f.apply(v[id(mr, r)]));
		}
	}
	
	
	
	/**
	 * f.test(fold(l, r)) が真となる最大の r を返す。そのような r が存在しなければ l を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * @param l
	 * @param f
	 * @return
	 */
	public <X> int maximalRight(int l, Predicate<X> f, Function<T, X> g, BinaryOperator<X> xop) {
		if (l>=n) throw new AssertionError();
		int r = l + 1;
		int id = id(l, r);
		X prd = g.apply(v[id]);
		if (!f.test(prd)) return l;
		while (true) {
			while (id != 1 && id % 2 == 1) id /= 2;
			if (id == 1) return Math.min(r, inputN);
			while (id % 2 == 0) {
				id /= 2;
				X nprd = xop.apply(prd, g.apply(v[2 * id + 1]));
				if (f.test(nprd)) {
					r = rangeFromId(2 * id + 1)[1];
					prd = nprd;
				} else {
					id = 2 * id + 1;
					while (2 * id < v.length) {
						nprd = xop.apply(prd, g.apply(v[2 * id]));
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
	 * f.test(fold(l, r)) が真となる最大の r を返す。そのような r が存在しなければ l を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * @param l
	 * @param f
	 * @return
	 */
	public int maximalRight(int l, Predicate<T> f) {
		if (l>=n) throw new AssertionError();
		int r = l + 1;
		int id = id(l, r);
		T prd = v[id];
		if (!f.test(prd)) return l;
		while (true) {
			while (id != 1 && id % 2 == 1) id /= 2;
			if (id == 1) return Math.min(r, inputN);
			while (id % 2 == 0) {
				id /= 2;
				T nprd = op(prd, v[2 * id + 1]);
				if (f.test(nprd)) {
					r = rangeFromId(2 * id + 1)[1];
					prd = nprd;
				} else {
					id = 2 * id + 1;
					while (2 * id < v.length) {
						nprd = op(prd, v[2 * id]);
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
	public int minimalLeft(int r, Predicate<T> f) {
		r++;
		if (r > n) throw new AssertionError();
		int l = r - 1;
		int id = id(l, r);
		T prd = v[id];
		if (!f.test(prd)) return r-1;
		while (true) {
			while (id % 2 == 0) id /= 2;
			if (id == 1) return l-1;
			while (id % 2 == 1 && id != 1) {
				id /= 2;
				T nprd = op(prd, v[2 * id]);
				if (f.test(nprd)) {
					l = rangeFromId(2 * id)[0];
					prd = nprd;
				} else {
					id = 2 * id;
					while (2 * id < v.length) {
						nprd = op(prd, v[2 * id + 1]);
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

	
	
	T op(T a, T b) {
		if (a == identity) return b;
		if (b == identity) return a;
		return op.apply(a, b);
	}
	

	public T identity() {
    	return identity;
    }
    
	
	public T get(int i) {
		return v[id(i, i+1)];
	}
	
	@Override
	public String toString() {
		String ret = "";
		for (int w = 1; w <= n; w *= 2) {
			for (int i = 0; i < w; ++i) {
				ret += (v[i + w] == null ? "id" : v[i + w].toString()) + " ".repeat((n == w ? 1 : 2 * n / w - 1));
			}
			ret += "\n";
		}
		return ret;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

	public BinaryOperator<T> mergeX() {
		return op;
	}
}