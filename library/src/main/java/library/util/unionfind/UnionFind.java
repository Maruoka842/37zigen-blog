package library.util.unionfind;

import java.util.Arrays;

public class UnionFind {
	int[] parent;
	int numberConnectedComponents;
	
	public UnionFind(int n) {
		numberConnectedComponents = n;
		parent=new int[n];
		Arrays.fill(parent, -1);
	}
	
	public int size() {
		return parent.length;
	}
	
	public int root(int x) {
		return parent[x] < 0 ? x : (parent[x]=root(parent[x]));
	}
	
	public boolean isRoot(int x) {
		return parent[x] < 0;
	}
	
	public void union(int x, int y) {
		x=root(x);y=root(y);
		if(x==y)return;
		parent[y]+=parent[x];
		parent[x]=y;
		--numberConnectedComponents;
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
	 * UnionFindの現在の状態をコピーした新しいインスタンスを返す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 元のUnionFindと同じ構造を持つ新しいUnionFindを返す。</li>
	 *   <li>副作用: なし。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 *   <li>参照共有・所有権: 内部の配列を含め、新しくインスタンスが生成される。</li>
	 *   <li>例外: なし。</li>
	 * </ul>
	 * @return UnionFindのコピー
	 */
	public UnionFind copy() {
		UnionFind ret = new UnionFind(parent.length);
		ret.numberConnectedComponents = this.numberConnectedComponents;
		ret.parent = this.parent.clone();
		return ret;
	}
	
	/**
	 * restrictedGrowingFunction。
	 * a[i]=(iと同じ集合に属す最小要素)を返す。
	 * @return
	 */
	public int[] rgf() {
		int[]ret=new int[parent.length];
		int[] minElement=new int[parent.length];
		Arrays.fill(minElement, Integer.MAX_VALUE);
		for (int i = 0; i < parent.length; i++) {
			minElement[root(i)]=Math.min(minElement[root(i)], i);
		}
		for (int i = 0; i < parent.length; i++) {
			ret[i]=minElement[root(i)];
		}
		return ret;
	}
	
	public static UnionFind fromRGF(int[] rgf) {
		UnionFind uf=new UnionFind(rgf.length);
		for (int i = 0; i < rgf.length; i++) {
			uf.union(i, rgf[i]);
		}
		return uf;
	}

	/**
	 * UnionFindの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列を返す。</li>
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
				sb.append("}");
			}
		}
		return sb.toString();
	}

	/**
	 * UnionFindの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括って出力する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 */
	public void dump() {
		System.out.println(toString());
	}
	
}
