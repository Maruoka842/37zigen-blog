package library.util.algebra.instance;

public abstract class MonoidActionElement<A extends MonoidElement<?>, X extends MonoidElement<?>> extends SemigroupActionElement<A, X> {
	public X merge(A a, X b) {
        if (a == null) return b;
        return mergeNonNull(a, b);
    }
    
    protected abstract X mergeNonNull(A a, X b);

    @Override
    public X act(A f, X s) {
        return merge(f, s);
    }
}
