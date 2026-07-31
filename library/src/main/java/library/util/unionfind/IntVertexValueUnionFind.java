package library.util.unionfind;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

public class IntVertexValueUnionFind {
	int[] parent;
	int[] vertexValues;
    IntBinaryOperator op;
    int numberConnectedComponents;
	
	@SuppressWarnings("unchecked")
	public IntVertexValueUnionFind(int n, IntBinaryOperator op) {
		parent=new int[n];
		vertexValues = new int[n];
		Arrays.fill(parent, -1);
		this.op  = op;
	}
	
	public int root(int x) {
		return parent[x] < 0 ? x : (parent[x]=root(parent[x]));
	}
	
	public boolean isRoot(int x) {
		return parent[x] < 0;
	}
	
	public void union(int left, int right) {
		left=root(left);right=root(right);
		if(left==right)return;
		if (-parent[right]>-parent[left]) {
			parent[right]+=parent[left];
			parent[left]=right;
			vertexValues[right] = op.applyAsInt(vertexValues[left], vertexValues[right]);
		} else {
			parent[left]+=parent[right];
			parent[right]=left;
			vertexValues[left] = op.applyAsInt(vertexValues[left], vertexValues[right]);
		}
		--numberConnectedComponents;
	}
	
	/***
	 * 初期状態ではidentityが入っている。root(v)にtを右から掛ける。
	 * @param v
	 * @param t
	 */
	public void mulRight(int v, int t) {
		vertexValues[root(v)] = op.applyAsInt(getVertexValue(root(v)), t);
	}
	

	/***
	 * 初期状態ではidentityが入っている。root(v)にtを左から掛ける。
	 * @param v
	 * @param t
	 */public void mulLeft(int v, int t) {
		vertexValues[root(v)] = op.applyAsInt(getVertexValue(root(v)), t);
	}
	
	/**
	 * root(v)にtを代入
	 * @param v
	 * @param t
	 */
	public void set(int v, int t) {
		v=root(v);
		vertexValues[root(v)] = t;
	}
	
	public int getVertexValue(int x) {
		return vertexValues[root(x)];
	}
	
	public boolean equiv(int x, int y) {
		return root(x)==root(y);
	}
	
	public int size(int x) {
		return -parent[root(x)];
	}
	
	public int[] roots() {
		int size = 0;
		for (int i = 0; i < parent.length; i++) {
			if (isRoot(i)) size++;
		}
		int pointer = 0;
		int[]ret=new int[size];
		for (int i = 0; i < parent.length; i++) {
			if (isRoot(i)) ret[pointer++] = i;
		}
		return ret;
	}
	
	public int numberConnectedComponents() {
		return numberConnectedComponents;
	}

	/**
	 * IntVertexValueUnionFindの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列と連結成分のrootの持つ値を併記した文字列を返す。</li>
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
					sb.append(groups[i][j]);
					if (j < groups[i].length - 1) sb.append(", ");
				}
				sb.append("}(").append(getVertexValue(i)).append(")");
			}
		}
		return sb.toString();
	}

	/**
	 * IntVertexValueUnionFindの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括って出力し、連結成分のrootの持つ値を併記する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}

}
