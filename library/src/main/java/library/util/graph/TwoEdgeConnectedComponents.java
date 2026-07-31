package library.util.graph;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import library.util.collections.ArrayLists;
import library.util.collections.IntArrayList;
import library.util.graph.tree.Tree;

/**
 * 無向グラフを二重辺連結成分（2-edge-connected components）に分解するライブラリ。
 * 二重辺連結成分とは、どの1本の辺を削除しても連結性が保たれる極大な部分グラフのことである。
 * グラフが非連結な場合も、各連結成分ごとに分解を行う。
 * {@link Graph#bridges()} の仕様に基づき、単純グラフ（多重辺がないグラフ）であることを仮定している。
 */
public class TwoEdgeConnectedComponents {
	private final Graph g;
	private final int[] componentIds;
	private final List<Integer>[] components;
	private final List<int[]> bridges;
	private final int componentCount;

	/**
	 * 与えられたグラフを二重辺連結成分に分解する。
	 * <ul>
	 *   <li>時間計算量: $O(N + M)$</li>
	 *   <li>空間計算量: $O(N + M)$</li>
	 * </ul>
	 * @param g 分解対象の無向グラフ
	 */
	@SuppressWarnings("unchecked")
	public TwoEdgeConnectedComponents(Graph g) {
		this.g = g;
		this.bridges = g.bridges();

		IntArrayList[] bridgeAdj = new IntArrayList[g.N];
		for (int i = 0; i < g.N; i++) bridgeAdj[i] = new IntArrayList();
		for (int[] b : bridges) {
			bridgeAdj[b[0]].add(b[1]);
			bridgeAdj[b[1]].add(b[0]);
		}

		IntArrayList[] cleanAdj = new IntArrayList[g.N];
		for (int i = 0; i < g.N; i++) cleanAdj[i] = new IntArrayList();
		boolean[] isBridgeNeighbor = new boolean[g.N];
		for (int v = 0; v < g.N; v++) {
			for (int j = 0; j < bridgeAdj[v].size(); j++) {
				isBridgeNeighbor[bridgeAdj[v].get(j)] = true;
			}
			for (int j = 0; j < g.adj[v].size(); j++) {
				int u = g.adj[v].get(j);
				if (!isBridgeNeighbor[u]) {
					cleanAdj[v].add(u);
				}
			}
			for (int j = 0; j < bridgeAdj[v].size(); j++) {
				isBridgeNeighbor[bridgeAdj[v].get(j)] = false;
			}
		}

		this.componentIds = new int[g.N];
		java.util.Arrays.fill(componentIds, -1);
		int count = 0;
		for (int i = 0; i < g.N; i++) {
			if (componentIds[i] == -1) {
				Queue<Integer> que = new ArrayDeque<>();
				que.add(i);
				componentIds[i] = count;
				while (!que.isEmpty()) {
					int v = que.poll();
					for (int j = 0; j < cleanAdj[v].size(); j++) {
						int u = cleanAdj[v].get(j);
						if (componentIds[u] == -1) {
							componentIds[u] = count;
							que.add(u);
						}
					}
				}
				count++;
			}
		}

		this.componentCount = count;
		this.components = ArrayLists.newArrayOfIntArrayLists(componentCount);
		for (int i = 0; i < g.N; i++) {
			components[componentIds[i]].add(i);
		}
	}

	/**
	 * 頂点 v が属する二重辺連結成分の ID を返す。
	 * @param v 頂点番号
	 * @return 成分 ID (0 以上 componentCount 未満)
	 */
	public int getComponentId(int v) {
		return componentIds[v];
	}

	/**
	 * 二重辺連結成分の総数を返す。
	 * @return 成分数
	 */
	public int getComponentCount() {
		return componentCount;
	}

	/**
	 * 指定された ID の成分に含まれる頂点リストを返す。
	 * @param id 成分 ID
	 * @return 頂点リスト
	 */
	public List<Integer> getComponentVertices(int id) {
		return components[id];
	}

	/**
	 * すべての二重辺連結成分の頂点リストの配列を返す。
	 * @return 成分ごとの頂点リスト
	 */
	public List<Integer>[] getComponents() {
		return components;
	}

	/**
	 * グラフに含まれる橋（bridges）のリストを返す。
	 * @return 橋のリスト。各要素は int[] {u, v} (u < v)
	 */
	public List<int[]> getBridges() {
		return bridges;
	}

	/**
	 * 二重辺連結成分を頂点とし、元のグラフの橋を辺とする縮約グラフ（bridge-block tree）を構築して返す。
	 * 元のグラフの各連結成分は、縮約後は1つの木になる。
	 * @return 縮約されたグラフ
	 */
	public Tree bridgeBlockTree() {
		Tree tree = new Tree(componentCount);
		for (int[] b : bridges) {
			tree.addEdge(componentIds[b[0]], componentIds[b[1]]);
		}
		return tree;
	}
}
