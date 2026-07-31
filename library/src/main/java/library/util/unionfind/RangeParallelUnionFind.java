package library.util.unionfind;

import library.util.MathUtils;

public class RangeParallelUnionFind {
	//https://judge.yosupo.jp/submission/357773
	UnionFind[] uf;
	
	public RangeParallelUnionFind(int N) {
		int log=MathUtils.floorLog2(N);
		uf=new UnionFind[log+1];
		for (int i = 0; i < uf.length; i++) {
			uf[i]=new UnionFind(N);
		}
	}
	
	/**
	 * 各i=0,..,w-1についてa+iとb+iを結ぶ
	 * @param a
	 * @param b
	 * @param w
	 */
	public void union(int a, int b, int w) {
		if (w == 0) return;
		if (a > b) {
			union(b, a, w);
			return;
		}
		int d=Integer.highestOneBit(w);
		int height = MathUtils.floorLog2(w);
		recUnion(a, b, height);
		recUnion(a + w - d, b + w - d, height);
	}
	
	void recUnion(int a, int b, int height) {
		if(uf[height].equiv(a, b)) return;
		
		uf[height].union(a, b);
		
		if (height != 0) {
			int w = (1 << height) / 2;
			recUnion(a, b, height - 1);
			recUnion(a + w, b + w, height - 1);
		}
	}
	
	public boolean equiv(int a, int b) {
		return uf[0].equiv(a, b);
	}

	/**
	 * RangeParallelUnionFindの現在の底（height=0の最下層のUnionFind）の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 最下層のUnionFindの連結成分ごとに要素を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(N \alpha(N))$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		return uf[0].toString();
	}

	/**
	 * RangeParallelUnionFindの現在の底（height=0 of の最下層のUnionFind）の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 最下層のUnionFindの連結成分ごとに要素を括弧で括って出力する。</li>
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
