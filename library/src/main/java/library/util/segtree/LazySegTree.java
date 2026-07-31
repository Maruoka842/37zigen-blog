
package library.util.segtree;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import library.util.MathUtils;

public class LazySegTree<Acting, Acted> {
	int n = 1;
	int inputN;
	Acted[] v;
	Acting[] lazy;
	private BinaryOperator<Acted> mergeX;
	private BinaryOperator<Acting> mergeA;
	private BiFunction<Acting, Acted, Acted> mergeAX;
	final Acting identityA;
	final Acted identityX;
	int root = 1;
	int LOG=0;
	
	@SuppressWarnings("unchecked")
	public LazySegTree(int n, LazySegTreeStrategy<Acting, Acted> strategy) {
		this.inputN = n;
		while (this.n < inputN) {
			this.n *= 2;
			LOG++;
		}
		v = (Acted[]) new Object[2 * this.n];
		lazy = (Acting[]) new Object[2 * this.n];
		this.identityX = strategy.identityX();
		this.identityA = strategy.identityA();
		this.mergeA = strategy::mergeA;
		this.mergeX = strategy::mergeX;
		this.mergeAX = strategy::mergeAX;
		Arrays.fill(v, identityX);
		Arrays.fill(lazy, identityA);
	}
	
	
	@SuppressWarnings("unchecked")
	public LazySegTree(int n, BinaryOperator<Acting> mergeA, BinaryOperator<Acted> mergeX, BiFunction<Acting, Acted, Acted> mergeAX, Acted identityX) {
		inputN=n;
		while (this.n < inputN) {
			this.n *= 2;
			LOG++;
		}
		v = (Acted[]) new Object[2 * this.n];
		lazy = (Acting[]) new Object[2 * this.n];
		this.identityX = identityX;
		Arrays.fill(v, identityX);
		this.identityA = null;
		this.mergeA = (x, y) -> {
			if (x == identityA)
				return y;
			if (y == identityA)
				return x;
			return mergeA.apply(x, y);
		};
		this.mergeX = (x, y) -> {
			if (x == identityX)
				return y;
			if (y == identityX)
				return x;
			return mergeX.apply(x, y);
		};
		this.mergeAX = (x, y) -> {
			if (x == identityA)
				return y;
			return mergeAX.apply(x, y);
		};
	}
	
	/**
	 * arr.length < N　でも可。
	 * @param arr
	 */
	public void build(Acted[] arr) {
		for (int i = 0; i < arr.length; ++i) {
			v[id(i, i + 1)] = arr[i];
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = mergeX.apply(v[2 * i], v[2 * i + 1]);
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
		v[k] = mergeAX.apply(lazy[k], v[k]);
		if (2 * k + 1 < v.length) {
			lazy[2 * k] = mergeA.apply(lazy[k], lazy[2 * k]);
			lazy[2 * k + 1] = mergeA.apply(lazy[k], lazy[2 * k + 1]);
		}
		lazy[k] = identityA;
	}
	
	public Acted get(int a) {
		return fold(a, a+1);
	}

	public Acted fold(int a, int b) {
		//[1         ]
		//[2   ][3   ]
		//[4][5][6][7]
		a=Math.max(a, 0);
		b=Math.min(b, inputN);
		if(a>=b)return identityX;
		a+=n;
		b+=n;
		for (int i = LOG; i >= 0; i--) {
			push((a>>i));
			push(((b-1)>>i));
		}
		var l=identityX;
		var r=identityX;
		while(a<b) {
			if(a%2==1) {
				if(lazy[a]!=identityA)push(a);
				l=mergeX.apply(l, v[a++]);
			}
			if(b%2==1) {
				if(lazy[b-1]!=identityA)push(b-1);
				r=mergeX.apply(v[--b], r);
			}
			a/=2;
			b/=2;
		}
		return mergeX.apply(l, r);
	}
	
	Acted fold(int l, int r, int a, int b, int k) {
        push(k);
		if ((a <= l) && (r <= b)) {
			return v[k];
        } else if (r <= a || b <= l) {
            return identityX;
        } else {
        	push(2*k);
    		push(2*k+1);
        	int m = (l + r) / 2;
            Acted vl = fold(l, m, a, b, 2 * k);
            Acted vr = fold(m, r, a, b, 2 * k + 1);
            return mergeX.apply(vl, vr);
        }
    }

	
	public void act(int a, int b, Acting add) {
		act(0, n, a, b, root, add);
	}
	
	public void mergeX(int id, Acted add) {
		mergeX(0, n, id, root, add);
	}
	
	void mergeX(int l, int r, int id, int k, Acted val) {
		if (r - l == 1) {
			if (l != id) throw new AssertionError();
			v[k] = mergeX.apply(v[k], val);
			return;
		}
		push(k);
		push(2*k);
		push(2*k+1);
		int m = (l + r) / 2;
		if (l <= id && id < m) mergeX(l, m, id, 2 * k, val);
		else mergeX(m, r, id, 2 * k + 1, val);
		v[k] = mergeX.apply(v[2 * k], v[2 * k + 1]);
	}
	
	
	public void set(int id, Acted add) {
		set(0, n, id, root, add);
	}
	
	void set(int l, int r, int id, int k, Acted val) {
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
		v[k] = mergeX.apply(v[2 * k], v[2 * k + 1]);
	}


	void act(int l, int r, int a, int b, int k, Acting add) {
		if (a <= l && r <= b) {
			lazy[k] = mergeA.apply(add, lazy[k]);
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
			v[k] = mergeX.apply(v[2 * k], v[2 * k + 1]);
			return;
		}
	}
	
    public Acted actThenFold(int a, int b, Acting add) {
        return actThenFold(0, n, a, b, root, add);
    }

    Acted actThenFold(int l, int r, int a, int b, int k, Acting add) {
        if ((a <= l) && (r <= b)) {
            lazy[k] = mergeA.apply(add, lazy[k]);
        }
        push(k);
        if ((a <= l) && (r <= b)) {
            return v[k];
        } else if ((r <= a) || (b <= l)) {
            return identityX;
        } else {
            int m = (l + r) / 2;
            Acted vl = actThenFold(l, m, a, b, 2 * k, add);
            Acted vr = actThenFold(m, r, a, b, (2 * k) + 1, add);
            v[k] = mergeX.apply(v[2 * k], v[(2 * k) + 1]);
            return mergeX.apply(vl, vr);
        }
    }

	

	int id(int a, int b) {
		int w = Integer.lowestOneBit(a ^ b);
		return n / w + a / w;
	}
	
	public Acted foldAll() {
		if (lazy[root]!=identityA) {
			push(root);
			v[root] = mergeX.apply(v[2 * root], v[2 * root + 1]);
		}
		return v[root];
	}

	@Override
	public String toString() {
		String ret = "";
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				var x=v[id(i, i + w)];
				if (x == identityX) {
					ret += "id ";
				} else if (x instanceof long[]) {
					ret += Arrays.toString((long[]) x) + " ";
				} else {
					ret += x.toString()+" ";
				}
			}
			ret += "\n";
		}
		for (int w = n; w >= 1; w /= 2) {
			for (int i = 0; i < n; i += w) {
				var x=lazy[id(i, i + w)];
				if (x == identityA) {
					ret += "id ";
				} else if (x instanceof long[]) {
					ret += Arrays.toString((long[]) x) + " ";
				} else {
					ret += x.toString()+" ";
				}
			}
			ret += "\n";
		}
		return ret;
	}

	/**
	 * 葉をは並べた配列を返す。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Acted[] getLeafs() {
		for (int i = 0; i < v.length; ++i)
			push(i);
		Acted[] ret = (Acted[]) new Object[n];
		for (int i = 0; i < n; ++i)
			ret[i] = v[id(i, i + 1)];
		return ret;
	}
	
	public Acted identityX() {
		return identityX;
	}
	
	public BinaryOperator<Acted> mergeX() {
		return mergeX;
	}
	
	
	/**
	 * f.test(fold(l, r)) が真となる最大の r を返す。そのような r が存在しなければ l を返す。
	 * fはf(a*b)=f(a)&&f(b)が成り立たなくてはならない。
	 * 未テスト
	 * @param l
	 * @param f
	 * @return
	 */
	public int maximalRight(int l, Predicate<Acted> f) {
		if (l>=n) throw new AssertionError();
		int r = l + 1;
		int id = id(l, r);
		for (int i = MathUtils.floorLog2(id); i >= 0; i--) {
			push(id >> i);
		}
		Acted prd = v[id];
		if (!f.test(prd)) return l;
		while (true) {
			while (id != 1 && id % 2 == 1) id /= 2;
			if (id == 1) return Math.min(r, inputN);
			while (id % 2 == 0) {
				id /= 2;
				push(2 * id + 1);
				var nprd = mergeX.apply(prd, v[2 * id + 1]);
				if (f.test(nprd)) {
					r = rFromId(2 * id + 1);
					prd = nprd;
				} else {
					id = 2 * id + 1;
					while (2 * id < v.length) {
						push(id);
						push(2 * id);
						nprd = mergeX.apply(prd, v[2 * id]);
						if (f.test(nprd)) {
							prd = nprd;
							r = rFromId(2 * id);
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

	
	int rFromId(int id) {
		int w = n / Integer.highestOneBit(id);
		int a = (id - Integer.highestOneBit(id)) * w;
		return a+w;
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
		System.out.println("LazySegTree { v: " + java.util.Arrays.toString(v) + ", lazy: " + java.util.Arrays.toString(lazy) + " }");
	}
}
