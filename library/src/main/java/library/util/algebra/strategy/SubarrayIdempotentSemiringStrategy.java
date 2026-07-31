package library.util.algebra.strategy;

public final class SubarrayIdempotentSemiringStrategy<S> {
//https://atcoder.jp/contests/past22-open/submissions/74950328
	private final IdempotentSemiringStrategy<S> sr;

    public SubarrayIdempotentSemiringStrategy(IdempotentSemiringStrategy<S> sr) {
        this.sr = sr;
    }

    public static final class Node<S> {
        public final S prefix;
        public final S suffix;
        public final S best;
        public final S whole;

        public Node(S prefix, S suffix, S best, S whole) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.best = best;
            this.whole = whole;
        }

        @Override
        public String toString() {
            return "{prefix=" + prefix
                    + ", suffix=" + suffix
                    + ", best=" + best
                    + ", whole=" + whole + "}";
        }
    }

    /**
     * 1 要素区間を作る。
     */
    public Node<S> singleton(S value) {
        return new Node<>(value, value, value, value);
    }

    /**
     * 空区間ノード。
     *
     * whole = one
     * prefix/suffix/best = zero
     *
     * これで merge(identity, x) = x, merge(x, identity) = x になる。
     */
    public Node<S> identity() {
        return new Node<>(sr.zero(), sr.zero(), sr.zero(), sr.one());
    }

    /**
     * 区間 [L..M) と [M..R) をマージする。
     */
    public Node<S> mul(Node<S> left, Node<S> right) {
        S whole = sr.mul(left.whole, right.whole);
        S prefix = sr.add(left.prefix, sr.mul(left.whole, right.prefix));
        S suffix = sr.add(right.suffix, sr.mul(left.suffix, right.whole));
        S best = sr.add(
                sr.add(left.best, right.best),
                sr.mul(left.suffix, right.prefix)
        );
        return new Node<>(prefix, suffix, best, whole);
    }

}
