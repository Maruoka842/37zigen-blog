
package library.util.segtree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import library.util.MathUtils;
import library.util.monoid.operator.MonoidOperator.Pair_int_long;

public class Add_CountMin {
	int n = 1;
	int inputN;
	long[] min;
	long[] countMin;
	long[] lazy;
	long identityA = 0;
	int root = 1;
	
	public Add_CountMin(int n) {
		this.inputN = n;
		while (this.n < inputN) {
			this.n *= 2;
		}
		min = new long[2 * this.n];
		countMin = new long[2 * this.n];
		lazy = new long[2 * this.n];
		Arrays.fill(min, Long.MAX_VALUE/3);
		Arrays.fill(lazy, identityA);
		identityA = 0;
	}
	
	public void fill0WithGivenWidth(long[] a) {
		for (int i = 0; i < a.length; ++i) {
			min[id(i, i + 1)] = 0;
			countMin[id(i, i + 1)] = a[i];
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			min[i] = Math.min(min[2 * i], min[2 * i + 1]);
			countMin[i] = 0;
			if (min[i] ==min[2*i])countMin[i] += countMin[2*i];
			if (min[i] ==min[2*i+1])countMin[i] += countMin[2*i+1];
		}
	}
	
	
	void push(int k) {
		if (lazy[k] == identityA)
			return;
		min[k] += lazy[k];
		if (2 * k + 1 < min.length) {
			lazy[2 * k] += lazy[k];
			lazy[2 * k + 1] += lazy[k];
		}
		lazy[k] = identityA;
	}
	
	public Pair get(int a) {
		return fold(a, a+1);
	}

	public Pair fold(int a, int b) {
		return fold(0, n, a, b, root);
	}

	public void act(int a, int b, long add) {
		act(0, n, a, b, root, add);
	}

	public void set(int id, long add) {
		set(0, n, id, root, add);
	}
	
	void set(int l, int r, int id, int k, long val) {
		if (r - l == 1) {
			if (l != id) throw new AssertionError();
			min[k] = val;
			return;
		}
		push(k);
		push(2*k);
		push(2*k+1);
		int m = (l + r) / 2;
		if (l <= id && id < m) set(l, m, id, 2 * k, val);
		else set(m, r, id, 2 * k + 1, val);
		min[k] = Math.min(min[2 * k], min[2 * k + 1]);
		countMin[k] = 0;
		if (min[k] == min[2 * k]) countMin[k] += countMin[2 * k];
		if (min[k] == min[2 * k + 1]) countMin[k] += countMin[2 * k + 1];
	}


	void act(int l, int r, int a, int b, int k, long add) {
		if (a <= l && r <= b) {
			lazy[k] += add	;
		}
		push(k);
		if (a <= l && r <= b) {
			return;
		} else if (r <= a || b <= l) {
			return;
		} else {
			int m = (l + r) / 2;
			act(l, m, a, b, 2 * k, add);
			act(m, r, a, b, 2 * k + 1, add);
			min[k] = Math.min(min[2 * k], min[2 * k + 1]);
			countMin[k] = 0;
			if (min[k] == min[2 * k]) countMin[k] += countMin[2 * k];
			if (min[k] == min[2 * k + 1]) countMin[k] += countMin[2 * k + 1];
			return;
		}
	}
	
	class Pair {
		long min;
		long countMin;
		
		public Pair(long min, long countMin) {
			this.min = min;
			this.countMin = countMin;
		}
	}
	
	public long minCount() {
		if (lazy[root]!=identityA) {
			push(root);
			min[root] = Math.min(min[2 * root], min[2 * root + 1]);
			countMin[root] = 0;
			if (min[root] == min[2 * root]) countMin[root] += countMin[2 * root];
			if (min[root] == min[2 * root + 1]) countMin[root] += countMin[2 * root + 1];
		}
		return countMin[root];
	}
	
	public long min() {
		if (lazy[root]!=identityA) {
			push(root);
			min[root] = Math.min(min[2 * root], min[2 * root + 1]);
			countMin[root] = 0;
			if (min[root] == min[2 * root]) countMin[root] += countMin[2 * root];
			if (min[root] == min[2 * root + 1]) countMin[root] += countMin[2 * root + 1];
		}
		return min[root];
	}
	
	Pair fold(int l, int r, int a, int b, int k) {
		push(k);
		if (a <= l && r <= b) {
			return new Pair(min[k], countMin[k]);
		} else if (r <= a || b <= l) {
			return new Pair(Long.MAX_VALUE / 3, 0);
		} else {
			int m = (l + r) / 2;
			var vl = fold(l, m, a, b, 2 * k);
			var vr = fold(m, r, a, b, 2 * k + 1);
			min[k] = Math.min(min[2 * k], min[2 * k + 1]);
			countMin[k] = 0;
			if (min[k] == min[2 * k]) countMin[k] += countMin[2 * k];
			if (min[k] == min[2 * k + 1]) countMin[k] += countMin[2 * k + 1];
			var ret = new Pair(Math.min(vl.min, vr.min), 0);
			if (ret.min == vl.min) ret.countMin += vl.countMin;
			if (ret.min == vr.min) ret.countMin += vr.countMin;
			return ret;
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
				ret += String.valueOf(min[id(i, i + w)]) + " ";
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
		for (int i = 0; i < min.length; ++i)
			push(i);
		long[] ret =  new long[n];
		for (int i = 0; i < n; ++i)
			ret[i] = min[id(i, i + 1)];
		return ret;
	}
	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}
}
