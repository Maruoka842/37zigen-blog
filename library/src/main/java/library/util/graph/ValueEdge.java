package library.util.graph;

/**
 * 辺の重みを型 T で持つ有向辺を表すレコードです。
 * @param <T> 辺の重みの型
 * @param src 始点
 * @param dst 終点
 * @param weight 辺の重み
 */
public record ValueEdge<T>(int src, int dst, T weight) {
}
