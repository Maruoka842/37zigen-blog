package library.util.collections;
import java.util.HashSet;

import library.util.Ints;

//https://atcoder.jp/contests/abc303/submissions/73660110
public class IntPairHashSet extends HashSet<Long>{
	
	
	public boolean add(int a, int b) {
		return add(Ints.pack(a, b));
	}
	
	public boolean contains(int a, int b) {
		return contains(Ints.pack(a, b));
	}
	
	public boolean remove(int a, int b) {
		return remove(Ints.pack(a, b));
	}

	/**
	 * このセットと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntPairHashSet)) return false;
		return super.equals(o);
	}

	/**
	 * このセットのハッシュコード値を返します。
	 * @return ハッシュコード値
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * 内部状態を文字列として表現します。
	 *
	 * <p>計算量: $O(N)$（$N$ は格納されている要素数）</p>
	 *
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		if (isEmpty()) return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		boolean first = true;
		for (long key : this) {
			if (!first) sb.append(", ");
			first = false;
			int a = Ints.unpack(key, true);
			int b = Ints.unpack(key, false);
			sb.append("(").append(a).append(", ").append(b).append(")");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * デバッグ用にセットの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
}
