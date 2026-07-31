package library.util.geometry;

/**
 * 点が円盤の内部、境界上、外部のどこに位置するかを表す列挙型。
 * CGAL::Bounded_side に対応します。
 */
public enum BoundedSide {
    /**
     * 円盤の内部（ON_BOUNDED_SIDE）。
     */
    ON_BOUNDED_SIDE,

    /**
     * 円盤の境界（円周）上（ON_BOUNDARY）。
     */
    ON_BOUNDARY,

    /**
     * 円盤の外部（ON_UNBOUNDED_SIDE）。
     */
    ON_UNBOUNDED_SIDE
}
