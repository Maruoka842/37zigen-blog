package library.util.algebra.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import library.util.algebra.instance.FreeModuleElement;

/**
 * 環 R 上の基底 B を持つ自由加群 R^(B) の演算を定義するストラテジ。
 * 元 x は有限台写像 x: B -> R とし、x(b) = 0_R である b は Map から除外する。
 *
 * @param <R> 係数環の型
 * @param <B> 基底の型
 */
public abstract class FreeModuleStrategy<R, B, E extends FreeModuleElement<R, B, E>>{
	/** 係数環 R の演算ストラテジ。 */
	protected final RingStrategy<R> ring;

	public FreeModuleStrategy(RingStrategy<R> ring) {
		this.ring = ring;
	}
	
	protected abstract E create(Map<B, R> normalizedVal);

	public E zero() {
       return create(Collections.emptyMap());
	}

	public E add(E a, E b) {
		return create(add(a.val(), b.val()));
	}

	public Map<B, R> add(Map<B, R> a, Map<B, R> b) {
		if (a == null || b == null) throw new AssertionError();
        Map<B, R> res = new HashMap<>(a);

        for (Map.Entry<B, R> e : b.entrySet()) {
            B basis = e.getKey();
            R c = e.getValue();
            R old = res.get(basis);
            if (old == null) {
            	res.put(basis, c);
            } else {
            	R sum = ring.add(old, c);
            	if (ring.isZero(sum)) {
            		res.remove(basis);
            	} else {
            		res.put(basis, sum);
            	}
            }
        }
        return Collections.unmodifiableMap(res);
	}
	
    public E sub(E a, E b) {
        return create(sub(a.val(), b.val()));
    }

    public Map<B, R> sub(Map<B, R> a, Map<B, R> b) {
	return add(a, neg(b));
    }
	
    public E neg(E a) {
	return create(neg(a.val()));
    }

    public Map<B, R> neg(Map<B, R> a) {
        Map<B, R> res = new HashMap<>();

        for (Map.Entry<B, R> e : a.entrySet()) {
            R c = ring.neg(e.getValue());
            res.put(e.getKey(), c);
        }

        return Collections.unmodifiableMap(res);
    }

    public E scalarMul(R coefficient, E a) {
	return create(scalarMul(coefficient, a.val()));
    }

    public Map<B, R> scalarMul(R coefficient, Map<B, R> a) {
	if (ring.isZero(coefficient)) return Collections.emptyMap();
	Map<B, R> res = new HashMap<>();
	for (Map.Entry<B, R> e : a.entrySet()) {
		R c = ring.mul(coefficient, e.getValue());
		if (!ring.isZero(c)) {
			res.put(e.getKey(), c);
		}
	}
	return Collections.unmodifiableMap(res);
    }

    public boolean isZero(Map<B, R> a) {
	return a.isEmpty();
    }

    public Map<B, R> term(B basis, R coefficient) {
	if (ring.isZero(coefficient)) return Collections.emptyMap();
	return Collections.singletonMap(basis, coefficient);
    }

    /**
     * 指定された基底の係数を返す。
     * 未テスト。
     * 数学的表記: [basis]val。
     * 事前条件: val != null, basis != null。
     * 事後条件: basis が台にないなら 0_R、あるなら対応する係数を返す。
     * 副作用: なし。
     * 計算量: O(log |val|) (Map が TreeMap の場合) または O(1) (Map が HashMap の場合)。
     * 破壊的変更: なし。
     * 参照共有・所有権: 係数が存在する場合は内部係数参照を共有する。
     * 例外・未定義条件: val == null または basis == null のとき NullPointerException。
     * @param val 有限台写像。
     * @param basis 基底。
     * @return basis の係数。
     */
    public R coefficientOf(Map<B, R> val, B basis) {
        R coefficient = val.get(Objects.requireNonNull(basis));
        return (coefficient == null ? ring.zero() : coefficient);
    }
    
    public boolean equals(E a, E b) {
        if (a.val().size() != b.val().size()) {
            return false;
        }

        for (Map.Entry<B, R> e : a.val().entrySet()) {
        	R other = b.val().get(e.getKey());
            if (other == null) {
            	return false;
            }
            if (!ring.equals(e.getValue(), other)) {
                return false;
            }
        }

        return true;
    }
}
