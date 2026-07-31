package library.util.collections;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.IntFunction;

public class HashMultiSet<E> {
    private final HashMap<E, Long> map;
    public HashMultiSet() {
    	map = new HashMap<>();
	}
    
    /***
     * repeatは負（削除）でもよいが、操作後の個数が負になるならerror
     * @param element
     * @param repeat
     */
    public void add(E element, long repeat) {
    	map.compute(element, (k, v) -> {
    		long num=(v==null?0:v)+repeat;
    		if (num < 0) throw new AssertionError();
    		return num==0?null:num;
    	});
    }
    
	public void add(E element) {
		add(element, 1);
	}

    
    public void remove(E element) {
    	remove(element, 1);
    }
    
    public void remove(E element, long repeat) {
    	long num = map.getOrDefault(element, 0L) - repeat;
    	if (num < 0) throw new AssertionError();
    	if (num == 0) map.remove(element);
    	else map.put(element, num);
    }
    
    public boolean contains(E element) {
    	return map.containsKey(element);
    }
    
    public Set<Entry<E, Long>> entrySet() {
    	return map.entrySet();
    }
    
    public int size() {
    	return map.size();
    }
    
    public void addAll(TreeMultiSet<E> set) {
    	for (var es : set.entrySet()) {
    		add(es.getKey(),es.getValue());
    	}
    }
    
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    
    public E[] toArray(IntFunction<E[]> generator) {
    	int sz = 0;
    	for (var es : map.entrySet()) sz += es.getValue();
    	E[] arr = generator.apply(sz);
    	int idx = 0;
    	for (var es : map.entrySet()) {
    		for (int i = 0; i < es.getValue(); i++) {
				arr[idx++] = es.getKey();
			}
    	}
    	return arr;
    }
    
    public long getValue(E element) {
    	return map.getOrDefault(element, 0L);
    }

    /**
     * このマルチセットと別のオブジェクトの同値性を判定します。
     * 各要素の出現回数が一致する場合に同値とみなします。
     *
     * <p>計算量: $O(N)$（$N$ は要素の種類数）</p>
     *
     * @param obj 比較対象のオブジェクト
     * @return 同値であれば true, そうでなければ false
     */
    // 未テスト
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HashMultiSet)) return false;
        HashMultiSet<?> other = (HashMultiSet<?>) obj;
        return this.map.equals(other.map);
    }

    /**
     * このマルチセットのハッシュコードを計算します。
     *
     * <p>計算量: $O(N)$（$N$ は要素の種類数）</p>
     *
     * @return ハッシュコード
     */
    // 未テスト
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * 集合の状態を文字列として表す。
     * <ul>
     *   <li>事前条件: 特になし。</li>
     *   <li>事後条件: 特になし。</li>
     *   <li>計算量: $O(N)$</li>
     *   <li>破壊的変更: なし。</li>
     * </ul>
     * @return 集合の状態を表す文字列
     */
    // 未テスト
    @Override
    public String toString() {
	if (isEmpty()) {
		return "空集合";
	} else {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (var entry : entrySet()) {
			if (!first) sb.append("\n");
			sb.append(entry.getKey()).append(" が ").append(entry.getValue()).append("個");
			first = false;
		}
		return sb.toString();
	}
    }

    /**
     * デバッグ用に集合の内容を標準出力に出力する。
     * $O(N)$
     */
    // 未テスト
    public void dump() {
	System.out.println(toString());
    }
}
