package library.util.unionfind;

import java.util.ArrayList;
/**
 * https://atcoder.jp/contests/abc349/submissions/72460902
 */
public class StaticRangeParallelUnionFind {
	
	UnionFind uf;
	ArrayList<int[]>[]list;
	boolean built=false;
	
	public StaticRangeParallelUnionFind(int N) {
		uf=new UnionFind(N);
		list=new ArrayList[N + 1];
		for (int i = 0; i < list.length; i++) {
			list[i]=new ArrayList<>();
		}
	}
	
	/**
	 * 区間 [a, a+w), [b, b+w) の各位置を対応させて union する操作を登録する。
	 * @param a
	 * @param b
	 * @param w
	 */
	public void union(int a, int b, int w) {
		built=false;
		if(a==b || w == 0)return;
		w=Math.min(w, uf.size());
		list[w].add(new int[] {a, b});
	}
	
	public void union(int a, int b) {
		if(!built)throw new AssertionError();
		uf.union(a, b);
	}
	
	public void build() {
		for (int i = uf.size(); i >= 1; i--) {
			for (var ab : list[i]) {
				int a=ab[0];
				int b=ab[1];
				if(uf.equiv(a, b)) continue;
				uf.union(a, b);
				a++;b++;
				if(a < uf.size() && b < uf.size() && i != 1) {
					list[i - 1].add(new int[] {a, b});
				}
			}
		}
		built=true;
	}
	
	public boolean equiv(int a, int b) {
		return uf.equiv(a, b);
	}
	
	public int root(int a) {
		return uf.root(a);
	}

	/**
	 * StaticRangeParallelUnionFindの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 内部のUnionFindの状態を連結成分ごとに要素を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return uf.toString();
	}

	/**
	 * StaticRangeParallelUnionFindの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 内部のUnionFindの状態を連結成分ごとに要素を括弧で括って出力する。</li>
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