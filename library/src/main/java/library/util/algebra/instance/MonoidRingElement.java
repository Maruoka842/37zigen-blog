package library.util.algebra.instance;

import java.util.Map;

import library.util.algebra.strategy.MonoidRingStrategy;

/**
 * モノイド環 R[M] の元を表す抽象クラス。
 * R は係数環、M はモノイドである。
 *
 * @param <R> 係数環の型
 * @param <M> モノイドの型
 * @param <E> 具象クラスの型 (CRTP)
 */
public abstract class MonoidRingElement<R, M, E extends MonoidRingElement<R, M, E>> extends FreeModuleElement<R, M, E>
 implements RingElement<E> {

	protected final MonoidRingStrategy<R, M, E> parent;
    public final MonoidRingStrategy<R, M, E> parent() {
        return parent;
    }
    
    public boolean isZero() {
    	return parent.isZero(self());
    }
    
    public boolean isOne() {
    	return parent.isOne(self());
    }
    
    protected MonoidRingElement(
            Map<M, R> val,
            MonoidRingStrategy<R, M, E> parent) {
        super(val, parent);
        this.parent = parent;
    }

	/**
	 * 項の写像から具象クラスのインスタンスを構築する。
	 * @param val 項の写像
	 * @return 具象クラスのインスタンス
	 */
	protected abstract E fromMap(Map<M, R> val);


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MonoidRingElement<?, ?, ?> other = (MonoidRingElement<?, ?, ?>) obj;

        return parent == other.parent && val.equals(other.val);
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(parent) + val.hashCode();
    }
}
