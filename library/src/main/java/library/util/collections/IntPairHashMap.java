package library.util.collections;
import java.util.HashMap;
import java.util.function.BiFunction;

import library.util.Ints;

public class IntPairHashMap<V> extends HashMap<Long, V> {
    public V put(int a, int b, V value) {
        return super.put(Ints.pack(a, b), value);
    }

    public V get(int a, int b) {
        return super.get(Ints.pack(a, b));
    }

    public boolean containsKey(int a, int b) {
        return super.containsKey(Ints.pack(a, b));
    }

    public V remove(int a, int b) {
        return super.remove(Ints.pack(a, b));
    }

    public V getOrDefault(int a, int b, V defaultValue) {
        return super.getOrDefault(Ints.pack(a, b), defaultValue);
    }

    public V putIfAbsent(int a, int b, V value) {
        return super.putIfAbsent(Ints.pack(a, b), value);
    }
    
    public V merge(
            int a,
            int b,
            V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction
    ) {
    	return super.merge(Ints.pack(a, b), value, remappingFunction);
    }

	/**
	 * このマップと指定されたオブジェクトが等価であるか検証します。
	 * @param o 比較対象のオブジェクト
	 * @return 等価であれば true, そうでなければ false
	 * $O(N)$
	 * // 未テスト
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntPairHashMap)) return false;
		return super.equals(o);
	}

	/**
	 * このマップのハッシュコード値を返します。
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
	 * <p>計算量: $O(N)$（$N$ は格納されているエントリ数）</p>
	 *
	 * @return 内部状態の文字列表現
	 */
	// 未テスト
	@Override
	public String toString() {
		if (isEmpty()) return "{}";
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (var entry : entrySet()) {
			if (!first) sb.append(", ");
			first = false;
			int a = Ints.unpack(entry.getKey(), true);
			int b = Ints.unpack(entry.getKey(), false);
			sb.append("(").append(a).append(", ").append(b).append(")=").append(entry.getValue());
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * デバッグ用にマップの内容を標準出力に出力する。
	 * $O(N)$
	 * // 未テスト
	 */
	public void dump() {
		System.out.println(toString());
	}
}
