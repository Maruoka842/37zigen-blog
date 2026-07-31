package library.util.segtree;

public interface LazySegTreeStrategy<Acting, Acted> {
    Acted identityX();
    
    Acted mergeX(Acted a, Acted b);
    Acting identityA();
    /**
     * newer ∘ older
     * (older を先に、newer を後に適用)
     */
    Acting mergeA(Acting newer, Acting older);
    Acted mergeAX(Acting a, Acted x);
}
