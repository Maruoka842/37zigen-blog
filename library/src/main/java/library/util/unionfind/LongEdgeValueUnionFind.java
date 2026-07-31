package library.util.unionfind;

import java.util.Arrays;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import library.util.algebra.strategy.longs.LongGroupStrategy;

public class LongEdgeValueUnionFind extends UnionFind {
	long[] v;
	LongGroupStrategy st;
	
	public LongEdgeValueUnionFind(int n, LongGroupStrategy st) {
		super(n);
		v = new long[n];
		Arrays.fill(v, st.identity());
		this.st = st;
	}
	
	public int root(int x) {
		if (parent[x] < 0) return x;
		else {
			int par = parent[x];
			int r = root(par);
			v[x] = st.mul(v[par], v[x]);
			return parent[x]=r;
		}
	}
	
	public boolean isRoot(int x) {
		return parent[x] < 0;
	}
	
	public boolean equiv(int x, int y) {
		return root(x) == root(y);
	}
	
	/***
	 * 辺重みvによるroot-aパスの重みAとroot-bパスの重みBについて
	 * A = B * w となるように v[root(a)] を定め、root(b)をunion後のrootに。そのような v が存在すれば true, しなければ false を返す。
	 * A ≤ B + w を扱いたい場合は最短路双対へ。
	 * @param a
	 * @param b
	 * @param w
	 * @return
	 */
	public boolean union(int a, int b, long w) {
		int rootA=root(a);
		int rootB=root(b);
		if (rootA==rootB) {
			return v[a] == st.mul(v[b], w);
		}
		v[rootA] = st.mul(st.mul(v[b], w), st.inverse(v[a]));//v[a] * v[rootA] == v[b] * w
		parent[rootB]+=parent[rootA];
		parent[rootA]=rootB;
		numberConnectedComponents++;
		return true;
	}
	
	/**
	 * root-iパスの重みを返す(rootの重みは任意性があるが、identityとする)。
	 * @param i
	 * @return
	 */
	public long getValue(int i) {
		root(i);
		return v[i];
	}

	/**
	 * LongEdgeValueUnionFindの現在の状態と各要素の重みを、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素と重み（rootからの累積重み）を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		int n = parent.length;
		int[] root = new int[n];
		for (int i = 0; i < n; i++) root[i] = root(i);
		int[] count = new int[n];
		for (int i = 0; i < n; i++) count[root[i]]++;
		int[][] groups = new int[n][];
		for (int i = 0; i < n; i++) if (count[i] > 0) groups[i] = new int[count[i]];
		int[] ptr = new int[n];
		for (int i = 0; i < n; i++) groups[root[i]][ptr[root[i]]++] = i;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			if (groups[i] != null) {
				sb.append("{");
				for (int j = 0; j < groups[i].length; j++) {
					int u = groups[i][j];
					sb.append(u).append("(").append(getValue(u)).append(")");
					if (j < groups[i].length - 1) sb.append(", ");
				}
				sb.append("}");
			}
		}
		return sb.toString();
	}

	/**
	 * LongEdgeValueUnionFindの現在の状態と各要素の重みを、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素と重み（rootからの累積重み）を括弧で括って出力する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
	
	public void draw() {
        System.setProperty("org.graphstream.ui", "swing");
		org.graphstream.graph.Graph graph = new SingleGraph("Graph");
		for (int i = 0; i < parent.length; ++i) {
			Node node = graph.addNode(String.valueOf(i));
	        String label = String.format("%d", i);
	        node.setAttribute("ui.label", label);
		}
		for (int i = 0; i < parent.length; ++i) {
	        if (parent[i] >= 0) {
			graph.addEdge(i+":"+parent[i], i, parent[i], true).setAttribute("ui.label", v[i]);
	        }
		}
        graph.setAttribute("ui.stylesheet",
		    "node { " +
		    "   fill-color: lightblue; " +
		    "   size: 30px; " +
		    "   text-alignment: center; " +
		    "   text-size: 20; " +
		    "   text-color: black; " +
		    "   text-mode: normal;" +
		    "}"
		    +
		    "edge { " +
		    "   text-size: 20px; " +
		    "   text-alignment: above; " +
		    "} "
		);
        graph.display();
	}

}
