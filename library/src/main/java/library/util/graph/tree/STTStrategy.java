package library.util.graph.tree;

import library.util.graph.*;

/**
 * Static Top Tree 上での動的計画法（Tree DP）の遷移を定義するインターフェース。
 * 頂点 v を根とする部分木を Point クラスター、重分解されたパスのセグメントを Path クラスターとして扱います。
 *
 * @param <Point> Point クラスターの状態を表す型
 * @param <Path> Path クラスターの状態を表す型
 */
public interface STTStrategy<Point, Path> {
	/**
	 * 同じ親を持つ 2 つの軽部分木（Point クラスター）をマージします。
	 * @param l 左側の Point クラスター
	 * @param r 右側の Point クラスター
	 * @return マージされた Point クラスター
	 */
	public Point mergeVirtualRoot(Point l, Point r);

	/**
	 * 軽辺 (parent, v) を追加し、Path クラスターを Point クラスターに変換します。
	 * 軽辺の下端（軽部分木の根）に関する情報は Path クラスターに含めておく必要があります。
	 * @param d 変換対象の Path クラスター
	 * @return 生成された Point クラスター
	 */
	public Point appendVirtualRoot(Path d);

	/**
	 * 頂点 v と、その子からなる Point クラスター（マージされた軽部分木群）を結合し、Path クラスターを作成します。
	 * @param d Point クラスター。v の子が一つもない場合は null になることがあります。
	 * @param v 結合する頂点インデックス
	 * @return 生成された Path クラスター（通例 top=bottom=v となる）
	 */
	public Path replaceVirtualRoot(Point d, int v);

	/**
	 * 重辺 (u, v) でつながれた 2 つのパスセグメント（Path クラスター）を結合します。
	 * 境界頂点 (u, v) に関する情報は Path クラスターに含めておく必要があります。
	 * @param parent 上側の Path クラスター
	 * @param child 下側の Path クラスター
	 * @return 結合された Path クラスター
	 */
	public Path joinHeavyEdge(Path parent, Path child);

	/**
	 * 単一の頂点 v からなる Path クラスターを作成します。
	 * @param v 頂点インデックス
	 * @return 生成された Path クラスター
	 */
	public Path createVertex(int v);
}
