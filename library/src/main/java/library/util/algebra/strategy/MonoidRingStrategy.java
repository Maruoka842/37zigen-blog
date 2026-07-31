package library.util.algebra.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import library.util.algebra.instance.FreeModuleElement;
import library.util.algebra.instance.MonoidRingElement;
import library.util.algebra.strategy.monoid.MonoidStrategy;

/**
 * モノイド環 R[M] の代数的構造を定義するストラテジ。 R は係数環、M はモノイドである。 形式的な和 Σ r_i m_i (r_i ∈ R, m_i
 * ∈ M) を Map&lt;M, R&gt; で表現する。 係数が R.zero() である項は Map から除外される。 加法構造は基底 M
 * を持つ自由加群 R^(M) とし、乗法は M の積で畳み込む。 形式和 x = Σ_{m ∈ M} x_m m は有限台写像 x: M -> R
 * とし、x_m = 0_R の項は Map から除外する。
 *
 * @param <R> 係数環の型
 * @param <M> モノイドの型
 */
public abstract class MonoidRingStrategy<R, M, E extends MonoidRingElement<R, M, E>> extends FreeModuleStrategy<R, M, E>
		implements RingStrategy<E> {
	/** モノイド M の演算ストラテジ。 */
	protected final MonoidStrategy<M> monoid;

	public MonoidRingStrategy(RingStrategy<R> ring, MonoidStrategy<M> monoid) {
		super(ring);
		this.monoid = monoid;
	}
	
	public E one() {
       return create(Collections.singletonMap(monoid.identity(), ring.one()));
	}
	
    @Override
    public E mul(E a, E b) {
        Map<M, R> res = new HashMap<>();

        for (Map.Entry<M, R> ea : a.val().entrySet()) {
            for (Map.Entry<M, R> eb : b.val().entrySet()) {
                M m = monoid.mul(ea.getKey(), eb.getKey());
                R r = ring.mul(ea.getValue(), eb.getValue());

                if (ring.isZero(r)) {
                    continue;
                }

                R old = res.get(m);

                if (old == null) {
                    res.put(m, r);
                } else {
                    R sum = ring.add(old, r);
                    if (ring.isZero(sum)) {
                        res.remove(m);
                    } else {
                        res.put(m, sum);
                    }
                }
            }
        }

        return create(Collections.unmodifiableMap(res));
    }
	
	
	
}
