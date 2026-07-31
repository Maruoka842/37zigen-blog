
package library.util.segtree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import library.util.MathUtils;
import library.util.monoid.operator.MonoidOperator.Pair_int_long;

public class LazySegTreelonglong {
	int n = 1;
	int inputN;
	long[] v;
	long[] lazy;
	private LongBinaryOperator mergeX;
	private LongBinaryOperator mergeA;
	private LongBinaryOperator mergeAX;
	long identityA;
	long identityX;
	int root = 1;
	
	public LazySegTreelonglong(int n, LazySegTreeStrategy_longlong strategy) {
		this.inputN = n;
		while (this.n < inputN) {
			this.n *= 2;
		}
		v = new long[2 * this.n];
		lazy = new long[2 * this.n];
		this.identityX = strategy.identityX();
		this.identityA = strategy.identityA();
		this.mergeA = strategy::mergeA;
		this.mergeX = strategy::mergeX;
		this.mergeAX = strategy::mergeAX;
		Arrays.fill(v, identityX);
		Arrays.fill(lazy, identityA);

	}
	
	
	@SuppressWarnings("unchecked")
	public LazySegTreelonglong(int n, LongBinaryOperator mergeA, LongBinaryOperator mergeX, LongBinaryOperator mergeAX, long identityA, long identityX) {
		this.inputN = n;
		this.n = 2 * Integer.highestOneBit(n);
		v = new long[2 * this.n];
		lazy = new long[2 * this.n];
		this.identityX = identityX;
		this.identityA = identityA;
		Arrays.fill(v, identityX);
		Arrays.fill(lazy, identityA);
		this.mergeA = (x, y) -> {
			if (x == identityA)
				return y;
			if (y == identityA)
				return x;
			return mergeA.applyAsLong(x, y);
		};
		this.mergeX = (x, y) -> {
			if (x == identityX)
				return y;
			if (y == identityX)
				return x;
			return mergeX.applyAsLong(x, y);
		};
		this.mergeAX = (x, y) -> {
			if (x == identityA)
				return y;
			return mergeAX.applyAsLong(x, y);
		};
	}
	
	
	public void build(long[] arr) {
		for (int i = 0; i < arr.length; ++i) {
			v[id(i, i + 1)] = arr[i];
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = mergeX.applyAsLong(v[2 * i], v[2 * i + 1]);
		}
	}
	
	public void fill(long val) {
		for (int i = 0; i < inputN; ++i) {
			v[id(i, i + 1)] = val;
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = mergeX.applyAsLong(v[2 * i], v[2 * i + 1]);
		}
	}

	/**
	 * 全ての要素を単位元 {@code identityX}, {@code identityA} で初期化する。
	 *
	 * <p>副作用: {@code v} の全ての要素が {@code identityX} に、{@code lazy} の全ての要素が {@code identityA} に書き換えられる。</p>
	 * <p>計算量: O(n)</p>
	 * 未テスト
	 */
	public void clear() {
		Arrays.fill(v, identityX);
		Arrays.fill(lazy, identityA);
	}

	void push(int k) {
		if (lazy[k] == identityA)
			return;
		v[k] = mergeAX.applyAsLong(lazy[k], v[k]);
		if (2 * k + 1 < v.length) {
			lazy[2 * k] = mergeA.applyAsLong(lazy[k], lazy[2 * k]);
			lazy[2 * k + 1] = mergeA.applyAsLong(lazy[k], lazy[2 * k + 1]);
		}
		lazy[k] = identityA;
	}
	
	public long get(int a) {
		return fold(a, a+1);
	}

	public long fold(int a, int b) {
		return fold(0, n, a, b, root);
	}

	public long act(int a, int b, long add) {
		return act(0, n, a, b, root, add);
	}

	
	public void mergeX(int id, long add) {
		mergeX(0, n, id, root, add);
	}
	
	void mergeX(int l, int r, int id, int k, long val) {
		if (r - l == 1) {
			if (l != id) throw new AssertionError();
			v[k] = mergeX.applyAsLong(v[k], val);
			return;
		}
		push(k);
		push(2*k);
		push(2*k+1);
		int m = (l + r) / 2;
		if (l <= id && id < m) set(l, m, id, 2 * k, val);
		else set(m, r, id, 2 * k + 1, val);
		v[k] = mergeX.applyAsLong(v[2 * k], v[2 * k + 1]);
	}
	
	
	public void set(int id, long add) {
		set(0, n, id, root, add);
	}
	
	void set(int l, int r, int id, int k, long val) {
		if (r - l == 1) {
			if (l != id) throw new AssertionError();
			v[k] = val;
			return;
		}
		push(k);
		push(2*k);
		push(2*k+1);
		int m = (l + r) / 2;
		if (l <= id && id < m) set(l, m, id, 2 * k, val);
		else set(m, r, id, 2 * k + 1, val);
		v[k] = mergeX.applyAsLong(v[2 * k], v[2 * k + 1]);
	}


	long act(int l, int r, int a, int b, int k, long add) {
		if (a <= l && r <= b) {
			lazy[k] = mergeA.applyAsLong(add, lazy[k]);
		}
		push(k);
		if (a <= l && r <= b) {
			return v[k];
		} else if (r <= a || b <= l) {
			return identityX;
		} else {
			int m = (l + r) / 2;
			long vl = act(l, m, a, b, 2 * k, add);
			long vr = act(m, r, a, b, 2 * k + 1, add);
			v[k] = mergeX.applyAsLong(v[2 * k], v[2 * k + 1]);
			return mergeX.applyAsLong(vl, vr);
		}
	}
	
	
	
	long fold(int l, int r, int a, int b, int k) {
		push(k);
		if (a <= l && r <= b) {
			return v[k];
		} else if (r <= a || b <= l) {
			return identityX;
		} else {
			int m = (l + r) / 2;
			long vl = fold(l, m, a, b, 2 * k);
			long vr = fold(m, r, a, b, 2 * k + 1);
			v[k] = mergeX.applyAsLong(v[2 * k], v[2 * k + 1]);
			return mergeX.applyAsLong(vl, vr);
		}
	}


	int id(int a, int b) {
		int w = Integer.lowestOneBit(a ^ b);
		return n / w + a / w;
	}
	
	public void dump() {
		System.out.println(toString());
		System.out.println("array");
		for (int i = 0; i < inputN; i++) {
			System.out.print(get(i)+(i==inputN-1?"\n":" "));
		}
	}

	@Override
	public String toString() {
		String ret = "";
		ret += "acted\n";
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				ret += String.valueOf(v[id(i, i + w)]) + " ";
			}
			ret += "\n";
		}
		ret += "acting\n";
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				ret += String.valueOf(lazy[id(i, i + w)])+" ";
			}
			if (w != 1) ret += "\n";
		}
		return ret;
	}

	/**
	 * 葉をは並べた配列を返す。
	 * @return
	 */
	public long[] getLeafs() {
		for (int i = 0; i < v.length; ++i)
			push(i);
		long[] ret =  new long[n];
		for (int i = 0; i < n; ++i)
			ret[i] = v[id(i, i + 1)];
		return ret;
	}
	
	public long identity() {
		return identityX;
	}
	
	public LongBinaryOperator mergeX() {
		return mergeX;
	}

	/**
	 * f.test(fold(l, r)) が真となる最大の r を返す。そのような r が存在しなければ l を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * https://atcoder.jp/contests/abc369/submissions/72236529
	 * @param l
	 * @param f
	 * @return
	 */
	public int maximalRight(int l, LongPredicate f) {
		if (l>=n) throw new AssertionError();
		int id = id(l, l + 1);
		int lastValidId = id;
		for (int i = MathUtils.floorLog2(id); i >= 0; i--) {
			push(id >> i);
		}
		long prd = v[id];
		if (!f.test(prd)) return l;
		while (true) {
			while (id != 1 && id % 2 == 1) {//rは変化なし
				id /= 2;
			}
			if (id == 1) return Math.min(rFromId(lastValidId), inputN);
			while (id % 2 == 0) {
				id /= 2;
				push(2 * id + 1);
				long nprd = mergeX.applyAsLong(prd, v[2 * id + 1]);
				if (f.test(nprd)) {
					lastValidId = 2 * id + 1;
					prd = nprd;
				} else {
					id = 2 * id + 1;
					while (2 * id < v.length) {
						push(id);
						push(2 * id);
						nprd = mergeX.applyAsLong(prd, v[2 * id]);
						if (f.test(nprd)) {
							prd = nprd;
							lastValidId = 2 * id;
							id = 2 * id + 1;
						} else {
							id = 2 * id;
						}
					}
					return Math.min(rFromId(lastValidId), inputN);
				}
			}
		}
	}
	
	
	
	
	/**
	 * f.test(fold(l+1, r+1)) が真となる最小の l を返す。そのような l が存在しなければ r を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * @param l
	 * @param f
	 * @return
	 */
	public int minimalLeft(int r, LongPredicate f) {
		r++;
		int l = r - 1;
		int id = id(l, r);
		for (int i = MathUtils.floorLog2(id); i >= 0; i--) {
			push(id >> i);
		}
		long prd = v[id];
		if (!f.test(prd)) return r;
		while (true) {
			while (id != 1 && id % 2 == 0) id /= 2;
			while (id % 2 == 1) {
				if (id == 1) return -1;
				id /= 2;
				push(2 * id);
				long nprd = mergeX.applyAsLong(v[2 * id], prd);
				if (f.test(nprd)) {
					l = lFromId(2 * id);
					prd = nprd;
				} else {
					id = 2 * id;
					while (2 * id < v.length) {
						push(id);
						push(2 * id + 1);
						nprd = mergeX.applyAsLong(v[2 * id + 1], prd);
						if (f.test(nprd)) {
							prd = nprd;
							l = lFromId(2 * id + 1);
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

	int rFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return a+w;
	}
	
	int lFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return a;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
