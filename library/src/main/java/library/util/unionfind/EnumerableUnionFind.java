package library.util.unionfind;

import java.util.ArrayList;
import java.util.Arrays;

import library.util.ArrayUtils;

public class EnumerableUnionFind extends UnionFind {
	int[] next;
	
	public EnumerableUnionFind(int n) {
		super(n);
		next = new int[n];
		Arrays.setAll(next, i->i);
	}
	
	public void union(int x, int y) {
		if (!super.equiv(x, y)) {
			ArrayUtils.swap(x, y, next);
		}
		super.union(x, y);
	}
	
	public ArrayList<Integer> component(int x) {
		ArrayList<Integer> ret=new ArrayList<>();
		int from = x;
		do {
			ret.add(x);
			x = next[x];
		} while (x != from);
		return ret;
	}

	/**
	 * EnumerableUnionFindの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		int n = parent.length;
		boolean[] visited = new boolean[n];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			if (!visited[i]) {
				sb.append("{");
				int curr = i;
				boolean first = true;
				do {
					if (!first) sb.append(", ");
					sb.append(curr);
					visited[curr] = true;
					curr = next[curr];
					first = false;
				} while (curr != i);
				sb.append("}");
			}
		}
		return sb.toString();
	}

	/**
	 * EnumerableUnionFindの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括って出力する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(N)$</li>
	 *   <li>破壊的変更: なし。</li>
	 * </ul>
	 * 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
	
}
