package library.util.segtree;

public interface IndexedLazySegTreeStrategy_longlong {
	long identityX();
    
	long mergeX(long a, long b, int l, int m, int r);
	long identityA();
    /**
     * newer ∘ older
     * (older を先に、newer を後に適用)
     */
	long mergeA(long newer, long older);
	long mergeAX(long a, long x, int l, int r);
}
