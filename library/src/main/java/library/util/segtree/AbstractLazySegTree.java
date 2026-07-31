package library.util.segtree;

import library.util.algebra.instance.MonoidActionElement;
import library.util.algebra.instance.MonoidElement;

/**
 *  null は identity の代わり
 */
public class AbstractLazySegTree <ActingMonoid extends MonoidElement<ActingMonoid>, ActedMonoid extends MonoidElement<ActedMonoid>>{
	int n=1;
	ActedMonoid[] v;
	ActingMonoid[] lazy;
	private MonoidActionElement<ActingMonoid, ActedMonoid> ma;
	
	Object identity = null;
	@SuppressWarnings("unchecked")
	public AbstractLazySegTree(int n, MonoidActionElement<ActingMonoid, ActedMonoid> ma) {
		this.n = 2 * Integer.highestOneBit(n);
		v = (ActedMonoid[]) new MonoidElement[this.n * 2];
		lazy = (ActingMonoid[]) new MonoidElement[this.n * 2];
		this.ma = ma;
	}
	
	@SuppressWarnings("unchecked")
	public void build(ActedMonoid[] arr) {
		for (int i = 0; i < arr.length; ++i) {
			v[id(i, i + 1)] = arr[i];
		}
		for (int i = id(0, 1) - 1; i >= id(0, n); --i) {
			v[i] = merge(v[2*i], v[2 * i + 1]);
		}
	}

    private void push(int k) {
        if (lazy[k] == identity) return;
        v[k] = ma.merge((ActingMonoid) lazy[k], (ActedMonoid) v[k]);
        if (2 * k + 1 < v.length) {
            lazy[2 * k] = mergeLazy(lazy[k], lazy[2 * k]);
            lazy[2 * k + 1] = mergeLazy(lazy[k], lazy[2 * k + 1]);
        }
        lazy[k] = null;
    }

    public ActedMonoid query(int a, int b) {
        return query(a, b, null);
    }

    public ActedMonoid query(int a, int b, ActingMonoid add) {
        return query(0, n, a, b, 1, add);
    }

    private ActedMonoid query(int l, int r, int a, int b, int k, ActingMonoid add) {
        if (a <= l && r <= b && add != null) {
        	lazy[k] = mergeLazy(add, lazy[k]);
        }
        push(k);
        if (a <= l && r <= b) {
            return (ActedMonoid) v[k];
        } else if (r <= a || b <= l) {
            return null;
        } else {
            int m = (l + r) / 2;
            ActedMonoid vl = query(l, m, a, b, 2 * k, add);
            ActedMonoid vr = query(m, r, a, b, 2 * k + 1, add);
            v[k] = merge(v[2 * k], v[2 * k + 1]);
            return merge(vl, vr);
        }
    }

    private ActedMonoid merge(ActedMonoid a, ActedMonoid b) {
        if (a == identity) return b;
        if (b == identity) return a;
        return a.mul(b);
    }

    private ActingMonoid mergeLazy(ActingMonoid a, ActingMonoid b) {
        if (a == identity) return b;
        if (b == identity) return a;
        
        return a.mul(b);
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
                ret += (v[id(i, i + w)] == null ? "id" : v[id(i, i + w)].toString()) + " ";
            }
            ret += "\n";
        }
        for (int w = n; w >= 1; w /= 2) {
            for (int i = 0; i < n; i += w) {
                ret += (lazy[id(i, i + w)] == null ? "id" : lazy[id(i, i + w)].toString()) + " ";
            }
            ret += "\n";
        }
        return ret;
    }

    public ActedMonoid get(int i) {
        return query(i, i + 1);
    }
    
}
