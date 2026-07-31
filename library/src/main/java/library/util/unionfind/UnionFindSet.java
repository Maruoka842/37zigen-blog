package library.util.unionfind;

import java.util.HashMap;
import java.util.Map;

/**
 * parentをMapで持つUnionFind。孤立点の情報は保持していない。
 * @param <T>
 */
public class UnionFindSet<T> {
	
	Map<T, T> parent;
	
	public UnionFindSet() {
		parent=new HashMap<>();
	}
	
	public T root(T x) {
		if (isRoot(x)) {
			return x;
		} else {
			T r = root(parent.get(x));
			parent.put(x, r);
			return r;
		}
	}
	
	public boolean isRoot(T x) {
		return !parent.containsKey(x);
	}
	
	public void union(T x, T y) {
		x=root(x);y=root(y);
		if(x.equals(y))return;
		parent.put(x, y);
	}
	
	public boolean equiv(T x, T y) {
		return root(x).equals(root(y));
	}
	
	/**
	 * マージテクはせず、愚直にマージする。
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> UnionFindSet<T> merge(UnionFindSet<T> a, UnionFindSet<T> b) {
		UnionFindSet<T> c=new UnionFindSet<>();
		for (var es:a.parent.entrySet()) {
			c.union(es.getKey(), es.getValue());
		}
		for (var es:b.parent.entrySet()) {
			c.union(es.getKey(), es.getValue());
		}
		return c;
	}

	/**
	 * UnionFindSetの現在の状態を、連結成分ごとに文字列として表す。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括った文字列を返す。</li>
	 *   <li>計算量: $O(U \alpha(U))$、ただし $U$ は登録されている要素数。</li>
	 *   <li>破壊的変更: 経路圧縮を伴う。</li>
	 * </ul>
	 * @return 連結成分ごとの文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		java.util.Set<T> elements = new java.util.HashSet<>(parent.keySet());
		elements.addAll(parent.values());
		java.util.Map<T, java.util.List<T>> groups = new java.util.HashMap<>();
		for (T x : elements) {
			T r = root(x);
			groups.computeIfAbsent(r, k -> new java.util.ArrayList<>()).add(x);
		}
		StringBuilder sb = new StringBuilder();
		for (var entry : groups.entrySet()) {
			sb.append("{");
			java.util.List<T> list = entry.getValue();
			for (int j = 0; j < list.size(); j++) {
				sb.append(list.get(j));
				if (j < list.size() - 1) sb.append(", ");
			}
			sb.append("}");
		}
		return sb.toString();
	}

	/**
	 * UnionFindSetの現在の状態を、連結成分ごとに標準出力へ出力する。
	 * <ul>
	 *   <li>事前条件: 特になし。</li>
	 *   <li>事後条件: 連結成分ごとに要素を括弧で括って出力する。</li>
	 *   <li>副作用: 標準出力への出力。</li>
	 *   <li>計算量: $O(U \alpha(U))$、ただし $U$ は登録されている要素数。</li>
	 *   <li>破壊的変更: 経路圧縮を伴う。</li>
	 * </ul>
	 * 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
	
}