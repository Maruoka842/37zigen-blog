package library.util.segtree;

public interface LazySegTreeStrategy_longlong {
	long identityX();
    
	long mergeX(long a, long b);
	long identityA();
    /**
     * newer ∘ older
     * (older を先に、newer を後に適用)
     */
	long mergeA(long newer, long older);
	long mergeAX(long a, long x);
}
