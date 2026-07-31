
package library.util.segtree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import library.util.MathUtils;
import library.util.monoid.operator.MonoidOperator.Pair_int_long;

public class IndexedLazySegTreelonglong {
	//https://atcoder.jp/contests/abc417/submissions/73786371
	int n = 1;
	int inputN;
	long[] v;
	long[] lazy;
	IndexedLazySegTreeStrategy_longlong strategy;
	long identityA;
	long identityX;
	int root = 1;
	
	public IndexedLazySegTreelonglong(int n, IndexedLazySegTreeStrategy_longlong strategy) {
		this.inputN = n;
		while (this.n < inputN) {
			this.n *= 2;
		}
		v = new long[2 * this.n];
		lazy = new long[2 * this.n];
		this.identityX = strategy.identityX();
		this.identityA = strategy.identityA();
		this.strategy = strategy;
		Arrays.fill(v, identityX);
		Arrays.fill(lazy, identityA);

	}
	
	
	public void build(long[] arr) {
		for (int i = 0; i < arr.length; ++i) {
			v[id(i, i + 1)] = arr[i];
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = strategy.mergeX(v[2*i], v[2*i+1], lFromId(2*i), rFromId(2*i), rFromId(2*i+1));
		}
	}
	
	public void fill(long val) {
		for (int i = 0; i < inputN; ++i) {
			v[id(i, i + 1)] = val;
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = strategy.mergeX(v[2*i], v[2*i+1], lFromId(2*i), rFromId(2*i), rFromId(2*i+1));
		}
	}

	void push(int k) {
		if (lazy[k] == identityA)
			return;
		int l = lFromId(k);
		int r = rFromId(k);
		v[k] = strategy.mergeAX(lazy[k], v[k], l, r);
		if (2 * k + 1 < v.length) {
			lazy[2 * k] = strategy.mergeA(lazy[k], lazy[2 * k]);
			lazy[2 * k + 1] = strategy.mergeA(lazy[k], lazy[2 * k + 1]);
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
		v[k] = strategy.mergeX(v[2 * k], v[2 * k + 1], l, m, r);
	}


	long act(int l, int r, int a, int b, int k, long add) {
		if (a <= l && r <= b) {
			lazy[k] = strategy.mergeA(add, lazy[k]);
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
			v[k] = strategy.mergeX(v[2 * k], v[2 * k + 1], l, m, r);
			return strategy.mergeX(vl, vr, Math.max(l, a), m, Math.min(r, b));
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
			v[k] = strategy.mergeX(v[2 * k], v[2 * k + 1], l, m, r);
			return strategy.mergeX(vl, vr, Math.max(l, a), m, Math.min(r, b));
		}
	}


	int id(int a, int b) {
		int w = Integer.lowestOneBit(a ^ b);
		return n / w + a / w;
	}
	
	public void dump() {
		System.out.println(toString());
		for (int i = 0; i < inputN; i++) {
			System.out.print(get(i)+(i==inputN-1?"\n":" "));
		}
	}

	@Override
	public String toString() {
		String ret = "";
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				ret += String.valueOf(v[id(i, i + w)]) + " ";
			}
			ret += "\n";
		}
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				ret += String.valueOf(lazy[id(i, i + w)])+" ";
			}
			ret += "\n";
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
