
package library.util.segtree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import library.util.MathUtils;
import library.util.monoid.operator.MonoidOperator.Pair_int_long;

public class SwappableLazySegTreelonglong {
	int n = 1;
	int inputN;
	long[] v;
	long[] lazy;
	boolean[] swapped = new boolean[64];
	private LongBinaryOperator mergeX;
	private LongBinaryOperator mergeA;
	private LongBinaryOperator mergeAX;
	long identityA;
	long identityX;
	int root = 1;
	
	public SwappableLazySegTreelonglong(int n, LazySegTreeStrategy_longlong strategy) {
		this.inputN = n;
		this.n = 2 * Integer.highestOneBit(n);
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
	public SwappableLazySegTreelonglong(int n, LongBinaryOperator mergeA, LongBinaryOperator mergeX, LongBinaryOperator mergeAX, long identityA, long identityX) {
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
		boolean f=swapped[MathUtils.floorLog2((r-l)/2)];
		if (l <= id && id < m) {
			if (f) set(l, m, id, 2 * k + 1, val);
			else set(l, m, id, 2 * k, val);
		}  else {
			if (f) set(m, r, id, 2 * k, val);
			else set(m, r, id, 2 * k + 1, val);
		}
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
			boolean f=swapped[MathUtils.floorLog2((r-l)/2)];
			int m = (l + r) / 2;
			long vl, vr;
			if (f) {
				vl = fold(l, m, a, b, 2 * k + 1);
				vr = fold(m, r, a, b, 2 * k);
			} else {
				vl = fold(l, m, a, b, 2 * k);
				vr = fold(m, r, a, b, 2 * k + 1);
			}
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
			boolean f=swapped[MathUtils.floorLog2((r-l)/2)];
			long vl, vr;
			if (f) {
				vl = fold(l, m, a, b, 2 * k + 1);
				vr = fold(m, r, a, b, 2 * k);
			} else {
				vl = fold(l, m, a, b, 2 * k);
				vr = fold(m, r, a, b, 2 * k + 1);
			}
			v[k] = mergeX.applyAsLong(v[2 * k], v[2 * k + 1]);
			return mergeX.applyAsLong(vl, vr);
		}
	}

	public void swapBit(int k) {
		swapped[k] = !swapped[k];
	}
	
	int id(int a, int b) {
		int w = Integer.lowestOneBit(a ^ b);
		return n / w + a / w;
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

	public long identity() {
		return identityX;
	}
	
	public LongBinaryOperator mergeX() {
		return mergeX;
	}

	
	void tr(Object...objects) {System.out.println(Arrays.deepToString(objects));}

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
		System.out.println("SwappableLazySegTreelonglong { inputN: " + inputN + ", n: " + n + ", v: " + java.util.Arrays.toString(v) + ", lazy: " + java.util.Arrays.toString(lazy) + ", swapped: " + java.util.Arrays.toString(swapped) + " }");
	}
}
